package org.figuramc.figura_client.text;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import org.figuramc.figura_core.manage.AvatarManagers;
import org.figuramc.figura_core.manage.AvatarView;
import org.figuramc.figura_core.text.FormattedText;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.UUID;

/**
 * Custom component for Figura extended formatting.
 */
public class FiguraTextContents implements ComponentContents {
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

    private final FormattedText fallback;

    private FormattedText contents() {
        AvatarView<UUID> maybeAvatar = AvatarManagers.ENTITIES.get(figura.avatar);
        if (maybeAvatar == null) return fallback;
        FormattedText extern = maybeAvatar.useFor(avatar -> avatar.getExposedFormattedText(figura.text));
        return extern == null ? fallback : extern;
    }

    @Override
    public @NotNull MapCodec<? extends ComponentContents> codec() {
        return CODEC;
    }

    public FiguraTextContents(FormattedTextRef figura) {
        this.figura = figura;
        this.fallback = new FormattedText(this.figura.fallback);
    }

    @Override
    public <T> @NotNull Optional<T> visit(net.minecraft.network.chat.FormattedText.@NotNull StyledContentConsumer<T> styledContentConsumer,
                                          @NotNull Style style) {
        return ComponentContents.super.visit(styledContentConsumer, style);
    }
}
