package es.superstrellaa.storagemanager;

import es.superstrellaa.storagemanager.internal.SQLiteBackend;
import es.superstrellaa.storagemanager.internal.lifecycle.ShutdownHook;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageManagerAPI implements ModInitializer {

	public static final String MOD_ID = "storagemanager-api";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		SQLiteBackend.init();
		ShutdownHook.register();

		LOGGER.info("StorageManager API initialized");
	}
}
