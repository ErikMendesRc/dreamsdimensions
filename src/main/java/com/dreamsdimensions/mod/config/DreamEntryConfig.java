package com.dreamsdimensions.mod.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server-only release guard; new entry remains OFF until 5.4, 5.5 and 5.7. */
public final class DreamEntryConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue ENABLED = BUILDER
            .comment("Unsafe lab-only dream entry; keep false in public Survival until 5.4/5.5/5.7.")
            .define("dream_entry_enabled", false);

    private static final ModConfigSpec.DoubleValue CHANCE = BUILDER
            .comment("One chance per completed 100-tick sleep; 0.30 is an unbalanced test baseline.")
            .defineInRange("dream_entry_chance", 0.30D, 0.0D, 1.0D);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private DreamEntryConfig() {}

    public static boolean enabled() { return ENABLED.get(); }
    public static double chance() { return CHANCE.get(); }
}
