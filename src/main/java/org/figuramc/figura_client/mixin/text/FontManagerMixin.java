package org.figuramc.figura_client.mixin.text;

import net.minecraft.client.gui.GlyphSource;
import net.minecraft.network.chat.FontDescription;
import org.figuramc.figura_client.text.FiguraFontDescription;
import org.figuramc.figura_client.text.FiguraGlyphProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// temporarily disabled in mixin config
@Mixin(targets = "net.minecraft.client.gui.font.FontManager.CachedFontProvider")
public class FontManagerMixin {
    // Get our custom fontdesc in there
    @Inject(method = "getGlyphSource", at = @At("HEAD"), cancellable = true)
    private void figura$source(FontDescription fontDescription, CallbackInfoReturnable<GlyphSource> cir) {
        if (fontDescription instanceof FiguraFontDescription descriptor) {
            cir.setReturnValue(FiguraGlyphProvider.INSTANCE.source(descriptor));
            cir.cancel();
        }
    }
}
