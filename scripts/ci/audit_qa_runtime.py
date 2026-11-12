#!/usr/bin/env python3
"""Operator-only /dreamqa command verification against a disposable CI server."""
import argparse
import json
from pathlib import Path
from check_dreamscape_homologation import Rcon


def run(seed: int, output: Path) -> dict:
    con = Rcon()
    try:
        con.auth()
        status = con.command("dreamqa status")
        assert "DREAM_QA status=OK enabled=true" in status, status
        assert f"run_id=ci-seed-{seed}" in status, status
        reply = con.command("dreamqa snapshot")
        assert "DREAM_QA_SNAPSHOT status=OK" in reply, reply
        assert output.is_file(), f"/dreamqa snapshot did not create {output}: {reply}"
        report = json.loads(output.read_text(encoding="utf-8"))
        assert report["schema_version"] == "dreamsdimensions.qa.v1"
        assert report["run_id"] == f"ci-seed-{seed}"
        assert report["enabled"] is True
        assert report["evidence_type"] == "RUNTIME_OBSERVED"
        print(f"QA_RUNTIME_COMMAND_PASS seed={seed} snapshot={output} "
              f"events={report['buffered_events']} dropped={report['dropped_events']}", flush=True)
        return report
    finally:
        con.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--seed", type=int, required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()
    run(args.seed, args.output)
