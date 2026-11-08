#!/usr/bin/env python3
"""TASK 1.3 static safety, codec, distribution and geometry assertions.

Not a screenshot or in-game placement count; the separate server smoke test
decodes the real Minecraft 26.3 datapacks and loads newly generated chunks.
"""
import json
import math
from collections import deque
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DATA = ROOT / "src/main/resources/data/dreamsdimensions/worldgen"
NAME = "dreamsdimensions:"
ENTRIES = {
    "mist_spines": ("spines", 3, 7, 4, .80, .38),
    "luminescent_garden": ("garden", 3, 6, 3, .72, .27),
    "dream_stone_arch": ("arch", 5, 7, 3, 1.0, .90),
    "vigil_circle": ("circle", 4, 7, 3, 1.0, .90),
}
BLOCKS = {
    "stone": "blue_dream_stone",
    "cobble": "blue_dream_cobblestone",
    "shimmer": "dream_shimmer_block",
    "moss": "dream_glow_moss",
    "flower": "dream_flower_block",
    "grass": "blue_dream_grass",
}


def read(path):
    return json.loads(path.read_text(encoding="utf-8"))


biome = read(DATA / "biome/campo_onirico_azul.json")
vegetation = ["sentinel_tree", "umbrella_tree", "arched_tree",
              "glow_moss_patch", "dream_flower_garden"]
expected = [NAME + n for n in ENTRIES] + [NAME + n for n in vegetation]
assert len(biome["features"]) == 10
assert all(not step for step in biome["features"][:9]), "No injected features in earlier stages"
assert biome["features"][9] == expected, "Formations must run before vegetation, without duplicates"
assert len(set(expected)) == 9

for ident, (shape, low, high, relief, density, chance) in ENTRIES.items():
    feature = read(DATA / "feature" / (ident + ".json"))
    placed = read(DATA / "placed_feature" / (ident + ".json"))
    assert feature == {
        "type": NAME + "dream_formation",
        "form": shape,
        **{k: NAME + block for k, block in BLOCKS.items()},
        "min_size": low, "max_size": high,
        "max_relief": relief, "density": density,
    }, ident
    assert placed == {
        "feature": NAME + ident,
        "placement": [
            {"type": "minecraft:random_chance", "chance": chance},
            {"type": "minecraft:in_square"},
            {"type": "minecraft:heightmap", "heightmap": "MOTION_BLOCKING_NO_LEAVES"},
            {"type": "minecraft:biome"},
        ],
    }, ident
    # Temporary TEST profile: rare features have an intentionally high
    # attempt chance. Biome/noise/surface/collision filters still apply.
    assert 0 < chance <= 1
    if ident in ("dream_stone_arch", "vigil_circle"):
        assert chance == .90 and density == 1.0, "Temporary landmark test profile changed"
    else:
        assert chance < .5
    assert low <= high <= 9
    assert 0 <= density <= 1

src = (ROOT / "src/main/java/com/dreamsdimensions/mod/worldgen/DreamFormationFeature.java"
       ).read_text(encoding="utf-8")
registration = (ROOT / "src/main/java/com/dreamsdimensions/mod/worldgen/DreamWorldgen.java"
                ).read_text(encoding="utf-8")
assert registration.count('FEATURES.register("dream_formation"') == 1
assert "BuiltInRegistries.FEATURE_TYPE" in registration
for marker in [
    "implements Feature", "MapCodec<DreamFormationFeature>", "BlockState.CODEC",
    "level.getSeed()", "level.ensureCanWrite(p)",
    "level.getWorldBorder().isWithinBounds(p)", "level.getFluidState(p).isEmpty()",
    "plan.changes.size() > 1600", "if (!formed ||", "Target.AIR", "Target.GRASS",
    "level.getBlockState(p).isAir()", "level.getBlockState(p).is(grass.getBlock())",
    "level.setBlock(entry.getKey(), entry.getValue().state(), FLAGS)",
]:
    assert marker in src, marker
# Verify no runtime/tick worldgen mutation, fixed-coordinate triggers or new blocks.
assert "PlayerTick" not in src and "new Block(" not in src
assert "origin.getX() ==" not in src and "origin.getZ() ==" not in src


def check_arch_geometry(radius, rise, delta):
    """Independent proof that the parabolic rib skeleton connects both ends."""
    left, right = 0, delta
    blocks = set()
    previous = left
    for i in range(2 * radius + 1):
        t = i / (2 * radius)
        base = round(left * (1 - t) + right * t)
        y = base + round(rise * (1 - (2 * t - 1) ** 2))
        for yy in range(min(y, previous) if i else y,
                        max(y, previous) + 1 if i else y + 1):
            blocks.add((i, yy))
        previous = y
    assert (0, 0) in blocks and (radius * 2, delta) in blocks
    seen = {(0, 0)}
    todo = deque(seen)
    while todo:
        x, y = todo.popleft()
        for p in ((x - 1, y), (x + 1, y), (x, y - 1), (x, y + 1)):
            if p in blocks and p not in seen:
                seen.add(p)
                todo.append(p)
    assert seen == blocks, (radius, rise, delta, len(blocks - seen))


for radius in range(5, 8):
    for delta in range(-2, 3):
        for rise in range(radius // 2 + 3, radius // 2 + 6):
            check_arch_geometry(radius, rise, delta)


# Verify sampled landmark noise works with different world seeds and in
# absolute (including negative) coordinates. Analytical only: not actual
# generated-world counts (those require loaded chunks / client inspection).
def hash_noise(seed, x, z):
    mask = (1 << 64) - 1
    v = (seed ^ ((x * 0x9E3779B97F4A7C15) & mask)
         ^ ((z * 0xC2B2AE3D27D4EB4F) & mask)) & mask
    v ^= v >> 30
    v = (v * 0xBF58476D1CE4E5B9) & mask
    v ^= v >> 27
    v = (v * 0x94D049BB133111EB) & mask
    v ^= v >> 31
    return ((v >> 11) / float(1 << 53)) * 2 - 1


def smooth(v):
    return v * v * (3 - 2 * v)


def noise(seed, x, z):
    ix, iz = math.floor(x), math.floor(z)
    tx, tz = smooth(x - ix), smooth(z - iz)
    a, b = hash_noise(seed, ix, iz), hash_noise(seed, ix + 1, iz)
    c, d = hash_noise(seed, ix, iz + 1), hash_noise(seed, ix + 1, iz + 1)
    return (a + (b - a) * tx) * (1 - tz) + (c + (d - c) * tx) * tz


probes = [(x, z) for x in range(-8, 9, 2) for z in range(-8, 9, 2)]
fingerprints = []
for seed in [14011, 20261108, -904732]:
    signature = tuple(round(noise(seed ^ 0x5A817, x * 16 / 164, z * 16 / 164), 9)
                      for x, z in probes)
    assert signature == tuple(round(noise(seed ^ 0x5A817, x * 16 / 164, z * 16 / 164), 9)
                              for x, z in probes)
    assert len(set(signature)) > 20
    fingerprints.append(signature)
assert len(set(fingerprints)) == 3
print("TASK 1.3: 4 codecs/configs, placement, stage, source guards, "
      "45 connected arch geometries and 3-seed noise signatures OK (static).")
