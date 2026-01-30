package com.dreamsdimensions.mod.event;

import com.dreamsdimensions.mod.attachment.DreamReturnData;
import com.dreamsdimensions.mod.registry.ModAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.entity.player.PlayerSetSpawnEvent;

/**
 * Atualiza o attachment de retorno ao sonho quando o jogador define spawn no Overworld.
 */
public final class DreamReturnAttachmentHandler {
    private DreamReturnAttachmentHandler() {
    }

    public static void onPlayerSetSpawn(PlayerSetSpawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (player.level().isClientSide()) {
            return;
        }
        if (event.getNewSpawn() == null || event.getSpawnLevel() != Level.OVERWORLD) {
            return;
        }

        DreamReturnData data = player.getData(ModAttachments.DREAM_RETURN);
        data.setOverworldBed(event.getNewSpawn(), player.getYRot());
    }
}
