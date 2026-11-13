package com.dreamsdimensions.mod.block.entity;

import com.dreamsdimensions.mod.registry.ModBlockEntities;
import com.dreamsdimensions.mod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Small persistent 3-bottle copper infusion kettle. No global inventory,
 * no player-tick loop: only an active placed block entity advances the brew.
 */
public final class OneiricKettleBlockEntity extends BlockEntity {
    public static final int BREW_TICKS=400;
    private int waterBottles;
    private boolean herbBase;
    private int reagent; // 0 = none, 1 = plumite, 2 = toasted herb
    private int progress;
    private boolean ready;

    public OneiricKettleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.KETTLE.get(),pos,state);
    }

    public static boolean onLitCampfire(Level level, BlockPos pos) {
        BlockState below=level.getBlockState(pos.below());
        return (below.is(Blocks.CAMPFIRE)||below.is(Blocks.SOUL_CAMPFIRE))
                && below.getValue(CampfireBlock.LIT);
    }

    public boolean isReady(){return ready;}
    public int getProgress(){return progress;}
    public int getBottles(){return waterBottles;}
    public boolean isBaseLoaded(){return herbBase;}
    public int getReagent(){return reagent;}

    public boolean addIngredient(ItemStack stack) {
        if(ready||progress>0) return false;
        if (stack.is(Items.POTION)
                && stack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS)!=null
                && stack.get(net.minecraft.core.component.DataComponents.POTION_CONTENTS).is(net.minecraft.world.item.alchemy.Potions.WATER)) {
            if(waterBottles>=3)return false;
            waterBottles++;setChanged();return true;
        }
        if(stack.is(ModItems.TOASTED_VIGIL_HERB.get())){
            if(!herbBase)herbBase=true;
            else if(reagent==0)reagent=2;
            else return false;
            setChanged();return true;
        }
        if(stack.is(ModItems.PLUMITE_SHARD.get()) && reagent==0){
            reagent=1;setChanged();return true;
        }
        return false;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
            OneiricKettleBlockEntity kettle) {
        if(kettle.ready || kettle.waterBottles==0 || !kettle.herbBase || kettle.reagent==0) return;
        if(!onLitCampfire(level,pos))return; // paused, never progress without heat
        kettle.progress++;
        if(kettle.progress>=BREW_TICKS){kettle.ready=true;kettle.progress=BREW_TICKS;}
        if(kettle.progress%20==0||kettle.ready) kettle.setChanged();
    }

    public ItemStack removeOne() {
        if(!ready || waterBottles==0) return ItemStack.EMPTY;
        ItemStack output = new ItemStack(reagent==1 ? ModItems.INFUSION_LIGHTNESS.get()
                : ModItems.INFUSION_VIGIL.get());
        waterBottles--;
        if(waterBottles==0) {
            herbBase=false;reagent=0;progress=0;ready=false;
        }
        setChanged();return output;
    }

    /** Drop what the block truly holds, without duplicating a finished batch. */
    public void dropStored(Level level, BlockPos pos) {
        if(ready) {
            for(int i=0;i<waterBottles;i++)Block.popResource(level,pos,
                    new ItemStack(reagent==1?ModItems.INFUSION_LIGHTNESS.get():ModItems.INFUSION_VIGIL.get()));
        } else {
            if(waterBottles>0) {
                for (int i=0; i<waterBottles; i++) {
                    Block.popResource(level, pos,
                            net.minecraft.world.item.alchemy.PotionContents.createItemStack(
                                    Items.POTION, net.minecraft.world.item.alchemy.Potions.WATER));
                }
            }
            if(herbBase)Block.popResource(level,pos,new ItemStack(ModItems.TOASTED_VIGIL_HERB.get()));
            if(reagent==1)Block.popResource(level,pos,new ItemStack(ModItems.PLUMITE_SHARD.get()));
            if(reagent==2)Block.popResource(level,pos,new ItemStack(ModItems.TOASTED_VIGIL_HERB.get()));
        }
        waterBottles=0;herbBase=false;reagent=0;progress=0;ready=false;
    }

    @Override protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("bottles",waterBottles);
        output.putBoolean("herb_base",herbBase);
        output.putInt("reagent",reagent);
        output.putInt("progress",progress);
        output.putBoolean("ready",ready);
    }
    @Override protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        waterBottles=Math.clamp(input.getIntOr("bottles",0),0,3);
        herbBase=input.getBooleanOr("herb_base",false);
        reagent=Math.clamp(input.getIntOr("reagent",0),0,2);
        progress=Math.clamp(input.getIntOr("progress",0),0,BREW_TICKS);
        ready=input.getBooleanOr("ready",false)
                && waterBottles>0 && herbBase && reagent!=0;
    }
}
