package org.figuramc.figura_client.ducks;

import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaPart;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

// Accessor to grab custom fields in ModelPartMixin
public interface ModelPartAccess {

    // Get/set the VanillaPart which corresponds to this ModelPart
    @Nullable VanillaPart figura_client$getVanillaPart();
    void figura_client$setVanillaPart(@Nullable VanillaPart part);

    // Get/set new "position" variable for the transform (added by Figura)
    Vector3f figura_client$getPosition();


}
