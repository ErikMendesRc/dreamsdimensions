package com.dreamsdimensions.mod.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.function.Consumer;

/** Read-only personal exploration journal; only recorded dimensions appear. */
public final class DreamJournalItem extends Item {
    public static final String VISITED="DreamsDimensionsVisited";
    public DreamJournalItem(Properties properties){super(properties);}

    @Override public void appendHoverText(ItemStack stack,TooltipContext context,
            TooltipDisplay display,Consumer<Component> tooltip,TooltipFlag flag) {
        CustomData data=stack.get(DataComponents.CUSTOM_DATA);
        String visited=data==null?"":data.copyTag().getStringOr(VISITED,"");
        tooltip.accept(Component.translatable(
                "tooltip.dreamsdimensions.journal.intro").withStyle(ChatFormatting.GRAY));
        if(visited.isEmpty()) {
            tooltip.accept(Component.translatable(
                    "tooltip.dreamsdimensions.journal.none").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        for(String key:visited.split(","))if(!key.isEmpty())
            tooltip.accept(Component.literal("• "+key).withStyle(ChatFormatting.AQUA));
    }
}
