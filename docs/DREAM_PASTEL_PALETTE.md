# Passe visual pastel — DreamsDimensions

**Branch:** `migration/minecraft-26.3` (sem merge na `main`)  
**Escopo:** apenas cores dos sprites existentes de blocos oníricos, sem alterar gameplay.

## Paletas

| Grupo | Materiais | Direção |
| --- | --- | --- |
| Campo Onírico Azul | `blue_dream_stone`, `blue_dream_cobblestone`, `blue_dream_dirt`, `blue_dream_grass_top`, `blue_dream_grass_side` | pervinca suave, terra azul-lilás, relva azul-menta |
| Dreamscape | `serene_stone`, `dream_dirt_block`, `dream_grass_block_top`, `dream_grass_block_side` | pedra lavanda, solo rosa empoeirado, relva verde-sálvia |
| Compartilhados entre sonhos | `dream_sand_block`, `dream_infused_stone`, `dream_shimmer_block`, `dream_leaves_block`, `dream_log_block`, `dream_log_block_top`, `dream_glow_moss`, `dream_flower_block` | creme, lilás, musgo menta, flores rosadas, madeira malva |

### Overworld
**Não foram recoloridos:** `marked_stone`, `cut_marked_stone`, `dream_ore`, `deepslate_dream_ore`, `dream_dust`. Esses recursos permanecem diferenciados do conteúdo vanilla pelo desenho original. A distinção de gameplay não depende de cor por si só.

## Restrições técnicas atendidas

- 17 arquivos PNG 16×16, RGBA, com transparência pixel a pixel preservada.
- Contornos, ruído, veios, grão e padrões de pixels continuam nas mesmas posições; somente canais RGB foram recalibrados.
- Código de recoloração em `scripts/art/recolor_pastel_png.js`, sem dependências de terceiros. Usa PNG com CRC32 validado, filtros PNG e zlib; o resultado é testado por roundtrip de todos os pixels antes do upload.
- A ferramenta compara os sprites atuais a resultados reconstruídos **a partir do baseline imutável** anterior ao passe de cores, evitando recoloração repetida:
  - `node scripts/art/recolor_pastel_png.js` — verificação read-only (PASS/DRIFT)
  - `node scripts/art/recolor_pastel_png.js --apply` — restaurar a paleta aprovada se houver divergência
- IDs, modelos, loot, receitas, worldgen, propriedades dos blocos, IA, partículas e geração de estruturas não foram modificados.
- Blocos compartilhados continuam usando a **mesma textura em todas as dimensões**; não há troca de textura em runtime.
- Overworld e sprites de itens/equipamentos não foram alterados neste passe.

## QA — Windows / PowerShell

```powershell
git switch migration/minecraft-26.3
git pull origin migration/minecraft-26.3
node scripts/art/recolor_pastel_png.js
python scripts/ci/test_visual_assets.py
python scripts/ci/audit_visual_assets.py --report qa-visual-assets.json
python scripts/ci/test_visual_release_gate.py
.\gradlew.bat --no-daemon clean build
.\gradlew.bat runClient
```

### Comandos rápidos, mundo descartável com cheats

```mcfunction
/gamemode creative
/give @s dreamsdimensions:blue_dream_stone 64
/give @s dreamsdimensions:blue_dream_cobblestone 64
/give @s dreamsdimensions:blue_dream_dirt 64
/give @s dreamsdimensions:blue_dream_grass 64
/give @s dreamsdimensions:serene_stone 64
/give @s dreamsdimensions:dream_dirt_block 64
/give @s dreamsdimensions:dream_grass_block 64
/give @s dreamsdimensions:dream_sand_block 64
/give @s dreamsdimensions:dream_infused_stone 64
/give @s dreamsdimensions:dream_shimmer_block 64
/give @s dreamsdimensions:dream_leaves_block 64
/give @s dreamsdimensions:dream_log_block 64
/give @s dreamsdimensions:dream_glow_moss 64
/give @s dreamsdimensions:dream_flower_block 64
```

Inspecionar as dimensões em **Spectator**, pois coordenadas arbitrárias não são locais seguros para Survival:

```mcfunction
/gamemode spectator
/execute in dreamsdimensions:campo_onirico_azul run tp @s 0 180 0
/execute in dreamsdimensions:dreamscape run tp @s 0 180 0
/execute in minecraft:overworld run tp @s 0 140 0
```

### Aceite visual manual ainda pendente

1. Comparar paredes 5×5 e blocos isolados sob luz natural, à noite e em cavernas; distinguir rocha, terra e relva por **textura + tonalidade**.
2. Conferir topo/lateral de grama e tronco; evitar costuras ou fronteiras artificiais.
3. Verificar alfafolhas/plantas/flor no mundo e inventário; comparar com árvores e plantas vanilla próximas.
4. Analisar a composição geral das duas dimensões. Revisar cores individualmente se alguma região ficar lavada ou com baixo contraste.
5. Testar screenshots com configurações gráficas comuns e, se possível, com shaders desabilitados e habilitados.

**Estado:** arquivos gerados e validáveis tecnicamente; **aprovação estética final e testes de cliente não executados aqui**.
