#!/usr/bin/env python3
"""Task 3.2: strict ecosystem datapack/safety contracts and observed server counts.

No claim of visual success or generated-tree count from static noise signatures.
With --rcon, force loads a bounded set of real new chunks in an ephemeral
dedicated CI server; with --log, counts opt-in actual successful placements.
"""
import argparse
from collections import Counter
import json
import math
from pathlib import Path
import re
import socket
import struct

ROOT = Path(__file__).resolve().parents[2]
WG = ROOT / "src/main/resources/data/dreamsdimensions/worldgen"
JAVA = ROOT / "src/main/java/com/dreamsdimensions/mod/worldgen"
NAMESPACE = "dreamsdimensions:"
FAMILIES = {
    "memory_tree": (10, 18, 4, .75, "count", 4),
    "witness_tree": (21, 28, 3, .44, "count", 1),
    "hanging_roots": (6, 14, 8, .68, "count", 4),
    "fractured_monolith": (12, 22, 4, .51, "count", 2),
    "stone_ribs": (7, 10, 2, .60, "random_chance", .24),
    "echo_garden": (3, 6, 3, .60, "count", 3),
}
MATERIALS = {
    "stone": "serene_stone", "log": "dream_log_block",
    "leaves": "dream_leaves_block", "grass": "dream_grass_block",
    "flower": "dream_flower_block", "moss": "dream_glow_moss",
    "shimmer": "dream_shimmer_block",
}
SEEDS = (14011, 20261108, -904732, 73199013)
MASK = (1 << 64) - 1


def load(p):
    return json.loads(p.read_text(encoding="utf-8"))


def static_checks():
    for p in (ROOT / "src/main/resources/data").rglob("*.json"):
        load(p)
    src = (JAVA / "DreamscapeEcosystemFeature.java").read_text(encoding="utf-8")
    reg = (JAVA / "DreamWorldgen.java").read_text(encoding="utf-8")
    blue_src = (JAVA / "DreamVegetationFeature.java").read_text(encoding="utf-8")
    assert reg.count('FEATURES.register("dreamscape_ecosystem"') == 1
    assert "BuiltInRegistries.FEATURE_TYPE" in reg
    assert "DreamscapeEcosystemFeature.CODEC" in reg
    biome = load(WG / "biome/dreamscape_biome.json")
    blue = load(WG / "biome/campo_onirico_azul.json")
    assert len(biome["features"]) == 10 and all(
        not x for x in biome["features"][:9])
    expected = [NAMESPACE + "dreamscape_" + name for name in FAMILIES]
    assert biome["features"][9] == expected
    assert len(set(expected)) == len(expected)
    assert all("dreamscape_" not in x for step in blue["features"] for x in step)
    assert len(blue["features"]) == 10 and len(blue["features"][9]) == 9
    assert '48L * 48L' in blue_src, "Blue algorithm must remain unchanged"

    for name, (lo, hi, relief, density, gate, attempts) in FAMILIES.items():
        feature = load(WG / "feature" / ("dreamscape_" + name + ".json"))
        placed = load(WG / "placed_feature" / ("dreamscape_" + name + ".json"))
        assert feature == {
            "type": NAMESPACE + "dreamscape_ecosystem",
            "form": name,
            **{k: NAMESPACE + v for k, v in MATERIALS.items()},
            "min_height": lo, "max_height": hi,
            "max_relief": relief, "density": density,
        }, name
        assert placed == {
            "feature": NAMESPACE + "dreamscape_" + name,
            "placement": [
                ({"type": "minecraft:count", "count": attempts} if gate == "count"
                 else {"type": "minecraft:random_chance", "chance": attempts}),
                {"type": "minecraft:in_square"},
                {"type": "minecraft:heightmap", "heightmap": "MOTION_BLOCKING_NO_LEAVES"},
                {"type": "minecraft:biome"},
            ],
        }, name
        assert 0 < density <= 1 and lo <= hi <= 32
        assert 0 < attempts <= 4
    for guard in (
        "implements Feature", "MapCodec<DreamscapeEcosystemFeature>",
        "RecordCodecBuilder.mapCodec", "BlockState.CODEC",
        "level.getSeed()", "SPAWN_PROTECTION = 68",
        "level.getWorldBorder().isWithinBounds(p)",
        "level.getFluidState(p).isEmpty()",
        "level.ensureCanWrite(p)", "MAX_PLANNED_BLOCKS = 1750",
        "if (!plan.valid || plan.blocks.isEmpty())",
        "level.getBlockState(p).isAir()",
        "level.getBlockState(p).is(grass.getBlock())",
        "level.setBlock(e.getKey(), e.getValue().block, FLAGS)",
        "naturalFloor(level, a, false)", "naturalFloor(level, b, false)",
        "anchor.getY() - length - 4",
        "air(level, p.below(12))",
        "LeavesBlock.DISTANCE", "LeavesBlock.PERSISTENT",
        "RotatedPillarBlock.AXIS", "dist >= 6",
        "DREAMSCAPE_ECOSYSTEM_GENERATED",
    ):
        assert guard in src, guard
    assert src.index("level.ensureCanWrite(p)") < src.index("level.setBlock(e.getKey()")
    assert "PlayerTick" not in src and "new Block(" not in src
    assert "minecraft:blue" not in json.dumps(biome)
    for name in FAMILIES:
        assert 'case "' + name + '"' in src, name

    # Analytic fingerprint: exact same value-noise hash and salts as the Java
    # regional gate. This proves only deterministic selection opportunities.
    def h(seed, x, z):
        v = (seed ^ (x * 0x9E3779B97F4A7C15) ^ (z * 0xC2B2AE3D27D4EB4F)) & MASK
        v ^= v >> 30
        v = v * 0xBF58476D1CE4E5B9 & MASK
        v ^= v >> 27
        v = v * 0x94D049BB133111EB & MASK
        v ^= v >> 31
        return (v >> 11) / (1 << 53) * 2.0 - 1.0
    def value_noise(seed, x, z):
        ix, iz = math.floor(x), math.floor(z)
        fx, fz = x - ix, z - iz
        fx, fz = fx * fx * (3 - 2 * fx), fz * fz * (3 - 2 * fz)
        a, b = h(seed, ix, iz), h(seed, ix + 1, iz)
        c, d = h(seed, ix, iz + 1), h(seed, ix + 1, iz + 1)
        return (a + (b - a) * fx) * (1 - fz) + (c + (d - c) * fx) * fz
    signatures = []
    for seed in SEEDS:
        samples = []
        for x in range(-20, 21, 4):
            for z in range(-20, 21, 4):
                xx, zz = x * 16 + 8, z * 16 + 8
                wx = xx + 33 * value_noise(seed ^ 0x771ACE, xx / 265, zz / 265)
                wz = zz + 33 * value_noise(seed ^ 0x3D102, xx / 265 + 17, zz / 265 - 39)
                regnoise = value_noise(seed ^ 0x6F123, wx / 187, wz / 187)
                detail = value_noise(seed ^ 0x8831, wx / 83, wz / 83)
                samples.append(round(.75 * regnoise + .25 * detail, 9))
        assert len(set(samples)) > 35
        assert all(-1.01 <= value <= 1.01 for value in samples)
        signatures.append(tuple(samples))
    assert len(set(signatures)) == len(SEEDS)
    print(f"TASK 3.2 STATIC PASS: {len(FAMILIES)} configured+placed families, "
          "registration, 26.3 stage, materials, write preflight, tree state, "
          "blue isolation, four-seed regional signatures")
    print("MODEL_ONLY: signatures do not count or prove worldgen placement.")


def rcon():
    def packet(sock, rid, typ, command):
        content = struct.pack("<ii", rid, typ) + command.encode() + bytes((0, 0))
        sock.sendall(struct.pack("<i", len(content)) + content)
    def receive(sock):
        def exact(length):
            b = bytearray()
            while len(b) < length:
                part = sock.recv(length - len(b))
                if not part:
                    raise ConnectionError("RCON socket closed")
                b.extend(part)
            return bytes(b)
        length = struct.unpack("<i", exact(4))[0]
        assert 10 <= length <= 1024 * 1024
        return struct.unpack("<ii", (raw := exact(length))[:8])[0], raw[8:-2].decode("utf-8", "replace")
    with socket.create_connection(("127.0.0.1", 25575), timeout=15) as sock:
        sock.settimeout(50)
        packet(sock, 11, 3, "dd-ci-local-only")
        for _ in range(4):
            rid, _ = receive(sock)
            if rid == 11:
                break
        else:
            raise AssertionError("RCON authentication failed")
        # 3 bounded 3x3 areas in distinct regions, only on CI test server.
        regions = ((240, 240, 287, 287), (-352, 176, -305, 223),
                   (528, -400, 575, -353))
        for i, (x0, z0, x1, z1) in enumerate(regions):
            cmd = (f"execute in dreamsdimensions:dreamscape run forceload add "
                   f"{x0} {z0} {x1} {z1}")
            packet(sock, 500 + i, 2, cmd)
            rid, msg = receive(sock)
            assert rid == 500 + i and "marked" in msg.lower(), (cmd, msg)
        for i, (x0, z0, x1, z1) in enumerate(regions):
            cmd = (f"execute in dreamsdimensions:dreamscape run forceload query {x0} {z0}")
            packet(sock, 600 + i, 2, cmd)
            rid, msg = receive(sock)
            assert rid == 600 + i and "marked for force loading" in msg.lower(), (cmd, msg)
        print("TASK 3.2 RCON PASS: 27 chunk tickets in 3 bounded regions; "
              "logs contain any successful placement counts (see --log).")


def actual_counts(path):
    raw = path.read_text(encoding="utf-8", errors="replace")
    events = re.findall(r"DREAMSCAPE_ECOSYSTEM_GENERATED form=(\w+) x=(-?\d+) y=(-?\d+) z=(-?\d+) blocks=(\d+)", raw)
    placements = Counter()
    unique_origins = set()
    duplicate_origin_events = 0
    for form, x, y, z, blocks in events:
        assert form in FAMILIES, form
        assert 0 < int(blocks) <= 1750
        assert 0 < int(y) < 255
        # Independent placed-feature attempts can select the same origin;
        # especially a scattered patch can succeed twice without overwriting
        # cells. Record both successful calls and unique origins explicitly.
        key = (form, x, y, z)
        if key in unique_origins:
            duplicate_origin_events += 1
        unique_origins.add(key)
        placements[form] += 1
    print("TASK 3.2 ACTUAL SERVER GENERATED: "
          + " ".join(f"{name}={placements[name]}" for name in FAMILIES)
          + f" total_successful_calls={len(events)}"
          + f" unique_origins={len(unique_origins)}"
          + f" repeat_origin_calls={duplicate_origin_events}")
    if not events:
        print("NOTICE: 0 real placements observed; worldgen loaded, but silhouette presence unverified.")
    else:
        print("LIMIT: count means feature returned success; neither visual topology nor"
              " full map biome frequency is guaranteed by this small sample.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--rcon", action="store_true")
    parser.add_argument("--log", type=Path)
    args = parser.parse_args()
    static_checks()
    if args.rcon:
        rcon()
    if args.log:
        actual_counts(args.log)
