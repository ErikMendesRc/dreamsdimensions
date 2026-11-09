package com.dreamsdimensions.mod.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.util.GeckoLibUtil;

/**
 * Pilot entity for GeckoLib 5. No natural spawning or player targeting yet.
 * Visual bobbing is animated in the renderer; server hitbox stays unchanged.
 */
public final class FragmentoErranteEntity extends PathfinderMob implements GeoEntity {
    private static final RawAnimation IDLE = RawAnimation.begin()
            .thenLoop("animation.fragmento_errante.idle");
    private static final RawAnimation MOVE = RawAnimation.begin()
            .thenLoop("animation.fragmento_errante.move");

    private final AnimatableInstanceCache geoCache = GeckoLibUtil.createInstanceCache(this);

    public FragmentoErranteEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 12.0)
                .add(Attributes.MOVEMENT_SPEED, 0.22)
                .add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        // Prevent gravity from re-enabling on load without moving the hitbox
        // to match the cosmetic Blockbench float animation.
        if (!this.isNoGravity()) {
            this.setNoGravity(true);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(test ->
                test.setAndContinue(test.isMoving() ? MOVE : IDLE))
                .setTransitionTicks(5));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.geoCache;
    }
}
