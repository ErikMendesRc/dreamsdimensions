package com.dreamsdimensions.mod.worldgen.decorator;

import com.dreamsdimensions.mod.registry.ModBlocks;
import com.dreamsdimensions.mod.registry.ModTreeDecorators;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DreamsHangingLuminaVinesDecorator extends TreeDecorator {
    private static final float DEFAULT_TREE_CHANCE = 0.45F;
    private static final int DEFAULT_MIN_COLUMNS = 1;
    private static final int DEFAULT_MAX_COLUMNS = 4;
    private static final int DEFAULT_MIN_LENGTH = 3;
    private static final int DEFAULT_MAX_LENGTH = 16;
    private static final int DEFAULT_MAX_ATTEMPTS = 8;

    public static final MapCodec<DreamsHangingLuminaVinesDecorator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BuiltInCodecs.BLOCK.fieldOf("vine_block").forGetter(decorator -> decorator.vineBlock),
                    Codec.floatRange(0.0F, 1.0F).optionalFieldOf("tree_chance", DEFAULT_TREE_CHANCE).forGetter(decorator -> decorator.treeChance),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("min_columns", DEFAULT_MIN_COLUMNS).forGetter(decorator -> decorator.minColumns),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("max_columns", DEFAULT_MAX_COLUMNS).forGetter(decorator -> decorator.maxColumns),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("min_length", DEFAULT_MIN_LENGTH).forGetter(decorator -> decorator.minLength),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("max_length", DEFAULT_MAX_LENGTH).forGetter(decorator -> decorator.maxLength),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("max_attempts", DEFAULT_MAX_ATTEMPTS).forGetter(decorator -> decorator.maxAttempts)
            ).apply(instance, DreamsHangingLuminaVinesDecorator::new)
    );

    private final Block vineBlock;
    private final float treeChance;
    private final int minColumns;
    private final int maxColumns;
    private final int minLength;
    private final int maxLength;
    private final int maxAttempts;

    public DreamsHangingLuminaVinesDecorator(Block vineBlock, float treeChance, int minColumns, int maxColumns, int minLength, int maxLength, int maxAttempts) {
        this.vineBlock = vineBlock;
        this.treeChance = treeChance;
        this.minColumns = minColumns;
        this.maxColumns = Math.max(minColumns, maxColumns);
        this.minLength = minLength;
        this.maxLength = Math.max(minLength, maxLength);
        this.maxAttempts = maxAttempts;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return ModTreeDecorators.DREAMS_HANGING_LUMINA_VINES.get();
    }

    @Override
    public void place(Context context) {
        if (this.vineBlock != ModBlocks.LUMINA_VINES.get() || context.random().nextFloat() > this.treeChance) {
            return;
        }

        List<BlockPos> candidates = pickExteriorLeaves(context);
        if (candidates.isEmpty()) {
            return;
        }

        RandomSource random = context.random();
        Collections.shuffle(candidates, new java.util.Random(random.nextLong()));

        int targetColumns = randomRange(random, this.minColumns, this.maxColumns);
        int placedColumns = 0;
        int attempts = 0;

        for (BlockPos leafPos : candidates) {
            if (attempts >= this.maxAttempts || placedColumns >= targetColumns) {
                break;
            }

            attempts++;
            int desiredLength = rollColumnLength(random);
            int placed = placeColumn(context, leafPos.below(), desiredLength);
            if (placed > 0) {
                placedColumns++;
            }
        }
    }

    private List<BlockPos> pickExteriorLeaves(Context context) {
        List<BlockPos> candidates = new ArrayList<>();
        for (BlockPos leafPos : context.leaves()) {
            if (!context.checkBlock(leafPos, state -> state.is(BlockTags.LEAVES))) {
                continue;
            }

            BlockPos startPos = leafPos.below();
            if (!context.isAir(startPos)) {
                continue;
            }

            if (hasHorizontalAirExposure(context, leafPos)) {
                candidates.add(leafPos);
            }
        }

        return candidates;
    }

    private static boolean hasHorizontalAirExposure(Context context, BlockPos pos) {
        return context.isAir(pos.north()) || context.isAir(pos.south()) || context.isAir(pos.east()) || context.isAir(pos.west());
    }

    private int placeColumn(Context context, BlockPos startPos, int desiredLength) {
        BlockState state = this.vineBlock.defaultBlockState();
        BlockPos cursor = startPos;
        int placed = 0;

        while (placed < desiredLength && context.checkBlock(cursor, BlockBehaviour.BlockStateBase::canBeReplaced)) {
            context.setBlock(cursor, state);
            placed++;
            cursor = cursor.below();
        }

        return placed;
    }

    private int rollColumnLength(RandomSource random) {
        int base = randomRange(random, this.minLength, this.maxLength);
        return random.nextFloat() < 0.1F ? Math.min(16, base + randomRange(random, 2, 4)) : base;
    }

    private static int randomRange(RandomSource random, int min, int max) {
        return min >= max ? min : min + random.nextInt(max - min + 1);
    }

    private static final class BuiltInCodecs {
        private static final Codec<Block> BLOCK = BuiltInRegistries.BLOCK.byNameCodec();

        private BuiltInCodecs() {
        }
    }
}
