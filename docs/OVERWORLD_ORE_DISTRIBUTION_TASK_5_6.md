# TASK 5.6 — Distribuição observada de Minérios Oníricos no Overworld

**Natureza:** **MEDIÇÃO REAL** de voxels em chunks novos gerados por Minecraft 26.3 / NeoForge 26.3, não modelo de probabilidade nem estimativa de JSON.
**Execução validada:** [GitHub Actions 42937528410](https://github.com/ErikMendesRc/dreamsdimensions/actions/runs/42937528410), 2026-11-10 (log GitHub em UTC: 2026-11-11).
**Commit medido:** [`2e3d8bb898ced73b8f1a539bc3ee02b14890e2b2`](https://github.com/ErikMendesRc/dreamsdimensions/commit/2e3d8bb898ced73b8f1a539bc3ee02b14890e2b2), branch `migration/minecraft-26.3`.
**Resultado de CI:** 4/4 jobs `success` com JDK25, Gradle clean build+JUnit, validação JSON/economia/Recipe Book, servidor dedicado, datapacks, checks e RCON de minérios.

## 1. Metodologia reproduzível

Em cada seed `14011`, `20261108`, `-904732`, `73199013`:
1. Criar mundo descartável de teste, novo, com seed explicitamente passada no workflow. Validar a seed em runtime por `/seed`.
2. Iniciar servidor dedicado `runServer`, mod normal com única opção de diagnóstico `-Ddreamsdimensions.enableOreAudit=true` (somente CI).
3. Carregar via `/execute in minecraft:overworld run forceload add` **quatro regiões espaciais não sobrepostas** de 8×8 chunks cada. Coordenadas dos cantos iniciais de chunk: `(-4,-4)`, `(64,-4)`, `(-68,64)`, `(4,-68)`. Cada região = 64 chunks, total = **256 chunks por seed**.
4. Executar `dream_ore_audit chunk_x chunk_z 8`, que **só lê chunks carregados** (não gera/chaveia minérios). Percorrer todos os blocos Y=-64..64 inclusive; contabilizar estado exato `dreamsdimensions:dream_ore` e `dreamsdimensions:deepslate_dream_ore`; coletar bioma no voxel, Y e quantidade de blocos adjacentes a ar. Recusar se qualquer chunk não estiver carregado, para não contar ausência de chunk como ausência de minério.
5. Logar resumo e exemplos de posições naturais; remover tickets forceload. Gravar JSON estruturado `ore-audit-seed-<seed>.json` e `server-smoke.log` como artefatos da CI.

**Distinção fundamental:** o `/forceload` **carrega e gera terreno real** em chunks novos; o auditor **não usa** `/setblock`, `/place feature`, `/give`, aleatoriedade substituta nem altera o mundo. Os blocos contados existem em worldgen real. O método seleciona regiões previamente definidas, portanto não garante amostragem estatística de todos os biomas. Os exemplos de XYZ são posições dos **minérios observados**, nunca destinos seguros de jogador.

## 2. Resultados por seed — 256 chunks cada

| Seed | Chunks com minério / 256 | Dream Ore | Deepslate Dream Ore | Total de blocos | Expostos a ar (normal+deep) |
| --- | ---: | ---: | ---: | ---: | ---: |
| `14011` | 256 | 1.455 | 1.355 | 2.810 | 52 + 32 = **84** |
| `20261108` | 256 | 1.467 | 1.404 | 2.871 | 30 + 46 = **76** |
| `-904732` | 253 | 1.445 | 1.187 | 2.632 | 32 + 48 = **80** |
| `73199013` | 255 | 1.536 | 1.387 | 2.923 | 42 + 42 = **84** |
| **TOTAL** | **1.020 / 1.024** | **5.903** | **5.333** | **11.236** | **156 + 168 = 324** |

- Densidade média observada na amostra: **10,97 blocos dos dois minérios por chunk**, incluindo os chunks sem minério.
- Chunks com pelo menos um minério: **99,61%** (1.020/1.024).
- Blocos adjacentes a ar: **324 de 11.236**, cerca de **2,88%**; **não** significa que somente 2,88% podem ser minerados (blocos enterrados podem ser encontrados em túneis).
- Variação por seed: **2.632 a 2.923 blocos por 256 chunks** nos quatro locais amostrados; não é uma distribuição universal.
- A presença de minério em quase todo chunk **não prova que o jogador o encontre facilmente**, especialmente em cavernas. A dificuldade de descoberta/tempo em Survival continua sem medida.

## 3. Comparação de quatro regiões por seed

Os centros abaixo são localizações planejadas da **amostragem**, e não minérios descobertos. Cada célula `normal / deep` conta **blocos reais**.

| Seed | (-4,-4) | (64,-4) | (-68,64) | (4,-68) |
| --- | ---: | ---: | ---: | ---: |
| `14011` | 414 / 342 | 395 / 290 | 288 / 371 | 358 / 352 |
| `20261108` | 397 / 357 | 372 / 342 | 341 / 336 | 357 / 369 |
| `-904732` | 381 / 328 | 296 / 230 | 431 / 338 | 337 / 291 |
| `73199013` | 409 / 375 | 349 / 381 | 405 / 327 | 373 / 304 |

**Validade:** no job `-904732`, região (64,-4) tem 62 chunks com minério e (4,-68) 63; no job `73199013`, região (4,-68) tem 63; todas as demais têm 64. As quantidades totais refletem todos os 64 chunks inspecionados por região, incluindo os vazios.

**Diversidade geológica e de biomas efetivamente presente nos logs:** biomas como `minecraft:forest`, `minecraft:plains`, `minecraft:river`, `minecraft:deep_dark`, `minecraft:deep_lukewarm_ocean`, `minecraft:warm_ocean`, `minecraft:mangrove_swamp`, `minecraft:desert` e `minecraft:dripstone_caves` apareceram como bioma do bloco de minério. Isso mostra que a amostra não ficou limitada a um único tipo de bioma. Não confundir isso com teste de TODOS os biomas do Overworld, dos mods externos, Nether ou End.

## 4. Faixas de altura observadas (agregado de 4 seeds)

| Categoria | Y=-64..-33 | Y=-32..-1 | Y=0..31 | Y=32..64 | Total |
| --- | ---: | ---: | ---: | ---: | ---: |
| Dream Ore | 0 | 16 | 3.897 | 1.990 | **5.903** |
| Deepslate Dream Ore | 4.218 | 1.115 | 0 | 0 | **5.333** |
| **Total** | **4.218** | **1.131** | **3.897** | **1.990** | **11.236** |

O objetivo de amostrar camadas profundas/próximas de zero/positivas foi cumprido. Os dados são contagem de blocos observados em terreno gerado, não probabilidade de descoberta na superfície.

## 5. Exemplos de minérios NATURAIS realmente encontrados

Os seguintes `X Y Z` foram registrados por `DREAM_ORE_AUDIT_HIT` no **servidor dedicado** do workflow (não são coordenadas inventadas). Os exemplos `exposed=true` possuem bloco adjacente a ar na leitura do jogo; isto **não garante rota segura** a partir da superfície.

| Seed | Tipo | Bioma lido no voxel | XYZ do **minério** | Exposição |
| --- | --- | --- | --- | --- |
| `14011` | Dream Ore | `minecraft:forest` | **-55, 7, -50** | Não |
| `14011` | Deepslate Dream Ore | `minecraft:forest` | **1037, -49, -54** | **Sim** |
| `20261108` | Dream Ore | `minecraft:plains` | **-59, 8, -54** | Não |
| `20261108` | Deepslate Dream Ore | `minecraft:deep_dark` | **-54, -37, -54** | Não |
| `-904732` | Dream Ore | `minecraft:forest` | **-59, 32, -50** | **Sim** |
| `-904732` | Deepslate Dream Ore | `minecraft:forest` | **-50, -43, -55** | **Sim** |
| `73199013` | Dream Ore | `minecraft:mangrove_swamp` | **-50, 15, -52** | Não |
| `73199013` | Deepslate Dream Ore | `minecraft:mangrove_swamp` | **-49, -43, -55** | **Sim** |

**Para visitar as coordenadas em um mundo com a mesma seed e versão, em laboratório:**

```mcfunction
/gamemode spectator
/execute in minecraft:overworld run tp @s -55 9 -50
```

A posição `-55 9 -50` corresponde a 2 blocos **acima** do minério em `-55 7 -50` na seed `14011`. Pode ser interior de pedra, portanto **usar somente Spectator**. Para outros exemplos, usar `/execute in minecraft:overworld run tp @s X Y+2 Z`, com Y do registro e a seed correspondente. Não passar para Survival sem inspeção visual de piso livre e ausência de lava/queda.

**Cada seed deve estar em seu próprio mundo**: as coordenadas pertencem à seed do relatório; não usar `-59 32 -50` da seed `-904732` esperando o mesmo minério na `14011`.

## 6. Interpretação econômica e decisão de balanceamento

A implantação das features, seu biome modifier e a existência de blocos reais dos dois minérios no Overworld estão **COMPROVADAS** nas quatro seeds amostradas. A estrutura de loot com Fortune/Silk, tiers e crafting foi verificada estaticamente e carregada pelo servidor. O tamanho e o count originais foram mantidos.

**Decisão: nenhuma alteração na geração por enquanto.** A quantidade subterrânea total é considerável, mas só cerca de **2,9%** dos blocos da amostra tinham ar adjacente. O fator limitante da experiência pode ser **descoberta**, não falta de ocorrência. Alterar size/count antes de medir 3, 12, 15 Pós em Survival poderia tornar o Despertador fácil demais e destruir sua economia recorrente.

Não deduzir tempo para chegar a 15 Pós a partir de 11 blocos/chunk: isso ignora acesso, mineração, ferramentas, localização de cavernas, mobs, caminhos, minério oculto, Fortune e experiência do jogador. **Pó efetivamente coletado ainda NÃO FOI MEDIDO**, e não deve ser estimado como loot real. O teste Fortune/Silk é **PENDENTE**. Não fizemos tuning antes/depois; só a baseline do mod.

## 7. Evidências e limitações

- **GitHub Actions:** https://github.com/ErikMendesRc/dreamsdimensions/actions/runs/42937528410
- **JSONs por seed:** artefatos `overworld-ore-survey-seed-14011`, `overworld-ore-survey-seed-20261108`, `overworld-ore-survey-seed--904732`, `overworld-ore-survey-seed-73199013`; dentro de cada, `ore-audit-seed-<seed>.json`.
- **Logs:** `server-smoke-log-seed-<seed>`, `server-smoke.log`, linhas `DREAM_ORE_AUDIT status=OK`, `DREAM_ORE_AUDIT_BANDS`, `DREAM_ORE_AUDIT_HIT`, `OVERWORLD_ORE_RUNTIME_SAMPLE`.
- **Amostragem real:** **PASS**, 4/4, total 1.024 chunks, com quatro regiões disjuntas por seed.
- **Distribuição:** regiões pré-selecionadas; não amostragem probabilística aleatória, falta teste em conjunto amplo de mods/biomas, veios por conectividade não identificados, distância entre descobertas por jogador não medida.
- **Exposição:** vizinho ar de 6 faces, não caminho caminhável até o minério; água/fluido e caverna fechada não contam como ar.
- **Loot real:** `NOT RUN`, mineração de bloco em Survival não simulada pela sondagem de mundo.
- **Crafting real/Recipe Book:** `NOT RUN` com jogador real; JSON carregou e validação estática passou.
- **Performance:** sondagem restrita a ambiente descartável e não repetida em gameplay. Não fornece TPS de servidor público.
- **Outras dimensões:** restrição Overworld por biome modifier/JSON e ausência de injeção nos worldgens próprios (verificação data-driven); **não foram sondados voxels reais de Nether, End, Dreamscape ou Campo Azul para comprovar ausência física**.

## 8. Próximo playtest

Em Survival sem `/give`, medir com cronometragem e captura:
1. Pegar ferro e fabricar picareta; achar três pós e craftar Colar.
2. Continuar minerando até 12 pós adicionais; craftar 4 Pedras Infundidas, Núcleo e Despertador.
3. Comparar cavernas abertas com túneis no mesmo intervalo e repetição com Fortune III.
4. Testar Silk Touch, `keepInventory`, receitas no Recipe Book e clicar em minério com picareta inferior ao ferro.
5. Para testar o ciclo inteiro, usar Colar, sono, expedição, Despertador consumível e voltar; a Task 5.7 ainda será necessária para recuperação rara dentro dos sonhos.

**Critério para ajuste futuro:** decidir com base em tempo real para 3/12/15 Pós, visibilidade do minério, experiência de mineração e custo do Despertador. Se mexer em parâmetros, regenerar mundos descartáveis com **as mesmas seeds e regiões** e comparar baseline e variante, sem retrogeração de chunks antigos.

**Não mudar o consumo da 5.4, nem a chance da 5.3.**
