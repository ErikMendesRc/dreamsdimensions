package com.dreamsdimensions.mod.event;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.config.DreamsConfig;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

public final class CommonEvents {

    private static final Logger LOGGER = DreamsDimensions.LOGGER;

    private CommonEvents() {}

    public static void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Servidor iniciando - Olá do Dreams Dimensions!");
        DreamsConfig.logResolvedDreamDimensions(event.getServer());
    }
}
