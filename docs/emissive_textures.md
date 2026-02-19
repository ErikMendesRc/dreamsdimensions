# Texturas Emissivas

## Sumário
- [Escopo](#escopo)
- [Como o mod implementa emissivo](#como-o-mod-implementa-emissivo)
- [light_emission vs lightLevel](#light_emission-vs-lightlevel)
- [Estrutura de assets](#estrutura-de-assets)
- [Exemplo prático](#exemplo-prático)
- [Performance e pitfalls](#performance-e-pitfalls)

## Escopo
Fonte externa `emissive-materials.txt` **não encontrada no workspace**. Esta documentação descreve apenas o que está implementado no projeto.

## Como o mod implementa emissivo
Padrões observados:
1. **Emissivo no model JSON** com `light_emission` por face/elemento.
2. **Textura emissiva dedicada** (`*_emissive.png`) nos models.
3. **Luz real de bloco** via `BlockBehaviour.Properties.lightLevel(...)` em classes Java.

Blocos com emissivo visível:
- `dreamsdimensions:ow_lumina_flower` (`models/block/ow_lumina_flower.json`)
- `dreamsdimensions:ow_somniflora` (`parent: minecraft:block/cross_emissive`)
- `dreamsdimensions:ow_somnibark_log` (textura lateral emissiva)
- `dreamsdimensions:ow_dream_glow_moss` (textura top emissiva)

## light_emission vs lightLevel
- `light_emission` (model JSON): componente visual emissivo na renderização do material.
- `lightLevel` (Java Block properties): luminosidade de bloco no engine de iluminação.

No projeto, ambos aparecem em combinação (ex.: `ow_lumina_flower` tem `lightLevel(12)` em Java + partes emissivas no model).

## Estrutura de assets
- Models: `assets/dreamsdimensions/models/block/*.json`
- Texturas base/emissivas: `assets/dreamsdimensions/textures/block/*_emissive.png`
- Animação emissiva opcional: `*_emissive.png.mcmeta` (quando presente)

## Exemplo prático
Trecho conceitual já existente em model:
```json
{
  "light_emission": 15,
  "faces": {
    "north": { "texture": "#plant_emissive" }
  }
}
```

## Performance e pitfalls
- Evite excesso de elementos emissivos por bloco para reduzir custo visual.
- Garanta coerência entre textura emissiva e `lightLevel`; brilho visual sem luz real pode confundir gameplay.
- Em flora, valide `noCollision`/`instabreak` com shape e sobrevivência para não causar flicker ou quebra inesperada.
