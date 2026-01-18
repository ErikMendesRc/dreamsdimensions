package com.dreamsdimensions.mod.config;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.slf4j.Logger;

/**
 * Configuração do mod baseada em {@link ModConfigSpec}.
 * <p>
 * A classe é inscrita no MOD bus via {@link EventBusSubscriber}, conforme eventos
 * {@link ModConfigEvent.Loading} e {@link ModConfigEvent.Reloading} descritos no SDK.
 * </p>
 */
@EventBusSubscriber(modid = DreamsDimensions.MODID, bus = EventBusSubscriber.Bus.MOD)
public final class DreamsConfig {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /**
     * Especificação final registrada no {@link net.neoforged.fml.ModContainer#registerConfig}.
     */
    public static final ModConfigSpec SPEC = BUILDER.build();

    private DreamsConfig() {
    }

    /**
     * Disparado quando a configuração é carregada.
     */
    @SubscribeEvent
    static void onLoad(final ModConfigEvent.Loading event) {
        LOGGER.info("Carregando configuração de Dreams Dimensions: {}", event.getConfig().getFileName());
    }

    /**
     * Disparado quando a configuração é recarregada (ex: /reload).
     */
    @SubscribeEvent
    static void onReload(final ModConfigEvent.Reloading event) {
        LOGGER.info("Recarregando configuração de Dreams Dimensions: {}", event.getConfig().getFileName());
    }
}
