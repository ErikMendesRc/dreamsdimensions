# QA Manual — Sprint 3.4 (Poções / Brewing / Retorno)

## Pré-requisitos
- Build recente da branch.
- Mundo de teste singleplayer.
- Servidor dedicado de teste com 2 jogadores.
- Comandos habilitados para inspeção (`/effect`, `/time`, `/reload`, `/gamemode`).

## 1) Singleplayer — Duração e amplifier

1. Obter ingredientes de brewing e criar:
   - `ow_oneiric_base`
   - `ow_potion_of_anchoring`
   - `ow_potion_of_clarity`
   - `ow_potion_of_ethereal_phase`
   - `ow_potion_of_early_awakening`
2. Consumir cada poção e medir duração esperada:
   - Anchoring: ~2:00
   - Clarity: ~1:30
   - Ethereal Phase: ~1:00
   - Early Awakening: ~1:00
3. Validar amplifier visível no efeito ativo (nível I / amplifier 0).
4. Repetir ciclo completo **3 vezes**.

## 2) Economia / custo real

1. Validar recipe de `ow_dream_catalyst` com os 3 ingredientes esperados.
2. Validar recipe de `ow_stabilizing_essence` com os 3 ingredientes esperados.
3. Validar brewing:
   - Anchoring usa `ow_dream_binding_thread`
   - Early Awakening usa `totem_of_undying`
4. Confirmar que não há caminho trivial de early game para as poções mais fortes.

## 3) Multiplayer dedicado — simultaneidade

1. Jogador A e B bebem poções diferentes no mesmo tick aproximado.
2. Confirmar que duração/efeitos de A não alteram B e vice-versa.
3. Repetir por 3 rodadas.

## 4) Multiplayer dedicado — Awakener simultâneo

1. Colocar A e B em dimensão onírica.
2. Ambos usam `ow_oneiric_awakener` quase ao mesmo tempo.
3. Confirmar:
   - teleporte único por uso
   - sem perda de retorno
   - sem consumo indevido
   - sem stuck em dimensão errada
4. Repetir por 3 rodadas.

## 5) Cooldown stacking / lag

1. Com `ow_oneiric_awakener`, provocar uso repetido com latência (rede limitada do ambiente, se disponível).
2. Confirmar que cooldown aplica **uma única vez por uso bem-sucedido**.
3. Confirmar ausência de duplicação ao finalizar uso sob atraso.

## 6) /reload e persistência

1. Executar `/reload` após entrar no mundo.
2. Revalidar recipes de crafting/brewing e aplicação dos efeitos.
3. Repetir fluxo de poções + awakener.

## 7) Critério de aceite

- Sem crash no cliente/servidor.
- Sem regressão de teleport/retorno.
- Sem duplicação de cooldown.
- Resultados consistentes em 3 repetições por cenário.
