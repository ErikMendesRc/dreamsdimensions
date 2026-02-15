package com.dreamsdimensions.mod.registry;

import com.dreamsdimensions.mod.DreamsDimensions;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModPotions {
    public static final int DURATION_SHORT_TICKS = 1200;  // 1:00
    public static final int DURATION_MEDIUM_TICKS = 1800; // 1:30
    public static final int DURATION_LONG_TICKS = 2400;   // 2:00

    private static final int AMPLIFIER_TIER_1 = 0;

    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(Registries.POTION, DreamsDimensions.MODID);

    public static final DeferredHolder<Potion, Potion> OW_ONEIRIC_BASE = POTIONS.register(
            "ow_oneiric_base",
            () -> new Potion("ow_oneiric_base")
    );

    public static final DeferredHolder<Potion, Potion> OW_POTION_OF_ANCHORING = POTIONS.register(
            "ow_potion_of_anchoring",
            () -> new Potion("ow_potion_of_anchoring", new MobEffectInstance(ModEffects.OW_ANCHORING, DURATION_LONG_TICKS, AMPLIFIER_TIER_1))
    );

    public static final DeferredHolder<Potion, Potion> OW_POTION_OF_CLARITY = POTIONS.register(
            "ow_potion_of_clarity",
            () -> new Potion("ow_potion_of_clarity", new MobEffectInstance(ModEffects.OW_CLARITY, DURATION_MEDIUM_TICKS, AMPLIFIER_TIER_1))
    );

    public static final DeferredHolder<Potion, Potion> OW_POTION_OF_ETHEREAL_PHASE = POTIONS.register(
            "ow_potion_of_ethereal_phase",
            () -> new Potion("ow_potion_of_ethereal_phase", new MobEffectInstance(ModEffects.OW_ETHEREAL_PHASE, DURATION_SHORT_TICKS, AMPLIFIER_TIER_1))
    );

    public static final DeferredHolder<Potion, Potion> OW_POTION_OF_EARLY_AWAKENING = POTIONS.register(
            "ow_potion_of_early_awakening",
            () -> new Potion("ow_potion_of_early_awakening", new MobEffectInstance(ModEffects.OW_EARLY_AWAKENING, DURATION_SHORT_TICKS, AMPLIFIER_TIER_1))
    );

    private ModPotions() {
    }

    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }
}
