# Changelog e Limitações Conhecidas

## Sumário
- [Estado atual](#estado-atual)
- [NÃO IMPLEMENTADO / NÃO EXISTE NO PROJETO](#não-implementado--não-existe-no-projeto)
- [Decisões arquiteturais atuais](#decisões-arquiteturais-atuais)

## Estado atual
- Economia OW consolidada em cadeia de refino até `ow_oneiric_awakener`.
- Entrada em sonho por sono + retorno seguro por helper dedicado.
- Integração TerraBlender para bioma Overworld (`lumina_hollows`).

## NÃO IMPLEMENTADO / NÃO EXISTE NO PROJETO
Com base na árvore `src/main/resources/data/dreamsdimensions` e código Java:
- Estruturas custom (`worldgen/structure`) — **não existe**.
- Loot tables de entidades/chests custom — **não existe** (apenas bloco).
- Smithing recipes — **não existe**.
- Stonecutting recipes — **não existe**.
- Campfire/smoker recipes — **não existe**.
- Comandos custom (`/dream...`) — **não existe**.
- Sistema próprio de advancements do mod — **não existe**.
- Pipeline JSON de brewing — **não existe** (brewing em código).
- `neoforge:add_biomes` do mod para Overworld — **não existe** (usa TerraBlender Region).
- Fontes externas solicitadas (`/mnt/data/*.md`, auditoria txt) — **não existem no workspace atual**.

## Decisões arquiteturais atuais
- Persistência por attachment (`dream_return`) com `copyOnDeath`.
- Regras de retorno centralizadas em `DreamReturnHelper`.
- Anti-teleporte por totem via varredura cúbica no raio configurável.
- Perfis de worldgen via property de JVM: `dreamsdimensions.worldgen.profile`.
