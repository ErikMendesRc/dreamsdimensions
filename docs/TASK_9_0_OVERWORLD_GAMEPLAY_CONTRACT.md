# Task 9.0 — Contrato Universal de Gameplay e Preparação para os Sonhos

**Projeto:** DreamsDimensions · Minecraft 26.3, NeoForge 26.3, Java 25  
**Branch:** `migration/minecraft-26.3` · sem merge na `main`  
**Revisão:** 13/11/2026, versão 2 — **a direção anterior centrada em construção foi substituída.**  
**Estado:** `DESIGN_ONLY`; a Task 9.1 ainda não implementou seus recursos novos.  
**Fonte de verdade verificável:** [catálogo e grafo 9.0](OVERWORLD_CONTENT_CATALOG_TASK_9_0.json); [estado do código](ESTADO_ATUAL.md).

## 1. A promessa: preparar-se para o desconhecido

**O Overworld é a base do jogador.** Nele se consegue comida, ferramentas, armas, proteção, minerais, plantas, suprimentos e conhecimento, e se constrói a infraestrutura de fabricação. O sonho não é o lugar onde o jogador precisa aprender a produzir o item essencial de saída. É a expedição para a qual ele se prepara.

O Colar permite tentar entrar nos sonhos, mas **a dimensão sorteada é desconhecida**: mundos pacíficos, hostis, escuros, instáveis, isolados ou com mecânicas ambientais diversas poderão coexistir. Os dois sonhos atuais são apenas testes iniciais, sem determinar a economia ou a dificuldade de todo o universo.

O **Despertador Onírico é o item crítico**: deve ser viável produzir o primeiro ainda no jogo inicial, porém depois de esforço e decisões reais. O Overworld precisa oferecer o caminho completo de obtenção. No mid/late game, o custo **marginal e o tempo de preparo** devem diminuir devido à infraestrutura, cultivo, ferramentas e aprendizado; a saída não se torna gratuita ou ilimitada.

A progressão deve **conversar com o Minecraft vanilla**, sem criar End Game paralelo, boss obrigatório, Nether ou End compulsórios, quests lineares nem dano inevitável por cair num sonho aleatório.

## 2. Diagnóstico factual do código atual (não confundir com proposta)

| Componente | Comportamento no código de hoje | Limitação de design |
| --- | --- | --- |
| `dream_ore` e `deepslate_dream_ore` | Geram no Overworld e dão Pó Onírico. Duas origens alternativas; mineração real amostrada. | Maioria da progressão é mineração repetida. |
| `dream_dust` | Insumo de Colar, Pedra Infundida e Núcleo, com origem natural exclusiva no Overworld. | Não existe integração com botânica ou descoberta. |
| `dream_necklace` | 3 Pós, não consumido; participa da política de entrada por sono. | Entrada permanece `dream_entry_enabled=false` para release. |
| `dream_infused_stone` | Receita 2 Pedra + 2 Pós → 1 bloco. | Serve principalmente de intermediário. |
| `oneiric_core_block` | 4 Pedras Infundidas + 4 Pós + relógio → Núcleo. É bloco decorativo/ingrediente, consumido no crafting do Despertador. | Não tem função de estação reutilizável. |
| `oneiric_awakener` | Núcleo + pena + frasco → Despertador. 12 Pós totais por unidade; uso 60 ticks e consumo após retorno confirmado. | Preço igual em todas as fases; nenhuma infraestrutura reduz esforço. |
| Ruínas de recuperação | Ruínas dimensionais com chance configurada de 25% de um Despertador completo em cada baú principal. | Não constitui **garantia** de fuga ou prova de acessibilidade em Survival; P0 pendente. |
| Pedra Marcada | Rocha e Stonecutter 1:1, construção independente. | É arquitetura complementar, não o eixo do mod. |

Referências: [economia Task 5.6](OVERWORLD_ECONOMY_TASK_5_6.md), [ruínas 5.7](DREAM_RUINS_TASK_5_7.md), [segurança 7.0](DREAM_RECOVERY_SAFETY_TASK_7_0.md) e [worldgen](WORLDGEN_E_CONTEUDO.md).

**Nenhuma receita acima foi modificada na Task 9.0.** O custo atual, a sorte da dimensão, a duração do uso e os gates de segurança permanecem vigentes até implementação e homologação de uma mudança explícita.

## 3. Grafo pretendido: ramos que se encontram

O objetivo é evitar a cadeia artificial `minério A → lingote B → item C`, sem mais nada. A cadeia central de retorno reúne **geologia + botânica + processamento + infraestrutura**, e as cadeias laterais tornam a expedição mais segura.

```text
OVERWORLD — origem e base do jogador
├── GEOLOGIA: Dream Ore / deepslate → Pó Onírico
│   ├── Colar dos Sonhos (equipamento de entrada; existente)
│   ├── Pedra Infundida → Núcleo Onírico (existentes)
│   │   └── [PROPOSTA] estação permanente de montagem
│   └── [PROPOSTA] Carga de Retorno ← Pó + Erva da Vigília + materiais vanilla
├── BOTÂNICA: Erva da Vigília [PROPOSTA, geração no Overworld]
│   ├── Carga de Retorno (obrigatória para montar Despertador na nova receita)
│   ├── Tônico de Lucidez (proteção situacional e temporária)
│   ├── Componentes de armamento/escudo
│   └── Horta de Vigília (mid game: logística renovável, NÃO produz Pó)
├── INFRAESTRUTURA: Núcleo reutilizável [PROPOSTA]
│   ├── Carga + insumos → DESPERTADOR (MESMO ID; consumido no retorno)
│   ├── Sonda de Ressonância (mid game; ajuda a procurar recursos)
│   └── Amplificação do Núcleo (late game; retorno mais barato, nunca grátis)
├── ARMAMENTO / DEFESA
│   ├── Lâmina de Vigília (interação específica com ameaças oníricas)
│   └── Escudo de Vigília / reforços (proteção limitada)
├── SUSTENTAÇÃO / NAVEGAÇÃO
│   ├── Tônico de Lucidez, lanterna de marcação e kit de expedição
│   └── Sinais de recuperação e informações parciais de risco
└── ESTRUTURAS E CRIATURAS
    ├── Vestígios temporais / pistas e aprimoramentos opcionais
    └── Mob, habitat, loot e mecânicas somente se tiverem consumidor real
```

A dependência `depends_on` no [catálogo](OVERWORLD_CONTENT_CATALOG_TASK_9_0.json) é verificável e acíclica. As origens alternativas de Pó permanecem alternativas, não pré-requisitos simultâneos. Cada nó tem origem, ação, custo, resultado, uso, validação e relação com o vanilla. Conteúdo marcado `CANDIDATE` **não está autorizado para codificação**.

### Por que o Despertador é o centro

O objetivo não é fazer o jogador passar horas só minerando para mais uma cópia do mesmo item. A primeira fabricação exige **investimento de capital e exploração diversificada**. A repetição usa a infraestrutura já construída, com custos por expedição ainda relevantes. Uma pessoa preparada deve poder guardar Despertadores de reserva sem eles perderem significado.

O Núcleo já possui o ID `dreamsdimensions:oneiric_core_block` e textura/partículas aprovadas: a revisão proposta **mantém o mesmo ID e blocos já salvos**, mas dá uma função que existe em gameplay. Como continuará havendo saves, receitas e compatibilidade legada, o design não assume que uma simples troca de JSON resolva a migração.

## 4. Early, mid e late game

| Fase | O jogador precisa fazer | O que fica mais acessível | O que continua valendo |
| --- | --- | --- | --- |
| **Early — primeira expedição** | Conseguir ferramenta de ferro, minerar Pó, localizar Erva da Vigília, fabricar Núcleo, produzir Carga, montar primeiro Despertador e estocar suprimentos. | A fabricação é possível **antes de Nether/End**, mas o primeiro Núcleo e a busca são dispendiosos. | Despertador não é dado gratuitamente. Dormir com Colar exige decisão e risco. |
| **Mid — expedições regulares** | Proteger e melhorar base, cultivar Erva, construir lanterna/arma/escudo, usar ferramentas limitadas de localização. | O Núcleo já está pago; cultivo e acesso a minério reduzem preparo por expedição. | Cada Despertador usa Carga real; equipamento degrada, tônicos acabam. |
| **Late — expedições especializadas** | Construir melhorias caras do Núcleo, logística de suprimentos, armas e ferramentas situacionais. | Produção em lote e preparação do inventário se tornam eficientes. | Não há Despertador infinito, escolha garantida de dimensão ou fuga gratuita. |

### Hipótese econômica inicial — precisa de teste, não é receita

- **Investimento inicial:** manter aproximadamente os atuais **12 Pós Oníricos** na fabricação de um Núcleo permanente; somar exploração botânica e ferramentas necessárias.
- **Preço marginal:** explorar uma Carga que custe inicialmente aproximadamente **4 Pós + material vegetal**, além de insumos vanilla para um Despertador. O primeiro Despertador, após fabricar o Núcleo, custaria aproximadamente **16 Pós cumulativos** (mais itens), contra os 12 Pós atuais por Despertador. Com o Colar, seriam aproximadamente 19 Pós na primeira preparação completa.
- **Após a infraestrutura:** o Despertador seguinte consome Carga e materiais, **não** outro Núcleo de 12 Pós. Cultivo e ferramentas ajudam; upgrades no late podem reduzir tempo/custo de modo limitado, nunca a zero.
- **Essa é hipótese de balanceamento**, não decisão imutável. As quatro seeds de minério mostram contagem subterrânea e apenas uma parcela exposta ao ar; não mediram tempo para 12/16/19 Pós em Survival. A Task 9.1 precisa experimentar custo cumulativo, custo marginal, acesso ao ferro, tempo até primeiro Despertador e custo em série de 5–10 viagens **antes de publicar valores**.

Não adicionar um fragmento que só aparece num chefe, numa dimensão ou numa estrutura rara como requisito para o **primeiro Despertador**. Isso criaria soft-lock e contradiria a promessa de preparação no Overworld.

## 5. Preparação para uma dimensão que o jogador não conhece

Antes do sono, o jogador deve ter um kit razoável que funcione diante de **vários tipos de risco**, não uma armadura específica para um único sonho.

| Necessidade | Resposta de gameplay | Observação |
| --- | --- | --- |
| **Voltar** | Despertador Onírico + reserva opcional | **Indispensável** para retorno normal; consumo pós-retorno confirmado permanece. |
| **Combater** | Arma vanilla + futura Lâmina de Vigília | Lâmina só vale a pena se fizer algo distinto de espada encantada, sem stunlock/imunidade. |
| **Defender** | Armadura e escudo vanilla + possível Escudo de Vigília | Proteção para ameaças de perfil desconhecido, nunca 100% universal. |
| **Atravessar** | Ferramentas, comida e blocos que o jogador escolhe levar | Respeitar ilha, gravidade, exploração e escolhas de inventário. |
| **Lidar com efeitos** | Tônico de Lucidez com duração/custo | Resiste a condição onírica específica; não é Totem ou leite melhorado. |
| **Não se perder** | Lanterna/marcador físico e, mais tarde, pistas de recuperação | Nada de radar passivo com busca de chunks por tick. |

**A imprevisibilidade é importante.** O mod não deve dar a escolha explícita da dimensão de destino como um upgrade comum. Uma ferramenta tardia pode informar **indícios de risco**, nunca revelar todo o resultado do sorteio.

### Contrato de chegada e recuperação

A combinação `Colar equipado + sono válido` não deve conduzir a um soft-lock previsível. A Task 9.1, antes de habilitar entrada, deve especificar uma **verificação de prontidão** ou aviso forte para jogadores sem Despertador e demonstrar: chegada em piso seguro; tempo/área suficiente para recuperar controle; inventário persistente; uso e interrupção corretos do Despertador; cenário em que o item é perdido. Escolher entre entrada bloqueada sem Despertador e modo de expedição de alto risco **é decisão futura de UX/gameplay**, não mudança feita aqui.

Se perder o item, a saída continua exigindo um **Despertador real** (fabricado previamente ou encontrado). As ruínas atuais têm 25% de chance por baú e não garantem retorno em tempo viável; isso precisa de homologação e, possivelmente, um caminho de recuperação raro mas confiável. **Não** dar teleport de emergência grátis nem tornar morte/cama saída automática; também não publicar uma dimensão que prende pessoas indefinidamente por falta de rota plausível.

A entrada continua desligada: `dream_entry_enabled=false`. O retorno usa **60 ticks**, servidor autoritativo e consumo único **após chegada validada ao Overworld**. Não reduzir segurança para fazer a nova economia.

## 6. Próximo slice aprovado — Task 9.1

**F1: preparação de expedição e economia do retorno**. Esta é a primeira fatia priorizada; construção arquitetônica passa a ser uma opção lateral posterior, não o centro da produção.

1. **`F1_VIGIL_HERB` — Erva da Vigília:** planta de habitat Overworld com coleta real; recurso para cargas e futuros consumíveis. Definir distribuição e colheita sem farm impossível de controlar.
2. **`F1_RETURN_CHARGE` — Carga de Retorno:** item consumível do circuito `Pó + Erva + vanilla`, receita acíclica, sem fabricar Pó a partir dele. Precisar de dois tipos de exploração; custo em Dust controlado.
3. **`F1_CORE_STATION` — Núcleo permanente:** transformar o bloco **já existente** em infraestrutura funcional de montagem com transação server-side, sem duplicação em multiplayer, relog ou quebra. Manter ID, drops e migração segura em saves antigos.
4. **`F1_AWAKENER_BALANCE` — Despertador:** migrar de `Núcleo consumido em Crafting Table` para `montagem no Núcleo com Carga + insumos`. Preservar o **mesmo** `oneiric_awakener`, o uso 60 ticks, cooldown e consumo pós-retorno. Atualizar recipe book/avancements/QA de economia no mesmo patch, sem compatibilidade silenciosamente quebrada.

**Entrega jogável do slice:** na base Overworld, o jogador produz e coloca Núcleo, colhe Erva, minera Pó, fabrica Carga, monta um Despertador, guarda cópia e pode repetir sem gastar o Núcleo novamente. **Não precisa entrar em sonho para completar esse loop.**

**Ordem técnica da 9.1:** levantamento detalhado da API 26.3 e dos saves → protótipo funcional de montagem/consumo → novo conteúdo e textura → worldgen natural e loot → receitas/livro → regressões de custo e save → servidor dedicado e quatro seeds → testes Survival e dois jogadores. Só aceitar quando o resultado for observável, não somente por registro JSON.

**Critérios de bloqueio:** nunca remover o Despertador atual sem migração segura; nenhum clique que consuma duas vezes; não gerar item se falta carga; custo inicial não depender de mob/dimensão/estrutura futura; Pó mantém origem Overworld; mundo novo e save migrado devem continuar íntegros.

## 7. Roadmap corrigido (design não implementado)

| Task proposta | Objetivo jogável | Pontes entre ramos |
| --- | --- | --- |
| **9.1 — Preparação e Retorno F1** | Núcleo funcional + planta + Carga + Despertador balanceado | Geologia + botânica + crafting + infraestrutura |
| **9.2 — Kit de Sobrevivência Onírica** | Primeira arma especial, consumível, proteção e marcação úteis contra riscos distintos | Botânica + minério + equipamento + ameaça |
| **9.3 — Descoberta e Logística do Overworld** | Habitats, estruturas opcionais, sonda limitada, cultivo e recursos avançados | Exploração + geração natural + agricultura + produção |
| **9.4 — Upgrade de Infraestrutura** | Estabilização, aprimoramento caro do Núcleo e especialização | Tecnologia + economia + equipamento |
| **9.5 — Fauna, Ambientes e Recuperação** | Mobs/POIs funcionais com risco e recompensa; verificar recuperação se perde o Despertador | Ecologia + combate + segurança |
| **9.6 — Integração Multi-Dimensão** | Contratos genéricos de perigo, equipamento situacional e expansão futura sem dependência das duas dimensões iniciais | Kit universal + habilidades + motor de sonhos |

A antiga cadeia **Líquen → Argamassa → Alvenaria** continua no catálogo como `CANDIDATE` e poderá virar conteúdo arquitetônico mais tarde. Não foi implementada, portanto a revisão não remove itens nem altera saves. Estruturas, plantas, minério e mobs passam a ser selecionados pelo efeito em **preparação, exploração, sobrevivência, retorno ou progressão**, não pela quantidade do catálogo.

## 8. QA e segurança da revisão

O validador [`check_overworld_contract_9_0.py`](../scripts/ci/check_overworld_contract_9_0.py) e os testes negativos [`test_overworld_contract_9_0.py`](../scripts/ci/test_overworld_contract_9_0.py) verificam: fontes Overworld, referências reais vs. propostas, grafo acíclico, quatro escolhas F1, seis caminhos de progressão, eras early/mid/late, Despertador como item central e entrada desligada. A CI em quatro seeds registra apenas `STATIC_CHECK` / `DESIGN_ONLY_NOT_PLAYABLE`, **não** prova crafting ou sobrevida.

```powershell
git switch migration/minecraft-26.3
git pull origin migration/minecraft-26.3
python scripts/ci/test_overworld_contract_9_0.py
python scripts/ci/check_overworld_contract_9_0.py --report qa-overworld-design-9-0.json
.\gradlew.bat --no-daemon clean build
.\gradlew.bat runClient
```

### Comandos de laboratório — **itens existentes**, sem fingir que a 9.1 já existe

```mcfunction
/gamemode creative
/give @s dreamsdimensions:dream_dust 16
/give @s dreamsdimensions:dream_necklace 1
/give @s dreamsdimensions:dream_infused_stone 4
/give @s dreamsdimensions:oneiric_core_block 1
/give @s dreamsdimensions:oneiric_awakener 2
/give @s minecraft:iron_pickaxe 1
/give @s minecraft:shield 1
/give @s minecraft:bread 32
/gamemode spectator
/execute in minecraft:overworld run tp @s 0 140 0
```

*Os itens Erva da Vigília, Carga de Retorno, Lâmina e Escudo de Vigília ainda não possuem registry IDs nem comandos de give. A posição do TP é apenas para observação em Spectator, não ponto seguro de Survival.*

## 9. Gates de conclusão

- [x] Reidentificar receita, Núcleo, consumos e limitações atuais pelo código do repo.
- [x] Substituir o foco arquitetônico pela preparação antes do sonho.
- [x] Catálogo de ramos e dependências de geologia, botânica, combate, suporte, exploração, infraestrutura.
- [x] Desenhar economia de primeira vs. repetidas expedições sem free return.
- [x] Contrato da Task 9.1 alinhado à função central do Despertador.
- [x] Atualizar validador estático, testes negativos e CI existentes.
- [ ] Implementar F1; medir com CI e jogadores.
- [ ] Fechar safety P0 de chegada, retorno, perda de item, ruínas, multiplayer e save.
- [ ] Equilibrar a economia com dados humanos, e não só distribuição por chunk.

**Estado final da 9.0:** uma árvore de preparação universal do Overworld, não novas features já existentes. Não modificar receitas, registro de item, chances, teleport ou chunks nesta revisão.
