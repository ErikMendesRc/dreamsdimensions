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

    /** Each profile starts after its own minimum delay; Blue stays at 160. */
    static void begin(AtmosphereProfile profile) {
        nextCueTick = profile.soundMinimumTicks();
    }

    static void tick(ClientLevel level, LocalPlayer player, AtmosphereProfile profile, long tick) {
        if (tick < nextCueTick || player.isInWater()) return;
        RandomSource random = player.getRandom();
        nextCueTick = tick + profile.soundMinimumTicks()
                + random.nextInt(profile.soundJitterTicks() + 1);
        if (profile.theme() == AtmosphereProfile.Theme.DREAMSCAPE) {
            dreamscapeCue(level, player, profile, random);
            return;
        }

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

    /** One sporadic spatial cue, no looping instance, using proven 26.3 sounds.
     * Darker pitch/low levels distinguish this from Blue, not horror stingers.
     */
    private static void dreamscapeCue(ClientLevel level, LocalPlayer player,
            AtmosphereProfile profile, RandomSource random) {
        BlockPos pos = player.blockPosition();
        BlockPos residue = AtmosphereSampling.memoryResidueFloor(
                level, pos, random, profile.maxParticleSamples(), profile.sampleRadius());
        boolean fracture = residue == null && AtmosphereSampling.isDreamscapeFracture(level, pos);
        boolean elevated = residue == null && !fracture
                && AtmosphereSampling.isDreamscapeElevatedVoid(level, pos);

        SoundEvent sound;
        float volume;
        float pitch;
        double x;
        double y;
        double z;
        if (residue != null) {
            sound = SoundEvents.AMETHYST_BLOCK_CHIME;
            volume = 0.14F;
            pitch = 0.62F + random.nextFloat() * 0.15F;
            x = residue.getX() + 0.5;
            y = residue.getY() + 1.0;
            z = residue.getZ() + 0.5;
        } else {
            sound = elevated ? SoundEvents.CHERRY_LEAVES_STEP : SoundEvents.BREEZE_IDLE_AIR;
            volume = fracture ? 0.12F : elevated ? 0.085F : 0.075F;
            pitch = fracture ? 0.56F + random.nextFloat() * 0.1F
                    : 0.72F + random.nextFloat() * 0.14F;
            x = player.getX() + (random.nextBoolean() ? 8.0 : -8.0);
            y = player.getY() + (fracture ? -3.0 : elevated ? 5.0 : 2.0);
            z = player.getZ() + (random.nextBoolean() ? 9.0 : -9.0);
        }
        // One source per cue; respects Ambient/Environment slider.
        level.playLocalSound(x, y, z, sound, SoundSource.AMBIENT, volume, pitch, false);
    }
}
