package es.superstrellaa.storagemanager.internal;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public final class StoragePaths {

    private static final Path BASE_DIR =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("storagemanager");

    public static Path getDatabasePath() {
        return BASE_DIR.resolve("main.db");
    }

    public static Path getBaseDir() {
        return BASE_DIR;
    }

    private StoragePaths() {}
}
