package org.figuramc.figura_client.textures;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import org.figuramc.figura_core.minecraft_interop.texture.MinecraftTexture;

public class MinecraftTextureImpl implements MinecraftTexture {

    public ResourceLocation location;
    public AbstractTexture backing;

    public MinecraftTextureImpl(ResourceLocation location, AbstractTexture backing) {
        this.location = location;
        this.backing = backing;
    }

    @Override
    public int width() {
        return backing.getTextureView().getWidth(0);
    }

    @Override
    public int height() {
        return backing.getTextureView().getHeight(0);
    }

}
