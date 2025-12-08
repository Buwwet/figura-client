package org.figuramc.figura_client.ducks;

import org.figuramc.figura_core.manage.AvatarView;

public interface ModelSubmitAccess {
    AvatarView<?> figura_client$getAvatar();
    boolean figura_client$isLivingEntity();
}
