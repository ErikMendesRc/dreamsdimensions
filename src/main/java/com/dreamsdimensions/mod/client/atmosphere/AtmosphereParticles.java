package com.dreamsdimensions.mod.client.atmosphere;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.util.RandomSource;

/** Purely local vanilla particles, never forced visible or networked. */
final class AtmosphereParticles {
    private AtmosphereParticles() {}

    static void tick(Minecraft client, ClientLevel level, LocalPlayer player,
                     AtmosphereProfile profile, long tick) {
        ParticleStatus setting = client.options.particles().get();
        if (setting == ParticleStatus.MINIMAL || player.isInWater()) return;
        int interval = profile.particleEveryTicks()
                * (setting == ParticleStatus.DECREASED ? profile.decreasedParticleMultiplier() : 1);
        if (tick % interval != 0) return;
        RandomSource random = player.getRandom();
        if (random.nextBoolean()) return;

        BlockPos playerPos = player.blockPosition();
        BlockPos floor = null;
        for (int attempt = 0; attempt < profile.maxParticleSamples(); attempt++) {
            BlockPos sample = AtmosphereSampling.sampledFloor(level, playerPos,
                    random, profile.sampleRadius());
            if (sample == null) continue;
            if (AtmosphereSampling.isLuminous(level, sample)) {
                floor = sample;
                break;
            }
            if (floor == null) floor = sample;
        }
        if (floor == null) return;
        boolean luminous = AtmosphereSampling.isLuminous(level, floor);
        if (!luminous && AtmosphereSampling.belowLeaves(level, playerPos)
                && random.nextInt(4) != 0) return;

        double x = floor.getX() + .25 + random.nextDouble() * .5;
        double y = floor.getY() + 1.1 + random.nextDouble() * 1.7;
        double z = floor.getZ() + .25 + random.nextDouble() * .5;
        if (luminous) {
            level.addParticle(random.nextInt(4) == 0 ? ParticleTypes.END_ROD : ParticleTypes.FIREFLY,
                    x, y, z, (random.nextDouble() - .5) * .007, .006,
                    (random.nextDouble() - .5) * .007);
        } else if (AtmosphereSampling.isValley(level, playerPos) && random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.WHITE_ASH, x, y, z, .007, .001, -.004);
        } else if (random.nextInt(3) == 0) {
            level.addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, x, y, z,
                    (random.nextDouble() - .5) * .009, .0015,
                    (random.nextDouble() - .5) * .009);
        }
    }
}
