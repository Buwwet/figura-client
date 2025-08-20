package org.figuramc.figura_client.mixin.entity_render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura_client.ducks.EntityRenderStateAccess;
import org.figuramc.figura_client.game_data.MinecraftEntityImpl;
import org.figuramc.figura_client.renderer.CompatibleRenderer;
import org.figuramc.figura_core.avatars.components.EntityRoot;
import org.figuramc.figura_core.manage.AvatarManagers;
import org.figuramc.figura_core.manage.AvatarView;
import org.figuramc.figura_core.util.FiguraTransformStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(EntityRenderer.class)
public class EntityRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    public void onRender(EntityRenderState renderState, PoseStack poseStack, MultiBufferSource multiBufferSource, int light, CallbackInfo ci) {
        // If this is a living entity renderer, then the more specific mixin will already have been run.
        // So this one is unnecessary.
        if ((Object) this instanceof LivingEntityRenderer<?,?,?>)
            return;

        // TODO inject at the callsite and invoke the entity_render event there, before any rendering occurs(? maybe?)

        // Otherwise, render the Avatar's entity root if it exists.
        Entity entity = ((EntityRenderStateAccess) renderState).figura_client$getEntity();
        try (AvatarView<UUID> avatar = AvatarManagers.tryGetEntityAvatar(new MinecraftEntityImpl(entity))) {
            if (avatar == null) return;
            EntityRoot root = avatar.get().getComponent(EntityRoot.TYPE);
            if (root != null) {
                float tickDelta = ((EntityRenderStateAccess) renderState).figura_client$getTickDelta();
                if (root.root.renderer instanceof CompatibleRenderer renderer) {
                    FiguraTransformStack stack = new FiguraTransformStack();
                    stack.peekPosition().set(poseStack.last().pose());
                    stack.peekNormal().set(poseStack.last().normal());
                    renderer.setup(multiBufferSource, stack, tickDelta, light, OverlayTexture.NO_OVERLAY);
                    avatar.get().tryRenderModelPart(renderer);
                }
            }
        }
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    public void extractEntityInstance(Entity entity, EntityRenderState entityRenderState, float tickDelta, CallbackInfo ci) {
        ((EntityRenderStateAccess) entityRenderState).figura_client$setEntity(entity);
        ((EntityRenderStateAccess) entityRenderState).figura_client$setTickDelta(tickDelta);
    }
}