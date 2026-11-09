# TASK 2.1.1 — Ajuste de visibilidade da Atmosfera Viva

**Branch:** `migration/minecraft-26.3` · **Stack:** Minecraft 26.3 / NeoForge 26.3 / Java 25

Esta atualização responde ao feedback de gameplay: a TASK 2.1 estava sutil demais. O controlador era válido, mas emitia em média muito poucas partículas, os testes de brilho raramente encontravam musgo e amostras de superfície falhavam ao voar no Criativo. Esta nota **substitui os parâmetros de partículas, áudio e névoa da descrição original da TASK 2.1**; o restante da arquitetura permanece.

## Parâmetros atuais

| Componente | Antes | Após ajuste |
|---|---|---|
| Intervalo ALL | 30 ticks, com descarte adicional de 50% e outros gates | 12 ticks, sem gate probabilístico geral |
| Partículas ambientais | Até 1 por intervalo e sem cor própria | Até 4 por intervalo; poeira RGB azul/ciano e spore blossom vanilla |
| Jardins luminosos | 3 amostras aleatórias; brilho raríssimo | Primeiro verifica bloco sob os pés, depois até 8 superfícies próximas; até 4 brilhos extras com poeira clara e END_ROD |
| DECREASED | Intervalo × 3 | Intervalo × 2, até 2 ambientais e 2 luminosas por emissão |
| MINIMAL | Nenhuma | Nenhuma (preservado) |
| Voo Criativo | Emissão dependente do heightmap do chão | Emissão ambiente em ar livre a até 6 blocos horizontais e poucos blocos verticais do jogador |
| Som ambiente | 340–640 ticks, gate 1/3, volumes 0.075–0.17 | 160–320 ticks, sem gate, volumes 0.15–0.26 |
| Distância da névoa | start 64 / end 224 / sky 256 | start 48 / end 170 / sky 210 |

A emissão respeita o estado de partículas do cliente, testa exclusivamente blocos de ar em chunks carregados e **não usa** `addAlwaysVisibleParticle`, pacotes de rede ou tipos próprios de partículas. `DustParticleOptions(int color, float scale)` é a API vanilla 26.3; nenhum novo arquivo gráfico foi adicionado. O limite máximo teórico na configuração ALL é de 8 partículas por intervalo, mas depende de ar livre e jardim luminoso realmente encontrado. A execução no servidor dedicado não carrega as classes cliente.

O controlador registra uma mensagem INFO **uma vez ao entrar no Campo Onírico Azul**: `Campo Onírico Azul: atmosfera cliente ativa`. Use-a para diagnosticar se o evento cliente entrou em funcionamento.

## Validação humana indispensável

1. Atualizar a branch, abrir pelo `.\\gradlew.bat runClient` e entrar no Campo Onírico Azul.
2. Conferir **Video Settings → Particles = All**. Em **Minimal**, o mod não emite partículas por projeto.
3. Conferir **Music & Sounds → Ambient/Environment** em volume audível.
4. Permanecer em pradaria aberta por 30 segundos e observar partículas azuis/ciano em torno do personagem; depois voar no Criativo e repetir.
5. Ficar sobre ou próximo a `dream_glow_moss`: observar os acentos luminosos eventuais.
6. Testar Decreased e Minimal, depois retornar a All.
7. Verificar uma única entrada do log `atmosfera cliente ativa` ao ingressar no mundo.
8. Comparar FPS e tempos de frame com a versão anterior usando as mesmas opções e local, sem afirmar ganho ou custo sem medição.
9. Sair para Overworld e Dreamscape: não deve haver novas partículas nem sons específicos.
10. Testar o retorno e reentrada, água, pausa e servidor dedicado.

**Limites:** névoa permanece estática (não há hook dinâmico por altitude); partículas RGB são sprites vanilla, sem emissão real de luz. A compilação e os testes de CI **não** comprovam visibilidade no cliente. Nenhum worldgen, item, mob, dimensão ou textura foi alterado.
