# Auditoria técnica — TerraBlender NeoForge 1.21.11 (SDK)

> Escopo auditado: `ContextoIA/TerraBlender-neoforge-1.21.11-21.11.0.0/`.
> Observação: o conteúdo está distribuído majoritariamente em `.class`; a análise de fluxo foi feita por inspeção de metadados (`javap`) + arquivos JSON/TOML/META.

## A) INVENTÁRIO

### Árvore relevante (resumida)

- `META-INF/`
  - `neoforge.mods.toml`
  - `accesstransformer.cfg`
  - `MANIFEST.MF`
- `terrablender/` (bytecode)
  - `core/` (`TerraBlender.class`, `TerraBlenderNeoForge.class`)
  - `api/` (`Region`, `Regions`, `RegionType`, `SurfaceRuleManager`, `EndBiomeRegistry`, builders de parâmetros)
  - `worldgen/` (`DefaultOverworldRegion`, `DefaultNetherRegion`, `RegionUtils`, `TBSurfaceRuleData`, interfaces estendidas)
  - `worldgen/noise/` (camada de unicidade/região: `Area`, `BiomeInitialLayer`, `LayeredNoiseUtil`, etc.)
  - `worldgen/surface/` (`NamespacedSurfaceRuleSource`)
  - `mixin/` (mixins em biome source/chunk generator/noise settings/registries/end)
  - `handler/` (`InitializationHandler`)
  - `util/` (`LevelUtils`, weighted utils)
- raiz:
  - `terrablender.mixins.json`
  - `terrablender_neoforge.mixins.json`
  - `terrablender.accesswidener`
  - `pack.mcmeta`
- `data/terrablender/`
  - `tags/dimension_type/overworld_regions.json`
  - `tags/dimension_type/nether_regions.json`
  - `worldgen/biome/deferred_placeholder.json`
- `assets/terrablender/lang/*.json`

### Classificação pedida

- **[1] API TerraBlender (core)**
  - `terrablender/api/*` (Region/Regions/SurfaceRuleManager/EndBiomeRegistry/...)
  - `terrablender/worldgen/*` (RegionUtils, TBSurfaceRuleData, interfaces)
  - `terrablender/core/*`
- **[2] Exemplo/template de integração**
  - `DefaultOverworldRegion.class` e `DefaultNetherRegion.class` (implementações de region padrão)
  - `ModifiedVanillaOverworldBuilder`, `VanillaParameterOverlayBuilder`
- **[3] Assets/data de demonstração**
  - `data/terrablender/worldgen/biome/deferred_placeholder.json`
  - `data/terrablender/tags/dimension_type/*.json`
  - `assets/terrablender/lang/*.json`
- **[4] Infra (mixins, accesswidener, meta)**
  - `terrablender.mixins.json`, `terrablender_neoforge.mixins.json`
  - `META-INF/accesstransformer.cfg`
  - `terrablender.accesswidener`
  - `META-INF/neoforge.mods.toml`

---

## B) PONTOS-CHAVE

### 1) Registro de Region

- Classe base: `terrablender.api.Region`.
  - Construtor com `(Identifier name, RegionType type, int weight)`.
  - `addBiomes(Registry<Biome>, Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>>)` é o ponto principal de injeção.
  - Helpers: `addBiome(...)`, `addBiomeSimilar(...)`, `addModifiedVanillaOverworldBiomes(...)`.
- Tipos de region encontrados: `RegionType.OVERWORLD` e `RegionType.NETHER`.
- Regiões padrão internas:
  - `terrablender.worldgen.DefaultOverworldRegion` (usa `OverworldBiomeBuilder.addBiomes(...)`).
  - `terrablender.worldgen.DefaultNetherRegion` (mapeia parâmetros climáticos para biomas nether vanilla).

### 2) Registro no mod / bootstrap

- Classe de entrada NeoForge: `terrablender.core.TerraBlenderNeoForge` (`@Mod("terrablender")`).
- Ela registra listener em `NeoForge.EVENT_BUS` com prioridade LOWEST para `ServerAboutToStartEvent`, chamando `InitializationHandler.onServerAboutToStart(...)`.
- `InitializationHandler` delega para `LevelUtils.initializeOnServerStart(server)`.
- Registro de regions é centralizado em `terrablender.api.Regions`:
  - `register(Identifier, Region)`
  - `register(Identifier, int index, Region)`
  - `register(Region)`
- No `static {}` de `Regions`, TerraBlender registra suas regions vanilla por config:
  - `DefaultOverworldRegion(vanillaOverworldRegionWeight)`
  - `DefaultNetherRegion(vanillaNetherRegionWeight)`

### 3) Seleção de biomas (climate/noise/parameter points)

- A lógica de resolução é acoplada por mixin em `Climate.ParameterList` e `MultiNoiseBiomeSource`:
  - `MixinParameterList` adiciona:
    - `initializeForTerraBlender(registryAccess, regionType, seed)`
    - `findValuePositional(targetPoint, x, y, z)`
    - árvore por region (`uniqueTrees`) + ruído de unicidade (`Area uniqueness`).
  - `MixinMultiNoiseBiomeSource` intercepta `getNoiseBiome(...)` (HEAD, cancellable) e resolve via `findValuePositional(...)`.
- `RegionUtils` oferece cache/mapeamento dos parameter points vanilla (`getVanillaParameterPoints`).
- `Region.DEFERRED_PLACEHOLDER` + biome JSON `deferred_placeholder` fornecem fallback para pontos diferidos.

### 4) Surface rules

- API principal: `terrablender.api.SurfaceRuleManager`:
  - `addSurfaceRules(category, namespace, ruleSource)`
  - `addToDefaultSurfaceRulesAtStage(...)`
  - `setDefaultSurfaceRules(...)`
  - `getNamespacedRules(...)`
- `TBSurfaceRuleData` contém builders padrão (`overworld()`, `overworldLike(...)`, `nether()`, `end()`, `air()`).
- `MixinNoiseGeneratorSettings` intercepta `surfaceRule()` e troca retorno por regras namespaced (`SurfaceRuleManager.getNamespacedRules(...)`) quando `ruleCategory` foi setada.

### 5) Mixins pedidos

- `terrablender.mixins.json` ativa mixins em:
  - `BiomeSource`, `ChunkGenerator`, `MultiNoiseBiomeSource`, `NoiseBasedChunkGenerator`, `NoiseGeneratorSettings`, `Climate.ParameterList`, `PrimaryLevelData`, `TheEndBiomeSource`, `BuiltInRegistries` + access interface `MultiNoiseBiomeSourceAccess`.
- `terrablender_neoforge.mixins.json` está vazio (sem mixins adicionais específicos NeoForge).

Função prática por alvo:
- `MixinNoiseBasedChunkGenerator`: `@ModifyArg` em `doCreateBiomes`, antes de `ChunkAccess.fillBiomesFromNoise(...)`, clonando biome source/parameter list para recriar unicidade por chunk.
- `MixinMultiNoiseBiomeSource`: injeta em `getNoiseBiome(...)` e usa seleção posicional por region.
- `MixinParameterList`: mantém árvores de busca por region e resolve fallback quando sai `DEFERRED_PLACEHOLDER`.
- `MixinNoiseGeneratorSettings`: compõe surface rules por namespace/categoria.
- `MixinTheEndBiomeSource`: injeta coleta/seleção de biomas do End com áreas ponderadas.
- `MixinBuiltInRegistries`: intercepta registro de `Registries.MATERIAL_RULE` e registra codec `terrablender:merged` (`NamespacedSurfaceRuleSource.CODEC`).
- `MixinPrimaryLevelData`: força lifecycle estável (remove bloqueio de experimental warning para esse fluxo).
- `MixinChunkGenerator`: cancela `validate`.
- `MixinBiomeSource`: permite append de biomas diferidos na lista de possíveis.

### 6) Data pack

- Há datapack interno mínimo:
  - `data/terrablender/tags/dimension_type/overworld_regions.json` -> `minecraft:overworld`
  - `data/terrablender/tags/dimension_type/nether_regions.json` -> `minecraft:the_nether`
  - `data/terrablender/worldgen/biome/deferred_placeholder.json`
- Conclusão: TerraBlender **não é só datapack**. O núcleo de integração é código+mixins; datapack é suporte (tags/fallback biome), não mecanismo principal de injeção.

---

## C) PIPELINE (diagrama textual obrigatório)

1. **Boot do mod TerraBlender**
   - NeoForge instancia `TerraBlenderNeoForge` (`@Mod`).
   - Config `terrablender.toml` é carregada e aplicada em `TerraBlender.setConfig(...)`.

2. **Inicialização de registries internos TerraBlender**
   - `Regions` static init cria mapas por `RegionType`.
   - Registra regions vanilla padrão (overworld/nether) com pesos vindos da config.

3. **Hook de infraestrutura de geração**
   - Mixins são aplicados em classes vanilla de worldgen.
   - `MixinBuiltInRegistries` injeta codec de regra de superfície namespaced (`terrablender:merged`) no registry material rule.

4. **Antes do worldgen efetivo por mundo/chunk**
   - Em criação de biomas do chunk (`NoiseBasedChunkGenerator#doCreateBiomes`), TerraBlender substitui arg para usar clone do biome source com unicidade recriada.

5. **Resolução de bioma por ponto climático**
   - `MultiNoiseBiomeSource#getNoiseBiome` é interceptado.
   - `ParameterList.findValuePositional(...)` consulta ruído de unicidade -> escolhe árvore da region -> busca `Climate.RTree` para retornar biome holder.
   - Se cair no `DEFERRED_PLACEHOLDER`, fallback para árvore base (índice 0 / vanilla base region).

6. **Surface rules no ciclo de noise settings**
   - Ao ler `NoiseGeneratorSettings.surfaceRule()`, mixin aplica composição de regras namespaced por categoria (`OVERWORLD/NETHER/...`) via `SurfaceRuleManager`.

7. **Eventos de servidor**
   - Em `ServerAboutToStartEvent`, `InitializationHandler` chama `LevelUtils.initializeOnServerStart(server)` para sincronizar estado runtime do TerraBlender com o servidor carregado.

---

## D) RELATÓRIO — responsabilidades por arquivo/classe

### Núcleo
- `terrablender/core/TerraBlenderNeoForge.class`
  - Entry-point NeoForge, carrega config e registra listener de servidor.
- `terrablender/handler/InitializationHandler.class`
  - Gancho de server start para inicialização de utilitários runtime.
- `terrablender/api/Regions.class`
  - Registry de regions por tipo, índices e logging.
- `terrablender/api/Region.class`
  - Contrato para injeção de biomas (override `addBiomes`).
- `terrablender/api/RegionType.class`
  - Tipos suportados nesta build: OVERWORLD/NETHER.

### Biome mapping / climate
- `terrablender/mixin/MixinParameterList.class`
  - Coração da seleção posicional por region (árvores + ruído de unicidade).
- `terrablender/mixin/MixinMultiNoiseBiomeSource.class`
  - Troca resolução padrão por resolução TerraBlender no `getNoiseBiome`.
- `terrablender/worldgen/RegionUtils.class`
  - Cache de parameter points vanilla para operações como `addBiomeSimilar`.

### Surface rules
- `terrablender/api/SurfaceRuleManager.class`
  - API para registrar/injetar regras de superfície namespaced.
- `terrablender/worldgen/TBSurfaceRuleData.class`
  - Regras padrão (overworld/nether/end).
- `terrablender/mixin/MixinNoiseGeneratorSettings.class`
  - Hook de composição final em `surfaceRule()`.
- `terrablender/worldgen/surface/NamespacedSurfaceRuleSource.class`
  - RuleSource custom serializável via codec (`terrablender:merged`).

### End
- `terrablender/api/EndBiomeRegistry.class`
  - Registries ponderados de highlands/midlands/edge/islands.
- `terrablender/mixin/MixinTheEndBiomeSource.class`
  - Hook de coleta e seleção dos biomas do End.

### Pontos de extensão (o que customizar)
1. Criar classe que estende `Region` e sobrescreve `addBiomes`.
2. Registrar essa region com `Regions.register(...)` no bootstrap do seu mod.
3. (Opcional) adicionar surface rules com `SurfaceRuleManager.addSurfaceRules(...)`.
4. (Opcional End) registrar biomas em `EndBiomeRegistry`.

### Pontos de falha (mais comuns)
1. **Region nunca registrada** (sem chamada a `Regions.register`).
2. **Biome key ausente no registry/data pack** (resource key não existe).
3. **Parâmetros climáticos estreitos demais** (spawn prático ~0).
4. **Tags/dimension type incompatíveis** para target dimension.
5. **Mixins não carregando** (problemas em `mods.toml` / versões).
6. **Surface rule sem category correta** -> regra não aplicada.

---

## E) GUIA APLICADO AO DREAMSDIMENSIONS (NeoForge 1.21.11)

## 1) Estrutura recomendada

```text
src/main/java/com/dreamsdimensions/mod/worldgen/terrablender/
  DreamsTerraBlender.java                # bootstrap/register
  DreamsOverworldRegion.java             # Region custom

src/main/java/com/dreamsdimensions/mod/worldgen/surface/
  LuminaHollowsSurfaceRules.java         # opcional, se usar SurfaceRuleManager

src/main/resources/data/dreamsdimensions/worldgen/
  biome/lumina_hollows.json              # já existe no projeto
  (opcional) noise_settings/...          # se você alterar dimensão/noise
```

## 2) Código mínimo (1 bioma raro no Overworld)

> Exemplo mínimo essencial usando apenas APIs encontradas na build auditada.

```java
package com.dreamsdimensions.mod.worldgen.terrablender;

import com.dreamsdimensions.mod.DreamsDimensions;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import terrablender.api.ParameterUtils;
import terrablender.api.Region;
import terrablender.api.RegionType;

import java.util.function.Consumer;
import com.mojang.datafixers.util.Pair;
import net.minecraft.world.level.biome.Climate;

public final class DreamsOverworldRegion extends Region {
    public static final Identifier REGION_ID = Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "overworld_region");
    public static final ResourceKey<Biome> LUMINA_HOLLOWS = ResourceKey.create(
            Registries.BIOME,
            Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "lumina_hollows")
    );

    public DreamsOverworldRegion(int weight) {
        super(REGION_ID, RegionType.OVERWORLD, weight);
    }

    @Override
    public void addBiomes(Registry<Biome> registry,
                          Consumer<Pair<Climate.ParameterPoint, ResourceKey<Biome>>> mapper) {
        // Exemplo: clima frio/seco + inland + weirdness alta.
        // Ajuste fino depois com debug.
        this.addBiome(
                mapper,
                ParameterUtils.Temperature.COOL,
                ParameterUtils.Humidity.DRY,
                ParameterUtils.Continentalness.INLAND,
                ParameterUtils.Erosion.EROSION_2,
                ParameterUtils.Weirdness.HIGH_SLICE_VARIANT_ASCENDING,
                ParameterUtils.Depth.SURFACE,
                0.0F,
                LUMINA_HOLLOWS
        );
    }
}
```

```java
package com.dreamsdimensions.mod.worldgen.terrablender;

import com.dreamsdimensions.mod.DreamsDimensions;
import net.minecraft.resources.Identifier;
import terrablender.api.Regions;

public final class DreamsTerraBlender {
    private static boolean done;

    private DreamsTerraBlender() {}

    public static void register() {
        if (done) return;
        done = true;

        Regions.register(new DreamsOverworldRegion(2)); // weight baixo = raro
        DreamsDimensions.LOGGER.info("[TerraBlender] Region registrada: {}", DreamsOverworldRegion.REGION_ID);
    }
}
```

No `DreamsDimensions#commonSetup`:

```java
private void commonSetup(final FMLCommonSetupEvent event) {
    event.enqueueWork(() -> {
        DreamsTerraBlender.register();
    });
}
```

## 3) Controle de raridade

- **Weight da Region** (no construtor / `Regions.register`) controla “presença relativa” da sua region versus outras.
- Para “raro mas existente”:
  - Comece com `weight` entre **1 e 3**.
  - Use faixas climáticas **não ultra estreitas** (evite combinar extremos em 6 dimensões de uma vez).
- Regra prática:
  - primeiro faça aparecer com faixa ampla,
  - depois estreite gradualmente (temperature/humidity/continentalness/erosion/weirdness/depth).

## 4) Debug checklist (comandos + logs)

1. **Confirmar registro da Region no init**
   - log explícito após `Regions.register(...)`.
2. **Confirmar que biome key existe**
   - arquivo `data/dreamsdimensions/worldgen/biome/lumina_hollows.json` presente e válido.
3. **Conferir que TerraBlender carregou mixins**
   - validação de carregamento de `terrablender.mixins.json` no boot.
4. **Inspecionar se parâmetros estão estreitos demais**
   - abra temporariamente o range para validar spawn.
5. **Conferir dimension tags do TB**
   - `data/terrablender/tags/dimension_type/overworld_regions.json` aponta para `minecraft:overworld`.
6. **Verificar ciclo correto de registro**
   - manter `DreamsTerraBlender.register()` no `commonSetup` com `enqueueWork`.
7. **Em mundo de teste**
   - gerar seed novo (evitar chunks já pregenerated), voar longas distâncias e usar modo espectador para inspeção.

---

## Observações específicas para o repositório atual (DreamsDimensions)

- Há classes placeholder vazias para integração TerraBlender:
  - `DDTerraBlender`, `DDOverworldRegion`, `DDBiomes`, `LuminaHollowsSurfaceRules`.
- Ou seja: o “esqueleto” já existe, mas a integração ainda não foi implementada nessas classes.

