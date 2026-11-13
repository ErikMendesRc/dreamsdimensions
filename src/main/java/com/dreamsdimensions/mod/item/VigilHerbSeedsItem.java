package com.dreamsdimensions.mod.item;

import com.dreamsdimensions.mod.block.VigilHerbBlock;
import com.dreamsdimensions.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Seeds always place a young, non-glowing herb. The block item separately
 * places an adult specimen for decoration and artist testing.
 */
public final class VigilHerbSeedsItem extends Item {
    public VigilHerbSeedsItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        if (!level.isEmptyBlock(pos) || !level.canSeeSky(pos)) {
            return InteractionResult.FAIL;
        }

        BlockState state = ModBlocks.VIGIL_HERB.get().defaultBlockState()
                .setValue(VigilHerbBlock.AGE, 0)
                .setValue(VigilHerbBlock.OPEN, false);
        if (!state.canSurvive(level, pos)) return InteractionResult.FAIL;

        if (!level.isClientSide()) {
            level.setBlock(pos, state, 3);
            if (context.getPlayer() == null
                    || !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
