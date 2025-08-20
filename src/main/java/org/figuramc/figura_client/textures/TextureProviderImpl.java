package org.figuramc.figura_client.textures;

import org.figuramc.figura_core.minecraft_interop.texture.MinecraftTextureProvider;
import org.figuramc.figura_core.minecraft_interop.texture.OwnedMinecraftTexture;

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
}
