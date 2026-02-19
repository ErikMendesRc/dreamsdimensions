package com.dreamsdimensions.mod.worldgen.decorator;

import com.dreamsdimensions.mod.registry.ModBlocks;
import com.dreamsdimensions.mod.registry.ModTreeDecorators;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class DreamsLeaveVineDecorator extends TreeDecorator {
    public static final MapCodec<DreamsLeaveVineDecorator> CODEC = Codec.floatRange(0.0F, 1.0F)
            .fieldOf("probability")
            .xmap(DreamsLeaveVineDecorator::new, decorator -> decorator.probability);

    private final float probability;

    public DreamsLeaveVineDecorator(float probability) {
        this.probability = probability;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return ModTreeDecorators.DREAMS_LEAVE_VINE.get();
    }

    @Override
    public void place(Context context) {
        var random = context.random();
        context.leaves().forEach(leafPos -> {
            if (random.nextFloat() < this.probability) tryPlaceHanging(context, leafPos.west(), Direction.EAST);
            if (random.nextFloat() < this.probability) tryPlaceHanging(context, leafPos.east(), Direction.WEST);
            if (random.nextFloat() < this.probability) tryPlaceHanging(context, leafPos.north(), Direction.SOUTH);
            if (random.nextFloat() < this.probability) tryPlaceHanging(context, leafPos.south(), Direction.NORTH);
        });
    }

    private static void tryPlaceHanging(Context context, BlockPos pos, Direction faceDirection) {
        if (!context.isAir(pos)) return;

        BooleanProperty face = MultifaceBlock.getFaceProperty(faceDirection);
        BlockState state = ModBlocks.OW_LUMINA_VINES.get().defaultBlockState().setValue(face, true);

        context.setBlock(pos, state);

        int maxDepth = 4;
        BlockPos belowPos = pos.below();
        while (context.isAir(belowPos) && maxDepth-- > 0) {
            context.setBlock(belowPos, state);
            belowPos = belowPos.below();
        }
    }
}
