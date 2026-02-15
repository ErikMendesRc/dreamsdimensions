package com.dreamsdimensions.mod.block;

import com.dreamsdimensions.mod.block.entity.OwLuminaFlowerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Flor luminosa do Overworld com brilho visual e partículas leves no cliente.
 */
public class OwLuminaFlowerBlock extends FlowerBlock implements EntityBlock {
    private static final float PARTICLE_CHANCE = 0.15F;

    public OwLuminaFlowerBlock(BlockBehaviour.Properties properties) {
        super(SuspiciousStewEffects.EMPTY, properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new OwLuminaFlowerBlockEntity(pos, state);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        if (!level.isClientSide() || random.nextFloat() >= PARTICLE_CHANCE) {
            return;
        }

        double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.25D;
        double y = pos.getY() + 0.55D + random.nextDouble() * 0.35D;
        double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.25D;

        double vx = (random.nextDouble() - 0.5D) * 0.01D;
        double vy = 0.01D + random.nextDouble() * 0.015D;
        double vz = (random.nextDouble() - 0.5D) * 0.01D;

        level.addParticle(ParticleTypes.GLOW, x, y, z, vx, vy, vz);
    }
}
