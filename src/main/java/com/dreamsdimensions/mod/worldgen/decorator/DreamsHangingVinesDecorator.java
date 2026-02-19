package com.dreamsdimensions.mod.worldgen.decorator;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.registry.ModTreeDecorators;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DreamsHangingVinesDecorator extends TreeDecorator {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final MapCodec<DreamsHangingVinesDecorator> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.floatRange(0.0F, 1.0F).fieldOf("tree_vine_chance").forGetter(d -> d.treeVineChance),
                    ExtraCodecs.POSITIVE_INT.fieldOf("min_columns").forGetter(d -> d.minColumns),
                    ExtraCodecs.POSITIVE_INT.fieldOf("max_columns").forGetter(d -> d.maxColumns),
                    ExtraCodecs.POSITIVE_INT.fieldOf("min_length").forGetter(d -> d.minLength),
                    ExtraCodecs.POSITIVE_INT.fieldOf("max_length").forGetter(d -> d.maxLength),
                    Codec.floatRange(0.0F, 1.0F).fieldOf("long_vines_chance").forGetter(d -> d.longVinesChance),
                    ExtraCodecs.POSITIVE_INT.fieldOf("long_min_length").forGetter(d -> d.longMinLength),
                    ExtraCodecs.POSITIVE_INT.fieldOf("long_max_length").forGetter(d -> d.longMaxLength),
                    Codec.BOOL.optionalFieldOf("debug", false).forGetter(d -> d.debug)
            ).apply(instance, DreamsHangingVinesDecorator::new)
    );

    private final float treeVineChance;
    private final int minColumns;
    private final int maxColumns;
    private final int minLength;
    private final int maxLength;
    private final float longVinesChance;
    private final int longMinLength;
    private final int longMaxLength;
    private final boolean debug;

    public DreamsHangingVinesDecorator(float treeVineChance, int minColumns, int maxColumns, int minLength, int maxLength,
                                       float longVinesChance, int longMinLength, int longMaxLength, boolean debug) {
        this.treeVineChance = treeVineChance;
        this.minColumns = minColumns;
        this.maxColumns = Math.max(minColumns, maxColumns);
        this.minLength = minLength;
        this.maxLength = Math.max(minLength, maxLength);
        this.longVinesChance = longVinesChance;
        this.longMinLength = longMinLength;
        this.longMaxLength = Math.max(longMinLength, longMaxLength);
        this.debug = debug;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return ModTreeDecorators.DREAMS_HANGING_VINES.get();
    }

    @Override
    public void place(Context context) {
        RandomSource random = context.random();
        float roll = random.nextFloat();
        if (roll > this.treeVineChance) {
            debugLog("skip tree vines roll={} > chance={}", roll, this.treeVineChance);
            return;
        }

        List<StartPoint> candidates = collectExternalLeafStarts(context);
        if (candidates.isEmpty()) {
            debugLog("no external leaves found for hanging vines");
            return;
        }

        int targetColumns = randomRange(random, this.minColumns, this.maxColumns);
        Collections.shuffle(candidates, new java.util.Random(random.nextLong()));

        int columnsPlaced = 0;
        int totalLength = 0;
        for (StartPoint start : candidates) {
            if (columnsPlaced >= targetColumns) {
                break;
            }

            int desiredLength = pickLength(random);
            PlacementResult result = placeColumn(context, start, desiredLength);
            if (result.placed > 0) {
                columnsPlaced++;
                totalLength += result.placed;
                debugLog("column {} at {} len={} stop={}", columnsPlaced, start.pos, result.placed, result.stopReason);
            }
        }

        float avgLength = columnsPlaced == 0 ? 0.0F : (float) totalLength / columnsPlaced;
        debugLog("tree vines applied: roll={} chance={} externalLeaves={} targetColumns={} placedColumns={} avgLen={}",
                roll, this.treeVineChance, candidates.size(), targetColumns, columnsPlaced, avgLength);
    }

    private List<StartPoint> collectExternalLeafStarts(Context context) {
        List<StartPoint> starts = new ArrayList<>();
        for (BlockPos leafPos : context.leaves()) {
            BlockPos below = leafPos.below();
            if (!context.isAir(below)) {
                continue;
            }

            StartPoint start = pickStartPoint(context, leafPos, below);
            if (start != null) {
                starts.add(start);
            }
        }
        return starts;
    }

    private StartPoint pickStartPoint(Context context, BlockPos leafPos, BlockPos below) {
        List<Direction> attachDirections = new ArrayList<>(4);
        if (context.isAir(leafPos.west())) attachDirections.add(Direction.EAST);
        if (context.isAir(leafPos.east())) attachDirections.add(Direction.WEST);
        if (context.isAir(leafPos.north())) attachDirections.add(Direction.SOUTH);
        if (context.isAir(leafPos.south())) attachDirections.add(Direction.NORTH);

        if (attachDirections.isEmpty()) {
            return null;
        }

        Direction face = attachDirections.get(context.random().nextInt(attachDirections.size()));
        return new StartPoint(below, face);
    }

    private PlacementResult placeColumn(Context context, StartPoint start, int desiredLength) {
        BooleanProperty face = VineBlock.getPropertyForFace(start.face);
        BlockState state = Blocks.VINE.defaultBlockState().setValue(face, true);

        int placed = 0;
        BlockPos cursor = start.pos;
        StopReason stopReason = StopReason.REACHED_MAX;

        while (placed < desiredLength) {
            if (!context.isAir(cursor)) {
                stopReason = StopReason.BLOCKED;
                break;
            }

            context.setBlock(cursor, state);
            placed++;
            cursor = cursor.below();
        }

        return new PlacementResult(placed, stopReason);
    }

    private int pickLength(RandomSource random) {
        if (random.nextFloat() < this.longVinesChance) {
            return randomRange(random, this.longMinLength, this.longMaxLength);
        }
        return randomRange(random, this.minLength, this.maxLength);
    }

    private static int randomRange(RandomSource random, int min, int max) {
        return min >= max ? min : min + random.nextInt(max - min + 1);
    }

    private void debugLog(String message, Object... args) {
        if (this.debug || DreamsDimensions.LOGGER.isDebugEnabled()) {
            LOGGER.debug("[DreamsHangingVinesDecorator] " + message, args);
        }
    }

    private record StartPoint(BlockPos pos, Direction face) {
    }

    private record PlacementResult(int placed, StopReason stopReason) {
    }

    private enum StopReason {
        BLOCKED,
        REACHED_MAX
    }
}
