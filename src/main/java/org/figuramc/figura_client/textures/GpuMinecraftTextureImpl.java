package org.figuramc.figura_client.textures;

import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import org.figuramc.figura_core.minecraft_interop.texture.GpuMinecraftTexture;
import org.jspecify.annotations.Nullable;

public class GpuMinecraftTextureImpl implements GpuMinecraftTexture {

    protected GpuTextureView textureView;

    public GpuMinecraftTextureImpl(GpuTextureView textureView) {
        this.textureView = textureView;
    }

    @Override
    public int width() {
        return textureView.getWidth(0);
    }

    @Override
    public int height() {
        return textureView.getHeight(0);
    }

    public GpuTextureView getTextureView() {
        return textureView;
    }

    public GpuTexture getTexture() {
        return textureView.texture();
    }
}
