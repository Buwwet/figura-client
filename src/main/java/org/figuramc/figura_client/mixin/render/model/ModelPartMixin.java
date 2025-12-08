package org.figuramc.figura_client.mixin.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import org.figuramc.figura_client.ducks.ModelPartAccess;
import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaPart;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPart.class)
public class ModelPartMixin implements ModelPartAccess {

    // Corresponding VanillaPart
    @Unique public @Nullable VanillaPart vanillaPart;

    // New "position" variable, respecting Figura transforms
    // It's in Minecraft's coordinate space, meaning x/y values are negated if it's for a LivingEntity
    @Unique public Vector3f position = new Vector3f();

    // Get/set
    @Override public @Nullable VanillaPart figura_client$getVanillaPart() { return vanillaPart; }
    @Override public void figura_client$setVanillaPart(@Nullable VanillaPart part) { this.vanillaPart = part; }
    @Override public Vector3f figura_client$getPosition() { return position; }

    // At the end of translation, incorporate Figura's new "position" variable into the transformation
    @Inject(method = "translateAndRotate", at = @At("RETURN"))
    public void usePosition(PoseStack poseStack, CallbackInfo ci) {
        poseStack.translate(position.x / 16.0f, position.y / 16.0f, position.z / 16.0f);
    }

}
