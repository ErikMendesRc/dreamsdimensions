# TASK 3.1 — DREAMSCAPE: O Sonho Fragmentado

**Branch:** `migration/minecraft-26.3` · Minecraft/NeoForge 26.3 · Java 25.
**Identidade:** `dreamsdimensions:dreamscape` (mantida). **Sem merge na main.**

## Arquitetura e regiões

Worldgen nativo, puramente via datapack: 12 `worldgen/noise/dreamscape_*.json`, 15
`worldgen/density_function/dreamscape_*.json`, um material rule e o
`dreamscape_noise.json` reconfigurado. Todas as entradas usam prefixo
`dreamscape_`; não alteram as density functions do Campo Azul.
Sem Java customizado, `WorldGenRegion`, features que cruzam chunks,
colocação de blocos por tick ou `Random` global.

1. **Terras da Memória:** `memory_macro` (ruído amplo -9, três oitavas),
   `memory_relief` (-7, três oitavas), warp horizontal ±27 blocos.
   `memory_height = 36*macro + 18*relief + 7*abs(macro)`.
   `mainland` combina altitude base média ~115 (gradiente Y: 9 em Y0,
   -11 em Y256), `0.074*memory_height` e `0.16*rock_weathering` 3D.
   Altitudes variam em campos contínuos; assimetria sem quantização ou grade.
2. **Fraturas do Sonho:** `fracture_region` (0..1) restringe fraturas a
   macrorregiões. `fracture_width` é um halo suave da isocurva
   `abs(fault_spine)≈0` com modulação por `fault_width`.
   `fracture_activity=region*width` e escavação máxima 8.6 unidades
   de densidade, apenas acima de Y6 (intensidade completa em Y44).
   Zero nos núcleos intactos, transições graduais e fendas curvas.
3. **Pontes/arestas geológicas:** `bridge_strength` seleciona zonas raras
   em ruído independente. Nas interseções entre essas zonas e fraturas,
   a escavação diminui gradualmente **apenas acima de Y74..112** (de
   8.6 para no mínimo 0.5). Isto permite cristas/abóbadas com vazio
   abaixo que permanecem presas a terrenos nas laterais; **não**
   garante uma ponte transitável em cada encontro.
4. **Fragmentos suspensos:** `island_region` é máscara regional;
   `island_footprint=3.9*sky_contours + 3*island_region - 2.05`.
   `island_volume=min(footprint,gradiente inferior Y145..178,
   gradiente superior Y176..230) + 0.28*sky_weathering` 3D.
   A interseção dos envelopes é uma **massa volumétrica**, não discos
   desenhados por features. Topos Y~160–220 conforme ruído local,
   bases pontiagudas/erosionadas, distribuição regional.
5. **Composição 3D:** `solid_density=max(mainland-fracture_excavation,
   island_volume)`, interpolação 4×4 em `final_density`.
   `chunk_surface_level=find_top_surface` examina 0..248 com passo 8
   na densidade bruta combinada; pode retornar ilha como topo.
   Uma ilha acima de terra altera o heightmap real; nunca presumir
   que o topo do heightmap é a massa continental.

## Materiais e spawn

Sem blocos ou texturas novos. `dreamscape_surface` usa
`serene_stone` nos paredões (`minecraft:steep`) e no interior,
`dream_grass_block` em pisos superficiais, `dream_dirt_block`
nas camadas superiores. `sea_level=-64`, aquifers ausentes.
`spawn_target` prioriza `fracture_activity=0..0.08` e
`island_region=0..0.30` para favorecer continente intacto.
Busca natural **12** blocos, colisão, pés e cabeça, sono **100 ticks**,
plataforma 5×5 com `serene_stone`, seleção ponderada, retorno persistido
e Despertador permanecem idênticos ao motor da Task 2.2.
Corrigidos os quatro tags Minecraft 26.3
`blocks_motion{,_no_leaves,_in_heightmap,_in_heightmap_no_leaves}`
para reconhecer explicitamente os 3 blocos de piso Dreamscape, sem
remover entradas existentes do Campo Azul. Tag específico
`dreamscape_natural_ground` serve apenas às sondas, não a teleportes.

## Determinismo, limites e custo

- Normal noises vanilla dependem da **seed** e coordenadas absolutas;
  warp é contínuo e amostrado via `cache+slice(Y=0)`.
  Nenhum hash de chunk, RNG por tick ou dependência de ordem de load.
- Altura existente **Y0..255**; gradientes superior/inferior impedem
  massa suspensa acima de ~230, o terreno base cai para densidade
  negativa antes do teto. `find_top_surface` limitado a Y248.
- Doze NormalNoise e várias composições geram maior custo que o
  Dreamscape antigo; usar medição real de geração antes de otimizar.
  Interpolação 4×4 resolve gargantas e ilhas menores melhor que 8×4,
  mas custa mais amostras que a versão anterior. `cache` de campos
  regionais reduz trabalho repetido, não equivale a caching universal.
- Floating point 32 bits em 26.3: operações com fatores moderados,
  sem `pow` extremo, divisões por zero ou funções descontínuas.
  Máximos, mínimos e clamps são contínuos, mas derivadas podem
  ter quinas naturais; revisar visualmente.
- Saves antigos preservam IDs e blocos, **mas chunks já gerados não
  serão refeitos**: novos chunks ao lado de antigos podem apresentar
  uma fronteira visível. Faça backup; recomende mundo de teste novo.

## Testes e evidências

`python3 scripts/ci/check_dreamscape_geography.py` valida parsing de
todos os JSONs, dependências e ciclos do grafo de DF, ruídos auxiliares,
alturas, parâmetros, surface rule, spawn_target, tags, perfil de spawn
e proteção básica de compatibilidade com o Campo Azul.
Não simula o Perlin vanilla e **não** afirma determinismo ou
continuidade voxel a voxel baseado em uma aproximação matemática.

`python3 scripts/ci/check_dreamscape_geography.py --rcon` roda dentro do
servidor CI 26.3 para cada seed 14011, 20261108, -904732 e 73199013:
força sete chunks distribuídos, testa **56 colunas reais** de
heightmap/piso/cabeça, procura voxels inferiores escavados e fragmentos
suspensos e faz probes de possíveis arcos. Registra as **ocorrências
observadas**, sem garantir arcos conectados/ilha navegável só pela
existência de um voxel. As entradas do Campo Azul continuam validadas
pelo script anterior `check_dimensions.py`.
O workflow também roda `./gradlew --no-daemon clean build` no JDK25,
script do motor, flora, formações, distribuição e atmosfera.

**Evidência executada:** GitHub Actions
[run 42621896891](https://github.com/ErikMendesRc/dreamsdimensions/actions/runs/42621896891),
commit `5e9e2e52a47bb0eab2e87a61813a68b226b185b8`:
`./gradlew --no-daemon clean build`, scripts estáticos anteriores,
`check_dreamscape_geography.py`, início de servidor dedicado 26.3,
`check_dimensions.py` e `check_dreamscape_geography.py --rcon`:
**todos concluídos com sucesso nas quatro seeds**.

| Seed | Build e CI | Servidor e datapacks | Piso natural seguro / 56 | Colunas escavadas Y52 | Voxels suspensos | Indícios de ponte Y118/Y76 |
| --- | --- | --- | ---: | ---: | ---: | ---: |
| 14011 | PASS | PASS | 56 | 0 | 5 | 0 |
| 20261108 | PASS | PASS | 56 | 0 | 16 | 0 |
| -904732 | PASS | PASS | 56 | 0 | 8 | 0 |
| 73199013 | PASS | PASS | 56 | 11 | 0 | 0 |

**Interpretação correta:** são contagens de blocos/colunas **efetivamente
gerados**, não modelos sintéticos de Perlin e nem contagens de ilhas
completas. `suspended_voxels` exige `serene_stone` em Y168/184/200
e ar 16 blocos abaixo. `cracked_lower_columns` exige ar em Y52 e
rocha em Y16. `bridge_like_voxels` exige pedra em Y118 acima de ar
em Y76. O valor **zero** significa exclusivamente que **as amostras
fixas não detectaram essa assinatura**, não que a formação não exista;
nenhum arco conectado foi demonstrado. Colunas consideradas seguras
usam o heightmap `motion_blocking_no_leaves` e tag de piso natural
com dois blocos de ar. A prova de ilhas navegáveis, pontes apoiadas
nas duas extremidades, transição de chunks antigos/novos, seed
reproduzida em saves independentes e chegada após sono permanece
**PENDENTE de homologação manual/instrumentação adicional**.

Tempos de `forceload add` no CI (proxies sujeitos à carga do runner):
14011 = 1,949 s; 20261108 = 2,083 s; -904732 = 1,817 s;
73199013 = 3,569 s para os seis chunks do smoke tradicional.
Esses tempos NÃO são tempo por chunk nem permitem deduzir FPS/MSPT.

## Checklist de homologação manual

- [ ] Mundo **novo** por seed; entrar por sono completo (100 ticks);
      verificar local natural, espaço para andar, falha/fallback 5×5.
- [ ] Explorar pelo menos 1000 blocos em X/Z, em modo espectador e
      sobrevivência; percorrer planaltos, fraturas, ilhas e cavernas.
- [ ] Percorrer fronteiras de chunks e arcos; confirmar geologia
      conectada nos dois apoios e presença de vão inferior.
- [ ] Comparar mesma seed e coordenadas em dois saves novos (determinismo);
      testar sequência de carregamento de chunks diferente.
- [ ] Verificar cliffs/grama/terra e luminosidade ao amanhecer/noite;
      sem pedra flutuante isolada gerada por artefato de interpolação.
- [ ] Testar ilhas acima do spawn e teto do heightmap;
      conferir retorno via Despertador e `dream_return`.
- [ ] Comparar MSPT, tempo de geração, uso de memória e FPS com a
      versão anterior em condições idênticas.
- [ ] Regressão no Campo Azul (geometria, árvores, atmosfera, retorno).

## Referências oficiais

- https://docs.neoforged.net/primer/docs/26.3/
- https://docs.neoforged.net/docs/concepts/registries/
- https://github.com/neoforged/NeoForge/tree/26.3.x
- https://github.com/NeoForgeMDKs/MDK-26.3-ModDevGradle
- https://feedback.minecraft.net/hc/en-us/articles/48913133328013-Minecraft-Java-Edition-26-3
