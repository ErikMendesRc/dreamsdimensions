# Campo Onírico Azul — ajustes de noise_settings

Este arquivo documenta os ajustes aplicados no `campo_onirico_azul_noise.json` para manter o perfil do Overworld, mas com relevo mais calmo e onírico.

## Ajustes de relevo (mantendo o noise vanilla)
- **Multiplicador do `final_density` (`0.64 → 0.58`)**
  - Reduz levemente a amplitude geral do terreno, suavizando picos e vales sem remover as camadas vanilla de densidade.
- **Rugosidade do `preliminary_surface_level` (`mul` de `4 → 3.2`)**
  - Diminui a variação brusca de altura, deixando transições mais longas e orgânicas.
- **Clamp de altura do `preliminary_surface_level` (`±64 → ±56`)**
  - Evita extremos muito altos/baixos, mantendo colinas suaves sem “chapar” o mundo.
- **Offset base do `preliminary_surface_level` (`-0.703125 → -0.65`)**
  - Ajuste sutil para aliviar depressões profundas, preservando a diversidade natural do relevo.

## Ajustes de mar
- **`sea_level` reduzido para `48`**
  - Mantém o nível de água baixo, evitando oceanos grandes e reforçando o aspecto “campo” da dimensão.

## Substituição de blocos de terreno
- **Base rochosa:** `dreamsdimensions:blue_dream_stone`
- **Subsolo:** `dreamsdimensions:blue_dream_dirt`
- **Superfície:** `dreamsdimensions:blue_dream_grass`
- **Camadas profundas:** `dreamsdimensions:blue_dream_cobblestone`

Essas trocas garantem que nenhum bloco vanilla de terreno (stone/dirt/grass_block/deepslate) apareça na dimensão, mantendo a compatibilidade com cavernas, minérios e estruturas vanilla.
