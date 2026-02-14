package com.dreamsdimensions.mod.event;

import com.dreamsdimensions.mod.registry.ModCreativeTabs;
import com.dreamsdimensions.mod.registry.ModPotions;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;

public final class CreativeTabEvents {
    private CreativeTabEvents() {
    }

    public static void onBuildCreativeTabContents(BuildCreativeModeTabContentsEvent event) {
        if (!event.getTabKey().equals(ModCreativeTabs.DREAMS_DIMENSIONS_TAB.getKey())) {
            return;
        }

        event.accept(PotionContents.createItemStack(Items.POTION, ModPotions.OW_ONEIRIC_BASE));
        event.accept(PotionContents.createItemStack(Items.POTION, ModPotions.OW_POTION_OF_ANCHORING));
        event.accept(PotionContents.createItemStack(Items.POTION, ModPotions.OW_POTION_OF_CLARITY));
        event.accept(PotionContents.createItemStack(Items.POTION, ModPotions.OW_POTION_OF_ETHEREAL_PHASE));
        event.accept(PotionContents.createItemStack(Items.POTION, ModPotions.OW_POTION_OF_EARLY_AWAKENING));
    }
}
