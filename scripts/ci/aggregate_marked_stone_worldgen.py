#!/usr/bin/env python3
"""Task 7.4: versioned, honest four-seed rollup for actual natural F0 blocks.

An air-exposed face is a useful visibility proxy but not proof of a traversable
route. Manual Survival playtesting remains NOT_RUN in this report.
"""
import argparse
import json
from pathlib import Path
from statistics import median

SCHEMA = "dreamsdimensions.qa.v1"
SEEDS = {14011, 20261108, -904732, 73199013}


def aggregate(records):
    if len(records) != 4 or {r.get("seed") for r in records} != SEEDS:
        raise ValueError("Expected exactly the four fixed distinct seeds, no extras")
    for item in records:
        if item.get("schema_version") != SCHEMA:
            raise ValueError("Schema mismatch")
        if item.get("source") != "REAL_NATURAL_GENERATION_LOADED_CHUNKS":
            raise ValueError("A manually placed or modeled block cannot count as worldgen")
        if item.get("evidence_type") != "RUNTIME_OBSERVED":
            raise ValueError("Not actual runtime evidence")
        if item.get("world") != "minecraft:overworld":
            raise ValueError("Unexpected dimension; must be native Overworld")
        if item.get("status") not in ("PASS", "WARN"):
            raise ValueError("Invalid source report status")
        t = item["totals"]
        if t.get("chunks") != 64 or not 0 < t.get("marked_stone_blocks", 0):
            raise ValueError("Wrong chunk count or no natural Marked Stone")
        if not 0 <= t["air_exposed_marked_stone"] <= t["marked_stone_blocks"]:
            raise ValueError("Impossible exposure metric")
        if len(item.get("regions", [])) != 4:
            raise ValueError("Four distinct region records required")
        if sum(region["chunks"] for region in item["regions"]) != 64:
            raise ValueError("Invalid region chunk coverage")
        if sum(region["marked_stone_blocks"] for region in item["regions"]) != t["marked_stone_blocks"]:
            raise ValueError("Region/total mismatch")
        if sum(region["air_exposed_marked_stone"] for region in item["regions"]) != t["air_exposed_marked_stone"]:
            raise ValueError("Exposed region/total mismatch")

    ordered = sorted(records, key=lambda r: r["seed"])
    chunks = sum(x["totals"]["chunks"] for x in records)
    blocks = sum(x["totals"]["marked_stone_blocks"] for x in records)
    exposed = sum(x["totals"]["air_exposed_marked_stone"] for x in records)
    present_chunks = sum(x["totals"]["chunks_with_marked_stone"] for x in records)
    visible_seeds = sum(x["totals"]["air_exposed_marked_stone"] > 0 for x in records)
    # Four independent seeds should have at least one exposed natural sample.
    if exposed == 0:
        raise ValueError("No exposed Marked Stone across four real-world seeds")
    return {
        "schema_version": SCHEMA,
        "test_id": "task_7_4_four_seed_natural_worldgen",
        "category": "WORLDGEN",
        "status": "PASS" if visible_seeds == 4 else "WARN",
        "evidence_type": "RUNTIME_OBSERVED",
        "source": "AGGREGATED_REAL_NATURAL_GENERATION",
        "seeds": [x["seed"] for x in ordered],
        "totals": {
            "chunks": chunks,
            "chunks_with_marked_stone": present_chunks,
            "marked_stone_blocks": blocks,
            "air_exposed_marked_stone": exposed,
            "seeds_with_exposed": visible_seeds,
            "blocks_per_chunk": round(blocks / chunks, 3),
            "air_exposed_fraction": round(exposed / blocks, 5),
        },
        "seed_reports": [{
            "seed": r["seed"], "status": r["status"],
            **r["totals"],
            "observed_exposed_xyz": [region["first_exposed"] for region in r["regions"]
                                     if region["first_exposed"] != "none"],
        } for r in ordered],
        "manual_qa": {
            "actual_survival_discovery": "NOT_RUN",
            "stonecutter_gui_1_to_1": "NOT_RUN",
            "harvesting_tool_tier": "NOT_RUN",
            "multiplayer_and_persistence": "NOT_RUN",
        },
        "limitations": [
            "A biased fixed 64-chunk sample per seed, NOT a random distribution",
            "Air adjacency is not a measured path to an exposed block",
            "Worldgen migration does not retroactively affect already generated chunks",
            "Actual player discovery percentiles P50/P90 require human playtests",
        ],
    }


def main():
    arg = argparse.ArgumentParser()
    arg.add_argument("--input-dir", type=Path, required=True)
    arg.add_argument("--output", type=Path, required=True)
    args = arg.parse_args()
    paths = sorted(args.input_dir.glob("marked-stone-worldgen-seed-*.json"))
    records = [json.loads(p.read_text(encoding="utf-8")) for p in paths]
    report = aggregate(records)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(report, indent=2, sort_keys=True) + "\n",
                           encoding="utf-8")
    print("TASK_7_4_FOUR_SEEDS", json.dumps(report["totals"], sort_keys=True), flush=True)
    print("TASK_7_4_MANUAL_SURVIVAL NOT_RUN", flush=True)


if __name__ == "__main__":
    main()
