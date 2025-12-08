package org.figuramc.figura_client.mixin.render.entity;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import org.figuramc.figura_client.ducks.EntityRenderStateAccess;
import org.figuramc.figura_client.renderer.submit.FiguraCallbackSubmit;
import org.figuramc.figura_client.renderer.submit.FiguraPartSubmit;
import org.figuramc.figura_core.manage.AvatarView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.UUID;

/**
 * Because EntityRenderState doesn't contain the actual entity instance, we need to add it manually.
 */
@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements EntityRenderStateAccess {

    @Unique public AvatarView<UUID> avatarView;
    @Unique public FiguraPartSubmit partSubmit;
    @Unique public FiguraCallbackSubmit codeSubmit;

    @Override public @Nullable AvatarView<UUID> figura_client$getAvatarView() { return avatarView; }
    @Override public void figura_client$setAvatarView(@Nullable AvatarView<UUID> view) { this.avatarView = view; }
    @Override public @Nullable FiguraPartSubmit figura_client$getPartSubmit() { return partSubmit; }
    @Override public void figura_client$setPartSubmit(@Nullable FiguraPartSubmit submit) { this.partSubmit = submit; }
    @Override public @Nullable FiguraCallbackSubmit figura_client$getCodeSubmit() { return codeSubmit; }
    @Override public void figura_client$setCodeSubmit(@Nullable FiguraCallbackSubmit submit) { this.codeSubmit = submit; }
}
