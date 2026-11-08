# PERFIL TEMPORÁRIO DE TESTES — Formações raras (TASK 1.3)

Esta alteração é **intencionalmente temporária** para inspecionar Arcos do Sonho
e Círculos de Vigília no Minecraft **26.3**. Não é a distribuição final.

| Formação | Chance de tentativa normal por chunk | Chance durante o teste | Densidade normal | Densidade de teste |
| --- | ---: | ---: | ---: | ---: |
| Arcos do Sonho | 5,5% | **90%** | 0,78 | **1,00** |
| Círculos de Vigília | 6,5% | **90%** | 0,72 | **1,00** |

O `PlacedFeature` ainda depende de: seed, ruído regional, tipo de solo,
topografia, disponibilidade de ar e proteção de limites de chunks.
`90%` é a **probabilidade de tentativa por chunk**, não uma estrutura garantida.
Nenhuma regra de proteção, codec, material ou geometria Java foi afrouxada.

## Testar imediatamente

1. `git switch migration/minecraft-26.3`
2. `git pull origin migration/minecraft-26.3`
3. `./gradlew runClient` (Windows: `.\\gradlew.bat runClient`)
4. **Crie um mundo NOVO para avaliação** (ex.: seed `14011`).
   Chunks antigos NÃO recebem novas estruturas após ajuste de worldgen.
5. Use `/gamemode spectator` e
   `/execute in dreamsdimensions:campo_onirico_azul run tp @s 0 160 0`.
6. Procure a partir de 100 a 400 blocos da origem, explorando chunks
   novos: arcos com 2 pilares unidos no topo e círculos quebrados de pedra.
7. Com `F3 + G`, inspecione bases dos arcos, obstruções, coerência de
   relevo e fronteiras de chunks. Observe sobreposições com árvores.
8. Depois da inspeção, exclua o mundo **de testes** se não quiser manter
   uma distribuição de estruturas temporária no save.

## Reversão depois do teste

Este perfil altera somente quatro JSONs e a asserção em
`scripts/ci/check_formations.py`. Para voltar à taxa rara original,
reverta o commit dedicado de teste com `git revert <SHA_DO_COMMIT>`
na branch de desenvolvimento e faça push.
**Não reverta o commit da Task 1.3.**

Configuração final prevista:
- Arcos: `random_chance=0.055`, `density=0.78`;
- Círculos: `random_chance=0.065`, `density=0.72`.

As Tasks 1.1 e 1.2 continuam intactas. Sem merge na main.
