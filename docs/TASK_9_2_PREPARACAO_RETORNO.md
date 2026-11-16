# Task 9.2 — Sistema de Preparação e Retorno (Fatia 1)

**Projeto:** DreamsDimensions · Minecraft 26.3 · NeoForge 26.3 · Java 25.
**Branch:** migration/minecraft-26.3. Sem merge na main.
**Design de referência:** Documento DreamsDimensions Game Design, versão 2, 13/11/2026.

## Mecânica implementada

- O Núcleo Onírico mantém seu ID e arte aprovada, mas agora abre um menu com 3 entradas e 1 saída. O menu não usa block entity: cada jogador tem sua própria instância transitória.
- A receita antiga do Despertador na bancada vanilla foi removida para impedir o consumo do Núcleo.
- O novo Despertador é montado no Núcleo com Carga de Retorno + pena + frasco de vidro. Sem mudança na duração (60 ticks), cooldown ou segurança do retorno.
- Nova planta Erva da Vigília: selvagem nos campos, abre ao sol, fecha à noite, dá folhas quando colhida de dia, somente sementes à noite; volta ao estágio inicial após a colheita.
- Sementes plantam a fase jovem; a planta cresce no período diurno por random ticks, sem aceitar farinha de osso.
- Folhas viram Erva Torrada na fogueira ou defumador. Carga de Retorno = 3 Pós Oníricos + 2 Ervas Torradas na bancada vanilla.
- Um Núcleo custa 12 Pós inicialmente. O primeiro Despertador acresce 3 Pós, somando 18 Pós com o Colar. A repetição custa somente 3 Pós, erva e consumíveis vanilla.

## Direção de arte

- Pixel art 16×16 desenhada diretamente para Erva (aberta/fechada), semente, folha, erva torrada e Carga.
- Interface do Núcleo em 176×166: base creme mineral, guarnições grafite e cobre, saída azul fria. Fiel à legibilidade vanilla+ e ao Overworld.
- Núcleo original e Pedra Infundida revisada preservados, sem recoloração desnecessária.
- Os models de folhagem usam render_type cutout; itens transparentes usam model generated.

## Contratos e segurança

- Inventário de trabalho fica no menu por jogador, não no bloco; ingredientes são devolvidos ou derrubados ao fechar.
- Craft é calculado e pago por unidade no servidor. Shift-click da saída está bloqueado até a homologação de transações em lote.
- Pó continua exclusivo do Overworld; não há loot/receita que produza Pó.
- A entrada por sono não foi habilitada: dream_entry_enabled=false permanece padrão.
- Nenhuma alteração nos serviços de teleport, respawn ou no item Despertador existente.

## Comandos de teste no Minecraft (mundo descartável)

```mcfunction
/give @s dreamsdimensions:oneiric_core_block 1
/give @s dreamsdimensions:vigil_herb 4
/give @s dreamsdimensions:vigil_herb_seeds 16
/give @s dreamsdimensions:vigil_herb_leaf 8
/give @s dreamsdimensions:toasted_vigil_herb 8
/give @s dreamsdimensions:return_charge 8
/give @s minecraft:feather 8
/give @s minecraft:glass_bottle 8
/time set day
```

Coloque o Núcleo, clique e preencha os três slots da esquerda para a direita. Retire o resultado; deve consumir exatamente 1 Carga, 1 pena e 1 frasco. Repita com 2 clientes abrindo o mesmo bloco. Quebre/relogue/feche com inputs para assegurar devolução sem duplicação. Teste colheita diurna/noturna, replantio e crescimento em novo mundo.

## Gates ainda a executar

- Compilação Gradle Java 25, carregamento dos datapacks e render da interface no cliente.
- Quatro seeds e amostragem de distribuição da Erva em chunks novos.
- Survival sem cheats: colher, processar, montar 2 Despertadores com 1 Núcleo, medir tempo e custo.
- Dois clientes em servidor dedicado: menu, devolução no fechamento, uso, relog, segurança de consumo.
- Save antigo com Núcleo colocado e validação de ausência de erro ao abrir.

Rodar no PowerShell:

```powershell
git switch migration/minecraft-26.3
git pull origin migration/minecraft-26.3
python scripts/ci/test_visual_assets.py
python scripts/ci/audit_visual_assets.py --report qa-visual-assets.json
python scripts/ci/test_content_contract.py
python scripts/ci/check_content_contract.py --report qa-content-contract.json
.\gradlew.bat --no-daemon clean build
.\gradlew.bat runClient
```

**Status:** implementação entregue em branch; os resultados de CI e os testes manuais devem ser registrados separadamente. CI verde não substitui Survival.

## Referências técnicas

- [NeoForge Recipes](https://docs.neoforged.net/docs/resources/server/recipes/)
- [NeoForge Screens](https://docs.neoforged.net/docs/rendering/screens/)
- [NeoForge Biome Modifiers](https://docs.neoforged.net/docs/worldgen/biomemodifier/)
