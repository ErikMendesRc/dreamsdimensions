# TASK 4.5 — Vigia Adormecido

**Branch:** `migration/minecraft-26.3` · **Minecraft:** 26.3 · **NeoForge:** 26.3.0.101 · **Java:** 25 · **GeckoLib:** 5.5.7

## Objetivo

Integrar o **Vigia Adormecido**, criatura terrestre não agressiva, com os assets originais provenientes do Blockbench. Diferentemente do Fragmento Errante, o Vigia permanece longos períodos imóvel, desperta quando um jogador se aproxima e observa antes de caminhar por pequenos trechos da ilha.

## Assets importados sem alteração

- `art/blockbench/vigia_adormecido.bbmodel` (arquivo fonte editável)
- `src/main/resources/assets/dreamsdimensions/geckolib/models/entity/vigia_adormecido.geo.json`
- `src/main/resources/assets/dreamsdimensions/geckolib/animations/entity/vigia_adormecido.animation.json`
- `src/main/resources/assets/dreamsdimensions/textures/entity/vigia_adormecido.png` (128×128)

Nove clips usados sem renomear os bones/arquivos: `idle_dormant`, `awaken`, `idle_awake`, `observe`, `walk`, `defensive`, `return_dormant`, `inspect_ground`, `alert`.

## Mecânica inicial

- **Dormant:** espera parado, sem varredura pesada; verifica presença de jogador a cada 10 ticks.
- **Awakening (80 ticks):** toca `awaken` ao encontrar jogador a até 12 blocos.
- **Watching:** observa o jogador; sem alvo, executa `idle_awake`; esporadicamente usa `inspect_ground`.
- **Moving:** navega em solo usando `GroundPathNavigation` a velocidade moderada e animação `walk` **in-place**. Alvos permanecem até 8 blocos da âncora da ilha natal.
- **Defensive (40 ticks):** reação `defensive` ao sofrer dano, sem contra-atacar.
- **Returning (80 ticks):** volta a `idle_dormant` após pelo menos 320 ticks sem jogador próximo.
- **Alert (1.5s):** reação visual a jogador correndo muito próximo, com cooldown.

Apenas um controller GeckoLib anima bones compartilhados. `SynchedEntityData` carrega o estado e o indicador de observação; o servidor é a fonte autoritativa. NBT guarda estado, contadores e âncora territorial. Sem renderização de bone no servidor, ataques, loot, itens ou novas mecânicas.

## Reutilização do Dream Mob Framework (4.4)

- `DreamMobPopulationService`: nenhum novo listener de PlayerTick.
- `MobPopulationProfile`: periodicidade 360 ticks, 32 candidatos, cap local 2, no máximo 1 spawn por avaliação, raio de amostragem 24–64, distanciamento 30 entre indivíduos.
- `DreamMobHabitat`: perfil da espécie delega ao reconhecimento de ilha física existente e exige um terceiro bloco de espaço livre para a criatura alta.
- `DreamFlyingMoveControl`: **não utilizado**, porque o Vigia é terrestre.

Spawn natural e complementar se restringem à Dreamscape, a ilhas suspensas com piso natural, chunks disponíveis e gamerule `spawn_mobs=true`. A entrada vanilla usa categoria `creature`, peso 18, grupo de um. O Fragmento mantém peso 100 e grupos 2–3; a proporção entre espécies no spawn passivo vanilla muda intencionalmente, sem afetar suas regras de IA.

## Como testar

1. `./gradlew --no-daemon clean build` com Java 25.
2. `python3 scripts/ci/check_vigia_adormecido.py`.
3. Validar a CI nas quatro seeds `14011`, `20261108`, `-904732`, `73199013`.
4. Em jogo, entrar na Dreamscape, usar `/summon dreamsdimensions:vigia_adormecido` sobre piso estável e inspecionar visualmente os nove clipes com movimentos reais.
5. Afastar-se e retornar; observar transições sem saltos. Testar dano, jogador correndo, terreno vazio nas bordas da ilha, save/reload e dois jogadores.
6. Visitar ilhas já geradas para verificar spawn natural e reposição com `spawn_mobs=true` sem superpopulação.

Testes estáticos, build e smoke no dedicated server **não substituem** validação visual nem prova de spawn com jogador real. Registrar como pendência quando não executados.

## Referências

- https://docs.neoforged.net/primer/docs/26.3/
- https://docs.neoforged.net/docs/entities/livingentity/
- https://wiki.geckolib.com/docs/geckolib5/entities/the-entity-renderer/
- https://wiki.geckolib.com/docs/geckolib5/concepts/animation/controller/overview/
