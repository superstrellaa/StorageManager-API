package es.superstrellaa.storagemanager.internal.lifecycle;

import es.superstrellaa.storagemanager.internal.SQLiteBackend;
import es.superstrellaa.storagemanager.internal.cache.WriteCache;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class ShutdownHook {

    public static void register() {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            WriteCache.getInstance().shutdown();
            SQLiteBackend.shutdown();
        });
    }

    private ShutdownHook() {}
}