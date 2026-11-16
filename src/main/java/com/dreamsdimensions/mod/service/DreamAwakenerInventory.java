package com.dreamsdimensions.mod.service;

import com.dreamsdimensions.mod.registry.ModItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Read-only inventory access compatible with the 26.3 Container API. */
public final class DreamAwakenerInventory {
    private DreamAwakenerInventory() {}

    public static boolean hasReturnItem(ServerPlayer player, boolean includeCracked) {
        var inv = player.getInventory();
        for (int slot = 0; slot < inv.getContainerSize(); slot++) {
            ItemStack stack = inv.getItem(slot);
            if (stack.is(ModItems.ONEIRIC_AWAKENER.get())
                    || (includeCracked && stack.is(ModItems.CRACKED_AWAKENER.get()))) {
                return true;
            }
        }
        // Offhand check is explicit to avoid inventory-shape assumptions.
        return player.getOffhandItem().is(ModItems.ONEIRIC_AWAKENER.get())
                || (includeCracked && player.getOffhandItem().is(ModItems.CRACKED_AWAKENER.get()));
    }
}
