# Progressão (Early → Mid → Late)

## Sumário
- [Resumo da progressão](#resumo-da-progressão)
- [Mapa de dependências](#mapa-de-dependências)
- [Rotas alternativas](#rotas-alternativas)

## Resumo da progressão
### Early game
1. Minerar `ow_dream_ore` / `ow_deepslate_dream_ore` no Overworld.
2. Converter `ow_dream_dust` → `ow_oneiric_residue`.
3. Refinar em fornalha/alto-forno para obter `ow_refined_oneiric_powder`.
4. Craftar `ow_anchoring_totem` se quiser controlar quando sonhar.

### Mid game
1. Produzir `ow_stabilized_dream_fragment`.
2. Produzir `ow_condensed_dream_crystal`.
3. Produzir `ow_dream_binding_thread` e `ow_dream_catalyst`.
4. Entrar na alquimia (Base Onírica + poções utilitárias).

### Late game
1. Craftar `ow_dream_infused_stone`.
2. Craftar `ow_oneiric_core_block`.
3. Craftar `ow_stabilizing_essence` (exige recursos de Nether).
4. Craftar `ow_oneiric_awakener` para retorno manual seguro e consistente.

## Mapa de dependências
- `ow_dream_ore` / `ow_deepslate_dream_ore`
  - → `ow_dream_dust`
    - → `ow_oneiric_residue`
      - → `ow_refined_oneiric_powder`
        - → `ow_stabilized_dream_fragment`
          - → `ow_condensed_dream_crystal`
            - → `ow_stabilizing_essence`
              - → `ow_oneiric_awakener`
        - → `ow_dream_binding_thread`
          - → `ow_dream_catalyst`
        - → `ow_dream_infused_stone`
          - → `ow_oneiric_core_block`
            - → `ow_oneiric_awakener`
        - → `ow_anchoring_totem`

Dependências vanilla importantes:
- `clock` para `ow_oneiric_core_block`
- `ghast_tear` para `ow_stabilizing_essence`
- `blaze_powder` para `ow_dream_catalyst`
- `totem_of_undying` para Poção de Despertar Prematuro

## Rotas alternativas
- **Refino rápido vs econômico:** blasting (100 ticks) é mais rápido que smelting (200 ticks).
- **Controle de risco:** pode priorizar `ow_anchoring_totem` cedo para evitar teleporte involuntário.
- **Foco em poções:** se ainda não tiver todo o caminho do Despertador, já dá para usar base onírica + poções utilitárias (exceto as que dependem de insumos avançados).
