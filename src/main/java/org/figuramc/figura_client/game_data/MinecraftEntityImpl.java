package org.figuramc.figura_client.game_data;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.*;
import org.figuramc.figura_client.FiguraClient;
import org.figuramc.figura_client.vanilla_model.VanillaModelCache;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.EntityKind;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftEntity;
import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaModel;
import org.joml.Vector2d;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record MinecraftEntityImpl(Entity entity) implements MinecraftEntity {

    @Override
    public EntityKind getKind() {
        return FiguraClient.ENTITY_KINDS.computeIfAbsent(entity.getType(), ty -> {
            Identifier loc = BuiltInRegistries.ENTITY_TYPE.getKey(ty);
            return new EntityKind(loc.getNamespace(), loc.getPath());
        });
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
    public Vector2d getRotation(float tickDelta, Vector2d output) {
        Vec2 vec = new Vec2(entity.getXRot(tickDelta), entity.getYRot(tickDelta));
        return output.set(vec.x, vec.y);
    }

    @Override
    public Vector3d getVelocity(Vector3d output) {
        Vec3 vec = new Vec3(entity.getX() - entity.xOld, entity.getY() - entity.yOld, entity.getZ() - entity.zOld);
        return output.set(vec.x, vec.y, vec.z);
    }

    @Override
    public Vector3d getLookDir(Vector3d output) {
        Vec3 vec = entity.getLookAngle();
        return output.set(vec.x, vec.y, vec.z);
    }

    // TODO: Derived view
    @Override
    public MinecraftEntity getVehicle() {
        return new MinecraftEntityImpl(entity.getVehicle());
        //return new EntityView<>(new MinecraftEntityImpl(entity.getVehicle()));
    }

    @Override
    public MinecraftEntity getControlledVehicle() {
        return new MinecraftEntityImpl(entity.getControlledVehicle());
    }

    @Override
    public List<MinecraftEntity> getPassengers() {
        List<MinecraftEntity> list = new ArrayList<>();
        for (Entity passenger : entity.getPassengers()) {
            list.add(new MinecraftEntityImpl(passenger));
        }
        return list;
    }

    @Override
    public MinecraftEntity getControllingPassenger() {
        return new MinecraftEntityImpl(entity.getControllingPassenger());
    }

    @Override
    public Object[] getTargetedEntity(Double distance) {
        if (distance == null) distance = 20d;
        distance = Math.max(Math.min(distance, 20), 0);

        Vec3 start = entity.getEyePosition(1f);
        HitResult hitResult = entity.pick(distance, 1f, false);
        distance = hitResult.getLocation().distanceToSqr(start);

        Vec3 vec32 = entity.getViewVector(1f);
        Vec3 vec33 = start.add(vec32.x * distance, vec32.y * distance, vec32.z * distance);
        AABB aABB = entity.getBoundingBox().expandTowards(vec32.scale(distance)).inflate(1d);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(entity, start, vec33, aABB, e -> e != entity, distance);

        if (entityHit != null) {
            return new Object[]{new MinecraftEntityImpl(entityHit.getEntity()), entityHit.getLocation()};
        }
        return null;
    }

    @Override
    public MinecraftEntity getNearestEntity(String type, Double radius) {
        radius = radius != null ? radius : 20;

        EntityType<?> entityType;
        if (type != null) {
            Identifier id = Identifier.tryParse(type);
            if (id == null) {
                // TODO: Still need better error handling.
                throw new RuntimeException("Invalid entity type: " + type);
            }
            entityType = BuiltInRegistries.ENTITY_TYPE.get(id).get().value();
        } else {
            entityType = null;
        }

        Vec3 pos = entity.getPosition(1.0f);

        AABB aabb = new AABB(pos.subtract(radius), pos.add(radius));

        return entity.level().getEntities(entity, aabb)
                .stream()
                .filter(e -> entityType == null || e.getType() == entityType)
                .min(Comparator.comparingDouble(e -> e.distanceToSqr(pos.x(), pos.y(), pos.z())))
                .map(MinecraftEntityImpl::new)
                .orElse(null);
    }

    @Override
    public boolean hasAvatar() {
        //TODO
        return false;
    }

    @Override
    public int getPermissionLevel() {
        //TODO
        return 0;
    }

    @Override
    public Object getVariable(String key) {
        // TODO
        return null;
    }

    @Override
    public Object getNBT() {
        // TODO
        // Requires a ValueOutput, but can't figure out the constructor.
        //entity.saveWithoutId(ValueOutput);
        return null;
    }

    // Simple state getters
    @Override
    public int getFrozenTicks() { return entity.getTicksFrozen(); }

    @Override
    public int getMaxAir() { return entity.getMaxAirSupply(); }

    @Override
    public float getEyeHeight() { return entity.getEyeHeight(); }

    @Override
    public String getName() { return entity.getName().getString(); }

    @Override
    public String getType() { return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString(); }

    @Override
    public String getDimensionName() { return entity.level().dimension().identifier().toString(); }

    @Override
    public String getPose() { return entity.getPose().toString(); }

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
