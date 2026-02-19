# Arquitetura do Projeto

## Sumário
- [Estrutura geral](#estrutura-geral)
- [Pacotes Java](#pacotes-java)
- [Resources / datapack](#resources--datapack)
- [Registries e convenções de ID](#registries-e-convenções-de-id)
- [Configuração DreamsConfig](#configuração-dreamsconfig)

## Estrutura geral
- Código: `src/main/java/com/dreamsdimensions/mod/**`
- Assets: `src/main/resources/assets/dreamsdimensions/**`
- Datapack: `src/main/resources/data/dreamsdimensions/**`

## Pacotes Java
- `registry`: registra blocos, itens, efeitos, poções, attachments.
- `event`: handlers de tick/efeitos/spawn/brewing.
- `item`: itens customizados, incluindo Despertador Onírico.
- `block`: blocos customizados e regras de sobrevivência/partículas.
- `attachment`: estado persistente de retorno (`dream_return`).
- `util`: helpers de retorno e anti-teleporte.
- `worldgen`: integração TerraBlender e perfis TEST/PROD.

## Resources / datapack
- `data/dreamsdimensions/dimension/*`: definições de dimensões.
- `data/dreamsdimensions/dimension_type/*`: propriedades de dimension type.
- `data/dreamsdimensions/worldgen/*`: biome, feature, placed feature, noise.
- `data/dreamsdimensions/loot_tables/blocks/*`: drops de blocos.
- `data/dreamsdimensions/recipe/*`: receitas JSON.
- `data/dreamsdimensions/neoforge/biome_modifier/*`: add_features por bioma.

## Registries e convenções de ID
Padrão observado:
- `ds_`: conteúdo ligado ao eixo "dreamscape" (ex.: `dreamsdimensions:ds_dream_grass`).
- `az_`: conteúdo ligado ao eixo azul (`dreamsdimensions:az_dream_stone`).
- `ow_`: conteúdo de economia/integração no Overworld (`dreamsdimensions:ow_dream_ore`, `dreamsdimensions:ow_oneiric_awakener`).

Registries principais:
- Blocos: `ModBlocks`
- Itens: `ModItems`
- Efeitos: `ModEffects`
- Poções: `ModPotions`
- Attachments: `ModAttachments`

## Configuração DreamsConfig
Arquivo Java: `src/main/java/com/dreamsdimensions/mod/config/DreamsConfig.java`

Chaves:
- `dream_dimensions`: lista de dimensões consideradas sonho.
  - default: `dreamsdimensions:dreamscape`, `dreamsdimensions:campo_onirico_azul`
- `anchoring_totem_radius`: raio (1-64, default 8) para bloquear teleporte por sono se houver `dreamsdimensions:ow_anchoring_totem` perto da referência de cama/spawn.

Referências cruzadas:
- [Loop de Gameplay](gameplay_loop.md)
- [Dimensões e Worldgen](dimensions_worldgen.md)
