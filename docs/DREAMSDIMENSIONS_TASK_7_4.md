# Task 7.4 — F0b: geração natural e lapidação da Pedra Marcada

**Projeto:** DreamsDimensions · Minecraft Java 26.3 · NeoForge 26.3 · Java 25  
**Branch:** `migration/minecraft-26.3`. **Não realizar merge na `main`.**  
**Tipo:** primeira vertical de geologia útil do Overworld.  
**Status:** implementação enviada; validações estáticas, CI runtime e Survival são evidências separadas.

## Objetivo de gameplay

Fechar a primeira ligação entre **explorar rochas do Overworld, minerar, processar e construir**, sem exigir Colar, Despertador, Dream Dust ou dimensão onírica.

**Grafo:** rocha natural visível/localizável → picareta de pedra ou superior → `marked_stone` → Stonecutter vanilla **1:1** → `cut_marked_stone` → construção na base.

- `marked_stone`, criado na 7.3, é fonte natural e material bruto de construção.
- `cut_marked_stone` é um segundo bloco completo, com textura própria 16×16, item de inventário, estado, loot e tradução.
- A Stonecutter é a **única** forma de acabamento desta task; sem crafting alternativo nem outro rendimento.
- O material **não** é minério de Pó Onírico, não concede XP, não tem Fortune multiplicador, não exige exploração dos sonhos e não pode render bloco duplicado.

## Integração nativa ao Minecraft 26.3

Nenhum `Feature` Java novo foi introduzido; a distribuição usa recursos vanilla e o `neoforge:add_features` em `data/dreamsdimensions/neoforge/biome_modifier/marked_stone_overworld.json`, filtrado por `#c:is_overworld`.

### Depósitos subterrâneos

- `worldgen/feature/marked_stone.json`: feature tipo `minecraft:ore`, substituindo exclusivamente `minecraft:stone_ore_replaceables`, `size=12`, com `discard_chance_on_air_exposure=0`.
- `worldgen/placed_feature/marked_stone.json`: `count=1`, `in_square`, distribuição `trapezoid` de **Y=-8 até 112**, com filtro por bioma.
- Esta é uma rocha de construção, não minério raro de progressão. **Tentativa por chunk não equivale a ocorrência garantida**, e `size` não determina um total de blocos fixo.

### Afloramentos da superfície rochosa

- `worldgen/feature/marked_stone_outcrop.json`: vanilla `minecraft:netherrack_replace_blobs`, com target estrito `minecraft:stone`, estado resultante `dreamsdimensions:marked_stone` e raio aleatório **1–2 blocos**.
- `worldgen/placed_feature/marked_stone_outcrop.json`: `rarity_filter chance=9`, `in_square`, heightmap `WORLD_SURFACE_WG` e filtro de bioma.
- A intenção é que superfícies de pedra exposta produzam pequenas marcas naturais, sem criar torres, minas artificiais, destruição de construções de jogador ou vegetação mágica.
- O algoritmo vanilla pode não encontrar pedra em toda tentativa; não interpretar `chance=9` como uma formação confirmada a cada nove chunks.

**Fronteiras:** não registrar em Dreamscape/Campo Azul; não alterar `dream_ore`, `deepslate_dream_ore`, `worldgen` das dimensões, ruínas ou estruturas. Nenhum *retro-gen*: mudanças só surgem nos chunks gerados após instalar a versão.

**Calibração:** frequência inicial é hipótese sujeita a dados de quatro seeds. Não ajustar por intuição com base em um único screenshot.

## Material lapidado

| Registro | Resultado |
| --- | --- |
| Block | `ModBlocks.CUT_MARKED_STONE` → `dreamsdimensions:cut_marked_stone` |
| BlockItem | `ModItems.CUT_MARKED_STONE_ITEM` |
| Creative tab | uma entrada, próxima à Pedra Marcada |
| Física | Tuff-like, dureza 1.5, resistência 6.0 |
| Ferramentas | `mineable/pickaxe` + `needs_stone_tool` |
| Client item 26.3 | `assets/dreamsdimensions/items/cut_marked_stone.json` |
| Modelos | `models/block` e `models/item`; bloco `cube_all` |
| Textura | `textures/block/cut_marked_stone.png`, **RGBA 16×16**, polimento e borda geométrica |
| Loot | `loot_table/blocks/cut_marked_stone.json`: um bloco de si mesmo, sem Dream Dust |
| Nomes | `pt_br` “Pedra Marcada Lapidada”; `en_us` “Cut Marked Stone” |
| Recipe | `recipe/cut_marked_stone_from_marked_stone_stonecutting.json` |

**Rendimento 1:1 confirmado na configuração:** um bloco entra e um sai. O motor do jogo valida o JSON durante o carregamento do servidor; a experiência no menu Stonecutter continua exigindo teste manual.

## QA e provas automatizadas

### Estática

`scripts/ci/check_marked_stone_7_4.py` valida 7.2 + 7.3 e os recursos 7.4: registros, BlockItems, assets/PNG, texturas diferentes, Creative Tab, loot, tradução, requisito de picareta de pedra, receita stonecutting exata, ausência de transformação reversa e ambas as features com parâmetros delimitados e dono Overworld. O antigo `check_marked_stone.py` foi atualizado para permitir sua evolução sem perder a proteção F0a.

### Geração real — não confundível com comandos

`MarkedStoneAuditCommand` é read-only, exclusivo de administração e habilitado apenas com `-Ddreamsdimensions.enableMarkedStoneAudit=true`; sem esta flag **não há comando** em produção. Ele verifica chunks previamente carregados e contabiliza blocos `marked_stone`, chunks com ocorrência, faces adjacentes ao ar e amostra de coordenadas realmente observadas. Não dá `/setblock` nem carrega chunks por si mesmo.

O harness `scripts/ci/audit_marked_stone_worldgen_runtime.py` usa RCON num mundo **CI descartável**, escolhe quatro regiões de **4×4 chunks** por seed (=64 chunks/seed), força os chunks apenas durante o ensaio e sempre remove os tickets em `finally`.

Seeds: `14011`, `20261108`, `-904732` e `73199013`.

Cada execução gera `marked-stone-worldgen-seed-<seed>.json` com `RUNTIME_OBSERVED`; `aggregate_marked_stone_worldgen.py` produz a agregação de quatro seeds e marca Survival, Stonecutter GUI, trajetória até descoberta e persistência como **NOT_RUN**. Amostra é de regiões fixas, não representa estatística aleatória global dos biomas. Faces com ar não provam acesso seguro. O auditor existente de Dream Ore segue independente.

**Gate de CI:** todos os quatro testes de servidor precisam ter ocorrido, sem falhas de registry, recipe, dimensões ou datapacks; cada seed deve conter algum `marked_stone` natural e a amostra conjunta precisa ter exposição ao ar. Uma seed com zero exposição individual gera WARN; não se deve falsificar status de descoberta.

## Como atualizar e testar no Windows

Na raiz do repositório:

```powershell
git switch migration/minecraft-26.3
git pull origin migration/minecraft-26.3
python scripts/ci/test_content_contract.py
python scripts/ci/check_content_contract.py --report qa-content-contract.json
python scripts/ci/check_marked_stone.py
python scripts/ci/check_marked_stone_7_4.py --report qa-marked-stone-7-4.json
python scripts/ci/test_marked_stone_worldgen_aggregate.py
.\gradlew.bat --no-daemon clean build
.\gradlew.bat runClient
```

**Atenção:** em mundos antigos, explorar terreno **ainda não gerado**. Não apagar saves para testar, nem supor que uma coordenada observada pela CI existe num mapa com outra seed/versão.

### Comandos rápidos Minecraft (cheats/OP)

**Blocos e Stonecutter:**

```mcfunction
/give @s dreamsdimensions:marked_stone 32
/give @s dreamsdimensions:cut_marked_stone 32
/give @s minecraft:stonecutter 1
/give @s minecraft:stone_pickaxe 1
/give @s minecraft:wooden_pickaxe 1
/gamemode creative
```

Monte parede 3×3 e 5×5 de cada material ao lado de `minecraft:stone`, `minecraft:tuff` e `minecraft:andesite`. Confira faces, repetição, cor, corte visível e inventário. Use Stonecutter com **1 Pedra Marcada** e observe **1 Pedra Marcada Lapidada**; repetir com uma pilha e verificar perda de 1 unidade de entrada por unidade produzida.

Depois, em **chão seguro**, testar mineração com madeira (sem drop), pedra e ferro (um bloco), Fortune/Silk (sem duplicação), salvar/recarregar, Creative Tab, dois jogadores e posicionamento de blocos. O comando `/give` **não prova obtenção em Survival**.

**Teleporte para inspeção do Overworld:**

```mcfunction
/gamemode spectator
/execute in minecraft:overworld run tp @s 0 140 0
```

**Localização observada em CI (somente seed correspondente):** consultar os artifacts `marked-stone-worldgen-seed-*` e usar o campo `first_exposed`. Para a seed `73199013`, a auditoria registrou uma face de `marked_stone` em **-49, 15, -33**, num mundo gerado sob o build dessa seed:

```mcfunction
/gamemode spectator
/execute in minecraft:overworld run tp @s -49 15 -33
```

Isso **não é destino de Survival seguro**; não usar em outro mundo nem interpretar como spawn garantido após retro-gen.

**Outras dimensões (verificar que não ganharam worldgen desse bloco):**

```mcfunction
/gamemode spectator
/execute in dreamsdimensions:dreamscape run tp @s 0 180 0
/execute in dreamsdimensions:campo_onirico_azul run tp @s 0 180 0
/execute in minecraft:overworld run tp @s 0 140 0
```

**Comando opcional de inspeção natural:** iniciar o jogo já com `-Ddreamsdimensions.enableMarkedStoneAudit=true`, tornar-se OP e usar `/marked_stone_audit -4 -4 4` **somente** com esses 16 chunks carregados; caso contrário o script informa `chunk_not_loaded`. Não fazer `forceload` em mundos de produção para contornar isso. A telemetria `/dreamqa` da 7.1 não registra worldgen automaticamente.

## Gate da Task 7.5

**Task 7.4 termina em CI_VERIFIED somente se os quatro testes e o relatório integrado passarem.** A fatia F0 completa fica `SURVIVAL_VERIFIED` apenas quando o jogador:

1. Encontra ao menos uma formação sem `/give` ou `/place`, num chunk novo;
2. Minera com picareta correta e retorna com o próprio bloco;
3. Usa Stonecutter real em 1:1 e reconhece visualmente o polimento;
4. Constrói algo, recupera a pedra e confirma persistência/reload;
5. Testa o comportamento com outro jogador e avalia custo para 64/256 blocos de construção.

Registrar seed, versão/commit, XYZ observados, biome, quantidade coletada, tempo de descoberta e screenshots. **Não confundir pixels/chunks observados com P50/P90 de jogadores.**

## Referências

- [Task 6.2 — Geologia e Pedra Marcada](OVERWORLD_RESOURCES_GEOLOGY_FLORA_BLOCKS_TASK_6_2.md)
- [Task 6.6 — Crafting 1:1](OVERWORLD_CRAFTING_PROCESSING_PROGRESSION_TASK_6_6.md)
- [Task 6.7 — Art Bible](OVERWORLD_VISUAL_DIRECTION_TEXTURES_MODELS_ASSETS_TASK_6_7.md)
- [Task 6.8 — Balanceamento e P50/P90](OVERWORLD_ECONOMY_RARITY_BALANCING_TASK_6_8.md)
- [Task 6.9 — Roadmap](OVERWORLD_EXECUTIVE_IMPLEMENTATION_ROADMAP_TASK_6_9.md)
- [Task 7.2 — Contrato de conteúdo/Assets](DREAMSDIMENSIONS_TASK_7_2.md)
- [Task 7.3 — Pedra Marcada F0a](DREAMSDIMENSIONS_TASK_7_3.md)
- [NeoForge — Biome Modifiers](https://docs.neoforged.net/docs/worldgen/biomemodifier/)
- [Minecraft 26.3 — Worldgen e features](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3)
- [NeoForge — Recipes](https://docs.neoforged.net/docs/resources/server/recipes/)
