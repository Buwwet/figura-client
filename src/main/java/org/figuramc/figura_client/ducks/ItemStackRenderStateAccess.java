package org.figuramc.figura_client.ducks;

import net.minecraft.world.item.ItemStack;

/**
 * Access the new fields in ItemStackRenderStateMixin
 */
public interface ItemStackRenderStateAccess {
    ItemStack figura_client$getItemStack();
    void figura_client$setItemStack(ItemStack itemStack);
}
