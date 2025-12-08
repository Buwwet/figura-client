package org.figuramc.figura_client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.figuramc.figura_core.manage.AvatarManagers;
import org.figuramc.figura_core.script_hooks.Event;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Inject(method = "tick", at = @At("RETURN"))
    private void endOfTick(CallbackInfo ci) {
        // Tick the things that need ticking
        AvatarManagers.pollAll();
        AvatarManagers.forEachAvatar(avatar -> {
            avatar.tick();
            avatar.getEventListener(Event.CLIENT_TICK).invoke(CallbackItem.Unit.INSTANCE);
        });
    }

    // Clear avatars when leaving the level. TODO come up with a cleaner way?
    @Inject(method = "updateLevelInEngines", at = @At("HEAD"))
    private void clearLevelHook(ClientLevel clientLevel, CallbackInfo ci) {
        if (clientLevel == null) {
            AvatarManagers.ENTITIES.clear();
            AvatarManagers.GUIS.clear();
        }
    }

}
