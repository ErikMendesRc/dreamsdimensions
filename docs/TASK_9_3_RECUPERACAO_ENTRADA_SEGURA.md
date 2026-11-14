# Task 9.3 — Recuperação e Entrada Segura

**Referência:** DreamsDimensions Game Design v2 (13/11/2026), Fatia 2 — Rede de segurança e abertura da entrada.
**Target:** Minecraft 26.3 / NeoForge 26.3 / Java 25. Branch `migration/minecraft-26.3`. Não fazer merge na main.

## Conteúdo implementado

- **Despertador Rachado (`cracked_awakener`)**: arte original pixel art 16×16 em cobre envelhecido, relógio danificado e centro azul dessaturado; não tem receita. Reutiliza o serviço de retorno seguro existente, com duração de 200 ticks (10s). O sistema só consome o item se o jogador teleporta, retorna vivo e para o Overworld, preservando identificação/stack/cooldown, sem criar uma via de teleporte paralela.
- **Ressaca Onírica (`ressaca_onirica`)**: efeito negativo visual com ícone 18×18, 6000 ticks de Fome e Fraqueza. O bloqueio do Colar dura **três noites distintas de sono concluído**, com attachment serializado e `copyOnDeath`; repetir a entrada na mesma noite não acelera a recuperação.
- **Ruínas de Recuperação**: em cada baú, um pool separado oferece 1 Despertador Rachado garantido; o pool original de 25% de Despertador íntegro é preservado, assim como os recursos de suporte. Estruturas, NBT e biomas não mudam.
- **Entrada por sono**: chance padrão de 100% quando qualificada (antes 30%), mas `dream_entry_enabled=false` permanece como guard de lançamento público até aprovação manual de Survival; arquivos de configuração já existentes devem ser revisados para atualizar chances persistidas.
- **Aviso na cama**: mensagem enviada uma vez por intervalo de sono a quem usa o Colar sem nenhum Despertador (normal ou rachado), sem barrar a entrada; não exibe aviso se o sistema estiver desativado.
- **Dormir em grupo**: cama a até 16 blocos e entrada em intervalo de 100 ticks usa a mesma dimensão e o mesmo ponto de referência de chegada, com validação individual do espaço seguro. Reserva em memória limitada à janela da expedição, limpa no shutdown.
- **Eco do Musgo**: ao usar o bloco com a mão vazia, jogador sem Despertador íntegro pode receber a direção/distância aproximada de uma ruína. A busca usa o índice oficial de estruturas, raio de no máximo 36 chunks, uma vez por ativação com cooldown. Em caso de falha, não gera teleporte e não promete uma ruína encontrada.

## Garantias técnicas

- Nenhum teleporte automático pela morte; respawn local nos sonhos permanece.
- Interrupção dos 200 ticks, troca do item, logout ou falha de destino não consomem item; cooldown/retorno pelo mesmo guard compartilhado com o Despertador original.
- Arte integrada a item model 26.3 e ícone vanilla de efeito. Localização pt-BR/en-US.
- Sem alteração na criação do Núcleo, nos minérios, nas receitas ou no mapa de estruturas.
- Não alterar `dream_entry_enabled` para true automaticamente até que o jogador possa entrar sem Despertador e voltar sem comandos, com tempo registrado.

## Testes de integração com comandos (mundo descartável)

```mcfunction
/give @s dreamsdimensions:cracked_awakener 1
/give @s dreamsdimensions:oneiric_awakener 1
/give @s dreamsdimensions:dream_necklace 1
/give @s dreamsdimensions:dream_glow_moss 16
/time set night
/locate structure dreamsdimensions:fragmented_dream_ruins
/locate structure dreamsdimensions:blue_dream_ruins
/execute in dreamsdimensions:dreamscape run tp @s 0 180 0
/execute in dreamsdimensions:campo_onirico_azul run tp @s 0 180 0
/execute in minecraft:overworld run tp @s 0 140 0
```

**Segurança:** estes pontos de TP servem apenas para inspeção em Spectator ou Creative; não são locais de spawn seguros atestados. Para testar a navegação, entre naturalmente no sonho e toque no Musgo com mão vazia e sem o Despertador íntegro.

Teste da Ressaca: `/effect give @s dreamsdimensions:ressaca_onirica 300 0 true` apenas mostra o ícone; **não** concede o bloqueio de três noites (o servidor só o cria com retorno confirmado do Rachado).

## Checklist de homologação obrigatória

1. Inspecionar em Survival, sem cheats, as duas ruínas geradas naturalmente, verificar 1 Rachado garantido por baú e a taxa adicional de íntegro aproximadamente 25% ao longo de amostras.
2. Sem Despertador, dormir com Colar e retornar ao Overworld localizando uma ruína e usando o Rachado: tempo, seed, coordenadas e falhas registrados. Testar ambas as dimensões.
3. Com Rachado, interromper 199 ticks, trocar de item, fazer logout, testar mãos, repetir a ativação: sem consumo nem teleporte; aos 200 ticks, exatamente um consumo após retorno confirmado.
4. Validar a Ressaca com cama/Colar: três noites distintas bloqueadas, quarta permite; persistência em relog, morte e restart.
5. Validar em servidor dedicado dois jogadores em camas próximas e distantes, entradas na mesma dimensão e pontos seguros, sem duplicações ou passagem para a dimensão errada.
6. Confirmar ruínas localizáveis em tempo razoável a partir das chegadas reais. Se não houver rota em Survival, o release guard deve permanecer desligado.
7. Revisar coordenadas de retorno em cama removida, respawn alternativo, mundo cheio, mundo antigo, e falhas de teleporte; nenhuma perda de item sem confirmação.
8. Validar contraste/legibilidade do Rachado a 16×16 e efeito a 18×18 no inventário, hotbar e HUD.

## Build

```powershell
git switch migration/minecraft-26.3
git pull origin migration/minecraft-26.3
python scripts/ci/check_dream_ruins.py
python scripts/ci/test_visual_assets.py
python scripts/ci/check_content_contract.py
.\gradlew.bat --no-daemon clean build
.\gradlew.bat runClient
```

CI verde atesta validação automatizada; NÃO substitui a homologação real de Survival. Todos os critérios de segurança devem estar PASS antes de habilitar entrada por padrão.

## APIs verificadas

- NeoForge 26.3: `ServerLevel.findNearestMapStructure(TagKey<Structure>, BlockPos, int, boolean)`.
- World clock 26.3: `Level.getOverworldClockTime()`.
- Registros/attachments seguem os mesmos padrões já adotados no repositório.
