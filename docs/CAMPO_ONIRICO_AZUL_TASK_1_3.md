# TASK 1.3 — Formações e Fenômenos do Campo Onírico Azul

Minecraft **26.3**, NeoForge **26.3.0.101**, branch `migration/minecraft-26.3`.
This follows the Task 1.1 terrain and Task 1.2 procedural vegetation.
No textures, items, new blocks, entities, loot, combat, PlayerTick generation, or changes to the Overworld and Dreamscape.

## 26.3 architecture / reusability

- New **`DreamFormationFeature` record implements `Feature`**, with
  `MapCodec<DreamFormationFeature>` and 11 inline data-driven fields
  (form, six blocks, two sizes, relief and density).
- Reuses the existing `DreamWorldgen` DeferredRegister for
  `BuiltInRegistries.FEATURE_TYPE`. Registers one additional type:
  `dreamsdimensions:dream_formation`. All four instances are datapack
  `data/dreamsdimensions/worldgen/feature/*.json`, not configured_feature.
- One `PlacedFeature` each, using the **26.3** pipeline
  `random_chance` → `in_square` → `heightmap`
  (`MOTION_BLOCKING_NO_LEAVES`) → `biome`. Random chance is checked
  once per input chunk; this is not a forced landmark for every chunk.
- Explicitly references those four `PlacedFeature`s **once** before
  Task 1.2's five placements in the owned Campo Onírico Azul biome's
  `VEGETAL_DECORATION` generation stage (index 9). No biome modifier,
  duplicate injection, or stage/cycle with other biomes.
- No checks for Campo Azul's dimension ID in the Java feature; other
  dimensions can supply their own form materials and size/frequency.

## Forms

### Espinhas da Névoa — `mist_spines`
- Two to four clustered spines, about 2–16 blocks tall (dependent on
  `size`, random variation, and spine index), with varying local lean.
- Each natural-ground-anchored spine has a guaranteed centerline; steps
  are bridged to remain face-connected. Irregular ellipsoidal rock
  slices taper up to sharp tips. Occasional cobble and **2% shimmer
  candidate probability per centerline/slice block** accent the stone;
  accents are not guaranteed.
- Each spine must pass individual ground/height/headroom checks.
  When any planned voxel intersects terrain/foliage/fluids or an
  unwritable chunk boundary, the **entire cluster is skipped**, not cut.

### Jardins de Luminescência — `luminescent_garden`
- Low asymmetrical ellipses (radius 3–6), with an irregular 1–2-block
  open corridor. A patch requires at least 12 natural supported grass
  cells and five candidate modifications.
- A fraction of natural **blue grass** is replaced with actual
  `dream_glow_moss` (light level **8**, copied from existing
  `ModBlocks.java`), while rare one-block shimmer outcrops and existing
  flowers break the surface. Most cells remain traversable.
- `dream_shimmer_block` inherits vanilla amethyst-block lighting
  (no configured light emission). It is **not** treated as a lamp.

### Arcos do Sonho — `dream_stone_arch`
- Rare 2-legged stone arches of horizontal half-width **5–7** blocks.
  Vertical rise approximately **5–8** blocks, X or Z orientation,
  sometimes with a secondary rib of thickness one. Two independent
  natural-stone/grass anchors are required, within 2 blocks of each
  other's surface elevation.
- Interpolation of a parabolic stone rib, plus vertical bridging at
  each consecutive X/Z column, produces a face-connected load-bearing
  skeleton. Any collision or out-of-region cell rejects the entire
  arch before modifications.
- **Intentional limit:** no gigantic cross-region structures. At most
  a 14-block span plus a 1-block side rib; avoids generating half-arches
  across distant/temporarily inaccessible chunks.

### Círculos de Vigília — `vigil_circle`
- Very rare irregular, broken elliptical rings, radius **4–7**.
  A multi-frequency outline breaks into natural ribs and holes rather
  than forming an ideal human-made stone circle. Individual low stone
  uprights (generally one block, occasionally two) are supported by
  original blue grass. Minor interior patches of moss and flowers
  preserve a walkable central space.
- Not functional portals, structures with loot, temples or mobs.

## Distribution / expected attempt rate

The `random_chance` column below represents a **chunk-level attempt
gate**, not the actual observed frequency. Every accepted attempt
still samples domain-warped value noise with the world seed, altitude,
relief, water/headroom, native ground and complete collision geometry.
Final chance inside the feature is:
`density × altitude_factor × (0.12 + 0.88 × regional_weight)`.

| Feature | Size | Chunk gate | Density | Max local relief | Morphological preference |
|---|---:|---:|---:|---:|---|
| Spines | 3–7 | 38.0% | 0.80 | 4 | medium/high ridges, transitional woodland |
| Garden | 3–6 | 27.0% | 0.72 | 3 | more open/lowlands |
| Arch | 5–7 | 5.5% | 0.78 | 3 | elevated, relatively flat stone/grass |
| Circle | 4–7 | 6.5% | 0.72 | 3 | open/flat grassy clearings |

All use the same *smooth* regional domain warp (241-block wavelength,
38-block warping) plus 164-block large-scale and 47-block finer noise,
but have separate salt/weights for biome coherence and differing
landmark groupings. No hard-coded world positions and no chunk-grid
selection. Java uses `level.getSeed()` and absolute X/Z; placed-feature
RNG is also seeded by vanilla worldgen. First 3×3 heightmap probes
sample local relief before attempting shapes.

These filters reduce actual spawned landmark frequency considerably.
The table is a *configuration* and not a measured density in-game.

## Integrity & performance constraints

- `Plan` is a per-attempt `LinkedHashMap<BlockPos,Change>`;
  **all** modifications are checked before any `setBlock`.
  Maximum **1600** write candidates per accepted formation.
- Solid material is written only in air with no fluid. The only
  replacement is existing `blue_dream_grass` → `dream_glow_moss`.
  No arbitrary replacement, no touching player blocks/structures, no
  tick subscriptions and no chunk recursion.
- Checks include vertical build limits, world border,
  `WorldGenLevel.ensureCanWrite` for each voxel, fluid, natural
  anchors and headroom. All-or-nothing skips prevent partial monuments
  where 26.3 worldgen-region restrictions apply.
- Formations are placed before Task 1.2 vegetation, allowing trees
  and flowers to reject occupied stone positions naturally.
- Arches are verified to be face-connected with both feet grounded;
  circles have low grounded uprights; spines have continuous cores.
- This uses the **existing** block IDs and blockstate mappings.

## Testing & what is NOT proved by compilation

CI workflow:
1. `./gradlew --no-daemon clean build` (Java 25).
2. `python3 scripts/ci/check_vegetation.py` (now allows the four Task 1.3
   placements; keeps all five Task 1.2 checks).
3. `python3 scripts/ci/check_formations.py`: exact codec/parameters,
   unique generation-stage ordering, source guard assertions, theoretical
   connectivity for **45 arch geometry cases** (three sizes, five
   ground offsets and three rises), and three world-seed noise profiles.
4. `./gradlew runServer`, dedicated server RCON smoke test for existing
   Dreamscape and five Campo Azul columns with safe nearby natural
   standing-space checks for seeds **14011, 20261108, -904732**.

Important: static geometry tests model the arch's mathematical skeleton;
they do NOT replace full block collision or appearance tests in Minecraft.
The server smoke test checks real codec loading and chunk generation, but
does NOT verify client rendering, in-game populations of every landmark,
visual composition, or 100% of possible seeds. See GitHub Actions result
for actual pass/fail status; do not infer from this document.

### IntelliJ / runClient exploration checklist

- [ ] Update `migration/minecraft-26.3`; use Java 25 and
      `./gradlew runClient`. **Back up any existing world.**
- [ ] Test a new world with seed `14011`, then separate new worlds for
      seeds `20261108` and `-904732`.
- [ ] Enter through normal sleep/teleport and verify a safe entry. For
      exploration, use spectator and (if needed)
      `/execute in dreamsdimensions:campo_onirico_azul run tp @s 0 160 0`.
- [ ] Fly a few hundred blocks through **new chunks** rather than
      already explored land. Check distant perspectives on geological
      spines, colored gardens, arches, and incomplete rings.
- [ ] Use **F3+G** and inspect every arch/cluster near chunk borders;
      confirm no chopped or broken silhouettes. Check all arch footings
      and any attempted structure near cliffs, water or forest walls.
- [ ] Compare wide grassland, woodland, spire valley and high terrace
      areas. Ensure 2-block passages through gardens and that moss glows
      but shimmer does not add artificial light.
- [ ] Estimate frequency in a spectator survey: inspect at least
      400 newly generated chunks per seed (20×20) and log counts by
      type; rare arches and circles may be absent by design.
- [ ] Reload the same seed at the same coordinates in a second fresh
      test world to verify identical placement; compare another seed.
- [ ] Observe TPS/FPS/chunk-loading stutter, logs for placement/codec
      errors, and ensure neither Dreamscape nor Overworld gained
      formations unexpectedly.
- [ ] Test normal sleep arrival and return with the Despertador Onírico;
      confirm no stone directly obstructs the spawn.

## Files

New:
- `src/main/java/com/dreamsdimensions/mod/worldgen/DreamFormationFeature.java`
- `src/main/resources/data/dreamsdimensions/worldgen/feature/{mist_spines,luminescent_garden,dream_stone_arch,vigil_circle}.json`
- `src/main/resources/data/dreamsdimensions/worldgen/placed_feature/{mist_spines,luminescent_garden,dream_stone_arch,vigil_circle}.json`
- `scripts/ci/check_formations.py`

Updated:
- `src/main/java/com/dreamsdimensions/mod/worldgen/DreamWorldgen.java`
- `src/main/resources/data/dreamsdimensions/worldgen/biome/campo_onirico_azul.json`
- `scripts/ci/check_vegetation.py`
- `.github/workflows/build.yml`

### Verified references (read against 26.3, not adapted blindly from old tutorial)
- https://docs.neoforged.net/primer/docs/26.3/
- https://docs.neoforged.net/docs/worldgen/biomemodifier/
- https://docs.neoforged.net/docs/concepts/registries/
- https://github.com/neoforged/NeoForge/tree/26.3.x
- https://github.com/NeoForgeMDKs/MDK-26.3-ModDevGradle
- https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3

The general registries and biome modifiers docs currently display version 26.1;
the **26.3 primer** and the effective NeoForge 26.3 compilation are decisive.
