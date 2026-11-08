# TASK 1.1: Geografia onírica (Minecraft / NeoForge 26.3)

A dimensão preserva IDs e mantém a geometria isolada no `campo_onirico_azul_noise`. Todos os ruídos são amostrados na seed e em coordenadas absolutas, sem estado por chunk.

## Camadas
- Pradarias: `wave_height`, duas escalas de ruído, declives abertos.
- Terraços: `terrace_units`, patamares largos de 18 blocos separados por escarpas suaves (`terrace_ramp`), com erosão localizada nos taludes (`terrace_scar`), sem degraus abruptos.
- Cordilheiras: `ridge_crest` produz faixas seguindo curvas de ruído; `ridge_height` aplica largura estreita e erosão secundária.
- `mist_strength` e `terrace_presence` misturam os três perfis em vez de delimitar regiões fixas. O núcleo dos terraços atenua parcialmente a interferência das cordilheiras, mantendo transições graduais. `warp_x`/`warp_z` dobram os contornos horizontalmente.
- `solid_density` usa gradiente vertical descendente, deslocamento de altitude, detalhe 3D pequeno, e interpolação a cada 4x8 blocos.
- `spawn_target` procura um vale com pouco relevo rochoso.
- `material_rule/campo_onirico_azul_surface`: bedrock natural na base, grama azul no topo caminhável, terra azul superficial e pedra azul nos paredões e subsolo. Todas as regras são data-driven; nenhum codec Java.

## Refinamento cinematográfico — Terraços do Sonho (TASK 1.1, segunda calibração)

- **Presença regional:** `terrace_strength` passa a ativar mais área útil com o ruído regional da própria seed. `terrace_presence` reforça o núcleo, sem criar fronteira abrupta: o peso é `clamp(1.45 * strength, 0, 1)^2`.
- **Mesas e patamares:** `terrace_broad` (oitavas -9 a -7) domina a topografia com amplitudes largas; `terrace_elevation` acrescenta variação secundária (92:20). Os níveis são quantizados a cada **18 blocos**, no intervalo de aproximadamente 0,65 a 8,8 unidades, mantendo altitude finita sem ultrapassar o teto da dimensão em condições normais.
- **Escarpas orgânicas:** `terrace_ramp` conserva uma interpolação smoothstep entre as frações **0,31 e 0,69** do nível. Assim aproximadamente 62% de cada faixa de altura fica em patamares mais planos e 38% representa a transição geomorfológica, antes da deformação pelos ruídos.
- **Erosão de encostas:** `terrace_weathering` adiciona textura de baixa amplitude (0,75 bloco nos patamares). `terrace_scar` concentra até cerca de 3,4 blocos de irregularidade no meio da escarpa, desaparecendo suavemente nas partes planas; gera reentrâncias, promontórios e taludes mais críveis sem arestas no valor da densidade.
- **Encontro das regiões:** `height_offset` preserva pradarias e cordilheiras fora dos terraços, mas reduz em até 60% a influência de `mist_strength` onde `terrace_presence` é máxima. No perímetro continua havendo blend contínuo, sem costuras por chunk.
- **Custo:** ruídos 2D com `y_scale: 0`, amostrados via `cache + slice(y=0)`, aproveitam o cache de altura na geração. Não adiciona estruturas, entidades, novas texturas, mudanças ao Dreamscape nem codecs Java.

**Validação:** usar **mundo novo** ou chunks ainda não gerados; chunks antigos não são regenerados pela alteração de datapack. Sobrevoar por vários centenas de blocos em espectador e procurar mesas escalonadas, patamares suficientemente largos para andar, paredões azuis e transições sem paredes artificiais. Inspecionar em múltiplas seeds, inclusive `14011`, `20261108` e `-904732`. O teste automatizado confirma carregamento de datapacks e superfícies/heightmap, não certifica o resultado estético ou a conectividade de todas as escarpas; estas exigem inspeção no cliente.

## Diagnóstico do spawn — causa confirmada no Minecraft 26.3
O Minecraft 26.3 introduziu os tags `#minecraft:blocks_motion_in_heightmap` e
`#minecraft:blocks_motion_in_heightmap_no_leaves`. O código antigo não adicionava
nenhum dos quatro blocos azuis a esses tags. No CI com três seeds o
`positioned over motion_blocking_no_leaves` apontava para bedrock na base
enquanto logo acima havia `blue_dream_stone`, confirmando uma discrepância entre
densidade presente e heightmap. Os quatro blocos foram adicionados (com
`replace:false`) aos dois tags de heightmap e aos tags
`#minecraft:blocks_motion` e `#minecraft:blocks_motion_no_leaves`, para preservar
a lógica de colisão e movimento prevista na 26.3.

Referências oficiais: https://www.minecraft.net/en-us/article/minecraft-26-3-snapshot-1
e https://feedback.minecraft.net/hc/en-us/articles/46861657001101-Minecraft-Java-Edition-26-3-Snapshot-1

## Investigação anterior do spawn
A lógica anterior consultava `MOTION_BLOCKING_NO_LEAVES` dentro de raio 12 e descartava colunas com água, piso sem colisão ou falta de espaço. A densidade de `sloped_cheese` e cavernas do Overworld pode explicar áreas sem superfície, mas os logs anteriores não permitem atribuir o problema a um filtro específico. O raio agora é 24 só para Campo Azul; o fallback 5x5 é preservado. Não reutilize chunks legados para a validação geomorfológica.

## CI e limites
A GitHub Action usa seeds 14011, 20261108 e -904732. O servidor dedicado testa cinco colunas diferentes por seed com forceload e o heightmap `motion_blocking_no_leaves`, verificando piso azul e dois blocos de ar. O Dreamscape mantém seu teste original. Estas verificações não validam aparência, continuidade visual, sono, retorno, desempenho em hardware real ou save legado. Estes itens exigem testes manuais no IntelliJ.

## Manual
`./gradlew --no-daemon clean build`, `./gradlew runServer`, `./gradlew runClient`.
Em mundo novo de testes, entrar na dimensão usando `/execute in dreamsdimensions:campo_onirico_azul run tp @s 0 160 0` e modo espectador para sobrevoar; inspecionar F3, margens de chunks, inclinações, platôs, alturas, e logs de fallback. Testar 100 ticks de sono e retorno com Despertador Onírico; salvar cópia antes de testar versões antigas.

Fontes: https://docs.neoforged.net/primer/docs/26.3/ ; https://docs.neoforged.net/docs/concepts/registries/ ; https://github.com/NeoForgeMDKs/MDK-26.3-ModDevGradle ; https://github.com/neoforged/NeoForge/tree/26.3.x ; https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3
