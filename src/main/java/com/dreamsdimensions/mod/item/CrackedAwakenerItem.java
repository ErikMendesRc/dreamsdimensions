package com.dreamsdimensions.mod.item;

import com.dreamsdimensions.mod.service.DreamHangoverService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * A genuinely slower emergency return, never craftable.
 * Reuses the audited atomic 60-tick item return engine, not a second teleport path.
 */
public final class CrackedAwakenerItem extends OneiricAwakenerItem {
    public static final int EMERGENCY_TICKS = 200;

    public CrackedAwakenerItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return EMERGENCY_TICKS;
    }

    @Override
    protected void afterConfirmedReturn(ServerPlayer returned) {
        DreamHangoverService.afterCrackedReturn(returned);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
            TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.dreamsdimensions.cracked_awakener.line1")
                .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.dreamsdimensions.cracked_awakener.line2")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }
}
