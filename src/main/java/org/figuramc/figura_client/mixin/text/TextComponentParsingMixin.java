package org.figuramc.figura_client.mixin.text;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ExtraCodecs;
import org.figuramc.figura_client.text.FiguraTextContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ComponentSerialization.class)
public abstract class TextComponentParsingMixin {
    /*
    On this version, text components are NBT.
    That means we have to deal with Codecs (instead of the JSON parser)
     */

    @Inject(method = "bootstrap", at=@At("RETURN"))
    private static void figura$addMapping(ExtraCodecs.LateBoundIdMapper<String, MapCodec<? extends ComponentContents>> lateBoundIdMapper,
                                          CallbackInfo ci) {
        lateBoundIdMapper.put("figura", FiguraTextContents.CODEC);
    }
}
