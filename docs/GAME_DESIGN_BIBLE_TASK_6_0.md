# DREAMSDIMENSIONS
## TASK 6.0 — GAME DESIGN BIBLE
### A expansão do Overworld e a jornada entre realidade e sonhos

**Tipo:** Documento mestre de direção de jogo (GDD / Game Design Bible)  
**Versão:** 1.0 — proposta para aprovação de direção  
**Data:** 11 de novembro de 2026  
**Projeto:** [DreamsDimensions](https://github.com/ErikMendesRc/dreamsdimensions)  
**Branch de referência:** `migration/minecraft-26.3`  
**HEAD consultado:** `a1f1775b5b52256b47ddc3acd2dea12e9fe06ce3`  
**Stack auditada:** Minecraft 26.3 · NeoForge 26.3.0.101 · Java 25 · ModDevGradle · GeckoLib · Curios  
**Escopo da Task:** direção narrativa, gameplay, progressão, catálogo conceitual e contratos para planejamento; **não implementa código, não altera o repositório e não aprova automaticamente novas receitas.**

> **Tese de direção:** DreamsDimensions não é um mod cujo único objetivo é entrar em outras dimensões. É uma expansão de Survival, exploração, natureza, mineração, construção e mecanismos do Minecraft, em que o contato entre realidade e sonhos acrescenta sistemas capazes de sustentar diferentes estilos de jogo. Os sonhos tornam-se **acessíveis de forma arriscada no Mid Game** e **consistentemente exploráveis apenas do Mid tardio ao Late Game**, sem um End Game exclusivo ou conclusão obrigatória.

---

# 0. COMO USAR ESTA BIBLE

Esta Bible estabelece **o que o DreamsDimensions deve ser e como avaliar o que será criado**. Não é uma lista de tarefas de implementação, nem uma autorização para inserir todos os conceitos descritos. O documento tem cinco níveis de decisão:

| Marcador | Significado | Ação permitida |
|---|---|---|
| **EXISTENTE / AUDITADO** | Código, recursos ou datapacks encontrados na branch consultada. | Preservar comportamento e contratos; qualquer mudança requer task e regressão. |
| **DIRETRIZ FIXADA** | Decisão de design assumida a partir das orientações do projeto. | Deve orientar novas propostas; mudanças exigem revisão explícita. |
| **PROPOSTO** | Conceito concreto para avaliação; não está implementado. | Pode orientar protótipo de risco limitado; não é conteúdo aprovado para produção. |
| **HIPÓTESE** | Explicação narrativa, nome, sistema ou balanceamento ainda a validar. | Prototipar/medir antes de confirmar. |
| **ADIADO / FORA DE ESCOPO** | Conteúdo que não precisamos nesta fase. | Não criar como dependência artificial. |

**Regra de leitura:** nome, raridade, bioma, receita, item e estatística escritos como **PROPOSTO** são deliberadamente revisáveis. A palavra “late game” neste documento **não representa um final de jogo**. Métricas de tempo, chance de obtenção e densidade são metas de experimentação, **não resultados medidos**.

### 0.1 Ordem de autoridade

1. Contratos de gameplay já implementados e homologados; configurações e registros reais do repositório.
2. Diretrizes fixadas nesta Bible após aprovação.
3. Especificações futuras das Tasks 6.1–6.9, que podem detalhar ou propor revisão da Bible.
4. Propostas de conteúdo, protótipos visuais e hipóteses narrativas.

Quando uma ideia conflitar com o código existente, **não alterar silenciosamente a regra em produção**: registrar a discrepância, justificar e criar task de migração.

---

# 1. VISÃO DO PRODUTO

## 1.1 Promessa ao jogador

Em uma sobrevivência aparentemente familiar, o jogador começa a perceber marcas discretas de algo que não deveria estar ali: veios minerais anômalos, vegetação que reage a condições estranhas, criaturas com hábitos incomuns, ruínas de origem incerta e materiais capazes de alterar mecanismos, luz, som ou comportamento ambiental.

Essas descobertas não são apenas pistas para um portal. Elas fornecem novas maneiras de **minerar, cultivar, explorar, construir, automatizar, se proteger e preparar expedições**. O universo dos sonhos dá sentido comum a esses sistemas, mas cada um entrega diversão independentemente de viagens dimensionais.

O Overworld deve continuar reconhecível como Minecraft. A estranheza será **localizada, gradual e memorável** — algo que merece investigação, não uma cor sobrenatural aplicada a cada árvore e bloco.

## 1.2 Fantasias de jogo que sustentam o mod

- **Explorador:** “Vi algo no horizonte; quero entender o que é e o que posso conseguir lá.”
- **Minerador:** “Aprendi a ler a geologia; agora sei onde procurar materiais com usos diferentes.”
- **Naturalista:** “Encontrei uma espécie ou planta rara; posso estudá-la, preservá-la e aproveitá-la.”
- **Construtor:** “Tenho uma identidade arquitetônica nova, com famílias de blocos e iluminação.”
- **Engenheiro de redstone:** “Posso montar sistemas que detectam ou respondem a fenômenos.”
- **Aventureiro:** “Uma estrutura perigosa merece preparação e oferece uma recompensa significativa.”
- **Viajante onírico:** “Depois de me preparar, posso arriscar uma expedição e, mais tarde, torná-la parte da minha rotina.”

Nenhuma dessas fantasias é secundária. **Um jogador pode passar dezenas de horas aproveitando o Overworld sem usar o Colar.**

## 1.3 O que diferencia DreamsDimensions

1. **Fronteira permeável entre sonhos e mundo físico:** o extraordinário já influencia o Overworld.
2. **Exploração por observação:** padrões de paisagem, criaturas, materiais, luz e som fornecem pistas que podem ser aprendidas.
3. **Materiais com múltiplos usos:** mineração alimenta construção, cultivo, mecanismos e equipamentos, não apenas um craft terminal.
4. **Ecossistemas com regras:** criaturas e plantas se relacionam com habitats e condições, não apenas spawns aleatórios.
5. **Risco dimensionado pela preparação:** a primeira viagem é uma decisão relevante; experiência e infraestrutura trazem consistência, não invulnerabilidade.
6. **Minecraft como fundamento:** mecânicas vanilla continuam relevantes e interagem com o conteúdo novo.

## 1.4 O que NÃO estamos construindo

- Um RPG linear com campanha obrigatória, classes fixas, moedas ou missões principais.
- Um mod em que todos os recursos só servem para Colar e Despertador.
- Um “End Game DreamsDimensions” com boss final, troféu definitivo e encerramento do conteúdo.
- Uma sequência de minérios que só produzem picaretas de maior dano ou durabilidade.
- Automação industrial total que substitua redstone, farms vanilla e logística de forma indiscriminada.
- Um Overworld inteiro tomado por anomalias que perca sua identidade original.
- Um sistema de sanidade, sono obrigatório, punição diária ou pressão constante sem benefício claro para o jogador.
- Softlocks técnicos, fuga gratuita dos sonhos, loot infinitamente renovável por reabrir baús ou progressão obrigatória condicionada a RNG impossível de controlar.

---

# 2. PILARES DE DIREÇÃO DE JOGO

## P1 — DESCUBRA ANTES DE RECEBER

**Experiência:** o mundo dá pistas de uma descoberta antes de entregar sua recompensa. Silhueta, materiais, sons, fauna e mudanças locais são linguagem de exploração.

**Exemplos aprováveis:** uma formação característica que indica depósito mineral nas proximidades; criatura com hábito de se aproximar de certos blocos; vegetação que cresce em determinada condição natural.

**Não:** mensagens indicando coordenadas de estruturas automaticamente, loot aleatório sem contexto, indicadores permanentes que resolvem a busca.

**Métrica:** jogadores percebem a pista e conseguem formular uma hipótese sem consultar Wiki; registro de quantos a identificam por observação.

## P2 — CADA RECURSO CRIA POSSIBILIDADES

**Experiência:** cada novo material possui um papel primário, uma aplicação secundária e pelo menos uma forma legítima de continuar útil mais tarde.

**Exemplos:** material mineral usado em um componente de detecção **e** em iluminação/redstone; planta usada em receitas simples **e** no processamento avançado; nova pedra com variantes de construção.

**Não:** quatro drops diferentes que fazem o mesmo trabalho ou um minério usado apenas para fabricar um item e depois descartado.

**Métrica:** usos de Survival reais, caminhos viáveis de obtenção, fontes e consumidores (“sources/sinks”) verificáveis.

## P3 — PROGRESSÃO POR CAPACIDADE, NÃO POR PREÇO

**Experiência:** cada estágio oferece uma nova capacidade, conhecimento ou abordagem. Custos crescentes podem existir, mas **mais grind não equivale automaticamente a progressão**.

**Exemplos:** passar de extração simples a processamento; de enxergar pistas a interpretá-las; de sobreviver a uma estrutura a utilizar seu mecanismo.

**Não:** dobrar custos sucessivamente sem mudar o que o jogador pode fazer.

**Métrica:** a nova etapa apresenta uma ação diferente? Há contrajogo, preparação e recompensa perceptível?

## P4 — SISTEMAS LIGADOS, CAMINHOS OPCIONAIS

**Experiência:** mineração, natureza, fauna, construção, exploração e fabricação trocam recursos entre si, mas não exigem que todo jogador complete todos os caminhos para aproveitar o mod.

**Exemplos:** mineral melhora cultivos; cultivo fornece reagente de construção; descoberta de ruína revela receita alternativa; animal deixa pistas mas não é vítima obrigatória.

**Não:** uma longa cadeia de dez sistemas todos obrigatórios para qualquer recompensa inicial.

**Métrica:** existirem diversos loops completos e ao menos dois estilos de jogo viáveis sem viagens dimensionais.

## P5 — O SOBRENATURAL É RARO, NÃO INVISÍVEL

**Experiência:** ocorrências anômalas não saturam o mapa, mas um jogador curioso encontra sinais com regularidade suficiente para perceber que o mod existe.

**Não:** elementos em todos os chunks ou tão raros que o jogador passe horas sem qualquer feedback.

**Métrica:** tempo até o primeiro sinal e primeira descoberta, medido por seed, bioma e perfil de jogador; não inferido de `spacing` ou contagem de blocos subterrâneos.

## P6 — DIFICULDADE JUSTA E CRESCIMENTO SUSTENTÁVEL

**Experiência:** o jogo pune falta de preparação e leitura incorreta dos riscos, mas respeita investimento, aprendizado e infraestrutura. Late Game oferece **regularidade** sem transformar o conteúdo em trivialidade.

**Não:** resetar conhecimento e ferramentas a cada viagem, RNG sem alternativa, morte como atalho de retorno ou tornar a exploração tediosa por exigência de repetição.

**Métrica:** progressão completa em Survival, taxa de mortes explicáveis, tempo entre expedições e recuperabilidade após perdas.

---

# 3. IDENTIDADE NARRATIVA — O MISTÉRIO É UMA MECÂNICA

## 3.1 Premissa do universo — DIRETRIZ FIXADA

**Realidade e sonhos não são mundos completamente isolados.** Existe uma ligação que deixa marcas materiais e ecológicas no Overworld. Alguns lugares guardam vestígios mais intensos, enquanto outros praticamente não apresentam alterações.

O jogador não é um “escolhido”, salvador de uma profecia ou personagem predestinado. É alguém capaz de descobrir o fenômeno por observação, exploração, construção e experimentação — dentro da liberdade típica do Minecraft.

A história será contada, principalmente, por **evidências ambientais e artefatos**, não por NPCs que despejam textos, cutscenes obrigatórias ou quests lineares.

## 3.2 Vocabulário do universo — PROPOSTO

| Conceito | Definição narrativa | Limite de utilização |
|---|---|---|
| **Limiar** | A fronteira entre o mundo desperto e a experiência onírica. | Termo guarda-chuva; não significa um terceiro mundo ou dimensão nova. |
| **Vestígio** | Marca física persistente deixada pelo contato entre os dois lados. | Pode justificar minerais, rochas, flora, microambientes e ruínas. |
| **Eco** | Manifestação repetitiva ou resposta local associada a um vestígio. | Não precisa virar recurso novo a cada uso; pode ser som, luz, partícula ou comportamento. |
| **Vigília** | Estado de preparação, observação e capacidade de reconhecer/administrar fenômenos. | Não é uma “barra de sanidade” nem punição por não dormir. |
| **Ressonância** | Princípio utilizado para explicar reações entre certos materiais e dispositivos. | Não equivale a magia genérica; exige regras simples, demonstráveis e testáveis. |
| **Observadores** | Nome provisório dado a pessoas/vestígios de uma cultura que estudou o fenômeno. | **Hipótese**, não canonizar civilização antiga antes de especificação de estruturas e narrativa. |

Os nomes são ajustáveis. **Não batizar itens apenas adicionando “Onírico” a um substantivo comum**; o nome precisa comunicar função, categoria ou origem.

## 3.3 Questões narrativas que o conteúdo deve abrir

1. Por que os primeiros minérios do mod existem debaixo do Overworld?
2. Por que alguns ambientes e seres vivos respondem à presença dos vestígios?
3. Por que certas ruínas registram o fenômeno e outras parecem abandonadas às pressas?
4. Quem compreendeu primeiro como construir artefatos para alcançar os sonhos?
5. Qual a diferença entre entrar em um sonho e conseguir despertar por vontade própria?
6. O que ainda pode ser descoberto depois que viagens frequentes se tornam possíveis?

As perguntas 1–4 podem **permanecer parcialmente abertas**. O conteúdo do Overworld não deve depender de uma explicação total do universo na versão inicial.

## 3.4 Regras de coerência para a criação de conteúdo

- **Geologia:** deve refletir processos/condições concretos no mundo: profundidade, biomas, formações, caves ou proximidade de pontos de interesse.
- **Natureza:** plantas e animais precisam de habitats, hábitos e relações ambientais identificáveis.
- **Tecnologia:** artefatos podem ser fantásticos, mas devem exibir uma relação inteligível entre insumo, processamento e efeito.
- **Ruínas:** comunicar função anterior do espaço por arquitetura; não parecem construídas apenas para armazenar um baú.
- **Anomalias:** funcionar com regras consistentes e sinais perceptíveis; evitar surpresas impossíveis de antecipar.
- **Recompensas:** devem fazer sentido em função do lugar, risco e conteúdo encontrado.
- **Mistério:** jamais exigir que o jogador leia lore externo para entender a ação necessária no jogo.

## 3.5 Técnica de narrativa ambiental

Um bom encontro tem **três camadas de interpretação**:

1. **Primeira leitura:** “Este local é diferente.” A silhueta e a composição de materiais bastam.
2. **Leitura exploratória:** “Existe uma regra aqui.” O jogador percebe uma reação, depósito ou comportamento.
3. **Leitura aprofundada:** “Consigo usar o conhecimento.” O fenômeno serve a um dispositivo, construção, cultivo ou outra exploração.

Um encontro falha se só funciona com tooltip explicativa; e também falha se a lore não produz nenhuma ação ou possibilidade nova.

---

# 4. PROGRESSÃO: EARLY → MID → LATE, SEM END GAME PRÓPRIO

## 4.1 Regra central

A progressão de DreamsDimensions **acompanha** o avanço vanilla, em vez de substituí-lo. Madeira, ferro, ouro, diamante, encantamentos, redstone e exploração vanilla seguem relevantes. O mod adiciona outras capacidades, não outra barra de nível obrigatória.

As classificações abaixo são **janelas aproximadas** de experiência, não portões artificiais baseados em tempo de jogo.

| Janela | Perfil vanilla aproximado | Fantasia DreamsDimensions | Relação com sonhos |
|---|---|---|---|
| **Early Game** | Primeira base, ferro, exploração inicial, cavernas e coleta | Reconhecer vestígios; coletar materiais; iniciar construção/natureza; experimentar mecanismos simples | Sonhos ainda não são atividade recorrente; o Overworld já deve valer a instalação do mod. |
| **Mid Game inicial** | Diamantes em progresso, redstone, encantamentos, exploração mais distante | Processar materiais; interpretar sinais; explorar estruturas intermediárias; enfrentar novos riscos | Preparação para eventuais primeiras travessias. |
| **Mid Game tardio** | Equipamento e reserva de recursos mais estáveis | Consolidar receitas e suprimentos; primeira viagem preparada; ampliar meios de recuperação | Viagens possíveis, ainda custosas e pouco frequentes. |
| **Late Game saudável** | Base consolidada, boa mobilidade, fontes de recursos e encantamentos | Sistemas de produção, construção avançada, expedições e projetos de longo prazo | Viagens tornam-se consistentes por infraestrutura conquistada, **nunca gratuitas**. |

**Não existe, nesta fase, chefe final, item definitivo, conclusão obrigatória, “fim do mod” ou recompensa que encerra a exploração.**

## 4.2 Três dimensões diferentes da progressão

- **Progresso de acesso:** onde posso ir e quais situações consigo enfrentar?
- **Progresso de eficiência:** quanto esforço preciso empregar para repetir uma atividade já dominada?
- **Progresso de expressão:** quais novas formas de construir, automatizar, cultivar e criar projetos estão disponíveis?

A árvore de progressão **não** deve conter apenas desbloqueios de acesso. Precisamos de melhorias nos três eixos — inclusive conteúdo horizontal que não conduz a uma próxima dimensão.

## 4.3 Loops por momento do jogo

### Early — descoberta e experimentação

`explorar vanilla → reconhecer um vestígio → coletar material → fabricar bloco/item útil → aplicar em base ou ferramenta → desejar investigar outra área`

Recompensa: um novo uso prático, não a obrigação de avançar para um plano dimensional.

### Mid — especialização e preparação

`explorar região/estrutura → superar risco → obter novo processo ou componente → melhorar produção/equipamento → ampliar exploração`

Recompensa: acesso a novas técnicas e opções, com custo proporcional.

### Mid tardio/Late — consistência e autonomia

`produzir recursos → preparar expedição → escolher risco/destino → retornar com descobertas → melhorar base e futuros preparativos`

Recompensa: redução de atrito por conhecimento e infraestrutura, sem tornar o perigo inexistente.

## 4.4 Regra anti-gating

**Uma progressão saudável não deve ser um corredor único de crafting.**

- Caminhos de construção e natureza precisam gerar valor próprio antes de qualquer viagem.
- Mobs ambientais não podem existir só para dropar um item obrigatório raro.
- Estruturas de lore podem oferecer receitas ou eficiência, mas conhecimento essencial precisa de uma alternativa racional, ou probabilidade de aquisição comprovadamente aceitável.
- Um equipamento de nível superior não deve anular a utilidade dos materiais anteriores.
- A perda de um recurso não pode destruir permanentemente a capacidade de voltar a progredir.
- Ressuprimento no Late Game pode ser renovável, mas deve consumir insumos e investimento.

## 4.5 Regra de interação com os sonhos existentes

- O **Colar** continua sendo o instrumento de elegibilidade para a viagem de sono, sujeito às regras já implementadas.
- O **Despertador** continua sendo o instrumento consumível de retorno confirmado.
- As **ruínas de recuperação** continuam oferecendo uma chance de obter um Despertador completo, sem fornecer Pó Onírico.
- O jogador **não** deve ganhar o direito a expedições frequentes no Early Game apenas por acumular minério.
- Não introduzir um “endgame” novo somente para justificar uma receita.
- A revisão futura de custos de Colar/Despertador deve considerar o **ecossistema completo** de recursos e não tornar esses dois itens o objetivo de todos os sistemas.

---
# 5. SEIS ECOSSISTEMAS DE GAMEPLAY DO OVERWORLD

> **Decisão de arquitetura de conteúdo:** cada pilar tem um loop independente e pelo menos duas conexões com outros pilares. Não se desenvolvem “todos os blocos primeiro, depois todos os itens”. Desenvolvem-se **fatias verticais jogáveis** contendo origem, descoberta, interação, transformação, recompensa e continuidade.

## 5.1 GEOGRAFIA, GEOLOGIA E MINERAÇÃO

**Fantasia:** o explorador aprende a distinguir o solo comum de áreas marcadas por eventos antigos. O mundo entrega sinais, não um minério mágico a cada esquina.

### Mecânicas candidatas

**A. Geologia de vestígios — PROPOSTO**  
Uma microformação natural pode reunir 2–3 blocos que indiquem uma condição subterrânea. Não precisa possuir recursos ricos, mas precisa produzir um comportamento observável ou uma informação espacial útil. Pode surgir em pequenas áreas de cavernas ou em fraturas superficiais, com a densidade calibrada separadamente por bioma/altura.

**B. Depósitos com identidade — PROPOSTO**  
Materiais têm distribuição explicável. Um recurso associado a antigas rochas profundas não deve aparecer com igual probabilidade em toda a superfície. O jogador pode aprender a diferenciar depósitos pelas rochas ao redor, por sinais indiretos ou por flora associada.

**C. Extração + processamento — PROPOSTO**  
Nem todo recurso avançado deve dropar a forma final. Podemos diferenciar minério, fragmento bruto e componente refinado **apenas quando isso habilitar uma ação nova**. Evitar criar três itens para a mesma etapa de um craft sem interação.

**D. Mineração útil à construção — DIRETRIZ FIXADA**  
Toda família geológica importante deve considerar bloco de base e variantes de construção, quando fizer sentido, mesmo que sua função principal seja um material de fabricação.

### Integração com o código existente

Os registros `dream_ore`, `deepslate_dream_ore` e `dream_dust` já possuem uma cadeia de worldgen/loot/crafting. A Task 5.6 confirmou sua geração em quatro seeds, mas **contagens de voxels não equivalem a tempo de mineração do jogador**. Não devemos alterar sua raridade apenas para abrir espaço a minérios novos, sem medir a economia real.

### Guardrails

- Nova geologia apenas em chunks novos; preservar Overworld vanilla e construções existentes.
- Veios/distribuições revisados em várias seeds com contagem de blocos **e** medida de recursos acessíveis.
- Drop com Fortune/Silk Touch, tiers de mineração, experiência e tags corretas.
- Não gerar o mesmo recurso em cinco variantes apenas para inflar catálogo.
- Não espalhar recursos dimensionais exclusivos pelo Overworld sem justificativa de lore.

## 5.2 NATUREZA, CULTIVO E RECURSOS ORGÂNICOS

**Fantasia:** o contato com os sonhos não altera somente pedra; há formas de vida discretas que se adaptaram aos vestígios.

### Mecânicas candidatas

**A. Flora indicadora — PROPOSTO**  
Certas plantas prosperam em solos ou ambientes com vestígios. Elas ensinam o jogador a observar o terreno. A ocorrência vegetal **não garante** um tesouro abaixo dela; indica probabilidade ou condição.

**B. Cultivo contextual — PROPOSTO**  
Uma pequena linha agrícola que depende de condições naturais verificáveis (luz, bloco de solo, proximidade de material, temperatura/bioma) e pode ser cultivada pelo jogador após aprender suas necessidades. Não introduzir uma estação sazonal global ou agricultura impossível de compreender.

**C. Recursos vegetais processáveis — PROPOSTO**  
Fibras, resinas e sementes podem alimentar decoração, preparo de suprimentos, componentes e mecanismos. A colheita repetível sustenta o Late Game, mas precisa de custo, tempo e limitação de crescimento claros.

**D. Renovabilidade cuidadosa — DIRETRIZ FIXADA**  
Uma planta renovável não deve gerar passivamente recursos raros suficientes para anular mineração profunda, exploração ou desafios. Separar materiais de **manutenção** dos de **desbloqueio**.

### Integração entre sistemas

Flora pode sinalizar geologia, mob pode dispersar sementes, estação de processamento pode utilizar resina ou fibra, construtor pode transformar os mesmos recursos em blocos estéticos e iluminação.

### Guardrails

- Utilizar regras vanilla de suporte, luz, bonemeal e growth quando coerentes; comportamento novo só com justificativa.
- Evitar buffs alimentares genéricos que invalidem comida vanilla.
- Não criar planta obrigatória de uma única estrutura raríssima que impeça avanço normal.

## 5.3 FAUNA, CRIATURAS E INTERAÇÕES

**Fantasia:** o Overworld apresenta organismos e guardiões adaptados à presença dos vestígios. Suas ações importam mesmo quando não oferecem drops.

### Três papéis de mob — PROPOSTO

1. **Indicador ambiental:** comportamento e posição sugerem um fenômeno. Jogador ganha conhecimento por observação; abater o mob não é o melhor meio de interagir.
2. **Habitante ecológico:** criatura com hábitos, reprodução/atração ou interação com recursos; fonte renovável moderada, não farm de itens essenciais raríssimos.
3. **Protetor territorial:** defende recursos, ninho ou estrutura; telegráfa ameaça, oferece espaço para fuga, furtividade, preparação ou confronto.

### Expectativas de inteligência artificial

- Spawn em habitats reais, não em pontos arbitrários perto de jogadores.
- Comportamento com no máximo alguns estados claramente distinguíveis: repouso, forrageio/observação, alerta, defesa/fuga, recuperação.
- Sons, partículas e animações comunicam o estado sem depender de tooltip.
- Loot nunca é a única razão de existir da criatura.
- Políticas de população, cooldown e navegação devem reutilizar a experiência adquirida com Fragmento Errante e Vigia Adormecido, **sem assumir** que regras de ilhas servem automaticamente para Overworld.
- GeckoLib quando a animação expressiva justificar; comportamento simples pode usar recursos vanilla.

### Guardrails

Não criar IA de varredura global, mobs que destroem construções de jogador de maneira imprevisível, spawns inevitáveis em bases pacíficas ou inimigos que dependem de invulnerabilidade artificial. Evitar penalizar excessivamente jogadores construtores ou pacifistas.

## 5.4 ESTRUTURAS, PONTOS DE INTERESSE E DESCOBERTAS

**Fantasia:** lugares carregam indícios de funções passadas. A arquitetura deixa o jogador formular perguntas: “que lugar era este, quem o usava e por que foi abandonado?”

### Escalas propostas

| Tipo | Escala | Papel primário | Recompensa admissível |
|---|---|---|---|
| **Marco ambiental** | Pequeno | Reforçar linguagem de pistas e geologia. | Informação ambiental, material de baixo valor; baú não obrigatório. |
| **Refúgio/atelier abandonado** | Médio | Convidar à investigação, abrigar espaços funcionais. | Suprimentos, materiais de construção e chance de método/variante de receita. |
| **Galeria ou estação de pesquisa** | Médio/grande | Exploração por salas, detalhes arquitetônicos, mecanismos. | Conhecimento tangível, recursos específicos, ferramentas situacionais. |
| **Câmara protegida** | Rara | Desafio opcional exigente e recompensa de longo prazo. | Componente ou método avançado; nunca equipamento final gratuito em cada baú. |

### Regras de design de estrutura

Cada POI precisa de: silhueta memorável, entrada/rota coerente, propósito arquitetônico, uma decisão de exploração, uma recompensa temática, integração ao terreno, segurança de geração, indicadores de que foi visitado e comportamento correto em multiplayer.

Ruínas não são todas iguais: uma câmara subterrânea não deve ser um pátio quadrado enterrado, e um posto de observação não deve ser apenas um baú no centro de uma torre.

### Guardrails

- Preferir estruturas nativas com `/locate` para diagnóstico e datapacks configuráveis.
- NBT real; loot table única e persistente; nada de “renovar ao reabrir”.
- Não gerar POIs em regiões já exploradas de saves antigos por padrão.
- Segurança do terreno validada em servidor real; altura e estrutura registrada **não** comprovam acessibilidade.
- Estruturas antigas do Dreamscape/Campo Azul não devem ser copiadas para Overworld como se fossem novos locais.

## 5.5 CRAFTING, ESTAÇÕES E EQUIPAMENTOS

**Fantasia:** materiais desconhecidos ganham utilidade quando o jogador aprende a processá-los e integrar novos componentes ao que já construiu.

### Papel de cada categoria

- **Ingrediente bruto:** extraído ou colhido; deve possuir finalidade clara.
- **Material processado:** permite uma receita, acabamento ou transformação diferente — não só renomeia o material bruto.
- **Estação de trabalho:** criada apenas quando a bancada/fornalha/stonecutter/smithing vanilla não oferecem a interação necessária.
- **Ferramenta especializada:** altera abordagem, precisão, informação, segurança ou mobilidade; não é só “+1 dano”.
- **Consumível:** oferece escolha contextual e custo renovável proporcional ao benefício.
- **Acessório:** enriquece builds, sem substituir armadura vanilla nem criar obrigação permanente por slot.
- **Componente avançado:** pertence a mais de uma receita, salvo se a exclusividade for justificada por progressão excepcional.

### Regras de usabilidade

A maioria das primeiras receitas deve usar crafting vanilla. Uma estação nova precisa justificar **pelo menos duas aplicações independentes**, UX legível, compatibilidade multiplayer, receita descoberta e processo repetível. Nunca inventar bancada obrigatória para a obtenção de um único item se isso criar atrito sem benefício.

### Guardrails
- Não reescrever receitas existentes automaticamente.
- Componentes importantes devem ter tooltips objetivos e Recipe Book/advancements coerentes.
- Evitar itens de uso único com efeito imperceptível.
- Toda capacidade desbloqueada deve ser demonstrável em um teste de Survival.

## 5.6 CONSTRUÇÃO, DECORAÇÃO E MECANISMOS

**Fantasia:** o mod transforma bases e construções mesmo para jogadores que não exploram dimensões.

### Linhas propostas

**A. Famílias arquitetônicas completas.** Bloco, laje, escada, muro e variações lapidadas quando combinarem com o material. Diferenciar acabamento por textura e silhueta, não por dez cores sem função.

**B. Iluminação contextual.** Lâmpadas ou blocos que exibem variações de intensidade, relação com ambiente ou redstone. O mecanismo precisa ser controlável e não causar spam de atualizações.

**C. Detectores e atuadores.** Blocos responsivos a condições próximas (presença de outro bloco, vibração válida, luz ou estado de anomalia), com uma saída de redstone compreensível. Não construir nova rede elétrica.

**D. Componentes para produção.** Blocos que permitem pequenos processos especializados e se integram à logística vanilla, sem anular fornalhas, funis e redstone.

### Guardrails

- Identidade visual Minecraft: formas legíveis a alguns blocos de distância, textura compatível com o mundo e boa relação com blocos vanilla.
- Blocos decorativos podem existir **sem função extra** quando oferecem valor estético real e pertencem a uma família de materiais útil.
- Não forçar animações complexas em centenas de blocos.
- Respeitar eficiência de chunk updates, redstone, block entities e pathfinding.

---

# 6. PROPOSTA DE CATÁLOGO CONCEITUAL V1 — NÃO APROVADO PARA CÓDIGO

Esta seção demonstra a **largura possível do universo**, além de Colar/Despertador. Os nomes são provisórios; não criar itens só porque aparecem nesta tabela. Cada candidato deve passar pelo **filtro de conteúdo** da seção 9 e ser desdobrado nas Tasks 6.2–6.6.

## 6.1 Geologia, plantas e construção

| Código conceitual | Família/candidato | Early/Mid/Late | Fonte de obtenção | Primeira função | Utilidade adicional e continuidade | Dependência / risco |
|---|---|---|---|---|---|---|
| GEO-01 | **Pedra Marcada** | Early | Microformações em rocha vanilla | Pista visual natural de vestígios. | Família arquitetônica de aparência desgastada. | Risco de geração excessiva e ruído visual. |
| GEO-02 | **Nódulo de Reverberação** | Early→Mid | Pequenos bolsões geológicos próprios, independentes do Dream Ore | Drop de fragmento para blocos reativos. | Reagente em mecanismos e iluminação. | Provar diferença funcional em relação ao Pó Onírico. |
| GEO-03 | **Fragmento Reverberante** | Early→Late | Nódulos, extração definida em loot | Material para sensor simples. | Refinado em aparelhos e acabamentos reativos. | Evitar moeda universal para tudo. |
| GEO-04 | **Cristal Velado** | Mid→Late | Depósitos profundos/estruturas compatíveis | Componente de dispositivos de precisão. | Reagente de contenção e construção avançada. | Não criar tier “melhor que diamante” sem mecânica. |
| GEO-05 | **Pedra de Contenção** | Mid→Late | Processamento de rocha + reagentes | Material para mecanismos de estabilização local. | Família de construção resistente e decorativa. | Limite espacial/efeito claro. |
| FLR-01 | **Líquen dos Vestígios** | Early | Rocha úmida/entrada de cavernas com condições específicas | Sinal de ambiente alterado. | Item decorativo e ingrediente modesto. | Não confundir com `dream_glow_moss` existente nas dimensões. |
| FLR-02 | **Hera do Repouso** | Early→Mid | Pequenas manchas naturais; cultivo após aprendizagem | Fibra e elementos decorativos. | Ligação com componentes e suportes de construção. | Precisa de condição de crescimento testável. |
| FLR-03 | **Flor de Vigília** | Mid→Late | Manchas raras e cultivo controlado | Ingrediente de preparação situacional. | Fonte renovável para manutenção de dispositivos. | Não gerar buff genérico permanente. |
| FLR-04 | **Fungo do Subsolo** | Early→Mid | Cavernas escuras, com habitat próprio | Alimento/ingrediente situacional. | Blocos naturais decorativos e processamento futuro. | Não competir gratuitamente com cogumelos vanilla. |
| BLD-01 | **Pedra Marcada Lapidada** | Early | Stonecutter ou processamento vanilla de GEO-01 | Blocos de base, laje, escada, muro. | Arquitetura de ruínas e bases. | Família coerente; não dezenas de variantes vazias. |
| BLD-02 | **Cristal Luminoso Trabalhado** | Mid | Recursos minerais/vegetais | Luz controlável. | Detector/atuador de redstone em projeto futuro. | Efeito especial não pode ser só visual. |
| BLD-03 | **Painel de Ressonância** | Mid | Fragmento Reverberante + recursos vanilla | Sinal de redstone sob condição definida. | Automação de base e observação ambiental. | Definir evento, alcance e estados sem polling pesado. |
| BLD-04 | **Parede de Contenção** | Late | Pedra de Contenção refinada | Redução local de um efeito contextual, se existir. | Construção de postos experimentais. | Só implementar depois de uma ameaça real e bem definida. |

**Nota de escopo:** GEO-02 e GEO-04 não são aprovados automaticamente como *novos minérios*. A Task 6.2 deve avaliar se blocos naturais, cristais de formação, drops de estruturas ou transformações de material vanilla resolveriam melhor o mesmo problema. **Quantidade de novos minérios não é um KPI.**

## 6.2 Itens, ferramentas e estações

| Código | Candidato | Estágio | Loop principal | Segunda aplicação | Condição de aprovação |
|---|---|---|---|---|---|
| EQP-01 | **Sonda de Vestígios** | Early/Mid | Interagir com material próximo para revelar uma propriedade local observável. | Ferramenta de laboratório e inspeção de construção. | Não fornecer GPS; útil sem abrir dimensão. |
| EQP-02 | **Lente de Observação** | Mid | Interpretar pistas visuais e identificar materiais reativos por proximidade. | Melhorar leitura de ruínas e blocos de mecanismo. | Não duplicar a Sonda: só uma sobrevive se os loops forem iguais. |
| EQP-03 | **Kit de Amostragem** | Mid | Coletar pequena amostra de um bloco ou ambiente sem destruí-lo. | Catalogação ou receita de dispositivo, se houver demanda real. | Não virar quest de “fotografar 20 objetos”. |
| EQP-04 | **Ampola de Estabilização** | Mid/Late | Consumível de proteção contextual limitada. | Suporte à exploração vanilla mais perigosa. | Só criar se existir risco específico e telegráfico. |
| EQP-05 | **Marcador de Expedição** | Mid/Late | Orientação local sem mapa automático global. | Suporte a exploração repetida de locais já descobertos. | Não substituir cama, bússola, mapa ou teleporte. |
| EQP-06 | **Instrumento de Medição** | Mid/Late | Comparar intensidade de fenômenos em poucos pontos. | Calibrar mecanismos de redstone. | Uso intuitivo; sem UI complexa ou planilha obrigatória. |
| STA-01 | **Mesa de Estabilização** | Mid→Late | Transformação material com dois insumos diferentes e condição identificável. | Preparo de consumíveis e componentes de construção. | Deve servir no mínimo duas famílias independentes. |
| STA-02 | **Banco de Instrumentos** | Mid | Montagem de equipamentos com componentes modulares. | Reparar/reconfigurar algum instrumento existente. | Só criar se crafting vanilla e smithing forem insuficientes. |
| STA-03 | **Módulo de Contenção** | Late | Preparar pequenas áreas para estudo de anomalias. | Alimentar mecanismo arquitetônico. | Depende de anomalia de gameplay, não de lore isolada. |

**Decisão importante:** os três instrumentos EQP-01, EQP-02 e EQP-06 são **alternativas a comparar**, não três itens já aprovados. É melhor **um único instrumento excelente**, com evolução de utilidade, do que três ferramentas que apenas brilham quando estão perto de minério.

## 6.3 Fauna e encontros

| Código | Candidato | Habitat e papel | Comportamento distintivo | Recompensa do encontro | Risco de design |
|---|---|---|---|---|---|
| MOB-01 | **Mariposa dos Vestígios** | Clareiras/entradas de caverna; indicador neutro | Prefere determinada flora e altera rota ao se aproximar de marcas ambientais. | Observação e orientação; coleta não violenta opcional. | Não criar AI que vasculhe blocos globalmente. |
| MOB-02 | **Habitante das Fendas** | Cavernas; criatura neutra territorial | Reage à iluminação e defende pequeno território/ninho. | Interação ecológica, recurso comum renovável com limite. | Design biológico/voxel precisa de identidade própria. |
| MOB-03 | **Guardião de Pedra** | Estrutura específica; defensor | Alerta progressivo, zona protegida, ataques evitáveis e períodos de exposição. | Permite explorar e recuperar materiais/contexto da estrutura. | Não ser só mais um zumbi com muita vida. |
| MOB-04 | **Observador Esquecido** | Hipótese de ambiente avançado | Presença rara e não necessariamente hostil. | Descoberta narrativa ambiental. | Não canonizar uma civilização/NPC antes de decidir o lore. |

**Importante:** nenhum mob novo do Overworld está registrado no HEAD consultado. Fragmento Errante e Vigia Adormecido pertencem ao Dreamscape e não serão usados como fauna comum do Overworld por conveniência.

## 6.4 Estruturas do Overworld

| Código | Família | Escala | O que o jogador faz | Recompensa coerente | Perigo/contrajogo |
|---|---|---|---|---|---|
| POI-01 | **Marco de Pedra Marcada** | Pequena | Identifica uma sequência de pistas geológicas. | Informação, materiais baixos e arquitetura reconhecível. | Quase nenhum; primeira descoberta. |
| POI-02 | **Abrigo do Observador** | Média | Explora um antigo local de estudo, interpreta disposição de objetos. | Receitas alternativas, recursos mistos, projeto construtivo. | Inimigos vanilla ou obstáculos de layout situacionais. |
| POI-03 | **Galeria Afundada** | Média | Desce por salas/fendas e encontra depósitos/espaços de trabalho. | Materiais intermediários e pistas sobre outro tipo de local. | Queda, escuridão, rotas, mobs apropriados. |
| POI-04 | **Oficina Abandonada** | Média | Encontra demonstração de uso de estação/mecanismo. | Componentes, documentação ambiental, pequena chance de peça rara. | Aprendizado experimental; não trancar crafting básico num RNG. |
| POI-05 | **Câmara de Contenção** | Grande, rara | Identifica um mecanismo, desativa ou evita defesa, alcança recompensa. | Reagente avançado ou possibilidade nova de montagem/construção. | Guardião/ambiente com sinais de perigo e alternativas. |
| POI-06 | **Sítio de Reverberação** | Formação especial | Estuda fenômeno repetível sem necessariamente combater. | Informação para encontrar outro recurso/atividade. | Localização e entendimento, não boss. |

**Não são seis estruturas aprovadas para implementação imediata.** Uma primeira fatia pode usar apenas GEO-01, GEO-02, BLD-01, um POI-01 e uma transformação útil. A complexidade cresce conforme o loop provar valor.

---

# 7. LOOPS TRANSVERSAIS — EXEMPLOS QUE JUSTIFICAM CONTEÚDO

Os exemplos abaixo não são receitas oficiais. São **testes de coerência** de um universo em que cada mecânica participa de mais de um caminho.

## Loop A — geologia → construção → redstone (Early/Mid)

1. O jogador reconhece a assinatura de Pedra Marcada em uma caverna.
2. Extrai um Nódulo de Reverberação; encontra Fragmento Reverberante.
3. Usa parte do material para criar um bloco construtivo/iluminação diferenciada.
4. Mais tarde utiliza o mesmo recurso num Painel de Ressonância com uma condição de ativação legível.
5. Integra o painel em uma construção ou em um sistema de redstone, adquirindo motivo para continuar coletando esse recurso no Late Game.

**Valor:** construção e automação antes de qualquer viagem. **Falha:** o painel não faz nada além de substituir uma placa de pressão sem outra característica relevante.

## Loop B — natureza → fauna → agricultura → processamento (Early/Late)

1. O jogador descobre uma planta rara associada a rocha úmida.
2. Observa que uma criatura visita o local em horários ou condições específicos.
3. Aprende a reproduzir uma parcela da planta na própria base, respeitando condições de crescimento.
4. Colhe um ingrediente renovável e usa em suprimentos ou processamento.
5. Integra planta, criatura e construção paisagística em seu espaço.

**Valor:** existe para naturalistas e construtores. **Falha:** a criatura não muda nada, ou cultivar substitui inteiramente a exploração de materiais raros.

## Loop C — exploração → ruína → nova técnica → novos projetos (Mid)

1. Um padrão de superfície sugere a existência de um local abandonado.
2. O jogador encontra uma Oficina Abandonada com pistas de uma técnica.
3. A descoberta dá acesso a uma **alternativa** de fabricação, aprimoramento ou componente — não necessariamente a um item exclusivo impossível de obter por outro caminho.
4. O jogador monta uma versão própria do mecanismo e a utiliza em outro projeto.
5. Regressar ao local pode ter valor visual ou de construção, mas **o baú não se renova**.

**Valor:** exploração muda o que o jogador consegue fazer. **Falha:** ruína é somente loot aleatório.

## Loop D — preparação → sonho → retorno → Overworld (Mid tardio/Late)

1. O jogador, com recursos e conhecimento, prepara uma expedição usando materiais do Overworld.
2. Equipa o Colar e usa a mecânica de sono existente quando habilitada pelas condições de teste/release.
3. Explora uma dimensão com riscos reais, recursos e ruínas de recuperação.
4. Volta através do Despertador, que é consumido somente após retorno confirmado.
5. Traz materiais/conhecimento que ampliam possibilidades na base e melhora sua capacidade de preparar a próxima viagem.

**Valor:** viagens consistentes são a consequência de um Overworld já rico e de infraestrutura acumulada; **não** são a única atividade valiosa. **Falha:** o jogador precisa refazer toda a progressão desde o início a cada retorno ou consegue viajar sem custo algum.

## Loop E — perigo → observação → preparação → recompensa (Mid/Late)

1. Uma Câmara de Contenção anuncia risco através de forma, som, sinais e ambiente.
2. O jogador pode escolher evitar, preparar consumíveis, investigar um mecanismo ou enfrentar um Guardião.
3. A recompensa permite novas construções ou ferramentas, não apenas uma arma com dano maior.
4. O conteúdo permanece opcional para quem prefere agricultura, construção e mineração.

**Valor:** desafio que depende de leitura e preparação. **Falha:** dano inevitável, boss-sponge ou recompensa obrigatória para qualquer ação básica.

## Teste obrigatório de cada loop

Responder: **Por que alguém iniciaria esta atividade? O que aprende? Que escolha faz? Que recompensa visível recebe? Que nova atividade passa a desejar? O loop continua valendo depois do Mid Game?**

Se não conseguirmos responder, a ideia volta para a prancheta em vez de receber um ID de item.

---
# 8. BASELINE REAL DO MOD — O QUE JÁ TEMOS E COMO EXPANDIR

A baseline a seguir foi auditada no HEAD indicado neste documento. **A direção nova deve partir do existente, mas não ficar limitada por ele.** O código do repositório prevalece sobre seções históricas dos documentos que ainda descrevem um comportamento anterior à refatoração.

## 8.1 Blocos e itens existentes — EXISTENTE / AUDITADO

**Registros de blocos:** 17 blocos, incluindo:

- Solos/rochas: `dream_grass_block`, `blue_dream_grass`, `dream_dirt_block`, `blue_dream_dirt`, `dream_sand_block`, `serene_stone`, `blue_dream_stone`, `blue_dream_cobblestone`.
- Flora/ambiente: `dream_flower_block`, `dream_log_block`, `dream_leaves_block`, `dream_glow_moss`.
- Recurso e construção: `dream_shimmer_block`, `dream_infused_stone`, `oneiric_core_block`.
- Minérios do Overworld: `dream_ore`, `deepslate_dream_ore`.

**Itens próprios relevantes:** `dream_dust`, `dream_necklace`, `oneiric_awakener` e os BlockItems. O Colar usa Curios; o Despertador é consumível após retorno confirmado. As receitas existentes são seis JSONs, incluindo as duas receitas de Blue Dream Stone por forno/alto-forno.

**Criaturas registradas:** `fragmento_errante` e `vigia_adormecido`, ambas associadas ao Dreamscape e seus habitats de ilhas; existe o efeito `pacto_de_vigilia`. **Não há uma família de mobs próprios do Overworld no registro atual.**

**Dimensões registradas:** `dreamsdimensions:dreamscape` e `dreamsdimensions:campo_onirico_azul`. A Task 6.0 não acrescenta ou redesenha essas dimensões.

**Estruturas nativas atuais:** `dreamsdimensions:blue_dream_ruins` (Campo Azul) e `dreamsdimensions:fragmented_dream_ruins` (Dreamscape), com NBTs, Structure Sets e loot tables próprios. Nenhuma delas é uma estrutura natural do Overworld.

## 8.2 Economia existente — referência técnica, NÃO balanceamento novo

- `dream_ore` e `deepslate_dream_ore` geram naturalmente no Overworld. A Task 5.6 mediu **11.236 voxels** de minério em **1.024 chunks amostrados** em quatro seeds; **324** estavam adjacentes a ar (**2,88%**). Isso não mede tempo para minerar ou coletar Pó em Survival.
- O Colar atual consome **3 Dream Dust** na receita, além de cobre e ametista; o item não é consumido na viagem.
- O Despertador atual consome indiretamente **12 Dream Dust** por meio das pedras infundidas e do núcleo, além de itens vanilla; uma unidade é consumida após retorno válido.
- As duas ruínas dimensionais configuram **25% de chance** de conter **um** Despertador completo em cada baú, sem `dream_dust`.
- A Task 5.7 comprovou via CI uma ruína com um baú válido por dimensão em cada uma de quatro seeds. Uma ocorrência no Dreamscape apresentou apenas **4/9 apoios locais** no critério conservador de segurança do relatório; não declarar segurança geométrica total a partir do `/locate`.
- O relatório do repositório registra `dream_entry_enabled=false` como **padrão de proteção**; o usuário informou ter homologado o ciclo de exploração em seus testes. A Bible não altera essa configuração nem confunde homologação relatada do usuário com um novo resultado automatizado.

## 8.3 Contratos de compatibilidade que não podemos quebrar

1. IDs de blocos, itens, entidades, dimensões, attachments e receitas existentes permanecem estáveis sem plano explícito de migração.
2. A mecânica de entrada pelo Colar permanece server-side, elegível por sono/slot conforme sistema existente.
3. Despertador continua com uso de 60 ticks e consumo de uma unidade após retorno confirmado; falhas não consomem item.
4. Morte em sonho não concede retorno gratuito; respawn deve obedecer às políticas existentes.
5. Dream Dust continua tendo como fonte mineral natural o Overworld; não introduzir geração automática de Pó nas dimensões sem revisão formal.
6. Loot de baús deve permanecer persistente por mundo, sem reroll por jogador, reabertura ou chunk reload.
7. Novos sistemas de worldgen não podem regenerar chunks antigos nem interferir silenciosamente em density functions de outras dimensões.
8. Sistemas de mobs existentes fornecem padrões de reuso, não uma justificativa para adicionar o mesmo mob a qualquer dimensão.

## 8.4 Decisões de refatoração que NÃO serão feitas na Task 6.0

- Nenhum rebalance de `dream_dust` ou de receitas do Colar/Despertador.
- Nenhuma mudança nas probabilidades da entrada por sono.
- Nenhuma alteração no loot dimensional de recuperação.
- Nenhum novo item ou bloco colocado em registro Java.
- Nenhum novo mob, textura, modelo GeckoLib, NBT ou structure set.
- Nenhuma alteração nos arquivos existentes de save ou nos scripts de CI.

Mudanças futuras serão avaliadas na Task 6.3/6.6 e implementadas com uma task dedicada, incluindo migração/compatibilidade quando necessária.

---

# 9. GATE DE APROVAÇÃO: TODO NOVO CONTEÚDO PRECISA DE UMA RAZÃO

Antes de registrar algo no código, a equipe deve preencher uma **Ficha de Conteúdo**. Uma ideia entra no backlog como **CANDIDATA**, não como aprovada.

## 9.1 Ficha obrigatória

```yaml
id_conceitual: "GEO-02"
nome_provisorio: "Nódulo de Reverberação"
status: "PROPOSTO"
pilar_primario: "Geologia e mineração"
pilares_conectados: ["Construção", "Mecanismos"]
fantasia_do_jogador: "Aprender a identificar uma formação incomum"
problema_de_gameplay: "Falta descoberta geológica com aplicação fora de viagens"
estagio: "Early -> Mid"
localizacao: "Overworld; cavernas, critérios ainda a validar"
forma_de_descoberta: "Assinatura visual e distribuição geológica"
forma_de_obtencao: "Mineração, tier e loot a especificar"
interacao_principal: "Extração de componente para reações de bloco"
recompensa_imediata: "Fragmento com uso inicial demonstrável"
uso_secundario: "Iluminação e redstone"
uso_no_late_game: "Mecanismos e construção recorrentes"
renovabilidade: "Finito no chunk; novas regiões produzem novos depósitos"
dependencias: ["Bloco/modelo", "Loot", "Recipe", "Worldgen", "Testes"]
risco_tecnico: "Gerar excessos ou não conseguir ver o depósito em Survival"
risco_de_design: "Duplicar o papel de dream_dust"
criterio_de_aceite: "Jogador encontra, minera, fabrica e usa em duas aplicações"
status_qa: "NOT RUN"
```

**O exemplo NÃO autoriza esse minério.** É um contrato de decisão. O formato exato da ficha e seus IDs devem ser fixados na Task 6.2.

## 9.2 Perguntas eliminatórias

Uma proposta deve ser redesenhada ou rejeitada se a resposta para qualquer questão essencial for **não**:

1. Dá ao jogador uma ação, escolha, sensação ou expressão significativa?
2. Pertence ao universo visual/narrativo do DreamsDimensions?
3. Possui origem plausível e obtenção testável em Survival?
4. Sua utilidade é distinta do que já existe em vanilla ou no próprio mod?
5. Continua útil em pelo menos um estágio posterior ou cumpre uma função estética claramente relevante?
6. Há uma forma coerente de balancear raridade, custo, manutenção e perda?
7. Temos uma fatia vertical para provar a experiência sem criar 20 pré-requisitos?
8. Preserva multiplayer, saves e performance dentro do custo previsto?

### Exceção legítima: conteúdo decorativo

Um bloco decorativo **pode** ter como função principal apenas a arquitetura. Nesse caso precisa cumprir três condições: identidade visual forte, pertença a uma família ou tema coerente e disponibilidade adequada para construções. Não inventar efeito mágico para justificar cada laje.

## 9.3 Indicadores qualitativos de sucesso

- **Descoberta:** o jogador vê, interpreta e responde a uma pista.
- **Utilidade:** consegue demonstrar a vantagem com um comando ou cenário de Survival claro.
- **Expressão:** algo novo pode ser construído ou decorado com qualidade perceptível.
- **Rejogabilidade:** novos biomas/seeds/projetos criam novas oportunidades, não apenas repetição obrigatória.
- **Coesão:** o conteúdo não parece importado de outro mod sem adaptação.
- **Saúde econômica:** recursos antigos continuam circulando.

## 9.4 Matriz simples de decisão

Pontuar cada proposta de 0 a 3 em **utilidade**, **coesão**, **reutilização sistêmica**, **clareza ao jogador** e **custo/risco técnico** (nesta última, 3 significa custo baixo). Pontuação máxima 15; **alvo inicial 11** para entrar em protótipo, mas nenhuma pontuação substitui os eliminatórios. Registrar uma frase justificando cada nota.

---

# 10. ECONOMIA, RARIDADE E BALANCEAMENTO

## 10.1 Classificar recursos pela função, não só pela cor

| Categoria | Papel | Origem desejada | Renovabilidade padrão | Exemplo conceitual |
|---|---|---|---|---|
| **Base construtiva** | Permitir construção sem grind excessivo. | Mineração/coleta fácil. | Abundante ou cultivável. | Pedra Marcada lapidada. |
| **Reagente de manutenção** | Alimentar equipamentos, experimentos e consumíveis. | Coleta/mineração/cultivo. | Sustentável após investimento. | Fibra, extrato vegetal. |
| **Material de descoberta** | Recompensar exploração e leitura de ambiente. | Formações, estruturas, ecossistemas. | Finito local, disponível em novas regiões. | Fragmento de formação especial. |
| **Componente de desafio** | Recompensar preparação e domínio de risco. | Locais perigosos ou processos difíceis. | Limitado; múltiplas fontes quando obrigatório. | Cristal avançado. |
| **Conhecimento/técnica** | Abrir alternativa de uso ou produção. | Observação, avanço de sistemas, ruína, experimento. | Aprendido uma vez quando apropriado. | Método de estabilização. |
| **Recompensa cosmética** | Dar identidade a projetos e coleções. | Craft, loot temático, atividades. | Varia conforme a raridade visual. | Acabamento antigo. |

### Regra importante

**Material raro não precisa ser o mais poderoso.** Pode ser especializado, estético ou oferecer uso horizontal. Não estabelecer uma escada obrigatória “cobre onírico → ferro onírico → diamante onírico → netherita onírica”.

## 10.2 Fonte, transformação e consumo

Cada material deverá ter um mapa `origens → transformação → saídas → uso/consumo`.

| Situação | Diretriz |
|---|---|
| Recurso aparece naturalmente | Registrar em que dimensão/bioma/faixa Y/estrutura e como é encontrado. |
| Drop de mob | Não tornar criatura pacífica a fonte obrigatória de progressão avançada por farm repetitivo. |
| Produção renovável | Exigir investimento real ou espaço/tempo de cultivo e preservar valor de exploração. |
| Receita intermediária | Reduzir multiplicações artificiais sem novo comportamento de gameplay. |
| Item consumível | Repetível no Late Game com custo identificável; não depender de recomeçar do zero. |
| Loot raro | Persistente, com taxa real medida; não garantir o essencial em um único sorteio. |
| Recurso finito por chunk | Provar que novos chunks mantêm possibilidade de obtenção em escala adequada. |

## 10.3 Raridade não significa apenas “chance baixa”

Raridade efetiva = **frequência da origem × chance de sucesso do worldgen × visibilidade × acessibilidade × risco da região × custo de extração × quantidade por ocorrência**.

Para estruturas, medir também distância de deslocamento real, barreiras e possibilidades de encontrar sem conhecer a seed. Para minérios, não confundir `count` de placed feature com quantidade minerada ou descoberta.

**Não fixar agora odds finais** para novos minérios, mobs ou estruturas. A Task 6.8 deverá propor distribuições, medir em quatro seeds como ponto de partida, revisar em mapas maiores e adicionar playtests de verdade.

## 10.4 Direção de dificuldade por estágio

| Elemento | Early | Mid | Late |
|---|---|---|---|
| Materiais comuns | Acessíveis, apresentação clara | Ainda consumidos em grandes projetos | Produção de escala viável |
| Minérios especiais | Descoberta eventual | Busca dirigida e processamento | Utilidade contínua, alguns ainda escassos |
| Estruturas | Pequenas e relativamente legíveis | Exigem leitura, combate/preparação | Grandes/complexas, mas opcionais |
| Mobs | Comunicam ecossistema | Ameaça/interação diferenciada | Domínio por conhecimento, não imunidade |
| Crafting | Majoritariamente vanilla | Transformações especiais pontuais | Repetição eficiente via infraestrutura |
| Sonhos | Pistas e motivação | Primeiras expedições com risco e custo | Frequência estável e planejada, sem custo zero |

## 10.5 O Colar e o Despertador não controlam toda a economia

A base atual deve permanecer funcional até existir proposta explícita de migração. A revisão de receitas, quando chegar, deve perguntar:

- **A progressão do Overworld entrega valor sem o Colar?** Se não, o desenho está errado.
- **O Colar é um marco significativo no Mid Game?** Não precisa ser um boss key nem moeda de grind.
- **O Despertador representa custo de expedição administrável no Late Game?** Deve continuar relevante, mas não consumir horas repetitivas de trabalho já dominado.
- **Existe alternativa de recuperação exploratória para perdas?** As ruínas dimensionais mantêm esse papel, sem virar farm garantida.
- **Outros novos recursos participam de construção, cultivo, mecanismos, combate e exploração?** Se não, a economia ficou estreita demais.

## 10.6 Medições propostas (alvos experimentais, não dados comprovados)

Na Task 6.8, definir alvos e amostras por estágio. Medir pelo menos:

- Tempo até o primeiro contato espontâneo com conteúdo do mod em um mundo novo.
- Tempo desde o primeiro contato até a primeira **ação útil completa** (obter + fabricar + usar).
- Tempo de obtenção de um material novo com e sem conhecimento prévio.
- Quantidade de locais visitados até encontrar uma estrutura de cada família.
- Frequência de encontros com criaturas por bioma e ciclo de dia/noite.
- Esforço para sustentar uma unidade de consumível de manutenção no Mid/Late Game.
- Progressão de duas pessoas com perfis distintos (explorador e construtor), em single-player e multiplayer.
- Distância real percorrida, mortes, recursos gastos e impacto de chunks novos.

As métricas devem registrar **mediana, P90/P95 quando a amostra permitir, seed, versão, bioma, equipamento e método de medição**. Não inferir tempo de jogador a partir de JSON de worldgen.

---

# 11. DIREÇÃO VISUAL, ANIMAÇÃO E ÁUDIO

## 11.1 Identidade visual geral

A referência é um Minecraft reconhecível com uma camada de mistério. **Fantasia contida, materialidade clara, contraste de formas e cores intencional.** No Overworld, 80% do cenário deve continuar sendo leitura vanilla; os elementos do mod chamam atenção por composição e detalhes, não por luz em excesso.

### Paleta conceitual

- **Overworld comum:** cores vanilla locais; não tingir o bioma inteiro.
- **Vestígios iniciais:** pedra natural, cinzas, tons frios apagados, marcas discretas.
- **Atividade/ressonância:** pequenos acentos ciano/azul ou âmbar conforme função, evitando que todo objeto brilhe igual.
- **Ruínas:** materiais envelhecidos, variação de construção real, vegetação apropriada e distribuição irregular de desgaste.
- **Dreamscape/Campo Azul:** conservar identidades visuais existentes, não transplantar suas paletas integralmente para qualquer floresta vanilla.

**Hipótese:** usar o azul para reação/ressonância e tons quentes para estabilização/proteção. **Não é regra aprovada** até testes de legibilidade, contraste e material.

## 11.2 Blocos

1. Preferir textura que tenha boa leitura em inventário e no mundo.
2. Definir face de topo, lateral e base quando necessário; evitar repetição que produza superfícies artificiais.
3. Coerência de escala de pixel com assets do Minecraft; avaliar 16×16 como base quando adequado, sem fixar resolução para todo material.
4. Blocos luminosos precisam de estados visuais diferenciáveis com partículas mínimas/desligadas.
5. Família arquitetônica deve ter texturas compatíveis entre bloco, escada, laje e muro.
6. Padrões de desgaste devem parecer naturais, não ruído sem propósito.

## 11.3 Modelagem de mobs em Blockbench

Cada mob deve possuir **model sheet** com: silhueta, escala em blocos, proporção de cabeça/membros, paleta, área de hitbox, zona de visão/alerta, animações mínimas e condições de spawn. Antes de textura final, testar silhueta cinza em cena real.

GeckoLib é preferível quando a criatura precisa de idle/locomoção/ataque/transições próprias. **Uma criatura simples não precisa obrigatoriamente de GeckoLib.** Reusar o framework atual de comportamento e população quando couber; separar render cliente e autoridade de gameplay do servidor.

## 11.4 Sinais ambientais e som

- Pistas não dependem exclusivamente de áudio, por acessibilidade.
- Partículas são sutis, localizadas, com orçamento definido e fallback visual sem partículas.
- Efeitos de proximidade não podem se transformar em alerta permanente intrusivo.
- Som é contextual: ajuda a observar, não informa coordenada exata.
- Evitar loops de áudio por entidade/por jogador sem limites e limpeza de estado.

## 11.5 Direção arquitetônica das estruturas

Toda estrutura exige concept por **planta, elevação, silhueta e leitura a distância** antes do template NBT final. Para cada variante: duas ou três funções visuais claras (entrada, área principal, objeto de interesse), materiais coerentes com ambiente e validação de colisões e acessibilidade. NBT válido e `/locate` funcionando **não equivalem** a bom level design.

---

# 12. CONTRATO TÉCNICO E REUTILIZAÇÃO

## 12.1 Tecnologias existentes

- Minecraft 26.3, NeoForge 26.3.0.101, Java 25, ModDevGradle.
- Recursos por registries/deferred registration, recipe JSON, loot tables, tags e datapacks quando viáveis.- Worldgen por configured/placed features, biomas, Structure Sets, NBT, template pools quando cabível.
- GeckoLib para mobs que necessitem animações próprias e Curios para acessórios existentes.
- CI com JUnit, verificações estáticas, build e servidor dedicado em seeds fixas.

A API deve ser conferida sempre na versão **26.3**, inclusive nomes de diretório, loot schema e estrutura de código. Esta Bible não é guia de assinatura de APIs nem substitui pesquisa oficial ao criar uma task técnica.

## 12.2 Ordem de implementação recomendada por fatia vertical

1. Especificar ação do jogador, origem, resultado, recompensa, custo e falhas.
2. Decidir se vanilla datapack/registries resolvem o sistema antes de Java customizado.
3. Implementar **um** bloco/item e sua obtenção com assets temporários válidos.
4. Fechar loot, ferramenta, recipe, advancement e uma aplicação jogável.
5. Integrar worldgen/spawn/estrutura com validação em mundo real.
6. Testar cliente, servidor dedicado, saves novos e regressão.
7. Fazer balanceamento com métricas antes de multiplicar conteúdo.
8. Refinar arte, som e UX após provar o loop central.

## 12.3 Reutilização responsável

- **Registro:** manter uma fonte clara de IDs e nomes; não duplicar aliases.
- **Worldgen:** utilitários comuns para validação de terreno e suporte apenas quando há pelo menos dois consumidores reais; distinguir regras de Overworld, Campo Azul e ilhas Dreamscape.
- **Mobs:** reutilizar políticas de habitat, population caps, cooldown e navegação; comportamentos específicos permanecem na espécie.
- **Estruturas:** catálogos e pools por JSON/NBT, regras de segurança explícitas, métricas de sucesso/rejeição e sem varreduras por tick.
- **Receitas:** grafos testáveis, custos recursivos, avanços do Recipe Book e preservação de migração.
- **Diagnósticos:** opt-in, somente operador/CI, escopo limitado, sem side effects, sem depender da UI do jogador.

**Não** construir “framework de todos os fenômenos oníricos” antes de provar dois usos. As abstrações devem surgir de casos concretos, não de possibilidades imaginadas.

## 12.4 Segurança, saves e performance

- Preservar IDs e formatos serializados existentes.
- Criar conteúdo natural apenas em novos chunks, salvo migração excepcional expressamente aprovada.
- Não forçar chunks ou fazer scans permanentes por PlayerTick; pesquisas devem ser locais e bounded.
- Definir limites de spawn, partículas, atualizações de bloco e block entities.
- Toda interação multiplayer deve ser autoritativa no servidor, com prevenção de duplicação e concorrência.
- Loot de baú mundial é persistente e compartilhado; não oferecer reroll por relog/logout/reabertura.
- Não escrever worldgen fora da região autorizada nem destruir indiscriminadamente formações vanilla.
- Documentar riscos para outros mods, datapacks e versões anteriores do mundo.
- Testar mudanças de dimensão, morte, restart e recolhimento/recolocação de blocos com estado.

## 12.5 Protocolos de homologação

Para cada task técnica futura, separar resultados em:

- **STATIC:** JSON, tags, assets, IDs, estrutura de arquivos, referências cruzadas.
- **UNIT:** regras puras, limites, casos de borda, determinismo.
- **BUILD:** compilação real Java 25, JUnit e recursos.
- **DEDICATED SERVER:** carregamento de registries/datapacks e ausência de crashes.
- **WORLDGEN RUNTIME:** voxels, mobs, estruturas e block entities observados em seeds reais.
- **CLIENT VISUAL:** texturas, animações, partículas, interface e legibilidade.
- **MANUAL SURVIVAL:** obtenção legítima, risco, loot, crafting, ciclo completo e esforço.
- **MULTIPLAYER:** concorrência, persistência, isolamento e sincronização.
- **SAVE COMPATIBILITY:** mundo antigo, chunks novos, restarts e estabilidade.

Não marcar um teste como aprovado com base em outro: `worldgen runtime PASS` **não** prova `manual survival PASS`.

---
# 13. EXPERIÊNCIA DO JOGADOR: REFERÊNCIA DE 3 SESSÕES

A cronologia abaixo é **um roteiro de experiência possível**, não uma campanha ou uma sequência mandatória. A mesma save pode seguir ordem diferente conforme bioma, spawn e preferência do jogador.

## Sessão A — Primeiro contato (Early Game)

O jogador constrói abrigo, explora uma entrada de caverna e encontra uma formação que não pertence ao repertório vanilla. Ele reconhece uma nova textura, aproxima-se e coleta um recurso modesto. Esse recurso tem um uso visível na própria base: uma pedra ornamental, uma luz, uma reação local ou uma ferramenta inicial. Ele percebe que o fenômeno não serve apenas para “abrir outra dimensão”.

**Condição de sucesso:** uma pessoa que nunca viu o mod consegue experimentar algo útil sem procurar wiki, entrar no sonho ou dominar redstone. O primeiro item não cria uma obrigação de farm.

## Sessão B — Um projeto próprio (Mid Game)

O jogador já possui ferro/diamante, uma pequena base e curiosidade por cavernas. Depois de encontrar um refúgio ou sinais relacionados, entende que determinado mineral interage com uma planta. Aprende um processo e cria uma ferramenta ou bloco que expande suas possibilidades. Um construtor pode usá-lo na base; um explorador, na próxima expedição. A descoberta pode inspirar interesse em materiais mais profundos, mas não obriga um novo capítulo imediato.

**Condição de sucesso:** a recompensa altera **o que o jogador pode fazer**, não somente quantos recursos ele possui.

## Sessão C — A base preparada (Mid tardio/Late Game)

O jogador dispõe de recursos, ferramentas e conhecimento. Possui cultivos, caminhos de mineração, mecanismos e estoques administráveis. Em vez de depender de sorte e improvisação para cada expedição, consegue preparar suprimentos e, quando desejado, visitar as dimensões dos sonhos. A viagem tem custo, o ambiente ainda exige atenção e o retorno depende do artefato correto. Depois de regressar, pode investir em outro projeto do Overworld, com ou sem nova viagem.

**Condição de sucesso:** sonhos passam a ser uma atividade recorrente possível, sem virar uma imposição ou uma fonte infinita de recursos grátis.

## Sessão D — O jogador que não quer viajar

Alguém gosta de construir casas, colecionar plantas e mexer em redstone. Encontra uma família de materiais novos, aprende a cultivar uma espécie e monta uma iluminação especial. Nunca equipa o Colar. Mesmo assim, sente que o DreamsDimensions acrescentou conteúdo legítimo à sua experiência vanilla.

**Esta sessão é um requisito de design, não um caso marginal.** Se a versão inicial não funcionar para esse jogador, o Overworld ficou subordinado demais aos sonhos.

---

# 14. PRIMEIRA FATIA VERTICAL RECOMENDADA — PROTÓTIPO, NÃO TASK DE EXECUÇÃO

A Bible não começa pela construção de uma civilização, cinco biomas ou um sistema global de ressonância. A primeira pergunta jogável deve ser **“uma anomalia do Overworld acrescenta algo que vale a pena descobrir?”**

## Protótipo F0 — “O primeiro vestígio”

**Objetivo:** provar um loop completo de exploração, geologia e construção, com a menor superfície de código possível.

### Conteúdo mínimo candidato

- **Uma formação superficial ou de caverna** com silhueta/paleta discreta, gerada naturalmente em chunks novos.
- **Um bloco mineral/rocha** associado a essa formação.
- **Um recurso obtível legitimamente** via ferramenta/loot definidos.
- **Uma transformação por crafting vanilla** com custo razoável.
- **Uma aplicação independente de dimensões** — inicialmente um bloco de construção com identidade própria ou mecanismo pequeno, não ambos se a implementação ficar dispersa.
- **Um sinal ambiental** que facilite reconhecer a origem do material sem coordenadas mágicas.

### Loop de aceitação

`avistar formação → aproximar-se → verificar que é diferente → extrair corretamente → fabricar → utilizar na base → buscar outra ocorrência por vontade própria`

### Critérios de aceite do protótipo

1. Novo jogador identifica que existe algo para investigar em uma sessão de Survival, com amostras reais a serem definidas.
2. O recurso é obtível sem Creative, comandos administrativos ou loot concedido por script.
3. O item transformado tem uso demonstrável e coerente com a história.
4. A obtenção não depende do Colar, Despertador ou viagem dimensional.
5. Mundo novo gera a formação; mundo antigo não retrogera nem quebra chunks existentes.
6. Nenhum mob ou estrutura grande é pré-requisito artificial desta primeira experiência.
7. Build, servidor, worldgen e testes do cliente passam.
8. O custo de performance é medido.

### O que deliberadamente não entra em F0

- Novo bioma completo.
- Dois minérios avançados, três estações, árvores e criaturas ao mesmo tempo.
- Sistema de níveis, reputação, sanidade ou inventário de pesquisa.
- Alteração do Colar/Despertador.
- Sistema de caça a POIs com HUD e waypoints.

**Aprendizado esperado:** testar se o conjunto “geologia anômala + recompensa material útil” tem força suficiente para fundamentar expansão real. Se não tiver, revisar fantasia/interação **antes** de criar vinte variantes.

---

# 15. ROADMAP DE PLANEJAMENTO — DESDOBRAMENTO DA TASK 6.0

Cada documento posterior deve ser criado depois de validar os anteriores. **O planejamento antecede código, mas não pode se transformar em uma eternidade de especificação:** depois de 6.9, iniciar uma fatia vertical pequena e homologá-la.

| Ordem | Task | Tipo | Entregável | Depende de | Gate para avançar |
|---|---|---|---|---|---|
| **1** | **6.0 — Game Design Bible** | Direção | Este documento: fantasia, pilares, limites, catálogo candidato, compatibilidade e avaliação. | Baseline real Tasks 1–5. | Diretrizes e escopo aprovados; propostas claramente marcadas. |
| **2** | **6.1 — Contrato de Progressão e Loops** | Design de sistemas | Mapa Early/Mid/Late, caminhos paralelos, fluxos de gameplay, dependências e exemplos de sessões. | 6.0 | Pelo menos 3 loops de Overworld que **não** dependam de viagens; nenhum gating circular. |
| **3** | **6.2 — Recursos, Geologia, Flora e Blocos** | Conteúdo/economia | Catálogo candidato filtrado; origem, distribuição, obtenção, processamento, usos, famílias construtivas. | 6.1 | Cada recurso tem fonte + uso principal + continuidade; não duplicar função de `dream_dust`. |
| **4** | **6.3 — Itens, Consumíveis, Ferramentas e Estações** | Design de sistemas | Specs de interação e UX para itens/estações escolhidos, com alternativas e protótipos. | 6.2 | Cada equipamento tem uma ação específica; nova estação justifica seu custo. |
| **5** | **6.4 — Ecologia, Mobs e Habitats** | Conteúdo/IA | Lista de papéis ecológicos, criaturas candidatas, spawns, relações e combate/observação. | 6.1–6.3 | Nenhum mob é drop ambulante; habitat e contrajogo descritos. |
| **6** | **6.5 — Estruturas e Descoberta Ambiental** | Level design | Famílias de POIs, distribuição, arquitetura, caminhos, recompensas, sinais e riscos. | 6.1–6.4 | Estruturas têm função além de baú e regras de posicionamento seguras. |
| **7** | **6.6 — Grafo de Crafting e Transformações** | Economia | DAG de ingredientes, receitas e estações, sinks e balanços, impactos em existentes. | 6.2–6.5 | Nenhum ciclo impossível; custos não são só inflação de quantidade; caminhos principais acessíveis. |
| **8** | **6.7 — Direção Visual e Produção de Assets** | Arte/UX | Paletas, block sheet, sprites de itens, silhuetas de mobs, referência NBT e ordem de protótipos. | 6.2–6.6 | Cada família tem style guide e plano de testes no cliente. |
| **9** | **6.8 — Economia, Raridade e Experimentos** | Balanceamento | Métricas-alvo, seeds, plano de amostragem, cenários de jogador, critérios de ajuste. | 6.1–6.7 | Raridade e custo dimensionados por evidência exigida, não por intuição. |
| **10** | **6.9 — Roadmap Executivo de Implementação** | Produção | Tasks de código ordenadas por fatia vertical, dependências, testes, comandos e definições de pronto. | 6.0–6.8 | Primeira fatia F0 implementável sem depender de conteúdo futuro não existente. |

## 15.1 Princípio de sequência após 6.9

Não sequenciar “30 blocos → 10 itens → 5 mobs → estruturas → gameplay”. Priorizar:

`F0 geologia + obtenção + uso → validação → F1 natureza/cultivo + uso → validação → F2 estrutura + técnica → validação → F3 mob ecológico + interação → validação → F4 mecanismo/progressão Mid → validação → F5 expansão Late e integração dimensional → validação`.

Os nomes e a ordem exata de F1–F5 dependem do que as Tasks 6.1–6.8 aprovarem. **O importante é que cada pacote tenha uma experiência completa no momento em que chegar ao jogo.**

## 15.2 Contrato mínimo de cada task técnica futura

Cada task de implementação deverá explicitar:

- Objetivo e problema de gameplay que resolve.
- Escopo positivo, fora de escopo, dependências e compatibilidade.
- IDs exatos e versão de API pesquisada.
- Fluxo completo de Survival e interações com conteúdo existente.
- Recursos, receitas, loot, tags, assets e traduções necessários.
- Regras de worldgen/spawn e performance se aplicáveis.
- Testes estáticos, unitários, servidor, gameplay, multiplayer e saves.
- Seeds e **comandos copiáveis** (`/give`, `/locate`, `/tp`, `/place`, `/loot` apenas quando corretos e verificados); nunca inventar posições.
- Evidência de implementação, diff, CI, documentação, commit e push apenas quando a tarefa realmente pedir código.
- Lista honesta de testes pendentes, bugs e riscos de regressão.

---

# 16. RISCOS DE PRODUTO E RESPOSTAS PREVENTIVAS

| Risco | Sinal de alerta | Resposta de direção |
|---|---|---|
| **Conteúdo sem função** | Muitos itens com uma única receita terminal e nenhum outro uso. | Voltar à ficha e exigir uma ação/uso real ou remover o candidato. |
| **Colar/Despertador monopolizam o mod** | Quase toda receita termina em itens de viagem. | Priorizar loops independentes e recompensas para construtores/naturalistas. |
| **Dificuldade confundida com grind** | Progressão exige milhares de drops sem novas escolhas. | Reduzir inflação; adicionar processamento/risco significativo ou caminho alternativo. |
| **Saturação visual** | Todas as cavernas têm cristais azuis e partículas. | Microambientes, densidade medida e composição mais contida. |
| **Frustração por RNG** | Jogador não encontra conteúdo essencial apesar de dominar mecânica. | Medir P90/P95 e oferecer alternativas, pistas ou melhorar distribuição. |
| **Late Game sem utilidade** | Recursos novos tornam-se obsoletos depois de uma viagem. | Projetar sinks de construção, manutenção, cultivo e mecanismos. |
| **Muito Java customizado** | Criar frameworks antes de ter loops comprovados. | Primeiro vanilla/JSON/datapack; Java somente quando necessário. |
| **Perda de performance** | Travamentos em chunk generation, muitas block entities ou tick scans. | Budget e perfis de CI, geração segura, caps, processamento limitado. |
| **Mobs sem identidade** | Criaturas apenas perseguem e atacam, diferenciadas só por textura. | Exigir papel ecológico, telegraph, estados, reação do ambiente e contrajogo. |
| **Estruturas sem valor** | Ruína = casa quadrada + baú no meio. | Planta, silhueta, função arquitetônica, pista/desafio/recompensa. |
| **Quebra de saves** | Mudança de IDs/campos serializados ou retrogeração involuntária. | Registro/migração formal, backups, testes de mundo velho + chunks novos. |
| **Dependência de mods extras sem necessidade** | Cada nova ferramenta exige library nova. | Reusar stack e APIs existentes, justificar dependência com valor mensurável. |
| **Promessa de homologação indevida** | “Passou no build” usado para afirmar “Survival perfeito”. | Separar categorias de evidência em todo relatório. |

---

# 17. DECISÕES QUE AINDA PRECISAM SER TOMADAS

A Bible **não** encerra as seguintes discussões. São decisões intencionalmente abertas que orientarão as próximas tasks:

### Narrativa

- O fenômeno é consequência de uma catástrofe antiga, propriedade natural do mundo ou atividade de uma cultura perdida? **Não canonizado.**
- Existiram “Observadores” de fato, ou isso será interpretação arqueológica do jogador? **Não canonizado.**
- Como vestígios distintos se conectam às identidades do Campo Azul e Dreamscape? **Planejamento posterior**, sem redesenhar suas dimensões agora.

### Sistemas

- Qual a primeira mecânica transversal de maior valor? **Geologia + construção** é a recomendação, não uma decisão implementada.
- Quantos materiais e plantas merecem registros reais na primeira release de Overworld? **Medir protótipos antes de fixar.**
- Precisaremos de estação customizada ou smithing/stonecutter/crafting vanilla bastarão? **A depender das receitas.**
- Conhecimentos serão apenas desbloqueios do Recipe Book, pistas ambientais ou um sistema persistente próprio? **Preferir solução simples até surgir necessidade real.**
- Um instrumento de leitura ambiental será útil, ou pistas naturais serão suficientes? **Comparar propostas da Task 6.3.**

### Progressão e economia

- Quando exatamente a primeira viagem se torna desejável no Mid Game e como proteger jogadores despreparados sem desativar a liberdade? **6.1 e 6.6.**
- As receitas atuais do Colar e do Despertador terão mudanças? **Não decidir aqui.** São contratos existentes com migração potencial.
- Qual orçamento de tempo e risco torna viagens consistentes no Late Game sem banalizá-las? **6.8 com dados reais.**
- Que conteúdo avançado permanece opcional para construtores e naturalistas? **Obrigatório responder em 6.1.**

### Arte

- Paleta de vestígios no Overworld: azul discreto, âmbar, cinza ou combinação funcional? **Testes visuais.**
- Primeira criatura deve ser passiva observacional ou guardiã territorial? **Começar pela de menor risco e maior valor exploratório.**

---

# 18. CRITÉRIOS DE ACEITE DA TASK 6.0

## Entregas deste documento

- [x] Declara fantasia central e posicionamento do DreamsDimensions.
- [x] Reconhece Overworld como experiência própria e as dimensões como parte de um universo maior.
- [x] Define seis pilares conectados: geologia, natureza, fauna, estruturas, crafting e construção/mecanismos.
- [x] Explica relação entre narrativa, sistemas e descoberta ambiental.
- [x] Diferencia Early, Mid e Late Game, sem End Game exclusivo.
- [x] Posiciona travessias ocasionais no Mid e consistência no Mid tardio/Late.
- [x] Não trata Colar/Despertador como finalidade de todo conteúdo.
- [x] Inclui catálogo amplo de candidatos com fontes, funções, conexões e riscos.
- [x] Define critérios para rejeitar itens, mobs e blocos que não agregam valor.
- [x] Preserva os contratos existentes e distingue implementação de hipótese.
- [x] Define governança de raridade, loot, progressão, multiplayer, saves e performance.
- [x] Estabelece primeira fatia vertical e ordem das Tasks 6.1–6.9.
- [x] Não introduz novas dimensões, chefe final ou conclusão obrigatória.
- [x] Não altera código, branch, receita, configuração ou CI.

## Antes de considerar a direção formalmente aprovada

- [ ] Confirmar que as seis fantasias de jogador representam o produto desejado.
- [ ] Validar ou corrigir a premissa narrativa do Limiar/Vestígios/Ecos.
- [ ] Aprovar o princípio de loops independentes das viagens dimensionais.
- [ ] Priorizar 2–3 famílias de conteúdo para os protótipos, sem fechar o catálogo inteiro.
- [ ] Escolher se o primeiro protótipo F0 continuará com geologia + construção ou terá outra interação de maior valor.
- [ ] Autorizar criação da Task 6.1 a partir da direção aprovada.

A marcação `[x]` nesta seção indica **entrega documental presente**, não conteúdo implementado em Minecraft.

---

# 19. REFERÊNCIAS INTERNAS E FONTE DA BASELINE

As referências abaixo são arquivos existentes na branch consultada; algumas tabelas históricas descrevem estados antigos e devem ser interpretadas à luz dos addenda mais recentes e dos registros atuais.

- [`docs/GAMEPLAY_CONTENT_MATRIX.md`](https://github.com/ErikMendesRc/dreamsdimensions/blob/migration/minecraft-26.3/docs/GAMEPLAY_CONTENT_MATRIX.md) — inventário histórico de recursos, mobs e sistemas, com addenda recentes.
- [`docs/GAMEPLAY_ROADMAP.md`](https://github.com/ErikMendesRc/dreamsdimensions/blob/migration/minecraft-26.3/docs/GAMEPLAY_ROADMAP.md) — cronologia anterior; **não confundir Task 5.0 histórica com a nova série 6.x**.
- [`docs/OVERWORLD_ECONOMY_TASK_5_6.md`](https://github.com/ErikMendesRc/dreamsdimensions/blob/migration/minecraft-26.3/docs/OVERWORLD_ECONOMY_TASK_5_6.md) — economia atual, custos e minério.
- [`docs/DREAM_RUINS_TASK_5_7.md`](https://github.com/ErikMendesRc/dreamsdimensions/blob/migration/minecraft-26.3/docs/DREAM_RUINS_TASK_5_7.md) — ruínas de recuperação e limites dos testes.
- [`src/main/java/com/dreamsdimensions/mod/registry/ModBlocks.java`](https://github.com/ErikMendesRc/dreamsdimensions/blob/migration/minecraft-26.3/src/main/java/com/dreamsdimensions/mod/registry/ModBlocks.java) — blocos efetivamente registrados.
- [`src/main/java/com/dreamsdimensions/mod/registry/ModItems.java`](https://github.com/ErikMendesRc/dreamsdimensions/blob/migration/minecraft-26.3/src/main/java/com/dreamsdimensions/mod/registry/ModItems.java) — itens efetivamente registrados.
- [`src/main/java/com/dreamsdimensions/mod/registry/ModEntities.java`](https://github.com/ErikMendesRc/dreamsdimensions/blob/migration/minecraft-26.3/src/main/java/com/dreamsdimensions/mod/registry/ModEntities.java) — entidades efetivamente registradas.
- `src/main/resources/data/dreamsdimensions/recipe/` — receitas existentes, referência para revisão de crafting.

Referências técnicas a consultar **no momento da implementação**:

- [NeoForge 26.3 Primer](https://docs.neoforged.net/primer/docs/26.3/)
- [NeoForge Worldgen](https://docs.neoforged.net/docs/worldgen/)
- [NeoForge Loot Tables](https://docs.neoforged.net/docs/resources/server/loottables/)
- [Minecraft Java 26.3](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3)

---

# 20. RESUMO EXECUTIVO — CONTRATO PARA O FUTURO

**DreamsDimensions deve ser uma expansão orgânica do Minecraft:** o Overworld ganha geologia, flora, fauna, estruturas, materiais, equipamentos, receitas, blocos de construção e mecanismos com vínculos temáticos e mecânicos.

- Os sonhos **não são a única recompensa** do mod.
- Cada recurso deve ter origem, aplicação imediata e motivo para continuar existindo.
- O conteúdo decorativo tem valor legítimo, sem mecânica inventada por obrigação.
- Exploração acontece porque o jogador observa, deseja descobrir e consegue agir.
- A progressão ocorre **Early → Mid → Late**, sem boss final ou fim exclusivo.
- A primeira viagem é significativa; a consistência onírica só surge depois de preparação e infraestrutura.
- Dificuldade vem de preparo, decisões e risco — não apenas de receitas infladas ou RNG excessivo.
- Sistemas são implementados por **fatias completas de Survival**, medidos em servidor real e reutilizados quando houver necessidades concretas.
- IDs, loot, travel/return, respawn e saves existentes são preservados até uma migração aprovada.
- Esta Game Design Bible orienta as Tasks 6.1–6.9 e **não representa implementação de novo conteúdo**.

> **Pergunta final para qualquer ideia:** “Se eu remover o Colar e o Despertador deste protótipo, esse novo bloco, criatura, estrutura ou sistema ainda torna o Minecraft mais interessante?”  
> Se a resposta for “não” para quase tudo o que criamos, ainda não construímos a expansão que queremos.

**Próxima task recomendada:** **6.1 — Contrato de Progressão e Loops do Overworld**, desenhando as atividades completas de Early/Mid/Late e a primeira fatia vertical a ser prototipada.

---

*Fim da Game Design Bible — Task 6.0 | Documento de direção para discussão e aprovação; nenhuma alteração em código ou repositório foi realizada.*