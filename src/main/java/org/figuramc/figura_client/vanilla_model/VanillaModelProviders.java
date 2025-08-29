package org.figuramc.figura_client.vanilla_model;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.figuramc.figura_client.vanilla_model.models.PlayerModelImpl;
import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaModel;
import org.jetbrains.annotations.NotNull;

/**
 * Contains logic to find the VanillaModel implementation depending on the entity.
 * Not responsible for caching; see VanillaModelCache for details.
 */
public class VanillaModelProviders {

    // Just have various if-branches for this...
    // There may be a better way, but it would be tricky to make compared to just manual if-branches.
    public static @NotNull VanillaModel getModel(EntityRenderer<?, ?> renderer) {
        if (renderer instanceof PlayerRenderer playerRenderer)
            return new PlayerModelImpl(playerRenderer);
        return VanillaModel.EMPTY;
    }

}
