package com.dreamsdimensions.mod.block;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
public final class PlumiteBlock extends Block {
    public PlumiteBlock(Properties properties) { super(properties); }
    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        entity.causeFallDamage(0.0, 0.0f, level.damageSources().fall());
    }
}
