# Worldgen e conteúdo existente

Este é o mapa de **recursos implementados e medidos**, não uma promessa de novos biomas, plantas ou POIs. Para IDs exatos, consultar `ModBlocks.java`, `ModItems.java`, `ModEntities.java` e recursos de `src/main/resources`.

## Dimensões atuais

**Campo Onírico Azul:** terreno, vegetação, formações, atributos de atmosfera e efeitos configurados; `dreamsdimensions:campo_onirico_azul`. Existem checks de vegetação, formações e atmosfera, e uma homologação inicial de servidor. A experiência visual e a exploração em Survival devem continuar sendo testadas no cliente.

**Dreamscape:** `dreamsdimensions:dreamscape`, terreno fragmentado, ilhas e travessias, ecossistema e atmosfera implementados. Os testes de ponte, ilhas e ruínas usam sondas reais; não confundir suporte geométrico com travessia segura pelo jogador.

**Status:** essas são apenas **duas primeiras dimensões**, não as únicas categorias/linhagens de sonhos admitidas para a evolução do mod.

## Overworld: minérios, material e processamento

| Fonte | Geração e uso | Prova |
| --- | --- | --- |
| `dreamsdimensions:dream_ore` | Minério Overworld, produz componente da economia onírica | Feature, biome modifier e amostragem RCON |
| `dreamsdimensions:deepslate_dream_ore` | Variante profunda | Feature, biome modifier e amostragem RCON |
| `dreamsdimensions:marked_stone` | Rocha natural em veios e afloramentos, minerável e construtiva | F0 7.3–7.5, worldgen real |
| `dreamsdimensions:cut_marked_stone` | Variante arquitetônica feita no Stonecutter | Receita nativa **1:1**, assets 16×16, loot nativo |
| `dreamsdimensions:dream_dust` | Material derivado da cadeia de minério | Itens/loot/receitas; drops em Survival manualmente pendentes |

**Medição Pedra Marcada — quatro seeds:** 1.942 blocos em 256 chunks verificados, 177 chunks com presença e 46 blocos com face adjacente ao ar. Não interpretar isso como tempo de descoberta, visibilidade humana garantida ou balanceamento final. Quatro seeds fixas: `14011`, `20261108`, `-904732` e `73199013`. Fonte: `scripts/ci/aggregate_marked_stone_worldgen.py` e `docs/DREAMSDIMENSIONS_TASK_7_5.md`.

**Medição Dream Ore:** `scripts/ci/audit_overworld_ore_runtime.py` examina 256 chunks por seed em quatro regiões fixas de 8×8; valores de baseline e metodologia em [Distribuição real dos minérios](OVERWORLD_ORE_DISTRIBUTION_TASK_5_6.md). Sem intervenção manual em saves de jogadores.

## Estruturas e loot

Existem ruínas nativas de recuperação específicas para as duas dimensões. Os arquivos NBT, estruturas, structure sets e loot tables são carregados pelo servidor; a QA confirmou starts e baús existentes em quatro seeds, porém **acesso em Survival, loot realmente aberto, persistência após restart e multiplayer** ainda não foram integralmente homologados.

O projeto **não** possui os POIs e recursos imaginados na antiga Game Design Bible 6.x: não declarar Líquen dos Vestígios, Argamassa, Câmara do Limiar, dezenas de estruturas e mobs propostos como implementados.

## Entidades atuais

- **Fragmento Errante:** entidade GeckoLib, população contextual de ilhas; mecânica não hostil de aproximação com comportamento específico.
- **Vigia Adormecido:** entidade GeckoLib, população contextual, IA e proteção/efeitos de comportamento próprios.

Mais detalhes sobre os padrões reutilizáveis em [Dream Mob Framework](DREAM_MOB_FRAMEWORK_TASK_4_4.md). Testes de população real, movimentação em cliente e multiplayer exigem jogadores.

## Limites de identidade do Overworld

A Pedra Marcada **ainda não tem uma mecânica exclusiva de sonhos**; é material natural de construção. O experimento inerte da antiga 7.6 foi removido na Task 8 e **não** gerou anomalias nem modificou o Overworld. Qualquer nova anomalia requer primeiro função de gameplay convincente, conexão com a temática geral dos sonhos (não apenas as duas dimensões atuais) e implementação validada, não um manifesto JSON.

## Refinamento visual Task 8

Nove superfícies naturais (grama, terra, rochas, areia e musgo existentes) possuem agora quatro variantes de orientação **apenas no blockstate do cliente**. Isso reduz padrões repetidos na aparência, **sem alterar a distribuição, a seed, a colisão ou a geração de mundo**. A Flor Onírica usa ícone plano vanilla-style e partículas da própria textura; a aba criativa usa Colar como ícone e categorias ordenadas. PNGs legados de alta resolução foram preservados até revisão artística real. Veja [Task 8](TASK_8_VISUAL_QUALITY_PASS.md).

## Ferramentas de diagnóstico

`scripts/ci/check_content_contract.py` cobre registros/client items/modelos/tags/loot/lang. `scripts/ci/check_marked_stone_7_4.py` verifica F0. Sondas de servidor incluem `audit_overworld_ore_runtime.py`, `audit_marked_stone_worldgen_runtime.py` e `audit_dream_ruins_runtime.py`. O teste de `/setblock` **não** comprova geração natural.
