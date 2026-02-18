package com.dreamsdimensions.mod.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.GlowLichenBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Trepadeira luminosa baseada em multiface (estilo glow lichen) para grudar em faces de troncos e folhas.
 */
public class OwLuminaVinesBlock extends GlowLichenBlock {
    public OwLuminaVinesBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }
}
