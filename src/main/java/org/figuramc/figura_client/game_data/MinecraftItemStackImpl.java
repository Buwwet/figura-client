package org.figuramc.figura_client.game_data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.properties.numeric.UseDuration;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.item.equipment.Equippable;
import org.figuramc.figura_core.minecraft_interop.game_data.block.MinecraftBlockState;
import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItem;
import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record MinecraftItemStackImpl(ItemStack stack) implements MinecraftItemStack {

    @Override
    public List<String> getTags() {
        List<String> list = new ArrayList<>();

        for (TagKey<Item> itemTagKey : stack.getTags().toList())
            list.add(itemTagKey.location().toString());

        return list;
    }

    @Override
    public MinecraftItemStack copy() { return new MinecraftItemStackImpl(stack.copy()); }

    @Override
    public MinecraftBlockState getBlockState() {
        if (stack.getItem() instanceof BlockItem blockItem)
            return new MinecraftBlockStateImpl(blockItem.getBlock().defaultBlockState(), null);
        return null;
    }

    @Override
    public MinecraftItem getItem() {
        return new MinecraftItemImpl(stack.getItem());
    }

    // TODO
    @Override
    public Object getTag() {
        return null;
    }

    @Override
    public String getUseAction() {
        return stack.getUseAnimation().name();
    }

    @Override
    public String getName() {
        return stack.getHoverName().getString();
    }

    @Override
    public String getID() {
        return stack.getItem().getName().getString();
    }

    @Override
    public String getRarity() {
        return stack.getRarity().name();
    }

    @Override
    public String toStackString() {
        //TODO NBT discussion
        return "";
    }

    @Override @Nullable
    public String getEquipmentSlot() {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null)
            return null;
        return equippable.slot().name();
    }

    @Override
    public int getCount() {
        return stack.getCount();
    }

    @Override
    public int getDamage() { return stack.getDamageValue(); }

    @Override
    public int getPopTime() { return stack.getPopTime(); }

    @Override
    public int getMaxDamage() {
        return stack.getMaxDamage();
    }

    @Override
    public int getRepairCost() {
        Integer repair_cost = stack.get(DataComponents.REPAIR_COST);
        if (repair_cost == null)
                return 0;
        return repair_cost;
    }

    @Override
    public int getUseDuration() {
        // Now requires an entity, passing ourselves.
        if (Minecraft.getInstance().player == null)
            return 0;
        return stack.getUseDuration(Minecraft.getInstance().player);
    }

    @Override
    public boolean hasGlint() { return stack.hasFoil(); }

    @Override
    public boolean isBlockItem() {
        return stack.getItem() instanceof BlockItem;
    }

    @Override
    public boolean isFood() {
        // If food data is found, return true.
        FoodProperties food = stack.get(DataComponents.FOOD);
        return food != null;
    }

    @Override
    public boolean isEnchantable() { return stack.isEnchantable(); }

    @Override
    public boolean isDamageable() {
        return stack.isDamageableItem();
    }

    @Override
    public boolean isStackable() {
        return stack.isStackable();
    }

    @Override
    public boolean isArmor() {
        // If it has the equippable tag, it must be armor.
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null;
    }

    @Override
    public boolean isTool() {
        Tool tool = stack.get(DataComponents.TOOL);
        return tool != null;
    }


}
