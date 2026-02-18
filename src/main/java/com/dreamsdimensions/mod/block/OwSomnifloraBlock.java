package com.dreamsdimensions.mod.block;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.FireflyBushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Somniflora do Overworld com regras explícitas de solo para worldgen e colocação manual.
 */
public class OwSomnifloraBlock extends FireflyBushBlock {
    private static final TagKey<net.minecraft.world.level.block.Block> LUMINA_SOIL = TagKey.create(
            Registries.BLOCK,
            Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "lumina_soil")
    );

    public OwSomnifloraBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT)
                || state.is(LUMINA_SOIL)
                || state.is(ModBlocks.OW_DREAM_GLOW_MOSS.get());
    }
}
