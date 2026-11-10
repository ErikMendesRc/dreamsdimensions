# TASK 5.5 — Sobrevivência e Respawn Onírico

**Data:** 2026-11-10 · **Branch:** `migration/minecraft-26.3` · **HEAD inicial:** `4d6f7f56ef97f34c04d45ef731cb3ef7413b1d9b` · **Stack:** Minecraft 26.3, NeoForge 26.3.0.101, Java 25, ModDevGradle 2.0.148, GeckoLib 5.5.7, Curios 17.0.1+26.3.

**Finalidade:** uma morte em uma dimensão do catálogo onírico deve renascer na mesma dimensão, em piso seguro, preservando penalidades vanilla e sem tornar a morte uma saída gratuita. Overworld, Nether, End, outras dimensões e Hardcore mantêm comportamento vanilla. Não introduz loot, itens ou missões.

## 1. Escolha explícita da regra de cama

Os dois `dimension_type` (Dreamscape e Campo Onírico Azul) agora declaram:

```json
"minecraft:gameplay/bed_rule": {
  "can_sleep": "never",
  "can_set_spawn": "never",
  "destroy_on_use": false,
  "destroy_on_leave": false,
  "error_message": {"translate": "block.minecraft.bed.no_sleep"}
}
```

**Motivo:** em Minecraft 26.3, a cama possui um único `RespawnConfig` vanilla por jogador. Permitir `can_set_spawn=always` no sonho pode substituir a cama do Overworld e mudar o fallback usado pelo Despertador. Essa opção dependeria de outro sistema para manter dois pontos de respawn simultâneos, além do escopo. A âncora persistente implementa renascimento onírico sem disputar a cama vanilla. A configuração `destroy_on_use` é o nome **correto na 26.3** (substituiu `explodes`); o valor false garante que não exploda na interação. A cama não acelera a noite, nem ativa teleporte de sono no Overworld.

**Implicação de gameplay:** não existe checkpoint avançado por cama dentro do sonho nesta versão; o jogador renasce próximo à chegada inicial e precisa recuperar seus drops seguindo regras normais. O retorno ao Overworld continua exclusivamente pelo Despertador.

Fontes:
- Migração oficial 26.2 → 26.3: https://docs.neoforged.net/primer/docs/26.3/
- Patch NeoForge 26.3 de `ServerPlayer.startSleepInBed`: https://github.com/neoforged/NeoForge/blob/26.3.x/patches/net/minecraft/server/level/ServerPlayer.java.patch
- Change log da versão 26.3 sobre `destroy_on_use`: https://feedback.minecraft.net/hc/en-us/articles/48913133328013-Minecraft-Java-Edition-26-3

## 2. O ciclo de morte e a API oficial

`PlayerRespawnPositionEvent` é o evento **anterior à criação do jogador novo**, disparado por `PlayerList.respawn` em servidor dedicado/integrado; oferece `getTeleportTransition()/setTeleportTransition(...)` e `isFromEndFight()`. O patch oficial confirma que `event.getTeleportTransition()` é aplicado **antes** de `new ServerPlayer(...)`. Não usar `PlayerRespawnEvent` para fazer uma segunda viagem, pois ele ocorre depois da criação do clone.

- Se origem do jogador antigo não consta em `DreamDimensionCatalog`, **não interferir**: Overworld, Nether, End e dimensões de outros mods continuam vanilla.
- Se `isFromEndFight()` é true ou o mundo está em Hardcore, **não interferir**.
- Se o jogador morreu dentro de dimensão cadastrada, resolver destino seguro **dentro da MESMA dimensão**, mesmo que `dream_dimensions` tenha sido desabilitado após a expedição. `DreamsConfig` controla *entrada*, não respawn.
- O evento é atualizado uma vez com `new TeleportTransition(dreamLevel, safeVec, Vec3.ZERO, yaw, pitch, Set.of(), TeleportTransition.DO_NOTHING)`. Não existem `/tp`, portais, respawn via `PlayerRespawnEvent` ou teleporte corretivo posterior.
- `event.copyOriginalSpawnPosition()` preserva a semântica vanilla de copiar a cama pessoal do Overworld quando válida. Não forçar cópia se vanilla a considerou quebrada.
- `PlayerEvent.PlayerRespawnEvent` é utilizado **só como observabilidade** após o respawn efetivo.

**Fontes oficiais 26.3:**
- https://github.com/neoforged/NeoForge/blob/26.3.x/src/main/java/net/neoforged/neoforge/event/entity/player/PlayerRespawnPositionEvent.java
- https://github.com/neoforged/NeoForge/blob/26.3.x/patches/net/minecraft/server/players/PlayerList.java.patch

## 3. Persistência da âncora de expedição

**Attachment:** `dreamsdimensions:dream_expedition_anchor`, definido em `ModAttachments` com `AttachmentType.builder(...).serialize(DreamExpeditionAnchorData.CODEC).copyOnDeath()`. Cada jogador tem seu próprio attachment; **não existe mapa global UUID → BlockPos**.

**Campos:** `dimension` (ResourceKey), `pos` (BlockPos de pé do jogador) e `yaw` (float). Todos opcionais no `MapCodec`, para tolerar mundos anteriores sem attachment. Nenhuma entidade, servidor, chunk, inventário ou cópia de `ServerPlayer` é persistida. `player.setData` grava uma instância imutável, evitando manipular dados acoplados ao nível. `player.removeData` invalida ao encerrar a expedição.

**Quando registra:** `DreamTeleportService.enterDream` usa o mesmo `DreamSpawnHelper` e confirma o `ServerPlayer.teleport` — retorno não nulo, UUID original, dimensão correta. Após o commit existente de `dream_return`, `DreamRespawnService.recordExpedition` revalida piso/colisão e registra **uma única âncora** naquela chegada. O fluxo de `/tp` administrativo não passa por esse serviço e não fabrica âncora.

**Cópia na morte:** o próprio NeoForge realiza o `copyOnDeath` durante `ServerPlayer.restoreFrom`/`PlayerEvent.Clone`; não há cópia manual em `Clone`. Isso evita duplicidade de dados. Um novo save não depende de migração; um save velho não terá o attachment e cairá na busca de piso segura.

**Logout/restart:** attachment serializado acompanha jogador; nenhuma posição de jogador é mantida em cache. Ao relogar no sonho a âncora continua disponível. Ao entrar no Overworld ou outra dimensão sem uma expedição legítima compatível, `PlayerTickEvent.Post` remove o attachment antigo no **tick seguinte**, não no callback síncrono de `teleport`.

**Despertador:** em `OneiricAwakenerItem`, após confirmá-lo e consumir uma unidade em Survival/Adventure (Creative sem consumo), `DreamRespawnService.completeExpedition` apaga a âncora. O `dream_return` original, com campos `dimension/pos/yaw`, nunca é apagado nem convertido. O próprio retorno não depende da âncora.

## 4. Segurança física e fallback

`DreamSpawnHelper.isSafeStandingSpace` agora é método público de validação compartilhada; verifica:
- Limites Y e borda do mundo.
- Piso sólido com face superior firme, sem fluido nem blocos de dano conhecidos (magma, cactus, fogueiras, dripstone, berry bush, wither rose).
- Dois blocos de ar/fluidos vazios e volume da pose em pé com `level.noCollision(player, player.getDimensions(Pose.STANDING).makeBoundingBox(...))`.
- Busca natural usa `MOTION_BLOCKING_NO_LEAVES` **somente para propor o Y**; exige validação física posterior.

Ordem de `DreamRespawnService.resolve` (com buscas locais limitadas):
1. **EXPEDITION_ANCHOR:** ponto salvo da mesma dimensão, relido do disco/chunk e validado.
2. **NEAR_ANCHOR:** piso natural até 8 blocos do ponto anterior se o piso original foi removido ou obstruído.
3. **NEAR_DEATH:** piso natural até 8 blocos da localização da morte, inclusive para quem entrou por comando sem âncora.
4. **WORLD_SPAWN:** piso seguro próximo ao spawn global da própria dimensão, no raio do `DreamDimensionDefinition` (12 ou 24).
5. **CONTROLLED_PLATFORM:** somente se todas as buscas anteriores falharem. Reutiliza `DreamSpawnHelper.findOrCreateSafeArrival` (plataforma 5×5 **air-only**, até 25 offsets xz, limite de subida 32, borda e altura validadas) perto do spawn global; **nunca** cria plataforma no ponto de morte/vazio. Plataforma intacta é encontrada na busca natural das próximas mortes.
6. **TECHNICAL_FAILURE:** se não existe nenhuma posição segura ou a dimensão deixou de ser resolvível, log **ERROR** com contexto; manter evento vanilla original como recuperação técnica extraordinária, não uma mecânica voluntária.

**Desempenho:** não existe loop de criação por tick. Os scans são limitados em raio e feitos somente no respawn; o helper pode carregar/gerar uma quantidade limitada de chunks próximos às regiões testadas e só cria a plataforma se nenhuma superfície natural funcionar. Sem prometer uma métrica de TPS que não foi medida em um servidor real.

**Limitações físicas:** chuva de mobs, alterações imediatamente posteriores à escolha, outros mods que modificam a transição e colisões dinâmicas não podem ser totalmente impedidas pela política local. O destino é validado antes da transição com o corpo do jogador antigo; uma validação visual/integrada de jogadores reais é pendente.

## 5. Morte, Hardcore, multiplayer e cenários administrativos

**Sem `keepInventory`:** o Minecraft mantém os drops e perda de XP no local de morte; a Task 5.5 **não** mexe em inventário, XP, Curios, `keepInventory`, `doImmediateRespawn`, tela de morte ou lógica de drops. O Despertador não é concedido, consumido ou recuperado automaticamente na morte. **Com `keepInventory`:** a preservação é 100% do vanilla.

**Hardcore:** se `getLevelData().isHardcore()`, ignora a substituição do respawn. Não há cancelamento de morte nem teleporte pós-morte. Um eventual clique de renascer em Hardcore segue a política do jogo.

**Multiplayer:** anchors de A e B são dados do próprio `ServerPlayer`. A morte de A não lê B, não altera B e não movimenta B. O listener não armazena último morto nem `Level` global. Dois respawns no mesmo tick podem executar buscas independentes. **Não executado ainda:** dois clientes reais, `keepInventory` de jogadores diferentes, modpacks Curios, colisão de jogadores no mesmo piso e TPS.

**Admin e mods externos:** entrar em Dreamscape com `/execute in ... run tp` não registra âncora legítima. Ainda assim, morte naquela dimensão usa piso seguro por perto/spawn local para evitar retorno gratuito. Mudar para Overworld por comando não provoca viagem corretiva de volta e a âncora anterior será invalidada no tick de jogador subsequente. Em sonhos não reconhecidos no catálogo, não há interceptação. Pacto de Vigília (`DreamAnchorService`) continua independente e intacto.

## 6. Testes — o que realmente comprovam

| Camada | Evidência / Status |
| --- | --- |
| UNIT TEST | `DreamRespawnPolicyTest` exercita elegibilidade, Hardcore, fronteiras de dimensão e preferência de fallback. Um teste JUnit do codec foi retirado porque o source set de testes Java não inclui as bibliotecas Minecraft/DFU. O round-trip real do MapCodec depende de GameTest/runtime Minecraft e permanece **PENDENTE**. **JUnit da política: PASS nas quatro seeds.** |
| DATA VALIDATION | `scripts/ci/check_dream_respawn.py` parseia `dimension_type`, protege contrato de cama, integração do attachment/respawn e não interferência no Despertador. **PASS** — quatro seeds, check_dream_respawn.py |
| BUILD | Workflow 4 seeds com JDK 25/Gradle `clean build`. **PASS** — quatro seeds, Gradle clean build Java 25 |
| SERVER SMOKE | Workflow existente inicia `runServer`, carrega datapacks/dimensões e usa RCON, 4 seeds. **PASS** — quatro seeds, dedicated server datapacks/RCON |
| GAMETEST | **NÃO EXECUTADO**: sem fixture 26.3 de morte/recriação de jogador de rede nessa task |
| MANUAL SURVIVAL | **NÃO EXECUTADO**: morte real em ilha, void, velho save, cama e Despertador consumível |
| MULTIPLAYER | **NÃO EXECUTADO**: dois clientes dedicados e respawns simultâneos |

Build ou server smoke sozinho **não prova** redirecionamento de morte; não declarar homologado sem essas verificações. `copyOnDeath()` foi confirmado como mecanismo oficial de cópia, mas persistência real em login/restart exige jogo real.

## 7. Comandos prontos para laboratório

Atualizar branch sem tocar na `main`:

```powershell
git checkout migration/minecraft-26.3
git pull origin migration/minecraft-26.3
```

**Mundo descartável**; para duas contas, substituir `NomeA` e `NomeB` pelos nomes exatos reais.

```mcfunction
/seed
/gamemode creative
/give @s dreamsdimensions:dream_necklace 1
/give @s dreamsdimensions:oneiric_awakener 2
/gamemode survival
```

Na configuração `dreamsdimensions-synced.toml` do mundo de testes, habilitar temporariamente:
```toml
dream_entry_enabled = true
dream_entry_chance = 1.0
```
Equipar Colar no slot `necklace` funcional do Curios; dormir no **Overworld** à noite e entrar legitimamente. Reverter `dream_entry_enabled=false` depois dos testes.

```mcfunction
/time set night
/clear @s dreamsdimensions:oneiric_awakener 0
/data get entity @s Pos
```

**Teleporte administrativo para testar fallback SEM âncora:** substitua `X Y Z` por coordenadas de piso seguras observadas na seed real; não são coordenadas inventadas:

```mcfunction
/execute in dreamsdimensions:dreamscape run tp @s X Y Z
/execute in dreamsdimensions:campo_onirico_azul run tp @s X Y Z
/execute in minecraft:overworld run tp @s X Y Z
```

**Teste de morte:** depois de entrar na dimensão, executar:

```mcfunction
/gamerule keepInventory false
/kill @s
```

Confirmar tela de morte vanilla, renascimento na **mesma** dimensão, posição com piso, perda de inventário/XP e drops no local da morte. Para testar inventário preservado:

```mcfunction
/gamerule keepInventory true
/kill @s
```

Confirmar renascimento local, inventário preservado. Testar **ambas** dimensões, com e sem âncora; testar âncora com piso quebrado/obstruído em save descartável, morte no vazio, desconectar e reiniciar antes de morrer.

**Para comprovar retorno e limpeza:** usar Despertador por 60 ticks, observar consumo de uma unidade (Survival) e depois morrer no Overworld, verificando respawn vanilla na cama original:

```mcfunction
/clear @s dreamsdimensions:oneiric_awakener 0
/kill @s
```

**Multiplayer (operador):**
```mcfunction
/give NomeA dreamsdimensions:oneiric_awakener 1
/give NomeB dreamsdimensions:oneiric_awakener 1
/execute as NomeA run data get entity @s Pos
/execute as NomeB run data get entity @s Pos
/kill NomeA
/kill NomeB
```

**Nota:** `/kill` só demonstra comportamento de morte normal em servidor sem Hardcore; não aciona política de Hardcore via script. Em Hardcore criar mundo descartável isolado e observar fluxo vanilla. `/clear ... 0` consulta a contagem na versão instalada (confirmar resposta da HUD).

**Coordenadas:** nenhuma nova coordenada de chegada foi medida nesta task. A seed `20261108` já teve pontes auditadas, mas tais coordenadas não garantem piso seguro para respawn. Não confundir `/tp` com entrada legítima.

## 8. Checklist manual e problemas conhecidos

- A: entrada com Colar → Dreamscape → morte → spawn mesma dimensão, sem itens extras.
- B: entrada com Colar → Campo Azul → morte → spawn mesma dimensão.
- C: Despertador → Overworld → morte → cama original no Overworld.
- D: entrar por comando sem âncora → morte → busca local segura no sonho.
- E: destruir/obstruir o piso da âncora → morte → fallback sem sufocamento.
- F: morrer no vazio do Dreamscape → spawn no piso, nunca no vazio.
- G: logout/restart com âncora → morte → mesmo mundo e âncora validada.
- H: dois jogadores (A no Dreamscape; B no Campo Azul) morrem na mesma janela; nenhum interfere no outro.
- I: Hardcore com morte natural e sem respawn artificial.
- J: cama nos sonhos não dorme, não explode e não marca `RespawnConfig`; voltar Overworld mantém cama anterior.
- K: `keepInventory` true/false preserva comportamento vanilla.
- L: alterar `dream_dimensions` **durante** viagem e repetir morte; catálogo prevalece para respawn.

**Riscos residuais:** (1) fallback técnico de dimensão irrecuperável permite destino vanilla por falta de local físico, sempre com log ERROR; (2) a validação de colisão considera estado no instante da resolução, não mundo alterado depois; (3) respawn em regiões descarregadas pode carregar/gerar um número limitado de chunks; (4) o sistema de rare loot 5.7 ainda não existe; jogadores sem Despertador ficam dependentes de testes/admin até 5.7.

## 9. Arquivos e regressões

- Novo: `DreamExpeditionAnchorData.java` — codec com campos opcionais.
- Novo: `DreamRespawnPolicy.java` — decisões puras para JUnit.
- Novo: `DreamRespawnService.java` — entrada legítima/anchor, override pre-clone, fallback, cleanup após retorno.
- Alterado: `ModAttachments.java` — attachment persistente com `copyOnDeath()`, sem mexer em `dream_return`.
- Alterado: `DreamTeleportService.java` — registra âncora após teleporte confirmado e UUID consistente.
- Alterado: `DreamSpawnHelper.java` — expõe verificações de piso existentes e amplia a lista de pisos perigosos.
- Alterado: `OneiricAwakenerItem.java` — limpa âncora após o retorno confirmado, sem mudar consumo/cooldown.
- Alterado: `DreamsDimensions.java` — listeners únicos em NeoForge bus.
- Alterados: dois `dimension_type` — bloqueio de cama e explosão.
- Novo: `DreamRespawnPolicyTest.java`, `check_dream_respawn.py` (validação estática da estrutura opcional de codec).
- Atualizados: CI `.github/workflows/build.yml`, `GAMEPLAY_ROADMAP.md` e `TRAVEL_RETURN_TEST_MATRIX_TASK_5_2.md`.

**Não alterados:** ID/codec do `dream_return`, inventário/Curios, receitas, frequência/algoritmo de spawn de mobs, mundo/biomas, geometria, Despertador 60 ticks, cooldown 60 ticks, Colar e chance, economia, densidade, spawn do Vigia.

## 10. Conclusão condicionada

Tarefa de código **aprovada na CI**: JUnit + Gradle build + checks + dedicated server smoke nas quatro seeds do workflow 42905228119. **Homologação real de Survival/multiplayer depende de testes dentro do cliente**, e o ciclo completo só pode ser habilitado em produção após a Task **5.7** (estruturas raras com Despertadores recuperáveis). Task 5.6 de economia de Dream Dust também continua pendente de balanceamento, sem impedir testes internos.

**Diagnóstico da primeira CI:** o commit inicial `334efcd17beb288acb8080dec34fd38b1e67adcf` compilou o código principal do Minecraft/NeoForge, mas `compileTestJava` falhou porque `DreamExpeditionAnchorDataTest` importava `net.minecraft.*` e `com.mojang.serialization.*`, ausentes do classpath dos testes Java convencionais do projeto. A correção mantém os testes de políticas **puros** e usa validação estática do formato `MapCodec` + smoke do servidor; testes reais de serialização/respawn ficam pendentes de fixture de runtime Minecraft. **Correções e evidências finais:**
- Implementação: [`334efcd17beb288acb8080dec34fd38b1e67adcf`](https://github.com/ErikMendesRc/dreamsdimensions/commit/334efcd17beb288acb8080dec34fd38b1e67adcf).
- Correção de source set de testes Java: [`ef7dea6129b1744d3c74d274e88cb943db1108b5`](https://github.com/ErikMendesRc/dreamsdimensions/commit/ef7dea6129b1744d3c74d274e88cb943db1108b5). O teste que importava bibliotecas Minecraft/DFU foi retirado; os JUnit das políticas puras foram preservados.
- Correção de falso positivo da verificação `setCopyOriginalSpawnPosition(true)` em comentário: [`4c374dfd04fac204493d213dd0818003499a27a4`](https://github.com/ErikMendesRc/dreamsdimensions/commit/4c374dfd04fac204493d213dd0818003499a27a4).
- **[Workflow de CI 42905228119](https://github.com/ErikMendesRc/dreamsdimensions/actions/runs/42905228119) — 4/4 `success`:** seeds 14011, 20261108, -904732, 73199013. Em cada job: Java 25, Gradle `clean build` com JUnit, scripts CI incluindo `check_dream_respawn.py`, assets preflight e dedicated server smoke com datapacks/RCON.
- **Não executados:** GameTest de morte real e MapCodec em runtime, dois clientes multiplayer, Survival, Hardcore prático, validação de cama no cliente e persistência após restart. Não converter o resultado do workflow em aprovação desses casos.
