# Worldgen e Dimensões

## Sumário
- [Dimensões registradas](#dimensões-registradas)
- [Tipos de dimensão](#tipos-de-dimensão)
- [Biomas](#biomas)
- [Features e geração de minério](#features-e-geração-de-minério)
- [Onde encontrar cada recurso-chave](#onde-encontrar-cada-recurso-chave)
- [Estruturas](#estruturas)

## Dimensões registradas
- `dreamsdimensions:dreamscape`
- `dreamsdimensions:campo_onirico_azul`

Ambas usam `generator.type = minecraft:noise` com `biome_source` fixa.

## Tipos de dimensão
### `dreamscape_type`
- luz ambiente 1.0
- skylight ativo
- bed_works = true
- natural = false
- sem respawn anchor

### `campo_onirico_azul_type`
- luz ambiente 1.0
- `fixed_time = 6000`
- skylight ativo
- bed_works = true
- natural = true
- sem respawn anchor

## Biomas
### `dreamscape_biome`
- Sem precipitação.
- Sem spawns configurados (listas vazias).
- Cores próprias de céu/folhagem/grama.

### `campo_onirico_azul`
- Sem precipitação.
- Spawns de monstros e criaturas vanilla (zombie, skeleton, creeper, etc.; também passivos como sheep/cow/horse).
- Efeito extra por código: Slow Falling reaplicado no jogador quando nesta dimensão.

## Features e geração de minério
A geração de minério acontece no **Overworld** via biome modifier:
- arquivo: `worldgen/biome_modifier/dream_ore_overworld.json`
- biomas alvo: `#minecraft:is_overworld`
- etapa: `underground_ores`

Features:
- `ow_dream_ore` (stone replaceables)
  - size 6
  - count 2
  - faixa Y aproximada: -16 a 64 (trapezoid)
- `ow_deepslate_dream_ore` (deepslate replaceables)
  - size 5
  - count 2
  - faixa Y aproximada: -64 a -16 (trapezoid)

## Onde encontrar cada recurso-chave
- `ow_dream_dust`: minerando `ow_dream_ore`/`ow_deepslate_dream_ore` sem Silk Touch no Overworld.
- `az_dream_*` (grama/terra/pedra): presentes no `campo_onirico_azul`.
- `ds_*` (grama/terra/pedra serena etc.): presentes na `dreamscape`.

## Estruturas
Não há estruturas custom registradas em `data/dreamsdimensions/worldgen/structure` nem loot de baú de estrutura no namespace do mod.
