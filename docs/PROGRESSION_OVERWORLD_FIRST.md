# Progressão Survival — Overworld como base (análise e decisões pendentes)

> **ATUALIZAÇÃO TASK 5.2 — 10/11/2026:** A recomendação antiga de Despertador **reutilizável** e de reduzir seu custo era hipótese anterior e foi **superada pela regra de produto confirmada**: o Despertador é **consumível** e relativamente difícil de fabricar, consumido somente após retorno confirmado (Task 5.4). Dream Dust nasce no Overworld, e Pó de Cristal futuro terá outra função. Entrada depende de **Colar equipado** (Task 5.3), com chance por sono completo. Morrer não deve ser rota gratuita de volta (5.5), e Despertadores serão recuperáveis em ruínas raras de ambas as dimensões (5.7). **Não liberar o Colar em Survival antes de implementar e validar 5.5 + 5.7.** Ver [contrato 5.2](TRAVEL_RETURN_CONTRACT_TASK_5_2.md) e [matriz](TRAVEL_RETURN_TEST_MATRIX_TASK_5_2.md).


**Branch:** migration/minecraft-26.3  
**Base auditada:** 1af737760a47972282eccf33689a0fa593d9584c  
**Status:** contrato de produto proposto e auditoria de recursos; não altera recipes, retorno, mobs ou worldgen geográfico. A correção do biome modifier está no mesmo commit.

## 1. Princípio aprovado

O jogador começa no Overworld, se prepara com ferramentas e mineração vanilla, encontra materiais do mod e cria instrumentos para expedições. O sono continua sendo a porta de entrada para duas dimensões, sem menu ou missão obrigatória. O jogador retorna, usa o que aprendeu e fabrica algo que torna a próxima expedição diferente.

Ciclo: Overworld -> preparar -> dormir -> explorar -> extrair -> retornar -> fabricar -> explorar áreas mais difíceis.

Cada item e mecânica novos devem ter uma cadeia explícita: **origem natural -> dificuldade de obtenção -> ação -> resultado -> uso em Survival -> razão para regressar**. Conteúdo somente decorativo continua válido se sua origem e uso construtivo forem claros.

## 2. Estado do código verificado

- O sono no Overworld completa 100 ticks e pode escolher Dreamscape ou Campo Onírico Azul, ambos com peso 1 em DreamDimensionCatalog.
- OneiricAwakenerItem só inicia uso nas dimensões configuradas, conclui em 60 ticks e retorna por DreamReturnHelper para cama/respawn/spawn seguros. O item não é consumido.
- O retorno não é garantido por uma segunda ação de sono dentro dos sonhos. O teste de jogador sem Despertador é P0 e antecede mudanças em receitas.
- Os dois minérios do Overworld usam configured/placed features, drop de dream_dust com Fortune e Silk Touch, além de XP. Sua configuração antes deste commit estava fora da pasta indicada na documentação NeoForge; este commit passa para neoforge/biome_modifier e usa #c:is_overworld.
- DreamDust é ingrediente de Dream Infused Stone, Oneiric Core e Awakener. A receita existente consome **12 dream_dust** por Despertador: quatro Pedras Infundidas com dois pós cada (8) e quatro pós adicionais no núcleo (4). Há ainda oito pedras, um relógio, uma pena e um frasco.
- O Campo Azul tem rochas, vegetação, arcos e formações; Dreamscape tem fendas, ilhas, pontes, monólitos, Jardins de Eco e cristais decorativos de superfície. Não existe feature própria para Câmara Cristalina subterrânea.
- A Task 5.1 já implementou uma pista ambiental curta de musgo para dream_shimmer_block. Isso não transforma o Shimmer em minério funcional e não deve virar um enigma por padrão.
- O Fragmento fornece Slow Falling temporário; o Vigia concede uma proteção contra uma queda nas ilhas. São vantagens de exploração, não chaves de acesso.
- Existe um bioma fixed por dimensão no datapack; regiões visuais não equivalem a biomas efetivamente separados.
- A CI executa build, verificações estáticas e smoke do servidor em quatro seeds; NÃO prova sozinha extração real de minérios ou experiência do jogador em Survival.

## 3. Proposta de progressão por capacidade (sem gates arbitrários)

| Estágio | Obtenção | Benefício imediato | Próximo desejo |
| --- | --- | --- | --- |
| Preparação no Overworld | Ferro, pedra, redstone, ouro e Dream Dust | Primeira ferramenta de retorno fabricável; base segura | Ver quais sonhos existem |
| Primeiras viagens | Materiais visuais e encontros com Fragmento/Vigia | Construção temática, conhecimento do terreno e travessias seguras | Encontrar locais raros |
| Dreamscape subterrâneo | Câmaras cristalinas pouco frequentes e difíceis de alcançar | Fontes alternativas de Dream Dust e materiais cristalinos; construção de Shimmer | Refinar equipamentos de expedição |
| Campo Azul especializado | Recursos em cordilheiras/terraços, com origem natural validada | Componente para instrumentos de navegação e preparação de viagens | Fazer expedições mais longas |
| Síntese entre dimensões | Recursos diferenciados das duas | Possível Afinador Onírico para influenciar destino do próximo sono | Buscar regiões e câmaras avançadas |

Uma Câmara Cristalina não é um desafio de sequência: dificuldade por localização, entrada, geologia, riscos, preparação e rendimento. Evitar item raro que torne a primeira viagem ou o retorno básico impossível.

## 4. Decisões de crafting a homologar antes de alterar receitas

**Problema:** 12 pós + relógio (ouro e redstone) + outros componentes tornam o Despertador uma receita pesada para a primeira expedição. Todavia o custo só pode ser balanceado após medir a distribuição natural de Dream Dust.

**Recomendação de design para teste A/B, NÃO implementada neste commit:**
- Despertador reutilizável deve ser acessível com recursos do Overworld ainda no estágio inicial, idealmente com 4–6 dream_dust em vez de 12, preservando seu ID e sua função.
- Pedra Infundida e Núcleo Onírico continuam úteis para construção e para equipamentos avançados; reduzir custo do retorno não significa remover o valor de seus ingredientes.
- Recursos cristalinos no Dreamscape poderão oferecer uma segunda via de obtenção de Dream Dust e um uso próprio para Shimmer, sem apagar o incentivo inicial de minerar no Overworld.
- Um equipamento avançado de controle de destinos deve exigir componentes de ambas as dimensões, mas nunca ser pré-requisito para sono, retorno ou segurança básica.
- Novo item só entra quando sua receita, fonte em Survival, consumo/renovabilidade e vantagem mecânica estiverem fechados.

## 5. Primeiro conjunto de entregas, em ordem

1. **5.2A — Corrigir e verificar fonte Overworld:** corrigir path do modifier, ligar CI estático, iniciar servidor e conferir geração de minério em chunks NOVOS do Overworld. Este commit faz o path/CI; geração natural e rendimento seguem PENDENTES.
2. **5.2B — Segurança do primeiro sonho:** testar quem dorme sem Despertador, cama/respawn nas duas dimensões, morte e restart; decidir uma forma de retorno sem aprisionamento, preservando o motor e o Despertador. Nenhuma mudança até concluir o contrato.
3. **5.2C — Economia inicial:** medir pó por expedição de mineração e custo real do relógio, depois ajustar receitas somente se necessário; verificar obtenção natural dos outros ingredientes.
4. **5.3 — Câmara Cristalina V0:** testar geração JSON de geode/ore em volumes sólidos apropriados ao Dreamscape; medir exposição, acesso, integridade de ilhas, pontes e saves. Java Feature apenas se o vanilla não servir.
5. **5.4 — Crafting cristalino:** adicionar fonte mineral e drops/recipes de Shimmer com aplicações verificáveis; não confundir Shimmer decorativo de superfície com minério de câmara.
6. **5.5 — Conteúdo útil do Campo Azul:** escolher uma única vantagem de viagem/experiência e projetar sua origem em bioma/região antes de registrar novo bloco.
7. **5.6 — Expedição avançada:** conectar materiais de ambos os mundos a equipamento opcional que melhore navegação/controle de sonho; avançar só após os dois loops anteriores fecharem.
8. **5.7 — Ecologia, biomas e mobs:** diversificar áreas com recursos e perigos distintos, após proteger a compatibilidade de spawn com múltiplos biomas.

## 6. Regras técnicas

- JSON primeiro: biome modifiers, configured/placed features, tags, loot tables, recipes, advancements, atributos e configurações disponíveis por datapack.
- Java somente para comportamento impossível em dados: retorno, habilidades, coordenação, estado persistente, geração geométrica não coberta por feature vanilla.
- Reutilizar os codecs de DreamWorldgen, registros e sistemas existentes; preservar IDs públicos e NBT.
- Cada task com evidência de seed, comandos e coordenadas de /tp APENAS quando descobertos por sondagem; nunca inventar local natural.
- Seeds de regressão: 14011, 20261108, -904732, 73199013.
- Mundo novo para verificar worldgen; chunks antigos não recebem minério retroativamente.
- Não criar progressão que dependa de sorte em uma dimensão aleatória sem via alternativa.
- Não anunciar sucesso de Survival natural somente por startup/build/JSON.
- Não realizar merge na main.

## 7. Testes manuais obrigatórios e comandos de apoio

- Confirmar, num NOVO mundo com seed de teste, que Dream Ore e Deepslate Dream Ore existem naturalmente no Overworld. Não há coordenadas validadas para os minérios nesta baseline. Não usar /give como prova.
- Com o personagem em local seguro e coordenadas já verificadas, usar /execute in minecraft:overworld run tp @s <x> <y> <z> para alternar dimensões. Os placeholders são intencionais, não destinos reais.
- Para o teste de crafting, minerar minério com picareta de ferro, observar dream_dust, fabricar quatro dream_infused_stone, um oneiric_core_block e um oneiric_awakener.
- Testar despertar normal com 60 ticks, morte/respawn, cama perdida e retorno seguro.
- Confirmar/registrar se o minério aparece em seed nova, quantas unidades por tempo amostrado, e quantos recursos vanilla adicionais são necessários.

## 8. Fontes oficiais

- https://docs.neoforged.net/docs/worldgen/biomemodifier/
- https://docs.neoforged.net/primer/docs/26.3/
- https://github.com/neoforged/NeoForge/blob/26.3.x/src/main/java/net/neoforged/neoforge/common/Tags.java


## 9. Addendum de precedência — Task 5.2 (10/11/2026)

O trecho histórico de preparação acima antecede a decisão do produto de exigir Colar equipado e Despertador **consumível**. Sua sugestão de reaproveitar Despertador e alterar receita para 4–6 pós **não deve orientar a Task 5.4/5.6**. O custo atual continua sem mudança nesta Task 5.2. O ciclo alvo é Overworld (minerar Dream Dust) → equipar Colar → completar sono com uma chance de viagem → explorar sonho → usar Despertador e consumir **uma unidade** após retorno confirmado → Overworld. Sem item, explorar ruínas raras por Despertador, sem dormir ou suicidar como saída gratuita. Respawn onírico local precisa existir e ser seguro; Hardcore vanilla é mantido. Ver [Contrato](TRAVEL_RETURN_CONTRACT_TASK_5_2.md) e [Matriz](TRAVEL_RETURN_TEST_MATRIX_TASK_5_2.md).
