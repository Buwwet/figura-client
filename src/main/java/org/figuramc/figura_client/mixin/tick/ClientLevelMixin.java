package org.figuramc.figura_client.mixin.tick;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura_client.game_data.MinecraftEntityImpl;
import org.figuramc.figura_client.game_data.MinecraftWorldImpl;
import org.figuramc.figura_core.manage.AvatarManagers;
import org.figuramc.figura_core.manage.AvatarView;
import org.figuramc.figura_core.script_hooks.Event;
import org.figuramc.figura_core.script_hooks.callback.CallbackType;
import org.figuramc.figura_core.script_hooks.callback.items.CallbackItem;
import org.figuramc.figura_core.script_hooks.callback.items.EntityView;
import org.figuramc.figura_core.script_hooks.callback.items.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.UUID;

// Mixin to run the entity_tick event
@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Inject(method = "tickNonPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    public void afterTick(Entity entity, CallbackInfo ci) {
        callTickMethod(entity);
    }

    @Inject(method = "tickPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;rideTick()V"))
    public void afterRideTick(Entity vehicle, Entity rider, CallbackInfo ci) {
        callTickMethod(rider);
    }

    @Unique
    private void callTickMethod(Entity entity) {
        AvatarView<UUID> avatar = AvatarManagers.tryGetEntityAvatar(new MinecraftEntityImpl<>(entity));

        if (avatar == null) return;
        avatar.use(a -> {
            try (
                    EntityView<MinecraftEntityImpl<?>> entityView = new EntityView<>(new MinecraftEntityImpl<>(entity));
                    WorldView<MinecraftWorldImpl> worldView = new WorldView<>(new MinecraftWorldImpl((ClientLevel) (Object) this))
            ) {
                a.getEventListener(Event.ENTITY_TICK).invoke(new CallbackItem.Tuple2<>(entityView, worldView));
            }
        });
    }

}
