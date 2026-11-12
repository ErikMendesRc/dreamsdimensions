# TASK 7.2 — Contrato Mínimo de Conteúdo e Assets

**DreamsDimensions · Minecraft 26.3 / NeoForge 26.3 / Java 25 · 12/11/2026**  
**Branch:** `migration/minecraft-26.3` · **Escopo:** validação de conteúdo existente e do próximo slice F0; **nenhum bloco, item, recurso, minério ou mecânica novo nesta task**.

## 1. Contrato implementado

A Task 7.2 prepara a implementação da Pedra Marcada (Task 7.3) sem antecipar conteúdo decorativo nem construir um framework de registro genérico. O código Java de registro permanece como fonte de verdade.

- `scripts/ci/content_contract.py`: varre os campos `DeferredBlock` e `DeferredItem` nos registros reais, seus IDs, relações `registerSimpleBlockItem`, arquivos e dependências. Erra fechado em novos estilos de registro até revisão explícita.
- `scripts/ci/check_content_contract.py`: CLI com saída `STATIC_CHECK` em JSON no schema `dreamsdimensions.qa.v1` da Task 7.1.
- `scripts/ci/test_content_contract.py`: testes autocontidos, inclusive um bloco *sandbox* chamado `marked_stone` **somente em pasta temporária**, com exemplos negativos. Ele **não existe no mod ainda**.
- `scripts/ci/content_contract_exceptions.json`: exceções *estritamente históricas* de loot (flor, tronco e folhas), documentadas com motivo. Novos blocos não ganham exceção automaticamente.
- CI: etapa antes das demais validações e artefato JSON por seed, sem introduzir novo servidor, nova instalação ou worldgen.

## 2. O que o script garante — e o que não garante

| Validação | Evidência estática |
| --- | --- |
| Bloco registrado + BlockItem ou Item registrado | Declarações Java conhecidas em `ModBlocks`/`ModItems` |
| `blockstates` | Um arquivo para cada bloco; sem arquivo solto, com variant/multipart/definition |
| `items/*.json` | Uma definição cliente 26.3 para cada item registrado |
| `models` | Referências locais a arquivos existentes, pais conhecidos e ciclo de referências bloqueado |
| `textures` | Referências internas a PNG existente, cabeçalho e dimensões válidas |
| `lang/en_us` e `pt_br` | Nome de cada bloco e item próprio, sem campos vazios |
| `loot_table/blocks` | Blocos com tabela própria, exceto lista legada; itens em entradas conhecidos |
| `tags/block` e `tags/item` | IDs próprios existentes; nunca sobrescrever tags globais com `replace=true` |
| ferramentas | Bloco `requiresCorrectToolForDrops` exige entrada no conjunto `mineable/*` |
| `recipe` | Entradas e saídas próprias registradas, shaped/shapeless/smelting/blasting/stonecutting e formas suportadas |
| demais JSON | Parse sintático de todos os JSON em `src/main/resources` |

**Limites explícitos:** não interpreta Java inteiro, nem valida assets externos `minecraft:`/outras dependências, model bake, posição no Creative Tab, renderização, colisão, luz, minabilidade real, `Silk Touch`, chance/drop efetivo ou obtenção por Survival. Esses aspectos continuam cobertos pelo carregamento real do servidor, runtime e homologação manual.

A recomendação **16×16** para blocos sólidos é diretriz da Art Bible 6.7. O script valida PNG, mas **não exige 16×16 para atlas ou todos os assets**.

## 3. Dívida técnica encontrada antes da implementação

O repositório já tinha três blocos sem `loot_table/blocks`: `dream_flower_block`, `dream_log_block` e `dream_leaves_block`. A Task 7.2 **não adiciona drops** sem decisão de gameplay, pois isso alteraria economia e saves. As exceções existem somente no arquivo versionado e devem ser removidas quando os drops forem corrigidos.

Os três nomes de bloco também não estavam traduzidos: foram adicionadas entradas em `en_us` e `pt_br`, sem alterar IDs, recipes, assets gráficos ou comportamento.

## 4. Checklist da Task 7.3 (Pedra Marcada / F0a)

1. Descrever o **consumidor Survival** antes de registrar a peça; bloco ornamental útil para construção, não gera Dream Dust.
2. `ModBlocks.java`: registrar `marked_stone` (nome *candidato*, validar nomenclatura antes de implementar).
3. `ModItems.java`: registrar `BlockItem` com mesmo ID. Creative Tab: exibir sem duplicar.
4. `assets/dreamsdimensions/blockstates/marked_stone.json`: um modelo de bloco válido.
5. `assets/dreamsdimensions/models/block/marked_stone.json`: usar parent nativo apropriado; textura original, preferencialmente 16×16 alinhada à Art Bible.
6. `assets/dreamsdimensions/textures/block/marked_stone.png`: PNG válido, silhueta/tiling revisado visualmente em Minecraft.
7. `assets/dreamsdimensions/items/marked_stone.json`: **arquivo de definição cliente obrigatório em 26.3**. Referenciar o modelo adequado; `models/item` pode ser opcional se o client item usa um modelo de bloco existente.
8. Traduções `en_us` e `pt_br` para `block.dreamsdimensions.marked_stone`.
9. Loot table `data/dreamsdimensions/loot_table/blocks/marked_stone.json`: não deixar implícita; evitar dupes.
10. Tags de ferramenta `data/minecraft/tags/block/mineable/pickaxe.json` caso exija picareta correta. `replace=false`; demais tiers conforme design.
11. Se houver receita, usar **`data/dreamsdimensions/recipe/` singular**, validar ingredientes/resultados e economia; a geração natural será exclusivamente Task 7.4.
12. Validar `python3 scripts/ci/test_content_contract.py`, `python3 scripts/ci/check_content_contract.py`, `./gradlew --no-daemon clean build` e CI dedicated server; registrar prova manual em Survival no momento adequado.

**Gate:** uma textura PNG visível e JSON parseado *não* são prova de um bloco renderizado/minerado no Minecraft; não marcar `SURVIVAL_VERIFIED` sem abrir o jogo.

## 5. Comandos prontos de QA (conteúdo existente)

No PowerShell, raiz do projeto:

```powershell
git switch migration/minecraft-26.3
git pull origin migration/minecraft-26.3
python scripts/ci/test_content_contract.py
python scripts/ci/check_content_contract.py --report qa-content-contract.json
.\gradlew.bat --no-daemon clean build
.\gradlew.bat runClient
```

No Minecraft (cheats/OP para comandos de alteração):

```mcfunction
/gamemode spectator
/execute in dreamsdimensions:dreamscape run tp @s 0 180 0
/execute in dreamsdimensions:campo_onirico_azul run tp @s 0 180 0
/execute in minecraft:overworld run tp @s 0 120 0
/give @s dreamsdimensions:blue_dream_stone 16
/give @s dreamsdimensions:dream_ore 16
/dreamqa status
/dreamqa enable
/dreamqa snapshot
```

Esses pontos de teleporte são **apenas de observação Spectator**, não spawn seguro em Survival. Sem `/give` nem `/tp`, validar mineração com ferramenta, blocos colocáveis, silk/fortune e drop de minério separadamente. **`/give dreamsdimensions:marked_stone` ainda não funciona nesta task**.

Para teste de sucesso no console da CI, o script imprime contadores e `STATIC PASS`. O relatório JSON tem classificação `STATIC_CHECK` distinta dos registros `RUNTIME_OBSERVED` do servidor.

## 6. Referências técnicas

- NeoForge Primer 26.3: https://docs.neoforged.net/primer/docs/26.3/
- NeoForge Client Items: https://docs.neoforged.net/docs/resources/client/models/items/
- NeoForge Models & Blockstates: https://docs.neoforged.net/docs/resources/client/models/
- NeoForge Loot Tables: https://docs.neoforged.net/docs/resources/server/loottables/
- [Art Bible 6.7](OVERWORLD_VISUAL_DIRECTION_TEXTURES_MODELS_ASSETS_TASK_6_7.md)
- [Roadmap 6.9](OVERWORLD_EXECUTIVE_IMPLEMENTATION_ROADMAP_TASK_6_9.md)
- [QA 7.1](DREAMSDIMENSIONS_TASK_7_1.md)
