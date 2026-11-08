package com.dreamsdimensions.mod.worldgen;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Only registers the 26.3 feature TYPE codec; instances live in datapacks. */
public final class DreamWorldgen {
    private static final DeferredRegister<MapCodec<? extends Feature>> FEATURES =
            DeferredRegister.create(BuiltInRegistries.FEATURE_TYPE, DreamsDimensions.MODID);

    static {
        FEATURES.register("dream_vegetation", () -> DreamVegetationFeature.CODEC);
    }

    private DreamWorldgen() {}

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
    }
}
