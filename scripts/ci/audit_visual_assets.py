#!/usr/bin/env python3
"""Task 8 — visual asset & presentation inventory, standard-library only.

Checks data, dimensions and model wiring. It does NOT pretend to have visually
inspected pixels in-game or replaced the artist's high-resolution images.
"""
from __future__ import annotations

import argparse
from collections import Counter
import hashlib
import json
import re
from pathlib import Path
import struct
import sys
import zlib

MOD = "dreamsdimensions"
ASSETS = Path("src/main/resources/assets") / MOD
REGISTRY = Path("src/main/java/com/dreamsdimensions/mod/registry")
ROTATED_NATURAL = frozenset((
    "serene_stone", "blue_dream_stone", "blue_dream_cobblestone",
    "dream_sand_block", "dream_dirt_block", "blue_dream_dirt",
    "dream_glow_moss", "dream_grass_block", "blue_dream_grass",
))
MODEL_REF = re.compile(r"^(?:dreamsdimensions):([a-z0-9_./-]+)$")
ICON_REF = re.compile(r"output\.accept\((ModItems|ModBlocks)\.([A-Z_]+)\.get\(\)\);")
PNG_SIG = b"\x89PNG\r\n\x1a\n"


class VisualAuditError(ValueError):
    pass


def require(ok: bool, msg: str) -> None:
    if not ok:
        raise VisualAuditError(msg)


def load_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def png_details(path: Path) -> dict:
    data = path.read_bytes()
    require(len(data) >= 45 and data.startswith(PNG_SIG),
            f"Invalid PNG signature: {path}")
    require(data[12:16] == b"IHDR" and struct.unpack(">I", data[8:12])[0] == 13,
            f"Invalid PNG IHDR: {path}")
    require(zlib.crc32(data[12:29]) & 0xFFFFFFFF == struct.unpack(">I", data[29:33])[0],
            f"Invalid PNG header CRC: {path}")
    width, height, depth, color, compression, filtering, interlace = struct.unpack(
        ">IIBBBBB", data[16:29]
    )
    require(0 < width <= 4096 and 0 < height <= 4096,
            f"Invalid PNG dimensions: {path} {width}x{height}")
    require(depth in (8, 16) and color in (0, 2, 3, 4, 6),
            f"Unsupported PNG bit depth/color: {path} ({depth}, {color})")
    require(compression == 0 and filtering == 0 and interlace in (0, 1),
            f"Unsupported PNG compression/filter/interlace: {path}")
    require(data[-12:] == b"\x00\x00\x00\x00IEND\xaeB\x60\x82",
            f"PNG missing terminal IEND: {path}")
    return {
        "width": width, "height": height, "bit_depth": depth,
        "color_type": color, "bytes": len(data), "sha256": hashlib.sha256(data).hexdigest(),
        "pixel_art_16": (width, height) == (16, 16),
        "power_of_two": ((width & (width - 1)) == 0 and
                         (height & (height - 1)) == 0),
    }


def unique_lang_keys(path: Path) -> dict:
    pairs = []

    def pairs_hook(rows):
        result = {}
        for key, value in rows:
            if key in result:
                raise VisualAuditError(f"Duplicate language key {key} in {path}")
            result[key] = value
        return result

    result = json.loads(path.read_text(encoding="utf-8"), object_pairs_hook=pairs_hook)
    require(isinstance(result, dict), f"Invalid locale format: {path}")
    require(all(isinstance(k, str) and isinstance(v, str) and v.strip()
                for k, v in result.items()), f"Blank/invalid locale entries: {path}")
    return result


def model_refs(node):
    """Traverse 26.3 client model alternatives and regular blockstate variants."""
    if isinstance(node, dict):
        for key, val in node.items():
            if key in ("model", "parent") and isinstance(val, str):
                yield val
            elif isinstance(val, (dict, list)):
                yield from model_refs(val)
    elif isinstance(node, list):
        for item in node:
            yield from model_refs(item)


def audit(root: Path, enforce_current_presentation: bool = True) -> dict:
    assets = root / ASSETS
    textures = sorted((assets / "textures").rglob("*.png"))
    require(textures, "Missing asset textures")
    texture_details = {}
    hashes = {}
    for file in textures:
        rel = file.relative_to(assets).as_posix()
        info = png_details(file)
        texture_details[rel] = info
        hashes.setdefault(info["sha256"], []).append(rel)
    duplicate_content = [sorted(v) for v in hashes.values() if len(v) > 1]

    # High-resolution artwork in existing repository is intentionally NOT
    # resampled blind. This debt will need an artistic A/B client review.
    legacy = [
        {
            "asset": name,
            "resolution": f"{info['width']}x{info['height']}",
            "bytes": info["bytes"],
            "reason": ("texture_is_not_16x16" if "textures/block/" in name
                       else "item_sprite_above_64px"),
        }
        for name, info in texture_details.items()
        if (name.startswith("textures/block/") and not info["pixel_art_16"])
        or (name.startswith("textures/item/") and max(info["width"], info["height"]) > 64)
    ]

    en = unique_lang_keys(assets / "lang/en_us.json")
    pt = unique_lang_keys(assets / "lang/pt_br.json")
    require(en.keys() == pt.keys(),
            f"Language keys mismatch: en_only={sorted(en.keys()-pt.keys())} "
            f"pt_only={sorted(pt.keys()-en.keys())}")

    blockstates_dir = assets / "blockstates"
    blockstates = sorted(blockstates_dir.glob("*.json"))
    items = sorted((assets / "items").glob("*.json"))
    models = sorted((assets / "models").rglob("*.json"))
    references = set()
    for p in blockstates + items + models:
        obj = load_json(p)
        for ref in model_refs(obj):
            match = MODEL_REF.match(ref)
            if match:
                target = match.group(1)
                ref_path = assets / "models" / f"{target}.json"
                require(ref_path.is_file(), f"Missing model: {ref} from {p}")
                references.add(target)

    unused_models = sorted(
        p.relative_to(assets / "models").with_suffix("").as_posix()
        for p in models if p.relative_to(assets / "models").with_suffix("").as_posix()
        not in references
    )
    # Orphan model inventory is informational: custom display/entity renderers
    # and external resource packs may reference assets not used by client items.
    expected_rotation = (0, 90, 180, 270)
    visual_variants = {}
    for name in ROTATED_NATURAL:
        path = blockstates_dir / f"{name}.json"
        require(path.exists(), f"Missing natural blockstate {name}")
        variants = load_json(path).get("variants", {}).get("")
        if enforce_current_presentation:
            require(isinstance(variants, list) and len(variants) == 4,
                    f"Natural material {name} should have four rotation variants")
            rotations = sorted(row.get("y") for row in variants)
            require(rotations == list(expected_rotation), f"Invalid Y variants {name}")
            require(all(row.get("model") == f"{MOD}:block/{name}" and row.get("weight") == 1
                        for row in variants), f"{name}: invalid material model/weight")
        visual_variants[name] = len(variants) if isinstance(variants, list) else 1

    flower = load_json(assets / "models/item/dream_flower_block.json")
    flower_world = load_json(assets / "models/block/dream_flower_block.json")
    if enforce_current_presentation:
        require(flower.get("parent") == "minecraft:item/generated" and
                flower.get("textures", {}).get("layer0") ==
                "dreamsdimensions:block/dream_flower_block",
                "Dream Flower inventory must be a flat vanilla-style item sprite")
        require(flower_world.get("textures", {}).get("particle") ==
                flower_world.get("textures", {}).get("cross"),
                "Dream Flower particle texture must match the actual crossed sprite")

    creative_source = (root / REGISTRY / "ModCreativeTabs.java").read_text(encoding="utf-8")
    creative = ICON_REF.findall(creative_source)
    require(creative and len(creative) == len(set(creative)),
            "Creative tab has missing/duplicate registered entries")
    if enforce_current_presentation:
        require("new ItemStack(ModItems.DREAM_NECKLACE.get())" in creative_source,
                "Creative tab needs the compact necklace icon")
        require(creative[:3] == [
            ("ModItems", "DREAM_NECKLACE"),
            ("ModItems", "ONEIRIC_AWAKENER"),
            ("ModItems", "DREAM_DUST"),
        ], "Creative tab navigation group out of order")
    return {
        "schema_version": "dreamsdimensions.qa.v1",
        "test_id": "task_8_visual_asset_quality",
        "category": "CONTENT",
        "status": "PASS",
        "evidence_type": "STATIC_CHECK",
        "visual_review": "NOT_RUN",
        "counts": {
            "blockstates": len(blockstates),
            "client_item_definitions": len(items),
            "models": len(models),
            "png_textures": len(textures),
            "pixel_art_16_textures": sum(info["pixel_art_16"] for info in texture_details.values()),
            "oversized_or_non16_assets": len(legacy),
            "total_texture_bytes": sum(info["bytes"] for info in texture_details.values()),
            "duplicate_binary_groups": len(duplicate_content),
            "orphan_model_candidates": len(unused_models),
            "locale_keys": len(en),
            "creative_entries": len(creative),
            "rotation_materials": len(visual_variants),
        },
        "legacy_texture_debt": sorted(legacy, key=lambda row: -row["bytes"]),
        "potential_duplicate_textures": duplicate_content,
        "potential_orphan_models": unused_models,
        "material_variants": dict(sorted(visual_variants.items())),
        "limitations": [
            "Visual check is STATIC: no runtime screenshot, tiling score, FPS or subjective QA claim",
            "High-resolution textures were retained, not downsampled blindly",
            "Worldgen density/count/placements and gameplay content are unchanged",
            "Texture format report is diagnostic, not automatic permission to delete assets",
        ],
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--report", type=Path)
    args = parser.parse_args()
    try:
        report = audit(args.root.resolve())
    except (VisualAuditError, OSError, ValueError, KeyError, TypeError) as exc:
        report = {
            "schema_version": "dreamsdimensions.qa.v1",
            "test_id": "task_8_visual_asset_quality",
            "category": "CONTENT", "evidence_type": "STATIC_CHECK",
            "status": "FAIL", "error": str(exc),
        }
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(json.dumps(report, indent=2, ensure_ascii=False,
                                         sort_keys=True) + "\n", encoding="utf-8")
    if report["status"] != "PASS":
        print("TASK 8 VISUAL AUDIT FAIL:", report["error"], file=sys.stderr)
        return 1
    print("TASK 8 VISUAL AUDIT STATIC PASS:", json.dumps(report["counts"], sort_keys=True))
    print("CLIENT ART REVIEW: NOT_RUN. High-resolution source textures retained.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
