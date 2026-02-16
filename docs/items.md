# Itens

## Sumário
- [Tabela completa de itens](#tabela-completa-de-itens)
- [Poções e efeitos](#poções-e-efeitos)
- [Detalhamento por item](#detalhamento-por-item)

## Tabela completa de itens
| Nome (PT-BR) | ID | Tipo | Stack | Durabilidade | Raridade | Onde obtém | Para que serve | Observações |
|---|---|---|---:|---|---|---|---|---|
| Pó dos Sonhos | `dreamsdimensions:ow_dream_dust` | Material | 64 | — | Comum | Drop de `ow_dream_ore` e `ow_deepslate_dream_ore` (sem Silk Touch) | Matéria-prima base do sistema onírico | Drop escala com Fortune |
| Despertador Onírico | `dreamsdimensions:ow_oneiric_awakener` | Item-chave utilizável | 1 | — | Rara | Crafting | Retorno manual do sonho para Overworld | Uso de 60 ticks + cooldown 60 ticks |
| Resíduo Onírico | `dreamsdimensions:ow_oneiric_residue` | Material | 64 | — | Comum | Crafting shapeless (2x Pó dos Sonhos) | Intermediário para refino | Pode ir à fornalha/alto-forno |
| Pó Onírico Refinado | `dreamsdimensions:ow_refined_oneiric_powder` | Material | 64 | — | Comum | Smelting ou Blasting de Resíduo Onírico | Base de quase todos os crafts avançados | Também base da alquimia |
| Fragmento Onírico Estabilizado | `dreamsdimensions:ow_stabilized_dream_fragment` | Material | 64 | — | Comum | Crafting (2x2 de pó refinado) | Componente para cristal e essência | Etapa de mid game |
| Cristal Onírico Condensado | `dreamsdimensions:ow_condensed_dream_crystal` | Material | 64 | — | Comum | Crafting | Componente de poções/itens centrais | Exige vidro + fragmentos |
| Fio de Ligação dos Sonhos | `dreamsdimensions:ow_dream_binding_thread` | Material | 64 | — | Comum | Crafting shapeless | Ingrediente de poção de ancoragem e catalyst | Usa linha + pó refinado |
| Catalisador dos Sonhos | `dreamsdimensions:ow_dream_catalyst` | Material | 64 | — | Comum | Crafting shapeless | Ingrediente da Poção de Clareza | Exige blaze powder |
| Essência Estabilizadora | `dreamsdimensions:ow_stabilizing_essence` | Material | 64 | — | Comum | Crafting shapeless | Ingrediente-chave do Despertador | Exige ghast tear |

## Poções e efeitos
### Poções registradas
- `dreamsdimensions:ow_oneiric_base`
- `dreamsdimensions:ow_potion_of_anchoring`
- `dreamsdimensions:ow_potion_of_clarity`
- `dreamsdimensions:ow_potion_of_ethereal_phase`
- `dreamsdimensions:ow_potion_of_early_awakening`

### Cadeia de brewing
- Awkward + `ow_refined_oneiric_powder` → **Base Onírica**
- Base Onírica + `ow_dream_binding_thread` → **Poção de Ancoragem Onírica**
- Base Onírica + `ow_dream_catalyst` → **Poção de Clareza Onírica**
- Base Onírica + `ow_condensed_dream_crystal` → **Poção de Fase Etérea**
- Base Onírica + `minecraft:totem_of_undying` → **Poção de Despertar Prematuro**

### Efeitos customizados
- `ow_anchoring`: reduz multiplicador de dano de queda para 10%.
- `ow_clarity`: remove debuffs específicos ao aplicar e bloqueia novos efeitos negativos.
- `ow_ethereal_phase`: reduz knockback recebido e desativa colisão via time no scoreboard.
- `ow_early_awakening`: permite “acordar” ao invés de morrer no sonho (morte cancelada + teleporte).

## Detalhamento por item
### Pó dos Sonhos (`ow_dream_dust`)
- **Como conseguir:** minerando minérios oníricos no Overworld sem Silk Touch.
- **Como usar:** converter em Resíduo Onírico.
- **Interações:** Fortune aumenta saída; explosões aplicam decay.
- **Receitas relacionadas:** `ow_oneiric_residue`.
- **Loot relacionado:** `loot_tables/blocks/ow_dream_ore.json`, `ow_deepslate_dream_ore.json`.

### Despertador Onírico (`ow_oneiric_awakener`)
- **Como conseguir:** receita shaped `ow/ow_oneiric_awakener.json`.
- **Como usar:** segure uso por 60 ticks em dimensão de sonho.
- **Interações:** bloqueado fora de sonho e durante fluxo de sleep teleport; cooldown de 60 ticks.
- **Receitas relacionadas:** `ow/ow_oneiric_awakener.json`.
- **Loot relacionado:** não dropa de loot table; apenas crafting.

### Resíduo, Pó Refinado, Fragmento, Cristal, Fio, Catalisador, Essência
- **Como conseguir:** cadeia de receitas em `data/dreamsdimensions/recipe/`.
- **Como usar:** materiais intermediários para bloco núcleo, totem, poções e Despertador.
- **Interações:** Pó Refinado é o gargalo principal da progressão.
- **Receitas relacionadas:** `ow_oneiric_residue`, `ow_refined_oneiric_powder_*`, `ow_stabilized_dream_fragment`, `ow_condensed_dream_crystal`, `ow_dream_binding_thread`, `ow_dream_catalyst`, `ow_stabilizing_essence`.
- **Loot relacionado:** somente via crafting/refino.
