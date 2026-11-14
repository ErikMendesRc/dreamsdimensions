package com.dreamsdimensions.mod.item;

import com.dreamsdimensions.mod.service.DreamTetherService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/** Arms one 120-second single-use void rescue from firm ground. */
public final class VigilTetherItem extends Item {
    public VigilTetherItem(Properties properties) {super(properties);}
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!(player instanceof ServerPlayer server))return InteractionResult.CONSUME;
        if (!DreamTetherService.arm(server)) {
            server.sendSystemMessage(Component.translatable(
                    "message.dreamsdimensions.vigil_tether.unavailable"));
            return InteractionResult.FAIL;
        }
        if (!server.getAbilities().instabuild)server.getItemInHand(hand).shrink(1);
        server.sendSystemMessage(Component.translatable(
                "message.dreamsdimensions.vigil_tether.armed"));
        return InteractionResult.CONSUME;
    }
}
