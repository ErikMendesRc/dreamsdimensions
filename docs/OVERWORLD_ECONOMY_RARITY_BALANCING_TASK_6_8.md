# TASK 6.8 — ECONOMIA, RARIDADE E BALANCEAMENTO

## DreamsDimensions — Distribuição, acesso, esforço, fauna, POIs, recuperação onírica e testes estatísticos

**Projeto:** DreamsDimensions. **Minecraft 26.3 / NeoForge 26.3 / Java 25**.  
**Tipo de entrega:** Game Design / Economia / Análise Quantitativa / Plano de Homologação — **somente documentação**.  
**Data:** 12/11/2026. **Branch:** `migration/minecraft-26.3`.  
**HEAD auditado:** `c585e3a829523da7d435de7e140088e9c2d53d9a`.  
**Dependências:** [Bible 6.0](GAME_DESIGN_BIBLE_TASK_6_0.md), [Pilares 6.1](OVERWORLD_GAMEPLAY_PILLARS_TASK_6_1.md), [Recursos 6.2](OVERWORLD_RESOURCES_GEOLOGY_FLORA_BLOCKS_TASK_6_2.md), [Itens 6.3](OVERWORLD_ITEMS_CONSUMABLES_TOOLS_STATIONS_TASK_6_3.md), [Ecologia 6.4](OVERWORLD_ECOLOGY_MOBS_HABITATS_TASK_6_4.md), [Estruturas 6.5](OVERWORLD_STRUCTURES_ENVIRONMENTAL_DISCOVERY_TASK_6_5.md), [Grafo 6.6](OVERWORLD_CRAFTING_PROCESSING_PROGRESSION_TASK_6_6.md), [Catálogo 6.6.1](OVERWORLD_UNIVERSE_CONTENT_EXPANSION_TASK_6_6_1.md), [Art Bible 6.7](OVERWORLD_VISUAL_DIRECTION_TEXTURES_MODELS_ASSETS_TASK_6_7.md).  
**Fontes medidas:** [Economia 5.6](OVERWORLD_ECONOMY_TASK_5_6.md), [Distribuição real 5.6](OVERWORLD_ORE_DISTRIBUTION_TASK_5_6.md), [Ruínas 5.7](DREAM_RUINS_TASK_5_7.md).  
**Próxima:** **Task 6.9 — Roadmap Executivo de Implementação**.  
**Escopo do commit:** só este `.md`; não alterar minérios, parâmetros de worldgen, JSON de loot, recipes, entidades, mob caps, config, teleporte, snapshots ou `main`.

> **Contrato de economia:** o jogador deve encontrar conteúdos comuns o suficiente para aprender e construir; descobrir estruturas/organismos extraordinários exige explorar; e visitar os sonhos deve continuar exigindo preparo. **Raridade não é dificuldade se o único desafio for percorrer milhares de blocos sem pistas.** O Late é consistência obtida por investimento, **não um End Game exclusivo**.

---

# 0. RESULTADO EXECUTIVO: DECISÕES, HIPÓTESES E BLOQUEIOS

| Domínio | Posição desta Task | Por quê | Próximo passo |
|---|---|---|---|
| **Dream Ore e Deepslate Dream Ore** | **MANTER baseline atual** | Geração real comprovada, falta mineração/tempo em Survival | Playtest A/B sem cheats antes de mexer na distribuição |
| **Colar/Despertador** | **MANTER receitas R0** | 3 Dust + 12 Dust/Despertador é custo real; percepção de dificuldade ainda não medida | Instrumentar tempo/risco das primeiras expedições |
| **25% de Despertador nos baús dimensionais** | **MANTER provisoriamente; elevar prioridade de revisão da recuperação** | 25% é regra configurada, mas cauda estatística de recuperação é longa | Medir tempo até ruína, loot REAL, acesso e taxa de falha |
| **F0 Pedra Marcada** | **Proposta de faixa natural moderada** | Construção Early não pode depender de POI raro | Prototipar feature e medir quantidade extraível |
| **F1 Líquen** | **Proposta de patches por microhabitat + cultivo sustentável** | Flora deve ser encontrável e replicável sem mob | Definir suporte/crescimento e medir duas colheitas |
| **Argamassa** | **Manter hipótese 2+1+1→4, não fixar rendimento ainda** | Modelo permite planejar obras 64/256 blocos | Comparar tempo de mineração vs processo agrícola |
| **12 POIs** | **Matriz de spacing/separation CANDIDATA**, nenhum POI novo registrado | Variedade sem saturação e sem mapa de baús | Medir candidatos/aceitos/rejeitados, P50/P90, experiências |
| **6 criaturas** | **Spawn por habitat e orçamento de encontro, não por drops** | Espécies têm sinais e limites; sem sobrescrever vanilla | Medir composição, caps e IA natural |
| **M02 arqueologia, M03 travessia, M06 ressonância** | **Protótipos antes de taxas de drop** | A mecânica vem antes de sua economia | Cancelar linha sem diferencial real |
| **POI-04 Câmara e POI-12 Casa de Ecos** | **BLOQUEADOS para release** | Câmara sem recompensa funcional, Casa depende de M06 | Não fabricar odds para objetos inexistentes |

**Estado correto:** esta Task entrega um **contrato de balanceamento** + valores de **primeira hipótese** para comparar, **não** um tuning já executado. Nenhuma medida nova de Survival, loot real ou worldgen foi criada nesta sessão.

---

# 1. BASELINE REAL: QUE NÚMEROS JÁ TEMOS?

## 1.1 Minérios oníricos, JSON efetivo

| Recurso real | Feature size | Colocação declarada | Altura declarada | Substrato | Loot |
|---|---:|---|---|---|---|
| `dreamsdimensions:dream_ore` | **6** | **2 tentativas/chunk** + in_square + biome | Trapezoidal, Y **-16..64** | `minecraft:stone_ore_replaceables` | Dream Dust normal/Fortune; bloco com Silk Touch |
| `dreamsdimensions:deepslate_dream_ore` | **5** | **2 tentativas/chunk** + in_square + biome | Trapezoidal, Y **-64..-16** | `minecraft:deepslate_ore_replaceables` | Mesmo contrato |

Arquivos: `worldgen/feature/dream_ore.json`, `deepslate_dream_ore.json`, `worldgen/placed_feature/*.json` e `loot_table/blocks/{dream_ore,deepslate_dream_ore}.json`.

**Importante:** count 2 **não significa dois veios bem-sucedidos por chunk**, nem exposição, descoberta ou quantidade de Dust garantida. `discard_chance_on_air_exposure=0` não significa minério visível. Fortune e Silk estão declarados nos loot tables, mas seus resultados efetivos por sessão ainda precisam ser testados no cliente/servidor.

## 1.2 Medição runtime da Task 5.6 — não simulação

Quatro seeds, cada uma com **256 chunks novos** em quatro regiões fixas de 8×8 chunks; leitura RCON em servidor dedicado. Amostras são regiões **pré-escolhidas**, não seleção aleatória representativa de todos os biomas.

| Seed | Chunks sondados | Dream Ore | Deepslate | Total voxels | Adjacentes a ar | Chunks c/ minério |
| --- | --- | --- | --- | --- | --- | --- |
| 14011 | 256 | 1.455 | 1.355 | 2.810 | 84 | 256/256 |
| 20261108 | 256 | 1.467 | 1.404 | 2.871 | 76 | 256/256 |
| -904732 | 256 | 1.445 | 1.187 | 2.632 | 80 | 253/256 |
| 73199013 | 256 | 1.536 | 1.387 | 2.923 | 84 | 255/256 |
| TOTAL | 1.024 | 5.903 | 5.333 | 11.236 | 324 | 1.020/1.024 |

**Agregados observados:**
- **11.236 voxels em 1.024 chunks**, densidade média amostral `11.236 / 1.024 = 10,97` voxels/chunk.
- **1.020/1.024 chunks** continham algum minério da família na faixa sondada (**99,61%** na amostra).
- **324/11.236 = 2,88%** tinham vizinho ar em alguma das seis faces. Isso **NÃO** é a porcentagem de minérios acessíveis/mineráveis: blocos enterrados são encontrados com escavação, e ar adjacente não garante rota segura.
- **5903 Dream Ore** e **5333 Deepslate Dream Ore** foram contados.
- Faixas sondadas: Y=-64..64; não é varredura de todo mundo.
- O pipeline usou quatro sementes `14011`, `20261108`, `-904732`, `73199013`; não se deve atribuir resultados a mundo do usuário com versão/seed diferente.

**Faltam dados de:** tempo até primeiro bloco encontrado por jogador, horas para 3/12/15/39 Dust, Fortune efetivo, caverna vs túnel, drops reais, demanda de ferro/ouro/amethyst, risco, morte, acessibilidade, sensação de grind e progressão do Colar. Logo, **não reduzir/elevar o minério ainda**.

## 1.3 Ruínas dimensionais reais da Task 5.7

| Ruína existente | Dimensão | `spacing` | `separation` | Chance teórica de Despertador por chest |
|---|---|---:|---:|---:|
| `blue_dream_ruins` | Campo Onírico Azul | **38 chunks** | **12** | **25%** |
| `fragmented_dream_ruins` | Dreamscape | **32 chunks** | **12** | **25%** |

**Como são as ruínas:** jigsaw vanilla, template único, um baú com `LootTable` persistente; pool raro de **1 Despertador (weight=1) vs empty (weight=3)**, sem Dust. Isso vale para as ruínas **das dimensões**, e não para novos POIs do Overworld.

**Evidência existente:** 8/8 ocorrências localizadas nas quatro seeds, 8/8 baús com referência de loot correta; **1/8** locais com sinal de suporte insuficiente **4/9** sob o piso da vizinhança do baú. Esse check é local, não auditoria arquitetônica total. Resultado de CI **não** comprova travessia em Survival, loot real, persistência de dois clientes nem percentis de distância entre muitas ruínas.

## 1.4 Custo real das receitas atuais (R0)

`dream_necklace`: 3 Dream Dust + 3 cobre + 1 amethyst shard.

`dream_infused_stone`: 2 stone + 2 Dust.  
`oneiric_core_block`: 4 infused stone + 4 Dust + 1 clock = **12 Dust + 8 stone + clock**.  
`oneiric_awakener`: core + feather + glass bottle. Um retorno confirmado consome 1 Despertador.

Para um Colar e **N** Despertadores: **`3 + 12N` Dream Dust**, `8N` stone, `4N` ouro, `N` redstone, `N` pena, `N` frasco, 3 cobre e 1 ametista. Para 1/3/5 retornos: **15/39/63 Dust**.

**Não alterar R0 para consertar falsa raridade.** Custos de material podem ser altos e, ainda assim, o risco/pré-requisito de viagem estar mal calibrado: Colar e retorno são problemas separados.

---

# 2. MODELO DE ESCASSEZ: CINCO MOEDAS DE JOGO, ZERO MOEDA ARTIFICIAL

## 2.1 O que o jogador realmente paga

**Tempo de descoberta (T_d):** procurar pista/bioma/rocha sem GPS, com deslocamento e exploração.

**Tempo de coleta (T_c):** minerar, plantar, escovar, descer, transportar e sobreviver.

**Tempo de processamento (T_p):** craft, cultivo, smelting, construção e espera por condição de jogo.

**Risco (R):** dano, água, queda, rotas difíceis, equipamento e chance de morte. **Risco comunicado antes do dano**.

**Oportunidade/decisão (O):** o que o jogador deixa de fazer enquanto busca um recurso; escolha entre escalar produção, procurar formação natural, usar item de suporte ou contornar.

**Custo total real de uma conquista** = `T_d + T_c + T_p + custo de risco/opção`. Não somar risco como se fosse automaticamente número de minutos: registrá-lo separadamente (dano, mortes, suprimento e tentativas). Tratar esses eixos como **observáveis**, não colapsar tudo em uma “raridade 5 estrelas” enganosa.

## 2.2 Matriz de famílias econômicas

| Economia | Fonte | Sink principal | Evolução Early → Late | Não permitir |
|---|---|---|---|---|
| Geologia arquitetônica | F0 natural | Lapidação/obras, Argamassa no Mid | Minerar → produzir/acabar em escala | Fonte rara o bastante para impedir construção |
| Flora | F1 e cultivo positivo | Jardim, Argamassa, Leitor condicional | Descobrir → replantar → colher excedente | Mobs gerarem plantas premium infinitas |
| Arqueologia | Sedimento contextual + brush | Relevo/aplicação construtiva | Encontrar evidência → copiar técnica | Livro RNG obrigatório |
| Travessia | Passagem contextual e materiais de apoio | Escora/obra, rota persistente | Improvisar → restaurar | Queda surpresa/softlock/blocks de player |
| Engenharia | Circuito M06, **somente se aprovado** | Equipamento de sinal local e diagnóstico | Estudar → construir circuito em base | Nódulo sem consumidor, lag, força global |
| Sonhos | Dream Ore → Dust | Colar/Despertador | Primeira viagem de risco → preparação recorrente | Dust farm da flora, saída gratuita ou boss chave |

## 2.3 Classes de raridade como experiência (não % de item)

| Classe | Significado de gameplay | Exemplos | Proibição |
|---|---|---|---|
| **Comum local** | Fácil achar quando se está no habitat certo; pode faltar em outro bioma | Pedra F0, Líquen F1 após calibração | Comum não deve cobrir toda a paisagem |
| **Contextual** | Dependente de geologia, luz ou estrutura; indício verificável | Sedimento, raiz, Mariposa | Pista falsa ou drop RNG essencial |
| **Incomum de exploração** | Exige buscar local ou atravessar escolha | Pedreira, Passarela, Galeria | Milhares de blocos sem sinal |
| **Raro opcional** | Especialização/arquitetura interessante, não básico | Arquivo, Observatório, Casa se aprovada | Chave obrigatória para Early |
| **Recuperação de risco** | Recurso que permite sair de situação perigosa com esforço real | Despertador em baús dimensionais | Cauda probabilística que torna recuperação inviável |

**Regra:** raridade de um objeto de **recuperação** não é equivalente à de cosmético; seu custo deve considerar risco de prisão, não apenas valor intrínseco.

---

# 3. RITMO EARLY / MID / LATE: CAPACIDADE, NÃO CRONÔMETRO DE CAMPANHA

| Etapa contextual | Sinal de prontidão | Objetivo econômico observável | Não fazer |
| --- | --- | --- | --- |
| Early / exploração | Ferro vanilla ainda em obtenção; primeira rocha/planta | Identificar F0/F1, retornar com material e replantar; POI curto opcional | Não exigir Despertador, boss, estação nem ferramenta nova |
| Mid inicial / capacidade | Picareta de ferro, stonecutter, redstone e cavernas conhecidos | F0 em obras, primeiras Argamassas/cultivo; preparar Colar e retorno quando seguro | Primeira viagem deliberada, não cadeia de recuperação impossível |
| Mid tardio / especialização | Estoque de recursos e infraestrutura vanilla, mais alternativas de percurso | Galeria/arqueologia/restauro, fauna contextual e 2–3 expedições planejadas | Custo continua por Despertador; sem mineração infinita por sessão |
| Late saudável / recorrência | Produção arquitetônica/engenharia sustentáveis, bom equipamento vanilla | Obras maiores, circuitos se aprovados, POIs excepcionais opcionais e viagens regulares | Sem boss final, viagem gratuita ou item de Câmara obrigatório |

## 3.1 Metas de teste de descoberta (não garantias de tempo global)

**Para contexto elegível:** jogador novo em cenário de floresta/afloramento/caverna relevante, com exploração normal e sem comando que revele coordenadas.

- **F0:** alvo experimental de primeira descoberta visível **P50 em 10–25 min** de exploração geológica *elegível*, **P90 abaixo de 60 min**. São metas A/B sujeitas a bioma/spawn/experiência, **não parâmetros de worldgen**. Se muito fácil, diminuir densidade local; se muito difícil, melhorar a pista antes de aumentar tudo.
- **F1:** primeiro patch observável **P50 em 10–30 min** *dentro de habitat sombreado elegível*; **P90 abaixo de 75 min**. Não contar minutos vagando num deserto como falha de um líquen de caverna.
- **Primeiro POI de escala micro/pequena:** jogador que escolhe explorar superfície elegível vê algo distintivo **P50 em 20–45 min**, **P90 abaixo de 100 min**; rastrear que tipo foi visto, sem garantir cada um dos 12.
- **Mid estrutura de percurso:** em sessão dedicada de exploração, candidato **P50 45–110 min** e P90 maior a medir; melhorar dicas e abordagens se ainda mais distante. Não usar estrutura como item obrigatório nesse estágio.
- **POIs especiais:** não definir P50 de obtenção de item essencial porque **nenhum item essencial deve vir deles**. Medir experiência de descoberta rara em vez de prometer um exemplar por sessão.

**Medição justa:** iniciar o cronômetro quando jogador tem **uma escolha informada de exploração relevante**, registrar se encontra natural e quantos blocos reais obtém. P50/P90 são **resultados futuros desejados** e poderão mudar após A/B.

## 3.2 Como fazer uma descoberta difícil sem se tornar irritante

1. Primeiro mostrar **contexto** (veio geológico, planta/sombra, apoio de ruína, marca de escavação).
2. Usar **forma ou comportamento único** para confirmar que vale investigar.
3. Permitir **escolha** entre continuar explorando ou usar outro caminho.
4. Entregar **algum resultado garantido** quando se chega lá.
5. Reservar RNG para conteúdo extra, nunca para a única utilidade.
6. Voltar a promover descoberta de outros exemplares com **variantes topológicas** e projetos fora do POI.

---

# 4. F0/F1 E RECURSOS DO OVERWORLD — PRIMEIROS VALORES CANDIDATOS

## 4.1 Cenário experimental de worldgen (NÃO CONFIGURADO)

| Família | Faixa inicial candidata para prototipagem | O que o auditor mede | Condição de aceite |
| --- | --- | --- | --- |
| F0 — Pedra Marcada | 1 candidato em ~8–20 chunks *elegíveis*; grupos naturais 5–18 full blocks | Fração de candidatos aceitos no substrato válido; P50/P90 até 1ª descoberta | Mineração/construção útil sem saturação de rochas estriadas |
| F1 — Líquen dos Vestígios | ~0,2–0,6 tentativa de patch por chunk com microhabitat válido; patches 3–9 unidades | Aproximação em cave/parede; taxa de replante/saldo de 2 colheitas | Primeiro cultivo possível, não tapete vegetal uniforme |
| B15 — Sedimento arqueológico | Patch ocasional ligado a estrato/POI, não a todo bloco de gravel | % de patches com pista verdadeira e padrão utilizável | Brush revela ambiente, não chave aleatória |
| B17 — Rocha Cediça | Somente em designs/POIs onde haja rota alternativa até passar QA | % de encontros avisados e sobrevivíveis, reparos por jogador | Jamais transformar stone genérica em armadilha |
| B21 — Nódulo Reverberante | **Nenhum parâmetro de worldgen antes de M06 aprovado** | Dois usos concretos do circuito e desempenho | Evitar minério novo sem consumidor |

Os números acima são **faixas de exploração A/B**. **Não são `count`/rarity_filter JSON nem geram essa frequência real**: biome filtering, altura, carver, suportes, substituição, outros features e restrição de terreno podem rejeitar a maioria dos candidatos.

### F0 — Pedra Marcada: um material de construção precisa existir em volume

- Primeira fatia: 1 tipo natural, 1 acabamento stonecutter; sem vinte variantes.
- Meta de utilidade: após primeira sessão em contexto elegível, jogador deve conseguir **uma construção pequena** (parede/elemento decorativo), não só 2 blocos guardados como troféu.
- Medir `blocks_found`, `blocks_mined`, `blocks_placed`, P50/P90 primeira descoberta, e `blocks_per_eligible_chunk`.
- **Não** converter Pedra Marcada em Dream Dust; não criar minério no Marco como fonte exclusiva.
- Afloramentos podem ser **features** se arquitetura não justificar `structure`; isso muda auditabilidade por `/locate`, não importância estética.

### F1 — Líquen: raridade natural vs reprodução

- Naturais suficientes para **adquirir a planta-matriz** sem percorrer mil blocos.
- Uma vez replantado, ciclo de cultivo deve entregar **excedente** após preservar a matriz. Medir taxa `net_new_lichen / player_minute` **com** e **sem** bonemeal, e por área de suporte.
- Replantio/colheita em bloco colocado pelo jogador **não** vira condição de spawn ilimitado de Mariposa.
- Não criar `lichen_seed`/fiber só para receita; usar próprio bloco/item como estoque conforme 6.2/6.6.
- Testar contraste com `minecraft:glow_lichen`, vines e `dreamsdimensions:dream_glow_moss`; se não diferenciar, simplificar design antes de reduzir raridade.

### Arqueologia e travessia (M02/M03)

- Sedimento só pode ser colocado onde o ambiente dá motivo para escovar; presença por toda caverna degrades descoberta.
- O **padrão aplicado na base** é recompensa; não exigir sorte de 1% em arquivo raro.
- Rocha Cediça só gera em local com duas opções e warning legível. **Raridade não neutraliza bug de queda fatal**.
- Antes de medir frequência de Passarela, confirmar **dois apoios físicos**, alternativa de rota e possibilidade real de reparo.

### Ressonância (M06) — sem minério até validar tecnologia

- Nenhuma taxa de nódulo, fragmento, magnetismo ou “energia” é aprovada nesta 6.8.
- Primeiro prototipar Emissor → pedras condutoras → Receptor (trecho local, carregado, máximo candidato ~12 segmentos, sem forceload).
- Exigir duas aplicações reais contra redstone/sculk, estabilidade no servidor e custo aceitável.
- Só então desenhar raridade **não extrema**, pois um recurso de **engenharia construtiva** não pode ser tão escasso que inviabilize qualquer circuito.
- Se M06 for repetição de observer/repeater, eliminar nódulo e Casa de Ecos, não aumentar rareza para simular valor.

---

# 5. DISTRIBUIÇÃO DE 12 ESTRUTURAS — MATRIZ INICIAL DE TESTE

**ATENÇÃO:** POI-01–12 são propostas de catálogo, **NENHUM registro novo existe hoje no Overworld**. A tabela seguinte **não altera structure sets** e nem valida que cada estrutura poderá usar `random_spread` com o espaçamento escolhido.

## 5.1 Hipótese V-A: parâmetros por família/POI

Unidade dos dois números = **chunks**. `spacing > separation` em todas as linhas. Não copiar `salt` das ruínas dimensionais. Na implementação cada estrutura deve ter um **salt estável próprio**, auditável; não gerar duplicados ao combinar sets.

| ID | Estrutura | Faixa/escala | spacing* | separation* | Status | Filtro/contexto obrigatório | Meta de relevância |
| --- | --- | --- | --- | --- | --- | --- | --- |
| POI-01 | Marco de Pedra Marcada | Early/micro | 22 | 8 | A, após F0 | Substrato de pedra exposta; sem chest | O jogador observa/reconhece F0 sem ruína dominando paisagem |
| POI-02 | Abrigo de Campo | Early/Mid/pequeno | 40 | 12 | A, após Marco | Encosta/fenda suave; 2 entradas | Cobertura e material recuperável sem loot raro |
| POI-03 | Galeria Afundada | Mid/subterrâneo | 64 | 20 | CONDICIONADO | Entrada vinculada a cavidade/altura verificável | 2 rotas, água contida e saída real |
| POI-04 | Câmara do Limiar | Mid/Late/especial | 112 | 36 | BLOQUEADO | Subsolo com acesso real, sem stronghold/ancient city | Somente após prêmio útil; boss nunca necessário |
| POI-05 | Pedreira Abandonada | Early/Mid/pequeno | 34 | 10 | HIPÓTESE | Afloramento/encosta rochosa | Extração e caminho restaurável |
| POI-06 | Jardim de Penumbra | Early/Mid/pequeno | 40 | 14 | HIPÓTESE | Microcaverna úmida com F1 verdadeiro | Mudas e habitat preservável |
| POI-07 | Estação de Sondagem | Mid/médio | 56 | 18 | HIPÓTESE | Terreno estratificado e dois pontos de comparação | Sondagem real, não GPS |
| POI-08 | Passarela Rompida | Mid/médio | 42 | 12 | HIPÓTESE | Dois apoios naturais e trecho recuperável | 2 abordagens legítimas |
| POI-09 | Poço de Camadas | Mid/subterrâneo | 64 | 20 | HIPÓTESE | Poço vertical com vários nichos e retorno | Descer/retornar com camadas legíveis |
| POI-10 | Arquivo de Relevos | Mid/subterrâneo | 72 | 22 | HIPÓTESE | Rocha/sala compacta acessível | Molde/padrão usável em base, sem RNG key |
| POI-11 | Observatório de Vigília | Mid/Late/médio | 88 | 28 | HIPÓTESE | Colina com vista/ecologia verdadeira | Fenômeno ambiental legível sem shader |
| POI-12 | Casa de Ecos | Mid/Late/especial | 96 | 30 | BLOQUEADO M06 | Subsolo técnico com Emissor/Receptor válidos | Circuito replicável fora do POI |

**`*` SPACING/SEPARATION SÃO VALORES CANDIDATOS, NÃO VALORES NO JOGO.** Um `spacing=22` não significa “22 chunks até encontrar”, “352 blocos exatos entre ruínas” ou “uma estrutura a cada 22 chunks”. O `random_spread` seleciona candidatos por regiões; biome/terrain/worldgen podem rejeitar, a silhueta pode não ser visível e terrenos não são uniformes. A T5.7 já demonstrou que suporte real pode falhar.

**Cuidado contra saturação:** 12 sets independentes podem produzir *muito mais* encontros que quatro sets, mesmo cada um parecendo raro. A 6.8 exige **medir densidade agregada** e sua interação com aldeias/estruturas vanilla; reduzir candidatos que competem por mesmo habitat ou fundi-los em variações antes de inflar raridade.

## 5.2 Por que não usar um só `spacing` para tudo

- Marco/Pedreira são **sinais ambientais**, não dungeons; uma pista pode ser comum sem baú.
- Abrigo/Jardim são locais úteis, com retorno arquitetônico; precisam aparecer em sessões Early/Mid sem saturar.
- Passarela/Poço/Galeria são experiências condicionais ao **terreno**. Maior número de candidatos não ajuda se 60% são inválidos e sem acesso.
- Arquivo/Observatório/Casa são descobertas de especialização e podem ser raros **desde que sua utilidade não seja necessária para primeira ferramenta/vida/sobrevivência**.
- Câmara está **BLOCKED_BY_REWARD_DESIGN**; número `112/36` é um cenário hipotético de auditoria, não aprovação para lançar o POI.

## 5.3 Métrica certa para estruturas

Registrar separadamente:

1. **Candidatos** `n_candidate_starts` pelo structure set e seed.
2. **Aceitos** `n_placed_starts` após bioma/terreno/codec.
3. **Íntegros** `n_playable_starts` com base, entrada, saída e rotas validadas.
4. **Visíveis/descobertos** `n_discovered` no playtest sem `/locate`.
5. **Completados** `n_completed` e resultado utilizado na base.
6. **Rejeitados com motivo** `terrain`, `biome`, `collision`, `no_entry`, `water`, `support`, `unknown`.
7. **Distância em blocos** P50/P90/P95 entre origem do jogador, entrada real e POIs da mesma/diferentes famílias.
8. **Tempo até primeira visão**, não apenas distância reta do `/locate`.
9. **Intervalo entre POIs** após eliminar duplicatas e sobreposição.
10. **Conflito vanilla** e impacto em desempenho de worldgen.

**Não medir só distância XZ à posição do structure start**, pois uma Galeria pode começar subterrânea e ser inacessível.

## 5.4 Condições de go/no-go antes do primeiro tuning

- POIs 01/02 testados em mundos novos com material vanilla-first; nenhuma entrada soterrada.
- Galeria/Poço: ligação física até ponto de entrada, ar respirável/água contida e saída; sob falha, corrigir *placement/layout* antes de spacing.
- Passarela: dois apoios e risco avisado. Não corrigir ponte flutuante aumentando spawn.
- Mundo antigo: sem retrogen automática ou substituição de chunks explorados.
- Spawn/loot não injetados em estruturas vanilla inadvertidamente.
- POI-04/12 continuam bloqueados por produto, mesmo que `/locate` ou JSON passem.

---

# 6. ECOLOGIA E SPAWNS — RARIDADE POR HABITAT, NÃO POR FARM

## 6.1 Parâmetros de laboratório, NÃO weights finais

| Mob | Condição | Grupo candidato | Quantidade candidata | Experiência observável desejada | Status/gate |
| --- | --- | --- | --- | --- | --- |
| MOB-01 | Mariposa dos Vestígios | HAB-01 F1 natural | 1–3 | 1–2 | Contatar um grupo após exploração intencional do habitat, não em toda sala | A, após F1; não spawnar em Líquen plantado pelo jogador |
| MOB-02 | Habitante das Fendas | HAB-03 fenda e rota de fuga | 1 | 1 | Encontro territorial contextual por travessia; alerta antes do ataque | CONDICIONADO; nenhuma obrigação de drop |
| MOB-03 | Guardião de Pedra | HAB-04 recinto íntegro de POI-04 | — | no máximo 1 encontro persistente por estrutura | Só pertence à Câmara se o desafio realmente melhora | BLOQUEADO pelo POI; nunca spawn geral |
| MOB-04 | Observador Esquecido | — | — | 0 | Nenhum contexto/benefício validado | ADIADO; sem parâmetros de spawn |
| MOB-05 | Besouro das Camadas | B15 sedimentação natural | 1–2 | 1–3 | Indicador ambiental verificável, nunca gerador de loot | CONDICIONADO à arqueologia M02 |
| MOB-06 | Grilo de Eco | B21 nódulo físico aprovado | 1–2 | 1–2 | Feedback audível/local de ressonância real, sem detectar mina distante | CONDICIONADO à M06 e minério |

**São tamanhos de grupo conceituais**, não parâmetros configurados de `MobCategory` ou `spawn_costs`. As categorias mob (CREATURE, AMBIENT, MONSTER etc.) e seus limites precisaram ser escolhidas e testadas na API atual e em servidor multiplayer.

## 6.2 Contrato NeoForge 26.3: não apagar spawns vanilla

Em Minecraft 26.3, a configuração de spawn natural mudou para `EnvironmentAttributes#NATURAL_MOB_SPAWNS`; uma operação `OVERLAY` mal usada pode **substituir entradas de categoria inteira**, silenciando fauna vanilla. Avaliar **`neoforge:add_spawns`** e o comportamento real de biome modifiers, respeitando o código da versão. Essa regra foi detalhada na 6.4 e no [NeoForge Primer 26.3](https://docs.neoforged.net/primer/docs/26.3/).

**Testes obrigatórios:**
- Diferença da lista de entidades elegíveis **antes/depois** de registrar cada espécie; nenhuma criatura vanilla desaparece por erro de camada.
- Spawn placements/colisão, dimensões, luz, piso, teto, água, cavernas, caves sem saída e biomas.
- Cap por categoria e população acumulada em `render_distance`/server simulation distance; nenhum player-tick spawn per species.
- Fauna natural nasce somente em habitats adequados: jardinzinho com Líquen transplantado **não gera Mariposas infinitas**.
- Testar Peaceful, gamerules, despawn/relog, tempo de vida, AI route e custo de 2, 4, 8 entidades no cliente.
- M3 Guardião é **encontro persistente de POI** e não mob de spawn mundial; anti-dupe em chunk reload e dois jogadores.
- MOB-04 Observador continua adiado. MOB-05/06 só fazem sentido com M02/M06.
- A composição de mobs vanilla é **baseline de qualidade**; matar Mariposa não pode ser necessário para crafting.

## 6.3 Como decidir se a Mariposa está “rara demais”

Se F1 aparece no habitat, mas nenhuma Mariposa é observável após **várias sessões intencionais** no local válido, investigar em ordem: `spawn placement`/categoria/cap, distância de jogador, teto/poleiro, luz, chunks, despawn e pathing. **Só depois alterar weight**. Se nasce em toda superfície Líquen, reduzir densidade e distinguir colônia natural da plantada; não transformar mundo em enxame.

**Uma criatura rara por definição não é boa fauna:** todo mob deve justificar ser encontrado pelo comportamento, não apenas pelo valor de coleção.

---

# 7. LOOT ECONOMY — RECOMPENSA REAL, RNG COMO EXTRAS

| POI | Recompensa garantida fora de RNG | Baú ou pool de teste (a definir) | Proibição |
| --- | --- | --- | --- |
| POI-01 Marco | Blocos B01 e informação visual | Sem chest por padrão | 0 Dust / 0 Despertador |
| POI-02 Abrigo | Proteção, bancada/fornalha vanilla e materiais recuperáveis | Se chest: 1–2 categorias utilitárias, pequenas pilhas de madeira/comida/tochas/cobre | 0 Dust / 0 Despertador |
| POI-03 Galeria | Duas rotas, pedra e trabalho recuperáveis | Se chest: 2–3 categorias de mineração/uso, sem mineral técnico futuro | 0 Dust / 0 Despertador |
| POI-04 Câmara | **Falta recompensa horizontal convincente** | Nenhum pool final antes de design aprovado | BLOQUEADO |
| POI-05 Pedreira | Materiais e percurso de extração | Se chest: recursos modestos de construção | 0 Dust / 0 Despertador |
| POI-06 Jardim | Muda/colônia real + condições de cultivo | Sem chest por padrão | Sem muda exclusiva RNG |
| POI-07 Sondagem | Duas leituras geológicas verificáveis | Suprimentos de trabalho se necessário | Sem scanner obrigatório no chest |
| POI-08 Passarela | Travessia recuperável e estrutura útil | Sem chest por padrão | Sem kit raro obrigatório |
| POI-09 Poço | Descida/retorno e estratos recuperáveis | Se chest: sobrevivência local modesta | Sem minério raro gratuito |
| POI-10 Arquivo | Padrão copiável, técnica reprodutível | Se chest: papel/objeto opcional, não chave | Não trancar recipe essencial |
| POI-11 Observatório | Observação contextual e plataforma | Sem chest ou suprimentos mínimos | Sem portal ou item final |
| POI-12 Casa de Ecos | Demonstração de circuito M06 reproduzível | Só após mecanismo real aprovado | Sem Fragmento antes de M06 |

## 7.1 Orçamento de baú por risco e relevância

**Proposta de classes, NÃO loot table configurada:**

- **Sem baú:** Marcos, Jardins, Passarelas e observação. Valor é elemento físico, material ou técnica.
- **Pequeno utilitário:** Abrigo/Pedreira; caso tenha chest, **1–2 categorias** de suprimento em pilhas modestas, sem equipamento que encerra o Early.
- **Médio de exploração:** Galeria/Poço; **2–3 categorias**, itens vanilla que respeitam o desafio, possíveis materiais próprios **só quando aprovados e já obtíveis fora de baú**.
- **Especial opcional:** Arquivo/Observatório/Câmara/Casa; benefício principal é capacidade/experiência, e chest é suplemento, nunca a única razão.
- **RNG raro:** disponível apenas se sua ausência **não torna a visita inútil**. Verificar quantidade esperada, repetição, loot viciado por seed e persistência.

**Anti-dupe:** loot de mundo resolve uma vez na primeira abertura. Não reabastecer por relog, abrir UI, matar guardião ou reiniciar chunk. Multi-jogador compartilha estoque. Sem `LootTableSeed` fixo para todas as cópias; validar loot por Minecraft, não por randomização inventada em Python.

## 7.2 Loot de recuperação não é a mesma economia

As duas ruínas **dimensionais** já possuem o pool de 25% Despertador + extra de comida/iluminação/materiais de travessia. **Não replicar esse pool no Overworld**. O loot das ruínas dimensionais é parte do **sistema de escape** e precisa medir recuperação segura/justa mesmo para personagem que perdeu equipamento.

**Ferramentas/suprimentos que ajudam a explorar** são positivos; outro minério/dust nos baús dimensionais poderia permitir fabricar os mesmos itens com circuito econômico não previsto e será reprovado até task específica.

---

# 8. A MATEMÁTICA DA RECUPERAÇÃO: 25% NÃO É GARANTIA

## 8.1 Modelo teórico condicionado

Se cada baú de recuperação for um sorteio Bernoulli **independente** com `p=0,25`, e todo baú visitado for **físico, alcançável e novo**:

- `P(nenhum Despertador após n ruínas) = (1 - p)^n = 0,75^n`.
- `P(encontrou pelo menos um após n) = 1 - 0,75^n`.
- `E[ruínas até primeiro sucesso] = 1/p = 4`.
- Mediana teórica: **3 ruínas**.
- Para superar 90% de chance acumulada: **9 ruínas**.
- Para superar 95% de chance acumulada: **11 ruínas**.

| Ruínas abertas (n) | Chance teórica de **ainda não** encontrar | Chance teórica de encontrar **≥1** |
|---:|---:|---:|
| 1 | 75,00% | 25,00% |
| 3 | 42,19% | 57,81% |
| 5 | 23,73% | 76,27% |
| 9 | 7,51% | 92,49% |
| 11 | 4,22% | 95,78% |

**Isto NÃO mede probabilidade real de escapar, nem tempo de recuperação**. Independência é hipótese matemática; baús podem estar abertos/saqueados em multiplayer, locais podem ser inacessíveis, o jogador pode morrer antes, e `/locate` não garante chegar ao loot. Se o jogador não consegue localizar 9 estruturas de forma plausível, a cauda de 25% é problemática mesmo que cada baú role corretamente.

## 8.2 Contrato de justiça da recuperação

A experiência desejada é: entrar em sonhos sem Despertador é arriscado, mas **não** depende de sorte sem limite ou death-respawn grátis.

**Ordem correta de correção (não implementar nesta Task):**
1. Auditar suportes, entradas e rota física até todos os baús dimensionais; resolver o alerta **4/9** da 5.7 antes de mexer em odds.
2. Medir distância e **tempo real por ruína alcançável** em ambos os mundos, sem `/locate`.
3. Verificar que loot é realmente rolado/persistido e que itens básicos de travessia tornam a expedição possível.
4. Medir quantas ruínas o jogador consegue encontrar/abrir **antes de esgotar suprimentos**; separar erro humano de geração inviável.
5. Se P90/P95 de tempo de recuperação exceder limite aprovado pelos playtests, propor **task específica de recuperação** para avaliar opções: aumentar chance, reduzir distância, sinalizar ruínas de forma legítima ou dar uma **rota alternativa cara e garantida por esforço** (sem teleporte/morte gratuita). Não aplicar as quatro mudanças ao mesmo tempo.
6. Não autorizar `dream_entry_enabled=true` amplamente apenas porque /locate e API de baús funcionam.

**Trade-off explícito:** subir a chance reduz o número de ruínas necessárias; **não** resolve ruína flutuante, inacessível ou sem sinal ambiental. Corrigir geometria é prioridade P0.

---

# 9. CRAFTING, AGRICULTURA E INFRAESTRUTURA: CUSTOS POR OBRA

## 9.1 Argamassa 6.6 — hipótese numérica preservada

Receita candidata: **2 Líquens + 1 bola de argila + 1 cascalho → 4 Argamassas**, aplicação **1 Argamassa + 1 stone vanilla colocado → 1 Pedra Marcada**. Stonecutter F0 **1:1** para Pedra Lapidada.

| Quantidade arquitetônica | Stone vanilla | Argamassa | Lotes de crafting | Líquens | Clay balls | Gravel |
|---:|---:|---:|---:|---:|---:|---:|
| 4 | 4 | 4 | 1 | 2 | 1 | 1 |
| 16 | 16 | 16 | 4 | 8 | 4 | 4 |
| 64 | 64 | 64 | 16 | 32 | 16 | 16 |
| 256 | 256 | 256 | 64 | 128 | 64 | 64 |

Esses números são **aritmética exata da proposta de recipe**, NÃO custo medido em minutos. `stone` pode exigir Silk Touch ou fundir cobblestone (combustível/tempo). Líquen precisa de taxa de cultivo positiva, com tempo e espaço. Argila/cascalho exigem obtenção natural/produção.

## 9.2 Teste de rendimento/custo que decide se a receita muda

**Experimentos com 4/16/64/256 blocos**, jogadores em equipagem comparável:

- **A:** encontrar/minerar 64 (ou 256) Pedras Marcadas naturais.
- **B:** colher Líquen reproduzível, obter argila/cascalho/stone e converter o mesmo número via Argamassa.
- **C:** usar materiais F0 já em estoque na base para obra, quando apropriado.
- Comparar `time_collect`, `time_process`, `travel`, `blocks_used`, `lichen_net`, recursos vanilla e satisfação de sessão.
- **Alvo de design:** Early favorece A (descoberta natural); Mid tardio/Late deve permitir B com menor **fricção por bloco**, mas **não produção gratuita**. A aprovação depende dos tempos medidos e do valor que jogadores realmente atribuem a projetos de obra.

**Sensibilidade de yield:** executar uma segunda variante **2/4/6 Argamassas por batch** no modelo de economia, sem alterar JSON até conclusão de QA. Para 256 blocos:
- yield 2 → **128 batches**, **256 Líquens**, 128 argilas, 128 cascalhos;
- yield 4 → **64 batches**, **128 Líquens**, 64 argilas, 64 cascalhos;
- yield 6 → **43 batches** para obter pelo menos 256 Argamassas (258 produzidas, 2 sobrantes), **86 Líquens**, 43 argilas, 43 cascalhos.

**Cuidado:** yield maior pode esconder F1 sem progressão e tornar construção trivial; yield menor pode gerar agricultura compulsiva. Ajustar **somente após medir** e mantendo uma transformação física 1:1.

## 9.3 Cinzel, Leitor e itens opcionais

**Cinzel I02:** receita candidata da 6.6 (ferro, cobre, graveto, Pedra Lapidada), durabilidade **128** de teste. A/B vs quebrar/stonecutting/recolocar; ciclos `marked ↔ cut` devem conservar um full block e não conceder XP/drops. Ajustar durabilidade só se uso/custo forem demonstrados.

**Leitor B26:** 2 redstone+cobre+Líquen+Pedra Lapidada, possível saída `0/4/8/12/15` por quatro vizinhos. Antes de cotar preço, provar que **quatro posições elegíveis existem na geometria real do Líquen**, que os estados mudam em Survival e que vantagem vs observer/comparator é real.

**Molde I03, Escora I04, Sonda I06, Diapasão I15:** custos ainda **sem recipe aprovado**. Nunca criar tabela de preços arbitrária antes de provar sua capacidade em M02/M03/M06.

**Nódulo B21:** sem rate de worldgen ou custo enquanto M06 não demonstrar desempenho e dois consumidores verdadeiros. Um material técnico que o jogador precisa para instalar muitos circuitos não deve ser mais escasso que o próprio sistema permite usar.

---

# 10. AMOSTRAGEM: COMO TER CONFIANÇA SEM INVENTAR NÚMEROS

## 10.1 Dois estágios, evidências distintas

**Estágio A — validação estática/eligibilidade:** checar JSON, biome modifiers, `placement`, tags, pool, caps, receita, loot, suporte por algoritmo, conjunto de biomas e predicados. Medir **candidatos** e possíveis rejeições em seeds, sem chamar isso de geração natural.

**Estágio B — runtime real + playtest cego:** gerar chunks novos em servidor com **Minecraft 26.3/NeoForge 26.3**, escanear blocos reais/starts aceitos, inspecionar entrada/apoio, e então jogar **Survival sem comandos de descoberta**. A+B são complementares, não substitutos.

**Baseline das quatro seeds existentes:** 14011, 20261108, -904732, 73199013. Usar as mesmas sementes/regiões para regressão de minério, e adicionar seeds/geografias **pré-registradas** para verificar generalização; não selecionar apenas screenshots bonitas. Registrar MC/NeoForge/mod SHA, dimensão, bioma, Y, hora de sessão, ferramenta e mods coexistentes.

## 10.2 Plano de amostragem mínimo escalonado

| Conteúdo | CI rápido | Auditoria ampliada | Teste humano |
|---|---|---|---|
| **R0 minério** | Reusar 4×256 chunks e artefatos da 5.6 | Estender se A/B indicar problema, com seeds independentes | 3/12/15/39 Dust, ferramenta normal/Fortune |
| **F0/F1** | Verificar features em várias regiões válidas por seed | >=4 seeds com superfícies/cavernas estratificadas; registrar tentativas falhas | Descoberta, colheita, obra e 2 ciclos de planta |
| **POI-01/02** | Vários starts por seed, não só um /locate | Uma segunda região por seed para evitar sobreajuste | Jogador novo encontra sem localizar e usa recompensa |
| **POI-03/09** | Teste de volume + rota+entrada+água em 4 seeds | Candidatos aceitos/rejeitados, múltiplas topologias/alturas | Duas abordagens e saída sem Spectator |
| **POI-04/12** | **Sem gerar worldgen enquanto bloqueados** | Só após recompensa/M06 aprovada | Desafio opcional significativo |
| **MOB-01/02** | Condições de spawn e lista vanilla antes/depois | 4 seeds, biomas/habitats variados, 2 jogadores | 20–40 min em habitat elegível, contagem/encontros |
| **Loot onírico** | Parse JSON/persistência | >=100 aberturas reais do Minecraft para sanity, ampliar se variância alta | Várias ruínas/2 jogadores/restart |
| **Performance** | Dedicated server e checks já existentes | Perf baseline vs mod com feature, mundo novo/antigo | FPS/TPS e consumo de memória na exploração |

**Aviso metodológico:** `100` aberturas reais com `p=0,25` têm incerteza amostral considerável (erro-padrão aproximado `sqrt(p(1-p)/100) = 0,0433` = 4,33 pontos percentuais). Um resultado de 20–30 em 100 é plausível e **não certifica** chance exatamente 25%; a regra exata vem do pool configurado. Recomenda-se intervalo Wilson para reportar probabilidade empírica e aumentar para centenas/milhares de aberturas em instrumento de QA **se a decisão depender dela**.

## 10.3 Percentis e métricas (não esconder fracassos)

**Tempo de descoberta:** usar cronômetro e registrar sessões **censuradas** (jogador não encontrou no tempo-limite), não simplesmente descartar o participante. Percentis de tempo não podem ser estimados ignorando censura como se todos encontrassem.

**Distância:** usar distância percorrida por player e distância até **entrada física**, não só straight-line start. Para estruturas subterrâneas, registrar depth e caminho seguro.

**Distribuição:** medir por bioma/altura/tipo/seed, usando regiões fixas para regressão e regiões novas independentes para generalização. Medir **densidade conjunta** de POIs e não apenas uma estrutura por vez.

**Mobs:** contar spawns válidos, vivos simultâneos, encontros observados, despawns, ratio vanilla/custom, hitbox inválida e chunks novos/antigos.

**Loot:** diferenciar presença teórica, conteúdo rolado e recuperação efetiva após obter item. Verificar estado compartilhado e não regeneração.

**Percepção:** registrar “encontrei algo novo”, “entendi como usar”, “foi repetitivo”, “houve sinal suficiente antes do risco”, “voltaria procurar outro exemplar”.

## 10.4 Registro padronizado de experimento

Formato **proposto** para relatórios futuros, não script instalado:

```json
{
  "task": "6.8",
  "status": "MEASURED | NOT_RUN | INCOMPLETE",
  "scenario": "f0_discovery | f1_cultivation | poi_route | dream_return",
  "build_sha": "<commit exato>",
  "minecraft_version": "26.3",
  "neoforge_version": "<versao efetiva>",
  "seed": 14011,
  "dimension": "minecraft:overworld",
  "biome": "<id real observado>",
  "starting_inventory": "<snapshot>",
  "rules": { "difficulty": "<valor>", "fortune": 0, "keepInventory": false },
  "start_xyz": "<coordenadas reais>",
  "elapsed_seconds": null,
  "outcome": "FOUND | NOT_FOUND | DEATH | BLOCKED | NOT_RUN",
  "observed": { "blocks": null, "chests_opened": null, "dust_acquired": null },
  "evidence": ["server_log", "screenshot", "video", "inventory_snapshot"]
}
```

**Regra:** não fabricar números para preencher `null`. O campo `outcome=NOT_FOUND` é dado real (com tempo censurado), diferente de `NOT_RUN` que indica que experimento não aconteceu.

---

# 11. PERFIS DE DIFICULDADE E SENSIBILIDADE (SEM FIXAR CONFIG FINAL)

## 11.1 Princípio de equilíbrio

**Três perguntas diferentes:**
- **É encontrável?** Descoberta do recurso/POI.
- **É conquistável?** Risco/retorno, tempo e insumos necessários.
- **É útil depois?** Uso na base, próximos projetos ou outra sessão.

Aumentar escassez só responde parcialmente à primeira; pode arruinar segunda e terceira. Tornar todo recurso abundante pode apagar o prazer da descoberta. A 6.8 precisa proteger **a curva de aprendizado** de cada linha.

## 11.2 Experimentos de sensibilidade em ordem segura

| Problema observado | Primeiro parâmetro a investigar | Evitar |
|---|---|---|
| F0 invisível em Survival | Forma da pista, altura/exposição, biome eligibility | Aumentar count universal sem medir |
| F0 tão comum que tudo parece igual | Cluster/bioma/contexto de ocorrência | Mudar 4 valores ao mesmo tempo |
| F1 impossível de cultivar | Suporte/growth/colheita, saldo após replante | Drop extra de mob |
| Argamassa cara demais por obra | Taxa de Líquen, yield 2/4/6 e custo vanilla | Criar nova máquina obrigatória |
| POI raro demais | Entrada/silhueta/contexto/rejeição de terreno | Inflar loot para justificar caminhada |
| POI saturando Overworld | Número de sets e variantes, pool conjunto, spacing | Aumentar rarity de tudo por igual |
| Mariposa não aparece | Spawn placement, habitat, caps/bioma | Criar timer de spawn no player |
| Habitante injusto | Aviso, rota de fuga e perseguição limitada | Dar menos HP e chamar de resolvido |
| M06 material impraticável | Provar circuito e demanda, depois disponibilidade | Lançar minério raríssimo primeiro |
| Jogador fica preso nos sonhos | Acesso físico às ruínas e recuperação, depois chance/cadência | Morte como saída gratuita |
| Despertador obtido muito cedo | Regras de **entrada** e tempo real até Colar/retorno | Aumentar preço de Despertador cegamente |
| Late perdeu todo desafio | Manutenção/preparo, objetivos opcionais e variedade | Boss obrigatório/endgame artificial |

**Regra de experimentação:** alterar **uma variável por versão de teste**, manter seeds/regiões e inventário controlados, registrar antes/depois; só consolidar melhoria no jogo depois de teste independente e ausência de regressão.

---

# 12. PERFORMANCE E EFEITOS SOBRE MUNDOS EXISTENTES

- Não executar scanner de chunk para minério/POI durante player tick; auditoria `dream_ore_audit` só em ambiente de testes com flag JVM.
- Não adicionar carregamento permanente de chunks para Líquen, mobs, circuitos ou estruturas.
- Não promover 12 POIs simultaneamente: geradores podem competir por terreno e gerar overhead significativo por chunk.
- Para features F0/F1, medir tempo de geração e número de blocos escritos/descartados; erro de posicionamento deve ser corrigido antes de diminuir rarity.
- M06, se aprovado, não pode depender de busca ilimitada de rede quando há pulso; instrumentar tempo de atualização e limitação de caminho.
- Novos mobs não devem multiplicar AI goals ou entidades perto de bases e vilas; verificar orçamento com 2/4/8 exemplares.
- Saves antigos: novas features somente em **chunks ainda não gerados**, sem retrogen oculta; receitas adicionadas não apagam inventários; structures mantêm chest data persistente.
- Players em multiplayer podem encontrar POI saqueado: materiais básicos continuam obtíveis **fora** de baú/estrutura; valor de POI deve sobreviver à ausência de prêmio RNG.
- Dificuldade Peaceful, gamerules, `keepInventory` e configs do servidor precisam de cenários separados. Uma estrutura não deve criar inimigo obrigatório para jogador pacífico.
- Loot e disponibilidade em mundos modded precisam observar interação com biomas/tag `#c:is_overworld` e modpacks, sem assumir que toda dimensão custom usa essa tag corretamente.

---

# 13. COMANDOS DE QA — EXISTENTES VS CANDIDATOS

**A Task 6.8 não registra F0/F1, nenhum dos 12 POIs novos ou mobs Overworld.** Não anunciar comandos falsos de `/locate`, `/give` ou `/summon`.

## 13.1 Laboratório de mineração/crafting existente

Em mundo **descartável**:

```mcfunction
/seed
/gamemode creative
/give @s minecraft:iron_pickaxe 1
/give @s dreamsdimensions:dream_ore 16
/give @s dreamsdimensions:deepslate_dream_ore 16
/gamemode survival
```

Para testar blocos colocados **fora dos pés** e em local livre, apenas após verificar o ambiente:

```mcfunction
/setblock ~2 ~ ~ dreamsdimensions:dream_ore
/setblock ~3 ~ ~ dreamsdimensions:deepslate_dream_ore
/clear @s dreamsdimensions:dream_dust 0
```

Esses comandos ajudam **QA de loot e ferramenta**, mas minério colocado manualmente **não comprova frequência natural**. Fortune/Silk e mineração em Survival sem cheats devem ser feitos em cenários distintos.

**Crafting R0 em bancada, sem dar itens resultantes:**

```mcfunction
/give @s dreamsdimensions:dream_dust 15
/give @s minecraft:copper_ingot 3
/give @s minecraft:amethyst_shard 1
/give @s minecraft:stone 8
/give @s minecraft:clock 1
/give @s minecraft:feather 1
/give @s minecraft:glass_bottle 1
```

Craftar Colar, 4 Infused Stones, Núcleo e Despertador para conferir 15 Dust. Isso testa receitas, **não** obtenção natural nem timing.

## 13.2 Inspeção de ruínas dimensionais existentes

IDs atuais verificáveis, usados na Task 5.7:

```mcfunction
/execute in dreamsdimensions:campo_onirico_azul run locate structure dreamsdimensions:blue_dream_ruins
/execute in dreamsdimensions:dreamscape run locate structure dreamsdimensions:fragmented_dream_ruins
```

Os comandos localizam **starts**, não baús nem locais seguros. Fazer `/tp` somente após encontrar **XYZ real** com piso/apoio inspecionados em Spectator; não inventar coordenadas de POIs novos. O exemplo real da Task 5.7 de suporte **4/9** permanece alerta, não destino homologado.

**Tabela de loot em ambiente de teste (não é baú físico encontrado):**

```mcfunction
/loot give @s loot dreamsdimensions:chests/blue_dream_ruins
/loot give @s loot dreamsdimensions:chests/fragmented_dream_ruins
```

Abrir um baú real, reiniciar mundo e verificar persistência com duas contas é outro teste. Não usar `/place structure` ou `/loot` para alegar natural spawning/recuperação completa.

## 13.3 Diagnóstico opt-in

Em dedicated server iniciado com `-Ddreamsdimensions.enableOreAudit=true` a Task 5.6 registra `dream_ore_audit` no laboratório. O comando faz leitura **somente em chunks previamente carregados**, limitado a 8×8 chunks, não move jogador nem carrega mundo por si. Não instalar rotina periódica, nem usar RCON/forceload no servidor público.

```mcfunction
/dream_ore_audit -4 -4 8
```

**Essa região é um local de amostragem pré-escolhido**, não coordenada de minério ou destino seguro. Usar `/seed` para confirmar o mundo de teste e carregar a região somente no workflow descartável. Se chunks não estiverem carregados, o comando recusa. O teste humano deve ser independente do auditor.

---

# 14. PLANO DE ACEITAÇÃO, RISCOS E PRIORIZAÇÃO

## 14.1 Gates P0: bloqueiam release antes de discutir raridade

- **R1 — Segurança de recuperação:** todas as ocorrências usadas em teste de retorno têm entrada/baú acessíveis; suporte estrutural e caminho de volta reais, não só /locate.
- **R2 — Nenhuma dependência circular:** primeiro Colar/Despertador não exige item de sonho, boss ou POI raro.
- **R3 — Sem duplicação:** Argamassa, chests, mobs e mecânica de pulso não geram Dust/itens premium grátis.
- **R4 — Integridade vanilla:** spawns não apagam criaturas vanilla, POIs não destroem estruturas vanilla, biomas não são sobrescritos indevidamente.
- **R5 — Sem grief/softlock:** pedra cediça não afeta construções dos jogadores, travessias sempre têm opção viável e risco legível.
- **R6 — Mundo antigo e multiplayer:** não mexer em chunks explorados, loot world-shared, inventários salvos intactos.
- **R7 — Performance:** sem scans por player tick, loops infinitos de redstone ou forceload de habitat.
- **R8 — Identidade do jogo:** Overworld deve continuar útil e coerente com vanilla, sem acesso obrigatório aos sonhos nem novo boss final.

## 14.2 Metas P1/P2 verificáveis

| Gate | Como será verificado | Decisão se falhar |
|---|---|---|
| F0 encontrável | P50/P90 elegíveis, volume para obra, textura distinta | Ajustar forma/habitat, depois taxa |
| F1 cultivável | Duas colheitas e saldo positivo | Corrigir suporte/propagação antes da Argamassa |
| Argamassa útil | 64/256 blocos, custo e fricção vs mineração | Yield e cultivo, não minério artificial |
| POIs variados | 1º encontro, taxa de completion, retorno à base, rotas | Fundir variantes ou melhorar level design |
| Mariposa presente | Spawn natural no microhabitat + vanilla preservado | Corrigir placement/cap antes de weight |
| Habitante justo | Aviso/território/fuga observados | Ajustar IA antes de vida/dano |
| Recovery dimensionais | P50/P90 de ruínas acessíveis, P90 de primeiro retorno sem cheats | Task específica de recovery, não retorno por morte |
| R0 progressão | 3/15/39 Dust e despesas vanilla, primeiro retorno | Task separada de entrada/crafting se comprovado |
| M06 worth it | Dois usos distintos, A/B vs redstone/sculk e custo de circuito | Cancelar nódulo e seus dependentes |

## 14.3 Ordem de investigação em 6.9

1. **P0 recuperação/risco da baseline:** inspeção do caso 4/9 e Survival real de retornos, sem alterar regra 25% automaticamente.
2. **F0/F1 vertical slices:** sondagem natural, design de pista e cultivos, separadamente.
3. **Argamassa:** produção em escala, teste de yield, integração server-side.
4. **POI-01/02, Pedreira/Jardim:** distribuição agregada, utilidade sem baú e terreno.
5. **Mariposa:** spawn natural pós-F1 e não supressão de mobs vanilla.
6. **M02/M03:** arqueologia/restauro em protótipos isolados, antes de equipar 12 estruturas.
7. **POI-03/08/09 e M2:** worldgen subterrâneo/vertical, rotas e risco.
8. **M06:** prova técnica curta com proxies antes de minério e Casa de Ecos.
9. **POI-04 Câmara:** continua **BLOCKED_BY_REWARD_DESIGN**; Guardião não é razão de criar arena sem recompensa.
10. **Revisão de entrada/receitas oníricas** apenas se 6.8 confirmar descompasso de Mid/Late, mantendo rota segura de retorno.

**Task 6.9 deverá transformar este contrato em PRs incrementais**, com qualquer número de spacing, rate, spawn weight ou loot explicitamente rotulado como PROPOSTO até medição real aprovada.

---

# 15. REFERÊNCIAS E STATUS DE EVIDÊNCIA

## 15.1 Internas

- [Worldgen real de minérios — 5.6](OVERWORLD_ORE_DISTRIBUTION_TASK_5_6.md)
- [Economia e receitas R0 — 5.6](OVERWORLD_ECONOMY_TASK_5_6.md)
- [Ruínas dimensionais / loot de recuperação — 5.7](DREAM_RUINS_TASK_5_7.md)
- [Geologia e flora — 6.2](OVERWORLD_RESOURCES_GEOLOGY_FLORA_BLOCKS_TASK_6_2.md)
- [Ecologia e spawns — 6.4](OVERWORLD_ECOLOGY_MOBS_HABITATS_TASK_6_4.md)
- [Estruturas — 6.5](OVERWORLD_STRUCTURES_ENVIRONMENTAL_DISCOVERY_TASK_6_5.md)
- [Receitas e economia — 6.6](OVERWORLD_CRAFTING_PROCESSING_PROGRESSION_TASK_6_6.md)
- [Catálogo expandido — 6.6.1](OVERWORLD_UNIVERSE_CONTENT_EXPANSION_TASK_6_6_1.md)
- [Art Bible — 6.7](OVERWORLD_VISUAL_DIRECTION_TEXTURES_MODELS_ASSETS_TASK_6_7.md)

## 15.2 Documentação oficial de compatibilidade

- [NeoForge Primer 26.3](https://docs.neoforged.net/primer/docs/26.3/) — worldgen/placements e mudanças de spawn por EnvironmentAttributes.
- [NeoForge Worldgen](https://docs.neoforged.net/docs/worldgen/) — features, placements, registros.
- [NeoForge Biome Modifiers](https://docs.neoforged.net/docs/worldgen/biomemodifier/) — adicionar conteúdo respeitando spawns vanilla.
- [NeoForge Loot Tables](https://docs.neoforged.net/docs/resources/server/loottables/) — parâmetros de loot, condições, persistência de containers e formato de dados.
- [Minecraft Java 26.3](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3) — versão alvo.

**Regra de fonte:** manter formato 26.3 efetivo dos JSON da branch como baseline; tutoriais antigos e páginas oficiais de versão distinta devem ser revalidados durante implementação.

## 15.3 Sem confundir dados e hipóteses

| Tipo | Exemplos da 6.8 | Pode dizer “observado”? |
|---|---|---|
| **MEDIDO EM RUNTIME ANTERIOR** | 11.236 voxels / 1.024 chunks, 324 adjacentes a ar, 8 ruínas com baú | **Sim, com limites da amostra** |
| **VERIFICADO NOS JSONs** | 2 tentativas/chunk, size 6/5, R0 3/12 Dust, pool 25%, spacing 38/32 | **Sim, como configuração** |
| **MATEMÁTICA DO MODELO** | P(no Despertador após n)=0,75^n; 4 ruínas em média teórica | **Não, é inferência sob hipóteses** |
| **PROPOSTO PARA TESTE** | F0 1/8–20 chunks elegíveis, POI spacing da matriz, crescimento e grupos de mobs | **Não** |
| **NÃO EXECUTADO** | First-discovery P50/P90, tempo até 15/39 Dust, crescimento real F1, loot rolado por player | **Não; marcar NOT RUN** |
| **BLOQUEADO** | Câmara sem reward, Casa sem M06, Nódulo sem consumidores | **Não gerar assets/registry** |

---

# 16. CRITÉRIOS DE ACEITE E VEREDITO

## Documentação 6.8 — CONCLUÍDA

- [x] Confrontou 6.0–6.7 com worldgen, receita e loot R0 realmente registrados.
- [x] Preservou geração original de minérios enquanto esforço Survival não foi medido.
- [x] Separou fonte de escassez, tempo de descoberta, coleta, processamento, risco e valor.
- [x] Definiu metas de tempo **candidatas** para recursos/primeiro POI com contexto elegível.
- [x] Propôs condições/frequências F0/F1/M02/M03 com gates de habitat/feedback.
- [x] Produziu matriz de **12 POIs com spacing/separation candidatos**, sem registrar estruturas.
- [x] Definiu orçamento de seis criaturas por habitat e política contra overwriting de fauna vanilla.
- [x] Projetou matriz de recompensa física e pools opcionais sem item-chave obrigatório.
- [x] Calculou cauda teórica real de **25% por baú** e risco de longa recuperação.
- [x] Preservou 3+12N Dust, testou sensibilidade da receita de Argamassa 2/4/6 como modelo.
- [x] Definiu critérios P0/P1/P2, estatística, seeds, logs, métricas e comandos de laboratório.
- [x] Evitou números fabricados de worldgen novo, loot observado, P90 de descoberta e TPS.
- [x] Não alterou código, worldgen, recipes, loot, mobs, configuração, dimensões ou main.

## Homologação de gameplay — AINDA PENDENTE

- [ ] Mineração em Survival sem cheats e Fortune/Silk Touch real.
- [ ] Primeira visita e retorno aos sonhos testados com equipamento preparado e sem ele.
- [ ] Ruínas de recuperação com suporte/rota real em múltiplos starts; caso 4/9 resolvido.
- [ ] F0/F1/Argamassa implementados e medidos como fonte econômica.
- [ ] POIs 01–12 gerados conforme seleção e calibrados em várias seeds/biomas.
- [ ] Fauna de Overworld realmente gerada sem apagar spawns vanilla.
- [ ] Recompensa da Câmara aprovada ou estrutura mantida bloqueada.
- [ ] M06 demonstrou utilidade antes de adicionar minério técnico.
- [ ] Relatórios de percentis com dados reais e tentativas censuradas publicados.
- [ ] CI, dedicated server, saves antigos, multiplayer e desempenho validados após PRs funcionais.

**Veredito:** **Task 6.8 concluída como especificação de economia e balanceamento.** O mod continua funcionalmente igual à baseline. A maior prioridade revelada não é aumentar ou diminuir uma chance isolada: é **transformar raridade em descoberta significativa e garantir que o retorno dos sonhos seja difícil, mas verificavelmente recuperável**.

**Próxima Task: 6.9 — Roadmap Executivo de Implementação.** Ela deverá começar pela segurança do sistema atual e organizar as expansões em fatias jogáveis com critérios de evidência e rollback claros.

---

*Fim — Task 6.8. Contrato quantitativo proposto, nenhum tuning ou gameplay implementado.*
