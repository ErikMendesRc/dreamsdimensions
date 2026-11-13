# Task 9.0 — Contrato Universal de Gameplay e Expansão do Overworld

**Projeto:** DreamsDimensions — Minecraft 26.3 / NeoForge 26.3 / Java 25  
**Branch:** `migration/minecraft-26.3` · **não fazer merge na `main`**  
**Tipo:** contrato de design verificável + inventário de dependências · **não é implementação de novos blocos, itens, mobs ou estruturas**.  
**Data:** 12/11/2026 · **Status:** contrato codificado, homologação da experiência ainda pendente.

> A Task 9.0 não reativa o protótipo antigo de anomalias 7.6 e não resgata documentos especulativos aposentados. O catálogo atual é o contrato de decisão para a próxima implementação, não uma Game Design Bible fingindo que o código já existe.

## 1. Objetivo que guia o mod

O **Overworld é a origem e a base independente** da exploração. O mod deve justificar uso em Survival sem exigir que o jogador visite uma dimensão de sonho. As dimensões atuais (Campo Onírico Azul e Dreamscape) comprovam sistemas técnicos e não esgotam o universo futuro, que poderá incluir sonhos pacíficos, estranhos e pesadelos.

**Progressão:** acompanhar o Minecraft vanilla, sem árvore de missões mandatória, sem End Game próprio e sem substituir a exploração livre. O contato com sonhos é **opcional e mais consistente no mid/late game**, desde que o retorno seja seguro e custoso; Late Game não torna consumíveis infinitos, viagens gratuitas ou recompensas garantidas.

**Três loops independentes com pontes opcionais:**

1. **Material/construção:** sair em busca de depósitos → colher com ferramenta adequada → processar sem duplicação → construir e recuperar materiais → decidir onde buscar novos insumos.
2. **Ecologia/descoberta:** observar habitat → reconhecer pistas visuais e comportamentos → chegar a plantas, formas naturais ou estruturas → obter recompensa proporcional → decidir nova exploração.
3. **Sonhos (posterior):** preparar-se no Overworld → acessar uma experiência onírica quando habilitada com segurança → explorar e assumir riscos → retornar mediante regra existente → aplicar recursos ou aprendizado de forma opcional no Overworld.

Uma pessoa que não se envolve com o terceiro loop deve continuar tendo motivos para usar o mod.

## 2. Evidência: o que já existe (fonte de verdade)

| Feature ou sistema | Estado verificável | Referência |
| --- | --- | --- |
| Dream Ore / Deepslate Dream Ore → Dream Dust | Implementado, gerado naturalmente em amostras reais do Overworld; obtenção por jogador pendente | [Economia 5.6](OVERWORLD_ECONOMY_TASK_5_6.md) |
| Colar dos Sonhos, Pedra Infundida, Núcleo e Despertador | Registrados com receitas, retorno protegido; testes em Survival/multiplayer pendentes | [Estado atual](ESTADO_ATUAL.md) |
| Pedra Marcada / Lapidada | F0: geração natural, mineração/corte 1:1, testes de CI; descoberta humana pendente | [Homologação F0 7.5](DREAMSDIMENSIONS_TASK_7_5.md) |
| Campo Azul / Dreamscape, mobs e ruínas dimensionais | Bases de teste implementadas; **não** são conteúdo do Overworld | [Worldgen e conteúdo](WORLDGEN_E_CONTEUDO.md) |
| Líquen, Argamassa, Marcos, Abrigos, Galeria e anomalias | **NÃO IMPLEMENTADOS**. Ideias de planejamento recuperadas e agora reavaliadas no catálogo 9.0 | [Catálogo 9.0](OVERWORLD_CONTENT_CATALOG_TASK_9_0.json) |

O Overworld já contém uma cadeia econômica de **3 Pós por Colar** e **12 Pós por Despertador**, conforme as receitas existentes. A Pedra Marcada é material de construção independente do Pó. **O Pó Onírico continua com origem natural exclusiva no Overworld**: não espalhar minério, receitas de geração de Pó ou drops garantidos dele pelos sonhos.

A Task 7.6 antiga tinha apenas experimento sem integração e foi removida na Task 8. Nenhuma anomalia está ativa.

## 3. Invariantes não negociáveis

1. **Função antes do registro:** toda feature proposta deve especificar origem → ação do jogador → custo/risco → resultado → consumidor → prova. Arte decorativa é válida, mas deve dizer claramente que é decorativa.
2. **Sem hard-lock dimensional:** nenhum sistema universal pode exigir especificamente Campo Azul ou Dreamscape. Sonhos futuros devem ser extensíveis por contratos reais, sem framework inventado de antemão.
3. **Sem progressão paralela forçada:** não criar missões obrigatórias, chefes obrigatórios, classe de poder que inutilize vanilla ou End Game próprio. Ferramenta proposta deve demonstrar vantagem distinta da picareta, do mapa, da bússola, das tesouras e do Stonecutter.
4. **Sem retorno grátis:** não conceder Despertador por regra geral, usar /tp de gameplay, contornar morte/recuperação, baratear 12 Pós por expedição ou ativar `dream_entry_enabled` antes dos gates P0.
5. **Economia rastreável:** cada transformação tem entrada e saída, rendimentos explícitos, anti-loop/anti-duplicação, ferramenta e loot. Variantes arquitetônicas não criam matéria do nada.
6. **Worldgen aditivo e seguro:** novas features só em chunks novos, sem retro-gen no save do jogador, sem tickets permanentes, sem scans de chunks em ticks, sem sobreposição destrutiva de construções.
7. **Respeitar o vanilla:** distribuir por habitat e custo, preservar tags/ferramentas e convivência com biomas de outros mods; não substituir pedra, minérios, árvores e estruturas existentes.
8. **Visual Vanilla+**: sprites de bloco em pixel art legível, distinção real do vanilla e textura contextual; visual pastel direcionado apenas a blocos exclusivos dos sonhos, sem obrigatoriedade para Overworld.
9. **Aprovação em camadas:** `STATIC_CHECK` não é `RUNTIME_OBSERVED`, nem `MANUAL_VERIFIED`. Não converter contagem de chunks em percentis de descoberta do jogador.
10. **Sem código especulativo:** não introduzir registries, abstrações globais, polling contínuo ou sistemas sem consumidor implementável na fatia atual.

## 4. Catálogo amplo e dependências verificáveis

**Fonte de planejamento versionada:** [`OVERWORLD_CONTENT_CATALOG_TASK_9_0.json`](OVERWORLD_CONTENT_CATALOG_TASK_9_0.json), formato `dreamsdimensions.overworld.contract.v1`.

O catálogo reúne **35 entradas**:
- **9 `IMPLEMENTED_CODE`**: nós de referência de registros existentes, sem declaração de validação manual.
- **3 `SELECTED`**: próxima fatia F1 da Task 9.1 — escolha de design, ainda não implementada.
- **23 `CANDIDATE`**: hipóteses de conteúdo; cada uma possui risco/custo, utilidade, consumidor e condição de aceite. Nenhuma recebeu registry ID reservado.
- **9 famílias:** geologia, recurso, flora, processamento, construção, estruturas, fauna, equipamento e sistema.

Os identificadores `F1_*` e similares são **IDs de catálogo, não IDs Minecraft**. A dependência `depends_on` significa pré-requisito de conteúdo e forma um **grafo acíclico**; quando existem origens alternativas, `source_alternatives` distingue fontes em vez de exigir todas simultaneamente.

### Matriz executiva de dependências

| Caminho | Entrada real | Etapa proposta | Consumidor concreto | Situação |
| --- | --- | --- | --- | --- |
| Mineração onírica | Dream Ore **ou** Deepslate Dream Ore | Dream Dust → Núcleo | Colar / Despertador | **Implementado**; Survival pendente |
| Material F0 | Pedra Marcada no Overworld | Stonecutter 1:1 | Pedra Marcada Lapidada | **Implementado**; Survival pendente |
| **F1 — Vestígios construíveis** | Afloramentos rochosos / Pedra Marcada | Líquen → Argamassa | Alvenaria Marcada | **Selecionado para 9.1** |
| Arqueologia ambiental | F1 + habitats | Marcos → Abrigos → Galerias | Pistas, locais opcionais, materiais | **Candidato 9.3** |
| Construção avançada | F1 Alvenaria | Lajes, paredes e ferramentas justificadas | Bases, conservação e arquitetura | **Candidato 9.4** |
| Ecologia e criaturas | Habitat geológico/vegetal | Vegetação + fauna contextual | Observação, pistas e risco justo | **Candidato 9.2/9.5** |
| Sistemas tardios | Descoberta física de estruturas | Cartografia/conservação | Investimento lateral, sem radar ou loot infinito | **Candidato 9.6** |

O objetivo **não** é forçar todos os ramos por uma única sequência de crafting. Construção, ecologia, exploração e engenharia devem oferecer caminhos paralelos.

## 5. Primeira fatia jogável selecionada: F1 / Task 9.1

**Nome:** *Vestígios construíveis: rocha, líquen e argamassa.*  
**Compromisso:** produzir uma feature testável e pequena, que termine num uso de Survival próprio, sem requerer um sistema de sonhos ou um POI complexo.

### F1.A — Líquen dos Vestígios (`F1_LICHEN`)

- **Origem:** manchas naturais e reconhecíveis em zonas rochosas/afloramentos do Overworld; variante distinta do Glow Lichen vanilla.
- **Interação:** o jogador identifica habitat e coleta com ferramenta vanilla; comportamento exato de quebra, itemização e renovação serão fixados e testados na 9.1.
- **Custo/risco:** deslocamento, escalada/mineração e densidade natural limitada; não exigir mob exclusivo.
- **Resultado:** recurso de origem ambiental, com coleta auditável.
- **Uso:** ingrediente obrigatório da Argamassa (F1.B), além de aplicação decorativa opcional.
- **Aceite:** geração real em quatro seeds, pelo menos uma sessão de descoberta humana não assistida, teste de ferramentas/drop e ausência de duplicação. Sem assumir taxa de sucesso antes da medição.

### F1.B — Argamassa dos Vestígios (`F1_MORTAR`)

- **Origem:** combinação de Líquen coletado e insumos vanilla comuns (ingrediente/quantidade definidos na 9.1).
- **Interação:** receita na Crafting Table, sem criar nova máquina ou GUI.
- **Custo:** consumo material controlado; sem receita inversa de produção ilimitada.
- **Resultado:** componente não obrigatório para a progressão vanilla.
- **Uso:** Alvenaria Marcada (F1.C).
- **Aceite:** ingredientes reais, receita e advancement coerentes, grafo acíclico, custo por 64/256 blocos e crafting manual em Survival.

### F1.C — Alvenaria Marcada (`F1_MASONRY`)

- **Origem:** Pedra Marcada já existente (F0) e Argamassa F1.
- **Interação:** fabricar, colocar, construir, minerar e recuperar.
- **Custo:** proporção rastreável, sem multiplicar matéria bruta e sem substituir a conversão F0 `marked_stone -> cut_marked_stone` 1:1.
- **Resultado:** bloco arquitetônico com identidade própria, textura legível e categoria clara.
- **Uso:** bases, refúgios e arquitetura de exploração do Overworld.
- **Aceite:** faces/costuras em paredes 5×5, ferramenta, loot, save/reload, colocação em multiplayer, compatibilidade e rendimento honesto.

**Definição de pronto F1:** o jogador, num mundo novo, encontra os dois insumos naturais, colhe, fabrica Argamassa, produz Alvenaria, constrói e recupera material sem comandos; CI mede o que é automatizável, enquanto a sessão humana comprova o loop real. Se a mecânica não for divertida ou o Líquen não for distinto da alternativa vanilla, a Task 9.1 deve ajustar/recusar o candidato antes de registrar conteúdo.

**Sem inventar aceites numéricos agora:** a Task 9.1 deve apresentar alvo inicial de densidade, medições por 4 seeds, tempo até primeira descoberta (amostra censurada), custo construtivo e tolerâncias de regressão. Não recalibrar minério existente só para tornar F1 mais acessível.

## 6. Sequenciamento pós-9.0 (planejamento, não conteúdo pronto)

| Task | Entrega vertical proposta | Dependência/gate |
| --- | --- | --- |
| **9.1 — Geologia e primeira cadeia F1** | Líquen + Argamassa + Alvenaria, com geração natural, crafting, construção e QA | Task 9.0, F0, auditoria Overworld, aceites Survival |
| **9.2 — Ecologia regional** | 1–2 habitats/plantas úteis e coerência de transições, sem redesenhar todo Overworld | F1 validada, função de cada espécie |
| **9.3 — Marcos e Abrigos** | Dois POIs distintos, acessíveis e com propósito de exploração, sem loot gratuito de progressão | Sistema de worldgen, loot e provas em quatro seeds |
| **9.4 — Construção e ferramentas** | Variantes arquitetônicas e um equipamento somente se provar vantagem própria | F1, uso real, sem replicar vanilla |
| **9.5 — Fauna e habitats** | Primeiro mob contextual do Overworld, seguido de ecossistema escalável | Habitat definido, AI/GeckoLib onde necessário, spawn verificado |
| **9.6 — Integração universal** | Pistas/engenharia e interfaces **opcionais** para sonhos futuros, sem reativar 7.6 | Economia e segurança comprovadas; gameplay de Overworld já útil |

**Sequência de desenvolvimento dentro de cada task:** contrato curto → implementação mínima → assets/receitas/loot → teste estático → servidor real em 4 seeds → playtest Survival/multiplayer → revisão de distribuição → só então expansão. Não aprovar uma feature apenas por uma documentação extensa.

### Filtros de seleção para futuras ideias

- Primeiro: utilidade real e distinção contra vanilla.
- Segundo: múltiplos usos e consumidores claros, incluindo decoração honesta.
- Terceiro: descoberta espacial/habitat/riscos mensuráveis.
- Quarto: custo técnico, desempenho e riscos de saves.
- Quinto: coerência temática de sonhos, sem uniformizar tudo em azul/violeta.

Itens do catálogo em `CANDIDATE` ficam **fora do backlog de implementação autorizado**, até um contrato de fatia aprovar origem, consumidor e testes. Nem todos serão produzidos.

## 7. QA da própria Task 9.0

O validador [`scripts/ci/check_overworld_contract_9_0.py`](../scripts/ci/check_overworld_contract_9_0.py) verifica: IDs únicos, campos obrigatórios, famílias/quantidade, estados reais vs. propostos, anchors de registry existentes, fontes Overworld, pré-requisitos presentes, DAG sem ciclos, ordem de fases, receita conceitual do F1 e invariantes. O teste negativo [`scripts/ci/test_overworld_contract_9_0.py`](../scripts/ci/test_overworld_contract_9_0.py) verifica rejeição de regressões. O relatório usa `dreamsdimensions.qa.v1` com **`evidence_type=STATIC_CHECK`** e **`design_status=DESIGN_ONLY_NOT_PLAYABLE`**.

Em Windows PowerShell, na raiz:

```powershell
git switch migration/minecraft-26.3
git pull origin migration/minecraft-26.3
python scripts/ci/test_overworld_contract_9_0.py
python scripts/ci/check_overworld_contract_9_0.py --report qa-overworld-design-9-0.json
python scripts/ci/test_repository_docs.py
python scripts/ci/check_repository_docs.py --report qa-repository-docs.json
python scripts/ci/test_content_contract.py
.\gradlew.bat --no-daemon clean build
.\gradlew.bat runClient
```

### Comandos reais para inspecionar a base F0 existente

Mundo de laboratório com cheats/OP, sem fingir que F1 existe:

```mcfunction
/gamemode creative
/give @s dreamsdimensions:marked_stone 64
/give @s dreamsdimensions:cut_marked_stone 64
/give @s minecraft:stonecutter 1
/give @s minecraft:stone_pickaxe 1
/give @s minecraft:clay_ball 64
/give @s dreamsdimensions:dream_dust 16
/gamemode spectator
/execute in minecraft:overworld run tp @s 0 140 0
```

No mundo da seed `73199013`, a Task 7.4 registrou Pedra Marcada exposta em `-49 15 -33`. **Somente para depuração em Spectator e chunks gerados no mesmo baseline**:

```mcfunction
/execute in minecraft:overworld run tp @s -49 15 -33
```

Não existe `/give dreamsdimensions:lichen...` nem teleporte de um Marco/Abrigo F1 neste estágio. Os IDs e comandos novos só poderão ser divulgados **após a Task 9.1 realmente registrar os conteúdos**.

## 8. Critérios de conclusão

- [x] Base atual reconstruída a partir do código, receitas e docs.
- [x] Decisões universais e limitações explícitas.
- [x] Catálogo multidisciplinar com consumidores, origens, dependências e status.
- [x] Primeira fatia F1 selecionada, independente de dimensões.
- [x] Contrato e testes estáticos versionados e incluídos na CI.
- [ ] **F1 implementada em jogo** — pertence à Task 9.1.
- [ ] **Teste humano Survival / construção / multiplayer** — exige jogador e versão 9.1.
- [ ] **Revisão de raridade por dados de descoberta** — exige amostra real.
- [ ] **Entry P0 de sonhos liberada** — não faz parte da 9.0; permanece desligada.

**Resultado de 9.0: contrato de engenharia e backlog condicionado. Não alterar loot, receitas, mundo ou saves nesta task.**
