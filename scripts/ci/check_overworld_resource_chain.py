#!/usr/bin/env python3
"""Validate the Overworld-first resource graph, not natural-ore occurrence.

Minecraft world generation must still be tested in new chunks in a running
26.3 dedicated server. This check deliberately avoids asserting a drop rate.
"""
from collections import Counter
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[2]
DATA = ROOT / "src/main/resources/data/dreamsdimensions"
MODIFIER = DATA / "neoforge/biome_modifier/dream_ore_overworld.json"
LEGACY = DATA / "worldgen/biome_modifier/dream_ore_overworld.json"


def read(path: Path):
    return json.loads(path.read_text(encoding="utf-8"))


assert MODIFIER.is_file(), "NeoForge biome modifier absent from official data path"
assert not LEGACY.exists(), "Legacy wrong-path modifier must be removed"
modifier = read(MODIFIER)
assert modifier == {
    "type": "neoforge:add_features",
    "biomes": "#c:is_overworld",
    "features": [
        "dreamsdimensions:dream_ore",
        "dreamsdimensions:deepslate_dream_ore",
    ],
    "step": "underground_ores",
}

for name in ("dream_ore", "deepslate_dream_ore"):
    configured = read(DATA / f"worldgen/feature/{name}.json")
    placed = read(DATA / f"worldgen/placed_feature/{name}.json")
    loot = read(DATA / f"loot_table/blocks/{name}.json")
    assert configured["type"] == "minecraft:ore"
    assert configured["targets"], f"Ore replacement targets missing for {name}"
    assert placed["feature"] == f"dreamsdimensions:{name}"
    assert any(p["type"] == "minecraft:height_range" for p in placed["placement"])
    assert any(p["type"] == "minecraft:biome" for p in placed["placement"])
    assert any(p["type"] == "minecraft:count" and p["count"] > 0
               for p in placed["placement"])
    assert "dreamsdimensions:dream_dust" in json.dumps(loot)
    assert "minecraft:fortune" in json.dumps(loot)
    assert "minecraft:tool/can_silk_touch" in json.dumps(loot)

pickaxe = read(ROOT / "src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json")
iron = read(ROOT / "src/main/resources/data/minecraft/tags/block/needs_iron_tool.json")
for name in ("dream_ore", "deepslate_dream_ore"):
    block = f"dreamsdimensions:{name}"
    assert block in pickaxe["values"], f"No pickaxe tag for {block}"
    assert block in iron["values"], f"No iron-tool requirement for {block}"


def ingredients(recipe_name: str) -> Counter:
    recipe = read(DATA / f"recipe/{recipe_name}.json")
    assert recipe["type"] == "minecraft:crafting_shaped"
    keys = recipe["key"]
    used = Counter(keys[ch] for row in recipe["pattern"]
                   for ch in row if ch != " ")
    assert used
    return used


infused = ingredients("dream_infused_stone")
core = ingredients("oneiric_core_block")
awakener = ingredients("oneiric_awakener")
assert core["dreamsdimensions:dream_infused_stone"] > 0
assert awakener["dreamsdimensions:oneiric_core_block"] > 0
assert infused["dreamsdimensions:dream_dust"] > 0
assert core["dreamsdimensions:dream_dust"] > 0
assert read(DATA / "recipe/oneiric_awakener.json")["result"]["id"] == (
    "dreamsdimensions:oneiric_awakener"
)

dust_cost = (infused["dreamsdimensions:dream_dust"]
             * core["dreamsdimensions:dream_infused_stone"]
             + core["dreamsdimensions:dream_dust"])
print(
    "Overworld-first static checks OK: modifier, two ore features, "
    "loot/tags, and crafting links. "
    f"Current oneiric_awakener recipe consumes {dust_cost} dream_dust "
    "(indirect + direct), not counting other ingredients. "
    "Natural spawning and Survival collection require runtime verification."
)
