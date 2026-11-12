# TASK 6.5 — ESTRUTURAS E DESCOBERTA AMBIENTAL DO OVERWORLD

## Contrato de level design, exploração, recompensas e worldgen — Early / Mid / Late

**Projeto:** DreamsDimensions  
**Natureza:** Game Design / Level Design / Worldgen Design — **somente documentação**  
**Versão:** 1.0 — especificação de estruturas e decisões de prototipagem  
**Data:** 12 de novembro de 2026  
**Branch:** `migration/minecraft-26.3`  
**HEAD auditado:** `263c9bacc9056a2835f4937f729a71325d85eded`  
**Base:** [Bible 6.0](GAME_DESIGN_BIBLE_TASK_6_0.md), [Pilares 6.1](OVERWORLD_GAMEPLAY_PILLARS_TASK_6_1.md), [Recursos/Flora 6.2](OVERWORLD_RESOURCES_GEOLOGY_FLORA_BLOCKS_TASK_6_2.md), [Itens/Estações 6.3](OVERWORLD_ITEMS_CONSUMABLES_TOOLS_STATIONS_TASK_6_3.md), [Ecologia/Mobs 6.4](OVERWORLD_ECOLOGY_MOBS_HABITATS_TASK_6_4.md).  
**Próxima Task:** **6.6 — Grafo de Crafting, Processamento e Progressão Material**.  
**Escopo:** não alterar blocos, itens, Java, NBT, worldgen, loot, crafting, mobs, configurações ou a branch main.

> **Diretriz central:** cada estrutura deve justificar o esforço de ser encontrada por uma experiência de jogo própria. O jogador deve identificar uma pista, interpretar o lugar, escolher como explorá-lo, obter uma recompensa adequada e usar o que descobriu no Overworld. Nem toda estrutura precisa de baú; nenhuma é apenas um container decorado; nenhuma representa o End Game do DreamsDimensions.

---

# 0. DECISÕES EXECUTIVAS

## 0.1 Catálogo selecionado

| Código | Nome de trabalho | Estágio | Formato | Experiência distinta | Decisão |
|---|---|---|---|---|---|
| **POI-01** | **Marco de Pedra Marcada** | Early | Pequena estrutura superficial | Reconhecimento de material e padrões geológicos; recompensa sem baú | **SELECIONADO** como primeiro POI a especificar/implementar após F0 |
| **POI-02** | **Abrigo de Campo** | Early tardio/Mid | Ruína utilitária superficial | Abrigo físico, observação de atividade antiga, duas entradas e suprimentos | **SELECIONADO** depois de POI-01 |
| **POI-03** | **Galeria Afundada** | Mid | Estrutura subterrânea com várias áreas | Travessia, água/desnível, decisão entre rota de preparação e rota de risco | **CONDICIONADA** a protótipo de geração subterrânea íntegra |
| **POI-04** | **Câmara do Limiar** | Mid tardio/Late | Estrutura subterrânea rara | Duas abordagens físicas/redstone e possível Guardião opcional | **CONDICIONADA** à Galeria, a recompensa definida em 6.6 e ao balanceamento 6.8 |

**Não criar agora:** boss de ruína obrigatório, vila de Observadores, “portal” que promete viagem ao sonho, dungeon gigantesca a cada quilômetro, minério exclusivo gerado dentro de baús, chave única de campanha, receitas essenciais em uma estrutura quase impossível de encontrar.

**Status:** selecionado = pode avançar à especificação de implementação; **não** significa existente no jogo. Qualquer ID/raridade/posição deste documento é candidato até passar 6.6–6.9 e CI/Survival.

## 0.2 O primeiro POI não é o primeiro recurso

**F0 Pedra Marcada** deve continuar sendo obtida **naturalmente** fora do Marco; **F1 Líquen** deve continuar cultivável sem Abrigo. O Marco serve para reforçar a linguagem do mundo, e não para tornar a mineração/artesanato básico dependente de um local raro. A Argamassa (6.3) permanece obtível por geologia + flora + vanilla, sem item exclusivo de estrutura. O Colar e Despertador mantêm seu ramo de progressão existente, sem chave de boss/POI adicionada nesta task.

## 0.3 O que este documento entrega

- Quatro famílias **com topologias e loops diferentes**.
- Pistas ambientais, histórias espaciais e decisões de exploração com alternativa não letal.
- Recompensas garantidas fora do sorteio de baú, com limites econômicos.
- Variantes **topológicas**, não apenas paletas recoloridas.
- Regras de worldgen, segurança de suporte, acesso, continuidade e persistência.
- Gates de 6.6–6.9, métricas e comandos de QA para **futuras** implementações.
- Nenhuma mudança funcional no jogo e nenhuma declaração de homologação inventada.

---

# 1. AUDITORIA DA BASELINE REAL

## 1.1 Estruturas já implementadas

O repositório contém:

- `dreamsdimensions:blue_dream_ruins` — ruína **do Campo Onírico Azul**, não Overworld.
- `dreamsdimensions:fragmented_dream_ruins` — ruína **do Dreamscape**, não Overworld.
- Arquivos nativos data-driven `worldgen/structure`, `worldgen/structure_set`, `worldgen/template_pool`, `structure/ruins/*.nbt` e `loot_table/chests/*.json`, todos em diretórios singulares adequados ao baseline 26.3.
- Estruturas `minecraft:jigsaw`, cada uma com um template inicial, structure set random spread e chest com referência pendente de loot table.
- Loot raro configurado com probabilidade de **25% de um Despertador** em cada baú dimensional, no máximo um, destinado a **recuperação após expedição onírica**. **Esse pool NÃO se aplica ao Overworld.**
- A Task 5.7 comprovou `/locate` e a presença física de oito baús em quatro seeds; um caso no Dreamscape apresentou suporte local de **4/9 pontos** na vizinhança do baú. Isso **não** prova que a estrutura inteira está navegável ou totalmente apoiada.
- O usuário relatou sucesso na homologação de seu ciclo de exploração. Ainda assim, novos POIs precisam de seus próprios testes de integridade/terreno; a observação parcial de 5.7 é motivo para preveni-los.

**Diagnóstico:** nenhum POI nativo do **Overworld** foi implementado até esta baseline. Não copiar NBT dimensional para o Overworld e trocar apenas a cor.

## 1.2 Dependências reais

| Sistema anterior | Condição relevante para 6.5 |
|---|---|
| Bible 6.0 | Estruturas em escalas diversas; narrativa ambiental, sem “endgame do mod” |
| Pilares 6.1 | Loop P4: notar → interpretar → explorar → aplicar benefício fora do POI |
| Recursos 6.2 | Pedra Marcada/F0 e Líquen/F1 selecionados, ainda não implementados |
| Itens 6.3 | Argamassa selecionada; Cinzel, Leitor e detectores condicionados; nenhuma estação nova aprovada |
| Mobs 6.4 | Mariposa ligada a Líquen; Habitante territorial opcional; Guardião de Pedra só em estrutura adequada |
| Ruínas 5.7 | Template nativo, loot persistente, auditoria de suporte, cautela com heightmaps e acesso |

**Não antecipar:** materiais, mobs, blocos e estações que só aparecem em futuras tasks não podem ser exigidos em um protótipo construído com vanilla.

---

# 2. CONTRATO GLOBAL DE EXPLORAÇÃO

## 2.1 Os seis elementos de um encontro completo

1. **Silhueta:** o jogador nota o objeto na paisagem; deve distingui-lo de geologia natural ou construção vanilla.
2. **Lugar:** a arquitetura tem justificativa física em bioma, água, rocha, relevo e clima.
3. **Pista:** existe algo para ser inferido sem HUD de GPS, texto explicando tudo ou Wiki.
4. **Escolha:** há duas formas de abordagem, ou uma decisão relevante entre observar/coletar/avançar/arriscar.
5. **Benefício:** algo material, arquitetônico, informacional ou funcional que pode ser usado fora do POI.
6. **Continuidade:** encontrar outro exemplar ainda faz sentido por contexto, variante de espaço ou novo objetivo.

**Proibição:** estrutura que só tem recompensa porque contém um baú com `rolls` altos.

## 2.2 Sandbox Minecraft, não RPG linear

O jogador pode quebrar paredes, colocar escadas, drenar água com baldes ou construir uma ponte. Isso é **parte do gameplay**. Um desafio bom apresenta caminhos com custos diferentes; não exige blocos indestrutíveis nem proíbe mineração para manter um puzzle.

**Não implementar** cadeado artificial de “primeira estrutura → segunda estrutura → terceira estrutura → Câmara → fim”. Essas são categorias de **desenvolvimento e dificuldade percebida**, não quest obrigatória.

## 2.3 Dificuldade por faixa

| Aspecto | Early | Mid | Late |
|---|---|---|---|
| Descoberta | Sinal relativamente legível | Contexto mais específico, mas compreensível | Região/arquitetura excepcional, pista aprendível |
| Entrada | Acesso simples e piso seguro | Desnível, água, alternativas de equipamento | Percursos interligados, decisão técnica/risco opcional |
| Risco | Baixo, sem surpresa fatal | Contornável, comunicado e preparável | Exigente mas justo, sem prisão ou boss obrigatório |
| Recompensa | Pedra/forma/abrigo | Materiais, técnica, ambiente e escolhas | Capacidade horizontal ou estética de longo prazo |
| Repetição | Aprende o sinal | Busca variações de contexto | Explora por projetos/desafios sem encerrar o jogo |

**Não usar tempo obrigatório de login, boss kills ou receita infinita como “dificuldade”**. A 6.8 definirá metas observáveis de frequência/distância.

---

# 3. POI-01 — MARCO DE PEDRA MARCADA

**Nome provisório:** Marco de Pedra Marcada.  
**ID candidato, NÃO registrado:** `dreamsdimensions:marked_stone_marker`.  
**Status:** selecionado **como primeiro POI**; depende de F0 Pedra Marcada.  
**Estágio:** Early. **Baú:** **NÃO** por padrão.

## 3.1 Silhueta e propósito

Arranjo antigo e intencional de rochas: **dois/três elementos verticais baixos**, apoio irregular e um foco construtivo de Pedra Marcada. Não altar de teleporte, portal inativo ou pilha aleatória de pedras. Parece uma estrutura feita para **marcar um lugar ou observar uma orientação**, sem canonizar quem a construiu.

**Paleta:** pedra, tuff, andesite, pequenas superfícies trabalhadas, Pedra Marcada depois de F0; Líquen opcional somente após F1. Contraste discreto, sem brilho neon.

## 3.2 Loop completo e escolha

`avistar marco → reconhecer alinhamento incomum → aproximar-se → observar amostra/organização → escolher seguir pista OU coletar alguns blocos → construir com Pedra Marcada → reconhecer outro marco`.

- **A — observar:** o jogador aprende a identificar Pedra Marcada e pode procurar ocorrência geológica natural no ambiente. Se o Marco der orientação literal, o worldgen deverá validar que a direção corresponde a uma ocorrência real.
- **B — coletar:** extrair os poucos blocos do próprio Marco como material construtivo; recompensa física finita, nunca renovável no mesmo local.

**Piso de recompensa:** amostra arquitetônica e informação visual, **não** baú aleatório.

**Questão de honestidade:** não dizer “Marco aponta para minério” se a formação F0 e a estrutura são colocadas **independentemente**. Se não houver acoplamento confiável, o Marco ensina textura/padrão, mas **não promete direção**.

## 3.3 Três variantes propostas (somente uma no primeiro release)

| Variante | Silhueta | Contexto | Experiência diferente |
|---|---|---|---|
| M01-A **Inclinada** | Duas pedras altas/baixas fora de simetria | Encosta/afloramento | Extrair ou estudar alinhamento |
| M01-B **Fraturada** | Arco curto incompleto | Clareira de rocha | Reconhecer marcas de material |
| M01-C **Semi-soterrada** | Pequeno eixo visível sob terra/folhas | Bosque/terreno irregular | Escavação leve opcional |

**Não produzir as três antes de homologar a primeira.** Variante deve modificar experiência/visibilidade, não apenas skin.

## 3.4 Worldgen e critérios

- Apoio em chão sólido e leitura da silhueta; nada de monólito flutuando na borda do barranco.
- Rejeitar terreno alagado/lava/gradiente extremo, sem nivelar a montanha inteira.
- Não destruir estruturas vanilla, árvores grandes ou construção do jogador.
- Deve ser encontrável em Early sem ter o mundo saturado de marcos.
- F0 ocorre naturalmente **independentemente** desse POI.
- Não criar chest, guardião, key, UI de puzzle nem Drop de Despertador.
- Se a versão final virar apenas uma formação geológica sem intenção arquitetônica, considerar **placed feature em vez de structure**; o uso de `/locate` não justifica categoria errada.

**Gate:** jogador novo nota a silhueta, entende material, recupera ou identifica uso e volta à base. Medição real de suporte/terrain e distância em quatro seeds ou mais conforme 6.8.

---

# 4. POI-02 — ABRIGO DE CAMPO

**ID candidato, NÃO registrado:** `dreamsdimensions:field_shelter`.  
**Status:** selecionado para especificação após Marco.  
**Estágio:** Early tardio/Mid. **Baú:** opcional, pequeno.

## 4.1 Fantasia arquitetônica

Pequeno **posto de trabalho abandonado** construído na borda de um afloramento, encosta ou entrada de caverna. Não afirmar que pertenceu à “Civilização dos Observadores”: sua origem narrativa continua aberta. A história é contada por **bancada, suportes, amostras, depósito pequeno e cobertura**, não pelo nome de uma facção inventada.

Forma irregular, telhado parcialmente desfeito, madeira/pedra/cobre vanilla, acabamento da Pedra Marcada quando implementado, iluminação contida.

## 4.2 Experiência real

1. O jogador vê uma cobertura anormal perto de rocha, percebe que pode haver abrigo.
2. A porta principal está parcialmente exposta e existe uma **segunda abertura acessível** lateral ou traseira.
3. Pode se proteger de mobs/clima e utilizar um **bloco de trabalho vanilla** (crafting table/furnace, se apropriado).
4. Investiga amostras de pedra e algum detalhe que liga o abrigo ao ambiente local.
5. Escolhe aproveitar o abrigo como base temporária, desmontar material ou procurar a pequena armazenagem.
6. Retorna com suprimentos, estilo de construção ou pistas úteis; **não precisa de um item novo para abrir a porta**.

**Piso garantido:** o abrigo físico e materiais recuperáveis existentes no layout. O baú não deve ser a única motivação.

## 4.3 Duas rotas

- **Entrada frontal:** simples, acesso ao posto de trabalho.
- **Entrada lateral:** exige passar por ruptura/obstáculo leve, encontra amostra ou pequena reserva de suprimentos.

Não colocar uma armadilha fatal no Early. Falhas de terreno que enterram ambas as entradas anulam a ocorrência.

## 4.4 Loot e variação

**Se houver baú:** suprimentos vanilla coerentes — tochas, carvão, madeira, cobre/ferro em quantidade limitada, pão, instrumentos de trabalho, argila. **Sem** Despertador, grande quantidade de Dream Dust ou armadura avançada como padrão.

| Variante | Geometria | Contexto | Diferenciação |
|---|---|---|---|
| S02-A **Encosta** | Abrigo apoiado lateralmente | Aclive leve | Acesso por varanda/ruptura |
| S02-B **Caverna** | Parede encaixada em rocha | Boca de caverna | Equipamentos de extração/amostra |
| S02-C **Bosque** | Telhado de madeira e pátio | Clareira | Pista vegetal e outro acesso |

**Primeira PR:** somente uma topologia sólida com duas entradas, uso e loot íntegro. Paleta recolorida não é variante suficiente.

## 4.5 Gates

- Dois acessos reais e apoio seguro; não suspenso sobre rio/ravina.
- A função do abrigo é percebida sem lore obrigatória.
- O bloco funcional vanilla é utilizável e, se removido pelo jogador, não volta por evento.
- Baú com loot table persistente apenas se existir; sem regenerar ao reiniciar.
- Não monopoliza recursos F0/F1 nem dá acesso gratuito às dimensões.

---

# 5. POI-03 — GALERIA AFUNDADA

**ID candidato, NÃO registrado:** `dreamsdimensions:sunken_gallery`.  
**Status:** condicionado à prova de geração e navegação subterrânea.  
**Estágio:** Mid. **Baú:** possível, mas não recompensa única.

## 5.1 Identidade

Estrutura antiga de trabalho subterrâneo **encaixada em uma fissura/veio**. A arquitetura segue o relevo: pilares, contrafortes, plataformas, pontes internas, escadas, corredores e pontos de desnível. Uma parte pode estar **alagada**, mas água precisa ser contida e gerar escolha real.

Não construir “pátio de NBT enterrado”. A galeria deve parecer escavada por uma atividade específica, com suporte físico e comunicação de entradas.

## 5.2 Duas rotas: preparação vs risco

```text
[ENTRADA VISÍVEL E SEGURA]
           |
       [ÁTRIO]
        /    \
       /      \
[SALA AUXILIAR] [PASSAGEM INTERROMPIDA/ALAGADA]
[ROTA PREPARADA]       [ROTA ARRISCADA]
       \       /
      [NÚCLEO DE DESCOBERTA]
                 |
       [SAÍDA/RETORNO REAL]
```

**Rota A (preparação):** explorar uma sala auxiliar, reconhecer alavanca/passagem/escadas, usar redstone vanilla simples, balde, blocos ou contorno; chegada segura, mais demorada.

**Rota B (risco):** atravessar diretamente água/desnível com equipamento/oxigênio/blocos vanilla; caminho mais curto e perigoso, mas **nunca uma armadilha inevitável**.

Os dois caminhos chegam ao **mesmo núcleo**. O risco pode poupar tempo ou revelar material extra, mas **não esconde a única chave de progressão**.

## 5.3 O que a Galeria recompensa

- **Garantido:** percurso arquitetônico interessante, amostras físicas de material e oportunidade de observar um padrão geológico real.
- **Opcional:** baú de mineração/construção/sobrevivência contextual, com conteúdo persistente.
- **Conhecimento:** eventual técnica/variante construtiva **somente se existir uma saída concreta aprovada em 6.6**. Não criar item “projeto antigo” como token sem função.

Não colocar Nódulo Reverberante/Cristal Velado só para preencher baú: esses recursos ainda não foram aprovados.

## 5.4 Fauna

O **Habitante das Fendas (M2)** poderá aparecer como risco contextual **depois** de existir e ser balanceado em habitat real; **não** é obrigatório para a primeira Galeria. Se usado: alerta antecipado, perseguição territorial curta e rota de fuga. Não criar mob exclusivo de geração global a partir da galeria.

## 5.5 Requisitos difíceis de worldgen

- **Acesso real da superfície/caverna até a galeria**, não coordenada subterrânea atrás de pedra sólida.
- Entradas e corredores transitáveis em Survival, incluindo rotação e desnível.
- **Água contida**: nenhuma vazão descontrolada para cavernas vanilla; testar aquifers, lava, borda de chunk e atualização de fluidos.
- Galeria não deve cortar stronghold/mineshaft/ancient city/Trial Chamber por placement agressivo; aplicar restrições/diagnóstico viáveis.
- Rejeitar posição inviável antes de gerar se a API permitir; medir **candidatos rejeitados**, não chamar rarity de sucesso.
- Não presumir que `jigsaw` com `WORLD_SURFACE_WG` sabe escolher terreno subterrâneo correto; avaliar solução nativa ou preflight técnico mínimo.
- Nenhuma armadilha mortal sem aviso, passagem de cama/lava improvisada ou teto colidindo com cabeça.

## 5.6 Variantes e Gate

Uma Galeria inundada e outra fraturada devem alterar **a rota**, não só a cor. Primeiro protótipo: duas rotas, núcleo acessível e saída, **sem mob custom**. Homologação exige várias ocorrências reais em pelo menos quatro seeds, biomas adequados, correlação de pista/terreno e teste humano Survival.

**Se a entrada subterrânea falhar**, corrigir placement/topologia antes de aumentar complexidade ou criar sala nova.

---

# 6. POI-04 — CÂMARA DO LIMIAR

**ID candidato, NÃO registrado:** `dreamsdimensions:threshold_chamber`.  
**Status:** condicionada a Galeria navegável, recompensa definida em 6.6 e frequência 6.8.  
**Estágio:** Mid tardio/Late **opcional**, nunca fim do mod.

## 6.1 Fantasia, não portal nem boss

Uma instalação arquitetônica de observação do contato entre rocha e fenômenos oníricos. Tem contrafortes, salão central, espaços de trabalho e mecanismos vanilla plausíveis. **Não é portal para outra dimensão**, nem confirma civilização ancestral nomeada.

**Silhueta:** volume subterrâneo distinto, entrada que comunica risco e uma sequência espacial de 3 atos. Não gerar salões vazios enormes com baú no meio.

## 6.2 Três atos e duas abordagens

**Ato 1 — Aproximação:** sinais visuais de engenharia mineral, iluminação rara e espaço para preparar avanço/retirada.

**Ato 2 — Escolha:** 
- **Rota de mecanismos:** redstone física simples (alavancas/portas/estado visível) ou reparo de circuito com recursos vanilla; feedback compreensível a cada ação.
- **Rota de travessia:** desvio por passarela, fenda ou corredor defensivo; se **Guardião M3** for aprovado depois, pode proteger esse percurso, com aviso e retirada.

**Ato 3 — Núcleo e saída:** recompensa útil, acesso ao ambiente/arquivo, caminho de retorno; nenhuma ação é irreversível ou depende de condição oculta impossível.

**Minecraft é sandbox:** jogador pode minerar a porta. Não usar blocos indestrutíveis ou modo Adventure permanente para “proteger o puzzle”. Um grupo bem equipado pode usar mais recursos para ignorar parte do desafio.

## 6.3 A recompensa ainda é uma decisão

**A 6.5 NÃO aprova item novo de Câmara.** Na 6.6 deverão competir recompensas reais:

- **Padrão construtivo opcional** ou amostra arquitetônica que o jogador consiga depois reproduzir com custo;
- **Método alternativo de processamento/eficiência** apenas se houver diferença concreta;
- **Mecanismo redstone ambiental** se Leitor da 6.3 passar no teste A/B;
- **Suprimentos vanilla adequados** em quantidade proporcional ao risco, sem pular todo o Mid/Late.

**Descartar:** Cristal Velado gratuito, Despertador gratuito, “chave do mod” e drop do boss obrigatório. O local pode ser raro, mas nunca a única fonte de uma capacidade básica de Overworld.

## 6.4 M3 Guardião de Pedra — condicional

**Primeiro construir e testar a Câmara SEM Guardião.** Só depois avaliar se M3 torna encontro melhor:
- guardião associado a estrutura concreta, sem spawn global;
- duas rotas permitem evitar e enfrentar;
- ataque anunciado, território limitado e fuga;
- persistência controlada; não duplica encontro/loot em chunk reload ou com dois jogadores;
- nenhum loot-chave exclusivo na morte;
- sem bossbar, invulnerabilidade forçada ou item de final de jogo.

**Se a Câmara for interessante sem Guardião, mantê-la assim.** A criatura não precisa existir para justificar uma arena já construída.

## 6.5 Gate

A Câmara só vai a código após a recompensa estar especificada em 6.6, o terreno/entradas do Mid estarem provados, layout possuir dois caminhos e saída testáveis e os custos/relevância serem medidos em 6.8. Late é **consistência e escolha**, não fim de campanha.

---

# 7. MATRIZ DE RECOMPENSAS — GARANTIA vs LOOT

| POI | Recompensa garantida sem RNG | Baú opcional | Benefício secundário | Não usar |
|---|---|---|---|---|
| POI-01 Marco | Pedra Marcada recuperável e amostra/padrão visual | **Não** | Pista ecológica/geológica verdadeira | Dream Dust e Despertador |
| POI-02 Abrigo | Cobertura, espaço de trabalho e materiais de construção | Sim, pequeno | Suprimentos e pista de habitat | Equipamento de Late grátis |
| POI-03 Galeria | Percurso, amostras e vista geológica contextual | Sim | Método ou variante **se aprovado** em 6.6 | Mineral técnico ainda não aprovado |
| POI-04 Câmara | Experiência completa de exploração + arquitetura especial | Sim, se justificado | Capacidade horizontal opcional 6.6 | Boss-key, final de jogo, item obrigatório |

**RNG altera o que é extra; nunca o direito de entender por que a estrutura é interessante.** Uma Galeria sem loot raro deve continuar divertida de explorar.

**Recompensa garantida não é gerador infinito:** os blocos da estrutura só podem ser removidos uma vez até haver reposição legítima por crafting; baú não renova. Remover material de estrutura pode ser escolha de jogador, com consequência permanente naquele lugar.

## 7.1 Conteúdo de chest

- Loot table **somente** quando existir chest real, ligada a BlockEntity.
- Itens vanilla coerentes com atividade do local: construção, ferramentas comuns, carvão/tochas, pequenos metais, comida, suprimentos de mineração/atravessia.
- Sem “token de narrativa” como único conteúdo: texto opcional não substitui recompensa útil.
- Não utilizar por reflexo o pool `oneiric_awakener` de 25% das duas ruínas dimensionais.
- Não inserir Dream Dust em alta abundância que substitua exploração dos minérios atuais.
- Sem seed de loot fixa que torne todas as cópias idênticas; usar comportamento persistente vanilla.
- Conservar loots de baús abertos, reload e dois jogadores: **um inventário de mundo**, não recompensa pessoal duplicável.

## 7.2 Progressão e RNG

Se uma técnica opcional de 6.6 exigir descoberta, avaliar:
- rota natural alternativa para recursos básicos;
- estrutura com distribuição testada e descoberta intencional;
- desbloqueio de eficiência/estética, não receita essencial a todo mod;
- multiplayer em mundos saqueados por outros usuários;
- não limitar o jogador a uma única estrutura de milhares de chunks com item 1%.

---

# 8. VARIAÇÃO GEOMÉTRICA E PADRÃO VISUAL

## 8.1 Uma estrutura profissional não é “um quadrado com tochas”

**Marco:** forma vertical baixa, base irregular, poucos blocos intencionais.  
**Abrigo:** estrutura horizontal com função social/utilitária e duas entradas.  
**Galeria:** linear/ramificada, pilares físicos, corredores e desníveis.  
**Câmara:** massa arquitetônica clara, núcleo de decisão, circulação e retorno.

**Três dimensões de variação:**
1. **Contexto** de bioma/relevo (sem copiar um pátio em qualquer lugar);
2. **Topologia** (rotas, entradas, alturas e decisões);
3. **Estado** de erosão, colapso ou vegetação, sempre sem bloquear todo acesso.

Trocar madeira por deepslate sem mudar percurso **não** conta como variante real.

## 8.2 Pipeline artístico/voxel

1. Desenhar mapa 2D de circulação com entradas, decisão, recompensa e saída.
2. Prototipar inteiramente com texturas/blocos **vanilla** e iluminação comum.
3. Testar em Survival antes de decorar.
4. Adicionar Pedra Marcada e Líquen só após implementados, com função ambiental.
5. Exportar/validar NBT real e rotations; preferir scanner de paleta estrutural e checks de bounds.
6. Verificar teto/fachada/interior/costas e laterais, não somente imagem frontal.
7. Criar novas variantes depois de comprovar que um exemplar funciona.

**Não fazer:** criar vários blocos novos “decorativos de ruína” antes de existir arquitetura que prove necessidade. Pode usar vanilla stone bricks, tuff, deepslate, andesite, madeira/cobre com coerência e textura premium.

---

# 9. WORLDGEN 26.3 — CONTRATOS TÉCNICOS

## 9.1 Arquitetura preferencial

O código da Task 5.7 demonstra estruturas nativas `minecraft:jigsaw`, estrutura/structure set/template pool e NBT gzip. Essa é a **primeira opção** para POIs que sejam realmente estruturas e precisem de `/locate` e `/place structure` em QA.

**Não presumir** que jigsaw de template único e `project_start_to_heightmap:WORLD_SURFACE_WG` resolvem entradas de Galeria/Câmara subterrânea. Para casos difíceis:
- reduzir tamanho e ajustar topologia/terrain context;
- testar colocação e projeção adequadas;
- adicionar preflight mínimo de terreno **só se evidência real exigir**;
- considerar estrutura custom/placement apenas se vanilla data-driven não puder oferecer entrada e integridade;
- rejeitar local inválido **antes** de posicionar, evitando conserto por evento/player tick.

## 9.2 Caminhos de recursos futuros (NÃO existem ainda)

```text
src/main/resources/data/dreamsdimensions/
  worldgen/
    structure/<id_aprovado>.json
    structure_set/<id_aprovado>.json
    template_pool/overworld/<id_aprovado>.json
  structure/overworld/<template_aprovado>.nbt
  loot_table/chests/<nome_se_houver_bau>.json
```

Conferir formato de **Minecraft 26.3**, estrutura singular e loot table singular. O JSON jigsaw da baseline requereu `use_expansion_hack=false`; preservar os campos atuais exigidos pelo codec no momento da implementação. NBT: binário gzip, paleta/blockstates, BEs, orientação, dimensões, DataVersion compatível. Evitar copiar exemplo antigo do Forge 1.20.

**Fontes oficiais:** [NeoForge Primer 26.3](https://docs.neoforged.net/primer/docs/26.3/); [NeoForge Worldgen](https://docs.neoforged.net/docs/worldgen/); [NeoForge Loot Tables](https://docs.neoforged.net/docs/resources/server/loottables/).

## 9.3 Colocação, biomas e integridade

- Somente `minecraft:overworld`, por biomas/ambientes aplicáveis; **não** adicionar estruturas novas ao Dreamscape/Campo Azul nesta task.
- Worldgen em chunks novos, sem retrogen automática, nem alteração de noise/terrain density vanilla.
- `structure_set` com salt/spacing/separation próprios, a serem **medidos em 6.8**, não copiados do espaçamento das ruínas dimensionais.
- Rejeitar superfície aquática, grandes inclinações, ravinas/penhascos ou falta de base quando não há projeto de suporte.
- Para subterrâneo: comprovar conectividade da entrada até cavidade/núcleo; não apenas “surgiu em Y=-30”.
- Não criar lava/água sem contenção nem provocar cascatas infinitas/colisões de chunk.
- Respeitar outros locais vanilla: vilas, mineshafts, strongholds, trial chambers, ancient cities etc. **Não afirmar proteção absoluta** com um simples `terrain_adaptation`.
- Rotação/mirror testados individualmente em stair, chest, signage, redstone e passagem.
- Recusar geração de sala sobreposta ao teto/bedrock ou abertura totalmente coberta, com rastreabilidade de diagnóstico.

## 9.4 Lições do caso 4/9

A auditoria da 5.7 mede apoio local sob baú, mas **não** toda a estrutura. Uma ferramenta nova precisa auditar:
- suporte sob base e pilares (amostra representativa e pontos críticos);
- entrada, piso e rota de saída;
- caixa de ocupação e acessibilidade das câmaras/baús;
- cavidades, água, lava, inclinação, bordas de chunks;
- área de puzzle e conexões de redstone;
- número de starts aceitos/rejeitados por terreno;
- presença e integridade de loot sem desempacotá-lo durante auditoria.

**Gate bloqueante:** JSON válido + /locate funcionando + baú físico **não** bastam quando o jogador cai, encontra entrada soterrada ou não alcança a recompensa.

---

# 10. CONTRATO DE ENIGMAS, TRAVESSIAS E CONTRAJOGO

| Princípio | Implementação de design |
|---|---|
| **Pista antes do risco** | Arquitetura, obstáculo e feedback devem indicar o que pode acontecer |
| **Duas abordagens** | Preparação/observação versus travessia/combate opcional, quando houver |
| **Uso de vanilla** | Redstone, baldes, blocos, escadas, lanternas, picareta, mapas |
| **Sem item mágico forçado** | Sonda/Lente ainda adiadas em 6.3; não criar fechadura para vendê-las |
| **Sem bloco indestrutível** | Quebrar parede é decisão sandbox com custo legítimo |
| **Sem softlock** | Entrada, retorno e rota de fuga não podem exigir segredo invisível |
| **Sem recompensa por repetição de sinal** | Puxar alavanca muitas vezes não gera loot novo |
| **Multiplayer** | Dois jogadores podem usar rotas diferentes; estado do mundo compartilhado |
| **Acessibilidade** | Cor/som não podem ser a única pista obrigatória |

Se o puzzle não for compreensível sem explicar em um documento externo, reduzir complexidade ou melhorar feedback antes de adicionar outro mecanismo.

---

# 11. RELAÇÃO COM FAUNA E SISTEMAS ANTERIORES

| POI | Ecologia/mob | Dependência |
|---|---|---|
| Marco | Nenhum mob obrigatório | Pode coexistir com flora de F1 |
| Abrigo | Mariposa M1 nas proximidades **se habitat verdadeiro** | Abrigo funciona sem M1 |
| Galeria | Habitante M2 como encontro territorial opcional | Primeiro protótipo deve funcionar sem ele |
| Câmara | Guardião de Pedra M3 **somente após level design aprovado** | Nunca boss/loot-key obrigatório |

Não usar player tick para spawnar defensor quando alguém visita baú. Para encontro de estrutura, exigir regra de existência/persistência ligada ao POI, sem duplicação após restart. Se um mob não acrescentar valor, deixar o encontro ambiental sem ele.

**Conexões restantes:** Pedra Marcada continua minério/rocha de construção acessível fora do Marco; Líquen é cultivo e habitat, não chave de portal; Argamassa permanece recurso construível sem explorar Câmara. Os mecanismos avançados do Leitor de Colônia só entram se a Task 6.3 aprovar uso prático.

---

# 12. ECONOMIA E PROGRESSÃO EARLY / MID / LATE

## 12.1 Portfólio de recompensa por estágio

**Early:** identificar materiais, obter abrigo e recursos construtivos. Novos blocos comuns **devem ser encontrados sem estrutura**.

**Mid:** exploração espacial e técnica; melhores decisões e variações arquitetônicas; risco contextual. Baús têm materiais úteis mas não fornecem arma definitiva.

**Mid tardio/Late:** grandes projetos, engenharia, desafio opcional e recompensas horizontais que continuam interessantes. Consistência não elimina necessidade de explorar outras regiões.

A ramificação de sonhos do Colar/Despertador segue paralela. **Não** colocar um Despertador em todo Abrigo ou Câmara; as ruínas de recuperação dimensionais já possuem função específica e loot próprio.

## 12.2 O que 6.6 precisa definir

- Se uma técnica arquitetônica de Abrigo/Galeria pode ser aprendida via receita/Recipe Book **sem token obrigatório de loot**.
- Qual item/bloco opcional realmente premiaria a Câmara e quais materiais são necessários para produzi-lo depois.
- Fontes de insumos fora de estruturas exauridas para jogadores novos em servidores.
- Eventual uso de itens comuns do próprio POI em receitas, sem gerar mais um minério por obrigação.
- Grafo de crafting acíclico que não depende de POI-04 raríssimo para qualquer ferramenta Early.

---

# 13. MULTIPLAYER, SAVES, LOOT E PERSISTÊNCIA

- **Estado de mundo compartilhado:** a estrutura é saqueada/modificada **uma vez** por baú/bloco, não regenerada a cada jogador.
- Chests com `LootTable` pendente são resolvidos na primeira abertura; conteúdo já aberto permanece no BlockEntity mesmo após restart.
- Não fixar `LootTableSeed` de forma a produzir mesmo loot em todas as cópias.
- Nenhum callback em tick/entrada reabastece baús.
- Se M3 for introduzido, persistir decisão do encontro de forma compatível com chunk unload e dois jogadores.
- Jogador B em servidor antigo pode explorar **outra** ocorrência; nenhuma habilidade básica depende do loot já consumido por jogador A.
- Mundo antigo não recebe retrogeração; procurar chunks novos.
- `/place structure` serve como teste **artificial**, não prova de natural spawning.
- Não modificar registros antigos das duas ruínas dimensionais para “reutilizar ID”.
- Cada release precisa de cenário de reinício e teste com pelo menos dois jogadores onde houver inventário/mecanismos.

---

# 14. MÉTRICAS E HOMOLOGAÇÃO

## 14.1 O que medir sem inventar odds

| Medida | Por quê | Evidência |
|---|---|---|
| Tempo para encontrar POI sem comando | Descobribilidade real | Playtest Survival cego |
| Distribuição por seed/bioma | Raridade consistente | Starts reais, várias regiões por seed |
| Percentis de distância | Evitar desertos de conteúdo | P50/P90/P95 com amostra adequada |
| Integridade da estrutura | Suporte, entrada e saída | Auditoria bbox/rotas, screenshots |
| Uso de rota alternativa | Não inventar escolha falsa | Logs de sessão e relato de jogadores |
| Recompensa utilizada na base | Relevância fora do POI | Projeto/crafting real |
| Conteúdo por baú | Economia e chance teórica | Loot tables e aberturas reais |
| Persistência com dois jogadores | Sem dupe/reset | Teste de relog e restart |
| Performance | Sem worldgen caro ou vazamento | Profiling de worldgen/TPS |
| Regressão | Não quebrar 5.7/Overworld vanilla | CI + dedicated server |

**Seeds iniciais de regressão conhecidas:** `14011`, `20261108`, `-904732`, `73199013`. Isso **não** estabelece coordenadas de POIs novos. `/locate` acha um start; **não** prova que entrada, centro e baú estejam acessíveis.

**Amostra necessária:** quatro seeds como mínimo inicial, **mais de um candidato por estrutura e por seed**, incluindo candidatos rejeitados por terreno. Uma estrutura encontrada por seed é evidência insuficiente para distribuição/raridade.

## 14.2 Testes obrigatórios por POI

| Teste | Marco | Abrigo | Galeria | Câmara |
|---|---|---|---|---|
| NBT/JSON e codecs 26.3 | Sim | Sim | Sim | Sim |
| Registro nativo e /locate | Se structure | Sim | Sim | Sim |
| Suporte/entrada | Base | Piso/teto + 2 acessos | Entrada + 2 rotas | Entrada + 2 abordagens |
| Fluido contido | Checar | Checar | **Crítico** | Conforme layout |
| Recompensa independente de baú | Sim | Sim | Sim | Sim |
| Loot persistente | Sem baú preferido | Se houver | Se houver | Se houver |
| Rotação/mirror | Sim | Sim | Sim | Sim |
| Recurso único condicionado à 6.6 | Não | Não | Somente opcional | Ainda não aprovado |
| Save/restart, multiplayer | Sim | Sim | Sim | Sim |
| Teste Survival natural | Sim | Sim | Sim | Sim |
| Compatibilidade vanilla/dimensões | Sim | Sim | Sim | Sim |

### 14.3 Procedimento de QA futuro

1. Gerar **mundo novo** com a branch/seed exata.
2. Validar datapack, structure registry, NBT, loot references.
3. Usar `/locate structure` em Spectator para **inspeção**, nunca para afirmar descoberta natural.
4. Auditar apoio, volume, entrada e vias, containers, estado de fluidos e chunks.
5. Repetir em novas regiões das quatro seeds e registrar rejeições.
6. Abrir baús em mundo descartável e validar loot efetivo/persistência após restart.
7. Testar escolha de rotas e recompensa em Survival **sem comandos de localização**.
8. Repetir com duas contas; comparar com build do mod sem POIs para efeitos colaterais de worldgen.
9. Fazer snapshots e capturas dos quatro lados/interior com texturas vanilla e sem shader.
10. Não liberar se houver POI de grande risco sem saída acessível.

---

# 15. COMANDOS DE TESTE — NÃO INVENTAR O QUE NÃO EXISTE

**Na Task 6.5, os quatro IDs de POI ainda não existem.** Portanto não são comandos executáveis:
`/locate structure dreamsdimensions:marked_stone_marker`,
`/locate structure dreamsdimensions:field_shelter`,
`/locate structure dreamsdimensions:sunken_gallery`,
`/locate structure dreamsdimensions:threshold_chamber`.

**Comandos vanilla disponíveis atualmente** para preparação de laboratório:

```mcfunction
/seed
/gamemode spectator
/gamemode survival
```

Quando cada POI for implementado e o ID verificado, a Task de código **deverá fornecer comandos literais sem placeholders** de `/execute in minecraft:overworld positioned ... run locate structure ...`, `/tp` para coordenadas **reais da seed** (primeiro Spectator, depois Survival), `/setblock` ou `/give` quando aplicável, e `/loot` com tabela validada.

Não usar `/place structure` para comprovar natural worldgen. `/locate` não encontra features/minérios isolados. Coordenadas das ruínas dimensionais de seed de CI não são coordenadas de POI-01–04 do usuário.

---

# 16. GRAFO DE DEPENDÊNCIAS E PRS FUTURAS

```text
[Minecraft vanilla] ------------------------+-----------------------+
         |                                  |                       |
         v                                  v                       v
[F0 Pedra Marcada]                   [F1 Líquen]            [Dream Ore atual]
         |                                  |                       |
         +---> [POI-01 Marco]               +---> [M1 Mariposa]      +--> [Colar/Despertador]
         |                                  |                                  |
         +---> [POI-02 Abrigo] <------------+ (opcional)                       +--> [Sonhos]
         |
         +---> [POI-03 Galeria] <-- [M2 Habitante opcional]
                       |
                 [prova de rotas, suporte e geração subterrânea]
                       |
                       +---> [POI-04 Câmara] <-- [M3 Guardião opcional, depois]
                                    |
                             [recompensa concreta 6.6]
```

**Este grafo é de dependência de conteúdo/desenvolvimento, não uma campanha linear.** Jogador pode descobrir Abrigo antes do Marco, completar Galeria sem matar M2 e nunca entrar numa Câmara.

| Fase proposta à Task 6.9 | Entrega verificável | Dependência |
|---|---|---|
| **F2-POI.1** | Primeiro Marco com material real e mundo novo | F0 + worldgen/testes 6.8 |
| **F2-POI.2** | Abrigo com utilidade física, 2 entradas e recompensas coerentes | Arquitetura/testes do Marco |
| **F3-POI** | Galeria com duas rotas, passagem segura, água contida, sem mob custom inicialmente | QA subterrâneo próprio |
| **F4-POI** | Câmara com caminhos reais e recompensa 6.6, sem Guardião inicialmente | Galeria + 6.6 |
| **F4-POI-M3** | Guardião opcional, apenas se melhorar o encontro | M3 6.4 + A/B |
| **Variantes** | Novas topologias/contextos após primeira homologação | Uma estrutura funcional por família |

**Não** implementar quatro templates simultâneos “para depois acertar loot e terreno”. Cada fatia precisa de loop natural fechado e teste humano antes de ampliar o catálogo.

---

# 17. DECISÕES EM ABERTO PARA 6.6–6.9

1. **6.6:** que recompensa horizontal de Câmara merece existir, se alguma? Sem consumidor real, não adicionar novo item.
2. **6.6:** algum método de arquitetura precisa de descoberta em POI, ou Recipe Book/stonecutter já bastam?
3. **6.7:** desenhos de planta e paletas finais; fazer NBT vanilla-first antes de texturas custom novas.
4. **6.8:** espaçamento, separação, salts, biomas, alturas, peso de variantes, distância e tempo de descoberta.
5. **6.9:** Marco como `structure` ou feature, conforme forma final; só structure possui /locate.
6. **6.9:** se Galeria/Câmara exigirão pré-verificação técnica de terreno além de jigsaw nativo.
7. **6.9:** quais validações automáticas de suporte, entrada, fluidos e loot serão obrigatórias no CI.
8. **Futuro:** lore de quem construiu ruínas. Não canonizar Observadores por inércia.
9. **Futuro:** se Guardião de Pedra merece ser integrado à Câmara após encontro base sem mob.
10. **Futuro:** ampliar variantes somente se topologia/risco mudam de verdade, não por recoloração.

---

# 18. CRITÉRIOS DE ACEITE DA TASK 6.5

## Documentação — CONCLUÍDA

- [x] Revisou as quatro tasks de planejamento anteriores e a Bible.
- [x] Auditou estrutura nativa, loot e limitações da Task 5.7.
- [x] Selecionou quatro tipos de POIs de Overworld com identidades diferentes.
- [x] Marco ensina geologia e entrega amostra sem baú.
- [x] Abrigo oferece utilidade física, duas entradas, suprimentos coerentes.
- [x] Galeria oferece escolhas de travessia e exige prova de geração subterrânea.
- [x] Câmara é desafio opcional Mid/Late, sem item final nem boss obrigatório.
- [x] Separou recompensas ambientais garantidas de loot aleatório.
- [x] Definiu variantes topológicas, direção arquitetônica e pipeline NBT.
- [x] Vinculou M1/M2/M3 sem criar dependência de drop.
- [x] Preservou F0/F1, Argamassa e fluxo Colar/Despertador.
- [x] Previu suporte, entrada, fluidos, rotação, terreno, saves e multiplayer.
- [x] Não fixou chances/espaçamentos sem medições.
- [x] Preparou o grafo 6.6–6.9 e regras de execução por fatias.
- [x] Nenhum NBT, bloco, item, estrutura ou código foi implementado nesta task.

## Gameplay/QA — AINDA NÃO EXECUTADO

- [ ] POI-01 gerado naturalmente, visto/extraído em Survival sem comando.
- [ ] POI-02 com duas entradas e base funcional, sem terreno quebrado.
- [ ] POI-03 com rotas navegáveis, água contida e saída real.
- [ ] POI-04 com recompensa de 6.6, duas abordagens e sem boss obrigatório.
- [ ] Estruturas e chests persistem com dois jogadores e após restart.
- [ ] Várias ocorrências por seed comprovam distribuição e suporte.
- [ ] Não existe loot/receita essencial baseado em um baú raríssimo.
- [ ] Dedicated server, CI, saves antigos, vanilla e dimensões passaram regressão.

**Esta Task conclui um documento de planejamento.** Nenhum POI novo existe em jogo só por causa desse commit.

---

# 19. FONTES, PRECEDÊNCIA E PRÓXIMA TASK

### Referências internas

1. [Game Design Bible — Task 6.0](GAME_DESIGN_BIBLE_TASK_6_0.md)
2. [Pilares de Gameplay — Task 6.1](OVERWORLD_GAMEPLAY_PILLARS_TASK_6_1.md)
3. [Recursos, Geologia, Flora e Blocos — Task 6.2](OVERWORLD_RESOURCES_GEOLOGY_FLORA_BLOCKS_TASK_6_2.md)
4. [Itens e Estações — Task 6.3](OVERWORLD_ITEMS_CONSUMABLES_TOOLS_STATIONS_TASK_6_3.md)
5. [Ecologia e Mobs — Task 6.4](OVERWORLD_ECOLOGY_MOBS_HABITATS_TASK_6_4.md)
6. [Ruínas e Baús de Recuperação — Task 5.7](DREAM_RUINS_TASK_5_7.md)
7. [Roadmap](GAMEPLAY_ROADMAP.md)

### Código/recursos auditados

- `src/main/resources/data/dreamsdimensions/worldgen/structure/blue_dream_ruins.json`
- `src/main/resources/data/dreamsdimensions/worldgen/structure/fragmented_dream_ruins.json`
- `src/main/resources/data/dreamsdimensions/worldgen/structure_set/*.json`
- `src/main/resources/data/dreamsdimensions/worldgen/template_pool/ruins/*.json`
- `src/main/resources/data/dreamsdimensions/structure/ruins/*.nbt`
- `src/main/resources/data/dreamsdimensions/loot_table/chests/*.json`
- `scripts/ci/check_dream_ruins.py` e `scripts/ci/audit_dream_ruins_runtime.py`

### Fontes oficiais 26.3 (revalidar ao codificar)

- [NeoForge Primer 26.3](https://docs.neoforged.net/primer/docs/26.3/)
- [NeoForge Worldgen](https://docs.neoforged.net/docs/worldgen/)
- [NeoForge Loot Tables](https://docs.neoforged.net/docs/resources/server/loottables/)
- [Minecraft Java 26.3](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3)

**Próxima Task: 6.6 — Grafo de Crafting, Processamento e Progressão Material.** Ela deverá decidir recompensas que mereçam existir, ingredientes, transformação de materiais, custo, fontes alternativas e dependências, sem exigir boss/estrutura raríssima para os recursos fundamentais.

---

# 20. VEREDITO DE DIREÇÃO

O **Marco** ensina a reconhecer uma anomalia geológica; o **Abrigo** mostra vestígios de trabalho e entrega utilidade imediata; a **Galeria** transforma terreno em escolha de percurso; a **Câmara** permite um desafio opcional de Mid tardio/Late com recompensa horizontal concreta, **sem encerrar o mod**.

Essas estruturas devem provocar a pergunta **“o que há ali e o que posso aprender ou conquistar?”** e não apenas **“quantos itens vêm no baú?”**. Esse é o critério para transformar o Overworld em um lugar que o jogador deseja continuar explorando.

---

*Fim — Task 6.5. Documento de level design. Nenhum novo POI foi implementado.*
