package org.figuramc.figura_client.game_data;

import com.mojang.brigadier.StringReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.timeline.Timelines;
import org.figuramc.figura_core.minecraft_interop.game_data.MinecraftWorld;
import org.figuramc.figura_core.minecraft_interop.game_data.block.MinecraftBlockState;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftEntity;
import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItem;
import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItemStack;
import org.figuramc.figura_core.util.exception.FiguraException;

import java.util.*;

public record MinecraftWorldImpl() implements MinecraftWorld {

    @Override
    public Map<String, MinecraftEntity> getPlayers() {
        HashMap<String, MinecraftEntity> map = new HashMap<>();
        for (AbstractClientPlayer player : getLevel().players()) {
            map.put(player.getName().toString(), new MinecraftEntityImpl(player.getLivingEntity()));
        }
        return map;
    }

    @Override
    public List<MinecraftEntity> getEntities(int x1, int y1, int z1, int x2, int y2, int z2) {
        ArrayList<MinecraftEntity> list = new ArrayList<>();
        AABB aabb = new AABB(x1, y1, z1, x2, y2, z2);
        // Wrap all entities inside
        for (Entity entity : getLevel().getEntitiesOfClass(Entity.class, aabb)) {
            list.add(new MinecraftEntityImpl(entity));
        }
        return list;
    }

    @Override
    public HashMap<String, Object> getMapData(int id) {
        MapItemSavedData data = getLevel().getMapData(new MapId(id));
        if (data == null)
            return null;

        HashMap<String, Object> map = new HashMap<>();

        map.put("center_x", data.centerX);
        map.put("center_z", data.centerZ);
        map.put("locked", data.locked);
        map.put("scale", data.scale);

        ArrayList<HashMap<String, Object>> decorations = new ArrayList<>();
        for (MapDecoration decoration : data.getDecorations()) {
            HashMap<String, Object> decorationMap = new HashMap<>();
            decorationMap.put("type", decoration.type().toString());
            decorationMap.put("name", decoration.name().isEmpty() ? "" : decoration.name());
            decorationMap.put("x", decoration.x());
            decorationMap.put("y", decoration.y());
            decorationMap.put("rot", decoration.rot());
            decorationMap.put("image", decoration.getSpriteLocation());
            decorations.add(decorationMap);
        }
        map.put("decorations", decorations);

        return map;
    }

    @Override
    public List<MinecraftBlockState> getBlocks(int x, int y, int z, int w, int t, int h) {
        List<MinecraftBlockState> list = new ArrayList<>();

        BlockPos min = new BlockPos(x, y, z);
        BlockPos max = new BlockPos(w, t, h);
        max = new BlockPos(
                Math.min(min.getX() + 8, max.getX()),
                Math.min(min.getY() + 8, max.getY()),
                Math.min(min.getZ() + 8, max.getZ())
        );
        if (min.compareTo(max) > 0) {
            // TODO Implement Figura Errors
            throw new RuntimeException("Your max value can't be smaller than your min!");
        }
        Level level = getLevel();
        if (!level.hasChunksAt(min, max))
            return list;

        BlockPos.betweenClosedStream(min, max).forEach(blockPos -> {
            BlockPos pos = new BlockPos(blockPos);
            list.add(new MinecraftBlockStateImpl(level.getBlockState(pos), pos));
        });
        return list;
    }

    @Override
    public MinecraftBlockState getBlockState(int x, int y, int z) {
        BlockPos blockPos = new BlockPos(x, y ,z);
        return new MinecraftBlockStateImpl(getLevel().getBlockState(new BlockPos(x, y ,z)), blockPos);
    }

    @Override
    public MinecraftBlockState newBlock(String string, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        try {
            Level level = getLevel();
            BlockState block = BlockStateArgument.block(CommandBuildContext.simple(level.registryAccess(), level.enabledFeatures())).parse(new StringReader(string)).getState();
            return new MinecraftBlockStateImpl(block, pos);
        } catch (Exception e) {
            // TODO: use Figura Exception
            throw new RuntimeException("Could not parse block state from string: " + string);
        }
    }

    @Override
    public MinecraftItemStack newItem(String string, int count, int damage) {
        try {
            Level level = getLevel();
            ItemStack item = ItemArgument.item(CommandBuildContext.simple(level.registryAccess(), level.enabledFeatures())).parse(new StringReader(string)).createItemStack(1, false);
            item.setCount(count);
            item.setDamageValue(damage);
            return new MinecraftItemStackImpl(item);
        } catch (Exception e) {
            // TODO: Use FiguraException
            throw new RuntimeException("Could not parse item stack from string: " + string);
        }
    }

    @Override
    public MinecraftEntity getEntity(UUID uuid) {
        return new MinecraftEntityImpl(getLevel().getEntity(uuid));
    }

    @Override
    public String getCurrentDimension() {
        return getLevel().dimension().identifier().toString();
    }

    @Override
    public int getRedstonePower(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        // Check that chunk is loaded
        if (getLevel().getChunkAt(pos).isEmpty())
            return 0;
        return getLevel().getBestNeighborSignal(pos);
    }

    @Override
    public int getStrongRedstonePower(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        // Check that chunk is loaded
        if (getLevel().getChunkAt(pos).isEmpty())
            return 0;
        return getLevel().getDirectSignalTo(pos);
    }

    @Override
    public int getMoonPhase() {
        // TODO getLevel().getMoonPhase() no longer exist and no references to moon or lunar
        //Timelines.MOON?
        return 0;
    }

    @Override
    public int getLightLevel(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        Level level = getLevel();
        if (level.getChunkAt(pos).isEmpty())
            return 0;

        level.updateSkyBrightness();
        return level.getLightEngine().getRawBrightness(pos, level.getSkyDarken());
    }

    @Override
    public int getSkyLightLevel(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        Level level = getLevel();
        if (level.getChunkAt(pos).isEmpty())
            return 0;

        level.updateSkyBrightness();
        return level.getBrightness(LightLayer.SKY, pos);
    }

    @Override
    public int getBlockLightLevel(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        Level level = getLevel();
        if (level.getChunkAt(pos).isEmpty())
            return 0;

        level.updateSkyBrightness();
        return level.getBrightness(LightLayer.BLOCK, pos);
    }

    @Override
    public int getHeight() { return getLevel().getHeight(); }

    @Override
    public double getTime(double delta) {
        return getLevel().getGameTime() + delta;
    }

    @Override
    public double getTimeOfDay(double delta) {
        return getLevel().getDayTime() + delta;
    }

    @Override
    public double getDayTime(double delta) {
        return (getLevel().getDayTime() + delta) % 24000;
    }

    @Override
    public double getDay(double delta) {
        return Math.floor((getLevel().getDayTime() + delta) / 24000);
    }

    @Override
    public double getRainGradient(Float delta) {
        if (delta == null)
            delta = 1.0f;
        return 0;
    }

    @Override
    public boolean isChunkLoaded(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        return !getLevel().getChunkAt(pos).isEmpty();
    }

    @Override
    public boolean isThundering(int x, int y, int z) { return getLevel().isThundering(); }

    @Override
    public boolean isOpenSky(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        Level level = getLevel();
        if (level.getChunkAt(pos).isEmpty())
            return false;

        level.updateSkyBrightness();
        return level.canSeeSky(pos);
    }

    ///  Helper to get the current level.
    private ClientLevel getLevel() {
        return Minecraft.getInstance().level;
    }
}
