# Arquitetura atual do DreamsDimensions

**Fonte de verdade:** Java e recursos na branch `migration/minecraft-26.3`; este documento não especifica classes futuras.

## Bootstrap, registros e configuração

`src/main/java/com/dreamsdimensions/mod/DreamsDimensions.java` registra `ModItems`, `ModBlocks`, `ModCreativeTabs`, `ModAttachments`, `ModEntities`, `ModEffects` e `DreamWorldgen`. Eventos comuns passam por `NeoForge.EVENT_BUS`; registros, por `IEventBus` do mod.

- `DreamEntryConfig`: configuração sincronizada `dream_entry_enabled=false` por padrão e `dream_entry_chance=0.30` como baseline experimental, **não balanceamento definitivo**.
- `DreamsConfig`: catálogo de definições dimensionais habilitadas.
- `ModAttachments`: estado de retorno e âncora local de expedição, com serialização e cuidado no respawn.
- `ModCreativeTabs`: conteúdos registrados, inclusive Pedra Marcada normal e lapidada.

## Dimensões e sono

- `dimension/DreamDimensionCatalog.java`: definições ordenadas e lookup por ResourceKey, hoje com duas dimensões; o contrato suporta novos perfis sem criar código duplicado por dimensão.
- `event/SleepTeleportHandler.java` e `service/DreamSleepCycle.java`: eventos de sono, guard contra tentativa duplicada, ciclo de 100 ticks e reset no logout/dimensão/clone.
- `service/DreamEntryPolicy.java`: elegibilidade e rolagem, distinta da distribuição ponderada de destinos.
- `service/DreamTeleportService.java`, `util/DreamSpawnHelper.java`: entrada no sonho e chegada segura.
- `item/OneiricAwakenerItem.java`, `service/OneiricReturnPolicy.java` e utilitários de retorno: 60 ticks de uso, retorno confirmado antes do consumo, cooldown e destinos seguros.
- `service/DreamRespawnService.java`: respawn no mundo onírico respeitando posição física validada.
- **Sem integração com anomalias da 7.6:** o código em `anomaly/` é uma biblioteca experimental inerte, sem registro de worldgen nem eventos de gameplay.

## Worldgen e conteúdo

- `src/main/resources/data/dreamsdimensions/dimension/` e `dimension_type/`: runtime dos mundos.
- `src/main/resources/data/dreamsdimensions/worldgen/` e `src/main/java/com/dreamsdimensions/mod/worldgen/`: geração do terreno, vegetação e features.
- `src/main/resources/data/dreamsdimensions/neoforge/biome_modifier/`: injeção additiva de recursos no Overworld.
- `src/main/resources/data/dreamsdimensions/recipe/` e `loot_table/`: receitas e drops, diretórios **singulares** do Minecraft 26.3.
- `src/main/resources/assets/dreamsdimensions/items/`: definições de item do cliente 26.3; `models/item/` **não** as substitui.

## Mobs, efeitos e feedback

`ModEntities` registra **Fragmento Errante** e **Vigia Adormecido**. A população reutiliza `entity/spawn/DreamMobPopulationService` e perfis específicos por espécie, com habitações verificadas e limites server-side. Objetivos, entidades e animações GeckoLib 5 continuam específicos por comportamento. O `event/EchoMossInteractionHandler` implementa a mecânica já existente de Eco do Musgo. `DreamDimensionEffectsHandler` aplica efeitos dimensionais controlados.

## Instrumentação e teste

`qa/DreamQaRecorder.java` tem buffer limitado e opt-in. Os comandos especializados em `diagnostics/` são registrados somente com suas opções JVM. O pipeline `.github/workflows/build.yml` executa Java 25, JUnit, checks Python, servidor dedicado e amostras em quatro seeds; não substitui testes de cliente/Survival.

**Regra para engenharia:** preservar IDs e semântica de saves; não adicionar listeners por tick, superclasses universais ou worldgen Java por conveniência. A justificativa para cada extensão deve nascer de um loop Survival comprovado, não de um nome de sistema.
