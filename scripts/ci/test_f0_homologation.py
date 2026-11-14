#!/usr/bin/env python3
"""Task 7.5 regression tests: real-source gating, economy and censored discovery."""
import json
import tempfile
import unittest
from copy import deepcopy
from pathlib import Path

from f0_homologation import (
    SCHEMA, SEEDS, ORE_BASELINE, HomologationError, build_aggregate,
    manual_template, manual_status, static_report, ore_regressions,
    _percentiles_km
)


def fixtures(seed):
    regions = [{
        "chunks": 16, "marked_stone_blocks": 12,
        "air_exposed_marked_stone": 1 if i == 0 else 0,
        "first_exposed": "40,62,40" if i == 0 else "none",
    } for i in range(4)]
    marked = {
        "seed": seed, "schema_version": SCHEMA,
        "source": "REAL_NATURAL_GENERATION_LOADED_CHUNKS",
        "evidence_type": "RUNTIME_OBSERVED", "world": "minecraft:overworld",
        "status": "PASS", "regions": regions,
        "totals": {
            "chunks": 64, "chunks_with_marked_stone": 4,
            "marked_stone_blocks": 48, "air_exposed_marked_stone": 1,
        },
    }
    baseline = ORE_BASELINE[seed]
    ore = {
        "seed": seed, "source": "ACTUAL_GENERATED_CHUNKS_WITH_RCON_AUDIT",
        "world": "minecraft:overworld", "chunks": 256,
        "regions": [{}, {}, {}, {}],
        "dream_ore_blocks": baseline[0],
        "deepslate_dream_ore_blocks": baseline[1],
    }
    ruins = {
        "seed": seed, "source": "REAL_NATIVE_STRUCTURE_LOCATE_AND_LOADED_CHEST_READ",
        "details": [{
            "variant": variant, "status": "OBSERVED",
            "chest_count": 1, "loot_matches": 1, "stable_chests": 1,
        } for variant in ("blue", "fragmented")],
    }
    return marked, ore, ruins


class F0Test(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        self.marked = self.root / "marked"
        self.ores = self.root / "ores"
        self.ruins = self.root / "ruins"
        for folder in (self.marked, self.ores, self.ruins):
            folder.mkdir()
        for seed in SEEDS:
            a, b, c = fixtures(seed)
            (self.marked / f"marked-stone-worldgen-seed-{seed}.json").write_text(json.dumps(a))
            (self.ores / f"ore-audit-seed-{seed}.json").write_text(json.dumps(b))
            (self.ruins / f"dream-ruins-seed-{seed}.json").write_text(json.dumps(c))

    def test_current_repo_content_and_recipe_graph(self):
        root = Path(__file__).resolve().parents[2]
        result = static_report(root)
        self.assertEqual(result["status"], "PASS")
        self.assertEqual(result["f0_conversion"]["output_count"], 1)
        self.assertTrue(result["recipe_cycle_free"])

    def test_runtime_matrix_is_not_manual_approval(self):
        result = build_aggregate(self.marked, self.ores, self.ruins)
        self.assertEqual(result["ci_gate"], "PASS")
        self.assertEqual(result["survival_gate"], "NOT_RUN")
        self.assertEqual(result["full_homologation"], "PENDING")
        self.assertEqual(result["economic_reference"]["marked_for_64_cut_blocks"], 64)
        self.assertEqual(result["four_seed_worldgen"]["totals"]["chunks"], 256)
        self.assertEqual(result["dream_ore_regression"]["sampled_chunks"], 1024)
        self.assertEqual(result["recovery_ruins_regression"]["tested_natural_ruins"], 8)

    def test_reject_missing_one_seed(self):
        (self.ores / f"ore-audit-seed-{max(SEEDS)}.json").unlink()
        with self.assertRaisesRegex(HomologationError, "four distinct"):
            build_aggregate(self.marked, self.ores, self.ruins)

    def test_reject_duplicate_seed_disguised_in_file(self):
        path = self.ores / f"ore-audit-seed-{max(SEEDS)}.json"
        row = json.loads(path.read_text())
        row["seed"] = min(SEEDS)
        path.write_text(json.dumps(row))
        with self.assertRaisesRegex(HomologationError, "duplicate seed"):
            build_aggregate(self.marked, self.ores, self.ruins)

    def test_reject_big_dream_ore_regression(self):
        base = {s: fixtures(s)[1] for s in SEEDS}
        sample = deepcopy(base)
        sample[14011]["dream_ore_blocks"] = 500
        with self.assertRaisesRegex(HomologationError, "displaced dream_ore"):
            ore_regressions(sample)

    def test_reject_bad_ruin(self):
        path = self.ruins / "dream-ruins-seed-14011.json"
        row = json.loads(path.read_text())
        row["details"][1]["stable_chests"] = 0
        path.write_text(json.dumps(row))
        with self.assertRaisesRegex(HomologationError, "Ruins regression"):
            build_aggregate(self.marked, self.ores, self.ruins)

    def test_reject_fake_worldgen(self):
        path = self.marked / "marked-stone-worldgen-seed-14011.json"
        row = json.loads(path.read_text())
        row["source"] = "ARTIFICIAL_SETBLOCK"
        path.write_text(json.dumps(row))
        with self.assertRaisesRegex(ValueError, "manually placed"):
            build_aggregate(self.marked, self.ores, self.ruins)

    def test_manual_pass_requires_evidence(self):
        row = manual_template()
        row["cases"]["stone_pickaxe_drops_one"]["status"] = "PASS"
        with self.assertRaisesRegex(HomologationError, "without a screenshot"):
            manual_status(row)

    def test_censored_discovery_cannot_invent_p90(self):
        sessions = [{
            "id": f"session_{i}", "duration_minutes": 20 if i < 5 else 60,
            "found": i < 5, "environment": "eligible_geology_overworld",
        } for i in range(10)]
        result = _percentiles_km(sessions)
        self.assertEqual(result["observed"], 5)
        self.assertEqual(result["censored"], 5)
        self.assertEqual(result["p50_minutes"], 20)
        self.assertIsNone(result["p90_minutes"])

    def test_ten_or_more_required_for_percentiles(self):
        result = _percentiles_km([{
            "id": f"session_{i}", "duration_minutes": 11,
            "found": True, "environment": "eligible_geology_overworld",
        } for i in range(9)])
        self.assertEqual(result["status"], "INSUFFICIENT_SAMPLE")
        self.assertIsNone(result["p50_minutes"])

    def test_noneligible_habitat_is_not_counted(self):
        with self.assertRaisesRegex(HomologationError, "non-eligible"):
            _percentiles_km([{
                "id": "wrong_habitat", "duration_minutes": 15,
                "found": False, "environment": "random_desert",
            }])


if __name__ == "__main__":
    unittest.main()
