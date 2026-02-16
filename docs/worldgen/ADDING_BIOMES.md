# Adding Overworld Biomes (TerraBlender)

Este documento descreve o pipeline oficial de biomas Overworld para o mod `dreamsdimensions` após o refactor.

## Fonte de verdade do pipeline

- O pipeline de inserção de biomas é **TerraBlender Region** (não `neoforge:add_biomes`).
- O padrão segue o que foi auditado no BOP: registro central via `Regions.register(...)` + `Region#addBiomes(...)` para mapear `Climate.ParameterPoint -> biome`. Veja o relatório técnico interno.  
  Referências:
  - `docs/relatorio-tecnico-biomas-overworld-terrablender.md`
  - `docs/terrablender_audit_neoforge_1.21.11.md`

## Arquitetura atual (Dreams Dimensions)

- `worldgen/terrablender/`
  - `DDTerraBlenderBootstrap`: registro central de regions TerraBlender.
  - `DreamsOverworldRegion`: region única de Overworld que consome o catálogo.
- `worldgen/biomes/`
  - `DreamsBiomeEntry`: contrato declarativo de 1 bioma.
  - `DreamsBiomesOverworld`: lista central de entries.
  - `LuminaHollowsEntry`: entry concreta do bioma.
- `worldgen/features/`
  - features continuam no datapack (`configured_feature`, `placed_feature`).
- `worldgen/modifiers/`
  - mantemos apenas `neoforge:add_features` para flora/ore placement.
  - `neoforge:add_biomes` deve permanecer ausente para evitar conflito com TerraBlender.

## Como adicionar um novo bioma Overworld

1. **Criar o JSON do biome** em:  
   `data/dreamsdimensions/worldgen/biome/<seu_bioma>.json`
2. **Criar 1 classe entry** (ex.: `LuminaHollowsEntry`):
   - `ResourceKey<Biome>` (id do biome)
   - `regionType()` retornando `RegionType.OVERWORLD`
   - `selectionWeight()`
   - `parameterPoints(WorldgenProfiles.Profile)` com perfis TEST/PROD
3. **Registrar na lista** em `DreamsBiomesOverworld`:
   - adicionar uma linha: `ENTRIES_INTERNAL.add(new SeuBiomeEntry());`
4. **(Opcional) Adicionar features**:
   - criar/ajustar `configured_feature`
   - criar/ajustar `placed_feature`
   - aplicar via `worldgen/biome_modifier/*` com `type = neoforge:add_features`

## Perfis de raridade (TEST e PROD)

- O perfil ativo fica em `WorldgenProfiles.ACTIVE`.
- Default é `PROD`.
- Para teste local rápido, rode com:
  - `-Ddreamsdimensions.worldgen.profile=TEST`

### Diretriz de uso
- `TEST`: ranges amplos para aparecer fácil e validar pipeline.
- `PROD`: ranges estreitos para manter raridade.

## Surface rules: quando usar

Use surface rules **somente** quando o bioma exigir troca de top/under blocks via ruído de superfície em larga escala.  
Para flora/decoração pontual, prefira `configured_feature` + `placed_feature` + `neoforge:add_features`.

## Novos blocos/plantas no biome

Fluxo recomendado:
1. Registrar bloco/item no mod.
2. Criar `configured_feature` com o bloco/planta.
3. Criar `placed_feature` com filtros/raridade.
4. Associar ao biome usando `neoforge:add_features` no step correto.

Registries/tags úteis:
- `worldgen/configured_feature`
- `worldgen/placed_feature`
- `worldgen/biome_modifier`
- tags de biome (`minecraft:is_overworld`, tags custom)

## Debug checklist

1. Confirmar log de registro:
   - `[TerraBlender] Registrando regions do Dreams Dimensions...`
   - `[TerraBlender] Region registrada: dreamsdimensions:overworld ...`
2. Confirmar execução de `addBiomes`:
   - log com `profile=TEST|PROD` e número de entries.
3. Validar “registrado vs raro”:
   - TEST deve localizar rápido (`/locate biome ...`).
   - PROD pode exigir exploração maior.
4. Sempre testar com **mundo novo** (seed nova) ao alterar parâmetros.

## Template de novo biome entry

```java
public final class MyNewBiomeEntry implements DreamsBiomeEntry {
    public static final ResourceKey<Biome> BIOME_KEY = ResourceKey.create(
            Registries.BIOME,
            Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "my_new_biome")
    );

    @Override
    public ResourceKey<Biome> biomeKey() {
        return BIOME_KEY;
    }

    @Override
    public RegionType regionType() {
        return RegionType.OVERWORLD;
    }

    @Override
    public int selectionWeight() {
        return 1;
    }

    @Override
    public List<Climate.ParameterPoint> parameterPoints(WorldgenProfiles.Profile profile) {
        return switch (profile) {
            case TEST -> List.of(/* ranges amplos */);
            case PROD -> List.of(/* ranges estreitos */);
        };
    }
}
```

Depois, registrar em `DreamsBiomesOverworld`:

```java
ENTRIES_INTERNAL.add(new MyNewBiomeEntry());
```
