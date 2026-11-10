# TASK 5.0 — Matriz rastreável de conteúdo

**Baseline:** migration/minecraft-26.3 @ dd614f4aef2ea3d7ed7ed24fb3250cfa5b62823c.  
**Fonte das regras:** [Contrato de gameplay](GAMEPLAY_EXPLORATION_TASK_5_0.md). **Sequência de execução:** [Roadmap](GAMEPLAY_ROADMAP.md).  
**Natureza:** INVENTÁRIO de recursos presentes na branch + PROPOSTAS, sem alteração de gameplay.

## Leitura e convenções

A = compartilhado, B = compartilhado com distribuição variável, C = origem natural exclusiva de dimensão, D = origem exclusiva de bioma/região; E = decorativo/construção, F = funcional. O eixo A–D (origem) pode coexistir com E/F (finalidade). “C” não significa que é impossível transportar o bloco. Um ID como GX-* é um **identificador de planejamento, não um registro Minecraft**. A dimensão indicada é de obtenção natural ou uso principal, não restrição de colocação.

- **EXISTENTE:** arquivo/registro implementado no código, sem implicar que foi homologado manualmente.
- **PENDENTE DE VALIDAÇÃO:** implementação presente mas geração/uso em Survival não comprovado; ou proposta sem cadeia fechada.
- **PROPOSTO:** comportamento inexistente, já com função de gameplay e task prevista.
- **CÓDIGO:** classes/JSONs auditados na baseline; **TESTAR:** resultado de runtime não observado nesta Task 5.0.

Cada linha precisa de origem → ação → recompensa → utilidade. A coluna “reuso/consumo” indica material gasto, item reaproveitável ou sinal informativo. “Renovável” distingue reposição da mesma origem de encontrar novas ocorrências em chunks futuros.

## 1. Blocos e itens registrados (inventário da branch)

Esta seção cobre **17 blocos, 17 BlockItems associados e 2 itens próprios**. Os 17 BlockItems não são contados novamente como recursos diferentes, pois têm o mesmo ID de bloco; todos foram verificados em ModItems. Todas as traduções/assets/loot têm caminho declarado no projeto; testes de carregamento dos modelos e coleta continuam necessários.

| ID real | Nome | Tipo | Classe | Dimensão | Região | Origem | Ação | Recompensa | Utilidade | Consumo/renovação | Dependências | Risco | Status | Task | Aceite |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| dreamsdimensions:dream_grass_block | Dream Grass | bloco | C,E | Dreamscape | solo continental/ilha | material_rule em superfície | minerar com pá | bloco de grama | construir paisagens | coleta; recurso finito por local | ModBlocks/loot/shovel | verificar coleta sem Silk Touch | EXISTENTE | 5.3 | item obtido e colocado em Survival |
| dreamsdimensions:blue_dream_grass | Blue Dream Grass | bloco | C,E | Campo Azul | piso natural | material_rule de superfície | minerar/colher | grama azul | construção temática | finito local; novos chunks | surface/loot/shovel | regras reais de drop | EXISTENTE | 5.3 | solo visível e recolhível |
| dreamsdimensions:dream_dirt_block | Dream Dirt | bloco | C,E | Dreamscape | subsolo | material_rule abaixo do piso | escavar | terra onírica | construção e preenchimento | finito local | surface/loot/shovel | profundidade real por terreno | EXISTENTE | 5.3 | escavação e loot |
| dreamsdimensions:blue_dream_dirt | Blue Dream Dirt | bloco | C,E | Campo Azul | subsolo | material_rule abaixo do piso | escavar | terra azul | construir e preencher | finito local | surface/loot/shovel | variação do relevo | EXISTENTE | 5.3 | obtenção em piso real |
| dreamsdimensions:dream_sand_block | Dream Sand | bloco | E; origem PENDENTE | Não comprovada | nenhuma ocorrência natural auditada | creative/colocação ou uso futuro | areia temática | construção | indefinida em Survival | ModBlocks/loot/shovel | sem origem natural confirmada | PENDENTE DE VALIDAÇÃO | 5.4 | definir origem ou limitar a decoração craftável |
| dreamsdimensions:serene_stone | Serene Stone | bloco | C,E | Dreamscape | rocha em continente/ilha | surface_rule e feature | minerar | rocha temática | construção e plataforma de chegada | finito local | material_rule/loot | colisão/altura | EXISTENTE | 5.3 | mineração Survival e uso construtivo |
| dreamsdimensions:blue_dream_stone | Blue Dream Stone | bloco | C,E,F | Campo Azul | rocha/surface e forno | minerar ou fundir cobblestone | pedra azul | construção; fallback do spawn | finito local; receita repetível com insumo | loot/pickaxe/smelting/blasting | taxa de conversão | EXISTENTE | 5.3 | ambas receitas e coleta |
| dreamsdimensions:blue_dream_cobblestone | Blue Dream Cobblestone | bloco | C,E,F | Campo Azul | formações/geração a verificar | minerar/obter em Survival | pedregulho azul | fundir em blue_dream_stone | finito local se gerado | loot/pickaxe/receitas | fonte natural incerta | PENDENTE DE VALIDAÇÃO | 5.3 | provar aquisição sem cheats |
| dreamsdimensions:dream_shimmer_block | Dream Shimmer | bloco | C,E | Dreamscape | Jardim de Eco | formação com chance por célula elegível | seguir eco; minerar com picareta | bloco brilhante visualmente | decoração/arquitetura | finito por ocorrência; novos chunks | DreamscapeEcosystemFeature/loot/pickaxe | raro e sem garantia por jardim | EXISTENTE | 5.1/5.4 | colhível com feedback e uso em construção |
| dreamsdimensions:dream_glow_moss | Dream Glow Moss | bloco | B,E; F proposto | Azul e Dreamscape | jardins e patches | worldgen localizado | observar/clicar/escavar | musgo de luz 8; sinal V0 | construir/indicar shimmer na V0 | finito local; novas regiões | features/loot/shovel | sinal pode não ter alvo | EXISTENTE | 5.1/5.4 | clique V0 e loot sem duplicar |
| dreamsdimensions:dream_ore | Dream Ore | minério | A,F | Overworld pretendido | camadas -16..64 | biome modifier pendente | picareta de ferro; quebrar | dream_dust + XP 1–3 | crafting de core/Despertador | finito por veio; novos chunks se corrigido | placed_feature/loot/needs_iron_tool | caminho de biome modifier possivelmente inválido | PENDENTE DE VALIDAÇÃO | 5.3 | veios naturais e drop sem cheats |
| dreamsdimensions:deepslate_dream_ore | Deepslate Dream Ore | minério | A,F | Overworld pretendido | camadas -64..-16 | biome modifier pendente | picareta de ferro; quebrar | dream_dust + XP 1–3 | crafting | finito por veio; novos chunks se corrigido | placed_feature/loot/needs_iron_tool | mesmo problema de path | PENDENTE DE VALIDAÇÃO | 5.3 | veio em profundidade e drop |
| dreamsdimensions:dream_infused_stone | Dream Infused Stone | bloco | A,E,F | mesa crafting | sem distribuição natural definida | 2 stone + 2 dream_dust → 1 | bloco infundido | construção e receita de core | insumos consumidos; bloco recuperável pelo loot | recipe/loot/pickaxe | acesso ao Dust não provado | EXISTENTE | 5.3 | receita utilizável no Survival |
| dreamsdimensions:oneiric_core_block | Oneiric Core | bloco | A,E,F | mesa crafting | crafting, sem geração natural | 4 infused + 4 dust + clock → 1 | núcleo | ingrediente do Despertador; construção | insumos consumidos; bloco recuperável | recipe/loot/pickaxe | custo alto se ore não nasce | EXISTENTE | 5.3 | crafting e conversão em Despertador |
| dreamsdimensions:dream_flower_block | Dream Flower | bloco | B,E | Azul e Dreamscape | jardins | vegetação de feature | colher | flor temática | decoração | finito por ocorrência | vegetation/ecosystem/loot/flowers tag | distribuição natural e loot | EXISTENTE | 5.4 | flores coletáveis no mundo |
| dreamsdimensions:dream_log_block | Dream Log | bloco | B,E | Azul e Dreamscape | árvores procedurais | feature de troncos | cortar | tronco | arquitetura e construção | novas árvores em novos chunks; replantio não definido | vegetation/ecosystem/loot/log tag | não prometer cultivo | EXISTENTE | 5.4 | tronco obtido de árvore natural |
| dreamsdimensions:dream_leaves_block | Dream Leaves | bloco | B,E | Azul e Dreamscape | copas naturais | feature de folhas | colher com ferramenta apropriada | folhagem | paisagismo | novas árvores em novos chunks; replantio não definido | DreamLeavesBlock/loot/leaves tag | drop e decay a testar | EXISTENTE | 5.4 | colhível/colocável sem crashes |
| dreamsdimensions:dream_dust | Dream Dust | item | A,F | Overworld pretendido | ore/deepslate ore | loot de minério sem Silk Touch | minerar e coletar | poeira onírica | infused stone/core/Despertador | consumível; geração natural pendente | ModItems/loot/recipes | cadeia completa pode ficar inacessível | PENDENTE DE VALIDAÇÃO | 5.3 | Dust obtido e gasto em receitas |
| dreamsdimensions:oneiric_awakener | Despertador Onírico | item | A,F | crafting; uso nas dimensões | mesa de crafting | feather + core + bottle | manter uso por 60 ticks | retorno ao Overworld | retorno seguro sem portal obrigatório | reutilizável; cooldown 60 ticks; insumos craft consumidos | OneiricAwakenerItem/DreamReturnHelper | não tocar no fallback seguro | EXISTENTE | 5.2 | retorno para cama/respawn/spawn seguro |

## 2. Sistemas, mobs e formações existentes

| ID / evidência | Tipo | Categoria | Dimensão/região | Origem → ação → recompensa → uso | Renovabilidade / dependência | Status | Próximo teste |
| --- | --- | --- | --- | --- | --- | --- | --- |
| dreamsdimensions:dreamscape | sistema de dimensão | C,F | Dreamscape | sono 100 ticks → chegada segura → explorar e obter recursos → retornar | Reutilizável; catalog/config/datapack | EXISTENTE | 5.2: dormir/retornar multiplayer |
| dreamsdimensions:campo_onirico_azul | sistema de dimensão | C,F | Campo Azul | sono 100 ticks → relevo azul → coletar/andar/construir | Reutilizável; catalog/config/datapack | EXISTENTE | 5.2: chegada e Slow Falling |
| dreams:blue_topography (identificador de planejamento) | formação | C,E | Azul; pradarias/cordilheiras/terraços (geomorfologias) | noise wave/ridge/terrace → explorar relevo → materiais/caminhos → construção | Novos chunks; density functions já existentes | EXISTENTE | 5.4: amostras e capturas |
| dreams:dreamscape_geology (planejamento) | formação | C,E | Dreamscape; continente/fendas/ilhas/pontes | density functions → achar travessia → explorar outra margem → novos POIs | Novos chunks; não garante ponte em todo setor | EXISTENTE | 5.4: medir pontes reais |
| dreamsdimensions:dream_vegetation / dream_formation | sistema worldgen | B,C,E | Campo Azul | features e placed_feature → vegetação, jardins e arcos → coleta/construção | Por chunk e seed; validação de escrita | EXISTENTE | 5.4: distribuição |
| dreamsdimensions:dreamscape_ecosystem | sistema worldgen | C,E | Dreamscape | 6 instâncias → jardins/raízes/árvores/monólitos → explorar/colher | Por chunk; rejeita ocupação e chunks inacessíveis | EXISTENTE | 5.4: Jardim de Eco e shimmer |
| dreamsdimensions:fragmento_errante | mob | C,F | Dreamscape; ilhas altas (não bioma distinto hoje) | encontro calmo 90 ticks → Slow Falling 360 ticks → travessia | Cooldown 1800 por criatura; spawn regional com teto 6; GeckoLib | EXISTENTE | 5.2/5.5: spawn, anim e buff |
| dreamsdimensions:vigia_adormecido | mob | C,F | Dreamscape; ilhas altas | atenção 100 ticks → Pacto 2400 ticks/1 resgate → explorar com segurança | Cooldown do ritual 3600 ticks; resgate consome vínculo; habitat depende de bioma fixo | EXISTENTE | 5.2/5.5: pacto/limpeza |
| dreamsdimensions:pacto_de_vigilia | sistema/efeito | C,F | Dreamscape; Vigia | ritual → ícone vanilla → informação de resgate ativo → travessia | HUD não é direito; DreamAnchorService transitório | EXISTENTE | 5.2: restart/login/sem efeito órfão |
| dreamsdimensions:dream_ore_overworld | sistema de distribuição | A,F | Overworld | biome modifier → minério natural → dust → crafting | Caminho data/worldgen/biome_modifier questionável | PENDENTE DE VALIDAÇÃO | 5.3: corrigir somente após reproduzir |

## 3. Conteúdo PROPOSTO com cadeia de valor definida

| ID provisório | Nome/tipo | Classe / dimensão / região | Origem → ação → recompensa → utilidade | Consumo / renovação | Dependências | Riscos | Status | Task | Critério de aceite |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| GX-S01 | Eco do Musgo / sistema | B,F; Dreamscape, Bosques da Memória (V0) | musgo natural ou de teste → clique vazio → pista de shimmer → minerar shimmer → construir | cooldown 100 ticks, não consome item; shimmer finito por ocorrência | ModBlocks, evento servidor, chunks carregados, loot/pickaxe | alvos escassos; não encontrar não pode parecer bug | PROPOSTO | 5.1 | sucesso/falha, Survival, sem chunk-load, 2 jogadores |
| GX-S02 | Catálogo de descoberta / sistema | A,F; várias dimensões | marco validado → visita → registro pessoal/compartilhado → revisitar com sentido | benefício permanente, somente quando justificar | identidade de POI estável, persistência | abuso por reposicionar bloco; excesso de estado | PROPOSTO | 5.7 | relatório por jogador e save, sem dup |
| GX-S03 | Ressonância de longo alcance / sistema | A,F; multi dimensão | pistas → instrumento opcional → orientação entre POIs → novo local | uso repetível, eventual desgaste/custo futuro | GX-S01 homologado e POI data-driven | scan massivo/geração de chunks | PROPOSTO | 5.7 | consultas indexadas sem carregar mundo |
| GX-S04 | Afinador Onírico / item/sistema | A,F; ambas dimensões | recursos de duas dimensões → crafting → influenciar preferência do próximo sono → exploração direcionada | instrumento reutilizável; receita ainda pendente | contrato seguro do motor e recursos distintos | viés e bloqueio de destino | PENDENTE DE VALIDAÇÃO | 5.8 | sem quebrar chance/default/retorno |
| GX-B01 | Pradarias das Ondas / região | C,D,E; Azul | explorar ondulações → jardins → musgo/flor → iluminar base | por novos chunks | worldgen e vegetação existente | monotonia de paleta | PROPOSTO | 5.6 | forma e recurso visualmente distintos |
| GX-B02 | Cordilheiras da Névoa / região | C,D,F; Azul | subir espinha → mirante e sinal de névoa → orientação → explorar vale distante | sinal reutilizável | noise ridge, feedback | risco de queda/fog | PROPOSTO | 5.6 | leitura de rota em Survival |
| GX-B03 | Terraços do Sonho / região | C,D,E,F; Azul | percorrer níveis/arcos → sequência de formações → rota/recurso de construção → nova visita | blocos coletáveis | terrace height, feature existente | colisão e saltos obrigatórios | PROPOSTO | 5.6 | travessia natural com aceitação |
| GX-B04 | Bosques da Memória / região | C,D,E,F; Dreamscape | procurar jardins de eco → GX-S01 → shimmer → arquitetura | locais finitos | eco_garden e fonte de musgo | falta de brilho natural | PROPOSTO | 5.6 | jardins diferenciáveis |
| GX-B05 | Ermos das Rupturas / região | C,D,F; Dreamscape | ler monólitos/fendas → identificar ponte → chegar a outro bioma | rotas reutilizáveis | density bridge e formações | “ponte sempre” falso | PROPOSTO | 5.6 | travessias comprovadas em seeds |
| GX-B06 | Ilhas da Vigília / região | C,D,F; Dreamscape | alcançar ilha → interagir Fragmento/Vigia → buff/resgate → atravessar | efeitos temporários | FloatingIslandHabitat com multi-bioma | spawn quebrado após biome change | PROPOSTO | 5.5/5.6 | densidade de mobs preservada |
| GX-S05 | Perfis de gameplay regionais / sistema | A,F; todas | categoria de região → conteúdo certo → recurso ou interação → exploração diversa | configuração reutilizável | tags, placed_features, habitação | framework especulativo | PENDENTE DE VALIDAÇÃO | 5.5/5.6 | implantar só quando JSON não bastar |
| GX-M01 | Material exclusivo futuro / item ou minério | D,F; região a escolher | **ORIGEM NÃO DEFINIDA** → **AÇÃO NÃO DEFINIDA** → **RECOMPENSA NÃO DEFINIDA** → **USO NÃO DEFINIDO** | não definido | precisa de mecânica aprovada | criar minério inútil | PENDENTE DE VALIDAÇÃO | >=5.8 | só aprovar quando cadeia fechar |

## 4. Receitas, loot, assets e estados de recurso

| Recurso | Fonte concreta de obtenção/resultado | Consome? | Arte/tags | Evidência e ressalva |
| --- | --- | --- | --- | --- |
| dream_dust | Loot dos dois minérios; Fortune/Silk Touch presentes na tabela | Consumível | assets/items e lang; ore precisa de picareta ferro | JSON e código presentes; geração natural ainda pendente |
| dream_infused_stone | Padrão 2×2 de stone e dust alternados, 2 de cada → 1 bloco | Sim, insumos | blockstate/model/loot, pickaxe | Craft definido; custo validável em servidor |
| oneiric_core_block | 4 infused stone + 4 dust + clock → 1 bloco | Sim, insumos | blockstate/model/loot, pickaxe | Craft definido; ainda não é dispositivo com uso próprio |
| oneiric_awakener | Feather + core block + glass bottle → 1 | Sim para crafting; ferramenta é reutilizada | items model, tooltip; cooldown componente | Uso por 60 ticks, retorno é existente; teste end-to-end necessário |
| blue_dream_cobblestone | Smelting 200 ticks ou blasting 100 ticks → blue_dream_stone | Sim na transformação | tags pickaxe; loot bloco | Testar fonte natural do cobblestone e as duas receitas |
| dream_shimmer_block | Aparece raramente no echoGarden; loot dá o bloco | Mineração remove ocorrência, não consumo do sinal | tag mineable/pickaxe, modelo e loot | V0 não duplica material; teste em Survival |
| dream_glow_moss | Patch Azul ou jardim Dreamscape; loot dá o bloco | Mineração remove ocorrência | tag mineable/shovel, luz 8, loot | V0 usa clique vazio, sem destruir |
| DreamOreBlock.clicked | Clique vazio alterna boolean clicked | Não | blockstate atual | Nenhum elo de recompensa/uso; NÃO aprovar expansão até especificação |

**Sistema de loot/asset:** há modelos de blockstates e items para recursos listados; a simples presença dos arquivos não prova renderização. Não alterar loot de minérios, receitas, tags nem worldgen na Task 5.0.

## 5. Pendências ordenadas por risco

1. **P0:** confirmar caminho e carregamento do biome modifier do minério, amostrar em Overworld novo, testar Fortune/Silk Touch e crafting completo.
2. **P1:** confirmar se shimmer do Jardim de Eco é encontrável a uma distância que torna GX-S01 interessante; logs de geração e playtest, não só probabilidade do código.
3. **P1:** adaptar a dependência de biome fixo em FloatingIslandHabitat antes de um novo biome_source.
4. **P1:** validar uso/retorno do Despertador, pacto e Slow Falling com dois jogadores; docs anteriores não substituem teste novo.
5. **P2:** determinar origem Survival de dream_sand_block e blue_dream_cobblestone, ou classificar explicitamente como material sem acesso natural por enquanto.
6. **P2:** avaliar interação clicked de DreamOreBlock; não criar recompensa artificial para justificar código antigo.
7. **P3:** manter itens exclusivamente decorativos sem efeitos extras se a coleta/construção já fornecer experiência satisfatória.

**Regra de aceite para nova linha:** deve haver ID, origem real, ação simples, recompensa, utilidade verificável, política de repetição, dono da autoridade, falha explicável, risco e teste Survival. Caso contrário permanece PENDENTE, mesmo que uma textura esteja pronta.
