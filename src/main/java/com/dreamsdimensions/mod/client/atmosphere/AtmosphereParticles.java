package com.dreamsdimensions.mod.client.atmosphere;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.util.RandomSource;

/**
 * Visible but bounded blue dream atmosphere. Vanilla dust textures with
 * custom cyan/blue RGB colors; no new textures, particle types or packets.
 *
 * Uses air near the player as a fallback, so particles are still observable
 * when exploring in Creative flight, where surface-height probes fail.
 */
final class AtmosphereParticles {
    private static final DustParticleOptions BLUE_DUST = new DustParticleOptions(0x80CFFF, 0.95F);
    private static final DustParticleOptions CYAN_DUST = new DustParticleOptions(0xA0F5EF, 0.9F);
    private static final DustParticleOptions LUMINOUS_DUST = new DustParticleOptions(0xC5F8FF, 1.15F);

    // Dreamscape: dusty rose + faded coral, much dimmer and less frequent than Blue.
    private static final DustParticleOptions ROSE_DUST = new DustParticleOptions(0xDDA9C3, 0.78F);
    private static final DustParticleOptions PALE_DUST = new DustParticleOptions(0xF1D4CE, 0.66F);
    private static final DustParticleOptions MEMORY_DUST = new DustParticleOptions(0xF4C5DB, 0.92F);

    private AtmosphereParticles() {}

    static void tick(Minecraft client, ClientLevel level, LocalPlayer player,
                     AtmosphereProfile profile, long tick) {
        ParticleStatus setting = client.options.particles().get();
        if (setting == ParticleStatus.MINIMAL || player.isInWater()) {
            return;
        }

        int interval = profile.particleEveryTicks()
                * (setting == ParticleStatus.DECREASED ? profile.decreasedParticleMultiplier() : 1);
        if (tick % interval != 0) {
            return;
        }

        RandomSource random = player.getRandom();
        BlockPos playerPos = player.blockPosition();
        if (profile.theme() == AtmosphereProfile.Theme.DREAMSCAPE) {
            dreamscapeTick(level, playerPos, profile, setting, random, tick);
            return;
        }
        int ambientCount = setting == ParticleStatus.DECREASED ? 2 : 4;

        // Colored vanilla dust around the viewer. The sample must be within a
        // loaded chunk and completely in air; no generation or forced loads.
        for (int i = 0; i < ambientCount; i++) {
            BlockPos sample = playerPos.offset(
                    random.nextInt(13) - 6, random.nextInt(7) - 2, random.nextInt(13) - 6
            );
            if (!isClearAir(level, sample)) {
                continue;
            }
            double x = sample.getX() + random.nextDouble();
            double y = sample.getY() + random.nextDouble();
            double z = sample.getZ() + random.nextDouble();
            double dx = (random.nextDouble() - 0.5) * 0.008;
            double dz = (random.nextDouble() - 0.5) * 0.008;
            level.addParticle(random.nextInt(5) == 0 ? ParticleTypes.SPORE_BLOSSOM_AIR
                            : (random.nextBoolean() ? BLUE_DUST : CYAN_DUST),
                    x, y, z, dx, 0.004, dz);
        }

        // Ground-sensitive accent: prioritize the moss directly under the
        // player's feet, then make a small bounded search for nearby gardens.
        BlockPos luminous = playerPos.below();
        if (!level.hasChunkAt(luminous) || !AtmosphereSampling.isLuminous(level, luminous)) {
            luminous = null;
            for (int attempt = 0; attempt < profile.maxParticleSamples(); attempt++) {
                BlockPos floor = AtmosphereSampling.sampledFloor(
                        level, playerPos, random, profile.sampleRadius());
                if (floor != null && AtmosphereSampling.isLuminous(level, floor)) {
                    luminous = floor;
                    break;
                }
            }
        }

        if (luminous != null) {
            int sparkleCount = setting == ParticleStatus.DECREASED ? 2 : 4;
            for (int i = 0; i < sparkleCount; i++) {
                BlockPos sample = luminous.offset(
                        random.nextInt(5) - 2, 1 + random.nextInt(3), random.nextInt(5) - 2
                );
                if (!isClearAir(level, sample)) {
                    continue;
                }
                level.addParticle(random.nextInt(5) == 0 ? ParticleTypes.END_ROD : LUMINOUS_DUST,
                        sample.getX() + random.nextDouble(),
                        sample.getY() + random.nextDouble(),
                        sample.getZ() + random.nextDouble(),
                        (random.nextDouble() - 0.5) * 0.008, 0.008,
                        (random.nextDouble() - 0.5) * 0.008);
            }
        }
    }

    /**
     * A separate palette and budget sharing the same tick dispatcher.
     * ALL: <=3 ambient + <=2 rare accent per 28 ticks, rare every 112 ticks.
     * DECREASED: <=1 ambient + <=1 rare accent per 56 ticks.
     * MINIMAL is handled by the common gate before this method is called.
     */
    private static void dreamscapeTick(ClientLevel level, BlockPos playerPos,
            AtmosphereProfile profile, ParticleStatus setting, RandomSource random, long tick) {
        int ambientCount = setting == ParticleStatus.DECREASED ? 1 : 3;
        for (int i = 0; i < ambientCount; i++) {
            BlockPos sample = playerPos.offset(
                    random.nextInt(15) - 7, random.nextInt(8) - 3, random.nextInt(15) - 7);
            if (!isClearAir(level, sample)) continue;
            level.addParticle(random.nextInt(7) == 0 ? ParticleTypes.WHITE_ASH
                            : (random.nextBoolean() ? ROSE_DUST : PALE_DUST),
                    sample.getX() + random.nextDouble(),
                    sample.getY() + random.nextDouble(),
                    sample.getZ() + random.nextDouble(),
                    (random.nextDouble() - 0.5) * 0.006,
                    0.001 + random.nextDouble() * 0.003,
                    (random.nextDouble() - 0.5) * 0.006);
        }
        int cadence = profile.particleEveryTicks()
                * (setting == ParticleStatus.DECREASED
                    ? profile.decreasedParticleMultiplier() : 1);
        if (tick % (cadence * 4L) != 0) return;

        // Signal selection only after cheap time/particle-settings gates.
        BlockPos residue = AtmosphereSampling.memoryResidueFloor(
                level, playerPos, random, profile.maxParticleSamples(), profile.sampleRadius());
        boolean fracture = residue == null && AtmosphereSampling.isDreamscapeFracture(level, playerPos);
        boolean elevated = residue == null && !fracture
                && AtmosphereSampling.isDreamscapeElevatedVoid(level, playerPos);
        if (residue == null && !fracture && !elevated) return;

        int accents = setting == ParticleStatus.DECREASED ? 1 : 2;
        BlockPos base = residue != null ? residue.above() : playerPos;
        for (int i = 0; i < accents; i++) {
            BlockPos sample = base.offset(
                    random.nextInt(7) - 3, 1 + random.nextInt(4), random.nextInt(7) - 3);
            if (!isClearAir(level, sample)) continue;
            level.addParticle(residue != null && random.nextInt(6) == 0
                            ? ParticleTypes.END_ROD
                            : (fracture ? PALE_DUST : MEMORY_DUST),
                    sample.getX() + random.nextDouble(),
                    sample.getY() + random.nextDouble(),
                    sample.getZ() + random.nextDouble(),
                    (random.nextDouble() - 0.5) * 0.007,
                    elevated ? -0.002 : 0.002,
                    (random.nextDouble() - 0.5) * 0.007);
        }
    }

    private static boolean isClearAir(ClientLevel level, BlockPos position) {
        return level.hasChunkAt(position)
                && level.isInsideBuildHeight(position)
                && level.getBlockState(position).isAir()
                && level.getFluidState(position).isEmpty();
    }
}
