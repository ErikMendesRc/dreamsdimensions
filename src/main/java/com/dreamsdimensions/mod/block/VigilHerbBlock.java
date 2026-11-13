package com.dreamsdimensions.mod.block;

import com.dreamsdimensions.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Overworld-only herb; wild patches start mature, placed seeds start young.
 * Harvesting resets growth: daytime grants leaves, nighttime only a seed.
 * Bone meal is deliberately unsupported.
 */
public final class VigilHerbBlock extends BushBlock {
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    public VigilHerbBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(OPEN, true).setValue(AGE, 3));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(AGE, 0)
                .setValue(OPEN, context.getLevel().isDay());
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT) || state.is(Blocks.PODZOL)
                || state.is(Blocks.FARMLAND) || state.is(Blocks.ROOTED_DIRT);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(OPEN, AGE);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        boolean open = level.isDay() && level.canSeeSky(pos);
        if (state.getValue(OPEN) != open) {
            level.setBlock(pos, state.setValue(OPEN, open), 2);
            state = state.setValue(OPEN, open);
        }
        if (open && state.getValue(AGE) < 3 && random.nextInt(4) == 0) {
            level.setBlock(pos, state.setValue(AGE, state.getValue(AGE) + 1), 2);
        }
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        if (state.getValue(AGE) < 3) return InteractionResult.PASS;
        if (!level.isClientSide()) {
            boolean day = level.isDay() && level.canSeeSky(pos);
            popResource(level, pos, new ItemStack(ModItems.VIGIL_HERB_SEEDS.get()));
            if (day) {
                popResource(level, pos, new ItemStack(ModItems.VIGIL_HERB_LEAF.get(), 1 + level.random.nextInt(2)));
            }
            level.setBlock(pos, state.setValue(AGE, 0).setValue(OPEN, day), 3);
        }
        return InteractionResult.SUCCESS;
    }
}
