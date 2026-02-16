# Blocos

## Sumário
- [Tabela completa](#tabela-completa)
- [Blocos especiais](#blocos-especiais)

## Tabela completa
| Nome | ID | Ferramenta correta (tags) | Drops | Loot table | Propriedades relevantes | Onde gera |
|---|---|---|---|---|---|---|
| Grama dos Sonhos | `ds_dream_grass` | Pá (`mineable/shovel`) | `ds_dream_dirt` | `loot_tables/blocks/ds_dream_grass.json` | Cópia de grass block | Superfície da `dreamscape` |
| Grama Onírica Azul | `az_dream_grass` | Pá | Silk Touch: ele mesmo; sem Silk: `az_dream_dirt` | `loot_tables/blocks/az_dream_grass.json` | Cópia de grass, map color azul | Superfície do `campo_onirico_azul` |
| Terra dos Sonhos | `ds_dream_dirt` | Pá | Ele mesmo | `.../ds_dream_dirt.json` | Cópia de dirt | Subsolo da dreamscape |
| Terra Onírica Azul | `az_dream_dirt` | Pá | Ela mesma | `.../az_dream_dirt.json` | Cópia de dirt azul | Subsolo do campo azul |
| Areia dos Sonhos | `ds_dream_sand` | Pá | Ela mesma | `.../ds_dream_sand.json` | Cópia de sand | Conteúdo decorativo |
| Pedra Serena | `ds_serene_stone` | (sem tag explícita) | Ela mesma | `.../ds_serene_stone.json` | Cópia de stone | Bloco base da dreamscape |
| Pedra Onírica Azul | `az_dream_stone` | Picareta | Ela mesma | `.../az_dream_stone.json` | requiresCorrectToolForDrops | Base rochosa campo azul |
| Pedregulho Onírico Azul | `az_dream_cobblestone` | Picareta | Ele mesmo | `.../az_dream_cobblestone.json` | requiresCorrectToolForDrops | Não gera natural (resultado de mineração/crafting) |
| Bloco Cintilante dos Sonhos | `ds_dream_shimmer_block` | Picareta | Ele mesmo | `.../ds_dream_shimmer_block.json` | força 1.5/6.0 | Decorativo |
| Musgo Luminoso dos Sonhos | `ds_dream_glow_moss` | Pá | Ele mesmo | `.../ds_dream_glow_moss.json` | emite luz 8 | Decorativo |
| Minério dos Sonhos | `ow_dream_ore` | Picareta + ferro (`needs_iron_tool`) | Silk: minério; normal: `ow_dream_dust` | `.../ow_dream_ore.json` | XP 1–3, estado `clicked` | Overworld via biome modifier |
| Minério dos Sonhos de Ardósia | `ow_deepslate_dream_ore` | Picareta + ferro | Silk: minério; normal: `ow_dream_dust` | `.../ow_deepslate_dream_ore.json` | XP 1–3, estado `clicked` | Overworld profundo |
| Pedra Infundida dos Sonhos | `ow_dream_infused_stone` | Picareta | Ela mesma | `.../ow_dream_infused_stone.json` | requires tool | Crafting |
| Bloco do Núcleo Onírico | `ow_oneiric_core_block` | Picareta | Ele mesmo | `.../ow_oneiric_core_block.json` | requires tool | Crafting |
| Totem de Ancoragem Onírica | `ow_anchoring_totem` | Picareta | Ele mesmo | `.../ow_anchoring_totem.json` | força 2/6 | Crafting; bloqueio de teleporte |
| Flor dos Sonhos | `ds_dream_flower` | mão/ferramenta leve | Ela mesma | `.../ds_dream_flower.json` | instabreak | Flora onírica |
| Flor Lumina do Overworld | `ow_lumina_flower` | mão/ferramenta leve | Ela mesma | `.../ow_lumina_flower.json` | instabreak, partículas GLOW | Flora |
| Somniflora do Overworld | `ow_somniflora` | mão | Ela mesma | `.../ow_somniflora.json` | FireflyBushBlock | Flora |
| Tronco dos Sonhos | `ds_dream_log` | machado (tag geral `logs`) | Ele mesmo | `.../ds_dream_log.json` | RotatedPillar | Ambiente onírico |
| Tronco Somnibark do Overworld | `ow_somnibark_log` | machado (`mineable/axe`) | Ele mesmo | `.../ow_somnibark_log.json` | RotatedPillar | Flora |
| Folhas dos Sonhos | `ds_dream_leaves` | tesoura/Silk recomendado | Só dropa com Silk Touch | `.../ds_dream_leaves.json` | noOcclusion, força 0.2 | Ambiente onírico |

## Blocos especiais
### Minérios oníricos (`ow_dream_ore`, `ow_deepslate_dream_ore`)
- Têm comportamento de minério com XP (`DropExperienceBlock`, 1–3).
- Possuem propriedade `clicked` alternável ao interagir sem item (efeito interativo/estado).
- Loot com alternativas: Silk Touch preserva bloco; sem Silk dropa Pó dos Sonhos com bônus de Fortune.

### Totem de Ancoragem Onírica (`ow_anchoring_totem`)
- Bloco funcional anti-teleporte por sono.
- Se estiver no raio configurado ao redor da cama/spawn referência, impede entrada automática no sonho.

### Flora luminosa (`ow_lumina_flower`)
- Gera partículas `minecraft:glow` no cliente durante animate tick.
