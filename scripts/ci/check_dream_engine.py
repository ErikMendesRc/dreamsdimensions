#!/usr/bin/env python3
"""TASK 2.2 static architecture contracts, not a simulated Minecraft player."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
JAVA = ROOT / "src/main/java/com/dreamsdimensions/mod"

def source(path):
    return (JAVA / path).read_text(encoding="utf-8")

catalog = source("dimension/DreamDimensionCatalog.java")
profile = source("dimension/DreamDimensionDefinition.java")
config = source("config/DreamsConfig.java")
teleport = source("service/DreamTeleportService.java")
sleep = source("event/SleepTeleportHandler.java")
spawn = source("util/DreamSpawnHelper.java")
effects = source("event/DreamDimensionEffectsHandler.java")
client = source("client/atmosphere/AtmosphereClientController.java")
transitions = source("client/event/ClientDimensionEvents.java")
return_data = source("attachment/DreamReturnData.java")
attachment = source("registry/ModAttachments.java")
awakener = source("item/OneiricAwakenerItem.java")

assert catalog.count("new DreamDimensionDefinition(") == 2
for name in ('"dreamscape"', '"campo_onirico_azul"'):
    assert name in catalog
assert "ModBlocks.SERENE_STONE_BLOCK, 12" in catalog
assert "ModBlocks.BLUE_DREAM_STONE, 24" in catalog
assert "DimensionEffectPolicy.SLOW_FALLING" in catalog
assert "DimensionEffectPolicy.NONE" in catalog
assert "BLUE_FIELD, true, 1" in catalog
assert "DREAMSCAPE, true, 1" in catalog
assert "List.of(" in catalog and "Map.copyOf(entries)" in catalog
assert "sleepDestination" in profile and "selectionWeight" in profile
assert "emergencyPlatform" not in profile or "fallbackFloor" in profile
assert "naturalSpawnRadius" in profile
assert "getEnabledDefinitions()" in config and "DreamDimensionCatalog.all()" in config
assert "DreamDimensionCatalog.find(key).isEmpty()" in config
assert '"dream_dimensions"' in config
assert "server.getLevel(definition.dimension())" in teleport
assert "WeightedDimensionPicker.order(" in teleport
assert "teleported.getData(ModAttachments.DREAM_RETURN).setOverworldBed" in teleport
assert "teleported == null" in teleport and "return false" in teleport
assert "TICKS_BEFORE_TELEPORT = 100" in sleep
assert "PlayerWakeUpEvent" in sleep and "COMPLETED_WAKEUPS" in sleep
assert "TELEPORTING" in sleep and "PlayerLoggedOutEvent" in sleep
assert "definition.naturalSpawnRadius()" in spawn
assert "definition.fallbackFloor().get().defaultBlockState()" in spawn
assert "isBlueDream(" not in spawn
assert "PLATFORM_RADIUS = 2" in spawn
assert "floorState.is(Blocks.MAGMA_BLOCK)" in spawn
assert "level.noCollision(" in spawn
assert "isClearForPlatform" in spawn and "level.getBlockState(floor.above(dy)).isAir()" in spawn
assert "getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES" in spawn
assert "DreamDimensionCatalog.find(player.level().dimension())" in effects
assert "policy != DimensionEffectPolicy.SLOW_FALLING" in effects
assert "current == null" in effects and "claim == null" in effects
assert "OWNED.remove(uuid)" in effects and "player.removeEffect(" not in effects
assert "DreamDimensionCatalog.find(level.dimension())" in client
assert "lastLevel != level || lastProfile != profile" in client
assert "AtmosphereAudio.reset()" in client
assert "DreamDimensionCatalog.all()" in transitions
assert "registerConditionalEffect(" in transitions
assert "DREAM_RETURN" in attachment and '"dream_return"' in attachment
assert "DreamReturnData.CODEC" in attachment and ".copyOnDeath()" in attachment
for field in ('"dimension"', '"pos"', '"yaw"'):
    assert f"optionalFieldOf({field})" in return_data, f"persisted field must remain {field}"
assert "USE_DURATION_TICKS = 60" in awakener
assert "COOLDOWN_TICKS = 60" in awakener
assert not any("net.minecraft.client" in source(p) for p in (
    "DreamsDimensions.java", "dimension/DreamDimensionCatalog.java",
    "service/DreamTeleportService.java", "event/DreamDimensionEffectsHandler.java"))
print("TASK 2.2: profile contracts, event isolation, routing, spawns and attachment schema OK (static).")
