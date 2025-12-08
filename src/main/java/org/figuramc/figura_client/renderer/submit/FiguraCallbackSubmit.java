package org.figuramc.figura_client.renderer.submit;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;

// Just code we can run on the render thread, doesn't actually draw anything
@FunctionalInterface
public interface FiguraCallbackSubmit extends SubmitNodeCollector.CustomGeometryRenderer {

    void run();

    default void render(PoseStack.Pose pose, VertexConsumer vertexConsumer) {
        run();
    }
}