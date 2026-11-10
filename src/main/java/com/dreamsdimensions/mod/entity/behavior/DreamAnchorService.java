package com.dreamsdimensions.mod.entity.behavior;

import com.dreamsdimensions.mod.dimension.DreamDimensionCatalog;
import com.dreamsdimensions.mod.entity.navigation.SafeGroundNavigation;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Shared one-use, temporary safe-land recall. The source mob chooses the
 * anchor; this service knows nothing about specific mob classes or animations.
 *
 * Transient per-session protection, intentionally not persisted as a teleport
 * right across server restarts. No chunk tickets or forced chunk loading.
 */
public final class DreamAnchorService {
    public static final DreamAnchorPolicy POLICY = new DreamAnchorPolicy(20 * 120, 12, 48, -0.10);
    private static final Map<UUID, Bond> ACTIVE = new HashMap<>();

    private record Bond(ResourceKey<Level> dimension, BlockPos feet, long expiresAt) {}
    private DreamAnchorService() {}

    public static boolean hasBond(ServerPlayer player) {
        Bond bond = ACTIVE.get(player.getUUID());
        return bond != null && player.level().dimension() == bond.dimension()
                && POLICY.isActive(player.level().getGameTime(), bond.expiresAt());
    }

    public static boolean grant(ServerPlayer player, BlockPos feet) {
        if (!(player.level() instanceof ServerLevel level)
                || level.dimension() != DreamDimensionCatalog.DREAMSCAPE
                || !player.isAlive() || player.isSpectator()
                || player.getAbilities().instabuild || hasBond(player)
                || !SafeGroundNavigation.canStand(level, feet, player)) return false;
        ACTIVE.put(player.getUUID(), new Bond(level.dimension(), feet.immutable(),
                level.getGameTime() + POLICY.durationTicks()));
        player.sendOverlayMessage(Component.literal(
                "Pacto de Vigília: a ilha se lembrará de você por 2 minutos (um resgate)."));
        level.sendParticles(ParticleTypes.ENCHANT, feet.getX() + 0.5, feet.getY() + 0.5,
                feet.getZ() + 0.5, 25, 0.8, 0.5, 0.8, 0.03);
        return true;
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Bond bond = ACTIVE.get(player.getUUID());
        if (bond == null) return;
        if (!(player.level() instanceof ServerLevel level)
                || level.dimension() != bond.dimension() || !player.isAlive()
                || player.isSpectator() || player.getAbilities().instabuild
                || !POLICY.isActive(level.getGameTime(), bond.expiresAt())) {
            ACTIVE.remove(player.getUUID());
            return;
        }
        double dx = player.getX() - bond.feet().getX() - 0.5;
        double dz = player.getZ() - bond.feet().getZ() - 0.5;
        // Once the player leaves the island neighborhood, the pact ends.
        if (!POLICY.insideHorizontalArea(dx, dz)) {
            ACTIVE.remove(player.getUUID());
            return;
        }
        if (!POLICY.shouldRecall(player.getY(), player.getDeltaMovement().y,
                bond.feet().getY())) return;

        // Consume the charge before movement, preventing duplicate callbacks.
        ACTIVE.remove(player.getUUID());
        if (!SafeGroundNavigation.canStand(level, bond.feet(), player)) {
            player.sendOverlayMessage(Component.literal(
                    "A âncora se desfez: o lugar de retorno não está mais seguro."));
            return;
        }
        Vec3 destination = Vec3.atBottomCenterOf(bond.feet());
        player.teleportTo(destination.x, destination.y, destination.z);
        player.setDeltaMovement(Vec3.ZERO);
        player.resetFallDistance();
        player.sendOverlayMessage(Component.literal(
                "O Vigia lembrou seu caminho. Seu Pacto de Vigília foi consumido."));
        level.sendParticles(ParticleTypes.END_ROD, destination.x, destination.y + 1,
                destination.z, 36, 0.8, 1.1, 0.8, 0.05);
    }

    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ACTIVE.remove(event.getEntity().getUUID());
    }

    public static void onPlayerClone(PlayerEvent.Clone event) {
        ACTIVE.remove(event.getEntity().getUUID());
    }

    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        ACTIVE.remove(event.getEntity().getUUID());
    }
}
