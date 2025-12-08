package org.figuramc.figura_client.renderer.part;

import net.minecraft.client.renderer.MultiBufferSource;
import org.figuramc.figura_core.avatars.AvatarError;
import org.figuramc.figura_core.minecraft_interop.render.PartRenderer;
import org.figuramc.figura_core.model.rendering.RenderingRoot;
import org.figuramc.figura_core.util.data_structures.FiguraTransformStack;

/**
 * Rendering state connected to a RenderingRoot.
 */
public abstract class FiguraClientPartRenderer extends PartRenderer {

    public FiguraClientPartRenderer(RenderingRoot<?> root) {
        super(root);
    }

    /**
     * Render method, drawing the root from the given info.
     */
    public abstract void render(MultiBufferSource bufferSource, FiguraTransformStack transformStack, int light, int overlay) throws AvatarError;

}
