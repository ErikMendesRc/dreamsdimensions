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
    private final boolean lightness;
    public DreamInfusionItem(boolean lightness, Properties properties) {
        super(properties);this.lightness=lightness;
    }
    @Override public ItemUseAnimation getUseAnimation(ItemStack stack) {return ItemUseAnimation.DRINK;}
    @Override public int getUseDuration(ItemStack stack, LivingEntity entity){return 32;}
    @Override public InteractionResult use(Level level, Player player, InteractionHand hand){
        player.startUsingItem(hand);return InteractionResult.CONSUME;
    }
    @Override public ItemStack finishUsingItem(ItemStack stack,Level level,LivingEntity entity){
        if(!level.isClientSide()){
            entity.addEffect(new MobEffectInstance(lightness?ModEffects.LIGHTNESS:ModEffects.VIGIL_INFUSION,3600));
            if(lightness)entity.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST,3600,0));
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
