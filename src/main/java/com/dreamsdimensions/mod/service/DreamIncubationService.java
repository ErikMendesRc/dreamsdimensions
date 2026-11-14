package com.dreamsdimensions.mod.service;

import com.dreamsdimensions.mod.block.entity.DreamBedsideBlockEntity;
import com.dreamsdimensions.mod.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** Called after a completed night, never from an unattended global tick. */
public final class DreamIncubationService {
    private DreamIncubationService(){}

    public static void afterFullNight(ServerPlayer player,BlockPos bed) {
        if(DreamNecklaceEquipmentPolicy.isEquipped(player))return;
        if(!(player.level() instanceof ServerLevel level))return;
        // A true vanilla night skip lands around daybreak. Quitting a bed early
        // does not produce: 100 ticks sleeping alone is not a full night.
        long clock=Math.floorMod(level.getOverworldClockTime(),24000L);
        if(clock>1400L)return;
        long night=Math.floorDiv(level.getOverworldClockTime()-12000L,24000L);
        DreamIncubationEnvironment.Kind room=DreamIncubationEnvironment.detect(level,bed);
        for(BlockPos pos:BlockPos.betweenClosed(bed.offset(-2,-1,-2),bed.offset(2,1,2))){
            if(pos.distManhattan(bed)>2||!level.hasChunkAt(pos)
                    ||!level.getBlockState(pos).is(ModBlocks.DREAM_BEDSIDE.get()))continue;
            if(!(level.getBlockEntity(pos) instanceof DreamBedsideBlockEntity bedside))continue;
            if(bedside.incubate(player,night,room,
                    DreamIncubationEnvironment.dreamcatcher(level,bed))) {
                player.sendSystemMessage(Component.translatable(
                        "message.dreamsdimensions.bedside.completed",
                        room.name().toLowerCase(java.util.Locale.ROOT)));
            }else if(room==DreamIncubationEnvironment.Kind.AMBIGUOUS)
                player.sendSystemMessage(Component.translatable("message.dreamsdimensions.bedside.ambiguous"));
        }
    }
}
