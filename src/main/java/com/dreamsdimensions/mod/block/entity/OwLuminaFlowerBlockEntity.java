package com.dreamsdimensions.mod.block.entity;

import com.dreamsdimensions.mod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * BlockEntity mínima para renderização emissiva client-side da Lumina Flower.
 */
public class OwLuminaFlowerBlockEntity extends BlockEntity {
    public OwLuminaFlowerBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.OW_LUMINA_FLOWER.get(), pos, blockState);
    }
}
