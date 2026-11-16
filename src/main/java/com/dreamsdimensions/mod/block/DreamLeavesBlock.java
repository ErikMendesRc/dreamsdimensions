package com.dreamsdimensions.mod.block;

import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.sounds.AmbientLeavesBlockSoundPlayer;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Preserves old leaves without state properties: missing legacy properties
 * resolve to this persistent default. Procedural foliage explicitly sets
 * PERSISTENT=false and calculates the distance from the generated logs.
 */
public class DreamLeavesBlock extends LeavesBlock {
    public DreamLeavesBlock(BlockBehaviour.Properties properties) {
        super(AmbientLeavesBlockSoundPlayer.noAmbientSound(), properties);
        registerDefaultState(defaultBlockState().setValue(PERSISTENT, true));
    }
}
