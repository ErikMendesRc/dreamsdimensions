# TASK 5.0 — Contrato de gameplay, exploração e progressão

**Projeto:** DreamsDimensions  
**Branch auditada:** migration/minecraft-26.3  
**Baseline de leitura:** dd614f4aef2ea3d7ed7ed24fb3250cfa5b62823c (10/11/2026)  
**Stack conferida:** Minecraft 26.3, NeoForge 26.3.0.101, Java 25, ModDevGradle 2.0.148 e GeckoLib 5.5.7 (gradle.properties e build.gradle).  
**Natureza desta task:** contrato e planejamento somente. Nenhum comportamento em runtime é criado por este documento.

> Regra de evidência: **CÓDIGO** confirma presença e intenção de uma implementação; **DOCUMENTAÇÃO OFICIAL** confirma um contrato geral da API; **PROPOSTA** é decisão futura; **PENDENTE** exige build, mundo real, multiplayer ou playtest. Compilar não é homologar gameplay. Documentos históricos são evidência secundária ao código da branch.

Documentos relacionados: [Matriz de conteúdo](GAMEPLAY_CONTENT_MATRIX.md) e [Roadmap executável](GAMEPLAY_ROADMAP.md). A matriz é a fonte dos IDs provisórios; este contrato é a fonte de verdade das regras.

## 1. Visão de produto: Survival livre, exploração com consequências

**Fantasia do jogador:** entrar em sonhos imprevisíveis, perceber pistas no ambiente, aprender a lê-las, obter algo útil, voltar ao Overworld para construir/fabricar, e retornar por iniciativa própria. O Minecraft continua jogável sem missões obrigatórias, árvores de habilidades, bloqueios de sono ou dependência de um guia externo.

**Pilares vinculantes:**
1. **Descoberta:** relevo, variação de luz, vegetação, silhuetas, sons e criaturas sinalizam locais dignos de inspeção.
2. **Recursos:** cada recompensa deve servir a pelo menos um uso concreto (construção também conta). A raridade não é uma função.
3. **Interação:** o mundo responde a ações inteligíveis, com retorno visual/sonoro e falhas explicáveis.

**Regra de aprovação de conteúdo:** origem → ação → recompensa → utilidade → motivo de retornar. Se faltar qualquer elo, marcar como PENDENTE na matriz, não criar automaticamente. Elementos estritamente decorativos são válidos quando têm obtenção Survival clara e função arquitetônica/estética identificável.

**Progressão não linear:** ferramentas tornam exploração mais conveniente, não desbloqueiam por decreto o acesso às dimensões. O motor de sono original é a porta de entrada em todos os estágios.

## 2. Auditoria factual do repositório

### 2.1 Motor Onírico — CÓDIGO EXISTENTE; runtime precisa de regressão

| Componente | Contrato atual constatado | Proteção para as próximas tasks |
| --- | --- | --- |
| DreamDimensionCatalog / DreamDimensionDefinition | Dois perfis lógicos de dimensões de datapack; ambos destinos de sono, peso 1. Dreamscape: piso reserva serene_stone e raio 12; Campo Azul: blue_dream_stone e raio 24. Políticas de atmosfera/efeito/transição são metadados. | Não registrar dimensões por DeferredRegister; não acrescentar progressão nem biomas ao catálogo de teleporte. |
| DreamsConfig / WeightedDimensionPicker | A config dream_dimensions filtra perfis conhecidos; sorteio ponderado sem reposição de destinos elegíveis carregados. | Não alterar pesos e nomes da config sem migração explícita. |
| SleepTeleportHandler / DreamTeleportService | Contagem de 100 ticks de sono no Overworld; proteção por UUID; seleção no servidor; tenta outro candidato apenas após falha de chegada, não após teleporte incerto. | Respeitar sono vanilla, despertar, jogadores simultâneos e falhas sem retry duplicado. |
| DreamSpawnHelper | Busca piso natural, espaço para corpo, colisão, fluidos e borda; contingência final: plataforma 5×5 somente em ar. | Não alterar spawn ou worldgen de chegada para introduzir um POI. |
| DreamReturnData / ModAttachments | Attachment dream_return com campos dimension, pos, yaw; copyOnDeath; grava a cama após teleporte confirmado. | Não reaproveitar este attachment para progresso de exploração; preservar codec e identidade. |
| DreamReturnHelper / OneiricAwakenerItem | Retorno por uso contínuo de 60 ticks e cooldown; prioridade cama válida → respawn pessoal válido → spawn global seguro; recusa retorno inseguro. | Não depender de ferramenta futura para sair; não mexer no Despertador. |

**Afinador Onírico (PROPOSTO, não implementar):** instrumento opcional para influenciar a prioridade dos destinos; exige primeiro recurso útil de cada dimensão, feedback e elegibilidade segura. Deve complementar a seleção do DreamTeleportService, sem reescrever o sono, sem remover a possibilidade de sortear uma dimensão e sem criar destino indisponível.

### 2.2 Mundo e biomas — CÓDIGO EXISTENTE

- As duas dimensões têm gerador noise com **biome_source minecraft:fixed**. Há somente dois JSONs de bioma registrados: campo_onirico_azul e dreamscape_biome. As seis regiões propostas na seção 6 **não** são seis biomas em runtime.
- Campo Azul: dimensão com min_y -64 e height 384; relevo composto por wave_height (pradarias), ridge_height (cordilheiras) e terrace_height/terrace_presence (terraços). Material rule usa blue_dream_grass em piso, blue_dream_dirt em subsuperfície, blue_dream_stone em faces íngremes e rocha-base.
- Dreamscape: min_y 0 e height 256; density functions combinam terra continental, fendas, ilhas suspensas e volumes de ponte. Material rule usa dream_grass_block/dream_dirt_block/serene_stone. Uma superfície alta não prova que seja ilha, pois regiões são verticalmente sobrepostas.
- DreamWorldgen registra três codecs de Feature: dream_vegetation, dream_formation e dreamscape_ecosystem. Campo Azul referencia cinco formas vegetais e quatro formações; Dreamscape referencia seis formas próprias no bioma. A distribuição depende dos placed_feature, relevo, seed e verificações geométricas.
- As features planejam geometria, validam blocos/heightmap/ocupação/borda e a área WorldGenLevel.ensureCanWrite antes de gravar. Não as transformar em detectores de descoberta por tick e não forçar chunks por interação.
- Tags já existentes blue_natural_ground e dreamscape_natural_ground definem piso de habitats/terreno. Tags de colheita vanilla registram pickaxe, shovel e needs_iron_tool para blocos relevantes.

**Separação obrigatória de conceitos:** bioma é a classificação do gerador; geomorfologia é a forma real (cordilheira, ilha, fenda, ponte); superfície é o bloco e a condição física em uma posição; habitat é um predicado de uso de criaturas/POIs; POI é o alvo de exploração e interação. Na Dreamscape, um biome query na mesma coordenada X/Z pode não distinguir continente e ilha. Futuras regras precisam combinar dimensão + bioma/tag + Y + piso real + apoio/vazio + região gerada, quando necessário.

**Pendência P0 de minério:** o arquivo dream_ore_overworld.json existe em data/dreamsdimensions/worldgen/biome_modifier/, com neoforge:add_features em #minecraft:is_overworld para placed features dream_ore e deepslate_dream_ore. A documentação NeoForge disponível em novembro/2026 descreve o diretório data/<modid>/neoforge/biome_modifier/. Esse conflito de caminho torna o carregamento **NÃO COMPROVADO**; não afirmar minério natural em Survival antes de validar a versão exata e geração em mundo novo. Não alterar na Task 5.0.

### 2.3 Inventário e uso atual de Survival — CÓDIGO/JSON EXISTENTES

Há **17 blocos registrados**, 17 BlockItems e **dois itens autônomos** (dream_dust e oneiric_awakener). A aba criativa inclui todos. Ver [inventário completo por ID](GAMEPLAY_CONTENT_MATRIX.md).

- **Construção e terreno:** dream_grass_block, blue_dream_grass, dream_dirt_block, blue_dream_dirt, dream_sand_block, serene_stone, blue_dream_stone, blue_dream_cobblestone, dream_flower_block, dream_log_block, dream_leaves_block, dream_glow_moss (luz 8) e dream_shimmer_block. Parte é gerada como superfície/vegetação; itens existentes têm loot/asset, mas obtenção em Survival deve ser testada.
- **Processamento:** blue_dream_cobblestone → blue_dream_stone por smelting (200 ticks) ou blasting (100 ticks). Dream infused stone é 2 stones + 2 dream_dust → 1 bloco (receita 2×2); oneiric_core_block consome 4 infused stones, 4 dream_dust e 1 clock → 1 bloco; oneiric_awakener consome feather + core + glass bottle → 1 item.
- **Mineração:** dream_ore e deepslate_dream_ore são DropExperienceBlock com XP 1–3, drops por loot table de dream_dust sem Silk Touch e próprios blocos com Silk Touch; pedem ferramenta de ferro pela tag. DreamDustItem não possui comportamento ativo. O clique sem item no DreamOreBlock alterna a propriedade clicked, mas **não concede uma recompensa conhecida**; classificar como interação sem utilidade atual (pendência de design), não como mecânica homologada.
- **Blocos funcionais de fato:** o Despertador é funcional; oneiric_core_block e dream_infused_stone **são ingredientes de receitas**, não estações interativas. Dream shimmer é recurso de paisagem/decoração, não detector atualmente.
- **Renovabilidade:** construção/colheita de blocos segue as regras de mineração e regeneração vanilla e worldgen; novos chunks podem produzir novas ocorrências, mas isso não torna um depósito individual renovável. Ainda não há contrato de fazenda controlada de dream_dust.

**Risco de cadeia:** até demonstrar geração de minério, o caminho minério → dust → core → Despertador não é garantia de Survival. Esse teste tem prioridade própria no roadmap. O primeiro protótipo não depende de Dust.

### 2.4 Mobs e sistemas compartilháveis — CÓDIGO EXISTENTE

- **Fragmento Errante:** entidade voadora com DreamFlyingMoveControl + FlightProfile e animações GeckoLib. Comportamento de proximidade calma: 90 ticks de sintonização, aplica Slow Falling por 360 ticks, cooldown por criatura de 1800 ticks; curiosidade, fuga e desaparecimento permanecem específicos da espécie. Habita ilhas da Dreamscape.
- **Vigia Adormecido:** entidade terrestre com vigília/reação, ritual de atenção de 100 ticks e concessão por DreamAnchorService de **um resgate por até 2400 ticks**, dentro dos limites da política (horizontal 48, queda 12, velocidade descendente abaixo de -0.10). Estado e HUD visual do Pacto de Vigília têm fontes de verdade separadas; vínculo é transitório, não passa por reinício.
- **Reutilizáveis:** DreamMobHabitat (predicado), FloatingIslandHabitat (vazio amostrado sob piso, sem gerar chunks), DreamMobPopulationService e MobPopulationProfile (orçamento e cotas), CalmPlayerInteraction (validação de atenção), SafeGroundNavigation (apoio seguro), DreamAnchorPolicy e DreamAnchorService.
- **Dependência a resolver ANTES de múltiplos biomas:** FloatingIslandHabitat recebe um ResourceKey<Biome> específico; FragmentoIslandHabitat fixa dreamscape_biome e VigiaIslandHabitat o reutiliza. Uma migração descuidada do biome_source pode reduzir/desligar o spawn natural das duas espécies. Planejar suporte a conjunto/tag de biomas com predicado de terreno preservado e testes regressivos; NÃO refatorar nesta task.
- A coordenação de população é server-only e usa perfis por espécie; jogador por região 128×128, até 256 chaves em cache por nível, consultando chunks já carregados. Não reaproveitar esse agendador para POIs; ele existe para mobs.

**Critério de evidência:** arquivos e lógica acima foram auditados diretamente. Os relatórios de homologação anteriores distinguem CI/smoke de playtest. Não foram executados cliente, server dedicado, GameTest ou testes multiplayer nesta Task 5.0.

## 3. Contrato dos loops

**Loop principal:** EXPLORAR → OBSERVAR → DESCOBRIR → INTERAGIR → OBTER RECURSO/INFORMAÇÃO → UTILIZAR → REEXPLORAR.

| Pergunta | Resposta contratual |
| --- | --- |
| Por que começar? | Sono leva a um mundo com materiais, formas e experiências indisponíveis ao redor da base vanilla. A origem segura permite observar sem tarefa inicial compulsória. |
| Como notar algo? | Silhueta, brilho, sons, caminhos, vegetação e contraste territorial; um sinal não deve exigir conhecimento externo. |
| Como identificar interação? | Pista visual/sonora coerente, tooltip/feedback contextual discreto e resposta ao clique/atenção; não marcar todos os blocos com HUD invasiva. |
| O que fazer? | Inspecionar, observar criatura com calma, minerar ou usar um bloco sinalizador. |
| O que ganhar? | Bloco útil, navegação/informação, segurança temporária ou material de crafting. |
| Como usar? | Construção, travessia, ferramenta e preparação da visita seguinte, sem menu obrigatório. |
| Por que voltar? | Variação espacial, recursos locais, caminhos ainda não explorados e benefícios opcionais. |
| E quando não houver nada? | Mensagem curta, feedback negativo e próximo experimento acessível; evitar sensação de bug ou varredura infinita. |
| Como evitar repetição? | Preferir descoberta informativa, recursos de usos distintos, limites regionais e redução de estímulo após mesma interação; não criar drops infinitos por clique. |
| Como preservar liberdade? | Nenhuma missão bloqueia biomas, teleporte, mineração ou construção. |

**Loops secundários** (entrada → ação → resultado → repetição):
- **Descoberta:** pista visível → investigar formação/jardim → informação ou recompensa → buscar outra ocorrência.
- **Mineração/crafting:** encontrar minério comprovadamente acessível → extrair Dust → fabricar pedra/core/Despertador → usar ou construir → repetir opcionalmente.
- **Construção:** recolher flora/rocha temática → combinar com vanilla → criar base ou marcos → encontrar novas paletas.
- **Criaturas:** observar Fragmento/Vigia sem hostilidade → obter Slow Falling/Pacto → explorar verticalidade e travessias → interagir com outras instâncias respeitando cooldown.
- **Dimensões:** sono livre → visitar uma das duas → explorar recurso exclusivo → regressar com Despertador → usar ganhos → reentrar por sono.

## 4. Seleção da primeira fatia vertical

**Comparação de opções** (escala relativa 1–5, estimativas de design, NÃO resultados de playtest; maior é melhor exceto custo/risco):

| Candidata | Valor | Clareza | Reuso | Custo (1 baixo, 5 alto) | Assets | Worldgen | Risco |
| --- | ---: | ---: | ---: | ---: | --- | --- | --- |
| Instrumento completo de Ressonância que rastreia POIs distantes | 5 | 4 | 5 | 5 | Provável novo item | Índice/POIs | Alto: chunks e persistência |
| **Eco do Musgo: sinal ambiental próximo** | **4** | **4** | **4** | **2** | Nenhum novo | Nenhuma alteração | Baixo/médio: distribuição natural |
| Coleta renovável de musgo por interação | 2 | 5 | 3 | 2 | Nenhum | Eventual rebalance | Médio: farm repetitivo |
| Nova recompensa no ritual do Vigia | 3 | 4 | 4 | 2 | Nenhum | Nenhuma | Médio: redundância com pacto |

**Escolha aprovada para protótipo: Eco do Musgo (Ressonância Ambiental V0).** É uma experiência autossuficiente com exploração, percepção, interação, orientação e coleta, sem infraestrutura antecipada de indexação de POIs. **Não** se confunde com ResonanceGoal do Fragmento, que é uma habilidade de criatura já existente.

### 4.1 Especificação comportamental V0 (PROPOSTA)

**Lugar inicial:** área de teste da Dreamscape, com dream_glow_moss e dream_shimmer_block **já existentes** colocados manualmente ou em Jardim de Eco comprovado.  
**Entrada:** jogador em Survival interage com dream_glow_moss com mão vazia, em dimensão habilitada pelo perfil de gameplay. Não ativa no Overworld por padrão; negar espectador e jogador morto.  
**Busca:** servidor inspeciona apenas blocos em chunks previamente carregados em uma caixa de até X/Z ±12 e Y ±6 a partir do musgo, obedecendo altura/borda do mundo, comparando o bloco exato dream_shimmer_block. Máximo de **8125 posições** por tentativa antes de filtros (25×25×13); sem worldgen, chunk tickets ou scan contínuo. Escolher alvo mais próximo, com desempate determinístico por coordenadas. É permitido limitar mais o orçamento após profiling; não ampliar sem benchmark.
**Sucesso:** direcionar 1–2 segundos de partículas vanilla com amostras discretas entre o musgo e o alvo, acompanhado por som vanilla e mensagem local curta. O alvo é alcançado e minerado com picareta (tag existente), produzindo dream_shimmer_block via loot table existente; o bloco tem uso de construção/decoração. **Não entregar um item automaticamente**.  
**Falha:** nenhum shimmer encontrado em chunks carregados, indisponível/destruído ou fora do alcance → mensagem breve “Nenhum eco próximo”/equivalente; nenhum custo, nenhuma informação falsa, nenhum novo chunk carregado. Não garantir que toda flor/musgo tenha segredo.
**Cooldown:** proposta inicial de **100 ticks / 5 segundos por jogador**, inclusive nas falhas, evitando spam de scans e partículas; deve ser aplicada antes da busca. Expira com o relógio do nível, é transitória e não gera direito a recompensa. Logout/clone/troca de dimensão limpam estado.  
**Autoridade:** servidor decide elegibilidade, busca e resposta; cliente só mostra feedback vanilla. Não abrir inventário, alterar blocos, teletransportar ou escrever inventário ao interagir.  
**Progresso:** a recompensa da V0 é informação local + acesso facilitado a um bloco de construção; nenhum badge persistente ou contador de descobertas nesta versão. Descobertas permanentes exigem uma task própria com requisitos de identidade estável de POIs.
**Reuso:** a política “sinalizador → alvo por tag/perfil em volume limitado → resultado” poderá aplicar-se a outros ambientes; a V0 usa IDs fixos somente como configuração mínima. Não criar um sistema universal de POIs apenas para um único par de blocos.

**Cadeia de valor V0:** Jardim de Eco/musgo natural → clique com mão vazia → direção do shimmer próximo → mineração com picareta → bloco de decoração/arquitetura → procurar outro jardim/estilo. Nenhuma raridade cosmética é apresentada como progressão obrigatória.

### 4.2 Balanceamento V0 e medidas de aceite

Parâmetros **estimados**, ainda não homologados: raio XZ 12, tolerância Y 6, teto 8125 inspeções por clique, cooldown 100 ticks, até 12 partículas por pulso e uma dica por uso. A frequência de Jardins de Eco e a chance de shimmer dependem da feature existente: em echoGarden o ramo choice > 0.982F oferece uma probabilidade nominal de aproximadamente 1,8% por célula elegível, não por jardim nem por chunk; candidatos podem falhar na validação. **Não prometer ocorrência garantida.**

Aceite: 1) teste positivo com alvo colocado e carregado; 2) dois resultados negativos distintos (ausente vs. chunk descarregado) sem gerar chunks; 3) alvo destruído deixa de ser encontrado; 4) mesmo clique não cria loot nem duplica bloco; 5) jogador B não herda cooldown de A; 6) resposta não ocorre em dimensão negada; 7) coleta em Survival devolve o item esperado; 8) ausência de timer por tick/global scanner; 9) resposta perceptível com partículas MINIMAL por mensagem textual; 10) multiplayer dedicado + logs e métricas de tempo por uso.

**Critério de interrupção:** se playtest mostrar que musgo e shimmer sempre são percebidos juntos ou que shimmer natural é inviavelmente raro, não expandir o sistema. Ajustar pista/raio ou escolher outra unidade de recompensa em task de balanceamento, com evidência; não introduzir uma nova camada de abstração para mascarar o problema.

## 5. Classificação de recursos — duas dimensões independentes

**Origem/distribuição:** A compartilhado (ex.: Dream Dust como recurso de uso transversal, obtenção atual pendente), B compartilhado com abundância variável, C exclusivo de dimensão, D exclusivo de bioma/região. **Finalidade:** E decorativo/construção e F funcional (fabricação, detecção, efeito, dispositivo, interação). E e F podem coexistir com A–D; não são subclasses Java mutuamente exclusivas.

**Contrato de introdução:** ID estável e único; origem natural/receita/loot explicitados; tag de mineração/superfície coerente; BlockItem/modelo/blockstate/textura/tradução quando aplicáveis; crafting data-driven; efeito com lado server/client bem definido; necessidade real de persistência justificada; testes estáticos de assets/IDs, build, runtime/drops e smoke de Survival. Qualquer recurso sem “origem → ação → recompensa → utilidade” fica PENDENTE. Priorizar registries/tags/datapacks e composição; não criar classe abstrata por bloco.

## 6. Identidade de dimensões e seis regiões projetadas

Estas seis unidades são **design de regiões/biomas futuros**, não IDs existentes. Expansão depende de estudos de resolução 3D, habitats, tags e testes de compatibilidade com chunks gerados. Uma região pode começar como máscara de distribuição de feature sem criar um novo Biome. Em todas, construção é recompensada por uma paleta própria.

### Campo Onírico Azul — natureza, serenidade e movimento

| Região PROPOSTA | Visual / relevo / vegetação | Blocos, recurso e obtenção | Mecânica exclusiva e POI | Recompensa, progressão, construção | Risco e relação |
| --- | --- | --- | --- | --- | --- |
| Pradarias das Ondas | Dunas suaves azuis, gramíneas e jardins baixos sobre wave_height | blue_dream_grass, blue_dream_dirt, dream_flower e glow moss; colher e construir | **Trilhas de Pólen**: seguir plantas que sinalizam jardins mais densos; POI Jardim Luminoso (não implementado) | Flores/musgo iluminado para bases; aprendizado de leitura de sinal | Descoberta legível sem HUD, não pode parecer uniforme; acesso inicial aos Terraços |
| Cordilheiras da Névoa | Espinhas íngremes, rocha exposta, árvores sentinelas; ridge_height | blue_dream_stone, blue_dream_cobblestone e depósitos de pedra; mineração e fornalha | **Escuta da Névoa**: sinais localizados em mirantes; POI Espinha Ressonante | Orientação entre alturas e rocha para construção/crafting | Queda/neblina exigem sinalização clara; conversa com buff Slow Falling existente, sem substituí-lo |
| Terraços do Sonho | Camadas escalonadas e arcos, copa arqueada; terrace_height | pedra azul, madeira/folhas e musgo existentes; acesso por percurso vertical | **Sequência de Terraços**: observar padrões em formações; POI Arco Estratificado | Rotas alternativas, materiais de construção e futuro ingrediente de mobilidade (pendente) | Evitar salto obrigatório e spawn inacessível; ponte natural às pradarias e cordilheiras |

### Dreamscape — fragmentação, mistério, travessias e risco controlado

| Região PROPOSTA | Visual / relevo / vegetação | Blocos, recurso e obtenção | Mecânica exclusiva e POI | Recompensa, progressão, construção | Risco e relação |
| --- | --- | --- | --- | --- | --- |
| Bosques da Memória | Árvores de memória/testemunhas, chão escuro e jardins; continente e ilhas baixas | serene_stone, dream_grass, logs/leaves, dream_glow_moss, shimmer; colher em formações reais | **Eco do Musgo** (V0 compartilhável): pistas para shimmer perto de Jardim de Eco | Bloco de brilho para arquitetura, preparação de exploração; primeira fatia V0 | Shimmer não garantido; relações com Ermos e rotas para ilhas |
| Ermos das Rupturas | Fendas, costelas de pedra, monólitos, passagens rochosas | serene_stone e material geológico; novo recurso de fenda só quando tiver uso comprovado | **Leitura das Fraturas**: orientação por monólitos e passagens; POI Ponte Geológica | Descoberta de rotas e pontos seguros; futura ferramenta de navegação (pendente) | Não forçar travessias fatais nem confundir ponte testada com garantia universal |
| Ilhas da Vigília | Ilhas altas suspensas, raízes, áreas de observação e mobs | blocos já existentes, materiais exclusivos futuros apenas após validar valor | **Ritual do Vigia existente** e encontro com Fragmento; POI Mirante da Vigília | Pacto de resgate/Slow Falling temporários, material temático para construção | Habitat não equivale ao bioma; manutenção dos spawns atuais é condição de aceite |

**Dependências cruzadas:** recursos compartilhados geram motivos para visitar dimensões distintas; os futuros itens exclusivos devem enriquecer métodos de movimento/orientação e estética, nunca condicionar o retorno ao Overworld. O motor do sono pode levar a qualquer dimensão elegível mesmo sem uma melhoria futura.

## 7. Contratos técnicos mínimos (PROPOSTA, sem novas interfaces Java)

| Contrato conceitual | Responsabilidade, entrada e saída | Autoridade / persistência | Dependências, limites e teste / decisão de implementação |
| --- | --- | --- | --- |
| ExplorationDiscoveryService | Reconhecer POI por identidade estável e registrar descoberta individual ou compartilhada; entrada jogador + identidade do POI + dimensão; saída descoberta nova/repetida | Servidor; persistência por UUID/mundo somente se descoberta permanente for aprovada | **ADIADO** até existirem ≥2 tipos reais de POI. Não usar coordenada arbitrária sem regra anti-reposicionamento; testes de relog, dois jogadores e save |
| ResonanceService | Resolver sinalizador + perfil de alvos locais → encontrado/direção ou falha tipada; V0 pode ser um helper pequeno | Servidor, cooldown transitório V0; sem varredura por tick | Sem chamadas de worldgen/chunk-load. Reutilizar verificações de chunks e tags; testar 8125 probes, raio, empate, borda e multiplayer. Implementar apenas o necessário na 5.1 |
| ResourceDefinition / ResourceProfile | Metadados de origem, obtenção, tags, receitas, usos e renovabilidade | Datapacks, registros e loot já são fonte; persistência somente se houver estado adicional | **NÃO CRIAR CLASSE** na V0; expandir só diante de regras dinâmicas não expressáveis em JSONs/tags |
| DimensionGameplayProfile | Capacidade/política de interação por dimensão: existe sinal? quais alvos? quais regras? | Configuração server-side; não altera DreamDimensionDefinition nem o motor de sono | Inicialmente switch ou mapa pequeno justificado, depois perfil data-driven somente se houver múltiplas mecânicas |
| BiomeGameplayProfile | Regras de região/habitat que tags e biome JSON não cobrem; entrada biome + Y + superfície/geomorfologia | Servidor; derivado de mundo, não salvo por tick | **ADIADO** até expansão; para ilhas validar vazios/apoios e biomas múltiplos por conjunto/tag |
| InteractionResult / RewardPolicy | Tipificar encontrado, não-encontrado, indisponível, cooldown e abuso; conceder uma recompensa autorizada uma vez | Servidor; transação/replay guard somente para loot futuro | **Reutilizar InteractionResult vanilla** e resultados Java simples na V0. Política persistente só com recompensa explorável/duplicável |
| DreamAnchorService / SafeGroundNavigation | Resgate seguro e inspeção de apoio existentes | Servidor; vínculo transitório documentado | Não usar como navegação geral e não inventar destino, nem substituir o retorno pelo Despertador |

**Regra anti-sobreengenharia:** a arquitetura deve nascer da segunda necessidade comprovada. Primeiro um fluxo fechado, depois extração do padrão, nunca um framework abstrato sem casos reais.

## 8. Progressão e duração dos benefícios

| Estágio / ganho | Tipo | Efeito e uso | Restrição |
| --- | --- | --- | --- |
| Acesso às duas dimensões via sono | Permanente pelo mod/config, sem item de desbloqueio | Descobrir recursos, construir, reencontrar criaturas | Respeitar proteção do teleporte atual |
| Dust → infused stone → core → Despertador | Itens consumíveis/transformação + instrumento reutilizável | Construir e retornar; disponibilidade do Dust ainda exige validação | Sem bloquear entrada; não depender de geração de minério suposta |
| Shimmer e materiais de bioma | Materiais coletáveis finitos em cada depósito e expansíveis ao explorar novos chunks | Construção e identidade arquitetônica | Não criar farm por clique ou “loot grátis” no sinal |
| Slow Falling do Fragmento | Temporário | Mobilidade/segurança durante exploração | Não reimplementar nem estender sem playtest |
| Pacto de Vigília do Vigia | Temporário e consumível (uma carga) | Segurança na travessia de ilhas | Sem persistência de carga após restart; HUD não é autoridade |
| Futuro Afinador Onírico | Instrumento reutilizável, custos de criação propostos | Influenciar destinos quando dimension profile permitir | Nunca ignorar disponibilidade, retorno ou sonho aleatório gratuito |

## 9. Riscos, proteções e evidência

**Abusos:** a V0 não concede item; portanto farm infinito e duplicação de loot são prevenidos por design. Ainda é necessário impedir spam de scans/partículas (cooldown por UUID, orçamento por interação), negar espectadores, descartar dados de logout/death/clone/troca, não consultar chunks descarregados, não manter referência forte a jogador/nivel, testar alteração/destruição do alvo entre busca e coleta e evitar vazamento entre jogadores. Reinício não precisa persistir informação local. Permissões de comandos em mundo de testes não provam Survival; testar sem cheats.

**Mudança de biome_source:** risco P0/P1 para Fragmento e Vigia; nenhuma expansão de bioma deve entrar antes de adaptar predicados de habitat e medir distribuição com os mesmos seeds. **Alteração de worldgen não modifica chunks já explorados**: medir mundo novo e cópias de save, nunca prometer compatibilidade.

**Falhas observáveis:** ausência de alvo não é falha de servidor; requisito de mensagem diferenciada de carregamento parcial. Se API oficial não conseguir consultar bloco sem carregar chunk, retornar “sem sinal”, não tentar contornar. Falhas de servidor devem entrar em logger com dados agregados, sem spam por clique.

## 10. Plano de testes e definição de homologação

| Camada | Evidência esperada |
| --- | --- |
| Unitário | Testes puros de raio, custo, desempate de alvos, elegibilidade, cooldown e estados da interação; preservar WeightedDimensionPickerTest, FlightProfileTest e DreamAnchorPolicyTest |
| Estático | CI de IDs, JSON, recipes, loot, tags mineable/needs_iron_tool, assets/pt_br/en_us, referências cruzadas, checks existentes em scripts/ci |
| Integração | Servidor 26.3 lê datapacks e tipos; interação real server-authoritative; coleta de shimmer com picareta; mesa de crafting; nenhuma regressão de sono/retorno/mobs |
| Mundo | Seeds 14011, 20261108, -904732, 73199013 em mundos NOVOS e chunks já gerados separados; contador de POIs, busca/alcance, locais negativos, geometria e MSPT/latência por interação |
| Multiplayer | Dois usuários, uma região e duas regiões, alternância de mão, duplicação de eventos, logout/relogin, morte, mudança dimensional, restart, permissão e logs |
| Manual | O jogador percebe pista e entende falha/sucesso sem comando, encontra recurso e usa em construção; Survival sem cheats. Registrar capturas e tempo até primeiro uso |

**Gates distintos, nunca sinônimos:** (1) build aprovado; (2) datapacks/registries carregados; (3) lógica exercitada em servidor e logs; (4) gameplay homologado manualmente; (5) performance comparada em hardware e saves equivalentes. Script CI não substitui passos 4 e 5.

Medições-alvo iniciais **a validar**, não resultados: busca responde em evento (não tick), teto teórico 8125 posições, rate limit 5s/usuário, nenhuma geração forçada. Para distribuição: contar Jardins de Eco com e sem shimmer em áreas novas de cada seed, registrar distância mediana musgo↔shimmer e porcentagem de jardins com alvo alcançável; **não definir taxa aprovada sem amostra**. Para benchmark de MSPT/FPS utilizar três passadas e mesmas configurações.

## 11. Decisões de arquitetura e critérios de revisão

1. **D-01:** preservar sono/retorno e IDs; todo ganho é optativo.
2. **D-02:** criar primeiro interação de alcance curto reaproveitando blocos atuais; POI indexado fica para depois.
3. **D-03:** não adicionar itens, minerais ou biomas sem cadeia de valor e teste Survival.
4. **D-04:** antes de expandir biomas Dreamscape, desacoplar o habitat de um único ResourceKey de bioma, com regressão de mobs.
5. **D-05:** manter bioma, geomorfologia, superfície, habitat e POI como conceitos independentes.
6. **D-06:** não confundir registro/compilação com geração natural; corrigir/validar biome modifier de minério em task própria.
7. **D-07:** usar tags, recipes, loot e datapacks antes de criar novos perfis Java.
8. **D-08:** projetar multiplayer e ausência de chunk-load desde a primeira mecânica.

### Evidências e referências efetivamente consultadas

**CÓDIGO da baseline:** gradle.properties; build.gradle; classes de dimension/, service/, event/, attachment/, util/, config/, registry/, item/, block/, entity/, entity/habitat/, entity/spawn/, entity/behavior/, entity/navigation/, worldgen/; JSONs em data/dreamsdimensions/{dimension,dimension_type,worldgen,recipe,loot_table,tags}; tags vanilla em data/minecraft/tags/block. Relatórios históricos relevantes: DREAM_ENGINE_TASK_2_2, DREAM_MOB_FRAMEWORK_TASK_4_4, DREAMSCAPE_TASK_3_2, DREAMSCAPE_TASK_3_4_HOMOLOGACAO, CAMPO_ONIRICO_AZUL_TASK_1_4_HOMOLOGACAO, VIGIA_PACTO_HUD_TASK_4_7.

**DOCUMENTAÇÃO OFICIAL consultada:** [NeoForge primer Minecraft 26.2 → 26.3](https://docs.neoforged.net/primer/docs/26.3/) e [NeoForge Biome Modifiers](https://docs.neoforged.net/docs/worldgen/biomemodifier/) (página geral atualmente rotulada 26.1; validar o caminho exato com loader 26.3 no runtime antes de correção). Referências de continuidade: [NeoForge docs](https://docs.neoforged.net/docs/), [NeoForge source](https://github.com/neoforged/NeoForge), [ModDevGradle](https://github.com/neoforged/ModDevGradle), [Minecraft Wiki](https://minecraft.wiki/).

**PROPOSTAS:** Eco do Musgo, seis regiões, perfis gameplay, progressão futura, Afinador. **PENDENTES:** origem natural do Dust, chance real de shimmer próximo, satisfação de exploração, coexistência multiplayer, compatibilidade entre biomas e saves.

**Ato administrativo da Task 5.0:** somente estes três arquivos de documentação são adicionados; nenhuma implementação existente foi modificada. O roadmap estabelece o contrato de implementação da Task 5.1.
