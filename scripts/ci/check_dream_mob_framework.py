#!/usr/bin/env python3
"""Task 4.4 static architecture and invariant guard; NOT a gameplay test."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
SRC = ROOT / "src/main/java/com/dreamsdimensions/mod"
def read(file):
    return (SRC / file).read_text(encoding="utf-8")

entity = read("entity/FragmentoErranteEntity.java")
population = read("entity/FragmentoIslandPopulation.java")
coordinator = read("entity/spawn/DreamMobPopulationService.java")
profile = read("entity/spawn/MobPopulationProfile.java")
habitat = read("entity/FragmentoIslandHabitat.java")
island = read("entity/habitat/FloatingIslandHabitat.java")
flight = read("entity/movement/DreamFlyingMoveControl.java")
configuration = read("entity/movement/FlightProfile.java")
main = read("DreamsDimensions.java")

# Only one NeoForge event subscription coordinates every registered species.
assert main.count("DreamMobPopulationService::onPlayerTick") == 1
assert "FragmentoIslandPopulation::onPlayerTick" not in main
assert "DreamMobPopulationService.register(FragmentoIslandPopulation.profile())" in main
assert "PlayerTickEvent.Post" in coordinator and "GameRules.SPAWN_MOBS" in coordinator
assert "ServerPlayer player" in coordinator and "ServerLevel level" in coordinator
assert "WeakHashMap" in coordinator and "MAX_REGION_KEYS = 256" in coordinator
assert "getEntitiesOfClass(" in coordinator
assert "profile.entityType().get().spawn(" in coordinator
assert "EntitySpawnReason.NATURAL" in coordinator
assert "level.hasChunkAt(chunkProbe)" in coordinator
assert "profile.habitat().isValid(level, surface)" in coordinator
assert "new FragmentoErranteEntity(" not in coordinator
assert "forceload" not in coordinator.lower()

# Habitat remains physical, dimension-specific and safe for unloaded columns.
assert "interface DreamMobHabitat" in read("entity/habitat/DreamMobHabitat.java")
assert "FloatingIslandHabitat implements DreamMobHabitat" in island
assert "level.getLevel().dimension().equals(dimension)" in island
assert "level.getBiome(feet).is(biome)" in island
assert "level.isEmptyBlock(airA) && level.isEmptyBlock(airB)" in island
assert "level.hasChunkAt(airA)" in island and "level.hasChunkAt(airB)" in island
assert "GAP_DEPTHS = {12, 20, 28, 36, 44, 52, 60, 68, 76, 84, 92, 100, 108}" in habitat
assert "MIN_SPAWN_Y = 158" in habitat and "MAX_SPAWN_Y = 231" in habitat

# Flight stays physical and GeckoLib-independent, while the six clips remain species-owned.
assert "new DreamFlyingMoveControl(this, FLIGHT_PROFILE)" in entity
assert "FlyingPathNavigation(this, level)" in entity
assert "FlyingMoveControl" in flight and "FlightProfile profile" in flight
assert "flyer.setDeltaMovement(result)" in flight
assert "flyer.level().noCollision(" in flight
assert "flyer.level().hasChunkAt(" in flight
assert "void applyIdleHover()" in flight
assert "void resetHover()" in flight
assert "setPos(" not in flight and "teleportTo(" not in flight
assert "GeoEntity" not in flight and "AnimationController" not in flight
for clip in ("idle", "move", "float", "curious", "startled", "disappear"):
    assert "animation.fragmento_errante." + clip in entity
assert entity.count("new AnimationController") == 1
assert "new FleeGoal()" in entity and "new ResonanceGoal()" in entity
assert "new CuriosityGoal()" in entity and "new WanderGoal()" in entity
for key in ("FragmentoIdlePause", "FragmentoCuriosityCooldown", "FragmentoStartleCooldown",
            "FragmentoResonanceCooldown", "FragmentoAggressionWindow",
            "FragmentoAggressionHits", "FragmentoVanishAfterFlee",
            "FragmentoIslandBound", "FragmentoIslandX", "FragmentoIslandY",
            "FragmentoIslandZ", "FragmentoDisappearTicks", "FragmentoRemoveAfterDisappear"):
    assert entity.count('"' + key + '"') >= 1, key
for literal in ("0.17, 0.08, 0.19, 0.028, 0.78, 0.70, 0.50, 0.10",
                "0.018, 0.015, 0.18, 0.87, 0.12",
                "HOVER_Y_AMPLITUDE = 0.14", "HOVER_XZ_AMPLITUDE = 0.035",
                "HOVER_PERIOD_TICKS = 64.0"):
    assert literal in entity, literal
for literal in ("PERIOD_TICKS = 200", "CANDIDATE_LIMIT = 48",
                "LOCAL_CAP = 6", "MAX_NEW_MOBS_PER_PASS = 2",
                "INNER_RADIUS = 16", "OUTER_RADIUS = 64",
                "MIN_PLAYER_Y = 145", "MAX_PLAYER_Y = 244"):
    assert literal in population, literal
assert "record MobPopulationProfile" in profile
print("TASK 4.4 STATIC PASS: shared coordinator, island validator, flight control, NBT and clip names")
