package org.figuramc.figura_client.text;

import net.minecraft.network.chat.FontDescription;
import org.figuramc.figura_core.manage.AvatarManagers;
import org.figuramc.figura_core.manage.AvatarView;
import org.figuramc.figura_core.text.FormattedText;

import java.util.UUID;

// the fakest font you've ever seen
// yeah it's just a marker interface LMAO
public record FiguraFontDescription(FormattedText fallback, UUID avatarRef, UUID textRef) implements FontDescription {
    public FormattedText contents() {
        AvatarView<UUID> maybeAvatar = AvatarManagers.ENTITIES.get(avatarRef);
        if (maybeAvatar == null) return fallback;
        FormattedText extern = maybeAvatar.useFor(avatar -> avatar.getExposedFormattedText(textRef));
        return extern == null ? fallback : extern;
    }
}
