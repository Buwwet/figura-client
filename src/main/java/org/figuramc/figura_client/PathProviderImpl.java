package org.figuramc.figura_client;

import net.fabricmc.loader.api.FabricLoader;
import org.figuramc.figura_core.minecraft_interop.PathProvider;
import org.figuramc.figura_core.util.exception.ExceptionUtils;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class PathProviderImpl implements PathProvider {

    private static final String CONFIG_FILENAME = "figura-directory-path.txt";
    private CompletableFuture<Path> currentFuture = null;

    @Override
    public synchronized CompletableFuture<Path> locateFiguraFolder() {
        if (currentFuture != null) return currentFuture;

        // Try to find CONFIG_FILENAME in /run/config.
        // If it exists, read it.
        Path pathConfigFile = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILENAME);
        if (Files.exists(pathConfigFile) && !Files.isDirectory(pathConfigFile)) {
            return currentFuture = CompletableFuture.supplyAsync(ExceptionUtils.wrapChecked(() -> {
                String s = Files.readString(pathConfigFile);
                Path path = Path.of(s);
                if (!Files.exists(path) || !Files.isDirectory(path))
                    throw new PathProvider.CouldNotLocateFolderError(null);
                return path;
            }, CompletionException::new));
        }

        // TODO make dialog title translatable
        return currentFuture = CompletableFuture.supplyAsync(ExceptionUtils.wrapChecked(() -> {
            // Prompt the user for a folder on another thread
            @Nullable String pathString = TinyFileDialogs.tinyfd_selectFolderDialog("Choose or create a folder to use as your Figura directory!", "");
            if (pathString == null) throw new PathProvider.CouldNotLocateFolderError(null);
            // Fetch the file
            Path path = Path.of(pathString);
            if (!Files.exists(path) || !Files.isDirectory(path)) throw new PathProvider.CouldNotLocateFolderError(null);
            // Now that we have the path and we know it's good, store it in /run/config/CONFIG_FILENAME before returning.
            Files.writeString(pathConfigFile, pathString, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            return path;
        }, CompletionException::new));
    }

}
