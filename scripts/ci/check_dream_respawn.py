#!/usr/bin/env python3
"""Task 5.5: schema/architecture regression checks, not live player gameplay.

Run alongside Gradle JUnit and the existing four-seed dedicated-server smoke.
"""
from pathlib import Path
import json

ROOT = Path(__file__).resolve().parents[2]
JAVA = ROOT / "src/main/java/com/dreamsdimensions/mod"
DATA = ROOT / "src/main/resources/data/dreamsdimensions"

def source(path):
    return (JAVA / path).read_text(encoding="utf-8")

def read(path):
    return json.loads((DATA / path).read_text(encoding="utf-8"))

for dimension in ("dreamscape_type", "campo_onirico_azul_type"):
    data = read(f"dimension_type/{dimension}.json")
    bed = data["attributes"]["minecraft:gameplay/bed_rule"]
    assert bed["can_sleep"] == "never", dimension
    assert bed["can_set_spawn"] == "never", dimension
    assert bed["destroy_on_use"] is False and bed["destroy_on_leave"] is False, dimension
    assert data["attributes"]["minecraft:gameplay/respawn_anchor_works"] is False
    assert data["skybox"] == "overworld"

anchor = source("attachment/DreamExpeditionAnchorData.java")
attachment = source("registry/ModAttachments.java")
policy = source("service/DreamRespawnPolicy.java")
service = source("service/DreamRespawnService.java")
bootstrap = source("DreamsDimensions.java")
entry = source("service/DreamTeleportService.java")
return_item = source("item/OneiricAwakenerItem.java")
spawn = source("util/DreamSpawnHelper.java")
original_return = source("attachment/DreamReturnData.java")

assert "MapCodec<DreamExpeditionAnchorData>" in anchor and '"dimension"' in anchor
assert '"pos"' in anchor and '"yaw"' in anchor
assert "isActiveFor(" in anchor
# Minecraft/DFU MapCodec classes are NOT on this project's JUnit test runtime.
# Check optional serialization structure statically; live codec round-trip
# requires a 26.3 GameTest / controlled Minecraft runtime.
assert "RecordCodecBuilder.mapCodec(" in anchor
assert 'optionalFieldOf("dimension")' in anchor
assert 'optionalFieldOf("pos")' in anchor
assert 'optionalFieldOf("yaw")' in anchor
assert "new DreamExpeditionAnchorData()" not in anchor.split("CODEC =", 1)[0]
assert '"dream_expedition_anchor"' in attachment
assert "DreamExpeditionAnchorData.CODEC" in attachment
# 9.3: hangover must also persist through death, without rewriting legacy codecs.
assert attachment.count(".copyOnDeath()") == 3
assert '"dream_hangover"' in attachment and "DreamHangoverData.CODEC" in attachment
assert '"dream_return"' in attachment
assert "DreamReturnData.CODEC" in attachment
assert '"dimension"' in original_return and '"pos"' in original_return and '"yaw"' in original_return

for must in (
    "DreamDimensionCatalog.find(source)", "PlayerRespawnPositionEvent",
    "event.isFromEndFight()", "getLevelData().isHardcore()",
    "event.setTeleportTransition(new TeleportTransition(",
    "DreamSpawnHelper.isSafeStandingSpace(",
    "DreamSpawnHelper.findNaturalArrival(",
    "DreamSpawnHelper.findOrCreateSafeArrival(",
    "player.setData(ModAttachments.DREAM_EXPEDITION_ANCHOR",
    "player.removeData(ModAttachments.DREAM_EXPEDITION_ANCHOR)",
    "player.hasData(ModAttachments.DREAM_EXPEDITION_ANCHOR)",
    "DREAM_RESPAWN", "onRespawnComplete(", "onPlayerTick(",
):
    assert must in service, must
assert "PlayerRespawnEvent" in service and "onRespawnPosition" in service
assert "player.getInventory().clearContent" not in service
assert "player.teleport(" not in service
# Reject an executable forced overwrite of vanilla's saved respawn.
# Comments may legitimately mention the API as a documented anti-pattern.
assert "event.setCopyOriginalSpawnPosition(true);" not in service
assert "GameRules.KEEP_INVENTORY" not in service

assert "boolean interceptDeath(" in policy
assert "enum Selection" in policy
assert "TECHNICAL_FAILURE" in policy
assert "findNaturalArrival(" in spawn
assert "public static boolean isSafeStandingSpace(" in spawn
assert "PLATFORM_OFFSETS = {0, 6, -6, 12, -12}" in spawn

assert "DreamRespawnService.recordExpedition(teleported, spawn.get())" in entry
assert entry.index("ServerPlayer teleported = player.teleport(") < entry.index("DreamRespawnService.recordExpedition(")
assert "DreamRespawnService.completeExpedition(returned)" in return_item
assert return_item.index("stack.shrink(1)") < return_item.index("DreamRespawnService.completeExpedition(")
assert "OneiricReturnPolicy.confirm(" in return_item
assert "DreamNecklaceEquipmentPolicy" not in return_item

for callback in ("onRespawnPosition", "onRespawnComplete", "onPlayerTick"):
    assert bootstrap.count(f"DreamRespawnService::{callback}") == 1, callback

assert (ROOT / "src/test/java/com/dreamsdimensions/mod/service/DreamRespawnPolicyTest.java").exists()

print("TASK 5.5 STATIC PASS: no sleeping/spawn-setting/explosions in dreams, "
      "pre-clone respawn, persisted anchor, safe fallback and post-payment cleanup.")
