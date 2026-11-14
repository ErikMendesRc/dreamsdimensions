package com.dreamsdimensions.mod.service;

import com.dreamsdimensions.mod.block.entity.DreamBedsideBlockEntity;
import com.dreamsdimensions.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;

/** A room has exactly one identity; ambiguous overlapping rooms do not produce. */
public final class DreamIncubationEnvironment {
    public enum Kind { NONE, SERENE, SILENCE, EMBER, DEEP, GARDEN, AMBIGUOUS }

    private DreamIncubationEnvironment() {}

    public static boolean bedsideAdjacent(Level world, BlockPos at) {
        for (BlockPos p : BlockPos.betweenClosed(at.offset(-2,-1,-2),at.offset(2,1,2))) {
            if(p.distManhattan(at)>2)continue;
            if(world.getBlockState(p).getBlock() instanceof BedBlock)return true;
        }
        return false;
    }

    public static boolean dreamcatcher(Level world, BlockPos bed) {
        return world.getBlockState(bed.above()).is(ModBlocks.DREAMCATCHER.get())
                || world.getBlockState(bed.above(2)).is(ModBlocks.DREAMCATCHER.get());
    }

    public static Kind detect(Level world, BlockPos bed) {
        boolean serene=world.canSeeSky(bed.above());
        boolean deep=bed.getY()<0&&!serene;
        int wool=0,flowers=0;
        boolean fire=false;
        for(BlockPos p:BlockPos.betweenClosed(bed.offset(-4,-2,-4),bed.offset(4,2,4))) {
            if(!world.hasChunkAt(p))continue;
            var state=world.getBlockState(p);
            int dist=Math.max(Math.abs(p.getX()-bed.getX()),Math.abs(p.getZ()-bed.getZ()));
            if(dist<=2&&p.getY()>=bed.getY()-1&&p.getY()<=bed.getY()+2
                    &&state.is(BlockTags.WOOL))wool++;
            if(dist<=4&&(state.is(Blocks.CAMPFIRE)||state.is(Blocks.SOUL_CAMPFIRE))
                    &&state.getValue(CampfireBlock.LIT))fire=true;
            if(dist<=3&& (state.is(BlockTags.FLOWERS)||state.is(BlockTags.LEAVES)))flowers++;
        }
        boolean silence=wool>=12;
        boolean garden=flowers>=8;
        // An intentionally cultivated outdoor Garden takes precedence over
        // an otherwise open-sky Sereno bedroom.
        if(garden)serene=false;
        int count=(serene?1:0)+(silence?1:0)+(fire?1:0)+(deep?1:0)+(garden?1:0);
        if(count==0)return Kind.NONE;
        if(count>1)return Kind.AMBIGUOUS;
        if(serene)return Kind.SERENE;
        if(silence)return Kind.SILENCE;
        if(fire)return Kind.EMBER;
        if(deep)return Kind.DEEP;
        return Kind.GARDEN;
    }
}
