# Task 9.5 — A Base e a Preparação Avançada

**Branch:** `migration/minecraft-26.3`. Minecraft 26.3 / NeoForge 26.3 / Java 25.  
**Source:** DreamsDimensions Game Design v2 (13/11/2026), Part C.3/C.8/C.10, E.4–E.9, F.2/F.4, H Fatia 4.

## Architectural contract

- The existing Core ID `oneiric_core_block` is retained; it has NO persistent inventory.
- N0 remains the existing Awakener and Vigil Tether.
- N1 requires **eight Cut Marked Stones at radius 2** around the Core (the corners and cardinal positions of a 5×5 footprint), leaving a walkable one-block gap around the workstation. The level is sampled **at menu opening**, and two Cargas can be made from 5 Pó + 4 Erva Torrada + 1 Cut Marked Stone.
- N1 also makes Resonance Probe (Compass + Return Charge + Cut Marked Stone) and recharges an existing Probe using one Charge.
- N2 requires the N1 ring, either a second ring of eight cut stones at radius 2 and Y+1 or four Deep Sleep Stones at radius 2 cardinal positions Y+1, AND one Reminiscence to attune the Core. The 1-block clearance remains open for interaction. The attunement is stored in an added boolean **blockstate**, not a block entity; existing cores keep their original ID and default to unattuned.
- N2 makes three Cargas from 6 Pó + 2 Erva Sonhada + 1 Reminiscência. The one-time Reminiscence attunement does not unlock recipes in other cores.
- Menus are per-player, ingredient counts are debited server-side, outputs cannot be shift-crafted and closing returns inputs.

## Incubation

- `dream_bedside`: an actual wooden cabinet with separate legs, drawer and upper surface, placed beside a bed. Input items are **one at a time**; a single item family per cabinet and 2-input capacity.
- `dreamcatcher`: a ring-and-feathers cutout, placed up to two blocks over the bed; expands the cabinet to 3 inputs.
- Serene (open sky): Glass Bottle → Serenity Vial.
- Silence (≥12 wool blocks around the room): String → Dream Thread.
- Ember (lit campfire within 4 blocks): Coal → Dormant Ember.
- Deep (Y below 0, no open sky): Dream Infused Stone → Deep Sleep Stone.
- Garden (≥8 flowers/leaves within radius 3): Toasted Vigil Herb → Dreamt Herb.
- Cultivated Garden takes precedence over otherwise open-sky Serene. Other conflicting environments are considered ambiguous, not arbitrarily resolved.
- Completion requires a genuine sleep-wakeup cycle without the Necklace, near sunrise. The serialized bedside tracks player owner, current inputs, finished outputs and processed night, preventing duplicate harvests on re-wake or relog.
- Destroyed cabinets return all pending items and completed products exactly once. No unattended ticking or shared global storage.
- Garden/horta uses existing daylight-grown Vigil Herb plants and vanilla farmland; no gratuitous new farm tile was introduced.

## Exploration

- Resonance Probe: 16 manually triggered uses, 24-block search for Dream Ores in *already loaded* Overworld chunks; in dreams, 32-block search for Trail Markers, with a bounded Recovery Ruin fallback. No terrain modification.
- Mineral Dream: an unmined Dream Ore near the bed gives a directional clue on waking; no automatic mining or new resource spawned.
- Dream Journal: reads only dreams the player has **confirmed visiting with the journal in inventory**, persists notes on the item. Does not reveal locked/future dimensions.
- Infusion of Route: brewing reagent is **Marked Stone**, with the existing toasted-herb base. Provides a timed navigation status and directions toward nearby Trail Markers.
- Serenity Vial substitutes water in the Oneiric Kettle, tagging the infusion for double duration (6 instead of 3 minutes).
- Marked Lining can be applied to vanilla armor at the Smithing Table, keeping an identifiable tooltip and allowing replacement. Its protection from **Drift** becomes relevant only when a dimension with that trait is implemented.
- Trail Marker: permanent stone/brass navigation landmark with restrained vertical signal particles; its **documented recipe requires Luzerna** (Task 9.6) and therefore is not fabricated in Survival in this slice. It can be placed in Creative for testing, without introducing an invented substitute recipe. The particle column is not yet a custom long-distance beam renderer.
- The active Probe's Vigilite search is likewise dependent on Vigilite's later worldgen; this slice supports existing Dream Ore.

## Visual direction (production sprites)

Assets are true 16×16 RGBA PNGs drawn to native pixel clusters, no scaling filters:
glass vial with liquid and cork; wound thread spool; coal with internal ember cracks; herb with stem/leaf nerves; hoop with feathers; fork-shaped resonance instrument; stitched leather lining; bound leather journal; golden directional infusion; carved wooden cabinet; dark stratified deep stone; metal-bound trail obelisk. Route effect: 18×18.

Colors are **object-led**, not one pastel tint applied to every item. All items are intentionally recognizable by silhouette and material at normal hotbar scale.

## Test commands (discardable test world)

```mcfunction
/give @s dreamsdimensions:oneiric_core_block 1
/give @s dreamsdimensions:cut_marked_stone 32
/give @s dreamsdimensions:reminiscence 4
/give @s dreamsdimensions:dream_bedside 2
/give @s dreamsdimensions:dreamcatcher 2
/give @s dreamsdimensions:serene_vial 3
/give @s dreamsdimensions:dream_thread 3
/give @s dreamsdimensions:dormant_ember 3
/give @s dreamsdimensions:dreamy_herb 3
/give @s dreamsdimensions:deep_sleep_stone 4
/give @s dreamsdimensions:resonance_probe 1
/give @s dreamsdimensions:trail_marker 1
/give @s dreamsdimensions:dream_journal 1
/give @s dreamsdimensions:marked_lining 1
/give @s dreamsdimensions:infusion_route 1
/give @s dreamsdimensions:dream_dust 32
/give @s dreamsdimensions:toasted_vigil_herb 16
/give @s dreamsdimensions:return_charge 8
/time set night
```

### Manual gate (NOT RUN / do not claim approved)

1. Survival: build N1/N2, confirm batch economics with repeated click + shift-click + closing menu. Break a ring while GUI open, relog, two players interacting in parallel, remove Core after save conversion.
2. Survival: sleep without Necklace in each of the five specialized room types; count outputs, prevent double nights, relog and break cabinets, verify no loss or duplication and clear owner rules.
3. Check overnight server behavior with other players awake, time skips, sleeping early, and Necklace-equipped sleeper.
4. Probe: loaded vs unloaded chunks, cross-dimension behavior, exactly 16 uses; verify non-discovery of future ores.
5. Journal persistence after confirmed entry; no future dimension in tooltip; repair and enchantment preservation when replacing armor linings.
6. Inspect all 15 new sprites in-world at inventory GUI scales, contrast and detailed object silhouette; sample full block models.
7. Measure preparation time in the FIRST and TENTH expedition; **target tenth < one third of first**, which cannot be claimed until a real timed Survival experiment.

Automated build + 4 seeds + dedicated server datapacks + structural QA are required but cannot replace this evidence.
