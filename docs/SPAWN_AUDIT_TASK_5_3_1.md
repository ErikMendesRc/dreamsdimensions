# TASK 5.3.1 — Auditoria de Spawn e Simplificação de Código

**Data:** 2026-11-10 | **Branch:** `migration/minecraft-26.3` | **Baseline:** `6f9d5ddbb7f92c7548c5ff89193379ee6b3b655b`
**Stack:** Minecraft 26.3, NeoForge 26.3.0.101, Java 25, GeckoLib 5.5.7.

## 1. Decisão principal: não trocar a distribuição de mobs sem evidência

A leitura integral dos relatórios originais `FRAGMENTO_ERRANTE_TASK_4_3.md`, `DREAM_MOB_FRAMEWORK_TASK_4_4.md` e `VIGIA_ADORMECIDO_TASK_4_5.md` revelou que **o spawn híbrido foi uma decisão deliberada, não necessariamente um bug**:

1. **Vanilla / JSON do bioma:** `worldgen/biome/dreamscape_biome.json` registra `fragmento_errante` (peso 100; grupo 2–3) e `vigia_adormecido` (peso 18; grupo 1) como `CREATURE`, com `minecraft:gameplay/creature_world_gen_spawn_probability = 0.34`. Essa fonte também contribui durante o povoamento na geração do terreno. Mobs permanecem sujeitos às restrições do `RegisterSpawnPlacementsEvent` e do `FloatingIslandHabitat`.
2. **NeoForge listener + Java específico:** `DreamMobPopulationService` complementa o spawn em ilhas anteriormente exploradas, quando jogadores estão próximos. A verificação de habitat é idêntica; o serviço conta entidades existentes antes de criar outras, respeita limites locais, valida chunks carregados, `GameRules.SPAWN_MOBS`, intervalo por perfil e região.
3. **Não há dois listeners Java por espécie:** existe só `DreamMobPopulationService::onPlayerTick`, com dois perfis imutáveis. O sistema não cria chunks para repor mobs e preserva `EntityType.spawn(..., EntitySpawnReason.NATURAL)`.

**Resultado da auditoria de código:** existe sobreposição entre duas *fontes de população*, mas **não foi demonstrada duplicação indevida de entidades ou excesso real em jogo**. Remover o spawn JSON causaria perda do povoamento vanilla/worldgen; remover o coordenador causaria perda do complemento em áreas exploradas. **Ambos são mantidos** até contagens reais antes/depois com jogadores em ilhas.

**Limites existentes:** Fragmento: período 200 ticks, até 48 candidatos, cap local 6, pelo menos 16 blocos do jogador, região de 128 blocos. Vigia: período 360 ticks, 32 candidatos, cap local 2. O serviço conta previamente mobs da mesma espécie no perímetro e não repõe acima do próprio cap; o spawn vanilla usa seus próprios caps e pode aumentar a população posteriormente. **Não confundir cap local com limite global garantido**. A documentação oficial 26.3 explica que `natural_mob_spawns` migrou de `Biome` para Environment Attributes, com comportamento próprio de `overlay`: https://docs.neoforged.net/primer/docs/26.3/#mob-spawn-settings-environment-attribute

## 2. Mudanças reais de código

| Caminho | Decisão | Justificativa |
| --- | --- | --- |
| `entity/spawn/DreamMobPopulationService.java` | Adicionado diagnóstico somente quando `-Ddreamsdimensions.auditMobSpawn=true` e cleanup de cache no `ServerStoppedEvent` | Observar contribuições *do coordenador* sem tags persistentes, novo inventário ou impacto em runtime normal; não manter caches de mundos encerrados |
| `DreamsDimensions.java` | Registrado `DreamMobPopulationService::onServerStopped` | Limpeza do estado transitório com o evento oficial NeoForge |
| `registry/ModItems.java` | `DREAM_DUST` passa a usar `DeferredItem<Item>` + `Item::new` | A classe `DreamDustItem` apenas herdava de `Item` sem sobrescritas; registro e ID não mudam |
| `item/DreamDustItem.java` | Removido | Código Java vazio sem função gameplay |
| `block/DreamOreBlock.java` | Removido `useWithoutItem`, que somente alternava `clicked` | As variantes tinham a mesma aparência e nenhum outro uso funcional; preserva mineração/XP vanilla |
| `assets/dreamsdimensions/blockstates/*dream_ore.json` | **Não alterados** | Variantes `clicked=false` e `clicked=true` continuam legíveis por mundos antigos, evitando migração de saves nesta task |
| `worldgen/biome/dreamscape_biome.json` | **Não alterado** | Preserva mecânica histórica e distribuição de mobs, pesos, grupos e taxa de worldgen |
| `scripts/ci/check_spawn_population_contract.py`, `.github/workflows/build.yml` | Nova guarda CI integrada ao workflow | Assegura entradas JSON únicas por espécie, ambos os registros e limites do coordenador, simplificação da poeira e preservação de estados antigos |
| `docs/GAMEPLAY_ROADMAP.md`, `docs/GAMEPLAY_CONTENT_MATRIX.md` | Addenda | Preservar histórico e registrar conclusão |

Nenhum ID de bloco/item/entidade/bioma alterado, nenhum NBT convertido, nenhuma receita modificada. Mobs, GeckoLib, teleporte, Curios, atmosfera e mundo onírico ficam iguais.

## 3. Diagnóstico opcional (sem reinvenção)

Por padrão **não há logging novo em produção**. No ambiente de desenvolvimento, iniciar com JVM system property:

```text
-Ddreamsdimensions.auditMobSpawn=true
```

Quando o coordenador criar uma entidade, a linha `DREAM_MOB_SPAWN origin=coordinator type=... dimension=... pos=... nearby_before=... local_cap=...` aparecerá no log. A linha **não** registra origens vanilla, não prova sozinha que não houve spawn natural e não altera o mob/NBT.

Desligar após amostragem. Para avaliar sobrepopulação: registrar contagem total das duas espécies dentro de regiões equivalentes, gameplay multiplayer e pelo menos 5 ilhas por seed; comparar com o número de eventos `origin=coordinator` e a permanência dos mobs em worldgen.

## 4. Testes — o que cada tipo comprova

| Camada | Situação |
| --- | --- |
| Contrato JSON e fonte de código (CI 5.3.1) | **PASS** — [GitHub Actions 42893544012](https://github.com/ErikMendesRc/dreamsdimensions/actions/runs/42893544012), nas quatro seeds |
| Regressões 4.2, 4.4, 4.5, 4.6 | Mantidas e **PASS** nas quatro seeds, sem reescrever asserts históricos: contrato de spawn híbrido preservado |
| `./gradlew --no-daemon clean test build` (Java 25) | **PASS** no commit de código \`d07dd6d89a12981bdbd8a0ce103f6192b779ece8\`, nas quatro seeds |
| Dedicated server 26.3: startup, datapack, summon/RCON, chunks/seed | **Pendente de execução CI no commit** |
| Survival manual: drop/XP, geração de poeira, Crafting | **Pendente** |
| Multiplayer natural: contagens, taxa de spawn em ilhas, caps e TPS | **Pendente**; CI sem jogador não reproduz isso |

Não declarar performance, frequência ou ausência de duplicidade em jogo homologadas por um teste estático.

## 5. Comandos copiáveis para testes manuais

**Atenção:** use mundo de laboratório; faça backup do save se estiver testando um mundo existente. As coordenadas (-4, 194, 18) registraram um Fragmento na seed `-904732` em relatório anterior, **não** foram homologadas como ponto de pouso seguro. Entre como *spectator* nesse levantamento:

```mcfunction
/seed
/gamemode spectator
/execute in dreamsdimensions:dreamscape run tp @s -4 194 18
/data get entity @s Pos
```

Inspecione as ilhas visualmente; escolha chão natural seguro na faixa de Y=158–231 e **só então** passe para survival para amostrar spawns. Vale também visitar o antigo ponto de ponte `-307 126 216` na seed `20261108` para conferir worldgen, **não** para medir spawn em ilhas (altura abaixo da faixa natural configurada).

```mcfunction
/gamemode creative
/gamerule spawn_mobs true
/execute in dreamsdimensions:dreamscape run tp @s -307 126 216
/summon dreamsdimensions:fragmento_errante ~ ~ ~
/summon dreamsdimensions:vigia_adormecido ~ ~ ~
/give @s dreamsdimensions:dream_dust 3
/give @s dreamsdimensions:oneiric_awakener 1
```

O `/summon` confirma registro e renderização, **não** comprova spawn natural. Para amostrar números perto de você após voltar a uma ilha segura com player ativo (não spectator), use scoreboard temporária:

```mcfunction
/scoreboard objectives add dd_spawn_audit dummy
/execute store result score #fragmentos dd_spawn_audit if entity @e[type=dreamsdimensions:fragmento_errante,distance=..72]
/scoreboard players get #fragmentos dd_spawn_audit
/execute store result score #vigias dd_spawn_audit if entity @e[type=dreamsdimensions:vigia_adormecido,distance=..72]
/scoreboard players get #vigias dd_spawn_audit
```

Registrar seed, posição, altura, quantidade de jogadores, dificuldade, tempo de observação e logs. O número reflete **entidades existentes**, independentemente da origem; só os logs de `origin=coordinator` distinguem contribuições do serviço.

Para testar save compatibility do estado do minério sem destruí-lo, use em local seguro no Overworld:

```mcfunction
/gamemode creative
/setblock ~2 ~ ~ dreamsdimensions:dream_ore[clicked=true]
/execute if block ~2 ~ ~ dreamsdimensions:dream_ore[clicked=true] run say Estado legado carregado
```

Clique no bloco com a mão vazia e repita o último comando; a propriedade **não deverá alternar**. Mine outro minério para verificar XP e loot, pois o comportamento de `DropExperienceBlock` continua vanilla. Faça o mesmo para `deepslate_dream_ore`.

Ao terminar o laboratório, limpe a scoreboard se desejado:

```mcfunction
/scoreboard objectives remove dd_spawn_audit
```

## 6. Critérios e próximas tasks

**Task 5.3.1** encerra revisão arquitetural e simplificações sem afetar o comportamento de Survival aprovado. **5.4** permanece prioridade: Despertador consumido após retorno confirmado; 5.5 respawn seguro; 5.7 recuperação em estruturas raras.

**Risco residual:** vanilla ainda pode adicionar criaturas apesar do coordenador já estar no cap local; só um levantamento real poderá dizer se isso gera sobrepopulação. Se as medições demonstrarem problema, avaliar balanceamento de pesos/periodicidade/caps com seeds controladas, sem substituir prematuramente o sistema.


## 7. Evidências verificadas após o push (10/11/2026)

O commit **`d07dd6d89a12981bdbd8a0ce103f6192b779ece8`** passou no [GitHub Actions run 42893544012](https://github.com/ErikMendesRc/dreamsdimensions/actions/runs/42893544012):

| Seed | JDK 25 / Gradle clean build + JUnit | Scripts CI, inclusive 5.3.1 | Dedicated server RCON/Datapacks | Job |
| --- | --- | --- | --- | --- |
| `14011` | PASS | PASS | PASS | SUCCESS |
| `20261108` | PASS | PASS | PASS | SUCCESS |
| `-904732` | PASS | PASS | PASS | SUCCESS |
| `73199013` | PASS | PASS | PASS | SUCCESS |

**PENDENTES de homologação:** comparação de população nas ilhas antes/depois por jogadores reais, teste de dois jogadores, avaliação de TPS em sessão prolongada, clique em minério já existente no save, mineração/XP observada e crafting manual. Nenhuma dessas verificações foi apresentada como executada.
