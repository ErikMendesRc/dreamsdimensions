# Lumina vines research (Vanilla 1.21.11 + NeoForge beta + BOP)

## Vanilla 1.21.11 (ContextoIA) — classes/métodos exatos

### A) Aplicação de vines em árvores
- `net.minecraft.world.level.levelgen.feature.treedecorators.LeaveVineDecorator#place(TreeDecorator.Context)`:
  - percorre `context.leaves()`;
  - faz roll por lado com `probability`;
  - usa `context.isAir(adjacentPos)`;
  - chama `addHangingVine(...)`.
- `LeaveVineDecorator#addHangingVine(BlockPos, BooleanProperty, TreeDecorator.Context)`:
  - coloca o primeiro vine no `startPos`;
  - desce em coluna (`cursor = cursor.below()`) sem drift lateral;
  - pára quando deixa de ser ar ou quando atinge o limite de comprimento.

### B) Validação / parada
- Vanilla usa `TreeDecorator.Context#isAir` para validar posição inicial e continuação da coluna.
- O loop de coluna em `addHangingVine` interrompe por:
  - bloco não-ar (`!context.isAir(pos)`), ou
  - limite de comprimento.
- No `LeaveVineDecorator`, o estado usado vem de `TreeDecorator.Context#placeVine`, que monta `Blocks.VINE.defaultBlockState().setValue(faceProperty, true)`.

### C) Acesso ao reader no decorator (evitando erro de API)
- `TreeDecorator.Context` armazena `LevelSimulatedReader` (campo `level`).
- **Não** há `getBlockState` em `LevelSimulatedReader`.
- O vanilla consulta estado por predicado via:
  - `TreeDecorator.Context#isAir(pos)` -> `LevelSimulatedReader#isStateAtPosition(pos, predicate)`
  - `TreeDecorator.Context#checkBlock(pos, predicate)` -> `LevelSimulatedReader#isStateAtPosition(pos, predicate)`
- O vanilla seta blocos pelo writer do contexto:
  - `TreeDecorator.Context#setBlock(pos, state)` (delegando ao `decorationSetter`).

## NeoForge beta
- O contrato de `TreeDecorator.Context` em 1.21.11-beta segue o padrão acima: leitura por `isAir/checkBlock` e escrita por `setBlock`.

## Biomes O' Plenty
- Padrão geral de densidade de vines em árvores: chance por árvore + múltiplas tentativas limitadas + comprimento curto/moderado.
- Isso foi usado como referência de tuning, mantendo a lógica base de coluna vertical do vanilla.

## Comandos usados na pesquisa
- `javap -classpath ContextoIA/neoforge-21.11.37-beta -c -p net.minecraft.world.level.levelgen.feature.treedecorators.LeaveVineDecorator`
- `javap -classpath ContextoIA/neoforge-21.11.37-beta -c -p net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator$Context`
- `javap -classpath ContextoIA/neoforge-21.11.37-beta -p net.minecraft.world.level.LevelSimulatedReader`
