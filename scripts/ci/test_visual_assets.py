#!/usr/bin/env python3
"""Task 8 visual asset QA: catches broken PNG, locales, visuals and inventory."""
import json
from pathlib import Path
import struct
import tempfile
import unittest
import zlib

from audit_visual_assets import (
    VisualAuditError, ROTATED_NATURAL, audit, png_details, unique_lang_keys, assert_png_integrity
)


ROOT = Path(__file__).resolve().parents[2]


def small_png(size):
    def chunk(kind, payload):
        body = kind + payload
        return struct.pack(">I", len(payload)) + body + struct.pack(">I", zlib.crc32(body))
    scan = b"".join(b"\x00" + b"\x92\x96\xa4\xff" * size for _ in range(size))
    return (b"\x89PNG\r\n\x1a\n"
            + chunk(b"IHDR", struct.pack(">IIBBBBB", size, size, 8, 6, 0, 0, 0))
            + chunk(b"IDAT", zlib.compress(scan)) + chunk(b"IEND", b""))


class VisualAssetTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)

    def save(self, name, content):
        path = self.root / name
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_bytes(content if isinstance(content, (bytes, bytearray)) else content.encode("utf-8"))
        return path

    def test_repo_assets_model_rotation_and_localization(self):
        result = audit(ROOT)
        self.assertEqual(result["status"], "PASS")
        self.assertEqual(result["visual_review"], "NOT_RUN")
        self.assertEqual(result["counts"]["rotation_materials"], len(ROTATED_NATURAL))
        self.assertEqual(result["counts"]["creative_entries"], 27)

    def test_task_9_1_infused_stone_recipe_and_texture_wiring(self):
        # Snapshot of the current Survival recipe; Task 9.1 changes art only.
        from audit_visual_assets import assert_png_integrity
        root = ROOT / "src/main/resources"
        texture = root / "assets/dreamsdimensions/textures/block/dream_infused_stone.png"
        info = png_details(texture)
        self.assertEqual((info["width"], info["height"], info["color_type"]), (16, 16, 6))
        assert_png_integrity(texture)

        recipe = json.loads((root / "data/dreamsdimensions/recipe/dream_infused_stone.json").read_text())
        self.assertEqual(recipe["type"], "minecraft:crafting_shaped")
        self.assertEqual(recipe["pattern"], ["SD", "DS"])
        self.assertEqual(recipe["key"], {
            "S": "minecraft:stone",
            "D": "dreamsdimensions:dream_dust",
        })
        self.assertEqual(recipe["result"], {
            "id": "dreamsdimensions:dream_infused_stone", "count": 1,
        })

        assets = root / "assets/dreamsdimensions"
        block_model = json.loads((assets / "models/block/dream_infused_stone.json").read_text())
        item_def = json.loads((assets / "items/dream_infused_stone.json").read_text())
        self.assertEqual(block_model["parent"], "minecraft:block/cube_all")
        self.assertEqual(block_model["textures"]["all"], "dreamsdimensions:block/dream_infused_stone")
        self.assertEqual(item_def["model"]["model"], "dreamsdimensions:block/dream_infused_stone")

    def test_task_9_2_visual_assets_and_crafting_contract(self):
        root = ROOT / "src/main/resources"
        base = root / "assets/dreamsdimensions"
        for filename in ("vigil_herb_open", "vigil_herb_closed"):
            path = base / ("textures/block/" + filename + ".png")
            self.assertEqual((png_details(path)["width"], png_details(path)["height"]), (16, 16))
            assert_png_integrity(path)

        for filename in ("vigil_herb_seeds", "vigil_herb_leaf",
                         "toasted_vigil_herb", "return_charge"):
            path = base / ("textures/item/" + filename + ".png")
            self.assertEqual((png_details(path)["width"], png_details(path)["height"]), (16, 16))
            assert_png_integrity(path)

        gui = base / "textures/gui/container/oneiric_core_gui.png"
        self.assertEqual((png_details(gui)["width"], png_details(gui)["height"]), (176, 166))
        assert_png_integrity(gui)

        charge = json.loads((root / "data/dreamsdimensions/recipe/return_charge.json").read_text())
        self.assertEqual(charge["result"], {"id": "dreamsdimensions:return_charge", "count": 1})
        self.assertEqual(charge["ingredients"].count("dreamsdimensions:dream_dust"), 3)
        self.assertEqual(charge["ingredients"].count("dreamsdimensions:toasted_vigil_herb"), 2)
        self.assertFalse((root / "data/dreamsdimensions/recipe/oneiric_awakener.json").exists())

    def test_real_oneiric_art_model_and_pngs(self):
        result = audit(ROOT)
        self.assertEqual(result["counts"]["premium_32_textures"], 3)
        self.assertNotIn("textures/block/oneiric_core_block.png",
                         [row["asset"] for row in result["legacy_texture_debt"]])

    def test_oneiric_png_corrupt_idat_fails(self):
        original = bytearray(small_png(32))
        # Corrupt the compressed payload, leaving IHDR untouched.
        original[45] ^= 1
        file = self.save("damaged.png", original)
        with self.assertRaisesRegex(VisualAuditError, "PNG chunk CRC failed"):
            assert_png_integrity(file)

    def test_all_curated_terrain_is_real_16px(self):
        from audit_visual_assets import CURATED_TERRAIN
        self.assertEqual(len(CURATED_TERRAIN), 17)
        report = audit(ROOT)
        self.assertEqual(report["counts"]["curated_terrain_16_textures"], 17)
        self.assertEqual(report["counts"]["oversized_or_non16_assets"], 0)
        self.assertEqual(report["counts"]["orphan_model_candidates"], 0)
        self.assertEqual(report["counts"]["png_textures"], 37)
        self.assertGreater(report["counts"]["pixel_art_16_textures"], 20)

    def test_cutout_vegetation_and_log_orientations(self):
        assets = ROOT / "src/main/resources/assets/dreamsdimensions"
        for block in ("dream_flower_block", "dream_leaves_block"):
            model = json.loads((assets / f"models/block/{block}.json").read_text())
            self.assertEqual(model["render_type"], "cutout")
        log = json.loads((assets / "blockstates/dream_log_block.json").read_text())
        self.assertEqual(sorted(variant["y"] for variant in log["variants"]["axis=y"]),
                         [0, 90, 180, 270])
        self.assertEqual(log["variants"]["axis=x"]["x"], 90)
        self.assertEqual(log["variants"]["axis=z"]["x"], 90)

    def test_valid_16_pixel_texture(self):
        file = self.save("stone.png", small_png(16))
        info = png_details(file)
        self.assertTrue(info["pixel_art_16"])
        self.assertEqual(info["width"], 16)
        self.assertEqual(info["height"], 16)

    def test_large_is_reported_not_automatically_destroyed(self):
        file = self.save("legacy.png", small_png(128))
        info = png_details(file)
        self.assertFalse(info["pixel_art_16"])
        self.assertTrue(info["power_of_two"])

    def test_corrupt_png_fails_closed(self):
        file = self.save("bad.png", b"this is not png")
        with self.assertRaisesRegex(VisualAuditError, "Invalid PNG"):
            png_details(file)

    def test_corrupt_png_header_crc_fails(self):
        buf = bytearray(small_png(16))
        buf[22] ^= 0x08
        file = self.save("bad_crc.png", buf)
        with self.assertRaisesRegex(VisualAuditError, "header CRC"):
            png_details(file)

    def test_missing_iend_fails(self):
        file = self.save("truncated.png", small_png(16)[:-12])
        with self.assertRaisesRegex(VisualAuditError, "IEND"):
            png_details(file)

    def test_duplicate_translation_key_fails(self):
        file = self.save("en_us.json", '{"key":"One","key":"Two"}')
        with self.assertRaisesRegex(VisualAuditError, "Duplicate language key"):
            unique_lang_keys(file)

    def test_empty_translation_fails(self):
        file = self.save("pt_br.json", '{"key":"    "}')
        with self.assertRaisesRegex(VisualAuditError, "Blank"):
            unique_lang_keys(file)

    def test_real_locale_keys_match(self):
        folder = ROOT / "src/main/resources/assets/dreamsdimensions/lang"
        en = unique_lang_keys(folder / "en_us.json")
        pt = unique_lang_keys(folder / "pt_br.json")
        self.assertEqual(en.keys(), pt.keys())
        self.assertGreaterEqual(len(pt), 37)


if __name__ == "__main__":
    unittest.main()
