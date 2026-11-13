package com.dreamsdimensions.mod.registry;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.effect.PactoVigiliaEffect;
import com.dreamsdimensions.mod.effect.DreamHangoverEffect;
import com.dreamsdimensions.mod.effect.LightnessEffect;
import com.dreamsdimensions.mod.effect.VigilInfusionEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Shared registry of native status-effect icons. Effects registered here
 * are not themselves proof that a server-side gameplay grant exists.
 */
public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, DreamsDimensions.MODID);

    public static final DeferredHolder<MobEffect, PactoVigiliaEffect> PACTO_DE_VIGILIA =
            EFFECTS.register("pacto_de_vigilia", PactoVigiliaEffect::new);

    public static final DeferredHolder<MobEffect, DreamHangoverEffect> RESSACA_ONIRICA =
            EFFECTS.register("ressaca_onirica", DreamHangoverEffect::new);

    public static final DeferredHolder<MobEffect, LightnessEffect> LIGHTNESS =
            EFFECTS.register("lightness", LightnessEffect::new);

    public static final DeferredHolder<MobEffect, VigilInfusionEffect> VIGIL_INFUSION =
            EFFECTS.register("vigil_infusion", VigilInfusionEffect::new);

    private ModEffects() {}

    public static void register(IEventBus modBus) {
        EFFECTS.register(modBus);
    }
}
