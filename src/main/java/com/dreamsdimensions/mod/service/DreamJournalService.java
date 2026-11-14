package com.dreamsdimensions.mod.service;

import com.dreamsdimensions.mod.dimension.DreamDimensionDefinition;
import com.dreamsdimensions.mod.item.DreamJournalItem;
import com.dreamsdimensions.mod.registry.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import java.util.LinkedHashSet;
import java.util.Set;

public final class DreamJournalService {
    private DreamJournalService(){}

    public static void recordVisit(ServerPlayer player,DreamDimensionDefinition destination){
        String key=destination.dimension().identifier().getPath();
        for(int slot=0;slot<player.getInventory().getContainerSize();slot++){
            ItemStack journal=player.getInventory().getItem(slot);
            if(!journal.is(ModItems.DREAM_JOURNAL.get()))continue;
            CustomData old=journal.get(DataComponents.CUSTOM_DATA);
            CompoundTag tag=old==null?new CompoundTag():old.copyTag();
            String existing=tag.getStringOr(DreamJournalItem.VISITED,"");
            Set<String> entries=new LinkedHashSet<>();
            for(String value:existing.split(","))if(!value.isBlank())entries.add(value);
            if(entries.add(key)) {
                tag.putString(DreamJournalItem.VISITED,String.join(",",entries));
                journal.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
            }
        }
    }
}
