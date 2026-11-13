package com.dreamsdimensions.mod.service;

import com.dreamsdimensions.mod.dimension.DreamDimensionCatalog;
import com.dreamsdimensions.mod.dimension.DreamTrait;
import com.dreamsdimensions.mod.registry.ModEffects;
import com.dreamsdimensions.mod.registry.ModItems;
import net.minecraft.core.component.DataComponents;
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
        double damageMultiplier=1.0;
        for(int i=0;i<Math.min(2,matched);i++) damageMultiplier*=0.65;
        if(player.hasEffect(ModEffects.LIGHTNESS))damageMultiplier*=0.5;
        event.setDamageMultiplier((float)(event.getDamageMultiplier()*damageMultiplier));
    }

    /** Right-click smithing table with Lining in main hand, armor in offhand. */
    public static void onSmithing(PlayerInteractEvent.RightClickBlock event) {
        if(event.getHand()!=InteractionHand.MAIN_HAND || !event.getLevel().getBlockState(event.getPos()).is(Blocks.SMITHING_TABLE))return;
        Player player=event.getEntity();
        ItemStack lining=player.getMainHandItem();
        if(!lining.is(ModItems.PLUME_LINING.get()))return;
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
        boolean previous="plumite".equals(tag.getStringOr(LINING_KEY,""));
        tag.putString(LINING_KEY,"plumite");
        armor.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
        if(!player.getAbilities().instabuild)lining.shrink(1);
        if(previous){
            ItemStack returned=new ItemStack(ModItems.PLUME_LINING.get());
            if(!player.getInventory().add(returned))player.drop(returned,false);
        }
    }
}
