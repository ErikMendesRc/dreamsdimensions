#!/usr/bin/env python3
"""Task 8.7 QA: static CI gate vs human client/Survival gate.

Never turns JUnit, screenshots of a preview, or dedicated-server smoke into
a fictional client playtest. Manual PASS requires an explicit evidence note.
"""
from __future__ import annotations

import argparse
import json
from pathlib import Path
import sys

from audit_visual_assets import audit

ROOT = Path(__file__).resolve().parents[2]
TEMPLATE = Path("scripts/ci/fixtures/visual_release_manual_template.json")
SCHEMA = "dreamsdimensions.visual_manual.v1"
REQUIRED_CASES = (
    "material_tile_day_night",
    "vegetation_alpha",
    "blue_material_cohesion",
    "necklace_inventory",
    "awakener_particles_settings",
    "core_ambient_particle",
    "awakener_survival_return",
    "awakener_cancel_and_cooldown",
    "awakener_bed_fallback",
    "awakener_offhand_creative",
    "awakener_multiplayer",
    "client_performance_and_reload",
)


def validate_manual(manual: dict) -> tuple[str, list[str]]:
    errors: list[str] = []
    if not isinstance(manual, dict) or manual.get("schema_version") != SCHEMA:
        return "FAIL", ["missing_or_wrong_manual_schema"]
    cases = manual.get("cases", [])
    if not isinstance(cases, list):
        return "FAIL", ["cases_must_be_array"]
    names = [row.get("id") for row in cases if isinstance(row, dict)]
    if len(names) != len(REQUIRED_CASES) or len(set(names)) != len(names) \
            or set(names) != set(REQUIRED_CASES):
        errors.append("exact_manual_case_set_required")
    by_id = {row.get("id"): row for row in cases if isinstance(row, dict)}
    status = "PASS"
    for key in REQUIRED_CASES:
        row = by_id.get(key, {})
        result = row.get("status", "NOT_RUN")
        evidence = row.get("evidence", [])
        if result not in ("PASS", "FAIL", "NOT_RUN"):
            errors.append(f"invalid_case_status:{key}")
        if not isinstance(evidence, list) or not all(
                isinstance(ref, str) and len(ref.strip()) >= 8 for ref in evidence):
            errors.append(f"invalid_evidence_format:{key}")
        if result in ("PASS", "FAIL") and not evidence:
            errors.append(f"evidence_required:{key}")
        if result == "FAIL":
            status = "FAIL"
        elif result == "NOT_RUN" and status != "FAIL":
            status = "NOT_RUN"
    if errors:
        status = "FAIL"
    return status, errors


def build(root: Path, manual: dict) -> dict:
    visual = audit(root)
    manual_gate, errors = validate_manual(manual)
    ci_gate = "PASS" if visual["status"] == "PASS" else "FAIL"
    full = "PASS" if ci_gate == "PASS" and manual_gate == "PASS" else (
        "FAIL" if ci_gate == "FAIL" or manual_gate == "FAIL" else "PENDING")
    return {
        "schema_version": "dreamsdimensions.qa.v1",
        "test_id": "task_8_7_visual_release_homologation",
        "category": "CONTENT",
        "evidence_type": "STATIC_CHECK",
        "ci_gate": ci_gate,
        "manual_gate": manual_gate,
        "full_homologation": full,
        "status": "FAIL" if ci_gate == "FAIL" or errors else "PASS",
        "cases_required": list(REQUIRED_CASES),
        "manual_errors": errors,
        "assets": visual["counts"],
        "limitations": [
            "CI cannot perform subjective pixel rendering approval",
            "No player or two-client Survival return is simulated by server smoke",
            "Manual gate remains NOT_RUN until evidence is supplied and reviewed",
        ],
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=ROOT)
    parser.add_argument("--manual", type=Path)
    parser.add_argument("--report", type=Path)
    args = parser.parse_args()
    manual = args.manual or args.root / TEMPLATE
    try:
        document = json.loads(manual.read_text(encoding="utf-8"))
        result = build(args.root.resolve(), document)
    except (ValueError, OSError, TypeError) as exc:
        print(f"VISUAL RELEASE GATE FAIL: {exc}", file=sys.stderr)
        return 1
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(json.dumps(result, ensure_ascii=False, indent=2, sort_keys=True)
                               + "\n", encoding="utf-8")
    print("TASK 8.7", "CI=" + result["ci_gate"],
          "MANUAL=" + result["manual_gate"],
          "FULL=" + result["full_homologation"])
    if result["manual_errors"]:
        print("MANUAL EVIDENCE ISSUES:", "; ".join(result["manual_errors"]), file=sys.stderr)
    return 1 if result["status"] == "FAIL" else 0


if __name__ == "__main__":
    raise SystemExit(main())
