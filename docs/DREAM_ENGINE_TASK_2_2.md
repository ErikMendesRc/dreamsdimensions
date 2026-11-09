# TASK 2.2 — Motor Onírico Reutilizável

**Branch:** `migration/minecraft-26.3` (não mesclar em `main`).  
**Stack:** Minecraft 26.3 / NeoForge 26.3.0.101 / Java 25 / ModDevGradle 2.0.148.  
**Escopo:** refatoração de código. Nenhum ID, asset, worldgen, item, bloco, mob ou dimensão novos.

## Auditoria e baseline

Foram examinados `DreamsDimensions`, `DreamsConfig`, `SleepTeleportHandler`, `DreamSpawnHelper`,
`DreamReturnHelper`, `DreamReturnData`, `ModAttachments`, `DreamDimensionEffectsHandler`,
`ClientDimensionEvents`, `DreamTransitionScreen`, `client/atmosphere/*`, o Despertador e
as notas das Tasks 1.1, 1.2, 1.3, 1.4, 2.1 e 2.1.1. Preservamos os quatro casos da CI
existente: seeds `14011`, `20261108`, `-904732`, `73199013`.

Problemas identificados: seleção a partir de `Set` sem ordenação explícita, branch de material/radio
de spawn baseada em ID, efeito do Campo Azul hard-coded, cliente amarrado a um único perfil,
transição com ID duplicado, e atualização da cama de retorno antes da confirmação do teleporte.

## Arquitetura após a refatoração

```text
Config dream_dimensions
   -> DreamDimensionCatalog (lista imutável de perfis lógicos)
       -> DreamDimensionDefinition
          -> sleepDestination + selectionWeight
          -> fallbackFloor + naturalSpawnRadius
          -> effectPolicy
          -> atmosphereId (identificador lógico; client-only runtime)
          -> transition (destino, origem e visual)
   -> SleepTeleportHandler (somente eventos e guarda de 100 ticks)
       -> DreamTeleportService (servidor; seleção, tentativa, falhas e persistência)
          -> WeightedDimensionPicker (sorteio ponderado sem reposição)
          -> DreamSpawnHelper (terreno natural, segurança, plataforma 5x5)
          -> dream_return (MapCodec atual, escrito após sucesso)
   -> DreamDimensionEffectsHandler (política server-side)
   -> AtmosphereClientController (busca perfil por ID; isolado no cliente)
   -> ClientDimensionEvents (uma inscrição para transições por perfil)
```

**Catálogo lógico não é registry do jogo.** Continua obrigatório cadastrar
`dimension`, `dimension_type` e `worldgen` normalmente no datapack. A ordem estável
vem da `List.of` do catálogo, nunca da iteração do `Set` configurado.

| Propriedade | Dreamscape | Campo Onírico Azul |
| --- | --- | --- |
| ID original | `dreamsdimensions:dreamscape` | `dreamsdimensions:campo_onirico_azul` |
| Sono | habilitado; peso 1 | habilitado; peso 1 |
| Raio de busca natural | 12 | 24 |
| Plataforma de emergência 5×5 | `serene_stone` | `blue_dream_stone` |
| Efeito de servidor | nenhum | Slow Falling |
| Perfil ambiente | nenhum ainda | `dreamsdimensions:blue_field` |
| Tela condicional | Overworld → Dreamscape (existente) | nenhuma |

**Compatibilidade de config:** `dream_dimensions` mantém o nome e os defaults.
Entradas sintaticamente inválidas são rejeitadas pelo spec; entradas válidas
sem perfil no catálogo geram aviso e são ignoradas; duplicatas geram aviso.
Perfil configurado, mas mundo não carregado: o teleporte registra aviso,
remove-o das candidatas daquele sorteio e tenta as demais dimensões elegíveis.
Config vazia impede a entrada, sem crash ou loop.

## Contratos preservados

- **Sono:** contador vanilla de **100 ticks**, captura de cama no `PlayerWakeUpEvent`
  antes de o jogo limpar a posição, execução no `PlayerTickEvent.Post` e bloqueio
  temporário por UUID. Despertar precoce não teleporta. Sem referência permanente ao jogador.
- **Sorteio:** pesos inicialmente iguais; escolhas sem reposição, todas as dimensões
  elegíveis e carregadas têm probabilidade de primeira escolha. Se a primeira falha
  ao encontrar spawn, a próxima ainda é testada. Teleporte falho/duvidoso NÃO é
  repetido cegamente, para não produzir duplicação ou loop.
- **Spawn:** primeiro piso natural, altura, colisão, ar, líquido, borda mundial e
  blocos perigosos; só então plataforma de **25 blocos** de material temático
  em volume 5×5 inteiramente em ar, com os offsets e limites originais.
  Nenhum bloco não-ar é substituído; execução no thread do servidor.
- **Persistência:** campos do `dream_return` continuam **dimension/pos/yaw**
  com o mesmo `MapCodec`, registro `dream_return` e `copyOnDeath`. A cama só é
  atualizada no jogador efetivamente teleportado. A ordem de retorno continua
  cama válida → respawn pessoal → spawn seguro do Overworld.
- **Despertador:** canalização por **60 ticks**, cooldown de 60 ticks e retorno
  pelo fluxo original, sem alteração do item ou da tela.
- **Efeitos:** Slow Falling do perfil Azul é de 200 ticks, renovado sob 100
  quando reconhecidamente aplicado pelo mod. Um buff externo não é substituído
  por padrão; mudança no efeito/perfil/duração invalida a reivindicação.
  Ao sair não se remove `SLOW_FALLING` indiscriminadamente: o último buff
  dimensional expira naturalmente em até 200 ticks. Este resíduo transitório
  é intencional para não apagar poções/comandos/outros mods.
- **Cliente:** perfil Azul usa literalmente os números da Task 2.1.1
  (`12,2,8,160,160,8`) e os mesmos sons/partículas. Troca de `ClientLevel`
  ou perfil reseta ticks/áudio. Dreamscape não toca o áudio azul.
  Tela de carregamento já existente continua somente na transição
  Overworld → Dreamscape. Sem pacotes de rede extras.
- **Multiplayer:** dados permanentes por jogador em attachment; estados
  transitórios por UUID. Nenhum estado global de destino ou ponto de spawn.

## Pontos de extensão para uma terceira dimensão

O processo conceitual é: **(1)** criar seu datapack/IDs e blocos por uma
task futura, **(2)** acrescentar uma `DreamDimensionDefinition` imutável
na lista do catálogo usando `sleepDestination=true`, um peso explícito,
`fallbackFloor`, raio e política de efeitos, **(3)** opcionalmente
cadastrar seu `AtmosphereProfile` cliente usando um novo `atmosphereId`
e configurar uma política de tela. **(4)** habilitar o ID existente em
`dream_dimensions`. Sono, sorteio, teleport, retorno e busca de spawn
são reutilizados automaticamente. Nenhuma terceira dimensão foi criada nesta task.

## Validações automatizadas

O workflow `.github/workflows/build.yml` continua rodando `clean build`
com JDK 25, os verificadores de vegetação, formações, distribuição e atmosfera,
e smoke de servidor dedicado real via RCON nas quatro seeds. Adicionado
`scripts/ci/check_dream_engine.py` com asserts estáticos para catálogo,
config, seleção, isolamento, colisão, efeito, tela e **formato do attachment**.
Acrescentados testes **JUnit 5 reais** para o algoritmo de seleção ponderada,
inclusive inexistência de destino, inclusão de ambos os candidatos, peso 3:1,
rejeição de pesos inválidos e candidatos indisponíveis/ordem de fallback.
`./gradlew --no-daemon clean build` executa esses testes; `check` também.

**Não confundir:** testes do algoritmo de seleção são unitários; asserts sobre
arquivos são estáticos; o RCON valida existência e carregamento de mundos e
chunks, não eventos de um jogador real ou efeitos client-side. Relatório do
CI da commit final deve ser ligado aqui somente após a conclusão da run.

| Critério pedido | Evidência automatizada nesta task | Ainda depende de teste manual |
| --- | --- | --- |
| 1. Resolução de perfis | Asserts do catálogo, config, IDs e raios | Validar entrada nos dois mundos |
| 2. Seleção de habilitadas | JUnit: sorteio e pesos; assert da config | Confirmar alternância após muitos sonos |
| 3. Configurada não carregada | Assert da filtragem `getLevel`; JUnit simula lista disponível | Testar config com dimensão ausente |
| 4. Entrada por sono | Assert 100 ticks e eventos | Sono no cliente, multiplayer |
| 5. Falha e recuperação | JUnit cobre lista de destinos e fallback lógico | Forçar falha segura e testar recuperação |
| 6. Spawn seguro/fallback | Asserts dos guardas; RCON verifica colunas naturais | Testar volume ocupado e plataforma |
| 7. Retorno Overworld | Persistido `dream_return`; retorno original não alterado | Canalização 60 ticks, cama removida |
| 8. Reinício/persistência | Assert de chaves e codec; servidor inicializa | Salvar, fechar e reabrir com cama |
| 9. Isolamento Slow Falling | Assert policy e guardas de origem | Poção/comando, sair/reentrar |
| 10. Atmosferas isoladas | `check_atmosphere.py` e verificações de classes | Ver/escutar e comparar FPS |

## Checklist de aceite humano

- [ ] Fazer backup de saves e testar em mundo novo com JDK 25.
- [ ] Nas quatro seeds, dormir 100 ticks e verificar chegada segura, uma entrada por sono,
      alternando dimensões em diversas tentativas. Testar 2 jogadores simultâneos.
- [ ] Desabilitar uma dimensão em `dream_dimensions` e confirmar que só a outra é selecionada.
      Inserir chave válida sem perfil e verificar aviso; lista vazia não deve teleportar.
- [ ] Testar cama quebrada, respawn pessoal, retorno ao spawn global e ausência de loops;
      usar o Despertador por 60 ticks e verificar cooldown.
- [ ] Colocar construções perto do spawn de uma **cópia** de mundo e verificar que nenhum bloco
      construído é destruído pela plataforma. Criar cenário sem piso natural próximo.
- [ ] Forçar cenário sem spawn possível e verificar aviso e permanência segura no Overworld.
- [ ] Salvar e reiniciar servidor com dois jogadores em dimensões diferentes;
      confirmar que seus respectivos attachments persistem e retorno não troca camas.
- [ ] Aplicar Slow Falling por poção e por comando; entrar e sair do Azul, Dreamscape e Overworld,
      observar renovação somente no Azul e ausência de remoção do buff legítimo.
- [ ] Comparar partículas ALL/DECREASED/MINIMAL e som ambiente no Azul;
      verificar ausência fora dele e limpeza de estado após reconectar.
- [ ] Confirmar a tela original Overworld → Dreamscape, sem pausa artificial;
      inspecionar `latest.log`, FPS, TPS e latência de troca em servidor dedicado.

## Limitações

Nenhum teste de entidade/jogador vivo via GameTest foi adicionado: a CI
tem RCON sem jogador autenticado. Falhas/retries de `ServerPlayer.teleport`,
cópias de saves antigos, efeitos concorrentes de mods e transições de cliente
precisam de gameplay manual, com logs e cópias de mundos; não estão
certificados pela compilação. O rastreamento de efeito é deliberadamente
conservador, temporário e não persistido: no login com efeito já existente
ele só volta a ser aplicado pelo mod quando o efeito antigo expirar.

## Referências verificadas

- https://docs.neoforged.net/primer/docs/26.3/
- https://docs.neoforged.net/docs/concepts/events/
- https://docs.neoforged.net/docs/concepts/registries/
- https://docs.neoforged.net/docs/datastorage/attachments/
- https://github.com/neoforged/NeoForge/tree/26.3.x
- https://github.com/NeoForgeMDKs/MDK-26.3-ModDevGradle
- https://aldak.netlify.app/javadoc/26.3.x/net/neoforged/neoforge/client/event/registerdimensiontransitionscreenevent
- https://aldak0.ru/javadoc/26.3.x/net/neoforged/neoforge/event/tick/PlayerTickEvent.Post.html

**Nota de versões:** páginas gerais `docs/concepts` e `docs/datastorage`
estavam rotuladas 26.1 no momento da consulta; as assinaturas efetivas
da versão 26.3 são confirmadas pelo build/CI e javadocs específicos.
