#!/usr/bin/env python3
"""TASK 3.3 — Dreamscape atmosphere contracts, independent of client rendering.

Static checks + the main CI dedicated server do not prove visible particles,
fog quality, sound playback, or real client pause/reconnect behavior.
"""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
JAVA = ROOT / "src/main/java/com/dreamsdimensions/mod"
CLIENT = JAVA / "client/atmosphere"
DATA = ROOT / "src/main/resources/data/dreamsdimensions"


def source(name):
    return (CLIENT / name).read_text(encoding="utf-8")


def read(name):
    return json.loads((DATA / name).read_text(encoding="utf-8"))


def check():
    profile = source("AtmosphereProfile.java")
    controller = source("AtmosphereClientController.java")
    particles = source("AtmosphereParticles.java")
    audio = source("AtmosphereAudio.java")
    sampling = source("AtmosphereSampling.java")
    catalog = (JAVA / "dimension/DreamDimensionCatalog.java").read_text(encoding="utf-8")
    bootstrap = (JAVA / "DreamsDimensions.java").read_text(encoding="utf-8")
    dimension = read("dimension_type/dreamscape_type.json")
    blue = read("dimension_type/campo_onirico_azul_type.json")
    biome = read("worldgen/biome/dreamscape_biome.json")
    blue_biome = read("worldgen/biome/campo_onirico_azul.json")
    p = "minecraft:visual/"

    # 1-3: resolver dois perfis disjuntos, mantendo catálogo de dimensões.
    assert 'BLUE_ID = DreamDimensionCatalog.id("blue_field")' in profile
    assert 'DREAMSCAPE_ID = DreamDimensionCatalog.id("dreamscape_ambience")' in profile
    assert 'DreamDimensionCatalog.BLUE_FIELD,' in profile
    assert 'DreamDimensionCatalog.DREAMSCAPE,' in profile
    assert 'BLUE_ID, BLUE_FIELD,' in profile and 'DREAMSCAPE_ID, DREAMSCAPE' in profile
    assert 'Optional.of(id("dreamscape_ambience"))' in catalog
    assert 'Optional.of(id("blue_field"))' in catalog
    assert catalog.count("new DreamDimensionDefinition(") == 2
    assert 'ModBlocks.SERENE_STONE_BLOCK, 12' in catalog
    assert 'ModBlocks.BLUE_DREAM_STONE, 24' in catalog
    assert 'DimensionEffectPolicy.SLOW_FALLING' in catalog
    assert 'DimensionEffectPolicy.NONE' in catalog
    assert '12, 2, 8, 160, 160, 8, Theme.BLUE_FIELD' in profile
    assert '28, 2, 4, 420, 240, 9, Theme.DREAMSCAPE' in profile

    # 4-5: exactly one active client controller, physically isolated bootstrap.
    assert controller.count("@EventBusSubscriber(") == 1
    assert "@EventBusSubscriber(modid = DreamsDimensions.MODID, value = Dist.CLIENT)" in controller
    assert "ClientTickEvent.Post" in controller
    assert "DreamDimensionCatalog.find(level.dimension())" in controller
    assert "flatMap(DreamDimensionDefinition::atmosphereId)" in controller
    assert "filter(candidate -> candidate.dimension().equals(level.dimension()))" in controller
    assert "client.level" in controller and "client.player == null" in controller
    assert "client.isPaused()" in controller
    assert "lastLevel != level || lastProfile != profile" in controller
    assert "AtmosphereAudio.begin(profile)" in controller
    assert "AtmosphereAudio.reset()" in controller
    assert "profile.displayName()" in controller
    assert not any("net.minecraft.client" in text for text in (
        bootstrap, catalog,
        (JAVA / "dimension/DreamDimensionDefinition.java").read_text(encoding="utf-8"),
        (JAVA / "service/DreamTeleportService.java").read_text(encoding="utf-8")
    ))
    assert "client.atmosphere" not in bootstrap

    # 6-7: explicit caps, no 'always visible' bypass, reduced settings, no
    # injection when underwater, and no background work in pause.
    assert "ParticleStatus.MINIMAL" in particles and "ParticleStatus.DECREASED" in particles
    assert 'setting == ParticleStatus.MINIMAL || player.isInWater()' in particles
    assert 'tick % interval != 0' in particles
    assert "int ambientCount = setting == ParticleStatus.DECREASED ? 1 : 3;" in particles
    assert "int accents = setting == ParticleStatus.DECREASED ? 1 : 2;" in particles
    assert "tick % (cadence * 4L) != 0" in particles
    assert "profile.maxParticleSamples()" in particles
    assert "profile.sampleRadius()" in particles
    assert "level.addParticle(" in particles
    assert "addAlwaysVisibleParticle" not in particles
    assert "ROSE_DUST" in particles and "PALE_DUST" in particles
    assert "new DustParticleOptions(0xDDA9C3, 0.78F)" in particles
    assert "new DustParticleOptions(0xF1D4CE, 0.66F)" in particles
    assert "ParticleTypes.WHITE_ASH" in particles and "ParticleTypes.END_ROD" in particles

    # 8: no pending player/world reference or elapsed timers after disconnect
    # or profile switch; initial sound interval profile-driven.
    assert "if (level == null || client.player == null || profile == null)" in controller
    assert "reset();" in controller
    assert "lastLevel = null;" in controller
    assert "lastProfile = null;" in controller
    assert "activeTicks = 0;" in controller
    assert "static void begin(AtmosphereProfile profile)" in audio
    assert "nextCueTick = profile.soundMinimumTicks();" in audio
    assert "nextCueTick = tick + profile.soundMinimumTicks()" in audio
    assert "random.nextInt(profile.soundJitterTicks() + 1)" in audio
    assert "SoundSource.AMBIENT" in audio and "level.playLocalSound(" in audio

    # 9: selected sound event fields are *already used by the Blue feature*
    # and validated by 26.3 compilation. No custom audio registrations.
    for sound in ("SoundEvents.AMETHYST_BLOCK_CHIME",
                  "SoundEvents.BREEZE_IDLE_AIR", "SoundEvents.CHERRY_LEAVES_STEP"):
        assert sound in audio
    assert "volume = 0.14F" in audio and "0.075F" in audio
    assert "level.playLocalSound(" in audio

    # 10: fog attributes are dimension-local. Do not rewrite color, sky,
    # biome, timelines or other (gameplay) attributes.
    attr = dimension["attributes"]
    assert [attr[p + n] for n in (
        "fog_start_distance", "fog_end_distance", "sky_fog_end_distance")] == [42, 190, 235]
    assert 0 < attr[p + "fog_start_distance"] < attr[p + "fog_end_distance"] < attr[p + "sky_fog_end_distance"]
    assert dimension["timelines"] == "#minecraft:in_overworld"
    assert dimension["has_fixed_time"] is False
    assert dimension["ambient_light"] == 1
    assert dimension["height"] == 256 and dimension["min_y"] == 0
    assert biome["attributes"][p + "sky_color"] == "#9e0200"
    assert biome["attributes"][p + "fog_color"] == "#db5b8f"
    assert biome["attributes"][p + "water_fog_color"] == "#050533"
    assert len(biome["features"]) == 10 and len(biome["features"][9]) == 6

    # 11: all detection is read-only, using only loaded client chunks;
    # no packets, custom particle registry, chunk force-loading, or tick IO.
    assert "level.hasChunkAt" in sampling
    assert "Heightmap.Types.MOTION_BLOCKING_NO_LEAVES" in sampling
    assert "memoryResidueFloor(" in sampling and "isDreamscapeFracture(" in sampling
    assert "isDreamscapeElevatedVoid(" in sampling
    assert "for (int i = 0; i < maxSamples; i++)" in sampling
    assert "level.getChunk(" not in sampling
    assert "forceload" not in sampling.lower()
    for body in (controller, particles, audio, sampling):
        for banned in ("sendToServer", "PacketDistributor", "setBlock(", "PlayerTickEvent",
                       "FogRenderer", "mixin", "registerParticle", "new Thread("):
            assert banned not in body, banned

    # 12: Blue parameters and environmental visual identity unchanged.
    blue_attr = blue["attributes"]
    assert (blue_attr[p + "fog_start_distance"], blue_attr[p + "fog_end_distance"],
            blue_attr[p + "sky_fog_end_distance"]) == (48, 170, 210)
    assert blue["has_fixed_time"] is True
    assert blue_biome["attributes"][p + "sky_color"] == "#9bdcfd"
    assert blue_biome["attributes"][p + "fog_color"] == "#b6e2ff"
    assert "int ambientCount = setting == ParticleStatus.DECREASED ? 2 : 4;" in particles
    assert "int sparkleCount = setting == ParticleStatus.DECREASED ? 2 : 4;" in particles
    assert "new DustParticleOptions(0x80CFFF, 0.95F)" in particles
    assert "new DustParticleOptions(0xA0F5EF, 0.9F)" in particles
    assert "new DustParticleOptions(0xC5F8FF, 1.15F)" in particles
    assert 'profile.theme() == AtmosphereProfile.Theme.DREAMSCAPE' in particles
    assert 'profile.theme() == AtmosphereProfile.Theme.DREAMSCAPE' in audio
    print("TASK 3.3 STATIC PASS: profiles, budgets, ambient particles, audio, reset, "
          "native fog, no packets, physical client isolation, Blue unchanged.")
    print("LIMIT: static tests and dedicated-server smoke do NOT render or play client effects.")


if __name__ == "__main__":
    check()
