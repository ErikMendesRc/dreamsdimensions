# Receitas

## Sumário
- [Visão geral](#visão-geral)
- [Receitas por tipo](#receitas-por-tipo)
- [Tabela resultado → crafting](#tabela-resultado--crafting)
- [Brewing em código](#brewing-em-código)

## Visão geral
Tipos presentes em `data/dreamsdimensions/recipe/**`:
- `minecraft:crafting_shaped`
- `minecraft:crafting_shapeless`
- `minecraft:smelting`
- `minecraft:blasting`

## Receitas por tipo
### Shaped
- `ow/ow_dream_infused_stone.json`
- `ow/ow_oneiric_core_block.json`
- `ow/ow_oneiric_awakener.json`
- `ow_anchoring_totem.json`
- `ow_condensed_dream_crystal.json`
- `ow_stabilized_dream_fragment.json`

### Shapeless
- `ow_oneiric_residue.json`
- `ow_dream_binding_thread.json`
- `ow_dream_catalyst.json`
- `ow_stabilizing_essence.json`

### Smelting / Blasting
- `ow_refined_oneiric_powder_from_smelting.json`
- `ow_refined_oneiric_powder_from_blasting.json`
- `az_dream_stone_from_smelting.json`
- `az_dream_stone_from_blasting.json`

## Tabela resultado → crafting
| Resultado | Como crafta | Pré-requisitos |
|---|---|---|
| `dreamsdimensions:ow_oneiric_residue` | shapeless 2x `ow_dream_dust` | acesso a minério OW |
| `dreamsdimensions:ow_refined_oneiric_powder` | smelting/blasting de `ow_oneiric_residue` | forno/alto-forno |
| `dreamsdimensions:ow_stabilized_dream_fragment` | shaped 2x2 `ow_refined_oneiric_powder` | refino prévio |
| `dreamsdimensions:ow_condensed_dream_crystal` | shaped 8 fragmentos + vidro | fragmentos |
| `dreamsdimensions:ow_dream_infused_stone` | shaped pedra + `ow_refined_oneiric_powder` | pedra vanilla |
| `dreamsdimensions:ow_oneiric_core_block` | infused stone + crystal + relógio | mid/late chain |
| `dreamsdimensions:ow_stabilizing_essence` | shapeless ghast tear + fragment + crystal | Nether + cadeia OW |
| `dreamsdimensions:ow_oneiric_awakener` | shaped feather + core + essence + garrafas | item final de retorno |
| `dreamsdimensions:ow_anchoring_totem` | shaped amethyst/redstone/iron/refined powder | anti-teleporte |
| `dreamsdimensions:ow_dream_binding_thread` | shapeless string + refined powder | brewing |
| `dreamsdimensions:ow_dream_catalyst` | shapeless blaze powder + refined powder + binding thread | brewing |
| `dreamsdimensions:az_dream_stone` | smelting/blasting `az_dream_cobblestone` | ciclo pedra azul |

## Brewing em código
**NÃO está em JSON de datapack**.

Registro: `src/main/java/com/dreamsdimensions/mod/event/BrewingRecipesHandler.java`.
Ver cadeia detalhada em [Itens](items.md#brewing-chain).
