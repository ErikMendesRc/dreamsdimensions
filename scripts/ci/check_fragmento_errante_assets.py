#!/usr/bin/env python3
"""Static validation of the exact Blockbench assets (not a render/compile test)."""
import json
from pathlib import Path
from struct import unpack

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src/main/resources/assets/dreamsdimensions"
GEO = ASSETS / "geckolib/models/entity/fragmento_errante.geo.json"
ANIM = ASSETS / "geckolib/animations/entity/fragmento_errante.animation.json"
PNG = ASSETS / "textures/entity/fragmento_errante.png"
PROJECT = ROOT / "art/blockbench/fragmento_errante.bbmodel"

geometry = json.loads(GEO.read_text(encoding="utf-8"))
animations = json.loads(ANIM.read_text(encoding="utf-8"))
project = json.loads(PROJECT.read_text(encoding="utf-8"))
assert geometry["minecraft:geometry"], "No model geometry"
bones = geometry["minecraft:geometry"][0]["bones"]
bone_names = [bone["name"] for bone in bones]
assert len(bone_names) == len(set(bone_names)), "Duplicate bone names"
assert sum(len(bone.get("cubes", ())) for bone in bones) >= 30
for bone in bones:
    if "parent" in bone:
        assert bone["parent"] in bone_names, f"Missing parent for {bone['name']}"
expected = {f"animation.fragmento_errante.{name}" for name in
            ("idle", "float", "move", "curious", "startled", "disappear")}
assert expected == set(animations["animations"]), "Wrong animation names"
for name, animation in animations["animations"].items():
    assert set(animation.get("bones", {})) <= set(bone_names), f"Unknown bone in {name}"
    assert animation["animation_length"] > 0
assert project["meta"]["model_format"] == "geckolib_model"
data = PNG.read_bytes()
assert data[:8] == b"\x89PNG\r\n\x1a\n"
width, height = unpack(">II", data[16:24])
assert (width, height) == (128, 128)
assert (ASSETS / "textures/entity/fragmento_errante_translucent.png").is_file()
print(f"TASK 4.0 ASSETS PASS: {len(bones)} bones, "
      f"{len(animations['animations'])} animations, texture {width}x{height}")
