package com.dreamsdimensions.mod.block;

import com.dreamsdimensions.mod.content.emissive.NightEmissiveBlockBase;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class OwDreamGlowMossBlock extends Block implements NightEmissiveBlockBase {
    public OwDreamGlowMossBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(defaultNightState(this.stateDefinition.any()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        appendNightEmissiveProperties(builder);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!oldState.is(this)) {
            scheduleInitial(level, pos, state, this);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        scheduledTick(state, level, pos, random, this);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        scheduledTick(state, level, pos, random, this);
    }
}
