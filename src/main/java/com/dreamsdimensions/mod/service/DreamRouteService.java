package com.dreamsdimensions.mod.service;

import com.dreamsdimensions.mod.dimension.DreamDimensionCatalog;
import com.dreamsdimensions.mod.registry.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/** Rumo is active only when consumed; bounded periodic guidance to a marker. */
public final class DreamRouteService {
    private DreamRouteService(){}
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player)
                ||!player.hasEffect(ModEffects.ROUTE)
                ||DreamDimensionCatalog.find(player.level().dimension()).isEmpty()
                ||player.level().getGameTime()%100L!=0) return;
        ServerLevel world=(ServerLevel)player.level();
        OreSignalService.marker(world,player.blockPosition(),32).ifPresent(found->
                player.sendOverlayMessage(Component.translatable(
                        "message.dreamsdimensions.route.marker",
                        DreamRecoveryLocator.direction(player.blockPosition(),found))));
    }
}
