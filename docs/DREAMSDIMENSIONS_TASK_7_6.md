# TASK 7.6 — Contrato do Sistema de Anomalias Oníricas

**Projeto:** DreamsDimensions | **Minecraft:** Java 26.3 | **Loader:** NeoForge 26.3 | **Java:** 25  
**Branch:** \`migration/minecraft-26.3\` | **Escopo:** contrato de gameplay, código de domínio testável e dois projetos de fenômenos | **Data:** 12/11/2026.

## 0. A decisão que muda o DreamsDimensions

> **O Overworld é a realidade. As dimensões são os sonhos. O Limiar é a interferência entre os dois.**

Pedra Marcada F0 (7.3–7.5) é **geologia aproveitável**. Ela não se torna anomalia porque sua textura é diferente. O jogador precisa perceber um evento coerente, formular uma hipótese, investigar evidências **independentes** e chegar a uma consequência física **de verdade**.

**Não aceitamos:** “uma partícula azul apareceu”, “um som tocou”, “uma pedra flutuou”, “uma estrutura com baú foi copiada”, “o jogador recebeu um texto misterioso”, “um evento aleatório teletransportou o jogador”. Esses podem ser *sinais*, nunca o resultado inteiro.

Um fenômeno precisa responder a cinco perguntas concretas:

1. **De onde veio?** A regra de interferência deriva de uma dimensão específica, com uma gramática visual e comportamental identificável.
2. **Como se percebe?** Existe um indício reconhecível no jogo normal, sem HUD, /locate, raio-X, nem depender exclusivamente de áudio ou cor.
3. **Por que é verdadeiro?** Pelo menos **duas pistas distintas**, distribuídas espacialmente, predizem o mesmo local/acontecimento físico.
4. **Que escolha o jogador faz?** O jogador pode comparar vestígios em vigília **ou** usar uma memória opcional de sono; nenhuma opção é uma chave RNG.
5. **O que muda ao final?** Encontra um segundo local previamente gerado, obtém uma vantagem material **usável** em F0, e aprende algo verificável sobre uma dimensão; deixa uma memória de descoberta persistível para futuros fenômenos.

O mistério não é uma caixa-preta com “resposta aleatória”. O jogador deve conseguir reconstruir *por que* o local descoberto fazia sentido.

## 1. Fundamentação do mod e dependências

Auditamos os sistemas que já existem:
- \`DreamDimensionCatalog\` define **\`dreamsdimensions:campo_onirico_azul\`** e **\`dreamsdimensions:dreamscape\`**, com atmosferas, arquitetura e transições diferentes.
- \`SleepTeleportHandler\` protege o fluxo de entrada em sonhos por eventos de sono e \`DreamEntryPolicy\`; entrada permanece **desabilitada por padrão** e nunca é contornada por anomalias.
- \`ModBlocks.MARKED_STONE\` gera naturalmente no Overworld (F0b 7.4) e \`ModBlocks.CUT_MARKED_STONE\` tem Stonecutter 1:1; construção é o primeiro consumidor material confiável.
- \`DreamQaRecorder\` é opt-in, limitado e não exporta dados pessoais; será o destino de **contadores agregados** após existir implementação runtime.
- O roadmap 6.9 originalmente numerava F1 Líquen como 7.6. **A decisão atual desloca 7.6–7.8 para anomalias** e deixa F1 para depois; veja o [adendo de numeração](OVERWORLD_ANOMALY_ROADMAP_ADDENDUM_TASK_7_6.md).

O sistema não substitui o motor de sonho, a recuperação por ruínas, efeitos dimensionais, mobs ou ferramentas vanilla.

## 2. O ciclo jogável — mistério em quatro atos com consequência

| Ato | Comportamento do jogador | Evidência autoritativa | Fracasso proibido |
|---|---|---|---|
| **A. Perceber** | Vê uma formação natural de Pedra Marcada com uma estria que parece continuar fora da rocha | \`NATURAL_MARK\` em **âncora originada de worldgen** | Pedra colocada pelo jogador ativar fenômeno |
| **B. Confrontar** | Encontra **duas** confirmações afastadas: uma descontinuidade geológica e uma alteração ambiental/contraparte visual | \`GEOLOGIC_DISCONTINUITY\` + \`AMBIENT_INVERSION\` ou \`VISUAL_FALLBACK\` | Repetir um clique/partícula contar como duas provas |
| **C. Interpretar** | Reconstitui a direção durante a vigília ou recebe uma memória opcional ao descansar perto do local | \`WAKING_RECONCILIATION\` **ou** \`SLEEP_RECALL\` | Sono obrigatório, RNG e teleporte gratuito |
| **D. Resolver** | Chega a uma formação secundária de geologia anômala **real**, que confirma a hipótese | \`PHYSICAL_PAYOFF\` **somente se o destino realmente existir** | Recompensa de texto ou pista apontando a lugar inexistente |

O contrato codificado expõe **\`HIDDEN → NOTICED → CORRELATED → INTERPRETED → RESOLVED\`**. 
Apenas uma transição legítima \`INTERPRETED → RESOLVED\` pode informar ao adaptador runtime que uma memória/recompensa foi desbloqueada. Replay, outro jogador, reload ou entidade remanescente não podem recriar a recompensa arbitrariamente.

### Estrutura do fenômeno: não existe “pedra mágica em todo chunk”

- A **Pedra Marcada normal** continua blocos geológicos comuns, mineráveis e movíveis.
- **Candidata a anomalia**: uma célula espacial com signature determinística; isso não é uma formação nem prova que a área é apta.
- **Anomalia autêntica**: uma instalação de worldgen **commitada pelo servidor**, com identidade própria, cinco pontos físicos válidos e percurso acessível, registrada de maneira persistente.
- **Vestígios cenográficos sem alvo real**: são inválidos. Se o terreno falhar, **não gerar sinal enganoso** nem iludir o jogador.

O selecionador puro \`AnomalyScenePlanner\` usa células de **12×12 chunks** e propõe uma em três células como **densidade candidata experimental**, equivalente a aproximadamente 1 candidata por 432 chunks **antes** de validar terreno, distribuição de biomas, interferência com estruturas e formas naturais. Isso **não é uma taxa observada**, e pode ser calibrado por gameplay. Ele nunca carrega chunks, não coloca blocos nem roda por tick.

### O espaço como linguagem

Cada anomalia precisa de pelo menos três vértices de informação: origem, testemunho geológico e testemunho ambiental. O motor impõe **separação mínima de 20 blocos** e raio máximo das pistas de 96 blocos; destino final deve ficar **entre 36 e 192 blocos** da origem. Esses limites são de **coerência geométrica**, não de alcançabilidade: a 7.7 terá de procurar caminhos realmente viáveis, sem queda-surpresa, água/lava imprevista, teto selado ou suporte frágil.

Pistas devem ser **reprodutíveis e causais**: a orientação da fenda na primeira pedra deve bater com a orientação da segunda e, juntas, indicar o último local. A solução não vem de uma coordenada sorteada independentemente depois de as pistas já serem colocadas.

## 3. Dois fenômenos com DNA diferente — não recolor de um template

### Fenômeno 01 — **Cicatriz Silenciosa** (\`quiet_seam\`, Campo Onírico Azul)

**Hipótese narrativa:** o ritmo suspenso do Campo Azul atravessou o Limiar e deixou a sensação de um momento “interrompido” em uma rocha mundana.

**Primeira experiência:** numa escarpa/caverna vanilla, duas ou três fissuras curtas de Pedra Marcada se interrompem de maneira suspeita. A direção das marcas *continua* em outra exposição mineral a algumas dezenas de blocos. O jogador decide acompanhar.

**Segundo testemunho:** o lugar onde as fraturas convergem tem uma alteração discreta e local da ambiência; também há uma geometria/silhueta visível que transmite **exatamente a mesma direção** para alguém com áudio desligado. Não existe silêncio arbitrário em todo o bioma nem um efeito global de música.

**Interpretação:** repousar ali pode apresentar um eco visual breve do horizonte do Campo Azul, sem entrar na dimensão. Alternativamente, reconstruir o alinhamento em vigília encontra a mesma direção. **Dormir não é requisito**; o sistema não pode contornar \`DreamEntryPolicy\`.

**Recompensa:** um **segundo local físico gerado e validado** com uma reserva recuperável de Pedra Marcada (mínimo inicial de 8 blocos; calibrar após QA) e uma correspondência visual verificável com o Campo Azul, gravável como conhecimento do jogador para a série de anomalias. A reserva vira **Pedra Marcada Lapidada** no Stonecutter já existente.

**Pergunta que fica:** *por que a realidade está reproduzindo a paisagem de um lugar que só deveria existir nos sonhos?*

### Fenômeno 02 — **Eixo Fraturado** (\`fractured_axis\`, Dreamscape)

**Hipótese narrativa:** a descontinuidade espacial característica do Dreamscape distorce o alinhamento de um estrato geológico, sem transformar o Overworld em um parque de ilhas flutuantes.

**Primeira experiência:** uma camada de pedra parece terminar num eixo impossível e reaparecer deslocada — sutilmente, mas de forma legível; o jogador pode comparar a direção com rocha intacta.

**Segundo testemunho:** outro ponto mostra um alinhamento geométrico quebrado que é coerente com a primeira observação. Uma reverberação direcional é um indício opcional, com contraparte visual de contorno/sombra. Não há gravidade imprevisível, buraco-armadilha nem teleporte.

**Interpretação:** o eco opcional remete às travessias e à topologia fraturada do Dreamscape; em vigília, o jogador pode resolver comparando os eixos. É outro verbo cognitivo: **reconstituir orientação**, não “seguir o silêncio”.

**Recompensa:** local final de estrato interrompido realmente existente, material recuperável de F0 e conexão comprovável com a assinatura geográfica do Dreamscape. O conhecimento deverá ganhar valor posterior em ruínas/travessias, sem criar uma chave obrigatória de progressão.

**Pergunta que fica:** *o que aconteceu aqui para duas partes da mesma camada seguirem orientações diferentes?*

**Ordem de execução:** 7.7 implementará **somente** a Cicatriz Silenciosa, com começo/meio/fim de verdade. O Eixo Fraturado é um segundo consumidor contratual para testar se a arquitetura generaliza; será ativado apenas depois da vertical 7.7 passar em Survival. Dois conceitos não equivalem a duas features já jogáveis.

## 4. Código de domínio entregue agora

### \`OneiricAnomalyContract.java\`

- Registra **somente tipos Java puros**, sem evento Minecraft, item, blocos, menus ou worldgen.
- Valida as posições/identidade da anomalia, origem natural e progressão.
- Exige dois testemunhos, com alternativa visual ao som e interpretação de vigília equivalente ao sono.
- Rejeita fases puladas, duplicações, pistas fora do raio, mundo errado, cliente não validado, bloco colocado por jogador e recompensa sem local confirmado.
- \`Progress\` é **imutável**, adequado a serialização posterior por adaptador, mas **não é persistido nesta task**.
- \`discoveryUnlocked=true\` ocorre apenas no instante de resolução e apenas uma vez pela máquina de estados. O domínio não concede item: a integração no servidor deve fazê-lo de forma transacional/idempotente.

### \`AnomalyScenePlanner.java\`

- Propõe **candidatas por célula espacial**, não por jogador, tick ou evento de render.
- Usa hash determinístico de seed/célula com \`floorDiv\` para coordenadas negativas; ordem de carregamento não muda a proposta.
- Propõe lineage Blue Field ou Dreamscape, orientação geométrica e ID estável.
- Retorna \`ProposedGeometry(terrainVerified=false)\`; **não afirmar que posições com Y herdado do ponto inicial têm solo seguro**.
- Não lê mundo nem reserva chunks: para 7.7, adaptador de worldgen deverá validar o terreno, os caminhos e os locais; se falhar, descartar em vez de fabricar um final.

### Catálogo e validador

- \`docs/contracts/ONEIRIC_ANOMALIES_V1.json\`: **design inerte**, dois perfis com causalidade, verbos, pistas, alternativas, payoff material + intelectual e medidas de segurança.
- \`scripts/ci/check_oneiric_anomaly_contract.py\`: rejeita perfis cosméticos sem recompensa, dimensões erradas, sonos obrigatórios, pistas sem fallback, spawns por jogador, provas falsas de densidade e ativação prematura.
- \`scripts/ci/test_oneiric_anomaly_contract.py\`: testes adversariais do contrato JSON.
- \`src/test/java/.../anomaly/OneiricAnomalyContractTest.java\`: testes do motor puro; entrada ilegítima/false lead, duas provas, replay, geometria, negativa X/Z, determinismo, densidade de candidatos.
- \`.github/workflows/build.yml\`: ambos validadores em CI, com evidência \`STATIC_CHECK\` segregada dos testes de mundo real 7.4–7.5.

**Importante:** a Task 7.6 **não habilita uma anomalia no jogo**. Nenhuma nova biome modifier, feature JSON, evento cliente, nova textura, registro ou extração de loot foi adicionada. Isso é intencional: uma implementação superficial agora violaria o contrato que estamos estabelecendo.

## 5. Adaptação runtime obrigatória para Task 7.7

O engine puro **não é uma fronteira de segurança** por si só. O método \`apply\` recebe evidências, mas é responsabilidade exclusiva do futuro **adaptador do servidor** criar provas confiáveis. **Nunca** receber \`serverValidated=true\` ou \`origin=NATURAL_WORLDGEN\` por packet do cliente.

### Ordem correta de integração

1. **Placement:** usar API atual 26.3 de \`Feature\`/\`PlacedFeature\` + NeoForge \`BiomeModifier\`, não APIs antigas de \`ConfiguredFeature\`.
2. **Provenance:** worldgen escreve um registro imutável de origem e ID do site na criação da própria estrutura; **não inferir origem natural simplesmente por contar Pedras Marcadas**, pois blocos podem ser colocados pelo jogador.
3. **Commit atômico:** todos os nós, destino e caminhos têm validação; somente depois registrar a anomalia como autêntica. Falha de terreno → \`SKIPPED\` com motivo, não metade da cena no save.
4. **Persistência de site:** SavedData **versionado e bounded**, armazenado por mundo, com ID, seed, dimensão, nós e compromisso da recompensa. Nunca gravar arquivo por tick.
5. **Progresso por jogador:** usar Data Attachment serializável/copied-on-death se a semântica exigir; entender distância e transição de mundos. Recompensa física é compartilhada e limitada; conhecimento é por jogador.
6. **Observação:** validar a posição do jogador, contexto e existência do alvo em chunks já carregados em interação real do servidor. Não tick-scan de raio grande; quando não carregado, deixar o jogador voltar, sem dar falso \`PASS\`.
7. **Experiência cliente:** pistas são ambientais e discretas com fallback visual/geométrico; todos os efeitos sincronizados quando necessários, nada de partículas globalmente em todas as Pedras Marcadas.
8. **Reconstrução da memória:** integração opcional com o ciclo de sono já existente por um caminho não-teleportador, sem alterar chances de entrada/segurança e com alternativa em vigília.
9. **Restauração e multiplayer:** persistir/recuperar o mesmo estado sem duplicar recompensa, mesmo que um jogador tenha quebrado parte da pedra; se um destino foi destruído, ter recuperação/fallback verificado ou cancelar o indício **antes** de enganchar outro jogador.
10. **Homologação:** quatro seeds reais, N candidatos→N válidos, abortos por terreno, destinos, distâncias, rotas, alterações em estruturas, tempo MSPT e ausência de anomalias geradas em bases ou pedaços antigos.

### Regras não negociáveis

- O evento acontece porque um sonho influencia aquele território, **não** porque partículas azuis são “bonitas”.
- **Toda pista verdadeira deve apontar a uma verdade física**; não criar recompensa aleatória depois da investigação.
- **Duas evidências independentes**: pelo menos uma geológica, outra ambiental/geométrica; espaçamento concreto. Duplo clique não é descoberta.
- Nenhuma forma de duplicação de material por repetição de interação; nenhum \`/tp\` especial ou atalho de Despertador.
- A exploração deve funcionar sem áudio, sem sonho desbloqueado e em Peaceful.
- Sem invadir construções de jogadores, sem alterar blocos de saves antigos, sem chunk force-load em mundo de produção.
- O Overworld não pode virar um bioma especial contínuo. Anomalias devem ser episódios locais, específicos, memoráveis.
- **Princípio de escala:** um núcleo profundo investigável é melhor do que vinte tipos de eventos aleatórios iguais.

## 6. Aceite e QA da Task 7.6

| Gate | Evidência | Estado esperado |
|---|---|---|
| \`JAVA_COMPILATION\` | Gradle Java 25/NeoForge 26.3 | PASS |
| \`DOMAIN_REGRESSION\` | JUnit com stage/alternativas/idempotência/anti-fraude | PASS |
| \`DESIGN_VALIDATION\` | Perfis JSON + 15 testes adversariais | PASS |
| \`DETERMINISM\` | Mesma seed/célula = proposta igual, inclusive X/Z negativos | PASS |
| \`CI_REGRESSION\` | Build completo existente / 4 seeds | PASS |
| \`PLAYABLE_ANOMALY\` | Não implementada até 7.7 | NOT_RUN |
| \`PERSISTENCE\` | Depende de SavedData + Data Attachments de 7.7 | NOT_RUN |
| \`BLIND_DISCOVERY\` | Jogadores encontrando, interpretando e resolvendo | NOT_RUN |

**Não declarar vitória de gameplay** baseado em testes de domínio; esses só provam regras e ausência de regressão na base.

## 7. Comandos para testar agora — Windows

\`\`\`powershell
git switch migration/minecraft-26.3
git pull origin migration/minecraft-26.3
python scripts/ci/test_oneiric_anomaly_contract.py
python scripts/ci/check_oneiric_anomaly_contract.py --report qa-oneiric-anomaly-contract.json
.\gradlew.bat --no-daemon clean build
.\gradlew.bat runClient
\`\`\`

No jogo, **não existe /anomaly, /dreamanomaly, /locate_anomaly nem fenômeno jogável na 7.6**. Só validar que o gameplay atual não mudou:

\`\`\`mcfunction
/gamemode creative
/give @s dreamsdimensions:marked_stone 32
/give @s dreamsdimensions:cut_marked_stone 32
/give @s minecraft:stonecutter 1
/gamemode spectator
/execute in minecraft:overworld run tp @s 0 140 0
/execute in dreamsdimensions:campo_onirico_azul run tp @s 0 180 0
/execute in dreamsdimensions:dreamscape run tp @s 0 180 0
\`\`\`

Todos os TPs são **pontos de observação Spectator**, não locais seguros para Survival. Dream Entry/Return permanece inalterado. A próxima task deve incluir comandos de QA concretos com **XYZ observados pela CI**, não destinos inventados.

## 8. Fontes técnicas e documentos

- [NeoForge 26.3 — Mudanças em Feature/worldgen](https://docs.neoforged.net/primer/docs/26.3/)
- [NeoForge — Biome Modifiers](https://docs.neoforged.net/docs/worldgen/biomemodifier/)
- [NeoForge — SavedData](https://docs.neoforged.net/docs/datastorage/saveddata/)
- [NeoForge — Data Attachments](https://docs.neoforged.net/docs/datastorage/attachments/)
- [7.3 — Pedra Marcada](DREAMSDIMENSIONS_TASK_7_3.md)
- [7.4 — Geração/Lapidação](DREAMSDIMENSIONS_TASK_7_4.md)
- [7.5 — Homologação F0](DREAMSDIMENSIONS_TASK_7_5.md)
- [6.0 — Game Design Bible](GAME_DESIGN_BIBLE_TASK_6_0.md)
- [6.9 — Roadmap](OVERWORLD_EXECUTIVE_IMPLEMENTATION_ROADMAP_TASK_6_9.md)
