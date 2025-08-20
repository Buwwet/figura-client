package org.figuramc.figura_client.mixin.general_render;

import com.mojang.blaze3d.systems.RenderSystem;
import org.figuramc.figura_client.util.RenderTasks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {

    @Inject(method = "executePendingTasks", at = @At("HEAD"), remap = false)
    private static void onExecutePendingTasks(CallbackInfo ci) {
        for (var task = RenderTasks.TASKS.poll(); task != null; task = RenderTasks.TASKS.poll())
            task.run();
    }

}
