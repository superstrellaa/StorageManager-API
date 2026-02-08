package es.superstrellaa.storagemanager.internal.lifecycle;

import es.superstrellaa.storagemanager.StorageManagerAPI;
import es.superstrellaa.storagemanager.internal.SQLiteBackend;
import es.superstrellaa.storagemanager.internal.cache.WriteCache;
import es.superstrellaa.storagemanager.internal.middleware.ServerGuard;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class ServerBootstrap {

    private static boolean initialized = false;

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (initialized) return;

            SQLiteBackend.init();
            WriteCache.getInstance().start();
            ShutdownHook.register();

            ServerGuard.markServerReady();

            initialized = true;
            StorageManagerAPI.LOGGER.info("StorageManager API initialized (server-side)");
        });
    }

    private ServerBootstrap() {}
}