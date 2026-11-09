# TASK 4.3 — Ecos das Ilhas: spawn e mecânica do Fragmento Errante

**Escopo:** `migration/minecraft-26.3`, Minecraft 26.3 / NeoForge 26.3.0.101 / Java 25 / GeckoLib 5.5.7. Não fazer merge na main.

## Decisão de gameplay

A criatura é um habitante comum das partes superiores das ilhas suspensas. Ela não ataca. Quem se aproxima calmamente pode obter uma proteção temporária de queda; agressões repetidas fazem o Fragmento se dissipar após fugir. O desaparecimento é **reativo e intencional**, não aleatório.

### Spawning — ecologia das ilhas

A Dreamscape usa um único bioma tanto para o continente quanto para as ilhas. O cadastro de bioma por si só não distingue a ilha: a proteção fundamental é `FragmentoIslandHabitat`, chamado pelo `RegisterSpawnPlacementsEvent` com `ON_GROUND` e `MOTION_BLOCKING_NO_LEAVES`.

O predicado de spawn natural exige simultaneamente:

- Dimensão `dreamsdimensions:dreamscape` e bioma `dreamsdimensions:dreamscape_biome`.
- Altura do pé entre **Y158 e Y231**.
- Piso `#dreamsdimensions:dreamscape_natural_ground`; pés e cabeça em ar, fluido vazio.
- Duas amostras de **ar abaixo da superfície** separadas por cinco blocos, procurando profundidades de **24, 40, 56 ou 68 blocos**, para diferenciar massa suspensa de terreno continental.
- Consultas somente a chunks já disponíveis; sem `forceload`, alteração de worldgen ou novas features.

O spawn natural é definido pelo atributo `minecraft:gameplay/natural_mob_spawns`, introduzido na 26.3, no JSON do bioma. Categoria `creature`, **peso 100**, grupos de **2–3**, probabilidade de povoamento em worldgen da categoria `creature` igual a **0.34**. A criatura permanece sujeita aos *mob caps* e às tentativas vanilla; peso 100 não corresponde a 100% de chance de surgir em cada ilha. Mobs invocados por `/summon` continuam possíveis em qualquer lugar.

Quando detecta pela primeira vez um pouso válido de ilha, a entidade guarda sua **âncora natal** e só seleciona novos destinos que fiquem até 14 blocos dela, com apoio natural de ilha a até nove blocos abaixo. O goal de fuga conserva limites de tempo, colisões e chunks carregados anteriores.

### Sintonização onírica — recompensa não violenta

- Um jogador chega a até **3,5 blocos**, sem correr. A entidade executa `curious`, interrompe navegação e olha o jogador.
- Permanecendo junto dela e sem movimento rápido por **90 ticks (4,5 segundos)**, recebe o efeito vanilla **Queda Lenta** por **360 ticks (18 segundos)**, acompanhado por um pequeno pulso de `END_ROD`.
- Cooldown **1800 ticks (90 segundos) por entidade**, salvo em NBT, evitando repetição contínua no mesmo Fragmento.
- Apenas **um jogador de cada vez** por mob. Se ele se afasta, corre, o mob sofre dano ou desaparece, a tentativa interrompe.
- Não foram criados itens, moedas, loot, partículas customizadas, áudio ou novos assets.

### Dissipação defensiva

- Primeiro dano válido: `startled` e fuga.
- Segundo dano válido em uma janela de **600 ticks (30 segundos)**: marca desaparecimento após concluir fuga. A entidade executa `disappear` e o servidor a remove somente após **27 ticks**.
- Jogadores correndo podem assustar, mas **não** fazem a entidade sumir só por correr.
- Contadores e cooldowns persistem após save/reload, assim como o vínculo com a ilha. Reações e efeitos continuam autoritativos no servidor.

### Testes

- `./gradlew --no-daemon clean build` (Java 25)
- `python3 scripts/ci/check_fragmento_errante_behavior.py`: dados 26.3, categoria, pesos, intervalos, restrição de habitat, reação ao dano, sintonização e invariantes da Task 4.2.
- Pipeline existente: dedicated server RCON, summon e oscilação Y, scripts do worldgen e quatro seeds `14011`, `20261108`, `-904732`, `73199013`.
- **Pendência in-game:** confirmação visual de distribuição real (recomenda-se contar criaturas em cinco ilhas por seed, com pelo menos dois jogadores e `/gamerule spawn_mobs true` (nome atual no Minecraft 26.3)), pois não há jogador real no servidor de CI para executar o ciclo natural. Evitar contar mobs invocados por comando.
- Testar efeito: aproximar sem correr por 5 segundos, conferir **Queda Lenta** por 18s, cooldown de 90s; repetir com dois jogadores.
- Testar fuga: um dano, depois segundo dano em 30s, observar animação completa `disappear` antes da remoção. Confirmar persistência após salvar.
- Verificar que grupos não aparecem no continente e não atravessam o vazio durante o wander em mapas reais.
- Evidência inicial de servidor: na seed `-904732`, durante o smoke de worldgen, foi observada uma instância do Fragmento em `(-4, 194, 18)` distinta da entidade invocada por comando. A ocorrência é compatível com spawn natural durante a geração e demonstra presença no envelope das ilhas, **não** prova abundância ou localização em todas as ilhas. O teste RCON foi corrigido para marcar a entidade invocada com tag exclusiva, sem confundi-la com mobs naturais.

### Documentações utilizadas

- NeoForge: https://docs.neoforged.net/primer/docs/26.3/
- Registro de spawn: https://docs.neoforged.net/docs/entities/livingentity/
- Notas oficiais Minecraft 26.3: https://feedback.minecraft.net/hc/en-us/articles/48913133328013-Minecraft-Java-Edition-26-3
