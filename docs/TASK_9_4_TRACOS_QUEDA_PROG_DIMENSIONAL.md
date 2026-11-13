# Task 9.4 — Traços, Queda e Primeira Progressão Dimensional

Projeto DreamsDimensions · Minecraft 26.3 · NeoForge 26.3 · Java 25
Branch `migration/minecraft-26.3` — NÃO realizar merge na `main`.
Referência: documento Game Design v2, Fatia 3 — primeiro ciclo de Traços e Queda.

## Contrato funcional

O Dreamscape possui o Traço `FALL` no catálogo; o Campo Onírico Azul permanece sem Traços de risco (mantém o comportamento pacífico). `DreamTrait` admite cinco traços sem codificar efeitos diretamente por ID de dimensão. Cada definição valida no máximo três Traços. A queda e os equipamentos consultam `definition.traits()`; futuras dimensões com Queda reutilizarão o mesmo sistema.

## Economia de recursos e exploração

1. `plumite_ore` nasce em pequenos afloramentos de pedra exposta em montanhas >= Y120 (peaks/snow slopes/windswept): no máximo uma tentativa em sete por chunk elegível. Cada afloramento tem 3–5 blocos, condicionado a terreno real e espaço permitido. Em loot: 1–2 Lascas de Plumita, Fortune/Silk Touch. São valores-alvo, não resultados de medição em seeds.
2. 9 `plumite_shard` ↔ 1 `plumite_block` (reversível). O bloco serve como superfície de pouso sem dano de queda.
3. `reminiscence` sai de cada Ruína de Recuperação (1 garantida) e dos encontros já existentes com Fragmento Errante (observação calma) e Vigia Adormecido (pacto). Sem receita no Overworld. A ruína mantém o Rachado garantido e a chance de íntegro de 25%.

## Preparação, ferramentas e receitas

| Recurso | Insumos e estação | Utilidade |
|---|---|---|
| Chaleira Onírica | 4 Lingotes de Cobre + 1 Pó Onírico na bancada | Caldeira persistente sobre fogueira acesa; até 3 garrafas, Erva Torrada + reagente; 400 ticks |
| Infusão de Leveza | Na Chaleira: água + Erva Torrada + Lasca de Plumita | 3 min: metade do dano de queda e Salto I, sem Queda Lenta |
| Infusão de Vigília | Na Chaleira: água + 2 Ervas Torradas (base+reagente) | Efeito de Vigília de 3 min, preparado para interagir com o futuro Traço Torpor |
| Forro de Pluma | 3 Lascas + Couro + Pó Onírico | Usar na mesa de ferraria com Forro na mão principal e armadura na secundária; redução de 35% por peça, limitada a 2 |
| Amarra de Vigília | Na interface do Núcleo: Carga de Retorno + 2 Lascas + Linha | Consumo ao armar. Resgate único quando abaixo do limite do mundo, válida por 120 s |
| Ponte de Pluma | 4 Lascas + Pó Onírico -> 4 itens | Quando em queda/vazio, cria piso temporário 3×3 sob o jogador, 600 ticks |

**Observação importante:** o Núcleo continua produzindo Despertador com Carga + Pena + Frasco, sem consumo do bloco. A Amarra é uma segunda receita da mesma interface. A Chaleira não tem interface inventário própria nesta Fatia; possui interações in-world, mensagens de status e NBT persistente. O encaixe do Forro utiliza a mesa de ferraria como ponto de interação, sem substituir a UI vanilla; é uma mecânica de aplicação funcional e não uma recipe serializer da smithing table.

## Direção de arte

Todas as novas texturas foram desenhadas em resolução nativa, sem upscale/blurring:
- `plumite_ore.png` (16×16): rocha cinza natural com fratura mineral azul-prateada, de leitura distinguível do minério onírico.
- `plumite_block.png` (16×16): material compactado claro, marcado por veios cristalinos e planos facetados, com bordas minerais.
- `plumite_shard.png` (16×16): fragmento estreito translúcido azul com cluster de highlights e sombras ciano-acinzentadas.
- `reminiscence.png` (16×16): memória mineral encapsulada, contraste frio e rebordo metálico desgastado.
- `kettle_top/side/bottom.png` (16×16): cobre temperado e pátina discreta; corpo com banda metálica e tampa.
- `infusion_lightness/infusion_vigil.png` (16×16): frascos irmãos com cores distintas e material vítreo.
- `plume_lining`, `vigil_tether`, `plume_bridge` (16×16): couro, tecido e metal artesanal; silhuetas distintas.
- `mob_effect/lightness`, `mob_effect/vigil_infusion` (18×18): leitura nativa no HUD.
Não aplicar cores neon exageradas, antialias, ou filtros de escala a esses assets.

## Segurança, regressão e limites

- Queda condicionada ao traço; não altera diretamente o Blue Field.
- Amarra guarda âncora apenas em memória da sessão; desarma após expiração, morte, logout, dimension switch ou rescue. Não duplica itens e não faz teleporte interdimensional.
- Ponte só escreve em nove blocos de ar livres, todos prechecados, dentro da borda do mundo. Os blocos expiram e não soltam loot.
- Chaleira salva componentes mínimos e devolve materiais ou Infusões no descarte; faz uma única mistura e tem estado persistido no block entity, sem tick global.
- Não altera o item Despertador, retorno confirmado, âncoras de respawn nem entrada por sono; `dream_entry_enabled=false` permanece em produção até 9.3 homologada.
- `Reminiscence` é um insumo de progressão futura, porém já tem fontes legítimas exclusivas da exploração onírica.

## Comandos em mundo descartável

```mcfunction
/give @s dreamsdimensions:plumite_ore 16
/give @s dreamsdimensions:plumite_shard 64
/give @s dreamsdimensions:plumite_block 16
/give @s dreamsdimensions:reminiscence 16
/give @s dreamsdimensions:oneiric_kettle 1
/give @s dreamsdimensions:plume_lining 4
/give @s dreamsdimensions:vigil_tether 8
/give @s dreamsdimensions:plume_bridge 8
/give @s dreamsdimensions:infusion_lightness 4
/give @s dreamsdimensions:infusion_vigil 4
/give @s dreamsdimensions:toasted_vigil_herb 16
/give @s dreamsdimensions:return_charge 8
/give @s minecraft:potion[potion_contents={potion:"minecraft:water"}] 3
/give @s minecraft:campfire 2
/give @s minecraft:smithing_table 1
/give @s minecraft:leather_chestplate 1
/execute in dreamsdimensions:dreamscape run tp @s 0 180 0
```

O comando `minecraft:potion[potion_contents=...]` deve ser validado pela versão do jogo; se a sintaxe local divergir, use a garrafa de água obtida naturalmente. Coordenadas 0,180,0 são somente de inspeção em Spectator, NÃO são spawns seguros atestados para Survival.

### QA automatizado

```powershell
git switch migration/minecraft-26.3
git pull origin migration/minecraft-26.3
python scripts/ci/test_visual_assets.py
python scripts/ci/check_dream_ruins.py
python scripts/ci/check_overworld_resource_chain.py
.\gradlew.bat --no-daemon clean build
.\gradlew.bat runClient
```

### QA manual obrigatório (NOT RUN enquanto não registrado)

1. Em quatro seeds, visitar montanhas >Y120 e medir reais veios de Plumita; testar Fortune III, Silk Touch e ausência em biomas não elegíveis.
2. No Dreamscape, tentar reduzir dano de queda com Infusão/Forro e tocar Plumita Block em alturas controladas (comparar dano com/sem proteções); garantir que o Campo Onírico Azul não recebe o mesmo traço.
3. Validar Chaleira sobre fogueira, 1 e 3 garrafas, ingredientes inválidos, fogueira apagada, reload enquanto mistura, quebra durante preparo e após concluído (sem duplicação).
4. Aplicar Forro a armadura encantada e danificada, repetir na mesma peça, usar/cancelar, morrer e relogar. Confirmar limite de duas peças e preservação de NBT.
5. Armar Amarra em terra firme, pular em travessias reais, provocar resgate, testar expiração/relog/vida/multiplayer. Não usar TP como substituto do teste de Queda.
6. Criar Ponte em queda sobre vazio; verificar 3×3, expiração 30 s e que não sobrescreve construções existentes.
7. Completar encontros do Fragmento e pacto com Vigia e obter Reminiscência; verificar baús em ruínas e inexistência no Overworld.
8. Validar 16×16 no mundo, hotbar e GUI com UI scale diferentes, contrastes sem pastéis lavados e integração com stone/copper do vanilla.

**Status desta documentação:** implementação em branch; CI/JUnit/datapack e ensaios manuais registrados separadamente. PASS de pipeline não equivale a equilíbrio artístico nem a segurança de Survival.

## Dependências futuras

Fatia 4 irá consumir Reminiscência e infra de base; Fatia 5 introduz Hostilidade/Escuridão; Fatia 6 adiciona Torpor, momento em que a Infusão de Vigília receberá seu efeito reativo específico. Não declarar essas fatias como já implementadas.
