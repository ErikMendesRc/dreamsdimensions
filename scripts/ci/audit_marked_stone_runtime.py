#!/usr/bin/env python3
"""Native block-identity probe on a disposable CI server, never natural worldgen."""
import argparse
import json
import re
import time
from pathlib import Path
from check_dreamscape_homologation import Rcon

BLOCK = "dreamsdimensions:marked_stone"
COORD = (0, 300, 0)


def run(seed, output):
    client = Rcon()
    forced = False
    try:
        client.auth()
        actual = client.command("seed")
        if seed not in [int(x) for x in re.findall(r"-?\d+", actual)]:
            raise AssertionError(f"Unexpected seed {actual!r}, expected {seed}")
        response = client.command("execute in minecraft:overworld run forceload add 0 0")
        if not response.strip():
            raise AssertionError("forceload returned no response")
        forced = True
        x, y, z = COORD
        placement = ""
        confirmation = ""
        for attempt in range(10):
            placement = client.command(
                f"execute in minecraft:overworld run setblock {x} {y} {z} {BLOCK}")
            confirmation = client.command(
                f"execute in minecraft:overworld if block {x} {y} {z} {BLOCK} run seed")
            if seed in [int(v) for v in re.findall(r"-?\d+", confirmation)]:
                break
            time.sleep(1)
        else:
            raise AssertionError(f"Marked Stone not observed after native setblock: "
                                 f"placement={placement!r}, confirmation={confirmation!r}")
        report = {
            "schema_version": "dreamsdimensions.qa.v1", "run_id": f"ci-seed-{seed}",
            "test_id": "task_7_3_marked_stone_native_placement",
            "category": "CONTENT", "status": "PASS",
            "evidence_type": "RUNTIME_OBSERVED", "seed": seed,
            "dimension": "minecraft:overworld", "block_id": BLOCK,
            "verified": ["native_setblock", "native_if_block", "seed_confirmed"],
            "limitations": [
                "Disposable CI setblock is not natural worldgen",
                "Actual Survival drops, wrong-tier tools and client rendering NOT_RUN",
                "One test chunk forced by CI harness and explicitly released",
            ],
        }
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(json.dumps(report, indent=2, sort_keys=True) + "\n",
                          encoding="utf-8")
        print("TASK 7.3 MARKED_STONE RUNTIME PASS:", json.dumps(report, sort_keys=True),
              flush=True)
        return report
    finally:
        try:
            if forced:
                client.command(f"execute in minecraft:overworld run setblock "
                               f"{COORD[0]} {COORD[1]} {COORD[2]} minecraft:air")
                client.command("execute in minecraft:overworld run forceload remove 0 0")
        finally:
            client.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--expected-seed", type=int, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    run(args.expected_seed, args.output)
