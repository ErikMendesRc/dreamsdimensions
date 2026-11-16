#!/usr/bin/env python3
"""Static task 5.1 invariants; does not claim Minecraft gameplay validation."""
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
JAVA = ROOT / "src/main/java/com/dreamsdimensions/mod"
handler = (JAVA / "event/EchoMossInteractionHandler.java").read_text(encoding="utf-8")
entry = (JAVA / "DreamsDimensions.java").read_text(encoding="utf-8")
search = (JAVA / "service/EchoMossSearch.java").read_text(encoding="utf-8")
cooldown = (JAVA / "service/EchoMossCooldown.java").read_text(encoding="utf-8")

assert entry.count("NeoForge.EVENT_BUS.addListener(EchoMossInteractionHandler::onRightClickBlock);") == 1
assert "PlayerInteractEvent.RightClickBlock" in handler
assert "InteractionResult.SUCCESS" in handler and "event.setCanceled(true)" in handler
assert "ServerPlayer player" in handler and "new EchoMossCooldown(COOLDOWN_TICKS)" in handler
assert "getChunkNow(" in handler and "chunk.getBlockState(probe)" in handler
assert "worldBorder" in handler.lower() or "getWorldBorder" in handler
assert "isWithinBlockInteractionRange" in handler
assert "EchoMossEligibility.accepts" in handler
assert "HORIZONTAL_RADIUS = 12" in search and "VERTICAL_RADIUS = 6" in search
assert "25 * 25 * 13" in search and "horizontalSquared >" in search
assert "Map<UUID, Long>" in cooldown and "durationTicks" in cooldown
for forbidden in ("setBlock(", "addItem(", "spawnAtLocation(", "getChunk(", "addTicket(", "drop("):
    assert forbidden not in handler, f"unexpected side effect: {forbidden}"
assert "PlayerTickEvent" not in handler and "Thread.sleep" not in handler
assert "ModBlocks.java" not in entry
for lang in ("pt_br", "en_us"):
    translations = json.loads((ROOT / f"src/main/resources/assets/dreamsdimensions/lang/{lang}.json").read_text(encoding="utf-8"))
    for key in ("found", "absent", "unavailable"):
        assert f"message.dreamsdimensions.echo_moss.{key}" in translations
print("Task 5.1: one event, eligibility, no loot/chunk loading/tick scans, translations OK (static).")
