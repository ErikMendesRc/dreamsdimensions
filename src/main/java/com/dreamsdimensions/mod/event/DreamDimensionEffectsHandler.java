package com.dreamsdimensions.mod.event;

import com.dreamsdimensions.mod.DreamsDimensions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Aplica efeitos globais enquanto o jogador estiver no Campo Onírico Azul.
 */
public final class DreamDimensionEffectsHandler {

    private static final ResourceKey<Level> CAMPO_ONIRICO_AZUL_KEY = ResourceKey.create(
            Registries.DIMENSION,
            Identifier.fromNamespaceAndPath(DreamsDimensions.MODID, "campo_onirico_azul")
    );

    private static final int EFFECT_DURATION_TICKS = 200;
    private static final int REAPPLY_THRESHOLD_TICKS = 100;

    private DreamDimensionEffectsHandler() {
    }

    /**
     * Reaplica Slow Falling no lado servidor, somente quando necessário.
     */
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (player.level().isClientSide()) {
            return;
        }

        if (!player.level().dimension().equals(CAMPO_ONIRICO_AZUL_KEY)) {
            return;
        }

        MobEffectInstance current = player.getEffect(MobEffects.SLOW_FALLING);
        if (current == null || current.getDuration() < REAPPLY_THRESHOLD_TICKS) {
            player.addEffect(new MobEffectInstance(
                    MobEffects.SLOW_FALLING,
                    EFFECT_DURATION_TICKS,
                    0,
                    true,
                    false,
                    true
            ));
        }
    }
}