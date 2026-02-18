package com.dreamsdimensions.mod.block;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.content.emissive.NightEmissiveBlockBase;
import com.dreamsdimensions.mod.content.emissive.NightEmissiveDebug;
import com.dreamsdimensions.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireflyBushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * Somniflora do Overworld com regras explícitas de solo para worldgen e colocação manual.
 */
public class OwSomnifloraBlock extends FireflyBushBlock implements NightEmissiveBlockBase {
    private static final TagKey<net.minecraft.world.level.block.Block> LUMINA_SOIL = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "lumina_soil")
    );

    public OwSomnifloraBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(defaultNightState(this.stateDefinition.any()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        appendNightEmissiveProperties(builder);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT)
                || state.is(LUMINA_SOIL)
                || state.is(ModBlocks.OW_DREAM_GLOW_MOSS.get());
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!oldState.is(this) && !level.isClientSide()) {
            scheduleInitial((ServerLevel) level, pos, state, this);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        NightEmissiveDebug.logRandomTick(level, pos, state, this);
        scheduledTick(state, level, pos, random, this);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        scheduledTick(state, level, pos, random, this);
    }
}
