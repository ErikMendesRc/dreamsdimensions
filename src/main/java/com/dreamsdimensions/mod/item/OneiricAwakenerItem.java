package com.dreamsdimensions.mod.item;

import com.dreamsdimensions.mod.config.DreamsConfig;
import com.dreamsdimensions.mod.util.DreamReturnHelper;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.slf4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class OneiricAwakenerItem extends Item {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int USE_DURATION_TICKS = 60;
    public static final int COOLDOWN_TICKS = 60;
    private static final Map<UUID, Integer> LAST_REMAINING_TICKS = new ConcurrentHashMap<>();

    public OneiricAwakenerItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public int getUseDuration(ItemStack pStack, LivingEntity pEntity) {
        return USE_DURATION_TICKS;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack pStack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public InteractionResult use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {

        ItemStack stack = pPlayer.getItemInHand(pUsedHand);

        String dimensionId = pLevel.dimension().identifier().toString();
        boolean isClient = pLevel.isClientSide();
        boolean allowed = DreamsConfig.isDreamDimension(pLevel.dimension());
        boolean onCooldown = pPlayer.getCooldowns().isOnCooldown(stack);

        LOGGER.info(
                "[Awakener][USE][{}] Player={} Dimension={} Allowed={} Cooldown={} isUsingItem={} held={} gameTime={}",
                isClient ? "CLIENT" : "SERVER",
                pPlayer.getName().getString(),
                dimensionId,
                allowed,
                onCooldown,
                pPlayer.isUsingItem(),
                stack,
                pLevel.getGameTime()
        );

        if (!allowed) {
            LOGGER.warn("[Awakener][USE] Blocked: dimension is not configured as dream.");
            return InteractionResult.PASS;
        }

        if (onCooldown) {
            LOGGER.warn("[Awakener][USE] Blocked: item is on cooldown.");
            return InteractionResult.FAIL;
        }

        if (!isClient && pPlayer instanceof ServerPlayer serverPlayer && serverPlayer.isSleepingLongEnough()) {
            LOGGER.warn("[Awakener][USE] Blocked: player is in sleep teleport flow.");
            return InteractionResult.FAIL;
        }

        pPlayer.startUsingItem(pUsedHand);
        LOGGER.info("[Awakener][USE] startUsingItem triggered successfully.");

        return pLevel.isClientSide() ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {

        if (!(pLivingEntity instanceof ServerPlayer serverPlayer)) {
            if (pLevel.isClientSide()) {
                LOGGER.debug("[Awakener][FINISH][CLIENT] Ignored: client-side finish.");
            } else {
                LOGGER.warn("[Awakener][FINISH][SERVER] Ignored: not a ServerPlayer (unexpected).");
            }
            return pStack;
        }

        String dimBefore = serverPlayer.level().dimension().identifier().toString();
        boolean allowed = DreamReturnHelper.isDreamDimension(serverPlayer.level());

        LOGGER.info(
                "[Awakener][FINISH][SERVER] Player={} Dimension={} Allowed={} isUsingItem={} remaining={} isSleeping={} gameTime={}",
                serverPlayer.getName().getString(),
                dimBefore,
                allowed,
                serverPlayer.isUsingItem(),
                serverPlayer.getUseItemRemainingTicks(),
                serverPlayer.isSleeping(),
                serverPlayer.level().getGameTime()
        );

        if (!allowed) {
            LOGGER.warn("[Awakener][FINISH] Blocked: not in a dream dimension.");
            return pStack;
        }

        DreamReturnHelper.buildReturnTransition(serverPlayer).ifPresentOrElse(transition -> {

            LOGGER.info(
                    "[Awakener][FINISH] BEFORE TELEPORT: Level={} Pos=({}, {}, {}) Rot=({}, {}) isUsingItem={} isSleeping={}",
                    serverPlayer.level().dimension().identifier(),
                    serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    serverPlayer.getYRot(), serverPlayer.getXRot(),
                    serverPlayer.isUsingItem(),
                    serverPlayer.isSleeping()
            );

            serverPlayer.teleport(transition);

            LOGGER.info(
                    "[Awakener][FINISH] AFTER TELEPORT: Level={} Pos=({}, {}, {}) Rot=({}, {})",
                    serverPlayer.level().dimension().identifier(),
                    serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    serverPlayer.getYRot(), serverPlayer.getXRot()
            );

            serverPlayer.stopUsingItem();
            serverPlayer.closeContainer();

            serverPlayer.displayClientMessage(
                    Component.translatable("message.dreamsdimensions.ow_oneiric_awakener.success"),
                    false
            );

            int cooldownTicks = COOLDOWN_TICKS;
            UseCooldown cooldown = pStack.get(DataComponents.USE_COOLDOWN);
            if (cooldown != null) {
                cooldownTicks = cooldown.ticks();
            }

            serverPlayer.getCooldowns().addCooldown(pStack, cooldownTicks);

            LOGGER.info("[Awakener][FINISH] Teleport successful. Cooldown applied: {} ticks", cooldownTicks);

        }, () -> {
            LOGGER.error("[Awakener][FINISH] Failed to build teleport transition.");
            serverPlayer.displayClientMessage(
                    Component.translatable("message.dreamsdimensions.ow_oneiric_awakener.fail"),
                    true
            );
        });

        return pStack;
    }

    public static void onServerPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        UUID id = serverPlayer.getUUID();

        boolean hasAwakenerInHand = serverPlayer.getMainHandItem().getItem() instanceof OneiricAwakenerItem
                || serverPlayer.getOffhandItem().getItem() instanceof OneiricAwakenerItem;
        boolean usingAwakener = serverPlayer.isUsingItem() && serverPlayer.getUseItem().getItem() instanceof OneiricAwakenerItem;

        if (!hasAwakenerInHand && !usingAwakener) {
            LAST_REMAINING_TICKS.remove(id);
            return;
        }

        int remaining = serverPlayer.getUseItemRemainingTicks();
        Integer lastRemaining = LAST_REMAINING_TICKS.get(id);

        if (lastRemaining == null || lastRemaining != remaining) {
            LOGGER.info("[Awakener][TICK][SERVER] player={} using={} remaining={} dim={} isSleeping={}",
                    serverPlayer.getGameProfile().name(),
                    usingAwakener,
                    remaining,
                    serverPlayer.level().dimension().identifier(),
                    serverPlayer.isSleeping());
            LAST_REMAINING_TICKS.put(id, remaining);
        }
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(
            ItemStack stack,
            Item.TooltipContext context,
            TooltipDisplay tooltipDisplay,
            java.util.function.Consumer<Component> tooltipAdder,
            TooltipFlag flag
    ) {
        tooltipAdder.accept(
                Component.translatable("tooltip.dreamsdimensions.ow_oneiric_awakener.line1")
                        .withStyle(ChatFormatting.GRAY)
        );

        tooltipAdder.accept(
                Component.translatable("tooltip.dreamsdimensions.ow_oneiric_awakener.line2")
                        .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)
        );

        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
    }
}
