# TASK 1.1: Geografia onírica (Minecraft / NeoForge 26.3)

A dimensão preserva IDs e mantém a geometria isolada no `campo_onirico_azul_noise`. Todos os ruídos são amostrados na seed e em coordenadas absolutas, sem estado por chunk.

## Camadas
- Pradarias: `wave_height`, duas escalas de ruído, declives abertos.
- Terraços: `terrace_units`, patamares de 14 blocos unidos por rampa cúbica contínua (`terrace_ramp`) sem degraus descontínuos.
- Cordilheiras: `ridge_crest` produz faixas seguindo curvas de ruído; `ridge_height` aplica largura estreita e erosão secundária.
- `mist_strength` e `terrace_strength` misturam os três perfis em vez de delimitar regiões fixas. `warp_x`/`warp_z` dobram os contornos horizontalmente.
- `solid_density` usa gradiente vertical descendente, deslocamento de altitude, detalhe 3D pequeno, e interpolação a cada 4x8 blocos.
- `spawn_target` procura um vale com pouco relevo rochoso.
- `material_rule/campo_onirico_azul_surface`: bedrock natural na base, grama azul no topo caminhável, terra azul superficial e pedra azul nos paredões e subsolo. Todas as regras são data-driven; nenhum codec Java.

## Diagnóstico do spawn
A lógica anterior consultava `MOTION_BLOCKING_NO_LEAVES` dentro de raio 12 e descartava colunas com água, piso sem colisão ou falta de espaço. A densidade de `sloped_cheese` e cavernas do Overworld pode explicar áreas sem superfície, mas os logs anteriores não permitem atribuir o problema a um filtro específico. O raio agora é 24 só para Campo Azul; o fallback 5x5 é preservado. Não reutilize chunks legados para a validação geomorfológica.

## CI e limites
A GitHub Action usa seeds 14011, 20261108 e -904732. O servidor dedicado testa cinco colunas diferentes por seed com forceload e o heightmap `motion_blocking_no_leaves`, verificando piso azul e dois blocos de ar. O Dreamscape mantém seu teste original. Estas verificações não validam aparência, continuidade visual, sono, retorno, desempenho em hardware real ou save legado. Estes itens exigem testes manuais no IntelliJ.

## Manual
`./gradlew --no-daemon clean build`, `./gradlew runServer`, `./gradlew runClient`.
Em mundo novo de testes, entrar na dimensão usando `/execute in dreamsdimensions:campo_onirico_azul run tp @s 0 160 0` e modo espectador para sobrevoar; inspecionar F3, margens de chunks, inclinações, platôs, alturas, e logs de fallback. Testar 100 ticks de sono e retorno com Despertador Onírico; salvar cópia antes de testar versões antigas.

Fontes: https://docs.neoforged.net/primer/docs/26.3/ ; https://docs.neoforged.net/docs/concepts/registries/ ; https://github.com/NeoForgeMDKs/MDK-26.3-ModDevGradle ; https://github.com/neoforged/NeoForge/tree/26.3.x ; https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3
