package com.dreamsdimensions.mod.worldgen;

import java.util.Locale;

/**
 * Perfil ativo de worldgen para injeção dos biomas no TerraBlender.
 *
 * <p>Por padrão usa {@link Profile#PROD}. Para testes locais rápidos,
 * inicie o jogo com {@code -Ddreamsdimensions.worldgen.profile=TEST}.</p>
 */
public final class WorldgenProfiles {
    public static final String PROFILE_PROPERTY = "dreamsdimensions.worldgen.profile";
    public static final Profile ACTIVE = resolveActiveProfile();

    private WorldgenProfiles() {
    }

    private static Profile resolveActiveProfile() {
        String raw = System.getProperty(PROFILE_PROPERTY, Profile.PROD.name());
        try {
            return Profile.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return Profile.PROD;
        }
    }

    public enum Profile {
        TEST,
        PROD
    }
}
