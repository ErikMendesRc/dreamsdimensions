#!/usr/bin/env python3
"""Validate 26.3 vegetation datapack wiring before booting the game.

This is a structural test, NOT a claim of visual or in-game correctness.
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
BASE = ROOT / "src/main/resources/data/dreamsdimensions/worldgen"
FEATURES = {
    "sentinel_tree": ("sentinel", 7),
    "umbrella_tree": ("umbrella", 4),
    "arched_tree": ("arch", 2),
    "glow_moss_patch": ("moss", 3),
    "dream_flower_garden": ("flower", 3),
}


def read(path):
    return json.loads(path.read_text(encoding="utf-8"))


biome = read(BASE / "biome/campo_onirico_azul.json")
assert len(biome["features"]) == 10, "26.3 vegetal_decoration is stage 9"
assert all(not stage for stage in biome["features"][:9])
assert len(biome["features"][9]) == len(FEATURES) + 4, "Task 1.3 adds four formations"
assert len(set(biome["features"][9])) == len(FEATURES) + 4, "duplicate biome features"
assert {"dreamsdimensions:" + name for name in FEATURES}.issubset(set(biome["features"][9]))
for name, (form, count) in FEATURES.items():
    feature = read(BASE / "feature" / (name + ".json"))
    placed = read(BASE / "placed_feature" / (name + ".json"))
    assert feature["type"] == "dreamsdimensions:dream_vegetation"
    assert feature["form"] == form
    assert feature["min_height"] <= feature["max_height"]
    assert 0 < feature["density"] <= 1
    for key, block in {
        "log": "dream_log_block",
        "leaves": "dream_leaves_block",
        "flower": "dream_flower_block",
        "moss": "dream_glow_moss",
        "soil": "blue_dream_grass",
    }.items():
        assert feature[key] == "dreamsdimensions:" + block, (name, key)
    assert placed["feature"] == "dreamsdimensions:" + name
    assert placed["placement"] == [
        {"type": "minecraft:count", "count": count},
        {"type": "minecraft:in_square"},
        {"type": "minecraft:heightmap", "heightmap": "MOTION_BLOCKING_NO_LEAVES"},
        {"type": "minecraft:biome"},
    ]
tags = ROOT / "src/main/resources/data/minecraft/tags/block"
for name in ("blocks_motion", "blocks_motion_no_leaves",
             "blocks_motion_in_heightmap", "blocks_motion_in_heightmap_no_leaves"):
    values = read(tags / (name + ".json"))["values"]
    assert "dreamsdimensions:dream_glow_moss" in values
    assert ("dreamsdimensions:dream_leaves_block" in values) == (
        name in ("blocks_motion", "blocks_motion_in_heightmap")
    )
assert "dreamsdimensions:dream_glow_moss" in read(
    ROOT / "src/main/resources/data/dreamsdimensions/tags/block/blue_natural_ground.json"
)["values"]
leaf_state = read(ROOT / "src/main/resources/assets/dreamsdimensions/blockstates/dream_leaves_block.json")
assert leaf_state["multipart"][0]["apply"]["model"] == "dreamsdimensions:block/dream_leaves_block"
print("TASK 1.2: 5 features, placement, stages, tags and leaf model OK (static only)")
