package com.dreamsdimensions.mod.worldgen.biomes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Catálogo central dos biomas de Overworld injetados por TerraBlender.
 *
 * <p>Para novo bioma, adicione 1 linha no bloco estático:
 * {@code ENTRIES_INTERNAL.add(new SeuBiomeEntry());}</p>
 */
public final class DreamsBiomesOverworld {
    private static final List<DreamsBiomeEntry> ENTRIES_INTERNAL = new ArrayList<>();
    public static final List<DreamsBiomeEntry> ENTRIES = Collections.unmodifiableList(ENTRIES_INTERNAL);

    static {
        ENTRIES_INTERNAL.add(new LuminaHollowsEntry());
    }

    private DreamsBiomesOverworld() {
    }
}
