package com.dreamsdimensions.mod.item;

import com.dreamsdimensions.mod.dimension.DreamDimensionCatalog;
import com.dreamsdimensions.mod.service.OreSignalService;
import com.dreamsdimensions.mod.service.DreamRecoveryLocator;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import java.util.Optional;

/** Explicit 16-use scanner; no passive timers, no chunk loading. */
public final class ResonanceProbeItem extends Item {
    public ResonanceProbeItem(Properties properties){super(properties);}

    @Override public InteractionResult use(Level level,Player player,InteractionHand hand){
        if(!(player instanceof ServerPlayer server))return InteractionResult.CONSUME;
        if(!(level instanceof ServerLevel world))return InteractionResult.FAIL;
        BlockPos from=server.blockPosition();
        Optional<BlockPos> target=Optional.empty();
        if(level.dimension()==Level.OVERWORLD)
            target=OreSignalService.find(world,from,24);
        else if(DreamDimensionCatalog.find(level.dimension()).isPresent()) {
            target=OreSignalService.marker(world,from,32);
            if(target.isEmpty())target=DreamRecoveryLocator.find(world,from);
        }else return InteractionResult.FAIL;

        if(target.isPresent()) {
            BlockPos found=target.get();
            server.sendSystemMessage(Component.translatable("message.dreamsdimensions.probe.found",
                    DreamRecoveryLocator.direction(from,found),
                    Math.round(Math.sqrt(from.distSqr(found)))));
        }else server.sendSystemMessage(Component.translatable("message.dreamsdimensions.probe.empty"));
        ItemStack stack=server.getItemInHand(hand);
        if(!server.getAbilities().instabuild) {
            int next=stack.getDamageValue()+1;
            if(next>=stack.getMaxDamage())stack.shrink(1);
            else stack.setDamageValue(next);
        }
        return InteractionResult.CONSUME;
    }
}
