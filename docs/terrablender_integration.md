# Integração TerraBlender

## Sumário
- [Estado atual no mod](#estado-atual-no-mod)
- [Pipeline prático](#pipeline-prático)
- [Diretrizes ADDING_BIOMES](#diretrizes-adding_biomes)
- [Boas práticas](#boas-práticas)
- [Falhas comuns](#falhas-comuns)

## Estado atual no mod
Implementação em Java:
- `worldgen/terrablender/DDTerraBlenderBootstrap`
- `worldgen/terrablender/DreamsOverworldRegion`
- `worldgen/biomes/DreamsBiomesOverworld`
- `worldgen/biomes/LuminaHollowsEntry`

Fluxo:
1. `DDTerraBlenderBootstrap.register()` chama `Regions.register(new DreamsOverworldRegion(weight))`.
2. `DreamsOverworldRegion.addBiomes(...)` injeta `Climate.ParameterPoint -> biome`.
3. Catálogo de entries fica em `DreamsBiomesOverworld.ENTRIES`.
4. Surface rules para `lumina_hollows` registradas em `LuminaHollowsSurfaceRules`.

## Pipeline prático
- Region type: `RegionType.OVERWORLD`.
- Bioma atual no catálogo: `dreamsdimensions:lumina_hollows`.
- Raridade/perfil de parâmetros: `WorldgenProfiles.ACTIVE` (TEST/PROD).

## Diretrizes ADDING_BIOMES
Baseado em `docs/worldgen/ADDING_BIOMES.md`:
- usar **entry system** (`DreamsBiomeEntry` + catálogo).
- manter `neoforge:add_biomes` ausente para evitar conflito com TerraBlender.
- usar `neoforge:add_features` para flora/ore placement.
- perfis:
  - `PROD` (padrão)
  - `TEST` com `-Ddreamsdimensions.worldgen.profile=TEST`

## Boas práticas
- testar worldgen com mundo novo (seed nova).
- logar execução de `addBiomes` para confirmar profile ativo.
- separar injeção de bioma (TerraBlender) de decoração (biome modifiers).

## Falhas comuns
- bioma registrado mas raro demais em `PROD`.
- esquecer registrar nova entry em `DreamsBiomesOverworld`.
- usar pipeline misto (`add_biomes` + TerraBlender), gerando comportamento inconsistente.

## Nota sobre auditoria externa
Os arquivos externos citados no pedido (`Auditoria-TerraBlender.txt`) **NÃO EXISTEM no workspace atual**. Portanto, esta página usa somente os artefatos locais do projeto.
