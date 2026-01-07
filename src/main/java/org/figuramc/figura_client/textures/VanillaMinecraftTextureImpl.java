package org.figuramc.figura_client.textures;

import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.resources.Identifier;
import org.figuramc.figura_core.minecraft_interop.texture.VanillaMinecraftTexture;

public class VanillaMinecraftTextureImpl implements VanillaMinecraftTexture {

    Identifier textureLocation;
    SimpleTexture backing;

    public VanillaMinecraftTextureImpl(Identifier textureLocation) {
        this.textureLocation = textureLocation;
        backing = new SimpleTexture(textureLocation);
    }

    @Override
    public void destroy() {
        backing.close();
    }

    @Override
    public int getPixel(int x, int y) {
        return 0;
    }

    @Override
    public int width() {
        return 0;
        //return texture.getWidth(0);
    }

    @Override
    public int height() {
        return 0;
        //return texture.getHeight(0);
    }
}
