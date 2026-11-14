package com.dreamsdimensions.mod.attachment;

import com.mojang.serialization.Codec;
import com.dreamsdimensions.mod.service.DreamRecoveryPolicy;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/** Stored per player and copied on death: three distinct sleeping nights. */
public record DreamHangoverData(int nightsRemaining, long lastBlockedNight) {
    public static final MapCodec<DreamHangoverData> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.intRange(0, 3).fieldOf("nights_remaining").forGetter(DreamHangoverData::nightsRemaining),
                    Codec.LONG.fieldOf("last_blocked_night").forGetter(DreamHangoverData::lastBlockedNight)
            ).apply(instance, DreamHangoverData::new));

    public static DreamHangoverData fresh() {
        return new DreamHangoverData(3, Long.MIN_VALUE);
    }

    public boolean blocks(long nightIndex) {
        return DreamRecoveryPolicy.hangoverBlocks(nightsRemaining, lastBlockedNight, nightIndex);
    }

    public DreamHangoverData blockNight(long nightIndex) {
        if (nightIndex == lastBlockedNight || nightsRemaining == 0) return this;
        return new DreamHangoverData(DreamRecoveryPolicy.hangoverNightsAfterSleep(
                nightsRemaining, lastBlockedNight, nightIndex), nightIndex);
    }
}
