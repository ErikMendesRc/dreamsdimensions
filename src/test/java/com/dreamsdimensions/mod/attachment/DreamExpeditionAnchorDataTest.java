package com.dreamsdimensions.mod.attachment;

import static org.junit.jupiter.api.Assertions.*;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.Test;

/** Persistent attachment codec and backward-compatible absence semantics. */
class DreamExpeditionAnchorDataTest {
    private static final ResourceKey<Level> DREAMSCAPE = ResourceKey.create(
            Registries.DIMENSION, Identifier.fromNamespaceAndPath("dreamsdimensions", "dreamscape"));
    private static final ResourceKey<Level> BLUE = ResourceKey.create(
            Registries.DIMENSION, Identifier.fromNamespaceAndPath("dreamsdimensions", "campo_onirico_azul"));

    @Test void oldSaveWithNoAttachmentHasSafeDefault() {
        DreamExpeditionAnchorData noAnchor = new DreamExpeditionAnchorData();
        assertNull(noAnchor.dimension());
        assertNull(noAnchor.pos());
        assertNull(noAnchor.yaw());
        assertFalse(noAnchor.isActiveFor(DREAMSCAPE));
    }

    @Test void correctlyMatchesOnlyTheDeathDimension() {
        var anchor = new DreamExpeditionAnchorData(DREAMSCAPE, new BlockPos(1,188,20), 90F);
        assertTrue(anchor.isActiveFor(DREAMSCAPE));
        assertFalse(anchor.isActiveFor(BLUE));
        assertFalse(anchor.isActiveFor(Level.OVERWORLD));
        assertEquals(90F, anchor.yawOr(0F));
    }

    @Test void missingPositionAndDimensionIsNotActive() {
        assertFalse(new DreamExpeditionAnchorData(DREAMSCAPE,null,90F).isActiveFor(DREAMSCAPE));
        assertFalse(new DreamExpeditionAnchorData(null,new BlockPos(0,100,0),0F).isActiveFor(DREAMSCAPE));
    }

    @Test void missingYawUsesCurrentOrientation() {
        var anchor = new DreamExpeditionAnchorData(BLUE, new BlockPos(-3,70,11), null);
        assertEquals(42F,anchor.yawOr(42F));
    }

    @Test void roundtripViaTheRealMapCodec() {
        var anchor = new DreamExpeditionAnchorData(DREAMSCAPE, new BlockPos(-17,194,32), -130.5F);
        var encoded = DreamExpeditionAnchorData.CODEC.codec()
                .encodeStart(JsonOps.INSTANCE, anchor).getOrThrow();
        var decoded = DreamExpeditionAnchorData.CODEC.codec()
                .parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(anchor, decoded);
        assertTrue(decoded.isActiveFor(DREAMSCAPE));
    }

    @Test void emptyLegacyMapParsesWithoutCrash() {
        var decoded = DreamExpeditionAnchorData.CODEC.codec()
                .parse(JsonOps.INSTANCE, new com.google.gson.JsonObject()).getOrThrow();
        assertFalse(decoded.isActiveFor(DREAMSCAPE));
    }
}
