package com.dreamsdimensions.mod.item;

import com.dreamsdimensions.mod.registry.ModEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** True three-minute consumable with empty-bottle return. */
public final class DreamInfusionItem extends Item {
    private final int kind; // 0=lightness, 1=vigil, 2=route
    public DreamInfusionItem(boolean lightness,Properties properties){
        this(lightness?0:1,properties);
    }
    public DreamInfusionItem(int kind, Properties properties){
        super(properties);this.kind=kind;
    }
    @Override public ItemUseAnimation getUseAnimation(ItemStack stack) {return ItemUseAnimation.DRINK;}
    @Override public int getUseDuration(ItemStack stack, LivingEntity entity){return 32;}
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand){
        player.startUsingItem(hand);return InteractionResult.CONSUME;
    }
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity entity){
        if(!level.isClientSide()){
            var custom=stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA);
            boolean longInfusion=custom!=null
                    && custom.copyTag().getIntOr("DreamsDimensionsDuration",3600)>=7200;
            int duration=longInfusion?7200:3600;
            entity.addEffect(new MobEffectInstance(
                    kind==0?ModEffects.LIGHTNESS:kind==1?ModEffects.VIGIL_INFUSION:ModEffects.ROUTE,duration));
            if(kind==0)entity.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST,duration,0));
        }
        if(entity instanceof Player player && !player.getAbilities().instabuild){
            stack.shrink(1);
            ItemStack bottle=new ItemStack(Items.GLASS_BOTTLE);
            if(stack.isEmpty())return bottle;
            if(!player.getInventory().add(bottle))net.minecraft.world.level.block.Block.popResource(level,player.blockPosition(),bottle);
        }
        return stack;
    }
}
