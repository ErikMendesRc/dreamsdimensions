package com.dreamsdimensions.mod.worldgen.decorator;

import com.dreamsdimensions.mod.registry.ModBlocks;
import com.dreamsdimensions.mod.registry.ModTreeDecorators;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

import java.util.List;

public class DreamsHangingVinesDecorator extends TreeDecorator {
    public static final MapCodec<DreamsHangingVinesDecorator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.floatRange(0.0F, 1.0F).fieldOf("tree_vine_chance").forGetter(d -> d.treeVineChance),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("leaf_chance").forGetter(d -> d.leafChance),
                    ExtraCodecs.POSITIVE_INT.fieldOf("min_length").forGetter(d -> d.minLength),
                    ExtraCodecs.POSITIVE_INT.fieldOf("max_length").forGetter(d -> d.maxLength)
            ).apply(instance, DreamsHangingVinesDecorator::new)
    );

    private final float treeVineChance;
    private final float leafChance;
    private final int minLength;
    private final int maxLength;

    public DreamsHangingVinesDecorator(float treeVineChance, float leafChance, int minLength, int maxLength) {
        this.treeVineChance = treeVineChance;
        this.leafChance = leafChance;
        this.minLength = minLength;
        this.maxLength = Math.max(minLength, maxLength);
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return ModTreeDecorators.DREAMS_HANGING_VINES.get();
    }

    @Override
    public void place(Context context) {
        var random = context.random();
        if (random.nextFloat() > this.treeVineChance) {
            return;
        }

        List<BlockPos> shuffledLeaves = Util.shuffledCopy(context.leaves(), random);
        for (BlockPos leafPos : shuffledLeaves) {
            if (random.nextFloat() >= this.leafChance) {
                continue;
            }

            tryPlaceFromLeaf(context, random, leafPos.west(), Direction.EAST);
            tryPlaceFromLeaf(context, random, leafPos.east(), Direction.WEST);
            tryPlaceFromLeaf(context, random, leafPos.north(), Direction.SOUTH);
            tryPlaceFromLeaf(context, random, leafPos.south(), Direction.NORTH);
        }
    }

    private void tryPlaceFromLeaf(Context context, net.minecraft.util.RandomSource random, BlockPos startPos, Direction faceDirection) {
        if (!context.isAir(startPos)) {
            return;
        }

        BooleanProperty face = MultifaceBlock.getFaceProperty(faceDirection);
        BlockState state = ModBlocks.OW_LUMINA_VINES.get().defaultBlockState().setValue(face, true);

        int length = pickBiasedLength(random);
        BlockPos cursor = startPos;
        for (int i = 0; i < length && context.isAir(cursor); i++) {
            context.setBlock(cursor, state);
            cursor = cursor.below();
        }
    }

    private int pickBiasedLength(net.minecraft.util.RandomSource random) {
        int span = this.maxLength - this.minLength;
        if (span <= 0) {
            return this.minLength;
        }
        int a = random.nextInt(span + 1);
        int b = random.nextInt(span + 1);
        return this.minLength + (a + b) / 2;
    }
}
