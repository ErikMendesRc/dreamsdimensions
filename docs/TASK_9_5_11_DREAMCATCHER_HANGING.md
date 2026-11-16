# Task 9.5.11 — Dreamcatcher ceiling-only

**Minecraft/NeoForge 26.3 · Java 25 · branch migration/minecraft-26.3**

## Comportamento

- O Apanhador de Sonhos só pode ser colocado clicando a **face inferior** de um bloco capaz de sustentar o centro de sua face `DOWN`.
- Cliques no chão, na superfície superior e nas paredes são rejeitados mesmo com um teto próximo.
- Orientação horizontal em quatro direções para mostrar a frente voltada ao jogador.
- Seleção estreita e rotacionada; sem colisão para pessoas; nenhuma tarefa periódica.
- Quando o bloco de sustentação é removido, a atualização de vizinhos remove o Apanhador; loot continua sendo o item `dreamsdimensions:dreamcatcher`.
- O bônus da Cabeceira Onírica passa a reconhecer um Apanhador na coluna até 4 blocos acima da cama, possibilitando tetos mais altos.
- IDs, receitas, item 2D, textura 3D e geometrias existentes permanecem.

## Implementação

`DreamcatcherBlock` usa `getStateForPlacement`, `canSurvive`, `updateShape`, `getShape`, `rotate` e `mirror` compatíveis com a versão 26.3. O `blockstates/dreamcatcher.json` possui `facing=north/east/south/west`, todos referenciando o mesmo modelo 3D, com rotação y.

O teste estático em `scripts/ci/check_task_9_5.py` valida as regras e os quatro variantes. A CI compila e inicia o servidor, mas não testa cliques reais no cliente.

## Teste no Minecraft

```mcfunction
/gamemode creative
/give @s dreamsdimensions:dreamcatcher 4
/setblock ~ ~3 ~ minecraft:oak_planks
/time set day
```

1. Olhe para cima e clique na **parte inferior** da madeira com o item em mãos: deve pendurar na posição abaixo.
2. Tente colocar na terra, em faces laterais e no topo da madeira: não deve colocar.
3. Crie outro suporte em outra direção, coloque de lados opostos e observe a rotação.
4. Em Survival, quebre o teto: deve soltar um item; não deve ficar flutuando.
5. Instale diretamente acima de uma cama, com teto em 3–5 blocos de altura. Verifique o bônus da Cabeceira.
6. Confira item e bloco sem texturas ausentes.

**Não use /setblock diretamente com dreamsdimensions:dreamcatcher para testar a restrição:** o comando administrativo ignora as regras de posicionamento normal. A regra deve ser testada clicando com o item.

**Limites:** a CI não substitui a inspeção do cliente nem o teste de drop de suporte em Survival. Sem merge na main.
