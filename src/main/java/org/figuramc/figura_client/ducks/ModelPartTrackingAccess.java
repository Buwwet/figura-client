package org.figuramc.figura_client.ducks;

import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaPart;
import org.jetbrains.annotations.Nullable;

// Accessor to grab custom fields in ModelPartTrackingMixin
public interface ModelPartTrackingAccess {
    @Nullable VanillaPart figura_client$getVanillaPart();
    void figura_client$setVanillaPart(@Nullable VanillaPart part);
}
