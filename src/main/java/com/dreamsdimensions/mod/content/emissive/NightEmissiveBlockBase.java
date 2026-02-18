package com.dreamsdimensions.mod.content.emissive;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

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

    default void scheduleInitial(Level level, BlockPos pos, BlockState state, Block block) {
        if (level instanceof ServerLevel serverLevel) {
            scheduleInitialServer(serverLevel, pos, state, block);
        }
    }

    default void scheduleInitial(ServerLevel level, BlockPos pos, BlockState state, Block block) {
        scheduleInitialServer(level, pos, state, block);
    }

    default void scheduleInitialServer(ServerLevel level, BlockPos pos, BlockState state, Block block) {
        level.scheduleTick(pos, block, INITIAL_CHECK_DELAY_TICKS);
    }

    default void scheduledTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, Block block) {
        if (!level.shouldTickBlocksAt(pos.asLong())) {
            level.scheduleTick(pos, block, WORLDGEN_RETRY_DELAY_TICKS);
            return;
        }

        boolean shouldLight = shouldBeLit(level, pos, state);
        if (state.getValue(LIT) != shouldLight) {
            level.setBlock(pos, state.setValue(LIT, shouldLight), UPDATE_FLAGS);
        }

        level.scheduleTick(pos, block, nextCheckDelayTicks(level, pos));
    }
}
