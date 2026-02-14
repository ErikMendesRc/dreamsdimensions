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
import org.slf4j.Logger;

public class OneiricAwakenerItem extends Item {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int USE_DURATION_TICKS = 60;
    public static final int COOLDOWN_TICKS = 60;

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

    // =========================
    // USE (Right Click)
    // =========================

    @Override
    public InteractionResult use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {

        ItemStack stack = pPlayer.getItemInHand(pUsedHand);

        String dimensionId = pLevel.dimension().identifier().toString();
        boolean isClient = pLevel.isClientSide();
        boolean allowed = DreamsConfig.isDreamDimension(pLevel.dimension());
        boolean onCooldown = pPlayer.getCooldowns().isOnCooldown(stack);

        LOGGER.info(
                "[Awakener][USE][{}] Player={} Dimension={} Allowed={} Cooldown={} isUsingItem={} isSleeping={}",
                isClient ? "CLIENT" : "SERVER",
                pPlayer.getName().getString(),
                dimensionId,
                allowed,
                onCooldown,
                pPlayer.isUsingItem(),
                (pPlayer instanceof ServerPlayer sp) ? sp.isSleeping() : "N/A"
        );

        if (!allowed) {
            LOGGER.warn("[Awakener][USE] Blocked: dimension is not configured as dream.");
            return InteractionResult.PASS;
        }

        if (onCooldown) {
            LOGGER.warn("[Awakener][USE] Blocked: item is on cooldown.");
            return InteractionResult.FAIL;
        }

        pPlayer.startUsingItem(pUsedHand);
        LOGGER.info("[Awakener][USE] startUsingItem triggered successfully.");

        return InteractionResult.CONSUME;
    }

    // =========================
    // FINISH (After 60 ticks)
    // =========================

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {

        // Esse método roda nos dois lados; teleporte só no server.
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
                "[Awakener][FINISH][SERVER] Player={} Dimension={} Allowed={} isUsingItem={} isSleeping={}",
                serverPlayer.getName().getString(),
                dimBefore,
                allowed,
                serverPlayer.isUsingItem(),
                serverPlayer.isSleeping()
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

            LOGGER.info("[Awakener][FINISH] Teleport transition created. Executing teleport...");
            serverPlayer.teleport(transition);

            LOGGER.info(
                    "[Awakener][FINISH] AFTER TELEPORT: Level={} Pos=({}, {}, {}) Rot=({}, {}) isUsingItem={} isSleeping={}",
                    serverPlayer.level().dimension().identifier(),
                    serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    serverPlayer.getYRot(), serverPlayer.getXRot(),
                    serverPlayer.isUsingItem(),
                    serverPlayer.isSleeping()
            );

            // ===== Hard sync / limpar estados que podem prender o client =====
            serverPlayer.stopUsingItem();
            serverPlayer.closeContainer();

            // Força o client a aceitar imediatamente a posição/rotação atual (resolve “precisa usar 2x” em muitos casos)
            if (serverPlayer.connection != null) {
                serverPlayer.connection.teleport(
                        serverPlayer.getX(),
                        serverPlayer.getY(),
                        serverPlayer.getZ(),
                        serverPlayer.getYRot(),
                        serverPlayer.getXRot()
                );
                LOGGER.info("[Awakener][FINISH] Post-teleport hard sync sent to client.");
            } else {
                LOGGER.warn("[Awakener][FINISH] Post-teleport hard sync skipped: connection is null.");
            }

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

    // =========================

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