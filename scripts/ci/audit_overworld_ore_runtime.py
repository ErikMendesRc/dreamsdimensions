#!/usr/bin/env python3
"""Task 5.6: bounded REAL voxel survey in a dedicated Minecraft 26.3 server.

Requires -Ddreamsdimensions.enableOreAudit=true, existing RCON setup and
a disposable test world. Coordinates below are fixed SURVEY REGIONS,
NOT claimed ore findings or safe teleport destinations.

4 disjoint 8x8-chunk regions = 256 chunks/seed. The Java command only
reads loaded chunks. RCON forceload is explicitly the TEST HARNESS, not
part of the mod or gameplay. No editing of chunks or items.
"""
import argparse
import json
import re
import time
from pathlib import Path
from check_dreamscape_homologation import Rcon

AREAS = ((-4, -4), (64, -4), (-68, 64), (4, -68))
SIDE = 8
REGEX = re.compile(
    r"DREAM_ORE_AUDIT status=OK dimension=minecraft:overworld"
    r" start_chunk=(-?\d+),(-?\d+) side=(\d+)"
    r" chunks=(\d+) chunks_with_ore=(\d+)"
    r" dream_ore=(\d+) deepslate_dream_ore=(\d+)"
    r" exposed_dream_ore=(\d+) exposed_deepslate_dream_ore=(\d+)"
    r" unknown_exposure=(\d+) y_range=(-?\d+):(-?\d+)"
)

def fail_if_bad(response, context):
    lower = response.lower()
    if any(word in lower for word in ("unknown command", "incorrect argument",
                                       "failed to execute", "not found", "error executing")):
        raise RuntimeError(f"{context}: {response}")

def run(expected_seed, output):
    client = Rcon()
    records = []
    try:
        client.auth()
        actual = client.command("seed")
        all_seed_numbers = [int(s) for s in re.findall(r"-?\d+", actual)]
        if expected_seed not in all_seed_numbers:
            raise AssertionError(f"Seed mismatch: expected={expected_seed}, response={actual!r}")
        print(f"OVERWORLD_ORE_SEED_VERIFIED seed={expected_seed}", flush=True)

        for index, (cx, cz) in enumerate(AREAS):
            x1, z1 = cx * 16, cz * 16
            x2, z2 = (cx + SIDE) * 16 - 1, (cz + SIDE) * 16 - 1
            load = (f"execute in minecraft:overworld run forceload add "
                    f"{x1} {z1} {x2} {z2}")
            print(f"OVERWORLD_ORE_PRELOAD region={index} chunks={SIDE*SIDE} "
                  f"xz={x1},{z1}..{x2},{z2}", flush=True)
            started = time.monotonic()
            loaded_response = client.command(load)
            fail_if_bad(loaded_response, "forceload")
            if not loaded_response.strip():
                raise RuntimeError("Empty forceload response: " + load)
            print(f"OVERWORLD_ORE_LOAD_RESULT {loaded_response.strip()[:250]}", flush=True)

            report = ""
            for attempt in range(12):
                report = client.command(f"dream_ore_audit {cx} {cz} {SIDE}")
                if "DREAM_ORE_AUDIT status=OK" in report:
                    break
                if "DREAM_ORE_AUDIT status=SKIPPED reason=chunk_not_loaded_" not in report:
                    raise AssertionError(f"Ore audit not ready: {report!r}")
                time.sleep(2)
            parsed = REGEX.search(report)
            if not parsed:
                raise AssertionError(f"Unparsable or incomplete REAL ore audit: {report!r}")
            fields = [int(n) for n in parsed.groups()]
            assert fields[:3] == [cx, cz, SIDE], (fields, cx, cz)
            assert fields[3] == SIDE * SIDE, f"Expected {SIDE*SIDE} FULL chunks, got {fields[3]}"
            row = {
                "region": index, "chunk_x": cx, "chunk_z": cz,
                "chunks": fields[3], "chunks_with_ore": fields[4],
                "dream_ore_blocks": fields[5],
                "deepslate_dream_ore_blocks": fields[6],
                "exposed_dream_ore_blocks": fields[7],
                "exposed_deepslate_dream_ore_blocks": fields[8],
                "unknown_exposure": fields[9],
                "min_y": fields[10], "max_y": fields[11],
                "duration_seconds": round(time.monotonic()-started, 2),
                "rcon_raw": parsed.group(0)
            }
            assert 0 <= row["chunks_with_ore"] <= row["chunks"]
            assert 0 <= row["exposed_dream_ore_blocks"] <= row["dream_ore_blocks"]
            assert 0 <= row["exposed_deepslate_dream_ore_blocks"] <= row["deepslate_dream_ore_blocks"]
            records.append(row)
            print(f"OVERWORLD_ORE_RUNTIME_REGION {json.dumps(row, sort_keys=True)}", flush=True)
            # Release CI's forcing tickets; no gameplay feature or persistent force-load.
            removed = client.command(
                f"execute in minecraft:overworld run forceload remove {x1} {z1} {x2} {z2}")
            fail_if_bad(removed, "remove forceload")
            print(f"OVERWORLD_ORE_UNLOAD_REGION region={index} {removed.strip()[:120]}", flush=True)

        sample = {
            "source": "ACTUAL_GENERATED_CHUNKS_WITH_RCON_AUDIT",
            "world": "minecraft:overworld", "seed": expected_seed,
            "chunks": sum(row["chunks"] for row in records),
            "chunks_with_ore": sum(row["chunks_with_ore"] for row in records),
            "dream_ore_blocks": sum(row["dream_ore_blocks"] for row in records),
            "deepslate_dream_ore_blocks": sum(row["deepslate_dream_ore_blocks"] for row in records),
            "exposed_dream_ore_blocks": sum(row["exposed_dream_ore_blocks"] for row in records),
            "exposed_deepslate_dream_ore_blocks": sum(row["exposed_deepslate_dream_ore_blocks"] for row in records),
            "regions": records,
            "limitations": [
                "worldgen samples fixed disjoint regions, not a random representative survey",
                "air-exposed voxel count does NOT equal reachable ore or powder yield",
                "4 region centers are predefined sampling coordinates, not discovered ores",
                "exact observed ore XYZ, biome and Y bands are in server-smoke.log",
                "drops/Fortune/Silk Touch and manual discovery need Survival playtest",
            ],
        }
        assert sample["chunks"] == 256, sample
        # Test harness cannot falsely claim that absence in a region means
        # worldgen is universally broken. Keep that as a REPORTABLE finding.
        print("OVERWORLD_ORE_RUNTIME_SAMPLE", json.dumps(sample, sort_keys=True), flush=True)
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(json.dumps(sample, indent=2, sort_keys=True) + "\n", encoding="utf-8")
        if sample["dream_ore_blocks"] == 0 or sample["deepslate_dream_ore_blocks"] == 0:
            print("::warning::A sampled ore type has zero actual voxels. "
                  "Investigate worldgen injection and geological variability before balancing.", flush=True)
        return sample
    finally:
        client.close()

if __name__ == "__main__":
    arg = argparse.ArgumentParser()
    arg.add_argument("--expected-seed", required=True, type=int)
    arg.add_argument("--output", type=Path, required=True)
    args = arg.parse_args()
    run(args.expected_seed, args.output)
