package com.dreamsdimensions.mod.item;

import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import java.util.List;

/**
 * Item ritualístico que representa o foco da mente do jogador para “atravessar o véu”.
 *
 * <p>Comportamento atual: inicia um uso com duração fixa e, ao concluir, emite uma mensagem
 * indicando que a lógica de teleporte ainda é um placeholder (não implementada).</p>
 *
 * <p>Regras de lado (side rules): alterações de estado (consumo do item e cooldown) e logs
 * são feitas apenas no servidor com {@link Level#isClientSide()} para evitar duplicações.</p>
 *
 * <p>Cooldown é verificado com {@link ItemStack} porque o SDK de {@link net.minecraft.world.item.ItemCooldowns}
 * aceita stack (não {@link Item}) nas assinaturas atuais, preservando componentes e comportamento vanilla.</p>
 *
 * <p>Cooldown e consumo são aplicados apenas no servidor para evitar duplicar efeitos e manter a autoridade
 * do estado do jogo (o cliente apenas apresenta efeitos visuais).</p>
 *
 *
 * <p>APIs do SDK referenciadas: {@link Item#use(Level, Player, InteractionHand)},
 * {@link Item#finishUsingItem(ItemStack, Level, LivingEntity)}, {@link Player#getCooldowns()},
 * {@link UseCooldown} via {@link DataComponents#USE_COOLDOWN} e {@link InteractionResult}.</p>
 */
public class OneiricAwakenerItem extends Item {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int USE_DURATION_TICKS = 60;
    public static final int COOLDOWN_TICKS = 100;

    public OneiricAwakenerItem(Properties pProperties) {
        super(pProperties);
    }

    // --- Duração e Animação (Permanecem iguais) ---

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
        if (pPlayer.getCooldowns().isOnCooldown(stack)) {
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

            if (!player.getAbilities().instabuild) {
                pStack.shrink(1);
            }

            int cooldownTicks = COOLDOWN_TICKS;
            UseCooldown cooldown = pStack.get(DataComponents.USE_COOLDOWN);
            if (cooldown != null) {
                cooldownTicks = cooldown.ticks();
            }
            player.getCooldowns().addCooldown(pStack, cooldownTicks);

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
