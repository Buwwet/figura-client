package org.figuramc.figura_client.game_data;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura_client.FiguraClient;
import org.figuramc.figura_client.vanilla_model.VanillaModelCache;
import org.figuramc.figura_core.minecraft_interop.game_data.MinecraftIdentifier;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.EntityPose;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftEntity;
import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaModel;
import org.figuramc.figura_core.util.ListUtils;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MinecraftEntityImpl<T extends Entity> implements MinecraftEntity {

    public final T entity;

    public MinecraftEntityImpl(T entity) {
        this.entity = entity;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MinecraftEntityImpl<?> entityImpl && Objects.equals(this.entity, entityImpl.entity);
    }

    @Override
    public UUID getUUID() {
        return entity.getUUID();
    }

    @Override
    public VanillaModel getModel() {
        return VanillaModelCache.get(entity);
    }

    @Override
    public boolean isGone() {
        return entity.isRemoved() || entity.level() != Minecraft.getInstance().level;
    }

    @Override
    public Vector3d getPosition(float tickDelta, Vector3d output) {
        Vec3 vec = entity.getPosition(tickDelta);
        return output.set(vec.x, vec.y, vec.z);
    }

    @Override
    public Vector2f getRotation(float tickDelta, Vector2f output) {
        return output.set(entity.getXRot(tickDelta), entity.getYRot(tickDelta));
    }

    @Override
    public Vector3d getVelocity(Vector3d output) {
        return output.set(entity.getX() - entity.xOld, entity.getY() - entity.yOld, entity.getZ() - entity.zOld);
    }

    @Override
    public Vector3d getLookDirection(float tickDelta, Vector3d output) {
        Vec3 vec = entity.calculateViewVector(entity.getXRot(tickDelta), entity.getYRot(tickDelta));
        return output.set(vec.x, vec.y, vec.z);
    }

    @Override @Nullable
    public MinecraftEntity getVehicle() {
        Entity vehicle = entity.getVehicle();
        return vehicle == null ? null : new MinecraftEntityImpl(vehicle);
    }

    @Override @Nullable
    public MinecraftEntity getControlledVehicle() {
        Entity vehicle = entity.getControlledVehicle();
        return vehicle == null ? null : new MinecraftEntityImpl(vehicle);
    }

    @Override
    public List<MinecraftEntity> getPassengers() {
        return ListUtils.map(entity.getPassengers(), MinecraftEntityImpl::new);
    }

    @Override @Nullable
    public MinecraftEntity getControllingPassenger() {
        Entity passenger = entity.getControllingPassenger();
        return passenger == null ? null : new MinecraftEntityImpl(passenger);
    }

//    @Override @Nullable
//    public Pair<MinecraftEntity, Vector3d> getTargetedEntity(Double distance) {
//        if (distance == null) distance = 20d;
//        distance = Math.max(Math.min(distance, 20), 0);
//
//        Vec3 start = entity.getEyePosition(1f);
//        HitResult hitResult = entity.pick(distance, 1f, false);
//        distance = hitResult.getLocation().distanceToSqr(start);
//
//        Vec3 vec32 = entity.getViewVector(1f);
//        Vec3 vec33 = start.add(vec32.x * distance, vec32.y * distance, vec32.z * distance);
//        AABB aABB = entity.getBoundingBox().expandTowards(vec32.scale(distance)).inflate(1d);
//        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(entity, start, vec33, aABB, e -> e != entity, distance);
//
//        if (entityHit != null) {
//            Vec3 pos = entityHit.getLocation();
//            return new Pair<>(new MinecraftEntityImpl(entityHit.getEntity()), new Vector3d(pos.x, pos.y, pos.z));
//        }
//        return null;
//    }

//    @Override @Nullable
//    public MinecraftEntity getNearestEntity(String type, Double radius) {
//        radius = radius != null ? radius : 20;
//
//        EntityType<?> entityType;
//        if (type != null) {
//            Identifier id = Identifier.tryParse(type);
//            if (id == null) {
//                // TODO: Still need better error handling on figura-client.
//                throw new RuntimeException("Invalid entity type: " + type);
//            }
//            entityType = BuiltInRegistries.ENTITY_TYPE.get(id).get().value();
//        } else {
//            entityType = null;
//        }
//
//        Vec3 pos = entity.getPosition(1.0f);
//
//        AABB aabb = new AABB(pos.subtract(radius), pos.add(radius));
//
//        return entity.level().getEntities(entity, aabb)
//                .stream()
//                .filter(e -> entityType == null || e.getType() == entityType)
//                .min(Comparator.comparingDouble(e -> e.distanceToSqr(pos.x(), pos.y(), pos.z())))
//                .map(MinecraftEntityImpl::new)
//                .orElse(null);
//    }

    @Override
    public String getName() { return entity.getName().getString(); }

    @Override
    public MinecraftIdentifier getType() { return FiguraClient.coreIdent(EntityType.getKey(entity.getType())); }

    @Override
    public EntityPose getPose() {
        // Ugly, but this will fail to compile if a new pose is added and we don't deal with it accordingly
        return switch (entity.getPose()) {
            case STANDING -> EntityPose.STANDING;
            case FALL_FLYING -> EntityPose.FALL_FLYING;
            case SLEEPING -> EntityPose.SLEEPING;
            case SWIMMING -> EntityPose.SWIMMING;
            case SPIN_ATTACK -> EntityPose.SPIN_ATTACK;
            case CROUCHING -> EntityPose.CROUCHING;
            case LONG_JUMPING -> EntityPose.LONG_JUMPING;
            case DYING -> EntityPose.DYING;
            case CROAKING -> EntityPose.CROAKING;
            case USING_TONGUE -> EntityPose.USING_TONGUE;
            case SITTING -> EntityPose.SITTING;
            case ROARING -> EntityPose.ROARING;
            case SNIFFING -> EntityPose.SNIFFING;
            case EMERGING -> EntityPose.EMERGING;
            case DIGGING -> EntityPose.DIGGING;
            case SLIDING -> EntityPose.SLIDING;
            case SHOOTING -> EntityPose.SHOOTING;
            case INHALING -> EntityPose.INHALING;
        };
    }

    @Override
    public int getPermissionLevel() {
        //TODO
        return 0;
    }

    // Simple state getters
    @Override
    public int getFrozenTicks() { return entity.getTicksFrozen(); }

    @Override
    public int getMaxAir() { return entity.getMaxAirSupply(); }

    @Override
    public float getEyeHeight() { return entity.getEyeHeight(); }

    @Override
    public boolean isPlayer() { return entity instanceof Player; }

    @Override
    public boolean isCrouching() { return entity.isCrouching(); }

    @Override
    public boolean isSprinting() { return entity.isSprinting(); }

    @Override
    public boolean isMoving(boolean ignoreY) {
        return entity.getX() != entity.xOld
                || (!ignoreY && (entity.getY() != entity.yOld))
                || entity.getZ() != entity.zOld;
    }

    @Override
    public boolean isOnGround() { return entity.onGround(); }

    @Override
    public boolean isFalling() { return !entity.onGround() && entity.getY() < entity.yOld; }

    @Override
    public boolean isWet() { return entity.isInWaterOrRain(); }

    @Override
    public boolean isInWater() { return entity.isInWater(); }

    @Override
    public boolean isUnderWater() { return entity.isUnderWater(); }

    @Override
    public boolean isInLava() { return entity.isInLava(); }

    @Override
    public boolean isInRain() {
        BlockPos blockPos = entity.blockPosition();
        return entity.level().isRainingAt(blockPos) ||
                entity.level().isRainingAt(new BlockPos(blockPos.getX(), (int) entity.getBoundingBox().maxY, blockPos.getZ()));
    }

    @Override
    public boolean isGlowing() { return entity.isCurrentlyGlowing(); }

    @Override
    public boolean isInvisible() { return entity.isInvisible(); }

    @Override
    public boolean isSilent() { return entity.isSilent(); }

    @Override
    public boolean isOnFire() { return entity.isOnFire(); }

    @Override
    public boolean isAlive() { return entity.isAlive(); }

    @Override
    public boolean hasInventory() { return entity instanceof HasCustomInventoryScreen; }
}
