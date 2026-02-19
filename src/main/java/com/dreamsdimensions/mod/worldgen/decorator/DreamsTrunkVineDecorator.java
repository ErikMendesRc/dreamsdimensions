package com.dreamsdimensions.mod.worldgen.decorator;

import com.dreamsdimensions.mod.registry.ModBlocks;
import com.dreamsdimensions.mod.registry.ModTreeDecorators;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

public class DreamsTrunkVineDecorator extends TreeDecorator {
    public static final MapCodec<DreamsTrunkVineDecorator> CODEC = MapCodec.unit(DreamsTrunkVineDecorator::new);

    @Override
    protected TreeDecoratorType<?> type() {
        return ModTreeDecorators.DREAMS_TRUNK_VINE.get();
    }

    @Override
    public void place(Context context) {
        var random = context.random();
        context.logs().forEach(logPos -> {
            if (random.nextInt(3) > 0) tryPlace(context, logPos.west(), Direction.EAST);
            if (random.nextInt(3) > 0) tryPlace(context, logPos.east(), Direction.WEST);
            if (random.nextInt(3) > 0) tryPlace(context, logPos.north(), Direction.SOUTH);
            if (random.nextInt(3) > 0) tryPlace(context, logPos.south(), Direction.NORTH);
        });
    }

    private static void tryPlace(Context context, BlockPos pos, Direction faceDirection) {
        if (!context.isAir(pos)) return;

        BooleanProperty face = MultifaceBlock.getFaceProperty(faceDirection);
        BlockState state = ModBlocks.OW_LUMINA_VINES.get().defaultBlockState().setValue(face, true);
        context.setBlock(pos, state);
    }
}
