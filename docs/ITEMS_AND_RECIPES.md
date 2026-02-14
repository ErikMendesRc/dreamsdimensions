# Dreams Dimensions — Itens, Blocos e Receitas (Inventário Técnico Completo)

**Versões-alvo:** Minecraft **1.21.11**, NeoForge **21.11.x**, Java **21**  
**Mod ID:** `dreamsdimensions`

## Índice
- [1. Sumário executivo](#1-sumário-executivo)
- [2. Catálogo Completo de Itens](#2-catálogo-completo-de-itens)
- [3. Matriz de Receitas (por output)](#3-matriz-de-receitas-por-output)
- [4. Matriz de Aquisição / Geração](#4-matriz-de-aquisição--geração)
- [5. Detalhamento por item/bloco](#5-detalhamento-por-itembloco)
  - [5.1 Pó dos Sonhos (`dreamsdimensions:dream_dust`)](#51-pó-dos-sonhos-dreamsdimensionsdream_dust)
  - [5.2 Despertador Onírico (`dreamsdimensions:oneiric_awakener`)](#52-despertador-onírico-dreamsdimensionsoneiric_awakener)
  - [5.3 Grama dos Sonhos (`dreamsdimensions:dream_grass_block`)](#53-grama-dos-sonhos-dreamsdimensionsdream_grass_block)
  - [5.4 Grama dos Sonhos Azul (`dreamsdimensions:blue_dream_grass`)](#54-grama-dos-sonhos-azul-dreamsdimensionsblue_dream_grass)
  - [5.5 Terra dos Sonhos (`dreamsdimensions:dream_dirt_block`)](#55-terra-dos-sonhos-dreamsdimensionsdream_dirt_block)
  - [5.6 Terra dos Sonhos Azul (`dreamsdimensions:blue_dream_dirt`)](#56-terra-dos-sonhos-azul-dreamsdimensionsblue_dream_dirt)
  - [5.7 Areia dos Sonhos (`dreamsdimensions:dream_sand_block`)](#57-areia-dos-sonhos-dreamsdimensionsdream_sand_block)
  - [5.8 Pedra Serena (`dreamsdimensions:serene_stone`)](#58-pedra-serena-dreamsdimensionsserene_stone)
  - [5.9 Pedra dos Sonhos Azul (`dreamsdimensions:blue_dream_stone`)](#59-pedra-dos-sonhos-azul-dreamsdimensionsblue_dream_stone)
  - [5.10 Pedregulho dos Sonhos Azul (`dreamsdimensions:blue_dream_cobblestone`)](#510-pedregulho-dos-sonhos-azul-dreamsdimensionsblue_dream_cobblestone)
  - [5.11 Bloco Cintilante dos Sonhos (`dreamsdimensions:dream_shimmer_block`)](#511-bloco-cintilante-dos-sonhos-dreamsdimensionsdream_shimmer_block)
  - [5.12 Musgo Luminoso dos Sonhos (`dreamsdimensions:dream_glow_moss`)](#512-musgo-luminoso-dos-sonhos-dreamsdimensionsdream_glow_moss)
  - [5.13 Minério dos Sonhos (`dreamsdimensions:dream_ore`)](#513-minério-dos-sonhos-dreamsdimensionsdream_ore)
  - [5.14 Minério dos Sonhos de Ardósia (`dreamsdimensions:deepslate_dream_ore`)](#514-minério-dos-sonhos-de-ardósia-dreamsdimensionsdeepslate_dream_ore)
  - [5.15 Pedra Infundida dos Sonhos (`dreamsdimensions:dream_infused_stone`)](#515-pedra-infundida-dos-sonhos-dreamsdimensionsdream_infused_stone)
  - [5.16 Bloco do Núcleo Onírico (`dreamsdimensions:oneiric_core_block`)](#516-bloco-do-núcleo-onírico-dreamsdimensionsoneiric_core_block)
  - [5.17 `dreamsdimensions:dream_flower_block`](#517-dreamsdimensionsdream_flower_block)
  - [5.18 `dreamsdimensions:dream_log_block`](#518-dreamsdimensionsdream_log_block)
  - [5.19 `dreamsdimensions:dream_leaves_block`](#519-dreamsdimensionsdream_leaves_block)
- [6. Fluxo detalhado: Oneiric Awakener e retorno](#6-fluxo-detalhado-oneiric-awakener-e-retorno)
- [Apêndice A: lista de receitas (por arquivo)](#apêndice-a-lista-de-receitas-por-arquivo)
- [Apêndice B: lista de loot tables](#apêndice-b-lista-de-loot-tables)
- [Apêndice C: worldgen (por feature)](#apêndice-c-worldgen-por-feature)
- [Apêndice D: chaves de tradução relevantes](#apêndice-d-chaves-de-tradução-relevantes)

## 1. Sumário executivo
O mod registra **19 entradas jogáveis** (2 itens diretos + 17 blocos com BlockItem), todas exibidas na aba criativa própria `itemGroup.dreamsdimensions.dreams_dimensions_tab`. Há **5 receitas JSON** no datapack (crafting/smelting/blasting), com progressão principal em torno de `dream_dust -> dream_infused_stone -> oneiric_core_block -> oneiric_awakener`. A aquisição survival de vários blocos depende de loot tabelas e, para minérios, de worldgen no Overworld via `neoforge:add_features`; diversos blocos não têm receita nem worldgen explícitos e, no estado atual, ficam como obtenção indireta/creative-only (quando não há outra fonte definida).

## 2. Catálogo Completo de Itens
| Nome (pt-BR) | ID | Tipo | Onde aparece | Como obter | Arquivos relevantes |
|---|---|---|---|---|---|
| Pó dos Sonhos | `dreamsdimensions:dream_dust` | Item (custom) | `dreams_dimensions_tab` | Drop de `dream_ore`/`deepslate_dream_ore` (sem Silk Touch); ingrediente de craft | `ModItems.java`, `DreamDustItem.java`, loot tables de minério |
| Despertador Onírico | `dreamsdimensions:oneiric_awakener` | Item utilitário (custom) | `dreams_dimensions_tab` | Craft (`oneiric_awakener.json`) | `ModItems.java`, `OneiricAwakenerItem.java`, `recipe/oneiric_awakener.json` |
| Grama dos Sonhos | `dreamsdimensions:dream_grass_block` | BlockItem | `dreams_dimensions_tab` | Dropa `dream_dirt_block` (loot); SEM RECEITA | `ModBlocks.java`, `loot_tables/blocks/dream_grass_block.json` |
| Grama dos Sonhos Azul | `dreamsdimensions:blue_dream_grass` | BlockItem | `dreams_dimensions_tab` | Drop com Silk Touch, senão `blue_dream_dirt`; SEM RECEITA | `ModBlocks.java`, `loot_tables/blocks/blue_dream_grass.json` |
| Terra dos Sonhos | `dreamsdimensions:dream_dirt_block` | BlockItem | `dreams_dimensions_tab` | Drop próprio; também drop de `dream_grass_block`; SEM RECEITA | `ModBlocks.java`, `loot_tables/blocks/dream_dirt_block.json`, `dream_grass_block.json` |
| Terra dos Sonhos Azul | `dreamsdimensions:blue_dream_dirt` | BlockItem | `dreams_dimensions_tab` | Drop próprio e de `blue_dream_grass` sem Silk Touch; SEM RECEITA | `ModBlocks.java`, `loot_tables/blocks/blue_dream_dirt.json`, `blue_dream_grass.json` |
| Areia dos Sonhos | `dreamsdimensions:dream_sand_block` | BlockItem | `dreams_dimensions_tab` | Drop próprio; SEM RECEITA | `ModBlocks.java`, `loot_tables/blocks/dream_sand_block.json` |
| Pedra Serena | `dreamsdimensions:serene_stone` | BlockItem | `dreams_dimensions_tab` | Drop próprio; SEM RECEITA | `ModBlocks.java`, `loot_tables/blocks/serene_stone.json` |
| Pedra dos Sonhos Azul | `dreamsdimensions:blue_dream_stone` | BlockItem | `dreams_dimensions_tab` | Smelting/blasting de `blue_dream_cobblestone` ou drop próprio | `ModBlocks.java`, `recipe/blue_dream_stone_*.json`, `loot_tables/blocks/blue_dream_stone.json` |
| Pedregulho dos Sonhos Azul | `dreamsdimensions:blue_dream_cobblestone` | BlockItem | `dreams_dimensions_tab` | Drop próprio; SEM RECEITA | `ModBlocks.java`, `loot_tables/blocks/blue_dream_cobblestone.json` |
| Bloco Cintilante dos Sonhos | `dreamsdimensions:dream_shimmer_block` | BlockItem | `dreams_dimensions_tab` | Drop próprio; SEM RECEITA | `ModBlocks.java`, `loot_tables/blocks/dream_shimmer_block.json` |
| Musgo Luminoso dos Sonhos | `dreamsdimensions:dream_glow_moss` | BlockItem | `dreams_dimensions_tab` | Drop próprio; SEM RECEITA | `ModBlocks.java`, `loot_tables/blocks/dream_glow_moss.json` |
| Minério dos Sonhos | `dreamsdimensions:dream_ore` | BlockItem (bloco custom) | `dreams_dimensions_tab` | Worldgen Overworld + loot (Silk Touch ou `dream_dust`) | `ModBlocks.java`, `DreamOreBlock.java`, worldgen + loot |
| Minério dos Sonhos de Ardósia | `dreamsdimensions:deepslate_dream_ore` | BlockItem (bloco custom) | `dreams_dimensions_tab` | Worldgen Overworld + loot (Silk Touch ou `dream_dust`) | `ModBlocks.java`, `DreamOreBlock.java`, worldgen + loot |
| Pedra Infundida dos Sonhos | `dreamsdimensions:dream_infused_stone` | BlockItem | `dreams_dimensions_tab` | Craft (`dream_infused_stone.json`) + drop próprio | `ModBlocks.java`, `recipe/dream_infused_stone.json`, loot table |
| Bloco do Núcleo Onírico | `dreamsdimensions:oneiric_core_block` | BlockItem | `dreams_dimensions_tab` | Craft (`oneiric_core_block.json`) + drop próprio | `ModBlocks.java`, `recipe/oneiric_core_block.json`, loot table |
| NÃO ENCONTRADO em lang | `dreamsdimensions:dream_flower_block` | BlockItem | `dreams_dimensions_tab` | SEM RECEITA / SEM LOOT TABLE / SEM WORLDGEN ENCONTRADO | `ModBlocks.java` |
| NÃO ENCONTRADO em lang | `dreamsdimensions:dream_log_block` | BlockItem | `dreams_dimensions_tab` | SEM RECEITA / SEM LOOT TABLE / SEM WORLDGEN ENCONTRADO | `ModBlocks.java` |
| NÃO ENCONTRADO em lang | `dreamsdimensions:dream_leaves_block` | BlockItem | `dreams_dimensions_tab` | SEM RECEITA / SEM LOOT TABLE / SEM WORLDGEN ENCONTRADO | `ModBlocks.java` |

## 3. Matriz de Receitas (por output)
- `dreamsdimensions:oneiric_awakener`
  - `minecraft:crafting_shaped` — `src/main/resources/data/dreamsdimensions/recipe/oneiric_awakener.json`
  - Pattern: `" F "`, `" K "`, `" B "`
  - Ingredientes: `minecraft:feather`, `dreamsdimensions:oneiric_core_block`, `minecraft:glass_bottle`
  - Output: 1x `dreamsdimensions:oneiric_awakener`
  - Conditions: **NÃO ENCONTRADO**
- `dreamsdimensions:oneiric_core_block`
  - `minecraft:crafting_shaped` — `src/main/resources/data/dreamsdimensions/recipe/oneiric_core_block.json`
  - Pattern: `"IDI"`, `"DCD"`, `"IDI"`
  - Ingredientes: `dreamsdimensions:dream_infused_stone`, `dreamsdimensions:dream_dust`, `minecraft:clock`
  - Output: 1x `dreamsdimensions:oneiric_core_block`
  - Conditions: **NÃO ENCONTRADO**
- `dreamsdimensions:dream_infused_stone`
  - `minecraft:crafting_shaped` — `src/main/resources/data/dreamsdimensions/recipe/dream_infused_stone.json`
  - Pattern: `"SD"`, `"DS"`
  - Ingredientes: `minecraft:stone`, `dreamsdimensions:dream_dust`
  - Output: 1x `dreamsdimensions:dream_infused_stone`
  - Conditions: **NÃO ENCONTRADO**
- `dreamsdimensions:blue_dream_stone`
  - `minecraft:smelting` — `src/main/resources/data/dreamsdimensions/recipe/blue_dream_stone_from_smelting.json`
  - Ingrediente: `dreamsdimensions:blue_dream_cobblestone`
  - Output: 1x `dreamsdimensions:blue_dream_stone`
  - Cooking time: 200 ticks; XP: 0.1
  - Conditions: **NÃO ENCONTRADO**
  - `minecraft:blasting` — `src/main/resources/data/dreamsdimensions/recipe/blue_dream_stone_from_blasting.json`
  - Ingrediente: `dreamsdimensions:blue_dream_cobblestone`
  - Output: 1x `dreamsdimensions:blue_dream_stone`
  - Cooking time: 100 ticks; XP: 0.1
  - Conditions: **NÃO ENCONTRADO**
- Demais IDs do catálogo: **SEM RECEITA (até o momento)**.

## 4. Matriz de Aquisição / Geração
- Crafting direto: `oneiric_awakener`, `oneiric_core_block`, `dream_infused_stone`, `blue_dream_stone`.
- Drop por loot table de bloco: `dream_dirt_block`, `blue_dream_dirt`, `dream_sand_block`, `serene_stone`, `blue_dream_cobblestone`, `dream_shimmer_block`, `dream_glow_moss`, `dream_infused_stone`, `oneiric_core_block`, `blue_dream_stone`.
- Minérios (`dream_ore`, `deepslate_dream_ore`):
  - Geram no Overworld por biome modifier `dream_ore_overworld.json` no passo `underground_ores`.
  - `dream_ore` placed feature: count=2, altura trapezoidal -16..64, `in_square`, filtro por bioma.
  - `deepslate_dream_ore` placed feature: count=2, altura trapezoidal -64..-16, `in_square`, filtro por bioma.
  - Loot: Silk Touch devolve bloco; sem Silk Touch dropa `dream_dust` com Fortune (`ore_drops`) + `explosion_decay`.
- Creative-only / não definido em survival no datapack atual:
  - `dream_flower_block`, `dream_log_block`, `dream_leaves_block` (**SEM RECEITA**, **SEM LOOT TABLE**, **SEM WORLDGEN ENCONTRADO**).

## 5. Detalhamento por item/bloco

## 5.1 Pó dos Sonhos (`dreamsdimensions:dream_dust`)
- **Tipo:** Item
- **Classe/Registro:** `DreamDustItem`; `ModItems.DREAM_DUST`
- **Descrição / Função:** material base de progressão; usado em `dream_infused_stone` e `oneiric_core_block`.
- **Stack/raridade/cooldown/uso:** stack padrão (`Item.Properties::new`), raridade padrão, sem cooldown.
- **Dimensões / Restrição:** sem restrição direta de uso.
- **Como obter:** drop de `dream_ore`/`deepslate_dream_ore` (sem Silk Touch).
- **Receitas (detalhadas):** output **SEM RECEITA (até o momento)**.
- **Notas técnicas:** mineração de minérios exige picareta adequada (`needs_iron_tool` + `mineable/pickaxe`).

## 5.2 Despertador Onírico (`dreamsdimensions:oneiric_awakener`)
- **Tipo:** Item utilitário (custom)
- **Classe/Registro:** `OneiricAwakenerItem`; `ModItems.ONEIRIC_AWAKENER`
- **Descrição / Função:** inicia uso carregado (animação arco, 60 ticks) e tenta retorno para Overworld.
- **Stack/raridade/cooldown/uso:** stack 1, `Rarity.RARE`, cooldown 60 ticks (`useCooldown`), duração de uso 60 ticks.
- **Dimensões / Restrição:** só inicia uso em dimensões marcadas em config (`dreamscape` e `campo_onirico_azul`, por padrão).
- **Como obter:** craft em `recipe/oneiric_awakener.json`.
- **Receitas (detalhadas):** shaped com `feather` + `oneiric_core_block` + `glass_bottle`.
- **Notas técnicas:** brilho (`isFoil=true`), tooltip custom, mensagem de sucesso/falha translatável.

## 5.3 Grama dos Sonhos (`dreamsdimensions:dream_grass_block`)
- **Tipo:** BlockItem
- **Classe/Registro:** `ModBlocks.DREAM_GRASS_BLOCK` + BlockItem em `ModItems`
- **Descrição / Função:** bloco de solo tipo grama.
- **Como obter:** quebrando bloco (dropa `dream_dirt_block`), SEM RECEITA.
- **Receitas (detalhadas):** **SEM RECEITA (até o momento)**.

## 5.4 Grama dos Sonhos Azul (`dreamsdimensions:blue_dream_grass`)
- **Tipo:** BlockItem
- **Classe/Registro:** `ModBlocks.BLUE_DREAM_GRASS`
- **Como obter:** Silk Touch => próprio bloco; sem Silk Touch => `blue_dream_dirt`.
- **Receitas (detalhadas):** **SEM RECEITA (até o momento)**.

## 5.5 Terra dos Sonhos (`dreamsdimensions:dream_dirt_block`)
- **Tipo:** BlockItem
- **Classe/Registro:** `ModBlocks.DREAM_DIRT_BLOCK`
- **Como obter:** loot próprio; também vem de `dream_grass_block`.
- **Receitas (detalhadas):** **SEM RECEITA (até o momento)**.

## 5.6 Terra dos Sonhos Azul (`dreamsdimensions:blue_dream_dirt`)
- **Tipo:** BlockItem
- **Classe/Registro:** `ModBlocks.BLUE_DREAM_DIRT`
- **Como obter:** loot próprio; também vem de `blue_dream_grass` sem Silk Touch.
- **Receitas (detalhadas):** **SEM RECEITA (até o momento)**.

## 5.7 Areia dos Sonhos (`dreamsdimensions:dream_sand_block`)
- **Tipo:** BlockItem
- **Classe/Registro:** `ModBlocks.DREAM_SAND_BLOCK`
- **Como obter:** loot próprio.
- **Receitas (detalhadas):** **SEM RECEITA (até o momento)**.

## 5.8 Pedra Serena (`dreamsdimensions:serene_stone`)
- **Tipo:** BlockItem
- **Classe/Registro:** `ModBlocks.SERENE_STONE_BLOCK`
- **Como obter:** loot próprio.
- **Receitas (detalhadas):** **SEM RECEITA (até o momento)**.

## 5.9 Pedra dos Sonhos Azul (`dreamsdimensions:blue_dream_stone`)
- **Tipo:** BlockItem
- **Classe/Registro:** `ModBlocks.BLUE_DREAM_STONE`
- **Como obter:** smelting/blasting de `blue_dream_cobblestone`; loot próprio.
- **Receitas (detalhadas):** `blue_dream_stone_from_smelting.json` e `blue_dream_stone_from_blasting.json`.

## 5.10 Pedregulho dos Sonhos Azul (`dreamsdimensions:blue_dream_cobblestone`)
- **Tipo:** BlockItem
- **Classe/Registro:** `ModBlocks.BLUE_DREAM_COBBLESTONE`
- **Como obter:** loot próprio.
- **Receitas (detalhadas):** output **SEM RECEITA (até o momento)**.

## 5.11 Bloco Cintilante dos Sonhos (`dreamsdimensions:dream_shimmer_block`)
- **Tipo:** BlockItem
- **Classe/Registro:** `ModBlocks.DREAM_SHIMMER_BLOCK`
- **Como obter:** loot próprio.
- **Receitas (detalhadas):** **SEM RECEITA (até o momento)**.

## 5.12 Musgo Luminoso dos Sonhos (`dreamsdimensions:dream_glow_moss`)
- **Tipo:** BlockItem
- **Classe/Registro:** `ModBlocks.DREAM_GLOW_MOSS`
- **Como obter:** loot próprio.
- **Receitas (detalhadas):** **SEM RECEITA (até o momento)**.

## 5.13 Minério dos Sonhos (`dreamsdimensions:dream_ore`)
- **Tipo:** BlockItem (bloco custom `DreamOreBlock`)
- **Classe/Registro:** `ModBlocks.DREAM_ORE`; XP 1..3 via `DropExperienceBlock`.
- **Descrição / Função:** minério interativo com state booleano `clicked` alternado por clique sem item.
- **Como obter:** worldgen Overworld + Silk Touch; sem Silk Touch dropa `dream_dust`.
- **Worldgen:** configured `worldgen/configured_feature/dream_ore.json`; placed `worldgen/placed_feature/dream_ore.json`.
- **Receitas (detalhadas):** **SEM RECEITA (até o momento)**.

## 5.14 Minério dos Sonhos de Ardósia (`dreamsdimensions:deepslate_dream_ore`)
- **Tipo:** BlockItem (bloco custom `DreamOreBlock`)
- **Classe/Registro:** `ModBlocks.DEEPSLATE_DREAM_ORE`; XP 1..3.
- **Como obter:** worldgen Overworld + Silk Touch; sem Silk Touch dropa `dream_dust`.
- **Worldgen:** configured `worldgen/configured_feature/deepslate_dream_ore.json`; placed `worldgen/placed_feature/deepslate_dream_ore.json`.
- **Receitas (detalhadas):** **SEM RECEITA (até o momento)**.

## 5.15 Pedra Infundida dos Sonhos (`dreamsdimensions:dream_infused_stone`)
- **Tipo:** BlockItem
- **Classe/Registro:** `ModBlocks.DREAM_INFUSED_STONE`
- **Como obter:** craft (`stone` + `dream_dust`) e loot próprio.
- **Receitas (detalhadas):** shaped em `recipe/dream_infused_stone.json`.

## 5.16 Bloco do Núcleo Onírico (`dreamsdimensions:oneiric_core_block`)
- **Tipo:** BlockItem
- **Classe/Registro:** `ModBlocks.ONEIRIC_CORE_BLOCK`
- **Como obter:** craft com `dream_infused_stone`, `dream_dust`, `clock`; loot próprio.
- **Receitas (detalhadas):** shaped em `recipe/oneiric_core_block.json`.

## 5.17 `dreamsdimensions:dream_flower_block`
- **Tipo:** BlockItem
- **Classe/Registro:** `ModBlocks.DREAM_FLOWER_BLOCK`
- **Nome amigável:** **NÃO ENCONTRADO** em `lang/en_us.json` e `lang/pt_br.json`.
- **Como obter:** **SEM RECEITA**, **SEM LOOT TABLE**, **SEM WORLDGEN ENCONTRADO**.

## 5.18 `dreamsdimensions:dream_log_block`
- **Tipo:** BlockItem (RotatedPillarBlock)
- **Classe/Registro:** `ModBlocks.DREAM_LOG_BLOCK`
- **Nome amigável:** **NÃO ENCONTRADO** em `lang/en_us.json` e `lang/pt_br.json`.
- **Como obter:** **SEM RECEITA**, **SEM LOOT TABLE**, **SEM WORLDGEN ENCONTRADO**.

## 5.19 `dreamsdimensions:dream_leaves_block`
- **Tipo:** BlockItem
- **Classe/Registro:** `ModBlocks.DREAM_LEAVES_BLOCK`
- **Nome amigável:** **NÃO ENCONTRADO** em `lang/en_us.json` e `lang/pt_br.json`.
- **Como obter:** **SEM RECEITA**, **SEM LOOT TABLE**, **SEM WORLDGEN ENCONTRADO**.

## 6. Fluxo detalhado: Oneiric Awakener e retorno
1. **Pré-condição de dimensão:** o item só entra em uso se `DreamsConfig.isDreamDimension(level.dimension())` for true; default inclui `dreamsdimensions:dreamscape` e `dreamsdimensions:campo_onirico_azul`.
2. **Uso/canalização:** ao clicar com botão direito, inicia uso por 60 ticks (`getUseDuration`), animação `BOW`.
3. **Cooldown:** ao finalizar com sucesso, aplica cooldown de 60 ticks (lido de `DataComponents.USE_COOLDOWN` do próprio item).
4. **Retorno com prioridade:**
   - **Prioridade 1:** cama salva no attachment `dream_return` (`DreamReturnData`) no Overworld.
   - **Prioridade 2:** respawn vanilla (`findRespawnPositionAndUseSpawnBlock`).
   - **Prioridade 3:** spawn global do mundo (`LevelData.RespawnData`) com busca de posição segura.
5. **Persistência do ponto de retorno:** `DreamReturnAttachmentHandler` atualiza attachment ao definir spawn no Overworld (`PlayerSetSpawnEvent`), e attachment está configurado com codec + `copyOnDeath`.
6. **Entrada nas dimensões de sonho:** `SleepTeleportHandler` teleporta jogadores após sono longo no Overworld para uma das dimensões de sonho registradas.

## Apêndice A: lista de receitas (por arquivo)
- `src/main/resources/data/dreamsdimensions/recipe/blue_dream_stone_from_blasting.json`
- `src/main/resources/data/dreamsdimensions/recipe/blue_dream_stone_from_smelting.json`
- `src/main/resources/data/dreamsdimensions/recipe/dream_infused_stone.json`
- `src/main/resources/data/dreamsdimensions/recipe/oneiric_core_block.json`
- `src/main/resources/data/dreamsdimensions/recipe/oneiric_awakener.json`

## Apêndice B: lista de loot tables
- `src/main/resources/data/dreamsdimensions/loot_tables/blocks/blue_dream_cobblestone.json`
- `src/main/resources/data/dreamsdimensions/loot_tables/blocks/blue_dream_dirt.json`
- `src/main/resources/data/dreamsdimensions/loot_tables/blocks/blue_dream_grass.json`
- `src/main/resources/data/dreamsdimensions/loot_tables/blocks/blue_dream_stone.json`
- `src/main/resources/data/dreamsdimensions/loot_tables/blocks/deepslate_dream_ore.json`
- `src/main/resources/data/dreamsdimensions/loot_tables/blocks/dream_dirt_block.json`
- `src/main/resources/data/dreamsdimensions/loot_tables/blocks/dream_glow_moss.json`
- `src/main/resources/data/dreamsdimensions/loot_tables/blocks/dream_grass_block.json`
- `src/main/resources/data/dreamsdimensions/loot_tables/blocks/dream_infused_stone.json`
- `src/main/resources/data/dreamsdimensions/loot_tables/blocks/dream_ore.json`
- `src/main/resources/data/dreamsdimensions/loot_tables/blocks/dream_sand_block.json`
- `src/main/resources/data/dreamsdimensions/loot_tables/blocks/dream_shimmer_block.json`
- `src/main/resources/data/dreamsdimensions/loot_tables/blocks/oneiric_core_block.json`
- `src/main/resources/data/dreamsdimensions/loot_tables/blocks/serene_stone.json`

## Apêndice C: worldgen (por feature)
- **Biome modifier:** `src/main/resources/data/dreamsdimensions/worldgen/biome_modifier/dream_ore_overworld.json`
- **Configured features:**
  - `src/main/resources/data/dreamsdimensions/worldgen/configured_feature/dream_ore.json`
  - `src/main/resources/data/dreamsdimensions/worldgen/configured_feature/deepslate_dream_ore.json`
- **Placed features:**
  - `src/main/resources/data/dreamsdimensions/worldgen/placed_feature/dream_ore.json`
  - `src/main/resources/data/dreamsdimensions/worldgen/placed_feature/deepslate_dream_ore.json`

## Apêndice D: chaves de tradução relevantes
- Arquivos:
  - `src/main/resources/assets/dreamsdimensions/lang/en_us.json`
  - `src/main/resources/assets/dreamsdimensions/lang/pt_br.json`
- Chaves de item/bloco encontradas:
  - `item.dreamsdimensions.dream_dust`
  - `item.dreamsdimensions.oneiric_awakener`
  - `block.dreamsdimensions.dream_grass_block`
  - `block.dreamsdimensions.blue_dream_grass`
  - `block.dreamsdimensions.serene_stone`
  - `block.dreamsdimensions.blue_dream_stone`
  - `block.dreamsdimensions.blue_dream_cobblestone`
  - `block.dreamsdimensions.dream_shimmer_block`
  - `block.dreamsdimensions.dream_glow_moss`
  - `block.dreamsdimensions.dream_ore`
  - `block.dreamsdimensions.deepslate_dream_ore`
  - `block.dreamsdimensions.dream_infused_stone`
  - `block.dreamsdimensions.oneiric_core_block`
  - `block.dreamsdimensions.dream_dirt_block`
  - `block.dreamsdimensions.blue_dream_dirt`
  - `block.dreamsdimensions.dream_sand_block`
- Chaves de bloco **NÃO ENCONTRADO**:
  - `block.dreamsdimensions.dream_flower_block`
  - `block.dreamsdimensions.dream_log_block`
  - `block.dreamsdimensions.dream_leaves_block`
