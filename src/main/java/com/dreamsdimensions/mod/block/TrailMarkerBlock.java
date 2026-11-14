package com.dreamsdimensions.mod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Lighted navigation obelisk. Client-only rising particle beam. */
public final class TrailMarkerBlock extends Block {
    private static final DustParticleOptions SIGNAL=new DustParticleOptions(0xE3AC70,0.9f);
    public TrailMarkerBlock(Properties p){super(p);}
    @Override public void animateTick(BlockState state, Level level,BlockPos pos,RandomSource random) {
        if(random.nextInt(4)!=0)return;
        double y=pos.getY()+1+random.nextDouble()*19.0;
        level.addParticle(SIGNAL,pos.getX()+0.5,y,pos.getZ()+0.5,
                0,0.018,0);
    }
}
