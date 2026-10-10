package ar.laplata;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Genera el trazado fundacional de La Plata sobre el mundo, de a pocas columnas por tick
 * para no trabar el servidor.
 *
 * Datos del trazado (fuente: UNLP, revista Habitat): cuadrado de 38 x 38 manzanas, una avenida
 * cada 6 calles, diagonales 73 y 74 que se cruzan en Plaza Moreno (centro) y el Paseo del Bosque
 * al noreste. Las medidas en bloques (MANZANA, CALLE, AVENIDA, etc.) son una escala aproximada
 * que se puede ajustar libremente aca abajo.
 *
 * Usar en un mundo superplano: solo se coloca una capa de superficie (y arboles).
 */
public final class CityBuilder {
	// ---- Parametros del trazado (ajustables) ----
	public static final int MANZANAS = 38;
	public static final int MANZANA = 60;
	public static final int CALLE = 10;
	public static final int AVENIDA = 16;
	public static final int CADA_AVENIDA = 6;
	public static final int ANCHO_DIAGONAL = 16;
	public static final int VEREDA = 2;
	public static final int ANCHO_BOSQUE = 400;

	private static final int COLUMNAS_POR_TICK = 3000;

	// ---- Mapa 1D de un eje: se arma una vez y sirve para X y para Z ----
	private static final int LADO;
	private static final boolean[] ES_CALLE;
	private static final boolean[] ES_AVENIDA;
	private static final int[] INDICE;
	private static final int[] POS;

	static {
		List<int[]> tramos = new ArrayList<>(); // {ancho, esCalle, esAvenida, indice}
		int total = 0;
		for (int k = 0; k <= MANZANAS; k++) {
			boolean avenida = k % CADA_AVENIDA == 0 || k == MANZANAS;
			int ancho = avenida ? AVENIDA : CALLE;
			tramos.add(new int[] {ancho, 1, avenida ? 1 : 0, k});
			total += ancho;
			if (k < MANZANAS) {
				tramos.add(new int[] {MANZANA, 0, 0, k});
				total += MANZANA;
			}
		}
		LADO = total;
		ES_CALLE = new boolean[LADO];
		ES_AVENIDA = new boolean[LADO];
		INDICE = new int[LADO];
		POS = new int[LADO];
		int cursor = 0;
		for (int[] t : tramos) {
			for (int i = 0; i < t[0]; i++) {
				ES_CALLE[cursor] = t[1] == 1;
				ES_AVENIDA[cursor] = t[2] == 1;
				INDICE[cursor] = t[3];
				POS[cursor] = i;
				cursor++;
			}
		}
	}

	/** Ancho total (eje X) del area generada, incluyendo el Bosque. */
	public static int anchoTotal() {
		return LADO + ANCHO_BOSQUE;
	}

	/** Lado del cuadrado de la ciudad en bloques. */
	public static int lado() {
		return LADO;
	}

	// ---- Bloques usados ----
	private static final BlockState ASFALTO_CALLE = Blocks.GRAY_CONCRETE.defaultBlockState();
	private static final BlockState ASFALTO_AVENIDA = Blocks.BLACK_CONCRETE.defaultBlockState();
	private static final BlockState VEREDA_B = Blocks.POLISHED_ANDESITE.defaultBlockState();
	private static final BlockState PASTO = Blocks.GRASS_BLOCK.defaultBlockState();
	private static final BlockState CAMINO = Blocks.DIRT_PATH.defaultBlockState();
	private static final BlockState TRONCO = Blocks.OAK_LOG.defaultBlockState();
	private static final BlockState HOJAS = Blocks.OAK_LEAVES.defaultBlockState();

	// ---- Trabajo en curso (uno a la vez) ----
	private static final class Trabajo {
		final ServerLevel nivel;
		final int ox;
		final int gy;
		final int oz;
		final long total;
		long siguiente = 0;
		int ultimoPorcentaje = 0;

		Trabajo(ServerLevel nivel, int ox, int gy, int oz) {
			this.nivel = nivel;
			this.ox = ox;
			this.gy = gy;
			this.oz = oz;
			this.total = (long) anchoTotal() * LADO;
		}
	}

	private static Trabajo actual;

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(CityBuilder::tick);
	}

	/** Centra la ciudad (Plaza Moreno) en el punto dado. Devuelve false si ya hay una generacion en curso. */
	public static boolean iniciar(ServerLevel nivel, BlockPos centro) {
		if (actual != null) {
			return false;
		}
		int ox = centro.getX() - LADO / 2;
		int oz = centro.getZ() - LADO / 2;
		actual = new Trabajo(nivel, ox, centro.getY() - 1, oz);
		return true;
	}

	public static boolean cancelar() {
		boolean habia = actual != null;
		actual = null;
		return habia;
	}

	public static boolean enCurso() {
		return actual != null;
	}

	private static void tick(MinecraftServer server) {
		Trabajo t = actual;
		if (t == null) {
			return;
		}
		int ancho = anchoTotal();
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		int hechas = 0;
		while (hechas < COLUMNAS_POR_TICK && t.siguiente < t.total) {
			int x = (int) (t.siguiente % ancho);
			int z = (int) (t.siguiente / ancho);
			t.siguiente++;
			hechas++;

			BlockState estado = superficie(x, z);
			if (estado == null) {
				continue;
			}
			pos.set(t.ox + x, t.gy, t.oz + z);
			t.nivel.setBlock(pos, estado, Block.UPDATE_CLIENTS);

			if (estado == PASTO && quiereArbol(x, z)) {
				plantarArbol(t.nivel, t.ox + x, t.gy, t.oz + z);
			}
		}

		int porcentaje = (int) (t.siguiente * 100 / t.total);
		if (porcentaje >= t.ultimoPorcentaje + 10 && t.siguiente < t.total) {
			t.ultimoPorcentaje = porcentaje - porcentaje % 10;
			server.getPlayerList().broadcastSystemMessage(
				Component.literal("[La Plata] Generando ciudad: " + t.ultimoPorcentaje + "%"), false);
		}
		if (t.siguiente >= t.total) {
			actual = null;
			server.getPlayerList().broadcastSystemMessage(
				Component.literal("[La Plata] Listo. Plaza Moreno esta en el centro de la ciudad."), false);
		}
	}

	// ---- Que va en cada columna (x, z) del area, con (0,0) en la esquina noroeste ----
	private static BlockState superficie(int x, int z) {
		// Paseo del Bosque: al este de la ciudad, sobre el tramo central del lado.
		if (x >= LADO) {
			if (z < LADO / 4 || z >= LADO * 3 / 4) {
				return null;
			}
			return PASTO;
		}

		int centro = LADO / 2;
		int dx = x - centro;
		int dz = z - centro;

		// Plaza Moreno: una manzana al centro, cruzada por las dos diagonales.
		int mitad = MANZANA / 2;
		if (Math.abs(dx) <= mitad && Math.abs(dz) <= mitad) {
			boolean camino = Math.abs(dx) <= 1 || Math.abs(dz) <= 1
				|| Math.abs(dx - dz) <= 1 || Math.abs(dx + dz) <= 1;
			return camino ? CAMINO : PASTO;
		}

		// Diagonales 73 y 74: de esquina a esquina del cuadrado.
		double semiancho = ANCHO_DIAGONAL / 2.0;
		if (Math.abs(x - z) * 0.7071 <= semiancho
			|| Math.abs(x + z - (LADO - 1)) * 0.7071 <= semiancho) {
			return ASFALTO_AVENIDA;
		}

		boolean calleX = ES_CALLE[x];
		boolean calleZ = ES_CALLE[z];

		if (calleX || calleZ) {
			boolean avenida = (calleX && ES_AVENIDA[x]) || (calleZ && ES_AVENIDA[z]);
			// Cantero central de las avenidas (solo donde no cruza otra calle).
			if (calleX && !calleZ && ES_AVENIDA[x] && POS[x] == AVENIDA / 2) {
				return PASTO;
			}
			if (calleZ && !calleX && ES_AVENIDA[z] && POS[z] == AVENIDA / 2) {
				return PASTO;
			}
			return avenida ? ASFALTO_AVENIDA : ASFALTO_CALLE;
		}

		// Dentro de una manzana.
		int mi = INDICE[x];
		int mj = INDICE[z];
		int px = POS[x];
		int pz = POS[z];

		if (esPlaza(mi) && esPlaza(mj)) {
			boolean cruz = Math.abs(px - MANZANA / 2) <= 1 || Math.abs(pz - MANZANA / 2) <= 1;
			return cruz ? CAMINO : PASTO;
		}
		boolean vereda = px < VEREDA || px >= MANZANA - VEREDA || pz < VEREDA || pz >= MANZANA - VEREDA;
		return vereda ? VEREDA_B : PASTO;
	}

	/**
	 * Plazas en los cruces de avenidas internas (aprox.: la real tiene ~23 plazas y parques).
	 * Es una simplificacion para la etapa 1; despues se puede reemplazar por una lista con
	 * las plazas reales (Moreno, San Martin, Italia, Rivadavia, Paso, etc.).
	 */
	private static boolean esPlaza(int manzana) {
		return manzana % CADA_AVENIDA == 0 && manzana >= CADA_AVENIDA && manzana <= MANZANAS - CADA_AVENIDA;
	}

	private static boolean zonaArbolada(int x, int z) {
		if (x >= LADO) {
			return x < LADO + ANCHO_BOSQUE - 3 && z >= LADO / 4 + 3 && z < LADO * 3 / 4 - 3;
		}
		if (ES_CALLE[x] || ES_CALLE[z]) {
			return false;
		}
		return esPlaza(INDICE[x]) && esPlaza(INDICE[z])
			&& POS[x] > 4 && POS[x] < MANZANA - 4 && POS[z] > 4 && POS[z] < MANZANA - 4;
	}

	private static boolean quiereArbol(int x, int z) {
		int h = ((x * 73856093) ^ (z * 19349663)) & 0x7fffffff;
		return h % 600 == 0 && zonaArbolada(x, z);
	}

	private static void plantarArbol(ServerLevel nivel, int x, int gy, int z) {
		BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
		for (int dy = 1; dy <= 5; dy++) {
			p.set(x, gy + dy, z);
			nivel.setBlock(p, TRONCO, Block.UPDATE_CLIENTS);
		}
		for (int dy = 3; dy <= 6; dy++) {
			int radio = dy >= 5 ? 1 : 2;
			for (int ax = -radio; ax <= radio; ax++) {
				for (int az = -radio; az <= radio; az++) {
					if (Math.abs(ax) == radio && Math.abs(az) == radio && radio == 2) {
						continue;
					}
					if (ax == 0 && az == 0 && dy <= 5) {
						continue;
					}
					p.set(x + ax, gy + dy, z + az);
					if (nivel.getBlockState(p).isAir()) {
						nivel.setBlock(p, HOJAS, Block.UPDATE_CLIENTS);
					}
				}
			}
		}
	}
}
