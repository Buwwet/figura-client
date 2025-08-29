package org.figuramc.figura_client.vanilla_model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cached mapping from EntityRenderer -> VanillaModel impl
 */
public class VanillaModelCache {

    private static final Map<EntityRenderer<?, ?>, @Nullable VanillaModel> models = new ConcurrentHashMap<>();

    // Get the model for this entity
    public static @NotNull VanillaModel get(Entity entity) {
        EntityRenderer<?, ?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);
        return models.computeIfAbsent(renderer, VanillaModelProviders::getModel);
    }

    public static void clearCache() {
        models.clear();
    }

}
