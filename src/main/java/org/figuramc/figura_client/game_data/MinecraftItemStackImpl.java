package org.figuramc.figura_client.game_data;

import net.minecraft.world.item.ItemStack;
import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItem;
import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItemStack;

public record MinecraftItemStackImpl(ItemStack stack) implements MinecraftItemStack {

    @Override
    public MinecraftItem getItem() {
        return new MinecraftItemImpl(stack.getItem());
    }

}
