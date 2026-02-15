# Sprint 3.4 — Balanceamento Final do Sistema de Poções

## Fase A — Auditoria e mapeamento

### A1) Arquivos mapeados

**Registro/definição de poções e efeitos**
- `src/main/java/com/dreamsdimensions/mod/registry/ModPotions.java`
- `src/main/java/com/dreamsdimensions/mod/registry/ModEffects.java`
- `src/main/java/com/dreamsdimensions/mod/effect/AnchoringEffect.java`
- `src/main/java/com/dreamsdimensions/mod/effect/ClarityEffect.java`
- `src/main/java/com/dreamsdimensions/mod/effect/EtherealPhaseEffect.java`
- `src/main/java/com/dreamsdimensions/mod/effect/EarlyAwakeningEffect.java`
- `src/main/java/com/dreamsdimensions/mod/event/EffectEventHandler.java`

**Brewing + ingrediente base (`ow_oneiric_base`)**
- `src/main/java/com/dreamsdimensions/mod/event/BrewingRecipesHandler.java`
- `src/main/resources/data/dreamsdimensions/recipe/ow_dream_catalyst.json`
- `src/main/resources/data/dreamsdimensions/recipe/ow_stabilizing_essence.json`

**Aba criativa**
- `src/main/java/com/dreamsdimensions/mod/event/CreativeTabEvents.java`
- `src/main/java/com/dreamsdimensions/mod/registry/ModCreativeTabs.java`

**Handlers de retorno (Awakener / Early Awakening)**
- `src/main/java/com/dreamsdimensions/mod/item/OneiricAwakenerItem.java`
- `src/main/java/com/dreamsdimensions/mod/util/DreamReturnHelper.java`
- `src/main/java/com/dreamsdimensions/mod/event/DreamReturnAttachmentHandler.java`
- `src/main/java/com/dreamsdimensions/mod/event/SleepTeleportHandler.java`
- `src/main/java/com/dreamsdimensions/mod/attachment/DreamReturnData.java`

### A2) Verificação de API no SDK local

Referências verificadas nas pastas locais `ContextoIA/neoforge-21.11.37-beta` e `ContextoIA/loader-10.0.36`:
- `net.minecraft.world.item.alchemy.PotionBrewing$Builder#addMix(...)`
- `net.minecraft.world.item.ItemCooldowns#isOnCooldown(ItemStack)` e `#addCooldown(ItemStack, int)`
- `net.minecraft.server.level.ServerPlayer#teleport(TeleportTransition)`
- `net.minecraft.server.level.ServerPlayer#findRespawnPositionAndUseSpawnBlock(...)`
- `net.minecraft.world.level.portal.TeleportTransition` (construtores e `PLACE_PORTAL_TICKET`)
- `net.minecraft.server.level.ServerLevel`
- `net.minecraft.world.item.component.UseCooldown`

### A3) Pontos de ajuste definidos

- **Duração/amplifier:** `ModPotions.java`
- **Custo de brewing/crafting OW:** `BrewingRecipesHandler.java` + receitas JSON de insumos (`ow_dream_catalyst.json`, `ow_stabilizing_essence.json`)
- **Cooldown e edge cases (Early Awakening / retorno):** `OneiricAwakenerItem.java` e `EffectEventHandler.java`

---

## Fase B — Balanceamento aplicado

### Tabela final (poção -> duração -> amplifier -> custo)

| Poção | Duração | Amplifier | Custo de brewing |
|---|---:|---:|---|
| `ow_oneiric_base` | — | — | `awkward` + `ow_refined_oneiric_powder` |
| `ow_potion_of_anchoring` | 2400 ticks (2:00) | 0 | `ow_oneiric_base` + `ow_dream_binding_thread` |
| `ow_potion_of_clarity` | 1800 ticks (1:30) | 0 | `ow_oneiric_base` + `ow_dream_catalyst` |
| `ow_potion_of_ethereal_phase` | 1200 ticks (1:00) | 0 | `ow_oneiric_base` + `ow_condensed_dream_crystal` |
| `ow_potion_of_early_awakening` | 1200 ticks (1:00) | 0 | `ow_oneiric_base` + `minecraft:totem_of_undying` |

### Economia OW (insumos auxiliares)
- `ow_dream_catalyst`: agora exige `blaze_powder + ow_refined_oneiric_powder + ow_dream_binding_thread`.
- `ow_stabilizing_essence`: agora exige `ghast_tear + ow_stabilized_dream_fragment + ow_condensed_dream_crystal`.

---

## Fase C — Hardening aplicado

- **Early Awakening:** cancelamento da morte e consumo do efeito só ocorrem quando existe `TeleportTransition` válido de retorno.
- **Cooldown stacking:** aplicação de cooldown do `OneiricAwakener` protegida com `isOnCooldown` para não duplicar em retry/lag.
- **Multiplayer/server-side:** fluxo crítico mantido no `ServerPlayer` (sem mudança para client logic).
- **Uso simultâneo:** estruturas concorrentes por `UUID` preservadas (`ConcurrentHashMap` / `newKeySet`).

---

## Fase E — Release note

Sprint 3 marcada como estável: **v0.3.0 — Potion System Stable**.
