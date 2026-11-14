# Task 9.5.7 — Rework artístico profissional (Vanilla+)

**Minecraft 26.3 / NeoForge 26.3 / Java 25.** Branch `migration/minecraft-26.3`; nenhum merge na `main`.

## Conteúdo
Revisão somente visual. IDs, receitas, registro de itens e blocos, comportamento e gameplay permanecem intactos.

- 9 sprites de item refeitos: `serene_vial`, `dream_thread`, `dormant_ember`, `dreamy_herb`, `dreamcatcher`, `resonance_probe`, `marked_lining`, `dream_journal`, `infusion_route`.
- 1 sprite de efeito: `textures/mob_effect/route.png` (18×18).
- 10 texturas de bloco: `deep_sleep_stone`, `bedside_side`, `bedside_top`, `bedside_drawer`, `bedside_niche`, `bedside_metal`, `trail_marker_side`, `trail_marker_top`, `trail_marker_brass`, `trail_marker_gem`. Dessas, cinco dão suporte novo à geometria.
- 3 modelos 3D estáticos revistos: `dream_bedside` (pernas, tampo, gaveta, puxador e nicho), `dreamcatcher` (silhueta recortada + elementos volumétricos) e `trail_marker` (base, coluna, anéis, gemas e seta direcional).

Resolução: 19 sprites 16×16 RGBA e um HUD 18×18. Desenho pixel-a-pixel com cores controladas, planos de luz superior/esquerda, diferenciação clara de pedra, metal, madeira, couro, vidro, fibras e carvão. Não foi usado blur nem downsampling. Objetos estáticos seguem modelos JSON nativos; não requerem GeckoLib.

## QA e homologação

Execute `python scripts/validate_task_9_5_7_art.py` para verificar assinatura e CRC de cada PNG, RGBA, dimensões, geometria dos modelos e referências a texturas. Essa verificação **não** comprova renderização no Minecraft.

**Pendentes:** Gradle build, carregamento de recursos em cliente, inspeção visual nativa/ampliada na hotbar/inventário, vista frontal/lateral/superior/perspectiva dos blocos, cutout/transparência do Apanhador, contraste sob luz variable e Survival. Teste de reconhecimento sem nomes exige revisão humana. O Apanhador usa orientação estática (bloco sem facing); testar vista lateral.

### /give para teste rápido
```mcfunction
/gamemode creative
/give @s dreamsdimensions:dream_bedside 1
/give @s dreamsdimensions:dreamcatcher 1
/give @s dreamsdimensions:trail_marker 1
/give @s dreamsdimensions:deep_sleep_stone 16
/give @s dreamsdimensions:serene_vial 1
/give @s dreamsdimensions:dream_thread 1
/give @s dreamsdimensions:dormant_ember 1
/give @s dreamsdimensions:dreamy_herb 1
/give @s dreamsdimensions:resonance_probe 1
/give @s dreamsdimensions:marked_lining 1
/give @s dreamsdimensions:dream_journal 1
/give @s dreamsdimensions:infusion_route 1
/time set day
```

### Inspeção em jogo
1. Comparar cada item sem ler tooltip em hotbar/inventário, fundos claro e escuro.
2. Cabeceira ao lado da cama: gaveta real, nicho e tampo, laterais e costas.
3. Apanhador: moldura e penas, recorte sem artefatos e perfil de ângulo agudo.
4. Marco: topo, pilar, seta, distância e partículas já existentes.
5. Deep Sleep Stone: paredes 3×3 para detectar padrões repetitivos.
6. Infusão de Rumo: frasco no inventário e rosa dos ventos no HUD.
7. Reiniciar cliente, repetir em Survival e servidor dedicado; **não afirmar aprovação ainda**.
