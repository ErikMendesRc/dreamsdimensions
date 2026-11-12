# TASK 6.6.1 — EXPANSÃO DO UNIVERSO E CATÁLOGO DE CONTEÚDO

## DreamsDimensions: Overworld amplo, mecânicas conectadas e direção Early / Mid / Late

**Tipo:** Game Design / Expansão Criativa / Arquitetura de Sistemas — **documentação, sem implementação**.  
**Data:** 12/11/2026. **Branch:** `migration/minecraft-26.3`.  
**HEAD base auditado:** `e5a6cd79fccfffb799956446b923a6a7c5877bb2`.  
**Dependências:** [Bible 6.0](GAME_DESIGN_BIBLE_TASK_6_0.md), [Pilares 6.1](OVERWORLD_GAMEPLAY_PILLARS_TASK_6_1.md), [Geologia/Flora 6.2](OVERWORLD_RESOURCES_GEOLOGY_FLORA_BLOCKS_TASK_6_2.md), [Itens 6.3](OVERWORLD_ITEMS_CONSUMABLES_TOOLS_STATIONS_TASK_6_3.md), [Mobs 6.4](OVERWORLD_ECOLOGY_MOBS_HABITATS_TASK_6_4.md), [Estruturas 6.5](OVERWORLD_STRUCTURES_ENVIRONMENTAL_DISCOVERY_TASK_6_5.md), [Crafting 6.6](OVERWORLD_CRAFTING_PROCESSING_PROGRESSION_TASK_6_6.md).  
**Próximas etapas:** seleção de portfólio → **6.7 Direção Visual** → **6.8 Economia/Raridade** → **6.9 Roadmap Executivo**.  
**Escopo do commit:** somente este Markdown; nenhum bloco, item, registro, NBT, modelo, recipe, spawn ou worldgen criado; não fazer merge na main.

> **Correção de rumo:** a regra “nenhum item sem sentido” virou um freio excessivo para imaginar conteúdo. Nesta Task o objetivo é abrir o universo de modo ambicioso, dar a cada ideia função e conexões, e depois selecionar. Catálogo não é backlog automático. O Overworld tem vida própria e os sonhos só passam a ser consistentemente exploráveis no Mid tardio/Late, **sem um End Game específico do mod**.

---

# 0. DIAGNÓSTICO E NOVA ESCALA

A Bible 6.0 já trazia hipóteses de arqueologia, ferramentas de observação, geologia reativa, sistemas de sinal, flora de suporte, fauna contextual e estruturas variadas. As Tasks 6.2–6.6 concentraram decisões implementáveis em F0 **Pedra Marcada**, F1 **Líquen**, **Argamassa**, Cinzel/Leitor condicionados, quatro estruturas e a primeira criatura. Isso é uma boa fundação, mas não cobre toda a amplitude de exploração desejada.

**Resultado desta expansão:**

| Categoria de catálogo | Entradas conceituais |
|---|---:|
| Blocos, plantas e variantes **B01–B30** | **30** |
| Itens, consumíveis e equipamentos **I01–I18** | **18** |
| Estruturas/POIs **POI-01–12** | **12** |
| Criaturas **MOB-01–06** (inclui MOB-04 adiado) | **6** |
| Mecânicas interconectadas **M01–M09** | **9** |

**As 30 entradas de blocos NÃO representam 30 minérios**: há variantes arquitetônicas e itens condicionados, e os 18 itens incluem **alternativas concorrentes**. As 12 estruturas incluem **as quatro anteriores + oito novas candidatas**; não é promessa de gerar 12 POIs na próxima versão.

## 0.1 Estados e regras de promoção

- **EXISTENTE:** código/JSON da branch; só Colar, Despertador, Dream Dust e outros registros auditados possuem esse status.
- **A:** já selecionado para especificação nas Tasks anteriores, **não** implementado.
- **B:** nova hipótese com loop concreto; pode passar à **prova de conceito**, não a produção.
- **C:** ideia de aprofundamento ou alternativa competidora; candidata a fusão/rejeição.
- **BLOQUEADO:** depende de uma experiência/recompensa ausente; não registrar nem gerar assets.
- **ADIADO:** manter contexto conceitual, sem implementar ou canonizar lore.

**Novos IDs B/I são códigos de catálogo, não registry IDs.** Nenhuma decisão desta Task converte automaticamente um material da 6.2 ou um dispositivo da 6.3 em item aprovado.

## 0.2 Invariantes que continuamos respeitando

R0 atual auditado: **Colar = 3 Dream Dust**; **Despertador = 12 Dust recursivos** mais vanilla. Total de um Colar e N retornos preparados: **3 + 12N Dream Dust**. Dream Dust é encontrado em dois minérios do Overworld; não pode ser produzido por Líquen/mob nem ter sua obtenção alterada aqui. Retorno segue consumo de Despertador só após confirmação. Os mobs Fragmento Errante/Vigia Adormecido e as duas ruínas já existentes são **dimensionais**, não novos conteúdos do Overworld.

---

# 1. CINCO FAMÍLIAS DE EXPANSÃO DE GAMEPLAY

## 1.1 Geologia que informa, não um minério em cada bioma

**Estratigrafia observável:** B16 apresenta orientação física de camadas. B15 é sedimento em posições **contextuais**, escovável com **brush vanilla**; M01/M02 relacionam pistas com POIs e padrões decorativos. Uma camada deve indicar **algo verificável** no terreno/estrutura, e não ser mera textura vendida como detector.

**Novo problema útil:** como notar que uma rocha foi exposta, trabalhada ou coberta por material orgânico? O jogador pode comparar duas amostras fisicamente presentes. Uma Sonda I06 só é aprovada se fizer algo que observar textura, usar F3 ou clicar normalmente não faz.

**Sem promessa de minério oculto:** não comunicar que uma linha de B16 aponta para Dream Ore se a geração natural não tiver correlação real.

## 1.2 Arqueologia de padrões que chegam à base

**Ciclo M02:** notar sedimento contextual → escovar com o brush vanilla → observar **padrão concreto** → usar Molde I03 em painel compatível → reproduzir acabamento B29 na construção.

Isso oferece uma recompensa **não associada a matar mob ou abrir baú**, sem travar stonecutter, plantio ou primeiro Colar. Pode ser obtida parcialmente em POI-05/10, mas não impor pergaminho de RNG como chave. Se a ação final só produz “livro de lore sem uso”, cancelar.

O **Molde** só deve existir se copiar/aplicar padrão for experiência distinta de stonecutter; se for possível usar ferramenta vanilla, priorizar a solução menor. Se for material do próprio item, proteger contra remainder/duplicação e serialização inconsistente.

## 1.3 Travessia, instabilidade e reconstrução

**Ciclo M03:** jogador vê piso B17 rachado ou ouve aviso → decide contornar com blocos/escadas vanilla, atravessar com risco conhecido ou usar Kit I04 para estabilizar B18 e montar apoio B19.

**Ação real:** reconstruir uma passagem **no próprio mundo**, com custo; após concluída ela permanece útil. POI-08 Passarela Rompida e POI-03 Galeria podem demonstrar a mecânica em contextos diferentes.

**Guardrails P0:** não aplicar fragilidade à stone vanilla em massa, não afetar construções existentes, não fazer TNT ambiental, não quebrar blocos de outros mods, nunca criar queda fatal inevitável. O aviso deve ser visual, não apenas som ou partículas. Comparar contra scaffolding/pilares vanilla: se Kit I04 só reproduz o trabalho da picareta sem vantagem, não criá-lo.

**Hera B11** e Cordame I10 são alternativas de travessia **condicionadas**: devem construir trechos curtos entre apoios físicos, sem atravessar chunks/gerar uma “tirolesa” que invalida Elytra, scaffolding ou escadas vanilla.

## 1.4 Flora com resposta observável e utilidade fora do canteiro

F1 Líquen continua primeira planta: colher, replantar, ter saldo positivo e usar na Argamassa. A expansão considera B11 Hera (crescimento com suporte), B12 Raiz Sensível (reage a estímulo local), B13 Flor de Vigília (abre ou ilumina sob condição física) e B14 Fungo de Penumbra (reage a luz/sombra).

Cada candidato precisa de **habitat natural, ciclo testável, fonte e aplicação**. Não produzir semente/fibra/pigmento separado se a utilidade se resolver com o próprio bloco ou itens vanilla.

O Leitor de Colônia B26 pode ler estados de plantas imediatamente vizinhas, mas seu design deve levar em conta que plantas podem ser **coberturas multiface**, e não full blocks. O valor de uma saída analógica só existe se houver projeto de jardinagem/redstone que realmente aproveite a leitura.

## 1.5 Ressonância local de pedra — hipótese mais ambiciosa

A 6.2 cogitou Nódulo de Reverberação para um mero pulso atrasado. A 6.3/6.6 corretamente **não aprovaram** isso: repeater/observer já atendem muitas situações. Esta Task propõe **um mecanismo alternativo M06** para testar antes de voltar a aprovar minério:

**Fantasia de engenharia:** o jogador transmite **um pulso curto por um caminho físico contíguo de pedra especializada**, mesmo dentro de paredes opacas, sem fio de redstone exposto na superfície. B23 Emissor transforma pulso vanilla em entrada; B22 conduz; B24 Receptor gera uma saída; B25 Lâmpada sinaliza o estado; I15 Diapasão auxilia diagnóstico **somente no bloco tocado**.

**Protótipo hipotético:** no máximo **12 segmentos carregados por percurso**, número a medir; buscar caminho local limitado, cortar ciclos, sem forceload e **sem player/world tick para varrer redes**. Se um segmento estiver em chunk descarregado, circuito fica inativo até atualização válida, sem buscar chunks.

**Dois usos diferentes mínimos:** (1) acionamento de saída redstone em arquitetura fechada; (2) diagnóstico/sinalização local genuinamente útil. Três receitas de blocos com efeito idêntico não contam como três capacidades.

**Prova anterior ao novo minério:** primeiro montar circuito experimental com blocos-proxy/recursos já existentes. **Só se M06 passar comparação com redstone dust, repeater, observer, sculk sensor e copper bulb**, reabrir B21 Nódulo Reverberante + I14 Fragmento como recurso geológico específico. Caso contrário cancelar também POI-12 e Grilo de Eco dependentes do fenômeno.

**Não** criar transmissão global entre bases, entre dimensões, para detectar minérios, ou para despertar automaticamente.

---

# 2. CATÁLOGO DE BLOCOS E FLORA — 30 ENTRADAS

| ID | Nome | Linha | Estágio | Status | Ação / uso | Dependência / teste |
| --- | --- | --- | --- | --- | --- | --- |
| B01 | Pedra Marcada | GEO | Early | A | Rocha obtida naturalmente; fonte construtiva F0, sem Dream Dust. | F0 6.2 |
| B02 | Pedra Marcada Lapidada | BLD | Early | A | Stonecutter 1:1, primeiro acabamento de base. | B01 |
| B03 | Escadas Marcadas | BLD | Early/Mid | B | Variante arquitetônica, sem minério novo. | B02; depois de F0 |
| B04 | Lajes Marcadas | BLD | Early/Mid | B | 2 lajes por full block, conserva volume. | B02; depois de F0 |
| B05 | Muro Marcado | BLD | Early/Mid | B | Conexões e acabamento de perímetro/ruína. | B01/B02 |
| B06 | Tijolos Marcados | BLD | Mid | C | Família de obra/restauração em escala. | B02; comparar vanilla |
| B07 | Tijolos Fraturados | BLD | Mid | C | Estado arquitetônico desgastado, sem loot extra. | B06; demanda real |
| B08 | Painel de Relevo | ARC/BLD | Mid | B | Superfície que mostra padrão copiável da arqueologia. | I03; POI-10 |
| B09 | Coluna Estratificada | BLD | Mid | C | Pilar visual que comunica geologia em construções. | B16; estética |
| B10 | Líquen dos Vestígios | FLR | Early | A | Coleta, transplante e cultivo positivo de F1. | 6.2 F1 |
| B11 | Hera do Repouso | FLR | Mid | B | Cresce em suporte vertical e pode alimentar cordame curto. | I09/I10; testar vines |
| B12 | Raiz Sensível | FLR | Mid | B | Encolhe ante estímulo local; estado legível para observação. | M04; sem scan global |
| B13 | Flor de Vigília | FLR | Mid | B | Abre ou ilumina sob condição local clara. | M04; comparar daylight sensor |
| B14 | Fungo de Penumbra | FLR | Early/Mid | B | Muda estado em luz/sombra; eventual pigmento específico. | I12; comparar fungo vanilla |
| B15 | Sedimento de Marcas | ARC/GEO | Early/Mid | B | Camada escovável com brush vanilla, padrão físico, sem chave RNG. | M02; worldgen contextual |
| B16 | Estrato Exposto | GEO | Early/Mid | B | Rocha orientada comunica camadas/fissura real. | M01; correlação honesta |
| B17 | Rocha Cediça | GEO/TRV | Mid | B | Piso especial avisa instabilidade antes de ceder em POI. | M03; anti-grief |
| B18 | Rocha Escorada | TRV | Mid | B | Estado reforçado de travessia, conversão com custo. | B17/I04 |
| B19 | Escora de Campo | BLD/TRV | Mid | B | Peça visível de reparação de apoio de ponte/galeria. | I04; duas rotas |
| B20 | Passarela Recuperada | BLD/TRV | Mid | C | Piso modular que permanece útil após restauro. | POI-08; comparar madeira |
| B21 | Nódulo Reverberante | GEO | Mid/Late | B | Depósito com reação local observável, NÃO aprovado como minério. | M06 antes do worldgen |
| B22 | Pedra Condutora de Eco | TEC | Mid/Late | B | Propaga pulso em sequência sólida curta e carregada. | M06; B21 apenas se aprovado |
| B23 | Emissor de Eco | TEC | Mid/Late | B | Recebe pulso de redstone vanilla e injeta em caminho lítico. | B22; 6.8 |
| B24 | Receptor de Eco | TEC | Mid/Late | B | Emite pulso redstone ao receber sinal por pedra sólida. | B22/B23; sem ciclos |
| B25 | Lâmpada Sintonizada | TEC | Mid/Late | B | Mostra pulso efetivo em circuito, não apenas brilho decorativo. | B24; segundo uso real |
| B26 | Leitor de Colônia | TEC/FLR | Mid/Late | B | Sinal analógico por estado de plantas vizinhas válidas. | 6.3; teste 0–4 |
| B27 | Lanterna Botânica | BLD/FLR | Mid/Late | C | Indicador de condição de jardim, se diferente de lanternas vanilla. | B13/B14; A/B |
| B28 | Placa de Sondagem | ARC | Mid | C | Suporte comparativo de amostras físicas, sem menu inútil. | I06; duas ações |
| B29 | Relevo Gravado | ARC/BLD | Mid/Late | B | Padrão obtido pela técnica de molde, aplicado em base. | I03; POI-10 |
| B30 | Mosaico de Camadas | BLD | Late | C | Acabamento para grandes obras, opcional, sem crystal gate. | B09/B29; estética |

**Agrupamento para evitar explosão técnica:** B01–B09 formam uma família arquitetônica; B10–B14 são flora; B15–B20 oferecem investigação/travessia; B21–B25 são uma **única experiência de circuito**; B26–B30 são especializações. Não registrar todos esses nomes como materiais de crafting separados. Bloco decorativo é legítimo pela estética se tiver paleta e obtenção interessantes — não exigir magia em todas as variantes.

---

# 3. CATÁLOGO DE ITENS, CONSUMÍVEIS E FERRAMENTAS — 18 ENTRADAS

| ID | Nome | Tipo | Estágio | Status | Ação que muda o jogo | Gate |
| --- | --- | --- | --- | --- | --- | --- |
| I01 | Argamassa de Marcas | Consumível | Mid | A | Cultivo + argila + cascalho geram composto para converter pedra comum 1:1. | B10/B01; custo 6.6 |
| I02 | Cinzel de Marcas | Ferramenta | Mid | B | Alterna acabamento de full block no mundo, gasta durabilidade válida. | B01/B02; A/B stonecutter |
| I03 | Molde de Relevo | Ferramenta de padrão | Mid | B | Registra desenho de painel observado e o reproduz na base. | B08/B29; POI-10 |
| I04 | Kit de Escoramento | Consumível | Mid | B | Reforça piso instável elegível, alternativa a construir ponte vanilla. | B17/B18; claims |
| I05 | Ponteiro de Fissuras | Ferramenta alternativa | Mid | C | Testa bloco tocado e lê risco local antes de atravessar. | B17; preferir pista ambiental |
| I06 | Sonda de Estratos | Instrumento | Mid | B | Compara propriedades de dois blocos tocados, sem GPS ou scan de minério. | B16/B21; 1 instrumento |
| I07 | Caderno de Campo | Registro opcional | Mid | C | Guarda descobertas efetivamente observadas, sem quest obrigatória. | I06; comparar advancements |
| I08 | Lente de Marcas | Instrumento concorrente | Mid | C | Lê relevo contextual com interação local; concorre com I06. | B08/POI-10; fundir se redundante |
| I09 | Fibra de Hera | Insumo orgânico | Mid | B | Colheita do cultivo para cordame e recuperação de passarela. | B11/I10; dois usos |
| I10 | Cordame de Hera | Ferramenta de travessia | Mid | B | Une apoios físicos próximos em rota curta; não é tirolesa infinita. | I09; comparar vines/scaffolding |
| I11 | Selo de Cultivo | Item de manejo | Mid | C | Identifica planta-matriz de jardim para evitar erro de colheita. | B10/B11; talvez UI vanilla |
| I12 | Pigmento de Penumbra | Acabamento | Mid | C | Cor de relevo/lanterna contextual; só se não equivaler a dyes vanilla. | B14/B29 |
| I13 | Carga de Luz Botânica | Consumível | Mid | C | Abastece indicador vegetal somente se recarga melhorar decisão. | B27; risco de grind |
| I14 | Fragmento Reverberante | Material técnico | Mid/Late | B | Origem B21, consumidores em rede lítica e diagnóstico local. | B21/M06; não Dream Dust |
| I15 | Diapasão de Eco | Ferramenta técnica | Mid/Late | B | Inspeciona a conexão física local de um circuito lítico. | B22/B24; sem GPS |
| I16 | Bobina Sintonizada | Componente | Mid/Late | C | Peça comum para emissor/receptor SE reduzir montagem real. | I14; evitar token intermediário |
| I17 | Matriz de Sintonização | Ferramenta alternativa | Mid/Late | C | Troca modo finito de receptor; descartar se clique no bloco basta. | B24; concorre I15 |
| I18 | Marcador de Campo | Orientação opcional | Mid | C | Marca rota já visitada; rejeitar se torch/sign/map bastarem. | POI-03/09; vanilla A/B |

## 3.1 Concorrência — escolher as melhores, não acumular scanners

| Alternativas | Comparação em Survival | Regra de seleção |
|---|---|---|
| I06 Sonda, I08 Lente e EQP-01/02/06 da Bible | Comparar dois locais vs observar um relevo com item | **No máximo um instrumento**; talvez nenhum |
| I04 Escora vs I05 Ponteiro | Preparar passagem vs só ler risco já anunciado | Priorizar ação com consequência real |
| I03 Molde vs I07 Caderno | Produzir acabamento vs guardar texto/advancement | Preferir Molde; registro pode ser advancement |
| I10 Cordame vs escadas/scaffolding/vines/leads vanilla | Travessia entre apoios com custo e alcance delimitado | Cancelar se solução vanilla for mais simples |
| I12 Pigmento vs dyes vanilla | Resultado visual percebido em builds | Fundir com dyes se equivalentes |
| I15 Diapasão vs I17 Matriz | Diagnóstico e troca de modo em um bloco | Fundir em uma ferramenta/interação |
| I18 Marcador vs tochas/placas/mapa/lodestone | Benefício após marcar a própria rota | Cancelar se for só mais uma tocha |

**Regra:** 18 linhas são opções de ideação. Uma única ferramenta de arqueologia que muda comportamento vale mais que 3 “detectores de raridade crescente”.

---

# 4. CATÁLOGO DE ESTRUTURAS — 12 EXPERIÊNCIAS DIFERENTES

| ID | Estrutura | Estágio | Local/contexto | O que o jogador faz e recebe | Ligação de sistemas | Status |
| --- | --- | --- | --- | --- | --- | --- |
| POI-01 | Marco de Pedra Marcada | Early | Afloramento | Reconhecer material por forma e recuperar amostra; sem baú obrigatório. | B01; fonte não exclusiva | 6.5 selecionado |
| POI-02 | Abrigo de Campo | Early/Mid | Encosta/caverna | Duas entradas, cobertura, bancada vanilla e suprimentos modestos. | B01; loot não obrigatório | 6.5 selecionado |
| POI-03 | Galeria Afundada | Mid | Subsolo | Escolher rota preparada ou água/desnível; entrar e sair com segurança. | B17/B19 opcionais | 6.5 condicionado |
| POI-04 | Câmara do Limiar | Mid/Late | Subsolo raro | Duas abordagens e eventual guardião, sem boss ou chave final. | Falta recompensa funcional | BLOQUEADO |
| POI-05 | Pedreira Abandonada | Early/Mid | Encosta rochosa | Interpretar extração antiga, reconstruir trajeto e recolher material arquitetônico. | B01/B16/B19 | NOVO; explorar |
| POI-06 | Jardim de Penumbra | Early/Mid | Caverna úmida | Reconhecer suporte vegetal, transplantar uma muda e preservar matriz. | B10/B11/B14; mariposa opcional | NOVO; explorar |
| POI-07 | Estação de Sondagem | Mid | Planalto rochoso | Comparar dois pontos de estrato e inferir a continuidade real de camadas. | B16/I06; sem GPS | NOVO; explorar |
| POI-08 | Passarela Rompida | Mid | Ravina curta | Reforçar, contornar ou construir travessia entre apoios fisicamente existentes. | B17/B19; anti-softlock | NOVO; explorar |
| POI-09 | Poço de Camadas | Mid | Poço subterrâneo | Descer nichos e níveis, gerenciar luz/saída, observar estratos diferentes. | B15/B16; saída real | NOVO; explorar |
| POI-10 | Arquivo de Relevos | Mid | Sala de rocha | Copiar um padrão útil para construção sem pegar chave obrigatória de baú. | B08/B29/I03 | NOVO; explorar |
| POI-11 | Observatório de Vigília | Mid/Late | Colina | Comparar a mesma flora/arquitetura em condições de luz distintas. | B13/B27; daylight A/B | NOVO; explorar |
| POI-12 | Casa de Ecos | Mid/Late | Subsolo técnico | Ver pulso atravessar caminho lítico e reproduzir mecanismo na base. | B21–B25; M06 | NOVO; dependente |

## 4.1 Cada escala de exploração tem uma função

- **Micro:** Marco e Passarela — sinal no terreno e pequena decisão, possivelmente como placed feature se o registro structure for exagerado.
- **Pequena/média ambiental:** Abrigo, Pedreira, Jardim, Estação — utilidade física, habitat e pistas.
- **Média com percurso:** Galeria, Poço e Arquivo — verticalidade, rota alternativa ou técnica reproduzível.
- **Especial opcional:** Observatório, Casa de Ecos e Câmara — dependem de uma **mecânica forte** além de loot.

**Diferença de design obrigatória:** Pedreira ensina recuperação de material; Jardim ensina habitat/cultivo; Passarela ensina reconstrução; Arquivo ensina molde; Casa de Ecos ensina circuito. Se dois lugares se tornarem o **mesmo corredor com baú**, fundir em variantes topológicas.

## 4.2 POI-04 continua bloqueado por recompensa

A Task 6.6 deixou a **Câmara do Limiar sem recompensa horizontal funcional aprovada**. Esta 6.6.1 não finge resolver o problema só por oferecer mais nomes. Marcar POI-04 como **BLOCKED_BY_REWARD_DESIGN** até existir capacidade real que compense sua raridade. Não criar boss key, cristal gratuito ou máquina apenas para encher baú.

## 4.3 Regras de worldgen

Integridade tem prioridade sobre quantidade: base apoiada, entrada visível e acessível, saída, espaço de circulação, água/lava contidas, rotas realmente diferentes e recompensa alcançável. Jigsaw/structure_set/loot seguem versão **26.3 e os contratos da Task 5.7**. /locate funcionar e baú existir **não provam** uma estrutura íntegra. Novos POIs somente em chunks novos de Overworld, sem retrogen padrão e sem sobrescrever ruínas do Dreamscape/Campo Azul.

---

# 5. CATÁLOGO DE CRIATURAS — 6 PAPÉIS

| ID | Criatura | Estágio | Comportamento/Habitat | Valor | Status |
| --- | --- | --- | --- | --- | --- |
| MOB-01 | Mariposa dos Vestígios | Early/Mid | Pousa em Líquen natural, foge de aproximação e revela condição de habitat. | Observação/jardim, não loot | 6.4; F1 primeiro |
| MOB-02 | Habitante das Fendas | Mid | Alerta territorial, perseguir pouco, recuar ou combater. | Escolha de rota, sem drop obrigatório | 6.4 condicionado |
| MOB-03 | Guardião de Pedra | Mid/Late | Protege área de estrutura; golpe anunciado e bypass possível. | Desafio opcional, não boss | 6.4; POI-04 |
| MOB-04 | Observador Esquecido | Indefinido | Figura narrativa hipotética; não criar NPC ou civilização sem canon. | Nenhum loop ainda | ADIADO |
| MOB-05 | Besouro das Camadas | Early/Mid | Vive em sedimento já existente; sua movimentação sinaliza onde observar. | Descoberta local, sem criar loot | NOVO; B15 |
| MOB-06 | Grilo de Eco | Mid | Canta apenas perto de nódulo físico em microhabitat, não procura tesouros longe. | Pista local verificável | NOVO; B21/M06 |

**MOB-04 permanece ADIADO**: não canonizar uma civilização ou NPC humanoide apenas porque o nome é interessante. MOB-05 Besouro e MOB-06 Grilo só existem como ideias ligadas a **sedimento real** ou **ressonância real**; não aprovar um novo mob simplesmente para atingir quantidade. Fauna deve permitir observação, fuga, habitat e escolha, **sem drop exclusivo de progressão**.

A futura implementação deve respeitar o cuidado NeoForge 26.3 com `EnvironmentAttributes#NATURAL_MOB_SPAWNS` e operações de composição que podem substituir spawns vanilla. Preferir adição não destrutiva, verificar categoria/biomas efetivos, caps, loaded chunks, dois jogadores e servidor dedicado. Reutilizar framework de MobHabitats/AI quando apropriado, sem copiar a regra de ilhas flutuantes para cavernas.

---

# 6. NOVE MECÂNICAS — MALHA ENTRE RECURSOS, POIs E JOGADORES

| ID | Mecânica | Estágio | Loop | Recompensa de capacidade | Dependência |
| --- | --- | --- | --- | --- | --- |
| M01 | Estratigrafia observável | Early/Mid | Reconhecer camadas e recuperar material/padrão em formação real. | Geologia legível e construção | B15/B16 + POI-05/09 |
| M02 | Arqueologia de relevos | Mid | Brush vanilla em sedimento → padrão copiável → bloco decorativo aplicado em base. | Recompensa que não é loot key | B15/B08/I03 + POI-10 |
| M03 | Restauro de travessias | Mid | Piso avisa instabilidade → contornar/construir/escorar → passagem recuperada. | Escolha de risco e resultado permanente | B17–B20/I04 + POI-08 |
| M04 | Jardim responsivo | Early/Late | Colher planta → replantar → observar luz/suporte → cultivar/iluminar/medir. | Cultivo com aplicação em construção | B10–B14/B26 |
| M05 | Restauração in situ | Mid/Late | Pedra + Líquen → Argamassa/Cinzel → produzir/acabar construção na posição. | Obra em escala com custo real | B01/B02/I01/I02 |
| M06 | Ressonância mineral local | Mid/Late | Nódulo visível → extrair fragmento → montar emissor/caminho/receptor → pulso. | Sinal local por parede, sem rede global | B21–B25/I14/I15; A/B |
| M07 | Fauna como pista | Early/Late | Ver/escutar criatura → reconhecer habitat verdadeiro → observar/recuar/contornar. | Exploração por comportamento | MOB-01/02/05/06 |
| M08 | POIs de exploração não linear | Early/Late | Notar lugar → escolher abordagem → obter algo utilizável fora dele. | Diversidade sem chave obrigatória | POI-01–12; QA terreno |
| M09 | Preparação onírica | Mid tardio/Late | Mineração/infra atual → Colar/Despertador → expedição → retorno consumível. | Consistência sem viagem gratuita | R0 preservado; 6.8 mede |

## 6.1 Matriz de relações horizontais

| Loop | Geologia | Flora | Ferramenta | Fauna | POIs | Reutilização na base |
|---|---|---|---|---|---|---|
| **M01 Estratigrafia** | B15/B16 | — | I06 opcional | Besouro opcional | 05/09 | Colunas/relevo |
| **M02 Arqueologia** | Sedimento | — | Brush + Molde I03 | Besouro | 05/10 | Painel gravado |
| **M03 Travessia** | Rocha cediça | Hera opcional | Escora I04 | Habitante opcional | 03/08 | Passagem restaurada |
| **M04 Jardim** | Substrato | B10–14 | Argamassa/colheita | Mariposa | 06/11 | Cultivo/sensor/luz |
| **M05 Restauro** | B01/B02 | F1 | Argamassa/Cinzel | — | 02/05 | Obra em escala |
| **M06 Ressonância** | B21 condicional | — | Fragmento/Diapasão | Grilo condicional | 12 | Circuito oculto |
| **M07 Fauna como pista** | Habitat real | Habitat real | Nenhuma obrigatória | Mobs com IA | Variados | Jardim/território |
| **M08 Exploração POI** | Terreno | Habitat | Vanilla + opcionais | Encontro opcional | 01–12 | Materiais/técnicas |
| **M09 Sonhos** | Dream Ore atual | Independente | Colar/Despertador | Mobs atuais das dimensões | Ruínas dimensionais | Preparação recorrente |

Cada ramo deve funcionar **sem completar todos os outros**, porém uma boa linha se conecta organicamente a pelo menos duas atividades: por exemplo, sedimentação conecta arqueologia + arquitetura + POI; travessia conecta geologia + restauro + exploração; ressonância conecta mineral + circuito + estrutura.

---

# 7. TRÊS EXPERIÊNCIAS ÂNCORA PARA PROVAR QUE A EXPANSÃO VALE A PENA

## 7.1 Âncora A — Escavação que vira construção

**Cena Survival:** jogador identifica uma pedreira antiga com estratos legíveis; encontra Sedimento de Marcas; usa brush vanilla para acessar um **relevo verificável**; com Molde I03 copia o padrão. Em casa aplica o desenho em Painel/Relevo Gravado. A recompensa é **uma técnica de construção** e material, não chave RNG.

**Fatia mínima:** B15/B16 + 1 POI de escala pequena + 1 padrão reproduzível.  
**Extensão condicionada:** I03/B29/POI-10 e Besouro se ajudarem de verdade.  
**Prova de valor:** sem Wiki, um jogador consegue usar o resultado em uma construção.  
**Cancelar se:** apenas mais loot de livro/papel, ou brush vanilla já oferece a mesma experiência sem adição.

## 7.2 Âncora B — A ponte que precisava ser reconstruída

**Cena Survival:** avista Passarela Rompida entre dois apoios; a rachadura B17 avisa que o piso é instável. Pode contornar com escada/blocos, tentar atravessar com preparo ou usar Kit I04 para escorar trecho. A passagem restaurada permanece no mundo.

**Fatia mínima:** POI-08 + piso avisado + pelo menos 2 estratégias legítimas.  
**Extensões condicionadas:** B18/B19, Kit e Cordame/Hera se úteis fora do POI.  
**Prova:** retorno à travessia e uso permanente justificam a descoberta sem baú.  
**Cancelar se:** armadilha invisível, queda inevitável, ponte sem apoios ou kit que só duplica scaffolding.

## 7.3 Âncora C — Tecnologia que existe na pedra

**Cena Survival:** o jogador vê B21 responder fisicamente a um pulso. Encontra Casa de Ecos com emissor e receptor conectados por blocos sólidos; aprende que um pulso pode atravessar parede sem redstone exposta; reproduz porta discreta em sua base. Diapasão ajuda diagnóstico do circuito local.

**Fatia mínima:** circuito com blocos **provisórios**, sem worldgen de minério novo.  
**Só depois:** B21/I14 e POI-12/MOB-06, caso circuito sobreviva A/B vs vanilla.  
**Prova:** outro jogador entende input/output, constrói circuito e reinicia mundo sem lag/desync.  
**Cancelar se:** serviço é só redstone cara, exige scan global ou tem updates sem limite.

---

# 8. GRAFO DE DEPENDÊNCIAS (NÃO É CAMPANHA LINEAR)

```text
[MINECRAFT VANILLA]
    +--> F0 Pedra Marcada ---> stonecutter ---> construção
    |          +--> Marco / Pedreira
    +--> F1 Líquen ---> cultivo ---> Argamassa ---> obra em escala
    |          +--> Mariposa / Jardim (não obrigatórios para F1)
    +--> B15 Sedimento + brush ---> padrão ---> painel em base (M02)
    |          +--> Arquivo / Besouro condicionais
    +--> B17 Rocha Cediça ---> escolha: contorno / bloco vanilla / Escora (M03)
    |          +--> Passarela / Galeria / Hera opcional
    +--> Protótipo de ressonância SEM minério (M06)
    |          +--> Emissor + Elo + Receptor + Feedback
    |          +--> SE aprovar: Nódulo + Fragmento + Casa de Ecos
    |          +--> SE aprovar: Grilo contextual
    +--> Dream Ore existente ---> Dust ---> Colar + Núcleo/Despertador
                                                   |
                                      viagens Mid tardio/Late
    +--> Câmara do Limiar: BLOQUEADA por recompensa
```

**Dependência de produção:** F1 precisa funcionar antes de Argamassa; M06 precisa provar efeito antes de Nódulo; padrões precisam ter aplicação antes de Arquivo; Galeria precisa ter entradas e rotas antes de mob guardião.

**Dependência apenas temática:** Marco pode ser visitado antes/depois de Abrigo; fauna não é ingrediente obrigatório de planta; nenhuma estrutura do Overworld é chave exclusiva do Colar/primeiro Despertador.

---

# 9. POLÍTICA DE SELEÇÃO E DESCARTE

## 9.1 Régua de valor (aplicar no protótipo, não inventar pontuação agora)

Para cada hipótese, medir em escala 0–3:
1. **Ação nova** em relação ao vanilla.
2. **Recompensa que continua útil** no Late.
3. **Conexão não artificial** com dois sistemas.
4. **Compreensão em Survival** sem Wiki/F3.
5. **Custo e segurança técnica** aceitáveis.

**Veto P0, independentemente de score:** duplicação de itens, corrupção de mundo, quebra de claims, violência inevitável, softlock, invasão de bases, chunk forceload, supressão de spawns vanilla ou regressão dos sonhos existentes.

## 9.2 O que precisa competir pelo mesmo espaço

| Família | Candidatos concorrentes | Preferência até teste |
|---|---|---|
| Leitura geológica | Sonda, Lente, Ponteiro e observação pura | **Um instrumento no máximo**, ou nenhum |
| Travessia | Escora, Cordame, scaffold/ladder vanilla | Melhor opção de duas rotas com custo justo |
| Arquitetura | Painel, Relevo, Mosaico e stonecutting | Melhor paleta/construção, não 20 recipes |
| Botânica | Flor, Fungo, Raiz, Lanterna e sensor | Estados com usos reais; fundir redundâncias |
| Ressonância | Delay original 6.2 vs transmissão local nova | **Transmitir por material**, se superar vanilla |
| POIs | Pedreira vs Marco; Sondagem vs Arquivo; Observatório vs Jardim | Fundir se ação/percursos são iguais |
| Fauna | Besouro/Grilo/Mariposa | Preservar somente pistas de habitat verificáveis |

---

# 10. FATIAS VERTICAIS RECOMENDADAS PARA 6.9

| Fase conceitual | Entrega demonstrável | Gate de aceitação |
|---|---|---|
| **V0 A primeira marca** | F0 natural → mineração → stonecutter → construção | Encontro sem cheats e utilidade real |
| **V1 Jardim autossuficiente** | F1 natural → transplante → duas colheitas | Saldo positivo e habitat distinto |
| **V2 Restauro que escala** | Argamassa e transformação de 64 blocos | 1:1, claims, multiplayer, economia |
| **V3 Arqueologia útil** | Sedimento + brush → padrão usado em base | Não depende de item raro |
| **V4 Travessia com escolha** | Uma Passarela → contornar/escorar | Apoios, risco anunciado, saída |
| **V5 Fauna que ensina** | Mariposa com pouso/voo e spawn natural | F1 válido, sem fauna vanilla perdida |
| **V6 Circuito de Eco** | Emissor → caminho limitado → receptor/lâmpada | A/B vs redstone/sculk, sem scans |
| **V7 Casa de Ecos** | Estrutura demonstra V6 e permite reproduzir | Só depois de V6 aprovada |
| **V8 Câmara opcional** | Desafio com recompensa horizontal genuína | BLOCKED_BY_REWARD_DESIGN até solução |

**As fatias são ordem de implementação e evidência, não quests obrigatórias no save.** Não implementar de uma vez 30 blocos, 18 itens e 12 estruturas.

---

# 11. CHECKLIST TÉCNICO E DE VALIDAÇÃO

| Categoria | Provas que futuras PRs devem fornecer |
|---|---|
| Geologia | Fonte, drops, tool tier, múltiplas seeds, distribuição em novos chunks |
| Arquitetura | Blockstates, models, tags, loot, variantes sem dupe de massa/volume |
| Flora | Luz/suporte, crescimento, colheita positiva, chunks, dois jogadores |
| Arqueologia | Brush realmente funciona, recompensa aplicável, persistência e RNG não essencial |
| Restauro | Dois caminhos, sem grief, sem queda obrigatória, escora consome após mutação confirmada |
| Ressonância | Camino local limitado, sem polling global, atualização finita, unload/reload sem loop |
| Itens | Receitas possíveis, custo/retorno/shift-click, proteção/Creative/Adventure |
| Mobs | IA/anim, habitat, spawn aditivo NeoForge 26.3, caps, multiplayer |
| Estruturas | Suporte, entrada/saída, caminho, rota alternativa, loot persistente, desempenho |
| Sonhos | R0 inalterado, retorno/respawn e ruínas dimensionais sem regressão |
| Qualidade | `en_us`/`pt_br`, dedicated server, CI, saves antigos, Survival sem cheats |

**Medições futuras para 6.8:** tempo real de descoberta, chance de jogador reconhecer pista, custo por bloco de Argamassa, uso real dos padrões, incidência de passarelas íntegra, largura de banda/latência local do circuito, valor A/B vs vanilla, uso de POI sem baú, impacto de entidades e worldgen no TPS.

Seeds históricas de regressão: `14011`, `20261108`, `-904732`, `73199013`; **nenhuma nova seed/posição foi medida nesta task de documentação**.

## 11.1 Comandos de teste

Nenhum código B01–B30/I01–I18/POI-05–12/MOB-05–06 é um **ID registrado**; portanto não publicar `/give`, `/summon`, `/locate` ou `/tp` inventados para candidatos.

Comandos de laboratório já válidos, sem implicar spawn/obtenção natural:

```mcfunction
/seed
/gamemode spectator
/gamemode survival
/give @s dreamsdimensions:dream_dust 15
/give @s dreamsdimensions:oneiric_awakener
```

Cada task de implementação real deve entregar comandos `/give`, `/setblock`, `/summon`, `/locate structure` somente para registries **verificados** e `/tp` com coordenadas **medidas na seed testada**, além de testes naturais sem comandos.

---

# 12. GOVERNANÇA E COMPATIBILIDADE COM 6.0–6.6

- **6.0/6.1**: seis pilares, liberdade de escolhas e Early→Mid→Late continuam.
- **6.2**: F0/F1 seguem selecionados; Hera/Nódulo/Cristal eram condicionados/adiados e **continuam não aprovados** mesmo sendo explorados aqui.
- **6.3**: Argamassa selecionada para especificação; Cinzel/Leitor condicionados; scanners e bancadas não são automaticamente autorizados.
- **6.4**: Mariposa priorizada, Habitante e Guardião condicionados, Observador adiado. Novos Besouro/Grilo são hipóteses, não mobs registrados.
- **6.5**: Marco/Abrigo selecionados; Galeria/Câmara condicionadas. As oito novas estruturas precisam de level design e worldgen próprios.
- **6.6**: R0 de Colar/Despertador preservado; receita de Argamassa 2 Líquens + 1 argila + 1 cascalho → 4 e Cinzel 128 usos permanecem **hipóteses**, não números homologados.
- **6.7**: selecionar assets só após fechar loops/protótipos; não texturizar ideias C por obrigação de catálogo.
- **6.8**: medir receita, raridade e descobribilidade; não converter porcentagem hipotética em fato.
- **6.9**: produzir roadmap executivo em slices com CI/QA e gates.

Para promover um novo candidato B/C a “selecionado”, registrar **loop, teste A/B, consumidor, aquisição, custo, relações de dependência e riscos**. Se isso mudar uma decisão antiga, escrever adendo explícito, não editar silenciosamente o contrato anterior.

---

# 13. CRITÉRIOS DE ACEITE DA TASK 6.6.1

## Documento — CONCLUÍDO

- [x] Revisou os contratos de 6.0 a 6.6 e identificou excesso de redução de escopo.
- [x] Produziu catálogo rastreável de **30 blocos**, **18 itens**, **12 estruturas**, **6 mobs** e **9 mecânicas**.
- [x] Deu a cada proposta estágio, ação, vínculo e condição de aprovação.
- [x] Detalhou três experiências âncora (arqueologia útil, travessia restaurável, circuito mineral).
- [x] Criou matriz entre sistemas e grafo de dependências não linear.
- [x] Reabriu novas hipóteses sem declarar minérios/estações antes adiados como aprovados.
- [x] Preservou quarto ramo de viagens oníricas e a consistência de Mid tardio/Late.
- [x] Diferenciou recompensas horizontais e decorativas de itens obrigatórios.
- [x] Definiu critérios eliminatórios de segurança, valor e custo.
- [x] Forneceu sequência de protótipos/slices, QA e comandos sem inventar IDs.
- [x] Nenhum arquivo funcional, receita, NBT, worldgen ou registry foi alterado.

## Conteúdo jogável — PENDENTE

- [ ] Provar cada linha em protótipos antes de selecionar seus assets.
- [ ] Validar F0/F1 em Survival real e testar Argamassa.
- [ ] Testar arqueologia, restauro e ressonância contra vanilla.
- [ ] Homologar fontes, ações, drops e rendimentos sem dupe/softlock.
- [ ] Medir faunas, POIs, progressão e desempenho em 6.8.
- [ ] Publicar alterações funcionais em PRs separadas, com dedicated server e CI.

**Estado exato:** esta Task conclui o **planejamento criativo estruturado**, não a implementação dos candidatos.

---

# 14. DIREÇÃO PARA A PRÓXIMA ETAPA

**Não avançar para a 6.7 tratando este catálogo como uma lista de 30 texturas a produzir.** Antes, selecionar quais loops realmente devem receber arte com base em protótipos/contratos. Começar por F0/F1/Argamassa já escolhidos e avaliar **três experiências novas**: arqueologia de padrões, travessia reconstruível e ressonância curta por pedra. Se uma delas falhar em utilidade, simplificar ou descartar; se funcionar, ela sustenta muitas estruturas, itens, blocos e criaturas **com sentido e reaproveitamento**.

**Critério central:** queremos um Overworld que o jogador explore porque há o que **entender, construir, cultivar, reparar e descobrir**, não porque o jogo o obriga a fabricar uma chave para o próximo mundo.

**Próxima Task:** **6.7 — Direção Visual, Texturas, Modelos e Assets**, condicionada à seleção de conteúdo desta 6.6.1.

---

*Fim — Task 6.6.1, catálogo expandido. Nenhum conteúdo novo foi registrado no Minecraft.*
