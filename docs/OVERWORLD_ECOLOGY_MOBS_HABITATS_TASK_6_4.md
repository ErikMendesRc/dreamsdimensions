# TASK 6.4 — ECOLOGIA, MOBS E HABITATS DO OVERWORLD

## Direção de criaturas, nichos ecológicos, comportamento e desafios Early / Mid / Late

**Projeto:** DreamsDimensions  
**Tipo:** Game Design / Ecology / Mob Behavior & Encounter Design  
**Versão:** 1.0 — decisões de prototipagem, **sem implementação**  
**Data:** 12 de novembro de 2026  
**Branch:** `migration/minecraft-26.3`  
**HEAD auditado:** `05df377a6720302f34f5dc87807db066c6a3b5f8`  
**Documentos superiores:** [Game Design Bible — 6.0](GAME_DESIGN_BIBLE_TASK_6_0.md), [Pilares do Overworld — 6.1](OVERWORLD_GAMEPLAY_PILLARS_TASK_6_1.md), [Geologia/Flora/Blocos — 6.2](OVERWORLD_RESOURCES_GEOLOGY_FLORA_BLOCKS_TASK_6_2.md), [Itens/Estações — 6.3](OVERWORLD_ITEMS_CONSUMABLES_TOOLS_STATIONS_TASK_6_3.md)  
**Próxima Task:** **6.5 — Estruturas e Descoberta Ambiental**  
**Escopo do commit:** **apenas documentação**; sem modelos, entidades, IA, Java, NBT, datapacks, worldgen, receita, loot ou alteração de spawn. Não realizar merge na `main`.

> **Tese de direção:** criaturas do Overworld existem para que o mundo pareça **vivo, reconhecível e capaz de reagir ao jogador**. Cada espécie tem habitat, hábitos, sinal de aproximação, uma escolha e uma consequência. Matar o mob não é sua principal utilidade. Early oferece descoberta, Mid abre interações e risco moderado, Late traz domínio e escolhas mais complexas; não há End Game ou boss final próprio. Os sonhos são uma possibilidade importante, não a única recompensa do mod.

---

# 0. DECISÕES EXECUTIVAS

## 0.1 Seleção de fauna

| Ordem | Código | Nome provisório | Papel | Estágio | Decisão |
|---|---|---|---|---|---|
| **M1** | MOB-01 | **Mariposa dos Vestígios** | Criatura neutra que pousa em flora de habitat; orienta observação | Early → Mid | **SELECIONADA PARA ESPECIFICAÇÃO**, dependente de F1 Líquen |
| **M2** | MOB-02 | **Habitante das Fendas** | Criatura cavernícola territorial; oferece aviso, fuga e confronto | Mid | **CANDIDATO CONDICIONADO AO PROTÓTIPO** |
| **M3** | MOB-03 | **Guardião de Pedra** | Defensor de ponto de interesse com percurso alternativo | Mid tardio → Late | **CONDICIONADO À TASK 6.5**, não é boss |
| — | MOB-04 | **Observador Esquecido** | Encontro de narrativa e mistério | Indefinido | **ADIADO**; civilização e origem não canonizadas |
| — | — | “Zumbi de cristal”, mob-minério ambulante, chefe de ruína obrigatório | Nenhum | Todos | **REJEITADOS** |

**Uma espécie por fatia vertical**, não um pacote obrigatório de três mobs. M1 é a única criatura prioritária para avançar a especificação após o Líquen funcionar em Survival. O projeto não precisa de novo item de drop para justificar nenhuma delas.

## 0.2 Critério de sucesso

Em jogo, sem comandos, o jogador deve poder:

1. identificar ao menos um sinal do habitat **antes** de encontrar a espécie;
2. reconhecer uma postura ou atividade do mob sem tooltip;
3. decidir observar, aproximar, recuar, contornar, construir habitat ou enfrentar;
4. obter informação ambiental, melhoria de ecossistema, experiência exploratória ou recompensa coerente **sem kill obrigatório**;
5. repetir essa leitura em outra região, sem depender de estrutura única;
6. continuar interessado no Overworld mesmo sem equipar Colar ou Despertador.

**Falha eliminatória:** “o mob simplesmente nasce, anda e dropa um ingrediente raro” não é um pilar ecológico.

## 0.3 Convenções de status

- **EXISTENTE/AUDITADO:** comprovado em repositório.
- **DIRETRIZ FIXADA:** decisão de 6.0–6.3.
- **SELECIONADA:** merece ficha e protótipo quando dependências estiverem prontas; não existe no jogo.
- **CONDICIONADO:** precisa passar teste de habitat, comportamento e valor.
- **ADIADO/REJEITADO:** não reservar ID nem produzir assets.
- **HIPÓTESE:** distribuição, probabilidade, dano, velocidade e odds não mensurados.
- **PENDENTE DE QA:** exige implementação e sessões reais de Survival.

---

# 1. AUDITORIA REAL DA ARQUITETURA

## 1.1 Código existente

- `ModEntities.java`: registra apenas `dreamsdimensions:fragmento_errante` e `dreamsdimensions:vigia_adormecido`, ambos associados ao **Dreamscape**. **Não há mob próprio do Overworld**.
- `entity/habitat/DreamMobHabitat.java`: contrato funcional de validação de habitat no servidor.
- `entity/habitat/FloatingIslandHabitat.java`: implementação específica das ilhas suspensas; **não serve** como predicado de caverna do Overworld.
- `entity/spawn/MobPopulationProfile.java`: perfil imutável com espécie, dimensão, limites, distâncias e seleção de candidatos.
- `entity/spawn/DreamMobPopulationService.java`: coordenador único server-only por jogador, com verificação de chunks carregados e limites regionais. É complemento para ilhas já visitadas, **não** fundamento obrigatório do spawn de Overworld.
- `entity/movement/DreamFlyingMoveControl.java`, `FlightProfile.java` e `navigation/SafeGroundNavigation.java`: código reutilizável **somente após comparar a física da nova espécie**, não pela simples existência.
- GeckoLib 5 já é usado no projeto. Uma Mariposa não deve reaproveitar a animação de flutuação do Fragmento nem o pacto do Vigia.
- Curios integra o Colar; **não** criar slot/acessório de mob que interfira no equipamento de sono/entrada dimensional.

**Baseline de conteúdo:** F0 Pedra Marcada e F1 Líquen são **famílias selecionadas em design, ainda não implementadas**. Argamassa é item selecionado para especificação; Cinzel e Leitor de Colônia são condicionados. O mob não poderá depender de uma planta não existente em runtime.

## 1.2 Lições do Dream Mob Framework (Task 4.4)

O framework separa:
- entidade/IA (comportamento da espécie);
- habitat (predicado físico e ecológico);
- registro/atributos e spawn placement;
- população (quando houver necessidade de complemento);
- renderização/animação;
- telemetria e testes.

**Contrato:** aproveitar essa divisão, não replicar classes e listeners por mob. O coordenador existente não deve ser alterado para abrigar spawn do Overworld automaticamente sem provar necessidade e custo.

## 1.3 Lições da auditoria de spawn (Task 5.3.1)

O Dreamscape mantém **duas fontes complementares** de spawns: configuração de bioma e coordenador Java. O cap local do coordenador **não limita globalmente** spawns naturais adicionais. A Task 5.3.1 não provou superpopulação, portanto não removeu nenhuma origem.

Para Overworld, a ordem é diferente:

1. usar sistema natural data-driven apropriado à versão;
2. testar regras físicas e de habitat;
3. medir ocorrência em novos e antigos chunks;
4. só considerar perfil adicional de população se os encontros naturais forem insuficientes;
5. quando necessário, usar **o coordenador existente** e medir interação das duas fontes.

**Não** criar um `PlayerTickEvent` diferente para cada criatura, nem copiar spawn weights/periodicidade do Fragmento.

## 1.4 Atenção crítica: spawn em Minecraft/NeoForge 26.3

O Minecraft 26.3 moveu `MobSpawnSettings` para `EnvironmentAttributes#NATURAL_MOB_SPAWNS`. O `OVERLAY` por categoria pode substituir **todas as entradas da categoria** na camada inferior, e não acrescentar somente um mob. Um datapack mal estruturado pode silenciar vacas, porcos ou mobs vanilla.

**Decisões técnicas para a futura PR:**
- consultar [NeoForge Primer 26.3](https://docs.neoforged.net/primer/docs/26.3/#mob-spawn-settings-environment-attribute);
- avaliar o `neoforge:add_spawns` documentado em [Biome Modifiers](https://docs.neoforged.net/docs/worldgen/biomemodifier/) como via aditiva não destrutiva;
- validar o serializer efetivo na versão do projeto e conferir a lista de spawns resultante após data pack load;
- registrar `RegisterSpawnPlacementsEvent` corretamente; sem registro, entidades podem nascer em posições fisicamente inválidas;
- comparar fauna vanilla e de outros mods **antes/depois**. Não sobrescrever o `creature`/ `monster` global do Overworld.

Fontes auxiliares: [NeoForge Living Entities](https://docs.neoforged.net/docs/entities/livingentity/), [GeckoLib 5 — Entities](https://github.com/bernie-g/geckolib/wiki/Geckolib-Entities-(Geckolib5)).

---

# 2. REGRAS ECOLÓGICAS DO UNIVERSO

## 2.1 Identidade

A fronteira entre sonhos e realidade deixa vestígios materiais. Alguns seres vivos se adaptam **ao microambiente** dessas marcas. Isso não significa que cada animal é mágico nem que todo mob precisa vir do Dreamscape. O Overworld deve continuar reconhecivelmente vanilla.

**Visual:** anatomia voxel coerente; pedra, musgo, creme, terra, cinza e marcas discretas. Evitar luz neon permanente, cores oníricas extremas ou criaturas gigantes em qualquer floresta.

**Narrativa:** não explicar toda a origem dos vestígios por um NPC, profecia ou civilização canonizada antes da decisão da Bible. O encontro comunica história ambiental por comportamento.

## 2.2 Regras de existência

Todo mob novo deve responder:
- Que **habitat** explica sua ocorrência?
- Qual **recurso ou condição** modifica seu comportamento?
- Que **pista** o jogador pode perceber?
- Que **escolha** o encontro oferece?
- Qual **vantagem imediata** pode ser demonstrada em Survival?
- O jogador continua tendo essa vantagem com equipamento vanilla avançado?
- O encontro é **opcional** ou obrigatório para algum sistema?
- Quais são seu **custo de servidor** e seus limites de população?

**Nunca** criar uma criatura apenas porque um modelo feito no Blockbench ficou bonito.

## 2.3 Proteções de gameplay

- Sem drop raro obrigatório para a primeira Pedra Marcada, Líquen, Argamassa, Colar ou Despertador.
- Sem grief de construções, mineração automática de blocos de jogadores ou geração de itens raros por farm de mobs.
- Sem ataques inevitáveis de criatura descrita como neutra.
- Sem perseguição infinita ou estado de raiva que persiste através de logout e longas distâncias.
- Sem spawn incontrolável em bases, vilas ou fazendas.
- Peaceful, gamerules e regras vanilla de morte/despawn devem continuar coerentes.
- Nenhum mob necessita abrir chunks descarregados; comportamentos devem falhar de forma segura.
- Nenhum encontro obrigatório depende de duas pessoas; single-player é o padrão.
- O Overworld continua interessante quando o jogador nunca entra nos sonhos.
- Late é fase de consistência e especialização, não combate a chefe final.

---

# 3. CATÁLOGO DE MICROHABITATS DO OVERWORLD

**Decisão:** microhabitats antes de novos biomas completos. O habitat é uma relação de clima, terreno, substrato, luz, espaço e flora — não apenas “tag de floresta”.

| Código | Habitat / cenário | Predicado conceitual | Espécie | Risco |
|---|---|---|---|---|
| **HAB-01** | Parede de caverna sombreada com cobertura | Rocha e Líquen F1 naturais, espaço de pouso | Mariposa | Flora ainda não implementada; não gerar mob em qualquer bloco |
| **HAB-02** | Afloramento de Pedra Marcada, transição mata/caverna | Substrato rochoso, cobertura, ar e luz adequados | Mariposa secundária, **se correlacionado** | Não prometer minério por observação de mob |
| **HAB-03** | Fenda subterrânea habitável | Deepslate/rocha, sombra, abrigo e rota de saída | Habitante das Fendas | Spawn em casa/escavação artificial ou corredor sem fuga |
| **HAB-04** | Recinto de POI protegido | Estrutura 6.5 colocada e íntegra, volume de encontro | Guardião de Pedra | Dupes de encontro ou boss obrigatório |
| **HAB-05** | Jardim do jogador | Líquen transplantado, suporte/luz válidos | Interação/pouso de Mariposa opcional | Invasão de bases e farm AFK |

**HAB-01 é o primeiro a validar.** Não devemos gastar modelando M2/M3 se ainda não sabemos se HAB-01 pode gerar encontros naturais de forma legível e performática.

## 3.1 Habitat e falsos indícios

**Líquen não é promessa de minério.** A Task 6.2 exige correlação real antes de dizer que uma planta indica Pedra Marcada. A Mariposa poderá sinalizar **a existência do próprio Líquen ou de condições de habitat**, não Dream Ore escondido.

Se a fonte natural de Líquen for rara demais, corrigir as regras de distribuição/visibilidade primeiro. Não compensar deficiência do worldgen espalhando mariposas artificialmente pelo Overworld.

## 3.2 Custos de sondagem

Spawn predicate: olhar posição candidata, ar/água, piso, bioma e **alguns vizinhos carregados**, não procurar 500 blocos de plantas. AI de pouso: amostras locais delimitadas com intervalo e cache transitório de destino; nenhuma varredura por tick ou chunks forçados.

---

# 4. MOB-01 — MARIPOSA DOS VESTÍGIOS

**Status:** SELECIONADA PARA ESPECIFICAÇÃO APÓS F1.  
**ID candidato não registrado:** `dreamsdimensions:vestige_moth`.  
**Estágio:** Early / Mid.  
**Papel:** indicador ecológico neutro e ambientação interativa; não é um Fragmento Errante em miniatura.

## 4.1 Como o jogador a encontra

1. Em Survival, o jogador encontra uma parede ou abrigo com Líquen dos Vestígios.
2. Percebe um pequeno som/movimento e uma silhueta alada pousando perto de cobertura.
3. Observa o comportamento e descobre outra superfície próxima em que há condições parecidas.
4. Pode aproximar-se devagar; se correr ou atacar, o animal se assusta e muda de pouso ou foge.
5. Ao aprender o comportamento, passa a usar a espécie como **pista de habitat**, não como bússola.
6. Em Mid/Late, um jardim com Líquen válido pode funcionar como local de pouso ou observação — **não** como spawner infinito.

**Recompensa:** conhecimento do ambiente e vida ecológica/arquitetônica. Sem matar a Mariposa, o jogador consegue aproveitar o Líquen.

## 4.2 Silhueta, movimento e animação

Corpo pequeno, volumoso, asas grandes e legíveis, tons pedra/creme com discreta marca de vestígio. Voo curto com pouso orgânico. **Não** flutuação vertical estática igual ao Fragmento.

| Estado proposto | Gatilho | Movimento | Feedback |
|---|---|---|---|
| `PERCH` | Poleiro/folha/parede válida | Pousa e movimenta asas lentamente | Silhueta estável e som delicado |
| `SHORT_FLIGHT` | Troca de posição | Arco curto evitando sólido | Oscilação de asa/corpo |
| `FORAGE` | Intervalo de busca local | Procura outro ponto próximo com Líquen | Rota compreensível |
| `CURIOUS` | Jogador próximo e não ameaçador | Pequena aproximação e orientação | Postura inclinada |
| `STARTLED` | Dano/aproximação brusca | Fuga limitada e destino seguro | Som curto, aceleração |
| `REST` | Sem destino seguro | Aguarda sem acessar chunk novo | Sem spam de procura |

**Estados e clips são proposta, não arquivos existentes.** Para a futura task, modelos/animations GeoEntity só depois do movimento e hitbox estarem coerentes. GeckoLib 5 é preferível se as asas, pouso e fuga exigirem clipes explícitos; não impor sistema pesado a todos os mobs.

## 4.3 Fonte e spawn

- Overworld, microhabitat com **Líquen real F1** ou outro predicado ecologicamente verificável.
- Verificar **posição física** e espaço livre no teto/parede/volume de voo conforme a placement API correta.
- Evitar água/lava, blocos sólidos interceptando hitbox, cavernas com altura insuficiente e bases artificiais sem contexto.
- Não copiar peso/tamanho de grupo/altura da Dreamscape: **parâmetros ficam para 6.8**.
- Não definir horários tão restritos que o jogador só encontre a espécie após horas.
- Categoria de mob e política de despawn precisam ser escolhidas após medir mob caps e composição vanilla.
- Não spawnar por simples existência de Líquen **colocado pelo jogador**; atração/pouso posterior pode ser uma interação, não nova população.

## 4.4 Recompensas e interações secundárias

**Não criar:** `dreamsdimensions:moth_dust`, `wing_essence` ou outro ingrediente de drop só para preencher loot table.

**Hipótese condicionada:** a Mariposa pode contribuir com disseminação local lenta de Líquen ao pousar. Só autorizar se F1 já é cultivável sem mob e se o jogador observa a relação. O efeito terá limites e cooldown, não substitui insumos caros nem prolifera blocos em bases sem permissão.

Sem tame/reprodução automática; essas técnicas só entram após comportamento comprovado. Matar a mariposa não é caminho ótimo.

## 4.5 Critérios de aprovação M1

- [ ] F1 já existe e é cultivável com habitat natural comprovado.
- [ ] Pouso e voo são legíveis em cliente e no servidor, sem mob ficar parado no ar.
- [ ] Jogador identifica ao menos uma área válida de Líquen por observação, sem GPS.
- [ ] Spawn natural aparece em novos chunks e não sobrepõe fauna vanilla.
- [ ] Não atravessa sólidos, não foge para chunk não carregado e não trava pathfinding.
- [ ] Jardins construídos não se tornam spawners infinitos.
- [ ] O mob continua interessante sem um drop de ingrediente obrigatório.
- [ ] Dois jogadores podem observá-la sem duplicação ou desync.

**Caso contrário:** redesenhar IA/habitat antes de registrar assets finais.

---

# 5. MOB-02 — HABITANTE DAS FENDAS

**Status:** PROTÓTIPO CONDICIONADO.  
**ID candidato não registrado:** `dreamsdimensions:fissure_dweller`.  
**Estágio:** Mid, não ameaça universal no primeiro dia.

## 5.1 Fantasia

Criatura baixa, robusta, com placas minerais aderidas ao corpo, membros curtos e comportamento cavernícola. Parece um **habitante adaptado** a fendas, não um zumbi de pedra ou golem colossal.

**Mecânica exclusiva proposta:** territorialidade **telegrafada**. Jogador reconhece a ameaça antes de receber dano, escolhe recuar, contornar ou entrar preparado.

## 5.2 Loop de Survival

`explorar caverna → ouvir estalos/observar postura → aproximar-se do território → aviso visual/sonoro → escolher recuar/contornar/confrontar → continuar explorando`.

Uma criatura assim não precisa “guardar diamante” para ter propósito. Entretanto o encontro deve gerar decisão real: se só imita um zumbi lento, reprovar.

## 5.3 Estados de IA

| Estado | Gatilho | Comportamento | Contrajogo |
|---|---|---|---|
| `REST` | Ambiente calmo | Repousa em fenda/terreno adequado | Observar e passar com cautela |
| `ALERT` | Aproximação/intrusão | Ergue placas, vira corpo, estala | Afastar-se |
| `DEFEND` | Intrusão persistente ou agressão | Ataque curto, anunciado, sem alcance injusto | Escudo, recuo, desvio |
| `PURSUIT` | Intruso permanece dentro do perímetro | Perseguição **limitada ao território** | Sair da região |
| `RECOVER` | Jogador recua | Dissipa agressividade, volta ao repouso | Retomar rota |

Não minerar blocos; não perseguição até a base; não usar scans de minérios, efeitos globais ou controle de região fora do chunk carregado. A IA não deve depender de jogador carregando com ticket especial.

## 5.4 Habitat e economia

- Cavernas/fendas com rocha ou deepslate, piso e espaço legítimos, abrigo e uma rota viável de retirada.
- Considerar exclusão de locais construídos/iluminados conforme as regras vanilla e comparações de spawn; não exigir “detector de casa” global.
- Spawn relativamente restrito a ambientes próprios; não competir com zumbis/esqueletos por abundância sem medição.
- Nenhum drop raro obrigatório; se loot for acrescentado, dependerá de consumidor concreto em 6.6.
- Não criar dano absurdo no Early nem mob que aparece de costas sem sinal.
- Respeitar Peaceful, dificuldades, gamerules, outros mods e local caps.

**Gate:** dois caminhos de contrajogo reais, aviso legível, comportamento diferente de mob vanilla e nenhum conteúdo essencial bloqueado por M2.

---

# 6. MOB-03 — GUARDIÃO DE PEDRA

**Status:** CONDICIONADO À TASK 6.5.  
**ID candidato não registrado:** `dreamsdimensions:stone_warden`.  
**Estágio:** Mid tardio/Late, encontro raro **de estrutura**, não espécie espalhada pelo mundo.

## 6.1 Papel

Protetor contextual que valoriza arquitetura e investigação de POI, sem tornar o DreamsDimensions RPG linear ou jogo de chefe final. Sua natureza (rocha viva, automato antigo ou outro fenômeno) ainda **não é canonizada**.

**Diferença do Vigia Adormecido:** Vigia do Dreamscape possui vínculo/proteção de queda; Guardião no Overworld teria território arquitetônico e contrajogo próprio, sem pacto ou teleporte.

## 6.2 Duas rotas obrigatórias

- **Contornar:** estrutura permite investigar outro percurso ou mecanismo, sem matar o Guardião.
- **Enfrentar:** ataques com wind-up, janelas de recuperação e retirada possível; equipamento avançado melhora eficiência, mas não exige invulnerabilidade ou esponja de HP.

O benefício deve vir **da estrutura** (descoberta, técnica, material contextual), não de um item “único do boss” obrigatório para todas as receitas.

## 6.3 Persistência e Spawn

- Não adicionar spawn global por bioma.
- Só existe quando estrutura 6.5 aprovada possuir espaço, risco anunciado, rota de fuga e justificativa.
- Proteger contra duplicação por reload, logout e segundo jogador.
- Se derrotado, não renascer automaticamente para produzir loot infinito.
- Nunca usar chunks sempre carregados para proteger uma arena.
- Não fechar narrativa com boss bar/final de campanha.

**Gate:** POI precisa ter função antes do protetor. Se uma câmara for divertida sem M3, talvez não precise de guardião.

---

# 7. MOB-04 E IDEIAS FUTURAS — NÃO CANONIZAR ANTES DO TEMPO

**Observador Esquecido: ADIADO.** Civilização, NPC, consciência e a própria origem dos vestígios permanecem perguntas do universo. Não criar personagem falante apenas para expor lore.

Outros nichos hipotéticos: criatura aquática de rio/caverna alagada; pequeno decompositor orgânico; ave de rotas locais; predador que reage a vestígios. **Nenhum** está aprovado. Cada um terá de apresentar habitat, loop, recompensa e diferencial perante fauna vanilla antes de entrar na fila de modelos.

---

# 8. MATRIZ TRANSVERSAL: ECOLOGIA NÃO É UM SISTEMA ISOLADO

| Origem | Conexão | Aplicação e limite |
|---|---|---|
| **Líquen F1 → Mariposa M1** | Habitat e pouso | Indicação da planta/condição, não scanner de minérios |
| **Pedra Marcada F0 → M1** | Possível suporte/paisagem | **Somente** se houver correlação real de distribuição |
| **M1 → Construção** | Jardim atrativo | Pouso/ambiente, **sem** spawn ilimitado por bloco colocado |
| **Fendas geológicas → M2** | Território e exploração | Risco local, não guardião de Pó Onírico |
| **M2 → Exploração** | Escolha de aproximação | Fuga/contorno/confronto, sem drop obrigatório |
| **POI 6.5 → M3** | Encontro e arquitetura | Recompensa da estrutura, persistente, sem boss |
| **Argamassa/Cinzel 6.3 ↔ mobs** | **Sem receita obrigatória** | Construção não deve depender de matar criaturas |
| **Sonhos ↔ fauna Overworld** | Lore e possibilidade futura | Nenhum mob novo é chave para viagem/retorno |

**Regra de qualidade:** se a Mariposa “indica minério” mas só lê Líquen local, o design está mentindo. Se o Guardião “guarda uma ruína” mas spawna fora dela, o spawn está errado. O comportamento precisa corresponder à realidade da geração.

---

# 9. PROGRESSÃO EARLY, MID E LATE

| Linha | Early | Mid | Late |
|---|---|---|---|
| **Ecologia** | Identificar Líquen/habitat, observar Mariposa | Reproduzir jardim e estudar hábitos | Projetar paisagismo e habitats com intenção |
| **Exploração** | Ler pistas e caminhar por microambientes | Navegar territórios de M2, selecionar rotas | Conhecer perigos e explorar POIs opcionais |
| **Combate** | Nenhum mob novo precisa punir primeiro dia | Avisos, defesa e fuga são válidos | Domínio tático, não invulnerabilidade |
| **Recursos** | Não matar fauna para obter Pedra Marcada | Drops apenas com consumidores definidos | Farms não substituem exploração/itens raros |
| **Infraestrutura** | Construção e cultivo são loops autônomos | Jardins, redstone botânica se aprovada | Bases expressivas com fauna e produção própria |
| **Viagens oníricas** | Não necessárias para ecologia | Ocasionalmente possíveis por outro ramo | Consistentes por preparação, não pelo loot de mobs |

Nenhuma coluna representa um End Game. O jogador pode investir em agricultura, construção, exploração e fauna indefinidamente.

---

# 10. CONTRATO DE SPAWN, HABITAT E PERFORMANCE

## 10.1 Ordem de implementação

1. Descrever habitat e comprovar com **F0/F1 reais** quando necessário.
2. Registrar EntityType/atributos e Renderers da espécie.
3. Registrar SpawnPlacement apropriado à física.
4. Incluir spawn natural de modo **aditivo** em biomas aplicáveis.
5. Medir distribuição e sobreposição com fauna vanilla em mundo novo e antigo.
6. Só se faltarem encontros avaliar perfil adicional no `DreamMobPopulationService` sem listener novo.
7. Validar multiplayer, unload/restart, Peaceful/gamerules e performance.

## 10.2 População e limites

- Não definir `MobCategory`, spawn weight, group count, hitbox e vida por cópia de outro mob.
- Manter caps e despawn coerentes com ecologia e custos; mobs não podem tomar todas as vagas de fauna vanilla.
- Separar natural spawn, spawn durante worldgen, `/summon`, estrutura e eventual coordenador na telemetria. Não confundir origem de eventos.
- O coordenador possui limite **local**, não garante limite global somado à população vanilla.
- Com dois jogadores na mesma região, não dobrar população por verificação independente de tick.
- Jogador espectador não deve ser argumento para gerar novas entidades de laboratório involuntariamente.

## 10.3 Predicados locais e IA

- Validar dimensão Overworld, biome/tag, bloco de piso ou suporte, ar/fluido, espaço e poucos vizinhos **carregados**.
- Não carregar chunks ao verificar habitat nem rastrear jogador a distâncias enormes.
- Voo/pouso: buscar destino em raio e intervalo pequenos, sem world scan a cada tick.
- Território: armazenar estado mínimo, com reset por timeout; não manter perma-aggro.
- POI: spawning/persistência fica sob contrato próprio da Task 6.5.
- Usar GeckoLib 5 apenas onde poses/ataques/voo realmente melhoram gameplay; não adicionar dependência paralela.
- Logs de spawn e estado **opt-in**, não imprimir cada movimento em produção.

## 10.4 Convivência com mods/claims

Não quebrar bloco, converter Pedra Marcada, colher planta alheia, ativar estruturas ou interagir com contêiner de terceiro pela IA. Se algum comportamento futuro alterar blocos, precisará de contrato de gamerule, permissões e edição de mundo separado. Nesta 6.4, **nenhum mob possui ação de grief ou coleta que modifique mundo**.

---

# 11. DIREÇÃO DE VOXEL E ANIMAÇÕES

| Espécie | Anatomia/escala conceitual | Clipes propostos | Teste de qualidade |
|---|---|---|---|
| **M1 Mariposa** | Corpo compacto, duas asas amplas legíveis, pernas/pouso discretos | `perch_idle`, `takeoff`, `flight`, `land`, `startled` | Voo orgânico com movimento e pouso; não hover estático |
| **M2 Habitante** | Criatura de solo baixa e robusta, placas de rocha, membros curtos | `idle`, `walk`, `alert`, `defend`, `recover` | Postura de alerta informa contrajogo |
| **M3 Guardião** | Silhueta de proteção rochosa proporcional a POI | `idle`, `awaken`, `windup`, `attack`, `recover` | Ataque anunciado, sem massa gigante de boss |

Paleta neutra e detalhes sutis, textura voxel compatível com vanilla, sem brilho que transforme toda a caverna em festa neon. Hitbox, escala e movimento devem concordar.

**Clipes são propostas para futuros assets, não nomes encontrados no repositório.** Blockbench e GeckoLib só entram depois que a função de gameplay e o mob forem aprovados.

---

# 12. RECOMPENSA, LOOT E RARIDADE

**Regra:** a maior recompensa inicial de M1 é descobrir/entender habitat. M2 recompensa leitura de risco/rota. M3 recompensa explorar POI por mais de um caminho.

| Mob | Recompensa primária | Drop exclusivo agora? | Benefício continuo |
|---|---|---|---|
| M1 | Pista de Líquen e presença natural | **NÃO** | Jardins, observação e exploração ecológica |
| M2 | Decisão de contorno/recuo/confronto | **NÃO** | Conhecimento de cavernas e rotas |
| M3 | Encontro de estrutura opcional | **NÃO** | POIs de risco/recompensa situacional |

Um drop temático futuro pode ser aprovado em 6.6 se existir consumidor real, fonte equilibrada e alternativa sem matar animais pacíficos quando necessário. **Não** gerar Dream Dust ou Cristal Velado por farms de criaturas. Não criar um loot de chefe que desbloqueia todas as receitas do mod.

**Nenhum** item existente — Colar, Despertador, Argamassa, Cinzel — exigirá abater um desses mobs nesta etapa.

---

# 13. PROTOCOLOS DE PLAYTEST DE EXPERIÊNCIA

## 13.1 A — Observação do habitat (M1)

**Cenário:** novo mundo com F1 real, jogador em Survival, sem spawn por comando.

- Vê cobertura vegetal, encontra Mariposa, observa pouso e identifica segunda superfície válida.
- Não precisa matar, colocar sensor, usar F3 ou abrir Wiki.
- A criatura muda de comportamento quando perturbada; não atravessa teto nem foge para chunk descarregado.
- Se M1 apenas decorar sem fornecer informação, manter como ambientação **somente se** o custo/valor visual justificarem, ou redesenhar.
- Repetir com outro jogador e em outro habitat/seed.

## 13.2 B — Jardim do jogador (M1, opcional)

- F1 já cresce e pode ser colhido sem mob.
- Mariposa pode mostrar pouso/atração local sem spawn permanente incontrolável.
- Remover condição de habitat não destrói mundo nem causa loop de busca.
- Nenhum material técnico raro nasce dessa interação.

## 13.3 C — Território de caverna (M2)

- Antes de dano, jogador percebe som e postura.
- Pode recuar/contornar ou enfrentar.
- Mob não segue até superfície/base por dezenas de chunks.
- Não destrói blocos, não ignora Peaceful e não spawna em todas as casas subterrâneas.
- Comparação com zombie/silverfish: precisa demonstrar uma **decisão diferente**, não só mais HP.

## 13.4 D — POI protegido (M3 condicionado)

- Estrutura 6.5 precisa estar íntegra, com entrada, rota de fuga e recompensa contextual.
- Evitar e enfrentar são duas abordagens reais.
- Derrotar guardião não produz repetição infinita de loot ao entrar/sair.
- Se a estrutura for divertida sem mob, considerar cancelar M3.

---

# 14. QA TÉCNICO FUTURO

| Sistema | Teste requerido |
|---|---|
| Registry/cliente | IDs, model/texture/animations, renderer, tradução, servidor dedicado sem crash |
| Spawn natural | Sem /summon, regras da versão 26.3, spawns vanilla preservados |
| Habitat | Vizinhos carregados, colisão, altura, luz, fluidos, terreno artificial |
| IA | Estados, transições, voo e navegação, nenhum pathfinding infinito |
| População | Multiseed, bioma, mob cap, despawn, dois jogadores, old chunks |
| M1 ecologia | Pouso real em Líquen, nenhum “minério garantido” falso |
| M2 desafio | Aviso, fuga, defesa, ausência de grief e spawn excessivo |
| M3 POI | Integridade, persistência, não renovação, bypass |
| Economia | Drop não obrigatório, sem dupe ou recursos raros infinitos |
| Performance | TPS, custo de spawn predicates, goals e render em regiões povoadas |
| Saves | Chunk unload/reload, restart, mobs persistentes e compatibilidade |
| Regressão | Fragmento Errante/Vigia continuam com mesmo spawn/IA e pacto |

### Seeds e métricas

Seeds existentes de regressão: `14011`, `20261108`, `-904732`, `73199013`. **Não são coordenadas de novos mobs**, pois nada foi registrado.

Métricas após implementação: distância/tempo até primeiro encontro, distribuição de habitats, encontros por sessão, coocorrência com Líquen/Pedra Marcada **realmente gerados**, quantidade de criaturas por região, efeitos em spawns vanilla, tempos de aviso/fuga, TPS e custo servidor com multiplayer. Medir mediana e dispersão (P90/P95 com amostra adequada), registrar condições, seeds, versão e se a amostra foi natural/forçada.

### Comandos de laboratório

**Não criar comandos /summon para os IDs candidatos**: eles ainda retornarão erro. Nas futuras tasks de código, fornecer comandos completos verificados `/summon`, `/give` (quando houver item), `/tp` com coordenadas **da seed testada**, `/locate` somente se existir estrutura nativa registrada, e `/data get entity` quando pertinente. O sucesso de `/summon` prova registro, **não** spawn natural.

Para auditoria de mundo sem inventar posições:

```mcfunction
/seed
/gamemode spectator
/gamemode survival
```

O teste natural precisa de personagem Survival em região válida, não somente spectator/Creative.

---

# 15. RISCOS CRÍTICOS E CONTRAMEDIDAS

| Risco | Mitigação | Gate P0 |
|---|---|---|
| Mob só existe para dropar | Definir observação/território/contrajogo antes de loot | Sem loop real, não implementar |
| M1 não encontra Líquen | F1 primeiro; ajustar distribuição e predicado, não spam de spawn | Habitat natural impossível |
| M1 “encontra minério” falso | Só vincular a flora/condições medidas | Pista que não corresponde ao terreno |
| Spawn NeoForge 26.3 remove fauna vanilla | Via aditiva e inspeção do ambiente efetivo | Regressão de vacas/pigs/morcegos/etc |
| População duplica com coordenador | Medir duas fontes separadas | Cap/telemetria incoerentes |
| Mob entra em bases | Predicado natural, limites e testes de interiores | Spawn constante na casa |
| M2 vira zumbi lento | Território/aviso/fuga em A/B real | Dano sem aviso, nenhum contrajogo |
| M3 vira boss obrigatório | POI opcional e bypass | Chave exclusiva do mod |
| M3 ressurge em reload | Persistência de encontro e loot | Farm infinita |
| IA carrega chunks/varre mundo | Busca local, loaded-check, cooldown e budgets | Queda TPS e chunk force-load |
| Mobs alteram construções | Sem grief e sem extração de recursos | Quebra/substitui bloco do jogador |
| Mudança altera Dreamscape | Regressão do framework existente | Mobs 4.x deixam de funcionar |

---

# 16. ROADMAP DE DEPENDÊNCIAS DA ECOLOGIA

| Task | Entrada da 6.4 | Próxima decisão |
|---|---|---|
| **6.5 — Estruturas e Descoberta Ambiental** | M3 condicional, M2 territorial, M1 ligado a flora | Escolher POIs e definir se algum realmente requer protetor, sem boss |
| **6.6 — Grafo de Crafting** | Nenhum drop novo aprovado | Só adicionar drop/item se consumidor e economia fechados |
| **6.7 — Direção Visual** | Silhuetas e clipes conceituais | Priorizar M1 quando F1 existir; não modelar M2/M3 antes dos gates |
| **6.8 — Economia e Raridade** | Habitats/ameaças/benefícios propostos | Medir spawn, densidade, valor observacional, contrajogo e custo de IA |
| **6.9 — Roadmap Executivo** | Dependências F0 → F1 → primeiro mob | Dividir em fatias completas com CI e QA Survival |

### Ordem candidata após 6.9

1. **F0:** Pedra Marcada com geração, obtenção e construção.
2. **F1:** Líquen com habitat, replantio e uso real.
3. **F2-ECO:** Mariposa, pouso, observação e spawn natural — **um mob por vez**.
4. **F3:** Habitante das Fendas, se passar teste de territorialidade.
5. **F4:** Estruturas de 6.5; Guardião somente quando uma delas o justificar.
6. **F5:** Expansões Mid/Late de ecossistema e encontros opcionais, sem fim próprio.

A Task 6.9 pode ajustar ordem por dependência de worldgen/estrutura, mas não pode anunciar que o animal foi implementado nesta documentação.

---

# 17. CRITÉRIOS DE ACEITE

## 17.1 Documentação — CONCLUÍDA

- [x] Auditou `ModEntities`, habitats, perfis, coordenador, framework e auditoria de spawn existentes.
- [x] Preservou as regras dos mobs do Dreamscape.
- [x] Definiu microhabitats com predicados e riscos.
- [x] Priorizou M1 ligado ao Líquen com loop independente de dimensões.
- [x] Projetou estados e animações de voo/pouso sem copiar o Fragmento.
- [x] Definiu M2 com aviso, território, contorno e combate opcional.
- [x] Condicionou M3 ao level design e persistência da Task 6.5.
- [x] Adiou MOB-04 sem canonizar civilização.
- [x] Evitou drops e componentes obrigatórios sem consumidor.
- [x] Documentou risco de spawn OVERLAY no 26.3 e política aditiva.
- [x] Incluiu multiplayer, saves, Peaceful, gamerules, loaded-chunks e performance.
- [x] Preservou Early/Mid/Late, ausência de End Game e ramo onírico opcional.
- [x] Definiu gates e comandos futuros sem inventar IDs registrados.
- [x] Nenhum código, biome, loot, NBT ou modelo foi modificado.

## 17.2 Implementação/QA — PENDENTE

- [ ] F1 Líquen realmente existe em chunks novos e pode ser cultivado.
- [ ] M1 modelada, animada, registrada e naturalmente encontrada.
- [ ] Pouso/voo e observação entregam benefício em Survival.
- [ ] Categoria/população preservam spawns vanilla.
- [ ] M2, se aprovada, comunica ameaça e permite fuga.
- [ ] POI/M3, se aprovados, não duplicam encontro/loot em reload.
- [ ] Dois jogadores e saves antigos não quebram entidades/Worldgen.
- [ ] CI/servidor dedicado/Suvival comprovam estabilidade, sem P0/P1.

**Não confundir checklist documental com homologação de gameplay.**

---

# 18. FONTES E PRECEDÊNCIA

### Internas

- [Game Design Bible 6.0](GAME_DESIGN_BIBLE_TASK_6_0.md)
- [Pilares 6.1](OVERWORLD_GAMEPLAY_PILLARS_TASK_6_1.md)
- [Recursos, Geologia, Flora e Blocos 6.2](OVERWORLD_RESOURCES_GEOLOGY_FLORA_BLOCKS_TASK_6_2.md)
- [Itens, Consumíveis, Ferramentas e Estações 6.3](OVERWORLD_ITEMS_CONSUMABLES_TOOLS_STATIONS_TASK_6_3.md)
- [Dream Mob Framework 4.4](DREAM_MOB_FRAMEWORK_TASK_4_4.md)
- [Spawn Audit 5.3.1](SPAWN_AUDIT_TASK_5_3_1.md)
- [Economia 5.6](OVERWORLD_ECONOMY_TASK_5_6.md)
- `src/main/java/com/dreamsdimensions/mod/registry/ModEntities.java`
- `src/main/java/com/dreamsdimensions/mod/entity/spawn/DreamMobPopulationService.java`
- `src/main/java/com/dreamsdimensions/mod/entity/spawn/MobPopulationProfile.java`
- `src/main/java/com/dreamsdimensions/mod/entity/habitat/DreamMobHabitat.java`
- `src/main/java/com/dreamsdimensions/mod/entity/habitat/FloatingIslandHabitat.java`

### Externas — verificação da API quando implementar

- [NeoForge 26.3 Primer / Mob Spawn Settings](https://docs.neoforged.net/primer/docs/26.3/#mob-spawn-settings-environment-attribute)
- [NeoForge Biome Modifiers](https://docs.neoforged.net/docs/worldgen/biomemodifier/)
- [NeoForge Living Entities](https://docs.neoforged.net/docs/entities/livingentity/)
- [GeckoLib 5](https://github.com/bernie-g/geckolib/wiki/Geckolib-Entities-(Geckolib5))

---

# 19. DIREÇÃO FINAL

A primeira criatura do Overworld deve **contar algo verdadeiro sobre o mundo**: a Mariposa vive e pousa onde há um habitat específico; o Habitante das Fendas mostra uma forma de vida territorial com avisos e decisões; o Guardião de Pedra só aparece se uma estrutura o justificar.

**Early revela ecossistemas, Mid ensina a compreendê-los e superá-los, Late permite coexistir e explorar com consistência.** Nenhum desses encontros encerra o mod; nenhum precisa existir apenas para um novo drop do Despertador.

**Próxima Task: 6.5 — Estruturas e Descoberta Ambiental.** A arquitetura precisa ensinar, recompensar e oferecer caminhos de exploração coerentes. Não construir outra coleção de baús com mobs guardando uma chave genérica.

---

*Fim — Task 6.4. Documento de design; nenhum novo mob foi implementado.*
