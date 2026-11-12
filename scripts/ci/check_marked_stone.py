#!/usr/bin/env python3
"""Task 7.3 F0a Marked Stone registry/assets/loot/tier contract. STATIC ONLY."""
import argparse
import json
import re
import struct
import sys
import zlib
from pathlib import Path
from content_contract import ContentContractError, read_json, validate_png, validate

MOD = "dreamsdimensions"
SCHEMA = "dreamsdimensions.qa.v1"


def require(value, message):
    if not value:
        raise ContentContractError(message)


def png_info(path):
    validate_png(path)
    data = path.read_bytes()
    offset, header, pixels = 8, None, bytearray()
    found_end = False
    while offset < len(data):
        require(offset + 12 <= len(data), "Truncated PNG chunk")
        size = struct.unpack(">I", data[offset:offset+4])[0]
        name = data[offset+4:offset+8]
        stop = offset + 12 + size
        require(stop <= len(data), "Invalid PNG chunk size")
        value = data[offset+8:offset+8+size]
        crc = struct.unpack(">I", data[offset+8+size:stop])[0]
        require(zlib.crc32(name + value) & 0xffffffff == crc,
                f"Texture PNG corrupt CRC in {name!r}")
        if name == b"IHDR":
            require(header is None, "Duplicate PNG IHDR")
            header = value
        if name == b"IDAT":
            pixels.extend(value)
        offset = stop
        if name == b"IEND":
            found_end = True
            break
    require(found_end and offset == len(data), "Missing IEND or trailing PNG bytes")
    require(header is not None and len(header) == 13, "Invalid IHDR")
    width, height, depth, kind, compression, filtering, interlace = struct.unpack(
        ">IIBBBBB", header)
    require((width, height, depth, kind, compression, filtering, interlace) ==
            (16, 16, 8, 6, 0, 0, 0), "Texture must be 16x16 RGBA 8-bit")
    raw = zlib.decompress(bytes(pixels))
    require(len(raw) == 16 * (1 + 16 * 4), "Incorrect decoded scanline length")
    return {"width": width, "height": height, "format": "RGBA8"}


def run(root):
    require(validate(root)["status"] == "PASS", "Task 7.2 content contract failed")
    j = root / "src/main/java/com/dreamsdimensions/mod/registry"
    res = root / "src/main/resources"
    assets = res / f"assets/{MOD}"
    data = res / f"data/{MOD}"
    blocks = (j / "ModBlocks.java").read_text(encoding="utf-8")
    items = (j / "ModItems.java").read_text(encoding="utf-8")
    creative = (j / "ModCreativeTabs.java").read_text(encoding="utf-8")
    head = "public static final DeferredBlock<Block> MARKED_STONE"
    require(blocks.count(head) == 1, "Missing/duplicate Block registry")
    section = blocks.split(head, 1)[1].split("public static final DeferredBlock<", 1)[0]
    require('BLOCKS.registerSimpleBlock(' in section and '"marked_stone"' in section,
            "Wrong Marked Stone registry ID")
    require("Blocks.TUFF" in section and ".strength(1.5F, 6.0F)" in section,
            "Hardness and Tuff-like material contract changed")
    require(".requiresCorrectToolForDrops()" in section,
            "Marked Stone must require correct pickaxe for drops")
    require(items.count("ModBlocks.MARKED_STONE") == 1,
            "BlockItem must be registered exactly once")
    require(creative.count("output.accept(ModBlocks.MARKED_STONE.get());") == 1,
            "Marked Stone must appear exactly once in Creative Tab")

    require(read_json(assets / "blockstates/marked_stone.json") ==
            {"variants": {"": {"model": f"{MOD}:block/marked_stone"}}},
            "Wrong blockstate / block model reference")
    model = read_json(assets / "models/block/marked_stone.json")
    require(model.get("parent") == "minecraft:block/cube_all",
            "Marked Stone must use vanilla cube_all model")
    require(model.get("textures") == {
        "all": f"{MOD}:block/marked_stone",
        "particle": f"{MOD}:block/marked_stone"},
        "Missing/wrong block and particle textures")
    require(read_json(assets / "items/marked_stone.json") == {
        "model": {"type": "minecraft:model", "model": f"{MOD}:item/marked_stone"}},
        "Client item definition not compatible with Minecraft 26.3")
    require(read_json(assets / "models/item/marked_stone.json") ==
            {"parent": f"{MOD}:block/marked_stone"},
            "Item must render full Marked Stone block")
    texture = png_info(assets / "textures/block/marked_stone.png")
    for lang, label in (("pt_br", "Pedra Marcada"), ("en_us", "Marked Stone")):
        require(read_json(assets / f"lang/{lang}.json").get(f"block.{MOD}.marked_stone") == label,
                f"Missing/incorrect translation: {lang}")

    for name in ("mineable/pickaxe", "needs_stone_tool"):
        tag = read_json(res / f"data/minecraft/tags/block/{name}.json")
        require(tag.get("replace") is False and
                tag.get("values", []).count(f"{MOD}:marked_stone") == 1,
                f"Missing/duplicated tool tag {name}")
    for tier in ("needs_iron_tool", "needs_diamond_tool"):
        path = res / f"data/minecraft/tags/block/{tier}.json"
        if path.is_file():
            require(f"{MOD}:marked_stone" not in read_json(path).get("values", []),
                    f"Marked Stone must not require {tier}")
    loot = read_json(data / "loot_table/blocks/marked_stone.json")
    require(loot.get("type") == "minecraft:block" and len(loot.get("pools", [])) == 1,
            "Expected block loot table with one pool")
    pool = loot["pools"][0]
    require(pool.get("rolls") == 1 and pool.get("entries") == [{
        "type": "minecraft:item", "name": f"{MOD}:marked_stone",
        "modifier": [{"type": "minecraft:explosion_decay"}]}],
        "Marked Stone must drop exactly itself, not dust or XP or Fortune bonus")
    require("dream_dust" not in json.dumps(loot), "Marked Stone must not drop Dream Dust")
    for category in ("recipe", "worldgen/feature", "worldgen/placed_feature"):
        require(not (data / category / "marked_stone.json").exists(),
                f"Task 7.3 must not ship {category}/marked_stone.json")
    require(not (data / "recipe/cut_marked_stone.json").exists(),
            "Lapidation belongs to Task 7.4")
    return {
        "schema_version": SCHEMA, "test_id": "task_7_3_marked_stone_static",
        "category": "CONTENT", "status": "PASS", "evidence_type": "STATIC_CHECK",
        "block_id": f"{MOD}:marked_stone",
        "stone_or_better": True, "loot": "own_block_1_no_fortune",
        "has_natural_worldgen": False, "has_lapidation": False, "texture": texture,
        "limitations": [
            "Static evidence does not prove client rendering or Survival mining/drops",
            "Natural placement and stonecutting remain reserved for Task 7.4",
        ],
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--report", type=Path)
    args = parser.parse_args()
    try:
        report = run(args.root.resolve())
    except (ContentContractError, ValueError, OSError, zlib.error, struct.error) as exc:
        report = {"schema_version": SCHEMA, "test_id": "task_7_3_marked_stone_static",
                  "category": "CONTENT", "status": "FAIL",
                  "evidence_type": "STATIC_CHECK", "error": str(exc)}
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(json.dumps(report, indent=2, sort_keys=True) + "\n",
                               encoding="utf-8")
    if report["status"] != "PASS":
        print("TASK 7.3 MARKED_STONE STATIC FAIL:", report["error"], file=sys.stderr)
        return 1
    print("TASK 7.3 MARKED_STONE STATIC PASS:", json.dumps(report["texture"]))
    print("LIMITATION: client rendering, mining and natural generation NOT_RUN")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
