# Dreams Dimensions

**Minecraft 26.3 · NeoForge 26.3.0.101 · Java 25 · Gradle 9.2.1 · ModDevGradle 2.0.148**

Mod de exploração de dimensões oníricas: ao dormir em uma cama do Overworld por **100 ticks**, o jogador visita uma dimensão de sonho. O item **Despertador Onírico** (`dreamsdimensions:oneiric_awakener`) permite retornar ao Overworld após **60 ticks de uso**.

> **Status da migração:** desenvolvimento na branch `migration/minecraft-26.3`. A existência do código compilável não comprova o carregamento das dimensões, a qualidade do terreno ou a compatibilidade com saves antigos. Leia [o registro da migração](docs/MIGRATION_26_3.md) antes de usar em um mundo importante.

## Mecânicas e conteúdo

- **Dimensões:** `dreamsdimensions:dreamscape` e `dreamsdimensions:campo_onirico_azul`, definidas por datapacks.
- **Sono:** no servidor, o `SleepTeleportHandler` acompanha 100 ticks contínuos; seleciona uma dimensão configurada e procura um ponto seguro.
- **Retorno:** o `OneiricAwakenerItem` canaliza por 60 ticks e aplica cooldown. O `DreamReturnHelper` prioriza a cama válida no Overworld, o respawn pessoal válido e um spawn mundial seguro. Se nenhum local for seguro, o retorno não deve ocorrer.
- **Estado do jogador:** attachment `dreamsdimensions:dream_return`; persistência e migração de mundos precisam ser verificadas.
- **Efeitos:** Slow Falling no `campo_onirico_azul`.
- **Blocos e itens:** blocos de terreno, minérios, `dream_dust`, `oneiric_awakener`, aba criativa, receitas e loot tables.

Não altere os IDs de blocos, itens e dimensões ao corrigir incompatibilidades: eles fazem parte da identidade dos mundos salvos.

## Ambiente de desenvolvimento

Use um **JDK 25** instalado ou disponibilizado pelo Gradle Toolchains e um IDE que suporte Java 25 (IntelliJ IDEA 2025.2+, por exemplo). O wrapper gerencia a versão do Gradle, não é preciso instalá-lo separadamente.

Em Linux/macOS:

```bash
./gradlew clean build
./gradlew runClient
./gradlew runServer
```

No Windows PowerShell:

```powershell
.\gradlew.bat clean build
.\gradlew.bat runClient
.\gradlew.bat runServer
```

Para data generation, quando habilitada no ModDevGradle, use `./gradlew runData`. Consulte `./gradlew tasks` para os nomes efetivos.

O build utiliza o plugin `net.neoforged.moddev`. As propriedades do projeto estão em `gradle.properties`; o arquivo de metadados é gerado a partir de `src/main/templates/META-INF/neoforge.mods.toml`.

## Organização

| Diretório | Responsabilidade |
|---|---|
| `src/main/java/com/dreamsdimensions/mod/registry/` | Blocos, itens, abas e attachments |
| `.../event/` | Sono, spawn, efeitos dimensionais e eventos do servidor |
| `.../item/` | Despertador e outros itens |
| `.../attachment/` | Dados persistentes de retorno |
| `.../util/` | Resolução segura do destino de retorno |
| `.../client/` | Tela de transição e recursos client-only |
| `.../config/` | Configuração de dimensões oníricas |
| `src/main/resources/data/dreamsdimensions/dimension/` | Dimensões |
| `src/main/resources/data/dreamsdimensions/dimension_type/` | Regras e limites das dimensões |
| `src/main/resources/data/dreamsdimensions/worldgen/` | Biomas, noise settings e features |
| `src/main/resources/data/dreamsdimensions/loot_table/` | Loot tables |
| `src/main/resources/assets/dreamsdimensions/` | Modelos, texturas, traduções |

## Testes antes de publicar

1. Compilar no CI (`clean build`) com JDK 25 e verificar o artifact gerado.
2. Abrir o cliente Minecraft 26.3 e iniciar um servidor dedicado.
3. Criar um mundo **novo** e entrar nas duas dimensões, verificando geração de chunks, biomas, superfícies e altura.
4. Testar sono por 100 ticks, transição, retorno pelo Despertador por 60 ticks e cooldown, incluindo multiplayer.
5. Verificar minérios, blocos, drops, receitas, modelos, tradução e efeitos.
6. Testar cama destruída, falta de respawn, reconexão, morte e desconexão.
7. Somente então testar **cópias** dos saves antigos, nunca os originais.

**Atenção:** build não valida codecs dinâmicos de worldgen; testes de runtime são indispensáveis. Não existe garantia de compatibilidade dos saves Minecraft 1.21.4 com 26.3 sem testes específicos.

## Referências

- [NeoForge MDK 26.3](https://github.com/NeoForgeMDKs/MDK-26.3-ModDevGradle)
- [Migration Primers](https://docs.neoforged.net/primer/docs/)
- [Primer 26.3](https://docs.neoforged.net/primer/docs/26.3/)
- [NeoForge código-fonte](https://github.com/neoforged/NeoForge)
- [ModDevGradle](https://github.com/neoforged/ModDevGradle)
