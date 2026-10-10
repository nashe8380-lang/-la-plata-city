package ar.laplata;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

/** /laplata generar | cancelar | info */
public final class LaPlataCommand {
	private LaPlataCommand() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, selection) ->
			dispatcher.register(Commands.literal("laplata")
				.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
				.then(Commands.literal("generar").executes(ctx -> generar(ctx.getSource())))
				.then(Commands.literal("cancelar").executes(ctx -> cancelar(ctx.getSource())))
				.then(Commands.literal("info").executes(ctx -> info(ctx.getSource())))));
	}

	private static int generar(CommandSourceStack source) {
		BlockPos centro = BlockPos.containing(source.getPosition());
		if (!CityBuilder.iniciar(source.getLevel(), centro)) {
			source.sendFailure(Component.literal("Ya hay una ciudad generandose. Usa /laplata cancelar para frenarla."));
			return 0;
		}
		source.sendSuccess(() -> Component.literal(
			"Generando La Plata centrada en tu posicion (Plaza Moreno). Usa un mundo superplano. "
				+ "Tarda unos minutos; el avance se informa cada 10%."), true);
		return 1;
	}

	private static int cancelar(CommandSourceStack source) {
		if (CityBuilder.cancelar()) {
			source.sendSuccess(() -> Component.literal("Generacion cancelada. Lo ya construido queda."), true);
			return 1;
		}
		source.sendFailure(Component.literal("No hay ninguna generacion en curso."));
		return 0;
	}

	private static int info(CommandSourceStack source) {
		String texto = "Ciudad de " + CityBuilder.MANZANAS + "x" + CityBuilder.MANZANAS + " manzanas, "
			+ CityBuilder.lado() + "x" + CityBuilder.lado() + " bloques, mas "
			+ CityBuilder.ANCHO_BOSQUE + " bloques de Paseo del Bosque al este.";
		source.sendSuccess(() -> Component.literal(texto), false);
		return 1;
	}
}
