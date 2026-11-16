# TASK 9.5.13 — Cabeceira Onírica com slots reais e interface vanilla+

**Versão:** Minecraft 26.3 / NeoForge 26.3 / Java 25  
**Branch:** `migration/minecraft-26.3` · **Sem merge na main**

## Objetivo e resultado

Substituir a tela com botões Inserir/Coletar/Cancelar por interação de inventário
nativa: arrastar, soltar, dividir pilha, Shift+clique e pegar saída. O jogador
consegue ver o insumo e o produto em slots reais. O bloco continua processando
uma noite completa de sono; a interface não concede processamento imediato.

A GUI de 176×166 pixels foi redesenhada com hierarquia vertical:
- título localizado e centralizado no topo;
- dois slots (entrada à esquerda, saída à direita) e seta onírica discreta;
- estado do ambiente e contadores separados das áreas clicáveis;
- inventário vanilla (27 slots + hotbar) ocupando as mesmas coordenadas padrão;
- moldura madeira/cobre e ícones decorativos pixel art, sem botões.

## Contrato do inventário

- `DreamBedsideBlockEntity implements Container`: slot 0 `INPUT`, slot 1 `OUTPUT`.
- Somente cinco famílias de insumos são aceitas; o output não aceita inserção.
- O slot de entrada limita a capacidade a 2, ou 3 com Dreamcatcher pendurado
  acima da cama. Uma pilha inserida antes da remoção do Dreamcatcher não é
  apagada nem truncada; o próximo ciclo processa no máximo 2.
- Uma única família por lote, saída bloqueando nova inserção enquanto pendente.
- O proprietário é registrado ao inserir; só ele altera os dois slots. Outros
  jogadores podem visualizar os slots, não manipular conteúdos.
- O menu sincroniza slots via protocolo vanilla, com `DataSlot` apenas para
  capacidade, ambiente, cama próxima e acesso do usuário.
- Não há novos handlers de rede, ticks globais, entidades ou receitas.
- `quickMoveStack` usa índices fixos: entrada 0, saída 1, inventário 2..28,
  hotbar 29..37; suporta shift na entrada/saída/inventário.
- Remover a Cabeceira derruba exatamente os itens restantes em cada slot.
- A serialização usa `ItemStack.CODEC`, com migração de `type/pending/finished`
  da Task 9.5.12 (incluindo unidades já incubadas); preserva `owner` e
  `last_night`, prevenindo processamento repetido da mesma noite.

## Limitações e QA

O pipeline valida compilação, JUnit, integridade de PNG, traduções e contratos de
menu e servidor. A homologação visual e os movimentos reais do mouse em cliente
são testes manuais (não inferir da CI que ocorreram).

### Smoke test em cliente

Na branch atualizada: `git pull origin migration/minecraft-26.3` e
`.\gradlew.bat runClient`.

```mcfunction
/gamemode creative
/give @s minecraft:red_bed 1
/give @s dreamsdimensions:dream_bedside 1
/give @s minecraft:glass_bottle 5
/time set night
```

Em um campo sem teto ou flores em excesso:
1. Colocar cama e Cabeceira adjacentes; abrir a Cabeceira.
2. Arrastar uma garrafa ao slot de entrada, depois outra. Capacidade máxima 2.
3. Testar Shift+clique: as garrafas válidas entram, outras não; 3ª garrafa
   permanece no inventário. Testar retorno do input via clique/Shift+clique.
4. Voltar ao Survival; dormir até amanhecer normalmente (sem Colar equipado).
5. Reabrir: os 2 frascos estão no slot de saída. Retirar por arrastar ou
   Shift+clique; repetir retirada não duplica.
6. Testar quinto ingrediente errado e conversão apenas no quarto específico.
7. Colocar Dreamcatcher num teto sólido sobre cama de Silêncio e confirmar
   capacidade 3; retirar o suporte após depositar 3 para conferir que nenhum
   insumo é destruído, e que no máximo 2 processam sem o Dreamcatcher.
8. Fechar/reabrir; sair e reentrar no mundo; quebrar Cabeceira para verificar
   drops únicos, com input + output se aplicável.
9. Multiplayer: segundo jogador vê conteúdo, mas não insere nem retira.
10. Aberto sem cama: exibe aviso e rejeita novos insumos.
11. Validar escala de GUI padrão e 2× / 3×; textos não se sobrepõem a slots.

**Não usar** `/time set day` para simular uma noite completa de incubação.
