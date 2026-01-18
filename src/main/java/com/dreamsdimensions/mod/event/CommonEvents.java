package com.dreamsdimensions.mod.event;

import com.dreamsdimensions.mod.DreamsDimensions;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

/**
 * Eventos comuns (runtime) do mod.
 * <p>
 * Registrado no {@link net.neoforged.neoforge.common.NeoForge#EVENT_BUS}, conforme os javadocs
 * de {@link net.neoforged.neoforge.event.server.ServerStartingEvent} para eventos de jogo.
 * </p>
 */
public final class CommonEvents {
    private static final Logger LOGGER = DreamsDimensions.LOGGER;

    private CommonEvents() {
    }

    /**
     * Executado quando o servidor está iniciando.
     */
    public static void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Servidor iniciando - Olá do Dreams Dimensions!");
    }
}
