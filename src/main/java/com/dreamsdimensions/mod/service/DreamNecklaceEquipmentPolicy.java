package com.dreamsdimensions.mod.service;

import com.dreamsdimensions.mod.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import top.theillusivec4.curios.api.CuriosApi;

/**
 * Server authority: checks only ACTIVE, FUNCTIONAL Curios necklace slots.
 * Main inventory, hands, offhand and cosmetic slots never qualify.
 */
public final class DreamNecklaceEquipmentPolicy {
    private static final String SLOT = "necklace";

    private DreamNecklaceEquipmentPolicy() {}

    public static boolean isEquipped(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player).flatMap(inventory ->
                inventory.getStacksHandler(SLOT).map(handler -> {
                    var stacks = handler.getStacks();
                    for (int slot = 0; slot < stacks.getSlots(); slot++) {
                        if (!inventory.isSlotActive(SLOT, slot)) continue;
                        if (stacks.getStackInSlot(slot).is(ModItems.DREAM_NECKLACE.get())) return true;
                    }
                    return false;
                })
        ).orElse(false);
    }
}
