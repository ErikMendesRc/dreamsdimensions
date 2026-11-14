# Migração do Dreams Dimensions: Minecraft 1.21.4 → 26.3

**Estado em 12/11/2026: migração de código e validações de CI realizadas; homologação de gameplay, multiplayer e saves antigos continua pendente.**

**Esta é a cronologia técnica da migração, não o relatório de estado atual.** Para registros/funcionalidades existentes e QA executado depois da migração, consultar [Estado atual](ESTADO_ATUAL.md) e [QA](QA_E_VALIDACAO.md). 
Trabalho isolado em `migration/minecraft-26.3`; não foi realizado merge em `main`.

## 1. Estado de partida e stack final

| Componente | Anterior | Novo |
|---|---|---|
| Minecraft | 1.21.4 | **26.3** |
| NeoForge | 21.4.124 | **26.3.0.101** |
| JDK | 21 | **25** |
| Gradle | 8.13 | **9.2.1** |
| Build plugin | NeoGradle 7.0.173 | **ModDevGradle 2.0.148** |
| Mappings | Parchment 2025.03.23 | Mappings oficiais |

O template `NeoForgeMDKs/MDK-26.3-ModDevGradle` orienta a configuração de plugins, execução e metadados. O mod ID continua sendo `dreamsdimensions` e o pacote base continua `com.dreamsdimensions.mod`.

## 2. Mudanças Java

- `ResourceLocation` → `Identifier` onde exigido pelas APIs oficiais.
- Atualização de assinaturas de `DeferredRegister`, `DeferredBlock`, `DeferredItem` e criação com propriedades.
- Código de inicialização, configuração, eventos e lifecycle atualizado para a versão 26.3.
- Estado de retorno persistente usa `MapCodec` no attachment `dream_return`.
- `SleepTeleportHandler`: contador do sono por `UUID`, limpeza em logout, teleporte uma única vez ao completar 100 ticks e seleção entre dimensões configuradas.
- `DreamReturnHelper`: retorno prioritário à cama realmente válida, respawn pessoal válido ou spawn seguro, sem Y fixo.
- `OneiricAwakenerItem`: APIs atuais de tooltip, interação, mensagens, cooldown e teleporte; preserva canalização por 60 ticks.
- Tela "Você está sonhando..." usa `LevelLoadingScreen` e `GuiGraphicsExtractor`, isolada do servidor dedicado.

**Nota:** compilar não garante que o fluxo completo de sono, morte, reconexão ou multiplayer funcione. Estes cenários ainda exigem testes com jogadores.

## 3. Geração de mundo e datapacks

- Preservadas as dimensões `dreamsdimensions:dreamscape` e `dreamsdimensions:campo_onirico_azul` e os biomas correspondentes.
- `worldgen/configured_feature` migrou para `worldgen/feature` com configuração inline. Os nomes dos minérios foram preservados.
- Atualização de `noise_router` (incluindo `chunk_surface_level`), funções de densidade, `material_rule` e configuração de `aquifers`.
- Conversão de estados de blocos da representação `Name/Properties` para o formato suportado em 26.3.
- Migração de `spawn_target` do formato legado de parâmetros climáticos para os identificadores das density functions de 26.3, mantendo faixas equivalentes.
- Biomas usam `attributes` para céu, neblina e natural mob spawns conforme os registros atuais.
- Tipos de dimensões usam `attributes`, `skybox`, `has_fixed_time`, `default_clock` e `timelines`.
- Inconsistência original na altura de `campo_onirico_azul` (dimension type 0/256 versus noise -64/384) corrigida para **-64 / 384 em ambos**.
- Loot tables migradas de `loot_tables` para `loot_table`; tags de blocos consolidadas em `tags/block`.
- IDs das dimensões, blocos e itens existentes preservados.

**Limitação de fidelidade visual:** o antigo `fixed_time: 6000` não existe mais na arquitetura nova. Foi mapeado para `has_fixed_time`, mas o ângulo de iluminação/relógio deve ser validado visualmente.

## 4. Arquivos e sistemas afetados

- Build: `build.gradle`, `settings.gradle`, `gradle.properties`, wrapper, template de `META-INF/neoforge.mods.toml`.
- Java: classe principal; registries; config; attachments; sono; efeitos; retorno; Despertador Onírico; eventos e tela client-side.
- Data: `dimension`, `dimension_type`, `worldgen/biome`, `worldgen/noise_settings`, `worldgen/feature`, loot tables, tags.
- CI: `.github/workflows/build.yml` (JDK 25, build, JAR e smoke test de servidor) e `scripts/ci/check_dimensions.py` (requisições de carga de chunks em ambas as dimensões).
- Documentação: `README.md` e este arquivo.

## 5. Evidências verificadas

1. Builds anteriores: foram registrados 39 erros de compatibilidade Java, posteriormente reduzidos a um e corrigidos.
2. `./gradlew --no-daemon clean build`: **aprovado** em CI com JDK 25. JAR gerado e disponibilizado como artifact.
3. Testes iniciais de `runServer`: falhas reais de codec nos datapacks; erros identificados e corrigidos, sem remover as duas dimensões.
4. [GitHub Actions — execução 42510741827](https://github.com/ErikMendesRc/dreamsdimensions/actions/runs/42510741827): **build aprovado, artifact aprovado, inicialização de servidor dedicado e carregamento de datapacks aprovados**.
5. **Verificação RCON de ambas as dimensões aprovada**, comprovada no [GitHub Actions — execução 42511901634](https://github.com/ErikMendesRc/dreamsdimensions/actions/runs/42511901634). Comandos `execute in dreamsdimensions:dreamscape run forceload add/query 0 0` e `execute in dreamsdimensions:campo_onirico_azul run forceload add/query 0 0` retornaram sucesso. Isso comprova a aceitação da carga de chunks, **não** a aparência ou integridade visual do terreno gerado.
6. Os três avisos `Unreachable entry` anteriormente registrados nas loot tables de `blue_dream_grass`, `dream_ore` e `deepslate_dream_ore` foram corrigidos. No log do run **42511901634** não foram encontrados mais esses avisos. Silk Touch, Fortune e drops reais ainda precisam de teste em gameplay.
7. Não foram executados testes visuais de `runClient`, GameTests automatizados de gameplay nem testes reais de multiplayer.

A etapa de smoke test utiliza um mundo descartável em runner do GitHub Actions, com arquivo EULA e propriedades temporários. Não publicar esse mundo.

## 6. Checklist manual obrigatório

- [ ] Iniciar Minecraft 26.3 + NeoForge 26.3.0.101 com o mod e abrir a aba criativa.
- [ ] Criar um mundo NOVO e validar `dreamscape`: chunks, terreno, superfície, luz, bioma e ponto seguro de spawn.
- [ ] Fazer o mesmo para `campo_onirico_azul`, confirmando altura -64/384 e sua identidade visual.
- [ ] Verificar minérios `dream_ore` e `deepslate_dream_ore` realmente presentes na geração.
- [ ] Dormir 100 ticks no Overworld e confirmar um único teleporte.
- [ ] Canalizar o Despertador Onírico por 60 ticks, voltar à cama e confirmar cooldown.
- [ ] Testar cama destruída, respawn pessoal e fallback de spawn mundial.
- [ ] Testar multiplayer, morte, respawn, logout, reconexão e reentrada.
- [ ] Conferir receitas, loot, modelos, texturas, traduções e `DreamOreBlock.clicked`.
- [ ] Conferir `campo_onirico_azul` com `has_fixed_time`: tempo/luz equivalentes à experiência anterior.
- [ ] Testar **CÓPIAS** de mundos criados em 1.21.4.

## 7. Compatibilidade de mundos

**Não comprovada.** A migração pode alterar geração de chunks, propriedades/formatos de dados, serialização de attachments e comportamento de biomas. Não abrir um save importante sem backup. Comparar cópias de saves entre 1.21.4 e 26.3 e documentar incompatibilidades encontradas.

## 8. Pendências antes de retirar o PR de Draft

A CI de compilação e de boot valida apenas uma parte. Faltam evidências de geração e aparência real dos dois terrenos, minérios, movimentação do jogador, efeitos, persistência, retorno e multiplayer. Se os testes de chunk por CI passarem, a qualidade visual e a jogabilidade continuam pendentes de inspeção.

## 9. Referências oficiais

- [MDK 26.3](https://github.com/NeoForgeMDKs/MDK-26.3-ModDevGradle)
- [NeoForge Migration Primers](https://docs.neoforged.net/primer/docs/)
- [Primer 26.3](https://docs.neoforged.net/primer/docs/26.3/)
- [NeoForge 26.3](https://github.com/neoforged/NeoForge/tree/26.3.x)
- [ModDevGradle](https://github.com/neoforged/ModDevGradle)
- [Minecraft Java Edition 26.3 — mudanças de datapacks](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3)
