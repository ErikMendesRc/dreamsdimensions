package com.dreamsdimensions.mod.service;

import com.dreamsdimensions.mod.attachment.DreamHangoverData;
import com.dreamsdimensions.mod.registry.ModAttachments;
import com.dreamsdimensions.mod.registry.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Persistent lockout, calculated only from genuine sleep entry attempts. */
public final class DreamHangoverService {
    private static final int RECOVERY_TICKS = 6000;

    private DreamHangoverService() {}

    public static void afterCrackedReturn(ServerPlayer player) {
        player.setData(ModAttachments.DREAM_HANGOVER, DreamHangoverData.fresh());
        player.addEffect(new MobEffectInstance(ModEffects.RESSACA_ONIRICA, RECOVERY_TICKS));
        player.addEffect(new MobEffectInstance(MobEffects.HUNGER, RECOVERY_TICKS, 0));
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, RECOVERY_TICKS, 0));
        player.sendSystemMessage(Component.translatable("message.dreamsdimensions.cracked_awakener.hangover"));
    }

    /** True when this complete sleep cycle must not trigger dimension entry. */
    public static boolean blockSleepEntry(ServerPlayer player) {
        if (!player.hasData(ModAttachments.DREAM_HANGOVER)) return false;
        long night = Math.floorDiv(player.level().getOverworldClockTime(), 24000L);
        DreamHangoverData previous = player.getData(ModAttachments.DREAM_HANGOVER);
        if (!previous.blocks(night)) return false;
        DreamHangoverData updated = previous.blockNight(night);
        if (!updated.equals(previous)) {
            player.setData(ModAttachments.DREAM_HANGOVER, updated);
        }
        player.sendSystemMessage(Component.translatable(
                "message.dreamsdimensions.dream_hangover.blocked",
                updated.nightsRemaining()));
        return true;
    }
}
