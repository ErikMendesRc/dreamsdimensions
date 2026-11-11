# TASK 5.6 — Economia e Recursos do Overworld

**Data:** 2026-11-10 · **Branch:** `migration/minecraft-26.3` · **Baseline:** `0be3e4cf03ae62b9ace51e936aca5227b3557c3c`
**Stack:** Minecraft 26.3, NeoForge 26.3.0.101, Java 25, ModDevGradle 2.0.148, GeckoLib 5.5.7, Curios 17.0.1+26.3.

## 1. Objetivo e contrato econômico

O Overworld é a **origem natural exclusiva** do Pó Onírico. Minerar minério onírico com ferramentas válidas produz Dream Dust. O Colar dos Sonhos é investimento **não consumível** em 3 Pós. Pedra Infundida → Núcleo Onírico → Despertador representam a segunda fase, cujo custo completo é **12 Pós por Despertador**, consumido uma única vez após o retorno confirmado (Task 5.4). A morte em sonhos não é retorno gratuito (Task 5.5). O jogador sem Despertador recuperará **um Despertador inteiro** nas ruínas raras da Task 5.7; **não** haverá mineração nem crafting de Dream Dust nos sonhos.

**Nesta task não mudamos:** preço, receita do Despertador, chance do Colar, motor de teleporte, respawn, spawn de mobs, worldgen Dreamscape/Azul, geografia, novos materiais/dimensões/loot de baús.

## 2. Auditoria de configured/placed features e biome modifier

| ID | Target de substituição | Tamanho da feature | Tentativas/chunk | Distribuição vertical | Air-exposure discard |
|---|---|---:|---:|---|---:|
| `dreamsdimensions:dream_ore` | `#minecraft:stone_ore_replaceables` | 6 | 2 | trapezoid Y=-16..64 | 0 |
| `dreamsdimensions:deepslate_dream_ore` | `#minecraft:deepslate_ore_replaceables` | 5 | 2 | trapezoid Y=-64..-16 | 0 |

Estes são os **parâmetros declarados nos datapacks, não minérios efetivamente observados**. Cada tentativa pode falhar, ser truncada por terreno/bioma, chegar a zero blocos ou resultar em veio parcial. `discard_chance_on_air_exposure = 0` não garante que o minério ficará visível em uma caverna; significa que a feature não aplica descarte extra por exposição.

O único modifier de inclusão desses dois minérios é:
`data/dreamsdimensions/neoforge/biome_modifier/dream_ore_overworld.json`

```json
{
  "type": "neoforge:add_features",
  "biomes": "#c:is_overworld",
  "features": ["dreamsdimensions:dream_ore", "dreamsdimensions:deepslate_dream_ore"],
  "step": "underground_ores"
}
```

**Decisão:** preservar os valores originais. O diretório segue a documentação NeoForge, sem `DeferredRegister`/evento Java que duplica minério. A tag `#c:is_overworld` é o filtro de biomas do Overworld, incluindo outros mods quando respeitam a convenção. Os worldgens do Dreamscape e Campo Azul não recebem essas placed features. **O servidor carregando os datapacks é evidência de parsing/registro, não de distribuição real em chunks.**

Fonte: https://docs.neoforged.net/docs/worldgen/biomemodifier/ (guia tem exemplos 26.1), https://docs.neoforged.net/primer/docs/26.3/ (migração 26.3).

## 3. Ferramentas, loot, Fortune e Silk Touch

Tanto `dream_ore` quanto `deepslate_dream_ore` estão em:
- `data/minecraft/tags/block/mineable/pickaxe.json`
- `data/minecraft/tags/block/needs_iron_tool.json`

As propriedades de ambos os blocos chamam `.requiresCorrectToolForDrops()`; não existe comparação de nomes de picaretas em Java. Resultados esperados: sem ferramenta, madeira e pedra **não** rendem Pó; ferro, diamante, netherita e ferramentas equivalentes de outros mods com tags/tier válido rendem Pó. Confirmar uso real de ferramenta em Survival, incluindo modpacks e Adventure.

Os dois loot tables são `minecraft:block` com 1 pool e alternativa exclusiva:
1. `condition: minecraft:tool/can_silk_touch` → o próprio bloco do minério (sem Pó).
2. Caso contrário, `dreamsdimensions:dream_dust`, com `modifier: minecraft:apply_bonus` / `formula: minecraft:ore_drops` / `enchantment: minecraft:fortune`, depois `minecraft:explosion_decay`.

Essa estrutura é compatível com o formato 26.3, que usa `condition` e `modifier` no lugar das arrays antigas `conditions`/`functions`. Os loot tables de `dream_infused_stone` e `oneiric_core_block` devolvem exatamente o bloco ao quebrar com ferramenta válida, preservando os materiais caros e evitando transformações que gerem Pó extra. Não foi introduzido loot modifier global.

**Pendente in-game:** Fortune I/II/III, Silk Touch, explosões, XP 1–3 e comparação madeira/pedra/ferro. JSON/CI prova formato e intenção, não resultado de drop por ferramenta.

Fontes: https://docs.neoforged.net/docs/resources/server/loottables/ e https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3

## 4. Custo completo das receitas (calculado recursivamente dos JSONs)

| Receita | Materiais diretos | Total indireto de Dream Dust |
|---|---|---:|
| Colar | 3 cobre + 3 Dream Dust + 1 ametista | **3** |
| Pedra Infundida | 2 pedra vanilla + 2 Dream Dust | **2** |
| Núcleo Onírico | 4 Pedras Infundidas + 4 Dream Dust + 1 relógio | **12** |
| Despertador | 1 Núcleo + 1 pena + 1 frasco de vidro | **12** |

O custo expandido para **um Despertador** é **12 Dream Dust + 8 pedras + 1 relógio + 1 pena + 1 frasco de vidro**. Relógio vanilla acrescenta **4 lingotes de ouro e 1 redstone**; o frasco de vidro pode ser obtido por fabricação vanilla. O custo expandido do **Colar** é **3 pós + 3 lingotes de cobre + 1 ametista**. Pedra e núcleo não se consomem quando colocados, mas são consumidos como ingredientes na próxima receita.

| Preparação | Dream Dust total | Quantidade de Despertadores |
|---|---:|---:|
| 1 Colar | 3 | 0 |
| 1 Despertador | 12 | 1 |
| Colar + 1 Despertador | 15 | 1 |
| Colar + 2 Despertadores | 27 | 2 |
| Colar + 3 Despertadores | 39 | 3 |

Fórmula: `dust_total = 3 + 12 * N`, `N` é o número de Despertadores fabricados e consumíveis. Para **nova expedição após já ter Colar**, o custo adicional é **12 Pós**, não 15. Nenhum ciclo de crafting produz minério ou Pó a partir de blocos infundidos; a cadeia é acíclica.

O script `scripts/ci/check_overworld_resource_chain.py` calcula esses custos **recursivamente a partir das receitas** (não estimativa solta), rejeita ingredientes inexistentes, detecta ciclos, audita links de receita, loot e tags e protege a decisão de custo.

## 5. Recipe Book: descoberta sem spoilers

Três `advancement` JSON invisíveis (sem `display`/toast/quest) usam `minecraft:inventory_changed` e `rewards.recipes` para a seguinte progressão:

| Descoberta (no inventário) | Receitas liberadas no Recipe Book |
|---|---|
| Primeiro `dream_dust` | `dream_necklace`, `dream_infused_stone` |
| Primeira `dream_infused_stone` | `oneiric_core_block` |
| Primeiro `oneiric_core_block` | `oneiric_awakener` |

Isso **não bloqueia o crafting manual** antes do advancement e não dá itens gratuitamente. É somente orientação visual no Recipe Book. O carregamento dos advancements/receitas precisa passar pelo servidor 26.3; a experiência visual e comportamento em save antigo precisam de teste manual.

## 6. Diagnóstico real de worldgen (somente dev/CI)

Foi criado `OverworldOreAuditCommand`, **registrado apenas** com `-Ddreamsdimensions.enableOreAudit=true` no arranque do servidor de laboratório. O comando requer permissão de operador e usa a dimensão `minecraft:overworld` **fixa**, nunca escaneia outros mundos. Não gera chunks, não quebra minério, não altera loot, não adiciona tickets nem se executa a cada tick. Recebe `chunk_x`, `chunk_z`, `chunks_per_side` (1..8) e **recusa chunks não carregados**. É um scan síncrono e limitado de no máximo 64 chunks por chamada, entre Y=-64 e Y=64, adequado apenas para ambiente de teste. Não utilizar em produção com tickrate ativo.

Comando de laboratório (não está disponível em jogo normal sem a system property):

```mcfunction
/dream_ore_audit -4 -4 8
```

O script `scripts/ci/audit_overworld_ore_runtime.py` conecta por RCON local, verifica `/seed`, usa `/forceload add` para **carregar chunks novos no mundo descartável do CI** e dispara quatro scans de 8×8 chunks em regiões **não sobrepostas**, totalizando 256 chunks por seed nas quatro seeds `14011`, `20261108`, `-904732`, `73199013`.

Regiões `chunkX/chunkZ`, escolhidas **antes** da geração: `(-4,-4)`, `(64,-4)`, `(-68,64)`, `(4,-68)`. **Essas são coordenadas de AMOSTRA planejadas, não locais onde minérios foram encontrados.** O script remove os tickets `forceload` depois de cada medição. A CI grava artefatos `ore-audit-seed-<seed>.json` e log do servidor `DREAM_ORE_AUDIT_HIT` com **posições reais somente quando observadas**.

O comando calcula:
- Chunks amostrados, chunks com algum minério e quantidades de blocos do minério comum/profundo.
- Contagem de faces adjacentes a ar por bloco (indicador de **exposição ao ar**, não de acessibilidade real).
- Faixas de Y (-64..-33, -32..-1, 0..31, 32..64); nomes de bioma por localização do minério nos logs.
- Até dez posições reais por região (primeiras ocorrências) no log, identificadas com tipo, biome e Y; são minérios, **não pontos seguros para /tp**.
- Tempo da operação na CI.

**Limitações metodológicas:** quatro regiões espaciais distintas não equivalem a uma amostra estatística aleatória nem garantem quatro biomas diferentes; `exposed` não garante uma caverna alcançável; veios conectados nas bordas e rendimento real em Dream Dust/XP não são contados; o diagnóstico registra a posição exata do bloco mas **não** determina um local seguro de chegada próximo. A configuração de `count=2` por chunk não implica dois veios, nem 256 amostras um rendimento garantido. Se a CI não completar todas as regiões, relatar `NOT RUN/INCOMPLETE` para a seed, jamais preencher resultados simulados.

## 7. Balanceamento: critérios e decisão

**Decisão inicial: nenhum ajuste nos pesos de minério.** O tamanho 6/5, count2/2, distribuição trapezoidal e yield 1 Pó sem Fortune devem ser mantidos até observar os 256 chunks por seed e ao menos um teste real de Survival. Mudar vários parâmetros de geração sem dados impossibilitaria comparar antes/depois com justiça.

Não estabelecer meta fixa de minutos; medir em cada build/seed:
- **Blocos gerados:** diagnóstico RCON acima.
- **Blocos expostos:** subconjunto dos blocos gerados adjacentes a ar, não sinônimo de encontrados.
- **Blocos encontrados pelo jogador:** caderno de campo de Survival, com posição e tempo.
- **Pó obtido:** contagem do inventário após mineração com e sem Fortune.
- **Acesso/custo:** tempo real para acumular 3, 12 e 15 Pós; diferenças entre cavernas/túneis, ferramentas ferro/Fortune III, disponibilidade de ametista/cobre e ouro/redstone.

Mundos antigos só receberão novas gerações de minério em **chunks ainda não gerados**. A Task não reescreve saves, retrogera minério, modifica distribuição já carregada ou destrói construções.

## 8. Comandos de laboratório copiáveis

Atualizar repo:

```powershell
git checkout migration/minecraft-26.3
git pull origin migration/minecraft-26.3
```

**Mundo descartável**, onde alteração de bloco e inventário não prejudiquem construções:

```mcfunction
/seed
/gamemode creative
/give @s minecraft:iron_pickaxe 1
/give @s dreamsdimensions:dream_ore 16
/give @s dreamsdimensions:deepslate_dream_ore 16
/gamemode survival
```

Para testar uma amostra de cada drop, num espaço **ao lado do jogador** e apenas depois de verificar que está livre:

```mcfunction
/setblock ~2 ~ ~ dreamsdimensions:dream_ore
/setblock ~3 ~ ~ dreamsdimensions:deepslate_dream_ore
/clear @s dreamsdimensions:dream_dust 0
```

Não use `/setblock ~ ~ ~` nos próprios pés. Com picareta de ferro, os blocos devem conceder Pó (sem Silk Touch); repita com ferramenta de pedra, Fortune III e Silk Touch em blocos novos. `/clear ... 0` é usado para consultar quantidade **sem remover** (confirmar output no jogo). Teste recuperação de Pedra Infundida/Núcleo corretamente minerados:

```mcfunction
/give @s dreamsdimensions:dream_infused_stone 1
/give @s dreamsdimensions:oneiric_core_block 1
```

Para laboratório de crafting (não confundir `/give` com prova de obtenção natural):

```mcfunction
/give @s dreamsdimensions:dream_dust 15
/give @s minecraft:copper_ingot 3
/give @s minecraft:amethyst_shard 1
/give @s minecraft:stone 8
/give @s minecraft:clock 1
/give @s minecraft:feather 1
/give @s minecraft:glass_bottle 1
```

Craftar Colar → Pedra Infundida ×4 → Núcleo → Despertador (sem `/give` dos itens finais), usando bancada 3×3. Abrir Recipe Book após coletar um Pó pela primeira vez (ideal testar em jogador novo sem receitas já desbloqueadas).

**Teleporte sem risco por coordenada inventada:** no Overworld, preferir observar minerando naturalmente (minério não tem `/locate`). Para inspeção da região de amostragem predefinida, usar **Spectator**, não Survival:

```mcfunction
/gamemode spectator
/execute in minecraft:overworld run tp @s 8 128 8
```

**Atenção:** a posição `8 128 8` é uma referência aérea para observar a região de teste e **não uma posição segura homologada**; em Spectator não há risco de queda e o jogador pode inspecionar o subsolo. Ela **não afirma existência de minério ali**. Para minérios efetivamente localizados, usar os `DREAM_ORE_AUDIT_HIT` do log de CI com seed idêntica e identificar um piso vizinho seguro antes de usar Survival.

**Jamais usar** `/locate` para minérios; `/place feature` seria geração forçada, não worldgen natural.

## 9. Status dos testes (atualizar somente com evidência)

| Camada | Estado inicial |
|---|---|
| **STATIC DATA VALIDATION** | Script ampliado; **CI PENDENTE** |
| **BUILD/UNIT** | Alterações Java do comando opt-in; **CI PENDENTE** |
| **SERVER SMOKE** | Carregamento de recursos, loot e recipe advancements; **CI PENDENTE** |
| **WORLDGEN RUNTIME VERIFICATION** | Script RCON e comando opt-in criados; **PENDENTE** de 4 amostras/seed |
| **MANUAL SURVIVAL** | **NOT RUN**: picaretas, Fortune, Silk, receita no Recipe Book e mineração legítima |
| **MULTIPLAYER** | **NOT RUN**: economia em servidor/Curios com dois usuários |

**Não declarar completa a tarefa sem resultado real de geração**. Se o ambiente CI falhar, preservar logs e reportar claramente o bloqueio. Resultados verificados por seed serão publicados em [OVERWORLD_ORE_DISTRIBUTION_TASK_5_6.md](OVERWORLD_ORE_DISTRIBUTION_TASK_5_6.md) somente se houver medições reais.

## 10. Alterações e dependências seguintes

**Alterações de escopo 5.6:** três advancements invisíveis de unlock, script de CI econômica e worldgen/loot, script RCON de 256 chunks/seed, comando de diagnóstico opt-in sem efeito gameplay, registro opcional no bootstrap, workflow de 4 seeds e docs de matriz/roadmap. **Não modificou** os arquivos de minério, suas propriedades, biome modifier, tags, loot nem recipes porque não foi comprovado defeito nesses dados.

**Próxima tarefa:** Task **5.7**, baús raros com o **Despertador completo** recuperável dentro de cada dimensão (sem produzir Dream Dust local), com distribuição real, proteção contra exploits e loot persistente. O Survival público fica desabilitado por padrão até que 5.7 e testes manuais 5.5/5.6/5.7 sejam concluídos.
