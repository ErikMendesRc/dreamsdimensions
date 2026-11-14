package com.dreamsdimensions.mod.service;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** Passive clue only after sleeping without Necklace near unmined ore. */
public final class DreamMineralSleepService {
    private DreamMineralSleepService(){}

    public static void afterFullNight(ServerPlayer player,BlockPos bed) {
        if(DreamNecklaceEquipmentPolicy.isEquipped(player)
                ||!(player.level() instanceof ServerLevel level))return;
        if(Math.floorMod(level.getOverworldClockTime(),24000L)>1400L)return;
        OreSignalService.find(level,bed,8).ifPresent(ore->
            player.sendSystemMessage(Component.translatable(
                    "message.dreamsdimensions.mineral_dream",
                    DreamRecoveryLocator.direction(bed,ore))));
    }
}
