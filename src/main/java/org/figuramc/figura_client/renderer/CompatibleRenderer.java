package org.figuramc.figura_client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.figura_client.textures.MinecraftTextureImpl;
import org.figuramc.figura_client.textures.OwnedMinecraftTextureImpl;
import org.figuramc.figura_core.minecraft_interop.model_part_renderers.FiguraModelPartRenderer;
import org.figuramc.figura_core.minecraft_interop.texture.MinecraftTexture;
import org.figuramc.figura_core.model.part.FiguraModelPart;
import org.figuramc.figura_core.model.shader.FiguraRenderType;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.figura_core.util.FiguraTransformStack;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class CompatibleRenderer implements FiguraModelPartRenderer {

    private final FiguraModelPart part;
    private MultiBufferSource bufferSource;
    private FiguraTransformStack matrixStack;
    private float tickDelta;
    private int light, overlay;

    public CompatibleRenderer(FiguraModelPart part) {
        this.part = part;
    }

    // Call setup() before rendering
    public void setup(MultiBufferSource bufferSource, FiguraTransformStack matrixStack, float tickDelta, int light, int overlay) {
        this.bufferSource = bufferSource;
        this.matrixStack = matrixStack;
        this.tickDelta = tickDelta;
        this.light = light;
        this.overlay = overlay;
    }

    @Override
    public void render() {
        recursiveRender(part, bufferSource, List.of(), 0, matrixStack, tickDelta, light, overlay);
    }

    private void recursiveRender(
            FiguraModelPart part, // The part being rendered
            MultiBufferSource bufferSource, // The source for render buffers
            List<RenderType> currentRenderTypes, // The currently cached render types which vertices will be passed to
            int renderTypePriority, // The current render type priority. The render type may only be changed if part.renderTypePriority >= renderTypePriority.
            FiguraTransformStack matrixStack, // The current matrix stack
            float tickDelta,
            int light,
            int overlay
    ) {

        for (var callback : part.preRenderCallbacks)
            callback.call(new CallbackItem.F32(tickDelta));

        // Cancel if not invisible
        if (!part.transform.getVisible())
            return;

        // Update render types if we can and this has priority
        if (part.getRenderType() != null && part.renderTypePriority >= renderTypePriority) {
            currentRenderTypes = switch (part.getRenderType()) {
                case FiguraRenderType.EndPortal __ -> List.of(RenderType.endPortal());
                case FiguraRenderType.EndGateway __ -> List.of(RenderType.endGateway());
                case FiguraRenderType.Basic(MinecraftTexture mainTex, MinecraftTexture emissiveTex) -> {
                    ArrayList<RenderType> list = new ArrayList<>(2);
                    if (mainTex != null) list.add(RenderType.entityTranslucent(texToLocation(mainTex)));
                    if (emissiveTex != null) list.add(RenderType.eyes(texToLocation(emissiveTex)));
                    yield list;
                }
            };
            renderTypePriority = part.renderTypePriority;
        }

        // Push matrix stack, apply transforms
        matrixStack.push();
        part.transform.affect(matrixStack);

        for (var callback : part.midRenderCallbacks)
            callback.call(new CallbackItem.F32(tickDelta));

        // Render children recursively
        for (FiguraModelPart child : part.children.values())
            recursiveRender(child, bufferSource, currentRenderTypes, renderTypePriority, matrixStack, tickDelta, light, overlay);

        // If this has vertices, send them in
        if (part.vertices.length > 0) {
            Vector4f pos = new Vector4f();
            Vector3f norm = new Vector3f();
            Matrix4f posMatrix = matrixStack.peekPosition();
            Matrix3f normalMatrix = matrixStack.peekNormal();
            Vector4f color = matrixStack.peekColor();
            float[] vertices = part.vertices;
            for (RenderType renderType : currentRenderTypes) {
                VertexConsumer consumer = bufferSource.getBuffer(renderType);
                for (int i = 0; i < vertices.length; i += 16) {
                    pos.set(vertices[i], vertices[i+1], vertices[i+2], 1.0).mul(posMatrix);
                    norm.set(vertices[i+3], vertices[i+4], vertices[i+5]).mul(normalMatrix).normalize();

                    // Skinning is ignored in compatible mode (TODO add, even if not performant)
                    consumer.addVertex(pos.x, pos.y, pos.z)
                            .setColor(color.x, color.y, color.z, color.w)
                            .setUv(vertices[i+6], vertices[i+7])
                            .setNormal(norm.x, norm.y, norm.z)
                            .setLight(light)
                            .setOverlay(overlay);
                }
            }
        }

        for (var callback : part.postRenderCallbacks)
            callback.call(new CallbackItem.F32(tickDelta));

        // Pop matrix stack
        matrixStack.pop();
    }

    @Override public void rebuild() {}
    @Override public void destroy() {}

    private static ResourceLocation texToLocation(MinecraftTexture texture) {
        return switch (texture) {
            case MinecraftTextureImpl impl -> impl.location;
            case OwnedMinecraftTextureImpl ownedImpl -> ownedImpl.location;
            default -> TextureManager.INTENTIONAL_MISSING_TEXTURE;
        };
    }

}
