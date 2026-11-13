package com.dreamsdimensions.mod.item;

import com.dreamsdimensions.mod.config.DreamsConfig;
import com.dreamsdimensions.mod.registry.ModItems;
import com.dreamsdimensions.mod.qa.DreamQaRecorder;
import com.dreamsdimensions.mod.service.OneiricReturnGuard;
import com.dreamsdimensions.mod.service.OneiricReturnPolicy;
import com.dreamsdimensions.mod.service.DreamRespawnService;
import com.dreamsdimensions.mod.service.OneiricReturnPolicy.Result;
import com.dreamsdimensions.mod.util.DreamReturnHelper;
import com.mojang.logging.LogUtils;
import java.util.UUID;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import org.slf4j.Logger;

/**
 * 60-tick, server-authoritative return to the Overworld. Only a confirmed
 * transition can consume the exact stack that started the vanilla use cycle.
 * No new return engine, inventory-wide search, or persistent state.
 */
public class OneiricAwakenerItem extends Item {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int USE_DURATION_TICKS = 60;
    public static final int COOLDOWN_TICKS = 60;
    private static final OneiricReturnGuard GUARD = new OneiricReturnGuard();

    public OneiricAwakenerItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION_TICKS;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        // A talisman does not draw a bow; keep its artwork visible while channeling.
        return ItemUseAnimation.NONE;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // In the Overworld, PASS must not begin use or block other interactions.
        if (!DreamsConfig.isDreamDimension(level.dimension())) return InteractionResult.PASS;
        Result eligibility = OneiricReturnPolicy.validate(
                player instanceof ServerPlayer || level.isClientSide(), player.isAlive(),
                player.isSpectator(), true, stack.is(this), !stack.isEmpty(),
                player.getCooldowns().isOnCooldown(stack));
        if (eligibility != Result.SUCCESS) return InteractionResult.FAIL;

        if (player instanceof ServerPlayer serverPlayer) {
            if (!GUARD.begin(serverPlayer.getUUID(), guardHand(hand), stack)) {
                LOGGER.debug("ONEIRIC_RETURN player_uuid={} result=duplicate_start hand={}",
                        serverPlayer.getUUID(), hand);
                return InteractionResult.FAIL;
            }
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME; // Interaction result, NOT an item decrement.
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer player) || !(level instanceof ServerLevel)
                || !player.isAlive()) return stack;

        UUID uuid = player.getUUID();
        InteractionHand hand = player.getUsedItemHand();
        // Vanilla normally checks the held item before calling finishUsingItem.
        // Revalidate strictly: no copied stack, swapped hand or different slot.
        if (OneiricReturnPolicy.validate(true, player.isAlive(), player.isSpectator(),
                DreamsConfig.isDreamDimension(player.level().dimension()),
                stack.is(this), !stack.isEmpty(), player.getCooldowns().isOnCooldown(stack))
                != Result.SUCCESS
                || player.getUseItem() != stack || player.getItemInHand(hand) != stack) {
            GUARD.cancel(uuid);
            LOGGER.warn("ONEIRIC_RETURN player_uuid={} result=invalid_item_state hand={} source={}",
                    uuid, hand, player.level().dimension().identifier());
            return stack;
        }

        if (!GUARD.claim(uuid, guardHand(hand), stack)) {
            LOGGER.debug("ONEIRIC_RETURN player_uuid={} result=duplicate_or_invalid_activation hand={}",
                    uuid, hand);
            return stack;
        }

        var source = player.level().dimension();
        DreamQaRecorder.global().record("RECOVERY", "oneiric_awakener",
                "DREAM_RETURN_REQUESTED", source.identifier().toString(), "PASS", "CLAIMED");
        int before = stack.getCount();
        try {
            // Build the existing bed -> personal respawn -> safe world spawn fallback.
            Optional<TeleportTransition> target;
            try {
                target = DreamReturnHelper.buildReturnTransition(player);
            } catch (RuntimeException exception) {
                // An invalid/corrupted bed or unloaded return context may fail
                // while resolving the destination, before any teleport occurs.
                // Preserve the initiating stack and avoid crashing the item tick.
                LOGGER.error("ONEIRIC_RETURN player_uuid={} source={} "
                        + "result=target_resolution_exception consumed=0 hand={}",
                        uuid, source.identifier(), hand, exception);
                failed(player, uuid, hand, source.identifier().toString(), Result.INVALID_TARGET);
                return stack;
            }
            if (target.isEmpty()) {
                failed(player, uuid, hand, source.identifier().toString(), Result.INVALID_TARGET);
                return stack;
            }

            TeleportTransition transition = target.get();
            if (transition.newLevel().dimension() != Level.OVERWORLD) {
                failed(player, uuid, hand, source.identifier().toString(), Result.INVALID_TARGET);
                return stack;
            }

            ServerPlayer returned;
            try {
                returned = player.teleport(transition);
            } catch (RuntimeException exception) {
                DreamQaRecorder.global().record("RECOVERY", "oneiric_awakener",
                        "DREAM_RETURN_REJECTED", source.identifier().toString(), "FAIL", "TELEPORT_EXCEPTION");
                LOGGER.error("ONEIRIC_RETURN player_uuid={} source={} target=minecraft:overworld "
                        + "result=teleport_exception consumed=0 hand={}", uuid,
                        source.identifier(), hand, exception);
                // A throw after a partial transition is possible: do not blindly retry.
                if (player.level().dimension() != Level.OVERWORLD) {
                    player.sendSystemMessage(Component.translatable(
                            "message.dreamsdimensions.oneiric_awakener.fail"));
                }
                return stack;
            }

            Result confirmation = OneiricReturnPolicy.confirm(true, returned != null,
                    returned != null && returned.isAlive(),
                    returned != null && uuid.equals(returned.getUUID()),
                    returned != null && returned.level().dimension() == Level.OVERWORLD);
            if (confirmation != Result.SUCCESS) {
                // If an unusual event already moved the player, never try another TP.
                failed(returned != null ? returned : player, uuid, hand,
                        source.identifier().toString(), confirmation);
                return stack;
            }

            // On NeoForge 26.3 successful ServerPlayer.teleport returns 'this'.
            // If another mod replaced the player/hand stack, fail closed: do
            // not consume another item by type, by inventory search or by slot.
            if (returned.getItemInHand(hand) != stack || !stack.is(this)
                    || stack.getCount() != before) {
                LOGGER.error("ONEIRIC_RETURN player_uuid={} source={} target=minecraft:overworld "
                        + "result=invalid_item_state_after_teleport consumed=0 hand={}",
                        uuid, source.identifier(), hand);
                returned.sendSystemMessage(Component.translatable(
                        "message.dreamsdimensions.oneiric_awakener.item_mismatch"));
                return stack;
            }

            // Inspect cooldown before shrinking; empty stacks lose their components.
            UseCooldown cooldown = stack.get(DataComponents.USE_COOLDOWN);
            int cooldownTicks = cooldown != null ? cooldown.ticks() : COOLDOWN_TICKS;
            ItemStack cooldownSource = stack.copy();
            if (!returned.getAbilities().instabuild) {
                stack.shrink(1);
                // Update the exact initiating hand on the returned player.
                returned.setItemInHand(hand, stack.isEmpty() ? ItemStack.EMPTY : stack);
            }
            // Adding a cooldown by the pre-consumption item group works when
            // the last stack has become empty. Oneiric Awakener is stacksTo(1).
            if (cooldownTicks > 0 && OneiricReturnPolicy.shouldApplyCooldown(Result.SUCCESS)) {
                returned.getCooldowns().addCooldown(cooldownSource, cooldownTicks);
            }
            // Close the expedition only after the same verified teleported
            // player has paid (or retained item in Creative). Not from the
            // ChangedDimensionEvent, which fires mid-teleport.
            DreamRespawnService.completeExpedition(returned);
            returned.sendSystemMessage(Component.translatable(
                    "message.dreamsdimensions.oneiric_awakener.success"));
            DreamQaRecorder.global().record("RECOVERY", "oneiric_awakener",
                    "DREAM_RETURN_COMPLETED", "minecraft:overworld", "PASS", "CONFIRMED");
            LOGGER.info("ONEIRIC_RETURN player_uuid={} source={} target={} result=success "
                            + "consumed={} hand={} before={} after={}",
                    uuid, source.identifier(), returned.level().dimension().identifier(),
                    OneiricReturnPolicy.consumption(Result.SUCCESS, returned.getAbilities().instabuild),
                    hand, before, stack.getCount());
            return stack;
        } finally {
            // ChangedDimensionEvent fires *inside* teleport: it must not clear
            // the in-flight claim; finish is the only commit/cleanup boundary.
            GUARD.finish(uuid);
        }
    }

    private static void failed(ServerPlayer player, UUID uuid, InteractionHand hand,
            String source, Result reason) {
        DreamQaRecorder.global().record("RECOVERY", "oneiric_awakener",
                "DREAM_RETURN_REJECTED", source, "FAIL", reason.name());
        LOGGER.warn("ONEIRIC_RETURN player_uuid={} source={} target={} result={} consumed=0 hand={}",
                uuid, source, player.level().dimension().identifier(), reason, hand);
        if (player.level().dimension() != Level.OVERWORLD) {
            player.sendSystemMessage(Component.translatable(
                    "message.dreamsdimensions.oneiric_awakener.fail"));
        }
    }

    public static void onStoppedUsing(
            net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent.Stop event) {
        // Ignore Stop events from unrelated items. Only the original
        // Awakener's interrupted use can invalidate its pending ticket.
        if (event.getEntity() instanceof ServerPlayer player
                && event.getItem().is(ModItems.ONEIRIC_AWAKENER.get())) {
            GUARD.cancel(player.getUUID());
        }
    }

    public static void onPlayerLoggedOut(
            net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        GUARD.cancel(event.getEntity().getUUID());
    }

    public static void onPlayerClone(
            net.neoforged.neoforge.event.entity.player.PlayerEvent.Clone event) {
        GUARD.cancel(event.getEntity().getUUID());
    }

    public static void onChangedDimension(
            net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerChangedDimensionEvent event) {
        GUARD.cancel(event.getEntity().getUUID());
    }

    public static void onServerStopped(
            net.neoforged.neoforge.event.server.ServerStoppedEvent event) {
        GUARD.clear();
    }

    private static OneiricReturnGuard.Hand guardHand(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND
                ? OneiricReturnGuard.Hand.MAIN_HAND : OneiricReturnGuard.Hand.OFF_HAND;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
            TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("tooltip.dreamsdimensions.oneiric_awakener.line1")
                .withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable("tooltip.dreamsdimensions.oneiric_awakener.line2")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        super.appendHoverText(stack, context, display, tooltip, flag);
    }
}
