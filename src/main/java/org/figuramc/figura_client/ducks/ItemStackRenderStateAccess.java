package org.figuramc.figura_client.ducks;

import org.figuramc.figura_client.renderer.submit.FiguraPartSubmit;
import org.jetbrains.annotations.Nullable;

/**
 * Access the new fields in ItemStackRenderStateMixin
 */
public interface ItemStackRenderStateAccess {

    @Nullable FiguraPartSubmit figura_client$getPartSubmit();
    void figura_client$setPartSubmit(@Nullable FiguraPartSubmit submit);

    // Reset to defaults
    default void figura_client$clear() {
        figura_client$setPartSubmit(null);
    }

}
