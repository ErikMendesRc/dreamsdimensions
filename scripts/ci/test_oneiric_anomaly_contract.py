#!/usr/bin/env python3
"""Task 7.6: adversarial checks for the anomaly design-to-gameplay contract."""
import copy
import unittest
from pathlib import Path

from check_oneiric_anomaly_contract import ContractError, inspect, load


ROOT = Path(__file__).resolve().parents[2]


class AnomalyContractTest(unittest.TestCase):
    def setUp(self):
        self.manifest = load(ROOT)

    def validate(self, obj):
        return inspect(obj, ROOT)

    def test_valid_contract_is_inert_and_has_two_dream_lineages(self):
        result = self.validate(self.manifest)
        self.assertEqual(result["status"], "PASS")
        self.assertEqual(result["activation"], "INERT_DESIGN_CONTRACT")
        self.assertEqual(result["required_independent_clues_per_profile"], 2)
        self.assertEqual(result["mandatory_interpretation_routes_per_profile"], 2)

    def test_reject_cosmetic_only_payoff(self):
        obj = copy.deepcopy(self.manifest)
        obj["profiles"][0]["payoff"]["physical_reward"] = "JUST_SPARKLES"
        with self.assertRaisesRegex(ContractError, "recoverable mechanical payoff"):
            self.validate(obj)

    def test_reject_nonexistent_final_target(self):
        obj = copy.deepcopy(self.manifest)
        obj["profiles"][0]["payoff"]["physical_site"] = "RANDOM_LOCATION_WITH_NO_SITE"
        with self.assertRaisesRegex(ContractError, "imaginary"):
            self.validate(obj)

    def test_reject_no_existing_consuming_mechanic(self):
        obj = copy.deepcopy(self.manifest)
        obj["profiles"][1]["payoff"]["existing_consumer"] = "dreamsdimensions:imaginary_sigil"
        with self.assertRaisesRegex(ContractError, "real gameplay consumer"):
            self.validate(obj)

    def test_reject_profile_without_narrative_causality(self):
        obj = copy.deepcopy(self.manifest)
        obj["profiles"][0]["causality"] = "Magic"
        with self.assertRaisesRegex(ContractError, "real, readable"):
            self.validate(obj)

    def test_reject_dream_motif_not_matching_dimension(self):
        obj = copy.deepcopy(self.manifest)
        obj["profiles"][1]["payoff"]["motif_dimension"] = "dreamsdimensions:campo_onirico_azul"
        with self.assertRaisesRegex(ContractError, "mismatch"):
            self.validate(obj)

    def test_no_forced_sleep_gate(self):
        obj = copy.deepcopy(self.manifest)
        obj["profiles"][0]["payoff"]["never_requires_dream_entry"] = False
        with self.assertRaisesRegex(ContractError, "no forced dream entry"):
            self.validate(obj)

    def test_reject_audio_only_correspondence(self):
        obj = copy.deepcopy(self.manifest)
        obj["profiles"][0]["alternate_corroborations"]["visual"] = ""
        with self.assertRaisesRegex(ContractError, "alternate_corroborations.visual"):
            self.validate(obj)

    def test_reject_no_waking_solution(self):
        obj = copy.deepcopy(self.manifest)
        obj["profiles"][1]["interpretation"]["awake"] = ""
        with self.assertRaisesRegex(ContractError, "interpretation.awake"):
            self.validate(obj)

    def test_reject_player_placed_origin(self):
        obj = copy.deepcopy(self.manifest)
        obj["selection"]["required_origin"] = "PLAYER_PLACED"
        with self.assertRaisesRegex(ContractError, "authentic worldgen"):
            self.validate(obj)

    def test_reject_unverified_terrain_and_false_leads(self):
        obj = copy.deepcopy(self.manifest)
        obj["selection"]["terrain_gate"].remove("two_viable_routes_to_payoff")
        with self.assertRaisesRegex(ContractError, "two accessible routes"):
            self.validate(obj)
        obj = copy.deepcopy(self.manifest)
        obj["selection"]["on_rejection"] = "PLACE_ANYWAY"
        with self.assertRaisesRegex(ContractError, "false clues"):
            self.validate(obj)

    def test_reject_client_spoof_or_repeatable_payoff(self):
        obj = copy.deepcopy(self.manifest)
        obj["anti_abuse"]["server_authoritative_evidence_only"] = False
        with self.assertRaisesRegex(ContractError, "anti-abuse"):
            self.validate(obj)
        obj = copy.deepcopy(self.manifest)
        obj["anti_abuse"]["no_rng_reward_reroll"] = False
        with self.assertRaisesRegex(ContractError, "anti-abuse"):
            self.validate(obj)

    def test_reject_unmeasured_frequency_claim(self):
        obj = copy.deepcopy(self.manifest)
        obj["selection"]["distribution_status"] = "ONE_PER_288_CHUNKS_CONFIRMED"
        with self.assertRaisesRegex(ContractError, "observed placed density"):
            self.validate(obj)

    def test_no_premature_activation(self):
        obj = copy.deepcopy(self.manifest)
        obj["activation"] = "LIVE"
        with self.assertRaisesRegex(ContractError, "never silently enable"):
            self.validate(obj)

    def test_requires_metrics_for_dropped_scenes_and_false_clues(self):
        obj = copy.deepcopy(self.manifest)
        obj["metrics_for_7_7"].remove("false_clue_without_target")
        with self.assertRaisesRegex(ContractError, "metrics"):
            self.validate(obj)


if __name__ == "__main__":
    unittest.main()
