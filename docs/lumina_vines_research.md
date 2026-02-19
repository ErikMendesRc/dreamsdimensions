# Lumina vines research (Vanilla + NeoForge + BOP)

## Vanilla (via NeoForge bundled minecraft classes)
- `net.minecraft.world.level.levelgen.feature.treedecorators.LeaveVineDecorator#place` rolls `probability` for each leaf side and calls `addHangingVine`.
- `LeaveVineDecorator#addHangingVine` places one vine at the start and then descends while air for up to 4 extra blocks.
- `net.minecraft.world.level.levelgen.feature.treedecorators.TrunkVineDecorator#place` rolls trunk-side attempts and places `minecraft:vine` on air-adjacent sides.
- `TreeDecorator.Context#placeVine` always places `Blocks.VINE.defaultBlockState().setValue(faceProperty, true)`.

Commands used for inspection:
- `javap -classpath ContextoIA/neoforge-21.11.37-beta -c -p net.minecraft.world.level.levelgen.feature.treedecorators.LeaveVineDecorator`
- `javap -classpath ContextoIA/neoforge-21.11.37-beta -c -p net.minecraft.world.level.levelgen.feature.treedecorators.TrunkVineDecorator`
- `javap -classpath ContextoIA/neoforge-21.11.37-beta -c -p net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator$Context`

## NeoForge beta 1.21.11 registries/codecs
- Vanilla tree configured features still declare decorators in `config.decorators` JSON list.
- Jungle uses `minecraft:trunk_vine` + `minecraft:leave_vine` (0.25) in configured feature JSON.
- `TreeDecoratorType` stores a `MapCodec` and registers each decorator type by id.

Evidence:
- `ContextoIA/neoforge-21.11.37-beta/data/minecraft/worldgen/configured_feature/jungle_tree.json`
- `ContextoIA/neoforge-21.11.37-beta/data/minecraft/worldgen/configured_feature/swamp_oak.json`
- `javap -classpath ContextoIA/neoforge-21.11.37-beta -c -p net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType`

## Biomes O' Plenty
- BOP tree configs commonly use custom tree features with `vine_provider` and `hanging_provider` controls.
- Example trees (bayou/willow/redwood) use vine state providers with `minecraft:vine`-like properties.
- BOP also controls density at tree placement level (`placed_feature/trees_bayou.json`) using low weighted counts.

Evidence:
- `ContextoIA/BiomesOPlenty-neoforge-1.21.11-21.11.0.28/data/biomesoplenty/worldgen/configured_feature/bayou_tree.json`
- `ContextoIA/BiomesOPlenty-neoforge-1.21.11-21.11.0.28/data/biomesoplenty/worldgen/configured_feature/willow_tree.json`
- `ContextoIA/BiomesOPlenty-neoforge-1.21.11-21.11.0.28/data/biomesoplenty/worldgen/placed_feature/trees_bayou.json`
