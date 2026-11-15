# DREAMSDIMENSIONS — DIREÇÃO DE ARTE PARA PIXEL ART

**Versão 1.0 — 15 de novembro de 2026**
**Minecraft 26.3 · NeoForge 26.3 · Java 25**
**Aplicação:** TODOS os itens, blocos, partículas e efeitos de inventário do mod.

## 1. DECISÃO ARTÍSTICA

O estilo oficial é **Minecraft Vanilla+ com materialidade rica e legibilidade premium**. Vanilla+ não significa cores pálidas, tons pastéis, superfícies chapadas, iconografia simplista ou ausência de sombras.

A referência enviada em 15/11/2026 apresenta objetos com contraste forte, pixels deliberados, bordas com espessura, metal e cristais facetados, couro volumétrico e fontes de luz claras. A qualidade técnica dessa referência deve ser reproduzida **sem copiar os sprites de terceiros**.

Os sprites da Task 9.5.7 são considerados rejeitados artisticamente devido à paleta desbotada, à ausência de profundidade e aos materiais mal definidos; a revisão 9.5.8 implementa a correção para seus 20 PNGs.

## 2. CONTRATO VISUAL OBRIGATÓRIO

1. **Reconhecimento em 16×16.** O item deve ser reconhecível sem tooltip, nome, runa ou descrição. A forma deve comunicar função física.
2. **Silhueta forte.** Um frasco não pode parecer um retângulo; uma sonda deve parecer uma ferramenta mecânica; couro acolchoado não pode parecer uma pedra ou papel.
3. **Sombreamento por planos.** Construir topo, frente, lateral, bordas e oclusão com iluminação dominante superior/esquerda. Diferenciar superfícies por valor e cor.
4. **Sombra profunda.** Cada família usa escuros expressivos, meios-tons relevantes e highlights seletivos. Não usar paleta pálida monocromática.
5. **Saturação com propósito.** Gemas, líquidos especiais e brasa podem ter azul, turquesa, vermelho, laranja, amarelo e verde intensos. Equilibrar com massas escuras e metal natural.
6. **Materiais distintos.** Madeira tem veios; metal tem reflexo estreito e rebites; couro tem costura/dobras; pedra tem estratos/fissuras; vidro tem borda e líquido; vegetação tem caule/folhas/nervuras.
7. **Pixel clusters, não ruído.** Cada pixel precisa representar luz, forma, corte, costura, fio, fissura, encaixe, reflexo ou textura real.
8. **Contornos materiais.** Usar grafite, marrom escuro ou azul petróleo conforme o objeto; evitar contorno preto igual em todo material.
9. **Não criar falso 3D.** Se a silhueta exige geometria, usar modelo JSON voxel com elementos reais, não apenas pintar uma textura no cubo.
10. **Não copiar sprites.** A referência do diretor é padrão de qualidade e contraste, não base para tracing de ícones de terceiros.

## 3. DIRETRIZES DE COR

Estas combinações são rampas de **material**, e não cores obrigatórias para todos os objetos:

| Família | Sombras | Base | Luz | Especular |
| --- | --- | --- | --- | --- |
| Madeira | #241820 | #81503A | #D8A36A | #F4CE82 |
| Latão | #25191C | #805029 | #DFA647 | #FFF6B8 |
| Vidro azul | #0D1B2C | #156A9E | #54CCE5 | #FFFFFF |
| Cristal teal | #0D1B2C | #087F93 | #6CEAF2 | #F1FFFF |
| Ardósia | #11151E | #393B50 | #77718A | #A6A0BB |
| Couro | #24191B | #964C33 | #E6AB69 | #F9D896 |
| Brasa | #1D121C | #BD2D19 | #FFA323 | #FFF07C |
| Vegetação | #0C251E | #329F44 | #B5E46E | #F5FDAC |

Usar aproximadamente 5–7 valores quando isso melhora a leitura, nunca como obrigação cega. Um pequeno highlight bem colocado vale mais que uma faixa inteira esbranquiçada. A paleta deve deixar aparente **qual material o jogador está vendo**.

## 4. ANTICRITÉRIOS — REPROVAÇÃO IMEDIATA

Reprovar se: depende do nome para ser reconhecido; usa apenas 2 tons próximos; é uma recoloração de outro item; usa blur, suavização ou downsample automático; mostra pixels aleatórios como textura; possui contorno gordo que engole o objeto; repete mesma paleta ciano em todas as famílias; faz metal parecer madeira/bege; faz vidro parecer pedra azul; apresenta bloco funcional como textura pintada num cubo genérico.

Não adianta passar testes técnicos e continuar parecendo placeholder. **Aprovação artística e aprovação de código são independentes.**

## 5. PROCESSO OBRIGATÓRIO

1. Inspecionar gameplay e necessidade do asset; definir material principal, cor e forma.
2. Produzir silhueta preta em 16×16 e verificar se é reconhecível.
3. Colocar sombra/meio-tom/luz sob uma direção coerente, formando volume.
4. Construir detalhes de material com clusters e 2–4 tons adicionais, sem ruído aleatório.
5. Verificar em tamanho nativo, 8× nearest-neighbor, fundos claro/escuro e interface de inventário.
6. Verificar a integração: PNG RGBA, largura/altura, CRC/chunks, modelos JSON, caminhos de textura.
7. Revisar em mundo real, frente/lado/cima/perspectiva para blocos, iluminação dia/noite e Survival.
8. Comparar antes/depois e conduzir teste de reconhecimento sem nomes.
9. Registrar a evidência e o que ainda depende de homologação humana.

O visual só é **aprovado** após teste humano no cliente. Build automatizado confirma compatibilidade estrutural, não qualidade artística final.

## 6. REVISÃO DE ASSETS — TASK 9.5.8

20 PNGs refeito em 15/11/2026, sem mudança de registro, loot, balanceamento, worldgen, receitas nem modelos 3D existentes:

- \`serene_vial\`: vidro transparente aparente com reflexo branco, rolha e líquido azul intenso.
- \`dream_thread\`: carretel de madeira, fios enrolados claros e fio solto.
- \`dormant_ember\`: carvão irregular com crosta escura, fissuras quentes e centro amarelo.
- \`dreamy_herb\`: folhas assimétricas, caule e nervuras em verdes profundos.
- \`dreamcatcher\`: aro trabalhado, rede fina e penas com contraste tonal.
- \`resonance_probe\`: empunhadura, ferragens em latão e cristal sensor distinto.
- \`marked_lining\`: peça de couro acolchoado, costurada, dobrada, reforçada.
- \`dream_journal\`: volume de livro com lombada, folhas e fecho.
- \`infusion_route\`: frasco de líquido âmbar, direção identificável no interior.
- \`mob_effect/route\`: rosa dos ventos brilhante em 18×18.
- \`deep_sleep_stone\`: estratos geológicos escuros e inclusões violetas.
- \`bedside_side\`, \`bedside_top\`, \`bedside_drawer\`, \`bedside_niche\`, \`bedside_metal\`: madeira quente com veios, encaixes, gaveta metálica e nicho profundo.
- \`trail_marker_side\`, \`trail_marker_top\`, \`trail_marker_brass\`, \`trail_marker_gem\`: pedra entalhada, placa direcional, latão e cristal turquesa facetado.

Não inventar mecânicas para justificar estética; geometria de cabeceira, apanhador e marco permanece a da 9.5.7. O escopo desta revisão é **exclusivamente textura**.

## 7. HOMOLOGAÇÃO EM CLIENTE

\`\`\`mcfunction
/gamemode creative
/give @s dreamsdimensions:serene_vial 1
/give @s dreamsdimensions:dream_thread 1
/give @s dreamsdimensions:dormant_ember 1
/give @s dreamsdimensions:dreamy_herb 1
/give @s dreamsdimensions:dreamcatcher 1
/give @s dreamsdimensions:resonance_probe 1
/give @s dreamsdimensions:marked_lining 1
/give @s dreamsdimensions:dream_journal 1
/give @s dreamsdimensions:infusion_route 1
/give @s dreamsdimensions:dream_bedside 1
/give @s dreamsdimensions:trail_marker 1
/give @s dreamsdimensions:deep_sleep_stone 16
/time set day
\`\`\`

Comparar iconografia em inventário/hotbar; colocar cabeceira/marco/apanhador e verificar os quatro ângulos e dois períodos. Verificar o HUD de \`route\` com Infusão de Rumo ativa.

**Não declarar inspeção dentro do Minecraft como feita sem captura real.**