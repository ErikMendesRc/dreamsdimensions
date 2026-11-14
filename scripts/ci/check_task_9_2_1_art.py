#!/usr/bin/env python3
"""Task 9.2.1: deterministic asset regression checks. Does not claim artistic approval."""
from pathlib import Path
import json
import struct
import zlib
from audit_visual_assets import png_details, assert_png_integrity

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src/main/resources/assets/dreamsdimensions"
HERBS = ("vigil_herb_open", "vigil_herb_closed")
ITEMS = ("vigil_herb_leaf", "vigil_herb_seeds", "toasted_vigil_herb", "return_charge")


def rgba(path):
    """Decode RGBA PNG scanlines without requiring Pillow in CI."""
    assert_png_integrity(path)
    data = path.read_bytes()
    w, h = png_details(path)["width"], png_details(path)["height"]
    assert (data[24], data[25], data[28]) == (8, 6, 0), f"{path}: require RGBA8 non-interlaced"
    at, idat = 8, bytearray()
    while at + 12 <= len(data):
        size = struct.unpack_from(">I", data, at)[0]
        kind = data[at+4:at+8]
        if kind == b"IDAT":
            idat.extend(data[at+8:at+8+size])
        at += size + 12
        if kind == b"IEND":
            break
    raw = zlib.decompress(idat)
    stride, prev, pos, rows = w*4, bytearray(w*4), 0, []
    for _ in range(h):
        filt = raw[pos]
        encoded = raw[pos+1:pos+stride+1]
        assert 0 <= filt <= 4 and len(encoded) == stride
        row = bytearray(stride)
        for n, src in enumerate(encoded):
            left = row[n-4] if n>=4 else 0
            above = prev[n]
            corner = prev[n-4] if n>=4 else 0
            if filt == 0: predictor = 0
            elif filt == 1: predictor = left
            elif filt == 2: predictor = above
            elif filt == 3: predictor = (left+above)//2
            else:
                p = left+above-corner
                diffs = (abs(p-left),abs(p-above),abs(p-corner))
                predictor = (left,above,corner)[diffs.index(min(diffs))]
            row[n] = (src+predictor)&255
        rows.append([tuple(row[x:x+4]) for x in range(0,stride,4)])
        prev, pos = row, pos+stride+1
    assert pos == len(raw), f"{path}: unexpected scanline length"
    return rows


def verify():
    sprites = {}
    for stem in HERBS+ITEMS:
        folder = "block" if stem in HERBS else "item"
        file = ASSETS / f"textures/{folder}/{stem}.png"
        dims = png_details(file)
        assert (dims["width"],dims["height"],dims["color_type"]) == (16,16,6), stem
        pixels = rgba(file)
        colors = {px for row in pixels for px in row if px[3]}
        alpha = {px[3] for row in pixels for px in row}
        assert alpha.issubset({0,255}), f"{stem}: antialiasing detected"
        assert len(colors) >= 6, f"{stem}: insufficient material variation"
        assert sum(px[3]>0 for row in pixels for px in row)>=35, f"{stem}: empty silhouette"
        sprites[stem] = pixels
    assert sprites["vigil_herb_open"][9:] == sprites["vigil_herb_closed"][9:], "plant anchor jumps at day/night switch"
    assert [[p[3] for p in row] for row in sprites["vigil_herb_leaf"]] == [[p[3] for p in row] for row in sprites["toasted_vigil_herb"]], "toasted leaf silhouette diverges"
    gui_file = ASSETS / "textures/gui/container/oneiric_core_gui.png"
    dims = png_details(gui_file)
    assert (dims["width"],dims["height"],dims["color_type"])==(176,166,6)
    gui = rgba(gui_file)
    assert all(pixel[3]==255 for row in gui for pixel in row)
    for x in (29,54,79):
        assert gui[34][x][:3] == (158,165,150), f"input slot at x={x} shifted"
    assert gui[34][138][:3] == (161,169,156), "result slot shifted"
    for x,y in ((8,84),(8,102),(8,120),(8,142)):
        assert gui[y][x][:3] == (158,165,150), f"inventory slot shifted: {x},{y}"
    for state in ("open","closed"):
        model = json.loads((ASSETS/f"models/block/vigil_herb_{state}.json").read_text(encoding="utf-8"))
        assert model["parent"] == "minecraft:block/cross"
        assert model["render_type"] == "cutout"
        assert model["textures"]["cross"] == f"dreamsdimensions:block/vigil_herb_{state}"
    states = json.loads((ASSETS/"blockstates/vigil_herb.json").read_text(encoding="utf-8"))["variants"]
    for state in ("true","false"):
        suffix = "open" if state=="true" else "closed"
        assert states[f"age=3,open={state}"]["model"] == f"dreamsdimensions:block/vigil_herb_{suffix}"
    print("PASS Task 9.2.1: six RGBA16 textures, GUI slot geometry, day/night continuity, model references")


if __name__ == "__main__":
    verify()
