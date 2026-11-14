package com.dreamsdimensions.mod.service;

import com.dreamsdimensions.mod.block.OneiricCoreBlock;
import com.dreamsdimensions.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Structure levels are read once, when the Core GUI opens. No block entity,
 * persistent inventory, chunk scan or every-tick listener.
 */
public final class CoreChamberPolicy {
    private CoreChamberPolicy() {}

    /** Eight pylons at radius two leave a one-block walkable gap around the Core. */
    public static boolean hasLowerRing(Level world, BlockPos core) {
        for (int dx=-2; dx<=2; dx+=2) for (int dz=-2; dz<=2; dz+=2) {
            if(dx==0 && dz==0) continue;
            if(!world.getBlockState(core.offset(dx,0,dz)).is(ModBlocks.CUT_MARKED_STONE.get())) return false;
        }
        return true;
    }

    public static boolean hasUpperRing(Level world, BlockPos core) {
        boolean allCut = true;
        for (int dx=-2; dx<=2; dx+=2) for (int dz=-2; dz<=2; dz+=2) {
            if(dx==0 && dz==0) continue;
            if(!world.getBlockState(core.offset(dx,1,dz)).is(ModBlocks.CUT_MARKED_STONE.get())) allCut=false;
        }
        if(allCut)return true;
        return world.getBlockState(core.offset(2,1,0)).is(ModBlocks.DEEP_SLEEP_STONE.get())
            && world.getBlockState(core.offset(-2,1,0)).is(ModBlocks.DEEP_SLEEP_STONE.get())
            && world.getBlockState(core.offset(0,1,2)).is(ModBlocks.DEEP_SLEEP_STONE.get())
            && world.getBlockState(core.offset(0,1,-2)).is(ModBlocks.DEEP_SLEEP_STONE.get());
    }

    public static int level(Level world, BlockPos core) {
        if (!hasLowerRing(world,core)) return 0;
        BlockState state=world.getBlockState(core);
        if(state.is(ModBlocks.ONEIRIC_CORE_BLOCK.get())
                && state.getValue(OneiricCoreBlock.ATTUNED) && hasUpperRing(world,core)) return 2;
        return 1;
    }

    public static boolean canAttune(Level world, BlockPos core) {
        BlockState state=world.getBlockState(core);
        return state.is(ModBlocks.ONEIRIC_CORE_BLOCK.get())
                && !state.getValue(OneiricCoreBlock.ATTUNED)
                && hasLowerRing(world,core) && hasUpperRing(world,core);
    }
}
