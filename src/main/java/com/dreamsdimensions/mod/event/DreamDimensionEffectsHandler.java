package com.dreamsdimensions.mod.event;

import com.dreamsdimensions.mod.dimension.DimensionEffectPolicy;
import com.dreamsdimensions.mod.dimension.DreamDimensionCatalog;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Only Blue Field renews a dimension-owned Slow Falling effect. External
 * potions/commands/mod effects are never removed or blindly replaced.
 *
 * Provenance is transient and conservative: if the vanilla effect instance
 * is externally changed, stop renewing it. Natural expiry handles exits.
 */
public final class DreamDimensionEffectsHandler {
    private static final int EFFECT_DURATION_TICKS = 200;
    private static final int REAPPLY_THRESHOLD_TICKS = 100;
    private static final Map<UUID, OwnedEffect> OWNED = new HashMap<>();

    private record OwnedEffect(MobEffectInstance instance, int lastDuration) {}

    private DreamDimensionEffectsHandler() {}

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        UUID uuid = player.getUUID();
        DimensionEffectPolicy policy = DreamDimensionCatalog.find(player.level().dimension())
                .map(definition -> definition.effectPolicy())
                .orElse(DimensionEffectPolicy.NONE);
        if (!player.isAlive() || policy != DimensionEffectPolicy.SLOW_FALLING) {
            OWNED.remove(uuid);
            return;
        }

        MobEffectInstance current = player.getEffect(MobEffects.SLOW_FALLING);
        if (current == null) {
            applyOwnedEffect(player, uuid);
            return;
        }

        OwnedEffect claim = OWNED.get(uuid);
        if (claim == null) {
            // An existing potion or command buff is not ours to refresh.
            return;
        }
        if (current != claim.instance()
                || current.getAmplifier() != 0
                || !current.isAmbient() || current.isVisible() || !current.showIcon()
                || current.getDuration() > claim.lastDuration()) {
            // Vanilla may mutate an effect instance when a potion is applied.
            // A duration increase or metadata change invalidates our claim.
            OWNED.remove(uuid);
            return;
        }

        if (current.getDuration() < REAPPLY_THRESHOLD_TICKS) {
            applyOwnedEffect(player, uuid);
        } else {
            OWNED.put(uuid, new OwnedEffect(current, current.getDuration()));
        }
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        OWNED.remove(event.getEntity().getUUID());
    }

    private static void applyOwnedEffect(ServerPlayer player, UUID uuid) {
        player.addEffect(new MobEffectInstance(
                MobEffects.SLOW_FALLING, EFFECT_DURATION_TICKS, 0, true, false, true));
        MobEffectInstance applied = player.getEffect(MobEffects.SLOW_FALLING);
        if (applied != null && applied.getAmplifier() == 0) {
            OWNED.put(uuid, new OwnedEffect(applied, applied.getDuration()));
        } else {
            OWNED.remove(uuid);
        }
    }
}
