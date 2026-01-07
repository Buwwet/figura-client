package org.figuramc.figura_client.textures;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.Identifier;
import org.figuramc.figura_core.data.materials.ModuleMaterials;
import org.figuramc.figura_core.minecraft_interop.texture.*;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class TextureProviderImpl implements MinecraftTextureProvider {

    @Override
    public OwnedMinecraftTexture createBlankTexture(int width, int height) {
        return new OwnedMinecraftTextureImpl(width, height);
    }

    @Override
    public OwnedMinecraftTexture createTextureFromPng(byte[] pngBytes) throws IOException {
        return new OwnedMinecraftTextureImpl(pngBytes);
    }

    @Override
    public @Nullable OwnedMinecraftTexture getVanillaTexture(ModuleMaterials.TextureMaterials.VanillaTexture vanilla) throws IOException {
        // Translate our MinecraftIdentifier into a real Identifier
        Identifier textureLocation = Identifier.parse(vanilla.resourceLocation().toString());
        // Use TextureManager to get it for us.

        //TODO: check that it exists
        //AbstractTexture backing = Minecraft.getInstance().getTextureManager().getTexture(textureLocation);
        return new OwnedMinecraftTextureImpl(textureLocation);
    }


    @Override
    public GpuMinecraftTexture getBuiltinTexture(ModuleMaterials.BuiltinTextureBinding builtin) {
        return switch (builtin) {
            case NONE -> null;
            case LIGHTMAP -> new GpuMinecraftTextureImpl(Minecraft.getInstance().gameRenderer.lightTexture().getTextureView());
            case OVERLAY -> new GpuMinecraftTextureImpl(Minecraft.getInstance().gameRenderer.overlayTexture().getTextureView());
        };
    }
}
