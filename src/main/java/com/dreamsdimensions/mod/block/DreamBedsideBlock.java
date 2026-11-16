package com.dreamsdimensions.mod.block;

import com.dreamsdimensions.mod.block.entity.DreamBedsideBlockEntity;
import com.dreamsdimensions.mod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

/** Opens the dedicated server-authoritative incubation menu for every interaction. */
public final class DreamBedsideBlock extends BaseEntityBlock {
    public DreamBedsideBlock(Properties properties) { super(properties); }

    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DreamBedsideBlockEntity(pos, state);
    }

    @Override protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    private InteractionResult open(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide() && player instanceof ServerPlayer server
                && level.getBlockEntity(pos) instanceof DreamBedsideBlockEntity bedside) {
            server.openMenu(bedside);
        }
        return InteractionResult.SUCCESS;
    }

    @Override protected InteractionResult useItemOn(ItemStack stack, BlockState state,
            Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return open(level, pos, player);
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level,
            BlockPos pos, Player player, BlockHitResult hit) {
        return open(level, pos, player);
    }
}
