package org.figuramc.figura_client;

import net.fabricmc.loader.api.FabricLoader;
import org.figuramc.figura_core.minecraft_interop.PathProvider;
import org.figuramc.figura_core.util.exception.ExceptionUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class PathProviderImpl implements PathProvider {

    private static final String CONFIG_FILENAME = "figura-directory-path.txt";
    private CompletableFuture<File> currentFuture = null;

    @Override
    public synchronized CompletableFuture<File> locateFiguraFolder() {
        if (currentFuture != null && !currentFuture.isCompletedExceptionally()) return currentFuture;

        // Try to find CONFIG_FILENAME in /run/config.
        // If it exists, read it.
        File pathConfigFile = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILENAME).toFile();
        if (pathConfigFile.exists() && !pathConfigFile.isDirectory()) {
            return currentFuture = CompletableFuture.supplyAsync(ExceptionUtils.wrapChecked(() -> {
                String absolutePath = Files.readString(pathConfigFile.toPath()); // Read an absolute path string from the file
                File figuraFolder = Path.of(absolutePath).toFile();
                if (!figuraFolder.exists() || !figuraFolder.isDirectory()) {
                    Files.deleteIfExists(pathConfigFile.toPath()); // If the config path was bad, delete the file
                    throw new PathProvider.CouldNotLocateFolderError(null);
                }
                return figuraFolder;
            }, CompletionException::new));
        }

        // TODO make dialog title translatable
        return currentFuture = CompletableFuture.supplyAsync(ExceptionUtils.wrapChecked(() -> {
            // Prompt the user for a folder on another thread
            @Nullable String absolutePath = TinyFileDialogs.tinyfd_selectFolderDialog("Choose or create a folder to use as your Figura directory!", "");
            if (absolutePath == null) throw new PathProvider.CouldNotLocateFolderError(null);
            // Fetch the file
            File figuraFolder = Path.of(absolutePath).toFile();
            if (!figuraFolder.exists() || !figuraFolder.isDirectory()) throw new PathProvider.CouldNotLocateFolderError(null);
            // Now that we have the path and we know it's good, store it in /run/config/CONFIG_FILENAME before returning.
            // Make sure we store the absolute path
            Files.writeString(pathConfigFile.toPath(), absolutePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            return figuraFolder;
        }, CompletionException::new));
    }

}
