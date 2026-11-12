# TASK 6.9 — ROADMAP EXECUTIVO DE IMPLEMENTAÇÃO

## DreamsDimensions — Transformar a Game Design Bible 6.0–6.8 em um mod jogável, extensível e verificável

**Tipo:** Engenharia de produto / Technical Program Management / Game Design / Arquitetura / QA.  
**Escopo desta Task:** **somente documentação**; nenhuma mudança no jogo, nenhum registro, receita, textura, modelo, estrutura, configuração, mundo ou branch principal.  
**Data:** 12/11/2026. **Branch:** `migration/minecraft-26.3`. **HEAD base auditado:** `785962e43a74785f88452ebe59c34859792f7af8`.  
**Stack verificada:** Minecraft 26.3, NeoForge 26.3, Java 25, ModDevGradle, GeckoLib 5; Curios para Colar.  
**Objetivo:** implementação em **26 tasks 7.0–7.25** distribuídas em trains; algumas condicionais, mutuamente exclusivas e canceláveis.  
**ATUALIZAÇÃO 12/11/2026:** a numeração 7.6–7.9 foi priorizada novamente para **Anomalias Oníricas**. Este documento é preservado como roadmap histórico; consultar [adendo 7.6](OVERWORLD_ANOMALY_ROADMAP_ADDENDUM_TASK_7_6.md) antes de executar tasks >= 7.6. **Não executar simultaneamente a antiga e a nova Task 7.6.**  
**Próxima task executiva real recomendada:** **Task 7.0 — Homologação e Segurança da Recuperação Onírica**.  
**Alternativa paralela segura:** 7.1 pode caminhar com 7.0 por ser infraestrutura de QA e não alterar gameplay.  
**Não:** merge automático na main; prometer um End Game/boss final; criar conteúdo por quota numérica.

**Base documental (autoridade):**
- [6.0 — Game Design Bible](GAME_DESIGN_BIBLE_TASK_6_0.md)
- [6.1 — Pilares do Overworld](OVERWORLD_GAMEPLAY_PILLARS_TASK_6_1.md)
- [6.2 — Geologia, Flora, Blocos](OVERWORLD_RESOURCES_GEOLOGY_FLORA_BLOCKS_TASK_6_2.md)
- [6.3 — Itens, Ferramentas, Estações](OVERWORLD_ITEMS_CONSUMABLES_TOOLS_STATIONS_TASK_6_3.md)
- [6.4 — Ecologia e Mobs](OVERWORLD_ECOLOGY_MOBS_HABITATS_TASK_6_4.md)
- [6.5 — Estruturas e Descoberta](OVERWORLD_STRUCTURES_ENVIRONMENTAL_DISCOVERY_TASK_6_5.md)
- [6.6 — Grafo de Crafting](OVERWORLD_CRAFTING_PROCESSING_PROGRESSION_TASK_6_6.md)
- [6.6.1 — Universo/Catálogo](OVERWORLD_UNIVERSE_CONTENT_EXPANSION_TASK_6_6_1.md)
- [6.7 — Art Bible](OVERWORLD_VISUAL_DIRECTION_TEXTURES_MODELS_ASSETS_TASK_6_7.md)
- [6.8 — Economia/Raridade](OVERWORLD_ECONOMY_RARITY_BALANCING_TASK_6_8.md)
- [5.6 — Medição real de minérios](OVERWORLD_ORE_DISTRIBUTION_TASK_5_6.md) e [5.7 — Recuperação por ruínas](DREAM_RUINS_TASK_5_7.md).

> **Contrato central:** primeiro entregar algo que seja jogável *desde a geração natural até a aplicação em Survival*, depois replicar componentes/serviços/asset packs que realmente provaram utilidade. **Sistemas reutilizáveis antes de 30 cópias, mas nunca frameworks genéricos sem consumidor.** O Overworld será uma expansão com vida própria; o ramo dos sonhos continua uma aventura arriscada, com consistência apenas no Mid tardio/Late e sem fim de jogo independente.

---

# 0. RESULTADO EXECUTIVO E ORDENAÇÃO CORRETA

## 0.1 O que está implementado versus o que é somente conceito

| Grupo | Já existe no código neste HEAD? | Efeito sobre o roadmap |
|---|---|---|
| Dream Ore / Deepslate / Dream Dust | **SIM**; worldgen Overworld e loot carregáveis | Não mudar count/size antes de Survival real |
| Colar, Núcleo, Pedra Infundida, Despertador | **SIM**; cadeia R0 e retorno sob regra atual | Preservar itens/IDs e contratos; QA 7.0 |
| Dreamscape / Campo Azul, Fragmento e Vigia | **SIM** | Não reescrever motor de dimensão ou AI existente |
| Ruínas dimensionais de recuperação | **SIM**, 2 registries nativas; 8 baús sondados | Falta acesso e suporte seguro; prioridade P0 |
| Pedra Marcada, Pedra Lapidada, Líquen F1 | **NÃO**, IDs candidatos de design | Implementar em slices F0/F1 |
| Argamassa/Cinzel/Leitor | **NÃO** | Argamassa após F0/F1; Cinzel e Leitor condicionados a A/B |
| 12 POIs do Overworld 6.6.1 | **NÃO** | Selecionar por função, gerar gradualmente |
| 6 criaturas candidatas do Overworld | **NÃO** (MOB-04 ainda adiado) | Mariposa após F1; demais condicionadas |
| B21 Nódulo/Fragmento Reverberante | **NÃO** | M06 deve provar vantagem antes do minério |
| Câmara do Limiar / Guardião | **NÃO** | **BLOCKED_BY_REWARD_DESIGN** |

**Nenhum arquivo de Tasks 6.0–6.8 equivale a implementação.** O código é a autoridade para o que está registrado; docs de brainstorming não autorizam /give ou receita de item inexistente.

## 0.2 Por que a próxima ação NÃO é criar 30 blocos de uma vez

O catálogo expandido possui **30 conceitos de blocos/plantas/variantes**, **18 itens/equipamentos**, **12 POIs**, **6 conceitos de mobs** e **9 loops**. Algumas ideias são alternativas que não devem coexistir; algumas permanecem bloqueadas. O valor de cada loop precisa existir antes que multipliquemos conteúdo e arte. A sequência abaixo retém **todo o portfólio** por linhas mapeadas, mas só promove candidatas com evidência.

## 0.3 Trens de entrega (release trains, não datas)

| Train | Tasks | Resultado jogável ou técnico | Dependências fundamentais | Critério de saída |
|---|---|---|---|---|
| **S0 — Proteger a base** | **7.0–7.2** | Recuperação auditada + testes/integrações reutilizáveis | Código atual 5.3–5.7 | Zero nova regressão e plano de correção P0 |
| **S1 — O Primeiro Vestígio** | **7.3–7.5** | Pedra Marcada natural → pedra lapidada → construir | S0 de QA | 1 ciclo completo em Survival e art 16×16 |
| **S2 — Natureza e primeiro POI** | **7.6–7.10** | Líquen cultivável + Argamassa + Marco + Abrigo | F0 | Duas colheitas, obras, dois POIs distintos |
| **S3 — Mundo vivo / novos loops** | **7.11–7.13, 7.15** | Mariposa, Pedreira/Jardim + *spikes* arqueologia/travessia | F1, frameworks reais | Fauna e 2 novos testes de mecânica |
| **S4 — Exploração com escolhas** | **7.14, 7.16–7.19** | Arquivo, Passarela, Galeria, Habitante, Poço/Observatório opcionais, spike Eco | Resultado dos spikes e terreno válido | 2 rotas e recompensas reutilizáveis |
| **S5 — Especialização condicional** | **7.20–7.23** | Circuito mineral, Casa de Ecos, extras e Câmara **somente se comprovados** | M06 e reward gate | Pode cancelar sem bloquear versão |
| **S6 — Homologar e publicar** | **7.24–7.25** | Balanceamento real, compat, release selecionada | Slices escolhidas | 0 P0 e aceite de produção |

**Trains não são obrigatoriamente sequenciais em todos os detalhes.** 7.0 (recovery) é **release gate**, não exige paralisar PR documental ou F0 em branch independente; 7.1 pode começar em paralelo, e spikes de 7.13/7.15/7.19 podem ocorrer em branch de laboratório desde que não poluam produção. Nenhum train significa um prazo fixo.

---

# 1. PRINCÍPIOS DE EXECUÇÃO E CONTROLE DE ESCOPO

## 1.1 Invariantes de produto

1. **Overworld-first:** um construtor/naturalista/engenheiro que não usa Colar encontra mecânicas e conteúdo valiosos.
2. **Dificuldade justa:** reconhecer → entender → agir → obter; nunca depender apenas de sorte ou um baú escondido.
3. **Early/Mid/Late sem End Game:** progressão por capacidade, infraestrutura, variedade e especialização. Sem boss final.
4. **Primeiro retorno sem circularidade:** o primeiro Despertador pode ser fabricado **no Overworld**; não exigir minério obtido no sonho para sair dele.
5. **Itens têm consumidor:** não adicionar materiais, ferramentas ou estações só porque cabe numa categoria.
6. **Sem moeda premium falsa:** Líquen, Argamassa ou mobs **não produzem Dream Dust**.
7. **Vanilla coexistente:** não substituir mobs vanilla, corromper worldgen, invadir construções ou adicionar grief em blocos do jogador.
8. **Conteúdo com valor fora da primeira descoberta:** o jogador usa material/técnica/rota em sua base.
9. **Origem e retorno dos sonhos preservados:** consumição somente após sucesso confirmado, mundo de sonho não vira fast travel grátis.
10. **Seleção honesta:** o catálogo é um conjunto de opções; a implementação é a seleção baseada em testes.

## 1.2 Estados de PR/tarefa

- `PROPOSED`: conceito sem implementação.
- `SPIKE`: prova isolada, pode ser descartada; não faz parte de release.
- `APPROVED_FOR_IMPLEMENTATION`: loop, consumer e segurança definidos; não quer dizer código pronto.
- `IMPLEMENTED_UNVERIFIED`: compilou/carregou; não provou Survival.
- `CI_VERIFIED`: build, dados e runtime de mundo testados; **não** equivale a experiência humana.
- `SURVIVAL_VERIFIED`: um jogador conseguiu descobrir/usar com resultados registrados.
- `MULTIPLAYER_VERIFIED`: comportamento cross-client e persistência corretos.
- `SHIPPED`: release aprovada, changelog e rollback possíveis.
- `BLOCKED_BY_*`: necessário resolver gate antes de gerar conteúdo.
- `CANCELLED_WITH_EVIDENCE`: ideia eliminada por A/B, preservando aprendizado sem lixo runtime.

**Não marcar `DONE` ao realizar commit, push, export do Blockbench, /give ou /locate isoladamente.**

## 1.3 Como uma task 7.x entra no repositório

Cada task precisa conter **toda a vertical mínima** que o usuário consegue verificar:
- Pré-voo de branch, HEAD e arquivos antes de editar, incluindo busca por código/sistemas reutilizáveis.
- Referências oficiais de **Minecraft 26.3, NeoForge 26.3 e GeckoLib 5 quando aplicável**, conferidas contra dependências do repo.
- Arquitetura curta com ponto de extensão/reutilização, sem novas classes por cópia.
- Diff mínimo: Java/registry somente se necessário, resources atuais, tags, recipe, loot, models/items/texture/lang.
- Validação automática suficiente para provar json/registry, build/Gradle, dedicated server, test de mundo real e regressão.
- Instruções de QA manual com comandos literais para registros existentes, sem coordenadas falsas.
- Evidência: seed, XYZ observado, SHA, captura/log, condição, material resultante e status NOT_RUN honesto.
- Push somente em `migration/minecraft-26.3` ou branch feature a partir dela; **não merge main** automaticamente.

---

# 2. INVENTÁRIO DO REPOSITÓRIO E ARQUITETURA QUE VAMOS REUTILIZAR

## 2.1 Fatos auditados

- `src/main/java/com/dreamsdimensions/mod/registry/ModBlocks.java` usa `DeferredRegister.Blocks` e `DeferredBlock`.
- `src/main/java/com/dreamsdimensions/mod/registry/ModItems.java` usa `DeferredRegister.Items`, incluindo `BlockItem` e `Item`.
- `src/main/java/com/dreamsdimensions/mod/registry/ModEntities.java` registra **Fragmento Errante e Vigia Adormecido**, com spawn placements; nenhum mob Overworld próprio.
- `src/main/java/com/dreamsdimensions/mod/service/DreamEntryPolicy.java` é política pura de permissão/chance, não local para registrar flora/stone.
- `src/main/resources/assets/dreamsdimensions/items`, `models/item`, `models/block`, `blockstates`, `textures/*`, `geckolib/models/entity` e `geckolib/animations/entity` são padrões de conteúdo atuais.
- `src/main/resources/data/dreamsdimensions/recipe`, `loot_table`, `worldgen/feature`, `placed_feature`, `structure`, `structure_set` e `template_pool` são caminhos **singulares relevantes em 26.3**.
- `scripts/ci/check_overworld_resource_chain.py` protege recipe chain R0; `check_dream_ruins.py` valida NBT/ruínas; `audit_overworld_ore_runtime.py` e `audit_dream_ruins_runtime.py` usam servidor real e seeds.
- `.github/workflows/build.yml` já roda matrix **14011, 20261108, -904732, 73199013**, Java 25, clean build, validações estáticas, servidor dedicado e artifacts.
- Framework de mobs 4.4 e contratos de AI/spawn são reais, mas **não** equivalem a um `spawn all` do Overworld.

## 2.2 Compatibilidade de versão: armadilhas específicas

A [NeoForge Primer 26.3](https://docs.neoforged.net/primer/docs/26.3/) documenta mudanças em `Feature`/registry (não repetir tutoriais `ConfiguredFeature` antigos) e a migração de spawns para `EnvironmentAttributes#NATURAL_MOB_SPAWNS`. No `OVERLAY` por categoria, uma nova lista pode substituir a anterior; verificar via [Biome Modifiers](https://docs.neoforged.net/docs/worldgen/biomemodifier/) a abordagem **aditiva** adequada, e comparar fauna vanilla antes/depois. Para modelos novos, seguir [GeckoLib 5 Blockbench](https://wiki.geckolib.com/docs/geckolib5/making-models/blockbench-plugin-usage/) e os paths reais existentes.

**Regra:** validar Java/JSON pela dependência instalada no repo, não por snippet de Minecraft 1.20.x/1.21.x. Nunca supor que um arquivo .geo.json anima automaticamente AI/movement.

## 2.3 Serviços reutilizáveis por necessidade real

| Capacidade | Reuso/abstração mínima | Quando abrir |
|---|---|---|
| Worldgen em microhabitat | Predicados de suporte/clima/solidez compartilhados; pequeno validador | 7.4/F0, 7.6/F1 |
| Provas de presença natural | Auditor com mundo + seed + source ID + loaded chunks | 7.1, reaproveitado |
| Transformação in situ | Verificação de block, claims, servidor, mão, item e confirmação | 7.8; depois Cinzel se aprovado |
| Arquitetura | Kits de NBT/feature e checks de suporte/entrada | 7.9/7.10; expandir após primeira ruína |
| Mob habitat | Predicado local, registro additivo, cap/AI reusável | 7.11; reaproveitar em 7.17 |
| GeckoLib | Convenção de GeoModel/controller e assets, clips por AI | 7.11; não clonar Fragmento |
| Redstone | Propagação local limitada e neighbor updates, sem scanner global | **Só se 7.19 aprovar M06** |
| Economia | Recipe DAG e yield/loot checks automáticos | 7.8, 7.14 etc. |
| Regressão de sonho | Política de entrada/consumo + respawn + loot/ruínas | 7.0 e em toda milestone |

**Anti-padrão explícito:** não criar `DreamEverythingManager`, `UniversalEntitySystem` ou dezenas de interfaces abstratas antes que **dois usos reais** tenham exigido a mesma abstração. Reusar por comportamento compartilhado, não por nome parecido.

---

# 3. O ROADMAP INTEGRAL — TASKS 7.0 A 7.25

**Tamanho relativo**: S=pequena, M=média, L=grande, XL=risco alto/múltiplas PRs. **Não são estimativas de horas nem prazo de entrega**. Um código `7.13 PASS` significa gate de protótipo realmente aprovado, não um SHA imaginário.

| Task 7.x | Entrega/epic | Train | Bloqueada por | Tamanho | O que muda no jogo/código | DoD mensurável | Contrato-base | Risco principal |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 7.0 | Homologação do retorno onírico (P0) | S0 | — | S | Medir geometrias das ruínas dimensionais, corrigir só defeito comprovado (inclusive 4/9 em Dreamscape), validar acesso real, loot/persistência/respawn. | 8 ruínas históricas + vários starts; ambos os mundos; Survival+2 players; sem alterar chance/entrada sem evidência. | DREAM_RUINS_TASK_5_7; DREAM_ENTRY; CI | Não habilitar dream_entry_enabled em produção até homologação |
| 7.1 | Contrato de QA e telemetria reutilizável | S0 | — | M | Unificar harness de worldgen/loot/habitat/POI com seed e SHA, sem criar scanner em gameplay. | Reportar candidates/accepted/accessible e NOT_RUN; 4 seeds; no forceload permanente. | scripts/ci, workflows; diagnostics | Não duplicar auditor de minério nem rodar sync scan em servidor público |
| 7.2 | Contrato mínimo de conteúdo e assets | S0 | 7.1 | S | Validar recipe/item definition/model/loot/tags/lang/assets sem registrar dezenas de objetos. | Um único checklist/validador compartilhado por família; existing 26.3 não sofre regressão. | assets paths, ModBlocks/Items, CI | Não criar 'framework universal' prematuro |
| 7.3 | F0a Pedra Marcada: bloco e assets | S1 | 7.2 | S | Um material de fonte natural com bloco, BlockItem, loot, tool, tradução, blockstate/model/PNG originais. | Bloco pode ser colocado/minerado sem Dust; sem missing textures; tags/loot corretos. | B01, 6.2 GEO-01, 6.7 ART-A | Não incluir escada/laje/muro nesta PR |
| 7.4 | F0b Geração natural + lapidação | S1 | 7.3 | M | Feature/placed feature biome-filtered, ocorrência visível, cut stone e receita stonecutter 1:1. | Observado em 4 seeds; 1ª descoberta sem cheats; blocos suficientes para pequena obra; no retro-gen. | B02, worldgen, recipe, 6.8 | Metas de rarity da 6.8 são candidatas, não constants |
| 7.5 | F0c Homologação econômica e regressão | S1 | 7.4 | S | Fechar a primeira fatia Survival, aferir P50/P90, tiling, 2 jogadores e compatibilidade. | Fonte real → coletar → cortar → usar na base; nenhuma receita circular; arte aprovada. | QA F0, telemetry, ART-A | Se F0 não for útil, não avançar 10 variantes |
| 7.6 | F1a Líquen: habitat e obtenção | S2 | 7.5 | M | Criar planta distinta do glow_lichen e do dream_glow_moss; suporte, drop, luz e patches naturais. | Planta em microhabitat real; não vira scanner de minério; 4 seeds; sem invasão de base. | B10, HAB-01, 6.2, ART-B | Sem Mariposa nesta PR |
| 7.7 | F1b Cultivo e ciclos | S2 | 7.6 | M | Transplante reprodutível, propagação controlada, duas colheitas, saldo positivo. | Matriz preservável; Growth sem tick scan; Garden funciona sem mob; save/reload. | F1, farming/loot, 6.8 | Se indistinguível de vanilla, revisar espécie antes de seguir |
| 7.8 | Argamassa: produção de arquitetura | S2 | 7.5,7.7 | M | Crafting shapeless 2 Líquen+argila+cascalho→4 (candidato); 1 mortar+stone→1 marked_stone in situ. | 64/256 blocos; servidor autoritativo; claims, creative, adventure, two-player sem dupe; yield A/B. | I01, B01/B10, 6.6, ART-C | Não introduzir estação especial nem Dream Dust |
| 7.9 | POI-01 Marco de Pedra Marcada | S2 | 7.5,7.1 | M | Primeira pista ambiental construível, talvez placed feature se micro; variante única inicialmente. | Descoberta orgânica e amostra; chão e silhueta; sem baú; suporte real multi-seed. | POI-01, 6.5, 6.8 | Não lançar copy-paste de monólitos |
| 7.10 | POI-02 Abrigo de Campo | S2 | 7.9 | M | Estrutura utilitária com duas entradas, proteção, bancada e loot vanilla modesto se necessário. | Duas rotas; solo íntegro; chest persistente/shared se existir; não depende de item raro. | POI-02, 6.5 | Não copiar 25% Despertador dos baús dimensionais |
| 7.11 | MOB-01 Mariposa dos Vestígios | S3 | 7.7,7.1 | L | GeckoLib 5/Blockbench, IA de pouso/voo/fuga, spawn natural não-destrutivo em HAB-01. | Pouso/flight sem hovering estático; sem eliminar mobs vanilla; 2/4/8 entidades; sem drop obrigatório. | MOB-01, 6.4, ART-D, Mob Framework | Sem spawn infinito em Líquen plantado pelo jogador |
| 7.12 | POI-05 Pedreira e POI-06 Jardim | S3 | 7.5,7.7,7.9 | L | Duas experiências de estrutura distintas: material recuperável vs habitat conservável. | Pedreira reconstrói percurso; Jardim dá muda replantável sem baú; medir densidade conjunta. | POI-05/06, M01/M04 | Se ambos parecem Marco, fundir variações |
| 7.13 | Spike M02: arqueologia sem loot vazio | S3 | 7.5 | M | Prototipar B15 sedimento + brush vanilla → um padrão que serve para construir, sem um catálogo inteiro. | Em mundo descartável, padrão do sedimento termina em bloco na base; 3 jogadores entendem uso. | M02, B08/B15/B29, I03 | Se é livro inútil ou arqueologia vanilla recolorida, cancelar |
| 7.14 | M02 em produção, POI-10 e Besouro opcional | S4 | 7.13 PASS,7.1 | L | Registrar somente a linha arqueológica aprovada: sedimento, molde/painel, Arquivo; MOB-05 se habitat provar valor. | Brush/loot/padrão persistentes; sem chave RNG; POI acessível; besouro não faz drop essencial. | POI-10, MOB-05, M02 | Separar em PRs menores por bloco→item→POI→mob se exigir |
| 7.15 | Spike M03: travessia com decisão | S3 | 7.5,7.1 | M | Graybox piso B17, aviso e passarela entre dois apoios; opções vanilla vs escorar. | Duas rotas sempre viáveis, nenhum grief, aviso sem som, sem queda-surpresa; caso não funcione, descartar. | B17/B18/I04/M03 | Não adicionar simulação global de física ou cave-ins |
| 7.16 | M03 em produção: Passarela e Galeria | S4 | 7.15 PASS,7.1 | L | POI-08 Passarela e POI-03 Galeria, com blocos B17–B19/kit somente se A/B provar valor. | Apoio/entrada/saída, água contida, duas soluções e passagem reusável; multi-seed. | POI-03/08, I04, M03 | Não mascarar terreno inválido inflando spacing |
| 7.17 | MOB-02 Habitante das Fendas | S4 | 7.16,7.1 | L | Mob de solo territorial com aviso, perseguir limitado e contorno; spawn HAB-03 real. | Duas respostas justas, IA/colisões/Peaceful/dedicated server; sem mob em bases. | MOB-02, 6.4 | Não copiar AI do Fragmento de ilhas |
| 7.18 | POI-07 Estação, POI-09 Poço, POI-11 Observatório | S4 | 7.14 ou 7.16,7.7 | L | Adicionar só locais cuja mecânica real tem consumidor: estratos, descida vertical ou estado da flora. | Três percursos distintos, sala acessível, pista verdadeira, não chest-clones; separar em PR por POI. | POI-07/09/11, M01/M04 | Observatório descartado se só muda iluminação |
| 7.19 | Spike M06: ressonância com proxies | S4 | 7.1 | L | Protótipo limitado de Emissor→Pedra Condutora→Receptor, sem nódulo real; A/B redstone/sculk. | Duas aplicações reais, caminho limitado, nenhuma força de chunk/tick-scan/loop; rejeitar se redundante. | M06, B22–B25 | Não criar minério/Fragmento ainda |
| 7.20 | M06 em produção + Nódulo opcional | S5 | 7.19 PASS | XL | Se A/B aprovar, registrar protocolo, blocos de rede, assets e somente então B21/I14 como fonte técnica. | Circuito carrega/salva e para em chunk unload; consumidor duplo; worldgen correto; CI TPS. | B21–B25,I14/I15, ART-G | Caso falhe, CANCELLED_WITH_EVIDENCE, não dívida permanente |
| 7.21 | Casa de Ecos e Grilo contextual | S5 | 7.20 PASS | L | POI-12 demonstra M06, MOB-06 reage só a nódulo físico real; arquitetura ensinável. | Após visitar, reproduzir mecanismo na base; fauna sem radar mágico; suporte/loot/2 players. | POI-12, MOB-06 | Não implementar se M06 não existir |
| 7.22 | Expansão horizontal: construção, botânica, ferramentas | S5 | 7.8,7.11 | L | Selecionar B03–B14/B26–B30, I02/I05–I13/I16–I18 por teste A/B, em PRs por capacidade. | Escadas/lajes com volume, Cinzel útil, Leitor só se 0–4 vizinhos válidos; nenhuma estação/minério vazios. | 6.6.1 catalog, 6.7 ART-H | Não implementar 30 blocos por quota; comparar vanilla |
| 7.23 | POI-04 Câmara e MOB-03 Guardião opcional | S5 | 7.16, recompensa aprovada | XL | Desbloquear Câmara somente quando recompensa horizontal forte passar protótipo; Guardião é opcional. | Duas rotas, escolha e recompensa sem boss; persistência; ZERO chave de fim de jogo. | POI-04, MOB-03, 6.5–6.8 | BLOCKED_BY_REWARD_DESIGN sem exceções |
| 7.24 | Balanceamento consolidado Survival/multiplayer | S6 | fatias aceitas | L | Comparar métricas 6.8, minérios atuais, F0/F1, fauna, POIs, custos por 64/256 blocos e retorno. | Seeds fixas + novas; P50/P90 censurados; UMA variável por tuning; diff e rollback. | 6.8, testing, scripts/ci | Não converter metas hipotéticas em dados observados |
| 7.25 | Release hardening + conteúdo persistente | S6 | 7.0 PASS,7.24 | XL | Empacotar versão pública segura, CI e dedicated server, compat e docs/jogador, smoke survival. | DoD 100%, zero P0, Dream Entry somente se homologada; versões de save, server e cliente. | build.yml, changelog | Sem merge automático nem garantia de 12 POIs já entregues |

**Nota de execução:** 7.14, 7.16, 7.18, 7.20, 7.21, 7.22, 7.23 e 7.25 **devem ser divididas em PRs menores conforme resultados reais**. O identificador do roadmap é um *epic/task envelope*, não licença para submeter centenas de arquivos de uma vez.

---

# 4. FICHAS EXECUTIVAS POR FRENTE — DEFINIÇÃO DE PRONTO SEM AMBIGUIDADE

## 1. 7.0 — P0 BASELINE: RECUPERAÇÃO DOS SONHOS

**Ponto de partida:** Risco já conhecido, não conjectura: 8/8 baús com LootTable; 7/8 suporte mínimo; Dreamscape seed 20261108 chest em 300 137 -4 com 4/9. Um POI dimensionado pode ser localizado e ainda assim ser perigoso/impraticável.

**Implementação por etapas:** Subtask 7.0-A: observação Spectator real (câmera, terreno, supports, ilhas, entrada, saída) nas duas dimensões. 7.0-B: ensaio Survival de trajeto e abertura de baú, inclusive sem Despertador. 7.0-C: reload/restart/2 clientes e loot executado Minecraft. 7.0-D: corrigir suporte/preflight apenas se problema comprovado, testar geração natural em chunks novos.

**Guardrails:** Não alterar p=25%, spacing 38/32, Colar ou preço do Núcleo antes de investigar. Se o erro for geométrico, corrigir placement / preflight local sem quebrar estrutura vanilla. Após correção, registrar diff de acessibilidade e disponibilidade.

**Definição de pronto:** PASS: travessia real documentada por seed + suporte seguro para jogador + persistência + nenhum retorno gratuito/softlock. Se não alcançarmos PASS, não ativar dream entry default nem anunciar homologação; outros PRs de Overworld podem seguir isoladamente.

## 2. 7.1 — PRIMEIRO CONTRATO REUTILIZÁVEL DE QA

**Ponto de partida:** Hoje já existem scripts 5.6/5.7, seed matrix e workflow. Não duplicar auditorias por novo objeto.

**Implementação por etapas:** Criar apenas o necessário: contrato JSON/evento de teste comum a features, POIs e mobs, check de referência em assets e flags de auditoria opt-in. Padronizar seed, SHA, dimensão, chunks carregados, status MEASURED/NOT_RUN, ID e critério. Ter relatório que separa candidato/gerado/descoberto.

**Guardrails:** Script novo não deve alterar worldgen, item, spawn nem instalar player-tick listeners. Forceload só em servidor de CI descartável, removido no final.

**Definição de pronto:** PASS: repetir 5.6/5.7 nas 4 seeds com mesmos resultados dentro de variabilidade e poder adicionar F0 sem criar cópia de 200 linhas do auditor.

## 3. 7.2 — CONTRATO DE INTEGRAÇÃO DE BLOCO/ITEM/ASSET

**Ponto de partida:** O projeto usa DeferredRegister.Blocks/Items, `assets/.../items` além de `models/item`, `blockstates`, texturas, recipe e loot singulares.

**Implementação por etapas:** Introduzir README/checklist e scripts de referência só onde reduzirem regressão. Exigir uma ficha com registry, Item, BlockItem, loot, tool tags, receitas, creative tab se aplicável, en_us, pt_br, asset, worldgen source, uso Survival e comandos. O catálogo conceitual NÃO cria 30 registries.

**Guardrails:** Manter APIs NeoForge 26.3, sem copiar antigo ConfiguredFeature/Feature API ou GeckoLib 4. Conservar IDs existentes e DataComponents.

**Definição de pronto:** PASS: um bloco sandbox com todos os refs resolvidos sem duplicação de processo; no-op se checklist já for suficiente.

## 4. 7.3 — F0a — MÍNIMO DE PEDRA MARCADA

**Ponto de partida:** Começar com B01 original e modelos/loot. Não antecipar natural worldgen sem referência a ID real.

**Implementação por etapas:** Registrar `marked_stone` apenas depois do nome final decidido; face/model com gramática da Art Bible, BlockItem, tags mineable/pickaxe corretas, loot sem dupe, traduções, Item client definition. Verificar textura 16×16 com stone/tuff; usar JUnit/static asset check.

**Guardrails:** Não contam como entrega: screenshot de texturas sozinho, pacote em que BlockItem não existe, usar `dream_dust` no crafting.

**Definição de pronto:** PASS: `/give` + `/setblock` usando ID confirmado, quebra/colocação e download JAR da CI, blocos não somem no reload.

## 5. 7.4 — F0b — GERAR, ENCONTRAR, LAPIDAR

**Ponto de partida:** Um material de construção requer origem natural e processo de transformação.

**Implementação por etapas:** Colocar F0 via data/Feature 26.3 e filtros localizados com logging de rejects; criar `cut_marked_stone` e stonecutter 1:1. Testar acessibilidade geológica, visibilidade real e locais não artificiais; worldgen em chunks novos somente. Medir F0 antes de aumentar size/count.

**Guardrails:** Hipótese 6.8: ~1 candidato/8–20 chunks elegíveis, grupos 5–18 blocks; são faixas de protótipo, não registros. Não assumir que 'count' é ocorrência real.

**Definição de pronto:** PASS: 4 seeds com amostras naturais + jogador acha/obtém/corta material + nenhuma receita de retorno > 1 full block + biomas próprios intactos.

## 6. 7.5 — F0c — FECHAR PRIMEIRO SLICE

**Ponto de partida:** Antes de expandir família, encerrar pipeline completo.

**Implementação por etapas:** Executar nova sessão sem cheats e outra instrumentada. Registrar P50/P90 (com censura), quantidade utilizada em uma obra curta, estética 3×3, Fortune/Silk onírico sem regressão, mundo antigo, dois jogadores.

**Guardrails:** Se não houver valor de construção ou identificação da pedra, corrigir F0. Não iniciar variantes B03–B09 como fuga criativa.

**Definição de pronto:** PASS: um jogador encontra, extrai, transforma e constrói sem recorrer a /give; um JAR testado com cliente e dedicated server.

## 7. 7.6 — F1a — BOTÂNICA COMO FONTE

**Ponto de partida:** F1 não pode ser glow_lichen recolorido nem indicar minério sem correlação real.

**Implementação por etapas:** Modelar/registrar Líquen com suporte, luz, regras de colocação/remover, drop e crescimento ainda restrito; feature de patches natural no HAB-01, sem ferir vanilla. Exigir item que possa ser levado e plantado, sem seed separada.

**Guardrails:** Hipótese 6.8 0,2–0,6 patches candidatos/chunk elegível e 3–9 unidades não é valor JSON. Cuidado com /forceload, player placed vs natural.

**Definição de pronto:** PASS: habitat identificável e planta coletável em 4 seeds, sem corrupção de chunks; comparação qualitativa com vanilla e dream_glow_moss.

## 8. 7.7 — F1b — CICLO DE CULTIVO

**Ponto de partida:** Colheita sem replantio é material finito; replantio que só preserva unidade não escala Argamassa.

**Implementação por etapas:** Propagação positiva com restrição local e suporte; implementar forma simples sem scans, patch size/performance; blocos plantados não geram artificialmente mobs. Medir ciclos com/sem bonemeal, offline/reload e 2 players.

**Guardrails:** Nenhuma taxa de crescimento aprovada sem experimento; não criar outra 'fiber' para disfarçar falha do cultivo.

**Definição de pronto:** PASS: planta matriz preservada + pelo menos duas colheitas documentadas + saldo líquido positivo e nenhuma expansão desenfreada.

## 9. 7.8 — ARGAMASSA E RESTAURO DE PEDRA

**Ponto de partida:** F0 + F1 criam economia Mid que pode retornar ao construtor no Late.

**Implementação por etapas:** Receita candidata 2 Líquens + 1 clay ball + 1 gravel → 4, depois interação segura 1 mortar + stone colocada → 1 marked stone. Usar evento/Item adequado ao 26.3 e verificar remoção server-side só após alteração real. Prevenir dupe por dois jogadores/shift-click/Crafter; respeitar claims.

**Guardrails:** Produzir 4/16/64/256 blocos; testar yield alternativo 2/4/6 sem mudar live até medição.

**Definição de pronto:** PASS: 1:1, 0 Dust, receita acessível sem sonhos, custo em massa no Mid/Late, jogador usa numa construção.

## 10. 7.9–7.10 — PRIMEIRO PAR DE POIs

**Ponto de partida:** F0 já fornece textura/pista; dois POIs devem oferecer atividades diferentes.

**Implementação por etapas:** Marco pequeno como pista e amostra sem loot; Abrigo como espaço de utilidade (2 entradas, bancada) e material/loot baixo. Para micro-POI considerar placed feature em vez de structure; para abrigo, template NBT e structure set quando justificável.

**Guardrails:** Garantir apoios, entrada, saída, suporte do baú, terrain rejection e tempo de exploração medidos. Nunca copiar 25% awakeners.

**Definição de pronto:** PASS: dois encontros distintos em Survival, 4 seeds e zero baús regenerativos.

## 11. 7.11 — MARIPOSA PÓS-LÍQUEN

**Ponto de partida:** Fauna serve de pista de habitat e variação comportamental.

**Implementação por etapas:** Definir AI primeiro e só então Blockbench/GeckoLib 5 com atlas original; clips perch/takeoff/flight/land/startled ligados a movement. Spawn aditivo no habitat natural usando API 26.3, não OVERLAY que substitui mobs da categoria; grupo/simulação calibrados posteriormente.

**Guardrails:** Evitar nome de mob que usa 'Fragmento' animation, invisibilidade de spawn, farm em jardim plantado, tick scan.

**Definição de pronto:** PASS: spawn 4 seeds, vento/flight, 2 players, vanilla fauna preservada, 2/4/8 entidades com FPS/TPS aceitáveis.

## 12. 7.12 — AMPLIAR GEOGRAFIA COM DUAS EXPERIÊNCIAS

**Ponto de partida:** Pedreira e Jardim não podem virar mais dois Marcos iguais.

**Implementação por etapas:** POI-05 fonte de recuperação de pedra e possível caminho; POI-06 microhabitat/muda conservável, com condição de luz e suporte verdadeiros. Reaproveitar NBT modular e POI tester da 7.1.

**Guardrails:** Testar distribuição conjunta (Marco+Abrigo+Pedreira+Jardim), não isolada: quatro sets podem saturar o mesmo bosque.

**Definição de pronto:** PASS: 2 funções, nenhum item-chave, visitas naturalmente distintas, Worldgen segura.

## 13. 7.13–7.14 — ARQUEOLOGIA COMO CAPACIDADE

**Ponto de partida:** Spike não deve criar três itens sem usuário.

**Implementação por etapas:** Provar M02 em mundo de QA: sedimento B15 ligado a estrato/POI + brush vanilla → padrão aplicável à base; usar I03 Molde e B29 Relevo só se necessário. Após PASS, fazer PR de bloco/brush, depois item/receita e depois POI-10 (e MOB-05 opcional).

**Guardrails:** Se vanilla brush/decoração já fizer melhor ou só drop de papel, rejeitar e manter pedreira útil sem isso.

**Definição de pronto:** PASS: padrão realmente aplicado em construção e pode ser repetido sem explorar outro baú raro.

## 14. 7.15–7.16 — TRAVESSIA E GALERIA COMO SISTEMA

**Ponto de partida:** Spike M03 deve ser pequeno, seguro e sem novos mobs.

**Implementação por etapas:** Bloco B17 avisa risco; alternativa construída com vanilla; Escora I04/B18 se fizer diferença; somente então POI-08 Passarela e POI-03 Galeria. Validar dois apoios e rota, água contida, regeneração; adicionar detalhe arquitetônico na PR seguinte.

**Guardrails:** Sem física global de gravidade, sem trocas em player blocks, sem buracos inevitáveis.

**Definição de pronto:** PASS: nenhuma queda surpresa em 2 jogadores e estruturas acessíveis em multi-seed.

## 15. 7.17–7.18 — ECOLOGIA/POIs INTERMEDIÁRIOS

**Ponto de partida:** O Habitante só precisa existir se uma fenda com rota/território real existir; Estação/Poço/Observatório só se tiverem fenômeno.

**Implementação por etapas:** Fazer MOB-02 com alerta+fuga/ataque limitado e spawn HAB-03; separar PRs dos POI-07 Estação, POI-09 Poço e POI-11 Observatório, cancelando locais redundantes. B16 estrato como suporte para duas atividades.

**Guardrails:** Mobs não devem prender exploração obrigatória e POI não deve exigir ferramenta cara para chegar à saída.

**Definição de pronto:** PASS: 2 contrajogos para M2; 3 POIs com atividades diferentes e saída; não multiplicar chest.

## 16. 7.19–7.21 — RESSONÂNCIA EM TRÊS DEGRAUS

**Ponto de partida:** Reabrir nódulo não é aprovar minério. Só faz sentido se M06 superar redstone/sculk.

**Implementação por etapas:** 7.19: circuitos com proxies sem asset/minério, limite local, ciclo controlado; 7.20: se PASS, produzir emissor, condutor, receptor e nódulo/fragmento com fonte; 7.21: Casa de Ecos e Grilo contextual se ambos ainda gerarem valor.

**Guardrails:** Loop/forceload/scan, circuitos entre chunks, custo de net updates e desync são gates P0.

**Definição de pronto:** PASS: dois usos reais, projeto replicável em base, modo sem shader e dedicated server; senão CANCELLED_WITH_EVIDENCE.

## 17. 7.22 — EXPANSÃO DE FAMÍLIAS SOB DEMANDA

**Ponto de partida:** Não perder amplitude planejada, mas também não iniciar 30 registries com uma PR.

**Implementação por etapas:** Agregar backlog arquitetônico/flores/ferramentas por temas, com A/B vs vanilla e issue por capacidade; por exemplo escadas/lajes/muros após F0, Cinzel quando economia de ação, Leitor quando F1 oferece estado legível. Plantas B11–B14 só com habitat/uso.

**Guardrails:** Fichas em 6.6.1 e Art Bible 6.7 continuam autoridade visual. A 6.8 fornece hipótese de rarity a medir.

**Definição de pronto:** PASS: cada família tem consumer/origem, assets, QA, número de recursos justificado e PR reversível.

## 18. 7.23 — CÂMARA / GUARDIÃO SEM CHEFE FINAL

**Ponto de partida:** Uma câmara bonita não é recompensa; 6.6 bloqueia essa estrutura.

**Implementação por etapas:** Antes de NBT gerar PoC de desafio horizontal e saída: técnica arquitetônica que vale repetição, projeto engenheirável ou método de preparo que não transforma escape em free. Se passar, POI-04 com duas rotas; Guardião só se acrescenta desafio sem chave obrigatória.

**Guardrails:** Jamais entregar arma/armadura tier nova, item boss-key, End Game ou requisito para primeiro retorno.

**Definição de pronto:** PASS: recompensa útil fora da Câmara, duas rotas viáveis, mob opcional; senão manter BLOQUEADA.

## 19. 7.24–7.25 — HOMOLOGAÇÃO DO PRODUTO E RELEASE

**Ponto de partida:** Um catálogo documentado não equivale a build pronto.

**Implementação por etapas:** Tuning medido no 7.24, release 7.25 com checklist de seeds, saves, multiplayer, assets, CI, smoke, Survival e documentação de jogador. Publicar só slices completas; features condicionadas podem permanecer fora sem bloquear versão.

**Guardrails:** Nunca mudar main automaticamente; branch de trabalho e PR com aprovação humana. Meta de release é qualidade, não 12 estruturas obrigatórias.

**Definição de pronto:** PASS: 0 P0, receita e recuperação confirmadas, changelog e compat documentada; merge somente após aprovação.

---

# 5. DEPENDÊNCIAS COMO DAG, CAMINHO CRÍTICO E PARALELISMO

## 5.1 DAG geral

```text
 BASE EXISTENTE: minério/Colar/Despertador/sonhos/ruínas (5.x)
    |
    +-- [7.0 P0: recuperar/voltar em segurança] -----------+----> [7.25 gate de release]
    |                                                      |
    +-- [7.1 QA + 7.2 assets/registry]                      |
                    |                                      |
                    +--> [7.3 F0 bloco]                    |
                            |                              |
                         [7.4 worldgen+lapidação]          |
                            |                              |
                         [7.5 Survival F0]                 |
                            |                              |
                            +--> [7.9 Marco] --> [7.10 Abrigo] -> [7.12 Pedreira]
                            |                                      |
                            +--> [7.6 F1] --> [7.7 2 colheitas]   |
                                                |                 |
                                                +--> [7.8 Argamassa]
                                                +--> [7.11 Mariposa]
                                                +--> [7.12 Jardim]
                                                +--> [7.22 Flora extra, condicional]
                            |
                            +--> [7.13 Spike Arqueologia] -- PASS --> [7.14 Arquivo/Besouro]
                            |
                            +--> [7.15 Spike Travessia] --- PASS --> [7.16 Passarela/Galeria]
                                                                            |
                                                                            +--> [7.17 Habitante]
                                                                            +--> [7.18 Poço/Estação/Observatório]
                                                                            +--> [7.23 Câmara? REWARD GATE]
    [7.1 QA] --> [7.19 Spike M06 com proxies] -- PASS --> [7.20 Circuito/Nódulo opcional]
                                                        |
                                                        +--> [7.21 Casa de Ecos/Grilo]
                                                        +--> [7.22 Ferramentas opcionais]
    [todas fatias selecionadas e verificadas] --> [7.24 Tuning real] --> [7.25 Release]
```

**O que é bloqueio real:** sem F1 não existe Argamassa sustentável nem habitat de Mariposa; sem M06 aprovado não existe Casa de Ecos/Grilo; sem recompensa aprovada não existe Câmara. **O que NÃO é bloqueio:** a correção da ruína dimensional não impede escrever testes para F0; as 12 estruturas não precisam existir para lançar uma primeira expansão Overworld.

## 5.2 Caminhos críticos por resultado

| Resultado que queremos ver | Caminho mínimo real | Pode avançar sem |
|---|---|---|
| Primeiro bloco útil em Survival | 7.1→7.2→7.3→7.4→7.5 | Flora, mob, ruína e sonho |
| Primeiro jardim útil | 7.5→7.6→7.7 | Mariposa, Argamassa |
| Primeira produção em escala | 7.5+7.7→7.8 | Cinzel, Leitor, Casa de Ecos |
| Primeiro POI útil | 7.5→7.9 | Chest, mob, Despertador |
| Primeiro mob Overworld | 7.7→7.11 | Galeria/Guardião |
| Arqueologia aplicável | 7.13 PASS→7.14 | Nódulo e Colar |
| Uma travessia reparável | 7.15 PASS→7.16 | Habitante/Guardião |
| Engenharia de Eco | 7.19 PASS→7.20 | Nova dimensão e estação própria |
| Uso público seguro do ramo sonho | 7.0 PASS + regressão completa | Catálogo inteiro de Overworld |
| Release de conteúdo Overworld | F0 fechado + demais slices selecionadas + 7.24/25 + P0 sonho resolvido para entry pública | B21, Câmara, boss, 30 variantes |

## 5.3 Política de paralelismo (WIP)

- **WIP recomendado: no máximo 2 slices funcionais ativas**: uma segurança/infra e uma gameplay vertical; spikes isolados podem ficar em branches sem merge.
- **Um responsável por alterar worldgen/registry no mesmo momento** ou coordenação por PR, para evitar conflitos de branch rebase/force push.
- A arte F0/F1 pode ser trabalhada em paralelo com a implementação **após estabilizar dimensões, colisão, render e IDs**.
- Cada PR deve partir do HEAD mais recente da branch `migration/minecraft-26.3`, **não fazer force push** para resolver conflitos; rebase/merge revisados.
- **Nenhuma feature B/C é desbloqueada porque outro trabalho demorou**. Ao falhar A/B, registrar `CANCELLED_WITH_EVIDENCE`.

---

# 6. PLANO DE RELEASE POR FATIA — QUANDO HÁ VALOR PARA JOGADOR

## 6.1 Milestone P0 — Baseline seguro (sem nova feature)

Uma build de homologação não é conteúdo novo, mas impede problemas graves: acesso às ruínas de recuperação, persistência e retorno seguro. Não ativar chance de entrada default se P0 continuar aberto. Manter `dream_entry_enabled=false` até homologação.

**Aceite:** evidência sobre ruínas 5.7, respawn/Curios, economia R0, mundo antigo e multiplayer. Se inspeção 4/9 realmente causar plataforma sem suporte, produzir task corretiva limitada e refazer sondagem.

## 6.2 Milestone P1 — Um motivo para explorar o Overworld

**Conjunto mínimo:** Pedra Marcada natural, Lapidação via stonecutter, possibilidade de construir, assets visíveis. O player que nunca visita sonhos pode completar ciclo. Este é o primeiro pacote funcional que realmente muda o jogo.

**Aceite:** end-to-end Survival, nenhuma textura quebrada, ambiente natural e material construível; não chamar P1 completo antes de 7.5.

## 6.3 Milestone P2 — Overworld vivo e construível

**Conjunto mínimo:** F1 + cultivo sustentável + Argamassa + Marco/Abrigo e Mariposa **se a IA estiver pronta**. O fluxo `encontrar → cultivar → produzir → construir → reconhecer habitat` torna exploração interessante.

**Aceite:** duas colheitas, obra de 64 blocos, 2 POIs distintos e sem loop de farming premium. Uma Mariposa atrasada não impede F0/F1/Argamassa de serem uma slice completa, mas deve ser honesta no changelog.

## 6.4 Milestone P3 — Exploração variada

**Conjunto possível:** Pedreira, Jardim, arqueologia, Passarela/Galeria e Habitante se cada projeto vencer A/B. Não perseguir o número “12” antes de fechar geometria/loot. O valor deve estar em **duas atividades novas**, não 8 quartos com chest.

## 6.5 Milestone P4 — Engenharia e especialização

M06 se e somente se diferenciar vanilla. Casa de Ecos e Nódulo só após circuito comprovado. Câmara sem reward continua fora; não exigir estas features para declarar a expansão jogável.

## 6.6 Milestone de publicação

7.24/7.25 possuem checklist de regressão e documentação. **Não garantir que o total do catálogo será incluído numa release.** O objetivo é um produto com alto nível de acabamento e sem conteúdos que só existem para completar listagem.

---

# 7. COVERAGE: PARA ONDE VAI CADA ENTRADA DO CATÁLOGO 6.6.1?

**Esta matriz resolve a preocupação de “planejamos pouco ou esquecemos metade”.** Todo conjunto B01–B30, I01–I18, POI-01–12, MOB-01–06 está classificado abaixo. Um intervalo agrupa entradas por família, sem autorização para implementar todas simultaneamente.

| Entradas | Envelope 7.x | Como entram | Gate de promoção | Prioridade |
| --- | --- | --- | --- | --- |
| B01–B02 | 7.3–7.5 | Prioridade inicial / F0 | Fonte, lapidação e construção | A |
| B03–B09, B30 | 7.22 | Backlog arquitetônico, variantes após F0 | Tiling, volume e uso sem ruído | B/C |
| B10 | 7.6–7.7 | Prioridade F1 | Coleta, habitat, cultivo e uso | A |
| B11–B14, B27 | 7.22 | Flora e iluminação condicionadas | Suporte, estados funcionais, consumidor | B/C |
| B15–B16, B08, B29 | 7.13–7.14 | Arqueologia e estratos | Brush → molde → bloco em base | B |
| B17–B20 | 7.15–7.16 | Travessia e restauro | Risco legível + rota alternativa | B/C |
| B21–B25 | 7.19–7.21 | Engenharia somente após M06 PASS | Duas aplicações, rede segura e física | B |
| B26 | 7.22 | Leitor condicionado | Planta real + 0/4/8/12/15 efetivos | B |
| B28 | 7.18/7.22 | Placa de sondagem opcional | Comparação útil vs ferramenta vanilla | C |
| I01 | 7.8 | Argamassa, receita candidata 2+1+1→4 | Mineração vs produção de 64/256 blocos | A |
| I02 | 7.22 | Cinzel, durabilidade 128 hipotética | A/B vs Stonecutter + picareta | B |
| I03 | 7.13–7.14 | Molde de Relevo | Padrão reutilizado na base | B |
| I04 | 7.15–7.16 | Kit de Escoramento | Reparo com 2 alternativas e claims | B |
| I05–I08 | 7.13/7.18/7.22 | Instrumentos que competem entre si | No máximo uma ferramenta por ação | C |
| I09–I13 | 7.22 | Fibra/cordame/cultivo/pigmento/luz condicionais | 2 usos reais; sem sementes vazias | B/C |
| I14–I17 | 7.19–7.21/7.22 | Reverberação e instrumentos condicionais | Nódulo apenas depois do circuito | B/C |
| I18 | 7.22 | Marcador de campo opcional | Comparar tocha/mapa/placa | C |
| POI-01,02 | 7.9,7.10 | Selecionados para especificação | Marco + Abrigo, distintos | A |
| POI-03,08 | 7.15–7.16 | Travessia e Galeria | Apoios e alternativas | B |
| POI-05,06 | 7.12 | Pedreira e Jardim | Material vs habitat | B |
| POI-07,09,11 | 7.18 | Sondagem, Poço e Observatório | Não clonar baú | B/C |
| POI-10 | 7.14 | Arquivo arqueológico | Padrão realmente replicável | B |
| POI-12 | 7.21 | Casa de Ecos | BLOCKED_BY_M06 | Bloqueado |
| POI-04 | 7.23 | Câmara do Limiar | BLOCKED_BY_REWARD_DESIGN | Bloqueado |
| MOB-01 | 7.11 | Mariposa | F1 natural + mob spawning aditivo | A |
| MOB-02 | 7.17 | Habitante de fendas | Aviso e território, sem loot chave | B |
| MOB-03 | 7.23 | Guardião de Pedra | Só POI-04 aprovado, sem boss | Bloqueado |
| MOB-04 | — | Observador Esquecido | ADIADO, não canonizar | Adiado |
| MOB-05 | 7.14 | Besouro de camadas | Sedimento verdadeiro | B |
| MOB-06 | 7.21 | Grilo de Eco | Nódulo verdadeiro + M06 | B |

**Não aprovar automaticamente B21 ou uma estação técnica por ter chegado a 7.20/7.22.** A decisão do spike deve ser registrada no documento da feature/PR com evidência. Uma família C pode permanecer ADIADA indefinidamente e isso não é débito técnico.

---

# 8. ECONOMIA, RARIDADE E MÉTRICAS — DA HIPÓTESE À PROVA

## 8.1 Baseline R0 inalterada até medição

A Task 5.6 verificou **11.236 voxels de Dream Ore+Deepslate em 1.024 chunks** das quatro seeds (média amostral **10,97/chunk**). **324/11.236=2,88%** estavam adjacentes a ar; isso **não** significa descoberta fácil nem pouco minério minerável. O minério comum é size 6/count 2 e o deepslate size 5/count 2 em Y declarados; preservar.

A receita do Colar requer **3 Dust**; cada Despertador requer **12 Dust** recursivos, além de vanilla. Custo de um Colar e N retornos: **`3+12N` Dust**. 1/3/5 retornos planejados: **15/39/63 Dust**. Sem medição real de Fortune/Silk e tempo de coleta, não reequilibrar.

## 8.2 Contrato de medição da Task 6.8

| Tema | Métrica | Gate / decisão |
|---|---|---|
| Dream Ore | P50/P90 de obter 3/15/39 Dust no Survival + Fortune/sem Fortune | Não mudar rarity sem evidência de disponibilidade/dificuldade |
| F0 | P50/P90 descobrir, quantidade extraível, obra | Descoberta Early sem escassez paralisante |
| F1 | Saldo de colheita por tempo, 2 ciclos, espaço de cultivo | Se negativo, corrigir reprodução |
| Argamassa | Custo real de construir 64/256 blocos; yields 2/4/6 | Mid/Late deve reduzir fricção, não gerar free blocks |
| POIs | Starts candidate/accepted/accessible, P50/P90 tempo até visão/entrada, conflitos | Prevenir saturação/POIs invisíveis |
| Mobs | Spawn real por habitat, vanilla diff, limite e encontros | Não transformar flora em spawner |
| Ressonância | Custo circuito e 2 usos contra vanilla, path bounded | Cancelar se redundante |
| Sonhos | Tempo até ruína acessível, prob. loot, tempo de retorno com/sem item | P0 softlock e fadiga de recuperação |

**Status dos tempos e percentis:** `NOT_RUN`. A 6.8 propõe hipóteses (ex.: F0 P50 10–25 min *em contexto elegível*), **não resultados**. Sessões sem descoberta devem ser registradas como censuradas, não descartadas. Validar ajustes com mesmas seeds de regressão e outras independentes.

## 8.3 Recuperação estatística e risco

Baús dimensionais têm **25% configurados** para 1 Despertador. Sob sorteios independentes e acessíveis, `P(nenhum em N)=0,75^N`; mediana teórica 3 ruínas, média teórica 4, 90% de chance acumulada somente após 9, 95% após 11. **Isso não é tempo real de recuperação nem comprovação de loot acessível**.

**Proibição:** não aumentar p de 25% arbitrariamente para resolver estrutura suspensa. Primeira ação é corrigir suporte e localização física, depois testar loot e tempo até N ruínas, e só então abrir tarefa específica de ajuste de escape se realmente necessário. Morte/cama não são saída gratuita.

## 8.4 Estruturas no Overworld

Os `spacing/separation` de POI-01–12 na Task 6.8 são **cenários candidatos**, não registries existentes. 12 structure sets independentes podem saturar o mundo. Cada POI implementado deve registrar razão `accepted/candidate`, integridade de suporte, entradas e distância/tempo de descoberta. Worldgen nova só em chunks novos, sem retrogen por padrão.

---

# 9. PORTA DE QUALIDADE: MATRIZ DE TESTES OBRIGATÓRIOS POR PR

| Gate | Teste automatizado | Teste manual / prova | Bloqueio |
|---|---|---|---|
| **Compile** | `./gradlew --no-daemon clean build` com JDK25 | JAR real carregado | P0 |
| **Datapack 26.3** | JSON codec/refs/namespaces, itens/loot/structure sets | `/reload`, ausência de erros na carga | P0 |
| **Registries/assets** | Item def + models + textures + blockstates, translation keys | Inventário/colocação/hotbar, sem missing texture | P0 |
| **Economia** | Recipe DAG, materiais fonte, sem ciclos de dupe | Craft shift-click/Crafter e receita disponível em Survival | P0 |
| **Worldgen** | 4 seeds, chunks novos, candidato/aceito/regiões | Jogador encontra naturalmente, sem /place | P0 |
| **POI** | NBT/estrutura, supporting floor, chest TTL/loot | Aproximação, piso, 2 rotas e saída em Survival | P0 |
| **Flora** | Predicados locais e não-forceload | Dois ciclos, saldo, save/load | P0 |
| **Mobs** | Spawn placement, habitat, vanilla diff, parser AI | AI/anim/colisão, 2 players, Peaceful | P0 |
| **Render** | Geo/animations refs, PNG dims/UV/paths | Dia/noite, 2/4/8 mobs, performance visual | P1 salvo ausência de render |
| **Server** | Dedicated server smoke + logs sem registry crash | Conectar 2 clientes, interagir/abrir chest | P0 |
| **Saves** | Novo mundo e replay de save antigo sem retrogen | Inventário/baú/chunks preservados | P0 |
| **Sonhos** | Entrada/retorno/Curios/consumo/respawn | Preparar retorno e recuperar em 2 dimensões | P0 para liberar sonhos |
| **CI** | 4/4 seeds, build, scripts e artifacts | Link workflow e SHA exatos | P0 |

**Regras de interpretação:** um script `PASS` significa só o escopo que efetivamente mediu. Um método que lê JSON não prova QA de animação. /locate não valida rota, /give não valida economia, /loot não valida baú físico nem natural generation. Não converter `NOT_RUN` para `PASS` por aparência de terminal.

## 9.1 Matriz de laboratório que deve acompanhar cada entrega

Relatório por PR:
```text
Commit:
Branch:
Minecraft/NeoForge/GeckoLib:
Seed / Dimensão / Bioma:
Build JDK25: PASS | FAIL
Datapack/registry: PASS | FAIL
Worldgen real: PASS | FAIL | NOT_RUN
Cliente/Asset: PASS | FAIL | NOT_RUN
Survival sem cheats: PASS | FAIL | NOT_RUN
Multiplayer 2 players: PASS | FAIL | NOT_RUN
Server dedicado: PASS | FAIL | NOT_RUN
Save antigo/Restart: PASS | FAIL | NOT_RUN
Performance (TPS/FPS): PASS | FAIL | NOT_RUN
Evidência: workflow + log + screenshot + XYZ realmente observado
P0 remanescente:
Decisão: MERGE NA BRANCH | REVERT | PROTOTYPE ONLY | BLOCKED
```

## 9.2 Definição de pronto para worldgen

Quando uma task registra fonte natural, precisa também produzir:
- Seed verificada via `/seed` em **mundo novo**.
- Regiões amostrais pré-definidas, sem selecionar locais após saber onde há minério.
- Medição de **chunks elegíveis, tentativas, sucesso/rejeição**, chunk locations e contagem de blocos.
- Captura de um exemplo **natural**, sem `/setblock`, `/place` e `/give`.
- O jogador encontra/colhe por rota de jogo; se não, auditar legibilidade/bioma antes de subir chance.
- Testes de regressão em chunks antigos e biomas vanilla.
- Instrumentação de diagnóstico **opt-in**, no servidor, não em player tick e sem chunk forceload no mod.

## 9.3 Definição de pronto para mobs

- Entidade spawnada por sistema 26.3 que **não remove nenhuma entrada de mob vanilla**.
- Habitat natural comprovado e biome/mod spawns realmente elegíveis.
- Hitbox/colisão/teto/água coerentes; nenhuma entidade presa em bloco.
- AI com estado observável/contrajogo, animação GeckoLib associada e sem slide.
- Sem drop premium essencial, raid em base ou farm por jogador plantar líquen.
- Performance 2/4/8 exemplares; Peaceful, difficulty e multiplayer.
- Comandos /summon somente depois de ID real, mais teste de spawn **natural**.

---

# 10. COMANDOS DE TESTE — EXISTENTES, FUTUROS E COORDENADAS SEGURAS

## 10.1 Preparar repositório local e executar verificações que existem hoje

PowerShell / terminal, na branch de desenvolvimento:

```powershell
git checkout migration/minecraft-26.3
git pull origin migration/minecraft-26.3
java -version
./gradlew --no-daemon clean build
python3 scripts/ci/check_overworld_resource_chain.py
python3 scripts/ci/check_dream_ruins.py
```

No **Windows PowerShell**, se `./gradlew` não funcionar como executável, usar `./gradlew.bat`; se `python3` não existir, usar o interpretador Python 3 configurado no ambiente. Rodar `./gradlew runClient` apenas se a task/Gradle instalada expuser esse alvo (conferir `./gradlew tasks`); não assumir que inicia servidor de produção.

**CI existente**: `.github/workflows/build.yml` com 4 seeds e servidor dedicado. Verificar o status mais recente do commit real após a PR, não reaproveitar `success` de commit antigo.

## 10.2 Comandos Minecraft atuais e válidos

```mcfunction
/seed
/gamemode spectator
/gamemode survival
/give @s dreamsdimensions:dream_dust 15
/give @s dreamsdimensions:dream_necklace 1
/give @s dreamsdimensions:oneiric_awakener 1
/give @s minecraft:iron_pickaxe 1
```

**Teste de receitas R0 (com recursos fornecidos, não prova de mineração natural):**
```mcfunction
/give @s dreamsdimensions:dream_dust 15
/give @s minecraft:copper_ingot 3
/give @s minecraft:amethyst_shard 1
/give @s minecraft:stone 8
/give @s minecraft:clock 1
/give @s minecraft:feather 1
/give @s minecraft:glass_bottle 1
```

Criar Colar, 4 Pedras Infundidas, Núcleo e Despertador. Verificar `3+12=15` Dust consumidos pelo conjunto.

## 10.3 Estruturas *dimensionais* existentes: localização sem fabricar worldgen

```mcfunction
/execute in dreamsdimensions:campo_onirico_azul run locate structure dreamsdimensions:blue_dream_ruins
/execute in dreamsdimensions:dreamscape run locate structure dreamsdimensions:fragmented_dream_ruins
```

**Exemplo de coordenada REAL da Task 5.7 (não destino seguro homologado):** seed **20261108**, Dreamscape, baú observado em **`300 137 -4`** com **4/9** apoios locais. Para inspeção **somente em SPECTATOR**, depois de confirmar seed exata e build:

```mcfunction
/seed
/gamemode spectator
/execute in dreamsdimensions:dreamscape run tp @s 300 139 -4
```

**Não** executar teleporte em Survival para esse ponto sem inspecionar geometria e piso; `300 139 -4` é uma posição de **câmera** derivada do baú, não área de spawn segura nem garantia de mesma ruína em chunks antigos. No teste de acesso real, começar em posição conhecida e **viajar sem /tp**.

## 10.4 Diagnósticos opt-in existentes (somente servidor de teste)

`-Ddreamsdimensions.enableOreAudit=true` habilita comando de auditoria **apenas para laboratório**:

```mcfunction
/dream_ore_audit -4 -4 8
```

Ele **não gera/carrega chunks** e recusa regiões não carregadas; coordenadas `-4 -4` são chunk X/Z de **amostra pré-selecionada**, não local de minério. O auditor de ruínas também depende de flag opt-in. Usar forceload somente em servidor **descartável de CI**, limpar tickets ao finalizar.

## 10.5 Comandos que as Tasks F0/F1/POI/mobs DEVERÃO gerar

Estes são **modelos de checklist com IDS FUTUROS**, não comandos funcionais hoje:

```text
QUANDO B01 REALMENTE REGISTRADO:
  /give @s dreamsdimensions:marked_stone 64
  /setblock ~2 ~ ~ dreamsdimensions:marked_stone

QUANDO B10 REALMENTE REGISTRADO:
  /give @s dreamsdimensions:vestige_lichen 16

QUANDO POI-01 GANHAR REGISTRY VERIFICADO:
  /execute in minecraft:overworld run locate structure <id_real_do_poi>
  (não usar se POI-01 for uma feature em vez de structure)

QUANDO MOB-01 GANHAR ENTITY ID REAL:
  /summon <id_real_do_mob> ~ ~ ~
  (não usar summon como evidência de spawn natural)

QUANDO UMA SEED FOR AUDITADA NO CI:
  /execute in minecraft:overworld run tp @s <X> <Y> <Z>
  (apenas com XYZ reais, piso seguro e modo de jogo verificado)
```

**Critério de entrega de cada task futura:** fornecer ao usuário **comandos específicos, copiados do registry e do log efetivo**, incluindo `/give`, `/setblock`, `/summon`, `/locate` e `/tp` **quando aplicáveis**, com separação entre testes de laboratório e progresso natural de Survival. Nunca dar ID inventado ou coordenada de outra seed.

---

# 11. MODULARIDADE DE ARTE E ASSETS — TASK 6.7 APLICADA

| Slice | Padrão mínimo de arte | Quando GeckoLib? |
|---|---|---|
| F0 Pedra | 16×16 blocos, model/blockstate, Item client definition + model/item | Nunca para pedra estática |
| F1 Líquen | 16×16 RGBA, suporte/cutout, estados legíveis | Não para planta estática |
| Argamassa | Item PNG 16×16 e uso in situ, feedback sem shader | Não |
| Mariposa | Blockbench `.bbmodel`, atlas 64/128 conforme UV, `.geo.json`/animations | **Sim**, se clips exigirem |
| POI Marco/Abrigo | Kit de blocos vanilla-first, NBT/testes e screenshot 4 lados | Não; worldgen nativo |
| M02 Arqueologia | Sedimento, estados brush, padrão/ícone de molde | Não por padrão |
| M03 Travessia | Rocha cediça com 3 estados de aviso, modelo escora | Não por padrão |
| M06 Eco | Blocos condutores/Emissor/Receptor, orientação, feedback state | Só se necessidade real, não por brilho |
| M2/M3 mobs | Bone tree e AI distintos, clip de alerta/cooldown | Sim, se aprovado |

**Arte não compensa mecânica falha:** tile bonito não torna worldgen acessível, `.geo.json` não move entidade, partícula não gera light level, NBT não garante apoio e /give não prova mineração. Arquivos editáveis em `art_sources/` só quando realmente criados e com licença de uso apropriada.

---

# 12. GERENCIAMENTO DE RISCO E ROLLBACK

## 12.1 Riscos P0 que bloqueiam qualquer release

| Risco | Evidência conhecida / hipótese | Mitigação executável | Dono no roadmap |
|---|---|---|---|
| Ruína de Dreamscape parcialmente apoiada | **4/9** na seed 20261108 | Inspeção, correção de placement/preflight só se comprovada | 7.0 |
| Saída de sonho inviável | Loot 25%, sem tempo de recuperação medido | Rotas, acessibilidade, persistência, QA Survival | 7.0/7.24 |
| Drop gratuito/Dust duplicado | R0 já auditado, novos recipes candidatos | DAG/loot/shift-click, server authoritative item use | 7.8, 7.24 |
| Dispositivo de rede infinito | M06 apenas hipótese | Bound path, no forceload, no scan, no loops | 7.19–7.20 |
| Mob Overworld remove fauna vanilla | EnvironmentAttributes overlay 26.3 | Spawns aditivos + antes/depois + caps | 7.11/7.17 |
| Grief de bloco/ponte | Rocha Cediça hipotética | Context-only, full warning, rotas alternativas | 7.15/7.16 |
| Estrutura inválida no terreno | Um chest correto não garante piso | Check de suporte/entrada/saída em multi-seed | 7.9/7.10/7.16 |
| Quebra de saves antigos | Novos block IDs/worldgen | Novo chunk, sem retrogen; mundo backup, IDs estáveis | Todas |
| Render/servidor desync | GeckoLib e clientes | Client-only models + dedicated server smoke + 2 players | 7.11+ |
| Scope creep em 30 recursos | Catalog inclui alternativas | Gate A/B e PR por slice, descartes explícitos | 7.22 |

## 12.2 Plano de rollback por tipo

- **Code/recipe/loot/asset:** revert commit/PR isolado com testes de startup e save; **não** remover ID de bloco/item já salvo sem estratégia de migração.
- **Worldgen:** desligar futuras colocações pelo datapack/biome modifier, mas blocos/ruínas já gerados continuam no save; **não prometer remoção do mundo existente**.
- **Estrutura NBT:** mudar template afeta novos placements, **não conserta instâncias já geradas**; registrar limitação de migração/recuperação manual quando necessário.
- **Entity AI:** rollback de spawn/behavior deve preservar entity IDs salvos e evitar crashes no load; testar clientes em versões de mod coerentes.
- **Config de sonhos:** se havia risco P0, manter `dream_entry_enabled=false` até homologação. Revertar erro não dá ao jogador teleport gratuito.
- **Criativo/protótipos:** usar branches e mundos descartáveis; nunca distribuir worldgen spike não homologado como release final.
- **Main:** merge só com aprovação explícita e revisão; este planejamento não concede essa autorização.

---

# 13. ROTINA DE ENTREGA E RELATÓRIO PARA CADA TASK

## 13.1 Checklist obrigatório ao concluir PR

```markdown
# Task 7.X — Entrega
- Branch e SHA:
- Objetivo de gameplay:
- Novo comportamento observável:
- Fonte de recursos e consumidor:
- Arquivos alterados/novos:
- Padrão reutilizável adotado:
- Fonte oficial 26.3/NeoForge/GeckoLib revisada:
- Comandos Minecraft de QA (IDs reais, seed/XYZ reais):
- BUILD: PASS/FAIL
- STATIC DATA/REGISTRY: PASS/FAIL
- DEDICATED SERVER: PASS/FAIL
- WORLDGEN NATURAL: PASS/FAIL/NOT_RUN
- LOOT/CRAFTING REAL: PASS/FAIL/NOT_RUN
- SURVIVAL: PASS/FAIL/NOT_RUN
- MULTIPLAYER/RESTART: PASS/FAIL/NOT_RUN
- VISUAL/ANIMATION: PASS/FAIL/NOT_RUN
- PERFORMANCE: PASS/FAIL/NOT_RUN
- Valor vs vanilla testado? PASS/FAIL/NOT_RUN
- Riscos P0 abertos:
- Link dos logs/CI/evidências:
- Próxima task liberada:
- Merge na main: NÃO REALIZADO
```

## 13.2 Critérios de aceite dos prompts de engenharia (para as futuras execuções)

Todo prompt específico 7.x gerado a partir deste roadmap deve exigir:
1. Auditar **HEAD da branch, docs da task, código afetado e APIs atuais** antes de alterar;
2. Implementar **no repo real**, não fornecer pseudo-code fingindo merge;
3. Fazer código **server-authoritative** onde altera mundo/itens;
4. Usar **dados e assets existentes** sempre que fizer sentido;
5. Criar **testes que falham se requisito quebrar** (sem hardcode frágil do texto do documento);
6. Gerar **exemplos de uso reais e comandos de TP/locate/give**, sem coordenadas falsas;
7. Proteger Curios, entrada/retorno, spawn vanilla, save antigo, multiplayer e performance;
8. Executar build/scripts/CI e registrar o que **não foi testado**, sem prometer conclusão futura;
9. Commitar/push **somente branch autorizada**, sem main merge;
10. Exibir usuário a próxima task e qual gate foi comprovado.

---

# 14. CONTRATO DE DECISÕES EM ABERTO (NÃO PERDER O ESCOPO MAIOR)

| Questão aberta | Dono | Por que ainda está em aberto | O que NÃO fazer |
|---|---|---|---|
| Recuperação: chance 25% é justa? | 7.0 e 7.24 | Acessibilidade/tempo não foram medidos | Aumentar chance arbitrariamente |
| F0/F1: frequência ideal? | 7.4/7.6/7.24 | Candidatos 6.8 não são worldgen real | Confundir count com minério visível |
| Argamassa: yield 4? | 7.8/7.24 | Cultivo e custo de obra não foram medidos | Gravar 4 como “balanceado” |
| Cinzel melhora experiência? | 7.22 | A/B falta; vanilla stonecutter já funciona | Criar ferramenta por estética |
| Leitor consegue ler 4 lados? | 7.22 | Geometria F1 pode ser cobertura | Registrar sensor que sempre lê zero |
| Sedimento e Molde valem sistema próprio? | 7.13 | Precisa aplicação real em base | Criar loot de pesquisa sem função |
| Rocha Cediça aumenta escolha ou frustrante? | 7.15 | Falta teste de aviso/risco/apoio | Espalhar armadilhas no Overworld |
| M06 supera circuitos vanilla? | 7.19 | Rede é só hipótese | Criar nódulo técnico primeiro |
| Câmara tem reward adequado? | 7.23 | `BLOCKED_BY_REWARD_DESIGN` desde 6.6 | Criar boss/key para fabricar valor |
| 12 POIs ao mesmo tempo saturam? | 7.12/7.24 | Spacing da 6.8 é candidato | Copiar JSON sem medição conjunta |
| Mobs extras têm habitat e diferencial? | 7.14/17/21 | Dependem de sistemas novos | Criar spawn só para completar número |
| Ramo onírico entra cedo demais? | 7.0/7.24 | Falta P50/P90 de 3/15/39 Dust | Subir custo ou mudar probabilidade cegamente |

**Regra de reunião decisória:** uma hipótese `C` que não passou A/B é removida do plano de produção, mas não do histórico do catálogo; reduzir catálogo em runtime é qualidade, não fracasso.

---

# 15. RESUMO DA ORDEM PRÁTICA

```text
AGORA:
  7.0 Homologar segurança da recuperação existente (P0, sem alterar odds)
  7.1 Instrumentação/CI reutilizável (em paralelo quando seguro)
  7.2 Contrato mínimo de integração

PRIMEIRA ENTREGA NOVA:
  7.3 B01 pedra + asset/registro
  7.4 B02 lapidação + geração natural
  7.5 Survival/verificação F0

PRIMEIRA EXPANSÃO VIVA:
  7.6 B10 Líquen natural
  7.7 Cultivo e duas colheitas
  7.8 Argamassa real
  7.9 Marco / 7.10 Abrigo
  7.11 Mariposa / 7.12 Pedreira e Jardim

EXPLORAÇÃO REAL:
  7.13–7.14 Arqueologia como técnica
  7.15–7.16 Travessia e Galeria
  7.17 Habitante / 7.18 outras estruturas

OPCIONAIS CONDICIONAIS:
  7.19 prova M06 → se PASS, 7.20 circuito/minério e 7.21 Casa/Grilo
  7.22 mais blocos/itens/biomas locais conforme demanda
  7.23 Câmara/Guardião apenas com recompensa útil

FECHAMENTO:
  7.24 Balancear com Survival/CI/sem dupe
  7.25 Release com zero P0 e compat
```

**Primeira ação recomendada depois desta Task 6.9: iniciar a Task 7.0.** A única razão para priorizar um problema do ramo dos sonhos antes de novos blocos é **risco de jogador preso/ruína inacessível**; a implementação do Overworld poderá avançar em paralelo sem habilitar o sistema de entrada publicamente. A primeira entrega realmente nova será F0.

---

# 16. CRITÉRIOS DE ACEITE DA TASK 6.9

## Planejamento — concluído

- [x] Reuniu 6.0–6.8 e conferiu registries, CI, assets, documentação e segurança atuais.
- [x] Diferenciou conteúdo existente, selecionado, condicionado, bloqueado e cancelável.
- [x] Decompôs programa em **26 tasks 7.0–7.25**, com dependências, valor, DoD e risco.
- [x] Não exigiu todos os candidatos para lançar as primeiras milestones.
- [x] Registrou duas frentes: **P0 segurança do retorno** e **F0 primeiro conteúdo Overworld**.
- [x] Mapeou B01–B30, I01–I18, POI-01–12 e MOB-01–06 para tasks/gates.
- [x] Protegeu R0: 3+12N Dust, dois minérios e ruínas dimensionais com chance configurada 25%.
- [x] Preservou o dado real de 11.236 minérios em 1.024 chunks, sem inventar discovery time.
- [x] Deu arquitetura de reuso mínima, QA, comandos, CI, seeds e modelo de relatório.
- [x] Definiu spike M02/M03/M06 com **capacidade de CANCELAR** sem forçar conteúdo.
- [x] Marcou Câmara e Casa de Ecos como condicionadas e sem boss/endgame obrigatório.
- [x] Definiu rollback/saves/multiplayer e Git branch sem merge automático.
- [x] Nenhum Java, assets, NBT, recipe, loot ou worldgen foi alterado.

## Engenharia/Survival — ainda não executado nesta Task documental

- [ ] Task 7.0: segurança/loot/retorno com evidência real e correção apenas se comprovada.
- [ ] Task 7.1: instrumentação reutilizável implementada caso necessária.
- [ ] F0 completo na branch com build + mundo real + Survival.
- [ ] F1 e Argamassa testados e calibrados.
- [ ] POIs e fauna naturais, sem suprimir vanilla.
- [ ] Spikes M02/M03/M06 avaliados e promovidos/rejeitados.
- [ ] Todas as mudanças funcionais aprovadas em CI, cliente, multiplayer e saves antigos.
- [ ] Release segura com zero P0, sem main merge nesta Task.

**Status:** **TASK 6.9 — ROADMAP EXECUTIVO DOCUMENTADO E CONCLUÍDO.** Programação de conteúdo das Tasks 7.x não iniciada por este commit. O roadmap evita escopo circular, dá ordem racional à expansão e mantém a ambição artística e de gameplay sem criar dívida técnica prematura.

---

*Fim — Task 6.9 | Documento executivo de implementação, não uma PR funcional.*
