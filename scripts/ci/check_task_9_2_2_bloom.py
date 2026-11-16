#!/usr/bin/env python3
"""Task 9.2.2: static bloom/art contracts and optional server-side RCON proof."""
import argparse
import json
import re
import time
from pathlib import Path

from audit_visual_assets import assert_png_integrity, png_details
from check_task_9_2_1_art import rgba

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src/main/resources/assets/dreamsdimensions"
CODE = ROOT / "src/main/java/com/dreamsdimensions/mod"


def static_checks():
    open_png = ASSETS / "textures/block/vigil_herb_open.png"
    closed_png = ASSETS / "textures/block/vigil_herb_closed.png"
    for path in (open_png, closed_png):
        assert_png_integrity(path)
        info = png_details(path)
        assert (info["width"], info["height"], info["color_type"]) == (16, 16, 6)
    opened, closed = rgba(open_png), rgba(closed_png)
    assert opened[9:] == closed[9:], "base must not jump during bloom"
    assert all(p[3] in (0, 255) for row in opened for p in row), "no antialiasing"
    cyan_pixels = sum(p[3] == 255 and p[2] > p[0] * 1.15 and p[1] > 125
                      for row in opened[:8] for p in row)
    assert cyan_pixels >= 8, f"night inflorescence has too few teal highlights: {cyan_pixels}"

    state = json.loads((ASSETS/"blockstates/vigil_herb.json").read_text())
    for age in range(4):
        for opened_state in (True, False):
            actual = state["variants"][f"age={age},open={str(opened_state).lower()}"]["model"]
            expected = "open" if age == 3 and opened_state else "closed"
            assert actual == f"dreamsdimensions:block/vigil_herb_{expected}"
    feature = json.loads((ROOT/"src/main/resources/data/dreamsdimensions/worldgen/feature/vigil_herb_patch.json").read_text())
    assert feature["to_place"]["state"]["properties"] == {"age": "3", "open": "false"}

    block = (CODE/"block/VigilHerbBlock.java").read_text()
    seeds = (CODE/"item/VigilHerbSeedsItem.java").read_text()
    reg = (CODE/"registry/ModBlocks.java").read_text()
    policy = (CODE/"block/VigilHerbBloomPolicy.java").read_text()
    tests = (ROOT/"src/test/java/com/dreamsdimensions/mod/block/VigilHerbBloomPolicyTest.java").read_text()
    for token in ("queueBloomCheck(server, pos)", "level.scheduleTick(pos, this,",
                  "protected void tick(", "level.getBlockTicks().hasScheduledTick",
                  "syncBloom(level, pos, state)", "public void randomTick(",
                  "public void animateTick(", "level.addParticle(SPORE",
                  "level.setBlock(pos, updated, 3)", "static int lightLevel("):
        assert token in block, f"missing runtime gate {token}"
    assert ".lightLevel(VigilHerbBlock::lightLevel)" in reg
    assert "VigilHerbBloomPolicy.shouldOpen" in block
    assert "setValue(AGE, VigilHerbBloomPolicy.MATURE_AGE)" in block
    assert ".setValue(VigilHerbBlock.AGE, 0)" in seeds
    assert ".setValue(VigilHerbBlock.OPEN, false)" in seeds
    assert "Math.floorMod" in policy and "isNight" in policy and "canGrow" in policy
    assert "matureFlowerOpensAtNightAndClosesAtDawn" in tests
    print("PASS Task 9.2.2 static: bloom state mapping, native sprite, scheduled sync, light, spore, growth")


def runtime_check():
    """Disposable test world, no player presence, real scheduled ticks and blockstate."""
    from check_dreamscape_homologation import Rcon
    conn = Rcon()
    x, z, y = 32, 32, 300
    try:
        conn.auth()

        def cmd(value):
            return conn.command(value).strip()

        def need_state(expect):
            check = (f"execute in minecraft:overworld if block {x} {y} {z} "
                     f"dreamsdimensions:vigil_herb[age=3,open={expect}]")
            last = ""
            for attempt in range(10):
                last = cmd(check)
                if "Test passed" in last or re.search(r"count:\s*1\b", last):
                    return
                time.sleep(1)
            raise AssertionError(f"Flower failed to reach open={expect} in 10s: {last!r}")

        response = cmd(f"execute in minecraft:overworld run forceload add {x} {z}")
        assert not any(s in response.lower() for s in ("unknown", "incorrect", "failed to execute")), response
        cmd(f"execute in minecraft:overworld run setblock {x} {y-1} {z} minecraft:grass_block replace")
        cmd(f"execute in minecraft:overworld run setblock {x} {y} {z} dreamsdimensions:vigil_herb[age=3,open=false] replace")
        cmd("execute in minecraft:overworld run time set night")
        need_state("true")
        cmd("execute in minecraft:overworld run time set day")
        need_state("false")
        print("PASS Task 9.2.2 RCON: /time set night opens; /time set day closes mature herb without random ticks")
    finally:
        try:
            conn.command(f"execute in minecraft:overworld run setblock {x} {y} {z} minecraft:air replace")
            conn.command(f"execute in minecraft:overworld run setblock {x} {y-1} {z} minecraft:air replace")
            conn.command(f"execute in minecraft:overworld run forceload remove {x} {z}")
        finally:
            conn.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--rcon", action="store_true")
    args = parser.parse_args()
    static_checks()
    if args.rcon:
        runtime_check()
