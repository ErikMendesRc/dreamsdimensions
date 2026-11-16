package com.dreamsdimensions.mod.block.entity;

import com.dreamsdimensions.mod.menu.DreamBedsideMenu;
import com.dreamsdimensions.mod.registry.ModBlockEntities;
import com.dreamsdimensions.mod.registry.ModBlocks;
import com.dreamsdimensions.mod.registry.ModItems;
import com.dreamsdimensions.mod.service.DreamIncubationEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

/**
 * Task 9.5.13: two REAL persistent slots, not read-only item previews.
 * Slot 0 holds incubating material, slot 1 holds completed products.
 * The owner, nightly deduplication, and old-world save format are preserved.
 */
public final class DreamBedsideBlockEntity extends BlockEntity implements MenuProvider, Container {
    public static final int INPUT = 0;
    public static final int OUTPUT = 1;
    private final NonNullList<ItemStack> items = NonNullList.withSize(2, ItemStack.EMPTY);
    private UUID owner;
    private long lastNight = Long.MIN_VALUE;

    public DreamBedsideBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BEDSIDE.get(), pos, state);
    }

    @Override public Component getDisplayName() {
        return Component.translatable("container.dreamsdimensions.dream_bedside");
    }

    @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new DreamBedsideMenu(id, inventory, this);
    }

    public boolean isOwner(Player player) {
        return owner == null || owner.equals(player.getUUID());
    }

    /** The input slot is claimed only once an actual item was placed. */
    public void claimOwner(Player player) {
        if (owner == null && !items.get(INPUT).isEmpty()) {
            owner = player.getUUID();
            setChanged();
        }
    }

    public int pending() { return items.get(INPUT).getCount(); }
    public int finished() { return items.get(OUTPUT).getCount(); }
    public int recipe() {
        if (!items.get(INPUT).isEmpty()) return typeOf(items.get(INPUT));
        ItemStack result = items.get(OUTPUT);
        for (int n = 1; n <= 5; n++) if (result.is(outputItem(n))) return n;
        return 0;
    }

    public static Item inputItem(int code) {
        return switch (code) {
            case 1 -> Items.GLASS_BOTTLE;
            case 2 -> Items.STRING;
            case 3 -> Items.COAL;
            case 4 -> ModBlocks.DREAM_INFUSED_STONE.get().asItem();
            case 5 -> ModItems.TOASTED_VIGIL_HERB.get();
            default -> Items.AIR;
        };
    }

    public static Item outputItem(int code) {
        return switch (code) {
            case 1 -> ModItems.SERENE_VIAL.get();
            case 2 -> ModItems.DREAM_THREAD.get();
            case 3 -> ModItems.DORMANT_EMBER.get();
            case 4 -> ModBlocks.DEEP_SLEEP_STONE.get().asItem();
            case 5 -> ModItems.DREAMY_HERB.get();
            default -> Items.AIR;
        };
    }

    public static int typeOf(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        for (int i = 1; i <= 5; i++) if (stack.is(inputItem(i))) return i;
        return 0;
    }

    public boolean canInsert(ItemStack incoming) {
        if (typeOf(incoming) == 0 || !items.get(OUTPUT).isEmpty()) return false;
        ItemStack present = items.get(INPUT);
        return present.isEmpty() || ItemStack.isSameItemSameComponents(present, incoming);
    }

    /** The actual processing happens only after a validated vanilla full night. */
    public boolean incubate(ServerPlayer player, long night,
            DreamIncubationEnvironment.Kind room, boolean catcher) {
        ItemStack input = items.get(INPUT);
        if (owner == null || !owner.equals(player.getUUID())
                || input.isEmpty() || !items.get(OUTPUT).isEmpty() || lastNight == night) return false;
        int code = typeOf(input);
        boolean match = switch (room) {
            case SERENE -> code == 1;
            case SILENCE -> code == 2;
            case EMBER -> code == 3;
            case DEEP -> code == 4;
            case GARDEN -> code == 5;
            default -> false;
        };
        if (!match) return false;

        // An existing 3-stack is not destroyed if its Dreamcatcher was removed.
        int processed = Math.min(input.getCount(), catcher ? 3 : 2);
        items.set(OUTPUT, new ItemStack(outputItem(code), processed));
        input.shrink(processed);
        if (input.isEmpty()) items.set(INPUT, ItemStack.EMPTY);
        lastNight = night;
        setChanged();
        return true;
    }

    @Override public int getContainerSize() { return 2; }

    @Override public boolean isEmpty() {
        return items.get(INPUT).isEmpty() && items.get(OUTPUT).isEmpty();
    }

    @Override public ItemStack getItem(int slot) {
        return slot >= 0 && slot < 2 ? items.get(slot) : ItemStack.EMPTY;
    }

    @Override public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= 2 || amount <= 0) return ItemStack.EMPTY;
        ItemStack removed = items.get(slot).split(amount);
        if (!removed.isEmpty()) {
            if (items.get(slot).isEmpty()) items.set(slot, ItemStack.EMPTY);
            setChanged();
        }
        return removed;
    }

    @Override public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= 2) return ItemStack.EMPTY;
        ItemStack removed = items.get(slot);
        items.set(slot, ItemStack.EMPTY);
        if (!removed.isEmpty()) setChanged();
        return removed;
    }

    @Override public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= 2) return;
        ItemStack copied = stack.copy();
        if (slot == INPUT && copied.getCount() > 3) copied.setCount(3);
        items.set(slot, copied);
        setChanged();
    }

    @Override public void clearContent() {
        items.set(INPUT, ItemStack.EMPTY);
        items.set(OUTPUT, ItemStack.EMPTY);
        setChanged();
    }

    @Override public void setChanged() {
        if (isEmpty()) owner = null;
        super.setChanged();
    }

    @Override public boolean stillValid(Player player) {
        return level != null
                && level.getBlockState(worldPosition).is(ModBlocks.DREAM_BEDSIDE.get())
                && player.distanceToSqr(
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 0.5,
                    worldPosition.getZ() + 0.5) <= 64.0;
    }

    /** Drops exactly the contents remaining in each slot; no duplicate drops. */
    @Override public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            for (int slot = 0; slot < 2; slot++) {
                ItemStack remaining = items.get(slot);
                if (!remaining.isEmpty()) {
                    Block.popResource(level, pos, remaining.copy());
                    items.set(slot, ItemStack.EMPTY);
                }
            }
            owner = null;
            setChanged();
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        if (owner != null) out.putString("owner", owner.toString());
        out.putLong("last_night", lastNight);

        // Keep the original keys so old saves and diagnostic tools are readable.
        out.putInt("type", recipe());
        out.putInt("pending", pending());
        out.putInt("finished", finished());

        // New format stores full item stacks, including data components.
        if (!items.get(INPUT).isEmpty())
            out.store("inputStack", ItemStack.CODEC, items.get(INPUT));
        if (!items.get(OUTPUT).isEmpty())
            out.store("outputStack", ItemStack.CODEC, items.get(OUTPUT));
    }

    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        String id = in.getStringOr("owner", "");
        try { owner = id.isEmpty() ? null : UUID.fromString(id); }
        catch (IllegalArgumentException invalid) { owner = null; }
        lastNight = in.getLongOr("last_night", Long.MIN_VALUE);

        // Transparent migration from Task 9.5.12's code + count storage.
        int legacyType = Math.clamp(in.getIntOr("type", 0), 0, 5);
        int legacyPending = Math.clamp(in.getIntOr("pending", 0), 0, 3);
        int legacyFinished = Math.clamp(in.getIntOr("finished", 0), 0, 3);
        ItemStack input = in.read("inputStack", ItemStack.CODEC)
                .orElseGet(() -> legacyType == 0 || legacyPending == 0
                        ? ItemStack.EMPTY : new ItemStack(inputItem(legacyType), legacyPending));
        ItemStack output = in.read("outputStack", ItemStack.CODEC)
                .orElseGet(() -> legacyType == 0 || legacyFinished == 0
                        ? ItemStack.EMPTY : new ItemStack(outputItem(legacyType), legacyFinished));
        if (typeOf(input) == 0) input = ItemStack.EMPTY;
        if (!output.isEmpty()) {
            boolean known = false;
            for (int i = 1; i <= 5; i++) if (output.is(outputItem(i))) known = true;
            if (!known) output = ItemStack.EMPTY;
        }
        if (!input.isEmpty() && input.getCount() > 3) input.setCount(3);
        if (!output.isEmpty() && output.getCount() > 3) output.setCount(3);
        items.set(INPUT, input);
        items.set(OUTPUT, output);
        if (isEmpty()) owner = null;
    }
}
