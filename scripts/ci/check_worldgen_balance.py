#!/usr/bin/env python3
"""Deterministic candidate-opportunity model for worldgen balance.

Measures the spatial *probability field* for Tasks 1.2/1.3; this never
generates terrain, does not model collisions or measure actual spawn counts.
Run on the same branch before/after tuning to compare unchanged samples.
"""
import json
import math
from pathlib import Path

BASE = Path(__file__).resolve().parents[2] / "src/main/resources/data/dreamsdimensions/worldgen"
SEEDS = (14011, 20261108, -904732, 73199013)
LANDMARKS = ("mist_spines", "luminescent_garden", "dream_stone_arch", "vigil_circle")
TREES = ("sentinel_tree", "umbrella_tree", "arched_tree")
MASK = (1 << 64) - 1


def load(folder, name):
    return json.loads((BASE / folder / (name + ".json")).read_text(encoding="utf-8"))


def smooth(lo, hi, v):
    t = max(0.0, min(1.0, (v - lo) / (hi - lo)))
    return t * t * (3 - 2 * t)


def hash_noise(seed, x, z):
    v = (seed ^ ((x * 0x9E3779B97F4A7C15) & MASK)
         ^ ((z * 0xC2B2AE3D27D4EB4F) & MASK)) & MASK
    v ^= v >> 30
    v = v * 0xBF58476D1CE4E5B9 & MASK
    v ^= v >> 27
    v = v * 0x94D049BB133111EB & MASK
    v ^= v >> 31
    return ((v >> 11) / (1 << 53)) * 2 - 1


def noise(seed, x, z):
    ix, iz = math.floor(x), math.floor(z)
    tx, tz = smooth(0, 1, x - ix), smooth(0, 1, z - iz)
    a, b = hash_noise(seed, ix, iz), hash_noise(seed, ix + 1, iz)
    c, d = hash_noise(seed, ix, iz + 1), hash_noise(seed, ix + 1, iz + 1)
    return (a + (b - a) * tx) * (1 - tz) + (c + (d - c) * tx) * tz


def field(seed, x, z):
    # Task 1.2 kernel: domain warp, two landscape scales + fine detail.
    wx = x + noise(seed ^ 0x459, x / 220, z / 220) * 45
    wz = z + noise(seed ^ 0xC13, x / 220 + 91, z / 220 - 37) * 45
    forest = 0.67 * noise(seed ^ 0xA07, wx / 145, wz / 145) + 0.33 * noise(seed ^ 0xE53, wx / 61, wz / 61)
    woodland = smooth(-0.22, 0.27, forest)
    detail = 0.72 + 0.28 * ((noise(seed ^ 0x8D1, x / 33, z / 33) + 1) * 0.5)

    # Task 1.3 kernel (same exact salts as Java).
    fx = x + noise(seed ^ 0x271A4D, x / 241, z / 241) * 38
    fz = z + noise(seed ^ 0x77F21, x / 241 + 19, z / 241 - 67) * 38
    regional = noise(seed ^ 0x5A817, fx / 164, fz / 164)
    small = noise(seed ^ 0xCA75D, x / 47, z / 47)
    return woodland, detail, regional, small


def weighted(form, regional, small):
    if form == "spines":
        return smooth(-.38, .48, .72 * regional + .28 * small)
    if form == "garden":
        return smooth(-.35, .25, -.56 * regional + .44 * small)
    if form == "arch":
        return smooth(-.15, .55, .80 * regional + .20 * small)
    return smooth(-.35, .40, -.30 * regional + .70 * small)


def main():
    configs = {}
    for name in LANDMARKS:
        f = load("feature", name)
        p = load("placed_feature", name)
        gate = p["placement"][0]["chance"]
        density = f["density"]
        assert 0 < gate <= 1 and 0 <= density <= 1
        configs[f["form"]] = (gate, density)
    tree_configs = {}
    for name in TREES:
        f = load("feature", name)
        p = load("placed_feature", name)
        assert p["placement"][0]["type"] == "minecraft:count"
        assert 0 <= f["density"] <= 1
        tree_configs[f["form"]] = (f["density"], p["placement"][0]["count"])

    # 40x40 chunk centers. Fixed *sampling domain* for CI comparisons, not
    # hardcoded worldgen coordinates or actual decoration locations.
    chunks = [(cx * 16 + 8, cz * 16 + 8) for cx in range(-20, 20) for cz in range(-20, 20)]
    print("MODEL_ONLY: candidate field, NO terrain/collisions/placed structures; altitude fixed at Y=110")
    for seed in SEEDS:
        clear, dense = 0, 0
        totals = {form: 0.0 for form in configs}
        tree_t = [0.0, 0.0, 0.0]
        for x, z in chunks:
            wood, detail, reg, small = field(seed, x, z)
            clear += wood < .10
            dense += wood > .65
            # A sentinel above Y122 gets an isolated-plateau fallback; this
            # survey uses Y110 and therefore no high-altitude fallback.
            tree_t[0] += tree_configs["sentinel"][0] * wood * .94 * detail * tree_configs["sentinel"][1]
            tree_t[1] += tree_configs["umbrella"][0] * wood * wood * detail * tree_configs["umbrella"][1]
            tree_t[2] += tree_configs["arch"][0] * wood * .62 * detail * tree_configs["arch"][1]
            for form, (gate, density) in configs.items():
                altitude = 1.0 if form in ("spines", "garden", "arch", "circle") else 0.0
                totals[form] += gate * density * altitude * (.12 + .88 * weighted(form, reg, small))
        assert 0 <= clear <= len(chunks) and 0 <= dense <= len(chunks)
        assert all(0 <= total <= len(chunks) for total in totals.values())
        print(
            f"seed={seed} chunks={len(chunks)} "
            f"model_open_fraction={clear/len(chunks):.3f} model_dense_fraction={dense/len(chunks):.3f} "
            + " ".join(f"model_{k}_opportunities_per_256_chunks={v*256/len(chunks):.2f}"
                       for k, v in totals.items())
            + " " + " ".join(f"model_{k}_tree_attempts_per_chunk={v/len(chunks):.2f}"
                            for k, v in zip(("sentinel", "umbrella", "arched"), tree_t))
        )
    print("MODEL_ONLY: figures exclude Y variation, terrain, collision, biome filtering and write rejection")


if __name__ == "__main__":
    main()
