# Task 9.5.10 — Dreamcatcher: correção do bloco no mundo (26.3)

**Branch:** `migration/minecraft-26.3` · **Escopo:** assets e QA; nenhuma alteração em gameplay, receita, item ID ou registro.

## Sintoma

O item `dreamsdimensions:dreamcatcher` funciona na mão/hotbar, mas, colocado no mundo, aparece como cubo preto/magenta de modelo/textura ausente.

## Causa identificada

O JSON `models/block/dreamcatcher.json` referenciava `dreamsdimensions:item/dreamcatcher` em `textures.cross` e `textures.particle`. A partir do Minecraft 1.21.11, itens e blocos têm **atlas separados**; texturas de modelos de bloco devem ser sprites do atlas de blocos. Ver nota oficial do Minecraft 1.21.11: https://www.minecraft.net/en-us/article/minecraft-java-edition-1-21-11

O modelo continha também `render_type: "cutout"` legado. O Minecraft 26.1+ escolhe automaticamente o pass de renderização pelo alfa do PNG; remoção desta chave evita dependência desnecessária de versões antigas. Ver 26.1: https://feedback.minecraft.net/hc/en-us/articles/44551668333837-Minecraft-Java-Edition-26-1

## Correção

- Novo PNG em `textures/block/dreamcatcher.png`, cópia fiel ao PNG do item nesta etapa (16×16 RGBA).
- Em `models/block/dreamcatcher.json`, `cross` e `particle` passam a `dreamsdimensions:block/dreamcatcher`.
- Removido `render_type` legado; o PNG recortado determina o render pass.
- Geometria voxel existente (13 elementos) permanece, sem alterar o modelo do item, blockstate ou registro.
- Testes agora checam atlas correto, dimensões/CRC do novo PNG, contagem de texturas e referências do modelo.

## Teste rápido (novo JAR obrigatório)

```powershell
git checkout migration/minecraft-26.3
git pull origin migration/minecraft-26.3
.\gradlew.bat build
```

Substituir o JAR em `mods/`, fechar/abrir Minecraft 26.3 e testar:

```mcfunction
/gamemode creative
/give @s dreamsdimensions:dreamcatcher 2
/time set day
```

Colocar um bloco no mundo, olhar frente/trás/lado e confirmar aro, rede e penas sem xadrez magenta; testar outro em recinto com teto. Conferir que ícone da hotbar continua intacto.

**Limite de evidência:** CI verifica arquivo, modelos e inicialização do servidor; não substitui inspeção do renderizador gráfico no cliente.
