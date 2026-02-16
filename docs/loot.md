# Loot Tables

## Sumário
- [Visão geral](#visão-geral)
- [Loot de blocos](#loot-de-blocos)
- [Condições e influências](#condições-e-influências)
- [Exemplos JSON](#exemplos-json)

## Visão geral
O mod possui **21 loot tables**, todas no namespace:
`data/dreamsdimensions/loot_tables/blocks/`.

Não há loot tables de:
- entidades,
- estruturas/baús,
- pesca,
- arqueologia.

## Loot de blocos
### 1) Drops simples (1 pool, roll 1, item fixo)
`az_dream_cobblestone`, `az_dream_dirt`, `az_dream_stone`, `ds_dream_dirt`, `ds_dream_flower`, `ds_dream_glow_moss`, `ds_dream_log`, `ds_dream_sand`, `ds_dream_shimmer_block`, `ds_serene_stone`, `ow_anchoring_totem`, `ow_dream_infused_stone`, `ow_lumina_flower`, `ow_oneiric_core_block`, `ow_somnibark_log`, `ow_somniflora`.

### 2) Drops com condição Silk Touch
- `az_dream_grass`: com Silk dropa `az_dream_grass`, sem Silk dropa `az_dream_dirt`.
- `ds_dream_leaves`: só dropa o bloco com Silk Touch.

### 3) Drops de minério (alternativas + funções)
- `ow_dream_ore`
- `ow_deepslate_dream_ore`

Comportamento:
- **Silk Touch** → dropa o bloco minério.
- **Sem Silk** → dropa `ow_dream_dust`.
- Função `apply_bonus` com fórmula `ore_drops` → Fortune aumenta quantidade.
- Função `explosion_decay` → explosões podem reduzir drop.

### 4) Caso especial de grama dos sonhos
- `ds_dream_grass` sempre dropa `ds_dream_dirt` (sem alternativa Silk).

## Condições e influências
- `minecraft:match_tool` com enchantment `minecraft:silk_touch` decide preservação de bloco em gramas/minérios/folhas.
- `minecraft:apply_bonus` + `minecraft:fortune` influencia somente os minérios oníricos.
- `minecraft:explosion_decay` afeta apenas o caminho de drop de `ow_dream_dust` nos minérios.

## Exemplos JSON
### Minério com Silk/Fortune
```json
{
  "type": "minecraft:item",
  "name": "dreamsdimensions:ow_dream_dust",
  "functions": [
    {"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops"},
    {"function": "minecraft:explosion_decay"}
  ]
}
```

### Grama azul com alternativa de drop
```json
{
  "type": "minecraft:alternatives",
  "children": [
    {"type": "minecraft:item", "name": "dreamsdimensions:az_dream_grass", "conditions": [{"condition": "minecraft:match_tool", "predicate": {"enchantments": [{"enchantment": "minecraft:silk_touch", "levels": {"min": 1}}]}}]},
    {"type": "minecraft:item", "name": "dreamsdimensions:az_dream_dirt"}
  ]
}
```
