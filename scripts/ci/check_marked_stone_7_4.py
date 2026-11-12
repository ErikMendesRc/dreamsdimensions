#!/usr/bin/env python3
"""Task 7.4 F0b: test configured worldgen, variant, stonecutter and no economy leakage.

STATIC_CHECK only; actual naturally generated coordinates are audited by
audit_marked_stone_worldgen_runtime.py on a disposable vanilla Overworld.
"""
import json
from pathlib import Path
import sys
from content_contract import ContentContractError, read_json, validate
from check_marked_stone import run as check_original, png_info

ROOT = Path(__file__).resolve().parents[2]
MOD = "dreamsdimensions"


def require(ok, msg):
    if not ok:
        raise ContentContractError(msg)


def inspect(root):
    prior = check_original(root)
    require(prior["status"] == "PASS", "Task 7.3 F0a regression failed")
    allcontent = validate(root)
    rootdata = root / "src/main/resources/data/dreamsdimensions"
    rootassets = root / "src/main/resources/assets/dreamsdimensions"
    rootmc = root / "src/main/resources/data/minecraft"
    registry = root / "src/main/java/com/dreamsdimensions/mod/registry"

    blocks = (registry / "ModBlocks.java").read_text(encoding="utf-8")
    items = (registry / "ModItems.java").read_text(encoding="utf-8")
    creative = (registry / "ModCreativeTabs.java").read_text(encoding="utf-8")
    require(blocks.count('CUT_MARKED_STONE = BLOCKS.registerSimpleBlock(') == 1,
            "Missing/duplicate cut block")
    require(blocks.count('"cut_marked_stone"') == 1, "Wrong cut block registry ID")
    section = blocks.split('CUT_MARKED_STONE =', 1)[1].split('    public static final', 1)[0]
    require('.requiresCorrectToolForDrops()' in section, "Cut stone must require correct tool")
    require("Blocks.TUFF" in section, "Wrong cut stone baseline material")
    require(items.count("ModBlocks.CUT_MARKED_STONE") == 1,
            "Cut variant must have exactly one BlockItem")
    require(creative.count("output.accept(ModBlocks.CUT_MARKED_STONE.get());") == 1,
            "Cut variant must appear exactly once in creative tab")

    for tag in ("mineable/pickaxe", "needs_stone_tool"):
        values = read_json(rootmc / f"tags/block/{tag}.json")
        require(values.get("replace") is False and
                values.get("values", []).count(f"{MOD}:cut_marked_stone") == 1,
                f"Cut variant missing {tag}")
    require(read_json(rootassets / "blockstates/cut_marked_stone.json") ==
            {"variants": {"": {"model": f"{MOD}:block/cut_marked_stone"}}},
            "Incorrect cut blockstate")
    require(read_json(rootassets / "items/cut_marked_stone.json") ==
            {"model": {"type": "minecraft:model",
                       "model": f"{MOD}:item/cut_marked_stone"}},
            "Missing 26.3 item client definition")
    require(read_json(rootassets / "models/item/cut_marked_stone.json") ==
            {"parent": f"{MOD}:block/cut_marked_stone"}, "Wrong cut item parent")
    blockmodel = read_json(rootassets / "models/block/cut_marked_stone.json")
    require(blockmodel.get("parent") == "minecraft:block/cube_all", "Wrong cut block parent")
    require(blockmodel.get("textures") == {
        "all": f"{MOD}:block/cut_marked_stone", "particle": f"{MOD}:block/cut_marked_stone"},
        "Wrong cut block textures")
    cuttexture = rootassets / "textures/block/cut_marked_stone.png"
    cut_info = png_info(cuttexture)
    require(cuttexture.read_bytes() !=
            (rootassets / "textures/block/marked_stone.png").read_bytes(),
            "Cut and rough stone must have distinct pixel art")
    for lang, name in (("pt_br", "Pedra Marcada Lapidada"), ("en_us", "Cut Marked Stone")):
        require(read_json(rootassets / f"lang/{lang}.json").get(
                    f"block.{MOD}.cut_marked_stone") == name,
                f"Missing cut stone display name in {lang}")
    loot = read_json(rootdata / "loot_table/blocks/cut_marked_stone.json")
    require(loot.get("type") == "minecraft:block" and len(loot.get("pools", [])) == 1,
            "Cut stone loot pool must be a single block table")
    require(loot["pools"][0].get("rolls") == 1 and
            loot["pools"][0].get("entries") == [{
                "type": "minecraft:item", "name": f"{MOD}:cut_marked_stone",
                "modifier": [{"type": "minecraft:explosion_decay"}]}],
            "Cut stone must return exactly itself without ore rewards")
    for path in (rootdata / "loot_table/blocks").glob("*marked_stone*.json"):
        require("dream_dust" not in path.read_text(encoding="utf-8"),
                "F0 is independent of Dream Dust")

    recipes = sorted((rootdata / "recipe").glob("*.json"))
    f0_recipes = [
        (p, read_json(p)) for p in recipes
        if "marked_stone" in p.name or "cut_marked_stone" in p.name
    ]
    require(len(f0_recipes) == 1, f"Exactly one F0 stonecutter recipe: {f0_recipes}")
    recipe_path, recipe = f0_recipes[0]
    require(recipe_path.name == "cut_marked_stone_from_marked_stone_stonecutting.json",
            "Unexpected recipe name")
    require(recipe == {
        "type": "minecraft:stonecutting",
        "ingredient": f"{MOD}:marked_stone",
        "result": {"id": f"{MOD}:cut_marked_stone", "count": 1}
    }, "Stonecutter must be 1:1, source marked_stone, no Dust/no shortcuts")
    # There is no reverse or alternative stone->marked_stone recipe at F0.
    for p in recipes:
        recipe_data = read_json(p)
        if p != recipe_path:
            require(recipe_data.get("result", {}).get("id") not in {
                f"{MOD}:marked_stone", f"{MOD}:cut_marked_stone"},
                f"Illegal extra F0 source or duplication recipe {p.name}")

    feature_dir = rootdata / "worldgen/feature"
    placed_dir = rootdata / "worldgen/placed_feature"
    primary = read_json(feature_dir / "marked_stone.json")
    require(primary == {
        "type": "minecraft:ore",
        "targets": [{"target": {"predicate_type": "minecraft:tag_match",
                                "tag": "minecraft:stone_ore_replaceables"},
                     "state": f"{MOD}:marked_stone"}],
        "size": 12,
        "discard_chance_on_air_exposure": 0
    }, "Marked Stone ore feature must replace only vanilla stone ores; do not touch deepstone")
    primary_placed = read_json(placed_dir / "marked_stone.json")
    require(primary_placed == {
        "feature": f"{MOD}:marked_stone",
        "placement": [
            {"type": "minecraft:count", "count": 1},
            {"type": "minecraft:in_square"},
            {"type": "minecraft:height_range", "height": {
                "type": "minecraft:trapezoid",
                "min_inclusive": {"absolute": -8},
                "max_inclusive": {"absolute": 112}}},
            {"type": "minecraft:biome"}]
    }, "F0 veins must be bounded and use biome filter")
    outcrop = read_json(feature_dir / "marked_stone_outcrop.json")
    require(outcrop == {
        "type": "minecraft:netherrack_replace_blobs",
        "target": "minecraft:stone",
        "state": f"{MOD}:marked_stone",
        "radius": {"type": "minecraft:uniform", "min_inclusive": 1, "max_inclusive": 2}
    }, "F0 outcrops must replace ONLY vanilla stone in small blobs")
    outcrop_placed = read_json(placed_dir / "marked_stone_outcrop.json")
    require(outcrop_placed == {
        "feature": f"{MOD}:marked_stone_outcrop",
        "placement": [
            {"type": "minecraft:rarity_filter", "chance": 9},
            {"type": "minecraft:in_square"},
            {"type": "minecraft:heightmap", "heightmap": "WORLD_SURFACE_WG"},
            {"type": "minecraft:biome"}]
    }, "F0 surface outcrops must be rare, heightmap-based and biome-filtered")
    mod_dir = rootdata / "neoforge/biome_modifier"
    target_name = "marked_stone_overworld.json"
    modifier = read_json(mod_dir / target_name)
    require(modifier == {
        "type": "neoforge:add_features",
        "biomes": "#c:is_overworld",
        "features": [f"{MOD}:marked_stone", f"{MOD}:marked_stone_outcrop"],
        "step": "underground_ores"
    }, "Feature injection must be an additive Overworld-only owner")
    for modifier_path in mod_dir.glob("*.json"):
        if modifier_path.name == target_name:
            continue
        record = read_json(modifier_path)
        require(not set(record.get("features", [])) & {
            f"{MOD}:marked_stone", f"{MOD}:marked_stone_outcrop"
        }, "Duplicate F0 feature injection in " + str(modifier_path))
    require(not list((rootdata / "worldgen/biome").glob("*marked*")),
            "F0 does not create a dimension or standalone biome")
    return {
        "schema_version": "dreamsdimensions.qa.v1",
        "test_id": "task_7_4_worldgen_and_lapidation",
        "category": "CONTENT", "status": "PASS", "evidence_type": "STATIC_CHECK",
        "counts": {**allcontent["counts"], "marked_features": 2,
                   "marked_stonecutter_recipes": 1},
        "texture": cut_info,
        "limitations": [
            "Static JSON/asset validation does NOT prove any natural generation",
            "Natural voxels, exposed faces, and coordinates require real server survey",
            "Survival harvesting, stonecutter GUI and actual player discovery remain NOT_RUN",
        ],
    }


def main():
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=ROOT)
    parser.add_argument("--report", type=Path)
    args = parser.parse_args()
    try:
        result = inspect(args.root.resolve())
    except (ContentContractError, OSError, ValueError, KeyError) as error:
        result = {"schema_version": "dreamsdimensions.qa.v1",
                  "test_id": "task_7_4_worldgen_and_lapidation",
                  "category": "CONTENT", "status": "FAIL",
                  "evidence_type": "STATIC_CHECK", "error": str(error)}
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(json.dumps(result, indent=2, sort_keys=True) + "\n",
                               encoding="utf-8")
    if result["status"] == "PASS":
        print("TASK 7.4 STATIC PASS: new cut block, 1:1 stonecutter, 2 F0 worldgen paths",
              json.dumps(result["counts"], sort_keys=True), flush=True)
        return 0
    print("TASK 7.4 STATIC FAIL:", result["error"], file=sys.stderr, flush=True)
    return 1


if __name__ == "__main__":
    raise SystemExit(main())
