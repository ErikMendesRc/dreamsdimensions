# TASK 5.0 — Roadmap executável de gameplay e exploração

> **ATUALIZAÇÃO TASK 5.2 — 10/11/2026:** A numeração 5.2–5.7 nesta seção reflete o plano **histórico** da Task 5.0. O contrato posterior de viagem/retorno usa **5.2 Contrato**, **5.3 Colar**, **5.4 Despertador Consumível**, **5.5 Respawn**, **5.6 Economia Overworld**, **5.7 Ruínas de Recuperação**. Para trabalhos futuros nesses fluxos, prevalecem [TRAVEL_RETURN_CONTRACT_TASK_5_2.md](TRAVEL_RETURN_CONTRACT_TASK_5_2.md) e [matriz de 30 cenários](TRAVEL_RETURN_TEST_MATRIX_TASK_5_2.md). Não renumerar retroativamente relatórios anteriores.


**Escopo de planejamento:** Minecraft 26.3 / NeoForge 26.3.0.101 / Java 25 / ModDevGradle 2.0.148 / GeckoLib 5.5.7.  
**Branch de execução:** migration/minecraft-26.3; **NUNCA merge automático na main**.  
**Base auditada:** dd614f4aef2ea3d7ed7ed24fb3250cfa5b62823c.  
**Fontes:** [Contrato](GAMEPLAY_EXPLORATION_TASK_5_0.md) e [Matriz rastreável](GAMEPLAY_CONTENT_MATRIX.md).

> Este arquivo é a ordem de dependências, não permissão para implementar as tasks futuras. Na Task 5.0 foram criados somente documentos. Toda task seguinte exige testes de código, mundo e regressão proporcionais ao que muda.

## 1. Sequência técnica decidida

| Ordem | ID e entrega | Dependência para iniciar | Resultado necessário para avançar | Barreira de implementação |
| --- | --- | --- | --- | --- |
| 1 | **5.1 — Eco do Musgo, fatia vertical V0** | 5.0; mundo de teste disponível | Clique → sinal → coleta de shimmer → construção em Survival | Não criar biomas, chunks, mob, item, novos assets ou geração |
| 2 | **5.2 — Homologação V0 e regressão do motor/mobs** | 5.1 com build/servidor | Mapa de falhas, teste in-game e multiplayer, métricas e correções cirúrgicas | Não expandir conteúdo sem evidência |
| 3 | **5.3 — Cadeia de recursos em Survival e auditoria de minério** | 5.2 estável | Gerar minério real → dream_dust → infused stone/core/Despertador → retorno | Confirmar antes de mover biome modifier; preservar IDs |
| 4 | **5.4 — Distribuição natural e legibilidade de POIs** | 5.2 e baseline 5.3 | Jardins, sinalizadores, recompensas e rotas aparecem na frequência adequada | Rebalance só com medição e regressão |
| 5 | **5.5 — Preparação multi-bioma sem regressão de habitats** | 5.4 e matrizes de seed | FloatingIslandHabitat e profiles funcionam com biomas múltiplos sem perda de spawn | Não ativar seis biomas de uma vez |
| 6 | 5.6 — Primeiro rollout regional/bioma por dimensão | 5.5 | 1 região diferenciada por vez, com mecânica, recurso e POI validados | Não substituir worldgen em saves existentes sem estratégia |
| 7 | 5.7 — Descobertas persistentes e Ressonância ampliada | 5.6 e necessidade de ≥2 tipos de POI | Registro por jogador/grupo e localização escalável | Sem scans globais nem duplicação por reposicionamento |
| 8 | 5.8 — Progressão opcional e possível Afinador Onírico | recursos de ambas as dimensões validados | Maior controle de destino sem bloquear sono e retorno | Recompensa funcional e custo proporcional |
| 9 | 5.9 — Mobs novos orientados por necessidades regionais | 5.6–5.8, gap comprovado | Nova criatura resolve mecânica não atendida por mobs atuais | Reusar população, habitat, locomoção e interação |

Ordem deliberada: **contrato → mecânica V0 mínima → homologação → origem dos recursos → distribuição natural → compatibilidade multi-bioma → biomas → progressão → novos mobs.** A infraestrutura mínima de 5.1 é implementada dentro da própria fatia e só é extraída para componentes genéricos quando houver uma segunda necessidade.

## 2. TASK 5.1 — Contrato PRONTO PARA IMPLEMENTAÇÃO

### 2.1 Objetivo e definição de valor

Implementar uma única ação de exploração que reutilize recursos existentes:

    EXPLORAR Dreamscape
        → OBSERVAR dream_glow_moss
        → INTERAGIR com mão principal vazia
        → RECEBER direção de dream_shimmer_block próximo
        → LOCALIZAR e MINERAR com picareta
        → USAR shimmer em construção
        → BUSCAR outro jardim

O Eco do Musgo **não é missão, não desbloqueia dimensão, não concede itens e não substitui a sintonização do Fragmento Errante**. Serve como primeiro contrato reutilizável de pista ambiental. Se a avaliação de jogador não demonstrar valor, interromper evolução antes de criar um framework de descoberta.

### 2.2 Dependências obrigatórias

1. Auditar no HEAD real as classes ModBlocks, ModItems, CommonEvents, DreamsDimensions, DreamscapeEcosystemFeature, event handlers existentes, registro de tags, loot_table/blocks/dream_shimmer_block.json, recipe/ e mundo dreamscape.
2. Ler o contrato da Task 5.0, seção 4, e os riscos da matriz GX-S01. Revisar as documentações oficiais de eventos/interação e mudanças da API 26.3 no momento da implementação. Não inferir assinatura de evento de versões 1.20/1.21.
3. Inspecionar testes e scripts existentes. Não modificar o motor de sono, retorno, sintonização do Fragmento, pacto do Vigia, model/texture, receitas, worldgen ou ID de registro.
4. Confirmar que dream_glow_moss, dream_shimmer_block e tags/loot existentes são válidos em jogo. Usar mundo de teste novo ou área editável, sem alterar estruturas do projeto.

### 2.3 Arquivos provavelmente afetados

**Criar apenas quando necessário:** uma classe pequena de lógica de interação em event/ ou service/ (por exemplo, EchoMossInteractionHandler), um helper puro para limites/alvos se trouxer ganho de teste, um teste JUnit puro e um check estático em scripts/ci quando compensar. **Modificar minimamente:** a inscrição do evento em CommonEvents ou DreamsDimensions (não registrar dois handlers), traduções lang/pt_br.json e lang/en_us.json se forem usadas chaves, documentação da task e um CI hook existente se houver padrão.

**Evitar modificar:** ModBlocks, ModItems, registries, biome JSON, noise, Feature, placer, BlockEntity, DreamMobPopulationService, DreamTeleportService, DreamReturnHelper, modelo/animação GeckoLib, hud do Pacto, default config. Se o listener não for viável com a API 26.3, avaliar uma mudança focal do bloco e documentar a razão antes de escrever; não refatorar registros por estética.

### 2.4 Invariantes e dados de entrada

**Gatilho:** evento real de interação com bloco (ou método de bloco vanilla/NeoForge compatível com 26.3, após verificação) no lado lógico do servidor. Condições simultâneas:
- Jogador é ServerPlayer vivo, não espectador, modo Survival/adventure apenas se quebra e coleta forem permitidas; para V0, aprovar **Survival** como escopo de recompensa.
- Level.dimension é dreamsdimensions:dreamscape; NÃO depender do bioma ser obrigatoriamente dreamscape_biome na interação.
- Bloco clicado é dreamsdimensions:dream_glow_moss.
- Mão principal ativa (MAIN_HAND) e ItemStack principal vazia; ignorar OFF_HAND para evitar dupla ativação.
- Jogador tem alcance vanilla válido para interação, sem atalhos de teleporte ou permissão administrativa.
- Não existe cooldown válido no servidor para UUID e dimensão atual. A checagem vem **antes de qualquer scan**.

**Busca determinística de candidato:**
- Centro = BlockPos do musgo clicado; caixa fechada X/Z em -12..+12, Y em -6..+6.
- Distância horizontal máxima em disco: dx² + dz² <= 144. Distância vertical máxima |dy| <= 6.
- Máximo geométrico da caixa: 25×25×13 = **8125 posições**; descartar cedo posições fora do disco e mundo. Nenhum loop recorrente por tick.
- Para cada posição, antes de qualquer leitura de bloco: validar altura, world border, chunk já carregado (API equivalente a hasChunkAt ou consulta comprovadamente não carregadora). **Nunca usar getChunkAt para trazer chunks**, nunca tickets, nunca chamar generate.
- Alvo é exatamente dreamsdimensions:dream_shimmer_block existente; escolher o menor deslocamento Euclidiano quadrático 3D (do centro); desempatar por X, depois Y, depois Z crescentes para garantir resultado estável independente da ordem de iteração.
- Se candidato estiver atrás de parede, o sinal ainda pode indicá-lo; esta V0 não computa pathfinding e não promete caminho seguro. Se for um ponto inalcançável na prática, isso será feedback de playtest.
- Proibido varrer um chunk inteiro por interação, indexar globalmente POIs ou armazenar referência a chunk/player no estado persistente.

### 2.5 Respostas esperadas

| Situação | Resultado autoritativo | Feedback ao cliente | Inventário / mundo |
| --- | --- | --- | --- |
| Alvo encontrado e carregado | sucesso com coordenada alvo e direção | 1 sequência curta de 1–2s com até 12 partículas vanilla ao longo do vetor + som vanilla + mensagem contextual pt_br/en_us | sem conceder/mover item, sem escrever bloco |
| Alvo não encontrado (inclui alvo em chunk indisponível) | no_target, sem alegar que não existe no mundo inteiro | mensagem breve “Nenhum eco próximo.”, sem partículas de direção | nenhuma mudança |
| Cooldown vigente | cooldown | sem scan nem spam de feedback; opcional mensagem rate-limited | nenhuma mudança |
| Player/hand/dimension inválidos | ignored | nenhum feedback | continuar comportamento vanilla normal |
| Alvo destruído antes de nova interação | recalcular; pode virar no_target | resultado atualizado | nenhum item criado |
| Jogadores A e B clicam ao mesmo tempo | dois processos independentes por UUID | pistas independentes | o mesmo bloco permanece material normal, sem loot duplicado |

**Fluxo de click:** o handler intercepta exclusivamente o clique elegível e marca evento/resultado como tratado somente na forma aprovada pela API 26.3. Em qualquer outro uso deve preservar interação vanilla. Evitar emitir feedback duplicado em cliente e servidor. A interação não minera o musgo.

**Cooldown inicial proposto:** 100 ticks de jogo (5 segundos) por UUID; inclusive resultado no_target para impedir flood de buscas. Map transitório limitado aos jogadores presentes; limpeza em logout, clone/morte e mudança de dimensão. Se o relógio de nível mudar/regredir, não manter cooldown inválido. Não usar attachment persistente só para cooldown. Resposta client-side nunca controla elegibilidade.

**Parâmetros configuráveis:** centralizar constantes nomeadas; não criar config de jogador e datapack de perfis ainda. Se for necessário expor ajuste para 5.2, manter estritamente server-side e com default documentado. A busca não deverá ter custo O(n) global de mundo; limite fixo por clique.

### 2.6 API e arquitetura — sem inventar classes da 26.3

- Consultar no projeto e documentação o evento NeoForge de clique direito e sua forma real de cancelamento/resultado na 26.3; verificar se um bloco simples como ModBlocks.DREAM_GLOW_MOSS recebe a ação com mão vazia. Só então codificar.
- Um serviço/helper pequeno e testável recebe predicado/limites e procura alvo **em blocos carregados**. Não implementar ExplorationDiscoveryService, ResourceProfile, catálogo de POI ou redes de pacotes customizados sem necessidade.
- Para mensagem e partícula, usar recursos vanilla acessíveis no lado servidor e traduções existentes/novas. A experiência precisa continuar compreensível com Particle Settings = MINIMAL, via texto.
- Usar um único registrador de eventos, nos padrões já existentes do projeto. Nunca instalar um PlayerTickEvent por player e nunca inserir lógica no tick do DreamMobPopulationService.
- Reuso futuro: os limites/algoritmos de busca e a apresentação podem ser parametrizados após homologar segundo sinalizador. Na V0, preferir simplicidade à generalização.

### 2.7 Unit tests, estáticos, integração e segurança

**Unit tests** para helper puro, se extraído:
1. região exata inclui 12 blocos em XZ e exclui 13, respeita ±6 Y;
2. teto de posições 8125 e filtro de disco; nada fora da dimensão/altura;
3. alvo único, dois alvos e desempate determinístico;
4. retorno no_target em zero candidatos e em chunk falso/carregamento ausente;
5. cooldown bloqueia novo scan antes de qualquer trabalho;
6. dois UUID não compartilham cooldown, e limpeza de estado é determinística.

**Testes estáticos:** referências a IDs exatos registrados, nenhuma alteração de registries/worldgen/recipes/loot, assets intactos, traduções em pt_br/en_us, nenhum import de eventos/client no lado dedicado, nenhuma duplicação de handler; compilar JDK 25 e rodar testes Gradle.

**Integração/manual em cliente e servidor dedicado** com mundo novo de seed 20261108:
1. Usar comandos SOMENTE para preparar laboratório: dois pisos de dream_glow_moss e um shimmer a 8 blocos na mesma região; desativar cheats e operar em Survival.
2. Clicar com mão vazia; mensagem/som/sinal; colher shimmer com picareta; confirmar bloco no inventário e colocá-lo em uma construção.
3. Repetir sem shimmer para no_target; colocar shimmer no limite e além do limite; testar Y +6 e Y +7.
4. Criar alvo na borda de chunk com parte do volume descarregada; nenhuma geração forçada; estudar uso com debug/performance; o resultado deve ser honesto.
5. Clicar com item na mão e mão secundária; nenhum acionamento duplicado nem impedimento do item vanilla.
6. A e B no mesmo lugar: respostas/cooldown independentes; desconectar A e clicar com B; voltar como A.
7. Trocar para Overworld/Campo Azul, morrer, retornar, reiniciar servidor; não deixar cooldown zumbi, crashes ou marcador persistente.
8. Regressões: dormir por 100 ticks, entrar nas duas dimensões, Despertador 60 ticks e retorno seguro; Fragmento (sintonização/Slow Falling) e Vigia (Pacto/HUD) continuam iguais.
9. Repetir inspeção de eco em jardim natural real; NÃO declarar que funciona naturalmente somente porque laboratório manual passou.

**Casos de abuso a provar:** spam 100 clicks em servidor, multiusuário, alvo removido, world border, mundo sem target, chunks indisponíveis, uso com comando de teleporte, restart, jogador espectador/creative e sincronização de eventos. Nenhum resultado pode duplicar loot, alterar inventário, criar blocos ou anexar um ticket de chunk.

**Métricas:** contagem de interações aceitas/ignoradas e resultados sucesso/no_target (logs debug rate-limited), duração por interação em ms para profiling, posições efetivamente lidas, variação de MSPT/TPS e observação FPS. Não registrar nomes/UUIDs completos em logs de telemetria; usar contagens agregadas. Não comparar hardware ou seed diferentes para alegar ganho.

### 2.8 Entrega e definição de pronto da 5.1

- Implementação pequena e documentada; nenhum bloco/item/bioma/mob/asset novo.
- Build JDK25, teste estático e unitário verde; servidor dedicado inicia e datapacks carregam.
- Laboratório Survival com sucesso e falha, shimmer coletado e colocado, cooldown e duas pessoas testadas.
- Regressão mínima do teleporte e das duas criaturas.
- Zero chamadas que gerem chunks; limites/timeout verificados.
- Relatório docs/RESONANCE_TASK_5_1.md com resultado, seed, coordenadas de teste, comandos de preparação, evidência e limites **reais**; não inventar capturas/logs.
- Um commit de implementação na branch migration/minecraft-26.3, sem merge. Se a homologação manual não puder ser executada, registrar como PENDENTE, não como concluída.

**Fora de escopo da 5.1:** novos biomas, variação de worldgen, modificadores de minério, novos blocos/itens, sons customizados, texturas, mobs, sistemas de talentos, loot por clique, desbloqueio de teleporte, descoberta persistente, tela de HUD personalizada, grandes refatorações.

## 3. TASK 5.2 — Homologação da V0 e regressão sistêmica

**Objetivo:** verificar valor de gameplay, segurança e estabilidade antes de escalar.

- **Dependência:** 5.1 com código compilável. **Arquivos:** testes em src/test, scripts/ci, docs/RESONANCE_TASK_5_1.md, eventualmente handler da V0 para bugfix; nenhuma feature nova.
- **Criar:** matriz de teste com sucessos/falhas, seeds 14011/20261108/-904732/73199013, cenário multiplayer, rastreio do tempo até perceber um sinal e até coletar shimmer; 3 passadas para benchmark comparável.
- **Reusar:** build/CI, pipeline de smoke dedicado, DreamSpawnHelper/ReturnHelper, mobs, scripts de worldgen existentes, relatório de Task 3.4.
- **Fora de escopo:** novos recursos/biomas, rework de terreno, “otimização” sem profiling.
- **Aceite:** laboratório funciona em Survival e multiplayer; teste no mundo natural e no mundo sem alvo, feedback MINIMAL legível; sleep/return/mobs intactos; logs e capturas identificadas.
- **Riscos:** shimmer raro/invisível, sinal redundante, queda ao minerar. **Testes:** olhar a experiência sem coordenadas ou comandos, colisão, chunk boundary, relog e FPS/MSPT.
- **Pronto:** decisão documentada GO (expandir), ADAPT (mudança focal) ou STOP (protótipo sem valor) com evidência; se falhar, 5.3 não é bloqueada para auditoria, mas expansão de Eco fica congelada.

## 4. TASK 5.3 — Origem de recursos e crafting Survival

**Objetivo:** fechar uma segunda cadeia de valor: minério natural → dream_dust → dream_infused_stone → oneiric_core_block → oneiric_awakener → retorno.

- **Dependências:** contrato 5.0, regressão 5.2 ou estado GO/ADAPT documentado. **Arquivos prováveis:** src/main/resources/data/dreamsdimensions/worldgen/biome_modifier/dream_ore_overworld.json; possível migração para src/main/resources/data/dreamsdimensions/neoforge/biome_modifier/dream_ore_overworld.json **somente se confirmado para NeoForge 26.3**; scripts/ci/check_dimensions.py e novos testes; docs/RESOURCE_CHAIN_TASK_5_3.md.
- **Criar/modificar:** prova de carregamento de biome modifier e, se incorreto, correção mínima do path; instrumentar distribuição com novo mundo. Revisar se #minecraft:is_overworld e targets de ore condizem com a versão.
- **Reusar:** ModBlocks, ModItems, DreamOreBlock, loot_table e recipe existentes, tag needs_iron_tool; manter ambas receitas de blue_dream_stone.
- **Fora de escopo:** minério exclusivo novo, fornalha nova, mudança de tamanho dos veios sem medição, alteração de IDs/receitas existentes, geração on-tick.
- **Aceite:** Minecraft 26.3/NeoForge carrega modifier, dois veios amostrados por range em seeds novas, drops com/sem Silk Touch e Fortune, 1–3 XP, receitas em survival, despertar em cama real e retorno seguro.
- **Riscos:** path incorreto, datapack antigo 26.1 vs 26.3, ausência de chunk novo, taxas excessivas ou zero, regressão de loot. **Testes:** unit/estáticos JSON, dedicated server e coleta real em mundos limpos.
- **Pronto:** provar com contagens e logs que Dust é acessível sem /give; se não, marcar P0 e não aprovar progressão que depende dele.

## 5. TASK 5.4 — Distribuição natural e pontos de interesse

**Objetivo:** verificar e, se necessário, rebalancear sinalizadores e recompensas naturais sem alterar topografia aprovada.

- **Dependências:** 5.2 decide utilidade da V0 e 5.3 valida acesso a recursos. **Arquivos:** DreamscapeEcosystemFeature, worldgen/{feature,placed_feature}, verificadores em scripts/ci, docs/NATURAL_EXPLORATION_TASK_5_4.md. Alterar somente se métricas indicarem problema.
- **Criar:** levantamento de Jardins de Eco com dream_glow_moss e shimmer (número de jardins, células, porcentagem com shimmer, distância média/mediana ao musgo e taxa de sucesso do Eco), amostra em quatro seeds. Cobrir também flora/rochas do Campo Azul.
- **Reusar:** mundo/feature existente, metrics opt-in de worldgen, CI e tags, V0 de 5.1.
- **Fora de escopo:** novos biomas, estruturas sofisticadas, minério/loot sem cadeia, mudança indiscriminada da densidade.
- **Aceite:** locais exploráveis e legíveis em Survival; se houver ajuste, é cirúrgico, determinístico por seed e não viola ensureCanWrite, spawn protection ou ponte aprovada.
- **Riscos:** aparência repetida, custo de feature, escrita fora do chunk, dados de apenas uma seed. **Testes:** contagens por mundo novo, capturas in-game, borda de chunks, MSPT/FPS comparáveis.
- **Pronto:** aceitar distribuição ou justificadamente ajustar e repetir regressão; relatório quantifica resultados sem vender probabilidade teórica como ocorrência real.

## 6. TASK 5.5 — Fundação compatível com múltiplos biomas

**Objetivo:** tornar o habitat/spawn existente robusto a variação de bioma antes de introduzir IDs regionais, mantendo a detecção física de ilha.

- **Dependências:** 5.4 com referências de terreno e baseline de população. **Arquivos:** FloatingIslandHabitat, FragmentoIslandHabitat, VigiaIslandHabitat, DreamMobHabitat, MobPopulationProfile e testes novos; possivelmente tags de bioma na task futura, **sem** substituir agora os dois biome_source fixed.
- **Criar:** contrato de elegibilidade baseado em conjunto/tag de biomas permitidos + dimensão + solo tag + faixas Y + amostragem de vazio sob piso real; refatoração mínima/composição somente se concretamente exigida.
- **Reusar:** DreamMobPopulationService, limites 200/360 ticks, cotas atuais 6/2, classes de criatura, animações, DreamAnchorService, SafeGroundNavigation.
- **Fora de escopo:** criar seis biomas de uma vez, reescrever IA, trocar spawn em bloco por tick, relaxar altura ou custo de população, alterar animação/GeckoLib.
- **Aceite:** bioma atual continua válido; outro bioma de teste permitido aceita o mesmo habitat se houver terreno de ilha; bioma proibido e superfície continental rejeitados; spawn natural e manutenção de população continuam estáveis, inclusive multiplayer.
- **Riscos:** bioma não acompanha Y esperado, mob spawn some ou aparece no continente, probes carregam chunks. **Testes:** caso de baixo/alto Y e abismo sob piso, loaded-only, seed 20261108, dois jogadores/célula e duração de interações existente.
- **Pronto:** novos biomas podem ser ativados um por vez sem quebrar Fragmento/Vigia, após comprovação em mundo real; a própria expansão permanece na 5.6.

## 7. TASK 5.6 — Rollout controlado de regiões

**Objetivo:** deixar as dimensões menos repetitivas **depois** de garantir uma recompensa e uma infraestrutura de habitat que aguentem a expansão.

- **Dependência:** 5.5; também precisa de cadeia de valor de 5.3 e distribuição de 5.4. **Arquivos:** dimension/*.json (biome_source), worldgen/biome/, material_rule/, features/placed_features e tags; eventualmente config de perfil, apenas quando houver uso real.
- **Criar:** primeiro uma região/bioma piloto em UMA dimensão (recomendação: Bosques da Memória como região de gameplay sem mexer na ilhas, ou Pradarias das Ondas com novo bioma efetivo, após teste técnico de biome resolution); incorporar os demais cinco gradualmente.
- **Reusar:** os seis perfis de design do contrato, density functions e vegetação já homologadas.
- **Fora de escopo:** rewrite total da worldgen, novos recursos sem utilidade, migrar chunks existentes como se fossem regenerados.
- **Aceite:** o jogador distingue região em 30–60s de exploração por forma/recurso/interação; recursos se cruzam entre regiões; spawn e retorno permanecem seguros.
- **Riscos:** interpolação 3D vertical, habitats, surface rules, dados antigos. **Testes:** seeds novas + cópias de saves, limites de bioma, dimensões sobrepostas, survival e benchmark.
- **Pronto:** lançar região piloto e expandir somente com GO em testes de conteúdo, não por meta numérica de biomas.

## 8. TASK 5.7 — Descobertas persistentes e ressonância ampliada

**Objetivo:** somente após duas famílias distintas de POIs já comprovadas, reconhecer visita e permitir orientação de maior alcance.

- **Dependência:** 5.6, GX-S01 homologada e prova de necessidade. **Arquivos prováveis:** service/ novo, registro de attachment ou SavedData **apenas com motivo**, data profiles/tags e testes de sincronização.
- **Criar:** identidade estável de POI (não somente bloco reposicionável), política de descoberta pessoal/compartilhada, query indexada carregada com o chunk e feedback de novidade.
- **Reusar:** algoritmo local de Eco, serviços de mundo e datapacks; não duplicar registro de POIs por mob.
- **Fora de escopo:** GPS de todo o mapa, scan de chunks não gerados, recompensas farmáveis por server restart.
- **Aceite:** duplicidade bloqueada, dois jogadores funcionam conforme política, save/reload e limites de memória comprovados.
- **Riscos:** ID instável, chunk unload e custos permanentes. **Testes:** ataques por reposicionar, relog, death, multiple saves, carga de chunks e servidor dedicado.
- **Pronto:** primeira descoberta funcional, persistência e benchmarking; senão manter apenas V0 sem catálogo.

## 9. TASK 5.8 — Progressão opcional e Afinador

**Objetivo:** motivar revisitas a ambas as dimensões, sem trocar o motor de sono.

- **Dependência:** cadeias de recurso de cada dimensão e duas razões para regressar já provadas. **Arquivos:** receitas/itens só quando aprovados, extension point do DreamTeleportService, profile complementar ao catálogo.
- **Criar:** proposta de receita com ao menos um material de cada dimensão e política de influenciar preferências do sorteio. O instrumento não cria nem habilita dimensões.
- **Reusar:** DreamDimensionDefinition, WeightedDimensionPicker, DreamsConfig, retorno seguro.
- **Fora de escopo:** quests obrigatórias, acesso bloqueado, mudança de spawn ou peso default, portal substituto de sono.
- **Aceite:** sem instrumento, 100 ticks e seleção original iguais; com instrumento, nunca escolhe mundo não carregado ou desabilitado; retorno seguro e cooldown intactos.
- **Riscos:** seleção enviesada sem feedback, exploits, multiplayer. **Testes:** universo de destinos 0/1/2, falhas de spawn, dois usuários, save/reload e playtest.
- **Pronto:** custo/benefício demonstrado em Survival sem grind artificial.

## 10. TASK 5.9 — Mobs por função de gameplay

**Objetivo:** preencher lacunas concretas de exploração e não decorar regiões com entidades sem propósito.

- **Dependências:** 5.6–5.8 e decisão documentada sobre a lacuna. **Arquivos:** novas entidades/assets GeckoLib somente após ficha técnica e mecânica aprovada; profiles do framework existente.
- **Criar:** um mob por vez com comportamento, habitat, interação, recompensa, cap, animação e critérios de UX verificáveis.
- **Reusar:** MobPopulationProfile, DreamMobPopulationService, DreamMobHabitat, CalmPlayerInteraction, SafeGroundNavigation, FlightProfile quando voo fizer sentido.
- **Fora de escopo:** novo sistema de spawn por espécie, entidade sem recompensa/razão, IA em tick de player.
- **Aceite:** criatura introduz interação que Fragmento/Vigia não cobrem, visual voxel consistente e Survival sem comandos, baixa sobrecarga.
- **Riscos:** spawn em superfícies inválidas, animações dessincronizadas, duplicação de mobs. **Testes:** cliente, servidor dedicado, duas pessoas, chunks e save.
- **Pronto:** homologar primeiro mob individualmente e somente depois planejar o próximo.

## 11. Gates de execução entre tasks

**Antes de todo commit:** revisar branch HEAD, diff e escopo; fontes oficiais 26.3 e classes reais; teste estático/unitário/build; runtime dedicado quando houver código; evidência no mundo para afirmar gameplay. Preferir um commit por task e evitar merge não autorizado.

**G0 — contrato:** os três documentos 5.0 referenciam-se e não alteram runtime.  
**G1 — fatia V0:** mão vazia → feedback → obter shimmer → construir.  
**G2 — jogabilidade:** entender pista e repetir experiência em Survival, inclusive duas pessoas.  
**G3 — recursos:** Dust naturalmente disponível + crafting end-to-end ou pendência P0 explícita.  
**G4 — distribuição:** taxas e visibilidade medidas em seeds novas, sem falsos positivos.  
**G5 — biomas:** habitats e spawn robustos, chunks antigos tratados separadamente.  
**G6 — escala:** novo bioma/item/mob só se sua cadeia de valor tiver validação anterior.

**Critério de bloqueio universal:** build verde com datapack rejeitado ou distribuição não observada não libera próximo estágio. Se um teste não foi executado, assinalar PENDENTE e documentar precisamente por que.

## 12. Resumo de risco técnico para execução

1. **P0: biome modifier dos minérios** usa data/dreamsdimensions/worldgen/biome_modifier, mas a documentação geral NeoForge descreve data/dreamsdimensions/neoforge/biome_modifier; validar no runtime 26.3 antes de mover e gerar chunk novo para testar.
2. **P1: bioma fixo como habitat:** FloatingIslandHabitat depende de ResourceKey<Biome> único; antes de novos biomas substituir por política segura multi-bioma.
3. **P1: POI natural com recurso raro:** Jardim de Eco distribui shimmer com probabilidade nominal por célula, não por jardim, e pode não gerar nenhum; evitar promessa de densidade.
4. **P1: tick e chunks:** ação deve ser limitada no clique, nunca usar serviços de população dos mobs para varredura de POIs.
5. **P1: compatibilidade:** IDs, codec de attachment e mundo salvo devem ser preservados; testar cópias de save somente após mundo novo.
6. **P2: sobreengenharia:** não registrar APIs Java para Source/Reward/Discovery enquanto tags, loot, recipes e um helper local bastarem.

### Referências técnicas de execução

- [Migration Primer 26.2 → 26.3](https://docs.neoforged.net/primer/docs/26.3/)
- [NeoForge documentação geral](https://docs.neoforged.net/docs/)
- [NeoForge Biome Modifiers](https://docs.neoforged.net/docs/worldgen/biomemodifier/) — página geral 26.1; validar diferenças no runtime 26.3
- [NeoForge source](https://github.com/neoforged/NeoForge)
- [ModDevGradle](https://github.com/neoforged/ModDevGradle)
- [Minecraft Wiki](https://minecraft.wiki/)

**Nenhuma das Tasks 5.1–5.9 está implementada por este roadmap.**


## 11. Addendum 5.2 — Ordem atual de viagem/retorno (supera apenas as tarefas homônimas para fluxos oníricos)

| Ordem atual | Entrega | Dependência/aceite | Status em 10/11/2026 |
| --- | --- | --- | --- |
| 5.2 | Contrato e auditoria | 30 cenários, preservação de baseline; nenhuma alteração runtime | Documentação na branch |
| 5.3 | Colar e elegibilidade de sono | Validar Curios 26.3; slot real; chance uma vez em sono completo; *não lançar isoladamente* | Pendente |
| 5.4 | Retorno consumível | Corrigir `finishUsingItem`: 1 unidade após confirmação; falhas sem gasto | Pendente |
| 5.5 | Respawn/sono onírico | `can_sleep=never`, âncora separada, morte sem saída gratuita; Hardcore vanilla | Pendente |
| 5.6 | Economia Overworld | Minério e Dust natural, crafting difícil, medir rendimento | Pendente |
| 5.7 | Ruínas/recuperação | Loot raro acessível nas duas dimensões; medir P90/P99 de recuperação sem gear | Pendente |

**Dependência de release:** 5.3 + 5.4 + 5.5 + 5.6 + 5.7 testadas em Survival/Multiplayer antes de oferecer a nova regra aos jogadores. A ordenação histórica das outras tarefas de exploração acima não foi removida nem reescrita.
