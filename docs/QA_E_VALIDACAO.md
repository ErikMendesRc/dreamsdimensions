# QA e validação do DreamsDimensions

**Versão de trabalho:** Minecraft 26.3, NeoForge 26.3.0.101, Java 25.  
**Branch:** `migration/minecraft-26.3`. Não realizar merge na `main` sem solicitação.

## Execução local — Windows

```powershell
git switch migration/minecraft-26.3
git pull origin migration/minecraft-26.3
.\gradlew.bat --no-daemon clean build
python scripts/ci/test_content_contract.py
python scripts/ci/check_content_contract.py --report qa-content-contract.json
python scripts/ci/check_marked_stone_7_4.py
python scripts/ci/test_f0_homologation.py
python scripts/ci/f0_homologation.py --static --output qa-f0-static.json
python scripts/ci/check_qa_contract.py
.\gradlew.bat runClient
```

Para teste manual no servidor dedicado:

```powershell
.\gradlew.bat runServer
```

Para **inventário criativo e F0** no mundo descartável com cheats:

```mcfunction
/gamemode creative
/give @s dreamsdimensions:marked_stone 64
/give @s dreamsdimensions:cut_marked_stone 64
/give @s minecraft:stonecutter 1
/give @s minecraft:stone_pickaxe 1
/give @s minecraft:wooden_pickaxe 1
/gamemode survival
```

Testar a conversão real `1:1` pelo menu Stonecutter, textura no cliente, drops com ferramentas diferentes, save/reload e obtenção de Pedra Marcada sem `/give` num **mundo novo**. Para visitar terreno com coordenadas de laboratório apenas em Spectator, a seed `73199013` apresentou uma Pedra Marcada natural exposta em `-49, 15, -33` na amostra da 7.4:

```mcfunction
/gamemode spectator
/execute in minecraft:overworld run tp @s -49 15 -33
```

Essa coordenada **não é segura para Survival nem existe em qualquer seed/salve**. Teleporte não comprova tempo de descoberta natural.

## CI e evidências

O GitHub Actions em `.github/workflows/build.yml` executa build, JUnit, scripts estáticos, servidor real, datapacks, diagnósticos e relatório consolidado em **quatro seeds:** `14011`, `20261108`, `-904732`, `73199013`. Há relatórios de minérios, F0, ruínas e loot. Os diagnósticos usam flags JVM para ficar **inativos em servidores comuns**.

| Evidência | O que comprova | O que NÃO comprova |
| --- | --- | --- |
| `STATIC_CHECK` | Arquivos, contratos, grafos de assets e receitas | Crafting/drops experimentados por um jogador |
| `RUNTIME_OBSERVED` | Servidor real carregou chunks/entidades/loot tables ou executou comandos nativos | Acessibilidade, cenário visual, UX, Survival real |
| `MANUAL_VERIFIED` | Jogador testou e anexou prova rastreável | Qualquer caso sem evidência deve permanecer `NOT_RUN` |

**Gates pendentes:** sonho com Colar em Survival (desativado em produção); retorno com Despertador consumível, respawn e baú de recuperação em multiplayer; Fortune/Silk Touch; coerência de mobs em população real; aparência/tiling; persistência e saves antigos. **Não** habilitar `dream_entry_enabled` por sucesso de JUnit ou uma única seed.

## Relatório manual F0

`docs/F0_TASK_7_5_MANUAL_TEMPLATE.json` é um **arquivo vivo necessário para a CI**. Não remover nem marcar resultados `PASS` fictícios. Para utilizar localmente:

```powershell
Copy-Item docs/F0_TASK_7_5_MANUAL_TEMPLATE.json qa-f0-meus-testes.json
python scripts/ci/f0_homologation.py --manual-review --manual qa-f0-meus-testes.json --output qa-f0-manual-review.json
```

O avaliador requer evidências para cada caso marcado como PASS/FAIL, e ao menos dez sessões elegíveis para avaliar percentis de descoberta (considerando sessões não concluídas/censuradas).

## Manter o repositório verificável

- Nunca remover scripts ou fixtures usadas na CI somente porque não são jogáveis.
- Testes de `/setblock`, `/loot mine` e seleção de candidatos não substituem mundo natural/ação de jogador.
- Não inferir tempo, coordenadas, taxa de spawn nem recompensas não observadas.
- Não alterar ID de registro ou worldgen existente sem teste de upgrade de mundo.
- Antes de publicar em `main`: backups de save, cliente, servidor dedicado, duas contas, reload, retorno seguro e ausência de erros P0.

Referências conservadas: [QA 7.1](DREAMSDIMENSIONS_TASK_7_1.md), [Contrato de conteúdo 7.2](DREAMSDIMENSIONS_TASK_7_2.md), [Recuperação 7.0](DREAM_RECOVERY_SAFETY_TASK_7_0.md), [F0 7.5](DREAMSDIMENSIONS_TASK_7_5.md).
