# 1. Diagnóstico Atual

## Escopo técnico do bioma `dreamsdimensions:lumina_hollows`

O `lumina_hollows` está funcionalmente integrado ao pipeline TerraBlender + datapack de worldgen, porém hoje atua majoritariamente como **bioma de identidade visual/ambiental**, com baixo impacto sistêmico na progressão macro do mod.

Pontos centrais do estado atual:
- Possui entrada dedicada em TerraBlender (`LuminaHollowsEntry`) com parâmetros climáticos distintos para perfis `TEST` e `PROD`.
- Possui `surface rule` ativa que converte o topo do solo em `ow_dream_glow_moss` dentro do bioma.
- Recebe flora e árvore exclusivas por `neoforge:add_features` no passo `vegetal_decoration`.
- Não possui spawns customizados (listas de spawn vazias no JSON do bioma).
- Não possui estruturas próprias.
- Não possui recurso mineral exclusivo do bioma; minérios oníricos entram por biome modifier global do Overworld.
- Não está conectado diretamente com crafting core, alquimia avançada, eventos raros, nem loops de risco/recompensa específicos.

Diagnóstico sintético:
- **Identidade visual:** forte.
- **Identidade mecânica:** fraca a moderada (principalmente via top block e flora luminosa).
- **Valor estratégico:** baixo.
- **Papel em progressão:** indireto e pouco obrigatório.

---

# 2. Mapa Técnico do Bioma

## 2.1 TerraBlender / Biome Entry

### `Climate.ParameterPoint` usados em `LuminaHollowsEntry`

**Perfil TEST**
- Temperature: span `COOL -> WARM`
- Humidity: span `NEUTRAL -> HUMID`
- Continentalness: span `COAST -> FAR_INLAND`
- Erosion: span `EROSION_1 -> EROSION_5`
- Weirdness: `FULL_RANGE`
- Depth: `SURFACE`
- Offset: `0.0F`

**Perfil PROD**
- Temperature: `NEUTRAL`
- Humidity: `HUMID`
- Continentalness: span `MID_INLAND -> FAR_INLAND`
- Erosion: `EROSION_3`
- Weirdness: `MID_SLICE_NORMAL_ASCENDING`
- Depth: `SURFACE`
- Offset: `0.0F`

### `selectionWeight`
- Valor atual: **1** (bioma raro na distribuição relativa dentro da region custom).

### Perfil TEST vs PROD
- Seleção do perfil via propriedade JVM `dreamsdimensions.worldgen.profile`.
- Default: `PROD`.
- `TEST` expande envelope climático para facilitar validação/localização do bioma durante testes.

### `RegionType`
- `RegionType.OVERWORLD`.

### Region registrada / peso da region
- Region registrada: `dreamsdimensions:overworld`.
- Peso da region (`OVERWORLD_REGION_WEIGHT`): **8**.

## 2.2 Features injetadas no bioma via `neoforge:add_features`

Biome modifiers específicos do `lumina_hollows`:
1. `add_somnibark_tree_to_lumina_hollows.json`
   - Feature: `dreamsdimensions:ow_somnibark_tree_placed`
   - Step: `vegetal_decoration`
2. `add_lumina_hollows_lumina_flower.json`
   - Feature: `dreamsdimensions:patch_ow_lumina_flower`
   - Step: `vegetal_decoration`
3. `add_lumina_hollows_somniflora.json`
   - Feature: `dreamsdimensions:patch_ow_somniflora`
   - Step: `vegetal_decoration`

## 2.3 Cadeia configured_feature / placed_feature usada

### Árvores
- `placed_feature`: `ow_somnibark_tree_placed`
  - referencia `configured_feature`: `ow_trees_lumina_hollows`
- `configured_feature`: `ow_trees_lumina_hollows`
  - random selector com default para `ow_somnibark_tree_checked`
- `placed_feature`: `ow_somnibark_tree_checked`
  - valida solo em `ow_dream_glow_moss` + filtros anti-água
  - referencia `configured_feature`: `ow_somnibark_tree`
- `configured_feature`: `ow_somnibark_tree`
  - tronco `ow_somnibark_log`
  - folhas `ow_somnibark_leaves`

### Flora
- `placed_feature`: `patch_ow_lumina_flower`
  - referencia `configured_feature`: `patch_ow_lumina_flower`
  - `count=8`, `heightmap=MOTION_BLOCKING`, `biome filter`
  - predicado exige bloco abaixo `ow_dream_glow_moss`
- `configured_feature`: `patch_ow_lumina_flower`
  - random patch -> feature `ow_lumina_flower`
- `configured_feature`: `ow_lumina_flower`
  - simple block `ow_lumina_flower`

- `placed_feature`: `patch_ow_somniflora`
  - referencia `configured_feature`: `patch_ow_somniflora`
  - `count=8`, `heightmap=MOTION_BLOCKING`, `biome filter`
  - predicado exige bloco abaixo `ow_dream_glow_moss`
- `configured_feature`: `patch_ow_somniflora`
  - random patch -> feature `ow_somniflora`
- `configured_feature`: `ow_somniflora`
  - simple block `ow_somniflora`

## 2.4 Surface rules e top/under blocks

- Usa surface rule por código: `LuminaHollowsSurfaceRules.register()`.
- Regra aplicada no Overworld via TerraBlender `SurfaceRuleManager`.
- Efeito: se o biome é `lumina_hollows` e está `ON_FLOOR`, força estado do bloco para `ow_dream_glow_moss`.
- Alteração explícita observada: **top block**.
- Não há regra explícita de **under block** dedicada ao bioma no código atual.

## 2.5 Emissive JSON e efeitos especiais em código

### Emissive JSON
- `ow_lumina_flower` usa textura emissiva (`plant_emissive`) com faces emissivas (`light_emission: 15`) no modelo.
- `ow_somniflora` herda `minecraft:block/cross_emissive` com `cross_emissive`.
- `ow_dream_glow_moss` possui camada emissiva superior com `neoforge_data` de luz.

### Efeitos especiais por código
- `OwLuminaFlowerBlock#animateTick` gera partículas `ParticleTypes.GLOW` no cliente.
- `OwLuminaFlowerBlock` e `OwSomnifloraBlock` têm regra custom de solo (`Dirt` + tag `lumina_soil` + `ow_dream_glow_moss`).

## 2.6 Conteúdo gerado no bioma (mapeamento exato)

### Blocos exclusivos observáveis no bioma

1. `ow_dream_glow_moss`
   - Tipo: estrutural/superfície temática.
   - Função: base ecológica do bioma (ancora flora e árvores por predicado).
   - Loot custom: sim (`loot_tables/blocks/ow_dream_glow_moss.json`).
   - Estado especial: sem blockstate custom relevante; propriedade de bloco com luz 8.

2. `ow_lumina_flower`
   - Tipo: flora decorativa luminosa.
   - Função: estética + sinalização visual.
   - Loot custom: sim (drop próprio).
   - Estado especial: sem propriedades de estado custom; tem comportamento especial via `animateTick` e emissive model.

3. `ow_somniflora`
   - Tipo: flora decorativa (base FireflyBush).
   - Função: ambientação luminosa/visual.
   - Loot custom: sim (drop próprio).
   - Estado especial: herda estados de `FireflyBushBlock`; possui regra custom de solo.

4. `ow_somnibark_log`
   - Tipo: estrutural/lenhoso.
   - Função: componente de árvore do bioma.
   - Loot custom: sim (drop próprio).
   - Estado especial: `axis` (RotatedPillar).

5. `ow_somnibark_leaves`
   - Tipo: estrutural/folhagem.
   - Função: copa de árvore temática.
   - Loot custom: sim (drop condicional Silk Touch).
   - Estado especial: estados padrão de folhas (distance/persistent/waterlogged) usados na configured feature.

### Flora: frequência estimada, placement e função

- **Lumina Flower**
  - placement efetivo: `patch_ow_lumina_flower` com `count=8` por tentativa/área de geração.
  - patch configurado com `tries=1`, spread mínimo (0/0), indicando patches pequenos e localizados.
  - depende de substrato `ow_dream_glow_moss`.
  - função atual: predominantemente decorativa (sem integração sistêmica direta).

- **Somniflora**
  - placement efetivo: `patch_ow_somniflora` com `count=8`.
  - patch com `tries=1` e dependência de `ow_dream_glow_moss`.
  - função atual: predominantemente decorativa.

- **Árvore Somnibark**
  - placement: `ow_somnibark_tree_placed` com count ponderado (2 com peso 2; 3 com peso 7; 4 com peso 1).
  - estimativa média de count por distribuição: ~2.9 por avaliação de placement.
  - filtro anti-água e necessidade de solo `ow_dream_glow_moss`.
  - função atual: estética/ambiental e coleta de bloco de madeira (sem cadeia de crafting exclusiva identificada).

### Minérios

- O bioma não injeta minério exclusivo próprio.
- `ow_dream_ore` e `ow_deepslate_dream_ore` entram por biome modifier global `#minecraft:is_overworld`, portanto podem aparecer também no `lumina_hollows`, mas não são diferenciais dele.

### Estruturas

- Não há estrutura custom registrada para o bioma (nem pasta de structures custom do mod com conteúdo associado ao `lumina_hollows`).

### Spawns

- `spawners` no JSON do bioma estão vazios para todas as categorias (`creature`, `monster`, `ambient`, etc.).
- Não há mob exclusivo vinculado ao bioma.
- Resultado prático: o bioma depende de comportamento de spawn externo/global; não entrega ecossistema próprio.

## 2.7 Loot tables, itens e progressão relacionada

### Loot tables dos blocos exclusivos do bioma
- `ow_dream_glow_moss`: drop próprio.
- `ow_lumina_flower`: drop próprio.
- `ow_somniflora`: drop próprio.
- `ow_somnibark_log`: drop próprio.
- `ow_somnibark_leaves`: drop condicional por Silk Touch.

### Itens relacionados
- Todos os blocos acima têm block items registrados.
- Não foram encontrados usos diretos desses blocos/flora em receitas principais (`recipe/ow`) de progressão core.

### Impacto na progressão
- Progressão principal documentada gira em torno de `ow_dream_dust` -> refinamento -> núcleo/totem/despertador/alquimia.
- O `lumina_hollows` hoje não aparece como etapa obrigatória, gargalo técnico ou fonte exclusiva de material-meta.

---

# 3. Análise de Gameplay

## Papel atual na progressão

- **Papel real:** bioma de ambientação com coleta opcional de blocos decorativos.
- **Papel esperado (não realizado):** poderia ser hub de recursos de estabilização/lucidez/alquimia onírica.

## Recursos únicos

- Únicos em estética/coleção: sim (flora e madeira temática, musgo luminoso).
- Únicos em progressão mecânica: não.

## Risco x recompensa

- Risco: baixo (sem mobs custom, sem hazards ambientais dedicados).
- Recompensa: baixa a moderada (principalmente cosmética).
- Resultado: bioma raro com payoff funcional limitado.

## Identidade mecânica

- Parcial: solo temático + flora condicionada + emissão luminosa + partículas.
- Ausente: mecânica de biome event, buff/debuff, interação com sonho, crafting exclusivo, objetivo estratégico.

## Impacto por sistema

- **Combate:** praticamente nulo.
- **Mobilidade:** nulo.
- **Crafting:** nulo (sem receitas ancoradas no bioma).
- **Economia de recursos:** baixo (apenas blocos de decoração/coleção).

## Diferenciação frente a `campo_onirico_azul` e `dreamscape`

- Diferencia visual: boa (paleta, flora luminosa, solo musgo).
- Diferencia sistêmica: fraca (não inaugura loop equivalente ao peso do pipeline de minério/itens oníricos geral).

Conclusão de gameplay:
- O `lumina_hollows` hoje funciona como **bioma raro estético com pouca agência sistêmica**.

---

# 4. Lacunas Estruturais

## Gaps principais

1. **Falta de mob exclusivo**
   - Sem criatura que materialize a fantasia do bioma.

2. **Falta de estrutura temática**
   - Sem ponto de interesse que justifique exploração dedicada.

3. **Falta de evento dinâmico**
   - Sem fenômeno raro (chuva lumina, pulsos oníricos, surtos de flora, etc.).

4. **Falta de recurso exclusivo com uso-meta**
   - Blocos/plantas existem, mas não convertem em item estratégico da progressão.

5. **Falta de risco projetado**
   - Ecossistema não impõe escolhas táticas (night threat, zonas instáveis, penalidades de permanência).

6. **Falta de recompensa exclusiva**
   - Ausência de drop/insumo só obtível (ou majoritariamente obtível) no bioma.

7. **Baixa integração com alquimia**
   - Sem ingrediente-chave de poção vinculado a Lumina.

8. **Baixa integração com Totem/Despertador**
   - Sem recipe augment, upgrade, catalisador ou modo especial desses sistemas.

## Classificação global

- Estado atual: **subutilizado**.
- Risco de design: virar “biome raro sem propósito” no médio prazo se não ganhar loop próprio.

---

# 5. Propostas de Expansão

## 5.1 Features pequenas (low cost)

### A) Drop secundário raro de flora (`lumina_pollen`)
- Ideia: `ow_lumina_flower` e `ow_somniflora` terem chance baixa de dropar item alquímico.
- Impacto técnico: baixo.
- Complexidade: baixa.
- Arquivos afetados:
  - `loot_tables/blocks/ow_lumina_flower.json`
  - `loot_tables/blocks/ow_somniflora.json`
  - registro de item + lang + model de item
- Sistemas: loot, item, progressão/alquimia.

### B) Receita utilitária de curto prazo para exploração
- Ideia: consumível simples de visão noturna curta ou resistência leve usando item Lumina.
- Impacto técnico: baixo.
- Complexidade: baixa.
- Arquivos: `recipe/ow/*`, itens, possivelmente efeito consumível.
- Sistemas: recipe, item/effect.

### C) Ajuste fino de densidade de flora por profile
- Ideia: aumentar `count` em `TEST`, calibrar `PROD` para legibilidade visual + performance.
- Impacto técnico: baixo.
- Complexidade: baixa.
- Arquivos: placed features do patch.
- Sistemas: worldgen.

## 5.2 Features médias (mid development)

### D) Evento periódico “Pulso de Lumina”
- Ideia: dentro do bioma, em janelas raras, aumentar partículas, spawn de flora efêmera e bônus temporário (ex.: resistência a efeitos oníricos negativos).
- Impacto técnico: médio.
- Complexidade: média.
- Arquivos:
  - handlers de tick/evento
  - effects/potions (se aplicável)
  - datapacks auxiliares (loot/spawn temporário)
- Sistemas: event runtime, effect, gameplay loop.

### E) Variante de estrutura pequena: “Nódulo de Somnibark”
- Ideia: micro-estrutura gerada raramente com loot table temática.
- Impacto técnico: médio.
- Complexidade: média.
- Arquivos:
  - `worldgen/structure/*`
  - `worldgen/structure_set/*`
  - `loot_tables/chests/*`
- Sistemas: worldgen structure, loot.

### F) Spawn custom leve (1 mob adaptado)
- Ideia: adicionar criatura existente com tuning de comportamento/atributos no bioma, antes de criar mob 100% novo.
- Impacto técnico: médio.
- Complexidade: média.
- Arquivos:
  - biome JSON (spawners)
  - possivelmente hooks de atributos/eventos
- Sistemas: spawn/combat.

## 5.3 Features grandes (high impact)

### G) Mob exclusivo “Lumina Wardenling” (nome conceitual)
- Ideia: entidade guardiã que dropa componente para upgrades late game.
- Impacto técnico: alto.
- Complexidade: alta.
- Arquivos:
  - entidade, renderer, IA, atributos, loot table, spawn rules
  - integração com progressão/recipes
- Sistemas: entity AI, combat loop, loot, progression.

### H) Estrutura maior “Santuário de Ancoragem Lúcida”
- Ideia: dungeon curta com puzzle de luz e recompensa de upgrade para Totem/Despertador.
- Impacto técnico: alto.
- Complexidade: alta.
- Arquivos:
  - structure + processor + loot + advancements
- Sistemas: structure worldgen, progression gating, exploration.

### I) Recurso exclusivo meta-shift: “Cristal de Lumina Estável”
- Ideia: item raro do bioma para reduzir custo/risco de teleporte onírico, ou melhorar eficiência de refinamento.
- Impacto técnico: alto (muda meta de progressão).
- Complexidade: média-alta.
- Arquivos: itens, receitas, possivelmente lógica do totem/despertador.
- Sistemas: item economy, progression balance, dream system.

## 5.4 Conteúdo late game

- Upgrade 2.0 do `ow_anchoring_totem` com componente Lumina (redução de cooldown, área de efeito, estabilidade).
- Upgrade do `ow_oneiric_awakener` (uso mais rápido, menor penalidade, ou uso condicional em emergência).
- Receita avançada de alquimia exigindo item exclusivo do bioma + drop de boss/mob.

## 5.5 Conteúdo interligado ao sistema de sonho

- “Carga de Lucidez”: recurso acumulável ao explorar Lumina Hollows, consumido para evitar efeitos negativos em sonho.
- “Ressonância de Bioma”: permanecer no bioma carrega buffer que modifica resultado de retorno via Despertador.
- Integrar com achievements/advancements para guiar jogador ao bioma como etapa de domínio onírico.

## 5.6 Eventos raros do bioma

1. **Chuva de partículas Lumina** (cosmético + bônus pequeno).
2. **Floração súbita** (burst de flora colhível por tempo curto).
3. **Anomalia de eco onírico** (mini desafio de combate com recompensa).

## 5.7 Estruturas temáticas sugeridas

- **Pequenas:** círculos de musgo luminoso com cache de loot.
- **Médias:** ruínas de madeira Somnibark com puzzle de luz.
- **Grandes:** santuário em múltiplas salas, reward room com item-chave.

## 5.8 Recurso exclusivo que altera meta

### Proposta prioritária: `ow_lumina_catalyst`
- Fonte: combinação de evento raro + estrutura + drop de mob exclusivo (evita farm trivial).
- Uso: crafting de upgrade de Totem/Despertador e poção endgame.
- Efeito estratégico:
  - transforma `lumina_hollows` em destino recorrente.
  - cria elo entre exploração, combate e alquimia.

---

# 6. Roadmap Estratégico

## Fase 1 (rápida, alto ROI, baixo risco)

Objetivo: dar utilidade imediata ao bioma sem refatorações pesadas.

1. Adicionar item derivado da flora (`lumina_pollen`) em loot.
2. Criar 1–2 receitas/alquimias utilitárias com esse item.
3. Ajustar densidade e legibilidade visual da flora.
4. Adicionar advancements básicos (“Encontre Lumina Hollows”, “Colete recurso Lumina”).

**Ganho de gameplay:** médio imediato.
**Custo técnico:** baixo.

## Fase 2 (consolidação de identidade mecânica)

Objetivo: tornar o bioma estrategicamente relevante.

1. Introduzir evento dinâmico “Pulso de Lumina”.
2. Inserir estrutura pequena/média com loot exclusivo.
3. Definir spawn custom leve (sem mob totalmente novo ainda).
4. Integrar item Lumina em upgrades intermediários do pipeline onírico.

**Ganho de gameplay:** alto.
**Custo técnico:** médio.

## Fase 3 (expansão high impact / endgame)

Objetivo: transformar Lumina Hollows em pilar de endgame onírico.

1. Implementar mob exclusivo com drop-chave.
2. Implementar estrutura maior temática (santuário/dungeon).
3. Lançar recurso meta (`ow_lumina_catalyst`) para upgrades avançados de Totem/Despertador/alquimia.
4. Balancear risco/recompensa (ameaças noturnas/eventos + recompensas significativas).

**Ganho de gameplay:** muito alto.
**Custo técnico:** alto.

---

## Síntese executiva

O `lumina_hollows` já está bem amarrado no pipeline técnico de worldgen, surface rules e estética emissiva, mas ainda não converte essa base em um loop sistêmico robusto. A prioridade arquitetural recomendada é evoluir de “bioma bonito” para “bioma funcional de progressão”, conectando flora/estrutura/evento/mob a um recurso exclusivo que impacte o meta do sistema de sonho.
