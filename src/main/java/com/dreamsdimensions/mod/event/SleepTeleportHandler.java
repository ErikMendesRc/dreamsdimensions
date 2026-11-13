package com.dreamsdimensions.mod.event;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.config.DreamEntryConfig;
import com.dreamsdimensions.mod.config.DreamsConfig;
import com.dreamsdimensions.mod.service.DreamEntryPolicy;
import com.dreamsdimensions.mod.service.DreamHangoverService;
import com.dreamsdimensions.mod.registry.ModItems;
import net.minecraft.network.chat.Component;
import com.dreamsdimensions.mod.service.DreamNecklaceEquipmentPolicy;
import com.dreamsdimensions.mod.service.DreamSleepCycle;
import com.dreamsdimensions.mod.service.DreamTeleportService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Server-side sleep adapter: tick/wake callbacks share one cycle and one roll.
 * Existing safe arrival and weighted dimension selection remain unchanged.
 */
public final class SleepTeleportHandler {
    public static final int TICKS_BEFORE_TELEPORT = 100;
    private static final DreamSleepCycle CYCLES = new DreamSleepCycle();
    private static final Set<UUID> TELEPORTING = new HashSet<>();
    private static final Set<UUID> WARNED = new HashSet<>();
    private static final Map<UUID, BedOrigin> COMPLETED_WAKEUPS = new HashMap<>();

    private record BedOrigin(BlockPos pos, float yaw) {}

    private SleepTeleportHandler() {}

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        UUID uuid = player.getUUID();

        BedOrigin completedWakeup = COMPLETED_WAKEUPS.remove(uuid);
        if (completedWakeup != null) {
            CYCLES.onAwake(uuid);
            if (player.isAlive() && player.level().dimension() == Level.OVERWORLD && !player.isSleeping()) {
                attemptEntry(player, completedWakeup);
            }
            return;
        }

        if (!player.isAlive() || player.level().dimension() != Level.OVERWORLD || !player.isSleeping()) {
            CYCLES.onAwake(uuid);
            WARNED.remove(uuid);
            return;
        }

        // Warn once per genuine sleeping interval, not on every player tick.
        if (DreamEntryConfig.enabled() && WARNED.add(uuid)
                && DreamNecklaceEquipmentPolicy.isEquipped(player)) {
            boolean hasReturn = player.getInventory().getItems().stream()
                    .anyMatch(stack -> stack.is(ModItems.ONEIRIC_AWAKENER.get())
                            || stack.is(ModItems.CRACKED_AWAKENER.get()))
                    || player.getOffhandItem().is(ModItems.ONEIRIC_AWAKENER.get())
                    || player.getOffhandItem().is(ModItems.CRACKED_AWAKENER.get());
            if (!hasReturn) {
                player.sendSystemMessage(Component.translatable(
                        "message.dreamsdimensions.sleep.risk"));
            }
        }

        if (!CYCLES.onSleeping(uuid, player.getSleepTimer(), TICKS_BEFORE_TELEPORT)) return;

        Optional<BlockPos> bed = player.getSleepingPos();
        if (bed.isEmpty()) {
            DreamsDimensions.LOGGER.warn("Sono completo sem cama válida para {}", uuid);
            return;
        }
        attemptEntry(player, new BedOrigin(bed.get(), player.getYRot()));
    }

    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !player.isAlive() || player.level().dimension() != Level.OVERWORLD) return;

        UUID uuid = player.getUUID();
        if (TELEPORTING.contains(uuid)) return;
        if (!CYCLES.onWake(uuid, player.getSleepTimer(), TICKS_BEFORE_TELEPORT)) return;

        // Consumes this cycle BEFORE queueing: duplicate wake callbacks cannot reroll.
        player.getSleepingPos().ifPresent(pos ->
                COMPLETED_WAKEUPS.put(uuid, new BedOrigin(pos, player.getYRot())));
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        forget(event.getEntity().getUUID());
    }

    public static void onPlayerClone(PlayerEvent.Clone event) {
        forget(event.getEntity().getUUID());
    }

    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        forget(event.getEntity().getUUID());
    }

    private static void forget(UUID uuid) {
        CYCLES.forget(uuid);
        TELEPORTING.remove(uuid);
        COMPLETED_WAKEUPS.remove(uuid);
        WARNED.remove(uuid);
    }

    private static void attemptEntry(ServerPlayer player, BedOrigin origin) {
        UUID uuid = player.getUUID();
        if (!TELEPORTING.add(uuid)) return;
        try {
            // Disabled by default: no Curios / RNG / destination work in normal Survival.
            if (!DreamEntryConfig.enabled()) return;
            // One distinct sleep per night during the recovery penalty.
            // The attempt remains consumed; retrying within the same night
            // cannot force entry when the third blocked night ends.
            if (DreamHangoverService.blockSleepEntry(player)) return;
            boolean overworld = player.level().dimension() == Level.OVERWORLD;
            boolean available = overworld && DreamsConfig.getEnabledDefinitions().stream()
                    .anyMatch(definition ->
                            ((ServerLevel) player.level()).getServer().getLevel(definition.dimension()) != null);
            boolean equipped = overworld && player.isAlive() && !player.isSpectator() && available
                    && DreamNecklaceEquipmentPolicy.isEquipped(player);
            DreamEntryPolicy.Input input = new DreamEntryPolicy.Input(
                    true, player.isAlive(), overworld, player.isSpectator(),
                    true, equipped, available);
            if (!DreamEntryPolicy.canRoll(input)) return;
            if (!DreamEntryPolicy.roll(DreamEntryConfig.chance(), ThreadLocalRandom.current()::nextDouble)) return;
            DreamTeleportService.enterDream(player, origin.pos(), origin.yaw());
        } finally {
            TELEPORTING.remove(uuid);
        }
    }
}
