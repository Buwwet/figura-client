package org.figuramc.figura_client.mixin.render;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.util.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CachedOrthoProjectionMatrixBuffer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura_client.ducks.LevelRenderStateAccess;
import org.figuramc.figura_client.renderer.part.FiguraClientPartRenderer;
import org.figuramc.figura_client.renderer.submit.FiguraCallbackSubmit;
import org.figuramc.figura_core.avatars.components.HudRoot;
import org.figuramc.figura_core.manage.AvatarManagers;
import org.figuramc.figura_core.manage.AvatarView;
import org.figuramc.figura_core.script_hooks.Event;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.figura_core.script_hooks.callback.items.FuncView;
import org.figuramc.figura_core.util.data_structures.FiguraTransformStack;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Shadow @Final private LevelRenderState levelRenderState;

    @Unique private FiguraCallbackSubmit clientRenderSubmissions;

    // Run client_render just before pick().
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V"))
    public void client_render(DeltaTracker deltaTracker, CallbackInfo ci) {
        // Run the client_render event on each avatar
        float tickDelta = deltaTracker.getGameTimeDeltaPartialTick(true);
        clientRenderSubmissions = invokeRenderEvent(Event.CLIENT_RENDER, new CallbackItem.F32(tickDelta));
    }

    // Run world_render just before LevelRenderer.renderLevel().
    @Inject(method = "renderLevel", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lnet/minecraft/client/renderer/LevelRenderer;renderLevel(Lcom/mojang/blaze3d/resource/GraphicsResourceAllocator;Lnet/minecraft/client/DeltaTracker;ZLnet/minecraft/client/Camera;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lorg/joml/Vector4f;Z)V"))
    public void world_render(DeltaTracker deltaTracker, CallbackInfo ci) {
        // Run the world_render event on each avatar
        float tickDelta = deltaTracker.getGameTimeDeltaPartialTick(true);
        FiguraCallbackSubmit worldRenderSubmissions = invokeRenderEvent(Event.WORLD_RENDER, new CallbackItem.F32(tickDelta));

        // Store submissions in the LevelRenderState for later
        FiguraCallbackSubmit clientRenderSubmissions = this.clientRenderSubmissions; // Capture
        ((LevelRenderStateAccess) this.levelRenderState).figura_client$setCodeSubmit(() -> {
            clientRenderSubmissions.run();
            worldRenderSubmissions.run();
        });
    }

    // Helper for invoking all render events, having them return callbacks to happen on the render thread
    @Unique private static <Args extends CallbackItem> FiguraCallbackSubmit invokeRenderEvent(Event<Args, CallbackItem.Tuple2<CallbackItem.Optional<FuncView<CallbackItem, CallbackItem.Unit>>, CallbackItem>> renderEvent, Args args) {
        List<Runnable> allCallbacks = new ArrayList<>();
        AvatarManagers.forEachAvatar(avatar -> {
            // Invoke the event and get some render-thread callbacks
            var callbacks = avatar.getEventListener(renderEvent).invokeFor(args);
            if (callbacks.isEmpty()) return;
            AvatarView<?> view = new AvatarView<>(avatar);
            Runnable invokeCallbacks = () -> view.use(renderThreadAvatar -> {
                // Run all the callbacks for this avatar:
                for (var callback : callbacks) {
                    var funcView = callback.a().value();
                    if (funcView == null) continue;
                    var data = callback.b();
                    var func = funcView.getCallback();
                    if (func == null) continue; // Skip if it was revoked (I don't think it *can* be revoked? But we'll check anyway)
                    func.call(data);
                }
            });
            allCallbacks.add(invokeCallbacks);
        });
        return () -> allCallbacks.forEach(Runnable::run);
    }

    // GPU resources we'll need to close

    // Swap near and far so that we can use Positive Z = away from camera like in blockbench. Pain
    @Unique private final CachedOrthoProjectionMatrixBuffer figuraGuiProjectionMatrix
            = new CachedOrthoProjectionMatrixBuffer("Figura GUI Projection Matrix", 1000, -1000, true);
    @Unique private final GpuBuffer figuraGuiLightingBuffer = Util.make(() -> {
        GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "Figura GUI Lighting Buffer", GpuBuffer.USAGE_COPY_DST | GpuBuffer.USAGE_UNIFORM, Lighting.UBO_SIZE);
        Vector3f light0 = new Vector3f(-0.2f, 0.7f, -0.5f);
        Vector3f light1 = new Vector3f(0.2f, 0.7f, -1.0f);
        try (MemoryStack memoryStack = MemoryStack.stackPush()) {
            ByteBuffer byteBuffer = Std140Builder.onStack(memoryStack, Lighting.UBO_SIZE).putVec3(light0).putVec3(light1).get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), byteBuffer);
        }
        return buffer;
    });

    // Render the hud at the very end of the frame
    @Inject(method = "render", at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lcom/mojang/blaze3d/resource/CrossFrameResourcePool;endFrame()V"))
    public void renderHud(DeltaTracker deltaTracker, boolean bl, CallbackInfo ci) {
        // Clear depth
        RenderTarget renderTarget = Minecraft.getInstance().getMainRenderTarget();
        RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(renderTarget.getDepthTexture(), 1.0);
        // Set up projection matrix
        Window window = Minecraft.getInstance().getWindow();
        RenderSystem.setProjectionMatrix(figuraGuiProjectionMatrix.getBuffer(
                (float) window.getWidth() / window.getGuiScale(),
                (float) window.getHeight() / window.getGuiScale()
        ), ProjectionType.ORTHOGRAPHIC);
        // Set up lighting
        RenderSystem.setShaderLights(figuraGuiLightingBuffer.slice());

        // Collect avatars to render the huds of
        List<AvatarView<?>> toRender = new ArrayList<>();
        AvatarView<?> mainHud = AvatarManagers.GUIS.get(AvatarManagers.GuiKind.MAIN_GUI);
        if (mainHud != null) toRender.add(mainHud);
        Entity e = Minecraft.getInstance().getCameraEntity();
        if (e != null) {
            AvatarView<?> cameraEntityHud = AvatarManagers.ENTITIES.get(e.getUUID());
            if (cameraEntityHud != null) toRender.add(cameraEntityHud);
        }

        // Render hud for each avatar in the list
        for (AvatarView<?> avatarView : toRender) {
            avatarView.use(avatar -> {
                HudRoot hudRoot = avatar.getComponent(HudRoot.TYPE);
                if (hudRoot == null) return;
                avatar.tryRenderModelPart(() -> {
                    FiguraClientPartRenderer renderer = (FiguraClientPartRenderer) hudRoot.root.getRenderer();
                    MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                    FiguraTransformStack stack = new FiguraTransformStack();
                    stack.light(new Vector2f(1f, 1f));
                    stack.scale(-1.0f, -1.0f, 1.0f); // Flip X and Y axis
//                        float tickDelta = deltaTracker.getGameTimeDeltaPartialTick(false);
                    renderer.render(bufferSource, stack, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
                    bufferSource.endBatch(); // Ensure we end the batch
                });
            });
        }
    }

    @Inject(method = "close", at = @At("HEAD"))
    public void closeOurThings(CallbackInfo ci) {
        figuraGuiProjectionMatrix.close();
        figuraGuiLightingBuffer.close();
    }

}
