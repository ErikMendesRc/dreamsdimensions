package com.dreamsdimensions.mod.service;

import com.dreamsdimensions.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import java.util.Optional;

/** Loaded-chunk-only bound search; never load chunks to probe for ores. */
public final class OreSignalService {
    private OreSignalService(){}

    public static Optional<BlockPos> find(ServerLevel level,BlockPos origin,int radius) {
        BlockPos winner=null;
        double nearest=Double.MAX_VALUE;
        for(BlockPos point:BlockPos.betweenClosed(
                origin.offset(-radius,-radius,-radius),
                origin.offset(radius,radius,radius))) {
            if(!level.hasChunkAt(point))continue;
            double d=point.distSqr(origin);
            if(d>radius*radius||d>=nearest)continue;
            var block=level.getBlockState(point);
            if(block.is(ModBlocks.DREAM_ORE.get())
                    ||block.is(ModBlocks.DEEPSLATE_DREAM_ORE.get())) {
                winner=point.immutable();nearest=d;
            }
        }
        return Optional.ofNullable(winner);
    }

    public static Optional<BlockPos> marker(ServerLevel level,BlockPos origin,int radius) {
        BlockPos winner=null;double nearest=Double.MAX_VALUE;
        for(BlockPos point:BlockPos.betweenClosed(origin.offset(-radius,-8,-radius),
                origin.offset(radius,8,radius))){
            if(!level.hasChunkAt(point))continue;
            double d=point.distSqr(origin);
            if(d>=nearest || d>radius*radius)continue;
            if(level.getBlockState(point).is(ModBlocks.TRAIL_MARKER.get())) {
                winner=point.immutable();nearest=d;
            }
        }
        return Optional.ofNullable(winner);
    }
}
