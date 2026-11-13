package com.dreamsdimensions.mod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Visible indicator. The durable three-night restriction lives in player data. */
public final class DreamHangoverEffect extends MobEffect {
    public DreamHangoverEffect() {
        super(MobEffectCategory.HARMFUL, 0x86758E);
    }
}
