package org.figuramc.figura_client.vanilla_model.models;

import org.figuramc.figura_client.vanilla_model.VanillaPartImpl;
import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaPart;
import org.figuramc.figura_core.minecraft_interop.vanilla_parts.vanilla_models.ElytraModel;
import org.jetbrains.annotations.Nullable;

public class ElytraModelImpl implements ElytraModel {

    protected final VanillaPartImpl left_wing, right_wing;

    public ElytraModelImpl(net.minecraft.client.model.ElytraModel model) {
        this.left_wing = new VanillaPartImpl(model.root().getChild("left_wing"), null);
        this.right_wing = new VanillaPartImpl(model.root().getChild("right_wing"), null);
    }

    @Override public @Nullable VanillaPart left_wing() { return left_wing; }
    @Override public @Nullable VanillaPart right_wing() { return right_wing; }

}
