package com.dreamsdimensions.mod.block.entity;

import com.dreamsdimensions.mod.registry.ModBlockEntities;
import com.dreamsdimensions.mod.registry.ModBlocks;
import com.dreamsdimensions.mod.registry.ModItems;
import com.dreamsdimensions.mod.service.DreamIncubationEnvironment;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
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
 * Minimal serialized inventory: exactly one input family per night, 2 or 3
 * units, not a shared player inventory. Both inputs and outputs are persisted.
 * Breaking the block drops only what remains, once.
 */
public final class DreamBedsideBlockEntity extends BlockEntity {
    private UUID owner;
    private int type; // 1 bottle, 2 string, 3 coal, 4 infused stone, 5 toasted herb
    private int inputCount;
    private int finishedCount;
    private long lastNight=Long.MIN_VALUE;

    public DreamBedsideBlockEntity(BlockPos pos,BlockState state){
        super(ModBlockEntities.BEDSIDE.get(),pos,state);
    }
    public boolean isOwner(ServerPlayer player){
        return owner==null||owner.equals(player.getUUID());
    }
    public int pending(){return inputCount;}
    public int finished(){return finishedCount;}
    public int recipe(){return type;}

    public static Item inputItem(int code){
        return switch(code) {
            case 1 -> Items.GLASS_BOTTLE;
            case 2 -> Items.STRING;
            case 3 -> Items.COAL;
            case 4 -> ModBlocks.DREAM_INFUSED_STONE.get().asItem();
            case 5 -> ModItems.TOASTED_VIGIL_HERB.get();
            default -> Items.AIR;
        };
    }
    public static Item outputItem(int code) {
        return switch(code) {
            case 1 -> ModItems.SERENE_VIAL.get();
            case 2 -> ModItems.DREAM_THREAD.get();
            case 3 -> ModItems.DORMANT_EMBER.get();
            case 4 -> ModBlocks.DEEP_SLEEP_STONE.get().asItem();
            case 5 -> ModItems.DREAMY_HERB.get();
            default -> Items.AIR;
        };
    }
    public static int typeOf(ItemStack stack){
        for(int i=1;i<=5;i++)if(stack.is(inputItem(i)))return i;
        return 0;
    }

    /** Only a real owner may load ingredients; each click costs one item. */
    public boolean load(ServerPlayer player, ItemStack stack, int capacity) {
        if(!isOwner(player)||finishedCount>0)return false;
        int incoming=typeOf(stack);
        if(incoming==0||(type!=0&&type!=incoming)||inputCount>=capacity)return false;
        if(owner==null)owner=player.getUUID();
        type=incoming;inputCount++;
        if(!player.getAbilities().instabuild)stack.shrink(1);
        setChanged();
        return true;
    }

    /** A finished batch replaces the inputs once, after a genuine full night. */
    public boolean incubate(ServerPlayer player,long night,
            DreamIncubationEnvironment.Kind room, boolean catcher) {
        if(owner==null||!owner.equals(player.getUUID())
                ||inputCount<=0||finishedCount>0||lastNight==night)return false;
        boolean match=switch(room) {
            case SERENE -> type==1;
            case SILENCE -> type==2;
            case EMBER -> type==3;
            case DEEP -> type==4;
            case GARDEN -> type==5;
            default -> false;
        };
        if(!match)return false;
        int limit=catcher?3:2;
        finishedCount=Math.min(inputCount,limit);
        inputCount-=finishedCount;
        lastNight=night;
        setChanged();
        return finishedCount>0;
    }

    public ItemStack take(ServerPlayer player) {
        if(!isOwner(player)||type==0)return ItemStack.EMPTY;
        if(finishedCount>0) {
            ItemStack out=new ItemStack(outputItem(type),finishedCount);
            finishedCount=0;
            if(inputCount==0)resetType();
            setChanged();return out;
        }
        if(inputCount>0){
            ItemStack out=new ItemStack(inputItem(type),inputCount);
            inputCount=0;resetType();setChanged();return out;
        }
        return ItemStack.EMPTY;
    }

    private void resetType(){type=0;owner=null;}

    @Override
    public void preRemoveSideEffects(BlockPos pos,BlockState state) {
        if(level!=null&&!level.isClientSide() && type>0) {
            if(inputCount>0)Block.popResource(level,pos,new ItemStack(inputItem(type),inputCount));
            if(finishedCount>0)Block.popResource(level,pos,new ItemStack(outputItem(type),finishedCount));
            type=0;inputCount=0;finishedCount=0;owner=null;
        }
        super.preRemoveSideEffects(pos,state);
    }

    @Override protected void saveAdditional(ValueOutput out) {
        super.saveAdditional(out);
        if(owner!=null)out.putString("owner",owner.toString());
        out.putInt("type",type);out.putInt("pending",inputCount);
        out.putInt("finished",finishedCount);out.putLong("last_night",lastNight);
    }
    @Override protected void loadAdditional(ValueInput in) {
        super.loadAdditional(in);
        String id=in.getStringOr("owner","");
        try {owner=id.isEmpty()?null:UUID.fromString(id);}catch(IllegalArgumentException invalid){owner=null;}
        type=Math.clamp(in.getIntOr("type",0),0,5);
        inputCount=Math.clamp(in.getIntOr("pending",0),0,3);
        finishedCount=Math.clamp(in.getIntOr("finished",0),0,3);
        lastNight=in.getLongOr("last_night",Long.MIN_VALUE);
        if(type==0){inputCount=0;finishedCount=0;}
    }
}
