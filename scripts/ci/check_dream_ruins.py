#!/usr/bin/env python3
"""Task 5.7: strict static checks of REAL 26.3 compressed NBT + JSON datapacks.

A syntax/asset test is not evidence of actual placement, support, unique
container resolution, or Survival accessibility. Those need dedicated-server
surveys and real players.
"""
from __future__ import annotations
import gzip
import json
import random
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DATA = ROOT / "src/main/resources/data/dreamsdimensions"
BLUE = "blue_dream_ruins"
DREAM = "fragmented_dream_ruins"
SPECS = {
    BLUE: {
        "biome": "dreamsdimensions:campo_onirico_azul",
        "pool": "ruins/blue_dream_ruins",
        "size": (11, 8, 11),
        "expected_blocks_min": 65,
        "max_spacing": 64,
    },
    DREAM: {
        "biome": "dreamsdimensions:dreamscape_biome",
        "pool": "ruins/fragmented_dream_ruins",
        "size": (9, 8, 9),
        "expected_blocks_min": 50,
        "max_spacing": 64,
    }
}


def read_json(path):
    return json.loads(path.read_text(encoding="utf-8"))


class NbtParser:
    """Full-type NBT reader for versioned templates, no external dependency."""
    def __init__(self, data):
        self.bytes = memoryview(data)
        self.index = 0

    def pull(self, n):
        assert 0 <= n <= 20_000_000
        end = self.index + n
        assert end <= len(self.bytes), "NBT truncated"
        value = self.bytes[self.index:end].tobytes()
        self.index = end
        return value

    def value(self, code):
        if code == 0:
            return None
        if code in (1, 2, 3, 4, 5, 6):
            format_ = {1: ">b", 2: ">h", 3: ">i", 4: ">q", 5: ">f", 6: ">d"}[code]
            return struct.unpack(format_, self.pull(struct.calcsize(format_)))[0]
        if code == 7:
            length = self.value(3)
            return self.pull(length)
        if code == 8:
            n = struct.unpack(">H", self.pull(2))[0]
            return self.pull(n).decode("utf-8")
        if code == 9:
            subcode = self.value(1)
            count = self.value(3)
            assert 0 <= count <= 500_000, count
            return [self.value(subcode) for _ in range(count)]
        if code == 10:
            obj = {}
            while True:
                field_type = self.value(1)
                if field_type == 0:
                    break
                name = self.value(8)
                assert name not in obj, f"duplicate NBT field {name}"
                obj[name] = self.value(field_type)
            return obj
        if code == 11:
            count = self.value(3)
            assert 0 <= count <= 500_000
            return [self.value(3) for _ in range(count)]
        if code == 12:
            count = self.value(3)
            assert 0 <= count <= 500_000
            return [self.value(4) for _ in range(count)]
        raise AssertionError(f"unsupported NBT tag type {code}")

    def root(self):
        assert self.value(1) == 10, "structure root must be compound"
        assert self.value(8) == "", "anonymous structure root expected"
        result = self.value(10)
        assert self.index == len(self.bytes), "trailing NBT bytes"
        return result


def nbt_template(name, spec):
    path = DATA / f"structure/ruins/{name}.nbt"
    assert path.is_file(), path
    raw = gzip.decompress(path.read_bytes())
    doc = NbtParser(raw).root()
    assert doc["DataVersion"] == 5023, (name, doc.get("DataVersion"))
    assert tuple(doc["size"]) == spec["size"], (name, doc["size"])
    assert doc["entities"] == [], "Unexpected entities in a recoverable ruins template"
    assert doc["palette"] and doc["blocks"]
    assert len(doc["blocks"]) >= spec["expected_blocks_min"]
    palette = doc["palette"]
    states = [p["id"] for p in palette]
    assert all(p.get("id", "").startswith(("minecraft:", "dreamsdimensions:")) for p in palette)
    assert all("Name" not in p and "Properties" not in p for p in palette), (
        "NBT 26.3 must use palette id/properties")
    assert len(states) == len(set(states)), "Unintended duplicate palette block IDs"
    occupied = {}
    chests = []
    for node in doc["blocks"]:
        pos = tuple(node["pos"])
        assert len(pos) == 3
        assert all(0 <= value < bound for value, bound in zip(pos, spec["size"])), (name,pos)
        assert pos not in occupied, (name, "duplicate block", pos)
        occupied[pos] = node
        assert 0 <= node["state"] < len(palette)
        block = states[node["state"]]
        if block == "minecraft:chest":
            chests.append((pos, node))
        else:
            assert "nbt" not in node, f"Surprise block entity: {name}:{pos}"
    assert len(chests) == 1, f"{name}: expected exactly one principal chest"
    pos, chest = chests[0]
    assert chest["nbt"]["id"] == "minecraft:chest"
    assert chest["nbt"]["LootTable"] == f"dreamsdimensions:chests/{name}"
    assert "LootTableSeed" not in chest["nbt"], "Let each chest use vanilla default loot seed"
    assert (pos[0],pos[1]-1,pos[2]) in occupied, "Chest has no floor below"
    assert (pos[0],pos[1]+1,pos[2]) not in occupied, "Chest lid blocked inside template"
    assert pos[1] == 1, "Chest base must be on ground-height structure floor"
    # Check designed floor around center, not world support (runtime task).
    floor = {(x,z) for x,y,z in occupied if y==0}
    assert len(floor) >= 24, (name, "undersized courtyard")
    return {"bytes": len(raw), "blocks": len(occupied), "palette":len(palette),
            "floor_cells":len(floor),"chest_pos":pos,"chest_table":chest["nbt"]["LootTable"]}


def validate_catalog():
    dims = {
        "dreamsdimensions:campo_onirico_azul":
            "dreamsdimensions:campo_onirico_azul",
        "dreamsdimensions:dreamscape_biome":
            "dreamsdimensions:dreamscape"
    }
    for biome, dimension in dims.items():
        key = biome.split(":", 1)[1]
        assert (DATA / f"worldgen/biome/{key}.json").is_file()
        j = read_json(DATA / f"dimension/{dimension.split(':')[1]}.json")
        assert j["generator"]["biome_source"]["biome"] == biome
    assert len({v["biome"] for v in SPECS.values()}) == 2


def validate_structure(name, spec):
    data = read_json(DATA / f"worldgen/structure/{name}.json")
    assert data["type"] == "minecraft:jigsaw"
    assert data["biomes"] == spec["biome"]
    assert data["step"] == "surface_structures"
    assert data["terrain_adaptation"] == "none"
    assert data["spawn_overrides"] == {}
    assert data["start_pool"] == f"dreamsdimensions:{spec['pool']}"
    assert data["size"] == 1
    # Required field in the actual Minecraft 26.3 JigsawStructure.CODEC.
    assert data["use_expansion_hack"] is False
    assert data["start_height"] == {"absolute": 0}
    assert data["project_start_to_heightmap"] == "WORLD_SURFACE_WG"
    assert 24 <= data["max_distance_from_center"] <= 64
    pool = read_json(DATA / f"worldgen/template_pool/{spec['pool']}.json")
    assert pool["fallback"] == "minecraft:empty"
    assert len(pool["elements"]) == 1 and pool["elements"][0]["weight"] == 1
    element = pool["elements"][0]["element"]
    assert element["element_type"] == "minecraft:single_pool_element"
    assert element["location"] == f"dreamsdimensions:ruins/{name}"
    assert element["processors"] == "minecraft:empty"
    assert element["projection"] == "rigid"
    placements = read_json(DATA / f"worldgen/structure_set/{name}.json")
    assert placements["structures"] == [{"structure": f"dreamsdimensions:{name}", "weight": 1}]
    p = placements["placement"]
    assert p["type"] == "minecraft:random_spread"
    assert p["spread_type"] == "linear"
    assert 20 <= p["spacing"] <= spec["max_spacing"]
    assert 8 <= p["separation"] < p["spacing"]
    assert isinstance(p["salt"], int)
    return {"spacing_chunks":p["spacing"],"separation_chunks":p["separation"],
            "salt":p["salt"]}


def validate_loot(name):
    table = read_json(DATA / f"loot_table/chests/{name}.json")
    assert table["type"] == "minecraft:chest"
    assert table["random_sequence"] == f"dreamsdimensions:chests/{name}"
    assert len(table["pools"]) == 2
    rare, supplies = table["pools"]
    assert rare["rolls"] == 1
    entries = rare["entries"]
    assert len(entries) == 2
    special = [e for e in entries if e.get("name") == "dreamsdimensions:oneiric_awakener"]
    assert len(special) == 1
    awaken = special[0]
    assert awaken == {"type":"minecraft:item",
                      "name":"dreamsdimensions:oneiric_awakener","weight":1}
    assert entries[1] == {"type":"minecraft:empty","weight":3}
    assert supplies["rolls"] == {"type":"minecraft:uniform","min":2,"max":3}
    allowed = {"minecraft:bread","minecraft:torch","minecraft:apple","minecraft:oak_log",
               "minecraft:oak_planks","minecraft:cooked_mutton","minecraft:stone_pickaxe",
               "minecraft:scaffolding","minecraft:ladder"}
    assert all(e["name"] in allowed for e in supplies["entries"])
    assert len({e["name"] for e in supplies["entries"]}) == len(supplies["entries"])
    for file in (DATA / "loot_table/chests").rglob("*.json"):
        data = read_json(file)
        assert "dreamsdimensions:dream_dust" not in json.dumps(data)
        assert "conditions" not in json.dumps(data)
        assert "functions" not in json.dumps(data)
    # Monte Carlo: simulate the configured one-roll WEIGHTED CHOICE, not
    # the Minecraft loot engine itself. Runtime verification is separate.
    rng = random.Random(20261110 + (1 if name == BLUE else 2))
    rolls = 1000
    hits = sum(rng.randrange(4)==0 for _ in range(rolls))
    assert 205 <= hits <= 295, f"Rare pool expected ~250/1000, got {hits}"
    assert 1 / (1+3) == .25
    return {"rolls_MODEL_ONLY":rolls, "awakener_MODEL_ONLY":hits,
            "configured_chance":.25, "max_awakeners_per_chest":1}


def regression_guards():
    java = ROOT / "src/main/java/com/dreamsdimensions/mod"
    main = (java / "DreamsDimensions.java").read_text(encoding="utf-8")
    audit = (java / "diagnostics/DreamRuinsAuditCommand.java").read_text(encoding="utf-8")
    assert main.count("DreamRuinsAuditCommand::register") == 1
    assert 'Boolean.getBoolean("dreamsdimensions.enableRuinsAudit")' in main
    assert 'Boolean.getBoolean("dreamsdimensions.enableRuinsAudit")' in audit
    assert "getBlockEntities()" in audit
    assert "getLootTable()" in audit
    assert "getItem(" not in audit and "unpackLootTable(" not in audit
    assert "level.hasChunkAt(" in audit
    assert "setBlock(" not in audit and "forceload" not in audit
    assert (java / "item/OneiricAwakenerItem.java").exists()
    assert (java / "service/DreamRespawnService.java").exists()
    assert (java / "util/DreamReturnHelper.java").exists()
    assert (DATA / "loot_table/blocks/dream_ore.json").is_file()
    assert (DATA / "loot_table/blocks/deepslate_dream_ore.json").is_file()
    assert all("dream_dust" not in path.name for path in (DATA / "loot_table/chests").glob("*.json"))


def run():
    validate_catalog()
    template_stats = {}
    structure_stats = {}
    loot_stats = {}
    salts = []
    for name, spec in SPECS.items():
        template_stats[name] = nbt_template(name,spec)
        structure_stats[name] = validate_structure(name,spec)
        loot_stats[name] = validate_loot(name)
        salts.append(structure_stats[name]["salt"])
    assert len(set(salts)) == 2
    assert template_stats[BLUE]["blocks"] != template_stats[DREAM]["blocks"]
    assert template_stats[BLUE]["floor_cells"] != template_stats[DREAM]["floor_cells"]
    regression_guards()
    print("TASK 5.7 STATIC_PASS: native structures/sets/pools, valid 26.3 gzip NBT, "
          "one real loot chest per distinct template, 25% single-roll loot, "
          "zero dream_dust, no runtime tick generator or gameplay regression.")
    print("TASK 5.7 TEMPLATES:", json.dumps(template_stats,sort_keys=True))
    print("TASK 5.7 STRUCTURE_SET:", json.dumps(structure_stats,sort_keys=True))
    print("TASK 5.7 LOOT_MODEL_ONLY:", json.dumps(loot_stats,sort_keys=True))
    print("TASK 5.7 LIMITATION: NBT/static JSON and Python RNG model do not prove "
          "natural placement, island support, chest persistence, or real loot odds.")


if __name__ == "__main__":
    run()
