package com.dreamsdimensions.mod.client.atmosphere;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/** Independent client sound cadence: no loops, no custom audio, no packets. */
final class AtmosphereAudio {
    private static long nextCueTick;
    private AtmosphereAudio() {}

    static void reset() { nextCueTick = 160; }

    static void tick(ClientLevel level, LocalPlayer player, AtmosphereProfile profile, long tick) {
        if (tick < nextCueTick || player.isInWater()) return;
        RandomSource random = player.getRandom();
        nextCueTick = tick + profile.soundMinimumTicks()
                + random.nextInt(profile.soundJitterTicks() + 1);

        BlockPos playerPos = player.blockPosition();
        BlockPos luminous = null;
        for (int n = 0; n < profile.maxParticleSamples(); n++) {
            BlockPos floor = AtmosphereSampling.sampledFloor(level, playerPos,
                    random, profile.sampleRadius());
            if (floor != null && AtmosphereSampling.isLuminous(level, floor)) {
                luminous = floor;
                break;
            }
        }

        SoundEvent sound;
        float volume;
        float pitch;
        double x, y, z;
        if (luminous != null) {
            sound = SoundEvents.AMETHYST_BLOCK_CHIME;
            volume = .26F;
            pitch = .85F + random.nextFloat() * .25F;
            x = luminous.getX() + .5;
            y = luminous.getY() + 1;
            z = luminous.getZ() + .5;
        } else {
            boolean grove = AtmosphereSampling.belowLeaves(level, playerPos);
            if (grove) {
                sound = SoundEvents.CHERRY_LEAVES_STEP;
                volume = .18F;
                pitch = .65F + random.nextFloat() * .15F;
            } else {
                sound = SoundEvents.BREEZE_IDLE_AIR;
                volume = .15F;
                pitch = .70F + random.nextFloat() * .15F;
            }
            x = player.getX() + (random.nextBoolean() ? 5 : -5);
            y = player.getY() + (grove ? 2 : 4);
            z = player.getZ() + (random.nextBoolean() ? 7 : -7);
        }
        level.playLocalSound(x, y, z, sound, SoundSource.AMBIENT, volume, pitch, false);
    }
}
