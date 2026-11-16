# TASK 5.1 — Eco do Musgo (Ressonância Ambiental V0)

**Branch:** migration/minecraft-26.3  
**Baseline inicial:** 8aea8e2073a06c1ca55cd2ae574c957236cd1931 (10/11/2026)  
**Stack:** Minecraft 26.3, NeoForge 26.3.0.101, Java 25, ModDevGradle 2.0.148, GeckoLib 5.5.7.  
**Fonte atual:** docs/GAMEPLAY_E_DIRECAO.md e docs/ARQUITETURA_ATUAL.md. Os documentos de planejamento da 5.0 foram aposentados e permanecem no histórico Git.

## Experiência entregue no código

Em **Survival** na dimensão **dreamsdimensions:dreamscape**, clique com a **mão principal vazia** em **dreamsdimensions:dream_glow_moss**. O servidor procura o bloco **dreamsdimensions:dream_shimmer_block** em raio horizontal de 12 (disco) e variação Y ±6. Ao encontrar, emite 12 partículas vanilla em um rastro direcional **curto**, toca um som vanilla de ametista e mostra uma mensagem localizada. Ao não encontrar, mostra mensagem honesta, com mensagem diferente se alguma área da pesquisa não estava carregada. O efeito é um pulso, sem animador/timer recorrente e sem coordenadas exatas. O jogador precisa minerar Shimmer com picareta e colocá-lo em construção pelos mecanismos vanilla. Nenhum item é concedido pelo clique.

Não há missões, loot mágico, progressão obrigatória, índice persistente de POIs ou dependência da habilidade ResonanceGoal do Fragmento Errante.

## Arquitetura efetivamente escrita

- `event/EchoMossInteractionHandler.java`: um adaptador para `PlayerInteractEvent.RightClickBlock`, assinado **uma única vez** no `NeoForge.EVENT_BUS` através de `DreamsDimensions`. Validação de mão, ItemStack, jogador vivo, `GameType.SURVIVAL`, dimensão, alcance `Player.isWithinBlockInteractionRange`, borda e tipo do bloco. Cancelamento com `InteractionResult.SUCCESS` nos dois lados apenas para a ação elegível; scan/som/mensagem só no servidor. Handlers de logout, clone/morte, mudança de dimensão e parada do servidor limpam cooldown.
- `service/EchoMossSearch.java`: algoritmo puro e limitado, com leitor de célula injetável e resultado tipado `FOUND`, `ABSENT` ou `UNAVAILABLE`; desempate por distância 3D, X/Y/Z crescentes; não acessa mundo e permite parametrizar futuros alvos sem novo serviço.
- `service/EchoMossEligibility.java`: guarda pura para jogador vivo, Survival, Dreamscape, mão principal e vazia, com testes negativos de cada condição.
- `service/EchoMossCooldown.java`: estado transitório por UUID, com relógio de ticks, sem Player/Level/Chunk, limpo nos eventos. Mesmo uma tentativa sem alvo consome 100 ticks. Retrocesso de relógio reinicia entrada.
- Idiomas `pt_br` e `en_us`: mensagens traduzidas de sucesso, ausência e região parcialmente descarregada.
- Testes `EchoMossSearchTest`, `EchoMossCooldownTest` e `EchoMossEligibilityTest`.

**Referências de API:** [NeoForge interactions](https://docs.neoforged.net/docs/items/interactions/), [Primer 26.3](https://docs.neoforged.net/primer/docs/26.3/), Javadocs do branch 26.3 (RightClickBlock; Player.isWithinBlockInteractionRange; ServerChunkCache.getChunkNow; ServerLevel.sendParticles). O documento Interactions hoje pode mostrar o rótulo 26.1; o contrato das assinaturas foi conferido em Javadocs 26.3. Nenhum listener de tick novo foi criado.

## Segurança, custo e multiplayer

- Caixa teórica máxima = 25 × 25 × 13 = **8125** posições; filtro de disco reduz o número efetivamente consultado. Y fora de `[minY,maxY)` e posições além da world border são rejeitados.
- Somente `ServerChunkCache.getChunkNow(cx,cz)` é usado antes de `LevelChunk.getBlockState`; null é tratado como indisponível. Não chama `getChunk`, geração, tickets, global scans, scans a cada tick ou armazenamento de referências a chunks.
- A busca ocorre **após** adquirir cooldown por UUID, na thread do servidor. Dois jogadores podem consultar o mesmo musgo independentemente; não há mutação de inventário, mundo nem loot pelo handler.
- Um único pulso de até **12 partículas** destinado ao jogador, som curto e mensagem overlay. Particle Settings MINIMAL mantém a mensagem; a percepção das partículas, bem como a duração percebida, requer playtest.
- Debug opt-in via JVM `-Ddreamsdimensions.echoMossDebug=true`: status, inspeções, células indisponíveis e duração em microssegundos; sem UUID/nome/coordenada em logs.

## Recursos Survival pré-existentes verificados por leitura

- `ModBlocks.DREAM_GLOW_MOSS`: ID `dreamsdimensions:dream_glow_moss`; fonte de brilho com light level 8.
- `ModBlocks.DREAM_SHIMMER_BLOCK`: ID `dreamsdimensions:dream_shimmer_block`; `requiresCorrectToolForDrops`, registrado como BlockItem em `ModItems`.
- `data/minecraft/tags/block/mineable/pickaxe.json`: inclui Shimmer.
- `data/dreamsdimensions/loot_table/blocks/dream_shimmer_block.json`: item próprio do bloco, 1 roll.
- Propriedades base copiadas de ametista; colisão regular de bloco. **Obtenção e recolocação reais em Survival ainda exigem playtest**.

Nenhum bloco, item, minério, feature, bioma, loot table, receita, dimensão, mob, textura ou sistema de teleporte/retorno foi modificado pela Task 5.1.

## Testes automatizados preparados

Casos JUnit: raio 12/13 e disco, Y 6/7, alvo mais próximo, três dimensões de desempate, ausência tipada, chunks indisponíveis, alturas, world border, teto de iteração, cooldown 100 ticks, dois UUIDs, limpeza e retrocesso do relógio. Elegibilidade por dimensão/mão/ItemStack tem testes da política pura; a entrega real do evento e sua não duplicação em cliente/servidor ainda exigem **integração/evento real**. O script CI `check_echo_moss.py` verifica o registro único e a ausência de chamadas diretas de loot/worldgen por inspeção estática. **Não confundir inspeção de código com GameTest executado.**

**Execução prevista:** `./gradlew test build` com Java 25, além dos scripts CI já existentes e smoke de servidor dedicado. O ambiente de edição conectado não dispõe do JDK 25 nem de acesso ao Gradle/Minecraft: o status de execução local deve ser informado no relatório de entrega, e os checks GitHub Actions devem ser conferidos depois do push.

## Laboratório manual ainda pendente (não homologado)

Seed `20261108`; Dreamscape. Escolher piso seguro conhecido no mundo de teste e executar, com os comandos ajustados às coordenadas reais:

```mcfunction
/gamemode creative
/setblock ~ ~-1 ~ dreamsdimensions:dream_glow_moss
/setblock ~8 ~-1 ~ dreamsdimensions:dream_shimmer_block
/gamemode survival
```

Agachar, manter a mão principal vazia, mirar no musgo e clicar; observar pulso, som e indicação. Minerar o Shimmer com picareta correta, conferir item no inventário, colocá-lo novamente. Para testes de limites, repetir preparação em área limpa com alvos exatamente a 12/13 blocos e Y +6/+7, sem outro Shimmer mais próximo.

**Matriz manual PENDENTE:** sucesso a 8 blocos, ausência, raio 12/13, Y ±6/±7, alvo destruído, cooldown e spam 100 cliques, item na mão, offhand, jogadores A/B, troca de dimensão, logout/relogin, clone após morte, restart, chunks descarregados/borda de chunk/world border, partículas MINIMAL, mineração/colocação, dedicated server. Regressões ainda pendentes: sono 100 ticks, duas dimensões, Despertador, Fragmento Errante/Slow Falling, Vigia/Pacto e seeds **14011, 20261108, -904732, 73199013**.

**Importante:** `/setblock` e `/give` preparam laboratório, **não provam geração natural**. A chance de `dream_shimmer_block` em um Jardim de Eco não garante Shimmer dentro de 12 blocos de cada musgo. Distribuição e legibilidade naturais pertencem às Tasks **5.2 e 5.4**. Não publicar FPS/MSPT/TPS sem medição real.

## Limitações e passos seguintes

Um alvo pode existir atrás de parede, em terreno inacessível ou num chunk carregado em região parcialmente inacessível; não há pathfinding. Um pulso de partículas tem persistência visual dependente do cliente e deve ser avaliado antes de criar uma agenda de 1–2 segundos. Se o sinal for pouco legível, ajustar em 5.2 com medição; só após validação estudar múltiplos POIs, biomas e estados persistentes.
