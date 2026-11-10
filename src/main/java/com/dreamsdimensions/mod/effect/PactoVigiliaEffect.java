package com.dreamsdimensions.mod.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Cosmetic-only beneficial marker for the native Minecraft HUD/inventory.
 * Rescue authority remains exclusively with DreamAnchorService, never with
 * possession of this effect (e.g. through /effect give).
 */
public final class PactoVigiliaEffect extends MobEffect {
    public PactoVigiliaEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x42D9D2);
    }
}
