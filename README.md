# Dreams Dimensions — Documentação Técnica Completa

## Sumário
- [1. Título e Informações do Projeto](#1-título-e-informações-do-projeto)
- [2. Visão Geral do Sistema](#2-visão-geral-do-sistema)
- [3. Instalação e Ambiente de Desenvolvimento](#3-instalação-e-ambiente-de-desenvolvimento)
- [4. Estrutura de Pacotes / Módulos](#4-estrutura-de-pacotes--módulos)
- [5. Componentes Principais](#5-componentes-principais)
- [6. Fluxos de Uso](#6-fluxos-de-uso)
- [7. Diagramas de Arquitetura](#7-diagramas-de-arquitetura)
- [8. Teste, Build e Release](#8-teste-build-e-release)
- [9. Contribuindo](#9-contribuindo)
- [10. Referências e Recursos](#10-referências-e-recursos)

---

## 1. Título e Informações do Projeto

**Nome do mod:** Dreams Dimensions  
**mod_id:** `dreamsdimensions`  
**Versão alvo do Minecraft:** `1.21.4`  
**API/Loader:** NeoForge (`21.4.124`) com plugin NeoGradle userdev (`7.0.173`)  
**Linguagem:** Java (toolchain Java 21)  
**Mappings:** Parchment (`minecraftVersion=1.21.4`, `mappingsVersion=2025.03.23`)  

### Objetivo do mod
O Dreams Dimensions adiciona dimensões oníricas acessadas pelo ato de dormir no Overworld e fornece uma mecânica de retorno seguro via item ritualístico (`oneiric_awakener`). O foco é oferecer progressão leve de exploração, ambientação diferenciada e ciclo “entrar no sonho → explorar → acordar”.

---

## 2. Visão Geral do Sistema

### O que o mod faz em alto nível
1. Registra blocos, itens, aba criativa e data attachments no ciclo de inicialização do mod.
2. Monitora jogadores dormindo no Overworld e, após 100 ticks, teleporta para uma dimensão onírica aleatória.
3. Armazena dados de retorno (cama no Overworld + yaw) para permitir teleporte de volta seguro.
4. Permite retorno usando o item **Despertador Onírico** após canalização (60 ticks).
5. Aplica efeitos específicos por dimensão (ex.: *Slow Falling* em `campo_onirico_azul`).

### Componentes principais
- **Bootstrap do mod**: `DreamsDimensions`.
- **Registro de conteúdo**: `ModBlocks`, `ModItems`, `ModCreativeTabs`, `ModAttachments`.
- **Eventos de runtime**: `SleepTeleportHandler`, `DreamReturnAttachmentHandler`, `DreamDimensionEffectsHandler`, `CommonEvents`.
- **Retorno seguro**: `DreamReturnHelper` + `DreamReturnData`.
- **Cliente**: `ClientModEvents`, `ClientDimensionEvents`, `DreamTransitionScreen`.
- **Data packs do mod**: dimensões, tipos de dimensão, biomas, noise settings, worldgen, receitas, loot tables, tags.

### Diagrama conceitual (alto nível)

```text
[Player dorme no Overworld]
          |
          v
[SleepTeleportHandler conta ticks]
          | >= 100 ticks
          v
[Escolhe dimensão onírica aleatória]
          |
          v
[Teleporta para dreamscape/campo_onirico_azul]
          |
          +--> [DreamDimensionEffectsHandler aplica efeitos por dimensão]
          |
          v
[Player usa OneiricAwakener (60 ticks)]
          |
          v
[DreamReturnHelper resolve destino de retorno]
          |
          v
[Teleporte para Overworld: cama > respawn pessoal > spawn global]
```

---

## 3. Instalação e Ambiente de Desenvolvimento

### Requisitos
- **JDK 21** (obrigatório para Minecraft 1.21.4).
- **Gradle Wrapper** do projeto (`./gradlew`).
- IDE recomendada:
  - IntelliJ IDEA (com suporte a Gradle), ou
  - Eclipse (suportado pelo script), ou
  - VS Code com extensões Java + Gradle.

### Passo a passo para importar o projeto
1. Clone o repositório.
2. Abra a pasta raiz (`dreamsdimensions`) na IDE.
3. Aguarde o sync do Gradle.
4. Garanta que a IDE está usando **Java 21**.
5. Execute as tarefas de setup/run conforme necessidade.

### Plugins/extensões úteis
- **IntelliJ**: Minecraft Development (opcional), Gradle, Lombok (não obrigatório neste projeto), Java.
- **VS Code**: Extension Pack for Java + Gradle for Java.

### Run configurations (cliente/servidor)
O `build.gradle` já define os runs:
- `client`
- `server` (`--nogui`)
- `gameTestServer`
- `clientData` (data generation)

Comandos típicos:
```bash
./gradlew runClient
./gradlew runServer
./gradlew runGameTestServer
./gradlew runClientData
```

---

## 4. Estrutura de Pacotes / Módulos

### Convenções de pacote
Base package: `com.dreamsdimensions.mod`.

Subpacotes principais:
- `attachment`: estado persistente de player para retorno.
- `block`: implementações custom de bloco (ex.: minério com XP).
- `client.event` / `client.screen`: lógica client-only.
- `config`: configuração com `ModConfigSpec`.
- `event`: listeners de runtime no `NeoForge.EVENT_BUS`.
- `item`: itens customizados e seus comportamentos.
- `registry`: `DeferredRegister` para blocos/itens/attachments/creative tab.
- `util`: helpers de domínio (teleporte de retorno).

### Organização arquitetural e justificativa
- **Separação por responsabilidade**: registro, regras de jogo, client, utilitários e dados persistentes estão isolados.
- **Registro declarativo via DeferredRegister**: reduz risco de ordem incorreta de inicialização.
- **Event-driven design**: comportamento emergente do jogo implementado via listeners de eventos do NeoForge.
- **Sem DI framework**: construção simples orientada ao ciclo do loader (padrão comum em mods).

### Padrões usados
- **Singleton estático de registries** (`ModBlocks`, `ModItems` etc.).
- **Data attachment** com codec para persistência (`DreamReturnData`).
- **Event subscribers e listeners** no MOD bus e no runtime bus.

---

## 5. Componentes Principais

### 5.1 Blocos e Itens

#### Blocos
- `DreamOreBlock`: minério que concede XP ao ser minerado.
- Blocos de terreno onírico (`dream_grass_block`, `blue_dream_grass`, `serene_stone`, etc.).
- Blocos utilitários/crafting (`dream_infused_stone`, `oneiric_core_block`).

Exemplo real de registro de bloco com `DeferredRegister`:

```java
public static final DeferredBlock<Block> DREAM_SHIMMER_BLOCK = BLOCKS.registerSimpleBlock(
    "dream_shimmer_block",
    BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_BLOCK)
        .mapColor(MapColor.COLOR_LIGHT_BLUE)
        .strength(1.5F, 6.0F)
        .requiresCorrectToolForDrops()
);
```

#### Itens
- `DreamDustItem`: recurso base de progressão.
- `OneiricAwakenerItem`: item de retorno do sonho.

Exemplo simplificado do fluxo do `OneiricAwakenerItem`:

```java
if (!DreamsConfig.isDreamDimension(level.dimension())) return PASS;
if (player.getCooldowns().isOnCooldown(stack)) return FAIL;
player.startUsingItem(hand); // canaliza 60 ticks

// ao finalizar uso no servidor:
DreamReturnHelper.buildReturnTransition(serverPlayer)
    .ifPresent(serverPlayer::teleport);
```

### 5.2 Dimensão Onírica

As dimensões são definidas por JSON em `data/dreamsdimensions/dimension/*.json` e conectadas a:
- `dimension_type/*.json`
- `worldgen/biome/*.json`
- `worldgen/noise_settings/*.json`

Dimensões atuais:
- `dreamsdimensions:dreamscape`
- `dreamsdimensions:campo_onirico_azul`

**Registro/lógica de entrada**: não via código de `LevelStem` em Java, mas por datapack JSON + teleporte por evento de sono.

### 5.3 Teleporte e saída

- **Entrada no sonho**: `SleepTeleportHandler` monitora `PlayerTickEvent.Post` e teleporta após 100 ticks dormindo no Overworld.
- **Saída**: `OneiricAwakenerItem.finishUsingItem` chama `DreamReturnHelper`.
- **Prioridade de retorno**:
  1. Cama original no Overworld (attachment).
  2. Respawn pessoal do jogador.
  3. Spawn global do Overworld.

### 5.4 Listeners / Eventos
- `CommonEvents.onServerStarting`: log de boot.
- `SleepTeleportHandler.onPlayerTick`: entrada na dimensão de sonho.
- `DreamReturnAttachmentHandler.onPlayerSetSpawn`: captura cama/respawn no Overworld.
- `DreamDimensionEffectsHandler.onPlayerTick`: aplica efeitos no `campo_onirico_azul`.
- `ClientDimensionEvents`: tela de transição ao entrar/sair de dimensão.
- `ClientModEvents`: configura camadas de render.

### 5.5 Configurações e JSON

#### Config (`DreamsConfig`)
- Chave principal: `dream_dimensions` (lista de `ResourceLocation` válidas).
- Eventos de load/reload “bake” para `Set<ResourceKey<Level>>` em memória.

#### Data-driven assets
- `data/dreamsdimensions/dimension/*`
- `data/dreamsdimensions/dimension_type/*`
- `data/dreamsdimensions/worldgen/*`
- `data/dreamsdimensions/recipe/*`
- `data/dreamsdimensions/loot_tables/*`
- `assets/dreamsdimensions/*` (modelos, blockstates, texturas, lang)

---

## 6. Fluxos de Uso

### Fluxo A — Jogador entra no sonho ao dormir

```text
Player -> Bed: dormir
PlayerTickEvent.Post -> SleepTeleportHandler: incrementa contador
SleepTeleportHandler -> SleepTeleportHandler: contador >= 100?
alt sim
  SleepTeleportHandler -> ServerLevel(dream): findSafeSpawnLocation
  SleepTeleportHandler -> Player: stopSleeping
  SleepTeleportHandler -> Player: teleportTo(dreamscape/campo_onirico_azul)
end
```

### Fluxo B — Jogador usa Oneiric Awakener para retornar

```text
Player -> OneiricAwakenerItem.use: valida dimensão e cooldown
OneiricAwakenerItem -> Player: startUsingItem (60 ticks)
Player -> OneiricAwakenerItem.finishUsingItem: completa canalização
OneiricAwakenerItem -> DreamReturnHelper: buildReturnTransition
DreamReturnHelper -> Overworld: resolve destino (cama > respawn > spawn global)
OneiricAwakenerItem -> Player: teleport + mensagem + cooldown
```

### Fluxo C — Atualização de ponto de retorno

```text
PlayerSetSpawnEvent -> DreamReturnAttachmentHandler
DreamReturnAttachmentHandler -> DreamReturnData: setOverworldBed(pos, yaw)
```

---

## 7. Diagramas de Arquitetura

### 7.1 Diagrama de pacotes

```text
com.dreamsdimensions.mod
├── DreamsDimensions (bootstrap)
├── attachment
│   └── DreamReturnData
├── block
│   └── DreamOreBlock
├── client
│   ├── event (ClientModEvents, ClientDimensionEvents)
│   └── screen (DreamTransitionScreen)
├── config
│   └── DreamsConfig
├── event
│   ├── CommonEvents
│   ├── SleepTeleportHandler
│   ├── DreamReturnAttachmentHandler
│   └── DreamDimensionEffectsHandler
├── item
│   ├── DreamDustItem
│   └── OneiricAwakenerItem
├── registry
│   ├── ModBlocks
│   ├── ModItems
│   ├── ModCreativeTabs
│   └── ModAttachments
└── util
    └── DreamReturnHelper
```

### 7.2 Diagrama de dependências (conceitual)

```text
DreamsDimensions
 ├─> ModBlocks / ModItems / ModCreativeTabs / ModAttachments
 ├─> DreamsConfig
 └─> NeoForge.EVENT_BUS listeners
      ├─> SleepTeleportHandler ----> dimension JSONs (ids)
      ├─> DreamReturnAttachmentHandler --> ModAttachments.DREAM_RETURN
      └─> DreamDimensionEffectsHandler

OneiricAwakenerItem
 ├─> DreamsConfig (validação de dimensão)
 └─> DreamReturnHelper
      └─> DreamReturnData attachment
```

---

## 8. Teste, Build e Release

### Build
```bash
./gradlew clean build
```

### Teste local
- Cliente dev:
  ```bash
  ./gradlew runClient
  ```
- Servidor dev:
  ```bash
  ./gradlew runServer
  ```

### Checklist de QA pré-release
1. Abrir jogo com mod carregado sem erros no log de bootstrap.
2. Validar registro de blocos/itens e creative tab.
3. Dormir no Overworld por 100 ticks e confirmar teleporte.
4. Testar uso do `oneiric_awakener` em cada dimensão onírica.
5. Confirmar fallback de retorno (cama inválida, sem respawn, spawn global).
6. Verificar receitas, loot e geração de minério no Overworld.
7. Validar assets (model, blockstate, textura e tradução `pt_br`/`en_us`).

---

## 9. Contribuindo

### Guidelines de contribuição
1. Crie branch de feature/fix (`feat/...`, `fix/...`).
2. Mantenha commits pequenos e descritivos.
3. Abra PR com:
   - objetivo,
   - impacto em gameplay,
   - como testar,
   - riscos/migrações.

### Estilo de código
- Seguir padrão Java do projeto (classes `final` utilitárias com construtor privado quando apropriado).
- Prefira nomes explícitos para handlers e registries.
- Evite lógica client-only em código comum de servidor.
- Use APIs recomendadas do NeoForge (`DeferredRegister`, event bus, attachments).

### Issues / Pull Requests
- Issue deve conter contexto, comportamento esperado, comportamento atual e passos de reprodução.
- PR deve referenciar issue quando aplicável e incluir checklist de testes executados.

---

## 10. Referências e Recursos

### Documentação oficial
- NeoForge Docs: https://docs.neoforged.net/
- NeoGradle: https://github.com/neoforged/NeoGradle
- ParchmentMC: https://parchmentmc.org/docs/getting-started
- Minecraft Wiki (formato datapack/worldgen): https://minecraft.wiki/

### Tópicos relevantes para este mod
- Deferred Register e registries do NeoForge.
- Event bus (MOD bus vs runtime bus).
- Data Attachments para estado persistente de entidades.
- Data-driven dimensions/biomes/noise settings via JSON.

### Boas práticas de modding (Fabric/Forge/NeoForge)
Embora este projeto use **NeoForge**, algumas práticas são universais no ecossistema (incluindo Fabric):
- separar conteúdo data-driven (JSON) de regra de negócio em código;
- manter lógica autoritativa no servidor;
- isolar código client-only;
- registrar conteúdo no momento correto do lifecycle;
- tratar teleporte e spawn com fallback seguro para evitar soft-lock.

---

## Apêndice — Comandos úteis

```bash
# Build completo
./gradlew clean build

# Rodar cliente dev
./gradlew runClient

# Rodar servidor dev
./gradlew runServer

# Gerar dados (se aplicável)
./gradlew runClientData
```
