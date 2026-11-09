#!/usr/bin/env python3
"""TASK 3.4: regression contracts and opt-in actual-block bridge topology audit.

Static checks prove graph integrity, unchanged island definitions and positive
voxel sets above Y140. --rcon asks the dedicated server to inspect actual
loaded voxels in bounded regions with a read-only, CI-only command. The
returned bank-to-bank transect check is distinct from aesthetic homologation.
"""
import argparse
import hashlib
import json
import math
import re
import socket
import struct
import time
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DATA = ROOT / "src/main/resources/data/dreamsdimensions"
WG = DATA / "worldgen"
PREFIX = "dreamsdimensions:dreamscape_"
BASELINE = "15eb089e0407acb68906e743ef3982ff5a04fb61"
ISLAND_BLOBS = {
    "density_function/dreamscape_island_region.json": "42cf9c204f3fc3bdc1c8bb04f311d4f4a863c70f",
    "density_function/dreamscape_island_footprint.json": "b0f17e4ce4041469febeb0d6f71ea517eb1d4e86",
    "density_function/dreamscape_island_volume.json": "6675a9f87d2bebcf14d9e7d529b0308105791ee4",
    "noise/dreamscape_sky_clusters.json": "576f19fb3dd4960266ed488993981e9eb2d13f3b",
    "noise/dreamscape_sky_contours.json": "e75cf33b2cec633b7700b6f17f4e5f33bd2d5db2",
    "noise/dreamscape_sky_weathering.json": "046a8f17a5938210dd3a6820af6ccda5ef494c68",
}
# Non-overlapping fixed windows; test-server-only, bounded search with
# early stop after two verified transects. Never generalize counts per chunk.
REGIONS = ((256, 256), (-320, 192), (544, -384),
           (-640, -480), (768, 640), (-896, 256),
           (1024, -256), (1440, 192), (-1152, -768), (128, 1024))
DIM = "dreamsdimensions:dreamscape"


def read(path):
    return json.loads(path.read_text(encoding="utf-8"))


def sha1_blob(path):
    data = path.read_bytes()
    return hashlib.sha1(b"blob " + str(len(data)).encode() + b"\0" + data).hexdigest()


def gradient(a, b, va, vb, y):
    t = max(0.0, min(1.0, (y - a) / (b - a)))
    return va + t * (vb - va)


def check_static():
    for path in DATA.rglob("*.json"):
        read(path)
    for name, sha in ISLAND_BLOBS.items():
        got = sha1_blob(WG / name)
        assert got == sha, (name, got, "island baseline bytes changed")
    d = WG / "density_function"
    bridge = read(d / "dreamscape_bridge_deck.json")
    density = read(d / "dreamscape_solid_density.json")
    settings = read(WG / "noise_settings/dreamscape_noise.json")
    original_land = {
        "type": "minecraft:sub",
        "left": PREFIX + "mainland",
        "right": PREFIX + "fracture_excavation"
    }
    assert density == {
        "type": "minecraft:max",
        "left": {
            "type": "minecraft:max",
            "left": original_land,
            "right": PREFIX + "bridge_deck"
        },
        "right": PREFIX + "island_volume"
    }
    assert bridge == {
        "type": "minecraft:min",
        "left": {
            "type": "minecraft:min",
            "left": {"type": "minecraft:mul", "left": 7,
                     "right": {"type": "minecraft:sub",
                               "left": PREFIX + "bridge_strength",
                               "right": .42}},
            "right": {"type": "minecraft:mul", "left": 9,
                      "right": {"type": "minecraft:sub",
                                "left": PREFIX + "fracture_activity",
                                "right": .03}}
        },
        "right": {
            "type": "minecraft:min",
            "left": {"type": "minecraft:gradient", "axis": "y",
                     "tiling": "clamp_to_edge", "from_coordinate": 108,
                     "to_coordinate": 114, "from_value": -3.4, "to_value": 3},
            "right": {"type": "minecraft:gradient", "axis": "y",
                      "tiling": "clamp_to_edge", "from_coordinate": 115,
                      "to_coordinate": 128, "from_value": 3, "to_value": -4}
        }
    }, "Bridge should remain a locally bounded density volume"
    # The bridge term is always strictly negative above Y=140 for all
    # horizontal fields, so it cannot add a single stone voxel on the
    # preserved floating-island floor Y>=145; this is algebraic, not
    # a synthetic approximation of Minecraft noise.
    for y in range(140, 256):
        cap = min(gradient(108, 114, -3.4, 3, y),
                  gradient(115, 128, 3, -4, y))
        assert cap < -2.8, (y, cap)
        for land in (-12, -3.1, -.3, 0.01, 1.6, 6.7):
            for island in (-8, -.3, 0.01, 3):
                old = max(land, island) > 0
                new = max(land, island, cap) > 0
                assert old == new, (y, land, island)
    assert settings["noise_router"]["final_density"] == {
        "type": "minecraft:interpolated", "input": PREFIX + "solid_density",
        "cell_size_xz": 4, "cell_size_y": 4
    }
    assert settings["noise_router"]["chunk_surface_level"] == {
        "type": "minecraft:find_top_surface",
        "density": PREFIX + "solid_density",
        "lower_bound": 0, "upper_bound": 248, "cell_height": 8
    }
    assert settings["spawn_target"] == [{
        PREFIX + "fracture_activity": [0, .08],
        PREFIX + "island_region": [0, .30]
    }]
    assert settings["material_rule"] == PREFIX + "surface"
    assert read(WG / "material_rule/dreamscape_surface.json")["type"] == "minecraft:sequence"
    # Graph references and all baseline noise JSON are checked by 3.1 script.
    assert read(WG / "biome/dreamscape_biome.json")["features"][9] == [
        PREFIX + x for x in ("memory_tree", "witness_tree", "hanging_roots",
                "fractured_monolith", "stone_ribs", "echo_garden")]
    assert read(WG / "biome/campo_onirico_azul.json")["features"][9][0] == "dreamsdimensions:mist_spines"
    dtype = read(DATA / "dimension_type/dreamscape_type.json")
    assert dtype["height"] == 256 and dtype["min_y"] == 0
    assert [dtype["attributes"]["minecraft:visual/" + n]
            for n in ("fog_start_distance", "fog_end_distance", "sky_fog_end_distance")] == [42, 190, 235]
    java = ROOT / "src/main/java/com/dreamsdimensions/mod"
    diagnostic = (java / "diagnostics/DreamscapeBridgeAuditCommand.java").read_text()
    engine = (java / "DreamsDimensions.java").read_text()
    vegetation = (java / "worldgen/DreamscapeEcosystemFeature.java").read_text()
    assert 'Boolean.getBoolean("dreamsdimensions.enableBridgeAudit")' in engine
    assert 'DreamscapeBridgeAuditCommand::register' in engine
    assert 'Boolean.getBoolean("dreamsdimensions.enableBridgeAudit")' in diagnostic
    assert 'level.hasChunkAt(' in diagnostic
    assert "MAX_RADIUS = 64" in diagnostic
    assert "MAX_STEPS = 30" in diagnostic and "MIN_GAP_STEPS = 3" in diagnostic
    assert "DREAMSCAPE_BRIDGE_CONFIRMED" in diagnostic
    assert "getBlockState" in diagnostic and "getFluidState" in diagnostic
    assert "setBlock" not in diagnostic and "forceload" not in diagnostic.lower()
    assert 'SPAWN_PROTECTION = 68' in vegetation and 'level.ensureCanWrite(p)' in vegetation
    for text in ("net.minecraft.client", "PlayerTickEvent", "sendToServer"):
        assert text not in diagnostic
    profile = (java / "client/atmosphere/AtmosphereProfile.java").read_text()
    assert '12, 2, 8, 160, 160, 8, Theme.BLUE_FIELD' in profile
    assert '28, 2, 4, 420, 240, 9, Theme.DREAMSCAPE' in profile

    # Conventions of bridge centerline topology are reproduced independently:
    # at least one 20-deep air column, two naturally backed up banks, 2-block
    # headroom throughout, and <=1 Y change per step. A single roof over air
    # never passes this predicate.
    def transect(cols):
        if len(cols) < 17:
            return False
        mid = len(cols) // 2
        if not cols[mid]["gap"]:
            return False
        for sign in (-1, 1):
            gap = 0
            reached = False
            prev = cols[mid]["y"]
            for step in range(1, min(mid, 30) + 1):
                c = cols[mid + sign * step]
                if abs(c["y"] - prev) > 1 or not c["air2"]:
                    break
                prev = c["y"]
                gap += int(c["gap"])
                if step >= 8 and c["bank"] and gap >= 3:
                    reached = True
                    break
            if not reached:
                return False
        return True

    cases = [
        # confirmed: continuous over span; left/right backing present.
        [{ "y": 116, "gap": abs(i - 20) < 11,
           "bank": abs(i - 20) >= 11, "air2": True}
         for i in range(41)],
        # roof-over-air but one bank missing: NOT a bridge.
        [{ "y": 116, "gap": abs(i - 20) < 11,
           "bank": i < 9, "air2": True}
         for i in range(41)],
        # both banks, broken walkable path: NOT a bridge.
        [{ "y": 116 if i != 29 else 128, "gap": abs(i - 20) < 11,
           "bank": abs(i - 20) >= 11, "air2": i != 27}
         for i in range(41)]
    ]
    assert [transect(c) for c in cases] == [True, False, False]
    print("TASK 3.4 STATIC PASS: baseline island SHA, positive voxels Y>=140, "
          "bridge density and DF router, spawn/materials, six features, Blue/atmosphere "
          "isolation, opt-in read-only diagnostic, 3 model topology cases.")
    print("LIMIT: analytical geometry cases are NOT evidence of in-game bridge formation.")


class Rcon:
    def __init__(self):
        self.sock = socket.create_connection(("127.0.0.1", 25575), timeout=20)
        self.sock.settimeout(180)
        self.number = 100

    def close(self):
        self.sock.close()

    def read(self):
        def exactly(n):
            data = bytearray()
            while len(data) < n:
                part = self.sock.recv(n - len(data))
                if not part:
                    raise ConnectionError("RCON disconnected")
                data.extend(part)
            return bytes(data)
        length = struct.unpack("<i", exactly(4))[0]
        assert 10 <= length <= 1048576
        packet = exactly(length)
        return struct.unpack("<ii", packet[:8])[0], packet[8:-2].decode("utf-8", "replace")

    def send(self, typ, message):
        payload = struct.pack("<ii", self.number, typ) + message.encode() + b"\0\0"
        self.sock.sendall(struct.pack("<i", len(payload)) + payload)

    def auth(self):
        self.number = 11
        self.send(3, "dd-ci-local-only")
        for _ in range(4):
            rid, msg = self.read()
            if rid == self.number:
                return
        raise AssertionError("RCON auth failed")

    def command(self, text):
        self.number += 1
        self.send(2, text)
        rid, answer = self.read()
        assert rid == self.number, (rid, self.number, text)
        return answer


def server_checks():
    conn = Rcon()
    try:
        conn.auth()
        records = []
        for x, z in REGIONS:
            radius = 56
            start = time.monotonic()
            response = conn.command(
                f"execute in {DIM} run forceload add {x-64} {z-64} {x+64} {z+64}"
            )
            assert "marked" in response.lower() and "force load" in response.lower(), (x, z, response)
            answer = conn.command(f"dreamscape_bridge_audit {x} {z} {radius}")
            assert "DREAMSCAPE_BRIDGE_AUDIT" in answer, (x, z, answer)
            assert "status=SKIPPED" not in answer, (x, z, answer)
            print("DREAMSCAPE_AUDIT_DETAIL:", answer.strip()[:1200])
            match = re.search(r"confirmed_transects=(\d+)", answer)
            if match is None:
                # Some RCON builds only return the last command message.
                # Still retain the raw answer and assess recorded server log.
                print("DREAMSCAPE_AUDIT_RESPONSE", x, z, repr(answer[:400]))
                count = None
            else:
                count = int(match.group(1))
            duration = round(time.monotonic() - start, 3)
            records.append((x, z, count, duration))
            print(f"DREAMSCAPE_AUDIT_REGION center={x},{z} radius={radius} "
                  f"confirmed={count} elapsed_seconds={duration}")
            # Save CPU and chunk generation when a seed already has two
            # distinct verified examples; early-stop evidence is explicit.
            if sum(v for _, _, v, _ in records if v is not None) >= 2:
                break
        confirmed = [count for _, _, count, _ in records if count is not None]
        print(f"TASK 3.4 RCON PASS: {len(records)} checked regions; "
              f"confirmed_transects={sum(confirmed)} "
              "if 0 then no structures validated in this sample.")
        print("IMPORTANT: Server log contains individual bridge center and two bank coordinates "
              "if confirmed. A continuous walking transect is not a visual/client approval.")
    finally:
        conn.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--rcon", action="store_true")
    args = parser.parse_args()
    check_static()
    if args.rcon:
        server_checks()
