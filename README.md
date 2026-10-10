# La Plata + Estudiantes (mod de Minecraft para Fabric)

Etapa 1: trazado fundacional de La Plata + contenido tematico de Estudiantes.

- Minecraft Java **26.3** (version estable mas nueva segun Fabric al 10/10/2026)
- Fabric Loader 0.19.5, Fabric API 0.162.0+26.3, Loom 1.17-SNAPSHOT
- Requiere **Java 25**

## Que incluye

- `/laplata generar`: genera la ciudad centrada en donde estas parado (Plaza Moreno al centro).
  Usalo en un mundo **superplano**. Se construye de a pocas columnas por tick y avisa cada 10%.
  - Cuadrado de 38 x 38 manzanas, una avenida cada 6 calles con cantero central
  - Diagonales 73 y 74 cruzandose en Plaza Moreno
  - Plazas en los cruces de avenidas internas
  - Paseo del Bosque al este, con arboles
- `/laplata cancelar` y `/laplata info`
- Bloques: rojo, blanco y a rayas (Pincha)
- Items: pelota, bufanda, camiseta (decorativos por ahora, con receta)

Las medidas en bloques estan al principio de `CityBuilder.java` (`MANZANA`, `CALLE`, `AVENIDA`, etc.).
Son una escala aproximada, no medidas oficiales: ajustalas a gusto.

## Compilar

1. Instala un JDK 25 y verifica con `java -version`.
2. En esta carpeta: `./gradlew build` (Windows: `gradlew.bat build`).
3. El mod queda en `build/libs/laplata-0.1.0.jar`.
4. Copialo a la carpeta `mods` junto con **Fabric API** (misma version de Minecraft).

Para probar rapido desde Gradle: `./gradlew runClient`.

## Texturas

`python3 tools/make_textures.py` regenera las texturas simples. Podes reemplazar los PNG
a mano (16x16) sin tocar nada mas. El escudo oficial del club no esta incluido.

## Proximas etapas

1. Edificios clave: Catedral, Palacio Municipal, Casa de Gobierno, Teatro Argentino, Museo, Estadio UNO
2. Lista real de plazas y nombres de calles
3. Camiseta y bufanda como armadura equipable
