package com.dreamsdimensions.mod.menu;

import com.dreamsdimensions.mod.block.entity.DreamBedsideBlockEntity;
import com.dreamsdimensions.mod.registry.ModBlocks;
import com.dreamsdimensions.mod.registry.ModMenuTypes;
import com.dreamsdimensions.mod.service.DreamIncubationEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.Level;

/**
 * Two synchronized PREVIEW slots (not removable inventory), three explicit
 * server-controlled actions. Legacy cabinet contents remain in its BlockEntity,
 * so opening/closing a GUI never extracts ingredients or duplicates outputs.
 */
public final class DreamBedsideMenu extends AbstractContainerMenu {
    private final DreamBedsideBlockEntity bedside; // null on client
    private final SimpleContainer preview = new SimpleContainer(2);
    private final DataSlot type = DataSlot.standalone();
    private final DataSlot pending = DataSlot.standalone();
    private final DataSlot finished = DataSlot.standalone();
    private final DataSlot capacity = DataSlot.standalone();
    private final DataSlot room = DataSlot.standalone();
    private final DataSlot access = DataSlot.standalone();

    public DreamBedsideMenu(int id, Inventory inventory) {
        this(id, inventory, null);
    }

    public DreamBedsideMenu(int id, Inventory inventory, DreamBedsideBlockEntity bedside) {
        super(ModMenuTypes.DREAM_BEDSIDE.get(), id);
        this.bedside = bedside;
        for (DataSlot data : new DataSlot[] {type, pending, finished, capacity, room, access}) {
            addDataSlot(data);
        }

        // These are visible ingredient/output snapshots, NOT movable slots.
        for (int slot = 0; slot < 2; slot++) {
            addSlot(new Slot(preview, slot, slot == 0 ? 37 : 120, 31) {
                @Override public boolean mayPlace(ItemStack stack) { return false; }
                @Override public boolean mayPickup(Player player) { return false; }
            });
        }

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 142));
        }
        refresh();
    }

    public int recipe() { return type.get(); }
    public int pending() { return pending.get(); }
    public int ready() { return finished.get(); }
    public int capacity() { return capacity.get(); }
    public int room() { return room.get(); }
    public boolean hasBed() { return access.get() != 0; }

    private BlockPos closestBed() {
        if (bedside == null || bedside.getLevel() == null) return null;
        Level world = bedside.getLevel();
        BlockPos cabinet = bedside.getBlockPos();
        BlockPos chosen = null;
        int minDistance = Integer.MAX_VALUE;
        for (BlockPos p : BlockPos.betweenClosed(cabinet.offset(-2,-1,-2), cabinet.offset(2,1,2))) {
            int distance = p.distManhattan(cabinet);
            if (distance > 2 || distance >= minDistance) continue;
            if (world.getBlockState(p).getBlock() instanceof BedBlock) {
                chosen = p.immutable();
                minDistance = distance;
            }
        }
        return chosen;
    }

    private int currentCapacity() {
        if (bedside == null || bedside.getLevel() == null) return 2;
        Level world = bedside.getLevel();
        BlockPos cabinet = bedside.getBlockPos();
        for (BlockPos p : BlockPos.betweenClosed(cabinet.offset(-2,-1,-2), cabinet.offset(2,1,2))) {
            if (p.distManhattan(cabinet) <= 2
                    && world.getBlockState(p).getBlock() instanceof BedBlock
                    && DreamIncubationEnvironment.dreamcatcher(world, p)) return 3;
        }
        return 2;
    }

    private void refresh() {
        if (bedside == null || bedside.getLevel() == null) return;
        BlockPos bed = closestBed();
        type.set(bedside.recipe());
        pending.set(bedside.pending());
        finished.set(bedside.finished());
        capacity.set(currentCapacity());
        access.set(bed == null ? 0 : 1);
        room.set(bed == null ? 0 : DreamIncubationEnvironment.detect(
                bedside.getLevel(), bed).ordinal());
        ItemStack input = bedside.pending() > 0
                ? new ItemStack(DreamBedsideBlockEntity.inputItem(bedside.recipe()), bedside.pending())
                : ItemStack.EMPTY;
        ItemStack output = bedside.finished() > 0
                ? new ItemStack(DreamBedsideBlockEntity.outputItem(bedside.recipe()), bedside.finished())
                : ItemStack.EMPTY;
        if (!ItemStack.matches(preview.getItem(0), input)) preview.setItem(0, input);
        if (!ItemStack.matches(preview.getItem(1), output)) preview.setItem(1, output);
    }

    @Override public void broadcastChanges() {
        refresh();
        super.broadcastChanges();
    }

    // Screen buttons are routed through vanilla inventory-button packets.
    @Override public boolean clickMenuButton(Player player, int buttonId) {
        if (!(player instanceof ServerPlayer server) || bedside == null || !stillValid(player)) {
            return false;
        }
        if (buttonId == 0) {
            if (closestBed() == null) {
                server.sendSystemMessage(Component.translatable(
                        "message.dreamsdimensions.bedside.no_bed"));
                return false;
            }
            ItemStack held = server.getMainHandItem();
            if (!bedside.load(server, held, currentCapacity())) {
                server.sendSystemMessage(Component.translatable(
                        "message.dreamsdimensions.bedside.invalid"));
                return false;
            }
        } else if (buttonId == 1 || buttonId == 2) {
            ItemStack returned = buttonId == 1
                    ? bedside.takeFinished(server) : bedside.takePending(server);
            if (returned.isEmpty()) return false;
            if (!server.getInventory().add(returned)) {
                net.minecraft.world.level.block.Block.popResource(
                        server.level(), bedside.getBlockPos(), returned);
            }
        } else return false;
        refresh();
        broadcastChanges();
        return true;
    }

    // Shift-click from inventory is a convenience for loading one material
    // family. Never transfers to the display slots or to an unowned cabinet.
    @Override public ItemStack quickMoveStack(Player player, int index) {
        if (!(player instanceof ServerPlayer server) || bedside == null
                || index < 2 || index >= slots.size() || !stillValid(player)
                || closestBed() == null) return ItemStack.EMPTY;
        Slot source = slots.get(index);
        if (!source.hasItem()) return ItemStack.EMPTY;
        ItemStack item = source.getItem();
        ItemStack old = item.copy();
        int inserted = 0;
        while (!item.isEmpty() && inserted < 3 && bedside.load(server, item, currentCapacity())) {
            inserted++;
        }
        if (inserted == 0) return ItemStack.EMPTY;
        source.setChanged();
        refresh();
        broadcastChanges();
        return old;
    }

    @Override public boolean stillValid(Player player) {
        if (bedside == null) return true; // client resolves through menu sync
        if (bedside.getLevel() == null) return false;
        return stillValid(ContainerLevelAccess.create(
                bedside.getLevel(), bedside.getBlockPos()), player, ModBlocks.DREAM_BEDSIDE.get());
    }
}
