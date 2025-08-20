package org.figuramc.figura_client.ducks;

import net.minecraft.world.entity.Entity;

/**
 * Access the new fields in EntityRenderStateMixin
 */
public interface EntityRenderStateAccess {
    Entity figura_client$getEntity();
    void figura_client$setEntity(Entity entity);
    float figura_client$getTickDelta();
    void figura_client$setTickDelta(float tickDelta);
}
