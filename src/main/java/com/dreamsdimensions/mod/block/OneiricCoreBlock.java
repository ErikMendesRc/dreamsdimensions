package com.dreamsdimensions.mod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Task 8.5: lightweight vanilla particle accent for an already-registered
 * decorative/crafting block. The block ID, state, loot, tools, collisions,
 * redstone and world generation are unchanged. Vanilla calls animateTick
 * client-side for visible nearby blocks; no server listeners or radius scan.
 */
public final class OneiricCoreBlock extends Block {
    private static final DustParticleOptions CORE_DUST =
            new DustParticleOptions(0x82CED2, 0.65F);

    public OneiricCoreBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        // At most one particle, approximately once per 12 visible random ticks.
        if (random.nextInt(12) != 0) return;
        double x = pos.getX() + 0.2 + random.nextDouble() * 0.6;
        double y = pos.getY() + 0.9 + random.nextDouble() * 0.15;
        double z = pos.getZ() + 0.2 + random.nextDouble() * 0.6;
        level.addParticle(CORE_DUST, x, y, z, 0, 0.007, 0);
    }
}
