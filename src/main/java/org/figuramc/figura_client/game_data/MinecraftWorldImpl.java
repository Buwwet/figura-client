package org.figuramc.figura_client.game_data;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.MoonPhase;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.figuramc.figura_client.FiguraClient;
import org.figuramc.figura_core.minecraft_interop.game_data.MinecraftIdentifier;
import org.figuramc.figura_core.minecraft_interop.game_data.MinecraftWorld;
import org.figuramc.figura_core.minecraft_interop.game_data.block.MinecraftBlockState;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftEntity;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftPlayer;
import org.figuramc.figura_core.util.functional.BiThrowingConsumer;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record MinecraftWorldImpl(ClientLevel level) implements MinecraftWorld {

    @Override
    public @Nullable MapData getMapData(int id) {
        MapItemSavedData data = level.getMapData(new MapId(id));
        if (data == null) return null;

        // TODO: Add the decorations back in
        return new MapData(data.centerX, data.centerZ, data.locked, data.scale);

//        ArrayList<HashMap<String, Object>> decorations = new ArrayList<>();
//        for (MapDecoration decoration : data.getDecorations()) {
//            HashMap<String, Object> decorationMap = new HashMap<>();
//            decorationMap.put("type", decoration.type().toString());
//            decorationMap.put("name", decoration.name().isEmpty() ? "" : decoration.name());
//            decorationMap.put("x", decoration.x());
//            decorationMap.put("y", decoration.y());
//            decorationMap.put("rot", decoration.rot());
//            decorationMap.put("image", decoration.getSpriteLocation());
//            decorations.add(decorationMap);
//        }
//        map.put("decorations", decorations);
    }

    @Override
    public <E1 extends Throwable, E2 extends Throwable> void forEachPlayer(BiThrowingConsumer<MinecraftPlayer, E1, E2> consumer) throws E1, E2 {
        for (Player player : level.players())
            consumer.accept(new MinecraftPlayerImpl<>(player));
    }

    @Override
    public <E1 extends Throwable, E2 extends Throwable> void forEachEntity(BiThrowingConsumer<MinecraftEntity, E1, E2> consumer) throws E1, E2 {
        for (Entity entity : level.entitiesForRendering()) // On 1.21.11, this gets ALL entities as expected
            consumer.accept(new MinecraftEntityImpl<>(entity));
    }

    @Override
    public MinecraftBlockState getBlockState(int x, int y, int z) {
        BlockPos blockPos = new BlockPos(x, y ,z);
        return new MinecraftBlockStateImpl(level.getBlockState(blockPos), blockPos);
    }

//    @Override
//    public MinecraftBlockState newBlock(String string, int x, int y, int z) {
//        BlockPos pos = new BlockPos(x, y, z);
//        try {
//            Level level = getLevel();
//            BlockState block = BlockStateArgument.block(CommandBuildContext.simple(level.registryAccess(), level.enabledFeatures())).parse(new StringReader(string)).getState();
//            return new MinecraftBlockStateImpl(block, pos);
//        } catch (Exception e) {
//            // TODO: use Figura Exception
//            throw new RuntimeException("Could not parse block state from string: " + string);
//        }
//    }
//
//    @Override
//    public MinecraftItemStack newItem(String string, int count, int damage) {
//        try {
//            Level level = getLevel();
//            ItemStack item = ItemArgument.item(CommandBuildContext.simple(level.registryAccess(), level.enabledFeatures())).parse(new StringReader(string)).createItemStack(1, false);
//            item.setCount(count);
//            item.setDamageValue(damage);
//            return new MinecraftItemStackImpl(item);
//        } catch (Exception e) {
//            // TODO: Use FiguraException
//            throw new RuntimeException("Could not parse item stack from string: " + string);
//        }
//    }

    @Override
    public MinecraftEntity getEntity(UUID uuid) {
        Entity e = level.getEntity(uuid);
        return e == null ? null : new MinecraftEntityImpl<>(e);
    }

    @Override
    public MinecraftIdentifier getDimension() {
        return FiguraClient.coreIdent(level.dimension().identifier());
    }

    @Override
    public int getRedstonePower(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (level.getChunkAt(pos).isEmpty()) return 0;
        return level.getBestNeighborSignal(pos);
    }

    @Override
    public int getStrongRedstonePower(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (level.getChunkAt(pos).isEmpty()) return 0;
        return level.getDirectSignalTo(pos);
    }

    // TODO: Should moon phase use EnumLike as well, like poses and stuff?
    @Override
    public int getMoonPhase() {
        // Block pos is needed here because..........................
        // This might be the most overengineered thing I have ever seen, this is some DFU level stuff
        // Don't look at the code behind this
        MoonPhase phase = level.environmentAttributes().getValue(EnvironmentAttributes.MOON_PHASE, BlockPos.ZERO);
        return phase.index();
    }

    @Override
    public int getLightLevel(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (level.getChunkAt(pos).isEmpty()) return 0;
        level.updateSkyBrightness();
        return level.getLightEngine().getRawBrightness(pos, level.getSkyDarken());
    }

    @Override
    public int getSkyLightLevel(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (level.getChunkAt(pos).isEmpty()) return 0;
        level.updateSkyBrightness();
        return level.getBrightness(LightLayer.SKY, pos);
    }

    @Override
    public int getBlockLightLevel(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (level.getChunkAt(pos).isEmpty()) return 0;
//        level.updateSkyBrightness(); // Sky brightness update shouldn't affect block light?
        return level.getBrightness(LightLayer.BLOCK, pos);
    }

    @Override public int getHeight() { return level.getHeight(); }

    @Override public long getTime() { return level.getGameTime(); }
    @Override public long getTimeOfDay() { return level.getDayTime() % 24000L; }
    @Override public long getDay() { return level.getDayTime() / 24000L; }

    @Override public float getRainGradient(float tickDelta) { return level.getRainLevel(tickDelta); }

    @Override public boolean isChunkLoaded(int x, int y, int z) { return !level.getChunkAt(new BlockPos(x, y, z)).isEmpty(); }

    @Override
    public boolean isThundering() { return level.isThundering(); }

    @Override
    public boolean isOpenSky(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        if (level.getChunkAt(pos).isEmpty()) return true;
        level.updateSkyBrightness();
        return level.canSeeSky(pos);
    }

}
