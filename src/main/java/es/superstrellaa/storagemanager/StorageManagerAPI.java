package es.superstrellaa.storagemanager;

import es.superstrellaa.storagemanager.internal.lifecycle.ServerBootstrap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageManagerAPI implements ModInitializer {

	public static final String MOD_ID = "storagemanager-api";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/**
	 * TODO:
	 * * - Ver cómo hacerlo en mundos singleplayer
	 *
	 * * - Optimizar algo más el rendimiento, especialmente en la parte de escritura, para reducir la latencia y mejorar la eficiencia general.
	 * * - Separar clases grandes en varias más pequeñas para mejorar la mantenibilidad y la claridad del código.
	 * * - Añadir nuevas funcionalidades, como la capacidad de usar diferentes tipos de backend aparte de SQLite, como texto plano (JSON, XML...)
	 * * o bases de datos relacionales(PostgreSQL/MySQL) para ofrecer más flexibilidad y cosas raras
	 *
	 * * - Hacer el readme de los huevos y la wiki (importante en el readme agregar que .internal no se toca)
	 * * - Ah y el modrinth/curseforge
	 * */

	@Override
	public void onInitialize() {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
			ServerBootstrap.register();
		} else {
			LOGGER.warn("StorageManager API is running in client-side enviroment.");
			LOGGER.warn("This is normal, but keep in mind that all API operations will be no-ops and won't have any effect.");
		}

		LOGGER.info("StorageManager API initialized (common)");
	}
}