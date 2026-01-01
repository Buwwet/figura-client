package org.figuramc.figura_client.game_data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.figuramc.figura_client.FiguraClient;
import org.figuramc.figura_core.minecraft_interop.game_data.MinecraftIdentifier;
import org.figuramc.figura_core.minecraft_interop.game_data.block.MinecraftBlockState;
import org.figuramc.figura_core.minecraft_interop.game_data.item.MinecraftItemStack;
import org.figuramc.figura_core.util.ListUtils;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record MinecraftBlockStateImpl(BlockState blockState, BlockPos blockPos) implements MinecraftBlockState {

    @Override
    public MinecraftIdentifier getIdentifier() {
        return blockState.getBlockHolder().unwrapKey().map(key -> FiguraClient.coreIdent(key.identifier())).orElse(FiguraClient.UNKNOWN);
    }

    @Override
    public Vector3i getBundledPos(Vector3i output) {
        return output.set(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }

    @Override
    public MinecraftBlockState withBundledPos(Vector3ic position) {
        return new MinecraftBlockStateImpl(blockState, new BlockPos(position.x(), position.y(), position.z()));
    }

    @Override
    public List<MinecraftBlockState.AABB> getCollisionShape() {
        return voxelShapeToCore(blockState.getCollisionShape(getLevel(), blockPos));
    }

    @Override
    public List<MinecraftBlockState.AABB> getOutlineShape() {
        return voxelShapeToCore(blockState.getShape(getLevel(), blockPos));
    }

//    @Override
//    public HashMap<String, Set<String>> getTextures() {
//        // Direct port from fig 0.1.0
//        HashMap<String, Set<String>> map = new HashMap<>();
//
//        RenderShape renderShape = blockState.getRenderShape();
//
//        if (renderShape == RenderShape.MODEL) {
//            BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
//
//            BlockStateModel bakedModel = blockRenderer.getBlockModel(blockState);
//            RandomSource randomSource = RandomSource.create();
//            long seed = 42L;
//
//            for (Direction direction : Direction.values())
//                map.put(direction.name(), getTexturesForFace(direction, randomSource, bakedModel, seed));
//            map.put("NONE", getTexturesForFace(null, randomSource, bakedModel, seed));
//
//            TextureAtlasSprite particle = blockRenderer.getBlockModelShaper().getParticleIcon(blockState);
//            map.put("PARTICLE", Set.of(getTextureName(particle)));
//        }
//        // TODO: RenderShape.ENTITYBLOCK_ANIMATED no longer exists.
//        //else if (renderShape == RenderShape.ENTITYBLOCK_ANIMATED) {
//            //map.put("PARTICLE", Set.of(getTextureName(Minecraft.getInstance().getItemRenderer().getModel(blockState.getBlock().asItem().getDefaultInstance(), WorldAPI.getCurrentWorld(), null, 42).getParticleIcon())));
//       //}
//        return map;
//    }
//
//    @Override
//    public Map<String, Object> getSounds() {
//        Map<String, Object> sounds = new HashMap<>();
//        SoundType snd = blockState.getSoundType();
//
//        sounds.put("pitch", snd.getPitch());
//        sounds.put("volume", snd.getVolume());
//        sounds.put("break", snd.getBreakSound().location().toString());
//        sounds.put("fall", snd.getFallSound().location().toString());
//        sounds.put("hit", snd.getHitSound().location().toString());
//        sounds.put("place", snd.getPlaceSound().location().toString());
//        sounds.put("step", snd.getStepSound().location().toString());
//
//        return sounds;
//    }

    @Override
    public List<String> getProperties() {
        return ListUtils.map(blockState.getProperties(), Property::getName);
    }

    @Override
    public List<MinecraftIdentifier> getTags() {
        return blockState.getTags().map(tag -> FiguraClient.coreIdent(tag.location())).toList();
    }

    @Override
    public List<MinecraftIdentifier> getFluidTags() {
        return blockState.getFluidState().getTags().map(tag -> FiguraClient.coreIdent(tag.location())).toList();
    }

    @Override
    public Vector3f getMapColor(Vector3f output) {
        // TODO: color util
        return output.set(0, 0, 0);
    }

    @Override
    public MinecraftItemStack asItem() {
        return new MinecraftItemStackImpl(new ItemStack(blockState.getBlock().asItem()));
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
    public int getComparatorOutput(String direction) {
        Direction dir = Direction.byName(direction);
        if (dir == null)
            return 0;

        return blockState.getAnalogOutputSignal(getLevel(), blockPos, dir);
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

    // Helper to convert a VoxelShape to the expected core representation
    private static List<MinecraftBlockState.AABB> voxelShapeToCore(VoxelShape voxelShape) {
        List<MinecraftBlockState.AABB> aabbs = new ArrayList<>();
        for (net.minecraft.world.phys.AABB aabb : voxelShape.toAabbs())
            aabbs.add(new MinecraftBlockState.AABB(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ));
        return aabbs;
    }

    private static ClientLevel getLevel() {
        return Minecraft.getInstance().level;
    }
}
