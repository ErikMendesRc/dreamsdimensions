# TASK 5.2 — Contrato de viagem, retorno e sobrevivência onírica

**Data:** 2026-11-10 · **Branch:** `migration/minecraft-26.3` · **HEAD auditado:** `c5e5c5ba63cab73e3de9e1ff1227992a4c3b52ed` · **Tipo:** documentação/auditoria, sem alteração em runtime.
**Stack comprovada:** Minecraft 26.3; NeoForge 26.3.0.101; Java 25; ModDevGradle 2.0.148; GeckoLib 5.5.7. Fonte: `gradle.properties`/`build.gradle`.

> **Precedência:** os requisitos fechados nesta Task 5.2 têm prioridade nas **novas implementações de viagem/retorno** sobre propostas históricas da Task 5.0 e da nota `PROGRESSION_OVERWORLD_FIRST.md`. Não reinterpretar seus relatórios como resultados novos. Termos: **RC** requisito confirmado pelo produto, **GD** recomendação de game design, **TP** decisão técnica proposta, **PV** pendência de validação, **IF** implementação futura, **C** evidência de código lido; **não** significa teste in-game.

## 1. Ciclo de Survival e invariantes

**RC:** Overworld é o mundo-base. Minerar `dreamsdimensions:dream_ore`/`deepslate_dream_ore` para `dream_dust` no Overworld, preparar Colar e Despertador, dormir voluntariamente, explorar Dreamscape ou Campo Onírico Azul, obter materiais, usar o Despertador **consumível** para retornar, evoluir equipamentos no Overworld. Pó de Cristal futuro ≠ Dream Dust. Exploração por curiosidade, risco, necessidade e recompensa; sem missões artificiais.

**RC:** Sem Colar **equipado** não há chance de viagem e o sono no Overworld deve permanecer vanilla. Colar na mochila/offhand não vale. Com Colar, **uma única tentativa de sorteio por ciclo de sono completo**; servidor decide; sorteio negativo deixa o sono vanilla terminar. Nenhum teleporte inseguro ou para dimensão inexistente.

**RC:** Despertador `dreamsdimensions:oneiric_awakener`, duração de uso 60 ticks, ativação própria (não requer cama), exclusivamente nos sonhos configurados. Exatamente 1 consumido **após** retorno confirmado em Survival/Adventure; falha/interrupção não consome; operação não reentrante. Sem item não existe saída gratuita por cama; a via planejada é baú raro em estrutura real em **ambas** as dimensões. Não forçar missões. Gamerules e Hardcore devem prevalecer.

**Bloqueio de lançamento:** O conjunto de regras introduz aprisionamento **intencional**, mas sem loot de recuperação implementado esse risco se torna **softlock acidental**. Não habilitar o Colar para Survival de produção até a 5.7 e a política de respawn da 5.5 estarem implementadas e medidas. O desenvolvedor poderá testar com /give, mas isso NÃO homologa o ciclo Survival.

## 2. Inventário do código real (C)

| Classe / recurso auditado | Evidência de implementação na HEAD | Diferença / risco | Dono |
| --- | --- | --- | --- |
| `DreamDimensionCatalog` | Dois perfis fixos: dreamscape (peso 1, raio 12, serene_stone) e campo_onirico_azul (peso 1, raio 24, blue_dream_stone); sem registro indevido de dimensão | Nenhum equipamento/chance global | 5.3 |
| `DreamDimensionDefinition` | Record imutável com `sleepDestination`, `selectionWeight`, piso, políticas visuais | Peso de destino NÃO é chance de entrar | 5.3 |
| `WeightedDimensionPicker` | Sorteio ponderado **sem reposição** e ordem de alternativas | Retry após ausência de piso, não após teleporte ambíguo | manter |
| `DreamsConfig` | `dream_dimensions`, Set de IDs e lista ordenada de perfis habilitados; ignora perfil desconhecido | Nenhuma probabilidade de entrada; configuração local | 5.3 |
| `SleepTeleportHandler` | `PlayerTickEvent.Post` exige 100 ticks e Overworld; `PlayerWakeUpEvent` enfileira conclusão; Sets/Map por UUID; limpa logout | **P0:** inicia viagem SEM colar e SEM chance; ausência de verificação de ciclo/colisão entre eventos requer teste | 5.3 |
| `DreamTeleportService` | Filtra níveis carregados; enumera pesos; exige chegada; para após teleporte incerto; usa resultado `ServerPlayer.teleport(...)` antes de gravar origem | Depende do sleep handler; não confundir commit de origem com spawn do sonho | manter/5.3 |
| `DreamSpawnHelper` | Procura piso firme, ar, fluidos/colisão/borda; fallback plataforma 5×5 no ar com limite | Fallback pode mudar cenário; falha parcial de colocação e custo de chunk precisam teste | 5.5/regressão |
| `DreamReturnHelper` | Resolve cama Overworld gravada → respawn pessoal válido Overworld → spawn global seguro; devolve `Optional<TeleportTransition>` | Prioridade e spawn podem divergir quando respawn é redefinido nas dimensões; não confundir construção com execução | 5.4/5.5 |
| `DreamReturnData` | `MapCodec` com campos opcionais `dimension`, `pos`, `yaw`; último registro de cama do Overworld | Mutável; preservar serialização | manter |
| `ModAttachments` | Registra `dream_return` com `.serialize(...).copyOnDeath()` | **Não** usar para âncora de respawn onírica | 5.5 |
| `DreamReturnAttachmentHandler` | Em `PlayerSetSpawnEvent`, grava posição/yaw ao definir spawn no Overworld | Não é confirmação de cama íntegra; fallback já valida cama real | 5.5 |
| `OneiricAwakenerItem` | `use` só na dimensão do mod, 60 ticks, cooldown 60; `finishUsingItem` retorna `pStack` inalterado | **P0:** item NÃO é consumido. `serverPlayer.teleport(transition)` não tem sucesso verificado antes da mensagem/cooldown | 5.4 |
| `DimensionEffectPolicy` / `DreamDimensionEffectsHandler` | Slow Falling com propriedade conservadora no Campo Azul, `NONE` no Dreamscape; limpa logout | Não acoplar respawn, impedir efeito preso após mudança | regressão |
| `CommonEvents` / `DreamsDimensions` | Listeners registrados uma vez no `NeoForge.EVENT_BUS`; registros no mod bus | 5.3/5.5 devem centralizar inscrição, evitar dupla execução | 5.3/5.5 |
| `ModItems` / `ModBlocks` | `oneiric_awakener` stack máximo 1; `dream_dust`, dois minérios, núcleo, pedras registradas; nenhum colar | Não existe slot de acessório/colar; preservar IDs | 5.3 |
| `dimension/*.json` | Dois mundos `minecraft:noise`, bioma fixo e tipos próprios | Não mudar IDs, worldgen ou biomas | manter |
| `dimension_type/*.json` | Ambos `bed_rule.can_sleep=when_dark`, `can_set_spawn=always`, sem explosão indicada | **P0:** sono atualmente permitido em condições de escuridão; criar política explícita em 5.5 | 5.5 |
| `recipe/oneiric_awakener.json` | Pena + núcleo onírico + frasco; cadeia total do núcleo consome Dream Dust | Receita não prova obtenção em Survival; dificuldade atual é requisito | 5.6 |
| Loot tables | Há tabelas de drops de blocos; **não** há baús de recuperação/estruturas da 5.7 | Softlock ao entrar sem item até estruturar recuperação | 5.7 |
| `GAMEPLAY_EXPLORATION_TASK_5_0.md`, `GAMEPLAY_CONTENT_MATRIX.md`, `GAMEPLAY_ROADMAP.md`, `PROGRESSION_OVERWORLD_FIRST.md` | Documentam base e várias hipóteses anteriores | Referências a “Despertador reutilizável” e IDs 5.2–5.7 antigos conflitavam; esta task os complementa sem apagar histórico | 5.2 |

Classificações: **IMPLEMENTADO (código):** seleção, teleporte, spawn e attachment; **IMPLEMENTADO COM DEFEITO:** custo do Despertador e confirmação de retorno, entrada irrestrita sob novas regras; **PENDENTE DE VALIDAÇÃO:** sono real, morte, spawn, reconexão, collision, playtest; **NÃO IMPLEMENTADO:** colar, probabilidade, âncora, baús e recuperação; **PROPOSTA FUTURA:** políticas abaixo.

## 3. Decisões D-01 a D-08: opções, escolha e trade-offs

### D-01 — Cama nos sonhos
- **Alternativas:** A impedir sono; B dormir sem retorno; C definir spawn sem dormir. B cria semântica confusa; A pura pode eliminar pontos de respawn de jogadores; C aproveita política de cama 26.3.
- **GD/TP escolhida:** `can_sleep=never`, `can_set_spawn=always` nos dimension types **somente em 5.5**, sem explosão, para permitir marcação de spawn sem dormir **se a API 26.3 confirmar o fluxo vanilla real**. Cama jamais executa retorno; Despertador é a única saída voluntária. Se o fluxo de respawn não for confiável, substituir marcação de spawn por âncora própria após teste, não ativar cama que devolva ao Overworld. **Survival:** pode construir base, não pular tempo. **Multiplayer:** política por dimensão, nunca sleep vote global para sair. **PV:** testar comportamento de set spawn em `can_sleep=never`, cama perdida/obstruída e mecânica real de respawn. **Task:** 5.5.

### D-02 — Morte nos sonhos
- **Alternativas:** A morte retorna ao Overworld (suicídio vira saída grátis); B respawn na dimensão atual (risco de morte em loop); C âncora de expedição (mais estado).
- **GD:** B com âncora segura C como fallback, sem saída gratuita. **TP:** antes da primeira viagem persistir âncora segura do nível/expedição; quando morrer, respawn local na cama onírica válida se admitida pela regra, senão na âncora validada, senão criar/achar ponto seguro no MESMO nível. **Último recurso excepcional:** dimensão indisponível/corrompida após restart → fallback ao spawn seguro vanilla, com log auditável; isso não pode ser ação repetível de gameplay. **Survival:** drops/XP vanilla, `keepInventory` respeitado; morte não fabrica Despertador. **Multiplayer:** âncora por UUID; nunca teletransportar companheiros. **Hardcore:** morte termina jogabilidade normal; não forçar respawn/cancelar morte. **PV:** validar momento correto de selecionar destino em 26.3; `PlayerRespawnEvent` sozinho pode ocorrer depois do roteamento do respawn. **Task:** 5.5.

### D-03 — Âncora de expedição
- **Alternativas:** spawn natural (pode ser vazio), última chegada (mais estável), attachment (persiste), interceptar respawn sem dado (insuficiente).
- **TP:** Attachment próprio `dream_expedition_anchor` (ID proposto, a confirmar em 5.5), serializado e copiado em morte, com `dimension`, `BlockPos`, `yaw`, `validity/version` mínima; gravar **após** `teleport()` confirmado e chegada segura. Em login/respawn validar nível carregado, borda, altura, piso, ar e colisão; não garantir que estrutura não foi removida. Atualizar ao iniciar **nova** expedição, não em cada tick; limpar somente após retorno confirmado ao Overworld ou desativação segura do fluxo, não em morte. **Survival:** ponto de sobrevivência, não ferramenta de retorno. **Multiplayer:** isolado por UUID. **Task:** 5.5. `dream_return` não é alterado.

### D-04 — Equipamento do Colar
- **Alternativas:** slot Curios; slot vanilla com item custom (equipamentos limitados); data components (dados do stack, NÃO comprovam equipar); UI inventário própria (custo alto).
- **GD/TP:** adotar **Curios 26.3** com slot `necklace` ou identificador validado pelo ecossistema; implementar adaptador pequeno `DreamNecklaceEquipmentPolicy` (nome conceitual) que pergunta ao servidor se o item está em slot efetivo. Curios **tem distribuição 26.3**, mas a compatibilidade da versão escolhida, coordenadas de dependência e assinaturas reais do SDK precisam ser confirmadas antes de codar. Não adicionar biblioteca na 5.2; decidir dependência obrigatória vs opcional na 5.3 (para o primeiro MVP, preferir obrigatória para evitar colar sem slot). **Survival:** feedback de slot equipado. **Multiplayer:** leitura server-side, não confiar em client packet ou tooltip. **Task:** 5.3.

### D-05 — Chance de entrada
- **Alternativas:** chance por tick (exploit); chance por despertar (duplicação); chance **uma vez por sono válido completo**.
- **GD/TP:** terceira. Tentativa autorizada no servidor ao concluir 100 ticks, com prova de início/continuidade do ciclo e colar no slot na confirmação; usar gate por UUID/ciclo e desarmar no wake/logout/morte/dimension change. Wake com conclusão deve ser processado UMA vez, não somado ao tick. Novo sono completo **é outro ciclo**, mesmo na mesma noite; interromper antes de 100 ticks não rola. Sorteio 30% é **hipótese de teste**, NÃO default confirmado. Só escolher destino se o sorteio de viagem passar; pesos 1:1 atuais continuam independentes. Se destino falhar, não rolar nova chance no mesmo ciclo; permitir fallback entre candidatos habilitados apenas para chegada segura. Se falhar após início de teleport ambíguo, parar. **Survival:** preserva sono normal em falha. **Multiplayer:** contador individual, respeitar dormir em grupo e hora alterada; preferir UUID + ciclo pequeno transitório, salvo apenas se logout exploit for confirmado. **PV:** instrumentar cenários wake/tick e reinício para não farmar tentativas com relog. **Task:** 5.3.

### D-06 — Recuperação
- **Alternativas:** saída gratuita (nega o risco), crafting trivial em sonho (enfraquece economia Overworld), Despertador em loot de ruínas raras.
- **GD/TP:** exclusivamente loot raro com chance menor que 100% por baú, em estruturas acessíveis nas **duas dimensões**, com JSON loot tables/structure sets e geração persistente vanilla; só primeiro saque normal de cada inventário (sem roll por reabrir). Não definir pesos/frequência sem métricas: medir distância média, P90/P99, risco de terreno, taxa de sucesso com equipamento mínimo e caminhos para reconstruir ferramentas básicas. **Survival:** possibilidade mensurável de sair, não garantia imediata; testar jogador que perdeu item e itens essenciais. **Multiplayer:** loot disputado ou instanciado é decisão futura; evitar inflação e duplicação por chunk reload. **PV:** antes de habilitar novas viagens, provar rota real de recuperação em novo mundo nas duas dimensões. **Task:** 5.7.

### D-07 — Despertador em multiplayer
- **Alternativas:** consumir ao iniciar (pune cancelamento), consumir ao fim antes de confirmar (perda), consumir **após** resultado confirmado.
- **TP:** concluir 60 ticks com servidor autoritativo; travar retorno por UUID até resolução; construir `TeleportTransition`, chamar teleport e verificar `ServerPlayer` retornado, identidade/dimensão Overworld antes de comprometer **um** stack unitário; se `null`, dimensão errada ou exceção, não consumir nem anunciar sucesso. Checar que mão/stack são os originais após a transição, inclusive offhand, e impedir reuso do mesmo evento; se houver troca de referência, capturar token do uso e resolver estado no jogador retornado, sem remover item estranho. `stacksTo(1)` atual simplifica contagem, mas não prova idempotência. Respeitar Creative/instabuild vanilla em testes próprios. **Survival:** custo exato; **multiplayer:** transações independentes por UUID. **PV:** verificar callbacks e troca de entidade em 26.3; morte, logout, duas mãos e falha. **Task:** 5.4.

### D-08 — Saves existentes
- **Alternativas:** reescrever IDs/codec (quebra save), preservar formato e adicionar dados novos sem migração, migrar explicitamente só em necessidade demonstrada.
- **TP:** conservar `dreamsdimensions:dreamscape`, `campo_onirico_azul`, `oneiric_awakener`, `dream_dust`, `dream_return` e seus campos `dimension/pos/yaw`; preservar pesos e `dream_dimensions`; nenhum rename, mudança de worldgen, itens, loot ou receitas na 5.2. Novo attachment precisa defaults seguros e tolerar save antigo ausente. `can_sleep=never` altera comportamento em chunks existentes **apenas quando 5.5 for instalada**, exigindo release note e teste. **Survival:** preservar mundo, **multiplayer:** persistência por UUID no servidor. **Tasks:** 5.3–5.7.

## 4. Contrato de estados e limites de responsabilidade

```text
OVERWORLD_IDLE --dormir com colar equipado--> SLEEP_ELIGIBLE
OVERWORLD_IDLE --sem colar/offhand/inventário--> VANILLA_SLEEP --> OVERWORLD_IDLE
SLEEP_ELIGIBLE --interrompido/colar retirado/logout/morte--> OVERWORLD_IDLE
SLEEP_ELIGIBLE --100 ticks, uma vez--> DREAM_ROLL
DREAM_ROLL --falhou chance/destino inexistente/chegada insegura--> VANILLA_WAKE/IDLE
DREAM_ROLL --passou + destino habilitado seguro--> DREAM_ENTERING
DREAM_ENTERING --teleport confirmado + âncora gravada--> DREAM_EXPLORING
DREAM_ENTERING --falha ambígua--> RECOVERY_LOG + nenhum retry de teleport
DREAM_EXPLORING --sem item--> EXPLORE_RECOVERY_CHESTS --> DREAM_EXPLORING
DREAM_EXPLORING --morte normal--> DREAM_RESPAWNING --> DREAM_EXPLORING
DREAM_EXPLORING --Hardcore morto--> VANILLA_HARDCORE
DREAM_EXPLORING --Despertador iniciado--> AWAKENER_ACTIVATING
AWAKENER_ACTIVATING --cancelado/morte/logout--> DREAM_EXPLORING ou fluxo vanilla, sem consumo
AWAKENER_ACTIVATING --60 ticks--> RETURNING
RETURNING --teleport confirmado + consumo único--> OVERWORLD_IDLE
RETURNING --falha/sem local seguro--> DREAM_EXPLORING, sem consumo
DREAM_EXPLORING --restart/login--> DREAM_EXPLORING (âncora validada)
DREAM_EXPLORING --/execute ou comando admin--> ADMIN_TRANSITION (não contar como despertar)
```

**Conceituais, NÃO novos estados persistidos automaticamente:** (a) `SleepTeleportHandler` orquestra elegibilidade/ciclo; (b) `DreamDimensionCatalog`/`Definition`/`Picker`/`DreamsConfig` resolvem destino; (c) `DreamTeleportService`/`DreamSpawnHelper` resolvem chegada e commit; (d) `DreamReturnHelper`/`DreamReturnData`/`OneiricAwakenerItem` tratam retorno; (e) serviço 5.5 trata morte/âncora; (f) política 5.3 verifica Curios. **Jamais** reutilizar `dream_return` como âncora. Logs e mensagens distinguem `not_eligible`, `roll_failed`, `no_target`, `unsafe_spawn`, `teleport_failed`, `returned`, `consumption_confirmed`.

## 5. Segurança, multiplayer, compatibilidade e critérios de liberação

1. Nunca tomar resultado positivo de `build` como prova de gameplay. Avaliar `ServerPlayer.teleport` em cada fluxo; confirmar novo jogador/dimensão antes de mutar attachment/stack.
2. Respawn local precisa evitar loop de spawn sufocado/queda: posição validada com colisão, piso, altura e world border, atualização sem teleport gratuito e fallback seguro no mesmo mundo; se a dimensão deixar de existir, falha extraordinária com observabilidade.
3. Logout/restart: não persistir objetos `ServerPlayer` nos maps do sono; limpar locks de uso e de sono; salvar attachment de âncora em serialização própria; preservar `dream_return.copyOnDeath`.
4. Multiplayer: isolamento por UUID, duas mãos e duas ativações simultâneas, /gamerule keepInventory, PvP, sono de equipe, latência, reconexão e death screen.
5. O produto só pode anunciar “jogador preso até explorar” quando **existirem** de fato estruturas com Despertadores e possibilidade prática de acesso. Sem isso, novas entradas sem Despertador estão **bloqueadas para release**.
6. Chegadas via admin ou outros mods não criam automaticamente a mesma garantia de retorno/âncora que uma entrada por sono; a 5.5 precisa fallback defensivo sem bloquear comandos administrativos.

## 6. API 26.3 pesquisada, com evidência e ressalvas

- `CanPlayerSleepEvent` no branch oficial `26.3.x`: server-only, **não cancelável**, `getProblem()/setProblem(@Nullable BedSleepingProblem)`, `getVanillaProblem()`, `getPos()`. Regressão reportada na 26.3.0.13-beta, corrigida na **26.3.0.30-beta**; versão instalada 26.3.0.101 é posterior. Ainda **PV** executar em jogo. Fontes: https://github.com/neoforged/NeoForge/issues/3541 ; https://github.com/neoforged/NeoForge/blob/26.3.x/src/main/java/net/neoforged/neoforge/event/entity/player/CanPlayerSleepEvent.java
- `PlayerWakeUpEvent` é **observacional** (`wakeImmediately`, `updateLevel`), sem controle de cancelamento. `CanContinueSleepingEvent` (server-only) pode alterar continuidade. Fontes: https://github.com/neoforged/NeoForge/blob/26.3.x/src/main/java/net/neoforged/neoforge/event/entity/player/PlayerWakeUpEvent.java ; https://github.com/neoforged/NeoForge/blob/26.3.x/src/main/java/net/neoforged/neoforge/event/entity/player/CanContinueSleepingEvent.java
- `PlayerSetSpawnEvent` é cancelável e tem `getNewSpawn()`, `getSpawnLevel()`; null significa reset. `PlayerEvent.Clone`: `getOriginal()`, `isWasDeath()`; `PlayerRespawnEvent`: `isEndConquered()`; `PlayerChangedDimensionEvent`: `getFrom()/getTo()`. **PV** ordem de eventos com `ServerPlayer.RespawnConfig` e `TeleportTransition` deve ser auditada no SDK antes do código de 5.5. Fontes: https://github.com/neoforged/NeoForge/blob/26.3.x/src/main/java/net/neoforged/neoforge/event/entity/player/PlayerSetSpawnEvent.java ; https://github.com/neoforged/NeoForge/blob/26.3.x/src/main/java/net/neoforged/neoforge/event/entity/player/PlayerEvent.java
- Attachments: `serialize`, `copyOnDeath` e `PlayerEvent.Clone` documentados oficialmente; documentação disponível no portal está rotulada **26.1** (referência conceitual), portanto assinatura 26.3 precisa ser confirmada no SDK, embora o mod atual já use `copyOnDeath`. https://docs.neoforged.net/docs/datastorage/attachments/
- GameTests: página oficial atualmente rotulada **26.1**; `build.gradle` já contém `gameTestServer`, mas nenhuma execução desta task comprova `runGameTestServer` em 26.3. Validar registry/data-driven/ambiente em 5.3–5.5 antes de adotar. https://docs.neoforged.net/docs/misc/gametest/
- Migração: https://docs.neoforged.net/primer/docs/26.3/ . Bed rules como `can_sleep=never` e `can_set_spawn=always` seguem atributos 26.3; validar interações com cama no SDK e em mundo: https://minecraft.wiki/w/Environment_attribute .
- Curios 26.3: publicação NeoForge 17.0.1 para 26.3 listada em novembro de 2026; distribuição NÃO equivale a integração já testada: https://www.curseforge.com/minecraft/mc-mods/curios .

## 7. Dependências, testes e evidências

| Task | Mudança futura | Bloqueio de aceite |
| --- | --- | --- |
| **5.3 Colar** | Registro, receita, Curios slot, política de equipamento, chance una por sono e regressão de sono | Não disponibilizar a versão para Survival de produção até 5.5/5.7 |
| **5.4 Despertador** | Consumo unitário pós-teleporte confirmado, proteção idempotente e duas mãos | 0 perdas em falha; 1 gasto em sucesso por jogador |
| **5.5 Respawn** | `bed_rule`, âncora separada, políticas de morte/Hardcore/keepInventory, restore | Morte sem saída gratuita; nenhuma posição letal repetida |
| **5.6 Economia** | Minerar Dust naturalmente no Overworld, receitas e custos medidos | Obtenção em Survival antes de considerar item disponível |
| **5.7 Ruínas** | Estruturas raras em ambas, tabelas JSON, baús sem reroll | Com pouco equipamento, recuperação **real** e viável documentada |

**Execução nesta 5.2:** leitura da branch, documentação oficial e inventário estático **realizados**. **Não executados:** Java build, unit tests, data validation automatizada, server smoke, GameTest, Survival manual ou multiplayer. Não houve alteração de código. A matriz operacional e comandos ficam em [TRAVEL_RETURN_TEST_MATRIX_TASK_5_2.md](TRAVEL_RETURN_TEST_MATRIX_TASK_5_2.md).
