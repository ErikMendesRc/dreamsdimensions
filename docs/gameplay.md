# Gameplay — Como funciona o sonho

## Sumário
- [Loop principal](#loop-principal)
- [Condições de entrada na dimensão onírica](#condições-de-entrada-na-dimensão-onírica)
- [Como voltar ao Overworld](#como-voltar-ao-overworld)
- [Regras e limitações](#regras-e-limitações)
- [Dicas práticas](#dicas-práticas)
- [FAQ](#faq)

## Loop principal
O loop do mod é:
1. **Dormir no Overworld**;
2. **Teleportar para sonho** (dreamscape ou campo_onirico_azul);
3. **Explorar/coletar/craftar**;
4. **Retornar com Despertador Onírico** ou via efeito de despertar prematuro.

## Condições de entrada na dimensão onírica
O teleporte por sono só acontece quando:
- o player está no **Overworld**;
- está realmente **dormindo**;
- já dormiu tempo suficiente (`isSleepingLongEnough`);
- não está usando o **Despertador Onírico** no mesmo momento;
- não há **Totem de Ancoragem Onírica** dentro do raio de proteção.

### Escolha da dimensão
- O mod lê a lista `dream_dimensions` da config.
- Se houver mais de uma dimensão válida, ele tenta evitar repetir imediatamente a última visitada.
- Fallback quando nenhuma dimensão configurada existe no servidor: `dreamsdimensions:dreamscape`.

## Como voltar ao Overworld
### Método 1 (principal): Despertador Onírico
- Pode ser usado apenas em dimensão marcada como sonho.
- Exige uso canalizado (~60 ticks).
- Ao concluir com sucesso:
  - teleporta para Overworld,
  - manda mensagem de sucesso,
  - aplica cooldown.

### Método 2 (situacional): efeito **Despertar Prematuro**
Se o jogador morrer em dimensão de sonho enquanto estiver com o efeito `ow_early_awakening`, a morte é cancelada e ocorre retorno ao Overworld com vida reduzida.

### Prioridade do ponto de retorno
1. posição de levantar da cama registrada no Overworld;
2. respawn padrão vanilla do jogador;
3. spawn global do Overworld com busca de ponto seguro.

## Regras e limitações
- Teleporte por sono **não** dispara fora do Overworld.
- O uso do Despertador fora de dimensões de sonho é bloqueado (`PASS`).
- O raio do Totem de Ancoragem é configurável (`anchoring_totem_radius`, padrão 8).
- No `campo_onirico_azul`, o jogador recebe Slow Falling reaplicado automaticamente.

## Dicas práticas
- Sempre mantenha cama válida no Overworld para retorno mais previsível.
- Coloque Totem de Ancoragem perto da base para evitar teleporte involuntário ao dormir.
- Faça estoque de Pó dos Sonhos cedo: ele alimenta quase toda a progressão de craft.

## FAQ
### “Durmo e não teleporto. Bug?”
Normalmente é uma destas causas:
- você não está no Overworld;
- ainda não dormiu o tempo mínimo;
- existe Totem de Ancoragem no raio;
- você está segurando/usando Despertador.

### “Usei o Despertador e nada aconteceu.”
Ele só funciona em dimensões marcadas como sonho na config.

### “Posso escolher sempre a mesma dimensão de sonho?”
Não diretamente; com múltiplas dimensões válidas, o mod tenta alternar para não repetir a última.
