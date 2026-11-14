#!/usr/bin/env python3
"""Task 7.1: honest, versioned aggregation of existing actual-world CI evidence.

Never converts static tests, /locate-only probes, or missing manual playtests
into claims of verified gameplay. Uses Python stdlib, no external telemetry.
"""
import argparse
import json
from pathlib import Path

SCHEMA = "dreamsdimensions.qa.v1"
VALID_STATUS = {"PASS", "FAIL", "WARN", "SKIPPED", "NOT_RUN"}
EVIDENCE = {"STATIC_CHECK", "UNIT_TEST", "RUNTIME_OBSERVED", "GAMETEST",
            "MANUAL_VERIFIED", "PERFORMANCE_MEASURED", "MODEL_ESTIMATE"}


def load(path: Path) -> dict:
    value = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(value, dict):
        raise ValueError(f"Expected JSON object in {path}")
    return value


def validate_runtime(snapshot: dict, seed: int):
    if snapshot.get("schema_version") != SCHEMA:
        raise ValueError("Invalid QA schema")
    if snapshot.get("run_id") != f"ci-seed-{seed}":
        raise ValueError("QA run/seed mismatch")
    if snapshot.get("enabled") is not True:
        raise ValueError("Runtime QA was not active in the test server")
    events = snapshot.get("events")
    counters = snapshot.get("counters")
    if not isinstance(events, list) or not isinstance(counters, dict):
        raise ValueError("Missing QA events or counters")
    if snapshot.get("buffered_events") != len(events):
        raise ValueError("QA buffered_events does not match event array")
    if type(snapshot.get("dropped_events")) is not int or snapshot["dropped_events"] < 0:
        raise ValueError("Invalid dropped event counter")
    for key, value in counters.items():
        if not isinstance(key, str) or type(value) is not int or value < 0:
            raise ValueError("Invalid QA counter")
    for event in events:
        if not isinstance(event, dict) or event.get("schema_version") != SCHEMA:
            raise ValueError("Bad QA event")
        if event.get("run_id") != snapshot["run_id"]:
            raise ValueError("Mixed QA run IDs")
        if event.get("status") not in VALID_STATUS:
            raise ValueError("Bad QA event state")
        if event.get("evidence_type") not in EVIDENCE:
            raise ValueError("Bad QA evidence type")
        if "player_uuid" in json.dumps(event).lower():
            raise ValueError("Personal identifiers forbidden in QA artifacts")


def result(test_id, category, status, evidence, details):
    if status not in VALID_STATUS or evidence not in EVIDENCE:
        raise ValueError("Invalid QA result classification")
    return {"test_id": test_id, "category": category, "status": status,
            "evidence_type": evidence, "details": details}


def build(seed: int, snapshot: dict, ore: dict, ruins: dict) -> dict:
    validate_runtime(snapshot, seed)
    if ore.get("seed") != seed or ore.get("source") != "ACTUAL_GENERATED_CHUNKS_WITH_RCON_AUDIT":
        raise ValueError("Ore evidence does not match seed or source")
    if ore.get("chunks") != 256 or len(ore.get("regions", [])) != 4:
        raise ValueError("Incomplete 256-chunk ore sample")
    if ruins.get("seed") != seed or ruins.get("source") != "REAL_NATIVE_STRUCTURE_LOCATE_AND_LOADED_CHEST_READ":
        raise ValueError("Ruins evidence does not match seed or source")
    details = ruins.get("details")
    if not isinstance(details, list) or len(details) != 2:
        raise ValueError("Both natural ruin variants must be audited")
    for variant in ("blue", "fragmented"):
        matching = [r for r in details if r.get("variant") == variant]
        if len(matching) != 1 or matching[0].get("status") != "OBSERVED" \
                or matching[0].get("chest_count") != 1 \
                or matching[0].get("loot_matches") != 1 \
                or matching[0].get("stable_chests") != 1:
            raise ValueError(f"Natural ruin {variant} missing or unsafe")
    results = [
        result("overworld_ore_256_chunks", "RESOURCE", "PASS", "RUNTIME_OBSERVED",
               {"chunks": 256, "dream_ore_blocks": ore["dream_ore_blocks"],
                "deepslate_dream_ore_blocks": ore["deepslate_dream_ore_blocks"]}),
        result("natural_recovery_ruins", "STRUCTURE", "PASS", "RUNTIME_OBSERVED",
               {"verified_chests": 2, "tested_dimensions": 2,
                "scope": "located natural chest, pending loot table and local support"}),
        result("dreamqa_command_snapshot", "PERFORMANCE", "PASS", "RUNTIME_OBSERVED",
               {"buffered_events": snapshot["buffered_events"],
                "dropped_events": snapshot["dropped_events"]}),
        result("survival_route_and_awakener", "RECOVERY", "NOT_RUN", "MANUAL_VERIFIED",
               {"reason": "Survival path, open chest and confirmed return require in-game test"}),
        result("multiplayer_and_restart", "PERSISTENCE", "NOT_RUN", "MANUAL_VERIFIED",
               {"reason": "Two-player and restart validation not performed"}),
        result("client_rendering", "CLIENT", "NOT_RUN", "MANUAL_VERIFIED",
               {"reason": "Dedicated server cannot validate rendering or animations"}),
    ]
    return {
        "schema_version": SCHEMA, "run_id": f"ci-seed-{seed}", "seed": seed,
        "results": results, "runtime_snapshot": snapshot,
        "summary": {k: sum(r["status"] == k for r in results) for k in sorted(VALID_STATUS)},
        "limitations": [
            "Runtime telemetry only covers instrumented paths actually exercised in this CI run",
            "RCON response times are not TPS, MSPT or actual chunk generation durations",
            "Worldgen sampling is finite and not a global abundance estimate",
            "Structure support probes do not establish an accessible Survival route",
            "Manual QA and client graphics are explicitly NOT_RUN",
        ],
    }


def aggregate(reports: list[dict]) -> dict:
    seeds = [r["seed"] for r in reports]
    if len(seeds) != len(set(seeds)) or set(seeds) != {14011, 20261108, -904732, 73199013}:
        raise ValueError(f"Expected four distinct configured seeds, found {seeds}")
    for report in reports:
        if report.get("schema_version") != SCHEMA:
            raise ValueError("Invalid per-seed schema")
        if any(r["status"] == "FAIL" for r in report.get("results", [])):
            raise ValueError(f"Failing QA result in seed {report['seed']}")
    return {"schema_version": SCHEMA, "seeds": sorted(seeds),
            "seed_reports": [{"seed": r["seed"], "summary": r["summary"]}
                             for r in sorted(reports, key=lambda item: item["seed"])],
            "manual_qa_complete": False,
            "limitations": ["CI success is not approval of Survival, multiplayer, client rendering or old-save migration"]}


def write(path: Path, obj: dict):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, indent=2, ensure_ascii=False, sort_keys=True) + "\n",
                    encoding="utf-8")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--seed", type=int)
    parser.add_argument("--runtime", type=Path)
    parser.add_argument("--ore", type=Path)
    parser.add_argument("--ruins", type=Path)
    parser.add_argument("--input-dir", type=Path)
    parser.add_argument("--out", type=Path, required=True)
    args = parser.parse_args()
    if args.input_dir is not None:
        inputs = sorted(args.input_dir.rglob("qa-report-seed-*.json"))
        if len(inputs) != 4:
            raise ValueError(f"Expected exactly 4 QA reports, got {len(inputs)}")
        data = aggregate([load(path) for path in inputs])
    else:
        if args.seed is None or any(p is None for p in (args.runtime, args.ore, args.ruins)):
            parser.error("Need --seed, --runtime, --ore and --ruins")
        data = build(args.seed, load(args.runtime), load(args.ore), load(args.ruins))
    write(args.out, data)
    print("DREAM_QA_REPORT_PASS", args.out, json.dumps(data.get("summary", data.get("seeds"))),
          flush=True)


if __name__ == "__main__":
    main()
