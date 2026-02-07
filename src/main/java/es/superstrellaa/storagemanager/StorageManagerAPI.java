package es.superstrellaa.storagemanager;

import es.superstrellaa.storagemanager.internal.SQLiteBackend;
import es.superstrellaa.storagemanager.internal.cache.WriteCache;
import es.superstrellaa.storagemanager.internal.lifecycle.ShutdownHook;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageManagerAPI implements ModInitializer {

	public static final String MOD_ID = "storagemanager-api";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	//TODO General: Optimizar algo más, separar clases grandes en varias pequeñas y nuevas funcionalidades(añadir sistema de usar otro tipo de Backend aparte de SQLite, como texto plano(json, xml...) o PostgreSQL/MySQL)

	@Override
	public void onInitialize() {
		SQLiteBackend.init();
		WriteCache.getInstance().start();
		ShutdownHook.register();

		LOGGER.info("StorageManager API initialized with write-behind cache");
	}
}