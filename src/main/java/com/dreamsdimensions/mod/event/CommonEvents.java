package com.dreamsdimensions.mod.event;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.config.DreamsConfig;
import com.dreamsdimensions.mod.content.emissive.NightEmissiveBlockBase;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.ChunkEvent;
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
        DreamsConfig.logResolvedDreamDimensions(event.getServer());
    }

    /**
     * Garante agendamento inicial para blocos emissivos noturnos em chunks carregados,
     * incluindo blocos de worldgen previamente existentes.
     */
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean debugLogs = DreamsConfig.isNightEmissiveDebugLogsEnabled();
        String threadName = Thread.currentThread().getName();

        if (debugLogs) {
            LOGGER.info(
                    "[NightEmissive] ChunkEvent.Load recebido chunk={} dim={} thread={}",
                    event.getChunk().getPos(),
                    serverLevel.dimension().identifier(),
                    threadName
            );
        }

        serverLevel.getServer().execute(() -> {
            var chunk = event.getChunk();
            int scheduled = 0;

            for (int y = serverLevel.getMinY(); y < serverLevel.getMaxY(); y++) {                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {
                        BlockPos pos = new BlockPos(chunk.getPos().getMinBlockX() + x, y, chunk.getPos().getMinBlockZ() + z);
                        BlockState state = chunk.getBlockState(pos);
                        Block block = state.getBlock();

                        if (!(block instanceof NightEmissiveBlockBase emissiveBlock)) {
                            continue;
                        }

                        if (serverLevel.getBlockTicks().hasScheduledTick(pos, block)) {
                            continue;
                        }

                        emissiveBlock.scheduleInitial(serverLevel, pos, state, block);
                        scheduled++;
                    }
                }
            }
        });
    }
}

