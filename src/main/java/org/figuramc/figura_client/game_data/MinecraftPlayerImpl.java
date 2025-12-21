package org.figuramc.figura_client.game_data;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftLivingEntity;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftPlayer;

public class MinecraftPlayerImpl<T extends Player> extends MinecraftLivingEntityImpl<T> implements MinecraftPlayer {
    public MinecraftPlayerImpl(T player) {
        super(player);
    }
}
