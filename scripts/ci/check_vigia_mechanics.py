#!/usr/bin/env python3
"""TASK 4.6 mechanical contracts. Static checks complement Java/JUnit and server smoke."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
BASE = ROOT / "src/main/java/com/dreamsdimensions/mod"
def source(path):
    return (BASE / path).read_text(encoding="utf-8")
vigia = source("entity/VigiaAdormecidoEntity.java")
safe = source("entity/navigation/SafeGroundNavigation.java")
calm = source("entity/behavior/CalmPlayerInteraction.java")
anchor = source("entity/behavior/DreamAnchorService.java")
policy = source("entity/behavior/DreamAnchorPolicy.java")
main = source("DreamsDimensions.java")
fragment = source("entity/FragmentoErranteEntity.java")
population = source("entity/spawn/DreamMobPopulationService.java")

assert "PactCooldown" not in fragment  # Fragmento effects and AI remain independent
assert "VigiaPactCooldown" in vigia and "VigiaStateTicks" in vigia
assert "resetRitual()" in vigia and "DreamAnchorService.grant(" in vigia
assert "CalmPlayerInteraction.isAttentive(" in vigia
assert "SafeGroundNavigation.hasSupportedCorridor(" in vigia
assert "SafeGroundNavigation.canStand(" in vigia
assert "player.hasLineOfSight(mob)" in calm
assert "player.isSprinting()" in calm and "player.onGround()" in calm
assert "player.getLookAngle()" in calm
assert "level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES" in safe
assert "level.hasChunkAt(" in safe and "level.noCollision(" in safe
assert "level.getWorldBorder()" in safe
assert "level.getFluidState(" in safe
assert "level.getBlockState(floor).isFaceSturdy(" in safe
assert "player.teleportTo(" in anchor
assert "player.resetFallDistance()" in anchor
assert "SafeGroundNavigation.canStand(" in anchor
assert "ACTIVE.remove(player.getUUID());" in anchor
assert "player.getAbilities().instabuild" in anchor
assert "onPlayerLoggedOut" in anchor and "onChangedDimension" in anchor
assert "onPlayerClone" in anchor
assert main.count("DreamAnchorService::onPlayerTick") == 1
assert main.count("DreamMobPopulationService::onPlayerTick") == 1
assert "DreamAnchorService::onPlayerLoggedOut" in main
assert "DreamAnchorService::onPlayerClone" in main
assert "DreamAnchorService::onChangedDimension" in main
assert "record DreamAnchorPolicy" in policy
assert "new DreamAnchorPolicy(20 * 120, 12, 48, -0.10)" in anchor
assert "getGameTime()" in anchor and "ACTIVE" in anchor
assert "EntitySpawnReason.NATURAL" in population
assert "new AnimationController" in vigia and vigia.count("new AnimationController") == 1
print("TASK 4.6 STATIC PASS: calm ritual, supported travel, one-use safe recall, event cleanup")
