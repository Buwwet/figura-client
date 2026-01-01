package org.figuramc.figura_client.renderer.part.vanilla_optimized;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.SamplerCache;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import org.figuramc.figura_client.renderer.part.FiguraClientPartRenderer;
import org.figuramc.figura_client.renderer.part.text_rendering.FiguraTextRenderer;
import org.figuramc.figura_client.util.RenderUtils;
import org.figuramc.figura_core.avatars.AvatarError;
import org.figuramc.figura_core.model.part.tasks.TextTask;
import org.figuramc.figura_core.model.rendering.PartDataStruct;
import org.figuramc.figura_core.model.rendering.RenderingRoot;
import org.figuramc.figura_core.model.rendering.shader.BuiltinShader;
import org.figuramc.figura_core.model.rendering.vertex.FiguraVertexFormat;
import org.figuramc.figura_core.util.ListUtils;
import org.figuramc.figura_core.util.data_structures.FiguraTransformStack;
import org.figuramc.figura_core.util.data_structures.Pair;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL46;

import java.nio.ByteBuffer;
import java.util.*;

public class OptimizedRenderer extends FiguraClientPartRenderer {

    private static final int FIGURA_UNIFORMS_SIZE = new Std140SizeCalculator()
            .putMat4f().putMat4f() // Matrices
            .putVec4() // Overlay color
            .putVec4().putVec4().putVec4().putVec4() // UV modifiers
            .putVec2() // ScreenSize
            .putFloat() // GameTime
            .align(16) // Align by 16 since there's vec4s in here
            .align(RenderSystem.getDevice().getUniformOffsetAlignment()) // Align by impl-dependent offset alignment
            .get();

    private @Nullable State state;

    public OptimizedRenderer(RenderingRoot<?> root) {
        super(root);
    }

    private record State(
            GpuBuffer transformsBuffer,
            GpuBuffer figuraUniformsBuffer,
            List<DrawCallState> drawCallInfos
    ) implements AutoCloseable {
        @Override
        public void close() {
            transformsBuffer.close();
            figuraUniformsBuffer.close();
            drawCallInfos.forEach(DrawCallState::close);
        }
    }
    private record DrawCallState(
            RenderingRoot.DrawCall base,
            RenderPipeline pipeline,
            GpuBuffer vertexBuffer
    ) implements AutoCloseable {
        @Override
        public void close() {
            vertexBuffer.close();
        }
    }

    // Re-create the state if it was lost
    private void rebuild() throws AvatarError {
        assert state == null;
        root.rebuildVertices();
        if (!root.builtVertexData.isEmpty()) {
            // Set up shared buffers
            GpuBuffer transformsBuffer = RenderSystem.getDevice().createBuffer(
                    () -> "Figura Transforms Buffer", GpuBuffer.USAGE_MAP_WRITE, (long) root.transformCount * PartDataStruct.GPU_SIZE);
            GpuBuffer figuraUniformsBuffer = RenderSystem.getDevice().createBuffer(
                    () -> "Figura Uniforms Buffer", GpuBuffer.USAGE_MAP_WRITE, (long) this.root.drawCalls.size() * FIGURA_UNIFORMS_SIZE); // Separate buffer range for each draw call
            // Generate draw call infos
            List<DrawCallState> drawCallInfos = new ArrayList<>();
            // Cache VBOs
            Map<Pair<FiguraVertexFormat, Integer>, GpuBuffer> vertexBuffers = new HashMap<>();
            // Create draw call infos
            for (RenderingRoot.DrawCall drawCall : this.root.drawCalls) {
                // Render pipeline
                RenderPipeline pipeline = CustomRenderPipelines.create(drawCall.renderType().shader());
                // Vertex buffer
                FiguraVertexFormat vertexFormat = drawCall.renderType().shader().vertexFormat();
                var formatKey = new Pair<>(vertexFormat, drawCall.start());
                GpuBuffer vertexBuffer = vertexBuffers.computeIfAbsent(formatKey, k -> RenderSystem.getDevice().createBuffer(
                                () -> "Figura Vertex Buffer", GpuBuffer.USAGE_VERTEX, root.builtVertexData.get(vertexFormat).slice(drawCall.start(), drawCall.length())));
                drawCallInfos.add(new DrawCallState(drawCall, pipeline, vertexBuffer));
            }

            state = new State(transformsBuffer, figuraUniformsBuffer, drawCallInfos);
        }
    }

    @Override
    public void render(MultiBufferSource bufferSource, FiguraTransformStack transformStack, int light, int overlay) throws AvatarError {
        // Ensure we have valid state before moving on
        if (state == null) rebuild();
        if (state == null) return;
        // Compute transforms
        FiguraTransformStack newStack = new FiguraTransformStack();
        newStack.light(transformStack.peekLight());
        newStack.color(transformStack.peekColor());
        root.extractTransforms(newStack, (renderTask, matrixStack) -> {
            // Code to handle render tasks. Just draw them as we encounter them.
            matrixStack.push();
            matrixStack.preMultiply(transformStack.peekPosition(), transformStack.peekNormal()); // Apply initial transforms from outside!
            switch (renderTask) {
                case TextTask textTask -> FiguraTextRenderer.render(textTask.formattedText, bufferSource, matrixStack, light, overlay);
            }
            matrixStack.pop();
        });
        // Map the transforms buffer and put data inside
        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        try (var transformsView = encoder.mapBuffer(state.transformsBuffer, false, true)) {
            ByteBuffer buf = transformsView.data();
            for (int i = 0; i < root.transformCount; i++)
                root.transforms[i].write(buf, i * PartDataStruct.GPU_SIZE);
        }

        // Loop over draw calls

        for (int drawIndex = 0; drawIndex < state.drawCallInfos.size(); drawIndex++) {
            DrawCallState drawCall = state.drawCallInfos.get(drawIndex);

            // Figura uniforms
            GpuBufferSlice uniformsBufferSlice = state.figuraUniformsBuffer.slice((long) drawIndex * FIGURA_UNIFORMS_SIZE, FIGURA_UNIFORMS_SIZE);
            try (var figuraUniformsView = encoder.mapBuffer(uniformsBufferSlice, false, true)) {
                ByteBuffer buf = figuraUniformsView.data();
                transformStack.peekPosition().get(0, buf); // CamRelWorldMat
                RenderSystem.getModelViewMatrix().get(64, buf); // ViewMat
                // Calculate overlay color... :P
                if (overlay >> 16 < 8) {
                    new Vector4f(1f, 0f, 0f, 178f / 255f).get(128, buf);
                } else {
                    float u = (overlay & 0xFFFF) / 15.0f;
                    float alpha = 1f - u * 0.75f;
                    new Vector4f(1f, 1f, 1f, alpha).get(128, buf);
                }
                // Builtin UV modifiers...? Too hardcoded?
                for (int i = 0; i < 4; i++) {
                    var figuraBinding = ListUtils.getOrNull(drawCall.base.renderType().textureBindings(), i);
                    if (figuraBinding == null) {
                        new Vector4f(0, 0, 1, 1).get(144 + i * 16, buf);
                    } else {
                        figuraBinding.uvModifier().get(144 + i * 16, buf);
                    }
                }
                // Screen size
                new Vector2f(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight()).get(208, buf);
                // Game time
                long l = Minecraft.getInstance().level == null ? 0L : Minecraft.getInstance().level.getGameTime();
                float gameTime = ((float)(l % 24000L) + Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false)) / 24000.0F;
                buf.putFloat(216, gameTime);
            }

            // Run the render pass
            try (RenderPass pass = encoder.createRenderPass(
                    () -> "Figura Render Pass",
                    Minecraft.getInstance().getMainRenderTarget().getColorTextureView(),
                    OptionalInt.empty(), // Don't clear color texture
                    Minecraft.getInstance().getMainRenderTarget().getDepthTextureView(),
                    OptionalDouble.empty() // Don't clear depth texture
            )) {
                // Vertex buffer:
                pass.setVertexBuffer(0, drawCall.vertexBuffer);
                // Index buffer:
                int vertexCount = drawCall.base.length() / drawCall.base.renderType().shader().vertexFormat().vertexSize;
                int indexCount = VertexFormat.Mode.QUADS.indexCount(vertexCount);
                RenderSystem.AutoStorageIndexBuffer indexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.Mode.QUADS);
                pass.setIndexBuffer(indexBuffer.getBuffer(indexCount), indexBuffer.type());
                // Pipeline:
                pass.setPipeline(drawCall.pipeline);
                // Uniforms
                RenderSystem.bindDefaultUniforms(pass);
                pass.setUniform("FiguraUniforms", uniformsBufferSlice);
                // Textures (Pain)
                var main_binding = ListUtils.getOrNull(drawCall.base.renderType().textureBindings(), 0);
                var main_handle = main_binding == null ? null : main_binding.textureHandle();
                var main_gpuTex = RenderUtils.texToGpuTextureView(main_handle);
                var main_tex = main_gpuTex == null ? RenderUtils.ZERO_PIXEL.getTextureView() : main_gpuTex;
                pass.bindTexture("Main", main_tex, RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST));
                var normal_binding = ListUtils.getOrNull(drawCall.base.renderType().textureBindings(), 1);
                var normal_handle = normal_binding == null ? null : normal_binding.textureHandle();
                var normal_gpuTex = RenderUtils.texToGpuTextureView(normal_handle);
                var normal_tex = normal_gpuTex == null ? RenderUtils.DEFAULT_NORMAL_MAP.getTextureView() : normal_gpuTex;
                pass.bindTexture("NormalMap", normal_tex, RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST));
                var specular_binding = ListUtils.getOrNull(drawCall.base.renderType().textureBindings(), 2);
                var specular_handle = specular_binding == null ? null : specular_binding.textureHandle();
                var specular_gpuTex = RenderUtils.texToGpuTextureView(specular_handle);
                var specular_tex = specular_gpuTex == null ? RenderUtils.ZERO_PIXEL.getTextureView() : specular_gpuTex;
                pass.bindTexture("SpecularMap", specular_tex, RenderSystem.getSamplerCache().getRepeat(FilterMode.NEAREST));
                var lightmap_binding = ListUtils.getOrNull(drawCall.base.renderType().textureBindings(), 3);
                var lightmap_handle = lightmap_binding == null ? null : lightmap_binding.textureHandle();
                var lightmap_gpuTex = RenderUtils.texToGpuTextureView(lightmap_handle);
                var lightmap_tex = lightmap_gpuTex == null ? Minecraft.getInstance().gameRenderer.lightTexture().getTextureView() : lightmap_gpuTex;
                pass.bindTexture("LightMap", lightmap_tex, RenderSystem.getSamplerCache().getRepeat(FilterMode.LINEAR)); // Linear filter on lightmap for smooth lighting

                // TODO: Add workaround for if SSBO isn't supported (or we're somehow not using OpenGL backend?)
                GL46.glBindBufferBase(GL46.GL_SHADER_STORAGE_BUFFER, 0, ((GlBuffer) state.transformsBuffer).handle);

                // Draw! (Base vertex, Base index, Index Count, Instance Count)
                pass.drawIndexed(0, 0, indexCount, 1);
            }
        }
    }

    @Override
    public void invalidate() {
        if (this.state != null) {
            this.state.close();
            this.state = null;
        }
    }

    @Override
    public void destroy() {
        invalidate();
    }
}
