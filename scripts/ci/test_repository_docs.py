#!/usr/bin/env python3
"""Regression tests: no dangling docs, no discarded references, no fake claims."""
import tempfile
from pathlib import Path
import unittest

from check_repository_docs import check


class DocumentationTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)
        (self.root / "docs").mkdir()

    def write(self, path: str, content: str):
        file = self.root / path
        file.parent.mkdir(parents=True, exist_ok=True)
        file.write_text(content, encoding="utf-8")

    def test_relative_link(self):
        self.write("README.md", "# Docs\n[Gameplay](docs/GAMEPLAY.md)\n")
        self.write("docs/GAMEPLAY.md", "# Gameplay\n")
        self.assertEqual(check(self.root, strict=False)["status"], "PASS")

    def test_fails_broken_link(self):
        self.write("docs/index.md", "[Does not exist](REMOVED.md)\n")
        result = check(self.root, strict=False)
        self.assertEqual(result["status"], "FAIL")
        self.assertIn("broken_link:docs/index.md:REMOVED.md", result["errors"])

    def test_nested_broken_link_has_platform_independent_path(self):
        self.write("docs/nested/page.md", "[Missing](missing.md)")
        result = check(self.root, strict=False)
        self.assertIn("broken_link:docs/nested/page.md:missing.md", result["errors"])

    def test_ignores_external_and_anchors(self):
        self.write("README.md", "[External](https://example.org/dreams) [Here](#story)\n")
        self.assertEqual(check(self.root, strict=False)["status"], "PASS")

    def test_nested_relative_links(self):
        self.write("docs/INDEX.md", "[Main](../README.md)")
        self.write("README.md", "Hello")
        self.assertEqual(check(self.root, strict=False)["status"], "PASS")

    def test_reject_absolute_link(self):
        self.write("README.md", "[Wrong](/docs/ONE.md)")
        result = check(self.root, strict=False)
        self.assertIn("absolute_local_link:README.md:/docs/ONE.md", result["errors"])

    def test_reject_links_escaping_project(self):
        self.write("README.md", "[Wrong](../outside.md)")
        result = check(self.root, strict=False)
        self.assertIn("link_escapes_repository:README.md:../outside.md", result["errors"])

    def test_main_real_repo(self):
        result = check(Path(__file__).resolve().parents[2], strict=True)
        self.assertEqual(result["status"], "PASS", result["errors"])
        self.assertGreaterEqual(result["docs_checked"], 5)


if __name__ == "__main__":
    unittest.main()
