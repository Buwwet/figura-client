package org.figuramc.figura_client.vanilla_model;

import net.minecraft.client.model.PlayerCapeModel;
import net.minecraft.client.renderer.entity.layers.CapeLayer;
import net.minecraft.client.renderer.entity.layers.WingsLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaPart;
import org.figuramc.figura_core.minecraft_interop.vanilla_parts.vanilla_models.PlayerModel;
import org.jetbrains.annotations.Nullable;

public class PlayerModelImpl implements PlayerModel {

    protected final HumanoidModelImpl humanoid;
    protected final ElytraModelImpl elytra;

    protected final VanillaPartImpl
            jacket, left_sleeve, right_sleeve, left_pants, right_pants,
            cape
    ;

    public PlayerModelImpl(PlayerRenderer playerRenderer) {
        this.humanoid = new HumanoidModelImpl(playerRenderer.getModel());
        this.elytra = new ElytraModelImpl(VanillaPartImpl.getRenderLayer(playerRenderer, WingsLayer.class).elytraModel);
        this.jacket = new VanillaPartImpl(playerRenderer.getModel().jacket, humanoid.body);
        this.left_sleeve = new VanillaPartImpl(playerRenderer.getModel().leftSleeve, humanoid.left_arm);
        this.right_sleeve = new VanillaPartImpl(playerRenderer.getModel().rightSleeve, humanoid.right_arm);
        this.left_pants = new VanillaPartImpl(playerRenderer.getModel().leftPants, humanoid.left_leg);
        this.right_pants = new VanillaPartImpl(playerRenderer.getModel().rightPants, humanoid.right_leg);

        this.cape = new VanillaPartImpl(((PlayerCapeModel<PlayerRenderState>) (VanillaPartImpl.getRenderLayer(playerRenderer, CapeLayer.class).model)).cape, humanoid.body);
    }

    @Override public @Nullable VanillaPart jacket() { return jacket; }
    @Override public @Nullable VanillaPart left_sleeve() { return left_sleeve; }
    @Override public @Nullable VanillaPart right_sleeve() { return right_sleeve; }
    @Override public @Nullable VanillaPart left_pants() { return left_pants; }
    @Override public @Nullable VanillaPart right_pants() { return right_pants; }
    @Override public @Nullable VanillaPart cape() { return cape; }
    @Override public @Nullable VanillaPart left_wing() { return elytra.left_wing; }
    @Override public @Nullable VanillaPart right_wing() { return elytra.right_wing; }
    @Override public @Nullable VanillaPart head() { return humanoid.head; }
    @Override public @Nullable VanillaPart hat() { return humanoid.hat; }
    @Override public @Nullable VanillaPart body() { return humanoid.body; }
    @Override public @Nullable VanillaPart left_arm() { return humanoid.left_arm; }
    @Override public @Nullable VanillaPart right_arm() { return humanoid.right_arm; }
    @Override public @Nullable VanillaPart left_leg() { return humanoid.left_leg; }
    @Override public @Nullable VanillaPart right_leg() { return humanoid.right_leg; }

}
