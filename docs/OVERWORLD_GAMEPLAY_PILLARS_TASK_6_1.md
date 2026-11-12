# TASK 6.1 — PILARES DE GAMEPLAY DO OVERWORLD
## Contrato de progressão Early / Mid / Late, loops independentes e conexões entre sistemas

**Projeto:** DreamsDimensions  
**Documento:** Game Design / Systems Design — especificação de planejamento (sem implementação de conteúdo)  
**Versão:** 1.0 — proposta executável de direção  
**Data:** 11 de novembro de 2026  
**Branch:** `migration/minecraft-26.3`  
**Baseline auditada:** `f1b43df48f9bf04d6075673515b06ae5414a240a`  
**Documento-pai:** [Task 6.0 — Game Design Bible](GAME_DESIGN_BIBLE_TASK_6_0.md)  
**Próxima tarefa:** 6.2 — Recursos, Geologia, Flora e Blocos  
**Escopo deste commit:** somente documentação; sem registro de conteúdo, sem receitas novas, sem modificação de mundo, sem merge em `main`.

> **Contrato central:** DreamsDimensions amplia o Minecraft no Overworld por meio de seis experiências complementares: geologia/mineração, natureza/cultivo, fauna, exploração/estruturas, crafting/processamento e construção/mecanismos. Cada uma precisa oferecer valor próprio em Survival, conectar-se a pelo menos dois outros pilares e manter utilidade no Mid/Late Game. Os sonhos são uma consequência possível dessa jornada — não seu único objetivo nem um End Game próprio.

---

# 0. COMO LER E O QUE ESTA TASK ENTREGA

## 0.1 Estado das decisões

| Status | Significado nesta especificação |
|---|---|
| **DIRETRIZ FIXADA** | Regra de produto extraída da Task 6.0 ou da orientação vigente; deve guiar as tasks seguintes. |
| **CONTRATO 6.1** | Comportamento-alvo de gameplay e dependência que este documento passa a especificar; não significa código implementado. |
| **EXISTENTE / AUDITADO** | Elemento verificado em código/recursos da branch. |
| **PROPOSTO / CANDIDATO** | Nome, recurso, mob, estrutura, mecânica ou abordagem a ser avaliada e aprovada em 6.2–6.9. |
| **HIPÓTESE PARA PLAYTEST** | Distribuição, tempo, risco ou UX que ainda precisa de medição com jogadores. |
| **ADIADO** | Não implementar nesta fase sem nova decisão de direção. |

**Precedência:** código e contratos já implementados > diretrizes fixadas da Bible > contratos propostos aqui > candidatos de catálogo. Uma divergência entre progressão desejada e código atual vira uma tarefa de migração, nunca uma alteração silenciosa.

## 0.2 Entregáveis

1. Definir a função jogável de cada um dos **seis pilares**, com um loop completo independente das dimensões.
2. Desenhar **Early → Mid → Late Game** por capacidade, eficiência e expressão, sem criar um End Game.
3. Explicitar pelo menos duas conexões significativas de cada pilar com outros pilares.
4. Separar **acesso obrigatório**, escolhas opcionais, conveniências e recompensas horizontais.
5. Criar um grafo conceitual de desbloqueios sem ciclo impossível, grind artificial ou dependência de uma seed/estrutura única.
6. Especificar **experiências de sessão** para explorador, construtor, naturalista e jogador orientado a mecanismos.
7. Priorizar uma primeira fatia vertical **F0 — O Primeiro Vestígio**, pronta para detalhamento em 6.2–6.9.
8. Definir métricas, testes de aceitação, riscos de softlock e decisões pendentes.
9. **Não** canonizar a origem dos vestígios, definir odds finais, adicionar registros ou alterar receitas existentes nesta task.

## 0.3 Definição de pronto documental

O documento estará concluído se um designer conseguir descrever, para cada loop:

`gatilho observável → escolha do jogador → ação em Survival → resultado material/informacional → aplicação útil → motivo para repetir ou explorar outro lugar`.

Um recurso que só existe como ingrediente em uma receita distante **não é um loop**. Uma ruína que só entrega um baú genérico **não é um sistema de exploração**. Um mob cuja única utilidade é morrer e dropar um componente **não fecha um pilar ecológico**.

---

# 1. ESTADO REAL DO PROJETO E LIMITES DE CONTINUIDADE

## 1.1 O que já existe

**EXISTENTE / AUDITADO**, conforme registros e documentos consultados:

- `dreamsdimensions:dream_ore` e `dreamsdimensions:deepslate_dream_ore`: minérios do **Overworld**; fonte de `dreamsdimensions:dream_dust`. A Task 5.6 mediu geração natural em quatro seeds; a disponibilidade observada não prova tempo de aquisição em Survival.
- `dreamsdimensions:dream_dust`, `dreamsdimensions:dream_infused_stone`, `dreamsdimensions:oneiric_core_block`, `dreamsdimensions:dream_necklace` e `dreamsdimensions:oneiric_awakener`: cadeia atualmente registrada. O Colar usa equipamento Curios e elegibilidade de sono; o Despertador é **consumido após retorno efetivamente confirmado**.
- `dreamsdimensions:fragmento_errante` e `dreamsdimensions:vigia_adormecido`: entidades associadas ao **Dreamscape**; **não** constituem fauna do Overworld.
- `dreamsdimensions:blue_dream_ruins` e `dreamsdimensions:fragmented_dream_ruins`: estruturas existentes **dentro das dimensões**; não são POIs novos do Overworld. Seus baús têm chance configurada de 25% de um Despertador, sem Pó Onírico.
- Campo Onírico Azul e Dreamscape: dimensões com geografia, formações, vegetação e experiências distintas; **não** são objeto de expansão na 6.1.
- Sistemas de mundo, entidades, efeitos, motor de sono, teleporte, morte, Curios e retorno já possuem contratos e devem ser preservados.
- O fluxo de entrada no sonho é protegido por configuração (valor padrão `dream_entry_enabled=false` na baseline). Relatos de homologação manual não autorizam mudar esse padrão por iniciativa desta task.

**Lacunas atuais do Overworld:** não há entidade nova registrada para viver naturalmente no Overworld, nem estruturas dedicadas registradas para ele no baseline revisado; não existe um ciclo novo e completo de natureza/cultivo, mecanismo reativo ou arquitetura funcional própria do Overworld.

**Importante:** algumas tabelas históricas da Task 5.0 e do arquivo `PROGRESSION_OVERWORLD_FIRST.md` antecedem a regra posterior de Despertador consumível. Prevalecem as Tasks 5.2–5.7 e os registros atuais; não recuperar ideias obsoletas de retorno gratuito, minério dimensional de Pó ou Despertador reutilizável.

## 1.2 O que esta Task NÃO decide

- Não criar IDs, Java, NBT, sprites, modelos, receitas, loot tables, biomas, mobs ou estruturas.
- Não alterar distribuição de Dream Ore, chance de sono, regras do Colar, consumo do Despertador, respawn ou loot das ruínas.
- Não anunciar que candidatos GEO/FLR/MOB/POI/STA da Bible estão aprovados.
- Não transformar o Overworld em um bioma mágico global nem duplicar recursos exclusivos de outras dimensões.
- Não inventar história fechada sobre “Observadores” nem criar missão principal.

---

# 2. CONTRATO GLOBAL DA EXPERIÊNCIA

## 2.1 A promessa em 15 minutos, em 3 horas e após muito progresso

Os tempos abaixo são **cenas hipotéticas de sessão**, não metas ou medidas de desempenho.

**Primeiros contatos:** um jogador que conhece Minecraft percebe sinais ambientais, investiga uma ocorrência e obtém alguma utilidade imediata para mineração, construção, cultivo, observação ou exploração. Ele **não** precisa conhecer a história nem ter feito uma viagem.

**Exploração intermediária:** o jogador passa de descobrir por acaso a **procurar deliberadamente** certos locais ou materiais. Diferencia habitats e rochas, prepara uma expedição e constrói ferramentas/estações apenas quando entregam ações novas.

**Mundo estabelecido:** o jogador tem base, projetos e métodos reproduzíveis. Consegue sustentar algumas atividades com produção renovável, mas continua explorando para materiais finitos, estruturas, variantes estéticas, perigos e descobertas. Sonhos podem se tornar viagens regulares a partir de Mid tardio/Late, mediante custo e preparo.

## 2.2 Três eixos de progressão

| Eixo | Pergunta de design | Early | Mid | Late |
|---|---|---|---|---|
| **Acesso / capacidade** | “O que eu consigo fazer agora?” | Reconhecer/coletar vestígios e testar usos simples. | Interpretar pistas, processar material, abordar POIs e ferramentas especializadas. | Planejar expedições avançadas, usar mecanismos e materiais especializados. |
| **Eficiência / autonomia** | “Quanto trabalho preciso repetir?” | Coleta manual, pequenas quantidades. | Ferramentas apropriadas e produção inicial. | Reabastecimento sustentável de insumos de manutenção, sem acesso infinito a recursos raros. |
| **Expressão / horizontalidade** | “O que posso criar do meu jeito?” | Blocos iniciais, jardim, primeiras composições. | Famílias construtivas, cultivo, redstone temática. | Grandes bases, experimentos, paisagismo, engenharia e rotas personalizadas. |

**CONTRATO 6.1:** uma evolução só conta como progressão se melhorar pelo menos um desses eixos de modo que o jogador perceba e utilize. Aumentar 12 unidades para 48 sem nova capacidade é só inflação de custo.

## 2.3 As janelas são referenciais, não cadeados universais

- **Early:** madeira, pedra, ferro, comida/abrigo e exploração inicial vanilla. Vestígios devem gerar curiosidade e oferecer usos acessíveis.
- **Mid inicial:** equipamentos melhores, exploração mais ampla, redstone, encantamentos ou diamantes em desenvolvimento. Novos processos, estruturas e especialização.
- **Mid tardio:** preparação de expedições; primeiras travessias oníricas intencionais e arriscadas, **quando** o fluxo existente estiver habilitado/seguro.
- **Late:** estoque, infraestrutura, mobilidade e conhecimento. Expedições oníricas regulares, não gratuitas; conteúdo de Overworld continua gerando projetos de construção, cultivo, exploração e engenharia.

Não criar requisito artificial do tipo “jogador deve ter jogado X horas” ou “derrotar boss próprio para colocar um bloco decorativo”.

## 2.4 Liberdade e compromisso

**Obrigatório para usar uma mecânica específica:** possuir a ferramenta, o insumo ou a condição que faz parte **daquela ação**, com obtenção natural comprovada.

**Nunca obrigatório para toda a experiência do mod:** completar agricultura para usar pedra decorativa, combater um guardião para plantar um jardim, entrar em sonhos para fabricar qualquer mecanismo básico do Overworld.

**Opcional de alto valor:** explorar uma estrutura para obter uma variante, técnica alternativa, eficiência ou recurso especializado; ainda assim deve haver caminhos não arbitrários para os fundamentos essenciais.

**Especialização válida:** um jogador pode seguir geologia + construção, natureza + fauna, exploração + combate ou mecanismos + crafting e completar loops próprios sem viagem dimensional.

---

# 3. SEIS PILARES JOGÁVEIS — CONTRATOS INDEPENDENTES

Os seis pilares abaixo **não** devem virar seis árvores de pesquisa obrigatórias. São famílias de atividades e recompensas que se reforçam. Os nomes de candidatos pertencem à Bible, seção 6, e **não são registros autorizados**.

## 3.1 P1 — GEOLOGIA, RELEVO E MINERAÇÃO

**Fantasia:** “Aprendi a ler o terreno; consigo encontrar e aproveitar um recurso que não perceberia antes.”

**Loop independente G1 (Early → Mid):**  
`ver indício geológico → investigar rocha/caverna → decidir onde extrair → minerar com ferramenta correta → obter recurso → transformar pela via vanilla → construir algo útil → buscar outro depósito`.

**Recompensa imediata:** um material com uso concreto (preferência F0: acabamento arquitetônico com identidade, não uma moeda extra).

**Recompensa duradoura:** aplicações adicionais para mecanismos/arquitetura e mineração deliberada em vez de ocorrência puramente casual.

**Conexões exigidas:**
- **G → Construção (B):** a rocha pode originar família de blocos e acabamento com abundância adequada.
- **G → Crafting (C):** o recurso serve em transformação ou dispositivo com função real.
- **G → Exploração (E), opcional:** formações podem fornecer indícios de POIs ou de depósitos, sem revelar coordenada.

**Candidatos comparáveis:** GEO-01 Pedra Marcada; GEO-02 Nódulo de Reverberação; GEO-03 Fragmento Reverberante. Comparar também variante de pedra vanilla + loot/processamento em vez de mais um minério inteiro.

**Anti-exemplos:** drop que só substitui `dream_dust`; minério com textura nova e nenhum uso além de craft futuro; 15 ocorrências por chunk que apagam a geologia vanilla.

**Teste de valor:** um iniciante consegue usar o recurso **antes** de adquirir Colar; outro jogador consegue identificar um tipo de ocorrência a partir de pistas aprendidas; o material ainda serve a projeto Mid/Late.

## 3.2 P2 — NATUREZA, AGRICULTURA E RECURSOS ORGÂNICOS

**Fantasia:** “Compreendi como esse organismo vive; posso levá-lo para minha base e construir um ecossistema útil.”

**Loop independente N1 (Early → Late):**  
`identificar habitat → descobrir planta/organismo → observar condição de sobrevivência → coletar sem extinguir ocorrência → reproduzir em espaço apropriado → colher recurso renovável → usar em paisagismo ou preparo → manter/expandir cultivo`.

**Recompensa imediata:** ornamentação/ingrediente simples que não depende de dimensões.

**Recompensa duradoura:** produção sustentável para decoração, alimentação situacional ou reagentes de manutenção **não equivalentes** a materiais raros de desbloqueio.

**Conexões exigidas:**
- **N → Fauna (F):** flora participa de habitat, comportamento observável ou alimentação/atração.
- **N → Crafting (C):** colheita alimenta pelo menos uma transformação real ou produto.
- **N → Construção (B), opcional:** jardim/folhagem fornece material arquitetônico e ambientes personalizados.

**Candidatos:** FLR-01 Líquen dos Vestígios, FLR-02 Hera do Repouso, FLR-03 Flor de Vigília; começar escolhendo **uma** linha, não três cultivos completos.

**Anti-exemplos:** planta que cresce em qualquer superfície como trigo renomeado; buff permanente superior à comida vanilla; farm AFK que substitui exploração de cristal profundo.

**Teste de valor:** jogador descobre as condições pelo mundo/UI, replica sem wiki externa e colhe novamente; no Late a produção continua útil e não substitui todos os recursos não renováveis.

## 3.3 P3 — FAUNA, COMPORTAMENTO E ECOLOGIA

**Fantasia:** “O ser vivo se comporta de um jeito que me ensina algo sobre este lugar; observar ou interagir pode ser melhor que atacar.”

**Loop independente F1 (Early → Mid):**  
`encontrar habitat → notar comportamento da criatura → observar padrão/horário → adaptar aproximação → receber informação/benefício ambiental → reencontrar em outro habitat ou integrar à base`.

**Recompensa imediata:** orientação, interação ecológica ou evento observável sem loot exclusivo por morte.

**Recompensa duradoura:** melhora de identificação de habitats, possibilidade de reprodução/interação ou recurso renovável moderado quando o candidato justificar.

**Conexões exigidas:**
- **F → Natureza (N):** uso real de plantas no comportamento e manutenção do habitat.
- **F → Exploração (E):** comportamento pode ajudar a identificar ocorrências ou alertar para locais perigosos.
- **F → Construção (B), opcional:** espaços de cuidado/ambientação compatíveis com bases, se a IA suportar.

**Candidatos:** MOB-01 Mariposa dos Vestígios como primeiro protótipo; MOB-02 Habitante das Fendas e MOB-03 Guardião de Pedra só após contratos de habitat/desafio. **Não há mob de Overworld registrado neste momento.**

**Anti-exemplos:** criatura presente apenas para dropar ingrediente obrigatório; spawn constante dentro de bases; AI que procura blocos em chunks não carregados; animação bonita escondendo falta de interação.

**Teste de valor:** sem matar o mob, o jogador percebe ao menos um comportamento com consequência prática; dois habitats e dois jogadores não causam spam, duplicação ou perda de controle populacional.

## 3.4 P4 — ESTRUTURAS, POIs E INVESTIGAÇÃO

**Fantasia:** “Há uma história e um motivo arquitetônico neste local; explorar muda minhas opções, não apenas meu inventário.”

**Loop independente E1 (Early → Mid):**  
`notar silhueta/pista na paisagem → escolher aproximar-se → reconhecer propósito do lugar → explorar um percurso ou interação → obter conhecimento material/técnica/variante → usar a descoberta na base → procurar outra tipologia`.

**Recompensa imediata:** informação e material de valor proporcional à escala; um Marco pode funcionar **sem baú**.

**Recompensa duradoura:** novas abordagens, variantes de construção, ferramentas situacionais e descobertas que influenciam outras atividades.

**Conexões exigidas:**
- **E → Geologia (G):** estruturas registram, expõem ou apontam critérios geológicos observáveis.
- **E → Crafting (C):** permitem experimentar uma técnica ou obter variante/processo **não obrigatório** para todo o mod.
- **E → Fauna (F), opcional:** habitats ou defesas associados ao contexto do local.

**Candidatos:** POI-01 Marco de Pedra Marcada, POI-02 Abrigo do Observador, POI-03 Galeria Afundada, POI-04 Câmara de Contenção (nomes provisórios).

**Anti-exemplos:** mesmo pátio NBT com um baú recolorido; item de progressão essencial em estrutura de localização praticamente impossível; geração superficial que corta montanhas/casas de jogadores; baú que renova loot ao reabrir.

**Teste de valor:** jogador entende como entrar e por que explorar; encontra ao menos uma escolha, caminho ou interação; demonstra utilidade da descoberta fora da estrutura; posição íntegra em mundo novo.

## 3.5 P5 — CRAFTING, PROCESSAMENTO E FERRAMENTAS

**Fantasia:** “Uma combinação de materiais permite fazer algo que nenhuma ferramenta que eu possuía fazia, e eu consigo repetir o processo conforme avanço.”

**Loop independente C1 (Early → Late):**  
`obter insumos com fontes claras → escolher receita/transformação → produzir item ou bloco → utilizar ação nova/variação construtiva → identificar benefício → melhorar repetibilidade e infraestrutura`.

**Recompensa imediata:** utilidade observável, não apenas um componente intermediário.

**Recompensa duradoura:** ferramenta situacional que continua válida, produção renovável de insumos de manutenção ou estação com múltiplas funções justificadas.

**Conexões exigidas:**
- **C → Geologia (G):** aproveita material extraído e pode melhorar a investigação ou transformação.
- **C → Construção (B):** produz blocos, mecanismos, iluminação ou variantes úteis.
- **C → Natureza (N), opcional:** transforma recursos agrícolas em insumos complementares.

**Candidatos:** EQP-01 Sonda de Vestígios, EQP-02 Lente de Observação, EQP-06 Instrumento de Medição são **alternativas**, não três itens cumulativos. STA-01 Mesa de Estabilização é hipótese para mais tarde: só se vanilla crafting/smithing/stonecutter/fornalha forem insuficientes.

**Anti-exemplos:** estação nova só para fabricar Despertador; receita com cinco intermediários renomeados e nenhuma ação diferente; ferramenta que revela coordenadas exatas sem exploração.

**Teste de valor:** um jogador demonstra antes/depois usando o novo item, sem vídeo explicativo; consegue repetir o ciclo em Survival; Recipe Book/tooltips oferecem informação suficiente.

## 3.6 P6 — CONSTRUÇÃO, DECORAÇÃO E MECANISMOS

**Fantasia:** “Quero construir com estes materiais porque minha base pode ficar diferente e também reagir ao ambiente de formas controláveis.”

**Loop independente B1 (Early → Late):**  
`obter material acessível → fabricar variantes no processo apropriado → construir um ambiente → escolher acabamento/iluminação → observar resultado → ampliar o projeto → usar novos mecanismos quando houver`.

**Recompensa imediata:** expressão arquitetônica real; **bloco puramente decorativo é válido** se bonito, coerente, recuperável e disponível para construir.

**Recompensa duradoura:** famílias completas e mecanismos de redstone cuja condição/saída sejam verificáveis; produção suficiente para projetos de escala.

**Conexões exigidas:**
- **B → Geologia (G):** transforma rochas/minerais em famílias arquitetônicas.
- **B → Natureza (N):** jardins/folhagens e ambientes de cultivo fazem parte de bases.
- **B → Crafting (C), opcional:** mecanismos são montados com insumos processados.

**Candidatos:** BLD-01 Pedra Marcada Lapidada (primeira família), BLD-02 Cristal Luminoso Trabalhado e BLD-03 Painel de Ressonância (futuro condicionado ao diferencial funcional).

**Anti-exemplos:** 20 variantes incongruentes; bloqueio de uma escada decorativa atrás de uma câmara rara; sensor que só imita placa de pressão sem característica própria; blocos que fazem atualização por tick em toda a base.

**Teste de valor:** um construtor usa e recupera os blocos em Survival, cria uma composição reconhecível e encontra materiais suficientes para um projeto razoável; se houver redstone, prova mudanças de estado e comparabilidade com vanilla.

---

# 4. MALHA ENTRE PILARES: CONEXÕES QUE GERAM JOGO

## 4.1 Mapa dirigido de interdependências (candidatos, não receitas)

| Origem | Destino | Tipo de conexão jogável | Natureza |
|---|---|---|---|
| G geologia | B construção | Pedra/rocha → variantes de material e acabamento | Principal |
| G geologia | C crafting | Depósito → material → componente/dispositivo | Principal |
| N natureza | F fauna | Planta/habitat → comportamento de criatura | Principal |
| N natureza | C crafting | Colheita → transformação/consumível de manutenção | Principal |
| F fauna | N natureza | Comportamento revela condição de cultivo/ecossistema | Principal |
| F fauna | E exploração | Movimento/alerta indica região ou risco, não coordenada exata | Principal |
| E exploração | G geologia | Arquitetura/pistas ajudam a compreender depósito | Principal |
| E exploração | C crafting | Descoberta entrega uso/técnica alternativa | Principal |
| C crafting | B construção | Processamento produz variantes/mecanismos | Principal |
| C crafting | G geologia | Ferramenta especializada melhora leitura ou extração, sem invalidar picareta | Principal |
| B construção | G geologia | Arquitetura usa materiais continuamente e demanda novos depósitos | Principal |
| B construção | N natureza | Espaços construídos possibilitam paisagismo e cultivo | Principal |
| B construção | C crafting | Base abriga estações e experimentos funcionais | Opcional |
| N natureza | B construção | Recursos orgânicos enriquecem acabamentos | Opcional |

**Invariante:** cada pilar tem **pelo menos duas conexões** coerentes. As conexões **não** são todas requisitos de desbloqueio. Em geral devem funcionar como incentivos e escolhas.

## 4.2 Evitar a falsa dependência circular

Uma conexão bidirecional de utilidade (por exemplo, planta usada em decoração e decoração melhorando espaço de cultivo) **não** significa que um jogador precise possuir ambos para iniciar qualquer um.

Todo loop inicial precisa de um **ponto de entrada independente**, obtível com vanilla e/ou uma descoberta inicial:

- Geologia: picareta vanilla + ocorrência natural.
- Natureza: observação/coleta + ambiente válido.
- Fauna: habitat natural + aproximação/observação.
- Estruturas: exploração vanilla + pistas visuais.
- Crafting: insumos obtíveis + estação vanilla quando possível.
- Construção: blocos coletados + crafting/stonecutter vanilla.

**Regra de DAG:** exigências obrigatórias para produzir um item devem formar grafo acíclico de obtenção e transformação. Ciclos de **uso opcional** são desejáveis (plantar para construir, construir para plantar melhor); ciclos de **pré-requisito** que bloqueiam o jogador são proibidos.

## 4.3 O Colar e o Despertador aparecem numa ramificação, não no tronco

A ramificação dimensional compartilha economia de mineração, crafting e preparação, mas **não** é o pré-requisito para fauna, construção, agricultura e ruínas do Overworld.

`vanilla + conteúdos Overworld (múltiplos caminhos) → preparação opcional para sonhos → Colar equipado/sono → expedição limitada → Despertador consumível no retorno → projetos no Overworld`.

**CONTRATO 6.1:** viagens ocasionais são uma ambição **Mid**, mas a capacidade de financiá-las e repeti-las consistentemente é uma conquista **Mid tardio/Late**. Isso deverá influenciar a revisão **futura** de custo e obtenção dos artefatos, não alterar o funcionamento atual nesta task.

---

# 5. PORTFÓLIO DE LOOPS COMPLETOS — PRIORIDADES DE EXPERIÊNCIA

**Critério de completude:** cada loop abaixo tem início, comportamento, recompensa imediata, uso futuro, limite de repetição, possível falha e um teste de aceitação. Os loops são especificações-alvo, **não conteúdo implementado**.

| Loop | Estágio | Começa quando… | Escolha/ação | Recompensa demonstrável | Continuidade Late | Independente dos sonhos? | Dependência de design |
|---|---|---|---|---|---|---|---|
| **L01 — Primeiro vestígio** | Early | Jogador vê formação natural discreta. | Explora, extrai e transforma. | Bloco/material útil numa base. | Construção e mecanismo futuro. | **SIM** | G + B + C |
| **L02 — Jardim observado** | Early/Mid | Jogador encontra flora em habitat. | Aprende condição e cultiva. | Jardim + insumo renovável contextual. | Produção de manutenção/paisagismo. | **SIM** | N + B + C |
| **L03 — Fauna como pista** | Early/Mid | Jogador vê criatura com hábito claro. | Segue/observa sem necessariamente atacar. | Encontra condição ambiental ou atividade nova. | Manejo ecológico e observação. | **SIM** | F + N + E |
| **L04 — Marco interpretável** | Early/Mid | Jogador identifica silhueta de POI. | Explora caminho/arranjo de pistas. | Técnica, amostra ou material pertinente. | Outras rotas e projetos. | **SIM** | E + G + C |
| **L05 — Base que reage** | Mid | Jogador tem componente processado. | Constrói bloco funcional e testa estado. | Sinal/luz/redstone útil e legível. | Automação localizada e arquitetura. | **SIM** | B + C + G |
| **L06 — Desafio com contrajogo** | Mid/Late | Jogador encontra lugar protegido. | Observa, prepara e evita/enfrenta. | Recurso opcional ou método avançado. | Projetos especializados, novas rotas. | **SIM** | E + F + C |
| **L07 — Expedição onírica** | Mid tardio/Late | Jogador acumulou preparo/artefatos. | Planeja ida, explora e retorna. | Conteúdo dimensional + experiência de travessia. | Viagens regulares por infraestrutura. | **NÃO** | Sistemas existentes + economia de Overworld |

**Mínimo formal exigido da 6.1:** L01, L02 e L04 são três loops completos inteiramente Overworld. **L03** constitui quarto loop, condicionado a especificação ecológica em 6.4. L05/L06 são visões Mid/Late, não promessa de que máquinas, mobs e sistemas sejam criados todos de uma vez.

## 5.1 L01 — O Primeiro Vestígio (fatia F0 candidata)

- **Sinal:** formação de rocha identificável a alguns blocos de distância, coerente com caverna/terreno.
- **Ação:** reconhecer e minerar com picareta vanilla adequada; sem ferramenta exclusiva para o primeiro contato.
- **Feedback:** bloco e drop reconhecíveis; texturas/UI mostram que material é especial sem iluminar mundo inteiro.
- **Transformação:** crafting vanilla ou stonecutter; **apenas uma transformação real**.
- **Uso:** acabamento ou bloco que contribua para uma construção pequena sem atravessar dimensões.
- **Repetição:** jogador quer encontrar outra formação por escolha estética/funcional, não porque uma única receita exige 200 unidades.
- **Falha controlada:** erro de ferramenta não concede loot premium; disponibilidade demonstrada em worldgen real; sem geração retroativa em saves existentes.
- **Não fazer:** criar minério extra e ferramenta de detecção na mesma F0 apenas para aumentar escopo.
- **Critério de aceite futuro:** 1 fonte natural + 1 coleta real + 1 transformação + 1 uso na base + feedback de observação, em Survival.

## 5.2 L02 — Jardim observado

- **Sinal:** planta em ambiente com condição distinguível (ex.: suporte/umidade/luz definida); fenômeno comunicado no mundo.
- **Ação:** descobrir habitat, coletar e experimentar replantio com feedback compreensível.
- **Benefício:** cultivo e paisagismo; componente de manutenção apenas se justificar uma aplicação adicional.
- **Repetição:** ciclo reprodutível com limite de crescimento/tempo; bonemeal ou luz vanilla quando pertinente, nunca farm instantânea premium.
- **Falha controlada:** ambiente errado deve comunicar que não funciona; não destruir permanentemente recursos raros por tentativa.
- **Critério de aceite futuro:** duas colheitas consecutivas, replantio legítimo, teste de restart/chunk unload e sem duplicação.

## 5.3 L03 — Fauna como pista

- **Sinal:** mob aparece em habitat real com postura, som e movimento que permitem inferir um padrão.
- **Ação:** observar/aproximar-se, escolher manter habitat e aproveitar orientação/resultado não letal.
- **Benefício:** conhecimento de local/habitat ou interação ecológica útil; sem drop obrigatório por morte.
- **Repetição:** estímulo continua válido em encontros seguintes, mas não vira GPS ou AFK farm de rare loot.
- **Falha controlada:** criatura pode fugir; não perde progresso obrigatório, não invade bases e não faz scan de chunks.
- **Critério de aceite futuro:** padrão observado por jogador novo, spawn/população medidos e comportamento seguro com dois clientes.

## 5.4 L04 — Marco interpretável

- **Sinal:** silhueta construída distinta da geologia e próxima a condições plausíveis de geração.
- **Ação:** investigar entrada, disposição arquitetônica, materiais e uma escolha leve (caminho, bloco, obstáculo).
- **Benefício:** aplicação prática; pode ser amostra, variante construtiva, informação geológica ou método opcional.
- **Repetição:** outros marcos oferecem variações de layout/contexto, **não** recarga de baús.
- **Falha controlada:** não gerar sobre lava/abismos sem regra; não enterrar entrada fora de alcance ou depender de wiki.
- **Critério de aceite futuro:** `/locate` reconhece estrutura quando registrada; terreno real, chest BE e distribuição testados em várias seeds; localização sem comando plausível em playtest.

## 5.5 L05 — Base que reage

- **Sinal:** material possui propriedade perceptível e um motivo para ser usado em construção.
- **Ação:** fabricar bloco funcional, colocá-lo, modificar condição válida e ler saída (luz/redstone/som).
- **Benefício:** aplicar em porta, indicador, iluminação ou experimento sem substituir mecanicamente bloco vanilla já existente.
- **Repetição:** custo/atualizações do bloco compatíveis com bases e máquinas de tamanho razoável.
- **Falha controlada:** não fazer polling de mundo a cada tick para dezenas de blocos; definir evento, área e estados.
- **Critério de aceite futuro:** input e output demonstráveis, multiplayer, redstone vanilla e benchmark de múltiplos blocos.

## 5.6 L06 — Desafio com contrajogo

- **Sinal:** arquitetura, mob ou efeito ambiental anuncia risco antes de dano severo.
- **Ação:** reconhecer perigo, preparar rota/equipamento/consumível, escolher confronto ou alternativa.
- **Benefício:** recompensa opcional suficientemente distinta para justificar o risco, jamais chave de acesso a todas as atividades do mod.
- **Repetição:** recompensas não renovam em baú sem custo; estruturas podem continuar úteis como referência e arquitetura.
- **Falha controlada:** derrota ou perda não apaga permanentemente a capacidade de progredir; escape viável e risco legível.
- **Critério de aceite futuro:** percursos de confronto e fuga testados; drops/economia e spawn controlados.

## 5.7 L07 — Expedição como consequência, não como eixo único

- **Sinal:** jogador adquiriu conhecimento/infraestrutura e decide preparar viagem.
- **Ação:** obtém insumos, equipa Colar, usa sono quando habilitado, explora, recupera/usa Despertador, retorna.
- **Benefício:** descobertas dimensionais e utilidade acumulada no Overworld.
- **Repetição:** Mid = viagem ocasional arriscada; Late = manutenção e preparação previsíveis, sempre com custo.
- **Falha controlada:** manter respawn onírico, regras de âncora, consumo após retorno e acesso a ruínas de recuperação.
- **Critério de aceite futuro:** economia por expedição medida; nenhuma cadeia da Fase 6 exige entrada em sonho para iniciar seus primeiros loops.

---

# 6. PROGRESSÃO POR ESTÁGIO E POR PERFIL DE JOGADOR

## 6.1 Matriz Early / Mid / Late para os seis pilares

| Pilar | Early — contato e recompensa | Mid — nova capacidade | Late — domínio sem trivializar |
|---|---|---|---|
| **G Geologia** | Descobrir microformação e obter material utilizável. | Identificar depósitos específicos; técnicas de transformação/extração. | Mineração deliberada para construção e mecanismos; materiais avançados ainda exigem exploração. |
| **N Natureza** | Colher/plantar uma espécie simples; decorar. | Cultivo contextual e ingredientes de processamento. | Produção sustentável de insumos de manutenção e projetos ecológicos. |
| **F Fauna** | Encontrar criatura neutra observável, sem grind de drops. | Interpretar hábitos/territórios; interação ecológica mais significativa. | Manejar habitats e enfrentar protetores opcionais com preparação. |
| **E Estruturas** | Reconhecer um marco e extrair utilidade de observação. | Percorrer refúgios/galerias com decisões e técnicas. | Escolher câmaras avançadas opcionais; recompensas horizontais/relevantes. |
| **C Crafting** | Transformar material em uso simples via vanilla. | Instrumentos/estações justificadas por novas ações. | Produção repetível e integração com manutenção/construção, sem eliminar custos. |
| **B Construção** | Blocos/variações e jardins acessíveis. | Famílias arquitetônicas e primeiros mecanismos específicos. | Bases expressivas com iluminação/redstone contextual e escala sustentável. |

**Acesso aos sonhos:** **Early** sem viagem como rotina; **Mid** primeiras travessias preparadas e perigosas; **Mid tardio/Late** frequência consistente por acúmulo de capacidade, recursos e infraestrutura. Não é uma quarta árvore de materiais que apaga as seis anteriores.

## 6.2 Jornadas demonstrativas: quatro pessoas, quatro motivações

### Perfil A — Explorador curioso (Early → Mid)

1. Vê uma formação distinta ao caminhar para uma caverna; investiga por curiosidade.
2. Obtém material e monta um pequeno elemento funcional/decorativo na base.
3. Aprende a reconhecer um padrão natural e usa esse aprendizado numa expedição posterior.
4. Descobre um POI de escala inicial; interpreta a arquitetura em vez de só abrir um baú.
5. Sai com variante/técnica útil que o incentiva a construir ou investigar outro bioma.

**Sem obrigação:** farmar um mob; viajar ao Dreamscape; derrotar chefe especial.

### Perfil B — Construtor (Early → Late)

1. Coleta recurso comum/vestígio e fabrica variantes de acabamento.
2. Constrói uma base temática com materiais vanilla e alguns blocos novos.
3. Opcionalmente encontra um jardim e usa elementos vegetais para paisagismo.
4. Mais tarde adiciona iluminação/bloco responsivo que tenha uma interação real com redstone.
5. Investe em produção de materiais de manutenção sem precisar completar uma campanha.

**Sem obrigação:** encontrar Câmara de Contenção para fazer escadas e muros.

### Perfil C — Naturalista (Early → Late)

1. Encontra uma espécie em habitat reconhecível, observa regras simples de ocorrência.
2. Planta em local apropriado e aprende, por tentativa legível, como cultivá-la.
3. Observa comportamento de fauna relacionado à espécie.
4. Constrói jardim/habitat e utiliza colheita em produto prático.
5. Sustenta ecossistema produtivo com limites; novas regiões trazem variações de descoberta.

**Sem obrigação:** minerar um cristal raríssimo para colher a primeira planta.

### Perfil D — Engenheiro/redstone (Mid → Late)

1. Obtém componente geológico ou de processamento numa fonte natural.
2. Constrói um bloco responsivo em projeto pequeno.
3. Observa claramente o que o ativa e qual saída fornece.
4. Integra a saída com redstone, funis, lâmpadas ou outra logística vanilla.
5. Reutiliza o mecanismo em outro projeto, sem scanner de mundo infinito.

**Sem obrigação:** acessar outra dimensão para montar o primeiro experimento.

## 6.3 Preparação dimensional sem sacrificar os outros perfis

O viajante pode combinar mineração, crafting, combate/exploração e infraestrutura. Porém o jogador que nunca equipar o Colar deve ter **pelo menos L01 + L02 + L04** disponíveis como atividades com descoberta, obtenção e recompensa reais.

O Colar e o Despertador **não são métrica principal** de sucesso dos seis pilares. São exemplos do valor de preparação e da longevidade do mundo, preservados para uma futura revisão específica da economia de viagem.

---

# 7. GRAFO DE DEPENDÊNCIAS: O QUE É GATE E O QUE É ESCOLHA

## 7.1 Camada 0: vanilla suficiente

- Mundo Survival gerado; madeira/pedra/ferro e ferramentas adequadas.
- Exploração de superfície/cavernas, cultivo simples, crafting e construção vanilla.
- Novas ocorrências **não podem depender** de qualquer item do mod para serem percebidas inicialmente.

## 7.2 Camada 1: primeiros loops autônomos

- **G1:** ocorrência + ferramenta vanilla → material → aplicação em base.
- **N1:** planta + condição de cultivo → colheita/ornamentação → reaplicação.
- **E1:** marcador/estrutura simples → exploração → pequena descoberta/aplicação.
- **F1:** habitat + mob observável → interpretação/benefício sem combate obrigatório.

**Regra:** cada raiz acima pode existir e valer a pena sozinha; artefatos dimensionais não são pré-requisitos.

## 7.3 Camada 2: especializações Mid

- Geologia + crafting → material processado/instrumento.
- Natureza + crafting → insumo de manutenção e cultivo especializado.
- Exploração + geologia → conhecimento aplicável a outra formação.
- Crafting + construção → bloco responsivo com aplicação redstone.
- Fauna + ambiente → desafio ou cooperação com comportamento legível.

Todas as ligações entre especializações são **convites**, a não ser que uma receita específica identifique claramente os ingredientes obrigatórios e suas fontes.

## 7.4 Camada 3: projetos Late e viagens consistentes

- Bases maiores; infraestrutura recorrente; exploração de estruturas avançadas **opcionais**.
- Componentes avançados e conhecimento sustentam projetos de longa duração.
- Ramificação dos sonhos usa o Colar/Despertador e suas regras existentes; produção recorrente do consumível deve ser possível após investimento, sem ganho gratuito.
- A dificuldade de ambientes novos permanece relevante mesmo com equipamento vanilla avançado.

## 7.5 Contratos anti-softlock

1. **Sem dependência de drop de uma única criatura rara** para uma ação básica.
2. **Sem receita circular**: uma estação não pode exigir um componente só produzido por ela própria.
3. **Sem descoberta obrigatória de POI único** em seed com distribuição imprevisível, salvo alternativa clara.
4. **Sem pré-requisito dimensional para recursos-base do Overworld.**
5. **Sem custo terminal renovável a partir de baú infinito**, duplicação de loot ou reset manual.
6. **Sem degradação permanente de mundo/saves** por novo mecanismo.
7. **Sem obrigação de multiplayer** para progredir em Survival solo.
8. **Sem equipamento de nova categoria que inutilize todo o vanilla.**
9. **Sem progressão baseada exclusivamente em tempo offline/AFK.**
10. **Sem confundir risco justo e travamento técnico**; a mecânica de retorno preserva a segurança aprovada.

## 7.6 Gate de entrada em sonho — problema real a resolver depois

O código atual contém uma receita relativamente simples de Colar e o Despertador baseado em 12 unidades indiretas de Pó Onírico. A visão desejada é Mid para primeiras travessias e Mid tardio/Late para consistência. **A diferença entre custo atual e progressão desejada é um item de design pendente, não uma autorização para reescrever receitas agora.**

A Task 6.6 deverá comparar alternativas:
- manter receitas e melhorar o equilíbrio pela origem/distribuição (apenas se medição sustentar);
- alterar componentes/cadeias com migração explícita e manutenção de IDs;
- distinguir acesso inicial de eficiência recorrente, sem inventar “chave de boss final”.

**Proibido** inserir novo gating de sono/retorno nesta task. O caso de jogador sem Despertador continua protegido pelo contrato das Tasks 5.x.

---

# 8. ECONOMIA, CUSTO, RARIDADE E CONTINUIDADE

## 8.1 Classes funcionais de recursos

| Classe | Comportamento esperado | Fonte preferencial | Continuidade |
|---|---|---|---|
| Base construtiva | Disponível em quantidade para obras | Depósito comum/coleta/stonecutter | Alto consumo em construção |
| Ingrediente renovável | Reposição por cultivo/produção | Planta/organismo com custo de tempo/espaço | Manutenção e projetos |
| Material de descoberta | Exploração observável | Formação, habitat, POI | Projetos específicos |
| Componente técnico | Libera comportamento distinto | Transformação de insumos | Instrumentos/redstone |
| Recurso de desafio | Valor ligado a perigo/estratégia | Estrutura/habitat perigoso, alternativas quando essencial | Projetos opcionais/avançados |
| Informação/técnica | Conhecimento de mecanismo ou alternativa | Observação, leitura de estrutura, experimento | Aprendizado persistente, sem grind contínuo |
| Cosmético | Estética sem mecânica obrigatória | Craft, coleta ou variante | Construções, coleções |

**Diretriz:** manter **material consumido em fabricação** distinto de **método aprendido uma vez**, e distinto de **recompensa estética**. Não monetizar a dificuldade com exigência arbitrária de quantidades.

## 8.2 Regra de utilidade mínima de recurso

Toda ficha em 6.2 deve conter:

`origem → extração/colheita → forma bruta → eventual transformação → uso primário → uso secundário ou exceção decorativa → consumidor no Late → modo de reposição → risco de inflação → testes`.

Uma família decorativa pode justificar existência apenas pela arquitetura, desde que seja abundante o suficiente para o tipo de construção proposto.

## 8.3 Economia para três comportamentos

- **Explorador:** recompensa material ou informacional com visibilidade e chance aceitáveis de encontrar; procurar outra ocorrência faz sentido.
- **Construtor:** obtenção em escala e conversão com perda/custo adequados; não exigir 100 horas de mineração para uma parede.
- **Viajante:** consumíveis são custo planejável no Mid tardio/Late; reposição ainda depende de recursos e pode exigir exploração de Outworld (Overworld) ou uso de mecanismos adquiridos.

## 8.4 O que medir, sem inventar percentuais

- **Encontrabilidade:** minutos/quilômetros até primeiro sinal, primeira ocorrência e segunda ocorrência por jogador/seed.
- **Acessibilidade:** quantos recursos o jogador consegue coletar legitimamente com o tier esperado.
- **Valor:** percentual de testadores que fabricam **e usam** a saída sem receber ordem direta.
- **Escala:** recursos de construção obtidos por sessão e quantidade necessária para um projeto de referência.
- **Sustentabilidade:** custo por ciclo de plantio/manutenção e custo por expedição dimensional.
- **Desafios:** mortes/retiradas e feedback de telemetria em locais opcionais; sem combate inevitável.
- **Variedade:** mais de uma ação/estilo de jogo a partir do mesmo encontro.

**Qualquer número futuro é alvo experimental até testado em mundos reais; não deduzir experiência de jogador por JSON ou `/locate` apenas.**

---

# 9. PRIMEIRA FATIA VERTICAL APROVÁVEL: F0 — O PRIMEIRO VESTÍGIO

**Status:** **PROPOSTO PARA PRIORIZAÇÃO**, não conteúdo aprovado para implementação. F0 é a maneira recomendada de testar o coração do mod **antes** de investir em mais minérios, mob, estrutura e estação.

## 9.1 Hipótese de produto

“Se uma anomalia geológica do Overworld oferecer descoberta ambiental e um uso imediato de construção, o jogador reconhecerá a identidade do DreamsDimensions e terá vontade de encontrar outra.”

## 9.2 Escopo máximo candidato

| Componente | Mínimo funcional | Decisão de 6.2/6.7 |
|---|---|---|
| Formação | Uma microformação geológica natural, discreta e identificável | Bioma, altura, blocos e frequência |
| Recurso | Um bloco/mineral recuperável por ferramenta vanilla | GEO-01 vs GEO-02 vs reaproveitamento de material existente |
| Obtenção | Drop claro com loot e tags adequadas | Ferramenta, Fortune, Silk Touch, XP quando fizer sentido |
| Craft | Uma transformação simples por crafting/stonecutter vanilla | Receita exata e custo |
| Recompensa | Uma aplicação imediata independente de sonhos | Bloco de construção **ou** mecanismo pequeno, não ambos inicialmente |
| Comunicação | Uma pista ambiental e feedback de interação/loot | Visual/áudio, sem GPS |
| QA | Geração real + mineração + crafting + aplicação + save | Testes CI, Survival e multiplayer |

**Não** criar nesta F0: novo bioma completo, mob, estrutura NBT grande, workstation customizada, sistema global de ressonância, nova armadura, teleporte ou mudança de Colar/Despertador.

## 9.3 Storyboard de Survival

1. O jogador vê uma formação durante exploração vanilla de caverna/superfície.
2. Decide investigar porque a formação se distingue do terreno, sem seta na tela.
3. Usa ferramenta vanilla compatível e obtém material.
4. Descobre crafting/aplicação por Recipe Book ou informação no item.
5. Constrói algo útil/visualmente novo em sua base.
6. Em outra exploração, reconhece a mesma linguagem ambiental e repete a atividade com mais intenção.

**Falha:** a formação é invisível/raríssima, a receita exige recursos de Mid sem aviso, o bloco não tem personalidade ou a nova mecânica depende do Colar.

## 9.4 Condições de aceite da primeira task de código futura

- Uma seed nova gera a ocorrência; resultados de várias seeds demonstram distribuição sem extremos.
- Jogador consegue achar, extrair e obter material em Survival **sem comandos**.
- Recipe Book/loot/tags/modelos/idiomas representam uso real.
- Pelo menos uma aplicação independente de dimensões funciona.
- Construção/remover/recolocar não duplica recurso nem corrompe chunk.
- Não altera frequência de Dream Ore nem contratos de viagem/retorno.
- Multiplayer, saves pré-existentes, worldgen e desempenho têm evidência identificada.
- Diagnóstico de laboratório fornece `/give`, `/tp`, `/locate` **somente** quando relevante e com destinos comprovados; comandos não substituem playtest natural.

## 9.5 Desdobramento posterior

- **F1 candidato:** natureza/cultivo + paisagismo ou ingrediente real.
- **F2 candidato:** POI que reforça geologia/uma técnica existente.
- **F3 candidato:** mob observável ligado a ambiente já implementado.
- **F4 candidato:** mecanismo ou instrumento de Mid que reutiliza fontes F0/F1.
- **F5 candidato:** conteúdo Late de manutenção, base e desafio opcional; integração de sonhos sem encerrar o mod.

**A ordem entre F1–F5 pode mudar após avaliação de dependências em 6.9.** F0 é a primeira hipótese, não um compromisso automático de lançar tudo.

---

# 10. CONTRATOS DE ARTE, EXPERIÊNCIA E ACESSIBILIDADE

## 10.1 Linguagem de pistas

Cada novo encontro precisa comunicar pelo menos **dois** dos seguintes elementos: silhueta, contraste de material, organização espacial, som local, comportamento de mob, partícula discreta ou mudança de luz.

- Pistas não devem revelar coordenadas com precisão absoluta.
- O jogador deve diferenciar recurso ambiental **comum** de recurso ou estrutura **excepcional**.
- Texturas/silhuetas precisam funcionar com configurações modestas e sem shader.
- Sons não devem substituir informação visual/textual essencial.
- A descoberta não pode depender de cor isolada (acessibilidade).

## 10.2 Coerência com vanilla

- Materiais comuns próximos a paletas vanilla; anomalia como destaque, não luz em todos os blocos.
- Usar crafting, stonecutter, furnaces, tags, redstone, loot e ferramentas vanilla quando resolvem a função.
- Novo bloco funcional só quando houver diferença prática de comportamento que não possa ser expressa de forma simples com vanilla.
- Itens de navegação/investigação não devem substituir mapas, bússola, luneta ou pistas ambientais sem razão.
- Nenhuma recompensa obrigatória é ocultada por tooltip vaga.

## 10.3 Reuso de assets

- Texturas de uma família material devem compartilhar identidade, não somente matiz recolorida.
- Modelos Blockbench e animações GeckoLib reservados a criaturas com comportamento que justifique o custo.
- Protótipos podem usar assets temporários, mas **nunca** publicar rosa/preto ou asset faltante como qualidade final.
- Worldgen visual deve respeitar geologia e evitar formas perfeitamente geométricas em toda ocorrência natural.

---

# 11. CONTRATOS DE TESTE, TÉCNICA E RISCO

**Este documento não implementa APIs.** As referências oficiais devem ser revisitadas em cada task de código para a versão real Minecraft 26.3 / NeoForge 26.3 / Java 25; não inferir assinatura de eventos de versões antigas.

## 11.1 Checklist por subsistema

| Pilar | Validação mínima para futuras implementações |
|---|---|
| G | Spawn natural multi-seed, altura/bioma, bloco exposto, ferramenta correta, Fortune/Silk/XP, chunks antigos |
| N | Habitat, suporte/luz/growth, replantio, obtenção e renovabilidade, chunk unload/restart |
| F | Mob cap, spawn natural, IA/estados, navegação, drops, cliente dedicado e multiplayer |
| E | Estrutura nativa e `/locate`, integridade/entrada, loot persistente, acessibilidade, geração em terreno válido |
| C | Receita e Recipe Book, fonte de cada insumo, crafting server-authoritative, sem duplicação, NBT/componentes |
| B | Loot de quebra, variantes de blockstate/model, colisão, redstone quando aplicável, atualização de chunks e performance |

## 11.2 Ferramentas de medição

- Inspeção do HEAD e registros para não criar duplicações.
- Scripts CI estáticos para schemas, referências e invariantes relevantes.
- Build e boot do servidor dedicado em ambiente suportado.
- **Mundo novo:** worldgen real e distribuição em ao menos quatro seeds de referência; não é necessário alterar seeds de produção.
- **Survival manual:** encontrar naturalmente, obter, fabricar, usar, desmontar e repetir; registrar tempo efetivo.
- **Multiplayer:** dois jogadores, persistência/restart, loot e consumo autoritativos.
- **Performance:** leituras limitadas, chunks já carregados, spawn caps, custo de block entities e atualização de redstone.
- Logs de diagnóstico devem ser **opt-in**, jamais flood permanente em jogadores finais.

Seeds existentes para baseline de testes: `14011`, `20261108`, `-904732`, `73199013`. Estas são referências para comparação, **não** prova antecipada de que conteúdos futuros foram gerados.

## 11.3 Comandos de laboratório — somente exemplos de formato

**Não há comandos novos de `/give`, `/locate`, `/summon` ou `/tp` para conteúdo 6.1:** a Task não registra nenhum bloco, entidade ou estrutura. **Não inventar IDs ou coordenadas.**

Quando uma fatia de código for implementada, documentar IDs verificados e comandos copiáveis de laboratório. Utilizar `/seed`, `/locate` e coordenadas reais do ambiente testado; não reutilizar coordenadas de outras seeds como se fossem universais.

## 11.4 Riscos do contrato

| Risco | Prevenção no design | Evidência exigida |
|---|---|---|
| Conteúdo sem propósito | Ficha de fonte → ação → recompensa → continuidade | Loop demonstrável |
| Overworld saturado | Microambientes em vez de biomas inteiros, distribuição localizada | Screenshots, contagens e sessões |
| Progressão linear forçada | Caminhos independentes e conexões opcionais | Jornadas de 4 perfis |
| Novo minério duplicando Dream Dust | Papéis econômicos distintos | Matriz de uso da 6.2 |
| Tempo gasto só em grind | Desbloqueio por capacidade/decisão | Sessões de Survival |
| Fauna sem ecossistema | Relação com habitat e atividades não letais | Observação e teste de mob |
| POI fora do terreno | Pré-condições de posicionamento e auditoria real | Worldgen multi-seed |
| Material decorativo inacessível | Custo/quantidade compatíveis com construção | Projeto de referência |
| Recurso Late infinito/OP | Renovabilidade só de manutenção, sinks e limites | Economia medida |
| Mudança acidental de sono/retorno | Escopo 6.1 apenas docs; preservar contrato | Git diff sem código/recipes |

---

# 12. MATRIZ DE DECISÕES E PREPARAÇÃO DAS TASKS 6.2–6.9

## 12.1 Itens para decidir em 6.2 — recursos, geologia, flora e blocos

| Questão | Critério recomendado | Status |
|---|---|---|
| F0 usa GEO-01, GEO-02 ou bloco vanilla transformado? | Identidade + obtenção + benefício imediato + custo mínimo de worldgen | **ABERTO** |
| Precisa de minério novo para o primeiro loop? | Somente se for distinto de Dream Ore e não puder ser representado por formação/rocha comum | **ABERTO** |
| Qual linha orgânica iniciar? | Uma espécie com habitat legível, replantio e utilidade real | **ABERTO** |
| Qual família arquitetônica inicial? | Variantes suficientes, custo compatível com construção, identidade visual | **ABERTO** |
| Quanto material precisa de geração natural? | A definir por medição; evitar saturação | **ABERTO** |

## 12.2 Itens para decidir em 6.3 — equipamentos/estações

- Comparar EQP-01, EQP-02 e EQP-06; **escolher no máximo um instrumento inicial** após comprovar que pistas ambientais sozinhas não bastam.
- Decidir se crafting/stonecutter/smithing/furnace cobrem as transformações. Estação nova exige **duas aplicações independentes**.
- Definir entradas, saídas, UX, cooldown e nenhuma promessa de navegação perfeita por scan global.
- Nenhum equipamento precisa melhorar diretamente o Colar para justificar existência.

## 12.3 Itens para decidir em 6.4 — fauna

- Selecionar o primeiro papel ecológico por experiência, não por beleza do modelo.
- Definir habitat/bioma, estados de IA, recompensa observacional, interação não letal e limite de população.
- Reaproveitar infraestrutura de população quando couber, mas **não** importar a hipótese de ilha flutuante para Overworld.

## 12.4 Itens para decidir em 6.5 — estruturas

- Começar por POI que ensine ou complemente geologia/crafting existentes.
- Especificar silhueta, arquitetura, escolha, loot, terreno, raridade-alvo e integração aos biomas vanilla.
- Nenhum único baú/POI raro deve segurar acesso a todos os sistemas.

## 12.5 Itens para decidir em 6.6 — crafting e progressão material

- Grafo real de insumos/transformações, sem ciclos.
- Custos por estágio, fontes alternativas quando essenciais, sinks de manutenção e receita descoberta.
- **Análise explícita da diferença entre receita atual de Colar/Despertador e objetivo Mid/Late**, com proposta de migração separada se necessária.
- Não criar bancada avançada cuja única receita interessante seja o Despertador.

## 12.6 Itens para decidir em 6.7 — arte

- Paletas, materiais, block sheet, modelos/sprites, animações e feedback ambiental.
- Primeira identidade visual completa focada em F0 (não em 40 itens simultâneos).

## 12.7 Itens para decidir em 6.8 — equilíbrio

- Definir hipóteses mensuráveis de encontrabilidade, risco, economia de construção, renovabilidade e custo de expedição.
- Comparar quatro seeds iniciais e ampliar amostra para P90/P95 quando pertinente.
- Separar números simulados, números de worldgen e tempos de jogador.

## 12.8 Itens para decidir em 6.9 — execução

- Ordenar implementações **por fatias verticais** e pré-requisitos técnicos, não por tipo de asset.
- Estabelecer tasks de código pequenas com CI, testes manuais, comandos e critérios de pronto.
- Confirmar que F0 pode ser implementada sem depender de mobs, POIs ou sistemas futuros não criados.

---

# 13. CRITÉRIOS DE ACEITE DA TASK 6.1

## 13.1 Checklist documental — concluído nesta entrega

- [x] Usa a Bible 6.0 como referência superior de produto.
- [x] Especifica os seis pilares, cada um com loop independente de sonhos.
- [x] Lista ao menos duas conexões válidas de cada pilar com outros pilares.
- [x] Define progressão Early, Mid e Late por acesso, eficiência e expressão.
- [x] Não propõe End Game próprio, chefe final obrigatório ou missão linear.
- [x] Mantém sonhos relevantes no Mid e consistentes só no Mid tardio/Late.
- [x] Preserva possibilidade de jogar apenas no Overworld.
- [x] Apresenta sete loops, seis dos quais independem de viagens dimensionais.
- [x] Distingue dependências obrigatórias, conexões opcionais e variações estéticas.
- [x] Enuncia pelo menos três loops de Overworld inteiramente jogáveis como contrato de design.
- [x] Define teste de softlocks, economia e renovabilidade.
- [x] Descreve F0 com fonte, ação, recompensa, continuidade e prova futura em Survival.
- [x] Marca IDs/conteúdo de catálogo como propostas, não aprova implementações.
- [x] Não altera Colar, Despertador, spawn, worldgen, receitas nem dimensões.
- [x] Prepara decisões e dependências das Tasks 6.2–6.9.

## 13.2 O que AINDA precisa de aprovação

- [ ] Escolher família/rocha de F0 com base em comparação 6.2, sem duplicar Dream Dust.
- [ ] Escolher primeiro cultivo da linha natureza e sua função além de decoração.
- [ ] Identificar se a primeira experiência ecológica deve ser observação de mob ou outra interação.
- [ ] Aprovar qual a recompensa concreta do primeiro marco/POI.
- [ ] Definir quando e por que precisamos de um dispositivo de leitura ambiental.
- [ ] Aprovar materiais e transformações com uma matriz de custos em 6.6.
- [ ] Confirmar visual em cliente Minecraft com screenshots; sem declarar sprites aprovados por imagens conceituais.
- [ ] Homologar em jogo pelo menos L01 e depois L02/L04; não contar tabelas como implementação.

**Esta Task está concluída como contrato de design, não como conteúdo jogável novo.** O avanço para 6.2 significa detalhar recursos e fazer decisões documentadas; não habilita a implementação antes de 6.9.

---

# 14. REFERÊNCIAS E PRECEDÊNCIA

## Documentos internos consultados

1. [Task 6.0 — Game Design Bible](GAME_DESIGN_BIBLE_TASK_6_0.md) — princípios, seis ecossistemas, catálogo conceitual, F0 e roadmap 6.x.
2. [Task 5.6 — Economia e Recursos do Overworld](OVERWORLD_ECONOMY_TASK_5_6.md) — geração natural de minérios, fonte de Dust, recipes e custos.
3. [Task 5.7 — Ruínas de Recuperação](DREAM_RUINS_TASK_5_7.md) — geração e loot das duas estruturas dimensionais; limites da evidência runtime.
4. [Matriz Histórica de Conteúdo](GAMEPLAY_CONTENT_MATRIX.md) — inventário de registros com addenda; não utilizar trechos históricos sem consultar correções posteriores.
5. [Progressão Overworld-first](PROGRESSION_OVERWORLD_FIRST.md) — referência histórica com addendum; recomendações antigas de Despertador reutilizável foram superadas.
6. [Contrato de Viagem/Retorno](TRAVEL_RETURN_CONTRACT_TASK_5_2.md) — precedência dos invariantes de sono/entrada/retorno.

## Registros e arquivos auditados

- `src/main/java/com/dreamsdimensions/mod/registry/ModBlocks.java`
- `src/main/java/com/dreamsdimensions/mod/registry/ModItems.java`
- `src/main/java/com/dreamsdimensions/mod/registry/ModEntities.java`
- `src/main/java/com/dreamsdimensions/mod/service/DreamEntryPolicy.java`
- `src/main/resources/data/dreamsdimensions/recipe/`
- `src/main/resources/data/dreamsdimensions/worldgen/structure/`

## Referências técnicas para implementação posterior

- [NeoForge 26.3 Primer](https://docs.neoforged.net/primer/docs/26.3/)
- [NeoForge Worldgen](https://docs.neoforged.net/docs/worldgen/)
- [NeoForge Loot Tables](https://docs.neoforged.net/docs/resources/server/loottables/)

A compatibilidade efetiva de APIs só será confirmada ao implementar cada subsistema no HEAD correspondente.

---

# 15. VEREDITO DE DIREÇÃO E PRÓXIMA TASK

**O Overworld precisa ser um lugar onde o jogador deseje passar tempo por escolhas próprias**: descobrir rochas, cultivar espécies, compreender criaturas, investigar estruturas, construir instrumentos e expressar criatividade em bases.

**O Colar e o Despertador não são o centro do catálogo.** O Overworld é a fundação de uma expansão contínua. O sonho é uma possibilidade poderosa que fica progressivamente acessível, e sua manutenção se torna confortável **apenas depois** que o jogador construiu condições reais para isso.

**Próxima Task: 6.2 — Recursos, Geologia, Flora e Blocos.** Ela escolherá as primeiras famílias viáveis, descartará nomes/itens redundantes e fechará fichas de conteúdo com origem, aquisição, papel, estética, crafting e continuidade. **Não iniciar simultaneamente cinco novas linhas de conteúdo.**

> **Pergunta eliminatória para a 6.2:** “Este bloco, planta ou recurso cria uma experiência no Overworld que o jogador consegue iniciar e concluir sem entrar nos sonhos?”  
> Se não, precisa de uma função própria melhor ou deve permanecer candidato/ser descartado.

---

*Fim — Task 6.1 | Contrato de planejamento. Documento de game design; nenhuma alteração funcional do mod foi realizada.*
