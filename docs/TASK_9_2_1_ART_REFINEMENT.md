# Task 9.2.1 — Overworld asset art direction & validation

Branch: `migration/minecraft-26.3`. Scope: **art only**; no gameplay, registry, recipe, worldgen or save changes.

## Visual contract

Native 16×16 RGBA for the six Erva/Return Charge textures, authored with nearest-neighbor pixel clusters and binary transparency. Natural forest/olive/leaf midtones and warm illuminated edges; restrained mineral blue is confined to the inflorescence and the Carga. Botanical base is pixel-identical in the open and closed textures (rows 9–15). The harvested leaf and toasted leaf have identical silhouettes: processing changes the pigment, not the species. Seeds are earthy, not crystalline.

The Return Charge is an aged iron/copper vessel with a dark rim, handmade fixtures and a small cyan reservoir. The Core GUI remains 176×166, using stone, aged copper and muted cyan. Slot geometry remains anchored at inputs (29,34), (54,34), (79,34), result (138,34), inventory columns x=8+18n, rows y=84,102,120 and hotbar y=142.

## Changed production textures

- `textures/block/vigil_herb_open.png`
- `textures/block/vigil_herb_closed.png`
- `textures/item/vigil_herb_leaf.png`
- `textures/item/vigil_herb_seeds.png`
- `textures/item/toasted_vigil_herb.png`
- `textures/item/return_charge.png`
- `textures/gui/container/oneiric_core_gui.png`

All paths above are relative to `src/main/resources/assets/dreamsdimensions`. Existing `minecraft:block/cross` models, blockstate variants, item models and gameplay classes are preserved.

## Automated checks

```sh
python3 scripts/ci/check_task_9_2_1_art.py
python3 scripts/ci/test_visual_assets.py
python3 scripts/ci/audit_visual_assets.py --report qa-visual-assets-9-2-1.json
./gradlew --no-daemon clean build
```

The Build workflow executes the new art regression check alongside the existing four-seed matrix. The art check validates PNG integrity, native sizes, binary alpha, palette variety, exact leaf silhouette continuity, identical plant root, GUI pixel anchors and cross-model references. **This cannot approve aesthetics or prove client rendering in-game.**

## In-game manual review (still required)

```mcfunction
/time set day
/give @p dreamsdimensions:vigil_herb 1
/give @p dreamsdimensions:vigil_herb_leaf 16
/give @p dreamsdimensions:vigil_herb_seeds 16
/give @p dreamsdimensions:toasted_vigil_herb 16
/give @p dreamsdimensions:return_charge 16
/give @p dreamsdimensions:oneiric_awakener 1
/setblock ~2 ~ ~ dreamsdimensions:vigil_herb[age=3,open=true]
/setblock ~4 ~ ~ dreamsdimensions:oneiric_core_block
/time set night
/setblock ~2 ~ ~ dreamsdimensions:vigil_herb[age=3,open=false]
```

Inspect inventory icons at GUI scale 2–4 and plant cross-planes both in daylight and darkness. Confirm visibility against grass, dirt, foliage and shaders disabled, and that the Core crafting guide and output slot remain legible. Switching day/night with `/time` does not necessarily force a block random tick; the explicit `/setblock` switches the preview deterministically. Repeat the actual player-use/return and Recovery tests separately; this patch does not change those mechanics.

Status: static PNG & layout QA validated; game-client pixel review and human Survival acceptance **NOT RUN**.
