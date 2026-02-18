package com.dreamsdimensions.mod.client.event;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.content.emissive.NightEmissiveDebug;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = DreamsDimensions.MODID, value = Dist.CLIENT)
public final class NightEmissiveClientDebugEvents {
    private NightEmissiveClientDebugEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        NightEmissiveDebug.logClientProbe(net.minecraft.client.Minecraft.getInstance());
    }
}
