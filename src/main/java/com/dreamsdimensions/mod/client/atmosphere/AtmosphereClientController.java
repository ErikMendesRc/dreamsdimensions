package com.dreamsdimensions.mod.client.atmosphere;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.dimension.DreamDimensionCatalog;
import com.dreamsdimensions.mod.dimension.DreamDimensionDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Client-only profile dispatcher; nothing here is loaded by the common bootstrap. */
@EventBusSubscriber(modid = DreamsDimensions.MODID, value = Dist.CLIENT)
public final class AtmosphereClientController {
    private static ClientLevel lastLevel;
    private static AtmosphereProfile lastProfile;
    private static long activeTicks;

    private AtmosphereClientController() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post ignored) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        AtmosphereProfile profile = level == null ? null
                : DreamDimensionCatalog.find(level.dimension())
                        .flatMap(DreamDimensionDefinition::atmosphereId)
                        .flatMap(AtmosphereProfile::find)
                        .filter(candidate -> candidate.dimension().equals(level.dimension()))
                        .orElse(null);

        if (level == null || client.player == null || profile == null) {
            reset();
            return;
        }
        if (lastLevel != level || lastProfile != profile) {
            reset();
            lastLevel = level;
            lastProfile = profile;
            DreamsDimensions.LOGGER.info("Campo Onírico Azul: atmosfera cliente ativa (partículas e áudio).");
        }
        if (client.isPaused()) return;

        activeTicks++;
        AtmosphereParticles.tick(client, level, client.player, profile, activeTicks);
        AtmosphereAudio.tick(level, client.player, profile, activeTicks);
    }

    private static void reset() {
        lastLevel = null;
        lastProfile = null;
        activeTicks = 0;
        AtmosphereAudio.reset();
    }
}
