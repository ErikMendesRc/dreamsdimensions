# Dreams Dimensions Mod - Documentação Técnica

## Visão Geral

O Dreams Dimensions é um mod para Minecraft que introduz múltiplas dimensões de sonho. Esta documentação detalha os aspectos técnicos, as regras de jogabilidade e o conteúdo do mod.

---

## Mecânicas de Jogo e Regras

Esta seção detalha as regras que governam a interação entre o Overworld e as dimensões de sonho.

### Entrando em uma Dimensão de Sonho (Sonhando)

Para entrar em uma das dimensões de sonho, o jogador deve seguir um procedimento específico:

*   **Ação Requerida**: Dormir em uma cama no Overworld.
*   **Duração**: O jogador deve permanecer dormindo por **100 ticks (5 segundos)**.
*   **Resultado**: Após o tempo de espera, o jogador é automaticamente transportado para uma das dimensões de sonho disponíveis, escolhida aleatoriamente. As dimensões disponíveis são `dreamscape` e `campo_onirico_azul`.

### Retornando do Sonho (Acordando)

Para retornar ao Overworld, o jogador precisa usar o item `Despertador Onírico`.

*   **Ação Requerida**: Usar o `Despertador Onírico` (`oneiric_awakener`).
*   **Condição**: O jogador deve estar em qualquer uma das dimensões de sonho (`dreamscape` ou `campo_onirico_azul`).
*   **Duração de Uso**: O item deve ser mantido em uso por **60 ticks (3 segundos)**.
*   **Cooldown**: Após o uso bem-sucedido, o item entra em um tempo de recarga de **60 ticks (3 segundos)**.
*   **Lógica de Ponto de Retorno**: O local para onde o jogador retorna no Overworld é determinado pela seguinte ordem de prioridade:
    1.  **Local da Cama Original**: Ao lado da cama específica que o jogador usou para entrar no sonho.
    2.  **Ponto de Spawn Pessoal**: Se a cama original não for válida, o jogador é enviado para seu ponto de respawn definido no Overworld.
    3.  **Ponto de Spawn Global**: Se nenhum dos anteriores for válido, o jogador retorna ao ponto de spawn global do mundo.

---

## As Dimensões dos Sonhos

O mod atualmente inclui duas dimensões de sonho, cada uma com suas próprias características.

### Dimensão `dreamscape`

*   **Propriedades**: Luz ambiente constante, camas funcionam, âncoras de renascimento não.
*   **Bioma Principal (`dreamscape_biome`)**:
    *   **Descrição**: Um bioma sereno e vazio, caracterizado por uma atmosfera tranquila.
    *   **Spawns de Criaturas**: Nenhuma criatura é gerada naturalmente.

### Dimensão `campo_onirico_azul`

*   **Propriedades**: Similares à `dreamscape`, com luz constante e camas funcionais.
*   **Bioma Principal (`campo_onirico_azul`)**:
    *   **Descrição**: Um bioma com uma paleta de cores azulada e vibrante.
    *   **Spawns de Criaturas**: Este bioma é habitado por criaturas do Overworld (hostis e passivas).

---

## Blocos e Itens

### Blocos por Dimensão

#### Blocos do Overworld

Estes são os blocos primários que o jogador deve encontrar para iniciar sua jornada.

| Nome do Bloco                 | ID do Bloco                               | Geração                               |
| ----------------------------- | ----------------------------------------- | ------------------------------------- |
| Minério dos Sonhos            | `dreamsdimensions:dream_ore`              | Gerado no subsolo do Overworld.       |
| Minério dos Sonhos de Ardósia | `dreamsdimensions:deepslate_dream_ore`    | Gerado nas camadas profundas do Overworld. |

#### Blocos da Dimensão `dreamscape`

Estes blocos formam a paisagem da dimensão `dreamscape`.

| Nome do Bloco      | ID do Bloco                          |
| ------------------ | ------------------------------------ |
| Grama dos Sonhos   | `dreamsdimensions:dream_grass_block` |
| Pedra Serena       | `dreamsdimensions:serene_stone`      |
| Terra dos Sonhos   | `dreamsdimensions:dream_dirt_block`  |
| Areia dos Sonhos   | `dreamsdimensions:dream_sand_block`  |

#### Blocos da Dimensão `campo_onirico_azul`

Estes blocos formam a paisagem da dimensão `campo_onirico_azul`.

| Nome do Bloco                 | ID do Bloco                               |
| ----------------------------- | ----------------------------------------- |
| Grama dos Sonhos Azul         | `dreamsdimensions:blue_dream_grass`       |
| Pedra dos Sonhos Azul         | `dreamsdimensions:blue_dream_stone`       |
| Pedregulho dos Sonhos Azul    | `dreamsdimensions:blue_dream_cobblestone` |
| Terra dos Sonhos Azul         | `dreamsdimensions:blue_dream_dirt`        |

#### Blocos Criados pelo Jogador

Estes blocos não são gerados naturalmente e devem ser criados.

| Nome do Bloco                 | ID do Bloco                               |
| ----------------------------- | ----------------------------------------- |
| Bloco Cintilante dos Sonhos   | `dreamsdimensions:dream_shimmer_block`    |
| Musgo Luminoso dos Sonhos     | `dreamsdimensions:dream_glow_moss`        |
| Pedra Infundida dos Sonhos    | `dreamsdimensions:dream_infused_stone`    |
| Bloco do Núcleo Onírico       | `dreamsdimensions:oneiric_core_block`     |

### Itens

Itens podem ser criados e transportados entre dimensões.

| Nome do Item         | ID do Item                       | Descrição                                         |
| -------------------- | -------------------------------- | ------------------------------------------------- |
| Pó dos Sonhos        | `dreamsdimensions:dream_dust`    | Material obtido da mineração de Minérios dos Sonhos. |
| Despertador Onírico  | `dreamsdimensions:oneiric_awakener` | Ferramenta para retornar ao Overworld.            |

---

## Receitas

### Criação (Crafting)

| Item Criado                  | Ingredientes                                                                                             | Padrão (Pattern)                                                                        |
| ---------------------------- | -------------------------------------------------------------------------------------------------------- | --------------------------------------------------------------------------------------- |
| **Despertador Onírico**      | 1x Pena, 1x Bloco do Núcleo Onírico, 1x Garrafa de Vidro                                                  | Vertical: Pena (topo), Bloco do Núcleo Onírico (meio), Garrafa de Vidro (baixo)         |
| **Bloco do Núcleo Onírico**  | 4x Pedra Infundida dos Sonhos, 4x Pó dos Sonhos, 1x Relógio                                              | Relógio no centro, cercado por Pedra Infundida e Pó dos Sonhos em padrão de xadrez.     |
| **Pedra Infundida dos Sonhos** | 2x Pedra, 2x Pó dos Sonhos                                                                               | Padrão de xadrez 2x2 com Pedra e Pó dos Sonhos.                                         |

### Fornalha (Smelting & Blasting)

| Item Resultante         | Ingrediente                  | Método         | Tempo (Ticks) | Experiência |
| ----------------------- | ---------------------------- | -------------- | ------------- | ----------- |
| **Pedra dos Sonhos Azul** | Pedregulho dos Sonhos Azul   | Smelting       | 200           | 0.1         |
| **Pedra dos Sonhos Azul** | Pedregulho dos Sonhos Azul   | Blasting       | 100           | 0.1         |
