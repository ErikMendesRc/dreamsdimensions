# Task 7.5 — Homologação econômica e regressão da família F0

**DreamsDimensions · Minecraft 26.3 · NeoForge 26.3 · Java 25**  
**Branch:** `migration/minecraft-26.3` · **Sem merge na main**  
**Escopo:** homologar `marked_stone` → `cut_marked_stone`, sem conteúdo novo ou ajuste de geração.  
**Evidências:** `STATIC_CHECK`, `RUNTIME_OBSERVED` e `MANUAL_VERIFIED` são independentes.

## 1. Escopo e definição de pronto

A Task 7.5 é a última verificação da fatia vertical de geologia arquitetônica F0. Seu papel é detectar problemas de inventário, mineração, drop, stonecutter, persistência, acessibilidade, multiplayer, economia e convivência com sistemas anteriores.

**O que está no jogo desde 7.3–7.4:**
- `dreamsdimensions:marked_stone`; ocorrência natural em chunks novos do Overworld;
- `dreamsdimensions:cut_marked_stone`, material lapidado, com item/model/loot/textura;
- Conversão **1:1** no Stonecutter vanilla; picareta de pedra ou superior; nunca Dream Dust.
- Dois caminhos de worldgen: veios subterrâneos e afloramentos rochosos.

**O que esta task deliberadamente NÃO introduz:** terceira variante, ferramenta especial, geração retroativa, anomalia onírica, fertilizante, novo POI, proteção de chunk, HUD, telemetria por tick ou mudanças de balanceamento sem evidências.

**Conexão com DreamsDimensions:** a pedra é um vestígio geológico. O fenômeno onírico que explica as marcas continua não implementado; a futura etapa de Anomalias Oníricas precisa criar **uma relação causal e uma descoberta real**. Não confundir a 7.5 com a implementação de uma anomalia.

## 2. Sistema de QA implementado

### `scripts/ci/f0_homologation.py`

Reutiliza a Task 7.4 para validar:
1. Recipe DAG **sem ciclos**, incluindo alternativas de forno/craft, com F0 tendo apenas `1 marked_stone → 1 cut_marked_stone`;
2. Fonte F0 natural, sem receita gratuita que gere `marked_stone`; sem Dream Dust na conversão;
3. Loot único por bloco, sem extra de progresso;
4. Comportamento e taxa de minério onírico comparados a 5.6;
5. Duas ruínas dimensionais com baús nativos realmente detectados por seed, sem alterar sua recuperação;
6. Quatro seeds obrigatórias, identificadas corretamente, com arquivos de provas reais;
7. Métricas por chunk, faces expostas e material bruto necessário para obras de 64 e 256 blocos;
8. Resultados manuais mantidos como `NOT_RUN` até que haja **evidência de usuário**.

Os testes negativos vivem em `scripts/ci/test_f0_homologation.py` e rejeitam seed faltante/duplicada, alteração substancial de minério, ruína quebrada, worldgen artificial disfarçado e homologação sem comprovação.

### `scripts/ci/audit_f0_loot_runtime.py`

Ensaio adicional no servidor descartável de CI: coloca temporariamente `marked_stone` e `cut_marked_stone`, consulta o **sistema de loot do jogo** usando `/loot ... mine ... minecraft:stone_pickaxe` e confirma item de saída; remove os itens, substitui a amostra por ar e libera o `forceload`.

**Cuidado:** comando `/loot mine` executa um contexto de loot, mas **não realiza uma quebra real por jogador**, não mede ferramenta de madeira, Durability, Fortune/Silk Touch nem prova combate/multiplayer. O tipo `RUNTIME_OBSERVED` desse teste indica **somente execução de loot nativa**.

### Arquivos de evidência

- `qa-f0-static-seed-<seed>.json`: validade estática, grafo de receitas, economia F0;
- `f0-native-loot-seed-<seed>.json`: verificação nativa de contexto de loot em servidor;
- `marked-stone-worldgen-seed-<seed>.json`: blocos de geração natural real;
- `ore-audit-seed-<seed>.json`: Dream Ore + Deepslate Dream Ore no Overworld;
- `dream-ruins-seed-<seed>.json`: evidência de duas ruínas naturais;
- `f0-homologation-summary.json`: relatório integrado da 7.5;
- `docs/F0_TASK_7_5_MANUAL_TEMPLATE.json`: **template vazia**, nunca preenchida com resultados inventados.

## 3. Baseline e limites reais

### Geração natural real 7.4 (256 chunks, quatro seeds, 64 por seed)

| Seed | Pedras Marcadas | Com uma face adjacente ao ar |
| --- | ---: | ---: |
| 14011 | 517 | 13 |
| 20261108 | 435 | 4 |
| -904732 | 477 | 15 |
| 73199013 | 513 | 14 |
| **Total** | **1.942** | **46** |

Há 177 de 256 chunks com rocha, **7,586 blocos/chunk** e **2,37% com uma face adjacente ao ar** na amostra. As regiões são fixas e viesadas; isso não significa que o jogador encontra o material em 10 minutos nem que consegue minerar todos os 1.942 blocos.

### Compatibilidade com Dream Ore (Task 5.6 como baseline)

| Seed | Dream Ore (baseline) | Deepslate Dream Ore (baseline) |
| --- | ---: | ---: |
| 14011 | 1.455 | 1.355 |
| 20261108 | 1.467 | 1.404 |
| -904732 | 1.445 | 1.187 |
| 73199013 | 1.536 | 1.387 |

O relatório 7.5 compara as **mesmas quatro regiões 8×8 / 256 chunks por seed** e falha se o novo resultado diferir mais que **10% por tipo e seed**. Isso é uma tolerância de regressão de engenharia, não o objetivo econômico de abundância do jogo. Se a geologia for alterada propositalmente em outra task, precisa revalidar o baseline em vez de alterar silenciosamente a tolerância.

As ruínas dimensionais de recuperação devem continuar funcionando: duas por seed; nenhum loot, estrutura ou restrição de entrada em sonho foi modificado.

## 4. O que conta como aprovação F0?

**`CI_GATE=PASS`** exige registro/asset/JSON/recipe, testes negativos, mundo real em 4 seeds, regressão Dream Ore, ruínas e execução do contexto de loot. Ele **não comprova `SURVIVAL_VERIFIED`**.

**`SURVIVAL_GATE=PASS`** exige testes manuais com evidências de F0. O relatório considera:
- Descoberta sem `/tp`, `/give`, `/locate` ou `/setblock`, em mundo novo/chunk ainda não gerado;
- Picareta de **madeira sem drop**, pedra/ferro com **um** bloco de volta;
- Fortune/Silk Touch sem multiplicação;
- Stonecutter **1 entrada → 1 saída**, sem duplicação;
- Construir 64 blocos, testar projeto de 256 blocos e medir volume/tempo real;
- Comparar textura bruto/lapidado e tiling 3×3/5×5, sol/sombra, ícone e partículas;
- Salvar/reabrir mundo novo e mundo anterior sem retro-gen; verificar blocos colocados;
- Dois jogadores no dedicado, coleta e corte concorrentes sem desync;
- Não afirmar que o mundo exibe anomalia onírica **antes de implementar essa mecânica**.

**P50/P90 de descoberta:** usar sessões de exploração geológica elegível (não desertos aleatórios sem habitat), observar se houve primeira descoberta e o tempo de sessão, registrar tentativas sem encontrar como **censura à direita**. O agregador utiliza Kaplan-Meier com mínimo de **10 sessões** para calcular percentis somente se identificáveis. Metas experimentais da Art/Balance Bible: **P50 de 10–25 minutos e P90 abaixo de 60 minutos**. Se dados insuficientes, percentis continuam `null`/`NOT_RUN`. Não preencher com o tempo de 1 tester e não atribuir P90 ao subset só dos que tiveram sucesso.

**Sinal de sucesso econômico:** um jogador que investigou um afloramento deve conseguir usar material real em uma pequena construção, e deve existir uma rota plausível até obras grandes com progressão futura (Argamassa/F1). O cálculo 64/256 representa quantidade de matéria-prima requerida **não** um tempo de produção aprovado. Uma rocha construtiva deve ser obtida sem 12 horas de mineração, mas não precisa ser tão comum quanto terra vanilla.

## 5. Como executar no seu PC (Windows)

Na pasta do projeto:

```powershell
git switch migration/minecraft-26.3
git pull origin migration/minecraft-26.3
python scripts/ci/test_content_contract.py
python scripts/ci/check_marked_stone.py
python scripts/ci/check_marked_stone_7_4.py
python scripts/ci/test_f0_homologation.py
python scripts/ci/f0_homologation.py --static --output qa-f0-static.json
python scripts/ci/f0_homologation.py --manual-template --output qa-f0-manual.json
.\gradlew.bat --no-daemon clean build
.\gradlew.bat runClient
```

Para preencher e avaliar resultados manuais, copie `docs/F0_TASK_7_5_MANUAL_TEMPLATE.json` para um arquivo fora do repositório, preencha somente o que você realmente testou (status `PASS/FAIL/NOT_RUN` + evidência referenciável) e execute:

```powershell
Copy-Item docs/F0_TASK_7_5_MANUAL_TEMPLATE.json qa-f0-meus-testes.json
python scripts/ci/f0_homologation.py --manual-review --manual qa-f0-meus-testes.json --output qa-f0-manual-review.json
```

A avaliação **não marca PASS sozinha**. O programa exige uma referência a evidência real (foto/log/notas rastreáveis) para cada teste marcado. Use `NOT_RUN` até testar; nunca editar o template da CI para fingir playtest. Evite publicar arquivo com dados pessoais.

Cada sessão de descoberta real em `discovery_sessions` tem o seguinte formato — **exemplo de preenchimento, não resultado medido**:

```json
{
  "id": "sessao_001",
  "environment": "eligible_geology_overworld",
  "duration_minutes": 28,
  "found": false
}
```

Se `found=false`, a sessão terminou sem descoberta e é computada como **observação censurada**, nunca como “tempo zero” ou sessão removida. Com `found=true`, informe o tempo decorrido até a **primeira** descoberta natural. Registre seed, versão e local nos apontamentos, mas não inclua identidade dos jogadores. É preciso pelo menos dez sessões elegíveis e observações suficientes para calcular P50/P90.

### Comandos de inspeção / QA sem estragar sua experiência de descoberta

**Primeira sessão:** crie um mundo novo numa seed escolhida, jogue em Survival sem cheats e registre tempo até achar Pedra Marcada em caverna/afloramento natural. Se ela não apareceu, registre também isso como sessão censurada. Não usar o seguinte TP nesse primeiro teste.

**Teste isolado de mineração e lapidação** (em outro mundo com cheats/OP):

```mcfunction
/gamemode creative
/give @s dreamsdimensions:marked_stone 64
/give @s dreamsdimensions:cut_marked_stone 64
/give @s minecraft:stone_pickaxe 1
/give @s minecraft:wooden_pickaxe 1
/give @s minecraft:iron_pickaxe 1
/give @s minecraft:stonecutter 1
```

Coloque blocos no chão firme, monte paredes 3×3/5×5 e um Stonecutter. Insira **1 Pedra Marcada** e veja se retorna **1 Pedra Marcada Lapidada**. Teste stacks de 64/256 de forma realista, anotando quantos blocos recuperou ao minerar.

Depois, ainda no chão seguro:

```mcfunction
/gamemode survival
```

Quebre blocos diferentes com picareta de madeira, pedra e ferro, com e sem encantamentos. Conte os itens recolhidos. Faça `Esc → Save and Quit`, reabra e confira que os blocos continuam.

**Teleporte para uma ocorrência da CI, exclusivamente para debug** (seed `73199013`, chunks novos da versão 7.4+):

```mcfunction
/gamemode spectator
/execute in minecraft:overworld run tp @s -49 15 -33
```

Coordenada realmente observada com face exposta ao ar, mas **não é um ponto seguro de aterrissagem em Survival** e não existe necessariamente no seu save.

Para testar compatibilidade entre dimensões, somente como Spectator:

```mcfunction
/gamemode spectator
/execute in dreamsdimensions:dreamscape run tp @s 0 180 0
/execute in dreamsdimensions:campo_onirico_azul run tp @s 0 180 0
/execute in minecraft:overworld run tp @s 0 140 0
```

Se quiser testar a geração real no próprio servidor de desenvolvimento, o comando `/marked_stone_audit -4 -4 4` só está disponível com JVM opt-in `-Ddreamsdimensions.enableMarkedStoneAudit=true` e exige chunks da região já carregados. Não habilitar scans/forceload permanentes em servidor público.

## 6. Critérios para a próxima fase (anomalias do Limiar)

A F0 já tem **um ciclo econômico**. Ainda não tem **fenômeno dos sonhos** no Overworld. Depois do QA manual, a decisão do projeto será introduzir um **contrato de Anomalias Oníricas** reutilizável, no qual as formações naturais de Pedra Marcada podem servir de pistas verdadeiras, sem ativação infinita de blocos colocados em bases. **Isso é design futuro, não uma funcionalidade implementada pela Task 7.5.**

Nenhuma taxa de spawn, flora, mob, estrutura, receita de Dream Dust ou regra de viagem foi alterada nesta homologação.

## 7. Links internos

- [Contrato 7.2](DREAMSDIMENSIONS_TASK_7_2.md)
- [F0a 7.3](DREAMSDIMENSIONS_TASK_7_3.md)
- [F0b 7.4](DREAMSDIMENSIONS_TASK_7_4.md)
- [Economia 6.8](OVERWORLD_ECONOMY_RARITY_BALANCING_TASK_6_8.md)
- [Roadmap 6.9](OVERWORLD_EXECUTIVE_IMPLEMENTATION_ROADMAP_TASK_6_9.md)
