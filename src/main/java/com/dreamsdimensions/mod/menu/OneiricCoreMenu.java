package com.dreamsdimensions.mod.menu;

import com.dreamsdimensions.mod.registry.ModBlocks;
import com.dreamsdimensions.mod.registry.ModItems;
import com.dreamsdimensions.mod.registry.ModMenuTypes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Three input slots and one server-authoritative result slot.
 * Each player receives a private temporary inventory; nothing persists in
 * the block, nothing can be duplicated by multiple users or breaking it.
 */
public final class OneiricCoreMenu extends AbstractContainerMenu {
    private static final int INPUT_SLOTS = 3;
    private static final int RESULT_SLOT = 3;
    private static final int PLAYER_START = 4;
    private static final int PLAYER_END = 40;

    private final ContainerLevelAccess access;
    private final Container inputs = new SimpleContainer(INPUT_SLOTS) {
        @Override
        public void setChanged() {
            super.setChanged();
            OneiricCoreMenu.this.slotsChanged(this);
        }
    };
    private final ResultContainer result = new ResultContainer();

    public OneiricCoreMenu(int id, Inventory inventory) {
        this(id, inventory, ContainerLevelAccess.NULL);
    }

    public OneiricCoreMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        super(ModMenuTypes.ONEIRIC_CORE.get(), id);
        this.access = access;

        this.addSlot(new Slot(inputs, 0, 29, 34) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.is(ModItems.RETURN_CHARGE.get()); }
        });
        this.addSlot(new Slot(inputs, 1, 54, 34) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.is(Items.FEATHER); }
        });
        this.addSlot(new Slot(inputs, 2, 79, 34) {
            @Override public boolean mayPlace(ItemStack stack) { return stack.is(Items.GLASS_BOTTLE); }
        });
        this.addSlot(new Slot(result, 0, 138, 34) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }

            @Override public boolean mayPickup(Player player) {
                return OneiricCoreMenu.this.ready();
            }

            @Override public void onTake(Player player, ItemStack stack) {
                // Only the server is allowed to debit materials.
                if (!player.level().isClientSide() && OneiricCoreMenu.this.ready()) {
                    for (int i = 0; i < INPUT_SLOTS; i++) {
                        inputs.removeItem(i, 1);
                    }
                    OneiricCoreMenu.this.slotsChanged(inputs);
                }
                super.onTake(player, stack);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9,
                        8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
    }

    private boolean ready() {
        return inputs.getItem(0).is(ModItems.RETURN_CHARGE.get())
                && inputs.getItem(1).is(Items.FEATHER)
                && inputs.getItem(2).is(Items.GLASS_BOTTLE);
    }

    @Override
    public void slotsChanged(Container container) {
        // Client inventory changes are only visuals. The server calculates
        // the result and synchronizes the output through vanilla menu slots.
        if (!container.getItem(0).isEmpty() || container == inputs) {
            if (ready()) {
                result.setItem(0, new ItemStack(ModItems.ONEIRIC_AWAKENER.get()));
            } else {
                result.setItem(0, ItemStack.EMPTY);
            }
            this.broadcastChanges();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.ONEIRIC_CORE_BLOCK.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= this.slots.size()) return ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        ItemStack original = slot.getItem().copy();
        ItemStack stack = slot.getItem();

        if (index == RESULT_SLOT) {
            // No shift-crafting output: a normal result click is an atomic
            // one-at-a-time transaction and cannot bypass recipe payment.
            return ItemStack.EMPTY;
        }
        if (index < PLAYER_START) {
            if (!moveItemStackTo(stack, PLAYER_START, PLAYER_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            int target = stack.is(ModItems.RETURN_CHARGE.get()) ? 0
                    : stack.is(Items.FEATHER) ? 1
                    : stack.is(Items.GLASS_BOTTLE) ? 2 : -1;
            if (target == -1 || !moveItemStackTo(stack, target, target + 1, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        if (stack.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        // Never persist ingredients in the block. Return them, or drop if
        // the inventory is full; closing/relogging cannot duplicate result.
        if (!player.level().isClientSide()) {
            this.clearContainer(player, inputs);
        }
        result.clearContent();
    }
}
