package org.figuramc.figura_client.game_data;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.figuramc.figura_client.FiguraClient;
import org.figuramc.figura_client.vanilla_model.VanillaModelCache;
import org.figuramc.figura_client.vanilla_model.models.PlayerModelImpl;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.EntityKind;
import org.figuramc.figura_core.minecraft_interop.game_data.entity.MinecraftEntity;
import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaModel;
import org.joml.Vector3d;

import java.util.UUID;

public record MinecraftEntityImpl(Entity entity) implements MinecraftEntity {

    @Override
    public EntityKind getKind() {
        return FiguraClient.ENTITY_KINDS.computeIfAbsent(entity.getType(), ty -> {
            ResourceLocation loc = BuiltInRegistries.ENTITY_TYPE.getKey(ty);
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

}
