# Task 8.4–8.7 — Acabamento visual e homologação verificável

**Stack:** Minecraft 26.3 · NeoForge 26.3 · Java 25  
**Branch:** migration/minecraft-26.3 · **sem merge na main**  
**Escopo:** recursos já existentes; *nenhum* novo bioma, dimensão, minério, loot, receita, sistema de entrada/retorno, spawn ou comportamento de Survival.

## 8.4 — Materiais, itens e vegetação

Foram substituídos **17 PNGs legados de altíssima resolução** por sprites de pixel art **desenhados diretamente em 16×16, sem redução por interpolação**. Os grupos trabalhados são:

- **Pedras:** `serene_stone`, `dream_infused_stone`, `blue_dream_stone`, `blue_dream_cobblestone`
- **Solo:** `dream_dirt_block`, `blue_dream_dirt`, `dream_sand_block`
- **Grama:** `dream_grass_block_{top,side}`, `blue_dream_grass_{top,side}`
- **Vegetação:** `dream_log_block`, `dream_log_block_top`, `dream_leaves_block`, `dream_glow_moss`, `dream_flower_block`
- **Material iluminado:** `dream_shimmer_block`

A base de terra nas texturas laterais de grama compartilha a mesma paleta/substrato de seus respectivos blocos de terra. Folhas e flor possuem alpha e continuam usando os modelos `cutout` registrados. Troncos preservam os três eixos; os anéis de topo e as linhas de casca têm padrões diferenciados.

**Colar dos Sonhos:** o ícone existente é leve, funciona na aba criativa e foi **preservado**, sem recriá-lo desnecessariamente. Também se mantiveram os sprites 16×16 de Pedra Marcada, Dream Ore/Dust e os três sprites 32×32 aprovados do Despertador/Núcleo.

**Importante:** a troca de textura afeta a aparência de blocos já colocados em saves antigos (esperado); não modifica seus IDs nem seus dados.

## 8.5 — Feedback e partículas com custo delimitado

- **Despertador**: partículas de canalização exclusivamente no cliente enquanto o jogador usa o Despertador em uma dimensão de sonho registrada. O efeito acompanha a mão de origem, começa discreto e muda suavemente do ciano ao violeta.
- `ALL`: até **14 partículas** ao longo dos 60 ticks; `DECREASED`: até **5**; `MINIMAL`: **zero**. O teste JUnit `OneiricFeedbackPacingTest` comprova esses limites.
- Ao soltar o item, mudar de dimensão, desconectar, morrer ou trocar de item, o estado visual é descartado. O contador visual **não** decide o sucesso do teleporte nem o consumo.
- **Núcleo Onírico**: `OneiricCoreBlock.animateTick` emite, no máximo, uma partícula ciano em aproximadamente um de cada 12 ticks aleatórios visíveis; nenhuma varredura de chunks, recurso persistente ou evento de servidor foi adicionado.
- **Ambiente do Campo Azul:** um único grão de pó suave pode cair sob folhas já existentes a cada três ciclos de partículas; exige bloco carregado e ar livre. O Dreamscape, a névoa, a música e o orçamento de geração foram preservados.

## 8.6 — Coerência ambiental sem worldgen novo

- Grama, terra, areia, pedra, casca, folhas e flores compartilham acabamento de pixel art na escala vanilla.
- A Pedra Infundida e o Dream Shimmer ganharam quatro alternativas rotacionais por `blockstate`; o tronco usa quatro rotações no eixo **Y** sem alterar os eixos horizontais.
- As nove rochas/gramas que já tinham rotações do passe 8.0–8.3 foram mantidas.
- **Nenhum arquivo de `data/.../worldgen` ou feature Java de colocação foi alterado**. Densidades, biomas, árvores, mobs, baús e estruturas permanecem iguais.

## 8.7 — Auditoria de referências e gates

- Removidos **15 modelos JSON de item** comprovadamente sem referência nas definições `assets/.../items/*.json` da versão 26.3. **Não** foram excluídos modelos de bloco, modelos GeckoLib, texturas de mobs ou IDs de registro.
- O auditor `scripts/ci/audit_visual_assets.py` exige agora:
  - os **17 sprites novos em 16×16 com PNG completo/CRC**;
  - as cinco texturas anteriores da família Overworld e os três PNGs premium 32×32;
  - as referências `models/*.json → textures/*.png` válidas;
  - zero modelos JSON de item sem consumidor entre as definições de cliente;
  - `pt_br/en_us`, `cutout`, eixos e rotações corretos.
- `scripts/ci/visual_release_gate.py` produz `ci_gate`, `manual_gate` e `full_homologation` separadamente; a CI faz os mesmos checks nos quatro servidores e **não inventa confirmação manual**.
- A fixture `scripts/ci/fixtures/visual_release_manual_template.json` lista 12 verificações humanas, incluindo duas pessoas em multiplayer, interrupção do Despertador, fallback de cama, compatibilidade com saves e FPS.

**Estado honesto:** CI `PASS` **não** significa aprovação da estética/Survival. O gate humano começa em `NOT_RUN` e o geral fica `PENDING` até que os doze casos tenham provas reais.

## Testar localmente — Windows PowerShell

```powershell
git switch migration/minecraft-26.3
git pull origin migration/minecraft-26.3

python scripts/ci/test_visual_assets.py
python scripts/ci/audit_visual_assets.py --report qa-visual-assets.json
python scripts/ci/test_visual_release_gate.py
python scripts/ci/visual_release_gate.py --report qa-visual-release.json
python scripts/ci/test_content_contract.py
python scripts/ci/check_content_contract.py --report qa-content-contract.json
python scripts/ci/test_repository_docs.py

.\gradlew.bat --no-daemon clean build
.\gradlew.bat runClient
```

### Comandos de comparação visual

Em um mundo descartável com cheats:

```mcfunction
/gamemode creative
/give @s dreamsdimensions:dream_necklace 1
/give @s dreamsdimensions:oneiric_awakener 2
/give @s dreamsdimensions:oneiric_core_block 16
/give @s dreamsdimensions:serene_stone 64
/give @s dreamsdimensions:dream_infused_stone 64
/give @s dreamsdimensions:dream_grass_block 64
/give @s dreamsdimensions:dream_dirt_block 64
/give @s dreamsdimensions:blue_dream_grass 64
/give @s dreamsdimensions:blue_dream_stone 64
/give @s dreamsdimensions:dream_log_block 64
/give @s dreamsdimensions:dream_leaves_block 64
/give @s dreamsdimensions:dream_flower_block 64
/give @s dreamsdimensions:dream_glow_moss 64
/give @s dreamsdimensions:dream_shimmer_block 64
```

Comparar paredes 5×5, blocos isolados, laterais/topos, grama ao lado de terra, transparência da flor/folhagem, inventário e luz diurna/noturna. **Não usar /tp como prova de descoberta natural**.

Para inspeção de ambas as dimensões **somente em Spectator**:

```mcfunction
/gamemode spectator
/execute in dreamsdimensions:campo_onirico_azul run tp @s 0 180 0
/execute in dreamsdimensions:dreamscape run tp @s 0 180 0
/execute in minecraft:overworld run tp @s 0 140 0
```

**Para testar o Despertador em Survival:** criar/identificar uma plataforma segura no mundo descartável, ativar `/gamemode survival` *após* confirmar a posição e segurar o Despertador por 60 ticks. Interromper antes de completar (20 e 59 ticks), repetir na offhand, em Creative e com duas pessoas. A entrada por sono continua desabilitada por padrão; usar um teleporte de laboratório **não homologa** a viagem natural de sono. Não testar isso em save principal.

### Registrar provas manuais

```powershell
Copy-Item scripts/ci/fixtures/visual_release_manual_template.json qa-visual-meus-testes.json
# Preencher status PASS/FAIL e evidence com caminhos de vídeos, prints ou logs reais
python scripts/ci/visual_release_gate.py --manual qa-visual-meus-testes.json --report qa-visual-review.json
```

Usar o código e as regras do jogo como fontes de verdade, não uma imagem conceitual ou um resultado de CI como substituto de um teste humano.
