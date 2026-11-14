package com.dreamsdimensions.mod.menu;

import com.dreamsdimensions.mod.registry.ModBlocks;
import com.dreamsdimensions.mod.registry.ModItems;
import com.dreamsdimensions.mod.registry.ModMenuTypes;
import com.dreamsdimensions.mod.service.CoreChamberPolicy;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Recipes are server-authoritative. Chamber level is captured at menu opening
 * and synchronized; the block never stores a shared inventory.
 */
public final class OneiricCoreMenu extends AbstractContainerMenu {
    private static final int INPUT_SLOTS=3,RESULT_SLOT=3,PLAYER_START=4,PLAYER_END=40;
    private final ContainerLevelAccess access;
    private final DataSlot chamber = DataSlot.standalone();
    private final Container inputs = new SimpleContainer(INPUT_SLOTS) {
        @Override public void setChanged() {super.setChanged();OneiricCoreMenu.this.slotsChanged(this);}
    };
    private final ResultContainer result = new ResultContainer();

    private record Assembly(int a,int b,int c,ItemStack output){}

    public OneiricCoreMenu(int id, Inventory inventory) {
        this(id,inventory,ContainerLevelAccess.NULL);
    }

    public OneiricCoreMenu(int id, Inventory inventory, ContainerLevelAccess access) {
        super(ModMenuTypes.ONEIRIC_CORE.get(),id);
        this.access=access;
        chamber.set(access.evaluate((level,pos)->CoreChamberPolicy.level(level,pos),0));
        addDataSlot(chamber);

        addSlot(new Slot(inputs,0,29,34){
            @Override public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.RETURN_CHARGE.get())
                        || stack.is(ModItems.DREAM_DUST.get())
                        || stack.is(Items.COMPASS)
                        || stack.is(ModItems.RESONANCE_PROBE.get());
            }
        });
        addSlot(new Slot(inputs,1,54,34){
            @Override public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.FEATHER)
                        || stack.is(ModItems.PLUMITE_SHARD.get())
                        || stack.is(ModItems.TOASTED_VIGIL_HERB.get())
                        || stack.is(ModItems.RETURN_CHARGE.get())
                        || stack.is(ModItems.DREAMY_HERB.get());
            }
        });
        addSlot(new Slot(inputs,2,79,34){
            @Override public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.GLASS_BOTTLE)
                        || stack.is(Items.STRING)
                        || stack.is(ModBlocks.CUT_MARKED_STONE.get().asItem())
                        || stack.is(ModItems.REMINISCENCE.get());
            }
        });
        addSlot(new Slot(result,0,138,34) {
            @Override public boolean mayPlace(ItemStack stack){return false;}
            @Override public boolean mayPickup(Player player){return OneiricCoreMenu.this.assembly()!=null;}
            @Override public void onTake(Player player,ItemStack stack) {
                if(!player.level().isClientSide()) {
                    Assembly match=OneiricCoreMenu.this.assembly();
                    if(match!=null) {
                        inputs.removeItem(0,match.a());
                        inputs.removeItem(1,match.b());
                        inputs.removeItem(2,match.c());
                        OneiricCoreMenu.this.slotsChanged(inputs);
                    }
                }
                super.onTake(player,stack);
            }
        });
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)
            addSlot(new Slot(inventory,col+row*9+9,8+col*18,84+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(inventory,col,8+col*18,142));
    }

    public int chamberLevel(){return chamber.get();}

    private boolean has(int slot,net.minecraft.world.item.Item item,int count) {
        ItemStack stack=inputs.getItem(slot);
        return stack.is(item) && stack.getCount()>=count;
    }
    private boolean empty(int slot){return inputs.getItem(slot).isEmpty();}

    private Assembly assembly() {
        // N0 remains entirely compatible with Task 9.2/9.4.
        if(has(0,ModItems.RETURN_CHARGE.get(),1)&&has(1,Items.FEATHER,1)
                &&has(2,Items.GLASS_BOTTLE,1))
            return new Assembly(1,1,1,new ItemStack(ModItems.ONEIRIC_AWAKENER.get()));
        if(has(0,ModItems.RETURN_CHARGE.get(),1)&&has(1,ModItems.PLUMITE_SHARD.get(),2)
                &&has(2,Items.STRING,1))
            return new Assembly(1,2,1,new ItemStack(ModItems.VIGIL_TETHER.get()));
        if(chamberLevel()>=1) {
            // Five dust and four herbs are measured by counts, not four slots.
            if(has(0,ModItems.DREAM_DUST.get(),5)
                    &&has(1,ModItems.TOASTED_VIGIL_HERB.get(),4)
                    &&has(2,ModBlocks.CUT_MARKED_STONE.get().asItem(),1))
                return new Assembly(5,4,1,new ItemStack(ModItems.RETURN_CHARGE.get(),2));
            if(has(0,Items.COMPASS,1)&&has(1,ModItems.RETURN_CHARGE.get(),1)
                    &&has(2,ModBlocks.CUT_MARKED_STONE.get().asItem(),1))
                return new Assembly(1,1,1,new ItemStack(ModItems.RESONANCE_PROBE.get()));
            if(has(0,ModItems.RESONANCE_PROBE.get(),1)&&has(1,ModItems.RETURN_CHARGE.get(),1)
                    &&empty(2)) {
                ItemStack recharged=inputs.getItem(0).copy();
                recharged.setDamageValue(0);
                recharged.setCount(1);
                return new Assembly(1,1,0,recharged);
            }
        }
        if(chamberLevel()>=2) {
            if(has(0,ModItems.DREAM_DUST.get(),6)
                    &&has(1,ModItems.DREAMY_HERB.get(),2)
                    &&has(2,ModItems.REMINISCENCE.get(),1))
                return new Assembly(6,2,1,new ItemStack(ModItems.RETURN_CHARGE.get(),3));
        }
        return null;
    }

    @Override public void slotsChanged(Container inventory) {
        Assembly match=assembly();
        result.setItem(0,match==null?ItemStack.EMPTY:match.output().copy());
        broadcastChanges();
    }

    @Override public boolean stillValid(Player player) {
        return stillValid(access,player,ModBlocks.ONEIRIC_CORE_BLOCK.get());
    }

    @Override public ItemStack quickMoveStack(Player player,int index) {
        if(index<0||index>=slots.size()||index==RESULT_SLOT) return ItemStack.EMPTY;
        Slot slot=slots.get(index);
        if(!slot.hasItem())return ItemStack.EMPTY;
        ItemStack original=slot.getItem().copy();
        ItemStack stack=slot.getItem();
        if(index<PLAYER_START) {
            if(!moveItemStackTo(stack,PLAYER_START,PLAYER_END,true))return ItemStack.EMPTY;
        } else {
            boolean inserted=false;
            for(int target=0;target<INPUT_SLOTS;target++) {
                if(slots.get(target).mayPlace(stack)
                        &&moveItemStackTo(stack,target,target+1,false)) {
                    inserted=true;break;
                }
            }
            if(!inserted)return ItemStack.EMPTY;
        }
        if(stack.isEmpty())slot.setByPlayer(ItemStack.EMPTY);else slot.setChanged();
        if(stack.getCount()==original.getCount())return ItemStack.EMPTY;
        slot.onTake(player,stack);
        return original;
    }

    @Override public void removed(Player player) {
        super.removed(player);
        if(!player.level().isClientSide())clearContainer(player,inputs);
        result.clearContent();
    }
}
