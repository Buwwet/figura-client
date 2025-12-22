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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.item.equipment.Equippable;
import org.figuramc.figura_client.FiguraClient;
import org.figuramc.figura_core.minecraft_interop.game_data.MinecraftIdentifier;
import org.figuramc.figura_core.minecraft_interop.game_data.block.MinecraftBlockState;
import org.figuramc.figura_core.minecraft_interop.game_data.item.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record MinecraftItemStackImpl(ItemStack stack) implements MinecraftItemStack {

    @Override
    public MinecraftIdentifier getIdentifier() {
        return stack.getItemHolder().unwrapKey().map(key -> FiguraClient.coreIdent(key.identifier())).orElse(FiguraClient.UNKNOWN);
    }

    @Override
    public List<MinecraftIdentifier> getTags() {
        return stack.getTags().map(tag -> FiguraClient.coreIdent(tag.location())).toList();
    }

    @Override
    public MinecraftBlockState getBlockState() {
        if (stack.getItem() instanceof BlockItem blockItem)
            return new MinecraftBlockStateImpl(blockItem.getBlock().defaultBlockState(), null);
        return null;
    }

    @Override
    public ItemUseAction getUseAction() {
        // Looks ugly, but if new animations are added and we don't deal with them, this will fail to compile
        return switch (stack.getUseAnimation()) {
            case NONE -> ItemUseAction.NONE;
            case EAT -> ItemUseAction.EAT;
            case DRINK -> ItemUseAction.DRINK;
            case BLOCK -> ItemUseAction.BLOCK;
            case BOW -> ItemUseAction.BOW;
            case TRIDENT -> ItemUseAction.TRIDENT;
            case CROSSBOW -> ItemUseAction.CROSSBOW;
            case SPYGLASS -> ItemUseAction.SPYGLASS;
            case TOOT_HORN -> ItemUseAction.TOOT_HORN;
            case BRUSH -> ItemUseAction.BRUSH;
            case BUNDLE -> ItemUseAction.BUNDLE;
            case SPEAR -> ItemUseAction.SPEAR;
        };
    }

    @Override
    public String getName() {
        return stack.getHoverName().getString();
    }

    @Override
    public ItemRarity getRarity() {
        // Looks ugly, but if new rarities are added and we don't deal with them, this will fail to compile
        return switch (stack.getRarity()) {
            case COMMON -> ItemRarity.COMMON;
            case UNCOMMON -> ItemRarity.UNCOMMON;
            case RARE -> ItemRarity.RARE;
            case EPIC -> ItemRarity.EPIC;
        };
    }

    @Override
    public String toStackString() {
        //TODO NBT discussion
        return "";
    }

    @Override
    public @Nullable EquipmentSlot getEquipmentSlot() {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null) return null;
        // Looks ugly, but if new slots are added and we don't deal with them, this will fail to compile
        return switch (equippable.slot()) {
            case MAINHAND -> EquipmentSlot.MAINHAND;
            case OFFHAND -> EquipmentSlot.OFFHAND;
            case FEET -> EquipmentSlot.FEET;
            case LEGS -> EquipmentSlot.LEGS;
            case CHEST -> EquipmentSlot.CHEST;
            case HEAD -> EquipmentSlot.HEAD;
            case BODY -> EquipmentSlot.BODY;
            case SADDLE -> EquipmentSlot.SADDLE;
        };
    }

    @Override public int getCount() { return stack.getCount(); }
    @Override public int getDamage() { return stack.getDamageValue(); }
    @Override public int getPopTime() { return stack.getPopTime(); }
    @Override public int getMaxDamage() { return stack.getMaxDamage(); }

    @Override
    public int getRepairCost() {
        Integer repair_cost = stack.get(DataComponents.REPAIR_COST);
        if (repair_cost == null)
                return 0;
        return repair_cost;
    }

    // TODO: Give this a MinecraftLivingEntity param instead of implicitly using the local player?
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
