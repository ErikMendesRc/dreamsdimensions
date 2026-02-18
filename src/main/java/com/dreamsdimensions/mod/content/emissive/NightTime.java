package com.dreamsdimensions.mod.content.emissive;

import net.minecraft.world.level.Level;

/**
 * Regras de horário para efeitos emissivos noturnos.
 */
public final class NightTime {
    public static final long DAY_TICKS = 24000L;
    public static final long NIGHT_START = 13000L;
    public static final long NIGHT_END = 23000L;

    private NightTime() {
    }

    public static boolean isVanillaNight(Level level) {
        long dayTime = Math.floorMod(level.getDayTime(), DAY_TICKS);
        return computeNight(dayTime);
    }

    public static boolean computeNight(long dayTimeModulo) {
        return dayTimeModulo >= NIGHT_START && dayTimeModulo <= NIGHT_END;
    }
}
