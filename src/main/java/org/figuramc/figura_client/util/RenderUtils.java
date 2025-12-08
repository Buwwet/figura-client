package org.figuramc.figura_client.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import org.figuramc.figura_client.FiguraClient;
import org.figuramc.figura_client.textures.MinecraftTextureImpl;
import org.figuramc.figura_client.textures.OwnedMinecraftTextureImpl;
import org.figuramc.figura_core.manage.AvatarView;
import org.figuramc.figura_core.minecraft_interop.texture.MinecraftTexture;
import org.figuramc.figura_core.util.data_structures.NullEmptyStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class RenderUtils {

    // Stack of views of currently-submitting avatars.
    public static final NullEmptyStack<AvatarView<?>> AVATAR_SUBMITTING_STACK = new NullEmptyStack<>();
    // Whether said avatar view is for a living entity
    public static final NullEmptyStack<Boolean> IS_LIVING_ENTITY_STACK = new NullEmptyStack<>(); // TODO look for a maybe better way to do this?

    // Convert a MinecraftTexture to an actual game resource
    public static ResourceLocation texToLocation(@Nullable MinecraftTexture texture, ResourceLocation defaultVal) {
        return switch (texture) {
            case MinecraftTextureImpl impl -> impl.location;
            case OwnedMinecraftTextureImpl ownedImpl -> ownedImpl.location;
            case null -> defaultVal;
            default -> throw new IllegalStateException("Unexpected implementation of MinecraftTexture: " + texture.getClass());
        };
    }

    @Contract("null -> null;!null -> !null")
    public static GpuTexture texToGpuTexture(@Nullable MinecraftTexture texture) {
        return switch (texture) {
            case MinecraftTextureImpl impl -> impl.backing.getTexture();
            case OwnedMinecraftTextureImpl ownedImpl -> ownedImpl.getTexture();
            case null -> null;
            default -> throw new IllegalStateException("Unexpected implementation of MinecraftTexture: " + texture.getClass());
        };
    }

    @Contract("null -> null;!null -> !null")
    public static GpuTextureView texToGpuTextureView(@Nullable MinecraftTexture texture) {
        return switch (texture) {
            case MinecraftTextureImpl impl -> impl.backing.getTextureView();
            case OwnedMinecraftTextureImpl ownedImpl -> ownedImpl.getTextureView();
            case null -> null;
            default -> throw new IllegalStateException("Unexpected implementation of MinecraftTexture: " + texture.getClass());
        };
    }

    // Single pixels of various colors to use as placeholders for no texture
    public static final DynamicTexture ZERO_PIXEL = new DynamicTexture(() -> "Zero Pixel", Util.make(() -> {
        NativeImage image = new NativeImage(1, 1, false);
        image.setPixel(0, 0, ARGB.color(0, 0, 0, 0));
        return image;
    }));
    public static final ResourceLocation ZERO_PIXEL_LOC = Util.make(() -> {
        ResourceLocation loc = FiguraClient.locate("zero_pixel");
        Minecraft.getInstance().getTextureManager().register(loc, ZERO_PIXEL);
        return loc;
    });

    public static final DynamicTexture DEFAULT_NORMAL_MAP = new DynamicTexture(() -> "Default Normal Map", Util.make(() -> {
        NativeImage image = new NativeImage(1, 1, false);
        image.setPixel(0, 0, ARGB.color(0, 128, 128, 0));
        return image;
    }));
    public static final ResourceLocation DEFAULT_NORMAL_MAP_LOC = Util.make(() -> {
        ResourceLocation loc = FiguraClient.locate("default_normal_map");
        Minecraft.getInstance().getTextureManager().register(loc, DEFAULT_NORMAL_MAP);
        return loc;
    });

    // Run tasks on the render thread
    public static final Queue<Runnable> TASKS = new ConcurrentLinkedDeque<>();
    public static void runOnRenderThread(Runnable task) {
        TASKS.add(task);
    }
}
