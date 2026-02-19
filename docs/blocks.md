# Blocos

## Sumário
- [Tabela completa](#tabela-completa)
- [Blocos especiais](#blocos-especiais)
- [Tags de ferramenta](#tags-de-ferramenta)
- [Referências](#referências)

## Tabela completa
Todos os blocos são registrados em `src/main/java/com/dreamsdimensions/mod/registry/ModBlocks.java`.

| ID | Classe/Tipo | Loot table |
|---|---|---|
| `dreamsdimensions:ds_dream_grass` | `Block` | `data/dreamsdimensions/loot_tables/blocks/ds_dream_grass.json` |
| `dreamsdimensions:az_dream_grass` | `Block` | `.../az_dream_grass.json` |
| `dreamsdimensions:ds_dream_dirt` | `Block` | `.../ds_dream_dirt.json` |
| `dreamsdimensions:az_dream_dirt` | `Block` | `.../az_dream_dirt.json` |
| `dreamsdimensions:ds_dream_sand` | `Block` | `.../ds_dream_sand.json` |
| `dreamsdimensions:ds_serene_stone` | `Block` | `.../ds_serene_stone.json` |
| `dreamsdimensions:az_dream_stone` | `Block` | `.../az_dream_stone.json` |
| `dreamsdimensions:az_dream_cobblestone` | `Block` | `.../az_dream_cobblestone.json` |
| `dreamsdimensions:ds_dream_shimmer_block` | `Block` | `.../ds_dream_shimmer_block.json` |
| `dreamsdimensions:ow_dream_glow_moss` | `OwDreamGlowMossBlock` | `.../ow_dream_glow_moss.json` |
| `dreamsdimensions:ow_dream_ore` | `DreamOreBlock` | `.../ow_dream_ore.json` |
| `dreamsdimensions:ow_deepslate_dream_ore` | `DreamOreBlock` | `.../ow_deepslate_dream_ore.json` |
| `dreamsdimensions:ow_dream_infused_stone` | `Block` | `.../ow_dream_infused_stone.json` |
| `dreamsdimensions:ow_oneiric_core_block` | `Block` | `.../ow_oneiric_core_block.json` |
| `dreamsdimensions:ow_anchoring_totem` | `Block` | `.../ow_anchoring_totem.json` |
| `dreamsdimensions:ds_dream_flower` | `Block` | `.../ds_dream_flower.json` |
| `dreamsdimensions:ow_lumina_flower` | `OwLuminaFlowerBlock` | `.../ow_lumina_flower.json` |
| `dreamsdimensions:ow_somniflora` | `OwSomnifloraBlock` | `.../ow_somniflora.json` |
| `dreamsdimensions:lumina_vines` | `LuminaVinesBlock` | `.../lumina_vines.json` |
| `dreamsdimensions:ds_dream_log` | `RotatedPillarBlock` | `.../ds_dream_log.json` |
| `dreamsdimensions:ow_somnibark_log` | `OwSomnibarkLogBlock` | `.../ow_somnibark_log.json` |
| `dreamsdimensions:ds_dream_leaves` | `Block` | `.../ds_dream_leaves.json` |
| `dreamsdimensions:ow_somnibark_leaves` | `Block` | `.../ow_somnibark_leaves.json` |

## Blocos especiais
### Minérios
- `dreamsdimensions:ow_dream_ore`
- `dreamsdimensions:ow_deepslate_dream_ore`

Comportamento:
- classe `DreamOreBlock` com propriedade de estado `clicked` alternável em interação sem item.
- loot com Silk Touch/Fortune/explosion_decay ([Loot Tables](loot_tables.md)).
- geração no Overworld via `data/dreamsdimensions/worldgen/biome_modifier/dream_ore_overworld.json` (step `underground_ores`).

### Totem de Ancoragem
- `dreamsdimensions:ow_anchoring_totem`
- Papel indireto: bloqueia teleporte por sono quando encontrado no raio de `anchoring_totem_radius`.

### Flora luminosa
- `dreamsdimensions:ow_lumina_flower`: partículas `minecraft:glow`, luz no bloco (12), textura emissiva no model.
- `dreamsdimensions:ow_somniflora`: base `FireflyBushBlock`, solo custom `dreamsdimensions:lumina_soil` + dirt + glow moss.
- `dreamsdimensions:lumina_vines`: sobrevive sob folhas/logs/vinhas; removida se suporte acima deixa de existir.

## Tags de ferramenta
Arquivos:
- `data/minecraft/tags/block/mineable/pickaxe.json`
- `data/minecraft/tags/block/mineable/axe.json`
- `data/minecraft/tags/block/mineable/shovel.json`
- `data/minecraft/tags/blocks/needs_iron_tool.json`

Resumo:
- pickaxe: `az_dream_stone`, `az_dream_cobblestone`, `ds_dream_shimmer_block`
- axe: `ow_somnibark_log`
- shovel: gramas/terras/areia + `ow_dream_glow_moss`
- needs_iron_tool: `ow_dream_ore`, `ow_deepslate_dream_ore`

## Referências
- [Loot Tables](loot_tables.md)
- [Dimensões e Worldgen](dimensions_worldgen.md)
