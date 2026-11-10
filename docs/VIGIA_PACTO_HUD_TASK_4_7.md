# TASK 4.7 — Ícone nativo do Pacto de Vigília

**Minecraft 26.3 · NeoForge 26.3.0.101 · Java 25 · branch `migration/minecraft-26.3`**

## Objetivo

Após homologação in-game do Pacto de Vigília (Task 4.6), adicionar seu ícone de efeito ao HUD padrão do Minecraft **sem criar overlay de interface paralelo** e sem mudar a habilidade de resgate já testada.

## Implementação

- `ModEffects`: registro de `dreamsdimensions:pacto_de_vigilia` com `DeferredRegister<MobEffect>`, configurado na inicialização do mod.
- `PactoVigiliaEffect`: `MobEffectCategory.BENEFICIAL`, cor ciano; **apenas um marcador cosmético**, sem atributos, buffs ou tick de gameplay.
- `textures/mob_effect/pacto_de_vigilia.png`: ícone exclusivo 18×18 pixels, RGBA, com a runa de vigilância da criatura e a lembrança da ilha. Padrão nativo do atlas GUI em 26.3; sem renderer client-side adicional.
- `lang/pt_br.json` e `lang/en_us.json`: `Pacto de Vigília` / `Vigil Pact`.

`DreamAnchorService` é a **única fonte de verdade** sobre o direito a resgate. A presença do efeito ou de comandos vanilla (`/effect give`) **não cria âncora**. A concessão aplica uma única `MobEffectInstance` de 2 minutos, visível na HUD e no inventário (`showIcon=true`), mas sem partículas de poção (`visible=false`).

## Ciclo de vida e consistência

| Evento | Comportamento |
|---|---|
| Ritual concluído | Validar âncora; aplicar efeito; só então registrar vínculo |
| Ainda protegido | Ícone e timer vanilla continuam presentes |
| Resgate consumido | Remover vínculo e efeito **antes** do teleporte |
| 120 s expirados | Remover vínculo e efeito |
| Distância horizontal acima de 48 blocos | Remover vínculo e efeito |
| Troca de dimensão, morte/clone, logout | Limpar vínculo e efeito |
| Leite ou comando remove o efeito | Revogar vínculo no próximo tick; jamais resgatar invisivelmente |
| Relog/restart com efeito persistido pelo Minecraft | Sem vínculo transitório, limpar ícone órfão automaticamente |
| `/effect give` de marcador sem pacto | Pode mostrar ícone brevemente, mas não oferece resgate e é limpo no tick |

Nenhum dado persistente de vínculo foi acrescentado; a vida útil do benefício depende do serviço servidor existente. **Não** altera Texturas GeckoLib, IA, colisão, dimensionamento ou o Fragmento Errante.

## Testes

- Compilar `./gradlew --no-daemon clean build` com Java 25.
- `python3 scripts/ci/check_pacto_vigilia_effect.py`: registro, delegação e limpeza, tradução, PNG 18×18 com transparência.
- Pipeline de smoke do servidor dedicado nas quatro seeds: deve passar sem novos erros de registro de MobEffect.
- **Homologação manual obrigatória:** ativar o pacto, ver o ícone de canto superior direito, abrir inventário e conferir `Pacto de Vigília` com contador decrescente, consumir resgate, esperar expirar, usar leite, sair da região, mudar dimensão e relogar. O ícone não deve permanecer após invalidar o vínculo.

## Referências

- https://docs.neoforged.net/docs/items/mobeffects/
- https://docs.neoforged.net/docs/concepts/registries/
- https://aldak0.ru/javadoc/26.3.x/net/minecraft/world/effect/MobEffectInstance.html
- https://minecraft.wiki/w/Atlas
