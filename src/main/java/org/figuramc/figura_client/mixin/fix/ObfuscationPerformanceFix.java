package org.figuramc.figura_client.mixin.fix;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.gui.font.FontSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FontSet.class)
public class ObfuscationPerformanceFix {

    // Just tune this int. Integer.MAX_VALUE is default behavior.
    // Higher number = more randomness/obfuscation, worse performance.
    @Unique private static final int OBFUSCATED_CHAR_CAP = 100;

    @WrapOperation(method = "getRandomGlyph", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/ints/IntList;size()I"))
    int capSize(IntList instance, Operation<Integer> original) {
        return Math.min(original.call(instance), OBFUSCATED_CHAR_CAP);
    }

}
