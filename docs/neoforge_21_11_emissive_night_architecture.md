# Dreams Dimensions — análise técnica (NeoForge 21.11.x beta / MC 1.21.11)

## Objetivo
Definir um padrão estável para que blocos/plantas/árvores/solo com emissivo em JSON **brilhem somente à noite**, preservando compatibilidade com servidor dedicado e evitando custo excessivo.

---

## 1) Base técnica: o que é possível e o que não é

### 1.1 `light_emission` no modelo JSON é estático
No pipeline de modelos, `light_emission` é lido como inteiro (`0..15`) durante desserialização do JSON do modelo e armazenado no `BlockElement`.

- Campo no elemento: `public final int lightEmission;`
- Parse: leitura de `"light_emission"` como inteiro válido de 0 a 15.

Isso caracteriza metadado **de modelo** (asset), não uma regra dinâmica por hora/bioma. Não existe no próprio JSON de modelo um hook condicional por horário, bioma, chuva etc.

### 1.2 Blockstates escolhem modelo por propriedades/variantes
O sistema vanilla/NeoForge escolhe modelo via `blockstates/*.json` (variants multipart), como no padrão `lit=false`/`lit=true` do redstone lamp.

Conclusão prática: para ligar/desligar emissivo conforme noite, é preciso mudar **estado do bloco** (ou trocar bloco), para que o blockstate selecione modelo OFF/ON.

### 1.3 Evidência de padrão vanilla para transição temporal
Há lógica vanilla que transforma estado com ticks e `scheduleTick` (ex.: `EyeblossomBlock`), reforçando a estratégia “estado + tick agendado” como abordagem robusta server-side.

---

## 2) Avaliação comparativa das abordagens

## A) `BooleanProperty LIT` + variantes OFF/ON + tick agendado

### Como funciona
1. Bloco possui `BooleanProperty lit`.
2. Blockstate define variantes `lit=false -> modelo_off`, `lit=true -> modelo_on`.
3. No servidor, bloco reavalia regra de brilho e atualiza estado quando necessário.

### Tick ideal: `scheduledTick` como base, `randomTick` opcional
- **Base recomendada:** `scheduledTick` para previsibilidade e para alinhar virada dia/noite.
- **Opcional:** `randomTick` apenas como fallback/auto-correção em casos especiais (chunks que carregam em horários já “fora de fase”).

### Frequência recomendada
- Não fazer tick todo tick (20 TPS) por bloco.
- Reagendar em janela moderada (ex.: dezenas de segundos em ticks), com **jitter aleatório** por bloco/chunk para evitar rajada sincronizada.
- Próximo da virada (quando detectado horário limítrofe), pode usar atraso menor temporariamente para reduzir latência perceptível.

### Prós
- Consistente em singleplayer e dedicated server.
- Sem desync visual: servidor autoritativo no `BlockState`.
- Compatível com todos os tipos (cross/cutout, solo sólido, logs/rotated pillar).
- Vanilla-friendly (mesma filosofia de blocos com estado).

### Contras/riscos
- Se mal configurado, pode gerar bursts de updates.
- Precisa estratégia anti-spike (jitter/bucketização).

**Veredito da abordagem A:** melhor base arquitetural.

---

## B) Atualização por evento de mundo (transição dia/noite)

### Existe evento específico confiável de “day/night changed”?
No conjunto de eventos NeoForge inspecionado, não há um evento dedicado e universal “entrou noite/saiu noite” para todos os casos de mudança de tempo. Há eventos de tick de nível e eventos específicos como `SleepFinishedTimeEvent`, mas isso não cobre sozinho todos os caminhos (ex.: `/time set`, comandos/admin, datapacks, lógicas custom de tempo).

### Prós
- Em teoria, menos polling contínuo.

### Contras/riscos
- Dificuldade de cobertura completa dos cenários de alteração de tempo.
- Se depender de evento incompleto, haverá blocos desatualizados.
- Implementação tende a virar mistura de eventos + fallback por tick (complexidade sem ganho real).

**Veredito da abordagem B:** útil apenas como complemento, não como mecanismo único.

---

## C) Somente client-side (troca visual sem `BlockState`)

### É possível?
Até é possível construir solução render-only em client (BakedModel/custom), porém:
- estado lógico no servidor não muda;
- clientes podem divergir visualmente;
- debug/manutenção ficam piores;
- pode quebrar expectativa em multiplayer e replays.

### Prós
- Menos tráfego de update de bloco (aparente).

### Contras/riscos
- Alto risco de desync visual servidor/cliente.
- Menos vanilla-friendly.
- Maior complexidade para um problema que blockstate resolve limpo.

**Veredito da abordagem C:** armadilha para este caso.

---

## 3) Validação dos requisitos de design

## Requisito: SP + Dedicated sem inconsistência
Atendido com lógica server-side (estado no servidor; cliente apenas renderiza blockstate recebido).

## Requisito: sem spam de updates em massa
Mitigar com:
- `scheduledTick` com intervalo moderado;
- jitter aleatório por posição/chunk;
- atualização somente quando `lit` realmente precisa mudar;
- opcionalmente espalhar verificações por buckets de tempo.

## Requisito: compatível com cross/cutout, logs e solo
Atendido pelo mesmo padrão de blockstate:
- cross/cutout: modelos OFF/ON ambos no render type adequado;
- logs (`RotatedPillar`): manter variante de eixo + `lit` (combinação de propriedades);
- solo sólido: variante simples OFF/ON.

## Regra opcional: “somente no bioma Lumina Hollows”
Aplicável na função de decisão (`shouldBeLit`): exigir bioma alvo antes de ligar.

## Regra opcional: “somente noite e céu visível”
Aplicável na mesma função:
- noite global + teste de visibilidade de céu (`canSeeSky`/equivalente).
- impacto: folhas/copas podem bloquear céu para blocos abaixo; definir se isso é desejado (efeito mais “natural”) ou se algumas famílias ignoram essa condição.

---

## 4) Plano de testes (checklist prático)

1. **Virada de horário**
   - Testar `dia -> noite` e `noite -> dia`.
   - Validar tempo de reação aceitável (sem atrasos longos).

2. **Mundo novo e chunks já existentes**
   - Colocar blocos novos e salvar/recarregar.
   - Entrar em chunks antigos e confirmar convergência correta do estado.

3. **Performance em alta densidade**
   - Área grande com milhares de blocos emissivos.
   - Medir TPS/lag na virada; confirmar ausência de burst severo.

4. **Validação visual OFF**
   - Garantir que modelo OFF não contém camada emissiva/`light_emission`.

5. **Validação visual ON**
   - Garantir que modelo ON contém emissivo correto.

6. **Regras opcionais**
   - Bioma Lumina Hollows: ligar apenas nele.
   - Céu visível: conferir comportamento sob copa, cavernas e construções.

---

## 5) Recomendação oficial (padrão reutilizável)

**Melhor caminho:** abordagem **A** — `BooleanProperty lit` + modelos OFF/ON + atualização server-side por `scheduledTick` com jitter (e opcional fallback por `randomTick`).

### Fluxo recomendado
1. Cada família de bloco emissivo expõe `lit` no `BlockState`.
2. `blockstates` mapeia `lit=false` para modelo OFF e `lit=true` para ON.
3. Em tick servidor, bloco calcula regra (`noite`, bioma, céu visível etc.).
4. Se regra mudou, atualiza `lit`; se não mudou, apenas reagenda próxima checagem.

### Armadilhas a evitar
- Atualizar todos os blocos na mesma tick (pico de lag).
- Confiar em evento único de mudança de dia/noite (cobertura incompleta).
- Fazer solução apenas client-side (desync e manutenção ruim).

### Template de adoção no mod
- **Solo emissivo:** padrão simples `lit` ON/OFF.
- **Plantas emissivas (cross/cutout):** mesmo padrão, mantendo render type correto.
- **Troncos/folhas emissivos:** combinar `lit` com propriedades existentes (eixo/distância/decay etc.) sem quebrar variantes já usadas.

---

## Observações específicas do projeto Dreams Dimensions
- Hoje já há emissivo em modelos (`ow_lumina_flower`, `ow_somniflora`, `ow_dream_glow_moss`, `ow_somnibark_log`) via textura emissiva e/ou `light_emission` em elementos de modelo.
- `OW_DREAM_GLOW_MOSS` ainda tem emissão fixa também por `BlockBehaviour.Properties.lightLevel(state -> 8)`, que é outra fonte de luz estática no gameplay e deve ser tratada ao migrar para “somente à noite”.

---

## Referências utilizadas nesta investigação
- Código-fonte local (NeoForge/Minecraft decompilado e assets vanilla) disponível no workspace.
- Tentativa de consulta à doc online NeoForged bloqueada no ambiente (`HTTP 403 CONNECT tunnel failed`), então a validação foi feita por inspeção direta de fontes/classes/assets locais.
