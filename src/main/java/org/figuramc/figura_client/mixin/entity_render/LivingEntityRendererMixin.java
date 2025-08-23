package org.figuramc.figura_client.mixin.entity_render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.figuramc.figura_client.ducks.EntityRenderStateAccess;
import org.figuramc.figura_client.game_data.MinecraftEntityImpl;
import org.figuramc.figura_client.renderer.CompatibleRenderer;
import org.figuramc.figura_core.avatars.components.EntityRoot;
import org.figuramc.figura_core.manage.AvatarManagers;
import org.figuramc.figura_core.manage.AvatarView;
import org.figuramc.figura_core.script_hooks.Event;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.figura_core.script_hooks.callback.items.EntityView;
import org.figuramc.figura_core.util.data_structures.FiguraTransformStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@SuppressWarnings({"unchecked", "rawtypes"})
@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    /**
     * Inject before rendering the vanilla model, to call the entity_render event.
     */
    @Inject(
            method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;)V")
    )
    public void beforeRenderLivingEntity(LivingEntityRenderState renderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, CallbackInfo ci) {
        // Fetch avatar
        LivingEntity livingEntity = (LivingEntity) ((EntityRenderStateAccess) renderState).figura_client$getEntity();
        try (AvatarView<UUID> avatar = AvatarManagers.tryGetEntityAvatar(new MinecraftEntityImpl(livingEntity))) {
            if (avatar == null) return;
            // Call entity_render event
            float tickDelta = ((EntityRenderStateAccess) renderState).figura_client$getTickDelta();
            EntityView<MinecraftEntityImpl> entityView = new EntityView<>(new MinecraftEntityImpl(livingEntity));
            try { avatar.get().runEvent(Event.ENTITY_RENDER, new CallbackItem.Tuple2<>(new CallbackItem.F32(tickDelta), entityView)); }
            finally { entityView.revoke(); }
        }
    }

    /**
     * Inject after rendering the vanilla model, to render our own model.
     * This will have all the necessary transforms of the model applied,
     * but it will also have some ones we don't want:
     * - Flipping the x and y axes. This is not necessary in our renderer,
     *   we try to keep it consistent with Blockbench.
     * - Translating vertically by 1.501.
     *   We choose to place (0,0,0) at the feet of the entity, rather than
     *   the neck of the entity like Minecraft does.
     * Both of these will need to be reverted.
     *
     * The reason we inject after rendering the model is so that mimic
     * parts can be up-to-date. If we render the Figura model before the
     * vanilla model, then mimic parts will be one frame behind.
     *
     * If you need code that runs before the Vanilla model is drawn, try
     * using render callbacks on vanilla parts, particularly Model roots!
     */
    @SuppressWarnings("UnreachableCode")
    @Inject(
            method = "render(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", shift = At.Shift.BEFORE, target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V")
    )
    public void afterRenderLivingEntity(LivingEntityRenderState renderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, CallbackInfo ci) {
        // Fetch avatar
        LivingEntity livingEntity = (LivingEntity) ((EntityRenderStateAccess) renderState).figura_client$getEntity();
        try (AvatarView<UUID> avatar = AvatarManagers.tryGetEntityAvatar(new MinecraftEntityImpl(livingEntity))) {
            if (avatar == null) return;
            EntityRoot root = avatar.get().getComponent(EntityRoot.TYPE);
            if (root != null && root.root.renderer instanceof CompatibleRenderer renderer) {
                FiguraTransformStack matrixStack = new FiguraTransformStack();
                matrixStack.peekPosition().set(poseStack.last().pose());
                matrixStack.peekNormal().set(poseStack.last().normal());

                // Undo the problematic translations above:
                // This has to be 1.500 exactly. NOT 1.501.
                // I have not been able to figure out why,
                // even though I have probably stared at the
                // vanilla source code for entity rendering for weeks in total.
                matrixStack.translate(0, 1.500f, 0);
                matrixStack.scale(-1, -1, 1);
                // Grab the overlay:
                float whiteOverlayProgress = ((LivingEntityRenderer) (Object) this).getWhiteOverlayProgress(renderState);
                int overlayCoords = LivingEntityRenderer.getOverlayCoords(renderState, whiteOverlayProgress);
                // Render
                float tickDelta = ((EntityRenderStateAccess) renderState).figura_client$getTickDelta();

                renderer.setup(multiBufferSource, matrixStack, tickDelta, light, overlayCoords);
                avatar.get().tryRenderModelPart(renderer);
            }
        }
    }
}
