# TASK 2.1 — Atmosfera Viva do Campo Onírico Azul

**Minecraft 26.3 · NeoForge 26.3.0.101 · Java 25 · ModDevGradle 2.0.148**  
**Branch:** `migration/minecraft-26.3` (sem merge na `main`)

## Objetivo e estado de aceite

Atmosfera leve: um perfil de névoa azul nativa, emissões locais esparsas
de partículas **vanilla** e pistas sonoras espaciais eventuais em ambiente
aberto, florestas e superfícies luminosas. O worldgen homologado nas Tasks
1.1–1.4 é mantido integralmente: **nenhuma** modificação em registries de
worldgen, noise settings, density functions, feature/placed features,
surface rules, biomas de outras dimensões ou blocos.

O teste compilado e o smoke do servidor comprovam APIs/datapacks em
Minecraft 26.3, mas não comprovam a experiência subjetiva no cliente.
O checklist visual e auditivo abaixo é **pendente** até execução no IntelliJ.

## Decisões de arquitetura

1. **Data-driven e nativo:**
   `data/dreamsdimensions/dimension_type/campo_onirico_azul_type.json`
   configura as distâncias da névoa ambiental:
   - `minecraft:visual/fog_start_distance = 64.0` blocos;
   - `minecraft:visual/fog_end_distance = 224.0` blocos;
   - `minecraft:visual/sky_fog_end_distance = 256.0` blocos.
   O bioma já definia `visual/sky_color = #9bdcfd`,
   `visual/fog_color = #b6e2ff` e
   `visual/water_fog_color = #050533`; **preservados**.
   Visibilidade final ainda respeita a distância de renderização,
   clima, visão noturna e regras vanilla. O efeito não modifica neblina
   subaquática nem substitui renderizadores ou shaders.

2. **Cliente e conteúdo vanilla:**
   `client/atmosphere/AtmosphereClientController.java` recebe
   `ClientTickEvent.Post` via
   `@EventBusSubscriber(value=Dist.CLIENT)`; **nenhuma** referência a
   `net.minecraft.client` é registrada pelo bootstrap comum.
   `AtmosphereProfile` contém apenas a identidade da dimensão e
   parâmetros de orçamento. `AtmosphereParticles`, `AtmosphereAudio`
   e `AtmosphereSampling` são componentes independentes e pequenos.
   Não foram registrados novos `ParticleType`s/`SoundEvent`s.

3. **Limitação de variação espacial da névoa:**
   A 26.3 permite atributos por dimensão, bioma e timeline. Como o Campo
   Azul tem **um único bioma**, usar a variação por bioma seria
   artificial. Não foi encontrado um hook estável, público e
   extensível de pós-processamento de névoa suficientemente confiável
   para modular distâncias por altitude em código NeoForge 26.3.
   **Não há névoa por vale/altitude em Java nesta versão**.
   A profundidade dos vales influencia **apenas partículas locais**.
   É preferível manter o pipeline ambiental nativo estável a escrever
   diretamente em `FogRenderer`, shaders ou campos privados.
   `dimension_type` preserva `ambient_light=1`,
   `skybox=overworld`, `default_clock=minecraft:overworld`,
   `timelines=#minecraft:in_overworld`, `has_fixed_time=true`.
   Não há novas timelines, flashes, alterações de skylight ou nuvens.

## Orçamento de partículas

- `ParticleStatus.MINIMAL`: **0** partículas adicionais.
- `ParticleStatus.DECREASED`: máximo **1 emissão/90 ticks**;
  `ALL`: máximo **1 emissão/30 ticks**.
- Cada janela ainda passa pelo gate de 50%, verificação de área
  carregada, superfície a no máximo 13 blocos verticalmente e
  pequenas condições de ambiente.
- `maxParticleSamples=3`, `sampleRadius=7`: até três amostras
  **somente em chunks carregados**, cada uma uma consulta de altura
  e teste de superfície, quando o intervalo vence. Nenhuma varredura
  volumétrica e nenhum `getChunk`, `forceload` ou pacote.
- Nos jardins, pode aparecer `minecraft:firefly` ou um
  `minecraft:end_rod` discreto quando uma amostra encontra
  `dream_glow_moss` / `dream_shimmer_block`.
- Em regiões abertas: `minecraft:spore_blossom_air`; em vales
  detectados por duas alturas próximas: `minecraft:white_ash`.
  Embaixo de copas as chances são reduzidas.
- Apenas `ClientLevel.addParticle` (variante comum, **não** always
  visible), e a própria engine limita outras partículas.
- As partículas não emitem luz de bloco nem substituem a iluminação
  existente do musgo (`lightLevel=8`).

## Orçamento sonoro

- Primeiro evento após um intervalo mínimo de **340 ticks**
  (aprox. 17 segundos); intervalo seguinte de **340–640 ticks**
  (17–32 segundos) acrescido de gate probabilístico.
- No máximo **um evento por vencimento**. Não há looping, áudio
  em rede, música própria nem sons agressivos como camada constante.
- Próximo de musgo/bloco cintilante amostrado: som vanilla
  `SoundEvents.AMETHYST_BLOCK_CHIME` (volume base **0.17**,
  pitch variável). Debaixo de folhas: `CHERRY_LEAVES_STEP`
  (**0.10**). Região aberta: `BREEZE_IDLE_AIR` (**0.075**).
  Estes últimos são efeitos vanilla pontuais, não uma gravação
  contínua de vento ou floresta. O resultado sonoro exato
  requer teste auditivo em Minecraft.
- `ClientLevel.playLocalSound(x,y,z, sound, SoundSource.AMBIENT,...)`
  reproduz som local, espacializado e sujeito ao volume
  **Ambient/Environment** do jogador.
- O estado temporário é zerado ao trocar de `ClientLevel`, sair
  da dimensão ou desconectar. Não existe instância loopada
  prolongando som por minutos fora do bioma. Um som vanilla já iniciado
  pode naturalmente terminar sua própria curta reprodução.

## Riscos, tradeoffs e testes automatizados

- A dimensão pode ter uma coloração mais pálida ou transições
  excessivamente claras dependendo de opções de render e resource
  packs; validar com 8, 12 e 20 chunks de renderização.
- Não há implementação de névoa dinâmica por altitude,
  nem verificação de árvores muito altas por análise espacial;
  os gatilhos são sinais aproximados e intencionalmente baratos.
- A amostragem de musgo detecta **somente alguns pisos aleatórios
  próximos**; não garante chime em cada jardim. Isso evita spam.
- Som `BREEZE_IDLE_AIR` e `CHERRY_LEAVES_STEP` reaproveitam clips
  vanilla originalmente associados a outros contextos; confirmar
  se soam serenos no cliente.
- Partículas vanilla geradas localmente não passam por
  `ParticleStatus.MINIMAL` por si mesmas; a verificação de opções
  está implementada explicitamente. `DECREASED` aplica multiplicador 3.
- A ativação de `@EventBusSubscriber(value=Dist.CLIENT)` é
  confirmada pelo build; o CI dedicado deve subir sem resolver
  as classes client-only.

**Validação automatizada:**
- `./gradlew --no-daemon clean build` em Java 25 (4 seeds via CI).
- `python3 scripts/ci/check_atmosphere.py`: exatamente os 3 atributos
  de fog, cores e estado de tempo preservados, IDs exclusivos,
  assinatura do subscriber CLIENT, salvaguardas de frequência e
  detecção de referências client-only no bootstrap comum.
- `python3 scripts/ci/check_vegetation.py`,
  `check_formations.py` e `check_worldgen_balance.py` sem alterações.
- `./gradlew --no-daemon runServer` e `check_dimensions.py`
  com seeds **14011, 20261108, -904732, 73199013**, carregamento
  de datapacks, forceload e verificação de espaço natural.
- **A aprovação visual/sonora e o sono/Despertador não foram testados
  automaticamente**. Ver o run GitHub Actions da commit final para
  resultados reais e possíveis falhas de infra independentes.

## Procedimento IntelliJ (pendente)

1. `git switch migration/minecraft-26.3` e
   `git pull origin migration/minecraft-26.3`, Java 25,
   `.\\gradlew.bat runClient`. Fazer backup de saves.
2. Criar mundo novo seed `14011`. Entrar naturalmente após
   **100 ticks de sono**, conferir posição segura e céu.
3. Em criativo/espectador, atravessar pradarias abertas, terraços,
   floresta e cordilheiras. Observar azul e contraste em
   distância de renderização **8, 12 e 20 chunks**.
4. Inspecionar vales (Y baixo), terrenos médios e cristas elevadas.
   A névoa deve continuar **estática em distância**; notar como
   densidade percebida muda com profundidade geométrica e partículas.
5. Sobrevoar os jardins de `dream_glow_moss`/`dream_shimmer_block`,
   aguardar observação local da emissão esparsa. Comparar com
   florestas densas e regiões abertas. Não esperar emissão contínua.
6. Alternar **Particles: All, Decreased, Minimal**. Confirmar que
   Minimal zera partículas adicionais; Decreased fica ainda mais
   discreto. Validar se partículas vanilla já existentes funcionam.
7. Reduzir **Ambient/Environment** ao mínimo e testar se os
   sons locais desaparecem; restaurar volume e ouvir 3–5 minutos
   em pradarias e jardins. Registrar evento e frequência percebida.
8. Medir FPS, MSPT e picos de frame time com opções fixas, antes e
   depois, em **mundos com mesma seed/mesmos chunks novos**.
   Para isolamento, usar `/debug start`, `/debug stop` e comparar
   pelo menos 3 execuções; anotar CPU, GPU e render distance.
9. Sair pelo **Despertador Onírico** ao Overworld e depois
   entrar na Dreamscape: os efeitos específicos não devem se
   repetir em nenhum dos dois lugares. Reentrar no Azul e
   conferir que os intervalos reiniciaram.
10. Repetir na seed `20261108` e em condições diurnas/noturnas,
    verificar nuvens, visão noturna, água e ausência de cintilação.
    Reportar capturas e `logs/latest.log`.

## Arquivos criados e alterados

**Criados:**
- `src/main/java/com/dreamsdimensions/mod/client/atmosphere/AtmosphereProfile.java`
- `src/main/java/com/dreamsdimensions/mod/client/atmosphere/AtmosphereSampling.java`
- `src/main/java/com/dreamsdimensions/mod/client/atmosphere/AtmosphereParticles.java`
- `src/main/java/com/dreamsdimensions/mod/client/atmosphere/AtmosphereAudio.java`
- `src/main/java/com/dreamsdimensions/mod/client/atmosphere/AtmosphereClientController.java`
- `scripts/ci/check_atmosphere.py`
- `docs/CAMPO_ONIRICO_AZUL_TASK_2_1.md`

**Modificados:**
- `src/main/resources/data/dreamsdimensions/dimension_type/campo_onirico_azul_type.json`
- `.github/workflows/build.yml` (um novo teste estático; todos os anteriores mantidos).

## Documentação / API 26.3

- https://docs.neoforged.net/primer/docs/26.3/
- https://docs.neoforged.net/docs/rendering/particles/
- https://docs.neoforged.net/docs/resources/client/particles/
- https://docs.neoforged.net/docs/concepts/registries/
- https://github.com/neoforged/NeoForge/tree/26.3.x
- https://github.com/NeoForgeMDKs/MDK-26.3-ModDevGradle
- https://feedback.minecraft.net/hc/en-us/articles/48913133328013-Minecraft-Java-Edition-26-3
- https://aldak0.ru/javadoc/26.3.x/net/neoforged/neoforge/client/event/ClientTickEvent.Post.html
- https://aldak0.ru/javadoc/26.3.x/net/minecraft/client/multiplayer/ClientLevel.html
- https://aldak.netlify.app/javadoc/26.3.x/net/minecraft/sounds/soundevents
- https://aldak.netlify.app/javadoc/26.3.x/net/minecraft/core/particles/particletypes
- https://minecraftdocs.dev/systems/world/environment-attributes-and-timelines

*As páginas gerais da documentação NeoForge em alguns endpoints estão
na versão 26.1. A API 26.3, os javadocs e o build real são a referência
para o código. Não há uso de API 1.21.x copiada sem verificação.*


## Evidência real do CI desta task (8 de novembro de 2026)

Execução após a correção do nome do bioma Dreamscape:
https://github.com/ErikMendesRc/dreamsdimensions/actions/runs/42540501755

| Seed | Java 25 clean build | Testes 1.2/1.3/1.4 | TASK 2.1 static | Preflight assets | Servidor dedicado/datapacks/chunks |
| --- | --- | --- | --- | --- | --- |
| 14011 | PASS | PASS | PASS | PASS | PASS |
| 20261108 | PASS | PASS | PASS | PASS | PASS |
| -904732 | PASS | PASS | PASS | PASS | PASS |
| 73199013 | PASS | PASS | PASS | PASS | PASS |

**Todos os quatro jobs terminaram em success.** O primeiro commit
`c59c730` compilou, mas a versão inicial do novo teste estático
apontava para `dreamscape.json` em vez do caminho real
`dreamscape_biome.json`; o commit `beef09c` corrigiu
somente essa referência e a execução acima validou a versão corrigida.

A existência de servidor dedicado com datapacks carregados é evidência
de isolamento suficiente para a inicialização, não comprova que todos
os sons e partículas funcionem no cliente. Sem teste humano não se
afirma aprovação de cor, volume, renderização ou experiência de gameplay.
