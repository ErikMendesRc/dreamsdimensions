package com.dreamsdimensions.mod.event;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.config.DreamsConfig;
import com.dreamsdimensions.mod.content.emissive.NightEmissiveDebug;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
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
        DreamsConfig.logResolvedDreamDimensions(event.getServer());
    }

    public static void onLevelTickPost(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        NightEmissiveDebug.logHeartbeat(serverLevel, "serverLevelTick");
    }
}
