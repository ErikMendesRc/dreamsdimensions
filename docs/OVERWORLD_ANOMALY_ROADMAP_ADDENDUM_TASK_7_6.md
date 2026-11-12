# Adendo de roadmap — Anomalias Oníricas antes do conteúdo horizontal

**Decisão de design:** 12/11/2026. O roadmap executivo da 6.9 mantinha a sequência 7.6 = F1 Líquen, 7.7 = cultivo, 7.8 = Argamassa, 7.9 = Marco de Pedra Marcada. A partir da conversa de design sobre a relação entre pedra e sonhos, o usuário decidiu que **o Overworld deve ser um lugar de anomalias reais que estimulem investigação, não apenas outra seleção de blocos vanilla-like**.

Este é um **adendo explícito** para preservar o histórico 6.9 e impedir que duas tasks diferentes sejam executadas com o mesmo ID. Não é retroativamente um fato da implementação 6.9.

| Nova ordem | Contrato de gameplay | Integração/saída |
| --- | --- | --- |
| **7.6** | Contrato do Sistema de Anomalias Oníricas | Domínio puro, provas independentes, payoff físico, catálogo causal, nenhuma feature ativada |
| **7.7** | Primeiro fenômeno completo: Cicatriz Silenciosa / Marcas do Limiar | Prova real de origem, 2 clues, alternativa sono-vigília, alvo gerado e recompensa, 4 seeds, Survival |
| **7.8** | Ecos de Memória e segunda camada de descoberta | Progresso persistido conectado às duas dimensões, sem teleportes gratuitos, replay/multiplayer seguros |
| **7.9** | Líquen dos Vestígios (F1a), com ecologia fundamentada no sistema de Anomalias | Habitat exclusivo e validado, coleta, 4 seeds, sem scanner tick |
| **7.10+** | Restante de F1, Argamassa, Marco/Abrigo e demais POIs/mobs | **IDs antigos da 6.9 NÃO se transportam automaticamente**. Renumerar em um novo plano executivo, preservando dependências reais |

**Dependências preservadas (não negociáveis):**
- Ciclo F0 7.3–7.5 permanece intacto, inclusive balanceamento, stonecutter e loot.
- Retorno onírico/recuperação **7.0 permanece P0** para habilitar entrada nos sonhos em produção, independentemente do novo roadmap.
- QA 7.1 e contrato de assets 7.2 continuam exigidos.
- Dreamscape e Campo Azul não ganham novas portas, teleporte, boss, item obrigatório nem alteração de engine por causa de 7.6.
- As antigas F1/Argamassa/POIs não foram canceladas; foram **adiadas** até existir identidade e função demonstradas.
- Não autorizar 30 blocos, 12 POIs ou 6 mobs por tabela; requer consumidor de gameplay, teste e recompensa.
- 7.7 deve implementar **uma** anomalia completa, não dez sinais genéricos.

**Definição de pronto para o novo train:** ao jogar Minecraft normalmente, um jogador encontra um vestígio no Overworld, reconhece uma contradição geológica, cruza duas pistas, opta por uma das duas interpretações, e encontra um local **preexistente no mundo** que entrega recurso/verdade sobre o sonho. A solução deve ser explicável e memorável, não RNG ou só partículas.

**Estado honesto:** a 7.6 constrói e testa o contrato, mas não ativa conteúdo runtime. A 7.7 será o primeiro teste real de curiosidade, acessibilidade e utilidade da anomalia.

[Contrato detalhado da 7.6](DREAMSDIMENSIONS_TASK_7_6.md)
