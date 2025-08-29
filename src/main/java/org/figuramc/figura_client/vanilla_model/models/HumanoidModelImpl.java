package org.figuramc.figura_client.vanilla_model.models;

import org.figuramc.figura_client.vanilla_model.VanillaPartImpl;
import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaPart;
import org.figuramc.figura_core.minecraft_interop.vanilla_parts.vanilla_models.HumanoidModel;
import org.jetbrains.annotations.Nullable;

public class HumanoidModelImpl implements HumanoidModel {

    protected final VanillaPartImpl head, hat, body, left_arm, right_arm, left_leg, right_leg;

    public HumanoidModelImpl(net.minecraft.client.model.HumanoidModel<?> model) {
        this.head = new VanillaPartImpl(model.head, null);
        this.hat = new VanillaPartImpl(model.hat, head);
        this.body = new VanillaPartImpl(model.body, null);
        this.left_arm = new VanillaPartImpl(model.leftArm, null);
        this.right_arm = new VanillaPartImpl(model.rightArm, null);
        this.left_leg = new VanillaPartImpl(model.leftLeg, null);
        this.right_leg = new VanillaPartImpl(model.rightLeg, null);
    }

    @Override public @Nullable VanillaPart head() { return head; }
    @Override public @Nullable VanillaPart hat() { return hat; }
    @Override public @Nullable VanillaPart body() { return body; }
    @Override public @Nullable VanillaPart left_arm() { return left_arm; }
    @Override public @Nullable VanillaPart right_arm() { return right_arm; }
    @Override public @Nullable VanillaPart left_leg() { return left_leg; }
    @Override public @Nullable VanillaPart right_leg() { return right_leg; }
}
