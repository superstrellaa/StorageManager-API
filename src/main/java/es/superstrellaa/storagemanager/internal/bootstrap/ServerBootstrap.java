package es.superstrellaa.storagemanager.internal.bootstrap;

import es.superstrellaa.storagemanager.StorageManagerAPI;
import es.superstrellaa.storagemanager.internal.SQLiteBackend;
import es.superstrellaa.storagemanager.internal.cache.WriteCache;
import es.superstrellaa.storagemanager.internal.lifecycle.ShutdownHook;
import es.superstrellaa.storagemanager.internal.middleware.ServerGuard;

public final class ServerBootstrap {

    private static boolean initialized = false;

    public static void register() {
        if (initialized) return;

        SQLiteBackend.init();
        WriteCache.getInstance().start();
        ShutdownHook.register();

        ServerGuard.markServerReady();

        initialized = true;
        StorageManagerAPI.LOGGER.info("StorageManager API initialized (server-side)");
    }

    private ServerBootstrap() {}
}