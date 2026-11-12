#!/usr/bin/env python3
"""QA 7.1 contract unit tests; no live server or Gradle required."""
import json
import tempfile
import unittest
from pathlib import Path

from collect_qa_report import SCHEMA, aggregate, build, validate_runtime


def fixtures(seed=14011):
    runtime = {"schema_version": SCHEMA, "run_id": f"ci-seed-{seed}",
               "enabled": True, "evidence_type": "RUNTIME_OBSERVED",
               "buffered_events": 0, "dropped_events": 0, "events": [], "counters": {}}
    ore = {"seed": seed, "source": "ACTUAL_GENERATED_CHUNKS_WITH_RCON_AUDIT",
           "chunks": 256, "regions": [{}, {}, {}, {}],
           "dream_ore_blocks": 1, "deepslate_dream_ore_blocks": 1}
    ruins = {"seed": seed, "source": "REAL_NATIVE_STRUCTURE_LOCATE_AND_LOADED_CHEST_READ",
             "details": [{"variant": variant, "status": "OBSERVED",
                          "chest_count": 1, "loot_matches": 1, "stable_chests": 1}
                         for variant in ("blue", "fragmented")]}
    return runtime, ore, ruins


class QaContractTest(unittest.TestCase):
    def test_reports_real_evidence_and_marks_manual_as_not_run(self):
        report = build(14011, *fixtures())
        self.assertEqual(report["summary"]["PASS"], 3)
        self.assertEqual(report["summary"]["NOT_RUN"], 3)
        self.assertTrue(all(r["evidence_type"] == "MANUAL_VERIFIED"
                            for r in report["results"] if r["status"] == "NOT_RUN"))
        self.assertFalse("MSPT" in json.dumps(report.get("results", [])))

    def test_mixed_seeds_fail(self):
        runtime, ore, ruins = fixtures()
        ore["seed"] = 20261108
        with self.assertRaises(ValueError):
            build(14011, runtime, ore, ruins)

    def test_unsafe_ruin_fails(self):
        runtime, ore, ruins = fixtures()
        ruins["details"][1]["stable_chests"] = 0
        with self.assertRaises(ValueError):
            build(14011, runtime, ore, ruins)

    def test_invalid_or_missing_runtime_fails(self):
        runtime, _, _ = fixtures()
        runtime["buffered_events"] = 1
        with self.assertRaises(ValueError):
            validate_runtime(runtime, 14011)

    def test_bad_status_fails(self):
        runtime, _, _ = fixtures()
        runtime["events"] = [{"schema_version": SCHEMA, "run_id": "ci-seed-14011",
                             "status": "MAGIC_PASS", "evidence_type": "RUNTIME_OBSERVED"}]
        runtime["buffered_events"] = 1
        with self.assertRaises(ValueError):
            validate_runtime(runtime, 14011)

    def test_all_four_seeds_required(self):
        reports = [build(seed, *fixtures(seed)) for seed in
                   (14011, 20261108, -904732, 73199013)]
        self.assertFalse(aggregate(reports)["manual_qa_complete"])
        with self.assertRaises(ValueError):
            aggregate(reports[:3])

    def test_junit_and_command_sources_exist(self):
        root = Path(__file__).resolve().parents[2]
        base = root / "src/main/java/com/dreamsdimensions/mod"
        recorder = (base / "qa/DreamQaRecorder.java").read_text()
        command = (base / "qa/DreamQaCommand.java").read_text()
        assert "DreamQaRecorder.global()" in (base / "service/DreamTeleportService.java").read_text()
        assert "DreamQaRecorder.global()" in (base / "item/OneiricAwakenerItem.java").read_text()
        assert "DreamQaRecorder.global()" in (base / "entity/spawn/DreamMobPopulationService.java").read_text()
        assert 'Boolean.getBoolean("dreamsdimensions.qa.enabled")' in recorder
        assert 'Commands.hasPermission(Commands.LEVEL_ADMINS)' in command
        assert "writeSnapshot" in command
        assert (root / "src/test/java/com/dreamsdimensions/mod/qa/DreamQaRecorderTest.java").exists()


if __name__ == "__main__":
    unittest.main()
