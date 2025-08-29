package org.figuramc.figura_client.mixin.fix;

import net.minecraft.client.gui.font.FontSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(FontSet.class)
public class ObfuscationPerformanceFix {

    // Just tune this int. Integer.MAX_VALUE is default behavior.
    // Higher number = more randomness/obfuscation, worse performance.
    @Unique private static final int OBFUSCATED_CHAR_CAP = 100;

    @ModifyArg(method = "getRandomGlyph", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"), index = 0)
    int capSize(int prevValue) {
        return Math.min(prevValue, OBFUSCATED_CHAR_CAP);
    }

}
