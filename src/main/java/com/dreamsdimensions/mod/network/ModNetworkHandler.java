// ModNetworkHandler.java
package com.dreamsdimensions.mod.network;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.event.SleepTeleportHandler;
import com.dreamsdimensions.mod.client.screen.DreamTransitionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

@EventBusSubscriber(modid = DreamsDimensions.MODID,
        bus = EventBusSubscriber.Bus.MOD)
public class ModNetworkHandler {
    private static final Logger LOGGER = DreamsDimensions.LOGGER;

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1.0");

        // servidor → cliente
        registrar.playToClient(
                MyPacket.TYPE, MyPacket.CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    LOGGER.info("Cliente recebeu MyPacket, abrindo DreamTransitionScreen");
                    Minecraft.getInstance().setScreen(new DreamTransitionScreen());
                })
        );

        // cliente → servidor
        registrar.playToServer(
                ReadyToTeleportPacket.TYPE, ReadyToTeleportPacket.CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    ServerPlayer player = (ServerPlayer) ctx.player();
                    LOGGER.info("ReadyToTeleportPacket recebido de {}", player.getGameProfile().getName());
                    SleepTeleportHandler.actuallyTeleport(player);
                })
        );
    }
}