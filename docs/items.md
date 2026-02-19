# Itens, Poções e Efeitos

## Sumário
- [Tabela de itens](#tabela-de-itens)
- [Detalhamento por item](#detalhamento-por-item)
- [Poções e efeitos](#poções-e-efeitos)
- [Brewing chain](#brewing-chain)

## Tabela de itens
| ID | Tipo | Definição | Comportamento |
|---|---|---|---|
| `dreamsdimensions:ow_dream_dust` | Item | `ModItems.DREAM_DUST` | Material base da cadeia OW; drop de minérios OW. |
| `dreamsdimensions:ow_oneiric_awakener` | Item | `ModItems.ONEIRIC_AWAKENER` | Retorno do sonho ao Overworld; uso de 60 ticks; cooldown 60. |
| `dreamsdimensions:ow_oneiric_residue` | Item | `ModItems.OW_ONEIRIC_RESIDUE` | Refino intermediário. |
| `dreamsdimensions:ow_refined_oneiric_powder` | Item | `ModItems.OW_REFINED_ONEIRIC_POWDER` | Insumo central de crafting/brewing. |
| `dreamsdimensions:ow_stabilized_dream_fragment` | Item | `ModItems.OW_STABILIZED_DREAM_FRAGMENT` | Intermediário da cadeia. |
| `dreamsdimensions:ow_condensed_dream_crystal` | Item | `ModItems.OW_CONDENSED_DREAM_CRYSTAL` | Intermediário + insumo de poção Ethereal Phase. |
| `dreamsdimensions:ow_dream_binding_thread` | Item | `ModItems.OW_DREAM_BINDING_THREAD` | Insumo de catalyst e poção Anchoring. |
| `dreamsdimensions:ow_dream_catalyst` | Item | `ModItems.OW_DREAM_CATALYST` | Insumo de poção Clarity. |
| `dreamsdimensions:ow_stabilizing_essence` | Item | `ModItems.OW_STABILIZING_ESSENCE` | Gate da receita do Despertador. |

## Detalhamento por item
### `dreamsdimensions:ow_oneiric_awakener` (Despertador Onírico)
- Classe: `src/main/java/com/dreamsdimensions/mod/item/OneiricAwakenerItem.java`
- Condições:
  - Só funciona em dimensão reconhecida por `DreamsConfig.isDreamDimension`.
  - Falha se item em cooldown.
  - Bloqueia uso durante fluxo de teleporte por sono.
- Interações:
  - Retorno usa [prioridade de alvo](gameplay_loop.md#prioridade-de-ponto-de-retorno).
  - Exibe mensagem de sucesso/falha no cliente.

### `dreamsdimensions:ow_dream_dust`
- Classe: `DreamDustItem` (item simples).
- Origem principal: loot de `dreamsdimensions:ow_dream_ore` e `dreamsdimensions:ow_deepslate_dream_ore` (com Fortune/Silk Touch).
- Referência: [Loot Tables](loot_tables.md).

### Cadeia econômica OW
`ow_dream_dust` → `ow_oneiric_residue` → `ow_refined_oneiric_powder` → `ow_stabilized_dream_fragment` → `ow_condensed_dream_crystal` → `ow_dream_infused_stone` → `ow_oneiric_core_block` → `ow_oneiric_awakener`.

Ver [Receitas](recipes.md) e [Progressão](progression.md).

## Poções e efeitos
### Tabela de efeitos
| ID efeito | Classe | Efeito prático |
|---|---|---|
| `dreamsdimensions:ow_anchoring` | `AnchoringEffect` + `EffectEventHandler` | Dano de queda multiplicado por 0.10. |
| `dreamsdimensions:ow_clarity` | `ClarityEffect` + `EffectEventHandler` | Remove debuffs ao aplicar e bloqueia novos efeitos negativos. |
| `dreamsdimensions:ow_ethereal_phase` | `EtherealPhaseEffect` + `EffectEventHandler` | Knockback x0.15 e colisão de equipe `NEVER`. |
| `dreamsdimensions:ow_early_awakening` | `EarlyAwakeningEffect` + `EffectEventHandler` | Cancela morte em sonho e força retorno com 25% HP. |

### Tabela de poções
| ID poção | Duração | Registro |
|---|---:|---|
| `dreamsdimensions:ow_oneiric_base` | sem efeito | `ModPotions` |
| `dreamsdimensions:ow_potion_of_anchoring` | 2400 ticks | `ModPotions` |
| `dreamsdimensions:ow_potion_of_clarity` | 1800 ticks | `ModPotions` |
| `dreamsdimensions:ow_potion_of_ethereal_phase` | 1200 ticks | `ModPotions` |
| `dreamsdimensions:ow_potion_of_early_awakening` | 1200 ticks | `ModPotions` |

## Brewing chain
Registrado em **código**, não em JSON: `event/BrewingRecipesHandler.java`.
- `minecraft:awkward` + `dreamsdimensions:ow_refined_oneiric_powder` → `dreamsdimensions:ow_oneiric_base`
- `ow_oneiric_base` + `dreamsdimensions:ow_dream_binding_thread` → `ow_potion_of_anchoring`
- `ow_oneiric_base` + `dreamsdimensions:ow_dream_catalyst` → `ow_potion_of_clarity`
- `ow_oneiric_base` + `dreamsdimensions:ow_condensed_dream_crystal` → `ow_potion_of_ethereal_phase`
- `ow_oneiric_base` + `minecraft:totem_of_undying` → `ow_potion_of_early_awakening`
