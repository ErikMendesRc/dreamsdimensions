package com.dreamsdimensions.mod.block;

import com.dreamsdimensions.mod.block.entity.OneiricKettleBlockEntity;
import com.dreamsdimensions.mod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.BlockHitResult;

/** Copper kettle; accepts water potions, one toasted-herb base, a reagent. */
public final class OneiricKettleBlock extends BaseEntityBlock {
    public OneiricKettleBlock(Properties props){super(props);}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){
        return new OneiricKettleBlockEntity(pos,state);
    }
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,
            BlockState state,BlockEntityType<T> type) {
        if(level.isClientSide())return null;
        return createTickerHelper(type,ModBlockEntities.KETTLE.get(),
                OneiricKettleBlockEntity::serverTick);
    }
    @Override protected InteractionResult useItemOn(ItemStack stack,BlockState state,
            Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof OneiricKettleBlockEntity kettle))return InteractionResult.FAIL;
        if(stack.isEmpty())return InteractionResult.TRY_WITH_EMPTY_HAND;
        if(level.isClientSide())return InteractionResult.SUCCESS;
        if(!OneiricKettleBlockEntity.onLitCampfire(level,pos)){
            player.sendSystemMessage(Component.translatable("message.dreamsdimensions.kettle.needs_fire"));
            return InteractionResult.SUCCESS;
        }
        if(kettle.addIngredient(stack)) {
            if(!player.getAbilities().instabuild) stack.shrink(1);
            player.sendSystemMessage(Component.translatable("message.dreamsdimensions.kettle.loaded",
                    kettle.getBottles(), kettle.getProgress()));
        } else {
            player.sendSystemMessage(Component.translatable("message.dreamsdimensions.kettle.invalid"));
        }
        return InteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level,
            BlockPos pos, Player player,BlockHitResult hit) {
        if(!(level.getBlockEntity(pos) instanceof OneiricKettleBlockEntity kettle))return InteractionResult.FAIL;
        if(!level.isClientSide()) {
            ItemStack finished=kettle.removeOne();
            if(!finished.isEmpty()) {
                if(!player.getInventory().add(finished))player.drop(finished,false);
            } else {
                player.sendSystemMessage(Component.translatable("message.dreamsdimensions.kettle.status",
                        kettle.getBottles(),kettle.getProgress()));
            }
        }
        return InteractionResult.SUCCESS;
    }
    @Override protected void onRemove(BlockState state,Level level,BlockPos pos,
            BlockState replacement,boolean movedByPiston) {
        if(!state.is(replacement.getBlock()) && !level.isClientSide()
                && level.getBlockEntity(pos) instanceof OneiricKettleBlockEntity kettle) {
            kettle.dropStored(level,pos);
        }
        super.onRemove(state,level,pos,replacement,movedByPiston);
    }
}
