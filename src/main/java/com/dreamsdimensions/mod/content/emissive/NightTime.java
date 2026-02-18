package com.dreamsdimensions.mod.content.emissive;

import net.minecraft.world.level.Level;

/**
 * Regras de horário para efeitos emissivos noturnos.
 */
public final class NightTime {
    private static final long DAY_TICKS = 24000L;
    private static final long NIGHT_START = 13000L;
    private static final long NIGHT_END = 23000L;

    private NightTime() {
    }

    public static boolean isVanillaNight(Level level) {
        long dayTime = Math.floorMod(level.getDayTime(), DAY_TICKS);
        return dayTime >= NIGHT_START && dayTime <= NIGHT_END;
    }
}

