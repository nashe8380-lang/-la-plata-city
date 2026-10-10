package ar.laplata;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LaPlataMod implements ModInitializer {
	public static final String MOD_ID = "laplata";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		ModBlocks.initialize();
		ModItems.initialize();
		CityBuilder.register();
		LaPlataCommand.register();
		LOGGER.info("La Plata + Estudiantes cargado. Usa /laplata generar en un mundo superplano.");
	}
}
