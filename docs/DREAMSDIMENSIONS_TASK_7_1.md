# Task 7.1 — Contrato de QA e Telemetria Reutilizável

**Projeto:** DreamsDimensions · Minecraft 26.3 / NeoForge 26.3 / Java 25.  
**Branch:** `migration/minecraft-26.3` · **sem merge na `main`**.  
**Status:** infraestrutura implementada; resultados de CI e homologação manual devem ser verificados por commit, sem presumir aprovação anterior.  
**Dependências:** [Task 7.0](DREAM_RECOVERY_SAFETY_TASK_7_0.md), [Task 5.7](DREAM_RUINS_TASK_5_7.md), [Task 5.3.1](SPAWN_AUDIT_TASK_5_3_1.md).

## 1. Escopo efetivamente entregue

Foi criada uma **camada de diagnóstico leve**; não um segundo motor de spawn, worldgen ou recuperação:

- `qa/DreamQaRecorder.java`: gravador central opt-in, registros limitados, contadores agregados e snapshots JSON versionados.
- `qa/DreamQaCommand.java`: status e help sempre registrados; operador ativa ou desativa a coleta com `/dreamqa enable` e `/dreamqa disable`.
- Instrumentação não decisória em `DreamMobPopulationService`, `DreamTeleportService` e `OneiricAwakenerItem`.
- `DreamQaRecorderTest`: testes JUnit de modo desligado, capacidade do buffer, descarte, contadores, exportação, isolamento e concorrência.
- `scripts/ci/check_qa_contract.py`: testes do contrato de relatório e regressões de integração.
- `scripts/ci/audit_qa_runtime.py`: verifica `/dreamqa status` e `/dreamqa snapshot` num dedicated server descartável.
- `scripts/ci/collect_qa_report.py`: agrega **evidências já existentes** de minérios e ruínas com snapshot runtime, sem converter testes não feitos em PASS.
- `.github/workflows/build.yml`: preserva os quatro seeds e auditorias anteriores, publica relatório por seed e consolida os quatro resultados.

Não se alteraram: IDs, balanceamento, frequência de estruturas, probabilidades de loot, lógica de retorno, regras de spawn ou configuração padrão de entrada no sonho.

## 2. Arquitetura e contrato

O recorder é Java puro e utilizável sem classes do Minecraft, de propósito: JUnit funciona de forma independente do motor.

```text
Minecraft / NeoForge events
  ├─ DreamMobPopulationService (spawn)
  ├─ DreamTeleportService (entrada)
  └─ OneiricAwakenerItem (retorno)
       │            (observação apenas)
       ▼
  DreamQaRecorder
    ├─ contadores agregados por categoria/subsistema/evento/status
    ├─ buffer circular limitado de eventos recentes
    └─ snapshot JSON sem identidade do jogador
       └─ /dreamqa snapshot → qa-reports/dreamqa-<run_id>.json

CI Python
  ├─ arquivo runtime de /dreamqa
  ├─ amostragem real de 256 chunks Overworld/seed (Task 5.6)
  └─ duas ruínas naturais / seed (Task 5.7/7.0)
       ▼
  qa-report-seed-<seed>.json
       ▼
  qa-summary.json (4 seeds)
```

Schema: `dreamsdimensions.qa.v1`. Cada evento registrado inclui UTC, run_id, categoria, subsistema, nome, dimensão, status, tipo de evidência e reason. `duration_ms` é opcional e **não é inventado**. Contadores acumulados continuam representando eventos observados mesmo que parte dos eventos recentes tenha sido descartada pelo buffer limitado.

Os estados aceitos são `PASS`, `FAIL`, `WARN`, `SKIPPED` e `NOT_RUN`. A agregação suporta evidências `STATIC_CHECK`, `UNIT_TEST`, `RUNTIME_OBSERVED`, `GAMETEST`, `MANUAL_VERIFIED`, `PERFORMANCE_MEASURED` e `MODEL_ESTIMATE`. Nenhuma categoria é elevada automaticamente para outra.

**Limitações importantes:**

- `/summon` não prova spawn natural. Os eventos de sucesso do coordenador são rotulados `COORDINATOR_NATURAL`; a geração vanilla nativa pode ocorrer por outro caminho e não é contabilizada por esse contador.
- `/locate` + inspeção do baú demonstram apenas o candidato natural e as propriedades locais observadas, nunca uma rota jogável em Survival.
- `/forceload` é responsabilidade do harness de CI, **não da telemetria**.
- Tempos RCON não são MSPT, FPS, TPS nem tempo real de um chunk.
- O snapshot poderá estar vazio em CI quando os caminhos instrumentados não forem exercitados por um jogador; isso não deve ser relatado como sucesso de spawn, sono ou retorno.

## 3. Segurança e custo

Por padrão `DreamQaRecorder.global().enabled()` é **false**; `/dreamqa status` e `/dreamqa help` ficam disponíveis mesmo sem coleta. A telemetria só registra eventos quando ativada por operador ou por JVM. Desabilitar limpa o buffer e os contadores. Quando ativado:

- Eventos são agregados em `LongAdder` e amostrados num buffer de 16 a 4096 eventos (padrão 512).
- Eventos antigos são substituídos ao atingir o limite; `dropped_events` informa quantos foram descartados.
- IDs do jogador, nomes, UUID e inventários **não** são armazenados na telemetria criada nesta task.
- Relatórios são gravados por comando explícito em um diretório configurado, nunca em cada tick.
- Exportação com falha não modifica o mundo nem bloqueia retorno do jogador.
- Comandos exigem nível de permissão administrativa (`Commands.LEVEL_ADMINS`).

Ainda são **pendentes**: benchmark de overhead, histograma/p50/p95 de duração do motor, integração com benchmarks de cliente e validação de comportamento real em multiplayer. Não há promessa de custo zero: medir em máquinas/servidores representativos antes de habilitar por períodos prolongados.

## 4. Configuração

Exemplo de desenvolvimento, **não usar em servidor público sem avaliar impacto e privacidade**:

```bash
JAVA_TOOL_OPTIONS="-Ddreamsdimensions.qa.enabled=true -Ddreamsdimensions.qa.runId=dev-local -Ddreamsdimensions.qa.maxBuffer=512" ./gradlew runServer
```

Windows PowerShell:

```powershell
$env:JAVA_TOOL_OPTIONS = "-Ddreamsdimensions.qa.enabled=true -Ddreamsdimensions.qa.runId=dev-local"
.\gradlew.bat runClient
```

Configurações efetivamente suportadas:

| System property | Default | Finalidade |
| --- | --- | --- |
| `dreamsdimensions.qa.enabled` | `false` | ativa a observação e comandos |
| `dreamsdimensions.qa.runId` | `runtime` | identifica execução; não inserir nome de jogador |
| `dreamsdimensions.qa.maxBuffer` | `512` | buffer limitado a 16..4096 |
| `dreamsdimensions.qa.reportDir` | `qa-reports` | local de saída de `/dreamqa snapshot` |

Propriedades de auditoria legadas continuam independentes: `worldgenMetrics`, `enableBridgeAudit`, `enableOreAudit`, `enableRuinsAudit` e `auditMobSpawn`.

O contrato é extensível; ainda não há coleta periódica automática, interface gráfica, painel online ou amostragem aleatória configurável.

## 5. Comandos administrativos

Com a versão atualizada do mod, `/dreamqa status` e `/dreamqa help` funcionam sem configurar flags da JVM. Para coletar:

```mcfunction
/dreamqa status
/dreamqa enable
/dreamqa snapshot
/dreamqa reset
/dreamqa disable
```

`enable`, `disable`, `snapshot` e `reset` exigem operador (ou cheats ativados no mundo Singleplayer); `status` e `help` são somente leitura. Após `disable`, os eventos são limpos e novos eventos deixam de ser registrados. `snapshot` só funciona quando a coleta está ativa. Desligar o servidor integrado restaura a opção inicial definida pela JVM.

Se `/dreamqa status` ainda aparecer como comando desconhecido, atualizar o JAR carregado e reiniciar o cliente ou servidor; não basta dar `/reload` no mundo. Para testar comandos de operador no mundo local, habilitar cheats nas configurações do mundo ou temporariamente por **Abrir para LAN > Permitir Cheats**.

## 6. Homologação manual — mundo de teste

**Usar um mundo novo ou descartável.** Os teletransportes abaixo são coordenadas exemplificativas, **somente em Spectator**, não pontos Survival validados:

```mcfunction
/gamemode spectator
/execute in dreamsdimensions:dreamscape run tp @s 0 180 0
/execute in dreamsdimensions:campo_onirico_azul run tp @s 0 180 0
/execute in minecraft:overworld run tp @s 0 120 0
```

**Mobs, estruturas e equipamento:**

```mcfunction
/execute in dreamsdimensions:dreamscape run summon dreamsdimensions:fragmento_errante 0 220 0 {Tags:["dd_qa_manual"]}
/execute in dreamsdimensions:dreamscape run summon dreamsdimensions:vigia_adormecido 5 220 0 {Tags:["dd_qa_manual"]}
/execute in dreamsdimensions:dreamscape run locate structure dreamsdimensions:fragmented_dream_ruins
/execute in dreamsdimensions:campo_onirico_azul run locate structure dreamsdimensions:blue_dream_ruins
/give @s dreamsdimensions:oneiric_awakener 1
/give @s dreamsdimensions:dream_necklace 1
/execute in dreamsdimensions:dreamscape run kill @e[type=dreamsdimensions:fragmento_errante,tag=dd_qa_manual]
/execute in dreamsdimensions:dreamscape run kill @e[type=dreamsdimensions:vigia_adormecido,tag=dd_qa_manual]
```

Os `/summon` são para inspecionar entidade e animações, **não** para testar spawn natural; os contadores do coordenador não devem aumentar por esse comando. `/kill` atinge apenas entidades marcadas manualmente com `dd_qa_manual` (não é a rotina de gameplay normal).

Com telemetria ativada, testar spawn natural perto de ilhas em Survival, entrada e retorno reais e então:

```mcfunction
/dreamqa status
/dreamqa snapshot
```

Em servidor/console RCON, usar os mesmos comandos sem `/` inicial quando necessário; `@s` não existe como jogador quando executado do console.

## 7. Matriz de qualidade e critérios de evidência

| Prioridade | Área | Evidência disponível | Ainda não comprovado |
| --- | --- | --- | --- |
| P0 | Recuperação/retorno | Políticas JUnit, instrumentação de eventos | percurso Survival, inventário, consumo após teleport, jogador real e multiplayer |
| P0 | Persistência/respawn | testes existentes de política, docs 5.5 e 7.0 | restart/saves antigos com dois usuários |
| P1 | Worldgen | sondas RCON, quatro seeds, geometria real limitada | continuidade de todos os chunks, diversidade visual e performance |
| P1 | Estruturas | localizações nativas, baú real, loot table pendente e suporte local | acessibilidade por Survival e conteúdo ao abrir |
| P2 | Minérios | varredura 256 chunks/seed | drops Fortune/Silk Touch em jogo |
| P2 | Mobs | perfis, eventos do coordenador, teste de invocação | taxa de spawn natural, animação, navegação e equilíbrio em Survival |
| P3 | Cliente | assets e checks estáticos | rendering real, FPS e experiência visual |

Passar CI **não desbloqueia** `dream_entry_enabled`; o portão de segurança da Task 7.0 permanece fechado até homologação humana.

## 8. Executar QA no CI/local

```bash
./gradlew --no-daemon clean build
python3 scripts/ci/check_qa_contract.py
```

O workflow realiza também todos os testes antigos. Para cada seed são exigidos:

- Relatório real de minérios `ore-audit-seed-<seed>.json`.
- Relatório real de ruínas `dream-ruins-seed-<seed>.json`.
- Snapshot `qa-reports/dreamqa-ci-seed-<seed>.json`.

Produz `qa-report-seed-<seed>.json`. O job `qa-summary` usa os quatro relatórios e falha se qualquer seed estiver faltando. Todos os arquivos permanecem disponíveis como artifacts do GitHub Actions; logs de servidor anteriores são preservados.

Para uma mecânica nova, adicionar um `DreamQaRecorder.global().record(...)` **somente nos pontos de decisão reais**, nunca em loops por tick. Usar nome estável, motivo sem UUID, categoria e dimension ID. Adicionar uma verificação automatizada apropriada e classificar com a evidência que foi realmente executada.

## 9. Pendências para fechar toda a homologação

1. Testar comandos com operador no cliente/servidor, incluindo exportação e reset.
2. Testar gameplay de sono, retorno e consumo em Survival.
3. Medir taxa de spawn natural de Fragmento/Vigia em sessões representativas.
4. Repetir teste de persistência após salvar/reiniciar com duas contas.
5. Validar overhead de QA habilitado/desabilitado; sem assumir valores de MSPT.
6. Só então avançar o portão de release da Task 7.0.

**Regra permanente:** `NOT_RUN` não pode se transformar em `PASS` sem execução e evidência correspondente.
