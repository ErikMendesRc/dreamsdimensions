package com.dreamsdimensions.mod; // Seu pacote base

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

// Usa EventBusSubscriber para escutar eventos de configuração no barramento de eventos do MOD.
@EventBusSubscriber(modid = DreamsDimensions.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DreamsConfig {

    private static final Logger LOGGER = LogUtils.getLogger();

    // --- Especificação de Configuração (Config Spec) ---
    // Um 'Builder' é usado para construir a especificação da configuração.
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    // --- Definição das Opções de Configuração ---
    // --- ADICIONE SUAS OPÇÕES DE CONFIGURAÇÃO AQUI NO FUTURO ---
    // Exemplo (mantido comentado para referência):
    /*
    private static final ModConfigSpec.DoubleValue TELEPORT_CHANCE = BUILDER
            .comment("Chance (de 0.0 a 1.0) de ser teleportado para uma dimensão ao acordar.") // Comentário que aparecerá no arquivo .toml
            .defineInRange("teleportChance", // Nome da chave no arquivo .toml
                           1.0,              // Valor padrão
                           0.0,              // Valor mínimo permitido
                           1.0);             // Valor máximo permitido

    private static final ModConfigSpec.BooleanValue NIGHTMARE_DIM_ENABLED = BUILDER
            .comment("Define se a Dimensão do Pesadelo pode ser acessada.")
            .define("enableNightmareDimension", true); // Valor padrão
    */

    // Constrói a especificação final a partir do builder.
    // Esta especificação será registrada na classe principal do mod.
    public static final ModConfigSpec SPEC = BUILDER.build();

    // --- Variáveis para Armazenar Valores Carregados ---
    // Estas variáveis guardarão os valores lidos do arquivo de configuração.
    // Crie variáveis correspondentes para cada opção definida acima.
    // Exemplo:
    // public static double teleportChance;
    // public static boolean nightmareDimEnabled;


    /**
     * Método chamado quando um arquivo de configuração do seu mod é carregado ou recarregado.
     * Anotado com @SubscribeEvent para ser descoberto automaticamente pelo EventBusSubscriber.
     * É aqui que você lê os valores da especificação (SPEC) e os armazena nas suas variáveis estáticas.
     */
    @SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading event) {
        LOGGER.info("Carregando configuração de Dreams Dimensions: {}", event.getConfig().getFileName());

        // Leia os valores das suas opções de configuração aqui:
        // Exemplo:
        // teleportChance = TELEPORT_CHANCE.get();
        // nightmareDimEnabled = NIGHTMARE_DIM_ENABLED.get();

        // LOGGER.info("Configuração carregada: Chance de Teleporte = {}, Dimensão Pesadelo Ativa = {}", teleportChance, nightmareDimEnabled); // Exemplo de log
    }

    /**
     * Método chamado quando um arquivo de configuração do seu mod é recarregado (ex: comando /reload).
     * Anotado com @SubscribeEvent para ser descoberto automaticamente pelo EventBusSubscriber.
     * Geralmente, você fará o mesmo que no onLoad aqui.
     */
    @SubscribeEvent
    static void onReload(final ModConfigEvent.Reloading event) {
        LOGGER.info("Recarregando configuração de Dreams Dimensions: {}", event.getConfig().getFileName());
        // Releia os valores aqui também para garantir que mudanças no arquivo sejam aplicadas sem reiniciar.
        // Exemplo:
        // teleportChance = TELEPORT_CHANCE.get();
        // nightmareDimEnabled = NIGHTMARE_DIM_ENABLED.get();
    }
}