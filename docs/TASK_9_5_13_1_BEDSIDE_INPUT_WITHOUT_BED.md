# Task 9.5.13.1 — Corrigir entrada bloqueada sem cama

**Problema observado:** a GUI exibe `No bed nearby` e, mesmo para
ingredientes reconhecidos (garrafa de vidro, linha, carvão, pedra infundida e
erva torrada), o slot de entrada não aceita soltar o item.

**Causa reproduzida no código:** o `Slot.mayPlace` da Cabeceira incluía
`DreamBedsideMenu.this.hasBed()`, vinculando uma operação de inventário à
existência de cama próxima. A checagem ocorre também no cliente, tornando o
slot visualmente não interativo até o leito ser detectado.

**Correção:** removido exclusivamente o bloqueio de cama na inserção.
O usuário pode armazenar ou retirar ingredientes a qualquer momento.
`DreamIncubationService.afterFullNight` continua exigindo um leito real,
noite completa e ambiente compatível: não há processamento sem cama.
Validação de ingrediente, capacidade de 2/3, propriedade, separação
entrada/saída e persistência continuam vigentes.

**Polimento:** indicador sem cama agora diz `Sem cama · 0/2` (ou
`No bed · 0/2`), evitando texto estourado. Eliminada a palavra
`Inventory` que estava sobre a borda da interface.

## Teste manual em cliente

```mcfunction
/gamemode creative
/give @s dreamsdimensions:dream_bedside 1
/give @s minecraft:glass_bottle 5
```

1. Coloque **apenas a Cabeceira, sem cama**. Abra a GUI:
   status `Sem cama · 0/2`. Arraste uma garrafa ao input — deve entrar.
2. Arraste a segunda (2/2); tente a terceira — deve permanecer na mão/inventário.
3. Retire ambas com mouse e Shift+clique; recoloque duas e feche/reabra
   a interface: os materiais devem persistir.
4. Coloque uma cama vanilla ao lado; reabra a interface:
   status `Sereno · 2/2 · Pronto: 0` em céu aberto.
5. `/gamemode survival` e `/time set night`, dormir até amanhecer.
   Conferir 2 Frascos de Sereno na saída.
6. Em outro jogador, confirmar que não pode depositar ou retirar os itens
   já pertencentes ao proprietário.
7. Tentar inserir pedra, terra ou outro item inválido — o input deve recusar.
8. Repetir teste com Dreamcatcher pendurado acima de quarto compatível
   para liberar 3 insumos.

**Nota:** os testes automatizados verificam esta regressão estrutural,
compilação e inicialização de servidor, mas não substituem teste de
arrastar com mouse no cliente.
