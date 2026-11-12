# TASK 6.2 — RECURSOS, GEOLOGIA, FLORA E BLOCOS DO OVERWORLD
## Seleção de famílias de conteúdo, contratos de obtenção e integração Early / Mid / Late

**Projeto:** DreamsDimensions  
**Tipo:** Game Design / Content & Economy Design — especificação de planejamento  
**Versão:** 1.0 — decisões conceituais com gates de implementação  
**Data:** 12 de novembro de 2026  
**Branch de trabalho:** `migration/minecraft-26.3`  
**Baseline de código/documentos:** `caed40cc305ed1a7e4b455bca76070e9b2e20a99`  
**Documentos superiores:** [Game Design Bible — Task 6.0](GAME_DESIGN_BIBLE_TASK_6_0.md) e [Pilares de Gameplay — Task 6.1](OVERWORLD_GAMEPLAY_PILLARS_TASK_6_1.md)  
**Próxima task:** 6.3 — Itens, Consumíveis, Ferramentas e Estações  
**Escopo deste commit:** exclusivamente documentação; não registra conteúdo, não modifica receitas, worldgen, loot, assets, dimensão, configuração ou código e não faz merge na main.

> **Tese:** a geologia e a natureza do Overworld devem contar a mesma história por meio de regras físicas, pistas, coleta e aplicações reais. **Early** oferece primeiras descobertas e construção acessível; **Mid** apresenta materiais com comportamentos técnicos e cultivos especializados; **Late** traz especialização, obras e suprimentos sustentáveis — **sem criar um End Game próprio** e sem reduzir tudo a ingredientes do Colar/Despertador.

---

# 0. SUMÁRIO EXECUTIVO: O QUE FOI DECIDIDO

A Task 6.1 estabeleceu seis pilares e sete loops. Esta Task 6.2 decide **quais famílias de recursos, blocos e plantas merecem ocupar o primeiro backlog** e quais ideias ficam de fora até justificarmos sua utilidade.

| Prioridade | Família / nome provisório | Estágio | Decisão 6.2 | Próxima ação permitida |
|---|---|---|---|---|
| **A / F0** | **Pedra Marcada** — rocha natural e acabamento | Early → Late | **SELECIONADA PARA ESPECIFICAÇÃO** | Fechar IDs/asset/receita e parametrização em 6.6–6.9; prototipar F0 só depois. |
| **A / F1** | **Líquen dos Vestígios** — cobertura vegetal e habitat | Early → Late | **SELECIONADO PARA ESPECIFICAÇÃO** | Fechar suporte, crescimento, propagação, loot e motivo de cultivo em 6.6–6.9. |
| **B / F2 ou F4** | **Nódulo de Reverberação** + **Fragmento Reverberante** | Mid → Late | **CONDICIONADO** | Aprovar somente após a Task 6.3 definir um mecanismo realmente novo e testável. |
| **B / F3** | **Hera do Repouso** — cultivo de recurso orgânico | Mid → Late | **CONDICIONADA** | Aprovar apenas após confirmar um consumidor prático distinto do Líquen e do vanilla. |
| **C** | **Cristal Velado** — depósito avançado | Late | **ADIADO** | Precisa de uso próprio, recompensa avançada opcional e teste de economia; não criar minério “de tier”. |
| **C** | **Pedra de Contenção / Parede de Contenção** | Mid/Late | **ADIADA** | Exige primeiro uma ameaça/efeito local real e uma resposta que o vanilla não forneça. |
| **C** | **Flor de Vigília / Fungo do Subsolo** | Mid/Late | **ADIADOS** | Só abrir nova linha botânica se ela produzir loop e consumidor diferentes. |
| **D** | Minérios de armas/armaduras em escada, Pó Onírico 2, cinco cristais coloridos | Qualquer | **REJEITADOS nesta fase** | Não criam capacidade nova nem justificam contaminação do Overworld. |

**Importante:** “selecionada” **não é “implementada” nem “aprovada para código”**. Significa que passa da ideia vaga para especificação de conteúdo. Odds, IDs finais, crafting, tiers e arte definitivos continuam sujeitos às tasks 6.3, 6.6, 6.7, 6.8 e 6.9. Nenhuma chance, frequência ou receita deste documento foi medida em jogo.

## 0.1 Estados obrigatórios

- **EXISTENTE / AUDITADO:** efetivamente presente no HEAD lido.
- **DIRETRIZ FIXADA:** restrição de produto herdada de 6.0/6.1.
- **SELECIONADO PARA ESPECIFICAÇÃO:** candidato escolhido para detalhamento futuro, sem autorização de implementar.
- **CONDICIONADO:** não avançar até comprovar função e dependência.
- **ADIADO:** ideia viável, mas sem motivo para receber ID agora.
- **REJEITADO:** desalinhado com nosso produto enquanto o contrato não mudar.
- **HIPÓTESE:** forma de distribuição, resultado ou métrica ainda não testada.

## 0.2 Perguntas que 6.2 responde

1. O primeiro conteúdo novo **precisa ser um minério?** **Não.** Precisa ser uma descoberta geológica com uso imediato e disponibilidade de construção.
2. O que diferencia o novo material de `dream_dust`? **Ele não produz Pó, não substitui a cadeia de viagem e não precisa de artefato dimensional.**
3. A flora serve apenas como cenário? **Não.** Precisa de habitat legível, possibilidade de replantio e uso paisagístico/transformação suficiente para um loop.
4. Quantos materiais raros já estão aprovados? **Nenhum.** O nódulo Mid é condicionado a mecanismo; o cristal Late é adiado.
5. Existe progressão? **Sim, por ampliação de capacidade**: reconhecer/colher → cultivar/processar → construir/mecanizar/reabastecer, com caminhos não obrigatórios.
6. Vamos adicionar tudo agora? **Não.** A primeira fatia F0 é deliberadamente pequena; o resto é backlog de design.

---

# 1. AUDITORIA E BASELINE QUE NÃO PODEMOS IGNORAR

## 1.1 Registros atuais de interesse

No HEAD consultado, `ModBlocks.java` já registra, entre outros:

| Registro / família | Local e função atual | Decisão desta Task |
|---|---|---|
| `dream_ore`, `deepslate_dream_ore` | Minérios naturais do Overworld, dropam `dream_dust` via loot | **Preservar** distribuição, IDs, drops, ferramentas e XP. |
| `dream_infused_stone`, `oneiric_core_block` | Blocos fabricáveis na cadeia de crafting já existente | **Preservar** valor e receitas até task específica de migração. |
| `dream_glow_moss`, `dream_shimmer_block` | Materiais e sinais existentes das dimensões oníricas | **Não** copiar para a flora/minérios comuns do Overworld. |
| `dream_log_block`, `dream_leaves_block`, `dream_flower_block` | Natureza existente das dimensões | **Não** reaproveitar como “nova árvore do Overworld” sem motivo ecológico. |
| `serene_stone`, `blue_dream_stone`, `blue_dream_cobblestone` | Identidade material das dimensões | **Manter origem própria**; não diluir exclusividade no Overworld. |
| `dream_sand_block`, `dream_dirt_block`, `dream_grass_block` e variantes azuis | Blocos do ecossistema dimensional | **Sem alteração** nesta fase. |

**Dados comprovados da Task 5.6:** `dream_ore` usa tamanho de veio 6 e 2 tentativas/chunk entre Y=-16 e 64; `deepslate_dream_ore` usa tamanho 5 e 2 tentativas/chunk entre Y=-64 e -16. A CI mediu geração em quatro seeds, **não** o tempo médio do jogador para encontrar e minerar Pó. Os dois usam target vanilla para rocha/deepslate, sem descartar minério exposto ao ar. Não confundir parâmetros de feature com minérios efetivamente acessíveis.

**Economia conhecida:** o Colar utiliza 3 `dream_dust`; o Despertador utiliza indiretamente 12 unidades, além de componentes vanilla, e é consumível no retorno confirmado. A Task 6.2 **não** altera nenhum desses valores, não oferece fonte de Dream Dust nas outras dimensões e não muda o padrão de entrada desabilitada sem homologação própria.

## 1.2 Gaps relevantes

Não existe, no baseline analisado:

- família de rocha exclusiva do **Overworld** com disponibilidade de construção e leitura ambiental;
- flora exclusiva do **Overworld** com crescimento e ciclo de cultivo próprios;
- material mineral do **Overworld** cuja função primária seja um mecanismo redstone próprio (não viagem);
- nova estrutura Overworld, que deverá ser contratada em 6.5;
- recompensa de Late Game Overworld suficiente para justificar outro minério raro (ainda precisa de design).

**Diretriz:** não confundir ausência de um registro com necessidade automática de adicioná-lo. É melhor uma família bem integrada do que oito minérios sem destino.

---

# 2. REGRAS DE FÍSICA FICCIONAL E ECOLOGIA

## 2.1 Por que vestígios materiais existem?

**DIRETRIZ FIXADA:** realidade e sonhos deixam marcas materiais uns nos outros. **HIPÓTESE DE PRODUÇÃO:** certas rochas conservam deformações e padrões associados a esse contato; alguns organismos crescem melhor nessas superfícies; depósitos profundos podem reagir a estímulos.

Isso justifica **sinais ambientais**, **mineração**, **flora associada**, **construção** e **mecanismos** sem decidir ainda se uma civilização perdeu o controle do fenômeno ou se ele é natural. Não usar “Observadores” como fato histórico até a narrativa e as estruturas validarem essa hipótese.

## 2.2 Leis observáveis, não magia arbitrária

1. Um vestígio tem **suporte físico**: tipo de rocha, profundidade, presença de uma formação ou habitat. Evitar spawn sem contexto.
2. Um sinal é **local e imperfeito**, mas não enganoso por erro técnico; plantas podem indicar condições favoráveis sem garantir tesouro.
3. A intensidade visual é **contida** no Overworld. Azuis luminosos, surrealismo e ilhas pertencem sobretudo às dimensões.
4. Um fenômeno com efeito mecânico deve ser **reproduzível** em laboratório com gatilho, estado e saída claros.
5. O jogador aprende a ler as pistas por **observação**, não por HUD de coordenadas.
6. Um material recuperável pelo jogador conserva suas propriedades essenciais quando colocado, salvo exceção explicitamente comunicada.
7. O uso de um recurso para construção ou cultivo **não** pode invalidar o ecossistema vanilla; deve coexistir com pedra, madeira, glow lichen, sculk, redstone, musgo e agricultura vanilla.

## 2.3 Relação com as dimensões

| Origem | O que pertence a ela | O que o Overworld pode compartilhar |
|---|---|---|
| **Overworld** | Pedra Marcada, formas discretas, flora adaptada, depósitos técnicos futuros | Princípios de vestígio/ressonância e receitas multiuso futuras. |
| **Campo Onírico Azul** | Paleta azul, rochas, flora e recursos de identidade própria | Eventual relação narrativa e transformações especiais futuras, **não** importação automática de blocos. |
| **Dreamscape** | Shimmer, ecossistemas de ilhas, mobs e estruturas próprios | Conhecimento e ingredientes exclusivos de atividades opcionais futuras, após planejamento dedicado. |

A contribuição de recursos dimensionais **não** é pré-requisito para iniciar F0/F1/F2 do Overworld.

---

# 3. TAXONOMIA ECONÔMICA DOS RECURSOS

Cada recurso deve possuir uma classe primária (para que “raro” não vire sinônimo de “forte”).

| Classe | Papel concreto | Reposição aceitável | Consumidor / continuidade | Risco principal |
|---|---|---|---|---|
| **Rochas de construção** | Material de massa para obras com identidade | Ocorrências frequentes/alternativa industrial futura | Blocos e variantes de arquitetura | Tornar construção impraticável por raridade. |
| **Flora paisagística** | Decoração, ecologia, descoberta de habitat | Replantio e crescimento controlado | Jardins, materiais orgânicos secundários | Copiar item vanilla sem diferencial. |
| **Reagente de manutenção** | Insumo consumido periodicamente em processos | Cultivo/coleta com custo de espaço/tempo | Processamento, mecanismos, suprimentos | Produção AFK ilimitada que cria material raro. |
| **Componente técnico** | Permite uma ação que vanilla não entrega da mesma forma | Depósito finito local + novas áreas, ou processamento com custos | Dispositivos Mid/Late com benefício definido | Novo minério que duplica Pó Onírico. |
| **Recurso opcional de desafio** | Acesso a uso específico de alto valor | Raro mas encontrável com estratégia; rotas alternativas quando essencial | Equipamentos/estruturas avançadas | RNG como gate de progressão. |
| **Material de viagem existente** | Colar, Núcleo, Despertador | Fonte Overworld conhecida e crafting vigente | Preparação onírica Mid tardio/Late | Deixar que monopolize todo o conteúdo novo. |

### 3.1 Matriz de exclusividade econômica

| Recurso | Viajar aos sonhos? | Construção? | Natureza/cultivo? | Mecanismo? |
|---|---|---|---|---|
| **Dream Dust existente** | **SIM**, cadeia atual | Via Pedra Infundida/Núcleo, mas cara | **NÃO** atualmente | **NÃO** atualmente |
| **Pedra Marcada proposta** | **NÃO** como função inicial | **SIM, uso principal** | Suporte/aceleração ecológica **se validado** | **TALVEZ** como substrato de bloco técnico |
| **Líquen dos Vestígios proposto** | **NÃO** | **SIM, paisagismo/acabamento** | **SIM, cultivo próprio** | Uso futuro **somente se distinto do vanilla** |
| **Fragmento Reverberante condicionado** | **NÃO** como função primária | Aplicação secundária | **NÃO** como origem necessária | **SIM, somente se mecanismo aprovado em 6.3** |
| **Cristal Velado adiado** | **NÃO DEFINIDO** | **NÃO DEFINIDO** | **NÃO DEFINIDO** | **NÃO DEFINIDO** |

**Decisão:** o Fragmento Reverberante não pode ser “outra poeira” com as mesmas receitas de `dream_dust`. Se 6.3 não demonstrar mecanismo distinto, a família inteira é descartada ou redesenhada sem criar minério.

---

# 4. GEO-01 — PEDRA MARCADA (PRIORIDADE A, F0)

## 4.1 Problema de gameplay

Atualmente o jogador consegue obter Pó Onírico no Overworld, mas não existe uma **formação visível que comunique o fenômeno e ofereça material de construção próprio sem consumir esse Pó**. Um construtor não deve gastar recursos de viagem para erguer uma parede temática. O explorador precisa de vestígio que possa reconhecer, extrair e levar para sua base.

## 4.2 Conceito e identidade

**Nome de trabalho:** Pedra Marcada (alternativa descartada: “Pedra Onírica 2”).  
**Origem narrativa:** rocha comum alterada por um contato antigo e localizado com o Limiar; exibe estrias discretas como anéis, fraturas ou veios em ritmos diferentes do minério existente.  
**Visual desejado:** cinza ardósia ou pedra acinzentada levemente quente, marca azul/cinza muito discreta nas fissuras, 16×16 ou resolução de resource pack do projeto; não ser um minério brilhante.  
**Silhueta ambiental:** pequenas marcas e fissuras agrupadas que chamem atenção no teto/parede/afloramento de rocha, sem parecer um quadrado de tijolos colocado por geração procedural.  
**Primeiro benefício:** material de construção com textura e variantes próprias, acessível com ferramenta vanilla, recuperável e legível.  
**Segundo benefício desejado:** conexão com flora F1 e, no Mid, substrato opcional para um mecanismo, **se a mecânica justificar**.

## 4.3 Contrato de aquisição

| Campo | Contrato de design |
|---|---|
| Dimensão | Somente `minecraft:overworld` na fase inicial. |
| Ambiente | Afloramentos de rocha, paredes expostas de cavernas ou transições geológicas; o alvo exato de bioma/altura ainda é hipótese. |
| Formação | Microformação irregular ancorada na rocha vanilla. Não criar ilha, arco artificial ou torre de minério. |
| Visibilidade | Pelo menos parte do conjunto deve ser identificável sem escavação aleatória, validada em mundo real. |
| Ferramenta | **Proposta:** picareta de pedra ou superior; confirmar tier/tags na task de implementação. |
| Drop | Preferir retorno do próprio bloco; não criar “fragmento bruto” só para renomear a pedra. |
| Renovabilidade | Finito por ocorrência; repetível em novos chunks. Grandes obras precisam de oferta suficiente ou de transformação alternativa posterior. |
| Experiência/encantamentos | Não conceder XP sem razão; Silk Touch/Fortune não devem multiplicar blocos construtivos indiscriminadamente. |
| Crafting | Stonecutter/crafting vanilla para acabamento. Receitas exatas ficam para 6.6. |
| Uso Mid/Late | Acabamentos, ruínas, projetos e possível substrato de flora/mecanismo. |
| Pistas | Contraste de padrão + organização espacial; som/partícula opcionais e discretos. |

**NÃO DEFINIR** agora “X tentativas por chunk” ou “Y exato”: taxas serão propostas e medidas em 6.8 com geração real. A Task 6.9 deverá exigir que algumas ocorrências sejam efetivamente visíveis no fluxo normal de exploração. Uma feature underground totalmente oculta **não atende F0**.

## 4.4 Catálogo de blocos da família (IDs candidatos, NÃO registros)

| ID conceitual em namespace | Item/bloco | Natureza | Prioridade | Como obter / papel |
|---|---|---|---|---|
| `dreamsdimensions:marked_stone` | Pedra Marcada | Bloco natural sólido | **F0** | Encontrar e extrair; origem da família. |
| `dreamsdimensions:cut_marked_stone` | Pedra Marcada Lapidada | Bloco trabalhado | **F0** | Transformar via stonecutter/crafting vanilla; primeira aplicação visível. |
| `dreamsdimensions:cut_marked_stone_stairs` | Escadas de Pedra Marcada | Bloco decorativo | Após F0 | Variante de construção, sem comportamento mágico. |
| `dreamsdimensions:cut_marked_stone_slab` | Laje de Pedra Marcada | Bloco decorativo | Após F0 | Variante de construção; drop/dupla laje corretos. |
| `dreamsdimensions:marked_stone_wall` | Muro de Pedra Marcada | Bloco decorativo | Após F0 | Variante para cercas, ruínas e bases. |

**Decisão de escopo:** F0 deve conter **no máximo** a fonte natural + uma transformação construtiva, com pista geológica. Escada, laje e muro entram depois de comprovada qualidade, volume e demanda de construção; não criar todos os assets antes de validar o loop. A família completa é um plano de expansão, não uma obrigação técnica da primeira PR.

## 4.5 Loop de Survival

`ver marcas na caverna → reconhecer agrupamento natural → minerar Pedra Marcada → voltar à base → lapidar no stonecutter/crafting → construir algo pequeno → reconhecer/visitar outra ocorrência`.

**Satisfação esperada:** percepção de que a rocha explica algo do mundo e serve para construir. **Ainda não** requer saber sobre Despertador, matar mob, encontrar uma ruína rara, fabricar estação ou entrar em dimensão.

**Por que continuaria útil no Late:** materiais de massa e estilos arquitetônicos nunca deixam de ter demanda; um jogador pode construir laboratórios, vilas, fortalezas ou paisagismo mesmo com netherita. Se a rocha ficar excessivamente rara, planejaremos método de produção arquitetônica em volume sem gerar mineral técnico gratuito.

## 4.6 Critérios de aprovação de F0

- A formação possui linguagem própria, distinguível de carvão/ferro/rocha vanilla sem textura exagerada.
- Jogador em Survival encontra ocorrências reais em um período de sessão definido por 6.8.
- Pegar o bloco requer ferramenta acessível ao Early e não oferece Poeira Onírica.
- Lapidar produz uma variante visualmente útil; ela pode ser colocada, quebrada e recuperada corretamente.
- A distribuição não recorta estruturas vanilla nem enche cavernas de padrões artificiais.
- Origem, transformação e consumidor são todos testáveis sem acessar sonhos.
- Um construtor consegue recolher material suficiente para um projeto demonstrativo sem mineração desproporcional.
- Nenhuma alteração em minérios existentes, loot de sonho ou viagem.
- Build, datapacks, mundo novo, save antigo e multiplayer terão testes conforme 6.9.

**Risco prioritário:** criar uma formação que é só “uma pedra bonita pouco frequente” e não motiva segunda visita. Se playtest indicar pouca utilidade, avaliar propriedade ambiental simples ou maior liberdade de arquitetura **antes** de aprovar novos minérios.

---

# 5. FLR-01 — LÍQUEN DOS VESTÍGIOS (PRIORIDADE A, F1)

## 5.1 Problema de gameplay

O Overworld precisa demonstrar que a alteração onírica influencia também a vida. A flora não pode ser só a mesma `dream_glow_moss` levada para outra dimensão. Ao mesmo tempo, **não** devemos criar sistema de agricultura enorme antes de ter uma espécie que seja agradável de localizar, cultivar e utilizar.

## 5.2 Identidade proposta

**Nome de trabalho:** Líquen dos Vestígios.  
**Aparência:** cobertura de pedra com pequenas ramificações foscas, coloração entre cinza-esverdeado e azul apagado; sem iluminação forte constante.  
**Habitat:** rochas úmidas/sombreadas em cavernas e abrigos naturais, inclusive perto de Pedra Marcada quando a geologia/habitat permitir.  
**Papel ambiental:** indica **condição** de habitat ou proximidade probabilística de determinada rocha; **não garante** um minério valioso abaixo de cada planta.  
**Papel para jogador:** é transplantável e pode decorar rochas/parede com um padrão singular de crescimento, permitindo pequenas instalações e jardins subterrâneos.

## 5.3 Mecanismo de cultivo (hipótese que precisa ser demonstrável)

1. O jogador retira uma unidade por método adequado (preferência: tesoura ou interação equivalente; respeitar loot vanilla quando possível).
2. Replanta em bloco de suporte válido em sombra/umidade simulada por regras **simples e comunicáveis** do Minecraft.
3. O organismo se estabelece em estado inicial; crescimento/reprodução ocorre por evento ou random tick **local**, condicionado ao suporte e à iluminação.
4. A presença de Pedra Marcada pode **favorecer**, mas nunca ser requisito absoluto para cultivar na base; escolher se será estímulo de crescimento somente após testes.
5. O jogador colhe parte da cobertura e mantém a fonte original. O sistema deve evitar multiplicação infinita instantânea por bonemeal e não exigir manter chunks carregados à força.
6. Ao mover/remover o suporte, o comportamento segue as regras de sustentação claras de blocos/plantas vanilla.

**Diferenciação exigida de Glow Lichen vanilla:** comportamento/habitat, textura e aplicação arquitetônica precisam ser reconhecíveis; se a planta funcionar exatamente como Glow Lichen sem outra vantagem, **não implementar** e reutilizar o item vanilla para F1.

## 5.4 Matriz de fontes e usos

| Elemento | Fonte | Recompensa imediata | Utilidade contínua | Limite |
|---|---|---|---|---|
| `dreamsdimensions:vestige_lichen` (ID candidato) | Habitat natural Overworld | Cobertura decorativa + indicação ambiental | Paisagismo, coleções, jardins, substratos arquitetônicos | Cultivo local com suporte/luz; não gera minério nem Pó. |
| Unidade propagável | Colheita da própria planta | Possibilidade de reproduzir sem voltar sempre ao local | Decoração em bases grandes e reposição | Não adicionar “semente” separada se o próprio item do bloco resolver. |
| Eventual derivado orgânico | **NÃO APROVADO** | Só se 6.3/6.6 fixarem consumidor concreto | Reagente de acabamento/manutenção | Sem drop genérico que serve “para futuro”. |

**Regra de design:** nenhum item adicional de fibra/resina será registrado só porque uma planta foi criada. O Líquen precisa primeiro entregar um ciclo de cultivo/arquitetura superior a mera coleta.

## 5.5 Conexão com outras atividades

- **Geologia:** o jogador aprende que certos padrões de rocha e sombra favorecem a planta; a pista é útil sem precisar de HUD.
- **Construção:** o jogador consegue levar textura viva a paredes, cavernas artificializadas e jardins.
- **Fauna (Task 6.4):** uma criatura poderá preferir esta flora como habitat/apoio, **mas não torná-la obrigatória** para o primeiro cultivo.
- **Crafting (Task 6.6):** uma aplicação secundária só entra quando for melhor que moss carpet/glow lichen e possuir consumidor real.

## 5.6 Riscos de mundo

- **RNG invisível:** a planta aparece em cavernas escuras, mas nunca é encontrada; exigir amostras de exposição e pistas visíveis.
- **Cópia vanilla:** sem diferenciação, é apenas mais uma textura de glow lichen.
- **Softlock agrícola:** só cresce perto de uma Pedra Marcada que o jogador perdeu; permitir suporte vanilla alternativo.
- **Lag:** crescimento faz busca ampla de blocos por tick; limitar a vizinhanças locais e usar padrão vanilla quando possível.
- **Economia:** bonemeal transforma um pequeno patch em ingrediente raríssimo ilimitado; manter cultivo separado de componentes de desafio.
- **Silk Touch / shears / drops:** testar tags e condições corretas na versão 26.3 quando implementar.

**Gate:** F1 só fica aprovada para código após demonstrar **pelo menos uma diferença de comportamento ou experiência** diante do Glow Lichen vanilla e duas colheitas bem-sucedidas em Survival.

---

# 6. GEO-02 / GEO-03 — NÓDULO DE REVERBERAÇÃO E FRAGMENTO REVERBERANTE (CONDICIONADOS)

## 6.1 Por que considerar outro depósito mineral?

Não basta desejar “mais minérios”. O Overworld ganha variedade geológica quando um depósito oferece:
- outra forma de buscar e extrair;
- **uma propriedade mecânica** que não está disponível pelo Dream Ore;
- um consumidor de Mid/Late Game que permaneça valioso;
- potencial arquitetônico secundário sem monopolizar a economia.

**Proposta:** uma inclusão mineral de natureza diferente da Pedra Marcada, ocasionalmente alojada em rochas profundas, que produz **Fragmento Reverberante**. O material responde a pulsos: é usado em dispositivo que **armazena/libera um sinal em um intervalo controlável**.

Não será magia genérica, chave de porta ou componente obrigatório do Despertador.

## 6.2 Diferencial funcional candidato — cuidado com duplicação de redstone

**Hipótese de mecanismo da 6.3:** um componente com **pulso redstone deliberadamente atrasado e duração configurável**, acionado por mudança de sinal de vizinho, com agendamento por eventos/ticks programados, **sem polling de mundo constante**.

Para justificar sua existência, a Task 6.3 precisa compará-lo com **repeater, observer, pulse extender e lógica vanilla**. Se só reproduzir algo trivial e caro, rejeitar. Isso não cria rede de energia nem substitui circuitos vanilla complexos.

**Segunda saída candidata:** acabamento luminoso/sinalização cujo estado represente o mesmo pulso, útil em construções. Não criar “cristal decorativo que brilha” como segunda justificativa falsa; precisa estar integrado ao mecanismo.

## 6.3 Contrato hipotético do depósito

| Campo | Proposta a validar |
|---|---|
| Dimensão | Overworld apenas. |
| Estágio | Mid: exige exploração e equipamento vanilla compatível com cavernas profundas. |
| Localização | Inclusões em rocha/deepslate, com geologia visível ao menos em parte das cavernas. |
| Pista | Assinatura visual discreta e possível conexão com formações, **sem** GPS. |
| Extração | Ferramenta apropriada e loot definido; Fortune/Silk Touch se adequados. |
| Saída | `reverberant_fragment` (ID conceitual), não `dream_dust`. |
| Aplicação | Um mecanismo Mid comprovado + possível feedback luminoso/arquitetônico. |
| Renovabilidade | Depósito finito local, disponível em chunks novos. |
| Risco | Duplicação de Dream Ore e de redstone vanilla; rejeitar se duas provas falharem. |

## 6.4 IDs candidatos — só se a Task 6.3 aprovar mecanismo

- `dreamsdimensions:reverberation_nodule` — bloco natural técnico;
- `dreamsdimensions:reverberant_fragment` — item obtido por extração;
- `dreamsdimensions:resonance_pulse_panel` — **não é criação autorizada pela 6.2**, dispositivo a especificar em 6.3.

**Gates cumulativos:**
1. demonstrar protótipo de mecanismo com comportamento distinto de vanilla;
2. fechar economia de material no grafo da 6.6;
3. provar worldgen e mineração adequados em 6.8/implementação;
4. aprovar direção visual em 6.7;
5. manter L01/L02 já úteis independentemente de Nódulo.

Até lá, **não registrar o minério “para já deixar pronto”.**

---

# 7. FLR-02 — HERA DO REPOUSO (CONDICIONADA)

**Problema:** o Líquen resolve a descoberta ecológica e parte do paisagismo, mas não basta para construir um sistema de cultivo de reagentes de Mid/Late Game.

**Fantasia candidata:** trepadeira de crescimento moderado, ligada a substrato/sombra e com fibras apropriadas a algum produto de manutenção **real**. A planta pode fornecer material renovável ao naturalista, além de uma arquitetura vertical distinta.

## 7.1 Duas experiências possíveis, apenas uma deve sobreviver

| Alternativa | Ação | Vantagem pretendida | Por que pode falhar |
|---|---|---|---|
| **A — Planta de fibra** | Cultivar, colher fibras e processar | Ingrediente renovável de um produto Mid específico, além de decoração | Se a fibra apenas substituir linha, lã ou papel, é item redundante. |
| **B — Planta estrutural** | Cultivar em suportes e colher trepadeiras colocáveis | Jardim vertical/módulos de paisagismo com crescimento dirigido | Se for só uma vinhas vanilla com cor diferente, não justifica custo. |

**Preferência provisória:** alternativa A, **condicionada** à existência de um consumidor útil em 6.3/6.6. Nunca criar fibra e estações para inventar demanda fictícia.

## 7.2 Princípios de produção

- Fonte natural limitada a habitats específicos, sem replantio obrigado em bioma distante.
- Reprodução possível em base com condição acessível, custo espacial e ritmo de crescimento.
- Nenhuma fazenda de Hera produz Cristal Velado, Dream Dust ou item de retorno.
- Se utilizada por mob em 6.4, comportamento deve ser adicional ao ciclo autônomo da planta.
- Não exigir um segundo item “semente de Hera” quando a coleta da planta é suficiente.

**Decisão:** manter no backlog para segunda família de flora, **sem IDs reservados nesta fase**.

---

# 8. RECURSOS LATE E OUTRAS IDEIAS — ADIAR OU REJEITAR

## 8.1 Cristal Velado — adiado, não minério garantido

A primeira proposta mencionava Cristal Velado como recurso profundo raro. Falta responder **o que o jogador poderá fazer com ele que não consegue por meios existentes**.

**Condição para reabrir:**
1. um equipamento ou mecanismo de Late Game realmente útil já especificado em 6.3;
2. um desafio, formação especial ou POI de 6.5 apropriado à obtenção;
3. roteiro de mineração/loot e alternativas econômicas em 6.6/6.8;
4. ganho de capacidade/expressão, não apenas +1 resistência ou dano;
5. não bloquear construção Early, agricultura ou primeira viagem por depender do cristal.

Até cumprir os cinco requisitos, **não registrar `veiled_crystal_ore` nem `veiled_crystal`**. Um encontro perigoso pode recompensar técnica/construção avançada sem precisar criar mais um minério.

## 8.2 Pedra/Parede de Contenção — adiada

Só merece existir depois de uma **ameaça ambiental localizada** ou interação concreta com a qual possa lidar. Sem efeito, uma “parede anti-anomalia” é decoração fantasiada de mecânica. Se uma estrutura não tiver risco relevante, não criar bloco que vende solução para um problema inexistente.

## 8.3 Flor de Vigília — adiada

Uma flor rara com buff genérico de “proteção do sonho” seria um atalho para transformar natureza em ingrediente do Colar. Só considerar após definir um uso próprio para exploração/sobrevivência no **Overworld** e uma produção legítima.

## 8.4 Fungo do Subsolo — adiado

Minecraft já oferece cogumelos, raízes, glow berries e plantas de caverna. Qualquer fungo novo deverá ter diferenciação clara de comportamento/bioma, colheita ou transformação. **Não criar cultivo por obrigação de preencher a categoria flora**.

## 8.5 Ideias rejeitadas nesta fase

- **Cinco “fragmentos elementais” com cores distintas** e crafting intercambiável.
- **Material especial de espada/picareta/armadura de tier superior** sem função nova.
- **Dream Dust renovável por planta/mob** que elimina a mineração Overworld.
- **Um minério exclusivo de cada bioma vanilla** só para forçar exploração artificial.
- **Blocos sensoriais que vasculham dezenas de chunks a cada tick**.
- **Uma escada decorativa que requer derrotar guardião raro**.
- **Novo bioma completo no Overworld antes de provar microhabitats**.
- **Árvore com madeira customizada antes de existir motivo ecológico ou construtivo**.

---

# 9. MATRIZ CONSOLIDADA DE CONTEÚDO — FONTE → AÇÃO → SAÍDA → CONSUMIDOR

| ID conceitual | Estado | Fonte | Obtenção / transformação | Consumidor primário | Aplicação secundária ou Late | Dependência |
|---|---|---|---|---|---|---|
| `dreamsdimensions:dream_ore` | **EXISTENTE** | Stone replaceables Overworld | Ferramenta válida, loot | Dream Dust → itens existentes | Economia de viagem | Sem alteração |
| `dreamsdimensions:deepslate_dream_ore` | **EXISTENTE** | Deepslate replaceables Overworld | Ferramenta válida, loot | Dream Dust → itens existentes | Economia de viagem | Sem alteração |
| `dreamsdimensions:dream_dust` | **EXISTENTE** | Dois minérios anteriores | Drop, Fortune/Silk sob regras existentes | Colar e cadeia de Núcleo/Despertador | Pedra Infundida/crafting existente | Sem alteração |
| `dreamsdimensions:marked_stone` | **SELECIONADO F0** | Afloramento/caverna | Picareta vanilla candidata; drop do bloco | `cut_marked_stone` | Construção, substrato de habitat opcional | 6.6–6.9 |
| `dreamsdimensions:cut_marked_stone` | **SELECIONADO F0** | Marcada natural | Stonecutter ou crafting vanilla | Parede/piso/construção | Famílias arquitetônicas | 6.6–6.9 |
| `dreamsdimensions:cut_marked_stone_stairs` | **BACKLOG** | Pedra lapidada | Stonecutter/crafting | Escadas | Arquitetura | Validar F0 |
| `dreamsdimensions:cut_marked_stone_slab` | **BACKLOG** | Pedra lapidada | Stonecutter/crafting | Lajes | Arquitetura | Validar F0 |
| `dreamsdimensions:marked_stone_wall` | **BACKLOG** | Pedra natural/lapidada | Stonecutter/crafting | Muros | Arquitetura | Validar F0 |
| `dreamsdimensions:vestige_lichen` | **SELECIONADO F1** | Habitat de caverna/sombra | Coleta, suporte e cultivo local | Jardins e revestimento vegetal | Ecologia/futura transformação se distinta | 6.6–6.9 |
| `dreamsdimensions:reverberation_nodule` | **CONDICIONADO** | Depósito profundo Overworld | Ferramenta/tier ainda a definir | Fragmento Reverberante | Mineral especial Mid | Requer mecanismo 6.3 |
| `dreamsdimensions:reverberant_fragment` | **CONDICIONADO** | Loot do nódulo | Extração | Dispositivo de pulso real | Sinalização arquitetônica | Requer mecanismo 6.3 |
| `dreamsdimensions:resonance_pulse_panel` | **CONDICIONADO 6.3** | Craft futuro | Reagente + redstone vanilla | Sinal atrasado/controlável, se diferenciado | Construções e automação | Comparar vanilla |
| Hera do Repouso | **CONDICIONADA** | Habitat vegetal | Colheita e cultivo | Insumo renovável ainda não especificado | Paisagismo | Consumidor 6.3/6.6 |
| Cristal Velado | **ADIADO** | Depósito/estrutura futura | Desafio opcional | **NÃO DEFINIDO** | **NÃO DEFINIDO** | Não registrar |
| Pedra de Contenção | **ADIADA** | Processo futuro | Craft especializado | Ameaça futura ainda não existe | Arquitetura | Não registrar |
| Flor de Vigília | **ADIADA** | Cultivo futuro | Coleta | **NÃO DEFINIDO** | **NÃO DEFINIDO** | Não registrar |

**Regra para a 6.3/6.6:** nenhum registro novo pode terminar em “uso futuro” como **único consumidor**. Cada item precisa de ao menos uma ação demonstrável na sua primeira entrega, ou não entra.

---

# 10. PROGRESSÃO CROSS-PILAR — EARLY / MID / LATE

## 10.1 Early — “uma marca na rocha, uma história no terreno”

`explorar cavernas/afloramentos → notar Pedra Marcada → coletar com ferramenta vanilla → lapidar → colocar na base`.

**Ganho real:** nova linguagem arquitetônica e forma de observar a geologia. **Não exige** mecanismo especial, mob, cultivo, Colar ou Despertador.

Se F1 já estiver implementada:  
`encontrar Líquen → conhecer o habitat → levar para base → replantar → criar jardim/parede viva`.

**Ganho real:** ecologia e paisagismo independentes de exploração dimensional.

## 10.2 Mid — “o mundo responde aos experimentos”

Com L01 e L02 comprovados, ampliar no **sentido de comportamento**:
- mineral técnico Mid **somente** após consumidor validado;
- dispositivo de construção/redstone com sinal previsível, não duplicação trivial de vanilla;
- cultivos que alimentam manutenção de mecanismos ou itens situacionais, sem exigir 20 intermediários;
- estruturas em 6.5 que reforcem leitura de geologia e métodos alternativos.

O jogador pode escolher aprofundar redstone, agricultura, estruturas ou equipamentos. Nenhuma rota obriga completar todas as outras.

## 10.3 Late — “autonomia e especialização, não um fim de jogo”

- Produção maior de materiais **construtivos e renováveis** após trabalho de infraestrutura.
- Componentes técnicos continuam requerendo exploração ou custos justos.
- Estruturas difíceis e materiais raros oferecem melhorias **opcionais**, não destravam o Minecraft inteiro.
- O crescimento de uma base dá motivo para continuar minerando, cultivando e estudando habitats.
- Viagens oníricas se tornam regulares apenas quando o jogador domina preparação/ressuprimento; **não** introduzir retorno gratuito nem dependência total de sonhos.

### 10.4 Histórias de jogador para validar o valor

**Construtor:** coleta Pedra Marcada → faz piso/ruína temática → procura outra formação → melhora escala de obtenção ao avançar. Não possui Colar.

**Naturalista:** identifica Líquen → reproduz em local sombreado → cria jardim interno → observa ecologia relacionada quando 6.4 entregar fauna. Não minera minério raro.

**Engenheiro:** minera nódulo **se aprovado** → fabrica painel de pulso → integra com redstone vanilla → usa em projeto Mid/Late. Não precisa entrar no Dreamscape.

**Explorador:** reconhece vestígios → aprende pistas → encontra POI de 6.5 → obtém técnica ou material de construção diferenciado → retorna ao Overworld com novo projeto.

**Viajante:** usa economia já existente de Pó Onírico e artefatos; pode aproveitar materiais da nova expansão em projetos, mas **não** converte todo recurso novo em moeda do Despertador.

---

# 11. ORIGEM E WORLDGEN: COMO NÃO ARRUINAR O OVERWORLD

## 11.1 Microambientes antes de biomas

A primeira expansão não modifica clima, distribuição de biomas, geração de cavernas vanilla ou superfícies inteiras. Usar **microformações** e **microhabitats**:
- geologia em rocha compatível, respeitando replaceables e cavidades;
- sinais visíveis mas não espalhados por todo chunk;
- flora com condição natural e relação espacial plausível;
- distribuição dependente de passo de worldgen apropriado, a confirmar no código real;
- evitar biomas incompatíveis com a fantasia sem depender de hardcoding de nome.

**Limite:** não colocar camada de Pedra Marcada por cima de vilas, strongholds, mineshafts, cavernas grandes ou construções de jogador. Worldgen só em chunks novos, e a geração deve rejeitar posições onde não exista contexto válido.

## 11.2 Teste de coocorrência sem mentir para o jogador

Se o Líquen servir como indicador de Pedra Marcada, a ligação precisa existir **na configuração de geração**, não só no texto de lore. Medir:

- proporção de manchas vegetais a uma distância de busca pré-definida de ocorrência mineral;
- proporção de minerais cercados por flora;
- falsos positivos e falsos negativos de pista;
- resultados por seed/bioma/altura;
- chance de o jogador ver a correlação andando normalmente.

**Não:** implementar texto “plantas indicam minério” se as duas features nascerem independentemente sem correlação. Pode ser líquen de habitat, não detector, se o acoplamento não for tecnicamente viável ou útil.

## 11.3 Contrato de desempenho e integridade

- Consultas em mundo durante geração limitadas a região local e às posições necessárias; sem flood de leituras.
- Preferir features/configurações vanilla/data-driven quando suportarem a forma, evitando Java que não se justifica.
- Preservar estruturas e fontes vanilla, sem alterar noise/density de Overworld por conveniência.
- Respeitar border, altura, bloco de destino, suporte de plantas e consistência de chunks.
- Não gerar o mesmo recurso por biome modifier e evento Java redundantes.
- Nenhuma operação com chunks não carregados em interação/growth fora de geração.
- Atualizações de bloco por vizinho/random tick **locais**; eventos reativos preferíveis a scans recorrentes.

## 11.4 Estratégia de compatibilidade de mundo

- Material novo aparece em **chunks futuros**, não retroativamente em região explorada.
- Jogadores de save antigo podem buscar conteúdos em novas áreas.
- IDs existentes, loot e state antigo `dream_ore.clicked` permanecem intactos.
- Não introduzir retrogeração automática; estudar apenas se algum conteúdo se tornar essencial e impossível em saves muito explorados.
- Nada de estrutura/feature “corrigida” por comando de laboratório aplicada silenciosamente em mundo permanente.

---

# 12. ASSETS E DIREÇÃO VISUAL POR FAMÍLIA

| Família | Paleta e silhueta | Como diferenciar em gameplay | Critérios para Task 6.7 |
|---|---|---|---|
| Pedra Marcada | Cinza natural, fissuras concêntricas e destaque sutil | Formação reconhecível, bloco compatível com pedra vanilla | Textura tiling; topo/lado se necessário; variantes com coerência. |
| Pedra Lapidada | Cortes regulares preservando marcas | Arquitetura de ruína e base sem brilho excessivo | Combinação lado a lado com stone bricks, tuff e deepslate. |
| Líquen | Cinza-esverdeado/azul apagado, ramificações esparsas | Habitat sombreado, propagação própria, cobertura reaproveitável | Não confundir com glow lichen vanilla/dream_glow_moss. |
| Nódulo Reverberação | Inclusão mineral localizada, veios mais compactos | Mid técnico, não visual de Dream Ore | Clareza de tier sem brilho uniforme em toda caverna. |
| Painel técnico (6.3) | Corpo de pedra/cobre e pequeno ponto de feedback | Estado redstone visível: inativo/acionado/saída | Renderização eficiente, sem animação por tick obrigatória. |
| Cristal Velado (adiado) | Não produzir arte final | Sem mecanismo decidido | **Não criar asset ainda.** |

**Regra:** um material precisa parecer parte do Overworld durante a maior parte do tempo. Os espaços oníricos continuam visualmente extraordinários; não roubar toda a linguagem de cor das dimensões.

---

# 13. BALANCEAMENTO: HIPÓTESES, NÃO NÚMEROS INVENTADOS

A Task 6.8 definirá alvos quantitativos, mas já estabelecemos o **que precisa ser medido**.

| Medição | Pergunta | Instrumento/ambiente |
|---|---|---|
| Primeira Pedra Marcada | Em quanto tempo um jogador a percebe casualmente? | Survival cego, múltiplas seeds/biomas. |
| Segunda ocorrência | O jogador consegue procurá-la após aprender os sinais? | Survival com conhecimento prévio. |
| Suprimento construtivo | Quantos blocos recupera numa sessão? Quantos precisa para obra de referência? | Mineração real + projeto construído. |
| Exposição | Que fração da formação é visível em cavernas/afloramentos? | Worldgen real, snapshot de chunks. |
| Distribuição espacial | Existem regiões sem conteúdo ou saturadas? | Amostras por bioma/altura/quatro seeds inicialmente. |
| Cultivo F1 | Quanto tempo para duas colheitas reais? Pode reproduzir sem visitar o mesmo bioma? | Survival, luz/suporte, unload/restart. |
| Diferenciação | O Líquen tem vantagem real diante do Glow Lichen? | A/B de experiência; usar vanilla como controle. |
| Nódulo Mid | Vale a exploração para obter a capacidade do dispositivo? | Protótipo de 6.3 + medidas de extração. |
| Recurso Late | É realmente necessário e distinto? | Análise de receita, desafios, construção e satisfação. |
| Multiplayer | Dois jogadores disputam recursos, duplicam loot ou desincronizam states? | Dedicated server, duas contas. |

Sem métricas de distribuição comprovadas, **não declarar** que um recurso será “raro na medida certa”. Cuidado com erros anteriores: contagem de minério em chunk **não** equivale a minerabilidade; `/locate` identifica start da estrutura **não** comprova integridade física de todo baú/terreno.

## 13.1 Baseline e cenários de teste

Seeds de regressão já usadas pelo projeto: `14011`, `20261108`, `-904732`, `73199013`.

A seed pessoal de um jogador, o resultado de um comando e um chunk já gerado não devem ser confundidos com baseline de worldgen novo. `/tp` e `/locate` só podem referir a estruturas existentes com posições verificadas na seed correta; **nenhuma coordenada nova foi medida nesta Task de documentação**.

A futura implementação terá scripts de CI com diagnósticos reais de distribuição e comandos de laboratório copiáveis. `/locate` só serve para **estrutura registrada**, não para um bloco isolado ou placed feature comum.

---

# 14. CONTRATOS DE DEPENDÊNCIA DAS PRÓXIMAS TASKS

| Próxima task | Decisão que deve receber desta 6.2 | O que não pode pressupor |
|---|---|---|
| **6.3 — Itens e Estações** | Família Pedra Marcada e Líquen selecionadas; Nódulo condicionado a uso técnico | Que três ferramentas ou uma bancada já foram aprovadas. |
| **6.4 — Ecologia/Mobs** | Habitat do Líquen e pistas da geologia como opções de comportamento | Que planta sempre indica minério; que mob é obrigatório para colher. |
| **6.5 — Estruturas** | Pedra Marcada pode ser material arquitetônico/linguagem de pista; origem física da geologia | Que todas as ruínas precisam de baú ou guardião. |
| **6.6 — Grafo de Crafting** | Fontes/saídas e IDs candidatos; exigir Stonecutter/crafting vanilla para F0 se suficientes | Que todo candidato tem consumidor; que receitas atuais podem mudar silenciosamente. |
| **6.7 — Arte/Assets** | Paletas, formas e escala; prototipar F0 e depois F1 | Que Cristal Velado merece modelagem agora. |
| **6.8 — Raridade** | Classes de recurso e indicadores de observação/tempo/escala | Que frequência/tier são aprovados sem simulação e Survival. |
| **6.9 — Roadmap Executivo** | F0 menor escopo, F1 cultivo, Mid mineral técnico condicionado, Late sem endpoint | Que 6.2 implementou algo ou autoriza codificar tudo em lote. |

## 14.1 Sequência técnica de implementação proposta após 6.9

**F0 — Pedra Marcada (obter + transformar + construir)**  
Pré-requisitos: 6.2 + 6.6 (receita) + 6.7 (textura) + 6.8 (worldgen/escassez) + 6.9 (task).  
Entrega: 1 origem geológica, 1 bloco de fonte, 1 saída lapidada, 1 receita vanilla, 1 playtest completo.

**F1 — Líquen dos Vestígios (encontrar + transplantar + cultivar)**  
Pré-requisitos: F0, especificação de crescimento e comparação Glow Lichen vanilla.  
Entrega: 1 espécie ambiental, 1 ciclo de cultivo, 1 uso paisagístico/consumidor validado, testes de reload.

**F2 — Primeira estrutura ou criatura vinculada às marcas**  
Escolha depende de 6.4/6.5, aproveitando conteúdo de F0/F1 em vez de criar outra economia.

**F3 — Mecanismo Mid condicionado**  
Só depois do A/B da Task 6.3 comparar dispositivo de pulso com vanilla. Se vencer, implementar Nódulo/Fragmento e dispositivo no **mesmo pacote**, testados juntos. Caso contrário, eliminar minério e escolher outra mecânica.

**F4 — Flora de manutenção ou ferramenta de especialização**  
Somente se um consumidor real exigir o recurso. Não criar Hera para preencher calendário.

**F5 — Conteúdo Late opcional e integração**  
Materiais raros/desafios só após recompensa de comportamento validada; mundo permanece sem End Game exclusivo.

**Relação com o ciclo onírico:** migração de Colar/Despertador, quando necessária, deve ser especificada em task própria e protegida por testes; **não** é pré-requisito do primeiro loop de Overworld.

---

# 15. RISCOS, TRADE-OFFS E DECISÕES QUE FICARAM ABERTAS

## 15.1 Riscos de produto

| Risco | Probabilidade/gravidade conceitual | Ação preventiva |
|---|---|---|
| Pedra Marcada parece mais um bloco vanilla recolorido | Alta se arte/gen falharem | Testar silhueta e construção lado a lado, rejeitar textura fraca. |
| F0 não entrega mecanismo imediato | Deliberado, risco moderado | Tratar F0 como prova pequena; se não divertir, elevar interação sem criar minério inútil. |
| Líquen duplica Glow Lichen | Alta sem mecânica própria | Gate A/B obrigatório antes de F1. |
| Mineral técnico Mid não supera redstone vanilla | Alto | Prototipar mecanismo antes de registrar nódulo. |
| Conteúdo Late vira grind artificial | Alto se inventarmos cristal raro antes de uso | Cristal Velado e Contenção adiados. |
| Overworld perde identidade vanilla | Alto se patches brilhantes saturarem | Microhabitats e paleta discreta, medições de exposição. |
| Jogador precisa do Colar para tudo | Alto se economia virar uma moeda única | Três loops autônomos obrigatórios e nenhuma origem dimensional em F0/F1. |
| Excessos de documentação sem gameplay | Médio | Em 6.9 iniciar F0 pequena; não prototipar 20 candidatos. |

## 15.2 Decisões abertas que NÃO podem ser inventadas por implementação

1. Nome definitivo de cada bloco e chave de registro, após revisar estilo e colisão de IDs.
2. Texturas e paleta final de Pedra Marcada/Líquen.
3. Biomas, altura, densidade, tamanho e orientação das formações.
4. Regra de crescimento/bonemeal/suporte de Líquen e sua diferenciação vanilla.
5. Receita específica de pedra lapidada, taxa de conversão e stonecutter.
6. Necessidade real de segundo minério Mid; depende da 6.3.
7. Consumidor concreto de Hera do Repouso; depende da 6.3/6.6.
8. Mecânica que justificará Cristal Velado ou Pedra de Contenção; ainda ausente.
9. Hipóteses de tempo/dificuldade a confirmar com sessões reais de Survival.
10. Como incorporar pistas e fauna às estruturas sem criar obrigatoriedade ou scans pesados.

---

# 16. CRITÉRIOS DE ACEITE — TASK 6.2

## 16.1 Contrato documental atendido

- [x] Leu Game Design Bible 6.0 e contrato de seis pilares/loops 6.1.
- [x] Auditou registros e economia relevantes do HEAD da branch.
- [x] Separou material existente de proposta não registrada.
- [x] Escolheu **um** material/família geológica para F0 com fonte, ação, transformação e uso independente de sonhos.
- [x] Selecionou **uma** espécie vegetal para F1 e definiu ciclo de obtenção/cultivo condicionado.
- [x] Descreveu pelo menos um candidato mineral de Mid com **consumidor condicionado à prova de mecanismo**.
- [x] Explicitou que minérios raros de Late não são aprovados por desejo de aumentar tiers.
- [x] Diferenciou utilidade de materiais novos em relação a Dream Dust e recursos dimensionais.
- [x] Definiu famílias decorativas sem “mágica obrigatória”.
- [x] Ligou geologia/natureza/construção/crafting e pontos para fauna/estruturas.
- [x] Separou recursos renováveis de recursos de exploração e componentes técnicos.
- [x] Descreveu integridade de worldgen e riscos de saves/multiplayer/performance.
- [x] Preparou dependências explícitas para 6.3–6.9 e sequência por fatias verticais.
- [x] Rejeitou/adiou ideias que ainda não têm gameplay justificável.
- [x] Não criou novo minério, item, bloco, receita, textura, NBT, entidade ou alteração funcional.

## 16.2 Aceite de conteúdo para futuras PRs — AINDA PENDENTE

- [ ] F0 aprovado visualmente em Minecraft e testado com obtenção/uso em Survival.
- [ ] Receita de F0 fechada e medida em 6.6/6.8.
- [ ] Worldgen F0 medido em múltiplas seeds por distância/visibilidade, sem saturação.
- [ ] F1 diferencia-se de Glow Lichen e cresce sem scan global, com ciclo reproduzível.
- [ ] Se nódulo for aprovado, mecanismo redstone é realmente distinto e verificável.
- [ ] Catálogo de IDs final sem colisão e com asset/loot/tag/idioma coerentes.
- [ ] Multiplayer, chunk unload, dedicated server e saves antigos aprovados.
- [ ] Economia Late não elimina valor de exploração ou recursos Early.

A primeira lista conclui **documentação**. A segunda só será marcada após implementação e QA em tasks futuras. **Não anunciar recursos como existentes.**

---

# 17. REFERÊNCIAS E GOVERNANÇA

### Referências internas (fonte da baseline)

- [Game Design Bible — Task 6.0](GAME_DESIGN_BIBLE_TASK_6_0.md)
- [Pilares de Gameplay — Task 6.1](OVERWORLD_GAMEPLAY_PILLARS_TASK_6_1.md)
- [Economia do Overworld — Task 5.6](OVERWORLD_ECONOMY_TASK_5_6.md)
- [Distribuição de Dream Ore — Task 5.6](OVERWORLD_ORE_DISTRIBUTION_TASK_5_6.md)
- [Matriz de Conteúdo — Task 5.0 + addenda](GAMEPLAY_CONTENT_MATRIX.md)
- [Ruínas de Recuperação — Task 5.7](DREAM_RUINS_TASK_5_7.md)
- `src/main/java/com/dreamsdimensions/mod/registry/ModBlocks.java`
- `src/main/java/com/dreamsdimensions/mod/registry/ModItems.java`
- `src/main/java/com/dreamsdimensions/mod/registry/ModEntities.java`
- `src/main/resources/data/dreamsdimensions/worldgen/feature/dream_ore.json`
- `src/main/resources/data/dreamsdimensions/worldgen/placed_feature/dream_ore.json`
- `src/main/resources/data/dreamsdimensions/worldgen/feature/deepslate_dream_ore.json`
- `src/main/resources/data/dreamsdimensions/worldgen/placed_feature/deepslate_dream_ore.json`

### Fontes técnicas para consulta ao implementar, não assinaturas já verificadas

- [NeoForge 26.3 Primer](https://docs.neoforged.net/primer/docs/26.3/)
- [NeoForge Worldgen](https://docs.neoforged.net/docs/worldgen/)
- [NeoForge Loot Tables](https://docs.neoforged.net/docs/resources/server/loottables/)

A compatibilidade efetiva de propriedades, eventos de crescimento, registries e codecs deve ser verificada contra a versão 26.3 e o código do projeto **quando** as tasks de implementação ocorrerem.

### Governaça das decisões

1. Uma seleção de conteúdo em 6.2 permite detalhamento em 6.3–6.9, **não** merge de código.
2. Se um recurso condicionado não obtiver consumidor convincente, removê-lo do backlog; não criar uso artificial.
3. Se um experimento contradisser a premissa de F0/F1, registrar o resultado e corrigir a direção antes de expandir.
4. O GitHub `main` permanece intocado; nenhuma receita ou minério existente muda nesta task.
5. As tasks de implementação deverão fornecer comandos copiáveis e coordenadas apenas após validação no mundo correto; não inventar `/tp`.

---

# 18. DECISÃO FINAL DA DIREÇÃO DE JOGO

**A próxima expansão do Overworld começa pelo mundo, não pela tabela de crafting.** A Pedra Marcada cria um vestígio físico que o jogador pode descobrir e usar em projetos. O Líquen dos Vestígios acrescenta vida e cultivo onde antes havia apenas geologia. Um possível Nódulo de Reverberação só nasce quando houver um mecanismo interessante o suficiente para merecer um novo recurso — e material raro de Late Game só nasce quando houver desafio e recompensa realmente distintos.

A consequência disso é um mod mais rico sem perder dificuldade: **Early oferece pequenas descobertas e construção; Mid permite especialização e mecanismos; Late permite produção sustentada, exploração e grandes projetos**, enquanto viagens oníricas se tornam consistentes somente após preparo, sem criar um final obrigatório.

> **Próxima task:** **6.3 — Itens, Consumíveis, Ferramentas e Estações**, com uma pergunta incontornável: **que ação nova cada ferramenta ou estação permite, e por que ela é melhor do que usar somente um item vanilla?**

---

*Fim — Task 6.2. Entrega documental; nenhum conteúdo novo foi implementado no jogo.*