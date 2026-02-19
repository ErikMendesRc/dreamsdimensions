# Loot Tables

## Sumário
- [Inventário de arquivos](#inventário-de-arquivos)
- [Categorias](#categorias)
- [Condições e funções](#condições-e-funções)
- [Exemplos](#exemplos)

## Inventário de arquivos
Total: **23** loot tables de bloco em `data/dreamsdimensions/loot_tables/blocks/`.

Principais casos especiais:
- `ow_dream_ore.json`
- `ow_deepslate_dream_ore.json`
- `ds_dream_grass.json`
- `ds_dream_leaves.json`
- `ow_somnibark_leaves.json`

## Categorias
### 1) Simples (self drop)
Maioria dos blocos retorna o próprio item-bloco sem condição extra.

### 2) Grass especial
- `dreamsdimensions:ds_dream_grass` não dropa a si mesmo.
- Loot: `dreamsdimensions:ds_dream_dirt`.

### 3) Folhas com Silk Touch
- `dreamsdimensions:ds_dream_leaves`
- `dreamsdimensions:ow_somnibark_leaves`

Condição: `minecraft:match_tool` com encantamento `minecraft:silk_touch` nível mínimo 1.
Sem Silk Touch: nenhum drop explícito.

### 4) Minérios com alternativas
- `dreamsdimensions:ow_dream_ore`
- `dreamsdimensions:ow_deepslate_dream_ore`

Com Silk Touch: dropa o bloco minério.
Sem Silk Touch: dropa `dreamsdimensions:ow_dream_dust` com funções Fortune e explosão.

## Condições e funções
- `minecraft:match_tool`: verifica encantamentos (Silk Touch).
- `minecraft:apply_bonus` (`formula = minecraft:ore_drops`): escala com Fortune.
- `minecraft:explosion_decay`: reduz drop em cenário de explosão.

## Exemplos
Exemplo resumido de alternativa em minério:
```json
{
  "type": "minecraft:alternatives",
  "children": [
    { "name": "dreamsdimensions:ow_dream_ore", "conditions": [{ "condition": "minecraft:match_tool" }] },
    { "name": "dreamsdimensions:ow_dream_dust", "functions": [{ "function": "minecraft:apply_bonus" }, { "function": "minecraft:explosion_decay" }] }
  ]
}
```

Referências cruzadas:
- [Blocos](blocks.md)
- [Itens](items.md)
