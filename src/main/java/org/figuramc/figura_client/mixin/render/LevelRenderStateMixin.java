package org.figuramc.figura_client.mixin.render;

import net.minecraft.client.renderer.state.LevelRenderState;
import org.figuramc.figura_client.ducks.LevelRenderStateAccess;
import org.figuramc.figura_client.renderer.submit.FiguraCallbackSubmit;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LevelRenderState.class)
public class LevelRenderStateMixin implements LevelRenderStateAccess {

    // Code to run at the beginning of rendering the level
    @Unique public FiguraCallbackSubmit codeSubmit;

    @Override public @Nullable FiguraCallbackSubmit figura_client$getCodeSubmit() { return codeSubmit; }
    @Override public void figura_client$setCodeSubmit(@Nullable FiguraCallbackSubmit submit) { this.codeSubmit = submit; }
}
