# Estado atual do DreamsDimensions

**Referência:** branch `migration/minecraft-26.3`, 12/11/2026. Este arquivo descreve **código e recursos existentes**, não um roadmap de desejos.

## Definição de estado

- **IMPLEMENTADO:** classes/registries/datapacks estão no repositório.
- **CI_VERIFICADO:** código compila e passou nos testes automatizados pertinentes.
- **MANUAL_PENDENTE:** ainda não existe prova de experiência real com jogador para todos os critérios.
- **DESABILITADO:** existe no código, mas uma configuração de segurança impede seu uso normal.
- **EXPERIMENTAL_INATIVO:** código ou teste exploratório sem integração de gameplay. Não contar como feature.

## Inventário verificável

| Sistema | Estado | Fonte de verdade |
| --- | --- | --- |
| Minecraft 26.3 / NeoForge 26.3.0.101 / Java 25 | IMPLEMENTADO, CI_VERIFICADO | `gradle.properties`, `build.gradle`, `.github/workflows/build.yml` |
| Catálogo com 2 dimensões de teste | IMPLEMENTADO, CI_VERIFICADO | `dimension/DreamDimensionCatalog.java`, datapacks `dimension/` |
| Terreno, vegetação e atmosfera do Campo Onírico Azul | IMPLEMENTADO, CI_VERIFICADO; visual MANUAL_PENDENTE | `worldgen/`, `scripts/ci/check_vegetation.py`, `check_atmosphere.py` |
| Terreno fragmentado, ilhas, pontes e ecossistema da Dreamscape | IMPLEMENTADO, CI_VERIFICADO; experiência MANUAL_PENDENTE | `worldgen/`, `scripts/ci/check_dreamscape_*.py` |
| Dormir por 100 ticks, seleção ponderada, Colar dos Sonhos | IMPLEMENTADO; entrada DESABILITADA por padrão | `event/SleepTeleportHandler.java`, `config/DreamEntryConfig.java`, `service/DreamEntryPolicy.java` |
| Despertador consumível, retorno seguro e respawn onírico | IMPLEMENTADO, CI_VERIFICADO; Survival/multiplayer MANUAL_PENDENTE | `item/OneiricAwakenerItem.java`, `service/OneiricReturnPolicy.java`, `service/DreamRespawnService.java` |
| Dream Ore, Deepslate Dream Ore, Dream Dust e receitas | IMPLEMENTADO, mundo natural CI_VERIFICADO; obtenção manual MANUAL_PENDENTE | `ModBlocks.java`, `ModItems.java`, `data/.../worldgen/`, `scripts/ci/audit_overworld_ore_runtime.py` |
| Pedra Marcada natural + versão lapidada + Stonecutter 1:1 | IMPLEMENTADO, worldgen/loot CI_VERIFICADO; Survival MANUAL_PENDENTE | `ModBlocks.java`, `check_marked_stone_7_4.py`, `f0_homologation.py` |
| Fragmento Errante e Vigia Adormecido | IMPLEMENTADO, testes CI; comportamento natural com jogadores MANUAL_PENDENTE | `ModEntities.java`, `entity/`, `scripts/ci/check_*behavior.py` |
| Framework de IA/habitat/população | IMPLEMENTADO e utilizado por duas espécies | `entity/spawn/DreamMobPopulationService.java`, `entity/habitat/` |
| Eco do Musgo e Pacto de Vigília | IMPLEMENTADO; homologação completa MANUAL_PENDENTE | `event/EchoMossInteractionHandler.java`, `registry/ModEffects.java` |
| Ruínas de recuperação e loot nativo | IMPLEMENTADO, CI_VERIFICADO; travessia/baú aberto em Survival MANUAL_PENDENTE | `data/.../worldgen/structure/`, `scripts/ci/audit_dream_ruins_runtime.py` |
| Telemetria e relatórios das quatro seeds | IMPLEMENTADO, CI_VERIFICADO; coleta opt-in | `qa/DreamQaRecorder.java`, `scripts/ci/` |
| Qualidade visual Tasks 8.0–8.7 | **MELHORIAS DE ASSETS E CLIENTE IMPLEMENTADAS**; avaliação visual/Survival MANUAL_PENDENTE | `assets/.../textures/`, `client/feedback/`, `block/OneiricCoreBlock.java`, `scripts/ci/visual_release_gate.py` |
| Task 9.0 — Contrato Universal e catálogo Overworld | **DESIGN_ONLY**; implementação da F1 e playtest não realizados | [Contrato 9.0](TASK_9_0_OVERWORLD_GAMEPLAY_CONTRACT.md), [catálogo 9.0](OVERWORLD_CONTENT_CATALOG_TASK_9_0.json), `scripts/ci/check_overworld_contract_9_0.py` |

**Crítico:** as duas dimensões atuais são exemplos iniciais. **Não** amarrar progressão geral do DreamsDimensions à lore, aos materiais ou às peculiaridades exclusivas delas.

## Bloqueios e pendências reais

1. **Release gate P0:** entrada em sonhos continua desligada por padrão; ainda faltam testes com jogadores de viagem/retorno, chance, Despertador, cama destruída, respawn e segurança de recuperação em ruínas.
2. **Compatibilidade:** mundo migrado de Minecraft 1.21.4 para 26.3 só pode ser validado numa **cópia** real de save, com teste de blocos, entidades, attachments, biomas e estruturas.
3. **Experiência multiplayer e visual:** build/servidor dedicado não prova renderização GeckoLib, qualidade dos biomas, dificuldade de exploração, drops físicos, nem sincronização entre dois jogadores.
4. **Economia F0:** 7.4–7.5 mediram pedras geradas e loot nativo, **não** percentis de descoberta por jogadores.
5. **Overworld:** ainda não existe sistema jogável de anomalias; o protótipo Java inerte da antiga 7.6 foi retirado na Task 8. Não alegar Cicatriz Silenciosa, Eixo Fraturado, portal ou manifestação gerada no mapa.
6. **Assets:** 17 texturas legadas de terreno/vegetação foram redesenhadas em 16×16, além das 3 de minério/pó já feitas; sprites premium do Despertador/Núcleo mantidos em 32×32. São necessários playtests visuais e medição de desempenho em cliente.
7. **Design futuro:** nenhum novo minério, planta, mob, dimensão ou POI entra no backlog executável sem papel claro em Survival e critério de aceite.

## Fonte das provas

Consultar [QA e validação](QA_E_VALIDACAO.md), além dos relatórios técnicos preservados: `DREAM_RECOVERY_SAFETY_TASK_7_0.md`, `DREAM_RUINS_TASK_5_7.md`, `OVERWORLD_ORE_DISTRIBUTION_TASK_5_6.md` e `DREAMSDIMENSIONS_TASK_7_5.md`. Todos os arquivos removidos da árvore continuam recuperáveis via histórico de commits Git.
