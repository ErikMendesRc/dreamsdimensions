package com.dreamsdimensions.mod.event;

import com.dreamsdimensions.mod.registry.ModEffects;
import com.dreamsdimensions.mod.util.DreamReturnHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handlers de runtime para os efeitos customizados da Sprint 3.2.
 */
public final class EffectEventHandler {
    private static final float ANCHORING_FALL_DAMAGE_MULTIPLIER = 0.10F;
    private static final float ETHEREAL_KNOCKBACK_MULTIPLIER = 0.15F;

    private static final String ETHEREAL_TEAM_NAME = "dreamsdimensions.ethereal_phase";
    private static final String NO_PREVIOUS_TEAM = "__dreamsdimensions_none__";
    private static final Map<UUID, String> PREVIOUS_TEAMS = new ConcurrentHashMap<>();

    private EffectEventHandler() {
    }

    public static void onLivingFall(LivingFallEvent event) {
        LivingEntity living = event.getEntity();
        if (living.hasEffect(ModEffects.OW_ANCHORING)) {
            event.setDamageMultiplier(event.getDamageMultiplier() * ANCHORING_FALL_DAMAGE_MULTIPLIER);
        }
    }

    public static void onMobEffectAdded(MobEffectEvent.Added event) {
        LivingEntity living = event.getEntity();
        MobEffectInstance newEffect = event.getEffectInstance();
        if (newEffect.getEffect().is(ModEffects.OW_CLARITY)) {
            living.removeEffect(MobEffects.POISON);
            living.removeEffect(MobEffects.WITHER);
            living.removeEffect(MobEffects.SLOWNESS);
            living.removeEffect(MobEffects.MINING_FATIGUE);
            living.removeEffect(MobEffects.BLINDNESS);
        }
    }

    public static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity living = event.getEntity();
        MobEffectInstance incoming = event.getEffectInstance();

        if (!living.hasEffect(ModEffects.OW_CLARITY)) {
            return;
        }

        if (incoming.getEffect().is(ModEffects.OW_CLARITY)) {
            return;
        }

        if (incoming.getEffect().value().getCategory() == MobEffectCategory.BENEFICIAL) {
            return;
        }

        event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
    }

    public static void onLivingKnockBack(LivingKnockBackEvent event) {
        if (event.getEntity().hasEffect(ModEffects.OW_ETHEREAL_PHASE)) {
            event.setStrength(event.getStrength() * ETHEREAL_KNOCKBACK_MULTIPLIER);
        }
    }

    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        boolean hasPhase = player.hasEffect(ModEffects.OW_ETHEREAL_PHASE);
        if (hasPhase) {
            applyEtherealCollision(player);
        } else {
            restoreCollision(player);
        }
    }

    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (!DreamReturnHelper.isDreamDimension(player.level())) {
            return;
        }

        if (!player.hasEffect(ModEffects.OW_EARLY_AWAKENING)) {
            return;
        }

        DreamReturnHelper.buildReturnTransition(player).ifPresent(transition -> {
            player.removeEffect(ModEffects.OW_EARLY_AWAKENING);
            event.setCanceled(true);

            player.setHealth(Math.max(1.0F, player.getMaxHealth() * 0.25F));
            player.stopUsingItem();
            player.clearFire();
            player.teleport(transition);
        });
    }

    private static void applyEtherealCollision(ServerPlayer player) {
        Scoreboard scoreboard = player.level().getScoreboard();
        PlayerTeam currentTeam = scoreboard.getPlayersTeam(player.getScoreboardName());

        if (currentTeam != null && !currentTeam.getName().equals(ETHEREAL_TEAM_NAME)) {
            PREVIOUS_TEAMS.putIfAbsent(player.getUUID(), currentTeam.getName());
        } else {
            PREVIOUS_TEAMS.putIfAbsent(player.getUUID(), NO_PREVIOUS_TEAM);
        }

        PlayerTeam etherealTeam = scoreboard.getPlayerTeam(ETHEREAL_TEAM_NAME);
        if (etherealTeam == null) {
            etherealTeam = scoreboard.addPlayerTeam(ETHEREAL_TEAM_NAME);
            etherealTeam.setCollisionRule(Team.CollisionRule.NEVER);
        } else if (etherealTeam.getCollisionRule() != Team.CollisionRule.NEVER) {
            etherealTeam.setCollisionRule(Team.CollisionRule.NEVER);
        }

        if (currentTeam == etherealTeam) {
            return;
        }

        scoreboard.addPlayerToTeam(player.getScoreboardName(), etherealTeam);
    }

    private static void restoreCollision(ServerPlayer player) {
        UUID uuid = player.getUUID();
        if (!PREVIOUS_TEAMS.containsKey(uuid)) {
            return;
        }

        Scoreboard scoreboard = player.level().getScoreboard();
        PlayerTeam currentTeam = scoreboard.getPlayersTeam(player.getScoreboardName());
        if (currentTeam != null && currentTeam.getName().equals(ETHEREAL_TEAM_NAME)) {
            scoreboard.removePlayerFromTeam(player.getScoreboardName(), currentTeam);
        }

        String previousTeamName = PREVIOUS_TEAMS.remove(uuid);
        if (NO_PREVIOUS_TEAM.equals(previousTeamName)) {
            return;
        }

        PlayerTeam previousTeam = scoreboard.getPlayerTeam(previousTeamName);
        if (previousTeam != null) {
            scoreboard.addPlayerToTeam(player.getScoreboardName(), previousTeam);
        }
    }
}
