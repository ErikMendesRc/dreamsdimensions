# TASK 4.6 — Vigia Adormecido: Pacto de Vigília e hardening da IA

**Branch:** `migration/minecraft-26.3`. **Minecraft 26.3 / NeoForge 26.3.0.101 / Java 25 / GeckoLib 5.5.7**.

## Diagnóstico da Task 4.5

1. **Rotas terrestres sem verificação intermediária:** a escolha do destino verificava apenas a ilha no final do caminho. Em bordas suspensas isso poderia conduzir o Vigia a atravessar fendas ou áreas sem suporte.
2. **Persistência de estados transitórios:** caminhos de navegação vanilla não eram restaurados. Recarregar uma entidade no estado MOVING podia deixar estado e animação inconsistentes. One-shots recarregados com contador parcial também podiam parar antes de o clip terminar.
3. **Sem progressão interativa:** o Vigia despertava/observava, mas ainda não havia ação útil e distinta da Sintonização Onírica do Fragmento.
4. **Seleção de alvo e cooldown:** o ritual precisa impedir farm, interrupções por dano/movimento e confusão entre jogadores no multiplayer.
5. **Proteção contra void:** ações de retorno exigem checagem final de chunk carregado, colisão, suporte, dimensão e distância. Não se pode usar `DreamSpawnHelper.findOrCreateSafeArrival`, que pode alterar blocos e gerar plataformas (correto para o Motor Onírico, incorreto para o Vigia).

## Nova mecânica: Pacto de Vigília

O Vigia torna-se uma **âncora viva**. Perto de uma ilha suspensa, um jogador observa a criatura imóvel; em resposta, ela reconhece o visitante e *guarda a memória do local*.

1. Aproxime-se de um Vigia desperto e esteja a até **5,5 blocos** dele.
2. Fique **no chão**, sem correr, praticamente imóvel (velocidade horizontal² ≤ **0,003**) e **olhando** para a criatura (produto escalar da visão ≥ **0,88**). Exige linha de visão real.
3. Mantenha a atenção por **100 ticks (5 s)**. Há pulsos sutis de `END_ROD` enquanto progride. Interromper o olhar, andar, mudar de alvo ou sofrer dano zera o progresso.
4. Ao completar, a animação GeckoLib existente **`inspect_ground`** é executada e o serviço de pacto registra um **ponto de retorno previamente validado** em piso natural, com colisão segura.
5. O jogador recebe mensagem de ação e uma proteção **de 120 s**, **de uso único**, que não consome itens.
6. Se cair pelo menos **12 blocos abaixo** da altura da âncora com velocidade vertical **inferior a -0,10**, estando a até **48 blocos horizontalmente**, o serviço tenta resgatá-lo para o ponto registrado. Revalida chunk, piso, fluidos, mundo e hitbox **no momento do uso**.
7. Se a âncora tiver sido destruída, bloqueada ou descarregada, o retorno é recusado com segurança; **nunca** força chunk ou modifica worldgen. A carga é consumida de qualquer forma.
8. O Vigia entra em cooldown de **180 s** após conceder um pacto; o cooldown é salvo em NBT. Um jogador só mantém **uma âncora ativa** por vez, com descarte ao expirar, trocar de dimensão, morrer ou desconectar.

**Limites:** o pacto não opera fora da Dreamscape, em criativo/espectador, para um ponto não seguro ou quando o jogador está fora da área do pacto. Não substitui a Queda Lenta do Fragmento Errante. A proteção é **transitória**, não persiste após restart; isso é proposital para evitar ponteiros obsoletos de chunks e dimensionamento de dados por jogador.

O ritual usa os **assets existentes**; sem novas animações, texturas, blocos, itens, loot ou partículas customizadas.

## Arquitetura e reutilização

| Componente | Papel |
|---|---|
| `CalmPlayerInteraction` | Predicado compartilhado para rituais de atenção, linha de visão e imobilidade. Não assume qual recompensa será concedida |
| `SafeGroundNavigation` | Verifica suporte, altura, colisão, fluidos, borda do mundo e disponibilidade dos chunks; amostra corredores terrestres para evitar quedas |
| `DreamAnchorPolicy` | Limites imutáveis de distância, duração e velocidade vertical; permite valores diferentes em futuras entidades |
| `DreamAnchorService` | Serviço independente da espécie para concessão, consumo, limpeza de sessão e retorno seguro por jogador |
| `VigiaAdormecidoEntity` | Apenas o ritual, o estado de IA e seus parâmetros exclusivos; usa o serviço, não controla teleporte de jogadores |

O motor compartilhado `DreamMobPopulationService` continua sendo o **único** coordenador de população. O listener adicional de `DreamAnchorService` é um **serviço de gameplay por jogador**, não outro motor de spawn. O Fragmento permanece inalterado.

## Correções de transição e movimento

- Toda transição do Vigia para fora de WATCHING cancela o progresso do ritual. Dano também cancela imediatamente.
- Caminhada exige habitat válido no destino, colisão segura e um corredor amostrado com suporte em todos os pontos. Se não houver rota admissível, o Vigia permanece imóvel; o worldgen não muda.
- Durante reload: o estado MOVING é convertido para WATCHING (não há caminho vanilla restaurado) e estados de animação one-shot reiniciam com `stateTicks=0`; cooldowns e chaves preexistentes são mantidos.
- O NBT acrescenta apenas `VigiaPactCooldown` e não renomeia `VigiaState`, `VigiaStateTicks`, `VigiaIdleWithoutPlayer`, `VigiaWanderCooldown`, `VigiaInspectCooldown`, `VigiaReactionCooldown` ou `VigiaHomeX/Y/Z`.

## Testes

- `./gradlew --no-daemon clean build` (Java 25), que executa `DreamAnchorPolicyTest` — limites de queda, velocidade, distância, expiração e perfis inválidos.
- `python3 scripts/ci/check_vigia_mechanics.py` (contratos estáticos de implementação e registro de eventos).
- A CI existente mantém as quatro seeds `14011`, `20261108`, `-904732`, `73199013`, verificações preexistentes e dedicated-server smoke tests.
- **Manual necessário:** criar pacto olhando para o Vigia, interromper ao correr/virar a câmera, conferir mensagens e partículas, cair no void, repetir após a carga, testar anchor destruída/bloqueada, distância >48, duas pessoas, persistência do cooldown do Vigia após save/reload e animações GeckoLib.

Os testes sem jogador humano no dedicated server **não comprovam** a dinâmica real do ritual nem uma recuperação de queda sob latência multiplayer; são critérios de homologação in-game.

## Referências consultadas

- https://docs.neoforged.net/primer/docs/26.3/
- https://docs.neoforged.net/docs/entities/livingentity/
- https://aldak0.ru/javadoc/26.3.x/net/minecraft/server/level/ServerPlayer.html
- https://aldak0.ru/javadoc/26.3.x/net/minecraft/world/entity/Entity.html
- https://aldak0.ru/javadoc/26.3.x/net/neoforged/neoforge/event/tick/PlayerTickEvent.Post.html
