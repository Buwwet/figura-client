package org.figuramc.figura_client.textures;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import org.figuramc.figura_core.minecraft_interop.texture.MinecraftTexture;

public class MinecraftTextureImpl implements MinecraftTexture {

    public Identifier location;
    public AbstractTexture backing;

    public MinecraftTextureImpl(Identifier location, AbstractTexture backing) {
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
