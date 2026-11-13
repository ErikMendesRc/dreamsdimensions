# Task 9.2.2 — Floração Onírica e Bioluminescência

**Branch:** `migration/minecraft-26.3` · Minecraft 26.3 / NeoForge 26.3 / Java 25.

## Observed root cause

Task 9.2 implemented `OPEN = isDaytime` and `getStateForPlacement` always returned AGE=0. At AGE 0–2, the existing blockstate JSON always resolves the closed model, even when OPEN=true. A block handed out with `/give` therefore could not visually bloom until random-tick growth. Also, `/time set` did not immediately force a random tick. These are separate causes.

## Implementation

- Nighttime at Overworld clock tick 12000–23999: **mature (AGE=3) opens**; at 0–11999: **closes**. A pure `VigilHerbBloomPolicy` makes the boundary and day rollover testable.
- Plant **block item** places a mature specimen (useful for Creative and /give QA); **seeds** remain AGE=0, CLOSED and grow only in exposed daylight, without bone meal.
- Mature plants schedule a sparse server update every **20–59 game ticks** staggered by position. Lighting and model change are synchronized using `setBlock(..., 3)`. Random ticks bootstrap pre-existing saved mature herbs without schedules. No global scanning or per-player callbacks.
- **Real Minecraft light level 8** only at mature OPEN state. No dynamic shader or separate emissive render pass is claimed.
- Client-only small cyan dust spores (`DustParticleOptions`) appear intermittently only when open at night. No server packet, particle entity or continuously running sound.
- A new native 16×16 cyan/teal inflorescence for the existing cross-plane `vigil_herb_open.png`, identical bottom seven rows to the compact version. Other assets and model paths unchanged.
- Wild worldgen places AGE=3 OPEN=false as safe neutral initial state. Scheduled/random tick opens it at night, avoiding permanent daytime bloom.
- Daytime harvest still gives seeds plus 1–2 leaves; nighttime harvest still gives only seeds. Time-dependent appearance no longer dictates harvesting.
- No recipe, ID, biome modifier, loot or teleport changes.

## Fast manual test (Overworld, cheats enabled)

On grass/dirt, with a mature flower placed one block east of the player:

```mcfunction
/time set day
/setblock ~1 ~-1 ~ minecraft:grass_block
/setblock ~1 ~ ~ dreamsdimensions:vigil_herb[age=3,open=false]
/time set night
```

Wait at most **3 seconds while the chunk ticks**. The mature flower should open, emit block light 8 and release subtle spores. Then:

```mcfunction
/time set day
```

Wait at most 3 seconds: it should close, stop emitting light and stop producing spores. Run `/time set night` again; the transition must repeat. Use `/give @p dreamsdimensions:vigil_herb` and place the BLOCK item to get an instant adult specimen; `/give @p dreamsdimensions:vigil_herb_seeds` plants AGE=0. Young herbs will **not** open at night, by design. To isolate render, disable shaders and set randomTickSpeed 0; scheduled bloom still works for already mature plants.

### Automatable regression

```sh
python3 scripts/ci/check_task_9_2_2_bloom.py
./gradlew --no-daemon clean build
```

In GitHub Actions, the script runs `--rcon` against the disposable dedicated server to prove the actual block switches after `/time set night` and `/time set day`.

**Final artistic acceptance still requires a Minecraft client inspection.** CI cannot prove brightness is visually pleasing, particle density, or all shaders/resource packs. Load pre-update worlds to check old herb states recover via random ticks; check new worlds for worldgen and multiplayer.

## Official technical references

- [NeoForge Blocks — scheduled ticks, animateTick and random ticks](https://docs.neoforged.net/docs/1.21.10/blocks/)
- [NeoForge 26.3 API BlockBehaviour](https://aldak0.ru/javadoc/26.3.x/net/minecraft/world/level/block/state/BlockBehaviour.html)
- [NeoForge 26.3 API ScheduledTickAccess](https://aldak0.ru/javadoc/26.3.x/net/minecraft/world/level/ScheduledTickAccess.html)
