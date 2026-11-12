#!/usr/bin/env python3
"""Validate the maintained DreamsDimensions docs without requiring external packages.

The current documentation only describes implementation, reproducible testing,
and product direction; superseded planning documents remain recoverable via Git.
"""
from __future__ import annotations

import argparse
import json
from pathlib import Path
import re
from urllib.parse import unquote, urlsplit

REQUIRED = (
    "README.md",
    "docs/ESTADO_ATUAL.md",
    "docs/GAMEPLAY_E_DIRECAO.md",
    "docs/ARQUITETURA_ATUAL.md",
    "docs/WORLDGEN_E_CONTEUDO.md",
    "docs/QA_E_VALIDACAO.md",
    "docs/MIGRATION_26_3.md",
    "docs/F0_TASK_7_5_MANUAL_TEMPLATE.json",
    "docs/TASK_8_VISUAL_QUALITY_PASS.md",
)
RETIRED = (
    "docs/GAMEPLAY_ROADMAP.md",
    "docs/GAME_DESIGN_BIBLE_TASK_6_0.md",
    "docs/OVERWORLD_EXECUTIVE_IMPLEMENTATION_ROADMAP_TASK_6_9.md",
    "docs/OVERWORLD_ANOMALY_ROADMAP_ADDENDUM_TASK_7_6.md",
    "docs/DREAMSDIMENSIONS_TASK_7_6.md",
    "docs/contracts/ONEIRIC_ANOMALIES_V1.json",
)
# The links in this repository do not use nested Markdown URLs.
LINKS = re.compile(r"(?<!!)\[[^\]\n]*\]\(([^)\n]+)\)")


def check(root: Path, strict: bool = True) -> dict:
    root = root.resolve()
    errors = []
    if strict:
        for name in REQUIRED:
            if not (root / name).is_file():
                errors.append("required_missing:" + name)
        for name in RETIRED:
            if (root / name).exists():
                errors.append("retired_document_reintroduced:" + name)
        workflow = root / ".github/workflows/build.yml"
        if workflow.is_file():
            content = workflow.read_text(encoding="utf-8")
            if "docs/F0_TASK_7_5_MANUAL_TEMPLATE.json" not in content:
                errors.append("f0_manual_template_disconnected_from_ci")
    md_files = sorted((root / "docs").rglob("*.md")) if (root / "docs").exists() else []
    if (root / "README.md").is_file():
        md_files.insert(0, root / "README.md")
    link_count = 0
    for md in md_files:
        text = md.read_text(encoding="utf-8")
        for match in LINKS.finditer(text):
            target = match.group(1).strip()
            if not target or target.startswith("#"):
                continue
            url = urlsplit(target)
            if url.scheme or url.netloc:
                continue
            raw_path = unquote(url.path)
            if not raw_path:
                continue
            link_count += 1
            if raw_path.startswith("/"):
                errors.append(f"absolute_local_link:{md.relative_to(root).as_posix()}:{raw_path}")
                continue
            destination = (md.parent / raw_path).resolve()
            if not destination.is_relative_to(root):
                errors.append(f"link_escapes_repository:{md.relative_to(root).as_posix()}:{raw_path}")
            elif not destination.exists():
                errors.append(f"broken_link:{md.relative_to(root).as_posix()}:{raw_path}")
    return {
        "schema_version": "dreamsdimensions.qa.v1",
        "test_id": "current_repository_documentation",
        "category": "CONTENT",
        "evidence_type": "STATIC_CHECK",
        "status": "PASS" if not errors else "FAIL",
        "docs_checked": len(md_files),
        "local_links_checked": link_count,
        "errors": sorted(errors),
        "notes": [
            "Git history preserves deleted planning docs",
            "Static link integrity does not validate gameplay/Survival or external websites",
        ],
    }


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path(__file__).resolve().parents[2])
    parser.add_argument("--report", type=Path)
    args = parser.parse_args()
    result = check(args.root)
    if args.report:
        args.report.parent.mkdir(parents=True, exist_ok=True)
        args.report.write_text(json.dumps(result, indent=2, ensure_ascii=False,
                                         sort_keys=True) + "\n", encoding="utf-8")
    if result["status"] == "FAIL":
        print("DOCUMENTATION CONTRACT FAIL", *result["errors"], sep="\n")
        return 1
    print("DOCUMENTATION CONTRACT PASS", json.dumps({
        "docs": result["docs_checked"],
        "local_links": result["local_links_checked"],
        "retired": len(RETIRED)
    }, sort_keys=True))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
