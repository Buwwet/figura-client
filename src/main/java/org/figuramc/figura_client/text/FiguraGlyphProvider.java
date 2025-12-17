package org.figuramc.figura_client.text;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GlyphSource;
import net.minecraft.client.gui.font.TextRenderable;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.network.chat.Style;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

/**
 * see: {@link net.minecraft.client.gui.font.PlayerGlyphProvider}
 */
public class FiguraGlyphProvider {
    public static final FiguraGlyphProvider INSTANCE = new FiguraGlyphProvider();

    private FiguraGlyphProvider() {
    }

    public static class FiguraGlyphRendering implements TextRenderable.Styled {

        @Override
        public Style style() {
            return null;
        }

        @Override
        public void render(Matrix4f matrix4f, VertexConsumer vertexConsumer, int i, boolean bl) {

        }

        @Override
        public RenderType renderType(Font.DisplayMode displayMode) {
            return null;
        }

        @Override
        public GpuTextureView textureView() {
            return null;
        }

        @Override
        public RenderPipeline guiPipeline() {
            return null;
        }

        @Override
        public float left() {
            return 0;
        }

        @Override
        public float top() {
            return 0;
        }

        @Override
        public float right() {
            return 0;
        }

        @Override
        public float bottom() {
            return 0;
        }
    }

    public static class FiguraGlyph implements BakedGlyph {

        @Override
        public GlyphInfo info() {
            return null;
        }

        @Override
        public TextRenderable.@Nullable Styled createGlyph(float f,
                                                           float g,
                                                           int i,
                                                           int j,
                                                           Style style,
                                                           float h,
                                                           float k) {
            return null;
        }
    }

    public static class FiguraGlyphSource implements GlyphSource {

        private final FiguraFontDescription descriptor;

        public FiguraGlyphSource(FiguraFontDescription descriptor) {
            this.descriptor = descriptor;
        }

        @Override
        public BakedGlyph getGlyph(int i) {
            return null;
        }

        @Override
        public BakedGlyph getRandomGlyph(RandomSource randomSource, int i) {
            return null;
        }
    }

    public GlyphSource source(FiguraFontDescription descriptor) {
        // TODO: does this do an allocation on every frame? if so there's a problem
        return new FiguraGlyphSource(descriptor);
    }
}
