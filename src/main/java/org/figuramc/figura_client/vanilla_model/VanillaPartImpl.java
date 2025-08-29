package org.figuramc.figura_client.vanilla_model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.figuramc.figura_client.ducks.ModelPartTrackingAccess;
import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaPart;
import org.jetbrains.annotations.Nullable;

public class VanillaPartImpl extends VanillaPart {

    private final @Nullable VanillaPart parent;

    public VanillaPartImpl(ModelPart minecraftPart, @Nullable VanillaPart parent) {
        ((ModelPartTrackingAccess) (Object) minecraftPart).figura_client$setVanillaPart(this); // Set the vanilla part
        this.parent = parent;
    }

    @Override
    public @Nullable VanillaPart parent() {
        return this.parent;
    }

    public static <S extends LivingEntityRenderState, M extends EntityModel<S>, L extends RenderLayer<S, M>> L getRenderLayer(LivingEntityRenderer<?, S, M> renderer, Class<L> layerClass) {
        for (var layer : renderer.layers) {
            if (layerClass.isInstance(layer))
                return (L) layer;
        }
        throw new IllegalStateException("Could not find RenderLayer of class " + layerClass.getCanonicalName() + " in entity!");
    }

}
