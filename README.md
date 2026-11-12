# DreamsDimensions

**Mod Minecraft 26.3 · NeoForge 26.3.0.101 · Java 25 · GeckoLib 5.5.7 · Curios 17.0.1+26.3**

DreamsDimensions é um mod de **exploração e gameplay relacionados aos sonhos**. Seu universo prevê diversas dimensões futuras, incluindo sonhos pacíficos, pesadelos e outras experiências. As **duas dimensões existentes são a fundação técnica inicial, não o limite da identidade do mod**. O Overworld deve funcionar como uma aventura interessante por si só e sustentar os sistemas compartilhados entre sonhos.

> **Status real:** desenvolvimento na branch `migration/minecraft-26.3`; **sem merge na `main`**. O projeto tem conteúdo e sistemas compiláveis, mas a homologação completa de entrada/retorno em sonhos, experiência Survival, multiplayer e saves antigos ainda exige ensaios manuais. A entrada em sonhos está **desabilitada por padrão** pela configuração `dream_entry_enabled=false`, apesar de seu fluxo e políticas existirem no código.

## Documentação atual — começar aqui

| Documento | Finalidade |
| --- | --- |
| [Estado atual](docs/ESTADO_ATUAL.md) | O que existe, o que está em teste, o que está desabilitado e o que ainda é experimental |
| [Gameplay e direção](docs/GAMEPLAY_E_DIRECAO.md) | Identidade universal do mod, Overworld e regras para aprovar novas mecânicas |
| [Arquitetura atual](docs/ARQUITETURA_ATUAL.md) | Onde estão os sistemas implementados, registros e extensões |
| [Worldgen e conteúdo](docs/WORLDGEN_E_CONTEUDO.md) | Biomas, dimensões, recursos, estruturas, mobs, Pedra Marcada e limites de evidência |
| [QA e validação](docs/QA_E_VALIDACAO.md) | Build, testes, seeds, comandos e critérios para diferenciar CI de playtest |
| [Migração 26.3](docs/MIGRATION_26_3.md) | Histórico de compatibilidade e riscos de mundos/saves |
| [Task 8 — Qualidade visual](docs/TASK_8_VISUAL_QUALITY_PASS.md) | Correções de apresentação, auditoria de PNGs e limitações de revisão visual |

Os documentos técnicos detalhados que permanecem em `docs/` são contratos de **sistemas já implementados**, relatórios de medição real, instruções de QA ou evidências de riscos ainda relevantes. Documentos de design especulativo e roadmaps contraditórios foram retirados da árvore atual; podem ser consultados pelo histórico Git.

## Iniciar o projeto

Em Windows PowerShell, na raiz do repositório:

```powershell
git switch migration/minecraft-26.3
git pull origin migration/minecraft-26.3
.\gradlew.bat --no-daemon clean build
.\gradlew.bat runClient
```

Para iniciar um servidor de desenvolvimento:

```powershell
.\gradlew.bat runServer
```

**Aviso:** não ligue `dream_entry_enabled` em mundos importantes antes de validar a recuperação com jogador real. Execute testes de migração somente em **cópias** de saves, não em seus mundos originais.

## Onde fica o código

- `src/main/java/com/dreamsdimensions/mod/`: mod, registros, políticas e serviços.
- `src/main/java/com/dreamsdimensions/mod/dimension/`: catálogo de dimensões extensível.
- `src/main/java/com/dreamsdimensions/mod/event/`: sono, retorno, efeitos e interações.
- `src/main/java/com/dreamsdimensions/mod/entity/`: entidades e framework de população/IA.
- `src/main/java/com/dreamsdimensions/mod/qa/`: telemetria opt-in.
- `src/main/resources/data/dreamsdimensions/`: worldgen, dimensões, estruturas, receitas, loot.
- `src/main/resources/assets/dreamsdimensions/`: modelos, texturas, client item definitions e traduções.
- `scripts/ci/` e `.github/workflows/build.yml`: testes estáticos e sondas de runtime nas quatro seeds.

**Regra para próximas tasks:** primeiro comprovar a função de gameplay e o consumidor de um recurso; depois registrar novos blocos/itens/entidades. Não transformar protótipos ou documentação em funcionalidades declaradas como existentes.
