package org.figuramc.figura_client.renderer;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.figura_core.text.FormattedText;
import org.figuramc.figura_core.text.TextStyle;
import org.figuramc.figura_core.util.data_structures.FiguraTransformStack;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector4f;

import java.util.ArrayList;

/**
 * Class containing logic for rendering text.
 */
public class FiguraTextRenderer {

    // Exposed method to do rendering.
    public static void render(FormattedText formattedText, MultiBufferSource bufferSource, FiguraTransformStack matrixStack, int light, int overlay) {
        FiguraTextRenderer renderer = new FiguraTextRenderer(bufferSource, matrixStack.peekPosition(), light, overlay);
        renderer.processChars(formattedText, 0);
        renderer.renderLine(); // Flush the final line
    }

    private static final char UNKNOWN = '�';

    private final MultiBufferSource bufferSource;
    private final Matrix4f matrix;
    private final int light, overlay;

    // Cur vars
    private TextStyle style;
    private final Font font;
    private FontSet fontSet;

    // Accumulation throughout line(s)
    private ArrayList<QueuedGlyph> line = new ArrayList<>();
    private float lineHeight;
    private float y = 0;


    private FiguraTextRenderer(MultiBufferSource bufferSource, Matrix4f matrix, int light, int overlay) {
        this.font = Minecraft.getInstance().font;
        this.bufferSource = bufferSource;
        this.matrix = matrix;
        this.light = light;
        this.overlay = overlay;
    }

    /**
     * Process chars for the given text piece, with the given index. Return the # of chars rendered. Recursive.
     */
    private int processChars(FormattedText text, int charIndex) {
        // TODO make font configurable? Wouldn't be controllable by molang since it's a string and not a float, though :/
        fontSet = font.getFontSet(ResourceLocation.parse("minecraft:default"));
        style = text.style;
        // Process codepoints
        for (int codepoint : text.codepoints)
            processChar(charIndex++, codepoint);
        // Process children
        for (FormattedText child : text.children)
            charIndex = processChars(child, charIndex);
        // Return index
        return charIndex;
    }

    /**
     * Process the given char
     */
    private void processChar(int charIndex, int codepoint) {
        Vector2f scale = style.scale.value(charIndex);
        float glyphHeight = font.lineHeight * scale.y;
        this.lineHeight = Math.max(this.lineHeight, glyphHeight);
        // If the codepoint is a newline, flush this line rendering; otherwise process the char
        if (codepoint == '\n') {
            renderLine();
        } else {
            // Process the char and add it to the current line, baking its size.
            GlyphInfo info = fontSet.getGlyphInfo(codepoint, false);
            BakedGlyph glyph = style.obfuscated.value(charIndex) ? fontSet.getRandomGlyph(info) : fontSet.getGlyph(codepoint);
            boolean bold = style.bold.value(charIndex);
            float width = info.getAdvance(bold) * scale.x;
            line.add(new QueuedGlyph(info, glyph, bold, scale.x, scale.y, width, glyphHeight, style, charIndex));
        }
    }

    /**
     * Flush the queue and render chars for this line
     */
    private void renderLine() {
        boolean empty = line.isEmpty();
        float x = 0;
        for (QueuedGlyph queuedGlyph : line) {
            renderGlyph(x, queuedGlyph);
            x += queuedGlyph.width;
        }
        y += empty ? font.lineHeight : lineHeight;
        line.clear();
        lineHeight = 0;
    }

    /**
     * Render a queued glyph
     */
    private void renderGlyph(float x, QueuedGlyph queuedGlyph) {
        // Localize vars
        float charIndex = queuedGlyph.charIndex;
        TextStyle style = queuedGlyph.style;
        BakedGlyph glyph = queuedGlyph.glyph;
        float width = queuedGlyph.width;
        float height = queuedGlyph.height;
        float scaleX = queuedGlyph.scaleX;
        float scaleY = queuedGlyph.scaleY;
        float y = this.y;
        final float initialX = x, initialY = y;
        float u0 = glyph.u0, u1 = glyph.u1, v0 = glyph.v0, v1 = glyph.v1;

        Vector4f backgroundColor = style.backgroundColor.value(charIndex);
        if (backgroundColor.w != 0) renderEffect(initialX, initialY, initialX + width, initialY + height, backgroundColor);

        if (!(glyph instanceof EmptyGlyph)) {
            VertexConsumer consumer = bufferSource.getBuffer(glyph.renderType(Font.DisplayMode.NORMAL));

            // Color
            Vector4f color = style.color.value(charIndex);
            float r = color.x; float g = color.y; float b = color.z; float a = color.w;

            // Offset (does not affect location of extra effects like background, underline, and strikethrough)
            Vector2f offset = style.offset.value(charIndex);
            x += offset.x; y += offset.y;

            // Alignment
            y += (lineHeight - height) * style.verticalAlignment.value(charIndex);

            // Skew
            float skewX = 0;
            if (style.italic.value(charIndex))
                skewX += scaleX;
            Vector2f skew = style.skew.value(charIndex);
            skewX += skew.x;
            float skewY = skew.y;

            // Rendering vars
            float x1 = x + glyph.left;
            float x2 = x + glyph.right;
            float xDiff = x2 - x1;
            float rY1 = glyph.up - 3;
            float rY2 = glyph.down - 3;
            float y1 = y + rY1;
            float y2 = y + rY2;
            float yDiff = y2 - y1;
            x2 += xDiff * (scaleX - 1);
            y2 += yDiff * (scaleY - 1);

            // Shadow
            Vector4f shadowColor = style.shadowColor.value(charIndex);
            if (shadowColor.w != 0) {
                Vector2f shadowOffset = style.shadowOffset.value(charIndex);
                float oX = shadowOffset.x * scaleX;
                float oY = shadowOffset.y * scaleY;
                renderCharInternal(consumer, x1 + oX, y1 + oY, x2 + oX, y2 + oY, u0, v0, u1, v1, skewX, skewY, shadowColor.x, shadowColor.y, shadowColor.z, shadowColor.w, light);
            }

            // Outline
            Vector4f outlineColor = style.outlineColor.value(charIndex);
            if (outlineColor.w != 0) {
                Vector2f outlineScale = style.outlineScale.value(charIndex);
                float cR = outlineColor.x, cG = outlineColor.y, cB = outlineColor.z, cA = outlineColor.w;
                for (int oY = -1; oY <= 1; oY++) {
                    for (int oX = -1; oX <= 1; oX++) {
                        if (oX == 0 && oY == 0) continue;
                        float cX = oX * outlineScale.x;
                        float cY = oY * outlineScale.y;
                        renderCharInternal(consumer, x1 + cX, y1 + cY, x2 + cX, y2 + cY, u0, v0, u1, v1, skewX, skewY, cR, cG, cB, cA, light);
                    }
                }
            }

            // Bold
            if (queuedGlyph.bold) {
                float weight = queuedGlyph.info.getBoldOffset() * scaleX;
                renderCharInternal(consumer, x1 + weight, y1, x2 + weight, y2, u0, v0, u1, v1, skewX, skewY, r, g, b, a, light);
            }

            // Normal rendering
            renderCharInternal(consumer, x1, y1, x2, y2, u0, v0, u1, v1, skewX, skewY, r, g, b, a, light);
        }

        // Strikethrough
        Vector4f strikethroughColor = style.strikethroughColor.value(charIndex);
        if (strikethroughColor.w != 0) {
            float cY1 = initialY + (lineHeight / 2) - 1;
            float cY2 = cY1 + 1;
            renderEffect(initialX, cY1, initialX + width, cY2, strikethroughColor);
        }

        // Underline
        Vector4f underlineColor = style.underlineColor.value(charIndex);
        if (underlineColor.w != 0) {
            float cY2 = initialY + lineHeight;
            float cY1 = cY2 - 1;
            renderEffect(initialX, cY1, initialX + width, cY2, underlineColor);
        }
    }

    private void renderCharInternal(VertexConsumer consumer, float x1, float y1, float x2, float y2, float u0, float v0, float u1, float v1, float skewX, float skewY, float r, float g, float b, float a, int light) {
        consumer.addVertex(matrix, x1 + skewX, y1 - skewY, 0).setColor(r, g, b, a).setUv(u0, v0).setLight(light);
        consumer.addVertex(matrix, x1 - skewX, y2 - skewY, 0).setColor(r, g, b, a).setUv(u0, v1).setLight(light);
        consumer.addVertex(matrix, x2 - skewX, y2 + skewY, 0).setColor(r, g, b, a).setUv(u1, v1).setLight(light);
        consumer.addVertex(matrix, x2 + skewX, y1 + skewY, 0).setColor(r, g, b, a).setUv(u1, v0).setLight(light);
    }

    private void renderEffect(float x1, float y1, float x2, float y2, Vector4f color) {
        float r = color.x, g = color.y, b = color.z, a = color.w;
        BakedGlyph whiteGlyph = this.fontSet.whiteGlyph();
        VertexConsumer consumer = bufferSource.getBuffer(whiteGlyph.renderType(Font.DisplayMode.NORMAL));
        consumer.addVertex(matrix, x1, y1, 0).setColor(r, g, b, a).setUv(whiteGlyph.u0, whiteGlyph.v0).setLight(light);
        consumer.addVertex(matrix, x1, y2, 0).setColor(r, g, b, a).setUv(whiteGlyph.u0, whiteGlyph.v1).setLight(light);
        consumer.addVertex(matrix, x2, y2, 0).setColor(r, g, b, a).setUv(whiteGlyph.u1, whiteGlyph.v1).setLight(light);
        consumer.addVertex(matrix, x2, y1, 0).setColor(r, g, b, a).setUv(whiteGlyph.u1, whiteGlyph.v0).setLight(light);
    }

    private record QueuedGlyph(GlyphInfo info, BakedGlyph glyph, boolean bold, float scaleX, float scaleY, float width, float height, TextStyle style, int charIndex) {

    }

}
