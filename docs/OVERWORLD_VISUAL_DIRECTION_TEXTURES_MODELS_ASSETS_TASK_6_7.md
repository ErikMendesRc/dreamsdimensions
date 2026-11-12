# TASK 6.7 — DIREÇÃO VISUAL, TEXTURAS, MODELOS E ASSETS

## Art Bible de produção do DreamsDimensions — Overworld, conexões oníricas e escala visual

**Projeto:** DreamsDimensions — Minecraft 26.3 / NeoForge 26.3 / Java 25 / GeckoLib 5.  
**Natureza:** direção de arte + especificação técnica + matriz de assets; **somente documentação nesta Task**.  
**Data:** 12/11/2026. **Branch:** `migration/minecraft-26.3`. **HEAD auditado:** `34f9b18b4e26bce06142680c79c634d6d5212dec`.  
**Precedência:** [Game Design Bible 6.0](GAME_DESIGN_BIBLE_TASK_6_0.md), [Pilares 6.1](OVERWORLD_GAMEPLAY_PILLARS_TASK_6_1.md), [Recursos 6.2](OVERWORLD_RESOURCES_GEOLOGY_FLORA_BLOCKS_TASK_6_2.md), [Itens 6.3](OVERWORLD_ITEMS_CONSUMABLES_TOOLS_STATIONS_TASK_6_3.md), [Mobs 6.4](OVERWORLD_ECOLOGY_MOBS_HABITATS_TASK_6_4.md), [Estruturas 6.5](OVERWORLD_STRUCTURES_ENVIRONMENTAL_DISCOVERY_TASK_6_5.md), [Crafting 6.6](OVERWORLD_CRAFTING_PROCESSING_PROGRESSION_TASK_6_6.md), [Expansão 6.6.1](OVERWORLD_UNIVERSE_CONTENT_EXPANSION_TASK_6_6_1.md).  
**Entrega:** este documento com padrões globais, fichas B01–B30, I01–I18, POI-01–12, MOB-01–06, pipeline Blockbench/GeckoLib 5, matriz de produção, QA de performance e critérios de aprovação.  
**Não autorizado nesta Task:** criar/substituir PNG, JSON de models, animações, NBT, Java, receitas, blockstates, mobs, itens, texturas, shaders, sons ou ícones; não fazer merge na main.  
**Próxima etapa:** **6.8 — Economia, Raridade e Balanceamento** e depois **6.9 — Roadmap Executivo**.

> **Manifesto visual:** “No Overworld, o estranho é discreto e físico: a pedra registra, a planta reage, a ruína conta. Nos sonhos, a realidade se distorce. A arte deve comunicar essa diferença sem abandonar a gramática visual de Minecraft.” A escala do universo é ampla, mas a produção deve continuar por **fatias verticais jogáveis**, não por volume de imagens.

---

# 0. AUDITORIA DO REPOSITÓRIO — FATOS, NÃO SUPOSIÇÕES

## 0.1 Inventário de convenções já existentes

A árvore auditada contém **assets próprios** em `src/main/resources/assets/dreamsdimensions/`. Entre eles:

- **Blocos:** `blockstates/*.json`, `models/block/*.json` e `textures/block/*.png`, incluindo `dream_ore`, `deepslate_dream_ore`, `dream_infused_stone`, `oneiric_core_block`, `blue_dream_stone`, `dream_glow_moss`, `dream_flower_block` e superfícies/vegetação dimensionais.
- **Itens 26.3:** definição de cliente em `items/*.json` **E** modelo geométrico em `models/item/*.json` **E** `textures/item/*.png`. Exemplo verificado: `items/dream_necklace.json` referencia `dreamsdimensions:item/dream_necklace`; `models/item/dream_necklace.json` usa `minecraft:item/generated`.
- **Blocos 26.3:** exemplo `blockstates/dream_ore.json` escolhe `models/block/dream_ore.json`; seu modelo usa `minecraft:block/cube_all` e textura `dreamsdimensions:block/dream_ore`.
- **Mobs GeckoLib existentes:** `geckolib/models/entity/fragmento_errante.geo.json`, `vigia_adormecido.geo.json`, animações `geckolib/animations/entity/*.animation.json` e texturas `textures/entity/*.png`.
- **Geometria real:** os dois `.geo.json` atuais declaram atlas **128×128**, geometria `format_version:1.12.0`; animações atuais usam `format_version:1.8.0`. **Isso descreve os assets existentes, não exige 128×128 para todo mob novo.**
- **Animação atual:** Fragmento possui idle flutuante com root e partes móveis; Vigia possui sequência de estados própria. Não usar os mesmos clips/pivôs por cópia em qualquer criatura nova.
- **Locale:** `lang/pt_br.json` e `lang/en_us.json` já existem.
- **Framework:** Task 4.4 declara GeckoLib 5.5.7, sistema de AI/movement reutilizável e controllers por espécie. Reaproveitar padrões de arquitetura quando adequados; não replicar voo de ilha em besouro de solo.

**Limite da auditoria:** nomes/caminhos/model JSON foram conferidos; a inspeção documental NÃO constitui verificação humana de cada pixel dos PNGs, densidade real, bordas ou qualidade visual in-game. A Task de implementação deve renderizar e avaliar os arquivos existentes sob condições controladas **antes** de qualquer repintura.

## 0.2 Mapa de compatibilidade

| Conteúdo | Pipeline recomendado | Motivo |
|---|---|---|
| Bloco sólido simples | Textura PNG 16×16 + block model vanilla JSON + blockstate | Baixo custo, coerência vanilla |
| Escada/laje/muro | Texturas familiares + modelos/blockstates nativos | Conexão, estado e culling sem animação |
| Planta/cobertura | PNG RGBA + modelo crossed/cutout conforme tipo e suporte | Silhueta legível, alpha controlado |
| Bloco com estado físico | Variantes blockstate + modelos/tex para estados visíveis | Rachadura, segurança e sinal comunicados |
| Item plano | PNG + `models/item` + `items` 26.3 | Cliente e inventário consistentes |
| Item com forma 3D realmente necessária | Modelo item JSON custom + displays, se A/B justificar | Sem transformar qualquer item em GeckoLib |
| Mob vivo | Blockbench GeckoLib 5 + geo/animation/texture + renderer | Animações com bone/pivot estáveis e estados de IA |
| Estrutura | NBT vanilla blocks + arquitetura/teste de terreno | Estrutura não é uma malha FBX/GLB no worldgen |
| Efeito contextual | Estado de bloco/particle vanilla reaproveitado antes de shader novo | Legibilidade e custo de GPU/CPU |

**Regra:** GeckoLib não é necessário para bloco estático, escada, laje, muro ou item plano. A tecnologia deve corresponder ao problema de renderização/anim.

---

# 1. IDENTIDADE VISUAL — A GRAMÁTICA DO UNIVERSO

## 1.1 Quatro territórios visuais (preservar o que já está publicado)

| Território | Assinatura | Paleta proposta de referência | Composição | Proibição |
| --- | --- | --- | --- | --- |
| Overworld — vestígios | cinzas quentes, tufo, ardósia, cobre envelhecido, verde-sálvia | base #72716B / #565C5A, luz #B5AB94, metal #8D745D, vegetal #6B8068, marca #A4A59A | terroso/mate, interferência sutil, silêncio visual entre pontos de interesse | nenhuma superfície totalmente azul neon |
| Campo Onírico Azul — existente | rochas azuladas, vegetação e luminosidade frias | referência conceitual azul-sombra #456B91, azul-luz #79A8C2, névoa #B4C9D6; NÃO substituir cores atuais | surreal calmo, silhuetas amplas e materiais próprios | não transferir todas as cores para Overworld |
| Dreamscape/Sonho Fragmentado — existente | rochas e ilhas fragmentadas, vazio e contraste de arestas | referência conceitual pedra #55536E, violeta seco #87799D, brilho pontual #B5A9CD; NÃO substituir cores atuais | fragmentação, bordas quebradas, assimetria e deslocamento físico | não recolorir ilha inteira com a paleta azul |
| Ressonância Overworld — somente conceito | metal oxidado, sulcos cinza azul, pulso mínimo | metal #997B5F, fosco #505F66, sinal #78A9B1; não é paleta do Dream Ore | indicador de circuito, não sinalização mágica universal | proibido criar emissivo antes de mecanismo aprovado |

**Cores em hexadecimal são referências para concept art**, não valores já extraídos dos arquivos do jogo, nem substituição de texturas existentes. Color matching verdadeiro deverá vir de uma **paleta amostrada nos PNGs atuais** em uma futura auditoria de imagem.

## 1.2 Cinco princípios de arte

1. **Vanilla-first sem aparência genérica:** pixels agrupados, volumes legíveis, bordas contidas, densidade próxima à de Minecraft. Identidade própria nas juntas, direção de veios, formas e estados; não copiar bloco vanilla e mudar saturação.
2. **Overworld sutil, sonhos extraordinários:** os materiais do Overworld precisam combinar com pedra, tuff, copper e madeira normal. Brilhos azuis/violetas fortes ficam majoritariamente nas dimensões oníricas ou em momentos de gameplay que realmente os justificam.
3. **Silhueta antes de textura:** mob, item e ruína devem ser reconhecíveis com a cor removida. Se duas entidades só diferem em tom, redesenhar shape.
4. **Informação antes de decoração:** perigo tem aviso, receptor tem orientação, planta apresenta estado, geologia mostra camada real. Cor/som/partícula **nunca** são o único canal da pista essencial.
5. **Variedade com parentesco:** cada novo material tem sua família visual; repetir regras de pedra/vegetação/desgaste. Evitar que cada POI tenha paleta completamente independente.

## 1.3 Hierarquia de contraste

| Papel | Frequência e tratamento | Exemplo |
|---|---|---|
| **Matéria comum** | 70–80% da composição como intenção artística, não percentual medido | Rochas foscas, madeira e solo |
| **Material com marca** | Aparecimento localizado e reconhecível | Pedra Marcada, Líquen em habitat |
| **Interação/risco** | Contraste funcional, forte apenas onde ação exige | Rachadura crescente, orientação de Emissor |
| **Momento anômalo** | Excepcional, curta duração e ligado a estado real | Pulso na rede B21–B25, se aprovada |

Proporção é **guia composicional**, não regra de RNG de worldgen. Imagens-conceito devem respeitar uma paisagem majoritariamente vanilla. Não tornar cada caverna um bioma azul neon.

## 1.4 Materiais e padrões

- **Pedra natural:** contraste angular, fragmentação geológica, planos de luz com alguns grupos maiores; nunca ruído de pixel aleatório distribuído igualmente.
- **Pedra lapidada:** faces menos irregulares, junta mais reta, coerente com fonte geológica.
- **Cobre/metal:** uso estrutural, oxidado/desgastado, luz especular sugerida por pixels; não texturizar como aço polido PBR realista.
- **Vegetação:** margens translúcidas/cutout intencionais, repetição controlada e sombra por pequenos grupos de cor; evitar borda com halo branco.
- **Tecnologia eco:** sulcos e conexões **físicas**, estados ligados a redstone. Não campo luminoso etéreo que funciona sem motor.
- **Ruínas:** desgaste nas junções e pontos de apoio, não toda face quebrada por igual; a arquitetura precisa ter sido construída antes de ruir.

---

# 2. PADRÕES DE TEXTURA E PIXEL ART

## 2.1 Resoluções e interoperabilidade

**Blocos e itens de primeira produção: 16×16 PNG**, seguindo a densidade base vanilla. **32×32 apenas como alternativa consciente** para itens com detalhe funcional indispensável e teste visual comparativo; **não** iniciar projeto com 64×64 em todos os blocos porque parece “HD”. Minecraft escala pixels; ampliar para 32 altera a densidade aparente e deve ser decisão artística global, não acidental.

**Entidades:** atlas depende do UV/complexidade do modelo. Modelos existentes usam **128×128**; para novos mobs leves testar atlas 64×64 ou 128×128 e selecionar o menor legível, com custo adequado. O número de pixels do atlas não é densidade de pixels do bloco.

| Classe de asset | Base | Quando fugir | O que validar |
|---|---|---|---|
| Bloco full cube | 16×16 por face | Só quando necessário à família | Seams, tiling 3×3, culling, mipmaps |
| Planta | 16×16 com alpha | Se volume/estados pedirem variação | Silhueta sobre stone/dirt/noite |
| Item plano | 16×16 | 32×32 com legibilidade demonstrada | Inventário, hotbar, mão, GUI scale |
| Mob pequeno | 64×64 atlas inicial **candidato** | 128×128 se UV/ossos exigirem | UV, seam, animação e preço de draw |
| Mob médio | 128×128 inicial **candidato** | Menor se possível | UV e presença em 2–8 exemplares |
| POI | Texturas de blocos; NBT | Sem imagem 3D única | 4 lados, interior e terreno |
| Partícula | Sprite curta 8×8/16×16 candidata | Se efeito legível exigir mais | Contraste, overdraw, TPS |

**Nunca:** interpolar/blur anti-aliased numa textura voxel 16×16 sem decisão específica; screenshots podem usar filtro do jogo, mas os fontes PNG devem permanecer nítidos. Evitar texturas fotográficas, gradientes suaves, chromatic aberration, bloom obrigatório e normal maps/PBR como dependência de estilo.

## 2.2 Linguagem de clusters

- Criar **grupos grandes e pequenos de pixels** que sugerem rachadura, porosidade, fibra, folhagem; evitar dithering uniforme e ruído branco.
- Limitar cores por material segundo necessidade (guia inicial: ~5–9 tons principais para uma face; variações são permitidas após revisão). Não transformar isso em teste automático ingênuo que rejeita transparências.
- Definir **um eixo de iluminação perceptual** consistente com vanilla para itens e mobs; texturas de blocos não devem fingir uma luz solar fixa incompatível com mundo giratório.
- Na borda de tile full cube, prever continuação do padrão ou término deliberado; testar mosaico 3×3 e rotação de modelos.
- Rachaduras de risco B17 precisam de **diferença topológica** entre estados, não só cor vermelha: aumento de espessura, número e continuidade.
- Em plantas, 1–2 pixels isolados só se contribuírem à silhueta; caso contrário causam flickering em movimento/distância.
- Exportação com canal alpha verdadeiro, sem halos de edição, bordas sem coloração residual, transparência conforme modelo/render type.
- Testar em fundos claro/escuro e com distância, sem confiar na imagem de 400% zoom do editor.

## 2.3 Mapas de estado

| Família | Estado visual mínimo | Quando usar emissive |
|---|---|---|
| Pedra Marcada | natural / lapidada; sem animação | **Não** |
| Líquen | presença de faces/suporte; crescimento quando mecanicamente real | Em regra **não** |
| Raiz Sensível | aberta / retraída | Não |
| Flor de Vigília | fechada / aberta; luz se gameplay permitir | Apenas se apoiado por render e clareza |
| Rocha Cediça | íntegra / aviso / prestes a ceder ou estados equivalentes | **Não**; risco por geometria/rachadura |
| Pedras de Eco | desconectada / conectada; estado de pulso transitório | Somente durante pulso aprovado, sem iluminação global falsa |
| Lâmpada Sintonizada | desligada / pulso aceso | Emitir luz do **bloco** só se mecânica suportar state/light-level; emissive visual não substitui iluminação real |
| Emissor/Receptor | faces de input/output e direção | Não necessário |
| Painel de Relevo | padrão específico e rotação | Não |

**Atenção:** translucência/alphablend, cutout e render state de itens evoluíram no MC/NeoForge recente. Definir render paths pelo **código da versão compilada**, não copiar `RenderType` ou `ItemProperties` de tutoriais antigos.

---

# 3. FICHAS VISUAIS DO CATÁLOGO EXPANDIDO — B01–B30

**Estes são requisitos de conceito**, não fila de 30 PNG para produzir de uma vez. “PROTOTYPE” = artes de graybox/exploração, sem registry final; “CONCEPT” = desenho e referência, sem fonte de produção. “FIRST” = priorizado para a primeira fatia funcional após gates.

| ID | Bloco | Prioridade visual | Base | Direção de forma e textura | Teste de legibilidade |
| --- | --- | --- | --- | --- | --- |
| B01 | Pedra Marcada | F0 / FIRST | 16×16 | base stone/tuff cinza-quente, veios marcados oblíquos descontínuos; contraste de textura antes de brilho | sem traço de minério e sem fluorescência |
| B02 | Pedra Marcada Lapidada | F0 / FIRST | 16×16 | mesmo mineral B01, superfície cortada com borda geométrica e núcleo legível | distinguir rough/cut a 6 blocos |
| B03 | Escadas Marcadas | AFTER F0 | herda B02 | herdar side/top/bottom; degraus com padrão UV contínuo | não usar textura nova obrigatória |
| B04 | Lajes Marcadas | AFTER F0 | herda B02 | metade da altura e topo coerente, textura de corte alinhada | double slab e UV testados |
| B05 | Muro Marcado | AFTER F0 | herda B01/02 | post/side conexos, leitura de juntas sem linhas quebradas | conexões e culling |
| B06 | Tijolos Marcados | CONCEPT | 16×16 | linhas de alvenaria deslocadas e irregularidade medida | não imitar stone bricks com cor diferente |
| B07 | Tijolos Fraturados | CONCEPT | 16×16 | variação estrutural de B06 com quebra plausível de canto/junta | fratura não vira ruído visual uniforme |
| B08 | Painel de Relevo | PROTOTYPE M02 | 16×16 + modelos por estado | sombra rasante revela sulcos largos com orientação cardinal clara | padrão legível em normal/oblique |
| B09 | Coluna Estratificada | CONCEPT | 16×16 (side/top) | camadas horizontais orientadas e topo com estratos reconhecíveis | construção não aparenta minério |
| B10 | Líquen dos Vestígios | F1 / FIRST | 16×16 RGBA | pequenos nós achatados verde-sálvia, contorno quebrado, faces finas; baixo brilho natural | não recolorir glow lichen/dream_glow_moss |
| B11 | Hera do Repouso | PROTOTYPE M03 | 16×16 RGBA + states | folhas estreitas em cadeia vertical e pontos de ancoragem aparentes | silhueta própria vs vines vanilla |
| B12 | Raiz Sensível | PROTOTYPE M04 | 16×16 + estados | filamentos curtos em leque; pose aberta/retraída visível sem partículas | estado perceptível em silêncio |
| B13 | Flor de Vigília | PROTOTYPE M04 | 16×16 + 2 estados | pétalas angulares cor marfim-âmbar; aberto/fechado é alteração de silhueta | luz condicional clara sem aura gigante |
| B14 | Fungo de Penumbra | PROTOTYPE M04 | 16×16 + estados | chapéu em camadas, borda ferrugem-violeta apagado, micélio claro | não copiar formato do mushroom vanilla |
| B15 | Sedimento de Marcas | PROTOTYPE M02 | 16×16 | camada fosca poeirenta, grânulos grandes e padrão parcialmente exposto | escovação legível e não igual suspicious gravel |
| B16 | Estrato Exposto | PROTOTYPE M01 | 16×16 + eixo | faixas com espessura variável, orientação de camada visível em top/side | informação correlacionada com terreno |
| B17 | Rocha Cediça | PROTOTYPE M03 | 16×16 + estados | rachaduras em três estados com alto contraste e geometria plausível | aviso pré-queda sem depender de cor |
| B18 | Rocha Escorada | PROTOTYPE M03 | 16×16 | mesma base de B17 com trava estrutural escura, sem fratura ativa | diferença entre seguro/perigoso a 5 blocos |
| B19 | Escora de Campo | PROTOTYPE M03 | 16×16 + modelo simples | braços de madeira/cobre com apoio triangular reconhecível | não invadir hitbox de passagem |
| B20 | Passarela Recuperada | CONCEPT | 16×16 | tábuas remendadas e travessas cruzadas com sistema de apoio coerente | não duplicar oak planks por cor |
| B21 | Nódulo Reverberante | CONCEPT ONLY M06 | 16×16 | inclusões minerais em fissuras de pedra, reflexos discretos azul-acinzentados, pulso pontual | não parecer minério de Dream Dust; não texturizar antes de M06 |
| B22 | Pedra Condutora de Eco | PROXY FIRST M06 | 16×16 + conexões | canal talhado físico nas bordas e centro indicando rede real | conexões face a face, sem linha mágica longa |
| B23 | Emissor de Eco | PROXY FIRST M06 | 16×16 + orientação | placa de cobre oxidado com entrada ampla e glifo de lançamento | frente/trás e origem legíveis |
| B24 | Receptor de Eco | PROXY FIRST M06 | 16×16 + orientação | funil/anel de recepção com glifo diferente do emissor | não diferenciar apenas pela cor |
| B25 | Lâmpada Sintonizada | PROXY FIRST M06 | 16×16 + estados | lente mineral protegida por aro e estados apagado/pulso | sem fullbright global; medir luz vs render |
| B26 | Leitor de Colônia | CONDITIONAL 6.3 | 16×16 + estados | placa botânica com quatro marcas/sensores nos lados | sinal analógico legível mesmo sem partículas |
| B27 | Lanterna Botânica | CONCEPT | 16×16 + modelos | cúpula vegetal construída, luz suave de estado mensurável | não apenas lanterna vanilla recolorida |
| B28 | Placa de Sondagem | CONCEPT | 16×16 + 2 slots | dois nichos de comparação com signos visuais simples | render/GUI só se interação real exigir |
| B29 | Relevo Gravado | PROTOTYPE M02 | 16×16 + estados | painel com padrão gravado e orientação modular | decoração reproduzível no mundo |
| B30 | Mosaico de Camadas | CONCEPT | 16×16 | padrão late de pavimento com estratos e poucos contrastes | sem minerio/cristal late obrigatório |

## 3.1 Família F0 — entregáveis efetivos da primeira fatia

Quando F0 estiver autorizada em PR de implementação, produzir **somente**:
1. Concept board com Pedra Marcada em caverna vanilla e material comparativo (stone/tuff/deepslate).
2. `marked_stone.png`, `cut_marked_stone.png` em 16×16, com top/side separados **apenas se a direção da matéria exigir**.
3. `models/block`, `blockstates`, `items`, `models/item` e loot/tags/strings conforme registro final; nesta Task não criar arquivos.
4. Mockup de estrutura Marco com materiais atuais, **sem exigir** B03–B09 para testar F0.
5. Teste de 3×3 tiling, parede em luz natural, caverna e inventário.

**Não produzir** escada/laje/muro/tijolos antes de provar paleta e mineração natural. Isso evita refazer dez assets após ajustar B01.

## 3.2 Família F1 — entregáveis efetivos da primeira fatia

Após definir suporte e crescimento:
- Comparar F1 com `minecraft:glow_lichen`, `minecraft:vine` e `dreamsdimensions:dream_glow_moss` existente **em screenshots**, para assegurar distinção.
- Construir sprite 16×16 RGBA com silhueta que funcione em stone/tuff/dirt, seco/úmido, luz alta/baixa e câmera móvel.
- Não usar verde de highlight que pareça goela de creeper nem colocar a cor azul de todas as dimensões no Overworld.
- O Líquen deve continuar **visível sem partículas**. Visual de crescimento não pode prometer taxa de cultivo impossível em servidor.
- Critério de arte: uma colônia de 16 blocos parece um habitat, não uma grade de cópias exatamente iguais.

---

# 4. FICHAS DE ITENS, FERRAMENTAS E CONSUMÍVEIS — I01–I18

**Regra do inventário:** identificar função por silhouette a 16 px, sem depender de nome. Um item funcional pode usar item model JSON vanilla gerado; não instalar renderer complexo por estética.

| ID | Item | Prioridade | Tamanho candidato | Silhueta e material | Critério de descarte |
| --- | --- | --- | --- | --- | --- |
| I01 | Argamassa de Marcas | CORE CRAFT | 16×16 | pequeno bloco/agregado de massa arenosa com folhas presas; não frasco mágico | diferenciar Dust no inventário |
| I02 | Cinzel de Marcas | CONDITIONAL | 16×16 ou 32×32 aprovado | haste curta com ponta quadrada e cobre envelhecido, silhueta visível a 16px | cabeça/ponta claramente distinguíveis de pickaxe |
| I03 | Molde de Relevo | PROTOTYPE M02 | 16×16 | placa rígida com recorte em baixo relevo e encaixe lateral | padrão aplicado identifica estado, não só cor |
| I04 | Kit de Escoramento | PROTOTYPE M03 | 16×16 | abraçadeira/peça triangular portátil com travessa de madeira | deve parecer reparo físico, não poção |
| I05 | Ponteiro de Fissuras | CONCEPT COMPETING | 16×16 | haste curta de inspeção de trinca, sem cristal GPS | preferir rachadura do próprio bloco se possível |
| I06 | Sonda de Estratos | PROTOTYPE M01 | 16×16 + opcional modelo handheld | sonda com dois pontos de contato, cobre e pedra gravada | não duplicar lente; nenhuma antena radar |
| I07 | Caderno de Campo | CONCEPT | 16×16 | caderno de couro simples com marca geométrica discreta | não modelar se advancement bastar |
| I08 | Lente de Marcas | COMPETING I06 | 16×16 | lente em moldura mineral, escala de inspeção curta | fundir em I06 se mesmo uso |
| I09 | Fibra de Hera | CONDITIONAL B11 | 16×16 | feixe vegetal amarrado sem efeito mágico | não existir sem Cordame |
| I10 | Cordame de Hera | CONDITIONAL B11 | 16×16 | corda torcida grossa e nó reconhecível | não copiar lead vanilla sem função extra |
| I11 | Selo de Cultivo | CONCEPT | 16×16 | plaqueta simples de canteiro sem HUD | evitar se proteção da matriz for UX suficiente |
| I12 | Pigmento de Penumbra | CONCEPT | 16×16 | placa pigmentada/pequeno saco, cor natural escura | diferenciar dos dyes por aplicação real |
| I13 | Carga de Luz Botânica | CONCEPT | 16×16 | inserto orgânico com luz apagada/ativa sutil | não criar recarga sem propósito |
| I14 | Fragmento Reverberante | BLOCKED M06 | 16×16 | fragmento em lâminas achatadas cinza-azul, não pó azul | somente se nódulo e consumidores aprovados |
| I15 | Diapasão de Eco | PROXY FIRST M06 | 16×16 ou 32×32 | diapasão angular em cobre e material de canal | diagnóstico visível sem UI complexa |
| I16 | Bobina Sintonizada | CONCEPT | 16×16 | peça enrolada em aro com contatos metálicos | não criar componente de um craft sem 2 usos |
| I17 | Matriz de Sintonização | CONCEPT COMPETING | 16×16 | ferramenta de ajuste mais fina, silhueta diferente do Diapasão | eliminar se click no bloco basta |
| I18 | Marcador de Campo | CONCEPT COMPETING | 16×16 | pequena placa reutilizável com padrão geométrico | cancelar se tocha/placa/mapa bastam |

## 4.1 Itens existentes: continuidade, não redesign automático

`dream_dust`, `dream_necklace`, `oneiric_awakener` têm PNG/modelos próprios e estão ligados a mecânicas sensíveis de exploração. Esta 6.7 **não autoriza redesenhar** esses assets para adequá-los à nova paleta. Em uma auditoria futura, colocar ao lado de B01/F1 e verificar se a iconografia informa a distinção entre Overworld e sonho. Só propor revisão após comparar screenshots e garantir que itens persistentes continuem reconhecíveis.

## 4.2 Família de ferramentas

- **Cinzel:** leitura de ponta e cabo; não parecer picareta de outro tier.
- **Molde:** pequena placa, recorte físico amplo, sem letras minúsculas ilegíveis.
- **Escora:** objeto de montagem/apoio, não garrafa de poção.
- **Sonda:** dois pontos de comparação visíveis; não antena que sugere radar de minério.
- **Diapasão:** duas hastes e contato local de engenharia; não bastão mágico de teleporte.
- **Ícones condicionados:** I05/I07/I08/I11–I13/I16–I18 **não devem ir à produção** até vencerem comparação com vanilla e outras ferramentas.

---

# 5. DIREÇÃO DE MOBS: SILHUETA, VOXEL, UV E ANIMAÇÃO

## 5.1 Regra para não "viajar" no design

A fauna do Overworld será **biologicamente plausível no vocabulário voxel**: membros funcionais, centro de massa, pequenas deformações estilizadas, movimento com desaceleração, estados claros e escala compatível com o Minecraft. Nenhum monstro flutuante de cristal por padrão. Os seres oníricos existentes continuam reservados a suas dimensões e não são recortados/recoloridos para o Overworld.

**Medidas nas fichas são proporções artísticas de conceito, NÃO dimensões de hitbox ou valores de `EntityDimensions` já aprovados.** Calibrar geometry/scale depois da AI e spawn.

| ID | Mob | Estado | Proporção/silhueta conceitual | Clips e linguagem corporal | Gate |
| --- | --- | --- | --- | --- | --- |
| MOB-01 | Mariposa dos Vestígios | AFTER F1 / FIRST MOB | 0,45–0,7 bloco visual; cabeça pequena, tórax compacto, 2 asas grandes com recortes diferentes | perch_idle → takeoff → flight → land → startled; asas não 'batem quadrado' estáticas | F1 natural; 2 poses reconhecíveis em monocromia |
| MOB-02 | Habitante das Fendas | MID CONDITIONAL | 0,8–1,2 blocos; corpo baixo e robusto, 4 apoios pesados, placas de rocha quebradas | idle → walk → alert → defend → recover; alerta altera altura/largura | território legível; sem zumbi com pedra |
| MOB-03 | Guardião de Pedra | POI-04 BLOCKED | 1,6–2,3 blocos; massa concentrada em ombros/escudos, silhueta defensiva, sem espada gigante | dormant → awaken → windup → attack → recover → reset; tell antes de hit | bypass sem combate; sem bossbar |
| MOB-04 | Observador Esquecido | DEFERRED | sem model sheet, sem rosto/civilização canonizados | nenhum clip autorizado até lore e função | não usar humanoide decorativo sem comportamento |
| MOB-05 | Besouro das Camadas | M02 CONDITIONAL | 0,35–0,6 bloco; casco em três volumes, antenas curtas, seis patas estilizadas | idle → crawl → inspect → hide; direção por textura do sedimento | pista de sedimento real, sem item-drop obrigatório |
| MOB-06 | Grilo de Eco | M06 CONDITIONAL | 0,3–0,5 bloco; pernas traseiras triangulares e cabeça inclinada, asas pequenas | idle → stridulate → hop → settle; sinais visuais junto a áudio | som só onde B21 é real, evitar radar falso |

## 5.2 Mariposa dos Vestígios — especificação de Blockbench prioritária

**Referência funcional:** inseto pequeno de corpo compacto, cabeça voltada a superfícies, duas asas grandes angulosas e ligeiramente assimétricas; sem rosto antropomórfico. Cor principal argila/oliva desbotada, marcas claras pontuais na asa, duas antenas discretas. Deve parecer **do Overworld**, não cópia miniatura do Fragmento Errante.

**Topologia sugerida (bone tree conceitual, ajustar pivôs em Blockbench):**
```text
root
 ├─ body
 │   ├─ thorax
 │   ├─ abdomen
 │   └─ head
 │       ├─ antenna_left
 │       └─ antenna_right
 ├─ wing_left
 ├─ wing_right
 ├─ legs_front
 ├─ legs_middle
 └─ legs_back
```

**Clips de contrato:** `perch_idle` (asas em repouso e micro oscilação), `takeoff` (preparação + primeiro batimento), `flight` (ciclo contínuo, elevação sutil coerente com AI), `land` (desaceleração e fechamento), `startled` (reação curta e fuga). Nomes finais do renderer/controller devem seguir classe implementada, não copiar prefixo `animation.fragmento_errante`.

**Atenção importante:** asas GeckoLib animadas **não são física de movimento**; a AI/move control movimenta a entidade. Sem sincronizar velocidade/estados, a Mariposa pode ficar batendo asas parada, pousar flutuando ou deslizar sem bater. O mundo de F1 e a IA devem primeiro existir; **Blockbench não define o spawn**.

**Teste técnico do modelo:** pose idle/pouso, voo frontal/lateral/traseiro, sombras, clipping de asas com bloco, hitbox em parede/teto, distância com 2/4/8 entidades, pausa/reload, animação local e estado de servidor.

## 5.3 Habitante das Fendas

Silhueta baixa e robusta, peso nas patas, placas naturais com sobreposição irregular, cabeça adaptada a cavernas; olhos pequenos, nenhuma caveira humana ou armadura de jogador. **Alerta muda postura** antes de perseguir; wind-up é visível mesmo sem áudio. Idle não é bobbing de voo; walking transfere peso lateral/frontal, pés não atravessam o chão. Mobs vanilla ao redor devem continuar identificáveis.

## 5.4 Guardião de Pedra

Somente condicionado à Câmara: robusto, defensivo, ombros/protetores que explicam território; sem parecer Ender Dragon em miniatura ou boss obrigatório. Idle dormindo integrado à arquitetura, `awaken` claro, `windup` que anuncia golpe, `recover` que permite fuga, `reset` sem teleportar gratuitamente. Deve caber no POI com raio e altura de rota alternativa. Não criar modelo final enquanto POI-04 está **BLOCKED_BY_REWARD_DESIGN**.

## 5.5 Besouro e Grilo

Ambos são **candidatos**: Besouro precisa de sedimento real/arqueologia, Grilo depende de nódulo real/ressonância. Direção voxel insetóide pequena, mas silhuetas **não confundíveis**: besouro arredondado e baixo com casco segmentado; grilo angular com pernas traseiras elevadas. Nenhum dos dois ganha destaque de 128×128 ultra detalhado sem necessidade.

## 5.6 Padrão GeckoLib 5 / Blockbench

- Criar projeto **`GeckoLib Animated Model`** no Blockbench, com plugin GeckoLib correto; `.bbmodel` é o **fonte editável** de autoria (se novo, versionar sob `art_sources/blockbench/` ou local definido pela 6.9).
- Nomes de bones sem espaço/acentos; hierarquia lógica root/body/heads/limbs; pivôs na articulação anatômica, não no centro de todas as peças.
- Preparar UV antes de texturizar; textura atlas apenas do tamanho realmente necessário; evitar faces invisíveis com UV desperdiçado.
- Usar cubes de volume coerente: assimetrias controladas, antenas/pernas apenas quantas são legíveis; evitar cubos minúsculos em excesso que geram flicker.
- Animar `idle`, `walk/flight`, `react/alert`, `attack` somente quando requerido pela AI; clips não utilizados **não** são item de checklist para “encher animação”.
- Loops precisam começar e terminar com estado compatível; ações não-loop precisam ter transições verificáveis. Sem keyframes inúteis em 60 bones.
- Exportar `.geo.json`, `.animation.json`, `.png` nos caminhos que a classe GeoModel/renderer **real** utiliza; não assumir convenção antiga `geo/` ou `animations/` da GeckoLib 4.
- GeckoLib não deve comandar movimento/world collision; sincronizar estados de AI, velocidade e animação com controllers de cliente, evitando excesso de dados de rede.
- Testar resource reload, spawn natural/ambiente, overlay de dano, sombra, culling, altura da textura, multiplayer e servidor dedicado.

---

# 6. ARQUITETURA VISUAL DAS 12 ESTRUTURAS

**Identidade arquitetônica central:** ruínas de trabalho/observação/manutenção — contrafortes, pilares, vias de acesso, materiais de chão, marcas de erosão, objetos usados. Não castelo medieval genérico com um baú, nem nave de ficção científica no Overworld.

**Cores e materiais:** stone/tuff/andesite/deepslate/wood/copper vanilla como maioria; B01, plantas e eventuais blocos técnicos aparecem somente quando a mecânica/POI pede.

| ID | Estrutura | Produção | Silhueta e organização | Gate visual/QA |
| --- | --- | --- | --- | --- |
| POI-01 | Marco de Pedra Marcada | READY FOR BLOCKOUT F0 | 2–3 pedras verticais assimétricas, base em afloramento; focal point é B01, sem chest | legível a ~20 blocos em terreno real; apoio total |
| POI-02 | Abrigo de Campo | READY FOR BLOCKOUT | cobertura horizontal quebrada, duas entradas, bancada vanilla, armazenamento integrado | leitura frontal e lateral, segurança e uso físico |
| POI-03 | Galeria Afundada | BLOCKOUT WHEN M03 | arco de mina na fissura, contrafortes, dois percursos com água contida | entrada acessível, rota A/B e saída |
| POI-04 | Câmara do Limiar | CONCEPT ONLY | núcleo volumétrico de engenharia antiga, navegação em anel e 2 vias | recompensa funcional ainda bloqueada |
| POI-05 | Pedreira Abandonada | BLOCKOUT PROTOTYPE | bancadas talhadas em rocha natural, travessas de madeira, caminho de recuperação | não parecer quarry vanilla com baú |
| POI-06 | Jardim de Penumbra | BLOCKOUT F1 | câmara orgânica pequena com bolsões úmidos, arcos de vegetação e passarela discreta | mudas coletáveis, fonte preservada |
| POI-07 | Estação de Sondagem | CONCEPT M01 | posto baixo na encosta, marcas de amostragem e dois pontos visuais separados | comparação sem scanner GPS |
| POI-08 | Passarela Rompida | BLOCKOUT M03 | dois apoios naturais visíveis e trecho central interrompido, pelo menos um desvio | piso real e queda avisada, sem softlock |
| POI-09 | Poço de Camadas | CONCEPT M01 | abertura estreita, nichos de rocha alternados em seção vertical | jogador vê retorno e níveis |
| POI-10 | Arquivo de Relevos | CONCEPT M02 | parede de registros com sulcos largos e luz lateral, circulação curta | padrão interpretável e copiável |
| POI-11 | Observatório de Vigília | CONCEPT M04 | plataforma de pedra discreta com vista para jardim/estado de luz | não depender de shader/horário irreal |
| POI-12 | Casa de Ecos | BLOCKED M06 | estrutura compacta demonstrando entrada, canal e receptor em parede sólida | não construir NBT se circuito falhar A/B |

## 6.1 Biblioteca modular de peças vanilla-first

**Kit K-Rocha:** pisos, cantos, duas larguras de paredes, bases, parapeitos, escadas, pilares e ruínas parciais em stone/tuff/andesite. Não criar um block variant novo só porque precisa de uma pedra em ângulo.

**Kit K-Trabalho:** mesa vanilla, bancada, support beam, contraforte, depósito, escoras, passarela, trilho simples, foco de armazenamento. Símbolos e desgaste posicionados coerentemente com uso.

**Kit K-Ecologia:** pedra úmida, pequena terra, folhas, cogumelo e Líquen (quando disponível), sem invadir toda a estrutura com colônia exuberante.

**Kit K-Relevo:** painéis e marcas de amostragem, apenas quando M02/POI-10 aprovados; padrões grandes o bastante para ler sem zoom extremo.

**Kit K-Eco:** emissor/receptor/condução apenas quando M06 provar capacidade; POI-12 não ganha tratamento audiovisual antes disso.

**Rota de produção de NBT:** diagrama de plano → blockout in-game com materiais vanilla → teste de duas abordagens/entrada/saída → acabamento técnico → export NBT → inspeção de rotação/terrain → screenshots 4 lados + interior + distância → testes multi-seed. Não “fazer estrutura” como uma imagem de castelo e tentar converter screenshot para NBT.

## 6.2 Distâncias de leitura como hipótese artística

- **Macro (40–80 blocos em terreno aberto):** distinguir família pela silhueta quando o bioma e visibilidade permitirem. **Não é raio de spawn nem garantia de sightline.**
- **Meso (12–30 blocos):** identificar entrada e contraste de suporte/ruína.
- **Micro (2–8 blocos):** ler rachadura, alavanca, material, padrão e recompensa.

Esses intervalos são **heurísticas para screenshot/QA**, não medições reais ou exigência de visibilidade através de floresta/caverna. Para POIs subterrâneos, a distância relevante é da **entrada visível**, não do núcleo enterrado.

## 6.3 Quatro problemas a evitar

1. **Baú central de prêmio** como único objetivo e toda estrutura geométrica idêntica.
2. **Pilares flutuantes** ou ruína cortada por terreno após worldgen.
3. **Metade do NBT com blocos do mod ainda não registrados** (dependência de assets fictícios).
4. **Material excessivamente brilhante** que descaracteriza o Overworld ou sugere loot premium universal.

---

# 7. COMO PRODUZIR ASSETS DE MODO REUTILIZÁVEL

## 7.1 Convenções reais e futuras

**Paths reais existentes:**
```text
src/main/resources/assets/dreamsdimensions/
  blockstates/*.json
  items/*.json                         # client item definitions 26.3
  models/block/*.json
  models/item/*.json
  textures/block/*.png
  textures/item/*.png
  textures/entity/*.png
  geckolib/models/entity/*.geo.json
  geckolib/animations/entity/*.animation.json
  lang/pt_br.json
  lang/en_us.json
```

**Fontes editáveis sugeridos para futura PR (NÃO existem/foram criados nesta Task):**
```text
art_sources/
  palettes/overworld.aseprite          # se software estiver disponível, senão PNG + ficha
  textures/marked_stone.aseprite       # fonte autoral, NÃO distribuir fontes/licenças de terceiros
  blockbench/vestige_moth.bbmodel
  structures/poi_01_blockout.md
  references/visual_contact_sheet.png # arquivo criado futuramente por artista
```

Arte fonte pode estar em local diferente da distribuição runtime. **Não** empacotar arquivos `.bbmodel` na pasta de resources como se fossem assets carregáveis; eles são fontes para edição. Evitar adicionar dependência de software pago ao pipeline obrigatório.

## 7.2 Manifest obrigatório para cada asset de produção

```yaml
id: dd-vis-f0-marked-stone
gameplay_ref: B01 / F0
status: concept | prototype | approved | shipped
target_version: Minecraft 26.3 NeoForge 26.3
source_file: art_sources/textures/marked_stone.aseprite
runtime_files:
  - assets/dreamsdimensions/textures/block/marked_stone.png
  - assets/dreamsdimensions/models/block/marked_stone.json
  - assets/dreamsdimensions/blockstates/marked_stone.json
dependencies: [F0-worldgen, F0-loot]
resolution: 16x16
palette_revision: overworld_v1
owner: art
acceptance: [tiling, ingame_day_night, inventory_icon, survival_test]
```

Esse trecho é **exemplo de manifest de documentação**, não formato de registro NeoForge nem YAML que o jogo carrega. O nome de arquivo final só deve ser confirmado após IDs de registry aprovados.

## 7.3 Arte não deve reimplementar gameplay

- A animação de Rocha Cediça **não** decide se colide; BlockState/gameplay no servidor decide, arte comunica.
- Um arquivo PNG com cor luminosa **não** cria emissive nem light level.
- Um block model crossed em planta não cria suporte/crescimento; código/BlockState são fonte de verdade.
- Uma animação GeckoLib de flutuar **não** altera trajetória/hitbox automaticamente.
- Uma NBT de Passarela não comprova travessia válida em mundo natural.
- Um item com ícone não concede ação se item useOn/recipe/registry não existir.

---

# 8. REFERÊNCIAS, COPYRIGHT E PALETA VANILLA

**Fontes visuais oficiais como base de comportamento**, não para copiar assets de outros mods como se fossem nossos. Podemos usar os **blocos e texturas vanilla dentro do jogo** para estruturas, e criar **texturas originais** para recursos do DreamsDimensions. Não copiar atlas/texturas/modelos de mods terceiros, resource packs pagos ou fan art sem permissão.

**Conceitos e imagens geradas externamente:** são **referências de composição e mood**, NÃO arquivos finais de jogo. Pixel art final precisa ser redesenhada/validada em 16×16, com tile/UV, direito de uso claro e sem marca d’água. Referências de criatura podem orientar silhueta, mas o mob final deve ter model/UV/fontes próprias.

As quatro paletas desta Task são **propostas**, não “paletas reais capturadas”. Antes de retexturizar conteúdo antigo, a equipe deve produzir **comparação lado a lado** do asset atual e novo na mesma cena/iluminação.

---

# 9. PERFORMANCE: ORÇAMENTOS INICIAIS, NÃO NÚMEROS HOMOLOGADOS

| Categoria | Orçamento inicial de experimento | Métrica de aprovação |
|---|---|---|
| Textura full block | 16×16 comum | Tile 3×3, atlassing/culling sem warnings |
| Folhagem multiface | 1–2 modelos por estado legível, alpha mínimo | FPS com 100–300 instâncias em chunks próximos |
| Mob pequeno GeckoLib | Menor atlas legível; alvo exploratório até ~20–40 cubes e ~8–14 bones | 8 entidades próximas em mundo real, sem stutter |
| Mob médio | Alvo exploratório ~30–70 cubes e ~10–20 bones | 4 entidades sem regressão relevante vs baseline |
| Animação | Somente bones que mudam de fato, clips por IA | 60 segundos de idle/locomotion, sem slides/clips cortados |
| Ressonância | Caminho local curto, sem muitos renderers especiais | Comparar 1/8/32 circuitos, loaded/unloaded, TPS |
| POI | Preferir vanilla blocks, mínimo de BlockEntities | Worldgen/tempo de chunks e interior jogável |
| Partículas | Evitar em blocos comuns; raras em eventos legíveis | Comparar settings minimal/decreased/all |

**Orçamentos são limites de investigação, não garantia de performance nem recomendações hardcoded na engenharia.** Custo depende de renderer, geometria, quantidade no campo de visão, transparência, shaders e hardware. Se 20 cubes ainda causar problema por render/state, otimizar por medição, não por número isolado.

**Poupar em especial:** alpha blending de entidades repetidas, overdraw de folhas, render emissivo constante, update de bloco por frame, BlockEntities para decoração simples e efeitos 3D em todo minério.

---

# 10. TESTES DE QUALIDADE VISUAL — FICHAS DE HOMOLOGAÇÃO

## 10.1 Screenshots e cenas de referência

**Cena A — inventário:** hotbar e baú, GUI scale normal e pequena, 16 itens lado a lado; reconhecer Colar/Despertador vs novos.

**Cena B — caverna vanilla:** stone/tuff/deepslate sob tochas e sem tochas; B01/B15/B16 devem se diferenciar sem neon.

**Cena C — bosque:** Líquen em stone/dirt com luz de dia/noite, chuva e folhas vanilla; testar alpha/cutout e suporte.

**Cena D — parede 3×3:** todas as rotações de Pedra Marcada/cut e variantes, sem seams agressivos/tiling feio.

**Cena E — entidade:** Mariposa e outros 2/4/8 exemplares com animação idle/movimento perto de blocos. Fotografar 4 ângulos, recortar corpo, chão e asas sem clipping.

**Cena F — estrutura:** campo aberto/floresta, aproximação, entrada, interior, saída, panorama 4 lados, no shader e com seed/coord real.

**Cena G — estados:** Rocha Cediça segura/aviso/perigo; Emissor/Receptor on/off apenas quando protótipos aprovados. Jogador deve indicar estado sem precisar ouvir som.

## 10.2 Matriz de QA por asset

| Critério | Aprova quando | Reprova quando |
|---|---|---|
| **Legibilidade** | Reconhecível em tamanho real e monocromia onde essencial | Só faz sentido com zoom/editor |
| **Vanilla fit** | Se mistura com o terreno sem perder identidade | PBR/HD/shader obrigatório |
| **Diferenciação** | Nova silhueta/função, não apenas recolor | Confundível com bloco/mob vanilla |
| **Estados** | Feedback antes de risco/ação | Diferença só de matiz ou partícula |
| **Tiling** | 3×3 coerente, orientação controlada | Grades artificiais, bordas desalinhadas |
| **Alpha/UV** | Sem halos, seams ou faces vazando | Flicker, textura perdida ou z-fighting |
| **Geometry** | Hitbox/colisão/sombra coerentes | Asa atravessando parede ou pé deslizando |
| **Performance** | Não ultrapassa baseline sem justificativa | Lag de alpha, instâncias ou tick global |
| **Pipeline** | Arquivo fonte + runtime + refs resolvidas | PNG sem model ou model sem texture |
| **Survival** | Jogador encontra/usa e reconhece função | Só funciona com command/debug |

## 10.3 CI proposto para futures PRs (não implementado)

**Asset graph:** validar existências de `items`, `models/item`, `models/block`, `blockstates`, `textures` para cada registry novo; no 26.3 os arquivos `assets/<modid>/items/*.json` são **distintos** de `models/item/*.json`.

**PNG validator:** assinatura PNG, width/height esperados por family, alpha, sem arquivo 0 byte, no máximo atlas permitido por projeto, impedir arquivo binário incorreto renomeado .png. **Não proibir tamanho 128×128 da GeckoLib existente.**

**Model validator:** parse JSON, refs de parent/textures, variants/multipart, ausência de caminho plural legado. Certificar transforms de stairs/slab/wall e item display.

**Animation validator:** parse dos `.geo.json`, `.animation.json`, bones referenciados existentes, clip names esperados pelos controllers, loops, duração positiva, tamanho UV e render callbacks reais.

**Gameplay regression:** assets novos não dispensam loot, worldgen, AI, receitas, Curios, retorno, spawn, servidor e multi-seed já exigidos em 6.9.

---

# 11. COMANDOS DE TESTE E LIMITES DE REGISTRO

**Esta Task NÃO registrou** `marked_stone`, `vestige_lichen`, `marked_mortar`, Mariposa, Besouro, Grilo, Passarela ou Nódulo. Por isso comandos `/give`, `/summon`, `/locate` com IDs candidatos **não funcionam hoje**.

**Comandos válidos para inspecionar o build atual e preparar capturas:**
```mcfunction
/seed
/gamemode spectator
/gamemode survival
/time set day
/time set night
/weather clear
/give @s dreamsdimensions:dream_dust 15
/give @s dreamsdimensions:dream_necklace
/give @s dreamsdimensions:oneiric_awakener
```

**Testes na implementação:** fornecer IDs concretos de `/give`, `/setblock`, `/summon`, `/locate structure` somente depois de confirmar registro; cada `/tp` deve usar coordenadas **reais e inspecionadas da seed**. Gerar capturas em Creative pode conferir modelos, mas não substituir worldgen/Survival.

---

# 12. PACOTES DE ENTREGA POR PRIORIDADE — O QUE ARTISTAS DEVEM FAZER

| Sprint / slice proposta | Produção VISUAL mínima | Pré-requisito | Status |
|---|---|---|---|
| **ART-A / F0** | Paleta auditada, 2 texturas B01/B02, modelos vanilla, previews in-game, exemplar de Marco graybox | F0 geomaterial e registro em task futura | **PRIMEIRA PRIORIDADE** |
| **ART-B / F1** | Planta B10 textura + suporte + variação de colônia, previews de habitat | F1 suporte/cultivo fechado | **SEGUNDA PRIORIDADE** |
| **ART-C / Argamassa** | Ícone I01 + teste de cor e escala no inventário; textura de transformação usa B01 | F0+F1 e recipe calibrada | **APÓS BASE** |
| **ART-D / Mariposa** | Concept sheet, Blockbench .bbmodel, atlas otimizado, 5 clips quando IA pronta | F1 + spawn/AI 6.4 | **PRIMEIRO MOB NOVO** |
| **ART-E / Arqueologia** | Blockout B15/B08/B29, brush/feedback, Molde I03 e POI-05/10 graybox | Protótipo M02 aprovado | **CONDICIONAL** |
| **ART-F / Travessia** | States B17/B18, escora I04/B19, POI-08 graybox | Protótipo M03 aprovado | **CONDICIONAL** |
| **ART-G / Eco** | Blockout sem minério + 3 sprites state de circuito; depois B21–B25/I14/I15 se aprovado | A/B M06 vs vanilla | **BLOQUEADO ATÉ PROVA** |
| **ART-H / Expansão** | Variantes B03–B09, demais POIs e mobs condicionados | Uso efetivo e seleção em 6.8/6.9 | **BACKLOG** |
| **ART-I / Câmara** | Somente planificação genérica, nenhuma model final | POI-04 recompensado e gameplay validado | **BLOCKED_BY_REWARD_DESIGN** |

**Definição de pacote entregue:** fonte editável se aplicável, runtime assets corretos, screenshots de contato antes/depois, manifesto de refs, critérios de QA, comandos de reprodução e evidência de teste no mundo. Não afirmar “modelo pronto” só porque arquivo geo.json existe.

---

# 13. GATES DA TASK 6.8 E 6.9

**A Task 6.8 deve decidir com evidência:**
- Como a visibilidade de B01 e B10 afeta a raridade aparente e a exploração Early.
- Se skins de materiais têm valor para construir 64–256 blocos sem repetição monótona.
- Se os sinais de B17 são vistos com tempo suficiente para evitar dano injusto.
- Se flora especial permanece ecológica e não parece um portal por engano.
- Se densidade de POIs e microhabitats dá variedade sem saturação.
- Se circuito M06 vale um conjunto complexo de sprites + minério.
- Se fauna é distinguível, natural e de custo compatível com spawn.

**A Task 6.9 deverá:** criar PRs pequenas por vertical slice, separar **prototype graybox** de **art final**, colocar QA de recursos no CI, preservar F0/F1 como prioridade e **não** autorizar indiscriminadamente assets para todos B01–B30/I01–I18/POI-01–12/MOB-01–06.

**Sem End Game próprio:** o Late é domínio de materiais, engenharia e viagens oníricas consistentes com custo e risco, não culminância de boss/crafting final.

---

# 14. FONTES E VERSIONAMENTO

## 14.1 Documentação do próprio projeto

- [Task 6.6.1 — Catálogo expandido](OVERWORLD_UNIVERSE_CONTENT_EXPANSION_TASK_6_6_1.md)
- [Task 6.4 — Ecologia/Mobs](OVERWORLD_ECOLOGY_MOBS_HABITATS_TASK_6_4.md)
- [Task 6.5 — Estruturas](OVERWORLD_STRUCTURES_ENVIRONMENTAL_DISCOVERY_TASK_6_5.md)
- [Task 4.4 — Framework reutilizável GeckoLib/Mobs](DREAM_MOB_FRAMEWORK_TASK_4_4.md)
- [Task 6.6 — Crafting](OVERWORLD_CRAFTING_PROCESSING_PROGRESSION_TASK_6_6.md)

## 14.2 Documentação oficial pesquisada para evitar formato legado

- [NeoForge — Models e blockstates](https://docs.neoforged.net/docs/resources/client/models/) — localização e ligação de modelos.
- [NeoForge 26.3 Primer](https://docs.neoforged.net/primer/docs/26.3/) — mudanças de client/render 26.3, metadata, atlas e render passes.
- [GeckoLib 5 — Blockbench plugin](https://wiki.geckolib.com/docs/geckolib5/making-models/blockbench-plugin-usage/) — iniciar projeto com GeckoLib Animated Model e organizar bones.
- [GeckoLib 5 — GeoModels](https://wiki.geckolib.com/docs/geckolib5/concepts/geomodels/overview/) — modelos/animations/textures ligados a renderers.
- [GeckoLib 5 — Basic GeoModels](https://wiki.geckolib.com/docs/geckolib5/concepts/geomodels/basic-geomodel/) — caminhos quando modelos são especificados manualmente.

**Atenção:** a informação da documentação pública pode mudar; quando houver divergência, testar **dependências reais 26.3/GeckoLib 5 da branch** e verificar export carregando no cliente. Model/renderer/animation nunca deve ser aceito apenas por sintaxe JSON.

---

# 15. CRITÉRIOS DE ACEITE DA TASK 6.7

## Documento — CONCLUÍDO

- [x] Auditou convenções reais `items`, `models`, `blockstates`, `textures` e `geckolib`.
- [x] Preservou visual atual das duas dimensões e itens centrais sem retextura não solicitada.
- [x] Definiu gramática de cores, materiais, silhuetas, contraste e regras de uso do brilho.
- [x] Estabeleceu 16×16 vanilla-first para blocos e itens, 64/128 candidatas para atlas GeckoLib.
- [x] Fichas específicas para 30 blocos, 18 itens, 12 POIs e 6 criaturas do catálogo expandido.
- [x] Desenhou Mariposa de Vestígios com bone hierarchy e clips concretos para Blockbench.
- [x] Definiu identidade geológica, botânica, arquitetônica, técnica e onírica, sem falsa sinalização de minério/POI.
- [x] Explicou assets 26.3 de item definition + model + PNG (três arquivos diferentes).
- [x] Definiu pipeline NBT de estrutura sem criar fotos/GLB como worldgen.
- [x] Documentou fontes editáveis, manifest, QA, budgets de performance, compatibilidade e CI propostos.
- [x] Determinou produção em ordem, apenas após gates de gameplay, com pacote de primeiro vertical slice.
- [x] Preservou receituário R0, progressão Late, recuperação dimensional e não-End-Game.
- [x] Não alterou runtime assets, registries, mundo, texturas, receitas, Java ou main.

## Produção visual e testes — PENDENTES

- [ ] Auditar amostras de pixel/color reais de PNGs atuais e gerar contact sheet de referência.
- [ ] Produzir concepts de F0/F1 e comparar lado a lado com vanilla em dia/noite.
- [ ] Prototipar fonte Blockbench de Mariposa e testar animações após implementação da IA/habitat.
- [ ] Modelar primeiras estruturas graybox e validar suporte/rotas em worldgen natural.
- [ ] Aprovar ou descartar M02/M03/M06 antes de investir em art final dessas linhas.
- [ ] Criar scripts CI de asset graph/PNG/UV/animations em PRs funcionais futuras.
- [ ] Testar carregamento no cliente, servidor dedicado, multiplayer e saves por asset real criado.

**Estado honesto:** Task 6.7 está **concluída como contrato de direção visual**. Nenhuma textura, modelo, animação ou estrutura nova foi criada neste commit.

---

# 16. CONCLUSÃO

**O DreamsDimensions terá identidade própria porque seus materiais e criaturas revelam regras reconhecíveis do mundo.** No Overworld, pedra, planta e ruína são físicas, desgastadas e coerentes com vanilla; nos sonhos, a composição pode se tornar mais surreal, contrastada e fragmentada. A arte não precisa esconder a falta de mecânica atrás de luz azul.

**Ordem vencedora:** base F0/F1 com fontes/atlas corretos → Argamassa → Mariposa → protótipos de arqueologia, travessia e ressonância → POIs e variantes que realmente dependem desses sistemas. Assim, a ambição de 30 blocos/18 itens/12 estruturas/6 mobs continua viva **como universo planejado**, mas cada asset que chegar ao jogo terá função, qualidade profissional e baixo custo de manutenção.

**Próxima Task:** **6.8 — Economia, Raridade e Balanceamento**, seguida de **6.9 — Roadmap Executivo**.

---

*Fim — Task 6.7, Art Bible e especificação visual. Nenhum asset funcional criado.*