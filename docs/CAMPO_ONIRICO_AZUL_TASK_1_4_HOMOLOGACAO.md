# TASK 1.4 — Relatório de homologação e estabilização (Campo Onírico Azul)

**Projeto:** DreamsDimensions | **Branch:** `migration/minecraft-26.3`  
**Runtime:** Minecraft 26.3, NeoForge 26.3.0.101, Java 25, ModDevGradle 2.0.148  
**Base:** Task 1.1 terreno, Task 1.2 vegetação, Task 1.3 formações.  
**Resultado técnico:** COMPILAÇÃO, DATAPACKS E SMOKE SERVER PASSARAM EM QUATRO SEEDS; **ACEITE VISUAL E TELEPORTE REAL PENDENTES**. Não houve merge na main.

## 1. Evidências e escopo da auditoria

Lidos os documentos `CAMPO_ONIRICO_AZUL_TASK_1_1.md`, `TASK_1_2.md`,
`TASK_1_3.md` e `TASK_1_3_TEST_PROFILE.md` (este último foi removido
da branch ativa na restauração do perfil). Revisados `DreamVegetationFeature`,
`DreamFormationFeature`, `DreamWorldgen`, `ModBlocks`, `DreamLeavesBlock`,
`DreamSpawnHelper`, `SleepTeleportHandler`, `OneiricAwakenerItem`, datapacks
`worldgen/{feature,placed_feature,biome,density_function,material_rule,noise_settings}`,
os tags de heightmap/motion, a CI e os scripts RCON.

O terreno continua definido pelo `campo_onirico_azul_noise`, combinando
`wave_height` (pradarias), `terrace_height/presence` (terraços), `ridge_height`
(cordilheiras) e a `material_rule/campo_onirico_azul_surface`. Não alteramos
os pesos, a topografia, os IDs ou a surface rule: não houve evidência mensurável
de defeito nesses datapacks que justificasse retoque estético.

Apenas o bioma Campo Onírico Azul referencia as quatro formações e as cinco
placed features vegetais. O tipo `Feature` está registrado por `MapCodec`
em `BuiltInRegistries.FEATURE_TYPE`, conforme a API real 26.3. Não há
nova injeção no Dreamscape ou Overworld, nem novos blocos, itens ou assets.

## 2. Problemas encontrados e correções

| Prioridade | Evidência observável | Ajuste efetuado |
| --- | --- | --- |
| P1 | Perfil de inspeção `39fcbbe` tinha 90% de tentativa e densidade 1,0 em arcos/círculos; não adequado à configuração normal exigida | Commit **`b21e236`** restaurou os quatro JSONs, o teste estático e excluiu o documento que apresentava o perfil temporário como ativo |
| P1 | Task 1.3 não tinha proteção do raio de chegada que já existia para a vegetação | `DreamFormationFeature` rejeita raízes no raio **36 + size + 4** em torno da origem; `DreamVegetationFeature` passou a usar **48** blocos para incluir a projeção das copas |
| P1 | Na 26.3, `dream_log_block` e `dream_shimmer_block` eram sólidos, mas faltavam nos quatro tags explícitos de motion/heightmap | Incluídos em `blocks_motion`, `blocks_motion_no_leaves`, `blocks_motion_in_heightmap` e `blocks_motion_in_heightmap_no_leaves`. **Folhas não entram nas variantes NO_LEAVES.** |
| P2 | Até quatro heightmaps de vizinhança da vegetação e oito das formações eram calculados mesmo antes do filtro probabilístico descartar a tentativa | Deslocadas as checagens de relevo para **depois** do gate de seed/ruído. Limites e verificações de terreno não foram relaxados. Reduz leituras em tentativas descartadas, sem alegar redução de FPS já medida |
| P2 | O smoke test anterior teve falha operacional de `downloadAssets` quando o endpoint Mojang respondeu HTTP **304** | Adicionado preflight de assets com **até três tentativas apenas nesse erro**. Qualquer outro erro continua falhando, e o servidor/testes continuam obrigatórios. Na execução de homologação o preflight passou na primeira tentativa; o caminho de retry não foi exercitado |
| P2 | CI anteriormente só cobria três seeds e não registrava indicadores temporais | Acrescentada a seed **73199013**, métricas RCON de forceload e consultas de altura; adicionada análise determinística `check_worldgen_balance.py` |
| P3 | As árvores não tinham limite explícito de entradas na lista final de escrita | Guarda `plan.size()>2200` rejeita candidatos anormais. **Não é limite prévio de alocação**; tamanhos de algoritmo/codec já são finitos |

**Limitações conhecidas, não mascaradas:** proteção radial é centrada na origem
da dimensão, pois é determinística; um servidor que defina um respawn
dinâmico muito distante ainda depende de `DreamSpawnHelper`. Este último
continua pesquisando piso caminhável e usa plataforma de contingência. Não
foi executado um teste com jogador real do sono/Despertador; é requisito manual.
No processo de worldgen, `ensureCanWrite` pode rejeitar um candidato cujo
volume exceda a região gravável — a geometria é descartada como um todo,
não truncada intencionalmente.

## 3. Balanceamento: parâmetros finais preservados

| Tipo | Tentativas/gate | Densidade | Dimensões |
| --- | --- | --- | --- |
| Sentinelas | 7 tentativas/chunk | 0,72 | altura 15–21 |
| Guarda-Chuva | 4 tentativas/chunk | 0,45 | altura 7–10 |
| Árvore arqueada | 2 tentativas/chunk | 0,20 | altura 11–16 |
| Jardim de flores | 3 tentativas/chunk | 0,48 | patch raio 2–5 |
| Musgo luminoso | 3 tentativas/chunk | 0,52 | patch raio 2–5 |
| Espinhas da Névoa | gate 38%/chunk | 0,80 | tamanho 3–7 |
| Jardim luminoso | gate 27%/chunk | 0,72 | tamanho 3–6 |
| Arco do Sonho | gate **5,5%/chunk** | **0,78** | tamanho 5–7 |
| Círculo de Vigília | gate **6,5%/chunk** | **0,72** | tamanho 4–7 |

Uma tentativa não equivale a uma formação realmente gerada. Ruído,
altitude, solo, declividade, colisão e permissões de escrita podem
rejeitá-la. Não foi aplicada mudança estética subjetiva nas frequências
comuns, silhuetas, surface rules ou formas dos arcos.

**Feedback real registrado:** durante o perfil de testes o usuário
**encontrou arcos e círculos** e observou que, por serem formações simples,
não precisam necessariamente ser tão raros. Por exigência explícita
desta Task 1.4, restauramos *exatamente* as taxas normais acima.
Uma eventual elevação futura deve ser decidida por contagem visual
de estruturas em áreas comparáveis — não nesta estabilização.

## 4. Proxies de distribuição calculados pela CI

`python3 scripts/ci/check_worldgen_balance.py` amostra 1600 **centros de
chunks** por seed, em janela fixa de 40×40 chunks. Reproduz matematicamente
os campos de ruído e estima oportunidades probabilísticas **a Y=110**.
Esses valores NÃO incluem relevo real, colisão, bordas, spawn, regiões
inacessíveis ou contagens reais de blocos/estruturas:

| Seed | Modelo de abertura (woodland < 0,10) | Modelo de bosque denso (woodland > 0,65) | Oportunidades arco / 256 chunks | Oportunidades círculo / 256 chunks |
| --- | ---: | ---: | ---: | ---: |
| 14011 | 22,4% | 55,0% | 3,93 | 7,25 |
| 20261108 | 48,2% | 31,2% | 4,70 | 6,14 |
| -904732 | 21,2% | 59,9% | 3,85 | 6,30 |
| 73199013 | 27,9% | 52,2% | 3,74 | 6,28 |

Este modelo evidencia **variação espacial e entre seeds** e pode detectar
mudanças de tuning em regressão; não comprova proporção efetiva de bosques
ou presença/ausência de círculos em qualquer chunk. O script mostra ainda
oportunidades de espinhas, jardins e árvores em log.

## 5. CI de homologação executado

**Código homologado:** commit `119e2e5d2e23688e162ec2f0b35820c6f582111b`.  
**GitHub Actions:** https://github.com/ErikMendesRc/dreamsdimensions/actions/runs/42537048072

| Seed | Java build | Teste vegetação | Teste formações | Modelo | Preflight assets | Dedicated server / datapacks / chunks |
| --- | --- | --- | --- | --- | --- | --- |
| 14011 | PASS | PASS | PASS | PASS | PASS | PASS |
| 20261108 | PASS | PASS | PASS | PASS | PASS | PASS |
| -904732 | PASS | PASS | PASS | PASS | PASS | PASS |
| 73199013 | PASS | PASS | PASS | PASS | PASS | PASS |

`check_formations.py` verifica 45 geometrias teóricas de arcos,
configurações, proteção de spawn, IDs, codec e quatro assinaturas de ruído.
`check_vegetation.py` valida os tags de colisão/heightmap e a proteção
ampliada. O smoke test inicia servidor **real**, carrega datapacks e força
cinco colunas azuis e uma Dreamscape, inspecionando piso natural e
espaço de cabeça nas proximidades.

**Métrica instrumental RCON da mesma execução:** `forceload_add_seconds`
(tempo de resposta às seis operações de forceload, NÃO tempo total de
geração independente), `heightmap_probe_seconds` (sondas finais) e
`total_including_settle_seconds` (inclui espera deliberada de 12 s):

| Seed | Forceload add (s) | Sondas heightmap (s) | Total incluindo espera (s) |
| --- | ---: | ---: | ---: |
| 14011 | 1,901 | 0,005 | 13,908 |
| 20261108 | 2,785 | 0,010 | 14,798 |
| -904732 | 3,532 | 0,010 | 15,546 |
| 73199013 | 2,635 | 0,008 | 14,646 |

Essas medições são **proxies operacionais de CI** e estão sujeitas a
variabilidade de runner, JIT, caching e ambiente. Não demonstram melhoria
percentual porque a versão anterior ainda não tinha a mesma instrumentação.
Não foram medidos TPS, MSPT, FPS ou tempo por chunk em um cliente real.

## 6. Riscos remanescentes / critérios pendentes

- [ ] Renderização, transições de biomas internos, neblina, clareiras e
  qualidade visual das três silhuetas em Minecraft cliente;
- [ ] Contagem real de **cada** formação em dezenas/centenas de chunks por
  seed (distinta do modelo de chances). Verificar estrutura inteira no
  limite entre chunks, bases de pilares e ausência de folhas órfãs;
- [ ] Entrar pela cama após 100 ticks e retornar pelo **Despertador Onírico**;
  inspecionar o comportamento da plataforma de fallback e a chegada
  em respawn alterado, sem danificar construções;
- [ ] Comparar mundos gerados com **a mesma seed** e o **mesmo commit**
  para confirmar visualmente coordenadas de árvores/estruturas;
- [ ] Comparar medições reais de FPS/MSPT, memória e duração de
  geração em máquinas equivalentes, com repetição e aquecimento;
- [ ] Testar cópia de um save antigo para verificar o
  `DreamLeavesBlock` e IDs persistidos.

**Conclusão responsável:** a integração está aprovada em compilação,
datapacks e smoke servidor para quatro seeds, pronta para **homologação
manual** antes de ser considerada baseline visual/performance final.
Não declarar aprovação estética, funcionamento de sono/retorno real ou
ausência de problemas em todas as seeds sem esses testes.

## 7. Checklist reproduzível no IntelliJ

1. Fazer backup do mundo antigo. `git switch migration/minecraft-26.3`,
   `git pull origin migration/minecraft-26.3`,
   `.\\gradlew.bat clean build`, `.\\gradlew.bat runClient` (Java 25).
2. Criar mundo NOVO, sem reaproveitar chunks, com seed `14011`;
   depois repetir com `20261108`, `-904732` e `73199013`.
3. No modo espectador, entrar pelo ciclo normal de sono e, se precisar,
   usar `/execute in dreamsdimensions:campo_onirico_azul run tp @s 0 160 0`.
   Atenção: o comando direto não testa o algoritmo de sono.
4. Ativar F3+G; percorrer ~20×20 chunks novos por seed, fotografar
   de longe e de perto as três árvores, espinhas, jardins, arcos e
   círculos. Registrar localização, tipo e resultado.
5. Confirmar água/encostas/árvores sem formações sobrepostas, bases dos
   arcos apoiadas e nenhuma borda de chunk com estrutura cortada.
   Quebrar um tronco e observar atualização natural de folhas.
6. Criar um SEGUNDO mundo novo com **a mesma seed e o mesmo commit**,
   revisitar X/Z equivalentes e confrontar capturas e contagem.
   Somente depois comparar versões diferentes no mesmo hardware.
7. Para profiling, manter distância de renderização/simulação e heap
   iguais; descartar primeira passada de geração (warm-up), fazer três
   passadas com saves frescos e registrar mediana de tempo, MSPT e FPS.
   Quando disponível, usar `/debug start` e `/debug stop` no servidor;
   não comparar os tempos de CI como se fossem FPS do cliente.
8. Testar entrada real pela cama (100 ticks) e saída com o
   Despertador Onírico, voltando ao Overworld, e verificar Dreamscape.
   Anotar falhas/avisos em `logs/latest.log`.

## Commits da tarefa

1. **`b21e236`** — revert do perfil temporário de inspeção da Task 1.3;
   remove `TASK_1_3_TEST_PROFILE.md` do estado ativo;
2. **`119e2e5`** — proteção de chegada, tags de heightmap, filtros
   antecipados, testes estáticos, quarta seed, métricas e preflight
   contra HTTP 304;
3. Este documento — evidências verificadas, parâmetros e aceite manual.

**Arquivos Java/JSON alterados:** `DreamVegetationFeature.java`,
`DreamFormationFeature.java`, os quatro tags JSON de
`data/minecraft/tags/block/blocks_motion*.json` e os quatro JSONs
`feature/placed_feature` de `dream_stone_arch` e `vigil_circle` para
restaurar frequências originais. `DreamWorldgen.java`, blocos, outros
biomas, noise settings, density functions e material rules permanecem
inalterados. Os demais arquivos alterados são
`scripts/ci/check_dimensions.py`, `check_formations.py`,
`check_vegetation.py`, novo `check_worldgen_balance.py` e
`.github/workflows/build.yml`.

**Incidente de infra anterior:** https://github.com/ErikMendesRc/dreamsdimensions/actions/runs/42535782492
(tentativa anterior do perfil 90%, `downloadAssets` recebeu HTTP 304;
outra execução verificou a seed com êxito). Isso não constitui evidência
de bug de codec ou de worldgen.
