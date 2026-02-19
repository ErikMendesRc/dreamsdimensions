package com.dreamsdimensions.mod.worldgen.decorator;

import com.dreamsdimensions.mod.DreamsDimensions;
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
    private static final float DEFAULT_TREE_CHANCE = 0.35F;
    private static final int DEFAULT_MIN_COLUMNS = 1;
    private static final int DEFAULT_MAX_COLUMNS = 3;
    private static final int DEFAULT_MIN_LENGTH = 2;
    private static final int DEFAULT_MAX_LENGTH = 6;

    public static final MapCodec<DreamsHangingLuminaVinesDecorator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    BuiltInCodecs.BLOCK.fieldOf("vine_block").forGetter(decorator -> decorator.vineBlock),
                    Codec.floatRange(0.0F, 1.0F).optionalFieldOf("tree_chance", DEFAULT_TREE_CHANCE).forGetter(decorator -> decorator.treeChance),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("min_columns", DEFAULT_MIN_COLUMNS).forGetter(decorator -> decorator.minColumns),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("max_columns", DEFAULT_MAX_COLUMNS).forGetter(decorator -> decorator.maxColumns),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("min_length", DEFAULT_MIN_LENGTH).forGetter(decorator -> decorator.minLength),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("max_length", DEFAULT_MAX_LENGTH).forGetter(decorator -> decorator.maxLength)
            ).apply(instance, DreamsHangingLuminaVinesDecorator::new)
    );

    private final Block vineBlock;
    private final float treeChance;
    private final int minColumns;
    private final int maxColumns;
    private final int minLength;
    private final int maxLength;

    public DreamsHangingLuminaVinesDecorator(Block vineBlock, float treeChance, int minColumns, int maxColumns, int minLength, int maxLength) {
        this.vineBlock = vineBlock;
        this.treeChance = treeChance;
        this.minColumns = minColumns;
        this.maxColumns = Math.max(minColumns, maxColumns);
        this.minLength = minLength;
        this.maxLength = Math.max(minLength, maxLength);
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return ModTreeDecorators.DREAMS_HANGING_LUMINA_VINES.get();
    }

    @Override
    public void place(Context context) {
        if (this.vineBlock != ModBlocks.LUMINA_HANGING_VINES.get()) {
            return;
        }

        RandomSource random = context.random();
        float treeRoll = random.nextFloat();
        DreamsDimensions.LOGGER.debug("[lumina_vines] RUN treeRoll={} treeChance={}", treeRoll, this.treeChance);
        if (treeRoll > this.treeChance) {
            return;
        }

        List<BlockPos> candidates = pickLeavesExposed(context);
        if (candidates.isEmpty()) {
            return;
        }

        int targetColumns = randomRange(random, this.minColumns, this.maxColumns);
        List<BlockPos> selectedLeaves = pickColumns(candidates, targetColumns, random);
        DreamsDimensions.LOGGER.debug("[lumina_vines] columnsTarget={} poolSize={} selected={}", targetColumns, candidates.size(), selectedLeaves.size());

        int placedColumns = 0;
        for (BlockPos leafPos : selectedLeaves) {
            int desiredLength = randomRange(random, this.minLength, this.maxLength);
            BlockPos startPos = leafPos.below();
            DreamsDimensions.LOGGER.debug("[lumina_vines] attempt leafPos={} startPos={} desiredLength={}", leafPos, startPos, desiredLength);
            if (placeColumn(context, leafPos, desiredLength) > 0) {
                placedColumns++;
            }
        }

        DreamsDimensions.LOGGER.debug("[lumina_vines] placedColumns={} targetColumns={}", placedColumns, targetColumns);
    }

    private List<BlockPos> pickLeavesExposed(Context context) {
        List<BlockPos> starts = new ArrayList<>();
        for (BlockPos leafPos : context.leaves()) {
            BlockPos startPos = leafPos.below();
            if (context.isAir(startPos) && context.checkBlock(leafPos, state -> state.is(BlockTags.LEAVES))) {
                starts.add(leafPos);
            }
        }
        return starts;
    }

    private List<BlockPos> pickColumns(List<BlockPos> candidates, int targetColumns, RandomSource random) {
        List<BlockPos> shuffled = new ArrayList<>(candidates);
        Collections.shuffle(shuffled, new java.util.Random(random.nextLong()));
        return shuffled.subList(0, Math.min(targetColumns, shuffled.size()));
    }

    private int placeColumn(Context context, BlockPos leafPos, int desiredLength) {
        BlockState state = this.vineBlock.defaultBlockState();
        BlockPos cursor = leafPos.below();
        int placed = 0;

        while (placed < desiredLength && context.checkBlock(cursor, BlockBehaviour.BlockStateBase::canBeReplaced)) {
            context.setBlock(cursor, state);

            BlockState stateAfterSet = context.level().getBlockState(cursor);
            boolean survives = stateAfterSet.canSurvive(context.level(), cursor);
            BlockState supportAbove = context.level().getBlockState(cursor.above());
            BlockState supportHorizontalNorth = context.level().getBlockState(cursor.north());

            DreamsDimensions.LOGGER.debug(
                    "[lumina_vines] post_set pos={} stateAfterSet={} canSurvive={} above={} north={} setter=context.setBlock",
                    cursor,
                    BuiltInRegistries.BLOCK.getKey(stateAfterSet.getBlock()),
                    survives,
                    BuiltInRegistries.BLOCK.getKey(supportAbove.getBlock()),
                    BuiltInRegistries.BLOCK.getKey(supportHorizontalNorth.getBlock())
            );

            if (!survives) {
                DreamsDimensions.LOGGER.debug("[lumina_vines] post_set_removed pos={} reason=canSurvive_false", cursor);
                break;
            }

            placed++;
            cursor = cursor.below();
        }

        return placed;
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
