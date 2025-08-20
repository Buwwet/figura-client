package org.figuramc.figura_client.game_data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItem;

public record MinecraftItemImpl(Item item) implements MinecraftItem {

    @Override
    public String identifier() {
        return BuiltInRegistries.ITEM.getKey(item).toString();
    }

}
