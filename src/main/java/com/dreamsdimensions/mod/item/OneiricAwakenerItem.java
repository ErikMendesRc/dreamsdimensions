package com.dreamsdimensions.mod.item;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import java.util.List;

public class OneiricAwakenerItem extends Item {
    private static final Logger LOGGER = LogUtils.getLogger();

    public OneiricAwakenerItem(Properties pProperties) {
        super(pProperties);
    }

    // --- Duração e Animação (Permanecem iguais) ---

    @Override
    public int getUseDuration(ItemStack pStack, LivingEntity pEntity) {
        return 60;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack pStack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public InteractionResult use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);

        if (pPlayer.getCooldowns().isOnCooldown(itemstack)) {
            return InteractionResult.FAIL;
        }

        pPlayer.startUsingItem(pUsedHand);

        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        if (!(pLivingEntity instanceof Player player)) {
            return pStack;
        }

        if (!pLevel.isClientSide()) {
            LOGGER.info("Oneiric Awakener totalmente focado por {} na dimensão {}", player.getName().getString(), pLevel.dimension().location());

            player.displayClientMessage(Component.literal("Sua mente atravessa o véu! (Lógica de teleporte ainda não implementada)"), false);

            // --- Consome o item AGORA ---
            pStack.shrink(1);

            // --- Aplica o Cooldown ---
            UseCooldown cooldown = pStack.get(DataComponents.USE_COOLDOWN);
            if (cooldown != null) {
                player.getCooldowns().addCooldown(pStack, cooldown.ticks());
            }

            // Futura lógica de teleporte viria aqui...
        }

        // Efeitos visuais/sonoros do lado do cliente podem ser adicionados aqui

        return pStack;
    }


    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        pTooltipComponents.add(Component.translatable("tooltip.dreamsdimensions.oneiric_awakener.line1").withStyle(ChatFormatting.GRAY));
        pTooltipComponents.add(Component.translatable("tooltip.dreamsdimensions.oneiric_awakener.line2").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
    }
}