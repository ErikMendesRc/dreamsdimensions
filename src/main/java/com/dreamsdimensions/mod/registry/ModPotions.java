package com.dreamsdimensions.mod.registry;

import com.dreamsdimensions.mod.DreamsDimensions;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModPotions {
    private static final int DEFAULT_DURATION_TICKS = 3600;

    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(Registries.POTION, DreamsDimensions.MODID);

    public static final DeferredHolder<Potion, Potion> OW_ONEIRIC_BASE = POTIONS.register(
            "ow_oneiric_base",
            () -> new Potion("ow_oneiric_base")
    );

    public static final DeferredHolder<Potion, Potion> OW_POTION_OF_ANCHORING = POTIONS.register(
            "ow_potion_of_anchoring",
            () -> new Potion("ow_potion_of_anchoring", new MobEffectInstance(ModEffects.OW_ANCHORING, DEFAULT_DURATION_TICKS, 0))
    );

    public static final DeferredHolder<Potion, Potion> OW_POTION_OF_CLARITY = POTIONS.register(
            "ow_potion_of_clarity",
            () -> new Potion("ow_potion_of_clarity", new MobEffectInstance(ModEffects.OW_CLARITY, DEFAULT_DURATION_TICKS, 0))
    );

    public static final DeferredHolder<Potion, Potion> OW_POTION_OF_ETHEREAL_PHASE = POTIONS.register(
            "ow_potion_of_ethereal_phase",
            () -> new Potion("ow_potion_of_ethereal_phase", new MobEffectInstance(ModEffects.OW_ETHEREAL_PHASE, DEFAULT_DURATION_TICKS, 0))
    );

    public static final DeferredHolder<Potion, Potion> OW_POTION_OF_EARLY_AWAKENING = POTIONS.register(
            "ow_potion_of_early_awakening",
            () -> new Potion("ow_potion_of_early_awakening", new MobEffectInstance(ModEffects.OW_EARLY_AWAKENING, DEFAULT_DURATION_TICKS, 0))
    );

    private ModPotions() {
    }

    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }
}
