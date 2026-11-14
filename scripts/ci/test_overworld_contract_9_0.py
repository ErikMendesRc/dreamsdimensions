#!/usr/bin/env python3
"""Task 9.0 negative tests. No network or Minecraft process required."""
from copy import deepcopy
import json
from pathlib import Path
import unittest

from check_overworld_contract_9_0 import validate

ROOT = Path(__file__).resolve().parents[2]
SOURCE = ROOT / "docs/OVERWORLD_CONTENT_CATALOG_TASK_9_0.json"


class OverworldContractTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.baseline = json.loads(SOURCE.read_text(encoding="utf-8"))

    def fresh(self):
        return deepcopy(self.baseline)

    def item(self, data, name):
        return next(x for x in data["entries"] if x["id"] == name)

    def test_real_repo_contract_passes(self):
        self.assertEqual(validate(self.fresh(), ROOT), [])

    def test_duplicate_identifier_rejected(self):
        d = self.fresh()
        d["entries"].append(deepcopy(d["entries"][-1]))
        self.assertTrue(any(e.startswith("duplicate_id:") for e in validate(d)))

    def test_missing_dependency_rejected(self):
        d = self.fresh()
        self.item(d, "F1_RETURN_CHARGE")["depends_on"] = ["NOT_A_RESOURCE"]
        self.assertIn("missing_dependency:F1_RETURN_CHARGE:NOT_A_RESOURCE", validate(d))

    def test_cyclic_dependency_rejected(self):
        d = self.fresh()
        self.item(d, "F1_VIGIL_HERB")["depends_on"] = ["F1_AWAKENER_BALANCE"]
        self.assertTrue(any("cyclic_dependency" in e for e in validate(d)))

    def test_mandatory_dimensional_source_rejected(self):
        d = self.fresh()
        self.item(d, "F1_VIGIL_HERB")["origin"] = "dreamsdimensions:dreamscape"
        self.assertIn("non_overworld_origin:F1_VIGIL_HERB", validate(d))

    def test_no_free_return_rule_rejected(self):
        d = self.fresh()
        d["principles"]["no_free_dream_return"] = False
        self.assertIn("principle_must_be_true:no_free_dream_return", validate(d))

    def test_no_new_registry_claim_for_candidate(self):
        d = self.fresh()
        self.item(d, "F1_VIGIL_HERB")["registry_id"] = "fake_registered_herb"
        self.assertIn("proposed_feature_must_not_claim_registry:F1_VIGIL_HERB", validate(d))

    def test_early_depends_on_late_rejected(self):
        d = self.fresh()
        self.item(d, "F1_VIGIL_HERB")["depends_on"].append("S03_ENGINEERING")
        self.assertIn("phase_inversion:F1_VIGIL_HERB:S03_ENGINEERING", validate(d))

    def test_missing_consumer_rejected(self):
        d = self.fresh()
        self.item(d, "F1_RETURN_CHARGE")["depends_on"] = []
        self.assertIn("missing_selected_consumer:F1_VIGIL_HERB:F1_RETURN_CHARGE", validate(d))

    def test_selected_feature_not_claimed_as_implemented(self):
        d = self.fresh()
        self.item(d, "F1_VIGIL_HERB")["status"] = "IMPLEMENTED_CODE"
        self.assertTrue(any(e.startswith("missing_code_anchor:F1_VIGIL_HERB")
                            for e in validate(d)))

    def test_dust_sources_are_alternatives(self):
        d = self.fresh()
        self.item(d, "E03")["depends_on"] = ["E01", "E02"]
        self.assertIn("dust_ores_are_alternatives_not_mandatory_combined_inputs",
                      validate(d))

    def test_existing_code_anchor_verified(self):
        d = self.fresh()
        self.item(d, "E06")["registry_id"] = "does_not_exist"
        self.assertIn("registry_id_not_in_anchor:E06", validate(d, ROOT))

    def test_selected_slice_only_next_task(self):
        d = self.fresh()
        d["selected_slice"]["decision"] = "IMPLEMENTED"
        self.assertIn("must_not_claim_selected_slice_implemented", validate(d))

    def test_critical_awakener_cannot_change(self):
        d = self.fresh()
        d["progression"]["central_critical_item"] = "dreamsdimensions:other_item"
        self.assertIn("incorrect_preparation_center", validate(d))

    def test_risk_preparation_requires_awakener(self):
        d = self.fresh()
        d["dream_risk_contract"]["universal_preparation"].remove("oneiric_awakener")
        self.assertIn("awakener_must_be_in_preparation_kit", validate(d))

    def test_no_mandatory_dimension_gear(self):
        d = self.fresh()
        d["principles"]["no_mandatory_dimension_specific_gear"] = False
        self.assertIn("principle_must_be_true:no_mandatory_dimension_specific_gear", validate(d))

    def test_awakener_activation_must_remain_safe(self):
        d = self.fresh()
        d["dream_risk_contract"]["use_return"] = "Teleport instantly for free"
        self.assertIn("awakener_60_ticks_must_remain", validate(d))

    def test_all_gameplay_branches_have_known_dependencies(self):
        d = self.fresh()
        d["progression"]["branches"][0]["path"].append("UNKNOWN_ITEM")
        self.assertIn("unresolved_gameplay_branch:UNKNOWN_ITEM", validate(d))

    def test_early_mid_late_progression_present(self):
        d = self.fresh()
        d["progression"]["stages"].pop()
        self.assertIn("incorrect_progression_stages", validate(d))

    def test_scope_not_runtime(self):
        d = self.fresh()
        d["scope"] = "IMPLEMENTED"
        self.assertIn("scope_must_be_design_only", validate(d))


if __name__ == "__main__":
    unittest.main()
