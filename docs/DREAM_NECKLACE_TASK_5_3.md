# TASK 5.3 — Colar dos Sonhos e elegibilidade de viagem

**Data:** 2026-11-10 · **Branch:** `migration/minecraft-26.3` · **Baseline inicial:** `35c446bbc357322627b7448651833eed17cbc44c` (contrato 5.2) · **Tipo:** implementação limitada e protegida para laboratório.

> **IMPLEMENTADO, MAS BLOQUEADO PARA RELEASE SURVIVAL ATÉ TASKS 5.4/5.5/5.7.** O administrador pode habilitar a entrada em mundo de teste descartável e assumir os riscos. `dream_entry_enabled=false` é o padrão. Testes de CI/build e playtest em jogo são evidências diferentes; este relatório não inventa resultados.

## 1. Objetivo e gameplay
O Overworld continua origem. O item `dreamsdimensions:dream_necklace` é um acessório de stack 1, sem durabilidade ou consumo. Estar **no slot funcional necklace de Curios e ativo** habilita apenas uma chance de viagem após sono completo de 100 ticks no Overworld. O colar **não** dá buffs, teleporte manual, recursos nem retorno. Inventário, hotbar, mãos e cosméticos **não** contam.

## 2. Stack e pesquisa
- Minecraft **26.3**, NeoForge **26.3.0.101**, Java **25**, ModDevGradle **2.0.148**, GeckoLib **5.5.7** (preservados).
- Curios **17.0.1+26.3** (release oficial de 09/11/2026), dependência **obrigatória de ambos os lados** `curios` no `neoforge.mods.toml`, range `[17.0.1,18)`.
- Gradle: `compileOnly "...:api"` + `localRuntime "..."`, Maven oficial `https://maven.theillusivec4.top/`. Evita bundlar biblioteca em nosso JAR, e fornece o mod inteiro para ambiente de desenvolvimento (cliente e dedicated server).
- APIs observadas em código Curios `26.x`: `CuriosApi.getCuriosInventory`, `getStacksHandler("necklace")`, `getStacks().getStackInSlot()`, `isSlotActive`; Curios 17.0.1 necessita **verificação por build**. `necklace` é preset oficial, sem inventário custom.
- Docs: https://docs.illusivesoulworks.com/curios/getting-started ; https://docs.illusivesoulworks.com/curios/slots/slot-register ; https://docs.illusivesoulworks.com/curios/slots/entity-register ; https://github.com/TheIllusiveC4/Curios/tree/26.x ; https://docs.neoforged.net/primer/docs/26.3/ ; https://www.curseforge.com/minecraft/mc-mods/curios/files/9521516 .

## 3. Arquitetura e arquivos

| Arquivo | Alteração |
| --- | --- |
| `gradle.properties`, `build.gradle`, `src/main/templates/META-INF/neoforge.mods.toml` | Dependência Curios 17.0.1 e verificação obrigatória de mod |
| `DreamsDimensions.java`, `config/DreamEntryConfig.java` | Configuração SERVER separada; listeners de clone/mudança de dimensão |
| `ModItems.java`, `ModCreativeTabs.java` | Item simples, LORE por Data Component, aba criativa |
| `service/DreamNecklaceEquipmentPolicy.java` | Única fronteira Curios, servidor, apenas slots necklace funcionais ativos |
| `service/DreamEntryPolicy.java` | Predicados de elegibilidade e sorteio puro `DoubleSupplier`, testável |
| `service/DreamSleepCycle.java` | Gate transitório por UUID e ciclo; nenhum jogador/nível retido |
| `SleepTeleportHandler.java` | Integrado ao fluxo tick/wake anterior, sem segundo motor; chance 1 vez |
| `assets/dreamsdimensions/items/dream_necklace.json`, `models/item/dream_necklace.json` | Modelo vanilla 2D de inventário |
| `assets/dreamsdimensions/textures/item/dream_necklace.png` | Pixel art 32×32 RGBA, cobre + cristal violeta/ciano |
| `assets/dreamsdimensions/lang/pt_br.json`, `en_us.json` | Nome e três linhas de tooltip em ambos os idiomas |
| `data/dreamsdimensions/recipe/dream_necklace.json` | Receita Survival JSON |
| `data/dreamsdimensions/curios/entities/dream_necklace.json` | Atribuição do slot preset necklace aos players |
| `data/curios/tags/item/necklace.json` | Tag do único item equípavel na categoria |
| `DreamEntryPolicyTest.java`, `DreamSleepCycleTest.java` | JUnit de elegibilidade, RNG e idempotência de ciclo |
| `docs/GAMEPLAY_E_DIRECAO.md`, `docs/ESTADO_ATUAL.md` | Conteúdo atual consolidado; histórico via Git |

**Intocado:** `DreamTeleportService`, `DreamSpawnHelper`, `WeightedDimensionPicker`, `DreamReturnData`, `DreamReturnHelper`, `OneiricAwakenerItem`, `dimension_type`, `worldgen`, mobs, receitas antigas, assets antigos e IDs existentes.

## 4. Como equipar e fabricar
1. **Fabricar** em crafting 3×3 com `C D C / D A D / _ C _`, sendo **C** cobre, **D** dream_dust do Overworld, **A** fragmento de ametista. Custo: 3 pós, 3 cobres, 1 ametista. Balanceamento provisório, depende de minério natural comprovado.
2. **Equipar**: abrir inventário Curios (tecla padrão **G** se não remapeada, ou botão Curios do inventário); colocar Colar no slot **necklace** funcional, não no cosmético. Curios mescla IDs de slot e o preset nativo tem tamanho 1; datapack associa um necklace ao jogador, sem duplicação manual de slots.
3. **Dormir** no Overworld. Sem colar funcional, sono vanilla. Com colar, configuração habilitada e dimensões carregadas, sorteio por ciclo completo; sucesso encaminhado ao serviço existente de teleporte e busca segura.

## 5. Configurações de servidor e teste em mundo descartável
`DreamEntryConfig` é um `ModConfig.Type.SYNCED`, distinto do antigo `DreamsConfig` LOCAL. O tipo correto em NeoForge 26.3.0.101 é `SYNCED` (o antigo `SERVER` não existe mais). No servidor, edite `config/dreamsdimensions-synced.toml`, ou use a substituição por mundo em `<mundo>/syncedconfig/dreamsdimensions-synced.toml`. Reinicie o servidor para carregar os valores e confirme o caminho real nos logs:

```toml
dream_entry_enabled = true
dream_entry_chance = 1.0
```

Para comprovar falha: `dream_entry_chance = 0.0`. Para ensaio estatístico: `0.30`. Faixa `[0.0,1.0]`, valor default **0.30** ainda não homologado. Não alterar pesos `selectionWeight=1` de cada dimensão para simular chance de entrada. **Voltar a false ao terminar testes.**

## 6. Semântica do sono e defesa contra duplicação
`DreamSleepCycle` mantém UUID em `activeSleep` e `attemptedSleep`. Só marca a tentativa com 100 ticks e único `attemptedSleep.add(uuid)`; `onWake` compartilha o mesmo gate, enfileira posição apenas uma vez. `onAwake` mantém a tentativa consumida até existir **novo estado real de dormir**; previne callback de wake atrasado criando um segundo sorteio. Logout/clone/mudança de dimensão limpam cache e fila. Lock `TELEPORTING` impede reentrada. Falha de chance não modifica cama, inventário ou attachment; teleporte só começa após roll positivo e alvo elegível carregado. Seleção de pesos e chegada segura continuam no motor existente. Não persiste boolean `hasNecklace`.

## 7. Multiplayer e limitações
A e B têm UUID e gate independentes. A com colar é candidato; B sem colar nunca é. Dois com colar rolam separadamente. Curios é consultado na decisão final, no servidor, nunca por packet cliente. **PENDENTE:** dois clientes dedicados, sleeping vote, logout aos 100 ticks, reentrada/duplo wake, Curios runtime 17.0.1 com mod externo e dados em saves antigos.

**Release bloqueado:** 5.4 ainda não consome o Despertador, 5.5 não trata respawn seguro, 5.7 ainda não permite recuperá-lo naturalmente em baús. Nenhuma mudança antecipada nessas três tarefas. Não chamar a etapa de homologação integral de Survival sem testar as dependências.

## 8. Testes e evidência
| Categoria | Situação nesta implementação |
| --- | --- |
| Inspeção de fonte e formato de assets/JSON | Preparados; validar no CI e em cliente |
| UNIT TEST | Implementados (JUnit); resultado de execução deve ser conferido no GitHub Actions |
| BUILD (`./gradlew --no-daemon clean test build`) | Não executado localmente (runtime local não possui JDK 25 nem acesso ao Maven); acompanhar CI do GitHub |
| DATA VALIDATION | Pendente de CI e dedicated server |
| SERVER SMOKE | CI `.github/workflows/build.yml` existe, verificar status após push |
| GAMETEST | Não executado |
| MANUAL SURVIVAL | Não executado |
| MULTIPLAYER | Não executado |

## 9. Comandos de laboratório prontos

```mcfunction
/seed
/gamemode creative
/give @s dreamsdimensions:dream_necklace 1
/give @s dreamsdimensions:oneiric_awakener 1
/clear @s dreamsdimensions:dream_necklace 0
/clear @s dreamsdimensions:oneiric_awakener 0
/time set night
/gamemode survival
/data get entity @s Pos
/execute in dreamsdimensions:dreamscape run tp @s X Y Z
/execute in dreamsdimensions:campo_onirico_azul run tp @s X Y Z
/execute in minecraft:overworld run tp @s X Y Z
```

**X Y Z devem ser coordenadas de piso seguro previamente verificadas no mundo; NÃO são coordenadas reais.** `/clear <player> <item> 0` consulta contagem sem remover no vanilla recente, confirmar a mensagem efetiva do servidor 26.3. `/give` e TP não provam crafting, worldgen, entrada por cama nem obtenção natural.

## 10. Checklist manual obrigatório, ainda PENDENTE
- A: sem colar, dorme vanilla. B: colar apenas no inventário. C: colar na offhand; os três não teleportam.
- D: colar no slot necklace ativo; `dream_entry_enabled=true`, chance 1.0: entrar em sonho carregado e seguro. E: chance 0.0: sono vanilla. F: 0.30: vários ciclos legítimos, observar somente uma tentativa por ciclo.
- G: multiplayer A com Colar/B sem; H: desconectar no sono/relog e duplicação; I: TP administrativo não simula contrato de entrada; J: Despertador continua podendo ser usado conforme baseline (consumo será corrigido na 5.4).
- Registrar seed, pos, versão Curios, chance, slot, resultado, log e contagem de itens. Seeds: 14011, 20261108, -904732, 73199013.

## 11. Próximas tasks
**5.4:** Despertador consumível idempotente após teleporte confirmado. **5.5:** sono nas dimensões e respawn local seguro em Survival/Hardcore. **5.6:** obtenção/custo do Dream Dust no Overworld. **5.7:** ruínas raras com Despertadores reais nas duas dimensões. Só então remover bloqueio de lançamento do ciclo de viagem.

Base de decisões: [Task 5.2](TRAVEL_RETURN_CONTRACT_TASK_5_2.md), [matriz 5.2](TRAVEL_RETURN_TEST_MATRIX_TASK_5_2.md).


## 12. Evidência de execução CI — 10/11/2026

**Código testado:** commit `6106debcda6aa10b7e5a622b9d60798b1e4d0af3` (`ModConfig.Type.SYNCED` para NeoForge 26.3), [GitHub Actions run 42881013010](https://github.com/ErikMendesRc/dreamsdimensions/actions/runs/42881013010).

| Seed | Gradle clean build (inclui JUnit) | Validações estáticas existentes | Dedicated server smoke/datapacks | Resultado do job |
| --- | --- | --- | --- | --- |
| 14011 | PASS | PASS | PASS | SUCCESS |
| 20261108 | PASS | PASS | PASS | SUCCESS |
| -904732 | PASS | PASS | PASS | SUCCESS |
| 73199013 | PASS | PASS | PASS | SUCCESS |

**Resultado:** as quatro matrizes de GitHub Actions finalizaram com `success`; Curios 17.0.1+26.3 foi resolvido na compilação, e o servidor de teste inicializou sem falha detectada pelo smoke. **Limite:** CI não prova, por si só, o comportamento visual do slot Curios no cliente, crafting manual, chance empírica, login/logout concorrente e multiplayer. **NOT RUN:** GameTest específico de sono, Survival manual, e dois clientes multiplayer. `dream_entry_enabled=false` continua sendo o bloqueio de release. O código do `OneiricAwakenerItem` não foi alterado na 5.3.
