# Dreams Dimensions — Documentação Técnica Completa

## Sumário

1. [Título e Informações do Projeto](#1-título-e-informações-do-projeto)
2. [Visão Geral do Sistema](#2-visão-geral-do-sistema)
3. [Instalação e Ambiente de Desenvolvimento](#3-instalação-e-ambiente-de-desenvolvimento)
4. [Estrutura de Pacotes / Módulos](#4-estrutura-de-pacotes--módulos)
5. [Componentes Principais](#5-componentes-principais)
6. [Fluxos de Uso](#6-fluxos-de-uso)
7. [Diagramas de Arquitetura](#7-diagramas-de-arquitetura)
8. [Teste, Build e Release](#8-teste-build-e-release)
9. [Contribuindo](#9-contribuindo)
10. [Referências e Recursos](#10-referências-e-recursos)

---

## 1. Título e Informações do Projeto

- **Nome do mod:** Dreams Dimensions  
- **mod_id:** `dreamsdimensions`  
- **Versão alvo do Minecraft:** `1.21.11`  
- **API de modding usada:** **NeoForge** (`21.11.37-beta`)  
- **Loader:** Java FML (`loader_version_range=[4,)`)  
- **Linguagem:** Java 21  
- **Build tool:** Gradle + plugin `net.neoforged.gradle.userdev` (`7.1.20`)  
- **Mappings:** mapeamentos oficiais via toolchain do NeoForge (não há configuração explícita de Parchment no build atual)  
- **Objetivo do mod:** adicionar dimensões oníricas acessadas ao dormir no Overworld e um mecanismo controlado de retorno via item (Despertador Onírico).

### Contexto funcional

O mod implementa um ciclo “**dormir → sonhar → despertar**”:

- O jogador dorme no Overworld por tempo suficiente e é teleportado para uma dimensão de sonho aleatória.
- O jogador usa o item `ow_oneiric_awakener` para retornar ao Overworld.
- O retorno prioriza posição da cama original e mantém fallback seguro para respawn ou spawn global.

---

## 2. Visão Geral do Sistema

### Descrição de alto nível

O Dreams Dimensions é estruturado em torno de **eventos de servidor** (ticks e spawn), **registros NeoForge** (itens, blocos, attachments, creative tab), e **datapacks** (dimensões, tipos de dimensão, biomas, noise settings, receitas e worldgen).

### Componentes principais

- **Bootstrap do mod** (`DreamsDimensions`): registra config, registries e listeners.
- **Teleporte por sono** (`SleepTeleportHandler`): envia jogador para dimensão onírica após 100 ticks de sono.
- **Retorno ao Overworld** (`OneiricAwakenerItem` + `DreamReturnHelper`): controla uso do item, cooldown e destino seguro.
- **Persistência de ponto de retorno** (`DreamReturnData` + attachment `dream_return`): salva cama/posição/yaw do jogador.
- **Configuração dinâmica** (`DreamsConfig`): lista quais dimensões são tratadas como “de sonho”.
- **Client-side visuals** (`ClientDimensionEvents` / `DreamTransitionScreen`): tela de transição ao trocar entre dimensões.

### Diagrama conceitual (alto nível)

```text
┌─────────────────────┐
│ Jogador no Overworld│
└──────────┬──────────┘
           │ dorme >= 100 ticks
           ▼
┌──────────────────────────────┐
│ SleepTeleportHandler         │
│ - escolhe dreamscape/campo   │
│ - teleporta com spawn seguro │
└──────────┬───────────────────┘
           ▼
┌──────────────────────────────┐
│ Dimensão de sonho            │
│ - exploração                 │
│ - efeitos especiais (azul)   │
└──────────┬───────────────────┘
           │ usa Oneiric Awakener
           ▼
┌──────────────────────────────┐
│ DreamReturnHelper            │
│ prioridade de retorno:       │
│ 1) cama salva                │
│ 2) respawn do player         │
│ 3) spawn global seguro       │
└──────────┬───────────────────┘
           ▼
┌─────────────────────┐
│ Overworld           │
└─────────────────────┘
```

---

## 3. Instalação e Ambiente de Desenvolvimento

### Requisitos

- **JDK 21** (obrigatório para MC 1.21.11)
- **Gradle wrapper** (já incluso no projeto)
- **IDE recomendada:** IntelliJ IDEA (ou Eclipse)
- **Git** para versionamento

### Passo a passo para importar o projeto

1. Clone o repositório.
2. Abra a pasta raiz no IntelliJ/Eclipse.
3. Aguarde sincronização Gradle.
4. Confirme SDK/JDK 21 no projeto.

### Plugins/extensões úteis

- **IntelliJ:** Minecraft Development (opcional), Lombok plugin (não obrigatório neste projeto), EditorConfig.
- **Eclipse:** Buildship Gradle Integration.

### Run Configurations (Client/Server)

O `build.gradle` já define `runs { client, server, gameTestServer, clientData }`.

Comandos práticos:

```bash
./gradlew runClient
./gradlew runServer
./gradlew runGameTestServer
./gradlew runClientData
```

Observações:
- `runServer` já passa `--nogui`.
- As propriedades `neoforge.enabledGameTestNamespaces` são configuradas automaticamente para `dreamsdimensions`.

---

## 4. Estrutura de Pacotes / Módulos

### Convenções de pacotes

Base package: `com.dreamsdimensions.mod`

Submódulos principais:

- `attachment` → dados persistentes por entidade (player)
- `block` → blocos customizados
- `client.event` / `client.screen` → eventos e UI client-only
- `config` → `ModConfigSpec` e parsing de configuração
- `event` → handlers de gameplay no servidor
- `item` → itens customizados
- `registry` → registros NeoForge (itens, blocos, tabs, attachments)
- `util` → regras utilitárias de teleporte/retorno

### Justificativa arquitetural

A arquitetura separa claramente:

- **Lifecycle / registry** (mod bus) de **runtime gameplay** (NeoForge event bus).
- **Lógica de domínio** (retorno e segurança de spawn) em utilitário dedicado (`DreamReturnHelper`).
- **Estado persistente de jogador** em attachment serializável (`DreamReturnData`) com `Codec`, seguindo padrão moderno NeoForge.

### Padrões utilizados

- **Singleton utilitário estático** para handlers (`final class` + construtor privado).
- **Registro declarativo** com `DeferredRegister`.
- **Event-driven architecture** (listeners de tick e spawn).
- **Attachment-based persistence** para dados do jogador (copy on death).

---

## 5. Componentes Principais

### 5.1 Blocos e Itens

#### Blocos

- Registrados em `ModBlocks`.
- Incluem blocos temáticos de sonho (`dream_grass_block`, `blue_dream_stone`, `ow_oneiric_core_block`, etc.).
- Recursos associados: `blockstates`, `models`, `textures`, `loot_tables`, tags de mineração e recipes.

#### Itens

- Registrados em `ModItems`.
- Itens-chave:
  - `ow_dream_dust`
  - `ow_oneiric_awakener`
  - `ow_oneiric_residue`
  - `ow_refined_oneiric_powder`
  - `ow_stabilized_dream_fragment`
  - `ow_condensed_dream_crystal`
  - `ow_dream_binding_thread`
  - `ow_dream_catalyst`
  - `ow_stabilizing_essence`

### Sprint 2 — Economia Overworld

Fluxo de refino onírico OW:
`ow_dream_dust -> ow_oneiric_residue -> ow_refined_oneiric_powder -> ow_stabilized_dream_fragment -> ow_condensed_dream_crystal`

Receitas implementadas nesta etapa:
- `ow_oneiric_residue`: shapeless `2x ow_dream_dust -> 1x ow_oneiric_residue`.
- `ow_refined_oneiric_powder`: smelting `ow_oneiric_residue` (200 ticks, 0.1 XP).
- `ow_refined_oneiric_powder`: blasting `ow_oneiric_residue` (100 ticks, 0.1 XP).
- `ow_stabilized_dream_fragment`: shaped 2x2 com `ow_refined_oneiric_powder`.
- `ow_condensed_dream_crystal`: shaped `8x ow_stabilized_dream_fragment` + `minecraft:glass` no centro.
- `ow_dream_binding_thread`: shapeless `minecraft:string + ow_refined_oneiric_powder`.
- `ow_dream_catalyst`: shapeless `minecraft:blaze_powder + ow_refined_oneiric_powder`.
- `ow_stabilizing_essence`: shapeless `minecraft:ghast_tear + ow_stabilized_dream_fragment`.

Exemplo de lógica do item despertador (resumo):

```java
@Override
public InteractionResult use(Level level, Player player, InteractionHand hand) {
    if (!DreamsConfig.isDreamDimension(level.dimension())) return InteractionResult.PASS;
    player.startUsingItem(hand);
    return InteractionResult.CONSUME;
}
```

No `finishUsingItem`, o código:
1. valida se é `ServerPlayer`,
2. valida dimensão onírica,
3. calcula transição de retorno,
4. teleporta,
5. aplica cooldown.

### 5.2 Dimensões oníricas

Dimensões principais:

- `dreamsdimensions:dreamscape`
- `dreamsdimensions:campo_onirico_azul`

Arquivos relacionados:

- `data/dreamsdimensions/dimension/*.json`
- `data/dreamsdimensions/dimension_type/*.json`
- `data/dreamsdimensions/worldgen/biome/*.json`
- `data/dreamsdimensions/worldgen/noise_settings/*.json`

Teleporte de entrada:
- `SleepTeleportHandler` escolhe dimensão aleatória da lista fixa.
- Usa `player.isSleepingLongEnough()` (>= 100 ticks).
- Evita retrigger por ciclo de sono com `Set<UUID>` concorrente.

### 5.3 Listeners / Events

Eventos tratados:

- `PlayerTickEvent.Post`:
  - `SleepTeleportHandler` (entrada no sonho)
  - `DreamDimensionEffectsHandler` (Slow Falling no campo onírico azul)
- `PlayerSetSpawnEvent`:
  - `DreamReturnAttachmentHandler` (atualiza cama/posição de retorno)
- Client event:
  - `RegisterDimensionTransitionScreenEvent` para `DreamTransitionScreen`

### 5.4 Configurações e JSON

- Config Java: `DreamsConfig` (`ModConfigSpec`)
  - Chave: `dream_dimensions` (lista de IDs de dimensões tratadas como sonho)
- JSON/datapack:
  - dimensão e tipo de dimensão
  - biomas e noise settings
  - worldgen de minério (`configured_feature`, `placed_feature`, `biome_modifier`)
  - receitas, loot tables, tags

---

## 6. Fluxos de Uso

### Fluxo A — Entrar em dimensão de sonho

1. Jogador dorme no Overworld.
2. Tick listener verifica `isSleepingLongEnough`.
3. Handler evita duplicidade de teleporte naquele ciclo.
4. Seleciona uma das dimensões de sonho.
5. Teleporta para spawn seguro da dimensão alvo.

#### Diagrama de sequência (Fluxo A)

```text
Jogador      SleepTeleportHandler      MinecraftServer      ServerLevel(target)
   |                 |                        |                    |
   |--dorme---------->|                        |                    |
   |                 |--consulta dimensão---->|                    |
   |                 |<-----ServerLevel-------|                    |
   |                 |--findSafeSpawn----------------------------->|
   |                 |<-------------pos segura---------------------|
   |<----------------|--teleportTo--------------------------------|
```

### Fluxo B — Retornar com Oneiric Awakener

1. Jogador usa item na dimensão onírica.
2. Ao concluir uso (`finishUsingItem`), mod chama `DreamReturnHelper`.
3. Helper decide destino por prioridade:
   - cama salva no attachment,
   - respawn do jogador,
   - spawn global seguro.
4. Servidor executa teleporte para Overworld.
5. Item entra em cooldown.

#### Diagrama de sequência (Fluxo B)

```text
Jogador  OneiricAwakenerItem  DreamReturnHelper  Attachment/Respawn  Overworld
   |            |                    |                  |              |
   |--use------>|                    |                  |              |
   |            |--finishUsingItem-->|                  |              |
   |            |                    |--resolveTarget-->|              |
   |            |                    |<--target---------|              |
   |<-----------|--teleport---------------------------->|              |
   |<-----------|--cooldown aplicado                                   |
```

### Exemplo de pseudo-código de resolução de retorno

```pseudo
if attachment.temCamaValidaNoOverworld():
    return standUpPositionDaCama
else if player.temRespawnValido():
    return respawnDoPlayer
else:
    return spawnGlobalComBuscaDePosicaoSegura
```

---

## 7. Diagramas de Arquitetura

### 7.1 Diagrama de pacotes

```text
com.dreamsdimensions.mod
├── DreamsDimensions (bootstrap)
├── attachment
│   └── DreamReturnData
├── config
│   └── DreamsConfig
├── event
│   ├── SleepTeleportHandler
│   ├── DreamReturnAttachmentHandler
│   └── DreamDimensionEffectsHandler
├── item
│   └── OneiricAwakenerItem, DreamDustItem
├── block
│   └── DreamOreBlock
├── registry
│   ├── ModItems
│   ├── ModBlocks
│   ├── ModCreativeTabs
│   └── ModAttachments
├── util
│   └── DreamReturnHelper
└── client
    ├── event (ClientModEvents, ClientDimensionEvents)
    └── screen (DreamTransitionScreen)
```

### 7.2 Diagrama de dependências (simplificado)

```text
DreamsDimensions
  ├─> ModItems / ModBlocks / ModCreativeTabs / ModAttachments
  ├─> DreamsConfig
  └─> Event Handlers (runtime)

OneiricAwakenerItem
  ├─> DreamsConfig
  └─> DreamReturnHelper
        ├─> ModAttachments (DreamReturnData)
        └─> APIs de TeleportTransition/Respawn

SleepTeleportHandler
  └─> dimensões de sonho + teleporte seguro
```

---

## 8. Teste, Build e Release

### Build do mod

Comandos principais:

```bash
./gradlew clean build
```

Artefato esperado:
- `build/libs/dreamsdimensions-<versão>.jar`

### Teste local no Minecraft

- Use `./gradlew runClient` para validar gameplay completo.
- Cenários mínimos de teste manual:
  1. Dormir no Overworld e confirmar teleporte após ~5 segundos.
  2. Usar `ow_oneiric_awakener` e validar retorno correto.
  3. Validar fallback de retorno removendo/invalidando cama.
  4. Confirmar efeito `Slow Falling` no `campo_onirico_azul`.

### Checklist de QA pré-release

- [ ] Compila sem warnings críticos.
- [ ] `runClient` inicializa sem crash.
- [ ] Entrar/sair das dimensões funciona em singleplayer.
- [ ] Localizações (`en_us`, `pt_br`) contêm textos de itens/mensagens.
- [ ] Recipes e loot tables válidos em datapack reload.
- [ ] JAR final contém assets e data esperados.

---

## 9. Contribuindo

### Guidelines de contribuição

1. Crie branch por feature/fix.
2. Mantenha commits pequenos e descritivos.
3. Abra PR com:
   - problema resolvido,
   - abordagem adotada,
   - impacto em gameplay,
   - passos de validação.

### Normas de estilo

- Manter organização por pacote (`event`, `registry`, `util`, etc.).
- Evitar lógica de negócio em classe de bootstrap.
- Preferir métodos pequenos e com responsabilidade única.
- Seguir convenções Java/NeoForge existentes no projeto.

### Issues e Pull Requests

Sugestão para issue template:
- Contexto
- Comportamento atual
- Comportamento esperado
- Logs / stack trace
- Versões (Minecraft, NeoForge, mod)

---

## 10. Referências e Recursos

### Documentação oficial

- NeoForge docs: https://docs.neoforged.net/
- NeoForge javadocs (conforme versão em uso)
- Minecraft Wiki (datapacks, dimensions, worldgen): https://minecraft.wiki/

### Tópicos úteis para este mod

- Deferred Register e ciclo de vida (`IEventBus`, MOD bus vs EVENT_BUS)
- Data Attachments no NeoForge
- `TeleportTransition` e regras de respawn
- Definição de dimensão/bioma/noise via datapack JSON

### Boas práticas de modding aplicáveis

- Validar sempre client vs server side em eventos.
- Evitar side effects em ticks sem guards (como set de controle por UUID).
- Implementar fallback seguro para teleporte/respawn.
- Manter dados persistentes com codec e compatibilidade forward.

---

## Exemplo prático de extensão futura

Caso queira adicionar uma terceira dimensão onírica:

1. Adicione JSONs de `dimension_type`, `dimension`, `biome`, `noise_settings`.
2. Inclua o novo ID em `dream_dimensions` da config.
3. (Opcional) Adicione efeitos específicos em novo handler.
4. Garanta assets/loot/recipes associados.
5. Teste o fluxo de entrada/retorno para nova dimensão.

Isso mantém o design atual escalável sem alterar o contrato principal do mod.
