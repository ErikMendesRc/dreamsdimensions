package com.dreamsdimensions.mod.event;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.config.DreamsConfig;
import com.dreamsdimensions.mod.content.emissive.NightEmissiveBlockBase;
import com.dreamsdimensions.mod.content.emissive.NightTime;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class CommonEvents {

    private static final Logger LOGGER = DreamsDimensions.LOGGER;

    private static final int DAY_NIGHT_MONITOR_INTERVAL_TICKS = 40;
    private static final Map<ResourceKey<Level>, Boolean> LAST_NIGHT_STATE = new ConcurrentHashMap<>();

    private CommonEvents() {}

    public static void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Servidor iniciando - Olá do Dreams Dimensions!");
        DreamsConfig.logResolvedDreamDimensions(event.getServer());
    }

    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (event.getChunk() instanceof LevelChunk levelChunk) {
            syncNightEmissiveChunk(serverLevel, levelChunk, "chunk_load");
        }
    }

    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (serverLevel.getGameTime() % DAY_NIGHT_MONITOR_INTERVAL_TICKS != 0L) {
            return;
        }

        boolean nowNight = NightTime.isVanillaNight(serverLevel)
                || DreamsConfig.forceNightEmissiveInDreamDimensions(serverLevel.dimension());
        ResourceKey<Level> dimension = serverLevel.dimension();
        Boolean previousNight = LAST_NIGHT_STATE.put(dimension, nowNight);

        if (previousNight == null || previousNight == nowNight) {
            return;
        }

        if (DreamsConfig.isNightEmissiveDebugLogsEnabled()) {
            LOGGER.info(
                    "[NightEmissive] Mudança de período detectada dim={} previousNight={} nowNight={} gameTime={} dayTime={} hasFixedTime={}",
                    dimension.identifier(),
                    previousNight,
                    nowNight,
                    serverLevel.getGameTime(),
                    serverLevel.getDayTime(),
                    serverLevel.dimensionType().hasFixedTime()
            );
        }

        serverLevel.getChunkSource().chunkMap.forEachReadyToSendChunk(chunk ->
                syncNightEmissiveChunk(serverLevel, chunk, "day_night_transition")
        );
    }

    private static void syncNightEmissiveChunk(ServerLevel serverLevel, LevelChunk chunk, String reason) {
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
                    chunk.getPos(),
                    serverLevel.dimension().identifier(),
                    serverLevel.dimension() == Level.OVERWORLD,
                    Thread.currentThread().getName()
            );
        }

        int scanned = 0;
        int emissiveFound = 0;
        int syncedLit = 0;
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

                    if (emissiveBlock.syncNightLighting(serverLevel, pos, state, NightEmissiveBlockBase.CHUNK_SYNC_UPDATE_FLAGS)) {
                        syncedLit++;
                    }

                    if (serverLevel.getBlockTicks().hasScheduledTick(pos, block)) {
                        alreadyScheduled++;
                        continue;
                    }

                    emissiveBlock.scheduleInitialServer(serverLevel, pos, state, block);
                    scheduled++;
                }
            }
        }

        if (debug) {
            long dayTime = serverLevel.getDayTime();
            long modulo = Math.floorMod(dayTime, NightTime.DAY_TICKS);

            LOGGER.info(
                    "\n---------------- [NightEmissive Chunk Scan Result] ----------------\n" +
                            "Reason: {}\n" +
                            "Chunk: {}\n" +
                            "Dim: {}\n" +
                            "DayTime: {}\n" +
                            "DayTime Mod 24000: {}\n" +
                            "É noite (Vanilla): {}\n" +
                            "Blocos escaneados: {}\n" +
                            "Emissivos encontrados: {}\n" +
                            "Estados LIT sincronizados agora: {}\n" +
                            "Ticks agendados agora: {}\n" +
                            "Já tinham tick agendado: {}\n" +
                            "-------------------------------------------------------------------",
                    reason,
                    chunk.getPos(),
                    serverLevel.dimension().identifier(),
                    dayTime,
                    modulo,
                    NightTime.computeNight(modulo),
                    scanned,
                    emissiveFound,
                    syncedLit,
                    scheduled,
                    alreadyScheduled
            );
        }
    }
}
