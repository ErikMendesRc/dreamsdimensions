# TASK 4.4 — Dream Mob Framework: fundação reutilizável

**Branch:** `migration/minecraft-26.3`  
**Stack:** Minecraft 26.3, NeoForge 26.3.0.101, Java 25, GeckoLib 5.5.7.  
**Escopo:** somente refatoração de código Java, testes e documentação; sem novos mobs, assets, worldgen, mecânicas ou alterações na `main`.

## 1. Arquitetura anterior e riscos

- `FragmentoIslandHabitat` continha a detecção física de massa suspensa e o predicado de spawn.
- `FragmentoIslandPopulation` unia o listener de `PlayerTickEvent.Post`, o algoritmo de amostragem de altura e todas as constantes da espécie.
- `FragmentoErranteEntity` tinha um `FlyingMoveControl` interno e o controle físico da órbita idle.
- Os goals `WanderGoal`, `CuriosityGoal`, `ResonanceGoal` e `FleeGoal` concentram mecânicas próprias do Fragmento.

O problema era a necessidade de duplicar esses três sistemas técnicos a cada nova espécie. A solução implementa composição e perfis explícitos, evitando uma hierarquia de entidades.

## 2. Arquitetura final

| Responsabilidade | Código compartilhado | Código específico do Fragmento |
| --- | --- | --- |
| População e avaliação periódica | `entity/spawn/DreamMobPopulationService` | `FragmentoIslandPopulation.profile()` |
| Parâmetros de spawn | `entity/spawn/MobPopulationProfile<T>` | Constantes de frequência, raios, cotas e spawn do Fragmento |
| Validação de habitat | `entity/habitat/DreamMobHabitat`, `FloatingIslandHabitat` | `FragmentoIslandHabitat` e seu perfil imutável |
| Movimentação aérea | `entity/movement/DreamFlyingMoveControl`, `FlightProfile` | `FLIGHT_PROFILE` no `FragmentoErranteEntity` |
| AI/GeckoLib | API vanilla + GeckoLib 5, **sem framework paralelo** | Quatro goals, controllers e sincronização exclusivos |

O `DreamsDimensions` registra uma única assinatura de tick para o coordenador. Cada espécie configura um perfil e um `CandidateSelection` (seletor de pousos); não instala outro `PlayerTickEvent`. O serviço usa apenas entidades já presentes e chunks carregados.

## 3. Fluxo de população

1. O único `DreamMobPopulationService.onPlayerTick` recebe um jogador de servidor.
2. Recusa espectadores, mortos e a gamerule `spawn_mobs=false`.
3. Para cada perfil registrado, valida dimensão, altura do jogador e fase temporal determinística por UUID.
4. Deduplica avaliações pela chave `(espécie, célula XZ de 128 blocos)` durante o intervalo configurado. O memo é por `ServerLevel`, fraco e limitado a 256 chaves por nível; não é salvo em NBT nem força carregamento de chunks.
5. Calcula a população local de **uma espécie** em AABB limitada.
6. Amostra até o orçamento do perfil, consultando primeiro a disponibilidade do chunk e depois o heightmap e o habitat.
7. Rejeita candidatos muito afastados verticalmente, perto demais do jogador ou de entidades existentes.
8. Utiliza `EntityType.spawn(level, surface, EntitySpawnReason.NATURAL)`, preservando inicialização/eventos vanilla/NeoForge.
9. Depois do primeiro spawn bem-sucedido, encerra a avaliação (mesma regra existente do Fragmento).

### Perfil existente do Fragmento (mantido)

- Período: **200 ticks**, máximo de **48 amostras**.
- População local: **6**, máximo configurado de **2 criações por avaliação**, parada real após **1 sucesso**.
- Anel de amostragem horizontal: **16–64 blocos**; contagem local até **72 blocos** em X/Z.
- Jogador em **Y145–244**; candidato em **Y158–231**, diferença vertical máxima **65 blocos**.
- Distâncias mínimas: **14 blocos** do jogador e de outras instâncias já contadas.
- Permanece o spawn CREATURE vanilla definido no bioma (peso 100, grupos 2–3, probabilidade de geração 0.34); o coordenador é apenas complemento para ilhas exploradas.

**Nota de compatibilidade:** com um jogador, permanece a fase temporal anterior. Em multiplayer, a deduplicação de avaliações em uma mesma célula reduz intencionalmente tentativas redundantes e pode alterar a cadência de manutenção quando os jogadores estão juntos; não altera o teto populacional configurado.

## 4. Detecção de ilhas

`FloatingIslandHabitat` recebe: dimensão, bioma, tag de piso, faixa de altura e uma lista de profundidades. O adaptador `FragmentoIslandHabitat` mantém as assinaturas estáticas utilizadas pelo predicado de spawn e pela AI.

O perfil atual usa `#dreamsdimensions:dreamscape_natural_ground`, `dreamsdimensions:dreamscape_biome`, Y158–231 e exatamente as profundidades **12, 20, 28, 36, 44, 52, 60, 68, 76, 84, 92, 100, 108**. Exige piso natural, espaço livre na entidade e ar em dois pontos separados por 5 blocos abaixo do poleiro. A checagem recusa as consultas a chunks não carregados. Não foi substituída por uma heurística simples de altura.

`hasNearbyIslandPerch` continua olhando até **9 blocos abaixo**; o Fragmento permanece limitado à âncora natal de **14 blocos**.

## 5. Voo físico e comportamento

`DreamFlyingMoveControl` é composto por um `PathfinderMob` e um `FlightProfile`; não depende de GeckoLib nem de `FragmentoErranteEntity`. Opera o mesmo servo de velocidade com `FlyingPathNavigation`, amortecimento e contenção de colisão/chunks. O estado transitório `hoverAnchor` agora pertence ao controlador e é reiniciado ao parar, interromper movimento ou recarregar.

Parâmetros transferidos sem ajuste numérico:

| Parâmetro | Valor |
| --- | --- |
| Fator de velocidade | 0.17 |
| Velocidade mínima / máxima | 0.08 / 0.19 |
| Aceleração máxima | 0.028 |
| Damping WAIT / chegada / obstáculo | 0.78 / 0.70 / 0.50 |
| Chegada (distância²) | 0.10 |
| Ondulação horizontal / vertical | 0.018 / 0.015 |
| Avanço de fase / razão vertical | 0.18 / 0.87 |
| Suavização de yaw | 0.12 |
| Oscilação idle Y / XZ | 0.14 / 0.035 |
| Período idle | 64 ticks |

A fórmula de correção do idle mantém ganhos **0.16/0.24** e limites **XZ ±0.014 / Y ±0.028**. Não são usadas escritas diretas de posição ou teletransporte para voo. Colisão/hitbox e ações físicas ficam do lado lógico do servidor.

Os quatro goals permaneceram **específicos**. A recompensa Queda Lenta, temporizadores de reação, curiosidade, fuga e dissipação não foram generalizados porque o próximo mob ainda não comprova contratos reutilizáveis para essas mecânicas.

## 6. GeckoLib, NBT e compatibilidade

A entidade permanece `GeoEntity` e mantém um controller denominado `fragmento`, os clipes `idle`, `move`, `float`, `curious`, `startled` e `disappear` e seus triggers de one-shot. `FragmentoRendererRegistration`, a geometria, o PNG, os assets e o registro `ModEntities` não foram alterados.

As chaves NBT preexistentes `FragmentoIdlePause`, `FragmentoCuriosityCooldown`, `FragmentoStartleCooldown`, `FragmentoResonanceCooldown`, `FragmentoAggressionWindow`, `FragmentoAggressionHits`, `FragmentoVanishAfterFlee`, `FragmentoIslandBound`, `FragmentoIslandX/Y/Z`, `FragmentoDisappearTicks` e `FragmentoRemoveAfterDisappear` permanecem iguais. O estado de flutuação é transiente e continua sendo reconstruído após reload.

O ID de registro continua `dreamsdimensions:fragmento_errante`, e a dimensão continua `dreamsdimensions:dreamscape`.

## 7. Como adicionar o próximo mob ao Dreams Dimensions

1. Crie a classe da espécie, estendendo uma entidade vanilla apropriada, com seus atributos e goals exclusivos. **Não** herde de `FragmentoErranteEntity`.
2. Registre o novo `EntityType` e seus atributos em `ModEntities`.
3. Caso use um habitat já disponível, forneça-o em um `MobPopulationProfile<T>` específico da espécie. Para ilhas, configure `FloatingIslandHabitat`. Para outros habitats, implemente apenas `DreamMobHabitat.isValid` sem modificar o validador de ilhas.
4. Caso precise de manutenção de população em chunks já visitados, crie um perfil próprio com tipo, classe, dimensão, heightmap, limites, distâncias, `CandidateSelection` e validação de habitat, e registre uma vez usando `DreamMobPopulationService.register(seuPerfil)`. **Não** crie outro listener de tick.
5. Registre separadamente o spawn natural vanilla/NeoForge com o `RegisterSpawnPlacementsEvent` e os atributos de bioma/worldgen. O serviço de população não substitui esse registro.
6. Caso o mob tenha **voo compatível com este estilo físico**, crie um `FlightProfile` próprio e instale `new DreamFlyingMoveControl(this, SEU_FLIGHT_PROFILE)`. No `aiStep` server-side, chame `applyIdleHover()` somente durante pausa; chame `resetHover()` quando navegar. Para outras mecânicas de voo, utilize um controlador diferente.
7. Para animações, use GeckoLib 5 como hoje: modelo/texture/animation da espécie, `GeoEntity`, cache, controllers e renderizador próprios. **Não** reaproveite à força nomes de clips do Fragmento.
8. Configure os testes do novo mob, valide colisões e chunks descarregados, NBT, gameplay em multiplayer e performance em quatro seeds.

## 8. Testes, CI e homologação

- `./gradlew --no-daemon clean build` com Java 25: **pendente de execução no GitHub Actions após atualização da branch**.
- `python3 scripts/ci/check_fragmento_errante_behavior.py`: ajustado para inspecionar os arquivos de voo e população extraídos; conserva asserts de NBT, spawn, efeitos e clipes.
- `python3 scripts/ci/check_dream_mob_framework.py`: novo teste **estático**, verifica event bus único, restrições de chunk, parametrização, IA e persistência.
- `src/test/java/com/dreamsdimensions/mod/entity/movement/FlightProfileTest.java`: teste JUnit de valores e validação do perfil.
- Matriz existente de CI: `14011`, `20261108`, `-904732`, `73199013`, build e dedicated server/RCON.
- **Pendente de homologação manual**: idle suave, voo ondulante com colisão, curiosidade, sintonização (90 ticks/360 ticks), fuga, desaparecimento (27 ticks), spawn e permanência sobre ilhas, dois jogadores na mesma região, bordas de chunks e save/reload.

Os scripts estáticos e o smoke com `/summon` **não comprovam** visualização/renderização, distribuição real de spawn nem a deduplicação em multiplayer. Registrar evidência in-game antes de declarar os itens acima aprovados.

## 9. Limitações e performance

- Limite de **48 amostras por avaliação** e **256 chaves recentes por nível**, sem caches globais de blocos.
- Um `CandidateSelection` deve consultar apenas chunks já carregados e retornar `null` para amostras indisponíveis. O seletor reutilizável `annulusHeightmapCandidate` preserva a distribuição atual do Fragmento.
- A deduplicação usa células de 128 blocos, portanto jogadores em lados opostos da borda de célula ainda podem gerar avaliações sobrepostas; o limite local ainda protege contra excesso.
- Somente o estilo de voo atual foi extraído. O framework não tenta servir mobs terrestres com um controlador aéreo.
- Novos perfis de população devem passar por validação de performance antes de ampliar a quantidade de espécies.
- Testes determinísticos de habitat em mundo real e testes end-to-end com jogadores humanos não são substituídos por asserts de código. Permanecem necessários na homologação.

## Referências

- https://docs.neoforged.net/primer/docs/26.3/
- https://docs.neoforged.net/docs/entities/
- https://docs.neoforged.net/docs/entities/livingentity/
- https://docs.neoforged.net/docs/concepts/events/
- https://github.com/bernie-g/geckolib/wiki/Geckolib-Entities-(Geckolib5)
- https://github.com/bernie-g/geckolib/wiki/Triggerable-Animations-(Geckolib5)
