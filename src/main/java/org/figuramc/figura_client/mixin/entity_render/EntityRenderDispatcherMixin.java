package org.figuramc.figura_client.mixin.entity_render;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura_client.FiguraClient;
import org.figuramc.figura_core.manage.AvatarManagers;
import org.figuramc.figura_core.manage.AvatarView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.UUID;

/**
 * - Manages the Avatar rendering stack, for vanilla parts usage
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Shadow public abstract <T extends Entity> EntityRenderer<? super T, ?> getRenderer(T entity);

    // x, y, z are the entity's position in world space relative to the camera.
    // Not relevant to the mixin, just felt like explaining it.
    @WrapMethod(method = "render(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    public void pushPopAvatar(Entity entity, double x, double y, double z, float tickDelta, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Operation<Void> original) {
        // Push the avatar before rendering, and pop afterward.
        try(AvatarView<UUID> view = AvatarManagers.ENTITIES.get(entity.getUUID())) {
            FiguraClient.AVATAR_RENDERING_STACK.push(view);
            FiguraClient.IS_LIVING_ENTITY_STACK.push(getRenderer(entity) instanceof LivingEntityRenderer);
            original.call(entity, x, y, z, tickDelta, poseStack, multiBufferSource, i);
        } finally {
            FiguraClient.AVATAR_RENDERING_STACK.pop();
            FiguraClient.IS_LIVING_ENTITY_STACK.pop();
        }
    }

}
