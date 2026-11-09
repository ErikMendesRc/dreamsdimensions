package com.dreamsdimensions.mod.client.atmosphere;

import com.dreamsdimensions.mod.DreamsDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Physical-client-only event subscriber; no server bootstrap references. */
@EventBusSubscriber(modid = DreamsDimensions.MODID, value = Dist.CLIENT)
public final class AtmosphereClientController {
    private static final AtmosphereProfile PROFILE = AtmosphereProfile.BLUE_FIELD;
    private static ClientLevel lastLevel;
    private static long activeTicks;

    private AtmosphereClientController() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post ignored) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        if (level == null || client.player == null
                || !PROFILE.dimension().equals(level.dimension())) {
            reset();
            return;
        }
        if (lastLevel != level) {
            reset();
            lastLevel = level;
        }
        if (client.isPaused()) return;
        activeTicks++;
        AtmosphereParticles.tick(client, level, client.player, PROFILE, activeTicks);
        AtmosphereAudio.tick(level, client.player, PROFILE, activeTicks);
    }

    private static void reset() {
        lastLevel = null;
        activeTicks = 0;
        AtmosphereAudio.reset();
    }
}
