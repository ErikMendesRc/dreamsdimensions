# TASK 3.2 — Dreamscape: Ecossistema do Sonho Fragmentado

**Minecraft/NeoForge 26.3 · Java 25 · branch `migration/minecraft-26.3` (sem merge).**

## Fundamentação: geografia real da Task 3.1

Lido `docs/DREAMSCAPE_TASK_3_1.md`. A dimensão continua com Y0..255, com
terra firme, falhas e possível volume suspenso em Y~160–220. As sondas
anteriores observaram volumes suspensos em três das quatro seeds, mas
**não** confirmaram pontes naturais e não garantiram que toda superfície
alta é uma ilha. Por isso:
- Todas as árvores usam **piso real** no heightmap e solo `dream_grass_block`;
  não presumem um Y específico de continente.
- Raízes exigem **face inferior real** em `serene_stone`, rocha sólida
  diretamente acima e espaços de ar abaixo em 1, 6 e 12 blocos.
  Falhas na verificação cancelam a feature.
- Pequenos arcos exigem **dois apoios** naturais em elevações próximas,
  sem atravessar desfiladeiros arbitrários.
- Worldgen da Task 3.1, motor onírico da Task 2.2, itens, atmosfera e
  regras do Campo Azul **intactos**.

## Arquitetura

`DreamscapeEcosystemFeature` é **um MapCodec/Feature novo**, registrado
exclusivamente como tipo `dreamsdimensions:dreamscape_ecosystem` em
`BuiltInRegistries.FEATURE_TYPE`, via `DeferredRegister` existente.
São seis instâncias em `worldgen/feature/dreamscape_*.json`, com seis
`placed_feature` correspondentes. Apenas
`worldgen/biome/dreamscape_biome.json` as referencia **uma vez** na
etapa 9, VEGETAL_DECORATION, na sequência:
Memory Tree, Witness Tree, Hanging Roots, Fractured Monolith,
Stone Ribs, Echo Garden.

Não há biome modifier nem geração em evento de tick. A seleção
usa ruído regional contínuo 2D, deformação (warp) derivada da seed e
coordenadas absolutas; o `RandomSource` é o da própria feature
vanilla posicionada, não gerador global. As tentativas são filtradas
antes de amostrar relevo ou criar o plano voxel.

### Morfologias e parâmetros

| Família | Altura/tamanho | Tentativas de distribuição | Densidade base | Geometria |
|---|---:|---:|---:|---|
| Árvores da Memória | 10–18 | 4/chunk | 0,75 | Tronco curvo inclinado, base espessa, 4–5 galhos angulares e copas rasgadas |
| Árvores Testemunhas | 21–28 | 1/chunk | 0,44 | Haste alta, 2–3 braços horizontais, folhagem pequena e espaçada |
| Raízes Suspensas | 6–14 | 4/chunk | 0,68 | Raiz descendente e bifurcada em underside rochosa, sem folhas |
| Monólitos Fraturados | 12–22 | 2/chunk | 0,51 | Pilar afilado e inclinado, base mais espessa, espinho lateral |
| Arcos e Costelas | raio 3–5 | gate 24%/chunk | 0,60 | Arco curto com duas bases verificadas e coluna contínua |
| Jardins de Eco | raio 3–6 | 3/chunk | 0,60 | Mancha inclinada irregular; flor 21%, musgo 20%, shimmer 1,8% por célula elegível |

**Importante:** "tentativa" e gate NÃO são ocorrências reais. A chance
efetiva é `density * altitude * regionalWeight` seguida de checagens
do terreno, colisão, espaço, biome, write bounds e altura. Haverá
regiões sem árvores e outros elementos; não há garantia por chunk.
Os pesos regionais variam com o formato e com a altitude; testemunhas
são mais frequentes em Y≥125 e raízes somente tentam acima de Y157.

## Segurança e performance

- Todos os blocos são planejados em um mapa limitado a **1750** alterações.
  Antes da primeira escrita, verifica-se ocupação, fluido, altura,
  world border e `WorldGenLevel.ensureCanWrite` para o conjunto inteiro.
  Se um único bloco falhar, rejeita-se toda a tentativa.
- Apenas `echo_garden` troca `dream_grass_block` por
  `dream_glow_moss`, com espaço livre acima; outros blocos só em ar.
- Troncos preservam `RotatedPillarBlock.AXIS` por segmento;
  folhagens propagam `LeavesBlock.DISTANCE ≤6` por BFS em plano
  de tamanho finito e usam `PERSISTENT=false`; folhas órfãs
  são removidas do plano antes da validação.
- Proteção de **68 blocos** em torno da origem da dimensão inclui
  a projeção horizontal de todas as formações e ultrapassa
  a busca natural (raio 12) do motor da Task 2.2.
  Caso o spawn configurado seja deslocado pelo servidor, mantém-se
  a proteção de colisão/chegada do motor, sem supor uma nova origem.
- Raízes verificam uma coluna de até 84 blocos **após** passar o
  gate regional raro. Não visitam chunks arbitrários.
- Custo potencial: consultas de heightmap de vizinhança (4 por
  tentativa elegível), BFS de folhas apenas nos dois tipos de árvores,
  pré-validação de geometria e procura inferior até 84 posições.
  Usar profiling real antes de aumentar densidades.
- `setBlock` não oferece transação atômica no worldgen; a
  pré-validação protege as causas conhecidas de falhas, mas não é
  rollback geral para uma falha inesperada durante escritas.
- A geometria é feita apenas nos chunks novos. Saves antigos
  preservam conteúdo; faça backup antes de testar.

## Testes e evidência

`scripts/ci/check_dreamscape_ecosystem.py` confere JSONs, seis codecs/
configs, etapa e ordem, ausência de duplicatas, blocos permitidos,
separação dos biomas, evidências de guardas de escrita, propriedades de
folhas/troncos e assinaturas regionais reprodutíveis em quatro seeds.
A análise estática não simula todas as formas nem conta estruturas reais.

`--rcon`: força geração em **três áreas limitadas de 3×3 chunks**
por seed, num servidor dedicado temporário; os verificadores da
Task 3.1 e do Campo Azul permanecem em execução.
`--log server-smoke.log`: conta apenas mensagens
`DREAMSCAPE_ECOSYSTEM_GENERATED form=... blocks=...` emitidas depois
de retornos bem-sucedidos de `Feature.place` com opt-in por
`-Ddreamsdimensions.worldgenMetrics=true`. Essas contagens são
**reais**, não estimativas baseadas no ruído. O código não emite
essas mensagens no jogo normal.

**Evidência do workflow:** pendente de compilação e smoke da nova versão,
não preencher as contagens com suposições. Consultar GitHub Actions do
commit desta Task. Nenhuma formação visualmente homologada no cliente.

## Homologação visual pendente

- [ ] Criar mundo novo para as seeds 14011, 20261108, -904732 e 73199013;
      sobrevoar pelo menos 1000 blocos em cada direção.
- [ ] Registrar captura de Memória/Testemunha a 50, 100 e 200 blocos;
      avaliar diferenças de escala e copas.
- [ ] Verificar galhos não flutuantes e folhas completas após load/reload.
- [ ] Voar por baixo de cada ilha e procurar raízes realmente presas
      à rocha, sem pendurar raízes em cavernas comuns.
- [ ] Verificar apoio de dois pés nos pequenos arcos e ausência de
      ilhas falsas formadas por erros de gravação.
- [ ] Inspecionar cruzamentos de chunks e regiões de borda de escrita,
      teste em survival de colisão e iluminação.
- [ ] Conferir 100 ticks de sono, chegada e Despertador Onírico,
      sem bloqueio na região de origem.
- [ ] Comparar dados de ocorrência e FPS/MSPT de cada seed com benchmark
      anterior, sem afirmar melhoria sem medir.
- [ ] Sobrevoar Campo Azul e verificar vegetação/formações/atmosfera
      inalterados.

## Referências

- https://docs.neoforged.net/primer/docs/26.3/
- https://docs.neoforged.net/docs/concepts/registries/
- https://docs.neoforged.net/docs/worldgen/biomemodifier/
- https://github.com/neoforged/NeoForge/tree/26.3.x
- https://github.com/NeoForgeMDKs/MDK-26.3-ModDevGradle
