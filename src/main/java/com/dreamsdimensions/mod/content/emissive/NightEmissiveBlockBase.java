package com.dreamsdimensions.mod.content.emissive;

import com.dreamsdimensions.mod.DreamsDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
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
        String litInfo = state.hasProperty(LIT) ? String.valueOf(state.getValue(LIT)) : "missing";

        DreamsDimensions.LOGGER.info(
                "[NightEmissiveRuntime] scheduleInitial block={} pos={} dim={} dayTimeRaw={} dayTimeMod={} gameTime={} doDaylightCycle={} fixedTime={} litState={}",
                BuiltInRegistries.BLOCK.getKey(block),
                pos,
                dimId(level),
                level.getDayTime(),
                Math.floorMod(level.getDayTime(), NightTime.DAY_TICKS),
                level.getGameTime(),
                daylightCycleInfo(level),
                fixedTimeInfo(level),
                litInfo
        );
        level.scheduleTick(pos, block, INITIAL_CHECK_DELAY_TICKS);
    }

    default void scheduledTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random, Block block) {
        if (!level.shouldTickBlocksAt(pos.asLong())) {
            DreamsDimensions.LOGGER.info(
                    "[NightEmissiveRuntime] skipTickNotInRange block={} pos={} dim={}",
                    BuiltInRegistries.BLOCK.getKey(block),
                    pos,
                    dimId(level)
            );
            level.scheduleTick(pos, block, WORLDGEN_RETRY_DELAY_TICKS);
            return;
        }

        if (!state.hasProperty(LIT)) {
            DreamsDimensions.LOGGER.error(
                    "[NightEmissiveRuntime] missingLITProperty block={} pos={} state={} - skipping",
                    BuiltInRegistries.BLOCK.getKey(block),
                    pos,
                    state
            );
            return;
        }

        boolean shouldLight = shouldBeLit(level, pos, state);
        if (shouldLight) {
            DreamsDimensions.LOGGER.info(
                    "[NightEmissiveRuntime] tickShouldLight block={} pos={} dim={} dayTimeRaw={} dayTimeMod={} gameTime={} doDaylightCycle={} fixedTime={} litCurrent={}",
                    BuiltInRegistries.BLOCK.getKey(block),
                    pos,
                    dimId(level),
                    level.getDayTime(),
                    Math.floorMod(level.getDayTime(), NightTime.DAY_TICKS),
                    level.getGameTime(),
                    daylightCycleInfo(level),
                    fixedTimeInfo(level),
                    state.getValue(LIT)
            );
        }

        if (state.getValue(LIT) != shouldLight) {
            DreamsDimensions.LOGGER.info(
                    "[NightEmissiveRuntime] stateChange block={} pos={} lit:{}->{} flags={}",
                    BuiltInRegistries.BLOCK.getKey(block),
                    pos,
                    state.getValue(LIT),
                    shouldLight,
                    UPDATE_FLAGS
            );
            level.setBlock(pos, state.setValue(LIT, shouldLight), UPDATE_FLAGS);
        }

        level.scheduleTick(pos, block, nextCheckDelayTicks(level, pos));
    }

    // API inspection (MC 1.21.11 + NeoForge beta from local workspace sources):
    // - net.minecraft.world.level.Level#dimension() -> ResourceKey<Level>
    // - net.minecraft.world.level.Level#getDayTime() / #getGameTime()
    // - net.minecraft.server.level.ServerLevel#getGameRules() exists, with GameRules#getBoolean(GameRules.RULE_DAYLIGHT)
    // - net.minecraft.world.level.dimension.DimensionType is a record with fixedTime(), but fixed time logging is intentionally disabled here.
    static String dimId(Level level) {
        return String.valueOf(level.dimension());
    }

    static String daylightCycleInfo(Level level) {
        return "unknown";
    }

    static String fixedTimeInfo(Level level) {
        return "unknown";
    }
}
