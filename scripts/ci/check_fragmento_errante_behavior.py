#!/usr/bin/env python3
"""TASK 4.2 static contracts plus a dedicated-server /summon RCON smoke test.

Static assertions are not substitutes for Java compilation or visual/MP tests.
"""
import argparse
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
        "addGoal(0, new FleeGoal())", "addGoal(1, new CuriosityGoal())",
        "addGoal(3, new WanderGoal())", "triggerAnim(CONTROLLER, ANIM_CURIOUS)",
        "triggerAnim(CONTROLLER, ANIM_STARTLED)", "triggerAnim(CONTROLLER, ANIM_DISAPPEAR)",
        "protected void defineSynchedData(", "EntityDataSerializers.BYTE",
        "EntityDataSerializers.BOOLEAN", "protected void readAdditionalSaveData(",
        "protected void addAdditionalSaveData(", "level().hasChunkAt(",
        "level().noCollision(", "FLEE_TICKS = 38", "DISAPPEAR_TICKS = 27",
        "public boolean beginDisappearance(boolean removeAfterAnimation)",
        "difference.normalize().scale(maxSpeed)", "setDeltaMovement(result)",
    ):
        assert fragment in SRC, fragment
    assert SRC.count("new AnimationController") == 1
    assert "NaturalSpawn" not in SRC and "finalizeSpawn" not in SRC
    assert "builder.sized(0.85F, 1.55F)" in REG
    assert 'registerEntityType("fragmento_errante"' in REG
    import json
    names = json.loads(ANIM)["animations"]
    assert set(names) == {"animation.fragmento_errante." + name for name in (
        "idle", "move", "float", "curious", "startled", "disappear"
    )}
    print("TASK 4.2 STATIC PASS: flight, goal priorities, sync, 6 animations, persistence, no natural spawn")

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
