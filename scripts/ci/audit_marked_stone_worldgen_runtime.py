#!/usr/bin/env python3
"""Task 7.4: natural Marked Stone measurement in ACTUAL 26.3 dedicated-server chunks.

Exactly four disjoint 4x4 chunk regions per seed = 64 chunks/seed. Uses an
opt-in read-only admin command. RCON forceload belongs ONLY to disposable CI;
cleanup in finally even on errors. Never substitutes /setblock or /place.
"""
import argparse
import json
import re
import time
from pathlib import Path
from check_dreamscape_homologation import Rcon

AREAS = ((-4, -4), (64, -4), (-68, 64), (4, -68))
SIDE = 4
RX = re.compile(
    r"MARKED_STONE_AUDIT status=OK dimension=minecraft:overworld"
    r" start_chunk=(-?\d+),(-?\d+) side=(\d+)"
    r" chunks=(\d+) chunks_with_marked=(\d+)"
    r" marked_stone=(\d+) exposed_marked_stone=(\d+)"
    r" unknown_exposure=(\d+) y_range=(-?\d+):(-?\d+)"
    r" first_any=([\w,-]+) first_exposed=([\w,-]+)"
)


def fail_if_bad(text, context):
    lower = text.lower()
    if any(word in lower for word in (
        "unknown command", "incorrect argument", "failed to execute",
        "error executing", "not found", "not enabled")):
        raise RuntimeError(f"{context}: {text!r}")


def run(seed: int, output: Path) -> dict:
    client = Rcon()
    records = []
    try:
        client.auth()
        response = client.command("seed")
        all_seed_numbers = [int(v) for v in re.findall(r"-?\d+", response)]
        if seed not in all_seed_numbers:
            raise AssertionError(f"Wrong seed {response!r}; expected {seed}")

        for index, (cx, cz) in enumerate(AREAS):
            x1, z1 = cx * 16, cz * 16
            x2, z2 = (cx + SIDE) * 16 - 1, (cz + SIDE) * 16 - 1
            forced = False
            try:
                resp = client.command(
                    f"execute in minecraft:overworld run forceload add {x1} {z1} {x2} {z2}")
                fail_if_bad(resp, "forceload add")
                if not resp.strip():
                    raise RuntimeError("Empty forceload response")
                forced = True
                report = ""
                for retry in range(12):
                    report = client.command(f"marked_stone_audit {cx} {cz} {SIDE}")
                    if "MARKED_STONE_AUDIT status=OK" in report:
                        break
                    if "MARKED_STONE_AUDIT status=SKIPPED reason=chunk_not_loaded_" not in report:
                        raise AssertionError(f"Natural audit not ready: {report!r}")
                    time.sleep(2)
                match = RX.search(report)
                if not match:
                    raise AssertionError(f"Unparseable real natural F0 audit: {report!r}")
                *values, first_any, first_exposed = match.groups()
                nums = list(map(int, values))
                if nums[:4] != [cx, cz, SIDE, SIDE * SIDE]:
                    raise AssertionError(f"Wrong coordinates/chunks: {nums!r}")
                _, _, _, chunks, with_marked, marked, visible, unknown, y1, y2 = nums
                if not 0 <= with_marked <= chunks:
                    raise AssertionError("Invalid chunk count")
                if not 0 <= visible <= marked:
                    raise AssertionError("Invalid natural exposure count")
                records.append({
                    "area": index, "chunk_x": cx, "chunk_z": cz,
                    "chunks": chunks, "chunks_with_marked_stone": with_marked,
                    "marked_stone_blocks": marked, "air_exposed_marked_stone": visible,
                    "unknown_exposure": unknown, "min_y": y1, "max_y": y2,
                    "first_any": first_any, "first_exposed": first_exposed,
                    "evidence_type": "RUNTIME_OBSERVED",
                })
                print("TASK_7_4_WORLDGEN_REGION", json.dumps(records[-1], sort_keys=True),
                      flush=True)
            finally:
                if forced:
                    resp = client.command(
                        f"execute in minecraft:overworld run forceload remove "
                        f"{x1} {z1} {x2} {z2}")
                    fail_if_bad(resp, "forceload remove")
        totals = {
            "chunks": sum(r["chunks"] for r in records),
            "chunks_with_marked_stone": sum(r["chunks_with_marked_stone"] for r in records),
            "marked_stone_blocks": sum(r["marked_stone_blocks"] for r in records),
            "air_exposed_marked_stone": sum(r["air_exposed_marked_stone"] for r in records),
            "unknown_exposure": sum(r["unknown_exposure"] for r in records)
        }
        if totals["chunks"] != SIDE * SIDE * len(AREAS):
            raise AssertionError(f"Wrong sample size: {totals}")
        # Missing generation is an actual failed worldgen contract.
        if totals["marked_stone_blocks"] == 0:
            raise AssertionError(f"No naturally generated marked stone in sampled "
                                 f"64 chunks for seed {seed}: {records}")
        report = {
            "schema_version": "dreamsdimensions.qa.v1",
            "run_id": f"ci-seed-{seed}",
            "test_id": "task_7_4_natural_marked_stone_survey",
            "category": "WORLDGEN",
            "status": "PASS" if totals["air_exposed_marked_stone"] > 0 else "WARN",
            "evidence_type": "RUNTIME_OBSERVED",
            "world": "minecraft:overworld", "seed": seed,
            "source": "REAL_NATURAL_GENERATION_LOADED_CHUNKS",
            "totals": totals, "regions": records,
            "limitations": [
                "Coordinates are observed actual voxel hits, not safe player teleport locations",
                "Air adjacency is not guaranteed reachability, route or time-to-discovery",
                "Fixed regions are a biased sample, not whole-biome representative distribution",
                "Surface vs underground source cannot be distinguished from placed blocks",
                "Manual Survival stonecutting and mining are NOT_RUN",
            ],
        }
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(json.dumps(report, indent=2, sort_keys=True) + "\n",
                          encoding="utf-8")
        print("TASK_7_4_WORLDGEN_RESULT", json.dumps({
            "seed": seed, "status": report["status"], **totals}, sort_keys=True),
            flush=True)
        return report
    finally:
        client.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--expected-seed", type=int, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    run(args.expected_seed, args.output)
