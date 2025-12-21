package org.figuramc.figura_client.game_data;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
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

//    @Override
//    public @Nullable MinecraftItem getItem(String identifier) {
//        Identifier loc = Identifier.tryParse(identifier);
//        if (loc == null) return null;
//        Optional<Item> item = BuiltInRegistries.ITEM.getOptional(loc);
//        if (item.isEmpty()) return null;
//        return new MinecraftItemImpl(item.get());
//    }

    @Override
    public float[] getWindowSize() {
        Window window = Minecraft.getInstance().getWindow();
        return new float[] { window.getWidth(), window.getHeight() }; // In pixels
    }

    @Override
    public float[] getScaledWindowSize() {
        Window window = Minecraft.getInstance().getWindow();
        return new float[] { window.getGuiScaledWidth(), window.getGuiScaledHeight() }; // In GUI units
    }

    @Override
    public float[] getMousePosition() {
        Window window = Minecraft.getInstance().getWindow();
        MouseHandler mouseHandler = Minecraft.getInstance().mouseHandler;
        // xpos() and ypos() are in "screen units", whatever that means, so we convert to pixels.
        float xPixels = (float) ((mouseHandler.xpos() / window.getScreenWidth()) * window.getWidth());
        float yPixels = (float) ((mouseHandler.ypos() / window.getScreenHeight()) * window.getHeight());
        return new float[] { xPixels, yPixels };
    }

    @Override
    public float[] getScaledMousePosition() {
        Window window = Minecraft.getInstance().getWindow();
        MouseHandler mouseHandler = Minecraft.getInstance().mouseHandler;
        return new float[] { (float) mouseHandler.getScaledXPos(window), (float) mouseHandler.getScaledYPos(window) };
    }

    @Override
    public float getGuiScale() {
        return Minecraft.getInstance().getWindow().getGuiScale();
    }
}
