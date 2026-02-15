# Ow Lumina Flower renderer - investigação no beta NeoForge 21.11.37

Comandos usados no workspace para validar API real (via `javap`):

- `javap -classpath ContextoIA/neoforge-21.11.37-beta -p net.minecraft.client.renderer.OrderedSubmitNodeCollector`
- `javap -classpath ContextoIA/neoforge-21.11.37-beta -p net.minecraft.client.renderer.SubmitNodeCollector`
- `javap -classpath ContextoIA/neoforge-21.11.37-beta -p -c net.minecraft.client.renderer.SubmitNodeCollection`
- `javap -classpath ContextoIA/neoforge-21.11.37-beta -p -c net.minecraft.client.renderer.feature.BlockFeatureRenderer`
- `javap -classpath ContextoIA/neoforge-21.11.37-beta -p -c net.minecraft.client.renderer.block.ModelBlockRenderer`
- `javap -classpath ContextoIA/neoforge-21.11.37-beta -p -c net.minecraft.client.renderer.chunk.SectionCompiler`
- `javap -classpath ContextoIA/neoforge-21.11.37-beta -p net.minecraft.client.renderer.block.BlockRenderDispatcher`

## Achados confirmados

1. `OrderedSubmitNodeCollector#submitBlockModel` existe com assinatura:
   `submitBlockModel(PoseStack, RenderType, BlockStateModel, float, float, float, int, int, int)`.
2. No beta atual, o 9º argumento é tratado como `outlineColor` em `SubmitNodeStorage$BlockModelSubmit`.
3. No caminho de renderização de `BlockModelSubmit`, `BlockFeatureRenderer` chama
   `ModelBlockRenderer.renderModel(Pose, VertexConsumer, BlockStateModel, r, g, b, light, overlay)`.
4. Esse `renderModel(...)` coleta partes com `RandomSource.create(42L)` (seed fixa), então esse caminho **não** usa seed por posição.
5. No pipeline vanilla de chunk (`SectionCompiler`), o seed por posição vem de
   `BlockState.getSeed(BlockPos)` antes de coletar partes do `BlockStateModel`.
6. `ModelBlockRenderer.tesselateBlock(...)` aplica deslocamento do bloco com
   `BlockState.getOffset(BlockPos)`; para flores/plants isso impacta alinhamento visual.
7. `RenderTypes.entityTranslucentEmissive(...)` é usado em renderers de entidade (ex.: Warden),
   não no pipeline padrão de blocos com atlas de blocos.

## Decisão implementada

Para corrigir o desalinhamento da camada emissiva da flor neste projeto:

- manter a camada emissiva custom (full-bright),
- usar sprite emissivo no `TextureAtlas.LOCATION_BLOCKS`,
- aplicar o mesmo `BlockState.getOffset(BlockPos)` no desenho da geometria emissiva.

Isso elimina o offset fora do eixo observado quando a geometria era renderizada sem o deslocamento vanilla.
