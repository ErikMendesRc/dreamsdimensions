#!/usr/bin/env python3
"""Task 9.0: validate the *design contract*, never claim runtime/Survival approval.

Standard library only. This gate protects naming, dependency DAG, traceability
to implemented registries, and the selected next vertical slice. It must not
be misrepresented as proof that any proposed feature is playable.
"""
from __future__ import annotations

import argparse
from collections import Counter, defaultdict
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CATALOG = Path("docs/OVERWORLD_CONTENT_CATALOG_TASK_9_0.json")
REQUIRED_FLAGS = (
    "no_custom_endgame", "vanilla_progression_compatible",
    "overworld_independent", "no_mandatory_quest", "no_free_dream_return",
    "dream_entry_default_disabled", "no_retro_generation",
    "early_overworld_optional_dreams", "no_one_dimension_hard_dependency",
    "design_does_not_register_content",
)
ALLOWED_STATUS = {"IMPLEMENTED_CODE", "SELECTED", "CANDIDATE"}
ALLOWED_PHASES = {"early": 0, "mid": 1, "late": 2}
REQUIRED_FIELDS = (
    "id", "name", "family", "status", "phase", "task", "origin",
    "action", "cost", "output", "use", "depends_on", "validation",
    "minecraft_relation",
)


def validate(data: object, root: Path | None = None) -> list[str]:
    errors: list[str] = []
    if not isinstance(data, dict):
        return ["invalid_catalog_root"]
    if data.get("schema_version") != "dreamsdimensions.overworld.contract.v1":
        errors.append("invalid_schema")
    if data.get("task") != "9.0" or data.get("scope") != "DESIGN_ONLY":
        errors.append("scope_must_be_design_only")
    principles = data.get("principles")
    if not isinstance(principles, dict):
        principles = {}
        errors.append("missing_principles")
    for key in REQUIRED_FLAGS:
        if principles.get(key) is not True:
            errors.append("principle_must_be_true:" + key)
    if principles.get("dream_dust_origin") != "minecraft:overworld":
        errors.append("dream_dust_must_originate_only_in_overworld")

    base = data.get("baseline")
    if not isinstance(base, dict) or base.get("branch") != "migration/minecraft-26.3":
        errors.append("invalid_baseline")
    elif base.get("manual_survival") != "PENDING":
        errors.append("manual_survival_must_not_be_claimed_as_pass")

    raw = data.get("entries")
    if not isinstance(raw, list) or not raw:
        return errors + ["entries_missing"]
    ids: set[str] = set()
    nodes: dict[str, dict] = {}
    counts: Counter = Counter()
    families: Counter = Counter()
    for index, item in enumerate(raw):
        if not isinstance(item, dict):
            errors.append(f"invalid_entry:{index}")
            continue
        uid = item.get("id")
        if not isinstance(uid, str) or not uid:
            errors.append(f"missing_id:{index}")
            continue
        if uid in ids:
            errors.append("duplicate_id:" + uid)
        ids.add(uid)
        nodes[uid] = item
        for key in REQUIRED_FIELDS:
            v = item.get(key)
            if v is None or v == "" or (key != "depends_on" and v == []):
                errors.append(f"missing_field:{uid}:{key}")
        state = item.get("status")
        counts[state] += 1
        families[item.get("family")] += 1
        if state not in ALLOWED_STATUS:
            errors.append("invalid_status:" + uid)
        if item.get("phase") not in ALLOWED_PHASES:
            errors.append("invalid_phase:" + uid)
        if item.get("origin") != "minecraft:overworld":
            errors.append("non_overworld_origin:" + uid)
        deps = item.get("depends_on")
        if not isinstance(deps, list):
            errors.append("dependencies_not_list:" + uid)
        elif len(deps) != len(set(str(x) for x in deps)):
            errors.append("duplicate_dependency:" + uid)
        alternatives = item.get("source_alternatives", [])
        if not isinstance(alternatives, list):
            errors.append("invalid_alternatives:" + uid)
        if state == "IMPLEMENTED_CODE":
            if not isinstance(item.get("code_anchor"), str) or not item.get("code_anchor"):
                errors.append("missing_code_anchor:" + uid)
            if not isinstance(item.get("registry_id"), str) or not item.get("registry_id"):
                errors.append("missing_registry_id:" + uid)
            if root is not None and isinstance(item.get("code_anchor"), str):
                anchor = (root / item["code_anchor"]).resolve()
                if not anchor.is_relative_to(root.resolve()) or not anchor.is_file():
                    errors.append("unresolved_anchor:" + uid)
                elif '"' + str(item.get("registry_id")) + '"' not in anchor.read_text(encoding="utf-8"):
                    errors.append("registry_id_not_in_anchor:" + uid)
        elif item.get("registry_id") is not None or item.get("code_anchor"):
            errors.append("proposed_feature_must_not_claim_registry:" + uid)

    if counts["IMPLEMENTED_CODE"] < 9:
        errors.append("insufficient_code_baseline")
    if counts["SELECTED"] != 3:
        errors.append("selected_slice_must_have_three_items")
    if counts["CANDIDATE"] < 20 or len(families) < 8:
        errors.append("insufficient_breadth")

    for uid, item in nodes.items():
        deps = item.get("depends_on")
        if not isinstance(deps, list):
            continue
        for dep in deps:
            if dep not in nodes:
                errors.append(f"missing_dependency:{uid}:{dep}")
            elif dep == uid:
                errors.append(f"self_dependency:{uid}")
            elif (item.get("phase") in ALLOWED_PHASES
                  and nodes[dep].get("phase") in ALLOWED_PHASES
                  and ALLOWED_PHASES[nodes[dep]["phase"]] > ALLOWED_PHASES[item["phase"]]):
                errors.append(f"phase_inversion:{uid}:{dep}")
        for alt in item.get("source_alternatives", []):
            if alt not in nodes:
                errors.append(f"missing_alternative:{uid}:{alt}")
    visited: set[str] = set()
    visiting: set[str] = set()
    def visit(uid: str) -> None:
        if uid in visiting:
            errors.append("cyclic_dependency:" + uid)
            return
        if uid in visited:
            return
        visiting.add(uid)
        for dep in nodes[uid].get("depends_on", []):
            if dep in nodes:
                visit(dep)
        visiting.remove(uid)
        visited.add(uid)
    for uid in nodes:
        visit(uid)

    if not {"E01", "E02"}.issubset(set(nodes.get("E03", {}).get("source_alternatives", []))):
        errors.append("dust_must_have_both_overworld_ore_sources")
    if nodes.get("E03", {}).get("depends_on"):
        errors.append("dust_ores_are_alternatives_not_mandatory_combined_inputs")
    if "E09" not in nodes.get("E05", {}).get("depends_on", []):
        errors.append("awakener_requires_core")

    selected = data.get("selected_slice")
    if not isinstance(selected, dict):
        selected = {}
        errors.append("selected_slice_missing")
    selected_ids = selected.get("entry_ids", [])
    if (selected.get("id") != "F1" or selected.get("task") != "9.1" or
            not isinstance(selected_ids, list) or
            set(selected_ids) != {"F1_LICHEN", "F1_MORTAR", "F1_MASONRY"}):
        errors.append("invalid_first_vertical_slice")
    actual_selected = {k for k, v in nodes.items() if v.get("status") == "SELECTED"}
    if isinstance(selected_ids, list) and set(selected_ids) != actual_selected:
        errors.append("selection_status_mismatch")
    if selected.get("decision") != "SELECTED_FOR_NEXT_TASK_NOT_IMPLEMENTED":
        errors.append("must_not_claim_selected_slice_implemented")
    if not {"STATIC_CHECK", "RUNTIME_OBSERVED", "MANUAL_PENDING"}.issubset(
        set(selected.get("required_evidence", []))
    ):
        errors.append("missing_evidence_separation")
    expected_edges = {"F1_LICHEN": "F1_MORTAR", "F1_MORTAR": "F1_MASONRY"}
    for a, consumer in expected_edges.items():
        if a not in nodes.get(consumer, {}).get("depends_on", []):
            errors.append(f"missing_selected_consumer:{a}:{consumer}")
    if "E06" not in nodes.get("F1_MASONRY", {}).get("depends_on", []):
        errors.append("masonry_needs_existing_geology")
    for uid in actual_selected:
        if nodes[uid].get("task") != "9.1" or nodes[uid].get("phase") != "early":
            errors.append("selection_outside_9_1_early:" + uid)
    if root is not None:
        for file in ("docs/TASK_9_0_OVERWORLD_GAMEPLAY_CONTRACT.md",
                     "docs/GAMEPLAY_E_DIRECAO.md", "docs/ESTADO_ATUAL.md"):
            if not (root / file).is_file():
                errors.append("missing_contract_doc:" + file)
    return sorted(set(errors))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=ROOT)
    parser.add_argument("--catalog", type=Path, default=CATALOG)
    parser.add_argument("--report", type=Path)
    args = parser.parse_args()
    root = args.root.resolve()
    source = args.catalog if args.catalog.is_absolute() else root / args.catalog
    try:
        data = json.loads(source.read_text(encoding="utf-8"))
        errors = validate(data, root)
    except (OSError, json.JSONDecodeError, ValueError) as exc:
        errors = ["read_error:" + str(exc)]
        data = {}
    rows = data.get("entries", []) if isinstance(data, dict) else []
    counts = dict(Counter(x.get("status") for x in rows if isinstance(x, dict)))
    report = {
        "schema_version": "dreamsdimensions.qa.v1",
        "test_id": "task_9_0_overworld_design_contract",
        "category": "CONTENT",
        "evidence_type": "STATIC_CHECK",
        "status": "PASS" if not errors else "FAIL",
        "design_status": "DESIGN_ONLY_NOT_PLAYABLE",
        "entry_count": len(rows),
        "status_counts": counts,
        "errors": errors,
        "notes": [
            "This validates design consistency and code anchors, not runtime worldgen.",
            "F1 is selected for Task 9.1 but not implemented in this task.",
            "Survival, rarity, exploration and multiplayer gates remain pending.",
        ],
    }
    if args.report:
        outfile = args.report if args.report.is_absolute() else root / args.report
        outfile.parent.mkdir(parents=True, exist_ok=True)
        outfile.write_text(json.dumps(report, indent=2, ensure_ascii=False) + "\n",
                           encoding="utf-8")
    print(json.dumps(report, indent=2, ensure_ascii=False))
    return 0 if not errors else 1


if __name__ == "__main__":
    raise SystemExit(main())
