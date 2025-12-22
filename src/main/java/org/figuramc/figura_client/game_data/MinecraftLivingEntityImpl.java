package org.figuramc.figura_client.game_data;

import net.minecraft.world.entity.LivingEntity;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftLivingEntity;

public class MinecraftLivingEntityImpl<T extends LivingEntity> extends MinecraftEntityImpl<T> implements MinecraftLivingEntity {
    public MinecraftLivingEntityImpl(T livingEntity) {
        super(livingEntity);
    }
}
