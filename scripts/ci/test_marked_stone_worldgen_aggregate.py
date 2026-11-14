#!/usr/bin/env python3
"""Task 7.4 regression: aggregation rejects fabricated and incomplete evidence."""
import unittest
from copy import deepcopy
from aggregate_marked_stone_worldgen import aggregate, SEEDS, SCHEMA


def fixture(seed):
    entries = [{
        "chunks": 16, "marked_stone_blocks": 12,
        "air_exposed_marked_stone": 1 if index == 0 else 0,
        "first_exposed": "-10,59,-7" if index == 0 else "none",
    } for index in range(4)]
    return {
        "schema_version": SCHEMA,
        "seed": seed,
        "status": "PASS",
        "source": "REAL_NATURAL_GENERATION_LOADED_CHUNKS",
        "evidence_type": "RUNTIME_OBSERVED",
        "world": "minecraft:overworld",
        "regions": entries,
        "totals": {
            "chunks": 64,
            "chunks_with_marked_stone": 4,
            "marked_stone_blocks": 48,
            "air_exposed_marked_stone": 1,
        },
    }


class AggregateTest(unittest.TestCase):
    def setUp(self):
        self.rows = [fixture(s) for s in SEEDS]

    def test_four_real_seeds(self):
        report = aggregate(self.rows)
        self.assertEqual(report["totals"]["chunks"], 256)
        self.assertEqual(report["totals"]["seeds_with_exposed"], 4)
        self.assertEqual(report["manual_qa"]["stonecutter_gui_1_to_1"], "NOT_RUN")

    def test_missing_seed(self):
        with self.assertRaisesRegex(ValueError, "four fixed distinct seeds"):
            aggregate(self.rows[:-1])

    def test_duplicate_seed(self):
        duplicated = deepcopy(self.rows)
        duplicated[-1]["seed"] = duplicated[0]["seed"]
        with self.assertRaisesRegex(ValueError, "four fixed distinct seeds"):
            aggregate(duplicated)

    def test_reject_fabricated_source(self):
        fabricated = deepcopy(self.rows)
        fabricated[0]["source"] = "MANUALLY_PLACED_WITH_SETBLOCK"
        with self.assertRaisesRegex(ValueError, "manually placed"):
            aggregate(fabricated)

    def test_no_exposed_globally(self):
        hidden = deepcopy(self.rows)
        for row in hidden:
            row["totals"]["air_exposed_marked_stone"] = 0
            for region in row["regions"]:
                region["air_exposed_marked_stone"] = 0
                region["first_exposed"] = "none"
        with self.assertRaisesRegex(ValueError, "No exposed"):
            aggregate(hidden)

    def test_region_counts_cannot_invent_blocks(self):
        tampered = deepcopy(self.rows)
        tampered[0]["regions"][0]["marked_stone_blocks"] = 999
        with self.assertRaisesRegex(ValueError, "Region/total mismatch"):
            aggregate(tampered)


if __name__ == "__main__":
    unittest.main()
