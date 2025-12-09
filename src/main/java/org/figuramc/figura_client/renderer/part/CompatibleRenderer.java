package org.figuramc.figura_client.renderer.part;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.figuramc.figura_client.renderer.part.text_rendering.FiguraTextRenderer;
import org.figuramc.figura_client.util.RenderUtils;
import org.figuramc.figura_core.avatars.AvatarError;
import org.figuramc.figura_core.model.part.tasks.TextTask;
import org.figuramc.figura_core.model.rendering.RenderingRoot;
import org.figuramc.figura_core.model.rendering.shader.BuiltinShader;
import org.figuramc.figura_core.model.rendering.shader.FiguraShader;
import org.figuramc.figura_core.model.rendering.vertex.FiguraVertexFormat;
import org.figuramc.figura_core.util.ListUtils;
import org.figuramc.figura_core.util.MathUtils;
import org.figuramc.figura_core.util.data_structures.FiguraTransformStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * This object is stored as state on a RenderingRoot.
 */

// TODO: This entire class is outdated!! It uses the outdated concept of "emissive texture" which we're changing.
//       Remake it all at some point; for now we're using OptimizedRenderer!
public class CompatibleRenderer extends FiguraClientPartRenderer {

    // Helpful cached info about converting from FiguraRenderType -> minecraft RenderTypes
    private List<CachedDrawCallData> cachedDrawCallData = null;

    public CompatibleRenderer(RenderingRoot<?> root) {
        super(root);
    }

    // Invalidate the cache, force a vertex rebuild
    @Override public void invalidate() {
        this.cachedDrawCallData = null;
    }

    private void rebuild() throws AvatarError {
        // Rebuild the vertices
        root.rebuildVertices();
        // Recreate draw call data
        cachedDrawCallData = new ArrayList<>(root.drawCalls.size());
        for (var drawCall : root.drawCalls) {
            FiguraShader shader = drawCall.renderType().shader();
            FiguraVertexFormat vertexFormat = shader.vertexFormat();
            List<MinecraftRenderTypeUsage> minecraftRenderTypeUsages = new ArrayList<>();

            if (shader == BuiltinShader.END_PORTAL || shader == BuiltinShader.END_GATEWAY) {
                // Textures 1 and 2
                var tex1 = ListUtils.getOrNull(drawCall.renderType().textureBindings(), 0);
                var loc1 = tex1 != null ? RenderUtils.texToLocation(tex1.textureHandle(), TheEndPortalRenderer.END_SKY_LOCATION) : TheEndPortalRenderer.END_SKY_LOCATION;
                var uv1 = tex1 != null ? tex1.uvModifier() : null;
                var tex2 = ListUtils.getOrNull(drawCall.renderType().textureBindings(), 1);
                var loc2 = tex2 != null ? RenderUtils.texToLocation(tex2.textureHandle(), TheEndPortalRenderer.END_PORTAL_LOCATION) : TheEndPortalRenderer.END_PORTAL_LOCATION;
                var uv2 = tex2 != null ? tex2.uvModifier() : null;
                // TODO: Ensure with an avatar error that tex1 and tex2 must have the same UV modifier; otherwise this will glitch out
                //       Users should achieve this by annotating the custom textures used here with the ".noatlas.png" flag

                var pipeline = (shader == BuiltinShader.END_PORTAL) ? RenderPipelines.END_PORTAL : RenderPipelines.END_GATEWAY;
                var renderType = RenderType.create("<Custom Figura RenderType>", RenderSetup.builder(pipeline)
                        .withTexture("Sampler0", loc1)
                        .withTexture("Sampler1", loc2)
                        .createRenderSetup());
                minecraftRenderTypeUsages.add(new MinecraftRenderTypeUsage(renderType, uv1));
            } else if (shader == BuiltinShader.BASIC) {
                // Textures
                var mainTex = ListUtils.getOrNull(drawCall.renderType().textureBindings(), 0);
                var mainLoc = mainTex != null ? RenderUtils.texToLocation(mainTex.textureHandle(), RenderUtils.ZERO_PIXEL_LOC) : RenderUtils.ZERO_PIXEL_LOC;
                var mainUV = mainTex != null ? mainTex.uvModifier() : null;
                var emissiveTex = ListUtils.getOrNull(drawCall.renderType().textureBindings(), 0);
                var emissiveLoc = emissiveTex != null ? RenderUtils.texToLocation(emissiveTex.textureHandle(), RenderUtils.ZERO_PIXEL_LOC) : RenderUtils.ZERO_PIXEL_LOC;
                var emissiveUV = emissiveTex != null ? emissiveTex.uvModifier() : null;
                // Render types
                var mainRenderType = RenderType.create("<Custom Figura RenderType>", RenderSetup.builder(RenderPipelines.ENTITY_TRANSLUCENT)
                        .withTexture("Sampler0", mainLoc)
                        .useLightmap()
                        .useOverlay()
                        .affectsCrumbling()
                        .sortOnUpload()
                        .setOutline(RenderSetup.OutlineProperty.AFFECTS_OUTLINE)
                        .createRenderSetup());
                minecraftRenderTypeUsages.add(new MinecraftRenderTypeUsage(mainRenderType, mainUV));
                var emissiveRenderType = RenderType.create("<Custom Figura RenderType>",RenderSetup.builder(RenderPipelines.EYES)
                        .withTexture("Sampler0", emissiveLoc)
                        .sortOnUpload()
                        .createRenderSetup());
                minecraftRenderTypeUsages.add(new MinecraftRenderTypeUsage(emissiveRenderType, emissiveUV));
            } else {
                throw new UnsupportedOperationException("Non-builtin shaders for compatible render mode: TODO figure this out");
            }
            int vertexCount = drawCall.length() / vertexFormat.vertexSize;
            cachedDrawCallData.add(new CachedDrawCallData(vertexFormat, minecraftRenderTypeUsages, drawCall.start(), vertexCount));
        }
    }

    private record CachedDrawCallData(FiguraVertexFormat vertexFormat, List<MinecraftRenderTypeUsage> minecraftRenderPasses, int startByte, int vertexCount) {}
    private record MinecraftRenderTypeUsage(RenderType renderType, @Nullable Vector4f uvModifier) {}

    /**
     * Render the given root (vertices already built)
     *
     * This function is an absolute mess full of special case hell.
     * Please stop being lazy and organize this at some point...
     */
    public void render(MultiBufferSource bufferSource, FiguraTransformStack transformStack, int light, int overlay) throws AvatarError {
        // Rebuild vertices if needed, ensuring we have the draw call data ready
        if (cachedDrawCallData == null) rebuild();
        // Calculate transforms
        root.extractTransforms(transformStack, (renderTask, matrixStack) -> {
            // Code to handle render tasks. Just draw them as we encounter them
            switch (renderTask) {
                case TextTask textTask -> FiguraTextRenderer.render(textTask.formattedText, bufferSource, transformStack, light, overlay);
            }
        });
        // Temporary variables :P
        Vector4f temp0 = new Vector4f();
        Vector4f temp1 = new Vector4f();
        Vector3f temp2 = new Vector3f();
        Vector3f temp3 = new Vector3f();
        Vector4f temp4 = new Vector4f();
        // Push each draw call
        for (var drawCall : cachedDrawCallData) {
            boolean posOnly = drawCall.vertexFormat == FiguraVertexFormat.POSITION;

            ByteBuffer buf = root.builtVertexData.get(drawCall.vertexFormat);

            buf.position(drawCall.startByte());

            // Store vertices temporarily CPU-side to send to possibly multiple vertex consumers
            int sharedDataStride = posOnly ? 3 : 11;
            float[] sharedData = new float[drawCall.vertexCount * sharedDataStride];
            float[] uvData = posOnly ? null : new float[drawCall.vertexCount * drawCall.minecraftRenderPasses.size() * 2];

            for (int vertexIndex = 0; vertexIndex < drawCall.vertexCount; vertexIndex++) {
                // Fetch positional data (required for all builtin shaders)
                float x = buf.getFloat();
                float y = buf.getFloat();
                float z = buf.getFloat();
                float weight0 = MathUtils.unsignedByteToFloat(buf.get());
                float weight1 = MathUtils.unsignedByteToFloat(buf.get());
                float weight2 = MathUtils.unsignedByteToFloat(buf.get());
                float weight3 = MathUtils.unsignedByteToFloat(buf.get());
                int offset0 = buf.getChar();
                int offset1 = buf.getChar();
                int offset2 = buf.getChar();
                int offset3 = buf.getChar();

                // Apply rigging weights and send out info

                if (posOnly) {
                    // In position format, we only need the position!
                    temp0.zero();
                    if (offset0 != Character.MAX_VALUE) root.transforms[offset0].transform.transform(x, y, z, 1, temp1).mul(weight0).add(temp0, temp0);
                    if (offset1 != Character.MAX_VALUE) root.transforms[offset1].transform.transform(x, y, z, 1, temp1).mul(weight1).add(temp0, temp0);
                    if (offset2 != Character.MAX_VALUE) root.transforms[offset2].transform.transform(x, y, z, 1, temp1).mul(weight2).add(temp0, temp0);
                    if (offset3 != Character.MAX_VALUE) root.transforms[offset3].transform.transform(x, y, z, 1, temp1).mul(weight3).add(temp0, temp0);
                    // Now send the position into the array.
                    int i = vertexIndex * sharedDataStride;
                    sharedData[i++] = temp0.x;
                    sharedData[i++] = temp0.y;
                    sharedData[i++] = temp0.z;
                } else {
                    // In default format, we need UV and Normal too.
                    // These are also affected by transforms and stored in the array(s).
                    float u = buf.getFloat();
                    float v = buf.getFloat();
                    float nx = MathUtils.signedByteToFloat(buf.get());
                    float ny = MathUtils.signedByteToFloat(buf.get());
                    float nz = MathUtils.signedByteToFloat(buf.get());
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
                    // Send data into array(s)
                    int i = vertexIndex * sharedDataStride;
                    sharedData[i++] = temp0.x;
                    sharedData[i++] = temp0.y;
                    sharedData[i++] = temp0.z;
                    sharedData[i++] = temp4.x;
                    sharedData[i++] = temp4.y;
                    sharedData[i++] = temp4.z;
                    sharedData[i++] = temp4.w;
                    sharedData[i++] = temp2.x;
                    sharedData[i++] = temp2.y;
                    sharedData[i++] = temp2.z;
                    // Send UVs
                    for (int mcPassIndex = 0; mcPassIndex < drawCall.minecraftRenderPasses.size(); mcPassIndex++) {
                        Vector4f uvMod = drawCall.minecraftRenderPasses.get(mcPassIndex).uvModifier;
                        float u2 = u, v2 = v;
                        if (uvMod != null) {
                            u2 = Math.fma(u, uvMod.z, uvMod.x);
                            v2 = Math.fma(v, uvMod.w, uvMod.y);
                        }
                        int i2 = (drawCall.vertexCount * mcPassIndex + vertexIndex) * 2;
                        uvData[i2++] = u2;
                        uvData[i2++] = v2;
                    }
                    // Skip padding byte
                    buf.get();
                }
            }

            // Now submit to buffers.
            for (int mcPassIndex = 0; mcPassIndex < drawCall.minecraftRenderPasses.size(); mcPassIndex++) {
                VertexConsumer buffer = bufferSource.getBuffer(drawCall.minecraftRenderPasses.get(mcPassIndex).renderType);
                int sharedOffset = 0;
                int uvOffset = mcPassIndex * drawCall.vertexCount * 2;
                for (int i = 0; i < drawCall.vertexCount; i++) {
                    buffer.addVertex(sharedData[sharedOffset++], sharedData[sharedOffset++], sharedData[sharedOffset++]);
                    if (!posOnly) {
                        // If not pos only, add the other values as well
                        buffer
                                .setColor(sharedData[sharedOffset++], sharedData[sharedOffset++], sharedData[sharedOffset++], sharedData[sharedOffset++])
                                .setNormal(sharedData[sharedOffset++], sharedData[sharedOffset++], sharedData[sharedOffset++])
                                .setUv(uvData[uvOffset++], uvData[uvOffset++])
                                .setOverlay(overlay)
                                .setLight(light);
                    }
                }
            }
        }
    }

    @Override public void destroy() {}
}
