package com.dreamsdimensions.mod.worldgen;

import com.dreamsdimensions.mod.registry.ModBlocks;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.Feature;
import java.util.ArrayList;
import java.util.List;

/** A small, exposed 3–5 block geological outcrop on stone mountains >=Y120. */
public record PlumiteOutcropFeature(int minimumY) implements Feature {
    public static final MapCodec<PlumiteOutcropFeature> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    net.minecraft.util.ExtraCodecs.POSITIVE_INT.fieldOf("minimum_y")
                            .forGetter(PlumiteOutcropFeature::minimumY)
            ).apply(instance, PlumiteOutcropFeature::new));

    @Override public MapCodec<PlumiteOutcropFeature> codec() { return CODEC; }

    @Override public boolean place(WorldGenLevel level, ChunkGenerator generator,
            RandomSource random, BlockPos origin) {
        if (origin.getY() < minimumY || origin.getY() > level.getMaxY() - 4) return false;
        if (!level.getWorldBorder().isWithinBounds(origin)) return false;

        // Surface snow/gravel may overlie the actual stone: only displace STONE.
        BlockPos exposed = null;
        for (int dy = 0; dy <= 3; dy++) {
            BlockPos p = origin.below(dy);
            if (p.getY() < minimumY || !level.ensureCanWrite(p)
                    || !level.getBlockState(p).is(Blocks.STONE)) continue;
            for (Direction face : Direction.values()) {
                if (level.getBlockState(p.relative(face)).isAir()) {
                    exposed = p; break;
                }
            }
            if (exposed != null) break;
        }
        if (exposed == null) return false;
        List<BlockPos> vein = new ArrayList<>();
        vein.add(exposed);
        BlockPos cursor = exposed;
        int target = 3 + random.nextInt(3);
        for (int i = 1; i < target; i++) {
            List<BlockPos> valid = new ArrayList<>();
            for (Direction d : Direction.values()) {
                BlockPos candidate = cursor.relative(d);
                if (candidate.getY() >= minimumY && level.ensureCanWrite(candidate)
                        && level.getWorldBorder().isWithinBounds(candidate)
                        && level.getBlockState(candidate).is(Blocks.STONE)
                        && !vein.contains(candidate)) valid.add(candidate);
            }
            if (valid.isEmpty()) break;
            cursor = valid.get(random.nextInt(valid.size()));
            vein.add(cursor);
        }
        if (vein.size() < 3) return false;
        for (BlockPos p : vein) {
            level.setBlock(p, ModBlocks.PLUMITE_ORE.get().defaultBlockState(), 2);
        }
        return true;
    }
}
