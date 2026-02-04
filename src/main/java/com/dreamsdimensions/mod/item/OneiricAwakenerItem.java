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
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.Consumer;

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

    @Override
    public InteractionResult use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);
        if (!DreamsConfig.isDreamDimension(pLevel.dimension())) {
            return InteractionResult.PASS;
        }
        if (pPlayer.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        pPlayer.startUsingItem(pUsedHand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        if (!(pLivingEntity instanceof ServerPlayer serverPlayer)) {
            return pStack;
        }

        // serverLevel() -> level()
        if (!DreamReturnHelper.isDreamDimension(serverPlayer.level())) {
            return pStack;
        }

        LOGGER.info(
                "Oneiric Awakener totalmente focado por {} na dimensão {}",
                serverPlayer.getName().getString(),
                pLevel.dimension().location()
        );

        DreamReturnHelper.buildReturnTransition(serverPlayer).ifPresentOrElse(transition -> {
            serverPlayer.teleport(transition);
            serverPlayer.displayClientMessage(
                    Component.translatable("message.dreamsdimensions.oneiric_awakener.success"),
                    false
            );

            int cooldownTicks = COOLDOWN_TICKS;
            UseCooldown cooldown = pStack.get(DataComponents.USE_COOLDOWN);
            if (cooldown != null) {
                cooldownTicks = cooldown.ticks();
            }
            serverPlayer.getCooldowns().addCooldown(pStack, cooldownTicks);

        }, () -> serverPlayer.displayClientMessage(
                Component.translatable("message.dreamsdimensions.oneiric_awakener.fail"),
                true
        ));

        return pStack;
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
        tooltipAdder.accept(Component.translatable("tooltip.dreamsdimensions.oneiric_awakener.line1")
                .withStyle(ChatFormatting.GRAY));
        tooltipAdder.accept(Component.translatable("tooltip.dreamsdimensions.oneiric_awakener.line2")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));

        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
    }
}