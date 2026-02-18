package com.dreamsdimensions.mod.content.emissive;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;

/**
 * Contrato e helpers compartilhados para blocos com emissivo noturno.
 */
public interface NightEmissiveBlockBase {
    BooleanProperty LIT = BlockStateProperties.LIT;
    int INITIAL_CHECK_DELAY_TICKS = 20;
    int WORLDGEN_RETRY_DELAY_TICKS = 160;
    int BASE_CHECK_DELAY_TICKS = 100;
    int JITTER_CHECK_DELAY_TICKS = 500;
    int UPDATE_FLAGS = Block.UPDATE_ALL;

    default void appendNightEmissiveProperties(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    default BlockState defaultNightState(BlockState state) {
        return state.setValue(LIT, false);
    }

    default boolean isNight(Level level) {
        NightEmissiveDebug.logClock(level, "isNight");
        return NightTime.isVanillaNight(level);
    }

    default boolean extraConditions(Level level, BlockPos pos, BlockState state) {
        return true;
    }

    default int nextCheckDelayTicks(Level level, BlockPos pos) {
        int jitter = Math.floorMod(pos.hashCode(), JITTER_CHECK_DELAY_TICKS + 1);
        return BASE_CHECK_DELAY_TICKS + jitter;
    }

    default boolean shouldBeLit(Level level, BlockPos pos, BlockState state) {
        return isNight(level) && extraConditions(level, pos, state);
    }

    default void scheduleInitial(ServerLevel level, BlockPos pos, BlockState state, Block block) {
        NightEmissiveDebug.ensureAssetValidation();
        level.scheduleTick(pos, block, INITIAL_CHECK_DELAY_TICKS);
        NightEmissiveDebug.logInitialSchedule(level, pos, state, block, INITIAL_CHECK_DELAY_TICKS, "onPlace");
    }

    default void scheduledTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, Block block) {
        NightEmissiveDebug.ensureAssetValidation();

        ChunkAccess fullChunk = level.getChunkSource().getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.FULL, false);
        if (fullChunk == null) {
            ChunkAccess observedChunk = level.getChunkSource().getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.EMPTY, false);
            Identifier blockId = BuiltInRegistries.BLOCK.getKey(block);
            String persistedStatus = observedChunk == null ? "<unloaded>" : observedChunk.getPersistedStatus().getName();

            level.scheduleTick(pos, block, WORLDGEN_RETRY_DELAY_TICKS);
            NightEmissiveDebug.logSkippedWorldgenUpdate(level, pos, blockId, persistedStatus, WORLDGEN_RETRY_DELAY_TICKS);
            return;
        }

        boolean shouldLight = shouldBeLit(level, pos, state);
        boolean currentlyLit = state.getValue(LIT);
        boolean changed = currentlyLit != shouldLight;

        if (changed) {
            BlockState updated = state.setValue(LIT, shouldLight);
            level.setBlock(pos, updated, UPDATE_FLAGS);
            NightEmissiveDebug.logSetBlockResult(level, pos, state, updated, UPDATE_FLAGS);
        }

        int nextDelay = nextCheckDelayTicks(level, pos);
        level.scheduleTick(pos, block, nextDelay);
        NightEmissiveDebug.logScheduledTick(state, level, pos, block, shouldLight, nextDelay, UPDATE_FLAGS, changed);
    }
}
