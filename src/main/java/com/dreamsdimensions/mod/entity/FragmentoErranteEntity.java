package com.dreamsdimensions.mod.entity;

import com.geckolib.animatable.GeoEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;
import java.util.EnumSet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import com.dreamsdimensions.mod.entity.movement.DreamFlyingMoveControl;
import com.dreamsdimensions.mod.entity.movement.FlightProfile;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * A gentle flying creature. All decisions and velocity changes are made on the
 * logical server. Only a three-value locomotion state is replicated; GeckoLib
 * propagates the non-looping reactions to tracking clients.
 */
public final class FragmentoErranteEntity extends PathfinderMob implements GeoEntity {
    private static final String CONTROLLER = "fragmento";
    private static final String ANIM_CURIOUS = "curious";
    private static final String ANIM_STARTLED = "startled";
    private static final String ANIM_DISAPPEAR = "disappear";

    private static final byte REST = 0;
    private static final byte TRAVEL = 1;
    private static final byte ASCEND = 2;

    private static final int DISAPPEAR_TICKS = 27; // 1.2s at 20 TPS + 3 transition ticks
    private static final int FLEE_TICKS = 38;
    private static final int STARTLE_COOLDOWN_TICKS = 120;
    private static final int MAX_SAVE_COOLDOWN = 400;
    private static final int RESONANCE_TICKS = 90;
    private static final int RESONANCE_COOLDOWN = 1800;
    private static final int SLOW_FALLING_TICKS = 360;
    private static final int AGRESSION_WINDOW = 600;

    // Physical (server-side) hover in world blocks; visual Geo bones add a
    // second, cosmetic layer without changing the entity's collision bounds.
    private static final double HOVER_Y_AMPLITUDE = 0.14;
    private static final double HOVER_XZ_AMPLITUDE = 0.035;
    private static final double HOVER_PERIOD_TICKS = 64.0;
    private static final FlightProfile FLIGHT_PROFILE = new FlightProfile(
            0.17, 0.08, 0.19, 0.028, 0.78, 0.70, 0.50, 0.10,
            0.018, 0.015, 0.18, 0.87, 0.12,
            HOVER_Y_AMPLITUDE, HOVER_XZ_AMPLITUDE, HOVER_PERIOD_TICKS);

    private static final EntityDataAccessor<Byte> MOTION =
            SynchedEntityData.defineId(FragmentoErranteEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> DISAPPEARING =
            SynchedEntityData.defineId(FragmentoErranteEntity.class, EntityDataSerializers.BOOLEAN);

    private static final RawAnimation IDLE = RawAnimation.begin()
            .thenLoop("animation.fragmento_errante.idle");
    private static final RawAnimation MOVE = RawAnimation.begin()
            .thenLoop("animation.fragmento_errante.move");
    private static final RawAnimation FLOAT = RawAnimation.begin()
            .thenLoop("animation.fragmento_errante.float");
    private static final RawAnimation CURIOUS = RawAnimation.begin()
            .thenPlay("animation.fragmento_errante.curious");
    private static final RawAnimation STARTLED = RawAnimation.begin()
            .thenPlay("animation.fragmento_errante.startled");
    private static final RawAnimation DISAPPEAR = RawAnimation.begin()
            .thenPlay("animation.fragmento_errante.disappear");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    private int idlePauseTicks = 30;
    private int curiosityCooldown = 40;
    private int startleCooldown;
    private int resonanceCooldown;
    private int aggressionWindowTicks;
    private int aggressionHits;
    private boolean vanishAfterFlee;
    private int fleeTicks;
    private int disappearTicks;
    private boolean removeAfterDisappearance;
    private boolean replayDisappearanceAfterLoad;
    private boolean reactedToCurrentDamage;
    private Vec3 threatPosition;
    private BlockPos islandHome;
    private boolean islandHabitatChecked;

    public FragmentoErranteEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        this.moveControl = new DreamFlyingMoveControl(this, FLIGHT_PROFILE);
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FLYING_SPEED, 0.16)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(MOTION, REST);
        builder.define(DISAPPEARING, false);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanFloat(true);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FleeGoal());
        this.goalSelector.addGoal(1, new ResonanceGoal());
        this.goalSelector.addGoal(2, new CuriosityGoal());
        this.goalSelector.addGoal(3, new WanderGoal());
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!isNoGravity()) setNoGravity(true);
        if (!(level() instanceof ServerLevel server)) return;

        // Only entities first detected on a floating island acquire a home
        // tether. Summoned mainland test mobs still behave as before.
        if (!this.islandHabitatChecked) {
            this.islandHabitatChecked = true;
            if (FragmentoIslandHabitat.hasNearbyIslandPerch(server, blockPosition())) {
                this.islandHome = blockPosition();
            }
        }
        if (this.resonanceCooldown > 0) this.resonanceCooldown--;
        if (this.aggressionWindowTicks > 0) this.aggressionWindowTicks--;
        else this.aggressionHits = 0;
        if (this.idlePauseTicks > 0) this.idlePauseTicks--;
        if (this.curiosityCooldown > 0) this.curiosityCooldown--;
        if (this.startleCooldown > 0) this.startleCooldown--;
        if (this.fleeTicks > 0 && --this.fleeTicks == 0) this.threatPosition = null;

        if (this.vanishAfterFlee && this.fleeTicks == 0 && this.disappearTicks == 0) {
            this.vanishAfterFlee = false;
            beginDisappearance(true);
        }
        if (this.disappearTicks > 0) {
            if (this.replayDisappearanceAfterLoad) {
                this.replayDisappearanceAfterLoad = false;
                this.disappearTicks = DISAPPEAR_TICKS;
                this.triggerAnim(CONTROLLER, ANIM_DISAPPEAR);
            }
            this.getNavigation().stop();
            brake();
            ((DreamFlyingMoveControl) this.moveControl).resetHover();
            if (--this.disappearTicks == 0) {
                this.entityData.set(DISAPPEARING, false);
                if (this.removeAfterDisappearance) {
                    discard();
                    return;
                }
            }
            this.entityData.set(MOTION, REST);
            return;
        }

        // A single damage event must not repeatedly retrigger the reaction.
        if (this.hurtTime > 0 && !this.reactedToCurrentDamage) {
            this.reactedToCurrentDamage = true;
            LivingEntity attacker = this.getLastHurtByMob();
            if (this.aggressionWindowTicks == 0) this.aggressionHits = 0;
            this.aggressionWindowTicks = AGRESSION_WINDOW;
            this.aggressionHits++;
            if (this.aggressionHits >= 2) this.vanishAfterFlee = true;
            startleAt(attacker != null ? attacker.position() : position().subtract(getLookAngle()));
        } else if (this.hurtTime == 0) {
            this.reactedToCurrentDamage = false;
        }

        // A rushing player is threatening only within a small radius; walking
        // and standing players can instead be observed by CuriosityGoal.
        if (this.tickCount % 10 == 0 && this.startleCooldown == 0) {
            Player player = level().getNearestPlayer(this, 3.0);
            if (player != null && !player.isSpectator() && player.isSprinting()) {
                startleAt(player.position());
            }
        }

        // The next position must already belong to an available chunk.
        // Never enter an unloaded chunk or request one for pathfinding.
        Vec3 next = position().add(getDeltaMovement().scale(2.0));
        if (!level().hasChunkAt(BlockPos.containing(next))) {
            this.getNavigation().stop();
            brake();
        }

        // No gravity means no natural levitation: gently orbit a fixed local
        // anchor during pauses. Never fight active path navigation or escape.
        boolean hovering = getNavigation().isDone() && this.fleeTicks == 0;
        if (hovering) {
            ((DreamFlyingMoveControl) this.moveControl).applyIdleHover();
        } else {
            ((DreamFlyingMoveControl) this.moveControl).resetHover();
        }

        Vec3 velocity = getDeltaMovement();
        double horizontal = Math.hypot(velocity.x, velocity.z);
        // The tiny physics bob is still IDLE, not a vertical-navigation loop.
        byte desired = hovering ? REST : velocity.lengthSqr() < 0.0010 ? REST
                : Math.abs(velocity.y) > 0.035 && Math.abs(velocity.y) > horizontal * 0.70
                ? ASCEND : TRAVEL;
        if (this.entityData.get(MOTION) != desired) this.entityData.set(MOTION, desired);
    }

    /** Reset only the Fragmento's active waypoint steering and transient rest anchor. */
    private void brake() {
        ((DreamFlyingMoveControl) this.moveControl).brake();
    }

    /**
     * Called only by explicit gameplay rules in a future task. No random
     * despawn is performed here. False plays the effect without discarding.
     */
    public boolean beginDisappearance(boolean removeAfterAnimation) {
        if (level().isClientSide() || !isAlive() || this.disappearTicks > 0) return false;
        this.disappearTicks = DISAPPEAR_TICKS;
        this.removeAfterDisappearance = removeAfterAnimation;
        this.fleeTicks = 0;
        this.threatPosition = null;
        this.vanishAfterFlee = false;
        this.getNavigation().stop();
        brake();
        this.entityData.set(DISAPPEARING, true);
        this.entityData.set(MOTION, REST);
        this.triggerAnim(CONTROLLER, ANIM_DISAPPEAR);
        return true;
    }

    private void startleAt(Vec3 threat) {
        if (this.startleCooldown > 0 || this.disappearTicks > 0) return;
        this.threatPosition = threat;
        this.fleeTicks = FLEE_TICKS;
        this.startleCooldown = STARTLE_COOLDOWN_TICKS;
        this.curiosityCooldown = Math.max(this.curiosityCooldown, 90);
        this.getNavigation().stop();
        brake();
        this.triggerAnim(CONTROLLER, ANIM_STARTLED);
    }

    /**
     * Target and all samples along the direct corridor must be loaded and
     * unobstructed. Pathfinding is still responsible for choosing waypoints.
     */
    private boolean isSafeDestination(Vec3 dest) {
        if (dest.y < level().getMinY() + 3 || dest.y > level().getMaxY() - 4) return false;
        Vec3 offset = dest.subtract(position());
        if (offset.lengthSqr() > 100) return false;
        if (this.islandHome != null) {
            if (dest.distanceToSqr(Vec3.atCenterOf(this.islandHome)) > 14.0 * 14.0
                    || !(level() instanceof ServerLevel server)
                    || !FragmentoIslandHabitat.hasNearbyIslandPerch(server, BlockPos.containing(dest))) {
                return false;
            }
        }
        for (int step = 1; step <= 5; step++) {
            Vec3 point = position().add(offset.scale(step / 5.0));
            BlockPos block = BlockPos.containing(point);
            if (!level().hasChunkAt(block)
                    || !level().getFluidState(block).isEmpty()
                    || !level().noCollision(this, getBoundingBox().move(point.subtract(position())))) {
                return false;
            }
        }
        return true;
    }

    private Vec3 tryWanderTarget(RandomSource random) {
        for (int i = 0; i < 7; i++) {
            Vec3 test = position().add(
                    (random.nextDouble() - 0.5) * 12.0,
                    (random.nextDouble() - 0.5) * 5.0,
                    (random.nextDouble() - 0.5) * 12.0);
            if (test.distanceToSqr(position()) > 3.0 && isSafeDestination(test)) return test;
        }
        return null;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("FragmentoIdlePause", this.idlePauseTicks);
        output.putInt("FragmentoCuriosityCooldown", this.curiosityCooldown);
        output.putInt("FragmentoStartleCooldown", this.startleCooldown);
        output.putInt("FragmentoResonanceCooldown", this.resonanceCooldown);
        output.putInt("FragmentoAggressionWindow", this.aggressionWindowTicks);
        output.putInt("FragmentoAggressionHits", this.aggressionHits);
        output.putBoolean("FragmentoVanishAfterFlee", this.vanishAfterFlee);
        output.putBoolean("FragmentoIslandBound", this.islandHome != null);
        if (this.islandHome != null) {
            output.putInt("FragmentoIslandX", this.islandHome.getX());
            output.putInt("FragmentoIslandY", this.islandHome.getY());
            output.putInt("FragmentoIslandZ", this.islandHome.getZ());
        }
        output.putInt("FragmentoDisappearTicks", this.disappearTicks);
        output.putBoolean("FragmentoRemoveAfterDisappear", this.removeAfterDisappearance);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.idlePauseTicks = Mth.clamp(input.getIntOr("FragmentoIdlePause", 30), 0, MAX_SAVE_COOLDOWN);
        this.curiosityCooldown = Mth.clamp(input.getIntOr("FragmentoCuriosityCooldown", 40), 0, MAX_SAVE_COOLDOWN);
        this.startleCooldown = Mth.clamp(input.getIntOr("FragmentoStartleCooldown", 0), 0, MAX_SAVE_COOLDOWN);
        this.resonanceCooldown = Mth.clamp(input.getIntOr("FragmentoResonanceCooldown", 0), 0, RESONANCE_COOLDOWN);
        this.aggressionWindowTicks = Mth.clamp(input.getIntOr("FragmentoAggressionWindow", 0), 0, AGRESSION_WINDOW);
        this.aggressionHits = Mth.clamp(input.getIntOr("FragmentoAggressionHits", 0), 0, 2);
        this.vanishAfterFlee = input.getBooleanOr("FragmentoVanishAfterFlee", false);
        if (input.getBooleanOr("FragmentoIslandBound", false)) {
            this.islandHome = new BlockPos(
                    input.getIntOr("FragmentoIslandX", 0),
                    input.getIntOr("FragmentoIslandY", 0),
                    input.getIntOr("FragmentoIslandZ", 0));
        } else {
            this.islandHome = null;
        }
        this.islandHabitatChecked = this.islandHome != null;
        this.disappearTicks = Mth.clamp(input.getIntOr("FragmentoDisappearTicks", 0), 0, DISAPPEAR_TICKS);
        this.removeAfterDisappearance = input.getBooleanOr("FragmentoRemoveAfterDisappear", false);
        this.replayDisappearanceAfterLoad = this.disappearTicks > 0;
        this.entityData.set(DISAPPEARING, this.disappearTicks > 0);
        this.fleeTicks = 0;
        this.threatPosition = null;
        ((DreamFlyingMoveControl) this.moveControl).resetHover();
        setNoGravity(true);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // ONE controller: idle/move/float overlap the root and energy bones
        // used by the one-shots. Separate controllers would fight each other.
        controllers.add(new AnimationController<FragmentoErranteEntity>(CONTROLLER, 4, test -> {
            if (this.entityData.get(DISAPPEARING)) return test.setAndContinue(DISAPPEAR);
            return test.setAndContinue(switch (this.entityData.get(MOTION)) {
                case TRAVEL -> MOVE;
                case ASCEND -> FLOAT;
                default -> IDLE;
            });
        }).triggerableAnim(ANIM_CURIOUS, CURIOUS)
          .triggerableAnim(ANIM_STARTLED, STARTLED)
          .triggerableAnim(ANIM_DISAPPEAR, DISAPPEAR));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return geoCache;
    }

    private final class WanderGoal extends Goal {
        private Vec3 target;
        private int elapsed;

        private WanderGoal() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (idlePauseTicks > 0 || fleeTicks > 0 || disappearTicks > 0
                    || !getNavigation().isDone()) return false;
            this.target = tryWanderTarget(getRandom());
            if (this.target == null) idlePauseTicks = 30;
            return this.target != null;
        }

        @Override
        public void start() {
            elapsed = 0;
            if (!getNavigation().moveTo(target.x, target.y, target.z, 0.75)) {
                idlePauseTicks = 40;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return fleeTicks == 0 && disappearTicks == 0
                    && ++elapsed < 110 && !getNavigation().isDone();
        }

        @Override
        public void stop() {
            getNavigation().stop();
            brake();
            idlePauseTicks = 35 + getRandom().nextInt(75);
            target = null;
        }
    }

    /**
     * A calm encounter: remain close without sprinting for 4.5 seconds.
     * Grants Slow Falling for 18s; a 90s per-creature cooldown prevents
     * constant reapplication. One listener at a time is server-authoritative.
     */
    private final class ResonanceGoal extends Goal {
        private Player listener;
        private int calmTicks;

        private ResonanceGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        private boolean isCalm(Player player) {
            return player != null && player.isAlive() && !player.isSpectator()
                    && !player.isSprinting()
                    && player.distanceToSqr(FragmentoErranteEntity.this) <= 3.5 * 3.5;
        }

        @Override
        public boolean canUse() {
            if (resonanceCooldown > 0 || fleeTicks > 0 || disappearTicks > 0
                    || vanishAfterFlee || startleCooldown > 0) return false;
            Player candidate = level().getNearestPlayer(FragmentoErranteEntity.this, 3.5);
            if (!isCalm(candidate)) return false;
            listener = candidate;
            return true;
        }

        @Override
        public void start() {
            calmTicks = 0;
            getNavigation().stop();
            brake();
            triggerAnim(CONTROLLER, ANIM_CURIOUS);
        }

        @Override
        public boolean canContinueToUse() {
            return resonanceCooldown == 0 && fleeTicks == 0 && disappearTicks == 0
                    && !vanishAfterFlee && isCalm(listener);
        }

        @Override
        public void tick() {
            getNavigation().stop();
            getLookControl().setLookAt(listener, 24, 24);
            // A player who keeps walking receives no free instant reward.
            if (listener.getDeltaMovement().horizontalDistanceSqr() > 0.018) {
                calmTicks = Math.max(0, calmTicks - 2);
                return;
            }
            if (++calmTicks < RESONANCE_TICKS) return;
            if (level() instanceof ServerLevel server) {
                listener.addEffect(new MobEffectInstance(
                        MobEffects.SLOW_FALLING, SLOW_FALLING_TICKS, 0, true, true));
                server.sendParticles(ParticleTypes.END_ROD,
                        getX(), getY() + 0.9, getZ(), 12, 0.4, 0.5, 0.4, 0.02);
            }
            resonanceCooldown = RESONANCE_COOLDOWN;
            curiosityCooldown = Math.max(curiosityCooldown, 60);
        }

        @Override
        public void stop() {
            idlePauseTicks = Math.max(idlePauseTicks, 35);
            listener = null;
            calmTicks = 0;
        }
    }

    private final class CuriosityGoal extends Goal {
        private Player observed;
        private int remaining;

        private CuriosityGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            if (curiosityCooldown > 0 || fleeTicks > 0 || disappearTicks > 0) return false;
            Player candidate = level().getNearestPlayer(FragmentoErranteEntity.this, 7.0);
            if (candidate == null || candidate.isSpectator() || !candidate.isAlive()) return false;
            observed = candidate;
            return true;
        }

        @Override
        public void start() {
            remaining = 36 + getRandom().nextInt(24);
            curiosityCooldown = 115 + getRandom().nextInt(110);
            getNavigation().stop();
            brake();
            triggerAnim(CONTROLLER, ANIM_CURIOUS);
            // Approach occasionally; always remain at least ~3 blocks away.
            if (observed.distanceToSqr(FragmentoErranteEntity.this) > 16
                    && getRandom().nextFloat() < 0.65f) {
                Vec3 away = position().subtract(observed.position()).normalize();
                Vec3 dest = observed.position().add(away.scale(3.3))
                        .add(0, Mth.clamp((observed.getEyeY() - getEyeY()) * 0.3, -1.0, 1.0), 0);
                if (isSafeDestination(dest)) {
                    getNavigation().moveTo(dest.x, dest.y, dest.z, 0.65);
                }
            }
        }

        @Override
        public boolean canContinueToUse() {
            return --remaining > 0 && fleeTicks == 0 && disappearTicks == 0
                    && observed != null && observed.isAlive()
                    && !observed.isSpectator() && observed.distanceToSqr(FragmentoErranteEntity.this) < 81;
        }

        @Override
        public void tick() {
            getLookControl().setLookAt(observed, 24, 24);
            if (observed.distanceToSqr(FragmentoErranteEntity.this) < 8.0) {
                getNavigation().stop();
                brake();
            }
        }

        @Override
        public void stop() {
            getNavigation().stop();
            brake();
            idlePauseTicks = Math.max(idlePauseTicks, 25);
            observed = null;
        }
    }

    private final class FleeGoal extends Goal {
        private FleeGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return fleeTicks > 0 && threatPosition != null && disappearTicks == 0;
        }

        @Override
        public void start() {
            getNavigation().stop();
            brake();
            Vec3 away = position().subtract(threatPosition);
            if (away.lengthSqr() < 0.001) {
                away = new Vec3(getRandom().nextDouble() - 0.5, 0, getRandom().nextDouble() - 0.5);
            }
            away = away.normalize();
            // A small fixed number of attempts; no continuous re-pathing.
            for (int i = 0; i < 6; i++) {
                Vec3 dest = position().add(away.scale(4.0 + getRandom().nextDouble() * 3.0))
                        .add((getRandom().nextDouble() - 0.5) * 2.0,
                             (getRandom().nextDouble() - 0.5) * 2.4,
                             (getRandom().nextDouble() - 0.5) * 2.0);
                if (isSafeDestination(dest) && getNavigation().moveTo(dest.x, dest.y, dest.z, 1.15)) break;
            }
        }

        @Override
        public boolean canContinueToUse() {
            return fleeTicks > 0 && disappearTicks == 0;
        }

        @Override
        public void tick() {
            if (threatPosition != null) {
                Vec3 away = position().subtract(threatPosition).normalize();
                getLookControl().setLookAt(position().add(away.scale(3.0)));
            }
        }

        @Override
        public void stop() {
            getNavigation().stop();
            brake();
            idlePauseTicks = Math.max(idlePauseTicks, 50);
            threatPosition = null;
            fleeTicks = 0;
        }
    }

}
