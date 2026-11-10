package com.dreamsdimensions.mod.entity;

import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * Slow, non-hostile island guardian. Server controls state and terrestrial
 * navigation; GeckoLib exclusively controls the visual bone animations.
 * The mob is intentionally inert until it senses a nearby player.
 */
public final class VigiaAdormecidoEntity extends PathfinderMob implements GeoEntity {
    private static final String CONTROLLER = "vigia";
    private static final byte DORMANT = 0;
    private static final byte AWAKENING = 1;
    private static final byte WATCHING = 2;
    private static final byte MOVING = 3;
    private static final byte DEFENSIVE = 4;
    private static final byte RETURNING = 5;

    private static final int AWAKEN_TICKS = 80;
    private static final int DEFEND_TICKS = 40;
    private static final int RETURN_TICKS = 80;
    private static final int SLEEP_AFTER_TICKS = 320;
    private static final int DAMAGE_REACTION_COOLDOWN = 55;

    private static final EntityDataAccessor<Byte> STATE =
            SynchedEntityData.defineId(VigiaAdormecidoEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> OBSERVING =
            SynchedEntityData.defineId(VigiaAdormecidoEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation IDLE_DORMANT = RawAnimation.begin()
            .thenLoop("animation.vigia_adormecido.idle_dormant");
    private static final RawAnimation AWAKEN = RawAnimation.begin()
            .thenPlay("animation.vigia_adormecido.awaken");
    private static final RawAnimation IDLE_AWAKE = RawAnimation.begin()
            .thenLoop("animation.vigia_adormecido.idle_awake");
    private static final RawAnimation OBSERVE = RawAnimation.begin()
            .thenLoop("animation.vigia_adormecido.observe");
    private static final RawAnimation WALK = RawAnimation.begin()
            .thenLoop("animation.vigia_adormecido.walk");
    private static final RawAnimation DEFEND = RawAnimation.begin()
            .thenPlay("animation.vigia_adormecido.defensive");
    private static final RawAnimation RETURN = RawAnimation.begin()
            .thenPlay("animation.vigia_adormecido.return_dormant");
    private static final RawAnimation INSPECT = RawAnimation.begin()
            .thenPlay("animation.vigia_adormecido.inspect_ground");
    private static final RawAnimation ALERT = RawAnimation.begin()
            .thenPlay("animation.vigia_adormecido.alert");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);
    private int stateTicks;
    private int idleWithoutPlayer;
    private int wanderCooldown = 140;
    private int inspectCooldown = 260;
    private int reactionCooldown;
    private boolean reactedToCurrentDamage;
    private BlockPos islandHome;
    private boolean homeChecked;

    public VigiaAdormecidoEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 26.0)
                .add(Attributes.MOVEMENT_SPEED, 0.12)
                .add(Attributes.FOLLOW_RANGE, 18.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.55);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(STATE, DORMANT);
        builder.define(OBSERVING, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new GroundPathNavigation(this, level);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel server)) return;
        if (!homeChecked) {
            homeChecked = true;
            if (VigiaIslandHabitat.habitat().isValid(server, blockPosition())) {
                islandHome = blockPosition();
            }
        }
        if (wanderCooldown > 0) wanderCooldown--;
        if (inspectCooldown > 0) inspectCooldown--;
        if (reactionCooldown > 0) reactionCooldown--;

        if (this.hurtTime > 0 && !reactedToCurrentDamage) {
            reactedToCurrentDamage = true;
            if (reactionCooldown == 0) {
                enterState(DEFENSIVE);
                reactionCooldown = DAMAGE_REACTION_COOLDOWN;
                getNavigation().stop();
            }
        } else if (this.hurtTime == 0) {
            reactedToCurrentDamage = false;
        }

        byte state = entityData.get(STATE);
        if (state == DEFENSIVE) {
            if (++stateTicks >= DEFEND_TICKS) enterState(WATCHING);
            return;
        }
        if (state == AWAKENING) {
            if (++stateTicks >= AWAKEN_TICKS) enterState(WATCHING);
            return;
        }
        if (state == RETURNING) {
            if (++stateTicks >= RETURN_TICKS) enterState(DORMANT);
            return;
        }

        // Player perception is intentionally sparse while dormant.
        if (tickCount % 10 == 0) {
            Player player = nearestValidPlayer(14.0);
            if (state == DORMANT) {
                if (player != null && player.distanceToSqr(this) <= 12.0 * 12.0) {
                    enterState(AWAKENING);
                }
                return;
            }
            if (player == null) {
                idleWithoutPlayer += 10;
                entityData.set(OBSERVING, false);
            } else {
                idleWithoutPlayer = 0;
                entityData.set(OBSERVING, true);
                getLookControl().setLookAt(player, 13, 9);
                if (player.isSprinting() && player.distanceToSqr(this) < 9.0 * 9.0
                        && inspectCooldown == 0 && getNavigation().isDone()) {
                    triggerAnim(CONTROLLER, "alert");
                    inspectCooldown = 220;
                }
            }
        }

        if (state == DORMANT) {
            getNavigation().stop();
            return;
        }
        if (idleWithoutPlayer >= SLEEP_AFTER_TICKS && getNavigation().isDone()) {
            enterState(RETURNING);
            return;
        }
        if (!getNavigation().isDone()) {
            if (state != MOVING) enterState(MOVING);
        } else if (state == MOVING) {
            enterState(WATCHING);
        }
        if (entityData.get(STATE) == WATCHING && getNavigation().isDone()) {
            if (inspectCooldown == 0 && entityData.get(OBSERVING) && getRandom().nextInt(200) == 0) {
                triggerAnim(CONTROLLER, "inspect_ground");
                inspectCooldown = 500;
            }
            if (wanderCooldown == 0 && entityData.get(OBSERVING)) {
                tryShortWalk(server);
                wanderCooldown = 160 + getRandom().nextInt(180);
            }
        }
    }

    private Player nearestValidPlayer(double radius) {
        Player player = level().getNearestPlayer(this, radius);
        return player != null && player.isAlive() && !player.isSpectator() ? player : null;
    }

    private void enterState(byte newState) {
        if (entityData.get(STATE) == newState) return;
        entityData.set(STATE, newState);
        stateTicks = 0;
        if (newState == DORMANT || newState == AWAKENING
                || newState == RETURNING || newState == DEFENSIVE) {
            getNavigation().stop();
        }
        if (newState == DORMANT) {
            idleWithoutPlayer = 0;
            entityData.set(OBSERVING, false);
        }
    }

    /** Bounded search of already-loaded terrain close to the original island. */
    private void tryShortWalk(ServerLevel level) {
        if (islandHome == null) return; // /summon away from an island stays safe.
        for (int attempt = 0; attempt < 5; attempt++) {
            int x = islandHome.getX() + getRandom().nextInt(17) - 8;
            int z = islandHome.getZ() + getRandom().nextInt(17) - 8;
            BlockPos probe = new BlockPos(x, islandHome.getY(), z);
            if (!level.hasChunkAt(probe)) continue;
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos target = new BlockPos(x, y, z);
            if (Math.abs(y - islandHome.getY()) > 3
                    || !VigiaIslandHabitat.habitat().isValid(level, target)) continue;
            if (getNavigation().moveTo(x + 0.5, y, z + 0.5, 0.42)) return;
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("VigiaState", entityData.get(STATE));
        output.putInt("VigiaStateTicks", stateTicks);
        output.putInt("VigiaIdleWithoutPlayer", idleWithoutPlayer);
        output.putInt("VigiaWanderCooldown", wanderCooldown);
        output.putInt("VigiaInspectCooldown", inspectCooldown);
        output.putInt("VigiaReactionCooldown", reactionCooldown);
        output.putBoolean("VigiaHasHome", islandHome != null);
        if (islandHome != null) {
            output.putInt("VigiaHomeX", islandHome.getX());
            output.putInt("VigiaHomeY", islandHome.getY());
            output.putInt("VigiaHomeZ", islandHome.getZ());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        byte state = (byte) Mth.clamp(input.getIntOr("VigiaState", DORMANT), DORMANT, RETURNING);
        stateTicks = Mth.clamp(input.getIntOr("VigiaStateTicks", 0), 0, 100);
        idleWithoutPlayer = Mth.clamp(input.getIntOr("VigiaIdleWithoutPlayer", 0), 0, SLEEP_AFTER_TICKS);
        wanderCooldown = Mth.clamp(input.getIntOr("VigiaWanderCooldown", 140), 0, 400);
        inspectCooldown = Mth.clamp(input.getIntOr("VigiaInspectCooldown", 260), 0, 600);
        reactionCooldown = Mth.clamp(input.getIntOr("VigiaReactionCooldown", 0), 0, DAMAGE_REACTION_COOLDOWN);
        if (input.getBooleanOr("VigiaHasHome", false)) {
            islandHome = new BlockPos(input.getIntOr("VigiaHomeX", 0),
                    input.getIntOr("VigiaHomeY", 0), input.getIntOr("VigiaHomeZ", 0));
        } else {
            islandHome = null;
        }
        homeChecked = islandHome != null;
        entityData.set(STATE, state);
        entityData.set(OBSERVING, false);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // One exclusive controller: all nine clips animate overlapping body bones.
        controllers.add(new AnimationController<VigiaAdormecidoEntity>(CONTROLLER, 5, test -> {
            return test.setAndContinue(switch (entityData.get(STATE)) {
                case AWAKENING -> AWAKEN;
                case WATCHING -> entityData.get(OBSERVING) ? OBSERVE : IDLE_AWAKE;
                case MOVING -> WALK;
                case DEFENSIVE -> DEFEND;
                case RETURNING -> RETURN;
                default -> IDLE_DORMANT;
            });
        }).triggerableAnim("alert", ALERT)
          .triggerableAnim("inspect_ground", INSPECT));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }
}
