# TASK 5.2 — Matriz de testes de viagem, retorno e respawn

**Escopo:** Minecraft 26.3 / NeoForge 26.3.0.101; HEAD lido `c5e5c5ba63cab73e3de9e1ff1227992a4c3b52ed`. Matriz da **especificação futura**. Nenhuma linha equivale a execução in-game.

**Legenda de evidência:** `C` = leitura do código real da branch; `PV` = pendente de teste real; `futuro` = ainda não implementado. Cada item de "Automação proposta" é um plano, não um teste executado. Diferença indica gap entre o código atual e o contrato. [Contrato principal](TRAVEL_RETURN_CONTRACT_TASK_5_2.md).

## 1. Cobertura obrigatória: 30 cenários

| # | Caso | Situação atual — evidência de código | Comportamento exigido | Diferença | Arquivos afetados | Risco | Automação proposta | Ensaio manual | Task | Evidência |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | Sem colar no sono | Transporte após 100 ticks, sem inspeção de item (C) | Somente sono vanilla | Gatilho indevido | SleepTeleportHandler | Teleporte não solicitado | Unit: elegibilidade false | Dormir 3 noites sem item | 5.3 | C; runtime PV |
| 2 | Colar equipado | Item não existe (C) | Rolar uma vez por sono completo | Slot e chance ausentes | ModItems; SleepTeleportHandler | Não funcionar no server | Unit: slot true e 1 roll | Colar no slot, inspecionar log | 5.3 | C; runtime PV |
| 3 | Colar no inventário | Sem implementação (C) | Não habilita viagem | Elegibilidade ausente | EquipmentPolicy; SleepTeleportHandler | Bypass de slot | Unit: inventário ignorado | Mover colar para mochila | 5.3 | C; runtime PV |
| 4 | Colar na offhand | Sem implementação (C) | Não habilita viagem | Offhand não filtrada | EquipmentPolicy | Bypass | Unit: offhand false | Colar só em offhand | 5.3 | C; runtime PV |
| 5 | Remoção do colar durante sono | Sem implementação (C) | Sem chance se ausente na confirmação | Falta rechecagem | SleepTeleportHandler | Reroll/troca | Unit: remove e invalida | Remover via 2 jogadores/comando | 5.3 | PV |
| 6 | Sorteio negativo | Não existe chance global (C) | Conclui sono vanilla, não teleporta | Chance não modelada | SleepTeleportHandler | Pular noite incorretamente | Unit: RNG força falha | RNG instrumentado 0% em teste | 5.3 | C; runtime PV |
| 7 | Sorteio positivo destino disponível | Entrada direta atual por 100 ticks (C) | Escolhe habilitado e chega seguro | Falta gate de chance | DreamTeleportService; SleepTeleportHandler | Duplicar viagem | Unit: uma transição por sono | 100% em build de teste, inspecionar | 5.3 | C; runtime PV |
| 8 | Sorteio positivo destino indisponível | Filtro level null existe (C) | Não teleportar, manter vanilla | Rechecagem após roll necessária | DreamTeleportService | Destino inválido | Unit: catálogo sem world | Desabilitar dimensão em mundo teste | 5.3 | C; runtime PV |
| 9 | Falha de chegada segura | Fallback plataforma 5x5 e falha existem (C) | Sem teleport se não seguro | Efeito worldgen e retry a testar | DreamSpawnHelper; DreamTeleportService | Spawn/terreno alterado | GameTest: área bloqueada | Criar ambiente fechado de teste | 5.3/5.5 | C; runtime PV |
| 10 | Entrada sem Despertador | Permitida (C) | Explorar até encontrar loot de ruína | Loot/estrutura ausentes | DreamTeleportService; loot tables futuras | Softlock P0 | Data: 2 dimensões com loot | Survival sem /give, procurar estrutura | 5.7 | C; runtime PV |
| 11 | Entrada com Despertador | Permitida (C) | Item habilita saída ao usar, não na entrada | Consumo faltante | OneiricAwakenerItem | Retorno infinito | GameTest: item no inventário | Entrar com 1 e ativar | 5.4 | C; runtime PV |
| 12 | Despertador encontrado no baú | Nenhuma ruína/loot (C) | Loot raro e único por baú | Fonte ausente | loot_table; structure sets | Sem recuperação real | Data: registro loot; unload/reload | Abrir baú natural e reabrir | 5.7 | C; futuro |
| 13 | Interromper ativação | Uso contínuo 60 ticks (C) | Sem teleport nem consumo | Cancelar antes do fim a testar | OneiricAwakenerItem | Consumo antecipado | GameTest: stop em 59 ticks | Soltar botão no tick 40 | 5.4 | C; runtime PV |
| 14 | Uso concluído + retorno confirmado | Chama teleport mas mantém stack (C) | Retorno e exatamente 1 consumido | Consumo ausente P0 | OneiricAwakenerItem; DreamReturnHelper | Duplicação | Unit: commit exatamente uma vez | Contar antes/depois | 5.4 | C; runtime PV |
| 15 | Uso concluído + retorno falho | Sucesso sem validar resultado (C) | Sem consumir, sem sucesso falso | Commit não verificado | OneiricAwakenerItem | Perda/falso positivo | Unit: transition indisponível | Forçar ausência do Overworld em harness | 5.4 | C; runtime PV |
| 16 | Dois usuários usam juntos | Sem lock específico do item (C) | Cada um retorna/consome 1 | Idempotência ausente | OneiricAwakenerItem | Corrida e duplicação | Integration: 2 UUID simultâneos | Dois clientes servidor dedicado | 5.4 | C; multiplayer PV |
| 17 | Morte no Dreamscape | Sem handler específico (C) | Respawn seguro no Dreamscape; vanilla drops | Fallback vanilla pode sair | Respawn handler novo; attachment | Suicídio como saída | GameTest: death and local respawn | /kill em Survival na Dreamscape | 5.5 | C; runtime PV |
| 18 | Morte no Campo Azul | Sem handler específico (C) | Respawn seguro no Campo Azul | Fallback vanilla pode sair | Respawn handler novo; attachment | Spawn em altura insegura | GameTest: void/ground | /kill em Survival no Campo | 5.5 | C; runtime PV |
| 19 | Morte keepInventory | Sem alteração do vanilla (C) | Manter itens, mas respawn onírico | Destino não roteado | Respawn handler | Duplicar/perder inventário | Integration: gamerule true/false | /gamerule keepInventory true; /kill | 5.5 | C; runtime PV |
| 20 | Morte Hardcore | Nenhum override de morte (C) | Hardcore permanece vanilla, sem ressuscitar | Precisa regressão | Respawn handler | Quebrar Hardcore | Integration: death hardcore | Mundo descartável Hardcore, morrer | 5.5 | C; runtime PV |
| 21 | Definir spawn em cama onírica | bed_rule permite spawn sempre (C) | Pode marcar, nunca retornar; validar sem dormir | Cama atual também aceita sono se escuro | dimension_type JSON; respawn handler | Spawn externo/obstruído | GameTest: set spawn sem sleep | Clicar cama antes/depois da 5.5 | 5.5 | C; runtime PV |
| 22 | Tentar dormir sem Despertador no sonho | can_sleep=when_dark (C) | Sono negado, sem fuga gratuita | Regra difere | dimension_type JSON | Exploit ou confusão | GameTest: sleep denied | Interagir com cama na noite | 5.5 | C; runtime PV |
| 23 | Tentar dormir com Despertador no sonho | can_sleep=when_dark (C) | Sono negado; usar item manualmente | Regra difere | dimension_type JSON; item | Sono substitui despertar | GameTest: no return via bed | Tentar cama com item no bolso | 5.5 | C; runtime PV |
| 24 | Desconexão durante entrada | Limpador logout existe (C) | Sem retry/estado pendurado; destino consistente | Ordem transitória incerta | SleepTeleportHandler; DreamTeleportService | Duplo teleport | Integration: disconnect during commit | Fechar cliente após 100 ticks | 5.3 | C; runtime PV |
| 25 | Desconexão durante Despertador | Sem lock no item (C) | Cancelamento sem perda, sem duplicate | Finalização/relog a validar | OneiricAwakenerItem | Duplo consumo | Integration: logout a 59 ticks | Desligar cliente durante uso | 5.4 | C; runtime PV |
| 26 | Restart no sonho | dream_return serializado copyOnDeath (C) | Continuar no sonho, manter âncora/itens | Âncora local ausente | ModAttachments; respawn novo | Softlock após restart | Integration: save/load attachment | Reiniciar servidor com jogador no sonho | 5.5 | C; runtime PV |
| 27 | Mudança administrativa de dimensão | /execute in não passa por sono (regra vanilla) | Permitir comando; não simular entrada normal | Definir fallback admin | DreamTeleportService; respawn handler | Retorno inconsistente | Integration: dimension change by op | /execute in ... run tp @s ... | 5.5 | PV |
| 28 | Morte após perder último item | Não há baús nem respawn próprio (C) | Respawn no sonho e busca possível | Softlock crítico | Respawn handler; structures; loot | Progressão impossível | GameTest: sem equipamento e death | Destruir item, morrer, buscar ruína | 5.5/5.7 | C; runtime PV |
| 29 | Usar Despertador no Overworld | use retorna PASS fora de sonhos (C) | Não iniciar uso, sem consumo | Conforme código; testar | OneiricAwakenerItem | Ativação indevida | Unit: dimension gating | /give e clique no Overworld | 5.4 regressão | C; runtime PV |
| 30 | Consumir exatamente uma unidade | Stack máximo 1 mas sem shrink (C) | 0 falha, 1 sucesso, inclusive 2 players | Violação P0 | ModItems; OneiricAwakenerItem | Item eterno ou perda | Unit: sucesso/falha/reentrada | /clear count antes/depois | 5.4 | C; runtime PV |

## 2. Laboratório — comandos de Minecraft 26.3

Abrir mundo **descartável**/duplicado, seed conhecida, cheats habilitados somente para preparar laboratório. `X Y Z` representa **posições seguras de piso verificadas no próprio mundo**. NÃO substituir por coords arbitrárias. Para testes reais em Survival, depois de preparar posição/itens, voltar para `/gamemode survival`.

```mcfunction
/seed
/gamemode creative
/gamemode survival
/give @s dreamsdimensions:oneiric_awakener 2
/clear @s dreamsdimensions:oneiric_awakener 0
/time set night
/execute in dreamsdimensions:dreamscape run tp @s X Y Z
/execute in dreamsdimensions:campo_onirico_azul run tp @s X Y Z
/execute in minecraft:overworld run tp @s X Y Z
/data get entity @s Pos
/gamerule keepInventory true
/gamerule keepInventory false
/kill @s
```

**Plano:** (1) `/seed` registrar mundo e F3/posição; (2) localizar **em jogo** piso seguro e copiar coordenadas X Y Z de cada dimensão; (3) usar os comandos de `/execute in` com essas coordenadas; (4) `/give` concede item só para teste; (5) `/clear @s ... 0` **consulta presença/contagem sem remover**, confirmar resposta do comando no build; (6) usar por 60 ticks e repetir contagem; (7) para morte, `/kill @s` em mundo de teste; (8) confirmar spawn e gamerules; (9) no final voltar ao Overworld e conferir estado.

Registro obrigatório por execução: `seed | dimensão | X Y Z | Minecraft/NeoForge | comando | esperado | observado | logs | data/hora | executor`. **Não existem coordenadas de spawn natural verificadas nesta Task 5.2.** A seed `20261108` foi usada em homologações históricas de relevo, mas isso **não** comprova piso seguro para todos os pontos após qualquer mudança de mundo. Repetir também nas seeds `14011`, `-904732`, `73199013`.

## 3. Design dos testes automatizados por camada

**UNIT TEST (5.3/5.4, propostos):** `entryEligible` com/sem slot Curios e offhand/inventário; um sorteio por ciclo; RNG determinístico 0/1; interrupção e wake duplicado; pesos vs chance; lista sem dimensão carregada; clear de UUID; compra zero item antes de sucesso; teleporte `null`/destino diferente/exceção; commit unitário e trava de reentrada. Manter funções puras para política, sem extrair framework de estados inteiro.

**BUILD (existente em CI, execução desta task: não realizada):** `./gradlew --no-daemon clean build` JDK 25. Regressões sem mudanças nos IDs, registro de mobs, mods, arquivos de recurso.

**DATA VALIDATION (5.5/5.7, proposta):** parse dos dois `dimension_type` após 5.5; assert `bed_rule.can_sleep=never` e decisão de spawn; validar `loot_table` de Despertador, estruturas em ambas as dimensões, sem reroll de baú; codec do `dream_return` preserva `dimension/pos/yaw`; test de migração para saves antigos com novo attachment ausente; não executar no contrato.

**SERVER SMOKE (já configurado na CI, execução desta task: não realizada):** `.github/workflows/build.yml` inicia `runServer` em 4 seeds, procura `Done (` e erros de datapack/loot; **não entra automaticamente num ciclo real de sono/morte/despertar**. Manter os checks existentes, adicionar testes de integração de viagem em task de código.

**GAMETEST (5.3–5.5, proposto):** compatibilidade do `gameTestServer` no `build.gradle` é observável, mas a página oficial de docs consultada está em 26.1: confirmar `test_function`, `test_instance`, `test_environment` e APIs com o SDK 26.3. Simular 100 ticks de sono elegível e respawn com ambiente real; validar piso, cama, dimensão e consumo. Não afirmar GameTest executado por ter configuração Gradle.

**MANUAL SURVIVAL (necessário):** minerar Dream Dust **natural** no Overworld, craftar itens, equipar Colar (5.3), dormir com chance verificável, retornar gastando exatamente um Despertador (5.4), morrer sem retorno grátis (5.5), achar Despertador em ruínas **naturais** com ferramentas escassas (5.7), preservar inventário/XP conforme gamerule. Só disponível após as tasks correspondentes.

**MULTIPLAYER (necessário):** 2 jogadores dedicados; 1 com Colar e 1 sem, sleeping vote; 2 Despertadores simultâneos; mão principal vs secundária; morte/perda de item; disconnect/reconnect durante animação, morte e chegada; troca administrativa; Restart em sonho; save antigo. Resultado precisa registrar logs por UUID e hashes/contagem do ItemStack antes/depois.

## 4. Cenários extremos e critérios de aceite

- **P0:** entrada sem Colar, teleporte repetido no wake, cancelamento punido, sucesso reportado sem retorno, consumo 0 ou >1, morte leva a Overworld intencionalmente, explosão de cama, spawn mortal, dimensões desabilitadas escolhidas e ficar preso sem forma real de obter item.
- **P1:** rollback/erro de teleporte e chunk, stack trocado durante animação, logout a 99 ticks de sono, dois eventos wake no mesmo tick, baú explorado por um usuário e visto pelo segundo, dois jogadores colocando/quebrando cama, mundo antigo com `dream_return` salvo.
- **P2:** taxa de viagem e de ruínas sem balanceamento, mensagens localizadas, efeito Slow Falling no Campo Azul não persistir indevidamente após saída e preço real em Dream Dust.
- **Bloqueio para release:** a Task 5.3 isolada **não habilita lançamento Survival** antes de respawn 5.5 e recuperação 5.7. Nenhum `/give`/TP administrado prova economia/loot.
- **Sem ambiguidade de evidência:** `PASS` somente com arquivo de log ou output real do teste; `NOT RUN` para todos os testes desta Task 5.2.

## 5. Status desta execução (10/11/2026)

| Camada | Resultado |
| --- | --- |
| Auditoria estática de Java, JSON, receitas e docs no GitHub | **EXECUTADA — inspeção manual, não parser/compilação** |
| Conferência de docs oficiais e classes NeoForge 26.3.x | **EXECUTADA — referência de fonte**, sem teste executável de API |
| UNIT TEST | **NOT RUN** |
| BUILD | **NOT RUN** |
| DATA VALIDATION automatizada | **NOT RUN** |
| SERVER SMOKE | **NOT RUN** |
| GAMETEST | **NOT RUN** |
| MANUAL SURVIVAL | **NOT RUN** |
| MULTIPLAYER | **NOT RUN** |

A execução de CI após push, caso ocorra, deve ser consultada separadamente; esta matriz não antecipa resultados.


## 6. Addendum Task 5.4 — contrato de consumo e verificação (10/11/2026)

Este addendum não altera os resultados de testes históricos da 5.2. A implementação da 5.4 adiciona `OneiricReturnPolicyTest` e `OneiricReturnGuardTest` com decisões puras e checagens de prevenção de replay. O fluxo de 60 ticks permanece vanilla, usando `getUsedItemHand()`, `getUseItem()` e a referência exata do stack para vincular mão/item à tentativa. Retorno via `DreamReturnHelper.buildReturnTransition` passa a ser confirmado por `ServerPlayer.teleport` não nulo, vivo, mesmo UUID e Overworld. `ItemStack.shrink(1)` somente após confirmação, quando não há `instabuild`. `USE_COOLDOWN` é 3.0 segundos = 60 ticks padrão, aplicado só após sucesso.

**Status de prova:** código e testes unitários preparados; verificar build, JUnit e server smoke da CI do commit 5.4; **NÃO EXECUTADOS ainda** teste real com dois clientes, use item durante 60 ticks, interrupção 59 ticks, mudança de mão e falha de teleporte. Ver [Relatório de implementação 5.4](ONEIRIC_AWAKENER_TASK_5_4.md). Nenhuma modificação em `dream_return`, `dream_entry_enabled` e `dream_entry_chance`; worldgen/loot/recipes intactos.


## 7. Addendum Task 5.5 — morte não é retorno gratuito (10/11/2026)

| Caso | Regra de código | Status de teste |
| --- | --- | --- |
| Morte no Dreamscape/Campo Azul | `PlayerRespawnPositionEvent` substitui destino antes de `PlayerList.respawn`, mantendo dimensão original, fora de Hardcore/End | Código + testes unitários preparados; jogo real pendente |
| Morte Overworld/Nether/End/terceiros | Não interceptar, vanilla mantém cama/respawn | Inspeção de política; manual pendente |
| Âncora por viagem de sono | Attachment `dream_expedition_anchor` com `dimension/pos/yaw`, `copyOnDeath` e gravação somente após chegada segura | Codec/JUnit; restart/manual pendentes |
| Ponto inseguro / mundo antigo sem attachment | Validação geométrica compartilhada → busca local limitada → spawn global → plataforma 5×5 somente em emergência; erro crítico se impossível | Teste de políticas; colisões in-game pendentes |
| Despertador e retorno | Ao retornar ao Overworld após teleporte+consumo confirmado, limpar apenas âncora de expedição; `dream_return` preservado | Inspeção de código; manual pendente |
| Cama nas duas dimensões | `can_sleep=never`, `can_set_spawn=never`, `destroy_on_use=false`, `destroy_on_leave=false`; não interfere no RespawnConfig do Overworld | Static CI + datapack smoke a verificar; interação real pendente |
| `keepInventory`, Hardcore, multiplayer | Sem manipulação de inventário/XP; Hardcore vanilla, anchors por jogador | Testes de políticas; dois clientes pendentes |

Documentação detalhada: [Task 5.5](DREAM_RESPAWN_TASK_5_5.md). Ainda **não** declarar homologação Survival completa até 5.7.
