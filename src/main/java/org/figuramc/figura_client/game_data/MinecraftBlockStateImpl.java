package org.figuramc.figura_client.game_data;

import net.fabricmc.fabric.mixin.content.registry.BlockBehaviourAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ColorMapColorUtil;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.figuramc.figura_core.minecraft_interop.game_data.block.MinecraftBlockState;
import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItem;
import org.joml.Vector3d;

import java.util.*;

public record MinecraftBlockStateImpl(BlockState blockState, BlockPos blockPos) implements MinecraftBlockState {
    @Override
    public String getId() {
        // TODO Check that it gives minecraft:block_id
        return blockState.getBlock().getName().toString();
    }

    @Override
    public Vector3d getPos() {
        return new Vector3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    @Override
    public List<List<Vector3d>> getCollisionShape() {
        return voxelShapeToTable(blockState.getCollisionShape(getLevel(), blockPos));
    }

    @Override
    public List<List<Vector3d>> getOutlineShape() {
        return voxelShapeToTable(blockState.getShape(getLevel(), blockPos));
    }

    @Override
    public HashMap<String, Set<String>> getTextures() {
        // Direct port from fig 0.1.0
        HashMap<String, Set<String>> map = new HashMap<>();

        RenderShape renderShape = blockState.getRenderShape();

        if (renderShape == RenderShape.MODEL) {
            BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

            BlockStateModel bakedModel = blockRenderer.getBlockModel(blockState);
            RandomSource randomSource = RandomSource.create();
            long seed = 42L;

            for (Direction direction : Direction.values())
                map.put(direction.name(), getTexturesForFace(direction, randomSource, bakedModel, seed));
            map.put("NONE", getTexturesForFace(null, randomSource, bakedModel, seed));

            TextureAtlasSprite particle = blockRenderer.getBlockModelShaper().getParticleIcon(blockState);
            map.put("PARTICLE", Set.of(getTextureName(particle)));
        }
        // TODO: RenderShape.ENTITYBLOCK_ANIMATED no longer exists.
        //else if (renderShape == RenderShape.ENTITYBLOCK_ANIMATED) {
            //map.put("PARTICLE", Set.of(getTextureName(Minecraft.getInstance().getItemRenderer().getModel(blockState.getBlock().asItem().getDefaultInstance(), WorldAPI.getCurrentWorld(), null, 42).getParticleIcon())));
       //}
        return map;
    }

    @Override
    public Map<String, Object> getSounds() {
        Map<String, Object> sounds = new HashMap<>();
        SoundType snd = blockState.getSoundType();

        sounds.put("pitch", snd.getPitch());
        sounds.put("volume", snd.getVolume());
        sounds.put("break", snd.getBreakSound().location().toString());
        sounds.put("fall", snd.getFallSound().location().toString());
        sounds.put("hit", snd.getHitSound().location().toString());
        sounds.put("place", snd.getPlaceSound().location().toString());
        sounds.put("step", snd.getStepSound().location().toString());

        return sounds;
    }

    @Override
    public List<String> getProperties() {
        ArrayList<String> properties = new ArrayList<>();
        for (Property<?> prop : blockState.getProperties()) {
            properties.add(prop.getName());
        }
        return List.of();
    }

    @Override
    public List<String> getTags() {
        List<String> list = new ArrayList<>();
        // Get the registry
        Registry<Block> registry = getLevel().registryAccess().getOrThrow(Registries.BLOCK).value();
        Optional<ResourceKey<Block>> key = registry.getResourceKey(blockState.getBlock());

        if (key.isEmpty())
            return list;
        // TODO
        /*
        for (TagKey<Block> blockTagKey : registry.getHolderOrThrow(key.get()).tags().toList())
            list.add(blockTagKey.location().toString());
        */
        return list;
    }

    @Override
    public List<String> getFluidTags() {
        List<String> list = new ArrayList<>();
        for (TagKey<Fluid> fluidTagKey : blockState.getFluidState().getTags().toList())
            list.add(fluidTagKey.location().toString());
        return list;
    }

    @Override
    public Vector3d getMapColor() {
        // TODO: color util
        //return blockState.getMapColor(getLevel(), blockPos).col
        return new Vector3d(0, 0, 0);
    }

    @Override
    public MinecraftItem asItem() {
        return new MinecraftItemImpl(blockState.getBlock().asItem());
    }

    @Override
    public String toStateString() {
        BlockEntity entity = getLevel().getBlockEntity(blockPos);
        ValueOutput output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        if (entity != null)
            entity.saveWithoutMetadata(output);

        return BlockStateParser.serialize(blockState) +  output;
    }

    @Override
    public int getOpacity() {
        return blockState.getLightBlock();
    }

    @Override
    public int getComparatorOutput() {
        ///  TODO: now supports a direction
        return blockState.getAnalogOutputSignal(getLevel(), blockPos, Direction.DOWN);
    }

    @Override
    public int getLuminance() {
        return blockState.getLightEmission();
    }

    @Override
    public float getHardness() {
        return blockState.getDestroySpeed(getLevel(), blockPos);
    }

    @Override
    public float getFriction() {
        return blockState.getBlock().getFriction();
    }

    @Override
    public float getVelocityModifier() {
        return blockState.getBlock().getSpeedFactor();
    }

    @Override
    public float getJumpVelocityModifier() {
        return blockState.getBlock().getJumpFactor();
    }

    @Override
    public float getBlastResistance() {
        return blockState.getBlock().getExplosionResistance();
    }

    @Override
    public boolean isTranslucent() {
        return blockState.propagatesSkylightDown();
    }

    @Override
    public boolean isSolidBlock() {
        return blockState.isRedstoneConductor(getLevel(), blockPos);
    }

    @Override
    public boolean isFullCube() {
        return blockState.isCollisionShapeFullBlock(getLevel(), blockPos);
    }

    @Override
    public boolean hasEmissiveLighting() {
        return blockState.emissiveRendering(getLevel(), blockPos);
    }

    @Override
    public boolean hasBlockEntity() {
        return blockState.hasBlockEntity();
    }

    @Override
    public boolean isOpaque() {
        return blockState.canOcclude();
    }

    @Override
    public boolean emitsRedstonePower() {
        return blockState.isSignalSource();
    }

    @Override
    public boolean hasCollision() {
        // TODO BlockBehaviourAcessor no longer has a collision check
        //return blockState.getBlock().properties();
        return false;
    }

    @Override
    public boolean isAir() {
        return blockState.isAir();
    }
    // Helper functions

    // Helper to get the texture name of a quad
    private static String getTextureName(TextureAtlasSprite sprite) {
        Identifier location = sprite.contents().name(); // do not close it
        return location.getNamespace() + ":textures/" + location.getPath();
    }

    // Helper to get the textures of a block face
    private static Set<String> getTexturesForFace(Direction direction, RandomSource randomSource, BlockStateModel bakedModel, long seed) {
        randomSource.setSeed(seed);
        List<BlockModelPart> list = new ArrayList<>();
        bakedModel.collectParts(randomSource, list);

        Set<String> textures = new HashSet<>();

        for (BlockModelPart part : list)
            for (BakedQuad quad : part.getQuads(direction)) {
                textures.add(getTextureName(quad.sprite()));
            }

        return textures;
    }

    // Helper to convert a VoxelShape into a 2D list of points
    private static List<List<Vector3d>> voxelShapeToTable(VoxelShape voxelShape) {
        List<List<Vector3d>> shapes = new ArrayList<>();
        for (AABB aabb : voxelShape.toAabbs())
            shapes.add(List.of(new Vector3d(aabb.minX, aabb.minY, aabb.minZ), new Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ)));
        return shapes;
    }

    private static ClientLevel getLevel() {
        return Minecraft.getInstance().level;
    }
}
