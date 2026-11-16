package com.dreamsdimensions.mod.menu;

import com.dreamsdimensions.mod.block.entity.DreamBedsideBlockEntity;
import com.dreamsdimensions.mod.registry.ModMenuTypes;
import com.dreamsdimensions.mod.service.DreamIncubationEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;

/**
 * Real two-slot bedside container: use vanilla click, drag, split, Q and shift
 * mechanics. Nothing is withdrawn just by opening or closing this screen.
 * The server-owned BlockEntity is the only durable inventory.
 */
public final class DreamBedsideMenu extends AbstractContainerMenu {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int PLAYER_START = 2;
    public static final int PLAYER_END = 38;

    private final DreamBedsideBlockEntity bedside; // null on the client
    private final Container storage;
    private final Player interactingPlayer;

    private final DataSlot capacity = DataSlot.standalone();
    private final DataSlot room = DataSlot.standalone();
    private final DataSlot hasBed = DataSlot.standalone();
    private final DataSlot accessible = DataSlot.standalone();

    public DreamBedsideMenu(int id, Inventory inventory) {
        this(id, inventory, null);
    }

    public DreamBedsideMenu(int id, Inventory inventory, DreamBedsideBlockEntity bedside) {
        super(ModMenuTypes.DREAM_BEDSIDE.get(), id);
        this.bedside = bedside;
        this.interactingPlayer = inventory.player;
        this.storage = bedside == null ? new SimpleContainer(2) : bedside;
        for (DataSlot status : new DataSlot[] {capacity, room, hasBed, accessible}) addDataSlot(status);

        addSlot(new Slot(storage, INPUT_SLOT, 34, 32) {
            @Override public boolean mayPlace(ItemStack incoming) {
                // Depositing is allowed before a bed is built. The bed and room
                // are prerequisites for incubation, not for inventory storage.
                return DreamBedsideBlockEntity.typeOf(incoming) != 0
                        && (bedside == null || (bedside.isOwner(interactingPlayer)
                                && bedside.canInsert(incoming)));
            }

            @Override public boolean mayPickup(Player player) {
                return bedside == null || bedside.isOwner(player);
            }

            @Override public int getMaxStackSize(ItemStack stack) {
                return DreamBedsideMenu.this.capacity();
            }

            @Override public void setChanged() {
                super.setChanged();
                if (bedside != null) bedside.claimOwner(interactingPlayer);
            }
        });

        addSlot(new Slot(storage, OUTPUT_SLOT, 126, 32) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
            @Override public boolean mayPickup(Player player) {
                return bedside == null || bedside.isOwner(player);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, 9 + row * 9 + col,
                        8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
        refreshStatus();
    }

    public int capacity() { return Math.max(2, capacity.get()); }
    public int room() { return room.get(); }
    public boolean hasBed() { return hasBed.get() != 0; }
    public boolean canEdit() { return accessible.get() != 0; }
    public int pending() { return storage.getItem(INPUT_SLOT).getCount(); }
    public int ready() { return storage.getItem(OUTPUT_SLOT).getCount(); }

    private BlockPos closestBed() {
        if (bedside == null || bedside.getLevel() == null) return null;
        Level level = bedside.getLevel();
        BlockPos at = bedside.getBlockPos();
        BlockPos selected = null;
        int bestDistance = Integer.MAX_VALUE;
        for (BlockPos pos : BlockPos.betweenClosed(at.offset(-2,-1,-2), at.offset(2,1,2))) {
            int distance = pos.distManhattan(at);
            if (distance > 2 || distance >= bestDistance) continue;
            if (level.getBlockState(pos).getBlock() instanceof BedBlock) {
                selected = pos.immutable();
                bestDistance = distance;
            }
        }
        return selected;
    }

    private int actualCapacity() {
        if (bedside == null || bedside.getLevel() == null) return 2;
        Level level = bedside.getLevel();
        BlockPos at = bedside.getBlockPos();
        for (BlockPos pos : BlockPos.betweenClosed(at.offset(-2,-1,-2), at.offset(2,1,2))) {
            if (pos.distManhattan(at) <= 2
                    && level.getBlockState(pos).getBlock() instanceof BedBlock
                    && DreamIncubationEnvironment.dreamcatcher(level, pos)) return 3;
        }
        return 2;
    }

    private void refreshStatus() {
        if (bedside == null || bedside.getLevel() == null) return;
        BlockPos bed = closestBed();
        capacity.set(actualCapacity());
        hasBed.set(bed == null ? 0 : 1);
        room.set(bed == null ? 0 : DreamIncubationEnvironment.detect(
                bedside.getLevel(), bed).ordinal());
        accessible.set(bedside.isOwner(interactingPlayer) ? 1 : 0);
    }

    @Override public void broadcastChanges() {
        refreshStatus();
        super.broadcastChanges();
    }

    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size() || !stillValid(player)) return ItemStack.EMPTY;
        Slot source = slots.get(index);
        if (!source.hasItem() || !source.mayPickup(player)) return ItemStack.EMPTY;

        ItemStack moving = source.getItem();
        ItemStack previous = moving.copy();

        if (index == INPUT_SLOT || index == OUTPUT_SLOT) {
            if (!moveItemStackTo(moving, PLAYER_START, PLAYER_END, true))
                return ItemStack.EMPTY;
        } else {
            // Input first. Invalid/filled inputs can be rearranged normally
            // between the hotbar and player inventory.
            boolean moved = slots.get(INPUT_SLOT).mayPlace(moving)
                    && moveItemStackTo(moving, INPUT_SLOT, INPUT_SLOT + 1, false);
            if (!moved) {
                if (index < PLAYER_START + 27) {
                    if (!moveItemStackTo(moving, PLAYER_START + 27, PLAYER_END, false))
                        return ItemStack.EMPTY;
                } else if (!moveItemStackTo(moving, PLAYER_START,
                        PLAYER_START + 27, false)) return ItemStack.EMPTY;
            }
        }

        if (moving.isEmpty()) source.setByPlayer(ItemStack.EMPTY);
        else source.setChanged();
        if (moving.getCount() == previous.getCount()) return ItemStack.EMPTY;
        source.onTake(player, moving);
        return previous;
    }

    @Override public boolean stillValid(Player player) {
        return bedside == null || bedside.stillValid(player);
    }
}
