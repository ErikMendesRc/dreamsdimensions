# Dreams Dimensions — Documentação Técnica para Desenvolvedores

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
- **Versão alvo do Minecraft:** `26.1-snapshot-4`
- **API / Loader:** NeoForge (ModDevGradle `net.neoforged.moddev`)
- **Versão NeoForge configurada:** `26.1.0.0-alpha.8+snapshot-4`
- **Linguagem / Toolchain:** Java com toolchain em **Java 25**
- **Build system:** Gradle + NeoGradle/ModDevGradle
- **Objetivo do mod:** introduzir “dimensões oníricas” acessadas ao dormir no Overworld, com lógica de teleporte, retorno seguro e conteúdo próprio (blocos/itens/worldgen).

### Convenções de modding aplicadas

Este projeto segue convenções clássicas de NeoForge:

- Uso de `@Mod` como ponto de entrada.
- Registro por `DeferredRegister` para blocos, itens e attachments.
- Separação entre:
  - **MOD Event Bus** (lifecycle e registros), e
  - **NeoForge.EVENT_BUS** (eventos de runtime).
- Uso de Data Pack JSON para dimensões, biomas, noise settings, receitas, loot e worldgen.

---

## 2. Visão Geral do Sistema

O Dreams Dimensions implementa dois mundos de sonho (`dreamscape` e `campo_onirico_azul`) e uma mecânica de ida/volta baseada em gameplay:

1. Jogador dorme no Overworld por pelo menos 100 ticks.
2. O mod teleporta o jogador para uma dimensão onírica aleatória.
3. Dentro das dimensões oníricas, o item **Oneiric Awakener** permite retorno ao Overworld.
4. O retorno prioriza local seguro próximo da cama registrada; se não for possível, aplica fallback para respawn pessoal ou spawn global.

### Componentes principais

- **Bootstrap do mod:** `DreamsDimensions`
- **Entrada por sono:** `SleepTeleportHandler`
- **Retorno seguro:** `OneiricAwakenerItem` + `DreamReturnHelper`
- **Persistência de retorno:** `DreamReturnData` via `AttachmentType`
- **Eventos de gameplay:** handlers em `event/`
- **Conteúdo registrável:** `registry/` (blocos, itens, abas criativas, attachments)
- **Conteúdo data-driven:** JSONs em `src/main/resources/data/...`

### Diagrama conceitual (alto nível)

```text
[Jogador dorme no Overworld]
            |
            v
 [SleepTeleportHandler on tick]
            |
   (>=100 ticks dormindo)
            |
            v
[Escolhe dimensão onírica aleatória]
            |
            v
[Teleporta para dreamscape/campo_onirico_azul]
            |
            v
[Jogador usa Oneiric Awakener]
            |
            v
[DreamReturnHelper resolve destino seguro]
     | cama válida | respawn player | spawn global
            v
      [Retorno ao Overworld]
```

---

## 3. Instalação e Ambiente de Desenvolvimento

## Requisitos

- **JDK 25** (compatível com toolchain do projeto).
- **Gradle Wrapper** (já incluído: `./gradlew`).
- **IDE recomendada:** IntelliJ IDEA (ou Eclipse com suporte Gradle/Java).
- Plugin de suporte Java/Gradle (normalmente já padrão na IDE).

## Passo a passo para importar

1. Clone o repositório.
2. Abra a pasta do projeto na IDE.
3. Importe como projeto Gradle.
4. Aguarde a sincronização de dependências e modelos do NeoForge ModDev.
5. Configure SDK/JDK para Java 25, se a IDE não detectar automaticamente.

## Run Configurations (client / server)

As runs são declaradas no `build.gradle` em `neoForge { runs { ... } }`:

- **client** → abre cliente de desenvolvimento.
- **server** → abre servidor dedicado (`--nogui`).
- **gameTestServer** → ambiente de testes GameTest.
- **data** → geração de dados para `src/generated/resources`.

Comandos úteis:

```bash
./gradlew runClient
./gradlew runServer
./gradlew runGameTestServer
./gradlew runData
```

---

## 4. Estrutura de Pacotes / Módulos

## Convenções de pacote

Namespace raiz: `com.dreamsdimensions.mod`

Subpacotes principais:

- `attachment` → dados persistentes por entidade/jogador.
- `block` → implementações customizadas de blocos.
- `client` → eventos e telas client-only.
- `config` → configuração via `ModConfigSpec`.
- `event` → handlers de eventos de runtime.
- `item` → itens customizados com comportamento.
- `registry` → registros com `DeferredRegister`.
- `util` → utilitários de domínio (ex.: lógica de retorno).

## Justificativa arquitetural

- **Separação por responsabilidade** facilita manutenção e evolução.
- **Registro centralizado** em `registry` evita acoplamento com bootstrap.
- **Eventos desacoplados**: cada classe trata uma fatia da regra de negócio.
- **Dados de mundo em JSON** (data-driven), reduzindo hardcode.

## Padrões utilizados

- **Singleton utilitário** por classe final + construtor privado + métodos estáticos.
- **Event-driven** via event bus NeoForge.
- **Deferred registration** para conteúdo registrável.
- **Attachment-based state** para persistência de dados de retorno por jogador.

---

## 5. Componentes Principais

## 5.1 Blocos e Itens

### Blocos

Registrados em `ModBlocks`:

- Terreno onírico: `dream_grass_block`, `blue_dream_grass`, `dream_dirt_block`, `blue_dream_dirt`, `dream_sand_block`.
- Rochas/minérios: `serene_stone`, `blue_dream_stone`, `blue_dream_cobblestone`, `dream_ore`, `deepslate_dream_ore`, `dream_infused_stone`.
- Blocos especiais: `dream_shimmer_block`, `dream_glow_moss`, `oneiric_core_block`, etc.

Exemplo de registro (trecho simplificado):

```java
public static final DeferredBlock<Block> DREAM_ORE = BLOCKS.registerBlock(
    "dream_ore",
    (properties) -> new DreamOreBlock(properties, UniformInt.of(1, 3)),
    props -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
        .strength(3.0F, 3.0F)
        .requiresCorrectToolForDrops()
);
```

### Itens

Registrados em `ModItems`:

- `dream_dust`: item de material.
- `oneiric_awakener`: item ativo para “acordar” da dimensão de sonho.

Características do `oneiric_awakener`:

- `stacksTo(1)`
- raridade `RARE`
- cooldown base de 60 ticks
- uso canalizado de 60 ticks (animação tipo bow)

## 5.2 Dimensão Onírica

As dimensões são definidas por JSON em `data/dreamsdimensions/dimension/*.json`, com tipo em `dimension_type/*.json` e worldgen em `worldgen/*`.

Dimensões existentes:

- `dreamsdimensions:dreamscape`
- `dreamsdimensions:campo_onirico_azul`

A entrada ocorre em `SleepTeleportHandler`:

- valida que o jogador está no servidor e no Overworld,
- valida que está dormindo tempo suficiente,
- impede teleporte repetido no mesmo ciclo de sono,
- escolhe dimensão aleatória de uma lista fixa.

## 5.3 Lógica de teleporte e saída

### Entrada por sono

Em `SleepTeleportHandler.onPlayerTick(...)`:

1. escuta `PlayerTickEvent.Post`;
2. se dormiu `>= 100 ticks`, chama `actuallyTeleport`;
3. `actuallyTeleport` escolhe uma dimensão de sonho aleatória e teleporta para spawn seguro.

### Saída pelo item

`OneiricAwakenerItem.finishUsingItem(...)`:

1. garante execução no servidor;
2. valida se jogador está em dimensão onírica;
3. chama `DreamReturnHelper.buildReturnTransition(...)`;
4. se houver transição válida, teleporta e aplica cooldown;
5. se falhar, envia mensagem de erro ao jogador.

## 5.4 Listeners / Events

- `CommonEvents.onServerStarting` → log de inicialização do servidor.
- `DreamDimensionEffectsHandler.onPlayerTick` → aplica `Slow Falling` no `campo_onirico_azul`.
- `DreamReturnAttachmentHandler.onPlayerSetSpawn` → atualiza dados de cama no attachment.
- `SleepTeleportHandler.onPlayerTick` → teleporte por sono no Overworld.
- `ClientDimensionEvents` → registra tela de transição para fluxo `dreamscape -> overworld`.

## 5.5 Configurações e arquivos JSON

### Config runtime

`DreamsConfig` define `dream_dimensions` (lista de IDs configuráveis). O conjunto carregado é usado para validar “dimensão onírica”.

### Data-driven content

Arquivos importantes:

- `dimension/` e `dimension_type/`
- `worldgen/biome`, `worldgen/noise_settings`, `worldgen/configured_feature`, `worldgen/placed_feature`, `worldgen/biome_modifier`
- `recipe/`
- `loot_tables/`
- `tags/`

---

## 6. Fluxos de Uso

## Fluxo A — Entrar em dimensão onírica ao dormir

```text
Player -> Cama no Overworld -> SleepTeleportHandler (tick)
       -> validações (server/overworld/sleep>=100)
       -> escolha aleatória dreamscape|campo_onirico_azul
       -> teleport para spawn seguro
```

Pseudo-código:

```java
if (player.isServer && inOverworld && player.isSleepingLongEnough()) {
    if (firstTeleportInThisSleepCycle(player)) {
        teleportToRandomDreamDimension(player);
    }
}
```

## Fluxo B — Retornar com Oneiric Awakener

```text
Player usa item (60 ticks)
   -> valida que está em dimensão onírica
   -> resolve destino via DreamReturnHelper:
      1) cama salva
      2) respawn do jogador
      3) spawn global
   -> teleporte + cooldown
```

Pseudo-código:

```java
transition = DreamReturnHelper.buildReturnTransition(player);
if (transition.present()) {
    player.teleport(transition);
    player.applyCooldown(item);
}
```

## Fluxo C — Atualizar ponto de retorno

```text
Player define spawn no Overworld
   -> PlayerSetSpawnEvent
   -> DreamReturnAttachmentHandler grava posição/yaw em DreamReturnData
```

---

## 7. Diagramas de Arquitetura

## 7.1 Diagrama de pacotes

```text
com.dreamsdimensions.mod
├─ DreamsDimensions (bootstrap)
├─ config
│  └─ DreamsConfig
├─ registry
│  ├─ ModBlocks
│  ├─ ModItems
│  ├─ ModCreativeTabs
│  └─ ModAttachments
├─ event
│  ├─ SleepTeleportHandler
│  ├─ DreamReturnAttachmentHandler
│  ├─ DreamDimensionEffectsHandler
│  └─ CommonEvents
├─ attachment
│  └─ DreamReturnData
├─ item
│  ├─ OneiricAwakenerItem
│  └─ DreamDustItem
├─ block
│  └─ DreamOreBlock
├─ util
│  └─ DreamReturnHelper
└─ client
   ├─ event
   │  ├─ ClientModEvents
   │  └─ ClientDimensionEvents
   └─ screen
      └─ DreamTransitionScreen
```

## 7.2 Diagrama de dependências (conceitual)

```text
DreamsDimensions
  ├─ registra -> ModBlocks / ModItems / ModCreativeTabs / ModAttachments
  └─ assina -> CommonEvents / SleepTeleportHandler / Dream*Handlers

OneiricAwakenerItem
  └─ usa -> DreamReturnHelper
           ├─ usa -> DreamsConfig
           ├─ usa -> ModAttachments
           └─ lê -> DreamReturnData

SleepTeleportHandler
  └─ referencia -> IDs das dimensões + teleporte server-side
```

---

## 8. Teste, Build e Release

## Build

Comandos base:

```bash
./gradlew clean build
./gradlew classes
```

## Teste local em instância Minecraft

1. Rode `./gradlew runClient`.
2. Crie mundo de teste.
3. Defina cama no Overworld.
4. Durma por ~5s (100 ticks).
5. Valide teleporte para uma dimensão onírica.
6. Use `oneiric_awakener` por 60 ticks.
7. Valide retorno para cama/respawn/spawn global.

## Checklist de QA pré-release

- [ ] Mod carrega sem erro no log.
- [ ] Registros de blocos/itens/attachments sem conflito de IDs.
- [ ] Teleporte por sono ocorre uma vez por ciclo de sono.
- [ ] Retorno funciona para os 3 níveis de fallback.
- [ ] JSONs de dimensão/worldgen/receita válidos.
- [ ] Textos de idioma (`en_us`, `pt_br`) cobrindo itens/blocos/mensagens.
- [ ] `build` gerando JAR sem falhas.

---

## 9. Contribuindo

## Guidelines

1. Crie branch de feature/bugfix.
2. Faça commits pequenos e descritivos.
3. Mantenha padrão de separação por pacote e responsabilidade.
4. Evite lógica de gameplay em classe de registro.
5. Prefira conteúdo data-driven quando possível.

## Estilo de código

- Java com classes `final` para utilitários e construtor privado quando aplicável.
- Nomes de classes e membros seguindo padrão Java/NeoForge.
- Comentários objetivos em pontos de regra de negócio.
- Evite acoplamento direto entre client-only e server-only.

## Issues e Pull Requests

- Abra issue com contexto mínimo reproduzível.
- Em PR, descreva:
  - problema,
  - abordagem técnica,
  - impacto em gameplay,
  - validações realizadas.
- Sempre incluir evidências (logs, screenshots, vídeos curtos) para mudanças visuais/comportamentais.

---

## 10. Referências e Recursos

## Documentação oficial

- NeoForge Docs: <https://docs.neoforged.net/>
- ModDevGradle (NeoForge): <https://github.com/neoforged/ModDevGradle>
- Minecraft Wiki (Data Pack / Dimension JSON): <https://minecraft.wiki/>

## Tópicos úteis para este mod

- Registro com `DeferredRegister`
- Eventos (`NeoForge.EVENT_BUS` e MOD bus)
- Data Attachments (persistência por jogador)
- Dimension/biome/noise settings JSON
- Teleporte seguro com `TeleportTransition`

## Boas práticas gerais de modding

- Priorizar lógica server-side para regras de gameplay.
- Fazer fallback robusto para teleporte e estado inválido.
- Usar configurações para valores de ambiente (ex.: lista de dimensões oníricas).
- Reduzir hardcode em favor de recursos data-driven.

---

### Exemplo técnico adicional (realista)

Trecho de abordagem de retorno seguro em etapas:

```java
Optional<Vec3> bed = resolveBedStandUp(overworld, data);
if (bed.isPresent()) return target(bed.get());

try {
    TeleportTransition respawn = player.findRespawnPositionAndUseSpawnBlock(false, DO_NOTHING);
    return target(respawn.position());
} catch (Throwable ignored) {
    // fallback
}

return target(findSafeSpawn(overworld, worldSpawn));
```

Este padrão ajuda a evitar “soft lock” do jogador em posições inválidas ao acordar.
