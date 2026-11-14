# Task 9.1 — Baseline de Survival e ajuste visual da Pedra Infundida

**Branch:** `migration/minecraft-26.3`  
**Referência:** documento *DreamsDimensions: redesign de gameplay, progressão e ecossistema*, versão 2, 13/11/2026.  
**Escopo desta alteração:** refinar `dream_infused_stone`, preservar o gameplay existente e preparar a homologação do ciclo atual, sem adicionar os recursos propostos para as fatias futuras.

> **Status:** ASSET IMPLEMENTADO; VALIDAÇÃO ESTÁTICA COBERTA POR TESTE; SURVIVAL E MULTIPLAYER **NOT RUN**. Não classificar a Task 9.1 como integralmente homologada antes da sessão manual.

## 1. Alteração aplicada

- Substituída **apenas** `assets/dreamsdimensions/textures/block/dream_infused_stone.png` por pixel art nativa **16×16 RGBA**, opaca, sem upscale/interpolação.
- A paleta é cinza mineral próxima de `minecraft:stone`, com microfissuras escuras e poucos depósitos azul-acinzentados dessaturados do Pó Onírico. O bloco deve parecer uma **pedra do Overworld impregnada**, não um minério, gema, bloco neon ou terreno de dimensão.
- Modelo `minecraft:block/cube_all` e `items/dream_infused_stone.json` continuam usando **a mesma textura**; não foi criada imagem de item duplicada.
- Mantidos **IDs, receita, registros, blockstate com quatro rotações, drop, worldgen, efeitos, retorno e saves**.
- Verificação local do novo arquivo: **16×16**, alpha **255 em todos os pixels**, **239/256 pixels praticamente neutros**, PNG válido e comprimido sem canais de emissão. O julgamento de qualidade **dentro do jogo** ainda é obrigatório.

## 2. Baseline factual que a task preserva

| Elemento | Estado atual |
| --- | --- |
| `dream_dust` | Vem de `dream_ore` e `deepslate_dream_ore` no Overworld. Fortune afeta drop, Silk Touch devolve o bloco |
| Pedra Infundida | Bancada: `SD / DS` (`S=minecraft:stone`, `D=dream_dust`) → 1 bloco |
| Núcleo | 4 Pedras Infundidas + 4 Pós + 1 relógio → 1 `oneiric_core_block` |
| Colar | 3 cobres + 3 Pós + 1 fragmento de ametista → 1 `dream_necklace` |
| Despertador | Pena + Núcleo + frasco de vidro → 1 `oneiric_awakener`; Núcleo **ainda é consumido** |
| Consumo total inicial | 12 Pós por Núcleo/Despertador; mais 3 Pós para o Colar = **15 Pós** |
| Entrada por sono | `dream_entry_enabled=false` por padrão até liberação explícita após QA |
| Retorno | 60 ticks; consumo só após retorno confirmado; a política de segurança não foi alterada |

**Importante:** a Task 9.0 antiga do repositório descreve uma “9.1 — Preparação e Retorno F1”; o Game Design revisado organiza o início com a *Fatia 0* (medir) antes da *Fatia 1* (Núcleo/Erva/Carga). A presente Task 9.1 segue a definição recente de **baseline e textura**, sem antecipar a implementação funcional F1.

## 3. Testes automatizados

O método `test_task_9_1_infused_stone_recipe_and_texture_wiring` foi incluído em `scripts/ci/test_visual_assets.py` para proteger:

- PNG 16×16 RGBA, assinatura e integridade de todos os chunks/CRC;
- padrão e materiais exatos da receita `SD / DS`;
- ID e contagem do item produzido;
- referência do bloco e do ícone à mesma textura.

### Executar localmente (PowerShell)

```powershell
git switch migration/minecraft-26.3
git pull origin migration/minecraft-26.3
python scripts/ci/test_visual_assets.py
python scripts/ci/audit_visual_assets.py --report qa-visual-assets.json
python scripts/ci/test_content_contract.py
python scripts/ci/check_content_contract.py --report qa-content-contract.json
.\gradlew.bat --no-daemon clean build
.\gradlew.bat runClient
```

**CI/compilação:** registrar o resultado do pipeline disparado pelo commit; não presumir `PASS` antes de verificar a execução. Esta alteração não foi jogada em cliente local nesta sessão.

## 4. Roteiro visual em mundo descartável

```mcfunction
/give @s dreamsdimensions:dream_infused_stone 64
/give @s minecraft:stone 64
/give @s dreamsdimensions:marked_stone 64
/give @s dreamsdimensions:cut_marked_stone 64
/fill ~1 ~ ~1 ~5 ~ ~5 dreamsdimensions:dream_infused_stone
/fill ~7 ~ ~1 ~11 ~ ~5 minecraft:stone
/fill ~13 ~ ~1 ~17 ~ ~5 dreamsdimensions:marked_stone
/fill ~19 ~ ~1 ~23 ~ ~5 dreamsdimensions:cut_marked_stone
```

Comparar no sol e na sombra, isolada, em piso/paredes 5×5, nos quatro ângulos e no inventário. Capturar print e registrar **PASS/FAIL**. Não usar estes comandos para medir tempo de progressão natural.

## 5. Baseline Survival — formulário de evidências manual

**Novo mundo e seed registrada, sem cheats:** usar um cronômetro e não usar `/give`, `/tp` ou Creative nesta medição.

| Caso | Evidência esperada | Status |
| --- | --- | --- |
| S1 — Obter 15 Pós | Tempo, Y, ferramenta, Fortuna e seed | NOT RUN |
| S2 — Obter cobre, ametista, ouro, redstone | Tempo acumulado até cada recurso | NOT RUN |
| S3 — Pedras Infundidas e Núcleo | Crafting completo sem comandos; prints e custo | NOT RUN |
| S4 — Colar e primeiro Despertador | Tempo total e inventário/custo | NOT RUN |
| S5 — Segundo Despertador | Tempo e custo marginal atuais | NOT RUN |
| S6 — Interromper uso do Despertador | 20/59 ticks: sem teleporte nem consumo | NOT RUN |
| S7 — Retorno após 60 ticks | Destino seguro e exatamente um consumo | NOT RUN |
| S8 — Perda do Despertador | Procurar ruína em Survival, tempo de recuperação real | NOT RUN |
| S9 — Respawn, morte, cama e save | Sem fuga grátis; persistência e regressão | NOT RUN |
| S10 — Servidor dedicado com dois jogadores | Retorno, offhand, relog, sem duplicações | NOT RUN |
| V1 — Repetição visual e lado a lado | 5×5, Stone, Pedra Marcada, inventário | NOT RUN |

### Diagnóstico em mundo separado, com cheats

```mcfunction
/give @s dreamsdimensions:dream_dust 32
/give @s minecraft:stone 32
/give @s dreamsdimensions:oneiric_core_block 1
/give @s dreamsdimensions:dream_necklace 1
/give @s dreamsdimensions:oneiric_awakener 2
/gamemode spectator
/execute in dreamsdimensions:dreamscape run tp @s 0 180 0
/execute in dreamsdimensions:campo_onirico_azul run tp @s 0 180 0
/execute in minecraft:overworld run tp @s 0 140 0
```

**Atenção:** coordenadas são pontos de inspeção em Spectator, não spawns comprovadamente seguros para Survival. A entrada por sono continua bloqueada por padrão. Não habilitar entrada pública apenas porque o PNG e as receitas passaram nas verificações estáticas.

## 6. Gate de conclusão

- **Implementação visual:** entregue.
- **Integridade e contrato do asset:** verificação local feita e regressão adicionada ao teste existente.
- **Build/CI do commit:** aguardar confirmação observável.
- **Homologação manual completa:** pendente por exigir o cliente Minecraft, mundo sem cheats e sessão multiplayer.

**Próximo passo após fechar os gates:** desenvolver a Fatia 1 (Núcleo permanente, Erva da Vigília e Carga de Retorno), de acordo com o novo documento de Game Design.
