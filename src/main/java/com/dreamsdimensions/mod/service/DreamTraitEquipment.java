package com.dreamsdimensions.mod.service;

import com.dreamsdimensions.mod.dimension.DreamDimensionCatalog;
import com.dreamsdimensions.mod.dimension.DreamTrait;
import com.dreamsdimensions.mod.registry.ModEffects;
import com.dreamsdimensions.mod.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/** Shared material->trait response, independent from Dreamscape-specific code. */
public final class DreamTraitEquipment {
    private static final String LINING_KEY="DreamsDimensionsLining";
    private DreamTraitEquipment(){}

    private static boolean inFallDream(Player player) {
        return DreamDimensionCatalog.find(player.level().dimension())
                .map(d->d.traits().contains(DreamTrait.FALL)).orElse(false);
    }

    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player)||!inFallDream(player))return;
        int matched=0;
        for(EquipmentSlot slot: new EquipmentSlot[] {
                EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET}) {
            ItemStack gear=player.getItemBySlot(slot);
            CustomData custom=gear.get(DataComponents.CUSTOM_DATA);
            if(custom!=null && "plumite".equals(custom.copyTag().getStringOr(LINING_KEY,"")))matched++;
        }
        float multiplier = DreamFallPolicy.damageMultiplier(
                matched, player.hasEffect(ModEffects.LIGHTNESS));
        event.setDamageMultiplier(event.getDamageMultiplier() * multiplier);
    }

    /** The lining must be discoverable on the equipped item itself. */
    public static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().is(ModItems.PLUME_LINING.get())) {
            event.getToolTip().add(Component.translatable(
                    "tooltip.dreamsdimensions.plume_lining").withStyle(ChatFormatting.GRAY));
        } else if (event.getItemStack().is(ModItems.VIGIL_TETHER.get())) {
            event.getToolTip().add(Component.translatable(
                    "tooltip.dreamsdimensions.vigil_tether").withStyle(ChatFormatting.GRAY));
        } else if (event.getItemStack().is(ModItems.MARKED_LINING.get())) {
            event.getToolTip().add(Component.translatable(
                    "tooltip.dreamsdimensions.marked_lining").withStyle(ChatFormatting.GRAY));
        } else if (event.getItemStack().is(ModItems.PLUME_BRIDGE.get())) {
            event.getToolTip().add(Component.translatable(
                    "tooltip.dreamsdimensions.plume_bridge").withStyle(ChatFormatting.GRAY));
        }
        CustomData data = event.getItemStack().get(DataComponents.CUSTOM_DATA);
        if (data != null) {
            String installed=data.copyTag().getStringOr(LINING_KEY, "");
            if ("plumite".equals(installed)) event.getToolTip().add(Component.translatable(
                    "tooltip.dreamsdimensions.armor.plume_lining").withStyle(ChatFormatting.AQUA));
            if ("marked".equals(installed)) event.getToolTip().add(Component.translatable(
                    "tooltip.dreamsdimensions.armor.marked_lining").withStyle(ChatFormatting.GOLD));
        }
    }

    /** Right-click smithing table with Lining in main hand, armor in offhand. */
    public static void onSmithing(PlayerInteractEvent.RightClickBlock event) {
        if(event.getHand()!=InteractionHand.MAIN_HAND || !event.getLevel().getBlockState(event.getPos()).is(Blocks.SMITHING_TABLE))return;
        Player player=event.getEntity();
        ItemStack lining=player.getMainHandItem();
        boolean marked=lining.is(ModItems.MARKED_LINING.get());
        if(!lining.is(ModItems.PLUME_LINING.get())&&!marked)return;
        ItemStack armor=player.getOffhandItem();
        var equippable=armor.get(DataComponents.EQUIPPABLE);
        if(equippable==null || !armor.isDamageableItem()
                || !(equippable.slot()==EquipmentSlot.HEAD||equippable.slot()==EquipmentSlot.CHEST
                ||equippable.slot()==EquipmentSlot.LEGS||equippable.slot()==EquipmentSlot.FEET))return;
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if(event.getLevel().isClientSide())return;
        CustomData old=armor.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag=old==null?new CompoundTag():old.copyTag();
        String previous=tag.getStringOr(LINING_KEY,"");
        String current=marked?"marked":"plumite";
        if (current.equals(previous)) {
            player.sendSystemMessage(Component.translatable(
                    "message.dreamsdimensions.plume_lining.already"));
            return;
        }
        // Replacing a different lining returns exactly the older material.
        if(!previous.isEmpty()){
            ItemStack replaced=new ItemStack("marked".equals(previous)
                    ?ModItems.MARKED_LINING.get():ModItems.PLUME_LINING.get());
            if(!player.getInventory().add(replaced))
                net.minecraft.world.level.block.Block.popResource(player.level(),
                        player.blockPosition(),replaced);
        }
        tag.putString(LINING_KEY,current);
        armor.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        if(!player.getAbilities().instabuild)lining.shrink(1);
        player.sendSystemMessage(Component.translatable(
                marked?"message.dreamsdimensions.marked_lining.applied":
                        "message.dreamsdimensions.plume_lining.applied"));
    }
}
