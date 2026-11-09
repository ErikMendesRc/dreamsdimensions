# TASK 3.3 — DREAMSCAPE: Atmosfera do Sonho Fragmentado

**Branch:** `migration/minecraft-26.3`. Minecraft 26.3, NeoForge 26.3.0.101,
Java 25 e ModDevGradle 2.0.148. Alterações limitadas à atmosfera cliente,
ao catálogo lógico `atmosphereId`, a atributos nativos de névoa
e aos verificadores de CI. **Não realizar merge na main.**

## Auditoria e contratos da arquitetura

Lidos `docs/DREAM_ENGINE_TASK_2_2.md`,
`docs/DREAMSCAPE_TASK_3_1.md`, `docs/DREAMSCAPE_TASK_3_2.md`,
`docs/CAMPO_ONIRICO_AZUL_TASK_2_1.md` e
`docs/CAMPO_ONIRICO_AZUL_TASK_2_1_1_VISIBILIDADE.md`.

`DreamDimensionCatalog` já fornece o destino `dreamsdimensions:dreamscape`;
somente o `atmosphereId` opcional antes vazio passou a apontar para
`dreamsdimensions:dreamscape_ambience`. **Esse ID é lógico**, sem novo mundo,
bioma, resource key do jogo ou registro de cliente no bootstrap comum.

`AtmosphereProfile` continua como record imutável, com um seletor simples
`Theme.BLUE_FIELD` / `Theme.DREAMSCAPE`. O registro privado inclui dois
perfis. O `AtmosphereClientController` permanece a **única** inscrição
`ClientTickEvent.Post` e escolhe perfil por ID e dimensão, nunca por
efeito global. Os emissores `AtmosphereParticles` e `AtmosphereAudio`
usam o mesmo dispatcher e separam o pequeno algoritmo sensorial pelo
`Theme`. `AtmosphereSampling` concentra as sondas de blocos e heightmap
em chunks carregados, sem carga forçada.

**Isolamento:** apenas os cinco arquivos
`client/atmosphere` e o subscriber `Dist.CLIENT` usam classes
`net.minecraft.client`; o catálogo comum não importa tais classes.
O perfil azul mantém **literalmente** os números 12,2,8,160,160,8,
as mesmas partículas, a seleção sonora e a névoa pré-existentes.
A política de `SLOW_FALLING`, o retorno, a tela de transição e o
teleporte não foram modificados.

## Identidade da Dreamscape e Environment Attributes

`dimension_type/dreamscape_type.json` adicionou exclusivamente:
- `minecraft:visual/fog_start_distance = 42` blocos;
- `minecraft:visual/fog_end_distance = 190` blocos;
- `minecraft:visual/sky_fog_end_distance = 235` blocos.

O intervalo extenso 42–190 induz transição rosada suave sem uma
parede próxima; em comparação, o Azul usa 48/170/210.
A Dreamscape preserva céu `#9e0200`, fog `#db5b8f` e névoa
subaquática `#050533`; não muda iluminação geral,
`ambient_light=1`, `timelines=#minecraft:in_overworld`,
`has_fixed_time=false`, ciclo vanilla ou atributos de gameplay.
Cores e visibilidade dependem também do tempo, render distance,
opções e resource packs. **Não existe neblina nativa dinâmica local
por desfiladeiro/altura nesta implementação.** A escolha foi manter
o renderizador vanilla e não alterar `FogRenderer` ou shaders.

## Partículas: materiais vanilla, orçamentos explícitos

| Propriedade | Campo Azul (intacto) | Dreamscape |
| --- | --- | --- |
| Intervalo ALL | 12 ticks | 28 ticks |
| Intervalo DECREASED | 24 ticks | 56 ticks |
| Ambientais por intervalo, ALL/DECREASED | até 4/2 | até 3/1 |
| Partículas de acento por gatilho, ALL/DECREASED | até 4/2 | até 2/1 |
| Gatilhos extras | jardins luminosos | a cada 4 intervalos |
| Tentativas de solo | até 8 | até 4 |
| Raio de leitura de solo | 8 blocos | 9 blocos |

Paleta Dreamscape em `DustParticleOptions(int color, float scale)`:
`ROSE_DUST = #DDA9C3 / 0.78`, `PALE_DUST = #F1D4CE / 0.66`,
`MEMORY_DUST = #F4C5DB / 0.92`. Emissões ocasionais vanilla
`WHITE_ASH` e raríssimos `END_ROD` quando há resíduos próximos.
Sem sprites, modelos, blocos luminosos ou `ParticleType` customizados.

`Particles.MINIMAL` desativa todas as emissões adicionais,
`DECREASED` reduz a taxa e quantidade, e `ALL` segue o orçamento
normal. Água e pausa também impedem emissão. Todas as partículas
exigem espaço de ar e chunk já carregado; não usamos
`addAlwaysVisibleParticle` nem pacotes.

### Fenômenos regionais: detecção, não geração

- **Eco das Fissuras:** comparação de altura entre três colunas
  carregadas (centro, leste +11 e norte +11); desnível máximo de
  **19+ blocos** indica borda acidentada, não confirmação de uma
  fenda geológica. Gera no máximo dois pontos pálidos de acento.
- **Silêncio Suspenso:** o jogador está em **Y≥158**, há ar em dez
  blocos abaixo e seis acima, com chunks carregados. É um sinal
  de vazio elevado, **não** prova de uma ilha suspensa.
- **Resíduos da Memória:** testa primeiro o piso sob o jogador e
  depois até quatro superfícies próximas, procurando `dream_glow_moss`,
  `dream_shimmer_block` ou flor `dream_flower_block` acima do piso.
  Gera brilho sutil e eventualmente `END_ROD`, sem exigir Garden IDs
  ou volumes fora dos chunks carregados.

Os gatilhos adicionais são examinados apenas uma vez a cada quatro
intervalos quando a configuração permite partículas. Não há scans de
chunks ou memória de estado regional entre dimensões.

## Som: eventos reais 26.3, um cue por vencimento

O perfil Dreamscape espera **420 ticks** (21 segundos) após entrada;
depois agenda um único cue entre **420 e 660 ticks** (21–33 segundos),
medidos em ticks ativos não pausados. O Azul continua 160–320 ticks.
`AtmosphereAudio.begin(profile)` alinha a primeira execução ao perfil,
com `reset()` e estado local zerado quando sai ou troca `ClientLevel`.

Seleção espacial Dreamscape:
- Próximo a resíduos: `SoundEvents.AMETHYST_BLOCK_CHIME` a volume
  **0.14**, pitch **0.62–0.77**, na posição real do bloco.
- Desnível/cânion próximo: `SoundEvents.BREEZE_IDLE_AIR` com volume
  **0.12**, pitch **0.56–0.66**, emitido baixo ao lado do jogador.
- Vazio elevado: `SoundEvents.CHERRY_LEAVES_STEP` volume **0.085**,
  pitch **0.72–0.86**; trata-se de um som vanilla reutilizado, e
  **deve ser avaliado auditivamente** se evoca a sensação desejada.
- Espaço comum: `SoundEvents.BREEZE_IDLE_AIR` volume **0.075**,
  pitch **0.72–0.86**.

Todas as pistas usam `SoundSource.AMBIENT` e
`ClientLevel.playLocalSound`. Nenhum loop, som novo, canal de rede
ou `SoundEvent` registrado. Uma amostra ambiental pode falhar e
produzir apenas o som do espaço comum; isso é intencional.

## Limites e tradeoffs

- **Somente cliente.** Não é esperado áudio/partículas em smoke
  dedicado. O servidor comprova isolamento físico e datapack decoding.
- Cadência limitada mesmo em voo criativo; não força carregamento e
  não realiza buscas em regiões vizinhas fora da memória atual.
- A cor de poeira não produz luz real nem garante mesma percepção
  em todos os pacotes gráficos.
- `MINIMAL`, água, mundo ausente, pausa, logout e troca de perfil
  impedem novas emissões; um som vanilla já disparado pode terminar
  naturalmente, mas não há fila/instância sonora mantida pelo mod.
- A verificação de fissuras usa `MOTION_BLOCKING_NO_LEAVES`, que
  pode apontar para ilhas ou formações: **proxy, não geomorfologia**.
- Performance de FPS/frame-time não foi medida em cliente nesta task.
  A comparação depende de hardware, chunks visíveis e configurações.
- Worldgen, densidades, folhas, mapas, blocos, entidades, renderizador,
  attachment e gameplay de ambas dimensões são **inalterados**.

## Testes

`python3 scripts/ci/check_dreamscape_atmosphere.py` testa os 12
contratos de codec/perfil/IDs, isolamento, orçamento, `ParticleStatus`,
limpeza, sons, atributos, rede e regressão do Azul. O verificador original
`check_atmosphere.py` permanece, atualizado apenas para reconhecer
as três novas chaves de névoa e o segundo perfil — guardas do Azul preservados.

Workflow `.github/workflows/build.yml` continua realizando
`./gradlew --no-daemon clean build`, scripts anteriores, asset preflight
e smoke de servidor dedicado nas seeds `14011`, `20261108`,
`-904732` e `73199013`. Novo teste estático é inserido
sem remover scripts antigos.

**Evidência CI / commit:** pendente de observar os jobs disparados
após o push dos testes e do código. Não marcar como testado antes
de confirmar execução completa. O pipeline não executa cliente
gráfico nem reproduz sons para inspeção humana.

## Checklist de homologação manual

- [ ] Instalar JDK 25, `git switch migration/minecraft-26.3`,
      `git pull`, `.\gradlew.bat runClient`.
- [ ] Criar save novo; seed 14011; ativar Particles **ALL**.
- [ ] Dreamscape: observar o céu vermelho e a névoa rosada em
      desfiladeiros e massas suspensas, com render distance 8/12/20.
- [ ] Em pradaria, subir a Y≥158 e entrar em área alta com ar embaixo;
      comparar emissão pálida e partículas básicas.
- [ ] Procurar `dream_glow_moss`/`dream_shimmer_block` e jardins:
      observar resíduos discretos após 112 ticks ativos (ALL).
- [ ] Observar borda de fenda para os acentos de profundidade;
      nenhum efeito deve depender de chunks descarregados.
- [ ] Comparar ALL vs DECREASED vs MINIMAL, água, voo criativo,
      pausa, alt-tab e desconexão.
- [ ] Ouvir por 3–5 minutos e comparar volumes/pitches em áreas
      abertas, desníveis e jardins; testar slider Ambiental=0.
- [ ] Sair para Campo Azul: deve tocar apenas áudio e cores azuis,
      com névoa preservada 48/170/210.
- [ ] Ir ao Overworld e reconectar: nenhuma nova emissão temática.
- [ ] Dormir 100 ticks e retornar via Despertador, sem alterar
      attachment/posição da cama; conferir mais de um jogador.
- [ ] Medir FPS e tempo de frame antes/depois, em mesmo hardware,
      seed, coordenadas e opções (múltiplas repetições).

## Fontes

- https://docs.neoforged.net/primer/docs/26.3/
- https://docs.neoforged.net/docs/concepts/events/
- https://docs.neoforged.net/docs/concepts/sides/
- https://docs.neoforged.net/docs/rendering/particles/
- https://github.com/neoforged/NeoForge/tree/26.3.x
- https://github.com/NeoForgeMDKs/MDK-26.3-ModDevGradle
- https://feedback.minecraft.net/hc/en-us/articles/48913133328013-Minecraft-Java-Edition-26-3

As páginas genéricas do NeoForge estavam rotuladas **26.1**; as
assinaturas 26.3 são verificadas adicionalmente pelo build do
projeto e pelo servidor dedicado, sem suposição de compatibilidade
de código legado.
