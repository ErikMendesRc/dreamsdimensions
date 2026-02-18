package com.dreamsdimensions.mod.content.emissive;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.config.DreamsConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

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
        return NightTime.isVanillaNight(level)
                || DreamsConfig.forceNightEmissiveInDreamDimensions(level.dimension());
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

    default void scheduleInitialServer(ServerLevel level, BlockPos pos, BlockState state, Block block) {
        level.scheduleTick(pos, block, INITIAL_CHECK_DELAY_TICKS);
    }

    default void scheduledTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, Block block) {
        if (!level.shouldTickBlocksAt(pos.asLong())) {
            level.scheduleTick(pos, block, WORLDGEN_RETRY_DELAY_TICKS);
            return;
        }

        if (!state.hasProperty(LIT)) {
            DreamsDimensions.LOGGER.error(
                    "[NightEmissive] missing LIT property at pos={} dim={} state={}",
                    pos, level.dimension().identifier(), state
            );
            return;
        }

        long dayTime = level.getDayTime();
        long dayTimeModulo = Math.floorMod(dayTime, NightTime.DAY_TICKS);
        boolean vanillaNight = NightTime.computeNight(dayTimeModulo);

        boolean extra = extraConditions(level, pos, state);
        boolean shouldLight = shouldBeLit(level, pos, state);
        boolean litBefore = state.getValue(LIT);

        if (litBefore != shouldLight) {
            level.setBlock(pos, state.setValue(LIT, shouldLight), UPDATE_FLAGS);
        }

        if (DreamsConfig.isNightEmissiveDebugLogsEnabled()) {
            DreamsDimensions.LOGGER.info(
                    "[NightEmissive] scheduledTick block={} pos={} dim={} overworld={} dayTime={} dayTimeModulo={} hasFixedTime={} vanillaNight={} extra={} shouldLight={} litBefore={} litAfter={}",
                    block.builtInRegistryHolder().key().identifier(),
                    pos,
                    level.dimension().identifier(),
                    level.dimension() == Level.OVERWORLD,
                    dayTime,
                    dayTimeModulo,
                    level.dimensionType().hasFixedTime(),
                    vanillaNight,
                    extra,
                    shouldLight,
                    litBefore,
                    shouldLight
            );
        }

        level.scheduleTick(pos, block, nextCheckDelayTicks(level, pos));
    }
}