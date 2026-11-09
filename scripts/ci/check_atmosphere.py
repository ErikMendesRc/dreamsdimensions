#!/usr/bin/env python3
"""TASK 2.1 + 2.2 static atmosphere isolation and 26.3 attribute guardrails.

Static assertions do not replace hearing/seeing ambience in a real client.
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
RESOURCE = ROOT / "src/main/resources/data/dreamsdimensions"
CLIENT = ROOT / "src/main/java/com/dreamsdimensions/mod/client/atmosphere"
COMMON = ROOT / "src/main/java/com/dreamsdimensions/mod"

def read(path):
    return json.loads(path.read_text(encoding="utf-8"))

dimension = read(RESOURCE / "dimension_type/campo_onirico_azul_type.json")
dreamscape = read(RESOURCE / "dimension_type/dreamscape_type.json")
biome = read(RESOURCE / "worldgen/biome/campo_onirico_azul.json")
attributes = dimension["attributes"]
assert attributes["minecraft:visual/fog_start_distance"] == 48
assert attributes["minecraft:visual/fog_end_distance"] == 170
assert attributes["minecraft:visual/sky_fog_end_distance"] == 210
assert 0 < attributes["minecraft:visual/fog_start_distance"] < attributes["minecraft:visual/fog_end_distance"]
assert attributes["minecraft:visual/fog_end_distance"] <= attributes["minecraft:visual/sky_fog_end_distance"]
assert "minecraft:visual/fog_start_distance" not in dreamscape.get("attributes", {})
assert "minecraft:visual/fog_end_distance" not in dreamscape.get("attributes", {})
assert dimension["has_fixed_time"] is True
assert dimension["timelines"] == "#minecraft:in_overworld"
assert dimension["ambient_light"] == 1
assert biome["attributes"]["minecraft:visual/sky_color"] == "#9bdcfd"
assert biome["attributes"]["minecraft:visual/fog_color"] == "#b6e2ff"
assert biome["attributes"]["minecraft:visual/water_fog_color"] == "#050533"
assert len(biome["features"][9]) == 9
assert not any((RESOURCE / "worldgen/biome/dreamscape_biome.json").read_text(encoding="utf-8").find(x) >= 0
               for x in ("minecraft:visual/fog_start_distance", "minecraft:visual/fog_end_distance"))
assert len(list(CLIENT.glob("*.java"))) == 5
profile = (CLIENT / "AtmosphereProfile.java").read_text(encoding="utf-8")
controller = (CLIENT / "AtmosphereClientController.java").read_text(encoding="utf-8")
particles = (CLIENT / "AtmosphereParticles.java").read_text(encoding="utf-8")
audio = (CLIENT / "AtmosphereAudio.java").read_text(encoding="utf-8")
sample = (CLIENT / "AtmosphereSampling.java").read_text(encoding="utf-8")
common = (COMMON / "DreamsDimensions.java").read_text(encoding="utf-8")
assert "net.minecraft.client" not in common
assert "client.atmosphere" not in common
assert "@EventBusSubscriber(modid = DreamsDimensions.MODID, value = Dist.CLIENT)" in controller
assert "ClientTickEvent.Post" in controller
assert "lastLevel != level || lastProfile != profile" in controller and "reset()" in controller
assert "candidate.dimension().equals(level.dimension())" in controller
assert "DreamDimensionCatalog.find(level.dimension())" in controller
assert "flatMap(DreamDimensionDefinition::atmosphereId)" in controller
assert "ParticleStatus.MINIMAL" in particles and "ParticleStatus.DECREASED" in particles
assert "level.addParticle(" in particles and "addAlwaysVisibleParticle" not in particles
assert "level.playLocalSound(" in audio and "SoundSource.AMBIENT" in audio
assert "nextCueTick = tick + profile.soundMinimumTicks()" in audio
assert "level.hasChunkAt" in sample and "level.getHeight" in sample
assert "level.getChunk(" not in sample and "sendToServer" not in (particles + audio + controller)
for sound in ("AMETHYST_BLOCK_CHIME", "CHERRY_LEAVES_STEP", "BREEZE_IDLE_AIR"):
    assert sound in audio
for particle in ("END_ROD", "SPORE_BLOSSOM_AIR"):
    assert particle in particles
assert "12, 2, 8, 160, 160, 8" in profile
assert "DustParticleOptions" in particles and "BLUE_DUST" in particles
assert "playerPos.offset(" in particles, "Creative-flight ambient particle fallback is required"
assert "isClearAir(level, sample)" in particles
assert "random.nextInt(3) == 0" not in audio
assert "dreamscape" not in profile
assert "dreamscape" not in controller
print("TASKS 2.1/2.2: 26.3 attributes, isolated client profiles, caps, sounds and particles OK (static).")
