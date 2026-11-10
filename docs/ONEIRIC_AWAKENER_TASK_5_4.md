# TASK 5.4 — Despertador Onírico consumível

**Data:** 2026-11-10 · **Stack:** Minecraft 26.3, NeoForge 26.3.0.101, Java 25, GeckoLib 5.5.7, Curios 17.0.1+26.3 · **Branch:** `migration/minecraft-26.3` · **HEAD inicial:** `0d0c9894872bd73fb04905042bdabfbc85ab9ad1`.

**Contrato normativo:** [TASK 5.2](TRAVEL_RETURN_CONTRACT_TASK_5_2.md) e [matriz](TRAVEL_RETURN_TEST_MATRIX_TASK_5_2.md). A entrada pelo Colar da 5.3 permanece INTACTA; não liberar viagem por padrão antes da 5.5 e 5.7.

## 1. Problema original e auditoria

O `OneiricAwakenerItem.finishUsingItem` chamava `ServerPlayer.teleport(transition)` e enviava a mensagem de sucesso independentemente do resultado; não alterava a quantidade do item. O Despertador podia ser utilizado indefinidamente. A origem do item/mão durante o uso não estava capturada e não havia proteção contra finalização duplicada.

Outro defeito: `Item.Properties.useCooldown(float)` recebe **segundos**, não ticks. O registro existente passava `60`, resultando em até 60 segundos de cooldown — ao contrário do contrato de 60 ticks. A implementação passa `60 / 20.0F` (3 segundos) e continua lendo `UseCooldown.ticks()` da stack real, respeitando grupos de cooldown alterados por outros mods.

`DreamReturnHelper` já implementa fallback: última cama válida no Overworld (com `BedBlock.findStandUpPosition`) → respawn pessoal válido no Overworld → spawn global com busca de piso seguro e `DismountHelper.findSafeDismountLocation`. O retorno **não precisa de cama existente**; não alteramos esse helper nem `dream_return`.

## 2. API 26.3, confirmação e semântica do uso

- `Item#use(Level, Player, InteractionHand)` começa uso vanilla, `InteractionResult.CONSUME` **não** remove unidades.
- `Item#getUseDuration` continua 60 ticks; `getUseAnimation` permanece `BOW`; `Item#finishUsingItem(ItemStack, Level, LivingEntity)` é chamado pelo ciclo vanilla de conclusão. Um `LivingEntityUseItemEvent.Stop` remove tokens transitórios em interrupção.
- `ServerPlayer#teleport(TeleportTransition)` retorna `@Nullable ServerPlayer`; NeoForge pode negar a viagem via `CommonHooks.onTravelToDimension`, retornando `null`. O patch 26.3.x altera a revivificação da entidade no fluxo cross-dimension, emite `PlayerChangedDimensionEvent` e termina com `return this` na trajetória padrão. Confirmar `returned != null`, `returned.isAlive()`, UUID original e `returned.level().dimension() == Level.OVERWORLD`; nunca inferir sucesso por chamada sem exceção.
- `DataComponents.USE_COOLDOWN` armazena `UseCooldown(seconds,...)`, com `ticks()` para `ItemCooldowns#addCooldown(stack, ticks)`. Uma cópia **apenas para consulta do cooldown** é obtida antes de `shrink`, para o grupo não desaparecer quando a última unidade é consumida.

Fontes de versão:
- https://docs.neoforged.net/primer/docs/26.3/
- https://docs.neoforged.net/docs/items/interactions/
- https://docs.neoforged.net/docs/items/
- https://github.com/neoforged/NeoForge/blob/26.3.x/patches/net/minecraft/server/level/ServerPlayer.java.patch

## 3. Alteração implementada

Fluxo em `OneiricAwakenerItem`:

```text
Item#use: validar dimensão, vivo, não-spectator, item/cooldown
  -> registrar UUID + mão + referência ItemStack (somente servidor)
  -> Player#startUsingItem (60 ticks vanilla)
  -> caso Stop/logout/morte/admin: cancelar registro transitório
  -> Item#finishUsingItem:
       revalidar server/player/sonho/item/mão/getUseItem==stack
       claim() uma única vez; bloquear reentrada
       DreamReturnHelper.buildReturnTransition()
       exigir transição com destino Overworld
       ServerPlayer.teleport(transition)
       conferir retorno, UUID, vida, dimensão
       conferir mesma mão + EXATA instância ItemStack e contagem
       copiar stack para leitura do grupo cooldown
       Survival/Adventure: ItemStack.shrink(1), atualizar mão usada
       Creative instabuild: não diminuir a quantidade
       aplicar cooldown ao grupo correto (60 ticks padrão)
       mensagem localizada + log de sucesso
       finally: liberar guard
```

**Idempotência e mão:** `OneiricReturnGuard` guarda somente UUID, enum MAIN_HAND/OFF_HAND e referência temporária `Object` do stack (sem Player/Level). `claim` exige **identidade do objeto**, não igualdade de tipo. Ao concluir, o jogador retornado precisa ainda ter esse objeto na mão de origem. Isso protege um segundo Despertador no inventário, a outra mão e trocas por mods. Não há busca global de itens. O guard impede segunda conclusão e reentrada na mesma pilha, usa `try/finally` e é limpo por Stop, logout, clone, troca administrativa de dimensão e parada de servidor. **ChangedDimension não cancela um claim em processamento**, pois NeoForge emite esse evento *dentro* de `teleport`.

**Política pura:** `OneiricReturnPolicy` agrupa `validate`, `confirm`, `consumption` e `shouldApplyCooldown`, permitindo JUnit sem instanciar o runtime do Minecraft.

## 4. Falhas e estados ambíguos

| Caso | Decisão de código |
| --- | --- |
| Fora de sonho/Overworld | `InteractionResult.PASS`; não inicia uso |
| Espectador, morte, stack vazio, item incorreto, cooldown ativo | Recusa, não consome |
| Solta antes de 60 ticks, troca item, logout, morte, troca administrativa | Vanilla encerra uso; guard remove ativação não concluída |
| Nenhum destino seguro | Mensagem de falha localizada e log `invalid_target`; sem cooldown/consumo |
| `teleport` devolve `null`/UUID diferente/jogador inválido | Log `teleport_rejected`, sem consumo |
| Dimensão final diferente do Overworld | Log `wrong_destination`, sem consumo; **não** tenta segundo teleporte |
| `teleport` lança exception | Log com stacktrace; falha sem consumo; se já mudou de dimensão, não tenta reverter cegamente |
| Mundo já alterado, mas stack foi trocado/copied | Log de erro `invalid_item_state_after_teleport`; **não** remove item desconhecido. Mensagem localizada distinta; sem consumir outro slot |
| Mesmo callback executado novamente | `claim` impede replay e `finally` libera; sem segundo consumo |
| Creative `instabuild` | Valida e retorna normalmente, consumo 0; cooldown de sucesso mantido |
| Spectator/Hardcore | Spectator bloqueado; fluxo de morte Hardcore vanilla não modificado |

**Limitação explicitada:** Minecraft não oferece transação atômica distribuída entre alteração dimensional e inventário. Se um mod substituir o `ItemStack` por uma nova instância durante um callback *síncrono* de teleporte, o jogador pode chegar ao Overworld sem ser possível consumir com segurança; o resultado é registrado como inconsistência, nunca mascarado como sucesso pago. Não adicionar heurística que remove itens não relacionados. A trajetória padrão 26.3 termina em `return this`, mantendo a mesma instância de ServerPlayer. O evento de mudança dimensional disparado no meio do teleporte está coberto pelo guard.

**Importante:** os JUnit testam políticas e idempotência, não simulam um teleporte real nem a sincronização de inventário de dois clientes. Isso é pendência de homologação manual.

## 5. Efeitos de gameplay e preservação

- **Survival/Adventure:** 60 ticks de ativação, 1 unidade consumida **somente após teleporte confirmado**, no slot de origem.
- **Creative:** via `player.getAbilities().instabuild`, item preservado.
- **Duas mãos:** possível uso em mão principal ou offhand, mas só a usada é elegível.
- **Sem Colar:** uso continua independente de Curios, de `dream_entry_enabled` e de `dream_entry_chance`.
- **Cooldown:** padrão 60 ticks após retorno bem-sucedido, sem cooldown quando a transição falha.
- **NBT/saves preservados:** `oneiric_awakener`, `dream_dust`, `dream_necklace`, `dream_return` e `dimension/pos/yaw` inalterados.
- **Intocados:** `DreamReturnHelper`, `DreamTeleportService`, worldgen, dimensões, crafting, loot, entidades, texturas, animação, slots Curios.
- **Mudanças visuais:** textos existentes de tooltip e falha PT-BR/EN-US esclarecem duração, consumo e preservação em falhas; nova mensagem localizada para inventário trocado.

## 6. Testes e evidência

| Teste | Evidência/status |
| --- | --- |
| `OneiricReturnPolicyTest` JUnit | Código implementado: validação/negações, destinos ausentes, `null`/UUID, dimension mismatch, survival/adventure/creative, cooldown |
| `OneiricReturnGuardTest` JUnit | Código implementado: duas mãos, item trocado, replay, duas identidades, interrupção, login/morte/admin cancel, evento cross-dimension |
| Gradle `clean build` + JUnit com JDK25 | **PASS** — [GitHub Actions 42895974012](https://github.com/ErikMendesRc/dreamsdimensions/actions/runs/42895974012), 4 seeds |
| CI scripts existentes | **PASS** — verificações existentes no mesmo workflow, 4 seeds |
| Dedicated server `runServer` + loading/datapacks | **PASS** — dedicado RCON, datapacks e mundo, 4 seeds |
| GameTest real de item+retorno | **NOT RUN**; sem fixture de jogador de rede para concluir 60 ticks |
| Cliente Survival / Adventure / Creative / Spectator | **NOT RUN** após commit |
| Multiplayer dedicado com dois clientes | **NOT RUN** |
| `dream_return` de save antigo + falha induzida de teleporte | **NOT RUN** |

O workflow CI executa em seeds `14011`, `20261108`, `-904732`, `73199013`. Dedicated server **sem jogador real** não prova consumo nem multiplayer; qualquer sucesso deve ser relatado com essa ressalva.

## 7. Comandos de teste in-game

Atualização:

```bash
git checkout migration/minecraft-26.3
git pull origin migration/minecraft-26.3
```

Preparar em **mundo descartável**:

```mcfunction
/seed
/gamemode creative
/give @s dreamsdimensions:oneiric_awakener 2
/clear @s dreamsdimensions:oneiric_awakener 0
/gamemode survival
```

Teleporte administrativo (**substitua X Y Z por posição segura efetivamente observada no seu mundo**):

```mcfunction
/execute in dreamsdimensions:dreamscape run tp @s X Y Z
/execute in dreamsdimensions:campo_onirico_azul run tp @s X Y Z
/execute in minecraft:overworld run tp @s X Y Z
```

Testes:
1. **A — Survival/Dreamscape:** use por pelo menos 60 ticks (3 segundos); deve voltar ao Overworld consumindo **1** Despertador. Conte com `/clear @s dreamsdimensions:oneiric_awakener 0`.
2. **B — Interromper:** solte com 1, 20, 59 ticks; sem teleporte/consumo.
3. **C — Campo Azul:** repita A com sobrevivência, independentemente de Colar.
4. **D — Overworld:** use em Overworld; sem ativação, cooldown nem consumo.
5. **E — Creative:** após entrar em sonho, `/gamemode creative` e usar; volta sem consumir.
6. **F — Dois itens:** deixe um em outro slot e outro na offhand; use cada mão separadamente e verifique que apenas o usado desapareceu.
7. **G — Multiplayer:** A e B com um Despertador cada, ativam juntos e verificam seus inventários independentemente.
8. **H — Falha:** usar fixture/ambiente de teste que negue transição; não comprometer save real.
9. **I — Logout/morte/admin:** interrompa antes de 60 ticks, reconecte; sem consumo ou teleporte atrasado.
10. **J — Entrada legítima:** viaje com Colar por sono em mundo de laboratório (config 5.3 temporariamente ligada) e teste novamente; o fallback de TP admin não é equivalente à cama.

**Para visualização dos atributos da entidade:**

```mcfunction
/data get entity @s Pos
/clear @s dreamsdimensions:oneiric_awakener 0
```

**Cuidado:** ao testar a entrada com Colar, terminar a sessão restaurando `dream_entry_enabled=false`; respawn da 5.5 e recuperação da 5.7 continuam ausentes.

## 8. Próximas dependências

**5.5:** respawn seguro e sono nas dimensões (sem saída gratuita por cama/morte); **5.7:** ruínas com Despertador recuperável em ambas as dimensões. **5.6:** economia e mineração real do Dream Dust. O release público permanece bloqueado até completar e homologar 5.5/5.7.

## 9. Registro de implementação

Arquivos previstos: `item/OneiricAwakenerItem.java`, `service/OneiricReturnPolicy.java`, `service/OneiricReturnGuard.java`, `registry/ModItems.java`, `DreamsDimensions.java`, traduções, 2 JUnit, este relatório, matriz e roadmap.

**Commit de implementação:** [ed166e9f3a88c55666d8ce24754d22926d199a6a](https://github.com/ErikMendesRc/dreamsdimensions/commit/ed166e9f3a88c55666d8ce24754d22926d199a6a) — `fix(gameplay): consume oneiric awakener after confirmed return`. **CI:** [run 42895974012](https://github.com/ErikMendesRc/dreamsdimensions/actions/runs/42895974012); os quatro jobs `build (14011)`, `build (20261108)`, `build (-904732)` e `build (73199013)` concluíram com **success**. Isso cobre JDK 25 + Gradle `clean build` (JUnit incluído), checks estáticos e dedicated server smoke. **Não é prova** de ativação de 60 ticks por jogador real nem de sincronização de inventário em multiplayer; tais testes continuam explicitamente PENDENTES.
