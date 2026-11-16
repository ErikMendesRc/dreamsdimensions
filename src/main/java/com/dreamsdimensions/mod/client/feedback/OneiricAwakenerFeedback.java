package com.dreamsdimensions.mod.client.feedback;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.config.DreamsConfig;
import com.dreamsdimensions.mod.registry.ModItems;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.world.InteractionHand;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/**
 * Task 8.5 — local, optional Awakener channeling feedback.
 *
 * Only the client player's currently-used Awakener is observed, once per
 * client tick; no packet, no chunk search, no server-side particles and no
 * effect outside a registered dream dimension. MINIMAL disables completely.
 * The visual progress NEVER participates in 60-tick return validation.
 */
@EventBusSubscriber(modid = DreamsDimensions.MODID, value = Dist.CLIENT)
public final class OneiricAwakenerFeedback {
    private static final DustParticleOptions BEGIN = new DustParticleOptions(0x79D3DF, 0.75F);
    private static final DustParticleOptions FINISH = new DustParticleOptions(0x9E92DB, 0.8F);
    private static ClientLevel lastLevel;
    private static UUID lastPlayer;
    private static long useTicks;

    private OneiricAwakenerFeedback() {}

    @SubscribeEvent
    public static void onTick(ClientTickEvent.Post event) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        LocalPlayer player = client.player;
        if (level == null || player == null
                || !player.isAlive() || player.isSpectator()
                || !DreamsConfig.isDreamDimension(level.dimension())
                || !player.isUsingItem()
                || !player.getUseItem().is(ModItems.ONEIRIC_AWAKENER.get())) {
            reset();
            return;
        }
        if (lastLevel != level || !player.getUUID().equals(lastPlayer)) {
            lastLevel = level;
            lastPlayer = player.getUUID();
            useTicks = 0;
        }
        if (client.isPaused()) return;

        useTicks++;
        ParticleStatus quality = client.options.particles().get();
        int count = OneiricFeedbackPacing.particleCount(useTicks,
                quality == ParticleStatus.MINIMAL, quality == ParticleStatus.DECREASED);
        if (count == 0) return;

        // Visible beside the initiating hand in first person, rather than a
        // distracting screen-wide aura or a random sparkle over other players.
        double yaw = Math.toRadians(player.getYRot());
        double hand = player.getUsedItemHand() == InteractionHand.OFF_HAND ? -1.0 : 1.0;
        double forwardX = -Math.sin(yaw), forwardZ = Math.cos(yaw);
        double rightX = Math.cos(yaw), rightZ = Math.sin(yaw);
        double originX = player.getX() + forwardX * 0.48 + rightX * hand * 0.32;
        double originY = player.getEyeY() - 0.70;
        double originZ = player.getZ() + forwardZ * 0.48 + rightZ * hand * 0.32;

        for (int i = 0; i < count; i++) {
            double angle = useTicks * 0.32 + i * Math.PI;
            double x = originX + rightX * Math.cos(angle) * 0.15;
            double y = originY + Math.sin(angle) * 0.10;
            double z = originZ + rightZ * Math.cos(angle) * 0.15;
            BlockPos pos = BlockPos.containing(x, y, z);
            if (!level.isInsideBuildHeight(pos) || !level.hasChunkAt(pos)
                    || !level.getFluidState(pos).isEmpty()) continue;
            level.addParticle(useTicks >= 48 && i == 1 ? ParticleTypes.END_ROD
                            : (useTicks >= 42 ? FINISH : BEGIN),
                    x, y, z, 0, 0.004, 0);
        }
    }

    private static void reset() {
        lastLevel = null;
        lastPlayer = null;
        useTicks = 0;
    }
}
