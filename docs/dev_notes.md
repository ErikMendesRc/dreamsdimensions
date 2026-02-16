# Notas de desenvolvimento

## Estrutura do projeto
- `src/main/java/com/dreamsdimensions/mod/registry`: registries centrais (itens, blocos, efeitos, poções, tabs, attachments).
- `src/main/java/com/dreamsdimensions/mod/event`: handlers de gameplay, efeitos, brewing, teleporte por sono.
- `src/main/java/com/dreamsdimensions/mod/util`: helpers de retorno e anti-sonho.
- `src/main/resources/data/dreamsdimensions`: datapack (dimensão, worldgen, recipes, loot).
- `src/main/resources/assets/dreamsdimensions`: lang, modelos, texturas, blockstates.

## Convenções observadas
- Prefixos de ID:
  - `ds_` para conteúdo de dreamscape;
  - `az_` para conteúdo do campo onírico azul;
  - `ow_` para materiais/itens de cadeia onírica e integração com Overworld.
- Registry via `DeferredRegister`.
- Loot/recipes em JSON vanilla.

## Como adicionar novo item/bloco/recipe/loot (padrão atual)
1. Registrar item/bloco em `ModItems`/`ModBlocks`.
2. Adicionar tradução em `assets/dreamsdimensions/lang/*.json`.
3. Adicionar modelo/item model/blockstate/textura.
4. Criar receita em `data/dreamsdimensions/recipe/` (ou em código se for brewing).
5. Criar loot table em `data/dreamsdimensions/loot_tables/blocks/` para blocos dropáveis.
6. Atualizar tags (`mineable/*`, tool requirements, grupos como logs/leaves/flowers) quando necessário.

## Config e compat
- Config comum: `DreamsConfig`
  - `dream_dimensions` (lista de dimensões reconhecidas como sonho)
  - `anchoring_totem_radius` (raio de bloqueio de teleporte por sono)
- Sem comandos custom registrados no código atual.
- Compat via tags vanilla e biome modifier NeoForge para inserir minério no Overworld.

## Troubleshooting rápido
- Teleporte de sono não ocorre: revisar dimensão atual, tempo de sono, presença de totem, uso do awakener.
- Despertador falha: verificar se a dimensão atual está em `dream_dimensions`.
- Loot estranho de minério: revisar ferramenta (Silk/Fortune) e explosões (explosion decay).
