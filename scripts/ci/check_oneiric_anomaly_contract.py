#!/usr/bin/env python3
"""Task 7.6 — design + architecture gate, not a gameplay implementation.

This deliberately validates files under docs/contracts, NOT in assets/ or
data/: until Task 7.7 commits actual physical sites and server provenance,
these scenarios must remain inactive. Fail closed on cosmetic-only profiles.
"""
from __future__ import annotations

import argparse
import json
from pathlib import Path
import re
import sys

SCHEMA = "dreamsdimensions.anomalies.v1"
MOD = "dreamsdimensions"
EXPECTED = {
    "BLUE_FIELD": "dreamsdimensions:campo_onirico_azul",
    "DREAMSCAPE": "dreamsdimensions:dreamscape",
}
MANIFEST = "docs/contracts/ONEIRIC_ANOMALIES_V1.json"
SOURCE = "src/main/java/com/dreamsdimensions/mod/anomaly"
VALID_METRICS = {
    "natural_candidates", "placed_sites", "terrain_rejections",
    "first_notice", "independent_corroborations", "interpretation_method",
    "physical_target_confirmed", "resolved_per_player",
    "payoff_duplicate_attempt", "false_clue_without_target",
    "route_accessible", "server_ms_budget",
}
REQUIRED_GATES = {
    "generated_loaded_chunks", "solid_foundation",
    "two_independent_clues_present", "two_viable_routes_to_payoff",
    "no_uncontrolled_fall_or_lava", "payoff_physically_present",
    "neighboring_block_checks_bounded",
}


class ContractError(ValueError):
    pass


def ensure(condition, message):
    if not condition:
        raise ContractError(message)


def load(root: Path) -> dict:
    return json.loads((root / MANIFEST).read_text(encoding="utf-8"))


def meaningful(value, context: str) -> None:
    ensure(isinstance(value, str) and len(value.strip()) >= 28,
           f"{context}: specify a real, readable player-facing consequence or clue")


def inspect(data: dict, root: Path) -> dict:
    ensure(isinstance(data, dict) and data.get("schema_version") == SCHEMA,
           "Versioned anomaly manifest required")
    ensure(data.get("activation") == "INERT_DESIGN_CONTRACT",
           "Task 7.6 is an inert contract; never silently enable a phenomenon")
    ensure(data.get("scope") == "minecraft:overworld", "Sources must begin in Overworld")
    meaningful(data.get("architectural_law"), "architectural_law")
    ensure(data.get("canonical_dimensions") == EXPECTED,
           "Named anomalies must be causally linked to the existing two dreams")

    selector = data.get("selection", {})
    ensure(selector.get("macrocell_side_chunks") == 12,
           "Macrocell policy must match the tested Java candidate selector")
    ensure(selector.get("candidate_cell_ratio") == "1/3 PROPOSAL ONLY",
           "Do not misrepresent candidates as successfully generated anomalies")
    ensure(selector.get("required_origin") == "NATURAL_WORLDGEN",
           "Only authentic worldgen anchors may start an investigation")
    ensure(selector.get("material_dependency") == f"{MOD}:marked_stone",
           "The first anomalies must have a consumer in the implemented F0 family")
    ensure(set(selector.get("terrain_gate", [])) == REQUIRED_GATES,
           "Physical terrain validation and two accessible routes are required")
    ensure(selector.get("on_rejection") == "SKIP_WITH_REASON_NEVER_FAKE_CLUE",
           "No false clues to missing or inaccessible targets")
    ensure(selector.get("distribution_status") == "UNMEASURED_ANOMALY_DENSITY",
           "Do not claim a candidate density equals an observed placed density")

    progress = data.get("progression", {})
    ensure(progress.get("stages") == [
        "HIDDEN", "NOTICED", "CORRELATED", "INTERPRETED", "RESOLVED"
    ], "Five-stage progression must match the tested Java engine")
    ensure(set(progress.get("mandatory_independent_evidence", [])) == {
        "GEOLOGIC_DISCONTINUITY", "AMBIENT_INVERSION_OR_VISUAL_FALLBACK"
    }, "Two independent clues with accessible fallback are mandatory")
    ensure(set(progress.get("interpretation_methods", [])) == {
        "SLEEP_RECALL", "WAKING_RECONCILIATION"
    }, "Sleep must remain optional, never a progression dead-end")
    ensure(progress.get("resolution_requirement") == "SERVER_VERIFIED_PHYSICAL_PAYOFF",
           "The final reward must require a real, server-validated target")
    ensure(progress.get("replay_policy") == "IDEMPOTENT_PER_WORLD_PER_PLAYER_PER_ANCHOR",
           "No repeatable loot or endless reward farming")
    ensure(progress.get("memory_persistence") == "PLANNED_FOR_7_7_NOT_YET_ACTIVE",
           "Do not claim persistent player progress exists in task 7.6")
    ensure(progress.get("true_reveals_only") is True,
           "Every authentic clue must lead to a meaningful real source")

    profiles = data.get("profiles", [])
    ensure(isinstance(profiles, list) and len(profiles) == len(EXPECTED),
           "Two dimension-specific anomaly profiles must be authored")
    ids = set()
    lineages = set()
    for profile in profiles:
        label = profile.get("id", "")
        ensure(re.fullmatch(r"dreamsdimensions:[a-z0-9_/-]+", label) is not None,
               "Each anomaly profile needs a namespaced stable ID")
        ensure(label not in ids, f"Duplicate anomaly profile ID: {label}")
        ids.add(label)
        lineage = profile.get("lineage")
        ensure(lineage in EXPECTED and lineage not in lineages,
               "One distinct mechanism must come from each dream dimension")
        lineages.add(lineage)
        for required in (
            "causality", "physical_origin", "initial_sign",
            "geologic_corroboration", "story_question",
            "gameplay_choice", "accessibility", "quality_gate"
        ):
            meaningful(profile.get(required), f"{label}.{required}")
        alternative = profile.get("alternate_corroborations", {})
        for channel in ("audio", "visual"):
            meaningful(alternative.get(channel), f"{label}.alternate_corroborations.{channel}")
        interpretation = profile.get("interpretation", {})
        for route in ("sleep", "awake"):
            meaningful(interpretation.get(route), f"{label}.interpretation.{route}")
        payoff = profile.get("payoff", {})
        ensure(payoff.get("physical_site") == "WORLDGEN_COMMITTED_SECONDARY_SITE",
               f"{label}: no imaginary or randomly selected target allowed")
        ensure(payoff.get("physical_reward") == "RECOVERABLE_MARKED_STONE_RESERVE",
               f"{label}: must have a recoverable mechanical payoff")
        ensure(isinstance(payoff.get("minimum_material_blocks"), int)
               and 8 <= payoff["minimum_material_blocks"] <= 64,
               f"{label}: material must be sufficient to matter without a free infinite farm")
        ensure(payoff.get("existing_consumer") == f"{MOD}:cut_marked_stone",
               f"{label}: tangible reward needs a real gameplay consumer")
        ensure(payoff.get("knowledge_reward") == (
            "VERIFIABLE_BLUE_FIELD_CORRESPONDENCE" if lineage == "BLUE_FIELD"
            else "VERIFIABLE_DREAMSCAPE_CORRESPONDENCE"
        ), f"{label}: dream correspondence must be meaningful and distinct")
        ensure(payoff.get("motif_dimension") == EXPECTED[lineage],
               f"{label}: mismatch between waking phenomenon and dream lineage")
        ensure(payoff.get("never_requires_dream_entry") is True,
               f"{label}: no forced dream entry, portal or equipment gating")
    ensure(lineages == set(EXPECTED),
           "Both existing dream dimensions must have unique anomaly causal rules")

    guards = data.get("anti_abuse", {})
    required_boolean = [
        "never_scan_overworld_each_tick", "never_force_chunks_in_gameplay",
        "no_anomaly_from_player_block", "no_rng_reward_reroll",
        "no_free_dream_entry", "never_modify_existing_sleep_teleport_gate",
        "server_authoritative_evidence_only", "one_resolution_per_player_per_site",
        "worldgen_only_future_integration", "no_new_content_registry_in_task_7_6",
    ]
    ensure(all(guards.get(key) is True for key in required_boolean),
           "Server authority, safety and performance anti-abuse gates are mandatory")
    ensure(set(data.get("metrics_for_7_7", [])) == VALID_METRICS,
           "Anomaly runtime release metrics must include detection, payoff and false-clue indicators")

    # Guards against accidentally activating design examples as real worldgen
    # or moving them into the dynamic Minecraft registries in task 7.6.
    generated = root / "src/main/resources/data/dreamsdimensions"
    for profile in profiles:
        key = profile["id"].split(":", 1)[1]
        for folder in ("worldgen/feature", "worldgen/placed_feature",
                       "neoforge/biome_modifier", "structure"):
            ensure(not (generated / folder / f"{key}.json").exists(),
                   f"{profile['id']}: should be inactive until Task 7.7")

    for filename in ("OneiricAnomalyContract.java", "AnomalyScenePlanner.java"):
        ensure((root / SOURCE / filename).is_file(),
               f"Core pure-Java contract missing: {filename}")
    tests = root / "src/test/java/com/dreamsdimensions/mod/anomaly/OneiricAnomalyContractTest.java"
    ensure(tests.is_file(), "Task 7.6 JUnit for scenario progression and anti-abuse is mandatory")
    return {
        "schema_version": "dreamsdimensions.qa.v1",
        "test_id": "task_7_6_oneiric_anomaly_contract",
        "category": "GAMEPLAY",
        "status": "PASS",
        "evidence_type": "STATIC_CHECK",
        "activation": "INERT_DESIGN_CONTRACT",
        "profiles": sorted(ids),
        "lineages": sorted(lineages),
        "required_independent_clues_per_profile": 2,
        "mandatory_interpretation_routes_per_profile": 2,
        "required_tangible_and_knowledge_payoffs": 2,
        "limitations": [
            "Design examples and pure-Java policy are NOT natural, playable anomalies",
            "Actual site provenance, terrain/reachability, save persistence and scene placement are Task 7.7",
            "Exact density, player discovery time and real client/audio experience remain NOT_RUN",
            "Marked Stone remains a construction material until physical anomalies are implemented",
        ]
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--report", type=Path)
    args = parser.parse_args()
    try:
        report = inspect(load(args.root), args.root)
    except (ContractError, FileNotFoundError, ValueError, TypeError, AttributeError) as exc:
        report = {
            "schema_version": "dreamsdimensions.qa.v1",
            "test_id": "task_7_6_oneiric_anomaly_contract",
            "status": "FAIL", "category": "GAMEPLAY",
            "evidence_type": "STATIC_CHECK", "error": str(exc),
        }
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(json.dumps(report, indent=2, ensure_ascii=False, sort_keys=True)
                               + "\n", encoding="utf-8")
    if report["status"] != "PASS":
        print("TASK 7.6 ANOMALY CONTRACT FAIL", report["error"], file=sys.stderr)
        return 1
    print("TASK 7.6 ANOMALY CONTRACT STATIC PASS:",
          json.dumps({key: report[key] for key in ("activation", "profiles", "lineages")},
                     sort_keys=True), flush=True)
    print("LIMITATION: actual anomaly worldgen, client effects and Survival investigation NOT_RUN")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
