package org.figuramc.figura_client.mixin.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.figuramc.figura_client.ducks.ModelSubmitAccess;
import org.figuramc.figura_client.util.RenderUtils;
import org.figuramc.figura_core.manage.AvatarView;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Add field to the model submit indicating which avatar this is part of
@Mixin(SubmitNodeStorage.ModelSubmit.class)
public class ModelSubmitMixin implements ModelSubmitAccess {

    @Unique public @Nullable AvatarView<?> avatar;
    @Unique public boolean isLivingEntity;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void setFields(PoseStack.Pose pose, Model<?> model, Object object, int i, int j, int k, TextureAtlasSprite textureAtlasSprite, int l, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, CallbackInfo ci) {
        // Grab data through the stack!
        this.avatar = RenderUtils.AVATAR_SUBMITTING_STACK.peek();
        this.isLivingEntity = RenderUtils.IS_LIVING_ENTITY_STACK.peek() == Boolean.TRUE;
    }

    @Override
    public AvatarView<?> figura_client$getAvatar() {
        return avatar;
    }

    @Override
    public boolean figura_client$isLivingEntity() {
        return isLivingEntity;
    }
}
