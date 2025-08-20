package org.figuramc.figura_client.mixin.general_render;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.figuramc.figura_core.manage.AvatarManagers;
import org.figuramc.figura_core.script_hooks.Event;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    public void preRender(DeltaTracker deltaTracker, boolean bl, CallbackInfo ci) {
        // Run the render event on each avatar:
        float tickDelta = deltaTracker.getGameTimeDeltaPartialTick(true);
        AvatarManagers.forEachAvatar(avatar -> {
            avatar.runEvent(Event.CLIENT_RENDER, new CallbackItem.F32(tickDelta));
        });
    }

}
