package com.dreamsdimensions.mod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** 30-second non-dropping temporary walkway; each cell owns a saved scheduled tick. */
public final class TemporaryPlumeBridgeBlock extends Block {
    public static final int LIFETIME_TICKS = 600;
    public TemporaryPlumeBridgeBlock(Properties properties) { super(properties); }
    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getBlockState(pos).is(this)) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }
}
