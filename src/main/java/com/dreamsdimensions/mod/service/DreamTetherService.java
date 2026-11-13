package com.dreamsdimensions.mod.service;

import com.dreamsdimensions.mod.dimension.DreamDimensionCatalog;
import com.dreamsdimensions.mod.dimension.DreamTrait;
import com.dreamsdimensions.mod.util.DreamSpawnHelper;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** Transient one-shot rescue: never creates blocks or transports between dimensions. */
public final class DreamTetherService {
    public static final int ACTIVE_TICKS=2400;
    private record Tether(net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dimension,
                          BlockPos anchor, long expiry) {}
    private static final Map<UUID, Tether> ACTIVE=new HashMap<>();
    private DreamTetherService(){}

    public static boolean arm(ServerPlayer player) {
        if (!hasFallTrait(player) || !player.onGround()) return false;
        ServerLevel level=(ServerLevel)player.level();
        BlockPos feet=player.blockPosition();
        if(!DreamSpawnHelper.isSafeStandingSpace(level,feet,player))return false;
        ACTIVE.put(player.getUUID(),new Tether(level.dimension(),feet,
                level.getGameTime()+ACTIVE_TICKS));
        return true;
    }

    private static boolean hasFallTrait(ServerPlayer player){
        return DreamDimensionCatalog.find(player.level().dimension())
                .map(d->d.traits().contains(DreamTrait.FALL)).orElse(false);
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player))return;
        UUID id=player.getUUID();
        Tether active=ACTIVE.get(id);
        if (active==null)return;
        ServerLevel level=(ServerLevel)player.level();
        if (!player.isAlive()||active.dimension()!=level.dimension()||
                level.getGameTime()>=active.expiry()) {ACTIVE.remove(id);return;}
        if (player.onGround() && DreamSpawnHelper.isSafeStandingSpace(level,player.blockPosition(),player)) {
            ACTIVE.put(id,new Tether(level.dimension(),player.blockPosition(),active.expiry()));
            return;
        }
        if (player.getY()>level.getMinY()+12) return;
        ACTIVE.remove(id);
        BlockPos anchor=active.anchor();
        if (!level.isInsideBuildHeight(anchor)
                || !DreamSpawnHelper.isSafeStandingSpace(level,anchor,player)) return;
        player.teleport(new TeleportTransition(level, Vec3.atBottomCenterOf(anchor),
                Vec3.ZERO,player.getYRot(),player.getXRot(),Set.of(),TeleportTransition.DO_NOTHING));
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent e) {ACTIVE.remove(e.getEntity().getUUID());}
    public static void onClone(PlayerEvent.Clone e) {ACTIVE.remove(e.getEntity().getUUID());}
    public static void onStopped(ServerStoppedEvent e) {ACTIVE.clear();}
}
