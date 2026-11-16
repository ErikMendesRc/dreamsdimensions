# Task 9.5.12 — Correção da incubação e nova interface da Cabeceira

## Diagnóstico reproduzido a partir do código

O ciclo de sono compartilha o estado `attemptedSleep` entre a tentativa de entrada nos sonhos no tick 100 e a rotina de despertar. Após o tick 100, `DreamSleepCycle.onWake` retorna `false`, impedindo `DreamIncubationService.afterFullNight` de executar para quem já teve a tentativa processada. Os ingredientes continuam na Cabeceira e o antigo clique com mão vazia os devolve ao jogador, parecendo uma incubação fracassada.

**Correção:** novo estado `completedWake`, independente de `attemptedSleep`, com deduplicação própria e rearmamento somente após novo ciclo de sono. A rotina registra o leito durante o sono (antes de o Minecraft limpar o `sleepingPos`) e enfileira o processamento de incubação para o próximo tick do servidor. A possibilidade de uma nova tentativa de entrada permanece condicionada ao gate anterior, evitando segunda rolagem após o tick 100.

## Nova interface — Vanilla+ cobre/madeira

O clique na Cabeceira abre `DreamBedsideMenu` e `DreamBedsideScreen` em vez de inserir ou retirar automaticamente.

- Dois slots de **visualização sincronizada**: `INSUMO` (quantidade pendente) e `PRODUTO` (quantidade pronta). Não são slots de depósito/retirada: o servidor impede mover itens fantasmas.
- Botão **Inserir**: coloca exatamente uma unidade do item na mão principal, caso seja insumo válido e a Cabeceira esteja próxima a uma cama. Não mistura categorias.
- **Shift+clique** em um ingrediente do inventário: deposita até a capacidade (2, ou 3 com Dreamcatcher).
- Botão **Coletar**: retira somente o produto já finalizado, sem entregar por engano os ingredientes pendentes.
- Botão **Cancelar**: devolve exclusivamente ingredientes ainda não processados.
- Mostra o ambiente detectado ao redor da cama e as contagens `pendentes/capacidade` e `prontos`.
- A Cabeceira continua com **inventário persistente por proprietário**, sem novos itens, novos drops, nem timer que funcione fora do ciclo de sono.
- Menu com 36 slots do inventário vanilla e GUI original 176×166. Traduções `pt_br` e `en_us`.

A chaleira existente não possui um menu registrado no código desta branch: é operada por cliques diretos. Esta task introduz uma GUI dedicada **para a Cabeceira**, sem mudar a Chaleira.

## Teste rápido em mundo descartável

```mcfunction
/gamemode creative
/give @s dreamsdimensions:dream_bedside 1
/give @s minecraft:red_bed 1
/give @s minecraft:glass_bottle 4
/time set day
```

Monte cama e Cabeceira lado a lado, **ao ar livre**, longe de fogueiras e de 8+ flores/folhas num raio de 3 blocos; não use Colar dos Sonhos no slot Curios.

1. Clique na Cabeceira: a interface deve abrir mesmo com o inventário vazio.
2. Mantenha garrafas de vidro na mão principal, clique duas vezes em **Inserir**, ou dê Shift+clique na pilha no inventário.
3. Verifique `INSUMO: 2`, `PRODUTO: vazio`, ambiente `Sereno`.
4. Feche a interface e reabra: os materiais devem persistir.
5. Entre em Survival e use `/time set night` (com cheats), **durma até amanhecer naturalmente** — não simule o despertar com `/time set day`.
6. Reabra: insumo vazio, produto contendo **2 Frascos de Sereno**. Clique **Coletar**; os produtos devem entrar no inventário apenas uma vez.
7. Repita com 3 unidades + Dreamcatcher suspenso em quarto de Silêncio para validar capacidade, sem teto no Sereno.
8. Teste **Cancelar**, relog, quebra de Cabeceira, inventário cheio, multiplayer dono vs não dono, rejeição de insumos errados, sono interrompido, cooldown de reentrada e duplicação de wake.

### Critérios técnicos

`DreamSleepCycleTest` cobre wake completo após tentativa no tick 100, duplicação e cancelamento. `scripts/ci/check_task_9_5.py` cobre registro de menu/screen, callbacks, traduções e integridade do PNG. CI de build/JUnit/servidor dedicado **não comprova** clique/sleep/drops manuais.

Nenhum merge na `main`.
