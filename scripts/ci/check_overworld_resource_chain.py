#!/usr/bin/env python3
"""Task 5.6 — structural and economic validation of actual 26.3 JSON datapacks.

This deliberately DOES NOT claim ore was naturally generated, mined, exposed,
or discovered by a player. Those require RCON voxel audit / manual Survival.
"""
from collections import Counter
from pathlib import Path
import json
import math

ROOT = Path(__file__).resolve().parents[2]
DATA = ROOT / "src/main/resources/data/dreamsdimensions"
MINECRAFT = ROOT / "src/main/resources/data/minecraft"
MODIFIER_DIR = DATA / "neoforge/biome_modifier"
ORE_IDS = ("dreamsdimensions:dream_ore", "dreamsdimensions:deepslate_dream_ore")
ORE_NAMES = ("dream_ore", "deepslate_dream_ore")
RECIPE_NAMES = ("dream_necklace", "dream_infused_stone", "oneiric_core_block")
OUR_ITEMS = {f"dreamsdimensions:{name}" for name in (
    "dream_dust", "dream_necklace", "dream_infused_stone", "oneiric_core_block", "oneiric_awakener",
    "return_charge", "toasted_vigil_herb", "vigil_herb_leaf", "vigil_herb_seeds")}
VANILLA_INPUTS = {"minecraft:copper_ingot", "minecraft:amethyst_shard",
                  "minecraft:stone", "minecraft:clock", "minecraft:feather",
                  "minecraft:glass_bottle"}
DUST = "dreamsdimensions:dream_dust"


def load(path):
    return json.loads(path.read_text(encoding="utf-8"))


def validate_worldgen():
    files = sorted(MODIFIER_DIR.glob("*.json"))
    assert files, "Missing biome modifiers in official NeoForge directory"
    expected = MODIFIER_DIR / "dream_ore_overworld.json"
    assert expected.is_file(), "Overworld biome modifier not loaded from NeoForge directory"
    assert not (DATA / "worldgen/biome_modifier/dream_ore_overworld.json").exists()
    owner = load(expected)
    assert owner == {
        "type": "neoforge:add_features",
        "biomes": "#c:is_overworld",
        "features": list(ORE_IDS),
        "step": "underground_ores",
    }, f"Unexpected owner or wrong world/dimension target: {owner}"
    for filename in files:
        modifier = load(filename)
        if filename != expected:
            assert not (set(modifier.get("features", [])) & set(ORE_IDS)), (
                f"Duplicate Overworld ore injection in {filename}")
    details = {}
    for name, block in zip(ORE_NAMES, ORE_IDS):
        configured = load(DATA / f"worldgen/feature/{name}.json")
        placed = load(DATA / f"worldgen/placed_feature/{name}.json")
        assert configured["type"] == "minecraft:ore"
        assert len(configured["targets"]) == 1
        target = configured["targets"][0]
        expected_tag = "minecraft:deepslate_ore_replaceables" if name.startswith("deepslate_") else "minecraft:stone_ore_replaceables"
        assert target["target"] == {"predicate_type": "minecraft:tag_match", "tag": expected_tag}
        assert target["state"] == block
        assert isinstance(configured["size"], int) and 1 <= configured["size"] <= 32
        assert 0 <= configured["discard_chance_on_air_exposure"] <= 1

        assert placed["feature"] == block
        stages = placed["placement"]
        types = [stage["type"] for stage in stages]
        assert types == ["minecraft:count", "minecraft:in_square",
                         "minecraft:height_range", "minecraft:biome"], types
        assert isinstance(stages[0]["count"], int) and 1 <= stages[0]["count"] <= 64
        height = stages[2]["height"]
        assert height["type"] in ("minecraft:trapezoid", "minecraft:uniform")
        lower, upper = height["min_inclusive"], height["max_inclusive"]
        assert isinstance(lower.get("absolute"), int) and isinstance(upper.get("absolute"), int)
        assert -64 <= lower["absolute"] < upper["absolute"] <= 320, (name, height)
        details[name] = {
            "vein_size": configured["size"],
            "tries_per_chunk": stages[0]["count"],
            "min_y": lower["absolute"], "max_y": upper["absolute"],
            "distribution": height["type"],
        }
    # Dreamscape/Blue: no ore injection, no accidental ore replacement inside
    # their configured worldgen features or biome registrations.
    for path in (DATA / "worldgen").rglob("*.json"):
        if path.parent.name in {"feature", "placed_feature"} and path.stem in ORE_NAMES:
            continue
        content = json.dumps(load(path))
        for ore in ORE_IDS:
            assert ore not in content, f"Dream ore appears outside its Overworld owner: {path}"
    # Task 9.2: vegetation generation uses the Minecraft 26.3 codec.
    # random_patch no longer exists; random_offset was renamed to offset.
    herb_feature = load(DATA / "worldgen/feature/vigil_herb_patch.json")
    assert herb_feature["type"] == "minecraft:simple_block"
    assert "config" not in herb_feature
    provider = herb_feature["to_place"]
    assert provider["type"] == "minecraft:simple"
    assert provider["state"]["id"] == "dreamsdimensions:vigil_herb"
    assert provider["state"]["properties"] == {"age": "3", "open": "false"}, ("Wild mature herbs initialize closed and unfold at night via scheduled ticks")
    placed_herb = load(DATA / "worldgen/placed_feature/vigil_herb_patch.json")
    assert placed_herb["feature"] == "dreamsdimensions:vigil_herb_patch"
    modifiers = placed_herb["placement"]
    kinds = [row["type"] for row in modifiers]
    assert kinds == ["minecraft:rarity_filter", "minecraft:in_square",
                     "minecraft:heightmap", "minecraft:biome", "minecraft:count",
                     "minecraft:offset", "minecraft:block_predicate_filter"], kinds
    assert 0 < modifiers[4]["count"] <= 4096
    for axis in ("x", "y", "z"):
        assert axis in modifiers[5], "26.3 offset must define x/y/z"
    herb_owner = load(MODIFIER_DIR / "vigil_herb_overworld.json")
    assert herb_owner["features"] == ["dreamsdimensions:vigil_herb_patch"]
    assert herb_owner["step"] == "vegetal_decoration"
    assert all(biome.startswith("minecraft:") for biome in herb_owner["biomes"])
    return details


def validate_loot_and_tools():
    pickaxe = load(MINECRAFT / "tags/block/mineable/pickaxe.json")
    iron = load(MINECRAFT / "tags/block/needs_iron_tool.json")
    assert pickaxe["replace"] is False and iron["replace"] is False
    for tag in (pickaxe, iron):
        assert len(tag["values"]) == len(set(tag["values"])), "Duplicate tag values"
        assert set(ORE_IDS).issubset(set(tag["values"]))
    for other_tier in ("needs_stone_tool", "needs_diamond_tool"):
        file = MINECRAFT / f"tags/block/{other_tier}.json"
        if file.is_file():
            assert not (set(load(file)["values"]) & set(ORE_IDS))
    loot = {}
    for name, ore_id in zip(ORE_NAMES, ORE_IDS):
        data = load(DATA / f"loot_table/blocks/{name}.json")
        assert data["type"] == "minecraft:block"
        assert len(data["pools"]) == 1 and data["pools"][0]["rolls"] == 1
        entries = data["pools"][0]["entries"]
        assert len(entries) == 1 and entries[0]["type"] == "minecraft:alternatives"
        children = entries[0]["children"]
        assert len(children) == 2
        silk, no_silk = children
        assert silk["type"] == no_silk["type"] == "minecraft:item"
        assert silk["name"] == ore_id and silk["condition"] == "minecraft:tool/can_silk_touch"
        assert "modifier" not in silk and "conditions" not in silk
        assert no_silk["name"] == DUST and "condition" not in no_silk
        modifiers = no_silk["modifier"]
        assert [m["type"] for m in modifiers] == [
            "minecraft:apply_bonus", "minecraft:explosion_decay"]
        assert modifiers[0]["enchantment"] == "minecraft:fortune"
        assert modifiers[0]["formula"] == "minecraft:ore_drops"
        assert data["random_sequence"] == f"dreamsdimensions:blocks/{name}"
        assert "conditions" not in json.dumps(data) and "functions" not in json.dumps(data)
        loot[name] = {"normal_drop": DUST, "silk_drop": ore_id,
                      "fortune_formula": modifiers[0]["formula"]}
    for id in ("dream_infused_stone", "oneiric_core_block"):
        data = load(DATA / f"loot_table/blocks/{id}.json")
        assert data["type"] == "minecraft:block"
        assert len(data["pools"]) == 1
        pool = data["pools"][0]
        assert pool["rolls"] == 1 and len(pool["entries"]) == 1
        entry = pool["entries"][0]
        assert entry["type"] == "minecraft:item"
        assert entry["name"] == f"dreamsdimensions:{id}"
    blocks = (ROOT / "src/main/java/com/dreamsdimensions/mod/registry/ModBlocks.java").read_text(encoding="utf-8")
    assert blocks.count(".requiresCorrectToolForDrops()") >= 6
    for name in ORE_NAMES:
        assert f'"{name}"' in blocks
    return loot


def recipe_data(name):
    result = load(DATA / f"recipe/{name}.json")
    assert result["type"] == "minecraft:crafting_shaped", name
    pattern = result["pattern"]
    assert 1 <= len(pattern) <= 3 and 1 <= len(pattern[0]) <= 3
    assert all(len(row) == len(pattern[0]) for row in pattern)
    key = result["key"]
    used = set("".join(pattern)) - {" "}
    assert set(key) == used and all(len(symbol) == 1 for symbol in key), (name, key, pattern)
    counts = Counter(key[c] for row in pattern for c in row if c != " ")
    assert counts and all(x in OUR_ITEMS | VANILLA_INPUTS for x in counts), (name, counts)
    output = result["result"]
    assert output["id"] == f"dreamsdimensions:{name}" and output.get("count", 1) == 1
    return counts


def calculate_economy():
    recipes = {name: recipe_data(name) for name in RECIPE_NAMES}
    costs = {}

    def expand(identifier, ancestors=frozenset()):
        if identifier in ancestors:
            raise AssertionError(f"Circular crafting dependency at {identifier}")
        if not identifier.startswith("dreamsdimensions:") or identifier == DUST:
            return Counter({identifier: 1})
        name = identifier.split(":", 1)[1]
        assert name in recipes, f"Missing crafting recipe for required resource {identifier}"
        if name not in costs:
            result = Counter()
            for ingredient, amount in recipes[name].items():
                for mineral, count in expand(ingredient, ancestors | {identifier}).items():
                    result[mineral] += count * amount
            costs[name] = result
        return costs[name].copy()

    for name in RECIPE_NAMES:
        expand("dreamsdimensions:" + name)

    for name, dust_count in {
        "dream_necklace": 3, "dream_infused_stone": 2, "oneiric_core_block": 12
    }.items():
        assert costs[name][DUST] == dust_count, (name, costs[name][DUST], dust_count)

    assert costs["oneiric_core_block"]["minecraft:stone"] == 8
    assert costs["oneiric_core_block"]["minecraft:clock"] == 1
    assert costs["dream_necklace"]["minecraft:copper_ingot"] == 3
    assert costs["dream_necklace"]["minecraft:amethyst_shard"] == 1

    # Task 9.2: the Awakener must not be craftable from vanilla Crafting Table.
    assert not (DATA / "recipe/oneiric_awakener.json").exists(), (
        "The old Awakener recipe must not consume a Core after the migration")

    charge = load(DATA / "recipe/return_charge.json")
    assert charge["type"] == "minecraft:crafting_shapeless"
    assert charge["result"] == {"id": "dreamsdimensions:return_charge", "count": 1}
    assert Counter(charge["ingredients"]) == Counter({
        DUST: 3, "dreamsdimensions:toasted_vigil_herb": 2
    }), "Return Charge requires both geology and botany"

    for name in ("toasted_vigil_herb_smoking", "toasted_vigil_herb_campfire"):
        recipe = load(DATA / f"recipe/{name}.json")
        assert recipe["ingredient"] == "dreamsdimensions:vigil_herb_leaf"
        assert recipe["result"] == {"id": "dreamsdimensions:toasted_vigil_herb", "count": 1}

    menu_file = ROOT / "src/main/java/com/dreamsdimensions/mod/menu/OneiricCoreMenu.java"
    menu = menu_file.read_text(encoding="utf-8")
    for ingredient in ("RETURN_CHARGE", "Items.FEATHER", "Items.GLASS_BOTTLE"):
        assert ingredient in menu, f"Missing Core assembly ingredient {ingredient}"
    assert "ONEIRIC_AWAKENER" in menu, "Core menu output missing"
    assert "private final ResultContainer result" in menu
    assert "clearContainer(player, inputs)" in menu

    summary = {
        "one_necklace": dict(costs["dream_necklace"]),
        "first_core": dict(costs["oneiric_core_block"]),
        "return_charge": dict(Counter(charge["ingredients"])),
        "dust_first_necklace_core_awakener": (
            costs["dream_necklace"][DUST] + costs["oneiric_core_block"][DUST]
            + Counter(charge["ingredients"])[DUST]
        ),
        "dust_each_subsequent_awakener": Counter(charge["ingredients"])[DUST],
    }
    assert summary["dust_first_necklace_core_awakener"] == 18
    assert summary["dust_each_subsequent_awakener"] == 3
    return summary


def validate_recipe_book():
    expected = {
        "dream_dust_discovered": (DUST,
            {"dreamsdimensions:dream_necklace", "dreamsdimensions:dream_infused_stone"}),
        "dream_infused_stone_discovered": ("dreamsdimensions:dream_infused_stone",
            {"dreamsdimensions:oneiric_core_block"}),
        "return_charge_discovered": ("dreamsdimensions:toasted_vigil_herb",
            {"dreamsdimensions:return_charge"}),
    }
    directory = DATA / "advancement/recipes"
    assert {p.stem for p in directory.glob("*.json")} == set(expected)
    rewards_seen = set()
    for name, (input_item, unlocks) in expected.items():
        advancement = load(directory / (name + ".json"))
        assert "display" not in advancement, "Recipe unlock should not spam toasts"
        assert len(advancement["criteria"]) == 1
        trigger = list(advancement["criteria"].values())[0]
        assert trigger["trigger"] == "minecraft:inventory_changed"
        assert trigger["conditions"]["items"] == [{"items": input_item}]
        rewards = advancement["rewards"]
        assert set(rewards) == {"recipes"}
        assert set(rewards["recipes"]) == unlocks
        assert not rewards_seen.intersection(unlocks), "Duplicate recipe book reward"
        rewards_seen |= unlocks
    assert rewards_seen == {f"dreamsdimensions:{name}" for name in RECIPE_NAMES} | {"dreamsdimensions:return_charge"}


if __name__ == "__main__":
    details = validate_worldgen()
    loot = validate_loot_and_tools()
    economy = calculate_economy()
    validate_recipe_book()
    print("TASK 5.6 STATIC_DATA_PASS: ore worldgen JSON, single Overworld modifier, "
          "26.3 silk/fortune loot, iron tool tags, recovered expensive blocks, "
          "acyclic recipes, reusable Core assembly and updated recipe-book unlocks.")
    print("TASK 5.6 WORLDGEN_PARAMETERS (NOT OBSERVED):",
          json.dumps(details, sort_keys=True))
    print("TASK 5.6 ECONOMY_CRAFTING (DERIVED FROM RECIPES):",
          json.dumps(economy, sort_keys=True))
    print("TASK 5.6 LIMITS: no block counts or dust yield observed; "
          "use audit_overworld_ore_runtime.py and real Survival playtest.")
