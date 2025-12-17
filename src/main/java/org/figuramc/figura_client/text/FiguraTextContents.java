package org.figuramc.figura_client.text;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import org.figuramc.figura_core.text.FormattedText;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * Custom component for Figura extended formatting.
 * <br>
 * for future reference: {@link net.minecraft.network.chat.contents.ObjectContents}, {@link net.minecraft.network.chat.contents.objects.ObjectInfo}, {@link net.minecraft.network.chat.contents.objects.PlayerSprite}
 */
public class FiguraTextContents implements ComponentContents {
    private static final String PLACEHOLDER = Character.toString('￼');

    public record FormattedTextRef(String fallback, UUID avatar, UUID text) {
        public static final Codec<FormattedTextRef> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.STRING.fieldOf("fallback").forGetter(FormattedTextRef::fallback),
                        UUIDUtil.CODEC.fieldOf("avatar").forGetter(FormattedTextRef::avatar),
                        UUIDUtil.CODEC.fieldOf("text").forGetter(FormattedTextRef::text)
                ).apply(instance, FormattedTextRef::new)
        );
    }

    public static final MapCodec<FiguraTextContents> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                    FormattedTextRef.CODEC.fieldOf("figura").forGetter(k -> k.figura)
            ).apply(instance, FiguraTextContents::new)
    );

    private final FormattedTextRef figura;
    private final FiguraFontDescription fontDesc;

    @Override
    public @NotNull MapCodec<? extends ComponentContents> codec() {
        return CODEC;
    }

    public FiguraTextContents(FormattedTextRef figura) {
        this.figura = figura;
        this.fontDesc = new FiguraFontDescription(new FormattedText(this.figura.fallback), figura.avatar, figura.text);
    }

    @Override
    public <T> @NotNull Optional<T> visit(net.minecraft.network.chat.FormattedText.@NotNull StyledContentConsumer<T> styledContentConsumer,
                                          @NotNull Style style) {
        // we have no intention of actually rendering the PLACEHOLDER,
        // but we need to communicate the font and at least 1 glyph to get into the rendering process
        return styledContentConsumer.accept(style.withFont(fontDesc), PLACEHOLDER);
    }

    @Override
    public <T> @NotNull Optional<T> visit(net.minecraft.network.chat.FormattedText.ContentConsumer<T> contentConsumer) {
        // This is the string-only fallback content, read out by the narrator etc. and written to logs
        return contentConsumer.accept("<" + figura.fallback + ">");
    }
}
