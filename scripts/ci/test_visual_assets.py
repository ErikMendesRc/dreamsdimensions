#!/usr/bin/env python3
"""Task 8 visual asset QA: catches broken PNG, locales, visuals and inventory."""
import json
from pathlib import Path
import struct
import tempfile
import unittest
import zlib

from audit_visual_assets import (
    VisualAuditError, ROTATED_NATURAL, audit, png_details, unique_lang_keys
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
        path.write_bytes(content if isinstance(content, bytes) else content.encode("utf-8"))
        return path

    def test_repo_assets_model_rotation_and_localization(self):
        result = audit(ROOT)
        self.assertEqual(result["status"], "PASS")
        self.assertEqual(result["visual_review"], "NOT_RUN")
        self.assertEqual(result["counts"]["rotation_materials"], len(ROTATED_NATURAL))
        self.assertEqual(result["counts"]["creative_entries"], 22)

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
        self.assertEqual(len(pt), 38)


if __name__ == "__main__":
    unittest.main()
