# TASK 7.0 — HOMOLOGAÇÃO E SEGURANÇA DA RECUPERAÇÃO ONÍRICA

**Projeto:** DreamsDimensions — Minecraft 26.3 / NeoForge 26.3 / Java 25.  
**Branch de integração:** `migration/minecraft-26.3`.  
**Data:** 12/11/2026.  
**Escopo implementado:** correção de terreno da ruína `fragmented_dream_ruins` e critérios estáticos/runtime de suporte, sem alterar economia ou retorno.  
**Escopo ainda pendente:** homologação de rota em Survival, loot **efetivamente aberto** em baús naturais, persistência após restart, multiplayer com duas contas, estimativas de tempo para recuperar e regressão em saves antigos.  
**Status formal:** **CORREÇÃO DE WORLDGEN APROVADA EM CI 4/4; HOMOLOGAÇÃO INTEGRAL DE GAMEPLAY AINDA NÃO APROVADA**.  
**Referências:** [5.7 — ruínas e baús](DREAM_RUINS_TASK_5_7.md), [6.8 — balanceamento](OVERWORLD_ECONOMY_RARITY_BALANCING_TASK_6_8.md), [6.9 — roadmap executivo](OVERWORLD_EXECUTIVE_IMPLEMENTATION_ROADMAP_TASK_6_9.md).

> Esta task NÃO deve ser interpretada como prova de que o jogador consegue recuperar o Despertador em tempo viável em qualquer seed. A correção removeu **um defeito reproduzível na checagem de sustentação**; o teste completo de exploração e retorno permanece separado.

---

## 1. Causa raiz confirmada

A ruína `dreamsdimensions:fragmented_dream_ruins` usa uma `minecraft:jigsaw` com projeção `WORLD_SURFACE_WG` em ilhas do Dreamscape. Antes desta correção, a estrutura declarava:

```json
"terrain_adaptation": "none"
```

Na seed `20261108`, o baú natural observado em **300, 137, -4** tinha `LootTable` correta e um local acessível lateralmente, mas o diagnóstico `dream_ruins_audit` mediu **4/9 suportes naturais** sob o piso 3×3 ao redor do baú, abaixo do contrato mínimo de **5/9**.

Os dados históricos de 5.7 tiveram **8/8 baús** e **7/8 ruínas aprovadas no suporte mínimo**, de quatro seeds e duas dimensões. Isso não era uma perda de loot; era um alerta de terreno/sustentação. O script antigo emitia `::warning::`, e a CI continuava verde.

### 1.1 Reproduzimos o defeito com um teste de controle

**Experimento A — alteração apenas no structure JSON:**

- Mudança: `terrain_adaptation: none → beard_thin` exclusivamente na ruína fragmentada.
- Adaptamos validação estática à nova propriedade; o auditor de runtime passou a **falhar** quando `stable_chests != 1`.
- Execução GitHub Actions: [43271268591](https://github.com/ErikMendesRc/dreamsdimensions/actions/runs/43271268591).
- Seed `20261108`: `DREAM_RUINS_AUDIT ... stable_chests=0`; log Java: `support=4/9:walkable=true`; o job **falhou**, como deveria.

**Conclusão:** apenas mudar `terrain_adaptation` era **insuficiente** no Minecraft 26.3.

### 1.2 Razão técnica específica de Minecraft 26.3

A partir do Minecraft 26.3, `noise_router.final_density` **não recebe mais `beardifier` implicitamente**. O tipo `minecraft:beardifier` deve ser incluído explicitamente na densidade final quando se quer efeito de adaptação de terreno de estruturas.

Fonte oficial: [Minecraft Java 26.3 — Noise Settings / Density Functions](https://www.minecraft.net/en-us/article/minecraft-java-edition-26-3).

---

## 2. Correção funcional aplicada

### 2.1 Somente a ruína do Dreamscape recebe adaptação

Arquivo:
`src/main/resources/data/dreamsdimensions/worldgen/structure/fragmented_dream_ruins.json`

```json
"terrain_adaptation": "beard_thin"
```

**Não** alteramos a ruína `blue_dream_ruins`, seus parâmetros nem qualquer `structure_set`. Não aumentamos a frequência de ruínas.

### 2.2 Ativar explicitamente o efeito no ruído do Dreamscape

Arquivo:
`src/main/resources/data/dreamsdimensions/worldgen/noise_settings/dreamscape_noise.json`

O campo `noise_router.final_density` foi convertido de interpolação pura para:

```json
"final_density": {
  "type": "minecraft:add",
  "left": {
    "type": "minecraft:interpolated",
    "input": "dreamsdimensions:dreamscape_solid_density",
    "cell_size_xz": 4,
    "cell_size_y": 4
  },
  "right": {
    "type": "minecraft:beardifier"
  }
}
```

**Intenção:** manter o terreno-base e acrescentar a adaptação de estruturas **próximas** com a opção `beard_thin`. Fora da influência de estruturas a contribuição é neutra. Isso não adiciona blocos manualmente após geração, não cria pontes gratuitas e não substitui a geometria do Dreamscape por um novo noise.

**Risco residual conhecido:** novas gerações próximas de ruínas podem ter diferenças locais de relevo; a configuração **não modifica chunks antigos nem conserta ruínas já geradas**. Um save iniciado antes do patch pode continuar com estrutura antiga em 4/9. Avaliar transição entre chunks em saves persistentes no QA humano.

### 2.3 Proteger a correção com testes automatizados

Arquivos alterados:
- `scripts/ci/check_dream_ruins.py`: contrato por estrutura, `none` para Campo Azul e `beard_thin` para Dreamscape; assert explícito de `minecraft:add` + `minecraft:beardifier` no noise de Dreamscape.
- `scripts/ci/check_dreamscape_geography.py`: asserção de estrutura de `final_density` 26.3 sem perder a referência original à densidade geológica do Dreamscape.
- `scripts/ci/check_dreamscape_homologation.py`: proteção do mesmo contrato na regressão de ilhas, pontes e density functions.
- `scripts/ci/audit_dream_ruins_runtime.py`: `stable_chests != 1` deixou de ser aviso e agora **faz o job falhar**. O relatório JSON é gravado **antes** da asserção, para preservar diagnóstico.

**A auditoria Java `DreamRuinsAuditCommand` não sofreu alterações.** Continua opt-in, loaded-only, sem mutação de baú, worldgen ou tickets de chunks.

---

## 3. Evidência real: comparação e regressão

**Experimento B — `beard_thin` + `minecraft:beardifier` explícito:**

- Commit testado em branch isolada: [`6d9d39a52c1bc5a30843c0d553bc2eb3fc6bfd34`](https://github.com/ErikMendesRc/dreamsdimensions/commit/6d9d39a52c1bc5a30843c0d553bc2eb3fc6bfd34).
- GitHub Actions: **[43272173119](https://github.com/ErikMendesRc/dreamsdimensions/actions/runs/43272173119)**.
- Resultado: **4/4 jobs aprovados**, Java 25, Gradle, datapacks e servidor dedicado por seed, auditoria natural de duas dimensões por mundo e gate de suporte obrigatório.

| Seed | Campo Azul (baú, loot, suporte mínimo) | Dreamscape (baú, loot, suporte mínimo) | Job |
|---|---|---|---|
| `14011` | PASS | PASS | **PASS** |
| `20261108` | PASS | **PASS — anteriormente 4/9 e reprovado** | **PASS** |
| `-904732` | PASS | PASS | **PASS** |
| `73199013` | PASS | PASS | **PASS** |

**O que PASS significa aqui:**
- Estrutura `/locate` nativa encontrada pelo dedicated server na seed fixada.
- Baú real existente e com referência de `LootTable` pendente correta.
- `stable_chests=1`: tampa/posição lateral com condição de andar e pelo menos **5/9** suportes no teste local.
- Zero aviso ignorado sobre suporte mínimo — a CI termina com erro se o teste falhar.
- A validação funciona em quatro seeds, incluindo o caso que falhava no Experimento A.

**O que PASS NÃO significa:**
- Inspeção do perímetro completo das ruínas ou conexões entre ilhas.
- Travessia em Survival sem comandos até ruínas, baús e retorno.
- Prova de conteúdo rolado de loot, chance empírica 25% ou distribuição global de ruínas.
- Persistência do inventário depois de dois jogadores e restart.
- Segurança de todos os starts e biomas possíveis além das regiões examinadas.

Não registrar `4/9 → 9/9` porque **o valor exato de suporte pós-correção não foi incluído no resumo estruturado**. O resultado observado é que o teste **passou**, portanto é **≥5/9 e lateral acessível** no escopo da auditoria.

---

## 4. Contratos preservados — sem alterações

- 2 dimensões e biomas existentes.
- 2 ruínas nativas com `/locate` e jigsaw, `spacing=38/32`, `separation=12/12`.
- Um baú principal de recuperação por ruína, loot não regenerativo.
- Chance configurada **25%** de um `dreamsdimensions:oneiric_awakener` por baú dimensional; sem `dream_dust`.
- Receita de Despertador, Núcleo, Colar e Dream Dust; economia R0 **3+12N Dust**.
- Colar não é consumido ao entrar; Despertador tem **60 ticks** e consome exatamente uma unidade **somente após retorno confirmado** ao Overworld.
- `DreamRespawnService`, `DreamReturnHelper`, `OneiricReturnPolicy`, `DreamEntryPolicy`, `OneiricAwakenerItem`, Curios e regras de dormir/morte inalterados.
- `dream_entry_enabled=false` permanece desabilitado por padrão até os testes de Survival/multiplayer e a liberação explícita.
- Assets/GeckoLib, criaturas, worldgen de minérios e Overworld não alterados.
- Nenhuma alteração na `main` nem merge automático.

---

## 5. Comandos de teste rápido no Minecraft

### 5.1 No servidor local / mundo descartável

Primeiro, criar um mundo **novo** com seed `20261108` na build atualizada. Se chunks da ruína já existirem antes da correção, **o mundo não retrogera a estrutura** e pode continuar exibindo 4/9.

```mcfunction
/seed
/gamemode spectator
/execute in dreamsdimensions:dreamscape run locate structure dreamsdimensions:fragmented_dream_ruins
/execute in dreamsdimensions:campo_onirico_azul run locate structure dreamsdimensions:blue_dream_ruins
```

### 5.2 Inspeção pontual da seed problemática

As coordenadas abaixo são da **amostra histórica** da seed `20261108` e destinam-se **somente a Spectator**; a altura e o piso devem ser reverificados no mundo novo:

```mcfunction
/gamemode spectator
/execute in dreamsdimensions:dreamscape run tp @s 300 139 -4
/execute in dreamsdimensions:campo_onirico_azul run tp @s 133 143 59
```

Examinar visualmente a plataforma por cima/baixo, apoios centrais, bordas, entrada do pátio e caminho viável desde ilha/plataforma. **Não** trocar para Survival nessa localização antes de encontrar uma posição estável e respirar/andar com segurança.

### 5.3 Teste do baú e da persistência (manual)

Após confirmar a coordenada XYZ **atual** do baú inspecionado:

```mcfunction
/execute in dreamsdimensions:dreamscape run data get block X Y Z LootTable
```

Substituir `X Y Z` pela coordenada efetivamente medida no **seu mundo**. Antes da primeira abertura, o baú deveria ter referência pendente a `dreamsdimensions:chests/fragmented_dream_ruins`. Depois, abri-lo **uma única vez por um jogador**, registrar `Items`, salvar/reiniciar servidor e verificar com a segunda conta que o conteúdo não foi regenerado. A ausência de Despertador numa abertura individual é **resultado esperado possível** (75%).

```mcfunction
/save-all flush
```

`/loot give` testa pool artificialmente, **não comprova** que o baú real permaneceu intacto no save.

### 5.4 Teste manual do Despertador (apenas após achar piso seguro)

```mcfunction
/give @s dreamsdimensions:oneiric_awakener 1
/give @s dreamsdimensions:dream_necklace 1
```

Testar em **Survival com coordenadas seguras** e equipamento de recuperação: entrar numa das duas dimensões, segurar uso do Despertador por **60 ticks** e confirmar destino Overworld e consumo **uma única vez**. Validar os casos de cancelamento, troca de mão, cooldown, relog e destino inválido; se falhar, **não consumir**. Morte/cama não são saída gratuita.

**Teste independente da economia:** entrar e retornar **sem /give** e **sem /tp**, localizar pelo menos uma ruína por exploração natural, abrir o chest e registrar dificuldade/tempo real. Esse teste continua PENDENTE.

### 5.5 Diagnóstico técnico opt-in

Somente com JVM `-Ddreamsdimensions.enableRuinsAudit=true`, permissão admin e todos os chunks da região carregados **em um servidor de teste**:

```mcfunction
/dream_ruins_audit fragmented 304 0 24
/dream_ruins_audit blue 128 64 24
```

Estas são coordenadas X/Z de **starts naturais da seed `20261108`** do workflow de 5.7/7.0, não coordenadas de baú nem comandos de teleporte. O diagnóstico não carrega chunks nem modifica o mundo. Se retornar `chunk_not_loaded`, deslocar-se ou carregar a área no mundo descartável antes de testar; não instalar `/forceload` permanente no servidor público.

---

## 6. QA de release: aberto versus concluído

| Gate de 7.0 | Status após correção | Evidência/limite |
|---|---|---|
| JSON 26.3 / 2 ruínas jigsaw | **PASS** | CI estática e loaded datapacks |
| Corrigir o 4/9 da seed `20261108` | **PASS no critério local** | CI obrigatória `stable_chests=1` |
| Quatro seeds, dois baús cada | **PASS** | Workflow 43272173119 4/4 |
| Itens, recipes, loot e chance inalterados | **PASS por diff e scripts** | Arquivos de economia/retorno não alterados |
| Verificação de perímetro/apoio total da ruína | **NOT RUN** | 3×3 é medição local limitada |
| Percurso real sem cheats até baú | **NOT RUN** | /locate não mede travessia |
| Despertador após 60 ticks, consumo e destino | **NOT RUN nesta task** | Políticas antigas continuam, mas sem novo manual |
| Persistência de baú após restart | **NOT RUN** | Não abriram chest real na auditoria |
| Abertura de loot no motor Minecraft e 25% empírico | **NOT RUN** | Taxa 25% verificada na configuração |
| Dois jogadores e diferentes inventários | **NOT RUN** | Requer sessão multiplayer |
| Save antigo em chunks já gerados / transições | **NOT RUN** | Sem retrogen automática |
| P50/P90 de recuperar Despertador | **NOT RUN** | Necessário playtest de exploração |

**Porta de release:** manter `dream_entry_enabled=false` como padrão até executar os gates humanos de sobrevivência, loot, saves e multiplayer. **A CI 4/4 não é autorização para ativar entrada irrestrita.**

---

## 7. Risco, rollback e próximos testes

- A correção de terrain adaptation se aplica a **novos chunks**; estruturas geradas em saves antigos não se reconstróem.
- Se for observada mudança geológica exagerada ao redor da ruína, uma regressão em ilha/ponte ou seam entre chunks, registrar seed/coords e comparar com o commit base; **não esconder alterando probabilidades de baú**.
- Revert do datapack/JSON impede futuras adaptações, mas não remove geografia já gerada. Preservar backups de mundos.
- Aumentar para `beard_box`, gerar pilares artificiais, alterar heightmap global ou forçar `/setblock` de suporte **não** foi aprovado.
- Se os testes de Survival revelarem outro problema, abrir correção mínima reproduzível; não remodelar sistema de sonhos nem criar motor de estruturas próprio sem prova.

**Próximo passo pós-merge:** realizar QA manual desta Task 7.0 em **mundo novo e duas contas** e registrar os resultados honestamente; somente depois fechar o P0 de recuperação e avançar para a 7.1/7.3 conforme roadmap 6.9.

---

*Fim do relatório — Task 7.0; correção de suporte confirmada em CI, homologação Survival ainda pendente.*
