# TASK 4.2 — Comportamentos do Fragmento Errante

**Escopo:** `migration/minecraft-26.3` — Minecraft 26.3, NeoForge 26.3.0.101, Java 25, GeckoLib 5.5.7. Sem merge na main.

## Auditoria

- `FragmentoErranteEntity` era uma `PathfinderMob` sem goals, navegação aérea ou sincronização própria de movimento.
- `ModEntities` registra apenas `fragmento_errante` com hitbox **0,85 × 1,55**, eye height 1,30; nenhuma tabela de spawn natural foi adicionada.
- `FragmentoRendererRegistration` usa o `GeoEntityRenderer` da GeckoLib 5, sem shaders ou troca de assets.
- Seis animações presentes e preservadas: `idle` (3 s, loop), `float` (2 s, loop), `move` (1 s, loop), `curious` (1,5 s), `startled` (0,8 s), `disappear` (1,2 s). `idle`, `float`, `move`, `startled` e `disappear` usam o root/energia: **um controller GeckoLib**, sem sobrepor controllers nos mesmos bones.

## Implementação

| Prioridade | Goal | Política |
|---:|---|---|
| 0 | FleeGoal | Dano ou jogador correndo a menos de 3 blocos: `startled` via `triggerAnim`, fuga de 38 ticks e cooldown de 120; no máximo seis destinos examinados |
| 1 | CuriosityGoal | Jogador vivo, não espectador a até 7 blocos; observa por 36–59 ticks, `curious` one-shot; aproxima-se em 65% das oportunidades **somente** se a distância inicial exceder 4 blocos, respeitando afastamento de cerca de 3,3 blocos |
| 3 | WanderGoal | Até sete amostras por escolha, até seis blocos em X/Z e 2,5 em Y; caminhos não são refeitos por tick, prazo máximo 110 ticks, pausas de 35–109 ticks |

O `FlyingPathNavigation` fornece caminhos 3D e o `GentleFlyingMoveControl` é um servo de velocidade/curva com limite de aceleração de **0,028 bloco/tick²** e velocidade de cruzeiro aproximada de **0,13 bloco/tick** (fuga até **0,19**). Parada usa amortecimento de velocidade, sem teleportes. A física da entidade continua responsável por resolver colisões. Destinos são limitados a **10 blocos**, corredor dividido em cinco amostras com `hasChunkAt`, `noCollision` e fluido vazio. A checagem da posição projetada evita atravessar chunks indisponíveis. Destino fora dos limites Y ou não seguro é descartado.

O cliente recebe somente `MOTION` (byte: 0 parado, 1 horizontal, 2 vertical) e `DISAPPEARING` (boolean). O servidor é responsável por objetivos, cooldowns, rota e movimento. Um controller `fragmento` escolhe loops `idle/move/float` e aceita `curious/startled/disappear` one-shots disparados no servidor pela GeckoLib. Oscilações visuais dos bones não são copiadas para a hitbox.

`beginDisappearance(boolean removeAfterAnimation)` é uma API explícita para regras futuras. **Não existe disparo aleatório.** Com `true`, remoção apenas no servidor após **27 ticks** (24 de animação + 3 de transição); com `false`, retorna ao idle. Um desaparecimento em andamento e os cooldowns são gravados via `ValueInput/ValueOutput`. No reload da entidade o desaparecimento é reapresentado para novos observadores, com nova janela integral de 27 ticks. O estado temporário de fuga é descartado no reload para evitar procurar uma ameaça já inexistente.

## Testes e limites

- `./gradlew --no-daemon clean build` (JDK 25) — exigido pela CI, quatro seeds: `14011`, `20261108`, `-904732`, `73199013`.
- `python3 scripts/ci/check_fragmento_errante_behavior.py` — invariantes estáticos de prioridades, sincronização, seis animações, persistência e ausência de spawn natural.
- `python3 scripts/ci/check_fragmento_errante_behavior.py --rcon` — servidor dedicado da CI: invocação `/summon` e consulta da entidade via `/data`, seguidas de limpeza.
- Demais verificações existentes de worldgen, motor onírico e seeds permanecem no workflow sem alterações.
- **Pendente no cliente:** voo em cavernas, obstáculos complexos, hitbox visual durante `startled`, transições de animação, save/reload durante uma animação e perseguição/evitação com dois jogadores.
- **Pendente teste manual multiplayer:** sincronização das animações em dois clientes simultâneos, interrupção de fuga e curiosidade quando um jogador sai.
- **Pendente benchmark:** MSPT/FPS com muitas entidades, rota em zonas de chunks descarregados e comparação após reiniciar o mundo.

### Roteiro de homologação manual

1. Inicie `./gradlew runClient` com Java 25; faça `/summon dreamsdimensions:fragmento_errante ~ ~3 ~` em área segura e já carregada.
2. Observe pausas e pequenos voos. Aproximar-se andando deve provocar observação ocasional, não perseguição permanente.
3. Corra a menos de três blocos ou cause dano controlado: verificar animação `startled` e fuga única sem oscilação de triggers.
4. Coloque paredes e teto próximos, caminhe até a borda de chunks carregados, verifique colisões e ausência de carregamento forçado.
5. Saia, salve e reabra o mundo com o mob ativo; repetir com dois jogadores e gravar vídeo das seis animações.
6. Testar `beginDisappearance` via regra de gameplay/developer test futuro; esta task **não** expõe comando de desaparecimento nem chama o método automaticamente.

## Fontes

- NeoForge 26.3 migration: https://docs.neoforged.net/primer/docs/26.3/
- NeoForge Entities / serialização: https://docs.neoforged.net/docs/entities/
- GeckoLib 5 Entities: https://github.com/bernie-g/geckolib/wiki/Geckolib-Entities-(Geckolib5)
- GeckoLib 5 Triggerable: https://github.com/bernie-g/geckolib/wiki/Triggerable-Animations-(Geckolib5)
- GeckoLib `AnimationController` e `GeoEntity` na branch principal: https://github.com/bernie-g/geckolib
