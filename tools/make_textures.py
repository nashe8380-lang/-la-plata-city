#!/usr/bin/env python3
"""Genera las texturas 16x16 del mod (sin dependencias, solo la libreria estandar).

Uso: python3 tools/make_textures.py
Escribe en src/main/resources/assets/laplata/textures/{block,item}/.
Para texturas mas lindas, reemplaza los PNG a mano: los nombres de archivo no cambian.
"""
import os
import struct
import zlib

ROJO = (200, 16, 46, 255)
ROJO_OSCURO = (160, 10, 36, 255)
BLANCO = (245, 245, 245, 255)
GRIS = (210, 210, 210, 255)
NEGRO = (25, 25, 25, 255)
CLARO = (0, 0, 0, 0)

BASE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..",
                    "src", "main", "resources", "assets", "laplata", "textures")


def png(path, pixels):
    """pixels: lista de 16 filas de 16 tuplas RGBA."""
    h, w = len(pixels), len(pixels[0])
    raw = b"".join(b"\x00" + b"".join(bytes(p) for p in row) for row in pixels)

    def chunk(tag, data):
        c = struct.pack(">I", len(data)) + tag + data
        return c + struct.pack(">I", zlib.crc32(tag + data) & 0xFFFFFFFF)

    data = (b"\x89PNG\r\n\x1a\n"
            + chunk(b"IHDR", struct.pack(">IIBBBBB", w, h, 8, 6, 0, 0, 0))
            + chunk(b"IDAT", zlib.compress(raw, 9))
            + chunk(b"IEND", b""))
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "wb") as f:
        f.write(data)


def lienzo(color=CLARO):
    return [[color for _ in range(16)] for _ in range(16)]


def borde(px, color):
    for i in range(16):
        px[0][i] = px[15][i] = px[i][0] = px[i][15] = color
    return px


# ---- Bloques ----
def bloque_liso(color, sombra):
    px = lienzo(color)
    for y in range(16):
        for x in range(16):
            if (x * 7 + y * 13) % 11 == 0:
                px[y][x] = sombra
    return borde(px, sombra)


def bloque_rayas():
    px = lienzo()
    for y in range(16):
        for x in range(16):
            px[y][x] = ROJO if (x // 4) % 2 == 0 else BLANCO
    return borde(px, GRIS)


# ---- Items ----
def item_pelota():
    px = lienzo()
    cx = cy = 7.5
    for y in range(16):
        for x in range(16):
            d = ((x - cx) ** 2 + (y - cy) ** 2) ** 0.5
            if d <= 6.5:
                px[y][x] = NEGRO if d >= 5.5 else BLANCO
    # parches oscuros del balon
    for (x, y) in [(7, 7), (8, 7), (7, 8), (8, 8), (4, 5), (11, 5), (4, 10), (11, 10), (7, 3), (8, 12)]:
        px[y][x] = NEGRO
    return px


def item_bufanda():
    px = lienzo()
    # Lazo horizontal con rayas rojiblancas + dos puntas colgando.
    for x in range(2, 14):
        for y in range(3, 7):
            px[y][x] = ROJO if (x // 2) % 2 == 0 else BLANCO
    for y in range(7, 14):
        for x in (4, 5, 10, 11):
            px[y][x] = ROJO if (y // 2) % 2 == 0 else BLANCO
    # flecos
    for x in (4, 5, 10, 11):
        px[14][x] = GRIS
    # contorno oscuro
    for x in range(2, 14):
        px[2][x] = ROJO_OSCURO
    return px


def item_camiseta():
    px = lienzo()
    cuerpo = [(x, y) for y in range(4, 15) for x in range(4, 12)]
    mangas = [(x, y) for y in range(2, 6) for x in list(range(1, 4)) + list(range(12, 15))]
    for (x, y) in cuerpo + mangas:
        px[y][x] = ROJO if (x // 2) % 2 == 0 else BLANCO
    # cuello
    for x in range(6, 10):
        px[2][x] = CLARO
        px[3][x] = ROJO_OSCURO if x in (6, 9) else CLARO
    for x in range(5, 11):
        px[2][x] = CLARO
    for x in range(6, 10):
        px[3][x] = ROJO_OSCURO
    return px


if __name__ == "__main__":
    png(os.path.join(BASE, "block", "rojo_pincha.png"), bloque_liso(ROJO, ROJO_OSCURO))
    png(os.path.join(BASE, "block", "blanco_pincha.png"), bloque_liso(BLANCO, GRIS))
    png(os.path.join(BASE, "block", "rayas_pincha.png"), bloque_rayas())
    png(os.path.join(BASE, "item", "pelota.png"), item_pelota())
    png(os.path.join(BASE, "item", "bufanda.png"), item_bufanda())
    png(os.path.join(BASE, "item", "camiseta.png"), item_camiseta())
    print("Texturas generadas.")
