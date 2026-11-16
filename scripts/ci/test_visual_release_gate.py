#!/usr/bin/env python3
"""Task 8.7: fail-closed review, no fabricated manual client approval."""
import copy
import json
import unittest
from pathlib import Path

from visual_release_gate import TEMPLATE, ROOT, SCHEMA, REQUIRED_CASES, validate_manual


class VisualReleaseGateTest(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.template = json.loads((ROOT / TEMPLATE).read_text(encoding="utf-8"))

    def test_template_never_claims_manual_pass(self):
        status, errors = validate_manual(self.template)
        self.assertEqual(status, "NOT_RUN")
        self.assertEqual(errors, [])

    def test_all_required_cases_are_listed(self):
        self.assertEqual(tuple(row["id"] for row in self.template["cases"]), REQUIRED_CASES)

    def test_missing_case_fails(self):
        data = copy.deepcopy(self.template)
        data["cases"].pop()
        status, errors = validate_manual(data)
        self.assertEqual(status, "FAIL")
        self.assertIn("exact_manual_case_set_required", errors)

    def test_duplicate_case_fails(self):
        data = copy.deepcopy(self.template)
        data["cases"][1]["id"] = data["cases"][0]["id"]
        status, errors = validate_manual(data)
        self.assertEqual(status, "FAIL")
        self.assertIn("exact_manual_case_set_required", errors)

    def test_pass_without_real_evidence_is_rejected(self):
        data = copy.deepcopy(self.template)
        data["cases"][0]["status"] = "PASS"
        status, errors = validate_manual(data)
        self.assertEqual(status, "FAIL")
        self.assertIn("evidence_required:material_tile_day_night", errors)

    def test_all_pass_with_evidence_is_only_manual_ready(self):
        data = copy.deepcopy(self.template)
        for row in data["cases"]:
            row["status"] = "PASS"
            row["evidence"] = ["recorded-screenshot-or-video-reference"]
        status, errors = validate_manual(data)
        self.assertEqual(status, "PASS")
        self.assertEqual(errors, [])

    def test_real_failure_never_means_pass(self):
        data = copy.deepcopy(self.template)
        data["cases"][0]["status"] = "FAIL"
        data["cases"][0]["evidence"] = ["recorded-issue-reference"]
        status, errors = validate_manual(data)
        self.assertEqual(status, "FAIL")
        self.assertEqual(errors, [])

    def test_schema_is_versioned(self):
        data = copy.deepcopy(self.template)
        data["schema_version"] = "old"
        status, errors = validate_manual(data)
        self.assertEqual(status, "FAIL")
        self.assertIn("missing_or_wrong_manual_schema", errors)


if __name__ == "__main__":
    unittest.main()
