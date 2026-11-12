# Task 8 unificada — Auditoria, Padronização, Ambientação e Polimento Visual

**Projeto:** DreamsDimensions · Minecraft 26.3 / NeoForge 26.3 / Java 25.  
**Branch de trabalho:** `migration/minecraft-26.3` · **Não fazer merge na main**.  
**Escopo:** Tasks 8.0, 8.1, 8.2 e 8.3 juntas, sem novos recursos, dimensões, sistemas ou alterações de balanceamento.

## Resultado executado

| Subetapa | Mudança realmente aplicada | Evidência |
| --- | --- | --- |
| **8.0 Auditoria** | Scanner read-only de arquivos PNG, resolução, CRC do cabeçalho, modelos, referências, duplicatas, traduções e Creative Tab. Relatório JSON por seed. | `scripts/ci/audit_visual_assets.py`, `test_visual_assets.py` |
| **8.1 Padronização** | Apresentação da Flor Onírica agora é sprite de item plano em vez de modelo 3D em cruz no inventário. Partículas do bloco apontam explicitamente para sua textura. | `models/item/dream_flower_block.json`, `models/block/dream_flower_block.json` |
| **8.2 Ambientação** | Quatro variações de rotação horizontal de um mesmo modelo para **9 blocos naturais já existentes** (Pedra Serena, Pedra Azul, Pedregulho Azul, Areia, Terra, Terra Azul, Musgo, Grama e Grama Azul); escolha estável por posição via blockstates do vanilla. | `assets/dreamsdimensions/blockstates/*.json` |
| **8.3 Polimento** | Creative Tab reordenada: equipamento/consumível, minérios e construção, pedra, terreno e natureza. Ícone da aba alterado para o Colar dos Sonhos, cujo sprite já existe. | `ModCreativeTabs.java`; validação de unicidade das entradas |
| **Coerência** | Removido código puramente experimental da Task 7.6 que nunca foi ligado ao jogo, seu catálogo e seus testes isolados. | Git diff; sem modificação dos sistemas ativos |

Os **22 itens/blocos registrados na Creative Tab foram preservados**, apenas reordenados. A mudança é de apresentação; não modifica IDs, Creative Tab registry, propriedades físicas, worldgen, loot, crafting, sono, IA ou persistência.

## Descobertas e dívida visual legítima

Os PNGs do repositório incluem artwork legado de alta resolução, muitos arquivos significativamente maiores do que os sprites 16×16 de Pedra Marcada / Pedra Marcada Lapidada. **Não reduzir cegamente essas texturas para 16×16**, porque um simples resize degradaria os detalhes originais e poderia gerar borrão ou perda de silhueta.

O relatório `qa-visual-assets.json` publica:
- resolução real e tamanho por textura com resolução fora do alvo;
- quantos arquivos de bloco não são 16×16 e quantos sprites de item excedem 64px;
- hashes de duplicatas binárias e candidatas a modelos sem referências diretas;
- paridade `en_us`/`pt_br`, consistência de `assets/items`, e 9 grupos de variações;
- **`visual_review: NOT_RUN`** até que alguém de fato abra o mod no cliente.

**Não se deve confundir arquivo de 1024px com arte melhor**: o objetivo é vanilla+, mas a próxima decisão sobre cada textura grande requer uma comparação 1:1 em cliente e um novo sprite desenhado em pixel art, não um redimensionamento automático por código. Se a fonte original parecer boa ao jogador, manter e otimizar de forma consciente.

### Melhorias condicionais — não executadas por falta de avaliação visual

- Substituir individualmente PNGs enormes por pixel art 16×16 após revisar o original, preservando leitura de ores, itens e vegetação.
- Medir legibilidade de tiling 5×5, ocorrência de rotações visivelmente artificiais e coerência entre verde/azul nas transições ambientais.
- Validar o sprite da Flor Onírica com canal alfa real, incluindo mão, chão, GUI e `/give`.
- Inspecionar duas entidades GeckoLib no cliente e FPS/MSPT com partículas reduzidas, sem aumentar spawn ou densidade.

## Testes no Windows — PowerShell

```powershell
git switch migration/minecraft-26.3
git pull origin migration/minecraft-26.3
python scripts/ci/test_visual_assets.py
python scripts/ci/audit_visual_assets.py --report qa-visual-assets.json
python scripts/ci/test_content_contract.py
python scripts/ci/check_content_contract.py --report qa-content-contract.json
python scripts/ci/test_repository_docs.py
python scripts/ci/check_repository_docs.py --report qa-repository-docs.json
.\gradlew.bat --no-daemon clean build
.\gradlew.bat runClient
```

### Galeria de inspeção no jogo

No mundo descartável com cheats:

```mcfunction
/gamemode creative
/give @s dreamsdimensions:dream_flower_block 64
/give @s dreamsdimensions:serene_stone 64
/give @s dreamsdimensions:blue_dream_stone 64
/give @s dreamsdimensions:blue_dream_cobblestone 64
/give @s dreamsdimensions:dream_dirt_block 64
/give @s dreamsdimensions:blue_dream_grass 64
/give @s dreamsdimensions:dream_sand_block 64
/give @s dreamsdimensions:dream_glow_moss 64
/give @s dreamsdimensions:marked_stone 64
/give @s dreamsdimensions:cut_marked_stone 64
```

Colocar paredes 5×5 das duas pedras e grama/terra, observar **de dia e à noite**, conferir escolha de orientação sem efeitos de costura grosseiros e olhar a Flor no inventário, na mão, no chão e no mundo. Testar também a aba criativa e o render dos mobs nas dimensões em **Spectator**:

```mcfunction
/gamemode spectator
/execute in dreamsdimensions:campo_onirico_azul run tp @s 0 180 0
/execute in dreamsdimensions:dreamscape run tp @s 0 180 0
/execute in minecraft:overworld run tp @s 0 140 0
```

**TPs apenas para observar**, sem afirmar que os locais são seguros para Survival. A tarefa não altera worldgen de novas ou antigas seeds, nem o número de criaturas.

## Gates de aceite

- **STATIC_CHECK:** PNG validado, 22 entradas criativas únicas, modelo de Flor, locale paritário, referências corretas, rotação deterministicamente configurada e sem assets faltantes.
- **CI:** build/JUnit + scripts e servidor nas mesmas 4 seeds. Nenhuma mudança na economia, loot, entrada, terreno ou respawn.
- **VISUAL_CLIENT:** `NOT_RUN` até player validar comparação lado a lado, inventário, mobs e performance. Não declarar “texturas aprovadas” com base apenas no relatório JSON.

**Decisão:** depois desta task, não lançar mais conteúdo visual por quota. O audit será a referência única para problemas evidenciados; mudanças adicionais devem atender um problema visual demonstrado, não um conceito genérico.
