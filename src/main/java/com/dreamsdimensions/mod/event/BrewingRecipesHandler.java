package com.dreamsdimensions.mod.event;

import com.dreamsdimensions.mod.registry.ModItems;
import com.dreamsdimensions.mod.registry.ModPotions;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;

public final class BrewingRecipesHandler {
    private BrewingRecipesHandler() {
    }

    public static void onRegisterBrewingRecipes(RegisterBrewingRecipesEvent event) {
        PotionBrewing.Builder builder = event.getBuilder();

        builder.addMix(Potions.AWKWARD, ModItems.OW_REFINED_ONEIRIC_POWDER.get(), ModPotions.OW_ONEIRIC_BASE);

        builder.addMix(ModPotions.OW_ONEIRIC_BASE, ModItems.OW_DREAM_BINDING_THREAD.get(), ModPotions.OW_POTION_OF_ANCHORING);
        builder.addMix(ModPotions.OW_ONEIRIC_BASE, ModItems.OW_DREAM_CATALYST.get(), ModPotions.OW_POTION_OF_CLARITY);
        builder.addMix(ModPotions.OW_ONEIRIC_BASE, ModItems.OW_CONDENSED_DREAM_CRYSTAL.get(), ModPotions.OW_POTION_OF_ETHEREAL_PHASE);
        builder.addMix(ModPotions.OW_ONEIRIC_BASE, Items.TOTEM_OF_UNDYING, ModPotions.OW_POTION_OF_EARLY_AWAKENING);
    }
}
