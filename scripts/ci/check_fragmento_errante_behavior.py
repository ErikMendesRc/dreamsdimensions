#!/usr/bin/env python3
"""TASK 4.2 static contracts plus a dedicated-server /summon RCON smoke test.

Static assertions are not substitutes for Java compilation or visual/MP tests.
"""
import argparse
import time
from pathlib import Path
import socket
import struct

ROOT = Path(__file__).resolve().parents[2]
SRC = (ROOT / "src/main/java/com/dreamsdimensions/mod/entity/FragmentoErranteEntity.java").read_text(encoding="utf-8")
REG = (ROOT / "src/main/java/com/dreamsdimensions/mod/registry/ModEntities.java").read_text(encoding="utf-8")
ANIM = (ROOT / "src/main/resources/assets/dreamsdimensions/geckolib/animations/entity/fragmento_errante.animation.json").read_text(encoding="utf-8")

def static():
    for fragment in (
        "new FlyingPathNavigation(this, level)", "new GentleFlyingMoveControl(this)",
        "addGoal(0, new FleeGoal())", "addGoal(1, new ResonanceGoal())",
        "addGoal(2, new CuriosityGoal())",
        "addGoal(3, new WanderGoal())", "triggerAnim(CONTROLLER, ANIM_CURIOUS)",
        "triggerAnim(CONTROLLER, ANIM_STARTLED)", "triggerAnim(CONTROLLER, ANIM_DISAPPEAR)",
        "protected void defineSynchedData(", "EntityDataSerializers.BYTE",
        "EntityDataSerializers.BOOLEAN", "protected void readAdditionalSaveData(",
        "protected void addAdditionalSaveData(", "level().hasChunkAt(",
        "level().noCollision(", "FLEE_TICKS = 38", "DISAPPEAR_TICKS = 27",
        "public boolean beginDisappearance(boolean removeAfterAnimation)",
        "difference.normalize().scale(maxSpeed)", "setDeltaMovement(result)",
        "HOVER_Y_AMPLITUDE = 0.14", "HOVER_PERIOD_TICKS = 64.0",
        "applyIdleHover();", "this.hoverAnchor = null",
        "Math.sin(phase) * HOVER_Y_AMPLITUDE",
        "sideways.scale(Math.sin(phase) * 0.018)",
        "RESONANCE_TICKS = 90", "RESONANCE_COOLDOWN = 1800",
        "SLOW_FALLING_TICKS = 360", "AGRESSION_WINDOW = 600",
        "new MobEffectInstance(", "MobEffects.SLOW_FALLING",
        "this.aggressionHits >= 2", "beginDisappearance(true)",
        "FragmentoIslandBound", "FragmentoResonanceCooldown",
        "FragmentoIslandHabitat.hasNearbyIslandPerch",
        "level().noCollision(this, getBoundingBox().move(correction.scale(2.0)))",
    ):
        assert fragment in SRC, fragment
    assert SRC.count("new AnimationController") == 1
    assert "NaturalSpawn" not in SRC and "finalizeSpawn" not in SRC
    assert "SpawnPlacementTypes.ON_GROUND" in REG
    assert "Heightmap.Types.MOTION_BLOCKING_NO_LEAVES" in REG
    assert "FragmentoIslandHabitat::maySpawn" in REG
    assert "RegisterSpawnPlacementsEvent.Operation.REPLACE" in REG
    habitat = (ROOT / "src/main/java/com/dreamsdimensions/mod/entity/FragmentoIslandHabitat.java").read_text(encoding="utf-8")
    assert "MIN_SPAWN_Y = 158" in habitat and "MAX_SPAWN_Y = 231" in habitat
    assert "dreamscape_natural_ground" in habitat and "DreamDimensionCatalog.DREAMSCAPE" in habitat
    assert "level.isEmptyBlock(airA) && level.isEmptyBlock(airB)" in habitat
    assert "level.hasChunkAt" in habitat
    import json
    dreamscape = json.loads((ROOT / "src/main/resources/data/dreamsdimensions/worldgen/biome/dreamscape_biome.json").read_text(encoding="utf-8"))
    spawns = dreamscape["attributes"]["minecraft:gameplay/natural_mob_spawns"]
    assert spawns["modifier"] == "overlay"
    creature = spawns["argument"]["spawns_by_category"]["creature"]
    assert len(creature) == 1 and creature[0]["type"] == "dreamsdimensions:fragmento_errante"
    assert creature[0]["weight"] == 100
    assert creature[0]["count"] == {
        "type": "minecraft:uniform", "min_inclusive": 2, "max_inclusive": 3
    }
    assert dreamscape["attributes"]["minecraft:gameplay/creature_world_gen_spawn_probability"] == .34
    assert "builder.sized(0.85F, 1.55F)" in REG
    assert 'registerEntityType("fragmento_errante"' in REG
    import json
    names = json.loads(ANIM)["animations"]
    assert set(names) == {"animation.fragmento_errante." + name for name in (
        "idle", "move", "float", "curious", "startled", "disappear"
    )}
    # Verify visible root displacement and overlapping body/head oscillation.
    for name, minimum_y in (("idle", 2.0), ("move", 1.4), ("float", 2.0)):
        clip = names["animation.fragmento_errante." + name]
        root = clip["bones"]["root"]["position"]
        y_values = [key["vector"][1] for key in root.values()]
        assert max(y_values) - min(y_values) >= minimum_y, name
        assert "rotation" in clip["bones"]["body"], name
    assert names["animation.fragmento_errante.move"]["animation_length"] == 1.5
    print("TASK 4.3 STATIC PASS: natural island spawning, resonance, defensive disappear; TASK 4.2 preserved")

def rcon():
    def send(sock, rid, kind, cmd):
        body = struct.pack("<ii", rid, kind) + cmd.encode("utf-8") + b"\x00\x00"
        sock.sendall(struct.pack("<i", len(body)) + body)
    def recv(sock):
        def exact(n):
            parts = bytearray()
            while len(parts) < n:
                chunk = sock.recv(n - len(parts))
                if not chunk:
                    raise ConnectionError("RCON disconnected")
                parts.extend(chunk)
            return bytes(parts)
        n = struct.unpack("<i", exact(4))[0]
        assert 10 <= n <= 1024 * 1024
        payload = exact(n)
        rid, typ = struct.unpack("<ii", payload[:8])
        return rid, typ, payload[8:-2].decode("utf-8", "replace")

    dim = "dreamsdimensions:dreamscape"
    with socket.create_connection(("127.0.0.1", 25575), timeout=15) as sock:
        sock.settimeout(30)
        send(sock, 11, 3, "dd-ci-local-only")
        for _ in range(4):
            rid, typ, _ = recv(sock)
            if typ == 2:
                assert rid == 11, "RCON auth rejected"
                break
        else:
            raise AssertionError("RCON authentication timed out")
        def command(rid, cmd):
            send(sock, rid, 2, cmd)
            received, _, answer = recv(sock)
            assert received == rid, (rid, received)
            print(cmd, "->", answer, flush=True)
            return answer
        # 0,0 dreamscape already loaded by check_dimensions.py; test at
        # Y=228 to minimize the probability of solid terrain on the spawn.
        summon = command(20, f"execute in {dim} run summon dreamsdimensions:fragmento_errante 0 228 0")
        assert summon and not any(s in summon.lower() for s in ("unknown", "error", "failed", "unable")), summon
        pos = command(21, f"execute in {dim} run data get entity @e[type=dreamsdimensions:fragmento_errante,limit=1,sort=nearest] Pos")
        assert pos and "228" in pos, pos
        # Observe server-authoritative Y bob while initial idle pause is active.
        samples = []
        for index in range(5):
            observed = command(30 + index, f"execute in {dim} run data get entity @e[type=dreamsdimensions:fragmento_errante,limit=1,sort=nearest] Pos")
            positions = observed.partition("[")[2].partition("]")[0].split(",")
            assert len(positions) == 3, observed
            samples.append(float(positions[1].strip().removesuffix("d")))
            if index != 4:
                time.sleep(0.35)
        amplitude = max(samples) - min(samples)
        assert amplitude >= 0.012, f"Mob is visually static in world position: Y samples={samples}"
        print(f"TASK 4.2 HOVER RCON PASS: vertical delta={amplitude:.4f} blocks samples={samples}", flush=True)
        cleanup = command(22, f"execute in {dim} run kill @e[type=dreamsdimensions:fragmento_errante,limit=1,sort=nearest]")
        assert cleanup and "no entity" not in cleanup.lower(), cleanup
    print("TASK 4.2 RCON PASS: dedicated server summon, entity lookup and cleanup (NOT visual validation)")

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--rcon", action="store_true")
    args = parser.parse_args()
    static()
    if args.rcon:
        rcon()
