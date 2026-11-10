#!/usr/bin/env python3
"""Task 5.3.1: regression guard for the deliberate 26.3 hybrid spawn contract.

Checks parsed datapack policies and source integration. It is NOT a live
population/frequency benchmark. Existing RCON smoke validates entity loading.
"""
from collections import Counter
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[2]
DATA = ROOT / "src/main/resources/data/dreamsdimensions"
SRC = ROOT / "src/main/java/com/dreamsdimensions/mod"
ASSET = ROOT / "src/main/resources/assets/dreamsdimensions"


def load(path):
    return json.loads(path.read_text(encoding="utf-8"))


def validate_biome():
    dreamscape = load(DATA / "worldgen/biome/dreamscape_biome.json")
    natural = dreamscape["attributes"]["minecraft:gameplay/natural_mob_spawns"]
    assert natural["modifier"] == "overlay"
    categories = natural["argument"]["spawns_by_category"]
    creature = categories["creature"]
    counts = Counter(row["type"] for row in creature)
    assert counts == {
        "dreamsdimensions:fragmento_errante": 1,
        "dreamsdimensions:vigia_adormecido": 1,
    }, f"Each Dreamscape species must have exactly one native biome entry: {counts}"
    by_id = {entry["type"]: entry for entry in creature}
    assert by_id["dreamsdimensions:fragmento_errante"]["weight"] == 100
    assert by_id["dreamsdimensions:vigia_adormecido"]["weight"] == 18
    assert by_id["dreamsdimensions:fragmento_errante"]["count"] == {
        "type": "minecraft:uniform", "min_inclusive": 2, "max_inclusive": 3
    }
    assert by_id["dreamsdimensions:vigia_adormecido"]["count"] == {
        "type": "minecraft:uniform", "min_inclusive": 1, "max_inclusive": 1
    }
    assert dreamscape["attributes"]["minecraft:gameplay/creature_world_gen_spawn_probability"] == .34
    # Only Dreamscape owns these species: avoid extra registrations on Blue Field.
    blue = load(DATA / "worldgen/biome/campo_onirico_azul.json")
    blue_spawns = blue["attributes"]["minecraft:gameplay/natural_mob_spawns"]["argument"]["spawns_by_category"]
    for group in blue_spawns.values():
        assert all(entry["type"] not in counts for entry in group)


def validate_coordinator():
    main = (SRC / "DreamsDimensions.java").read_text(encoding="utf-8")
    core = (SRC / "entity/spawn/DreamMobPopulationService.java").read_text(encoding="utf-8")
    entities = (SRC / "registry/ModEntities.java").read_text(encoding="utf-8")
    for species in ("FragmentoIslandPopulation", "VigiaIslandPopulation"):
        assert main.count(f"DreamMobPopulationService.register({species}.profile())") == 1
        assert f"{species}::onPlayerTick" not in main
    assert main.count("DreamMobPopulationService::onPlayerTick") == 1
    assert main.count("DreamMobPopulationService::onServerStopped") == 1
    assert "EntitySpawnReason.NATURAL" in core
    assert "getEntitiesOfClass(" in core and "profile.localCap()" in core
    assert "if (existing.size() >= profile.localCap()) return;" in core
    assert "if (!reserveRegion(level, player, profile)) continue;" in core
    assert "MAX_REGION_KEYS = 256" in core and "RECENT.clear();" in core
    assert "level.hasChunkAt(chunkProbe)" in core
    assert "profile.habitat().isValid(level, surface)" in core
    assert 'Boolean.getBoolean("dreamsdimensions.auditMobSpawn")' in core
    assert "DREAM_MOB_SPAWN origin=coordinator" in core
    assert "RegisterSpawnPlacementsEvent.Operation.REPLACE" in entities
    assert "FragmentoIslandHabitat::maySpawn" in entities
    assert "VigiaIslandHabitat::maySpawn" in entities


def validate_simplifications():
    items = (SRC / "registry/ModItems.java").read_text(encoding="utf-8")
    assert not (SRC / "item/DreamDustItem.java").exists()
    assert "DeferredItem<Item> DREAM_DUST" in items
    assert "DreamDustItem" not in items
    assert '"dream_dust",\n            Item::new,' in items
    # A no-op click used to toggle persisted ore blockstate; retaining states
    # avoids rewriting saved chunks and preserves existing block models.
    ore = (SRC / "block/DreamOreBlock.java").read_text(encoding="utf-8")
    assert "extends DropExperienceBlock" in ore
    assert "useWithoutItem" not in ore and "setBlockAndUpdate" not in ore
    assert "BooleanProperty CLICKED" in ore and "builder.add(CLICKED);" in ore
    for block in ("dream_ore", "deepslate_dream_ore"):
        states = load(ASSET / "blockstates" / f"{block}.json")["variants"]
        assert set(states.keys()) == {"clicked=false", "clicked=true"}
        assert states["clicked=false"] == states["clicked=true"], block


if __name__ == "__main__":
    validate_biome()
    validate_coordinator()
    validate_simplifications()
    print("TASK 5.3.1 STATIC PASS: intentional native+coordinator spawn preserved; "
          "per-species native entries unique, coordinator caps intact, "
          "legacy ore states preserved and empty DustItem removed.")
