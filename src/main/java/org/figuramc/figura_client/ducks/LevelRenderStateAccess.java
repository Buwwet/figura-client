package org.figuramc.figura_client.ducks;

import org.figuramc.figura_client.renderer.submit.FiguraCallbackSubmit;
import org.figuramc.figura_client.renderer.submit.FiguraPartSubmit;
import org.figuramc.figura_core.manage.AvatarView;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * We add new fields to LevelRenderState through a mixin.
 * Accessor is used to get/set those fields.
 */
public interface LevelRenderStateAccess {

//    @Nullable FiguraPartSubmit figura_client$getPartSubmit();
//    void figura_client$setPartSubmit(@Nullable FiguraPartSubmit submit);

    @Nullable FiguraCallbackSubmit figura_client$getCodeSubmit();
    void figura_client$setCodeSubmit(@Nullable FiguraCallbackSubmit submit);

    // Reset to defaults
    default void figura_client$reset() {
//        figura_client$setPartSubmit(null);
        figura_client$setCodeSubmit(null);
    }

    default boolean isDefault() {
        return figura_client$getCodeSubmit() != null;
    }

}
