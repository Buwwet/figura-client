package org.figuramc.figura_client.mixin.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.ResourceHandle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.util.profiling.ProfilerFiller;
import org.figuramc.figura_client.ducks.LevelRenderStateAccess;
import org.figuramc.figura_client.renderer.submit.FiguraCallbackSubmit;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    // Add our own code running on render thread, before any rendering for the main render pass
    // Injects into the lambda passed to framePass.executes()
    @Inject(method = "method_62214", at = @At("HEAD"))
    private void runLevelRenderCallbacks(GpuBufferSlice gpuBufferSlice, LevelRenderState levelRenderState, ProfilerFiller profilerFiller, Matrix4f matrix4f, ResourceHandle resourceHandle, ResourceHandle resourceHandle2, boolean bl, Frustum frustum, ResourceHandle resourceHandle3, ResourceHandle resourceHandle4, CallbackInfo ci) {
        FiguraCallbackSubmit levelRenderCallbacks = ((LevelRenderStateAccess) levelRenderState).figura_client$getCodeSubmit();
        if (levelRenderCallbacks != null)
            levelRenderCallbacks.run();
    }

}
