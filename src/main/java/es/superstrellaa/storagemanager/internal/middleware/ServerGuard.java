package es.superstrellaa.storagemanager.internal.middleware;

import es.superstrellaa.storagemanager.StorageManagerAPI;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class ServerGuard {

    private static volatile boolean serverReady = false;

    public static void markServerReady() {
        serverReady = true;
    }

    public static boolean requireServer() {
        if (!serverReady) {
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
                StorageManagerAPI.LOGGER.error("StorageManager API is not ready yet! This likely means that you are trying to use it during mod initialization, which is too early. Please wait until the server has started.");
            } else {
                StorageManagerAPI.LOGGER.warn("StorageManager API isn't meant for here! This likely means that you are trying to use it on the client side, which is not supported. Please only use the API on the server side.");
            }
            return false;
        }
        return true;
    }

    private ServerGuard() {}
}
