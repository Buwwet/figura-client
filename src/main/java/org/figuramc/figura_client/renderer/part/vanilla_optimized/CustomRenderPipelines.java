package org.figuramc.figura_client.renderer.part.vanilla_optimized;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.ShaderDefines;
import org.figuramc.figura_client.FiguraClient;
import org.figuramc.figura_core.model.rendering.shader.BuiltinShader;
import org.figuramc.figura_core.model.rendering.shader.ExtensionShader;
import org.figuramc.figura_core.model.rendering.shader.FiguraShader;
import org.figuramc.figura_core.model.rendering.shader.ShaderHookPoint;
import org.figuramc.figura_core.model.rendering.vertex.FiguraVertexFormat;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class CustomRenderPipelines {

    public static final CustomVertexFormat DEFAULT_VERTEX_FORMAT = new CustomVertexFormat(FiguraVertexFormat.DEFAULT);

    // Base snippets without additional extensions
    private static final RenderPipeline.Snippet BASIC_SNIPPET = RenderPipeline.builder()
            // Uniforms
            .withUniform("Fog", UniformType.UNIFORM_BUFFER)
            .withUniform("Projection", UniformType.UNIFORM_BUFFER)
            .withUniform("Lighting", UniformType.UNIFORM_BUFFER)
            .withUniform("FiguraUniforms", UniformType.UNIFORM_BUFFER)
            // Textures
            .withSampler("Main")
            .withSampler("NormalMap")
            .withSampler("SpecularMap")
            .withSampler("LightMap")
            // Shaders
            .withVertexShader(FiguraClient.locate("core/figura_basic"))
            .withFragmentShader(FiguraClient.locate("core/figura_basic"))
            // Other
            .withVertexFormat(DEFAULT_VERTEX_FORMAT, VertexFormat.Mode.QUADS)
            .withBlend(BlendFunction.TRANSLUCENT)
            .withCull(false)
            .buildSnippet();

    public static RenderPipeline create(FiguraShader figuraShader) {
        return switch (figuraShader) {
            case BuiltinShader builtin -> createBase(builtin).build();
            case ExtensionShader extension -> createExtension(extension).build();
        };
    }

    // Get a RenderPipeline from a builtin shader. Uses default hooks.
    private static RenderPipeline.Builder createBase(BuiltinShader figuraShader) {
        return switch (figuraShader) {
            case BASIC -> withHooks(RenderPipeline.builder(BASIC_SNIPPET).withLocation(FiguraClient.locate("pipeline/figura_basic")), Map.of()); // Default hooks
            default -> throw new UnsupportedOperationException("TODO");
        };
    }

    private static RenderPipeline.Builder createExtension(ExtensionShader extensionShader) {
        RenderPipeline.Builder builder = createBase(extensionShader.base);
        // Replace vertex format
        builder.withVertexFormat(new CustomVertexFormat(extensionShader.vertexFormat()), VertexFormat.Mode.QUADS);
        // Add only the *additional* texture binding points to the builder
        for (int i = extensionShader.base.textureBindingPoints.size(); i < extensionShader.textureBindingPoints.size(); i++)
            builder.withSampler(extensionShader.textureBindingPoints.get(i));
        // Add our custom hooks
        return withHooks(builder, extensionShader.hookImplementations);
    }

    private static RenderPipeline.Builder withHooks(RenderPipeline.Builder builder, Map<ShaderHookPoint, @Nullable String> hookImplementations) {
        StringBuilder allHooks = new StringBuilder();
        for (ShaderHookPoint hookPoint : ShaderHookPoint.values(ShaderHookPoint.class)) {
            String impl = hookImplementations.get(hookPoint);
            if (impl == null) impl = hookPoint.defaultImpl;
            allHooks.append(impl).append("\n");
        }
        return withDefines(builder, Map.of("FIGURA_HOOKS", allHooks.toString()));
    }

    // Helper for adding string-based defines nicely.
    // Mojang's API is cringe so we can't use .withShaderDefine() to define a string to another string.
    private static RenderPipeline.Builder withDefines(RenderPipeline.Builder builder, Map<String, String> defines) {
        builder.definesBuilder = Optional.of(ShaderDefines.builder());
        defines.entrySet().forEach(e -> builder.definesBuilder.get().define(e.getKey(), e.getValue()));
        return builder;
    }

}
