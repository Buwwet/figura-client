package org.figuramc.figura_client.game_data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.figuramc.figura_core.minecraft_interop.game_data.GameDataProvider;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftEntity;
import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItem;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class GameDataProviderImpl implements GameDataProvider {

    @Override
    public UUID getLocalUUID() {
        return Minecraft.getInstance().getUser().getProfileId();
    }

    @Override
    public @Nullable MinecraftEntity getEntity(UUID uuid) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) return null;
        return new MinecraftEntityImpl(level.getEntity(uuid));
    }

    @Override
    public @Nullable MinecraftItem getItem(String identifier) {
        ResourceLocation loc = ResourceLocation.tryParse(identifier);
        if (loc == null) return null;
        Optional<Item> item = BuiltInRegistries.ITEM.getOptional(loc);
        if (item.isEmpty()) return null;
        return new MinecraftItemImpl(item.get());
    }
}
