package org.figuramc.figura_client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import org.figuramc.figura_client.textures.MinecraftTextureImpl;
import org.figuramc.figura_client.textures.OwnedMinecraftTextureImpl;
import org.figuramc.figura_core.avatars.AvatarError;
import org.figuramc.figura_core.minecraft_interop.texture.MinecraftTexture;
import org.figuramc.figura_core.model.rendering.FiguraRenderType;
import org.figuramc.figura_core.model.rendering.RenderingRoot;
import org.figuramc.figura_core.model.rendering.vertex.FiguraVertexFormat;
import org.figuramc.figura_core.util.ListUtils;
import org.figuramc.figura_core.util.MathUtils;
import org.figuramc.figura_core.util.data_structures.FiguraTransformStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

/**
 * This object is stored as state on a RenderingRoot.
 */
public class CompatibleRenderer2 implements RenderingRoot.Destructible {

    // Construct a renderer for the given rendering root, setting its clientState as well
    private final RenderingRoot<?> root;
    public CompatibleRenderer2(RenderingRoot<?> root) throws AvatarError {
        this.root = root;
        this.root.clientState = this;
        this.root.rebuildVertices(); // Build vertices once. TODO build this when needed :P
    }

    /**
     * Render the given root (vertices already built?)
     */
    public void render(MultiBufferSource bufferSource, FiguraTransformStack transformStack, int light, int overlay) throws AvatarError {
        // Calculate transforms
        root.calculateTransforms(transformStack);
        // Temporary variables :P
        Vector4f temp0 = new Vector4f();
        Vector4f temp1 = new Vector4f();
        Vector3f temp2 = new Vector3f();
        Vector3f temp3 = new Vector3f();
        Vector4f temp4 = new Vector4f();
        // Push each draw call
        for (var drawCall : root.drawCalls) {
            // Fetch minecraft render types, (TODO choosing the fallback for custom rendertypes!)
            List<RenderType> minecraftRenderTypes = switch (drawCall.renderType()) {
                case FiguraRenderType.EndPortal __ -> List.of(RenderType.endPortal());
                case FiguraRenderType.EndGateway __ -> List.of(RenderType.endGateway());
                case FiguraRenderType.Basic(MinecraftTexture mainTex, MinecraftTexture emissiveTex, int __) -> {
                    ArrayList<RenderType> list = new ArrayList<>(2);
                    if (mainTex != null) list.add(RenderType.entityTranslucent(texToLocation(mainTex))); // TODO deal with memory leak this causes (Util.memoize)
                    if (emissiveTex != null) list.add(RenderType.eyes(texToLocation(emissiveTex))); // TODO deal with memory leak this causes (Util.memoize)
                    yield list;
                }
            };
            root.builtVertexData.position(drawCall.start());
            int vertexCount = drawCall.length() / drawCall.renderType().vertexFormat().vertexSize;

            // Because the compatible renderer only supports builtin render types, we know all possible states.

            if (drawCall.renderType().vertexFormat() == FiguraVertexFormat.POSITION) {
                // Only position
                VertexConsumer buffer = bufferSource.getBuffer(minecraftRenderTypes.getFirst());
                for (int i = 0; i < vertexCount; i++) {
                    float x = root.builtVertexData.getFloat();
                    float y = root.builtVertexData.getFloat();
                    float z = root.builtVertexData.getFloat();
                    float weight0 = MathUtils.unsignedByteToFloat(root.builtVertexData.get());
                    float weight1 = MathUtils.unsignedByteToFloat(root.builtVertexData.get());
                    float weight2 = MathUtils.unsignedByteToFloat(root.builtVertexData.get());
                    float weight3 = MathUtils.unsignedByteToFloat(root.builtVertexData.get());
                    int offset0 = root.builtVertexData.getChar();
                    int offset1 = root.builtVertexData.getChar();
                    int offset2 = root.builtVertexData.getChar();
                    int offset3 = root.builtVertexData.getChar();

                    // Compute position in temp0, write it to vertex
                    temp0.zero();
                    if (offset0 != Character.MAX_VALUE) root.transforms[offset0].transform.transform(x, y, z, 1, temp1).mul(weight0).add(temp0, temp0);
                    if (offset1 != Character.MAX_VALUE) root.transforms[offset1].transform.transform(x, y, z, 1, temp1).mul(weight1).add(temp0, temp0);
                    if (offset2 != Character.MAX_VALUE) root.transforms[offset2].transform.transform(x, y, z, 1, temp1).mul(weight2).add(temp0, temp0);
                    if (offset3 != Character.MAX_VALUE) root.transforms[offset3].transform.transform(x, y, z, 1, temp1).mul(weight3).add(temp0, temp0);
                    buffer.addVertex(temp0.x, temp0.y, temp0.z);
                }
            } else if (drawCall.renderType().vertexFormat() == FiguraVertexFormat.DEFAULT) {
                List<VertexConsumer> buffers = ListUtils.map(minecraftRenderTypes, bufferSource::getBuffer);

                for (int i = 0; i < vertexCount; i++) {
                    float x = root.builtVertexData.getFloat();
                    float y = root.builtVertexData.getFloat();
                    float z = root.builtVertexData.getFloat();
                    float weight0 = MathUtils.unsignedByteToFloat(root.builtVertexData.get());
                    float weight1 = MathUtils.unsignedByteToFloat(root.builtVertexData.get());
                    float weight2 = MathUtils.unsignedByteToFloat(root.builtVertexData.get());
                    float weight3 = MathUtils.unsignedByteToFloat(root.builtVertexData.get());
                    int offset0 = root.builtVertexData.getChar();
                    int offset1 = root.builtVertexData.getChar();
                    int offset2 = root.builtVertexData.getChar();
                    int offset3 = root.builtVertexData.getChar();
                    float u = root.builtVertexData.getFloat();
                    float v = root.builtVertexData.getFloat();
                    float nx = MathUtils.signedByteToFloat(root.builtVertexData.get());
                    float ny = MathUtils.signedByteToFloat(root.builtVertexData.get());
                    float nz = MathUtils.signedByteToFloat(root.builtVertexData.get());
                    root.builtVertexData.get(); // Padding :catstare:

                    // Apply rigging weights
                    temp0.zero(); // temp0 = position
                    temp2.zero(); // temp2 = normal
                    temp4.zero(); // temp4 = color
                    if (offset0 != Character.MAX_VALUE) {
                        root.transforms[offset0].transform.transform(x, y, z, 1, temp1).mul(weight0).add(temp0, temp0);
                        root.transforms[offset0].normalMat.transform(nx, ny, nz, temp3).mulAdd(weight0, temp2, temp2);
                        temp1.set(root.transforms[offset0].colorMultiplier).mul(weight0).add(temp4, temp4);
                    }
                    if (offset1 != Character.MAX_VALUE) {
                        root.transforms[offset1].transform.transform(x, y, z, 1, temp1).mul(weight1).add(temp0, temp0);
                        root.transforms[offset1].normalMat.transform(nx, ny, nz, temp3).mulAdd(weight1, temp2, temp2);
                        temp1.set(root.transforms[offset1].colorMultiplier).mul(weight1).add(temp4, temp4);
                    }
                    if (offset2 != Character.MAX_VALUE) {
                        root.transforms[offset2].transform.transform(x, y, z, 1, temp1).mul(weight2).add(temp0, temp0);
                        root.transforms[offset2].normalMat.transform(nx, ny, nz, temp3).mulAdd(weight2, temp2, temp2);
                        temp1.set(root.transforms[offset2].colorMultiplier).mul(weight2).add(temp4, temp4);
                    }
                    if (offset3 != Character.MAX_VALUE) {
                        root.transforms[offset3].transform.transform(x, y, z, 1, temp1).mul(weight3).add(temp0, temp0);
                        root.transforms[offset3].normalMat.transform(nx, ny, nz, temp3).mulAdd(weight3, temp2, temp2);
                        temp1.set(root.transforms[offset3].colorMultiplier).mul(weight3).add(temp4, temp4);
                    }
                    // Normalize tha normal
                    temp2.normalize();

                    // Push to buffer(s)
                    for (var buffer : buffers) {
                        buffer
                                .addVertex(temp0.x, temp0.y, temp0.z)
                                .setColor(temp4.x, temp4.y, temp4.z, temp4.w)
                                .setUv(u, v)
                                .setOverlay(overlay)
                                .setLight(light)
                                .setNormal(temp2.x, temp2.y, temp2.z);
                    }
                }
            } else throw new IllegalStateException("Expected position format or default format for compatible renderer");
        }

    }

    private static ResourceLocation texToLocation(MinecraftTexture texture) {
        return switch (texture) {
            case MinecraftTextureImpl impl -> impl.location;
            case OwnedMinecraftTextureImpl ownedImpl -> ownedImpl.location;
            default -> TextureManager.INTENTIONAL_MISSING_TEXTURE;
        };
    }

    @Override public void destroy() {
        // No destruction needed, as this has no native VBOs or similar
    }
}
