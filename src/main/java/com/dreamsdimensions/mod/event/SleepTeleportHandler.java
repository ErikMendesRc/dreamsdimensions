package com.dreamsdimensions.mod.event;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.service.DreamTeleportService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Thin server-side sleep event adapter; preserves native 100 ticks and
 * wake-before-post timing. Transient state is per UUID, never ServerPlayer.
 */
public final class SleepTeleportHandler {
    public static final int TICKS_BEFORE_TELEPORT = 100;
    private static final Set<UUID> ATTEMPTED_SLEEP = new HashSet<>();
    private static final Set<UUID> TELEPORTING = new HashSet<>();
    private static final Map<UUID, BedOrigin> COMPLETED_WAKEUPS = new HashMap<>();

    private record BedOrigin(BlockPos pos, float yaw) {}

    private SleepTeleportHandler() {}

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        UUID uuid = player.getUUID();

        BedOrigin completedWakeup = COMPLETED_WAKEUPS.remove(uuid);
        if (completedWakeup != null) {
            ATTEMPTED_SLEEP.remove(uuid);
            if (player.isAlive() && player.level().dimension() == Level.OVERWORLD && !player.isSleeping()) {
                teleportOnce(player, completedWakeup);
            }
            return;
        }

        if (!player.isAlive() || player.level().dimension() != Level.OVERWORLD || !player.isSleeping()) {
            ATTEMPTED_SLEEP.remove(uuid);
            return;
        }
        if (player.getSleepTimer() < TICKS_BEFORE_TELEPORT || !ATTEMPTED_SLEEP.add(uuid)) return;

        Optional<BlockPos> bed = player.getSleepingPos();
        if (bed.isEmpty()) {
            DreamsDimensions.LOGGER.warn("Sono completo, mas a cama de {} não está disponível.", uuid);
            return;
        }
        teleportOnce(player, new BedOrigin(bed.get(), player.getYRot()));
    }

    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !player.isAlive() || player.level().dimension() != Level.OVERWORLD) return;

        UUID uuid = player.getUUID();
        if (TELEPORTING.contains(uuid) || ATTEMPTED_SLEEP.contains(uuid)
                || player.getSleepTimer() < TICKS_BEFORE_TELEPORT) return;
        player.getSleepingPos().ifPresent(pos ->
                COMPLETED_WAKEUPS.put(uuid, new BedOrigin(pos, player.getYRot())));
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        UUID uuid = event.getEntity().getUUID();
        ATTEMPTED_SLEEP.remove(uuid);
        TELEPORTING.remove(uuid);
        COMPLETED_WAKEUPS.remove(uuid);
    }

    private static void teleportOnce(ServerPlayer player, BedOrigin origin) {
        UUID uuid = player.getUUID();
        if (!TELEPORTING.add(uuid)) return;
        try {
            DreamTeleportService.enterDream(player, origin.pos(), origin.yaw());
        } finally {
            TELEPORTING.remove(uuid);
        }
    }
}
