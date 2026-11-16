#!/usr/bin/env python3
"""Task 7.2 CLI: fail CI on incomplete registrations/assets and export static QA."""
import argparse
import json
import sys
from pathlib import Path

from content_contract import SCHEMA, ContentContractError, validate


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--report", type=Path)
    args = parser.parse_args()
    try:
        report = validate(args.root.resolve())
    except (ContentContractError, OSError, ValueError) as exc:
        report = {
            "schema_version": SCHEMA,
            "test_id": "task_7_2_content_asset_contract",
            "category": "CONTENT",
            "evidence_type": "STATIC_CHECK",
            "status": "FAIL",
            "error": str(exc),
        }
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(json.dumps(report, indent=2, sort_keys=True) + "\n", encoding="utf-8")
    if report["status"] != "PASS":
        print("TASK 7.2 CONTENT ASSETS FAIL:", report["error"], file=sys.stderr, flush=True)
        return 1
    print("TASK 7.2 CONTENT ASSETS STATIC PASS:", json.dumps(report["counts"], sort_keys=True),
          flush=True)
    print("LIMITATION: STATIC_CHECK is not proof of textures rendered, Survival drops, "
          "creative inventory visibility or tool effectiveness.", flush=True)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
