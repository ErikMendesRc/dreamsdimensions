package com.dreamsdimensions.mod.block;

import com.dreamsdimensions.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Native Overworld plant. Mature flowers unfold at night, closing again at
 * dawn; sunlight grows seedlings, and daytime harvesting still gives leaves.
 *
 * A sparse scheduled tick (1–3 seconds, staggered by position) updates a
 * mature plant after /time changes, even when randomTickSpeed=0. Random ticks
 * are still the growth mechanic and bootstrap old-world mature plants.
 * Particles are client-side only: no player scanning, entity or per-tick hook.
 */
public final class VigilHerbBlock extends BushBlock {
    public static final BooleanProperty OPEN = BooleanProperty.create("open");
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    private static final DustParticleOptions SPORE =
            new DustParticleOptions(0x82D4CB, 0.55F);

    public VigilHerbBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(OPEN, false).setValue(AGE, 3));
    }

    /** Vanilla block-light emission: only the mature, unfolded flower glows. */
    public static int lightLevel(BlockState state) {
        return state.getValue(AGE) == VigilHerbBloomPolicy.MATURE_AGE
                && state.getValue(OPEN) ? 8 : 0;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // The actual block item is for decoration/testing and places a mature
        // specimen. VigilHerbSeedsItem independently creates AGE=0 plants.
        return defaultBlockState().setValue(AGE, VigilHerbBloomPolicy.MATURE_AGE)
                .setValue(OPEN, VigilHerbBloomPolicy.shouldOpen(
                        context.getLevel().getOverworldClockTime(),
                        VigilHerbBloomPolicy.MATURE_AGE));
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

    private void queueBloomCheck(ServerLevel level, BlockPos pos) {
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, 20 + Math.floorMod(pos.hashCode(), 40));
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos,
            BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level instanceof ServerLevel server
                && state.getValue(AGE) == VigilHerbBloomPolicy.MATURE_AGE) {
            queueBloomCheck(server, pos);
        }
    }

    private BlockState syncBloom(ServerLevel level, BlockPos pos, BlockState state) {
        boolean shouldOpen = VigilHerbBloomPolicy.shouldOpen(
                level.getOverworldClockTime(), state.getValue(AGE));
        if (state.getValue(OPEN) != shouldOpen) {
            BlockState updated = state.setValue(OPEN, shouldOpen);
            // Client receives model change; lighting engine receives light update.
            level.setBlock(pos, updated, 3);
            return updated;
        }
        return state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos,
            RandomSource random) {
        if (state.getValue(AGE) != VigilHerbBloomPolicy.MATURE_AGE) {
            return;
        }
        syncBloom(level, pos, state);
        // Re-arm only when the plant is mature. No per-tick global listener.
        queueBloomCheck(level, pos);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        if (age < VigilHerbBloomPolicy.MATURE_AGE
                && VigilHerbBloomPolicy.canGrow(level.getOverworldClockTime(),
                        level.canSeeSky(pos))
                && random.nextInt(4) == 0) {
            age++;
            state = state.setValue(AGE, age).setValue(OPEN, false);
            level.setBlock(pos, state, 3);
        }
        if (age == VigilHerbBloomPolicy.MATURE_AGE) {
            // Also repairs pre-9.2.2 saved plants that had no scheduled tick.
            syncBloom(level, pos, state);
            queueBloomCheck(level, pos);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos,
            RandomSource random) {
        if (state.getValue(AGE) != VigilHerbBloomPolicy.MATURE_AGE
                || !state.getValue(OPEN)
                || !VigilHerbBloomPolicy.isNight(level.getOverworldClockTime())
                || random.nextInt(9) != 0) {
            return;
        }
        double x = pos.getX() + 0.35 + random.nextDouble() * 0.30;
        double y = pos.getY() + 0.56 + random.nextDouble() * 0.33;
        double z = pos.getZ() + 0.35 + random.nextDouble() * 0.30;
        level.addParticle(SPORE, x, y, z,
                (random.nextDouble() - 0.5) * 0.004,
                0.008 + random.nextDouble() * 0.007,
                (random.nextDouble() - 0.5) * 0.004);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
            Player player, BlockHitResult hit) {
        if (state.getValue(AGE) < VigilHerbBloomPolicy.MATURE_AGE) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            // Preserve Task 9.2 harvest: leaves in daylight, seeds at night.
            boolean day = !VigilHerbBloomPolicy.isNight(level.getOverworldClockTime())
                    && level.canSeeSky(pos);
            popResource(level, pos, new ItemStack(ModItems.VIGIL_HERB_SEEDS.get()));
            if (day) {
                popResource(level, pos, new ItemStack(ModItems.VIGIL_HERB_LEAF.get(),
                        1 + level.getRandom().nextInt(2)));
            }
            level.setBlock(pos, state.setValue(AGE, 0).setValue(OPEN, false), 3);
        }
        return InteractionResult.SUCCESS;
    }
}
