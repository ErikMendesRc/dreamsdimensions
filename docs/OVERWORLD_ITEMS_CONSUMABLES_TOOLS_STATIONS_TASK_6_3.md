
# TASK 6.3 — ITENS, CONSUMÍVEIS, FERRAMENTAS E ESTAÇÕES DO OVERWORLD

**Projeto:** DreamsDimensions  
**Natureza:** Systems Design / Contrato de conteúdo e protótipos; **sem alteração funcional**  
**Versão:** 1.0 — direção de design para as Tasks 6.4–6.9  
**Data:** 12/11/2026  
**Branch:** `migration/minecraft-26.3`  
**HEAD auditado:** `5f133761dfda0ed2b9e96f595009130402c947f6`  
**Base de direção:** [Game Design Bible — 6.0](GAME_DESIGN_BIBLE_TASK_6_0.md), [Pilares do Overworld — 6.1](OVERWORLD_GAMEPLAY_PILLARS_TASK_6_1.md), [Recursos, Geologia, Flora e Blocos — 6.2](OVERWORLD_RESOURCES_GEOLOGY_FLORA_BLOCKS_TASK_6_2.md).

> **Tese da 6.3:** um item merece existir quando oferece ao jogador uma ação, escolha ou capacidade significativa; um consumível precisa ter custo e benefício verificáveis; uma estação só é necessária quando recursos vanilla não conseguem executar a experiência desejada. Nossa expansão não se resume ao Colar e ao Despertador. **Early:** descobrir e experimentar. **Mid:** desenvolver técnicas, especializar-se e ganhar eficiência. **Late:** infraestrutura e atividades sustentáveis, sem um End Game próprio.

---

# 0. RESUMO EXECUTIVO E STATUS

A Task 6.2 selecionou Pedra Marcada para F0 e Líquen dos Vestígios para F1. Ela **não aprovou** o Nódulo de Reverberação nem Cristal Velado. Esta Task define equipamentos, consumíveis e estações que poderiam dar continuidade a essas famílias **sem inventar demanda para recursos inexistentes**.

| Prioridade | Nome e ID candidato | Categoria | Estágio | Decisão 6.3 | Motivo |
|---|---|---|---|---|---|
| A0 | Stonecutter / bancada vanilla | Processo existente | Early | **PREFERIDO / SEM ITEM NOVO** | Lapidar Pedra Marcada sem estação adicional. |
| A0 | Tesoura e bloco de Líquen replantável | Item vanilla + bloco 6.2 | Early | **PREFERIDO / SEM SEMENTE NOVA** | Cultivar e decorar com F1 sem criar tokens. |
| A1 | **Argamassa de Marcas** — `dreamsdimensions:marked_mortar` | Consumível construtivo | Mid → Late | **SELECIONADO PARA ESPECIFICAÇÃO** | Transformação de `minecraft:stone` já colocado em Pedra Marcada; permite obras em escala com cultivo e materiais comuns. |
| B1 | **Cinzel de Marcas** — `dreamsdimensions:marked_chisel` | Ferramenta | Mid → Late | **PROTÓTIPO CONDICIONADO** | Alternar acabamento de blocos completos no mundo, sem quebrar/recolocar. |
| B2 | **Leitor de Colônia** — `dreamsdimensions:lichen_colony_sensor` | Bloco de redstone | Mid → Late | **EXPERIMENTO CONDICIONADO** | Medir flora diretamente adjacente e emitir sinal analógico; comparação obrigatória com vanilla. |
| C1 | Sonda de Vestígios / Lente / Instrumento de Medição | Ferramentas alternativas | Mid | **NÃO SELECIONADOS AGORA** | F0 usa pistas ambientais; 6.5 deve provar necessidade de item. |
| C2 | Nódulo de Reverberação / Fragmento Reverberante | Mineral técnico 6.2 | Mid | **CONTINUA CONDICIONADO** | Nenhum mecanismo aprovado exige outro minério neste momento. |
| C3 | Hera do Repouso / fibra | Orgânico 6.2 | Mid/Late | **CONTINUA CONDICIONADO** | A Argamassa pode consumir o próprio Líquen, sem fibra nova. |
| D1 | Mesa de Estabilização / Banco de Instrumentos | Estação custom | Mid/Late | **ADIADOS** | Crafting, stonecutter, item use e redstone vanilla bastam por enquanto. |
| D2 | Ampola de Estabilização | Consumível de proteção | Mid/Late | **ADIADA** | Falta ameaça ambiental concreta, específica e comunicada. |
| D3 | Cristal Velado e ferramentas/armaduras de tier superior | Materiais e equipamentos | Late | **ADIADOS** | Não existe benefício exclusivo comprovado que justifique obtenção difícil. |
| D4 | Mochila especial, GPS de ruína, alimento de buff genérico | Itens | Todos | **REJEITADOS** | Duplicam Bundle, mapa/lodestone, alimento ou poções vanilla. |

**Atenção:** todos os IDs novos são **conceituais**. Nenhum foi registrado; a decisão “selecionado” autoriza detalhamento nas Tasks 6.6–6.9, não implementação imediata.

## Estados formais

- **EXISTENTE / AUDITADO:** implementado e encontrado no HEAD da branch.
- **DIRETRIZ:** regra de produto estabelecida em 6.0/6.1.
- **SELECIONADO PARA ESPECIFICAÇÃO:** candidato com função identificada, sujeito a custos, arte e protótipo.
- **CONDICIONADO:** só permanece após teste A/B e demonstração de ganho.
- **ADIADO / REJEITADO:** não criar por inércia.
- **HIPÓTESE:** parâmetros e resultados sem evidência in-game.

---

# 1. AUDITORIA DA BASELINE E COMPATIBILIDADE

**Arquivos auditados:**

- `src/main/java/com/dreamsdimensions/mod/registry/ModItems.java`
- `src/main/java/com/dreamsdimensions/mod/registry/ModBlocks.java`
- `src/main/java/com/dreamsdimensions/mod/item/OneiricAwakenerItem.java`
- `src/main/java/com/dreamsdimensions/mod/event/CommonEvents.java`
- `src/main/resources/data/dreamsdimensions/recipe/`
- `docs/OVERWORLD_ECONOMY_TASK_5_6.md`
- `docs/OVERWORLD_RESOURCES_GEOLOGY_FLORA_BLOCKS_TASK_6_2.md`

O mod já registra `dreamsdimensions:dream_dust`, `dreamsdimensions:dream_necklace`, `dreamsdimensions:oneiric_awakener`, os BlockItems dos blocos dimensionais e os minérios do Overworld. **Não registra ainda ferramentas construtivas, reagentes de cultivo ou estação especializada de Overworld.**

- Colar: 3 Dream Dust + 3 cobre + ametista; equipamento de elegibilidade via Curios.
- Despertador: 12 Dream Dust indiretos via Pedra Infundida/Núcleo, consumido **somente após retorno confirmado**, com ciclo de uso de 60 ticks.
- Dream Ore / Deepslate Dream Ore: fontes naturais de Pó no Overworld, com loot/Fortune/Silk Touch e geração já contratados.
- As duas estruturas de ruínas existentes são das dimensões; a Task 6.3 não registra POIs de Overworld.
- Fragmento Errante e Vigia Adormecido são mobs do Dreamscape; não existem novos mobs de Overworld aprovados.

**Contrato de preservação:** não alterar IDs, receituário, custo dos artefatos, chance de sono, motor de viagem, respawn, salvamentos, estado legado de minério, loot ou geração dimensional. A revisão da dificuldade de preparar viagens é um trabalho de economia/migração **posterior**, não desculpa para adicionar recursos sem função.

**Defasagem identificada:** a receita atual pode permitir obtenção de artefatos antes do estágio desejado de progressão. Isso é uma **decisão pendente** de 6.6/6.8, não autorização para bloquear o sono ou refazer recipes nesta Task.

---

# 2. ARQUITETURA DE PROGRESSÃO POR CAPACIDADE

## 2.1 Early — reconhecer, obter, aplicar

Fluxo de material:

`Minecraft vanilla → encontrar Pedra Marcada → minerar → stonecutter/crafting vanilla → construir`.

Fluxo orgânico:

`encontrar Líquen dos Vestígios → colher com ferramenta apropriada → replantar → compor jardim ou parede viva`.

**Nenhuma ferramenta nova obrigatória.** O conteúdo aparece naturalmente no jogo; o jogador não precisa achar uma estação antes de descobrir que o mod existe.

**Ganho:** descoberta geológica, expressão arquitetônica, observação e cultivo.

## 2.2 Mid — reduzir atrito e especializar atividades

O jogador pode optar por:
- desenvolver produção de **Argamassa de Marcas** a partir de Líquen e materiais vanilla, em vez de minerar mais pedra decorativa;
- usar o **Cinzel** se o A/B comprovar ganho real ao construir;
- prototipar **redstone botânica** se o Líquen possuir propriedades legíveis para automação;
- explorar POIs de 6.5 e testar técnicas sem necessariamente portar uma sonda especial.

**Ganho:** ações novas, eficiência e possibilidades — não apenas exigência de mais ingredientes.

## 2.3 Late — produção sustentável e projetos

O jogador cultiva e coleta insumos comuns, constrói em escala e reaproveita ferramentas duráveis; conteúdo de desafio mantém utilidade opcional. Se houver mecanismo avançado, ele amplia aplicações de redstone sem tornar vanilla obsoleto.

Os sonhos tornam-se expedições consistentes após o **Mid tardio/Late**, mas nenhuma família de construção, flora ou redstone depende do Colar para começar.

**Sem endgame próprio, chefe final obrigatório ou item definitivo.**

---

# 3. CRITÉRIO ELIMINATÓRIO PARA NOVOS ITENS

Para cada candidato, preencher:

1. **Problema:** o que falta para o jogador fazer?
2. **Origem:** quais fontes legítimas de Survival fornecem seus insumos?
3. **Ação:** interação concreta, alvo, resposta e feedback.
4. **Capacidade:** o que fica possível ou muito melhor após obter?
5. **Comparador vanilla:** que solução já existe e por que ela não basta?
6. **Escassez/repetição:** é consumível ou durável; o Late permite reposição?
7. **Riscos:** dupes, claims, chunk loads, efeitos indevidos, economia, multiplayer.
8. **Independência:** tem valor sem Colar/Despertador?
9. **DAG:** todos os pré-requisitos existem antes da saída?
10. **Teste:** como reprovar em Survival se a ideia não funcionar?

Um item de decoração pode existir sem uma nova habilidade quando tiver valor estético evidente e fonte acessível. Mas **itens técnicos** não têm essa exceção.

---

# 4. A1 — ARGAMASSA DE MARCAS

**ID candidato:** `dreamsdimensions:marked_mortar`  
**Categoria:** consumível construtivo; Mid/Late.  
**Status:** selecionada para **especificação futura**, não registrada.  
**Pilares:** natureza + geologia + crafting + construção.

## 4.1 Problema

Pedra Marcada será uma formação natural finita por ocorrência. Para construir uma base com centenas de blocos, obrigar o jogador a percorrer cavernas indefinidamente pode transformar construção em grind. **Aprender a cultivar e fabricar um composto para reproduzir acabamento** é evolução por capacidade, não um atalho gratuito.

## 4.2 Cadeia de fantasia e gameplay

`explorar → minerar Pedra Marcada → construir F0 → encontrar/cultivar Líquen F1 → combinar Líquen + materiais vanilla → Argamassa → transformar pedra comum em Pedra Marcada → grandes projetos`.

O jogador aprende a **reproduzir a marca arquitetônica**, não a criar minério novo ou gerar Dream Dust. A técnica é replicável com investimento em cultivo, material e tempo.

## 4.3 Efeito especificado

**Interação principal:** com Argamassa na mão principal, clique direito em bloco **`minecraft:stone`** válido, em alcance vanilla.

**Resultado:** exatamente **um bloco de `minecraft:stone` torna-se um bloco completo `dreamsdimensions:marked_stone` na mesma posição**. Uma unidade de Argamassa é consumida **somente após conversão bem-sucedida** em Survival; nenhuma unidade ao falhar.

**Whitelist:** somente pedra comum; proibir minérios, blocos de outros mods, slabs, stairs, tile/block entities, spawners, chests, Pedra Infundida, Núcleo e material técnico.

**Sem área automática:** converte um bloco por uso, não raio de 9×9, não processa chunks ou estruturas.

**Sem drop:** troca de bloco não concede pedra extra, XP nem qualquer recurso mineral técnico. A Pedra Marcada resultante é o bloco construtivo normal, recuperável pelo loot de construção.

**Feedback:** som seco de argamassa, efeito discreto, textura muda, tooltip em português e inglês informando o alvo permitido.

**Proteção:** servidor valida posição, alcance, hand/stack, estado atual e regras de proteção/mods terceiros antes de mutar mundo. Não dar `SUCCESS` falso se a mutação falhou.

**Criativo:** não consumir, sem drops extras. Adventure/claims: recusar quando interação/modificação não autorizada.

## 4.4 Segunda interface candidata

A Task 6.6 pode permitir **crafting equivalente** de pedra + Argamassa no inventário para produzir Pedra Marcada. Essa interface só será aprovada se o rendimento seguir **a mesma economia**, sem devolução indevida de reagente e sem recipe remainder que duplique recursos.

A interface alternativa é conveniência de construção, não um segundo modo de imprimir material gratuitamente.

## 4.5 Receita e economia — apenas categorias, não quantidade aprovada

**Entradas candidatas:** Líquen dos Vestígios cultivado + argila/pedra ou outro aglutinante vanilla acessível. Não criar item de fibra, resina ou estação porque precisamos de “mais etapas”.

- **Early:** mineração natural continua sendo a origem principal.
- **Mid:** o cultivador pode produzir argamassa com recurso renovável e insumo vanilla.
- **Late:** produção arquitetônica planejável em escala; ainda consome reagente por bloco.
- **Nunca:** Argamassa → Dream Ore, Dream Dust, Fragmento Reverberante, Cristal Velado, XP ou item de retorno.

A Task 6.6 deve propor e calcular custos; a 6.8 deve comparar mineração natural e fabricação por tempo de Survival. Se Argamassa for inviável financeiramente, não melhorar artificialmente o output sem analisar a economia do Líquen.

## 4.6 Teste de aceitação

- F0 funciona **antes** da Argamassa; F1 produz Líquen renovável.
- Um alvo válido consome exatamente 1 Argamassa e converte exatamente 1 bloco.
- Clique em alvo inválido não consome nem bloqueia interação vanilla.
- Converter 100 pedras gera 100 Pedras Marcadas, sem drops/XP.
- Dois jogadores clicando a mesma pedra produzem **uma conversão e um consumo total**; a segunda interação falha após ver estado atualizado.
- Mundo com proteção/claims não deve ser ignorado; sem permissão, zero transformação e consumo.
- Save/restart e chunk unload conservam resultado.
- Testes de Creative/Adventure, offhand, alcance, borda de mundo e blocos não suportados.
- Uma obra de referência comprova benefício para construtor sem fabricar/destruir recursos premium.

**Gate:** Argamassa só vira implementação depois de F0/F1 homologados, grafo 6.6, visual 6.7 e custo 6.8. Se F1 não passar no teste de diferença vs Glow Lichen vanilla, reformular a cadeia antes de registrar item.

---

# 5. B1 — CINZEL DE MARCAS

**ID candidato:** `dreamsdimensions:marked_chisel`  
**Categoria:** ferramenta reutilizável; Mid/Late.  
**Status:** protótipo condicionado a A/B.

## 5.1 Problema

O jogador montou uma parede de Pedra Marcada e quer alternar o acabamento. Vanilla exige quebrar, recolher, lapidar no stonecutter e recolocar. A ferramenta proposta altera acabamento **no local**, com custo de durabilidade.

## 5.2 Comportamento mínimo

`marked_stone ↔ cut_marked_stone`, exatamente um bloco completo e sem drop.

**Não** alterar forma/volume: nunca converter full block em slab, stair ou wall; nunca gerar drop, XP ou devolver itens. Não trocar blocos com inventário/BlockEntity, orientação crítica ou estados incompatíveis.

- Clique com ferramenta em bloco permitido: muda para outro bloco completo da família.
- Um dano de durabilidade por transformação efetivada em Survival, sem custo em caso de falha.
- Apenas whitelisted IDs e posições modificáveis; `PASS` quando não elegível.
- Nenhum alcance extra, nova capacidade de mineração ou bônus de ataque.
- Não alterar Pedra Infundida, Núcleo, minério ou bloco de outro mod.
- Feedback sonoro e visual discreto. Tooltip deve enfatizar acabamento, não mineração.

## 5.3 Comparação com vanilla

| Critério | Stonecutter + picareta | Cinzel |
|---|---|---|
| Bloco já instalado | Quebrar e recolocar | Alterar na posição |
| Custo | Viagem, inventário, ferramentas | Durabilidade, item específico |
| Variações | Muitas via receitas vanilla | Apenas duas formas completas permitidas |
| Risco | Queda do bloco ou erro manual | Proteção/dupe se implementado incorretamente |
| Benefício demonstrável | Sempre disponível | Requer A/B real com construtores |

**Condição de aprovação:** reduzir trabalho real e ampliar expressão de construção de forma significativa. Se não for percebido como útil, descartar e manter stonecutter. Não inventar receita cara para justificar ferramenta.

## 5.4 Aquisição

Proposta: metais vanilla + material da Pedra Marcada; custo e durabilidade ainda **não fixados** (Task 6.6). Não usar Pó Onírico, cristal raro ou drop de mob como requisito.

## 5.5 QA obrigatório

Duas transformações retornam o bloco original sem criar/consumir bloco adicional; ferramenta perde durabilidade corretamente. Testar muro com 100 conversões, redstone/vizinhos, chunks, restart, Creative/Adventure, alcance, proteção e multiplayer.

---

# 6. B2 — LEITOR DE COLÔNIA: REDSTONE QUE LEVA FLORA EM CONTA

**ID candidato:** `dreamsdimensions:lichen_colony_sensor`  
**Categoria:** bloco funcional.  
**Status:** experimento condicionado, **não** aprovado para crafting.

## 6.1 Problema

Queremos um cruzamento útil entre natureza e mecanismos: um jardim cuja colonização por Líquen possa ser medida por redstone. Precisamos de comportamento novo além de um observer detectar mudança.

## 6.2 Protocolo de experimento

**Entrada:** quantidade de até **quatro posições horizontais imediatamente adjacentes** ocupadas por Líquen em estado considerado elegível pela especificação F1.

**Saída analógica proposta:**

| Vizinhos elegíveis | Força redstone |
|---:|---:|
| 0 | 0 |
| 1 | 4 |
| 2 | 8 |
| 3 | 12 |
| 4 | 15 |

**Esta tabela é experimento, não número final.** Deve existir uma razão prática para a leitura: monitoramento local de colonização, painel de jardim ou controle de sistema contextual. Se Líquen não tiver crescimento/estado significativo e o sensor não servir a projeto real, rejeitar.

**Política técnica:** leitura somente de quatro blocos vizinhos, recalcular por atualizações locais de vizinho/colocação/remoção, nunca scan em área, nunca forceload ou player tick. Estado do sinal deve ser derivável após restart. Se atualização de crescimento da planta não avisar vizinhos, estudar evento/notification adequado sem listener global por sensor.

**Comparadores vanilla:** observer, comparator, repeater, sculk sensor e daylight detector. O sensor deve **medir um estado ambiental específico do mod** em vez de simplesmente emitir pulso atrasado ou acender uma lâmpada.

**Economia:** protótipo inicial com redstone/cobre vanilla + Pedra Marcada e/ou Líquen. **Não criar Nódulo novo só para fornecer ingrediente.**

## 6.3 Critérios de seleção

- Um cenário de Survival realmente utiliza 0–4 leituras.
- Redstone responde de forma previsível à alteração dos quatro vizinhos, incluindo unload e borda de chunk.
- Custo de 64–256 sensores em teste de carga não causa varredura/tick excessivo.
- Multijogador não duplica updates nem altera resultados.
- Não exige viagem dimensional nem minério raro.
- Falha nesses pontos implica **cancelar**, não adicionar complexidade para salvá-lo.

---

# 7. EQUIPAMENTOS DE INVESTIGAÇÃO — ESCOLHER, NÃO ACUMULAR

A Bible apresenta alternativas EQP-01 Sonda de Vestígios, EQP-02 Lente de Observação e EQP-06 Instrumento de Medição. A pergunta não é qual modelo voxel fica mais bonito, mas se o mundo necessita de ferramenta.

| Candidato | Ação proposta | Comparador vanilla | Decisão agora |
|---|---|---|---|
| Sonda | Ler característica local da rocha | Pista visual, mineração, observação | **Adiar** até existir variável significativa |
| Lente | Interpretar inscrições/marcas | Luneta, arquitetura, tooltip, papel/brush | **Adiar** até Task 6.5 aprovar caso concreto |
| Instrumento | Comparar intensidade de fenômenos | Comparator, blocos reativos, observação | **Adiar** até intensidade ser estado de gameplay real |

**A/B exigido:** uma ruína ou formação deve ser compreendida primeiro com sinais naturais. Se um único instrumento permitir ação de investigação substancial que não seja GPS nem tooltip óbvia, selecionar **no máximo um**. Não construir três detectores com alcance progressivamente maior.

**Regras se aprovado futuramente:** leitura no servidor, raio limitado e chunks já carregados, cooldown proporcional à operação, nenhuma procura global de POIs ou localização exata de baús, UI leve e feedback compreensível com partículas no mínimo.

---

# 8. DECISÃO SOBRE O NÓDULO REVERBERANTE E OUTROS RECURSOS

A Task 6.2 condicionou outro minério a um **consumidor técnico comprovado**. Esta Task 6.3 ainda não encontrou tal consumidor:

- Argamassa não precisa de minério técnico; consome material vegetal e vanilla.
- Cinzel não precisa de minério; é ferramenta de acabamento.
- Leitor pode ser prototipado com redstone/cobre e os recursos F0/F1.
- Detectores portáteis não foram aprovados.

**Portanto, não criar `reverberation_nodule` nem `reverberant_fragment` em 6.9 sem nova justificativa e teste.** O candidato continua condicionado — não está cancelado para sempre, mas está **bloqueado para implementação**.

**Reabertura:** quando existir mecanismo distinto da redstone vanilla com comportamento comprovado, duas aplicações verdadeiras e origem geológica coerente. Evitar “pó 2” cuja função é apenas aumentar a receita de um relógio.

A **Hera do Repouso** também permanece condicionada: Argamassa pode usar Líquen diretamente; criar fibra de Hera só seria válido se outro consumidor específico merecer o ciclo de cultivo. Cristal Velado e Pedra de Contenção continuam adiados até mecânicas e riscos associados serem realmente definidos.

---

# 9. CONSUMÍVEIS — CUSTO, TIPO DE USO E REABASTECIMENTO

| Candidato | Benefício por uso | Fonte e custo | Decisão |
|---|---|---|---|
| **Argamassa de Marcas** | Converter 1 bloco de pedra comum em 1 Pedra Marcada | Líquen cultivado + vanilla; 1 unidade por conversão | **Selecionado para especificação** |
| Fertilizante especial | Crescimento acelerado de Líquen | Poderia copiar bonemeal | **Rejeitado como novo item** |
| Ampola de Estabilização | Proteção contextual contra perigo | Risco ainda não especificado | **Adiada** |
| Comida de “sono profundo” | Buff genérico | Alimento/potions vanilla já existem | **Rejeitada nesta fase** |
| Amuleto extra de mobilidade | Efeito redundante com outros sistemas | Ainda sem custo/diferencial | **Não aprovado** |

**Contrato:** consumível existe quando entrega efeito delimitado e previsível; não conceder buff perpétuo ou criar estoque infinito de material raro por farming vegetal.

Argamassa é **consumível de expressão/construção**, não de combate, magia, teleporte ou segurança. Um catálogo saudável mistura funções, mas não precisa preencher cada categoria antes de validar um loop.

---

# 10. ESTAÇÕES — POR QUE NÃO CRIAR UMA NESTA TASK

**Decisão:** nenhuma estação customizada é necessária para os candidatos selecionados.

| Atividade | Sistema suficiente | Comentário |
|---|---|---|
| Pedra Marcada → Lapidada | Stonecutter/crafting | F0 Early deve ser acessível. |
| Líquen → jardim/replantio | Bloco, tesoura/interação vanilla | Sem semente ou máquina. |
| Argamassa | Crafting vanilla | Processo simples; usar dados/recipe. |
| Argamassa aplicada | Interação de item com bloco | Não requer bloco-entidade ou menu. |
| Cinzel | Interação de ferramenta com bloco | É comportamento, não estação. |
| Leitor de Colônia | Bloco redstone próprio, se passar A/B | Sem estação de energia ou processo. |
| Instrumentos opcionais | A decidir | Não antecipar UI de receitas. |

**Critério cumulativo para futura Mesa de Estabilização/Banco de Instrumentos:**

1. Duas famílias **independentes e aprovadas** precisam de processos não resolvidos por vanilla.
2. O jogador executa escolha ou interação significativa, e não apenas assiste a uma barra.
3. O custo do bloco é proporcional ao novo uso; nunca barreira falsa para um único item.
4. Entradas, saídas e estado têm comunicação clara, receitas visíveis e custo definido.
5. Multiplayer/servidor dedicado, salvos, shift-click, hoppers e possíveis receitas duplicadas são seguros.
6. É útil no Late sem tornar todo material do mod obrigatório.
7. Recipe serializer/menu/BlockEntity só serão criados quando justificáveis por essas ações.
8. Não reescrever a fabricação do Despertador para “precisar da máquina” por capricho.

**Se a segunda função só existir para justificar a bancada, a bancada não deve existir.**

---

# 11. ESTRUTURAS, DESCOBERTA E ITENS DE LORE

Estruturas de 6.5 poderão apresentar inscrições, amostras e técnicas. Antes de criar “Pergaminho de Memória”, “Folha de Decalque” e “Runas” como três novos itens:

- usar leitura ambiental, interação direta, papel/brush vanilla e Recipe Book/advancements sempre que bastarem;
- uma descoberta pode ensinar uma **variante opcional de construção** ou método alternativo, sem travar agricultura/primeiro uso de Pedra Marcada;
- não criar sistema de “pontos de pesquisa” ou quests sem conteúdo que justifique interface e persistência;
- não tornar um baú raro fonte exclusiva de ferramenta para construir blocos básicos;
- nenhum novo item é autorizado antes do contrato de exploração 6.5.

Se, após playtest, um instrumento de inspeção for necessário, selecionar uma ação específica em vez de criar uma árvore de três detectores.

---

# 12. GRAFO DE DEPENDÊNCIAS — SEM SOFTLOCK

```text
[Minecraft vanilla: exploração, pedra, ferramentas, crafting]
                   |
                   +--> [F0: Pedra Marcada natural] ---> [Stonecutter]
                   |                                 ---> [Construção Early]
                   |                                 ---> [Cinzel Mid opcional]
                   |
                   +--> [F1: Líquen natural] ---> [Replantio / Jardim]
                   |                          ---> [Argamassa Mid] <--- [Vanilla]
                   |                                       |
                   |                                       +--> [Stone -> marked_stone]
                   |                                                   |
                   |                                                   +--> [Construção Late]
                   |
                   +--> [F1: Líquen] + [redstone vanilla]
                   |                          |
                   |                          +--> [Leitor de Colônia opcional]
                   |
                   +--> [Dream Ore existente] ---> [Dream Dust]
                                                   |
                                                   +--> [Colar / Núcleo / Despertador]
                                                              |
                                                              +--> [Expedição onírica]
```

- **F0 não requer F1. F1 não requer F0.**
- Argamassa requer fontes já disponíveis e não produz minério de viagem.
- Cinzel não é necessário para nenhum bloco básico.
- Leitor não pode tornar-se pré-requisito para cultivar Líquen.
- O ramo dos sonhos continua paralelo, com custo e regras atuais preservados.
- Nenhuma saída retorna como insumo obrigatório exclusivo de si mesma.
- No Late, o mesmo conteúdo Early continua útil por construção e cultivo.

---

# 13. FICHAS DE DECISÃO — PRONTAS PARA AS TASKS SEGUINTES

## 13.1 Argamassa de Marcas

```yaml
id_conceitual: EQUIP-A2
id_proposto: dreamsdimensions:marked_mortar
status: selecionado_para_especificacao
estagio: Mid_Late
categoria: consumivel_construtivo
problema: escalar_obtencao_de_pedra_marcada
fontes: [vestige_lichen_cultivado, minecraft_stone, insumos_vanilla]
interacao: use_on_minecraft_stone
saida: 1_marked_stone_mesma_posicao
consumo: 1_unidade_somente_se_confirmado
sem_drops: true
sem_xp: true
sem_dream_dust: true
renovabilidade: por_cultivo_e_materiais_comuns
comparador: mineracao_de_novas_ocorrencias
riscos: [dupe, claims, atomicidade, recipe_remainder, multiplayer]
dependencias: [F0, F1, "6.6", "6.7", "6.8", "6.9"]
aceite: conversao_1_por_1_sem_atalhos
```

## 13.2 Cinzel de Marcas

```yaml
id_conceitual: EQUIP-B1
id_proposto: dreamsdimensions:marked_chisel
status: prototipo_condicionado
estagio: Mid_Late
categoria: ferramenta_reutilizavel
problema: trocar_acabamento_in_situ
interacao: marked_stone_para_cut_marked_stone_e_vice_versa
saida: mesmo_bloco_mesma_posicao
custo: durabilidade_por_uso_confirmado
proibicoes: [drops, xp, ores, block_entities, slabs, stairs, walls]
comparador: stonecutter_picareta_recolocacao
riscos: [claims, spam, multiplayer, integridade_de_blocos]
aceite: teste_AB_e_reversao_sem_dupe
```

## 13.3 Leitor de Colônia

```yaml
id_conceitual: EQUIP-B2
id_proposto: dreamsdimensions:lichen_colony_sensor
status: experimento_condicionado
estagio: Mid_Late
categoria: bloco_redstone
problema: leitura_analogica_de_colonizacao_local
entrada: 0_ate_4_vizinhos_horizontais_de_lichen_elegivel
saida_proposta: [0, 4, 8, 12, 15]
atualizacao: somente_eventos_locais
comparadores: [observer, comparator, repeater]
nao_requer_minerio_novo: true
riscos: [block_updates, chunk_border, uso_inutil, desync]
aceite: projeto_real_mais_teste_de_carga
```

---

# 14. CONTRATO TÉCNICO DE IMPLEMENTAÇÃO FUTURA

Esta task é **design**, não código. Assinaturas e APIs devem ser verificadas contra Minecraft 26.3 / NeoForge 26.3 / Java 25 quando iniciar as PRs.

## 14.1 Interação de Argamassa/Cinzel

- Considerar pipeline NeoForge de item/bloco: eventos, `Item#useOn`, `InteractionResult`, uso principal/secundário e alcance. Não inventar hooks.
- Servidor valida **quem**, **qual item**, **qual bloco**, **qual permissão** e **se ainda está no estado elegível**.
- Mutação e gasto de item/durabilidade só após sucesso verdadeiro, com comportamento previsível no mesmo tick e duas pessoas.
- `PASS` para alvo não suportado, sem bloquear interação normal de outros mods. No lado cliente, feedback correto sem mutar mundo.
- Verificar claims/mods de proteção; não assumir que chamada crua de `setBlock` garante respeito a proteção.
- Não carregar chunks, não fazer scan por tick, não procurar itens em inventário global para consumir outra pilha.
- Verificar diferenças de Creative/Survival/Adventure.

## 14.2 Receitas

- Preferir `minecraft:crafting_shaped`, `crafting_shapeless`, `stonecutting` nos datapacks.
- Se custo/transformação ainda não exigem condição extra, **não criar RecipeType/Serializer**.
- Em custom recipes futuras, servidor controla resultado, dados de item e Recipe Book/recipe displays; evitar receitas que aparecem mas não funcionam no cliente.
- Verificar componentes, tool durability e recipes contra API real da versão.
- Advancements de desbloqueio com condições claras, sem bloquear todo o crafting básico.

## 14.3 Blocos e redstone

- Estado finito: privilegiar blockstate; inventário/estado arbitrário: BlockEntity só se justificar.
- Sensor usa vizinhos **imediatos**, eventos de atualização e outputs definidos; nenhum player/chunk ticket por bloco.
- Sinal deve ser recalculável após unload/reload sem varredura de área.
- Testar 0–4 neighbors, suporte e reação com comparator/observer e outros mods de redstone.

## 14.4 Compatibilidade

- IDs e comportamento existentes permanecem intactos.
- Worldgen novo só em chunks futuros; F0/F1 devem funcionar em novas regiões de saves antigos.
- Não alterar regras de despertar, segurança ou obtenção de Dream Dust como efeito colateral.
- Não mergear a main automaticamente; cada fatia com CI, relatório e comandos de teste.

**Fontes oficiais atuais de referência técnica:**
- NeoForge — [Interactions](https://docs.neoforged.net/docs/items/interactions/)
- NeoForge — [Recipes](https://docs.neoforged.net/docs/resources/server/recipes/)
- NeoForge — [Built-In Recipes](https://docs.neoforged.net/docs/resources/server/recipes/builtin/)
- NeoForge — [Custom Recipes](https://docs.neoforged.net/docs/resources/server/recipes/custom/)
- NeoForge — [Blockstates](https://docs.neoforged.net/docs/blocks/states/)
- NeoForge — [Primer 26.3](https://docs.neoforged.net/primer/docs/26.3/)

---

# 15. VALIDAÇÃO, PLAYTEST E CRITÉRIOS DE CUSTO

| Conteúdo futuro | Teste de valor | Testes de segurança | Decisão se falhar |
|---|---|---|---|
| F0 Stonecutter | Encontra, minera, lapida e constrói sem ajuda | Loot, tool tier, 4 seeds, restart | Melhorar descoberta/arte; não inventar minério |
| F1 Líquen | Encontra, transplanta, cultiva e colhe duas vezes | Suporte, luz, neighbor, reload | Se duplicar Glow Lichen, redesenhar |
| Argamassa | Obra de referência, autonomia no Late | 1:1, proteções, 100 conversões, dois players | Corrigir UX/economia ou cancelar |
| Cinzel | A/B mostra ganho vs stonecutter | Dupla reversão, durabilidade, claims, multiplayer | Cancelar se valor marginal |
| Leitor | Projeto redstone usa sinal 0–15 | 4 vizinhos, borders, updates, 64–256 blocos | Cancelar se simples duplicação |
| Estação futura | Duas linhas independentes, uso recompensador | Serialização, hopper, shift-click, save | Não criar |

Métricas a fechar na 6.8: minutos até 1ª descoberta, tempo de construção, custo de material por obra, retorno do cultivo, valor do Cinzel em cliques poupados, uso do Leitor em circuito real, variação entre seeds e impacto no servidor.

**Seeds de regressão conhecidas:** `14011`, `20261108`, `-904732`, `73199013`. São referências de CI já usadas, **não** evidência de que conteúdos propostos existem nessas seeds.

## Política de comandos

A Task 6.3 **não registra nenhum novo item**; consequentemente `/give @s dreamsdimensions:marked_mortar`, `/give @s dreamsdimensions:marked_chisel` e `/setblock ... lichen_colony_sensor` **ainda não funcionam**. Não anunciá-los como comandos de QA.

Para PRs futuras, fornecer comandos completos **com IDs realmente compilados**, como `/give`, `/setblock`, `/loot` e `/tp` com coordenadas **medidas na seed correta**. `/locate structure` não localiza um minério isolado ou placed feature. Diagnósticos não substituem Survival.

---

# 16. REGRAS PARA AS TASKS 6.4–6.9

| Task | Entrada recebida da 6.3 | Entrega que esperamos |
|---|---|---|
| **6.4 — Ecologia e Mobs** | F1 Líquen é habitat candidato, sem obrigação de drop | Mob deve observar/interagir com bioma e planta com mecânica própria. |
| **6.5 — Estruturas e Descoberta** | Detectores adiados; começar com observação e vanilla | POI mostra valor concreto; só instrumentar se provar necessidade. |
| **6.6 — Grafo de Crafting** | Argamassa selecionada, Cinzel/Leitor condicionados | Custos, rendimentos, DAG, materiais de origem e alternativa de receita. |
| **6.7 — Direção Visual** | Silhueta de argamassa/cinzel/painel, sem assets definitivos | Identidade visual por família, priorizando F0/F1. |
| **6.8 — Economia e Raridade** | Escala de construção e reabastecimento são variáveis de design | Metas experimentais e como medi-las em Survival. |
| **6.9 — Roadmap Executivo** | Escopos e dependências prontos | PRs curtas por fatias verticais, critérios de aceitação e CI. |

## Ordem sugerida após 6.9

1. **F0:** Pedra Marcada → lapidação → construção vanilla.
2. **F1:** Líquen → cultivo → paisagismo.
3. **F1.5:** Argamassa de Marcas → conversão em mundo → obra em escala.
4. **F2:** fauna ou estrutura que reutilize geologia/vegetação, conforme 6.4/6.5.
5. **F3:** Cinzel, se vencer teste A/B.
6. **F4:** Leitor de Colônia, se houver projeto útil e custo aceitável.
7. **F5:** aprofundamento Late, desafios opcionais e economia contínua, **sem endgame próprio**.

Nenhum pacote deve existir apenas para “preparar infraestrutura para conteúdo futuro” sem entregar um loop completo no próprio release.

---

# 17. CRITÉRIOS DE ACEITE DA TASK 6.3

## Documentação — CONCLUÍDA

- [x] Auditou o HEAD da branch e as especificações 6.0–6.2.
- [x] Confrontou alternativas com ferramentas e estações vanilla.
- [x] Selecionou consumível com fonte, ação, resultado e economia independente dos sonhos.
- [x] Descreveu regras de consumo, multiplayer, claims e prevenção de duplicação.
- [x] Definiu ferramenta condicional que melhora construção sem criar novo tier de picareta.
- [x] Definiu experimento de redstone sem introduzir novo minério arbitrário.
- [x] Rejeitou múltiplos detectores genéricos e consumíveis sem situação concreta.
- [x] Explicou por que não devemos criar estação customizada agora.
- [x] Preservou as receitas e mecânicas de Colar/Despertador e a economia de Dream Dust.
- [x] Descreveu progressão Early/Mid/Late e utilidade contínua no Overworld.
- [x] Preparou dependências e critérios de teste para 6.4–6.9.
- [x] Não alterou código, assets, recipes, worldgen ou registries.

## Gameplay — FUTURO / NÃO EXECUTADO

- [ ] F0/F1 aprovados e testados em Survival.
- [ ] Receita de Argamassa concluída com custo de uso justo e antifraude de crafting.
- [ ] Conversão em mundo confirmada com dois jogadores e proteções.
- [ ] Cinzel testado A/B, compatível com saves e sem item duplication.
- [ ] Leitor mostra aplicação útil, update por vizinho, performance estável.
- [ ] Recursos novos são distinguíveis de materiais e capacidades vanilla.
- [ ] IDs, assets, tags, traduções, Recipe Book e ferramentas finalizados.
- [ ] Nenhum conteúdo essencial depende de viagem dimensional para existir.

**Status exato:** a Task 6.3 foi concluída **como planejamento**, não como novo conteúdo do jogo.

---

# 18. VEREDITO

DreamsDimensions precisa de um Overworld com **vontade própria**: rochas que mereçam ser descobertas, plantas que possam ser cultivadas, construções que expressem estilo e mecanismos que respondam ao mundo. Isso se traduz em tarefas satisfatórias sem que todo recurso seja transformado em combustível do Despertador.

A primeira expansão concreta prioriza geologia e natureza. A **Argamassa de Marcas** é o primeiro item novo com função suficientemente clara para ser desenvolvido após essas bases. O **Cinzel** e o **Leitor** são experimentos que podem ser eliminados se o jogo não ficar melhor com eles. Não criamos receita complexa nem máquina para esconder um loop fraco.

**Próxima Task: 6.4 — Ecologia, Mobs e Habitats.** Precisamos usar os biomas e microambientes como fonte de comportamentos, não apenas de drops.

---

*Fim — Task 6.3. Documento de Systems Design. Nenhuma mudança funcional no Minecraft foi realizada.*
