package org.figuramc.figura_client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.figuramc.figura_client.game_data.MinecraftEntityImpl;
import org.figuramc.figura_client.game_data.MinecraftWorldImpl;
import org.figuramc.figura_core.manage.AvatarManagers;
import org.figuramc.figura_core.script_hooks.Event;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.figura_core.script_hooks.callback.items.EntityView;
import org.figuramc.figura_core.script_hooks.callback.items.WorldView;
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
             // TODO: What do we do if level is null here?
            // Always invoke CLIENT_TICK:
            avatar.getEventListener(Event.CLIENT_TICK).invoke(CallbackItem.Unit.INSTANCE);
            // Invoke WORLD_TICK if the world is non-null:
            ClientLevel level = Minecraft.getInstance().level;
            if (level != null) {
                try (WorldView<MinecraftWorldImpl> worldView = new WorldView<>(new MinecraftWorldImpl(level))) {
                    avatar.getEventListener(Event.WORLD_TICK).invoke(worldView);
                }
            }
        });
    }

    // Clear avatars when leaving the level. TODO come up with a cleaner way?
    @Inject(method = "updateLevelInEngines(Lnet/minecraft/client/multiplayer/ClientLevel;Z)V", at = @At("HEAD"))
    private void clearLevelHook(ClientLevel clientLevel, boolean stopSounds, CallbackInfo ci) {
        if (clientLevel == null) {
            AvatarManagers.ENTITIES.clear();
            AvatarManagers.GUIS.clear();
        }
    }

}
