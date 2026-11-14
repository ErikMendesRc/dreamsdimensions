package com.dreamsdimensions.mod.block;

import com.dreamsdimensions.mod.block.entity.DreamBedsideBlockEntity;
import com.dreamsdimensions.mod.registry.ModBlockEntities;
import com.dreamsdimensions.mod.service.DreamIncubationEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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

/** Bedside cabinet: use input in hand to load; empty hand collects or inspects. */
public final class DreamBedsideBlock extends BaseEntityBlock {
    public DreamBedsideBlock(Properties p){super(p);}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){
        return new DreamBedsideBlockEntity(pos,state);
    }
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override protected InteractionResult useItemOn(ItemStack item,BlockState state,Level level,
            BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit) {
        if(DreamBedsideBlockEntity.typeOf(item)==0)return InteractionResult.TRY_WITH_EMPTY_HAND;
        if(!(player instanceof ServerPlayer server))return InteractionResult.SUCCESS;
        if(!(level.getBlockEntity(pos) instanceof DreamBedsideBlockEntity bedside))return InteractionResult.FAIL;
        if(!DreamIncubationEnvironment.bedsideAdjacent(level,pos)){
            server.sendSystemMessage(Component.translatable("message.dreamsdimensions.bedside.no_bed"));
            return InteractionResult.SUCCESS;
        }
        int capacity=2;
        for(BlockPos bed:BlockPos.betweenClosed(pos.offset(-2,-1,-2),pos.offset(2,1,2))) {
            if(bed.distManhattan(pos)>2)continue;
            if(level.getBlockState(bed).getBlock() instanceof net.minecraft.world.level.block.BedBlock
                    &&DreamIncubationEnvironment.dreamcatcher(level,bed)) {
                capacity=3;break;
            }
        }
        boolean loaded=bedside.load(server,item,capacity);
        server.sendSystemMessage(Component.translatable(
                loaded?"message.dreamsdimensions.bedside.loaded":"message.dreamsdimensions.bedside.invalid",
                bedside.pending(),capacity));
        return InteractionResult.SUCCESS;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,
            Player player,BlockHitResult hit) {
        if(!(player instanceof ServerPlayer server))return InteractionResult.SUCCESS;
        if(!(level.getBlockEntity(pos) instanceof DreamBedsideBlockEntity bedside))return InteractionResult.FAIL;
        ItemStack result=bedside.take(server);
        if(!result.isEmpty()){
            if(!server.getInventory().add(result))
                net.minecraft.world.level.block.Block.popResource(level,pos,result);
            server.sendSystemMessage(Component.translatable("message.dreamsdimensions.bedside.collected"));
        }else server.sendSystemMessage(Component.translatable(
                "message.dreamsdimensions.bedside.status",
                bedside.pending(),bedside.finished()));
        return InteractionResult.SUCCESS;
    }
}
