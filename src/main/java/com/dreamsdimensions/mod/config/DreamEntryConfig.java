package com.dreamsdimensions.mod.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Server-side release gate: only enable public entry after Task 9.3 Survival recovery homologation. */
public final class DreamEntryConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final ModConfigSpec.BooleanValue ENABLED = BUILDER
            .comment("Keep false until a player proves ruined recovery without commands in Survival.")
            .define("dream_entry_enabled", false);

    private static final ModConfigSpec.DoubleValue CHANCE = BUILDER
            .comment("Full qualified sleep always attempts entry; destination remains weighted. Entry gate defaults off.")
            .defineInRange("dream_entry_chance", 1.0D, 0.0D, 1.0D);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private DreamEntryConfig() {}

    public static boolean enabled() { return ENABLED.get(); }
    public static double chance() { return CHANCE.get(); }
}
