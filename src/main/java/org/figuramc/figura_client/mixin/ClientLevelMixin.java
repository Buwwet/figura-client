package org.figuramc.figura_client.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.figuramc.figura_client.FiguraClient;
import org.figuramc.figura_client.game_data.MinecraftEntityImpl;
import org.figuramc.figura_core.avatars.AvatarModules;
import org.figuramc.figura_core.avatars.AvatarTemplates;
import org.figuramc.figura_core.data.ModuleImporter;
import org.figuramc.figura_core.data.ModuleMaterials;
import org.figuramc.figura_core.manage.AvatarManagers;
import org.figuramc.figura_core.minecraft_interop.FiguraConnectionPoint;
import org.figuramc.figura_core.minecraft_interop.vanilla_parts.VanillaModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.function.BooleanSupplier;

@SuppressWarnings("rawtypes")
@Mixin(ClientLevel.class)
public class ClientLevelMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void resetLoaded(ClientPacketListener clientPacketListener, ClientLevel.ClientLevelData clientLevelData, ResourceKey resourceKey, Holder holder, int i, int j, LevelRenderer levelRenderer, boolean bl, long l, int k, CallbackInfo ci) {
        FiguraClient.LOADED_TEST_AVATARS = false;
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void testing(BooleanSupplier booleanSupplier, CallbackInfo ci) {
        // TESTING CODE, AVATAR LOADING
        if (!FiguraClient.LOADED_TEST_AVATARS) {

            AvatarManagers.ENTITIES.clear();
            AvatarManagers.GUIS.clear();

            AvatarManagers.ENTITIES.load(FiguraConnectionPoint.GAME_DATA_PROVIDER.getLocalUUID(), () -> {
                Path avatarPath = FiguraConnectionPoint.PATH_PROVIDER.getAvatarsFolder().join().resolve("test_avatar");
                ModuleMaterials materials = ModuleImporter.importPath(avatarPath);
                AvatarModules modules = AvatarModules.loadModules(materials);
                VanillaModel vanillaModel = new MinecraftEntityImpl(Minecraft.getInstance().player).getModel();
                return AvatarTemplates.localPlayer(modules, vanillaModel);
            });
            AvatarManagers.GUIS.load(AvatarManagers.GuiKind.MAIN_GUI, () -> {
                Path avatarPath = FiguraConnectionPoint.PATH_PROVIDER.getGuisFolder().join().resolve("test_gui");
                ModuleMaterials materials = ModuleImporter.importPath(avatarPath);
                AvatarModules modules = AvatarModules.loadModules(materials);
                return AvatarTemplates.mainGui(modules);
            });

            FiguraClient.LOADED_TEST_AVATARS = true;
        }
    }

}
