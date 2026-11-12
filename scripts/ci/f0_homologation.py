#!/usr/bin/env python3
"""Task 7.5 — F0 economy / compatibility / manual evidence contract.

All automatic checks are bounded and read-only. Never use a static assertion,
an RCON setblock test, or an air-exposed voxel count as proof that a player
found, mined or crafted anything in Survival.
"""
from __future__ import annotations

import argparse
from collections import defaultdict
import json
import math
from pathlib import Path
from typing import Any

from aggregate_marked_stone_worldgen import aggregate as aggregate_f0
from check_marked_stone_7_4 import inspect as inspect_f0

SCHEMA = "dreamsdimensions.qa.v1"
SEEDS = frozenset((14011, 20261108, -904732, 73199013))
MOD = "dreamsdimensions"
# Actual per-seed Task 5.6 baseline, measured in the same four fixed 8x8
# Overworld regions. Record the provenance and allow only a modest tolerance,
# because an independent material must not silently displace Dream Ore.
ORE_BASELINE = {
    14011: (1455, 1355),
    20261108: (1467, 1404),
    -904732: (1445, 1187),
    73199013: (1536, 1387),
}
ORE_RELATIVE_TOLERANCE = 0.10

MANUAL_CASES = (
    "discovery_in_new_chunks_without_commands",
    "stone_pickaxe_drops_one",
    "wooden_pickaxe_drops_zero",
    "iron_pickaxe_drops_one",
    "fortune_silk_no_multiplication",
    "stonecutter_gui_one_to_one",
    "texture_tiling_inventory_and_lighting",
    "small_build_64_blocks",
    "large_build_256_blocks",
    "save_reload_new_world",
    "existing_save_no_retro_gen",
    "two_players_no_desync_dupes",
    "no_accidental_overworld_anomaly_claim",
)
MANUAL_STATUS = ("PASS", "FAIL", "NOT_RUN")


class HomologationError(ValueError):
    pass


def ensure(ok: Any, message: str) -> None:
    if not ok:
        raise HomologationError(message)


def load(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def static_report(root: Path) -> dict:
    base = inspect_f0(root)
    ensure(base["status"] == "PASS", "F0 static source/asset contract must pass")
    recipe_dir = root / "src/main/resources/data/dreamsdimensions/recipe"
    recipe_files = sorted(recipe_dir.glob("*.json"))
    recipes = [load(path) for path in recipe_files]
    # Entire registered craft graph: detect cycles across ALL mod recipe
    # outputs, not only the newly registered stonecutter, even with shared
    # output IDs from vanilla furnace/blasting alternatives.
    dependencies: dict[str, set[str]] = defaultdict(set)
    for recipe in recipes:
        output = recipe.get("result", {}).get("id")
        ensure(isinstance(output, str) and output.startswith(MOD + ":"),
               "Every mod recipe result must be a mod item in this known family")
        ingredients = []
        if "ingredient" in recipe:
            ingredients.append(recipe["ingredient"])
        ingredients.extend(recipe.get("ingredients", []))
        ingredients.extend(recipe.get("key", {}).values())
        for ingredient in ingredients:
            dependencies[output].update(_ingredients(ingredient))
    for name in tuple(dependencies):
        dependencies[name] = {v for v in dependencies[name] if v.startswith(MOD + ":")}

    visiting: set[str] = set()
    visited: set[str] = set()

    def walk(identifier: str) -> None:
        if identifier in visiting:
            raise HomologationError(f"Cyclic recipe dependency: {identifier}")
        if identifier in visited:
            return
        visiting.add(identifier)
        for dep in dependencies.get(identifier, ()):
            if dep in dependencies:
                walk(dep)
        visiting.remove(identifier)
        visited.add(identifier)

    for name in dependencies:
        walk(name)

    raw = f"{MOD}:marked_stone"
    cut = f"{MOD}:cut_marked_stone"
    ensure(dependencies[cut] == {raw},
           f"Cut Marked Stone must depend on raw F0 only, got {dependencies[cut]}")
    ensure(raw not in dependencies, "New F0 material must have natural origin, not a craft shortcut")
    ensure(f"{MOD}:dream_dust" not in dependencies[cut],
           "F0 must not spend Dream Dust")
    # All F0 loot must be exactly the matching block with no XP/bonus.
    loot_dir = root / "src/main/resources/data/dreamsdimensions/loot_table/blocks"
    for block in ("marked_stone", "cut_marked_stone"):
        item = f"{MOD}:{block}"
        table = load(loot_dir / f"{block}.json")
        ensure(len(table.get("pools", [])) == 1, f"Invalid F0 loot pools for {block}")
        entries = table["pools"][0].get("entries", [])
        ensure(len(entries) == 1 and entries[0].get("name") == item,
               f"F0 {block} must return only itself")
        ensure("dream_dust" not in json.dumps(table),
               f"F0 {block} must never drop dream_dust")
    return {
        "schema_version": SCHEMA,
        "test_id": "task_7_5_f0_static_regression",
        "category": "CONTENT",
        "status": "PASS",
        "evidence_type": "STATIC_CHECK",
        "recipes_checked": len(recipes),
        "recipe_cycle_free": True,
        "f0_conversion": {"input": raw, "input_count": 1,
                          "output": cut, "output_count": 1},
        "construction_reference": {
            "small_build_blocks": 64,
            "small_build_input_marked_stone": 64,
            "large_build_blocks": 256,
            "large_build_input_marked_stone": 256,
            "note": "These are material requirements, not measured harvesting times."
        },
        "limitations": [
            "Recipe graph/static loot are not actual Survival mining/crafting",
            "A real player's route, harvest rate, texture quality and persistence need manual evidence",
        ],
    }


def _ingredients(value: Any) -> set[str]:
    if isinstance(value, str):
        return set() if value.startswith("#") else {value}
    if isinstance(value, dict):
        if "id" in value:
            return {value["id"]}
        if "item" in value:
            return {value["item"]}
        if "tag" in value:
            return set()
        raise HomologationError(f"Unknown recipe ingredient: {value}")
    if isinstance(value, list):
        result: set[str] = set()
        for item in value:
            result.update(_ingredients(item))
        return result
    raise HomologationError(f"Invalid recipe ingredient: {value}")


def collect_kind(directory: Path, glob_pattern: str, kind: str) -> dict[int, dict]:
    files = sorted(directory.glob(glob_pattern))
    ensure(len(files) == 4, f"{kind}: require exactly four distinct evidence files, got {len(files)}")
    evidence: dict[int, dict] = {}
    for file in files:
        item = load(file)
        seed = item.get("seed")
        ensure(isinstance(seed, int) and seed in SEEDS, f"{kind}: unexpected seed in {file}")
        ensure(seed not in evidence, f"{kind}: duplicate seed {seed}")
        evidence[seed] = item
    ensure(set(evidence) == SEEDS, f"{kind}: not all four benchmark seeds covered")
    return evidence


def ore_regressions(evidence: dict[int, dict]) -> dict:
    details = []
    total_current = [0, 0]
    total_baseline = [0, 0]
    for seed in sorted(SEEDS):
        item = evidence[seed]
        ensure(item.get("source") == "ACTUAL_GENERATED_CHUNKS_WITH_RCON_AUDIT",
               f"Ore seed {seed}: must be observed, not modeled")
        ensure(item.get("world") == "minecraft:overworld",
               f"Ore seed {seed}: not the vanilla Overworld")
        ensure(item.get("chunks") == 256 and len(item.get("regions", [])) == 4,
               f"Ore seed {seed}: wrong scan coverage")
        actual = (item.get("dream_ore_blocks"), item.get("deepslate_dream_ore_blocks"))
        expected = ORE_BASELINE[seed]
        ensure(all(isinstance(v, int) and v >= 0 for v in actual),
               f"Ore seed {seed}: missing measured voxel counts")
        deviations = []
        for index, typ in enumerate(("dream_ore", "deepslate_dream_ore")):
            diff = (actual[index] - expected[index]) / expected[index]
            deviations.append(round(diff, 6))
            ensure(abs(diff) <= ORE_RELATIVE_TOLERANCE,
                   f"F0 displaced {typ} on seed {seed}: "
                   f"actual={actual[index]}, baseline={expected[index]}, deviation={diff:.2%}")
            total_current[index] += actual[index]
            total_baseline[index] += expected[index]
        details.append({
            "seed": seed,
            "baseline_dream_ore": expected[0],
            "actual_dream_ore": actual[0],
            "baseline_deepslate_dream_ore": expected[1],
            "actual_deepslate_dream_ore": actual[1],
            "relative_deviations": deviations,
        })
    return {
        "status": "PASS",
        "sampled_chunks": 1024,
        "current_dream_ore": total_current[0],
        "current_deepslate_dream_ore": total_current[1],
        "baseline_dream_ore": total_baseline[0],
        "baseline_deepslate_dream_ore": total_baseline[1],
        "max_abs_relative_deviation": ORE_RELATIVE_TOLERANCE,
        "by_seed": details,
        "baseline_source": "Task 5.6 fixed 4 seeds x 256 chunks, same 8x8 survey coordinates",
    }


def ruins_regressions(evidence: dict[int, dict]) -> dict:
    details = []
    for seed in sorted(SEEDS):
        item = evidence[seed]
        ensure(item.get("source") == "REAL_NATIVE_STRUCTURE_LOCATE_AND_LOADED_CHEST_READ",
               f"Ruins seed {seed}: expected real native structure evidence")
        types = {r.get("variant"): r for r in item.get("details", [])}
        ensure(set(types) == {"blue", "fragmented"},
               f"Ruins seed {seed}: require two natural dimension ruins")
        for name, record in types.items():
            ensure(record.get("status") == "OBSERVED" and
                   record.get("chest_count") == 1 and
                   record.get("loot_matches") == 1 and
                   record.get("stable_chests") == 1,
                   f"Ruins regression on seed {seed}, type {name}: {record}")
        details.append({"seed": seed, "natural_ruin_types": sorted(types)})
    return {
        "status": "PASS",
        "seed_reports": details,
        "tested_natural_ruins": 8,
        "limitations": "This does not establish a traversable Survival path or loot opened by a player.",
    }


def manual_template() -> dict:
    return {
        "schema_version": SCHEMA,
        "test_id": "task_7_5_f0_manual_homologation",
        "status": "NOT_RUN",
        "evidence_type": "MANUAL_VERIFIED",
        "source": "USER_SURVIVAL_SESSIONS_ONLY",
        "cases": {key: {"status": "NOT_RUN", "evidence": ""} for key in MANUAL_CASES},
        "discovery_sessions": [],
        "notes": "No human playtest evidence was provided. Do not infer PASS from server CI."
    }


def _percentiles_km(sessions: list[dict]) -> dict:
    """Kaplan-Meier quantiles when the right-censored sample supports them."""
    if not sessions:
        return {"n": 0, "observed": 0, "censored": 0, "p50_minutes": None,
                "p90_minutes": None, "status": "NOT_RUN"}
    events: dict[float, list[bool]] = defaultdict(list)
    for session in sessions:
        t = session.get("duration_minutes")
        ensure(isinstance(t, (float, int)) and not isinstance(t, bool)
               and math.isfinite(t) and 0 < t <= 600,
               "Manual session time must be 0–600 finite minutes")
        ensure(isinstance(session.get("found"), bool),
               "Discovery session must explicitly state found true/false")
        ensure(session.get("environment") == "eligible_geology_overworld",
               "Do not count non-eligible habitats against the discoverability goal")
        ensure(isinstance(session.get("id"), str) and session["id"].strip(),
               "Each manual session must have a nonempty ID")
        events[float(t)].append(session["found"])
    ids = [row["id"] for row in sessions]
    ensure(len(set(ids)) == len(ids), "Duplicate manual discovery session ID")
    remaining = len(sessions)
    survival = 1.0
    p50 = p90 = None
    for t in sorted(events):
        found = sum(events[t])
        censor = len(events[t]) - found
        if remaining:
            survival *= 1 - found / remaining
        if p50 is None and survival <= 0.5:
            p50 = t
        if p90 is None and survival <= 0.1:
            p90 = t
        remaining -= found + censor
    observed = sum(r["found"] for r in sessions)
    return {
        "n": len(sessions), "observed": observed, "censored": len(sessions) - observed,
        # Require >= 10 independent eligible sessions before treating quantiles as
        # evaluable. Never substitute percentiles of only successful finds.
        "p50_minutes": p50 if len(sessions) >= 10 else None,
        "p90_minutes": p90 if len(sessions) >= 10 else None,
        "status": "EVALUABLE" if len(sessions) >= 10 else "INSUFFICIENT_SAMPLE",
        "method": "Kaplan-Meier right-censored time-to-first-natural-discovery",
    }


def manual_status(evidence: dict) -> dict:
    ensure(evidence.get("schema_version") == SCHEMA, "Wrong manual evidence schema")
    ensure(evidence.get("source") == "USER_SURVIVAL_SESSIONS_ONLY",
           "Manual evidence source must be explicit")
    cases = evidence.get("cases", {})
    ensure(set(cases) == set(MANUAL_CASES),
           "Manual cases must contain all F0 acceptance gates, no missing/extra cases")
    counts = {k: 0 for k in MANUAL_STATUS}
    for case, result in cases.items():
        status = result.get("status")
        ensure(status in MANUAL_STATUS, f"{case}: status not supported")
        if status in ("PASS", "FAIL"):
            ensure(isinstance(result.get("evidence"), str) and
                   result["evidence"].strip(),
                   f"{case}: cannot claim {status} without a screenshot/log/notes reference")
        counts[status] += 1
    discovery = _percentiles_km(evidence.get("discovery_sessions", []))
    ready = counts["PASS"] == len(MANUAL_CASES) and discovery["n"] >= 10
    # Even with N=10, a censored P90 is not yet established. Do NOT promote it.
    ready = ready and discovery["p50_minutes"] is not None and discovery["p90_minutes"] is not None
    if counts["FAIL"]:
        status = "FAIL"
    elif ready:
        status = "PASS"
    else:
        status = "NOT_RUN"
    return {
        "status": status,
        "evidence_type": "MANUAL_VERIFIED",
        "case_counts": counts,
        "discovery": discovery,
        "target_p50_minutes": [10, 25],
        "target_p90_below_minutes": 60,
        "discovery_target_assessment": (
            "NOT_RUN" if discovery["p50_minutes"] is None or discovery["p90_minutes"] is None
            else "PASS" if (10 <= discovery["p50_minutes"] <= 25
                            and discovery["p90_minutes"] < 60)
            else "REVIEW_BALANCE"
        ),
        "release_note": "CI alone never provides or auto-approves manual Survival evidence.",
    }


def build_aggregate(marked_dir: Path, ore_dir: Path, ruins_dir: Path,
                    manual_evidence: dict | None = None) -> dict:
    marked = collect_kind(marked_dir, "marked-stone-worldgen-seed-*.json", "F0 worldgen")
    ores = collect_kind(ore_dir, "ore-audit-seed-*.json", "Dream Ores")
    ruins = collect_kind(ruins_dir, "dream-ruins-seed-*.json", "Dream Ruins")
    f0 = aggregate_f0(list(marked.values()))
    regression_ore = ore_regressions(ores)
    regression_ruins = ruins_regressions(ruins)
    manual = manual_status(manual_evidence or manual_template())
    return {
        "schema_version": SCHEMA,
        "test_id": "task_7_5_f0_homologation",
        "category": "F0",
        "evidence_type": "RUNTIME_OBSERVED",
        "status": "PASS" if f0["status"] == "PASS" else "WARN",
        "ci_gate": "PASS",
        "survival_gate": manual["status"],
        "full_homologation": "PASS" if manual["status"] == "PASS" and
                              manual["discovery_target_assessment"] == "PASS" else "PENDING",
        "four_seed_worldgen": f0,
        "dream_ore_regression": regression_ore,
        "recovery_ruins_regression": regression_ruins,
        "economic_reference": {
            "marked_to_cut": "1:1",
            "marked_for_64_cut_blocks": 64,
            "marked_for_256_cut_blocks": 256,
            "measured_harvesting_minutes": None,
            "finite_natural_material": True,
            "note": "Worldgen voxels do not equal material a player can collect.",
        },
        "manual": manual,
        "limitations": [
            "CI PASS does NOT mean F0 full Survival homologation",
            "Fixed seeded samples cannot establish real player discovery P50/P90",
            "Air adjacency is not player reachability or actual visual tile QA",
            "Old-save persistence and two-player tests remain separate manual gates",
            "No balance changes are approved solely by aggregated voxel density",
        ],
    }


def write(path: Path, obj: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(obj, ensure_ascii=False, indent=2, sort_keys=True) + "\n",
                    encoding="utf-8")


def main() -> None:
    parser = argparse.ArgumentParser()
    group = parser.add_mutually_exclusive_group(required=True)
    group.add_argument("--static", action="store_true")
    group.add_argument("--aggregate", action="store_true")
    group.add_argument("--manual-template", action="store_true")
    group.add_argument("--manual-review", action="store_true")
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--marked-dir", type=Path)
    parser.add_argument("--ore-dir", type=Path)
    parser.add_argument("--ruins-dir", type=Path)
    parser.add_argument("--manual", type=Path)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    if args.static:
        report = static_report(args.root.resolve())
    elif args.manual_template:
        report = manual_template()
    elif args.manual_review:
        if args.manual is None:
            parser.error("--manual-review requires --manual path/to/manual-results.json")
        results = manual_status(load(args.manual))
        report = {
            "schema_version": SCHEMA,
            "test_id": "task_7_5_f0_manual_review",
            "status": results["status"],
            "evidence_type": "MANUAL_VERIFIED",
            "results": results,
            "note": "Manual evidence was evaluated from user-supplied results, not verified by CI.",
        }
    else:
        if not all((args.marked_dir, args.ore_dir, args.ruins_dir)):
            parser.error("--aggregate requires --marked-dir, --ore-dir and --ruins-dir")
        report = build_aggregate(args.marked_dir, args.ore_dir, args.ruins_dir,
                                 load(args.manual) if args.manual else None)
    write(args.output, report)
    print("TASK_7_5_F0", json.dumps({
        "status": report["status"], "ci_gate": report.get("ci_gate"),
        "survival_gate": report.get("survival_gate"),
        "full_homologation": report.get("full_homologation"),
        "output": str(args.output)}, sort_keys=True), flush=True)


if __name__ == "__main__":
    main()
