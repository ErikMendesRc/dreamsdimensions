package com.dreamsdimensions.mod.registry;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.effect.AnchoringEffect;
import com.dreamsdimensions.mod.effect.ClarityEffect;
import com.dreamsdimensions.mod.effect.EarlyAwakeningEffect;
import com.dreamsdimensions.mod.effect.EtherealPhaseEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, DreamsDimensions.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> OW_ANCHORING = EFFECTS.register(
            "ow_anchoring",
            AnchoringEffect::new
    );

    public static final DeferredHolder<MobEffect, MobEffect> OW_CLARITY = EFFECTS.register(
            "ow_clarity",
            ClarityEffect::new
    );

    public static final DeferredHolder<MobEffect, MobEffect> OW_ETHEREAL_PHASE = EFFECTS.register(
            "ow_ethereal_phase",
            EtherealPhaseEffect::new
    );

    public static final DeferredHolder<MobEffect, MobEffect> OW_EARLY_AWAKENING = EFFECTS.register(
            "ow_early_awakening",
            EarlyAwakeningEffect::new
    );

    private ModEffects() {
    }

    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}
