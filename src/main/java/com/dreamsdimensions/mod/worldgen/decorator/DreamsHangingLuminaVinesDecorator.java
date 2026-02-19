package com.dreamsdimensions.mod.worldgen.decorator;

import com.dreamsdimensions.mod.registry.ModBlocks;
import com.dreamsdimensions.mod.registry.ModTreeDecorators;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
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
    private static final int DEFAULT_MAX_LENGTH = 8;

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
        if (this.vineBlock != ModBlocks.OW_LUMINA_VINES.get()) {
            return;
        }

        RandomSource random = context.random();
        if (random.nextFloat() > this.treeChance) {
            return;
        }

        List<StartPoint> candidates = collectExternalLeaves(context);
        if (candidates.isEmpty()) {
            return;
        }

        int targetColumns = randomRange(random, this.minColumns, this.maxColumns);
        Collections.shuffle(candidates, new java.util.Random(random.nextLong()));

        int placedColumns = 0;
        for (StartPoint startPoint : candidates) {
            if (placedColumns >= targetColumns) {
                break;
            }

            if (placeColumn(context, startPoint, randomRange(random, this.minLength, this.maxLength)) > 0) {
                placedColumns++;
            }
        }
    }

    private List<StartPoint> collectExternalLeaves(Context context) {
        List<StartPoint> starts = new ArrayList<>();
        for (BlockPos leafPos : context.leaves()) {
            if (countHorizontalAirNeighbors(context, leafPos) == 0) {
                continue;
            }

            BlockPos startPos = leafPos.below();
            if (!context.isAir(startPos)) {
                continue;
            }

            Direction attachmentFace = pickAttachmentFace(context, leafPos, startPos);
            if (attachmentFace != null) {
                starts.add(new StartPoint(startPos, attachmentFace));
            }
        }

        return starts;
    }

    private int countHorizontalAirNeighbors(Context context, BlockPos leafPos) {
        int airNeighbors = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (context.isAir(leafPos.relative(direction))) {
                airNeighbors++;
            }
        }
        return airNeighbors;
    }

    private Direction pickAttachmentFace(Context context, BlockPos leafPos, BlockPos startPos) {
        List<Direction> faces = new ArrayList<>(5);

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (!context.isAir(startPos.relative(direction))) {
                faces.add(direction);
            }
        }

        if (!context.isAir(leafPos)) {
            faces.add(Direction.UP);
        }

        if (faces.isEmpty()) {
            return null;
        }

        return faces.get(context.random().nextInt(faces.size()));
    }

    private int placeColumn(Context context, StartPoint startPoint, int desiredLength) {
        BlockState state = vineStateForFace(startPoint.attachmentFace());

        int placed = 0;
        BlockPos cursor = startPoint.startPos();
        while (placed < desiredLength && context.isAir(cursor)) {
            context.setBlock(cursor, state);
            placed++;
            cursor = cursor.below();
        }

        return placed;
    }

    private BlockState vineStateForFace(Direction face) {
        BooleanProperty property = MultifaceBlock.getFaceProperty(face);
        return this.vineBlock.defaultBlockState().setValue(property, true);
    }

    private static int randomRange(RandomSource random, int min, int max) {
        return min >= max ? min : min + random.nextInt(max - min + 1);
    }

    private record StartPoint(BlockPos startPos, Direction attachmentFace) {
    }

    private static final class BuiltInCodecs {
        private static final Codec<Block> BLOCK = net.minecraft.core.registries.BuiltInRegistries.BLOCK.byNameCodec();

        private BuiltInCodecs() {
        }
    }
}
