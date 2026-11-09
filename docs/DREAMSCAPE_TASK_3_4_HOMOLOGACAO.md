# TASK 3.4 — HOMOLOGAÇÃO, ESTABILIZAÇÃO E REFINAMENTO DA DREAMSCAPE

**Branch:** `migration/minecraft-26.3` (sem merge na `main`)
**Baseline auditado:** `15eb089e0407acb68906e743ef3982ff5a04fb61`, proveniente das Tasks 2.2, 3.1, 3.2 e 3.3.
**Ambiente:** Minecraft 26.3 / NeoForge 26.3 / Java 25 / ModDevGradle.

## 1. Diagnóstico do feedback — "ilhas sim, pontes não"

O jogador confirmou que **as ilhas suspensas estão boas**, mas as pontes
geológicas não são perceptíveis. A geração de ilhas NÃO foi redesenhada.

A inspeção do grafo original de densidade explica a deficiência das pontes:
- `dreamscape_fracture_excavation` subtrai
  `fracture_activity × gradient(Y6→44) ×
  (8.6 − 8.1 × bridge_strength × gradient(Y74→112))`
  da densidade continental.
- Mesmo com `bridge_strength=1` em Y≥112, a escavação remanescente é
  **0.5** unidade de densidade. Nas camadas em que o continente já
  está próximo de zero, a forma **continua negativa (ar)**. E reduzir
  escavação não estabelece por si só um volume horizontal
  positivo ligado às duas margens.
- `bridge_strength` vem de um ruído 2D independente
  (`dreamscape_bridge_vein`, 3 oitavas começando em -7). Sua
  interseção com a faixa de fratura é probabilística, não
  condicionada à presença de dois apoios geológicos. O código original
  não verifica topologia nem navegação. A sondagem anterior de
  56 colunas/seed retornou **zero** indícios simples de ponte em
  Y118/Y76 em quatro seeds. Isso é uma amostra pequena, NÃO uma
  prova de que nenhuma ponte existia no mundo.

**Causa demonstrada no código:** ausência de campo dedicado de
densidade positiva/garantia de conexão; o amortecimento sozinho
não cria necessariamente a travessia. **Hipótese ainda aberta:** baixa
frequência de coincidência entre `bridge_strength`, fenda e altura
suficiente; a prevalência real depende de amostragem em mundos gerados.

## 2. Correção cirúrgica: volume geológico de ponte

Criadas duas funções de densidade dedicadas às pontes:
`worldgen/density_function/dreamscape_bridge_deck.json` e
`worldgen/density_function/dreamscape_bridge_halo.json`.
O valor é o mínimo de quatro termos:

```text
R = min(
      5 × (bridge_strength − 0.28),
      9 × (bridge_halo − 0.03),
      gradient_Y(108, 114, -3.4, 3),
      gradient_Y(115, 128, 3, -4)
    )
```

A densidade final fica
`max(max(mainland - fracture_excavation, R), island_volume)`.
O halo é calculado com a máscara de fratura antiga e a mesma isocurva
de fault-spine, mas amplia somente a meia-largura do selecionador
de pontes de `0.15` para `0.23`. A escavação do terreno existente
**não é alterada**.
Numa interseção regional positiva, `R` pode ser positivo em
uma banda vertical cuja espessura vai diminuindo nas bordas;
é permitido criar pedra atravessando a parte superior de
uma fenda, enquanto o vão abaixo permanece no campo
continental original. Nas bordas sem fratura, o continente
intacto permanece o próprio ponto de apoio.

**O perfil foi afinado após a primeira sondagem real:** a tentativa inicial com gradientes 94–110 e 115–144 era espessa demais; em duas seeds instrumentadas havia de 0 a 5 colunas com vazio profundo por janela, sempre 0 pontes confirmadas. O novo intervalo 108–128 cria uma camada mais fina, aproximadamente Y112–120 onde o campo horizontal é positivo, deixando ar abaixo quando existe uma fratura verdadeira. A existência de travessias continua dependendo da sondagem
dos blocos reais.

**Intencionalmente não é uma estrada gerada por feature.** O
campo é suave e baseado em ruído; formações variam com o contorno
da fratura, a zona do ruído e a interpolação 4×4 existentes.
A operação `min` evita uma placa positiva generalizada:
se `bridge_strength≤0.28`, `bridge_halo≤0.03` ou
a máscara vertical for negativa, não existe contribuição
sólida de ponte nessa coordenada.

**Ajuste mínimo:** somente `dreamscape_solid_density.json` foi
composto com a nova função de volume, e foi acrescentado um halo
de fraturas específico para pontes. `dreamscape_fracture_excavation`,
todas as fraturas, `bridge_strength`, material rules,
`noise_settings` e `spawn_target` permaneceram inalterados.
Não foram criados blocos, mobs, itens, assets, triggers ou
novas features de estrutura.

### Ilhas flutuantes — regressão protegida por dois mecanismos

As três density functions de ilhas e seus três ruídos foram comparados
**byte por byte** com seus Git blob SHA-1 do baseline; o novo script
`check_dreamscape_homologation.py` impede alterações acidentais.

Em `Y≥140`, o gradiente superior já fixou `R = -4` ou inferior (por `min`), estritamente negativo,
independentemente da seed ou dos ruídos. Portanto:
- `max(old_density,R)` não pode converter qualquer voxel
  anterior não sólido (≤0) em sólido (>0) nessa faixa.
- Não remove qualquer voxel positivo anterior.
- A massa insular aprovada em Y~145–230 conserva a mesma
  decisão **pedra versus ar** por construção do grafo; a
  *densidade numérica negativa* em vazios pode mudar sem
  afetar o voxel binário ou as ilhas.
- Para plena certeza de iluminação, material rule, biome,
  chunk seams e bordas de mundos antigos é preciso
  comparação de saves no cliente; essas inspeções permanecem pendentes.

### Refinamento após seeds realmente fixadas

A primeira medição com `level-seed` e comando `/seed` confirmou
duas pontes na seed `-904732`, mas **zero** nas dez janelas
amostradas para as outras três seeds; nelas foram observados
centenas de vãos sem duas margens conectadas.
Isso justifica **um ajuste medido de seleção regional**:
`bridge_strength` passa a exigir `>0.28` (antes 0.42),
enquanto a amplitude máxima horizontal foi reduzida
de fator 7 para 5. Não altera qualquer ruído, ilhas,
sistema de fratura, altitude, material, atmosfera nem
seu custo em NormalNoise. A mudança visa aumentar a
área de interseção entre fenda e faixa da ponte, mas
**não constitui garantia** de conexão ou segurança;
somente o próximo RCON com seed verificada valida o efeito.

### Faixa de suporte nas margens (teste de topologia)

Após executar 10 janelas reais por seed com `level-seed`
verificado por `/seed`, a versão anterior retornou:
`14011: 0`, `20261108: 1`, `-904732: 2`,
`73199013: 0` traçados com face de rocha conectada.
A grande quantidade de colunas com vazio em
`14011` (783 em uma janela) e `73199013`
(867 em outra) sem duas margens atingidas sugere
insuficiência de conexão lateral. Essa observação
não prova que todos os vãos analisados eram pontes.

Foi incluído **apenas para a ponte** o
`dreamscape_bridge_halo`, calculado com o mesmo
`fault_spine`, `fault_width` e `fracture_region`
da Task 3.1, mas com largura de isocurva
`0.23` em vez de `0.15`. Isso alonga a região
geológica de união para além da escavação e
permite conectar a fina massa positiva a terreno
intacto. Como `fracture_region` ainda limita
o halo e `bridge_strength` é exigido, o efeito
**não é uma faixa contínua** pelo mapa todo.
Essa mudança precisa passar pela sonda real antes
de ser classificada como melhoria comprovada.

## 3. Instrumentação de topologia real (somente CI)

Criado `DreamscapeBridgeAuditCommand` (server-side), registrado
somente quando o processo tem a propriedade
`-Ddreamsdimensions.enableBridgeAudit=true`. **Não existe comando
de diagnóstico no jogo normal**, e mesmo quando habilitado o comando
é somente leitura, sem loading forçado, blocos novos, geração
manual, packets, entidade ou evento periódico.

Comando para testes:

```mcfunction
/dreamscape_bridge_audit 256 256 56
```

O comando exige que o conjunto inteiro de chunks da área já
esteja carregado; na CI o Python usa `forceload add` **antes**
de chamar o diagnóstico. Em cada coluna na caixa de 113×113:
- Examina níveis Y100..138 **mesmo se houver ilha acima**;
- Exige piso natural sólido (pedra/grama/terra) e dois voxels de ar
  acima para caminhar;
- Classifica como **vão** apenas se há ar 12 E 20 blocos abaixo;
- Classifica um **apoio natural** apenas se há terreno natural
  sólido 12 E 20 blocos abaixo;
- Analisa duas metades de um traçado X ou Z com extensão limitada
  a 30 passos, deriva lateral ≤2 e degrau vertical ≤1;
- Exige no mínimo três passos sobre o vazio **por lado** e apoios
  em margens opostas, separados do centro por no mínimo 8 passos;
- Rejeita traçados sem apoio, em construção, interrompidos,
  sem headroom ou acima dos limites. Os logs reportam até três
  traçados separados por janela, com centro, eixo, margens
  e evidências.

**Níveis distintos de prova:**

- `R>0` é apenas oportunidade matemática, NÃO é ponte.
- Um bloco sobre ar é apenas indício fraco, NÃO é ponte.
- `DREAMSCAPE_BRIDGE_CONFIRMED` significa um **traçado físico
  amostrado com dois apoios profundos, vazio abaixo e superfície
  caminhável** no conjunto de chunks lidos. Ainda não prova largura
  total nem navegabilidade por jogador real.
- Uma ponte só será **visual e plenamente aprovada** depois
  de captura no `runClient` e travessia manual.

O diagnóstico deliberadamente detecta apenas pontes na janela
vertical e orientadas o suficiente ao X/Z. Pontes oblíquas
ou fora de Y100..138 podem passar despercebidas. Um `0`
significa "nenhuma **confirmada nesta sondagem**", não
"inexistentes no mundo". Scan limitado em CI; não rodar
continuamente em produção.

## 4. Cobertura da homologação

`scripts/ci/check_dreamscape_homologation.py` executa assertivos
estáticos com dependências do `check_dreamscape_geography.py`
original; oferece também `--rcon --expected-seed N` com até **dez**
janelas fixas por seed, raio 56 e parada antecipada somente após
duas travessias confirmadas. As três primeiras são
`(256,256)`, `(-320,192)` e `(544,-384)`, seguidas de
`(-640,-480)`, `(768,640)`, `(-896,256)`,
`(1024,-256)`, `(1440,192)`, `(-1152,-768)`,
`(128,1024)`. A sonda pode falhar em encontrar pontes fora
das janelas, alturas Y100..138 ou orientações X/Z.

Os scripts anteriores **não foram removidos**:
`check_vegetation.py`, `check_formations.py`,
`check_worldgen_balance.py`, `check_atmosphere.py`,
`check_dream_engine.py`, `check_dreamscape_geography.py`,
`check_dreamscape_ecosystem.py` e
`check_dreamscape_atmosphere.py`.

Os resultados de testes automatizados cobrem:
- Parsing e referências dos datapacks, grafo DF sem ciclos
  (Task 3.1) e ordem das features (Task 3.2);
- Configuração correta do noise router `interpolated 4×4`,
  heightmap e material rule;
- Alturas da dimensão, proteção de spawn, plataforma 5×5
  do motor onírico;
- Imutabilidade por hash de todos os seis insumo/volume
  de ilhas suspensas, preservação analítica acima de Y140;
- Três modelos de caminho: ponte contínua, falta de apoio
  e interrupção; não são voxels de Minecraft;
- Acesso somente a chunks carregados e teto de scanner;
- Isolamento entre biome/atmosfera do Campo Azul e Dreamscape;
- Smoke com servidor dedicado, quatro seeds, logs reais de
  geração de ecossistema e sondagem topológica.

### Evidência de execução — seed real verificada

**A correção do CI é indispensável:** até o início desta task o workflow
escrevia `seed=${{ matrix.seed }}` em `server.properties`, propriedade
que NÃO fixa a seed do Minecraft. Desde a revisão da Task 3.4, a CI usa
**`level-seed=${{ matrix.seed }}`** e cada job lê `/seed` via RCON,
rejeitando diferenças. Isso invalida os **rótulos de seed** das
contagens de mundo dos jobs mais antigos (Tasks 3.1–3.3), mas NÃO
invalida seus resultados de compilação, carregamento de datapacks
ou teste da API. Somente os resultados com `DREAMSCAPE_SEED_VERIFIED`
abaixo são apresentados como evidência atrelada a uma seed.

Versão final com halo `0.23` e força da ponte `5(S−0.28)`:
[GitHub Actions run 42637169718](https://github.com/ErikMendesRc/dreamsdimensions/actions/runs/42637169718)
e [repetição 42637188070](https://github.com/ErikMendesRc/dreamsdimensions/actions/runs/42637188070),
commit `2d51bd57e8e234e44a75df7ab6843472130e587e`.
**Ambas concluíram com `success` nas quatro seeds**, incluindo:
`./gradlew --no-daemon clean build` com Java 25, JUnit, scripts
estáticos existentes e novos, preflight de assets, servidor NeoForge
dedicado, datapacks, sondas anteriores e a nova topologia RCON.

| Seed real (RCON) | Build/servidor | Janelas verificadas | Traçados com 2 apoios + 6-conectividade | Centros X,Y,Z |
|---|---|---:|---:|---|
| `14011` | PASS | 10 | **0** | Nenhum na amostra |
| `20261108` | PASS | 10 | **1** | **(-1118, 120, -800)** |
| `-904732` | PASS | 3 (parada antecipada) | **2** | **(576, 120, -385)** e **(583, 120, -366)** |
| `73199013` | PASS | 10 | **1** | **(-298, 120, 223)** |

**Centros e apoios do último código testado:**

| Seed | Centro | Margem A | Margem B | Direção |
|---|---|---|---|---|
| 20261108 | (-1118,120,-800) | (-1128,120,-800) | (-1110,120,-800) | X |
| -904732 | (576,120,-385) | (575,123,-393) | (576,121,-368) | Z |
| -904732 | (583,120,-366) | (583,121,-389) | (584,122,-358) | Z |
| 73199013 | (-298,120,223) | (-298,120,212) | (-298,120,235) | Z |

Todos os quatro registros incluem `void_at_center=12,20_below`,
`headroom=2`, `continuous_walk=yes`,
`rock6_connected=yes` e pelo menos 3 passos com vazio
**em cada lado**. As duas execuções independentes produziram
**os mesmos quatro centros, margens, seeds e contagens**.
Isto valida trajetos em blocos reais para as regiões amostradas,
não a aparência artística e nem uma frequência por chunk.

**Meta ainda não atingida integralmente:** na seed `14011`,
nenhuma travessia passou pelo critério estrito nas dez janelas.
Uma janela nessa seed apresentou centenas de colunas com vazio
inferior, porém sem caminho contínuo de apoio a apoio; ela
**não prova inexistência de pontes** fora da amostra. Não foi
feito aumento indiscriminado de frequência ou reconfiguração
das ilhas apenas para produzir uma estatística melhor.

**Progresso registrado e limitações do diagnóstico:** a revisão
anterior ao halo tinha os valores `14011: 0`,
`20261108: 1`, `-904732: 2` e `73199013: 0`
com as mesmas seeds reais. O halo permitiu uma travessia
verificada em `73199013` sem alterar as três funções das ilhas.
O scanner aceita trajetos aproximadamente X/Z, com tolerância de
curvatura lateral pequena; não é verificador universal de
estrutura geológica ou de conexões para todo o mapa.

**Estado da homologação:**
- Compilação/Java25, datapacks, servidor e regressões estáticas:
  **APROVADOS** (4/4 seeds, 2 execuções).
- Ponte topológica real: **COMPROVADA em 3/4 seeds amostradas**
  (4 traçados), não certificada globalmente.
- Ilhas suspensas: os 6 hashes JSON de origem e a decisão
  sólido/ar no envelope Y≥140 são preservados; aprovação
  visual anterior do jogador mantida, mas nenhuma comparação
  visual automatizada foi executada.
- Gameplay visual, navegação em Survival, efeito de áudio/névoa
  em cliente, FPS/MSPT comparável e multiplayer: **PENDENTES**.

Não houve apagamento nem conversão de saves existentes. Mudanças
de worldgen exigem chunks não gerados ou mundo novo para comparação.

## 5. Custo de geração / benchmark

O novo campo reusa dois campos 2D `cache+slice` anteriores
e dois gradientes Y. Não adiciona nenhum `NormalNoise`, nova interpolação ou
`Feature.place`; custa dois `max`, poucos `min` e
aritmética local nas amostras, incluindo um `cache+slice`
para o halo de fratura. Trata-se de justificativa de complexidade, **não**
uma medida precisa de geração de chunk.

O diagnóstico topológico roda somente em processo CI opt-in
e consulta uma janela finita por comando. Carga de chunks em CI
não equivale a custo por chunk nem mede FPS. Para uma comparação
válida, usar a mesma máquina, JDK, render distance, seed,
coordenadas e regiões ainda não geradas, repetir ≥3 vezes,
e reportar medianas de MSPT / duração. O pipeline compartilha
runner e cache com outros jobs; números isolados não são
benchmark confiável de otimização.

## 6. Checklist manual em mundo novo

- [ ] Fazer backup de saves; **não apagar chunks antigos**.
- [ ] `git switch migration/minecraft-26.3`; `git pull`;
      executar `.\gradlew.bat runClient` com JDK 25.
- [ ] Novo save por seed 14011, 20261108, -904732, 73199013.
- [ ] Dormir 100 ticks: confirmar natural spawn, colisão e
      plataforma de emergência caso necessário.
- [ ] Via espectador, sobrevoar as três regiões de sondagem
      acima; para coordenadas **confirmadas** consultar a tabela
      de CI e a saída `DREAMSCAPE_BRIDGE_CONFIRMED`.
- [ ] Observar de lado a forma de cada ponte: textura natural,
      silhueta, bordas erosionadas e vão perceptível.
- [ ] Caminhar/saltar pela ponte em survival, sem voo e
      com espaço livre, diferenciar trechos largos e estreitos.
- [ ] Voar **por baixo** das ilhas em Y145–230; fotografar
      massas, raízes e conexão com terra ao redor.
- [ ] Inspecionar cruzamentos de bordas de chunk sem plataformas
      truncadas e sem rocha pendurada indevidamente.
- [ ] Verificar árvores de Memória/Testemunha, copas, folhas,
      galhos, raízes em ancoragens reais, monólitos e jardins.
- [ ] Trocar Dreamscape ↔ Campo Azul ↔ Overworld;
      conferir fog 42/190/235 e 48/170/210, partículas
      ALL/DECREASED/MINIMAL, sons e limpeza ao desconectar.
- [ ] Testar Despertador por 60 ticks, cama original,
      reconexão e multiplayer (ao menos dois jogadores).
- [ ] Comparar FPS, frame time, MSPT, TPS e memória antes/depois
      em hardware e chunks equivalentes, usando 3 repetições.
- [ ] Fazer teste de determinismo com duas cópias de mundo
      da mesma seed (ordem de visita diferente) e fotografar
      as mesmas coordenadas; chunk previamente gerado não muda
      após atualização do datapack.

**Pendências para iniciar Task 4.1 — Criaturas Oníricas:**
aceitar visualmente as pontes em cliente, avaliar a frequência
na seed `14011` (0 traçados nas janelas amostradas), verificar
percurso em survival, FPS/MSPT e custo de geração sob condições
comparáveis. Build verde e smoke RCON
**não substituem** essas homologações.

## Referências

- https://docs.neoforged.net/primer/docs/26.3/
- https://docs.neoforged.net/docs/concepts/registries/
- https://docs.neoforged.net/docs/concepts/events/
- https://docs.neoforged.net/docs/misc/commands/
- https://github.com/neoforged/NeoForge/tree/26.3.x
- https://github.com/NeoForgeMDKs/MDK-26.3-ModDevGradle
- https://feedback.minecraft.net/hc/en-us/articles/48913133328013-Minecraft-Java-Edition-26-3

Páginas gerais de NeoForge por vezes aparecem na versão 26.1.
O contrato 26.3 final foi conferido por sintaxe de datapack e
por compilação real JDK 25, sem usar ConfiguredFeature legado.
