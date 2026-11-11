# TASK 5.7 — Ruínas e Baús de Recuperação Onírica

**Branch:** `migration/minecraft-26.3` · **Data:** 2026-11-10 (America/Sao_Paulo) · **Baseline:** `50399472b0ddc2cbf59603bbe22333e5e6080408` · **Versão:** Minecraft 26.3 / NeoForge 26.3.0.101 / Java 25.

## 1. Contrato de gameplay

O Colar dos Sonhos concede uma chance de viagem por sono. O Despertador Onírico (`dreamsdimensions:oneiric_awakener`) funciona por 60 ticks e é consumido **somente após retorno confirmado** ao Overworld. A morte mantém o jogador no sonho. Esta Task implementa uma **fonte exploratória, rara e persistente** do Despertador completo para quem perdeu/entrou sem o item.

**Não há** Pó Onírico nas ruínas; não existe receita dimensional de Dream Dust, fuga por morte/cama, baú renovável, item substituto, entrega por timer, porta mágica, nova textura, novo mob ou mudança no motor de viagem. `dream_entry_enabled=false` permanece padrão até homologação Survival e multiplayer.

## 2. Arquitetura oficial e compatibilidade 26.3

- Estruturas **nativas** data-driven: `minecraft:jigsaw` com um único template inicial (`size=1`), `minecraft:single_pool_element`, `minecraft:empty` fallback. Isso preserva `/locate structure` e `/place structure`, persistência vanilla e geração por Structure Set em chunks novos.
- Diretórios 26.3: `data/dreamsdimensions/worldgen/structure/*.json`, `worldgen/structure_set/*.json`, `worldgen/template_pool/ruins/*.json`, `structure/ruins/*.nbt` **singular** e `loot_table/chests/*.json` **singular**.
- Templates reais: NBT binário **gzip**, big-endian, `DataVersion=5023`, paleta moderna `id` / `properties`. Gerados com geometria independente, auditados por parser NBT da CI. Cada template tem **um único chest block entity** com a propriedade `LootTable` (não faz o sorteio durante a geração). Sem `LootTableSeed` fixo.
- JSON worldgen usa `project_start_to_heightmap: WORLD_SURFACE_WG`, `terrain_adaptation:none` (evita gerar terra extra/alterar density) e start height 0 como offset. Não foi criado Java para gerar ou posicionar estruturas em tick. **Limitação**: Jigsaw/heightmap sozinho não valida o apoio real de *todos* os pilares/cantos em ilhas, nem garante conexão navegável com uma ilha adjacente. Não declarar Dreamscape homologado sem conferir suporte em estruturas reais; um futuro preflight customizado deverá ser adicionado **somente se necessário e demonstrado**.

Fontes: [NeoForge 26.3](https://docs.neoforged.net/primer/docs/26.3/), [Worldgen](https://docs.neoforged.net/docs/worldgen/), [Loot](https://docs.neoforged.net/docs/resources/server/loottables/), [Minecraft Java 26.3](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3).

## 3. Identidade visual e posicionamento inicial

| Estrutura e dimensão | Visual original | Template | Distribuição inicial |
| --- | --- | --- | --- |
| `dreamsdimensions:blue_dream_ruins` · `campo_onirico_azul` | Pátio oval arruinado 11×11, piso central irregular de pedra azul/pedregulho, quatro colunas de alturas distintas, musgo discreto, um baú central | `ruins/blue_dream_ruins.nbt` | `random_spread`, spacing **38** chunks, separation **12**, salt **16783011** |
| `dreamsdimensions:fragmented_dream_ruins` · `dreamscape` | Plataforma quebrada 9×9 com silhueta estreita, eixos interrompidos, quatro obeliscos quebrados assimétricos, rocha Serena/terra e musgo, um baú central | `ruins/fragmented_dream_ruins.nbt` | `random_spread`, spacing **32** chunks, separation **12**, salt **16783029** |

Os biomas do catálogo são respectivamente `dreamsdimensions:campo_onirico_azul` e `dreamsdimensions:dreamscape_biome`. Os filtros de bioma das duas estruturas são **IDs exatos**, não biomas Overworld, Nether ou End. Os pesos e sais são distintos; versões futuras podem alterar spacing/separation/salt via datapack. Há **uma única estrutura por set**, evitando instâncias duplicadas pelo mesmo slot. Não usar esses valores como prova de distância real ao jogador, taxa de sucesso ou P90 de descoberta.

**Riscos a medir:** base parcialmente suspensa por desnível, geração sobre ilha minúscula, proximidade a borda/vazio ou geometria natural, colunas visíveis mas baú coberto por vegetação gerada posteriormente. A checagem de suporte local dos baús do diagnóstico é evidência parcial; inspeção visual do terreno e pontes é indispensável.

## 4. Baús, loot e economia

**Uma** loot table por estrutura:
- `dreamsdimensions:chests/blue_dream_ruins`
- `dreamsdimensions:chests/fragmented_dream_ruins`

Cada tabela contém:
- **Pool raro**: `rolls=1`, `dreamsdimensions:oneiric_awakener` com weight=1, `minecraft:empty` com weight=3; probabilidade **exata configurada = 1/(1+3) = 25%**, no máximo **1** Despertador e nenhum Pó.
- **Pool de suprimentos**: 2–3 rolls de pequena lista de bread, torch, madeiras, ferramentas de pedra e blocos para travessia. Diferentes entre Blue e Dreamscape. Esses rolls não incluem Despertador nem materiais de mineração de alto valor. Testar no cliente quantidades/valor real da recompensa.

**Probabilidade teórica de não encontrar Despertador em N ruínas independentes:** `0.75^N`; para N=1: 75%, N=5: 23,73%, N=10: 5,63%. NÃO é frequência observada de loot, nem medida de dificuldade.

**Persistência:** o `RandomizableContainerBlockEntity` vanilla armazena `LootTable` até a primeira abertura. O primeiro acesso desempacota e fixa o conteúdo no próprio baú; fechamentos, relog, restarts, mudanças de dimensão e multiplayer não geram novo pool. O diagnóstico **não abre** o chest nem chama `getItem`/`unpackLootTable`, preservando o estado. A experiência real de persistência ainda precisa de playtest com um ou dois clientes.

## 5. Diagnóstico de worldgen real e CI

`scripts/ci/check_dream_ruins.py` abre os dois arquivos NBT como gzip e valida **paleta, limites 3D, piso do baú, único chest block entity, ligação da loot table**, JSON structure-set/bioma/pool, chance 25%, ausência de Pó, consistência das recipes e limitações da arquitetura. Simulação Python de 1.000 escolhas RNG **não é execução da loot table Minecraft**; não chamar de loot observado.

`DreamRuinsAuditCommand` só é registrado se `-Ddreamsdimensions.enableRuinsAudit=true` e exige operador. `dream_ruins_audit blue|fragmented x z radius` aceita raio **8..24**; **não carrega chunks**: recusa se parte da região não estiver carregada; lê block entities dos chunks, valida o loot table ID em baús ainda não abertos, ar sobre a tampa, possibilidade de aproximação e número de apoios sob o piso numa janela 3×3. Não gera loot nem escreve blocos. É instrumentação de laboratório, não feature de gameplay.

`scripts/ci/audit_dream_ruins_runtime.py` inicia com seed validada por `/seed`, usa `/execute in <dimension> run locate structure <id>` para encontrar candidato nativo (sem chamar `/place`), faz forceload transitório da região **somente no mundo descartável do workflow**, executa o diagnóstico e remove tickets. Produz `dream-ruins-seed-<seed>.json`; o log `DREAM_RUINS_CHEST` registra **X Y Z reais se encontrados**, bioma, status loot e suporte. Se alguma região não puder ser avaliada, marcar `NOT RUN`, `LOCATE_FAILED` ou `AUDIT_FAILED`, nunca inventar coordenadas.

**Seeds:** 14011, 20261108, -904732, 73199013. **Validação inicial da CI:** PENDENTE de resultado no commit de implementação. Dedicated server + /locate **não comprova** travessia real, persistência após reboot, o sorteio real ou a raridade populacional. É necessário expandir a amostra de vários setores por seed para estimar distâncias e rejeições.

## 6. Comandos copiáveis de teste

Atualizar a branch sem mexer na main:

```powershell
git checkout migration/minecraft-26.3
git pull origin migration/minecraft-26.3
```

Config inicial **em mundo descartável**: manter `dream_entry_enabled=false` fora do laboratório. Com operador:

```mcfunction
/seed
/gamemode creative
/give @s dreamsdimensions:dream_necklace 1
/give @s dreamsdimensions:oneiric_awakener 1
/gamemode spectator
```

Em **Spectator**, teleportar às dimensões usando coordenadas que você já verificou no seu save, sem afirmar posições seguras genéricas:

```mcfunction
/execute in dreamsdimensions:campo_onirico_azul run tp @s X Y Z
/execute in dreamsdimensions:dreamscape run tp @s X Y Z
/execute in minecraft:overworld run tp @s X Y Z
```

**Localizar estruturas nativas** (IDs implementados):

```mcfunction
/execute in dreamsdimensions:campo_onirico_azul run locate structure dreamsdimensions:blue_dream_ruins
/execute in dreamsdimensions:dreamscape run locate structure dreamsdimensions:fragmented_dream_ruins
```

**Gerar forçadamente para laboratório visual** — isto NÃO prova existência natural:

```mcfunction
/execute in dreamsdimensions:campo_onirico_azul run place structure dreamsdimensions:blue_dream_ruins
/execute in dreamsdimensions:dreamscape run place structure dreamsdimensions:fragmented_dream_ruins
```

**Conferir loot table no laboratório**, sem afirmar que o item foi obtido naturalmente:

```mcfunction
/loot give @s loot dreamsdimensions:chests/blue_dream_ruins
/loot give @s loot dreamsdimensions:chests/fragmented_dream_ruins
```

**Ler um baú real sem sortear loot** (substituir X Y Z pelo bloco realmente localizado):

```mcfunction
/execute in dreamsdimensions:dreamscape run data get block X Y Z LootTable
/execute in dreamsdimensions:campo_onirico_azul run data get block X Y Z LootTable
```

Depois da **primeira abertura**, `LootTable` deixa de estar pendente; consultar `/data get block X Y Z Items` para inspecionar inventário (pode estar vazio e não prova regeneração). O teste correto é conferir antes, abrir uma única vez, fechar/reabrir, reload/restart e comparar conteúdo. Em multiplayer, dois jogadores devem ver **o mesmo** inventário mundial.

**Diagnóstico opcional**, somente com propriedade JVM `-Ddreamsdimensions.enableRuinsAudit=true`, e após carregar os chunks da área no servidor de teste:

```mcfunction
/dream_ruins_audit blue X Z 24
/dream_ruins_audit fragmented X Z 24
```

**Ciclo Survival:** com Colar em Curios e entrada de teste habilitada, viajar por sono, remover Despertadores e explorar sem comandos. Encontrar ruína, abrir baú e, quando surgir o Despertador, usá-lo durante 60 ticks. Confirmar retorno ao Overworld, gasto único e limpeza da âncora. `/kill @s` deve manter respawn no sonho. `keepInventory` permanece vanilla.

**Segurança de teleporte:** as coordenadas de `/locate` são estrutura, **não necessariamente chão seguro**. Primeiramente entrar em Spectator, inspecionar o piso e confirmar uma coluna segura; só então usar Survival. Nenhum TP é apresentado como homologado sem medição.

## 7. Testes e critérios de aceite

| Categoria | Status inicial | Interpretação |
| --- | --- | --- |
| NBT real + estrutura JSON + 25% single-roll | **PENDENTE de CI** | Prova formato e regra configurada |
| Build/JUnit + datapack dedicated server | **PENDENTE de CI** | Confirma classes, registries e parsing |
| /locate real por seed/dimensão | **PENDENTE de CI** | Confirma estrutura nativa localizável |
| Baú físico + referência de loot + suporte local | **PENDENTE de CI** | Observação real não-destrutiva |
| 1.000 loot rolls executados pelo Minecraft | **NOT RUN** | Modelo Python não substitui essa medição |
| Acesso aos baús em Survival | **NOT RUN** | Travel routes/queda/mobs em jogo |
| Persistência após reload/restart/duas contas | **NOT RUN** | Não inferir somente de lógica vanilla |
| Salvamento antigo e novos chunks | **NOT RUN** | Jigsaw não retrogera chunks por padrão |
| Distribuição por muitas estruturas/percentis de distância | **NOT RUN** | Uma ocorrência por seed/dimensão é amostra insuficiente |

**Bloqueio de release:** `dream_entry_enabled` continua false até homologar recuperação normal em ambas as dimensões, comportamento multiplayer e reaparecimento seguro após morte. Se o layout vanilla permitir baú inseguro/ruína flutuando, **não publicar como resolvido**; uma task corretiva de preflight deve impedir estruturas inválidas antes de aceitar um mundo como Survival seguro.

## 8. Arquivos e preservação

Novos: 2 `worldgen/structure`, 2 `worldgen/structure_set`, 2 `worldgen/template_pool`, 2 `structure/ruins/*.nbt` reais, 2 `loot_table/chests`, `diagnostics/DreamRuinsAuditCommand.java`, scripts estático/runtime, relatório. Alterados: `DreamsDimensions.java` (registro de diagnóstico apenas se opt-in), CI e documentação do roadmap/matriz.

**Preservados integralmente:** `DreamDimensionCatalog`, `DreamSpawnHelper`, `DreamRespawnService`, `DreamTeleportService`, `OneiricAwakenerItem`, `DreamReturnHelper`, `ModBlocks`, `ModItems`, biomas e noise, minérios, receitas, chance do Colar, Curios, mobs, GeckoLib.

**Próximas decisões:** após reproduzir as quatro seeds, verificar se o dimensionamento em ilhas exige preflight mínimo; medir acessibilidade/tempo de recuperação em Survival, loot real por 1.000 rolls e persistência em dois clientes. Não mover a entrada do mod para enabled até evidência real.

**Commits/CI:** preencher após execução real. Não declarar homologação automática do Survival.
