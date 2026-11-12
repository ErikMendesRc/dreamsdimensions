# TASK 6.6 — GRAFO DE CRAFTING, PROCESSAMENTO E PROGRESSÃO MATERIAL

## DreamsDimensions: economia Early / Mid / Late sem End Game próprio

**Status:** especificação de Game Design, não implementação.  
**Data:** 12/11/2026. **Branch:** `migration/minecraft-26.3`.  
**HEAD base:** `46d26d24cf83badca8c27365737f219d9bbe9e95`.  
**Dependências:** [Bible 6.0](GAME_DESIGN_BIBLE_TASK_6_0.md), [Pilares 6.1](OVERWORLD_GAMEPLAY_PILLARS_TASK_6_1.md), [Geologia e Flora 6.2](OVERWORLD_RESOURCES_GEOLOGY_FLORA_BLOCKS_TASK_6_2.md), [Itens 6.3](OVERWORLD_ITEMS_CONSUMABLES_TOOLS_STATIONS_TASK_6_3.md), [Ecologia 6.4](OVERWORLD_ECOLOGY_MOBS_HABITATS_TASK_6_4.md), [Estruturas 6.5](OVERWORLD_STRUCTURES_ENVIRONMENTAL_DISCOVERY_TASK_6_5.md).  
**Próxima Task:** **6.7 — Direção Visual, Texturas, Modelos e Assets**.  
**Limite desta Task:** criar somente este documento. Não alterar Java, recipes, loot, registros, NBT, worldgen, configuração do sono, Curios, teleportes nem a main.

> **Tese:** a progressão deve ser resultado de aprender novas ações e montar processos sustentáveis, não de acrescentar cinco cristais intermediários. O Overworld deve oferecer geologia, cultivo, construção, engenharia e exploração úteis sem exigir Colar ou Despertador. Os sonhos tornam-se viagens arriscadas no Mid e uma atividade consistente **somente no Mid tardio/Late**; não existe boss final ou End Game exclusivo do mod.

---

# 0. DECISÕES DA TASK 6.6

| Ramo | Resultado | Estado da decisão |
|---|---|---|
| Colar, Pedra Infundida, Núcleo e Despertador | Contrato atual auditado; custos recursivos calculados | **EXISTENTE — PRESERVAR** |
| Pedra Marcada → Pedra Lapidada | Stonecutter, **1 bloco → 1 bloco** | **RECEITA CANDIDATA PRIORITÁRIA F0** |
| Pedra Marcada → variantes de escada/laje/muro | Rendimento equivalente ao vanilla | **APÓS F0** |
| Líquen dos Vestígios | Cultivo por colheita/replantio do próprio item | **SELECIONADO F1; não adicionar semente** |
| **Argamassa de Marcas** | **2 Líquens + 1 bola de argila + 1 cascalho → 4 Argamassas** | **HIPÓTESE NUMÉRICA PARA 6.8** |
| Argamassa + stone | **1 Argamassa consumida + 1 stone → 1 Pedra Marcada** | **MECÂNICA CANDIDATA MID** |
| **Cinzel de Marcas** | Ferro+cobre+graveto+Pedra Lapidada; durabilidade hipotética 128 | **CONDICIONADO AO TESTE A/B** |
| **Leitor de Colônia** | Redstone+cobre+Líquen+Pedra Lapidada | **CONDICIONADO A F1 E USO REAL DE REDSTONE** |
| Estações custom, Nódulo, Cristal, Hera | Sem consumidores aprovados; não fabricar demanda artificial | **ADIADOS** |
| Câmara do Limiar | Ainda sem recompensa funcional de longo prazo aprovada | **BLOQUEADA PARA PRODUÇÃO FINAL** |
| Progressão do sono | Manter receita atual até medir se realmente antecipa viagens | **REBALANCEAMENTO EM TASK SEPARADA** |

**Convenções:** EXISTENTE é código/dependência real da branch; CANDIDATA é proposta a testar, ainda não arquivo JSON; CONDICIONADO requer prova de vantagem; ADIADO não recebe ID/asset/recipe novo. Os números desta 6.6 não foram calibrados por playtest.

---

# 1. RECEITAS R0 — AUDITORIA DA BASELINE

Lidos os arquivos reais:

- `src/main/resources/data/dreamsdimensions/recipe/dream_necklace.json`;
- `src/main/resources/data/dreamsdimensions/recipe/dream_infused_stone.json`;
- `src/main/resources/data/dreamsdimensions/recipe/oneiric_core_block.json`;
- `src/main/resources/data/dreamsdimensions/recipe/oneiric_awakener.json`;
- receitas smelting/blasting de `blue_dream_stone`;
- `loot_table/blocks/dream_ore.json` e `deepslate_dream_ore.json`;
- `ModItems.java` e `ModBlocks.java`, contratos 5.3–5.7.

## 1.1 Insumos por craft confirmado

| Item atual | Ingredientes verificados | Output | Status |
|---|---|---:|---|
| Colar `dream_necklace` | **3 Dream Dust**, 3 cobre, 1 fragmento de ametista | 1 | EXISTE |
| Pedra Infundida `dream_infused_stone` | **2 Dream Dust**, 2 stone vanilla | 1 | EXISTE |
| Núcleo `oneiric_core_block` | **4 Pedras Infundidas**, **4 Dream Dust**, 1 clock | 1 | EXISTE |
| Despertador `oneiric_awakener` | 1 Núcleo, 1 pena, 1 glass bottle | 1 | EXISTE |

**Cálculo recursivo correto:**

```text
4 Pedras Infundidas = 4 × (2 stone + 2 Dust)
                   = 8 stone + 8 Dream Dust

1 Núcleo = 4 Pedras Infundidas + 4 Dust + 1 clock
         = 8 stone + 12 Dream Dust + 1 clock

1 Despertador = 1 Núcleo + 1 pena + 1 glass_bottle
              = 8 stone + 12 Dream Dust + 1 clock + 1 pena + 1 frasco
```

O clock vanilla usa **4 lingotes de ouro e 1 redstone**. A receita vanilla de **3 vidros entrega 3 frascos**, portanto custo de vidro deve considerar lote e excedente, não 3 vidros para cada Despertador.

## 1.2 Custo por uma série de expedições

Para **um Colar preservado** e **N Despertadores de retorno**:

- **Dream Dust = 3 + 12N**;
- **stone = 8N**;
- **gold = 4N**, **redstone = N**, **feather = N**, **glass_bottle = N**;
- **copper = 3** e **amethyst_shard = 1**;
- vidro mínimo para frascos, sem estoque, = **3 × ceil(N/3)**.

| Meta | Dust | Stone | Ouro | Redstone | Penas | Frascos | Cobre | Ametista |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| Colar + 1 retorno | **15** | 8 | 4 | 1 | 1 | 1 | 3 | 1 |
| Colar + 3 retornos | **39** | 24 | 12 | 3 | 3 | 3 | 3 | 1 |
| Colar + 5 retornos | **63** | 40 | 20 | 5 | 5 | 5 | 3 | 1 |

**Não confundir:** Dream Ore e Deepslate Dream Ore são fontes do Dust por loot Overworld, com Fortune e Silk Touch. Contagem de minério por chunk não equivale a tempo de obtenção em Survival. As ruínas de recuperação **das dimensões** têm chance teórica **configurada de 25%** de Despertador por baú, sem Dust, e não serão alteradas.

## 1.3 Diagnóstico

O Despertador já tem custo material relevante (12 Dust), mas sua progressão ainda é principalmente **quantidade de mineração + crafting vanilla**. Não existe uma ação nova de processamento que prove preparo técnico Mid/Late. Isso **não** autoriza aumentar para 50 Dust nem inserir um boss/minério raro como ingrediente arbitrário.

**Decisão:** R0 continua intacta. A Task 6.8 mede se o tempo de obter **3, 15 e 39 Dust**, junto dos outros materiais, está coerente com a janela desejada. A revisão da receita/entrada, se necessária, precisa ser **task própria** com migração e QA.

---

# 2. QUATRO ECONOMIAS CONECTADAS, MAS NÃO OBRIGATÓRIAS

**E1 — Geologia → construção:** Pedra Marcada natural → pedra lapidada → variantes → arquitetura.

**E2 — Natureza → processamento:** Líquen natural → transplante → cultivo → Argamassa → Pedra Marcada reproduzível localmente → obras Late.

**E3 — Exploração → engenharia:** Marcos, Abrigos e Galerias → suprimentos/conhecimento; Cinzel e Leitor entram apenas se entregarem ações melhores que vanilla.

**E4 — Preparação onírica:** Dream Ore → Dust → Colar e cadeia de Núcleo/Despertador → expedição com retorno consumível.

**Regras do grafo:**
1. Cada saída possui fonte Survival, ação, consumidor e consequência.
2. Não há dependência que exija um resultado para fabricar o próprio pré-requisito.
3. Não há receita de Argamassa/Líquen/mob → Dream Ore ou Dream Dust.
4. POIs não detêm ingrediente obrigatório de F0/F1, Argamassa, primeiro Colar ou primeiro retorno.
5. Uma estação é criada somente depois que **duas famílias independentes** exigirem processo que crafting/stonecutter/furnace não façam.
6. Renovabilidade de material **decorativo** não equivale a renovabilidade de recurso **premium**.
7. Early, Mid e Late são estágios de capacidade e preparo, não quests que exigem fazer POI-01 antes de POI-02.
8. Uma receita “para talvez utilizar depois” não é receita aprovável.

## 2.1 DAG canônico

```text
[Vanilla Survival]
     |
     +--> [F0 Pedra Marcada natural]
     |       +--> Stonecutter 1:1 --> [Pedra Marcada Lapidada] --> [Construção]
     |       |                                  +--> [Cinzel opcional]
     |       |                                  +--> [Leitor opcional]
     |       +--> [POI-01 Marco = amostra, não fonte exclusiva]
     |
     +--> [F1 Líquen natural] --> [Cultivo renovável] --> [Jardim]
     |                                  |
     |                                  +--> [Argamassa] <-- [Argila + Cascalho]
     |                                            |
     +--> [Stone vanilla] -----------------------+
     |                                            |
     |                                     [1 Stone Marcada]
     |                                            |
     |                                       [Obra Late]
     |
     +--> [POI-02/03] --> [Abrigo/travessia/suprimentos/arquitetura]
     +--> [POI-04] --> [Recompensa funcional ainda NÃO aprovada]
     |
     +--> [Dream Ore / Deepslate Dream Ore] --> [Dream Dust]
                      |                              |
                      +--> [Colar: 3 Dust + cobre + ametista]
                      |
                      +--> [4 Infused Stone + 4 Dust + clock]
                                              |
                                         [1 Núcleo]
                                              |
                                       [pena + frasco]
                                              |
                                        [Despertador]
                                              |
                                   [1 retorno confirmado]
                                              |
                                   [1 Despertador consumido]
```

**Observação:** o grafo é de **materiais e desenvolvimento**. Um jogador pode encontrar o Abrigo antes do Marco e jogar sem construir nenhum Leitor.

---

# 3. F0 — RECEITAS DA PEDRA MARCADA

**IDs candidatos de 6.2:** `dreamsdimensions:marked_stone`, `dreamsdimensions:cut_marked_stone`. **Não existem no runtime atual.**

## 3.1 Receita selecionada para especificação

```text
1 marked_stone → stonecutter vanilla → 1 cut_marked_stone
```

- **1:1**, sem consumo de Dust, sem estação nova.
- Origem de `marked_stone`: worldgen natural F0, não POI obrigatório.
- Saída: decoração e construção funcional (bloco físico, loot e recuperação corretos).
- A receita só entra no datapack após IDs/loot/assets/QA do F0.

**Fallback condicionado:** crafting 2×2 com 4 marked_stone → 4 cut_marked_stone, se testadores Early precisarem de alternativa ao Stonecutter. Não criar rendimento melhor do que 1:1.

## 3.2 Variantes após F0

| Transformação | Stonecutter | Crafting opcional | Regras |
|---|---|---|---|
| Escadas | 1 cut block → 1 stair | 6 blocks → 4 stairs | Sem reversão com lucro |
| Lajes | 1 cut block → **2 slabs** | 3 blocks → **6 slabs** | Duas lajes somam um volume de bloco |
| Muros | 1 cut block → 1 wall | 6 blocks → 6 walls | Conexão e loot corretos |

Não converter 2 lajes em 2 blocos integrais; não trocar forma via Cinzel. Variações novas aguardam qualidade de arte, uso real e revisão 6.7–6.9.

---

# 4. F1 — LÍQUEN DOS VESTÍGIOS COMO RECURSO VIVO

**ID candidato:** `dreamsdimensions:vestige_lichen`.

**Não criar** sementes, fibra ou pó orgânico extra. O próprio item de planta colhida deve permitir replantio.

`habitat natural → colheita adequada → transplante → crescimento controlado → excedente → jardim ou Argamassa`.

**Gates:** suporte/replantio alcançáveis em base; ao menos duas colheitas reais; crescimento com saldo positivo sem player-tick scan ou chunks carregados à força; diferença perceptível frente ao Glow Lichen vanilla; coleta não destrói todo estoque sem alternativa. A Mariposa da 6.4 pode pousar como interação opcional, **não** é requisito de cultivo.

**Se F1 não funcionar, a receita de Argamassa com Líquen fica bloqueada**. Não esconder falha de economia exigindo matar Mariposa nem gerar item orgânico livre em todo chunk.

---

# 5. MID — ARGAMASSA DE MARCAS: RECEITA E CUSTOS

**ID candidato:** `dreamsdimensions:marked_mortar`.  
**Estado:** proposta concreta para **testar na 6.8**, não receita implementada.

## 5.1 Proposta de receita por lote

```text
2 dreamsdimensions:vestige_lichen
1 minecraft:clay_ball
1 minecraft:gravel
            |
    crafting_shapeless
            v
4 dreamsdimensions:marked_mortar
```

Argila é aglutinante, cascalho é agregado e Líquen é o material de acabamento. A receita evita criar Hera/fibra/minério técnico para explicar um composto arquitetônico.

## 5.2 Uso no mundo

```text
1 minecraft:stone (bloco colocado)
+ 1 marked_mortar (consumida)
= 1 dreamsdimensions:marked_stone (mesma posição)
```

**Contrato de segurança:** converter **somente stone vanilla full block**; validar servidor, distância, mão, estado, Creative/Adventure, claims e permissão antes da alteração. **Uma conversão confirmada consome uma unidade**; falhas não consomem; não gerar drop/XP. Não converter minério, stone brick, núcleo, Pedra Infundida, BlockEntity ou blocos de outros mods. Dois jogadores sobre a mesma pedra não devem duplicar gasto/saída. Sem transformação em raio ou scan por tick.

**Crafting equivalente opcional:** 1 stone item + 1 Argamassa → 1 marked_stone item. Precisa provar equivalência com uso no mundo e inexistência de recipe remainder ou shift-click/Crafter que multiplique materiais.

## 5.3 Custos de construção (hipótese)

Rendimento 4 Argamassas por craft; cada uma atua sobre 1 stone.

| Obra | Stone | Argamassa | Lotes | Líquen | Clay balls | Gravel |
|---|---:|---:|---:|---:|---:|---:|
| 4 blocos | 4 | 4 | 1 | 2 | 1 | 1 |
| 16 blocos | 16 | 16 | 4 | 8 | 4 | 4 |
| 64 blocos | 64 | 64 | 16 | 32 | 16 | 16 |
| 256 blocos | 256 | 256 | 64 | 128 | 64 | 64 |

`minecraft:stone` continua exigindo massa real: obter com Silk Touch ou fundir cobblestone consumindo tempo/combustível. A 6.8 deve medir se o jogador consegue cultivar 32/128 unidades de Líquen de forma razoável. **Não alegar que a proposta 2+1+1→4 já está balanceada.**

**Progressão resultante:** Early minera; Mid cultiva e processa; Late produz material decorativo em escala. O Late reduz atrito pela infraestrutura, sem criar minério de sonho gratuitamente.

---

# 6. MID — CINZEL E LEITOR (CONDICIONADOS)

## 6.1 Cinzel de Marcas

ID: `dreamsdimensions:marked_chisel`, **não registrado**.

**Receita candidata:** 1 `minecraft:iron_ingot` + 1 `minecraft:copper_ingot` + 1 `minecraft:stick` + 1 `cut_marked_stone` → **1 Cinzel**.

**Durabilidade hipotética:** **128 transformações válidas**. Ação: alternar **`marked_stone ↔ cut_marked_stone`** em um bloco completo **in situ**. Sem drop, XP, alteração de massa, perda de estado, conversão de stairs/slabs/walls ou minérios. Durabilidade é consumida somente se alteração foi confirmada no servidor e autorizada.

**Gate A/B:** construtor realmente poupa esforço em relação a quebrar, recolher, passar no Stonecutter e recolocar? Se não, **cancelar Cinzel**, não adicionar receita cara.

## 6.2 Leitor de Colônia

ID: `dreamsdimensions:lichen_colony_sensor`, **não registrado**.

**Receita candidata:** 2 `minecraft:redstone` + 1 cobre + 1 `vestige_lichen` + 1 `cut_marked_stone` → **1 Leitor**.

**Sinal experimental:** 0/1/2/3/4 posições vizinhas elegíveis → nível redstone **0/4/8/12/15**. Entrada e saída precisam ter utilidade em jardim/circuito real. **Alerta:** o Líquen pode ser cobertura em face de bloco, não full block adjacente; F1 deve validar fisicamente onde a planta ocupa espaço antes de escrever sensor. Se a geometria impedir ler vizinhos, revisar design em vez de registrar dispositivo que sempre mostra zero.

**Gate A/B:** benefício contra observer/comparator/repeater, atualização por vizinho **sem scan mundial**, comportamento correto em chunks e multiplayer.

**Nenhuma das duas receitas exige Nódulo Reverberante.** Um novo minério continua condicionado à criação **de uma capacidade genuína** que o justifique.

---

# 7. LOOT DE ESTRUTURAS, MOBS E PRÊMIOS

| Conteúdo de 6.4/6.5 | Valor no grafo | Novo item aprovado? |
|---|---|---|
| POI-01 Marco | Amostra física/visual de Pedra Marcada, sem baú por padrão | Não |
| POI-02 Abrigo | Utilidade de abrigo/bancada vanilla, suprimentos | Não |
| POI-03 Galeria | Escolha de rotas, amostras, suprimentos de exploração | Não |
| POI-04 Câmara | Risco/arquitetura; **ainda falta recompensa funcional de longo prazo** | **Não — BLOQUEADO** |
| M1 Mariposa | Informação ecológica e habitat | Não |
| M2 Habitante | Territorialidade, fuga/confronto e domínio de rota | Não |
| M3 Guardião opcional | Desafio de POI-04 apenas se justificar | Não |

**A Câmara não pode ser enviada à produção só com um “Livro Antigo” ou Cristal Velado sem consumidor.** Possíveis prêmios futuros devem competir por valor: técnica de arquitetura realmente útil, circuito botânico aprovado ou processo de preparação não trivial; sem transformar POI em requisito para o primeiro Colar/Despertador.

A Task 6.9 deve marcar formalmente **POI-04: BLOCKED_BY_REWARD_DESIGN**. Um protótipo de layout não resolve o requisito.

**Loot de baús:** persistente uma única vez, compartilhado pelo mundo, sem reabastecimento por relog, sem Dust massificado e sem copiar **25% de Despertador** das ruínas oníricas. Nenhum mob dá pó/essência obrigatória.

---

# 8. ESTAÇÕES E MINÉRIOS — POR QUE CONTINUAM ADIADOS

**Não aprovar Mesa de Estabilização, Oficina de Vigília nem Banco de Instrumentos.** F0 é Stonecutter, Argamassa e ferramentas usam crafting, Cinzel é interação e Leitor é redstone. **Nenhum** processo necessita de receita custom com tempo/inventário/GUI. Uma estação custom só será reaberta se **dois usos independentes comprovados** demandarem experiência além de vanilla.

**Não registrar Nódulo Reverberante, Fragmento Reverberante, Cristal Velado nem Hera/fibra.** Atualmente faltam consumidores próprios, e colocá-los em receitas de Despertador/Argamassa seria demanda inventada para justificar um recurso.

**Não adicionar:** armadura de novo tier, comida de buff genérico, drop de Guardião obrigatório, chave de estrutura, item de pesquisa sem função, flor que produz Dream Dust.

---

# 9. PREPARAÇÃO ONÍRICA: ALVO MID → LATE, SEM FIM DE JOGO

| Estágio | Experiência desejada | Papel dos itens |
|---|---|---|
| **Early** | Descoberta e autonomia no Overworld | Conhecer Dust/vestígios; viagens não são atividade recorrente |
| **Mid inicial** | Mineração mais profunda, ametista/redstone/ouro, preparação e risco | Primeira expedição é decisão significativa; retorno preparado ou busca real |
| **Mid tardio** | Base, suprimentos e reservas de Dust | É viável planejar várias expedições, mas cada uma tem custo |
| **Late saudável** | Infraestrutura, mobilidade, domínio e reposição de recursos | Viagens consistentes **não gratuitas**; risco permanece, sem boss final |

**Defeito conceitual que não podemos ignorar:** o Colar custa 3 Dust, cobre e ametista, então **se a entrada estiver habilitada** a receita por si só não impede viajar relativamente cedo. Custo de Despertador alto **não reduz chance de entrada**, apenas dificulta retornar preparado. Separar política de entrada de economia de retorno.

**Decisões proibidas nesta fase:** aumentar Dust para 50–100 por grind, exigir cristal da dimensão para primeira saída, obrigar boss/POI-04 para primeiro Colar, alterar chance de despertar sem medir, permitir morte/cama como retorno grátis.

**Plano adotado:** manter as receitas R0 até a Task 6.8 medir obtenção de 3/15/39 Dust, disponibilidade dos ingredientes vanilla, tempos de recuperação nas duas dimensões e taxa de expedições sustentadas. Se a qualidade de progressão for insuficiente, criar **task específica** para ajustar preparação/entrada/receitas com migração, CI, dedicated server e Survival, sem arriscar softlock.

Preservar uso de **60 ticks** do Despertador e consumo **exatamente uma vez após retorno confirmado**. O item de recuperação continua sendo encontrado por loot raro nas ruínas dimensionais; morte/cama não são mecanismo de saída. Não ativar automaticamente o `dream_entry_enabled` como efeito do novo crafting.

---

# 10. ECONOMIA, CUSTOS E PREVENÇÃO DE EXPLOITS

## 10.1 Classificação de fontes e sinks

| Recurso | Fonte | Sink | Renovabilidade |
|---|---|---|---|
| Dream Dust | Minérios existentes no Overworld | Colar, Núcleo/Despertador | Finito por depósito, novos chunks |
| Pedra Marcada | Formação F0; Argamassa no Mid | Arquitetura, Lapidação, Cinzel/Leitor condicionais | Produção arquitetônica com custo |
| Líquen | Habitat/cultivo F1 | Jardins, Argamassa, Leitor condicional | Renovável se cultivo funcionar |
| Argamassa | Líquen + argila + cascalho | Uma conversão por unidade | Repetível com insumos pagos |
| Cinzel | Metais + Pedra Lapidada | Durabilidade por alteração | Craftável, não item de tier |
| Leitor | Redstone + cobre + Líquen + Pedra Lapidada | Construção/engenharia | Craftável, sem minério novo |
| POIs | Worldgen + arquitetura | Exploração e material recuperável | Finito local |
| Cristal/Nódulo | **Não aprovado** | **Nenhum** | Não definir |

## 10.2 Ciclos proibidos

- `stone + mortar → marked_stone → stone + mortar`: devolução do insumo consumido;
- `1 full block → 2 full blocks`;
- `1 marked stone → 2 slabs → 2 marked stone`: duplicação de volume;
- `infused_stone → Dream Dust com ganho`;
- `moth farm → Dust`;
- `abrir baú de novo → novo Despertador`;
- `repetir puzzle → loot reiniciado`;
- `um jogador altera stone, outro consome o mesmo item e ambos ganham recompensa`.

## 10.3 Ciclos legítimos

- Crescer/colher Líquen após investimento de tempo/ambiente;
- Fabricar Argamassa consumindo ingredientes novos e transformá-la em construção;
- Cinzel alternar acabamento `marked_stone ↔ cut_marked_stone` **sem drop** e consumindo durabilidade;
- Conversão 1 full block → 2 slabs cujo volume somado é 1 full block;
- Colar preservado e Despertador consumido por retorno, com economia recorrente.

---

# 11. MATRIZ DE RECEITAS PARA 6.7–6.9

| Código | Output | Inputs | Resultado | Método | Estado |
|---|---|---|---|---|---|
| R0-01 | Colar | 3 Dust + 3 cobre + ametista | 1 | Crafting | **EXISTE** |
| R0-02 | Infused Stone | 2 stone + 2 Dust | 1 | Crafting | **EXISTE** |
| R0-03 | Núcleo | 4 infused + 4 Dust + clock | 1 | Crafting | **EXISTE** |
| R0-04 | Despertador | Núcleo + pena + glass bottle | 1 | Crafting | **EXISTE** |
| F0-01 | Cut Marked Stone | 1 Marked Stone | 1 | Stonecutting | **SELECIONADA** |
| F0-02 | Cut Marked Stone | 4 Marked Stone | 4 | Crafting | **OPCIONAL** |
| F1-00 | Líquen | Habitat + cultivo | Colheita | Plantio | **SELECIONADO** |
| M-01 | Argamassa | 2 Líquen + 1 clay ball + 1 gravel | **4** | Crafting shapeless | **HIPÓTESE 6.8** |
| M-02 | Marked Stone | 1 mortar + 1 stone no mundo | 1 bloco | Interação | **CANDIDATO** |
| M-03 | Marked Stone | 1 mortar + 1 stone item | 1 item | Crafting | **OPCIONAL** |
| M-04 | Cinzel | ferro + cobre + graveto + cut stone | 1 ferramenta | Crafting | **CONDICIONAL** |
| M-05 | Leitor | 2 redstone + cobre + Líquen + cut stone | 1 bloco | Crafting | **CONDICIONAL** |
| F0-V | Escadas/lajes/muro | cut stone | Vanilla-equivalente | Stonecutter | **APÓS F0** |
| P04 | Recompensa da Câmara | **A definir** | **A definir** | — | **BLOQUEADA** |

A Task 6.8 deve revisar outputs e custos. **Nenhum número de receita candidata significa valor já compilado ou calibrado.**

---

# 12. QA E IMPLEMENTAÇÃO FUTURA

## 12.1 Validações obrigatórias

1. Parser/codec dos JSON reais; todas as entradas e outputs devem resolver IDs de registry.
2. Calcular o grafo recursivo R0: **12 Dust** por Despertador e **3 Dust** por Colar; nenhuma regressão.
3. Testar fontes naturais dos novos ingredientes antes de criar consumers.
4. Provar que F0 oferece bloco suficiente para uma pequena construção no Early.
5. Provar que F1 oferece **saldo positivo** e duas colheitas reais sem mob obrigatório.
6. Aprovar/rejeitar Argamassa 2+1+1→4 com obras de **64 e 256 blocos** e tempo de fabricação.
7. Testar uso 1:1 e proteção/claims/Adventure/Creative no servidor; clique inválido não consome.
8. Testar crafting normal, shift-click, Crafter, remainder e duas contas; nenhum item duplication.
9. Testar Cinzel e Leitor por A/B e performance; cancelar se o valor for marginal.
10. Testar recompensas POI sem crafting essencial de baú raríssimo, restart ou outro jogador.
11. Testar `recipe` IDs em caminhos singulares `data/dreamsdimensions/recipe/` e modelos/traduções `pt_br`/`en_us`.
12. Rodar Gradle, dedicated server, carregamento de datapacks, saves antigos, CI multi-seed e playtest Survival.

**Documentação oficial:** [NeoForge Recipes](https://docs.neoforged.net/docs/resources/server/recipes/), [Built-In Recipes](https://docs.neoforged.net/docs/resources/server/recipes/builtin/), [Custom Recipes](https://docs.neoforged.net/docs/resources/server/recipes/custom/), [Primer 26.3](https://docs.neoforged.net/primer/docs/26.3/). As páginas públicas de receitas podem estar identificadas como **26.1**; verificar formatos e API com a dependência **26.3 real** antes de escrever código. A baseline dos JSON atuais é a fonte de verdade sobre o que funciona no projeto.

## 12.2 Métricas para a Task 6.8

- Mediana/P90 de tempo até 3/15/39 Dust, com e sem Fortune;
- tempo até a primeira Pedra Marcada, quantidade após uma sessão de mineração;
- taxa de Líquen replantável e quantas unidades geradas no Mid;
- custo real de uma obra de 64 e 256 blocos por mineração vs Argamassa;
- diferença real de esforço ao usar Cinzel;
- valor do Leitor em projeto redstone sem scanner de mundo;
- tempo de acesso/recuperação às ruínas de ambas as dimensões;
- repetibilidade por seed, inventário, distância, estado de chunk, multiplayer;
- integridade das receitas e ausência de ciclos economicamente lucrativos.

Seeds históricas de regressão: `14011`, `20261108`, `-904732`, `73199013`. **Não há medições novas** de obtenção ou raridade nesta Task documental.

## 12.3 Comandos de laboratório

**Estes IDs já existem (uso de /give não prova obtenção natural):**

```mcfunction
/seed
/give @s dreamsdimensions:dream_dust 15
/give @s dreamsdimensions:dream_necklace
/give @s dreamsdimensions:dream_infused_stone 4
/give @s dreamsdimensions:oneiric_core_block
/give @s dreamsdimensions:oneiric_awakener
/gamemode survival
```

**Estes IDs ainda NÃO existem:** `dreamsdimensions:marked_mortar`, `marked_chisel`, `marked_stone`, `cut_marked_stone`, `vestige_lichen`, `lichen_colony_sensor`. Não fornecer comandos /give ou /setblock falsamente funcionais. Futuras PRs devem incluir comandos concretos com IDs efetivos e `/tp` **somente com coordenadas reais da seed testada**.

---

# 13. CRONOGRAMA DE DEPENDÊNCIAS

| Próxima Task | O que recebe | Entrega exigida |
|---|---|---|
| **6.7** | F0/F1/Argamassa e condicionais, paleta de uso, custos e status | Direção Visual e Assets, sem modelar conteúdos adiados |
| **6.8** | Quantidades de receitas candidatas, custos por obra, R0 3+12N Dust | Medição, aprovação/ajuste de raridade e custo |
| **6.9** | DAG, status, gates e anti-softlocks | Roadmap executivo de PRs pequenas e QA |
| **Implementação F0** | Pedra natural + lapidação 1:1 | Loop completo de mineração→construção |
| **Implementação F1** | Líquen, habitat/cultivo | Loop completo de coleta→replantio→uso |
| **Implementação MID** | Argamassa, Cinzel/Leitor se aprovados | Produção arquitetônica e ferramenta/redstone opcional |
| **Rebalanceamento onírico** | Diagnóstico + relatório 6.8 | Task específica com diff, migração e Survival |
| **POI-04** | Falta prêmio funcional convincente | **Não produzir** antes de resolver o gate |

**Ordem executiva sugerida:** F0 → F1 → Argamassa → Marcos/Abrigos → Mariposa → ferramentas condicionadas → Galeria → conteúdo horizontal Mid/Late (Câmara só com benefício real). **Não** interpretar isso como uma sequência obrigatória de visitas no jogo.

---

# 14. CRITÉRIOS DE ACEITE

## Documentação — concluída

- [x] Inspecionou recipes e loot atuais no repositório.
- [x] Calculou custos recursivos reais para Colar, Núcleo, Despertador e múltiplos retornos.
- [x] Definiu quatro economias paralelas e DAG de origem→transformação→consumo.
- [x] Selecionou stonecutter 1:1 e tratou variantes com conservação de volume.
- [x] Definiu renovabilidade do Líquen sem nova semente/fibra.
- [x] Definiu receita experimental 2+1+1→4 de Argamassa e custo de 64/256 blocos.
- [x] Definiu receitas condicionadas e testes A/B de Cinzel/Leitor.
- [x] Rejeitou novo minério/estação/drop sem função, e explicitou gate POI-04.
- [x] Separou dificuldade de primeira entrada, primeiro retorno e viagens recorrentes.
- [x] Preservou lógica dos sonhos e ausência de End Game próprio.
- [x] Especificou QA de receita, loot, proteção, multiplayer, saves e cálculo de custos.
- [x] Não alterou nenhum arquivo funcional do mod.

## Gameplay — não executado

- [ ] F0 e F1 gerados/testados em Survival.
- [ ] Argamassa calibrada e livre de exploit.
- [ ] Cinzel/Leitor aprovados ou cancelados pelo playtest.
- [ ] Câmara possui recompensa horizontal convincente antes de release.
- [ ] Custo real de obtenção Dust e ritmo Early/Mid/Late medidos em 6.8.
- [ ] Servidor dedicado, multiplayer, Recipe Book e worldgen aprovados em futuras PRs.

**Status:** **Task 6.6 concluída como planejamento**. Nenhum novo item, bloco ou recipe foi adicionado.

---

# 15. VEREDITO DE DIREÇÃO

**A progressão do DreamsDimensions deve gerar competências que se acumulam.** O construtor descobre rocha e depois aprende a produzi-la em escala; o naturalista encontra planta e aprende a cultivá-la; o engenheiro decide se precisa de mecanismos próprios; o explorador busca estruturas por experiência, não por chave universal.

A economia do Colar e do Despertador está documentada sem maquiar a dificuldade: **3 Dust para o Colar, 12 Dust para cada Despertador**, com custos adicionais em vanilla e riscos de expedição. A consistência dos sonhos só deve aparecer após preparação e investimento no Mid tardio/Late; se os testes mostrarem outro ritmo, a correção virá de sistemas e balanceamento baseado em evidência, não de inflar material.

**Próxima Task: 6.7 — Direção Visual, Texturas, Modelos e Assets.** O primeiro material e a primeira planta devem comunicar ao jogador por que existem antes que fabriquemos uma coleção de cristais e mobs sem propósito.

---

*Fim — Task 6.6, documentação de crafting e progressão. Sem mudanças funcionais.*
