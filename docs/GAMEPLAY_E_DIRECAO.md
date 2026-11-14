# Gameplay e direção atual — DreamsDimensions

## Promessa do jogo

**DreamsDimensions é um mod de sonhos e suas dimensões.** Ao longo da evolução do mod poderão existir experiências pacíficas, pesadelos, sonhos estranhos ou outras famílias de dimensões, com diferentes riscos e regras. **Nenhuma das duas dimensões atuais define o universo inteiro.**

O **Overworld é a base compartilhada**. Ele deve oferecer exploração, obtenção de recursos, construção, descoberta e preparação interessantes mesmo quando o jogador não está em sonho. O que o jogador traz ou aprende dos sonhos poderá futuramente ampliar suas possibilidades no Overworld; essa integração **não é uma feature automaticamente presente**.

## Princípios para aprovar gameplay novo

1. **Função antes de item.** Indicar o que o jogador faz, que decisão toma, que resultado obtém e qual consumidor concreto utiliza o resultado. Um bloco decorativo pode ser assumidamente decorativo; não inventar lore mecânica para justificá-lo.
2. **Exploração que compensa.** Biomas, flora, rochas, ruínas, mobs e fenômenos precisam justificar o deslocamento: caminho, recurso, ferramenta, hábito, risco justo, informação verificável ou utilização em base.
3. **Overworld independente.** Novas funções do mundo real não devem depender de existir Dreamscape ou Campo Azul. O projeto inteiro pode ter dezenas de dimensões futuras.
4. **Reuso comprovado.** Reutilizar ferramentas/contratos quando mais de uma feature concreta exigir o mesmo comportamento. Não criar um framework que só tem testes sem consumidor jogável.
5. **Dreams ≠ magia genérica.** A temática do sono, da imaginação, de experiências e de lembranças pode orientar mecânicas; partículas azuis e inscrições misteriosas por si sós não criam profundidade.
6. **Equilíbrio e segurança.** Sem duplicação, impossibilidade de recuperar itens, obstáculos injustos, scans caros em ticks ou alteração retroativa inesperada de chunks de jogadores.
7. **Gameplay observável.** Testes de CI não substituem descoberta sem comandos, sobrevivência, multiplayer, persistência, caminhos acessíveis e experiência visual.

## Ciclos existentes no código

**Overworld:** descobrir Dream Ore/Deepslate Dream Ore, minerar Dream Dust, fabricar itens de preparação e retorno; encontrar Pedra Marcada, minerar e transformá-la no Stonecutter para construir. O jogador deve poder usar esses recursos sem precisar de uma dimensão específica.

**Sono/sonhos:** o código contempla cama, Colar equipado, rolagem por sono concluído, dimensão de destino, retorno seguro, Despertador consumível e respawn; **a entrada real segue desligada por padrão** até aprovação de segurança com jogadores.

**Mundo de sonhos:** ilhas, vegetação, ruínas e mobs são a prova de conceito de biomas e sistemas, não o catálogo definitivo de experiências.

## O que está deliberadamente em aberto

- Como será a progressão universal ao longo de muitas dimensões.
- Qual mecânica original do Overworld criará o primeiro incentivo real para descobrir o universo dos sonhos.
- Como anomalias ambientais existirão com função e recompensa, sem roteiros artificiais ou sistema de investigação vazio.
- Quais flora, mobs, equipamentos e estações futuras terão consumidores concretos.

**Essas são questões de design, não tarefas numeradas aprovadas.** Nenhuma nova sequência 7.7+ é autorizada por este documento.

**Decisão posterior (Task 9.0):** a expansão do Overworld foi autorizada como **contrato de design**, formalizado em [Task 9.0](TASK_9_0_OVERWORLD_GAMEPLAY_CONTRACT.md) e [catálogo 9.0](OVERWORLD_CONTENT_CATALOG_TASK_9_0.json). A seleção da fatia F1 para a futura 9.1 não registra blocos e não implementa as ideias do catálogo; cada implementação precisa de sua task e de homologação própria.

## Critério de pronto para qualquer ideia

Um novo recurso só entra quando há especificação de **origem → interação → custo/risco → resultado → uso → validação**, pelo menos um playtest-alvo, e uma justificativa clara de por que não basta usar a alternativa vanilla. Mecânicas universais não devem codificar dependências de duas dimensões apenas porque são as únicas disponíveis hoje.
