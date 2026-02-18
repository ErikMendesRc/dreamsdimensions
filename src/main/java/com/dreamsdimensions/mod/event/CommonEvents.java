package com.dreamsdimensions.mod.event;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.config.DreamsConfig;
import com.dreamsdimensions.mod.content.emissive.NightEmissiveBlockBase;
import com.dreamsdimensions.mod.content.emissive.NightTime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;

public final class CommonEvents {

    private static final Logger LOGGER = DreamsDimensions.LOGGER;

    private CommonEvents() {}

    public static void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Servidor iniciando - Olá do Dreams Dimensions!");
        DreamsConfig.logResolvedDreamDimensions(event.getServer());
    }

    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean debug = DreamsConfig.isNightEmissiveDebugLogsEnabled();

        if (debug) {
            LOGGER.info(
                    "\n==================== [NightEmissive DEBUG] ====================\n" +
                            "ChunkEvent.Load DISPARADO\n" +
                            "Chunk: {}\n" +
                            "Dim: {}\n" +
                            "É Overworld: {}\n" +
                            "Thread: {}\n" +
                            "===============================================================",
                    event.getChunk().getPos(),
                    serverLevel.dimension().identifier(),
                    serverLevel.dimension() == Level.OVERWORLD,
                    Thread.currentThread().getName()
            );
        }

        serverLevel.getServer().execute(() -> {

            var chunk = event.getChunk();

            int scanned = 0;
            int emissiveFound = 0;
            int scheduled = 0;
            int alreadyScheduled = 0;

            for (int y = serverLevel.getMinY(); y < serverLevel.getMaxY(); y++) {
                for (int z = 0; z < 16; z++) {
                    for (int x = 0; x < 16; x++) {

                        scanned++;

                        BlockPos pos = new BlockPos(
                                chunk.getPos().getMinBlockX() + x,
                                y,
                                chunk.getPos().getMinBlockZ() + z
                        );

                        BlockState state = chunk.getBlockState(pos);
                        Block block = state.getBlock();

                        if (!(block instanceof NightEmissiveBlockBase emissiveBlock)) {
                            continue;
                        }

                        emissiveFound++;

                        boolean hasLit = state.hasProperty(BlockStateProperties.LIT);
                        boolean currentLit = hasLit && state.getValue(BlockStateProperties.LIT);

                        if (debug) {
                            LOGGER.info(
                                    "[NightEmissive] Encontrado bloco emissivo pos={} block={} hasLIT={} currentLIT={}",
                                    pos,
                                    block.builtInRegistryHolder().key().identifier(),
                                    hasLit,
                                    currentLit
                            );
                        }

                        if (serverLevel.getBlockTicks().hasScheduledTick(pos, block)) {
                            alreadyScheduled++;
                            continue;
                        }

                        emissiveBlock.scheduleInitial(serverLevel, pos, state, block);
                        scheduled++;
                    }
                }
            }

            if (debug) {
                long dayTime = serverLevel.getDayTime();
                long modulo = Math.floorMod(dayTime, NightTime.DAY_TICKS);

                LOGGER.info(
                        "\n---------------- [NightEmissive Chunk Scan Result] ----------------\n" +
                                "Chunk: {}\n" +
                                "Dim: {}\n" +
                                "DayTime: {}\n" +
                                "DayTime Mod 24000: {}\n" +
                                "É noite (Vanilla): {}\n" +
                                "Blocos escaneados: {}\n" +
                                "Emissivos encontrados: {}\n" +
                                "Ticks agendados agora: {}\n" +
                                "Já tinham tick agendado: {}\n" +
                                "-------------------------------------------------------------------",
                        chunk.getPos(),
                        serverLevel.dimension().identifier(),
                        dayTime,
                        modulo,
                        NightTime.computeNight(modulo),
                        scanned,
                        emissiveFound,
                        scheduled,
                        alreadyScheduled
                );
            }
        });
    }
}
