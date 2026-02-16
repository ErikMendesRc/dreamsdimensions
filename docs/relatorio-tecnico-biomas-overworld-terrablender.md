# Relatório técnico — pipeline de biomas Overworld (NeoForge 1.21.11 + TerraBlender)

## Escopo e fonte de verdade usadas
- **Biomes O' Plenty (BOP)** no diretório `ContextoIA/BiomesOPlenty-neoforge-1.21.11-21.11.0.28` (análise por bytecode/classes e datapack).
- **TerraBlender 1.21.11-21.11.0.0** no diretório `ContextoIA/TerraBlender-neoforge-1.21.11-21.11.0.0` (API real por classes).
- A auditoria informada em `/mnt/data/Auditoria-TerraBlender.txt` **não estava disponível neste ambiente**.

## TAREFA A — Levantamento real do pipeline do BOP

### 1) Como o BOP registra biomas e injeta no Overworld
- **Bioma em datapack JSON**: BOP mantém os biomas em `data/biomesoplenty/worldgen/biome/*.json` (atributos/effects/carvers/features/spawners).
- **Bootstrap/registro**: BOP chama `ModBiomes.bootstrapBiomes(BootstrapContext<Biome>)` para registrar `ResourceKey<Biome>` com os builders de biome.
- **Inserção no Overworld via TerraBlender Region**:
  - `ModBiomes.setupTerraBlender()` chama `Regions.register(...)` para múltiplas regions (`BOPOverworldRegionPrimary`, `BOPOverworldRegionSecondary`, `BOPOverworldRegionRare`, etc.).
  - Cada `Region` sobrescreve `addBiomes(...)` e delega para builders climáticos (`BOPOverworldBiomeBuilder` / `BOPRareOverworldBiomeBuilder`) que montam `Climate.ParameterPoint`.
- **Lifecycle no NeoForge**:
  - `BiomesOPlentyNeoForge.commonSetup(FMLCommonSetupEvent)` usa `event.enqueueWork(...)`.
  - Dentro do `enqueueWork`, chama `BiomesOPlenty.setupTerraBlender()` (que chama `ModBiomes.setupTerraBlender()`).

### 2) Surface rules, parâmetros climáticos e raridade no BOP
- **Surface rules**: BOP possui `BOPSurfaceRuleData` com regras customizadas por biome/dimensão (`overworld()`, `nether()`, `end()`), pronto para integração com TerraBlender.
- **Parâmetros climáticos**:
  - BOP utiliza helpers do vanilla/TB com `Climate.Parameter` e `Climate.parameters(...)` para montar points.
  - Estrutura padrão: temperature + humidity + continentalness + erosion + weirdness + depth + offset.
  - O builder separa ranges comuns e raros (`COMMON_RARENESS_RANGE = span(-1.0, 0.35)` e `RARE_RARENESS_RANGE = span(0.35, 1.0)`).
- **Controle de raridade/peso**:
  - BOP usa **dois mecanismos em conjunto**:
    1. **weight da Region** no construtor `new Region(..., weight)` (equilíbrio global entre regiões).
    2. **faixa climática mais estreita** (especialmente em region “rare”).

### 3) Padrões concretos observados (BOP)
- Classes-chave:
  - `biomesoplenty.init.ModBiomes` → `setupTerraBlender()` + `bootstrapBiomes(...)`
  - `biomesoplenty.worldgen.BOPOverworldRegionPrimary`
  - `biomesoplenty.worldgen.BOPOverworldRegionSecondary`
  - `biomesoplenty.worldgen.BOPOverworldRegionRare`
  - `biomesoplenty.biome.BOPOverworldBiomeBuilder`
  - `biomesoplenty.biome.BOPRareOverworldBiomeBuilder`
  - `biomesoplenty.worldgen.BOPSurfaceRuleData`
  - `biomesoplenty.neoforge.core.BiomesOPlentyNeoForge`
- Onde registra region:
  - `ModBiomes.setupTerraBlender()` -> `Regions.register(new BOPOverworldRegionPrimary(weight))`, etc.
- Garantia de entrada no biome source:
  - O caminho oficial é o mapeamento de `Climate.ParameterPoint` em `Region#addBiomes(...)`.

## Checklist mínimo para criar bioma próprio no Overworld (1.21.11)
1. Criar biome datapack completo (`worldgen/biome/*.json`) com effects/carvers/features/spawners válidos.
2. Criar classe `Region` TerraBlender (`RegionType.OVERWORLD`) e sobrescrever `addBiomes(...)`.
3. Adicionar `addBiome(...)` com parâmetros climáticos **não extremos** para primeiro teste.
4. Registrar a region em `FMLCommonSetupEvent` com `event.enqueueWork(() -> Regions.register(...))`.
5. Manter `neoforge:add_features` para flora/decorações no bioma (pipeline complementar válido).
6. Testar em **mundo novo** (biome source é gerado no world creation).

## Checklist para bioma raríssimo (sem “sumir”)
1. Primeiro validar com perfil amplo (temperatura/humidade/erosão/continentalness abrangentes).
2. Só depois estreitar faixas (ex.: continentalness mid/far inland + weirdness slice específico + erosão específica).
3. Não deixar simultaneamente: faixa climática estreita **e** region weight muito baixo no início.
4. Confirmar logs de `register()` e de `addBiomes()` antes de concluir que “não spawnou”.

## Diferença prática: `neoforge:add_biomes` vs TerraBlender Region
- **`neoforge:add_biomes`**:
  - Injeta biome por climate point em biome modifiers.
  - É útil para casos simples/rápidos, mas menos alinhado ao padrão de mods de bioma grandes.
- **TerraBlender Region**:
  - É o pipeline padrão de mods como BOP para Overworld/Nether/End com controle fino de clima, raridade e interoperabilidade.
  - Centraliza estratégia de inserção no biome source multi-noise.

## Quando usar cada um
- Use **TerraBlender Region** como **fonte de verdade** para biomas novos de Overworld em mod de biomas.
- Use **biome modifiers (`add_features`, etc.)** como complemento para decorar biomas já registrados.
- Evite manter, ao mesmo tempo, duas fontes de inserção do mesmo bioma (TB + `add_biomes`) para não confundir diagnóstico.

## TAREFA B — O que foi implantado no mod `dreamsdimensions`
- Implementada region TerraBlender para `dreamsdimensions:lumina_hollows` com dois perfis:
  - `TESTING_EASY_SPAWN` (amplo para validação).
  - `PRODUCTION_RARE` (estreito para produção).
- Registro da region no `commonSetup` com `enqueueWork`.
- Logs de diagnóstico para confirmar:
  - chamada de bootstrap TB;
  - region registrada;
  - execução de `addBiomes(...)`.
- `neoforge:add_features` do bioma foi preservado para flora emissiva.
- `neoforge:add_biomes` removido para evitar duplicidade de pipeline.

## TAREFA C — Guia prático de debug

### Logs esperados
1. `[TerraBlender] Registrando regions do Dreams Dimensions...`
2. `[TerraBlender] Region registrada: dreamsdimensions:overworld_lumina_hollows (weight=...)`
3. `[TerraBlender] addBiomes executando para region=... perfil=...`

### Procedimento de teste
1. Crie **mundo novo** (preferencialmente seed nova).
2. Use perfil `TESTING_EASY_SPAWN` até encontrar o bioma.
3. Depois mude para `PRODUCTION_RARE` e regenere mundo para validar raridade.
4. Use `/locate biome dreamsdimensions:lumina_hollows` como confirmação funcional.

### Diagnóstico rápido
- **Não registrado**: logs 1/2 não aparecem → revisar `commonSetup + enqueueWork`.
- **Registrado mas não aparece**: logs 1/2/3 aparecem e locate falha em várias seeds → clima muito estreito e/ou weight baixo.

### Arquivos para revisar se não aparecer
- `DreamsDimensions.java` (lifecycle/`enqueueWork`).
- `DDTerraBlenderBootstrap.java` (registro da region).
- `LuminaHollowsOverworldRegion.java` (parâmetros/offset/weight).
- `worldgen/biome/lumina_hollows.json` (bioma válido).
- `worldgen/biome_modifier/lumina_hollows_flora.json` (decoração complementar).
