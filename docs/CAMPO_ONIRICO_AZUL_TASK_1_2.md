# TASK 1.2 — Floresta dos Sonhos (Campo Onírico Azul)

Branch: `migration/minecraft-26.3`. Minecraft **26.3**, NeoForge **26.3.0.101**.
No new textures, blocks, entities, assets or items. Changes restricted to this biome's
vegetation plus corrections to the existing leaf and heightmap semantics.

## 26.3 architecture

- `DreamWorldgen` registers the **MapCodec** for `dreamsdimensions:dream_vegetation`
  in `BuiltInRegistries.FEATURE_TYPE` using NeoForge's `DeferredRegister`.
- `DreamVegetationFeature` implements the **26.3 Feature interface**. Five datapack
  instances are in `data/dreamsdimensions/worldgen/feature`; their five
  `placed_feature` wrappers use count → in_square → heightmap
  (MOTION_BLOCKING_NO_LEAVES) → biome.
- `campo_onirico_azul.json` references each PlacedFeature **once** in
  `VEGETAL_DECORATION` (generation step **9**, zero-based). No biome modifier:
  the biome is already owned by the mod and injecting the same features via
  modifiers would duplicate trees.
- The Dreamscape biome and Overworld receive **no new feature injection**.
- The feature's state/shape/height/chance/soil are datapack parameters;
  other biomes can reuse the code with their own block states and distribution.

## Trees (distinct shape algorithms)

| Form | Height | Shape | Distribution parameters |
| --- | --- | --- | --- |
| Sentinel | 15–21 | Thin Y-axis trunk, tall tapered spire, 3–4 ascending outer branches, narrow elongated leaf lobes | 7 attempts/chunk, base density 0.72 |
| Umbrella | 7–10 | Straight intermediate trunk, 3–4 rotated asymmetric long X/Z limbs, low thick overlapping horizontal canopy disks | 4 attempts/chunk, base density 0.45 |
| Arched | 11–16 | Progressively displaced curved trunk, side lobe clusters, extending descending terminal arm and elevated crown | 2 attempts/chunk, base density 0.20 |

These are *attempt counts*, NOT expected trees/chunk. Each attempt must pass
regional continuous noise, elevation, relief, soil, fluid, collision, height,
spawn exclusion and full candidate validation. Umbrella limbs can extend
several blocks; their leaves are rejected if colliding with existing
vegetation/solid blocks rather than cut in half.

## Geography and determinism

- All regional values are computed by smooth value noise from the **world
  seed and absolute X/Z**, not a per-chunk hash. A 220-block field warps the
  sample space (up to 45 blocks); a 145-block regional noise and 61-block
  secondary noise mix at weights 0.67 and 0.33, with 33-block local detail.
- A smoothstep transition from regional noise **-0.22 to +0.27**
  defines `woodland` between zero and one. Deep low-noise regions produce
  genuine forest-free meadows, without chunk-aligned borders.
- Sentinel chance: `density × max(0.94×woodland, isolated plateau chance)
  × detail`. High plateaus above Y=122 can contain rare isolated sentinels.
- Umbrella chance: `density × woodland² × detail` (grove interiors).
- Arch chance: `density × woodland × 0.62 × detail` (rare markers).
- All trees require four 4-block-offset heightmap probes, maximum 4-block
  height difference, and original configured blue-grass soil.
- The radial 36-block protection near the dimension entry coordinates is
  a **safety exception**, not a landscape sampling anchor.

## Ground vegetation

- Flower garden: **3 attempts/chunk**, base chance 0.48 multiplied by
  `(0.32 + 0.68×(1−woodland)) × detail`. A successful garden makes an
  uneven circular patch (radius 2–5) of existing dream flowers on blue
  grass; local random thinning leaves irregular clusters.
- Glow moss: **3 attempts/chunk**, base chance 0.52 multiplied by
  `(0.15+0.85×woodland) × altitude factor × detail`; factor
  1.0 below Y=92, 0.62 below Y=130, 0.22 above. Moss **replaces grass
  blocks** in uneven circular patches, never floats in midair.
- Both only use nearby columns within 2 blocks of the center Y, avoid
  existing vegetation and fluids, and check worldgen writing bounds.

## Leaf, log and tag migration

- `dream_leaves_block` retains its ID; previously a plain `Block`,
  now `DreamLeavesBlock extends LeavesBlock` with the 26.3
  `AmbientLeavesBlockSoundPlayer.noAmbientSound()` constructor.
- Leaf defaults use `PERSISTENT=true` to preserve old world data lacking
  leaf properties. Newly generated canopies use
  `PERSISTENT=false`, with a BFS assigning `DISTANCE=1..6` from
  generated logs and eliminating disconnected leaves. Distance and
  persistence continue updating when logs are removed in gameplay.
- Multipart model covers all leaf blockstates without new assets;
  the existing cutout model is reused. No change to leaf ID or texture.
- Existing `RotatedPillarBlock` already supports X/Y/Z; branch logs
  explicitly set AXIS=X or Z, trunks generally use Y.
- Dream leaves are in the foliage/motion-heightmap tags but **not**
  the NO_LEAVES motion/heightmap tags. Glow moss is a solid terrain
  replacement; it is included in all four motion/heightmap tags and in
  `dreamsdimensions:blue_natural_ground`.
- This is a best-effort blockstate-compatible migration; **back up old saves**
  and verify legacy leaf preservation in-client.

## Performance / safety

- No events per tick. At most 19 tree placement attempts and 6 patch
  attempts per chunk; failed attempts exit early. Trees are built into a
  bounded in-memory map first; writes proceed only if **all** candidate
  blocks are air, fluid-free, within height/write bounds. Trees either
  place completely or fail without partial trunks.
- Cross-chunk candidates are not clipped by a manual chunk-local box.
  The worldgen region's `ensureCanWrite` decides whether the entire
  shape can be committed.
- Crown BFS runs only for an accepted tree attempt. No dynamic registers
  are modified during worldgen; no player tick subscriptions.

## Tests

Automated on GitHub Actions per seed **14011, 20261108, -904732**:
1. `./gradlew --no-daemon clean build`
2. `python3 scripts/ci/check_vegetation.py` checks feature-to-placed
   registration, stage, modifiers, tag policy, leaf model and configurations.
3. Dedicated `runServer` checks datapack decoding, dimension loading,
   forceload of five distinct Campo Azul columns and one Dreamscape column,
   plus *nearby* safe natural-floor/headroom sampling. The neighborhood
   probing replaces the old exact-column headroom test, since flowers and
   trees now legitimately grow on some sampled columns.

**Limits:** CI loading and static checks are not visual proof, nor do
they directly count different tree silhouettes or measure client FPS.
A server/log result should be reported as `pending` until the run finishes.

### Manual visual acceptance (required before final sign-off)

1. Start a **new world** with seed 14011, Creative/Spectator, keep the original
   world as backup; build using `./gradlew runClient`.
2. Enter via normal sleep and confirm safe arrival; also check direct command
   `/execute in dreamsdimensions:campo_onirico_azul run tp @s 0 160 0`.
   Descend carefully in spectator before walking.
3. Overfly **new chunks** from radius 64–1024 around spawn. Compare the three
   tree shapes from at least 60 blocks away; look for clearings, sparse
   plateaus, luminous lowland patches and irregular flower gardens.
4. Press **F3+G** to see chunk borders. Follow canopy and branch geometry across
   borders. Verify no unnatural strips, chopped crowns, floating leaves or
   trees inside overhangs.
5. Break a generated sentinel log in survival; watch nonpersistent leaves
   recalculate distance and eventually decay. Verify foliage from a legacy
   backup doesn't disappear on load.
6. Repeat **fresh worlds** for seeds 20261108 and -904732. Confirm tree and
   patch placement reproducibility with the same seed and same build;
   change seed to verify variation. Monitor logs for codec/placement errors.
7. Enter and exit normally using the Dreamscape sleep/return flow and
   Despertador Onírico, then inspect Overworld and Dreamscape for absence of
   unexpected trees. Measure FPS and chunk generation latency on the
   production client.

Official references:
- https://docs.neoforged.net/primer/docs/26.3/
- https://docs.neoforged.net/docs/concepts/registries/
- https://docs.neoforged.net/docs/worldgen/biomemodifier/
- https://github.com/neoforged/NeoForge/tree/26.3.x
- https://www.minecraft.net/en-us/article/minecraft-26-3-snapshot-1
