# TASK 7.3 — Pedra Marcada (F0a / B01)

**DreamsDimensions · Minecraft 26.3 · NeoForge 26.3 · Java 25**  
**Branch:** `migration/minecraft-26.3` — sem merge na `main`.  
**Data:** 12/11/2026.  
**Estado:** implementação enviada; CI e Survival são gates separados.

## 1. Objetivo e papel no gameplay

A Pedra Marcada (`dreamsdimensions:marked_stone`) é a fonte geológica da família F0 descrita na Game Design Bible 6.0, na Geologia 6.2 e na Art Bible 6.7. Seu **consumidor** é construção de paredes, pisos, ruínas e bases. A etapa posterior vai incluir a transformação **1:1** em Pedra Marcada Lapidada, via stonecutter vanilla.

A rocha tem estrias discretas oblíquas, cinza-quente com toque ardósia, sem luz própria, não é minério e **não gera Dream Dust**, XP nem fragmentos extras.

Esta é exclusivamente a etapa **F0a — bloco/asset/obtenção manual**. Ainda não existe formação natural no Overworld nem receita/lapidação. F0b (Task 7.4) integrará a geração natural e o processamento com evidência de mundo real. Não tratar testes por comando como descoberta em Survival.

## 2. Conteúdo integrado

| Área | Arquivo ou regra |
| --- | --- |
| Registry de bloco | `ModBlocks.MARKED_STONE`, ID `dreamsdimensions:marked_stone` via `DeferredRegister.Blocks` |
| Física | `Blocks.TUFF` como material de referência, dureza `1.5`, resistência `6.0`, cor de mapa de rocha |
| Ferramenta | `data/minecraft/tags/block/mineable/pickaxe.json` mais `needs_stone_tool.json`; stone ou melhor |
| BlockItem | `ModItems.MARKED_STONE_ITEM`, `registerSimpleBlockItem` |
| Inventário Creative | uma entrada `ModCreativeTabs`, sem repetição |
| Loot | `loot_table/blocks/marked_stone.json`, 1 bloco de si mesmo + `explosion_decay` |
| Blockstate | variante vazia para `dreamsdimensions:block/marked_stone` |
| Block model | `minecraft:block/cube_all` com textura para `all` e `particle` |
| Item model | `assets/dreamsdimensions/items/marked_stone.json` + `models/item/marked_stone.json` |
| Arte | `textures/block/marked_stone.png` — **PNG original RGBA 16×16**, faces sem emissive |
| Idiomas | `block.dreamsdimensions.marked_stone` em `en_us` e `pt_br` |
| Verificação | contrato 7.2 + teste estático dedicado + colocação nativa em CI |

### Escopo negado nesta tarefa

Não adicionar: `cut_marked_stone`, mineração natural, placed/configured features, biome modifiers, worldgen retroativo, flora, novas ferramentas, estruturas, estações, receitinhas de Stone, escadas, lajes, muros ou outro minério.

Não tocar no Dream Ore, em suas taxas de geração e drops, nas ruínas, na progressão do Colar/Despertador ou no comportamento de mobs/dimensões.

### Por que um bloco próprio?

A versão natural da Pedra Marcada será um vestígio reconhecível do Overworld e um material arquitetônico sem custo de Dream Dust. O bloco está preparado para ser encontrado e processado futuramente; o seu registro agora permite validar textura, ferramenta, persistência e recuperação sem alterar geração do mundo.

## 3. Arte e critérios de aceitação

A textura foi desenhada especificamente para esta task, em 16×16; tons foscos aproximados de stone/tuff, fraturas diagonais descontínuas e sinais cinza-azulados discretos. Não é recorte, reamostragem ou simples alteração de matiz de textura vanilla. Foi comparada num preview **3×3** em escala nearest-neighbor antes do commit.

O check estático verifica cabeçalho e chunks PNG/CRC, compressão, dimensões e formato RGBA8; **não prova qualidade visual efetiva em Minecraft**. Revisão manual exigida:
- parede 3×3 e 5×5 para detectar repetição artificial, seams e contraste;
- pedra isolada em stone, tuff e andesite, em sol/sombra;
- inventário/ícone e partículas de quebra;
- captura real após instalação do mod no cliente.

## 4. Contratos de drop, ferramenta e economia

`marked_stone` não é minério. O loot table possui um pool, uma entrada, um drop do próprio bloco; Fortune não multiplica o material, e Silk Touch não é necessário para recuperá-lo.

O `mineable/pickaxe` e `needs_stone_tool` determinam a intenção de **picareta de pedra ou superior**. Por segurança, o `BlockBehaviour` também usa `requiresCorrectToolForDrops()`. Teste real em Survival é obrigatório para provar que:
- picareta de madeira não recupera o bloco;
- picareta de pedra e ferro recuperam **um** bloco;
- com Fortune/Silk Touch o rendimento não aumenta;
- ao recolocar e minerar, não há duplicação.

Em Creative, o bloco aparece uma única vez na aba Dreams Dimensions. A 7.3 **não disponibiliza fonte natural**: `/give` é só para QA. Não criar receita gratuita que poderia contornar a 7.4.

## 5. Testes automatizados

### Estático e build

Executar na raiz do projeto Windows:

```powershell
git switch migration/minecraft-26.3
git pull origin migration/minecraft-26.3
python scripts/ci/test_content_contract.py
python scripts/ci/check_content_contract.py --report qa-content-contract.json
python scripts/ci/check_marked_stone.py --report qa-marked-stone-static.json
.\gradlew.bat --no-daemon clean build
.\gradlew.bat runClient
```

O script `check_marked_stone.py` reutiliza o contrato T7.2 e acrescenta invariantes do B01: registry, modelo, ferramenta de pedra, 16×16 RGBA válido, loot único, nenhum Dust, nenhuma recipe ou worldgen prematura. Ele exporta relatório `STATIC_CHECK` compatível com o schema `dreamsdimensions.qa.v1` da 7.1.

### CI runtime

O workflow atual executa Java, checks de conteúdo e servidor dedicado nas quatro seeds **14011**, **20261108**, **-904732** e **73199013**.

O script `audit_marked_stone_runtime.py` usa comandos **nativos** num Overworld descartável: força apenas o chunk (0,0), coloca um bloco temporário em (0,300,0), verifica o ID real com `execute if block ... run seed`, remove o bloco de teste e libera o forceload em `finally`. Isso mede apenas **registro e colocação runtime**, não loot real ou worldgen natural.

Artifacts: `qa-marked-stone-static-seed-*.json` e `marked-stone-runtime-seed-*.json`, com status e tipo de evidência independentes.

## 6. Comandos Minecraft — testes visuais e mecânicos

Usar mundo de testes e cheats habilitados (ou conta OP). **A Pedra Marcada não aparece naturalmente neste estágio.**

```mcfunction
/give @s dreamsdimensions:marked_stone 64
/give @s minecraft:stone_pickaxe 1
/give @s minecraft:wooden_pickaxe 1
/give @s minecraft:iron_pickaxe 1
/gamemode creative
```

Colocar uma parede de 3×3 e outra 5×5 junto de `minecraft:stone`, `minecraft:tuff` e `minecraft:andesite`. Verificar repetição, juntas e model no inventário.

Antes de Survival, escolher chão firme e seguro; usar o inventário, bloco colocado no chão, depois:

```mcfunction
/gamemode survival
```

Fazer testes de mineração com madeira, pedra, ferro e encantamentos; verificar **número de itens realmente recolhidos**. Salvar/recarregar e verificar que o bloco não desaparece e que permanece minerável. Num mundo antigo, `marked_stone` só existe se colocado manualmente (sem retro-gen).

### Teleporte para inspeção de compatibilidade com dimensões

Coordenadas genéricas, **apenas Spectator**; não são pontos seguros em Survival.

```mcfunction
/gamemode spectator
/execute in dreamsdimensions:dreamscape run tp @s 0 180 0
/execute in dreamsdimensions:campo_onirico_azul run tp @s 0 180 0
/execute in minecraft:overworld run tp @s 0 120 0
```

Verificar que o item pode ser colocado nessas dimensões; a Task 7.3 **não** gera ocorrências nelas.

### QA 7.1 — diagnóstico opcional

```mcfunction
/dreamqa status
/dreamqa enable
/dreamqa snapshot
/dreamqa disable
```

O gravador de spawn/teleporte não instrumenta automaticamente a mineração da Pedra Marcada. Para drops, usar observação real e screenshots/logs; não atribuir `PASS` à mineração somente porque o snapshot foi exportado.

## 7. Critérios de aprovação

- [x] ID do bloco, BlockItem, Creative, tags, models, texto, textura, loot e regressões implementados.
- [ ] CI do commit final aprovada nas quatro seeds e servidor real.
- [ ] Render/tiling aprovado no cliente.
- [ ] Mineração com pedra/ferro e ausência de drop com madeira validada em Survival.
- [ ] Persistência e compatibilidade após reiniciar confirmadas.
- [ ] F0b / Task 7.4: geração natural visível + Pedra Marcada Lapidada 1:1 com Stonecutter.

Os checks nativos/estáticos não podem promover os itens de Survival para `PASS`.

## 8. Fontes e continuidade

- [Art Bible 6.7](OVERWORLD_VISUAL_DIRECTION_TEXTURES_MODELS_ASSETS_TASK_6_7.md)
- [Geologia F0 / Task 6.2](OVERWORLD_RESOURCES_GEOLOGY_FLORA_BLOCKS_TASK_6_2.md)
- [Crafting 6.6](OVERWORLD_CRAFTING_PROCESSING_PROGRESSION_TASK_6_6.md)
- [Roadmap 6.9](OVERWORLD_EXECUTIVE_IMPLEMENTATION_ROADMAP_TASK_6_9.md)
- [Contrato de assets 7.2](DREAMSDIMENSIONS_TASK_7_2.md)
- [NeoForge 26.3 models](https://docs.neoforged.net/docs/resources/client/models/)
- [NeoForge 26.3 client items](https://docs.neoforged.net/docs/resources/client/models/items/)
- [NeoForge 26.3 loot](https://docs.neoforged.net/docs/resources/server/loottables/)
