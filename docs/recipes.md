# Receitas

## Sumário
- [Visão geral do sistema de crafting](#visão-geral-do-sistema-de-crafting)
- [Receitas por tipo](#receitas-por-tipo)
- [Tabela Resultado → Como crafta → Pré-requisitos](#tabela-resultado--como-crafta--pré-requisitos)
- [Brewing (alquimia)](#brewing-alquimia)

## Visão geral do sistema de crafting
O mod usa receitas em `data/dreamsdimensions/recipe/` (pasta singular `recipe`):
- **crafting_shaped**
- **crafting_shapeless**
- **smelting**
- **blasting**

Não há receitas custom registradas para:
- smithing transform/trim,
- stonecutting,
- campfire,
- smoking.

## Receitas por tipo

### Crafting shaped
1. **`ow/ow_dream_infused_stone`** → `ow_dream_infused_stone` x1  
   Ingredientes: pedra + pó onírico refinado.

2. **`ow/ow_oneiric_core_block`** → `ow_oneiric_core_block` x1  
   Ingredientes: pedra infundida + cristal condensado + relógio.

3. **`ow/ow_oneiric_awakener`** → `ow_oneiric_awakener` x1  
   Ingredientes: pena + bloco núcleo + essência estabilizadora + 2 garrafas.

4. **`ow_anchoring_totem`** → `ow_anchoring_totem` x1  
   Ingredientes: shard de ametista + redstone + ferro + pó refinado.

5. **`ow_condensed_dream_crystal`** → `ow_condensed_dream_crystal` x1  
   Ingredientes: 8 fragmentos estabilizados + vidro.

6. **`ow_stabilized_dream_fragment`** → `ow_stabilized_dream_fragment` x1  
   Ingredientes: 2x2 de pó refinado.

### Crafting shapeless
1. **`ow_oneiric_residue`** → `ow_oneiric_residue` x1  
   Ingredientes: 2x `ow_dream_dust`.

2. **`ow_dream_binding_thread`** → `ow_dream_binding_thread` x1  
   Ingredientes: `minecraft:string` + `ow_refined_oneiric_powder`.

3. **`ow_dream_catalyst`** → `ow_dream_catalyst` x1  
   Ingredientes: `minecraft:blaze_powder` + `ow_refined_oneiric_powder` + `ow_dream_binding_thread`.

4. **`ow_stabilizing_essence`** → `ow_stabilizing_essence` x1  
   Ingredientes: `minecraft:ghast_tear` + `ow_stabilized_dream_fragment` + `ow_condensed_dream_crystal`.

### Smelting / Blasting
1. **`ow_refined_oneiric_powder_from_smelting`** (`smelting`)  
   `ow_oneiric_residue` → `ow_refined_oneiric_powder`, 200 ticks, 0.1 XP.

2. **`ow_refined_oneiric_powder_from_blasting`** (`blasting`)  
   `ow_oneiric_residue` → `ow_refined_oneiric_powder`, 100 ticks, 0.1 XP.

3. **`az_dream_stone_from_smelting`** (`smelting`)  
   `az_dream_cobblestone` → `az_dream_stone`, 200 ticks, 0.1 XP.

4. **`az_dream_stone_from_blasting`** (`blasting`)  
   `az_dream_cobblestone` → `az_dream_stone`, 100 ticks, 0.1 XP.

## Tabela Resultado → Como crafta → Pré-requisitos
| Resultado | Como crafta | Pré-requisitos / Onde obter ingredientes |
|---|---|---|
| `ow_oneiric_residue` | 2x `ow_dream_dust` (shapeless) | Minerar minérios oníricos no Overworld |
| `ow_refined_oneiric_powder` | Smelting/Blasting de resíduo | Ter fornalha ou alto-forno + combustível |
| `ow_stabilized_dream_fragment` | 2x2 de pó refinado | Estoque alto de pó refinado |
| `ow_condensed_dream_crystal` | 8 fragmentos + vidro | Areia + fornalha para vidro e cadeia de fragmentos |
| `ow_dream_binding_thread` | Linha + pó refinado | Aranhas para linha |
| `ow_dream_catalyst` | Blaze powder + pó refinado + fio | Acesso ao Nether para blaze |
| `ow_stabilizing_essence` | Ghast tear + fragmento + cristal | Nether + cadeia onírica mid game |
| `ow_dream_infused_stone` | Pedra + pó refinado | Farm de pedra + cadeia base |
| `ow_oneiric_core_block` | Pedra infundida + cristal + relógio | Ouro + redstone para relógio |
| `ow_oneiric_awakener` | Pena + núcleo + essência + garrafas | Acesso completo à cadeia onírica |
| `ow_anchoring_totem` | Ametista + redstone + ferro + pó refinado | Mineração Overworld |
| `az_dream_stone` | Fundir/blastar `az_dream_cobblestone` | Fonte de cobblestone azul |

## Brewing (alquimia)
As receitas de poção são registradas em código (não em JSON):
- Awkward + `ow_refined_oneiric_powder` → `ow_oneiric_base`
- Base + `ow_dream_binding_thread` → `ow_potion_of_anchoring`
- Base + `ow_dream_catalyst` → `ow_potion_of_clarity`
- Base + `ow_condensed_dream_crystal` → `ow_potion_of_ethereal_phase`
- Base + `minecraft:totem_of_undying` → `ow_potion_of_early_awakening`

Exemplo curto de receita JSON (shapeless):
```json
{
  "type": "minecraft:crafting_shapeless",
  "ingredients": ["dreamsdimensions:ow_dream_dust", "dreamsdimensions:ow_dream_dust"],
  "result": {"id": "dreamsdimensions:ow_oneiric_residue", "count": 1}
}
```
