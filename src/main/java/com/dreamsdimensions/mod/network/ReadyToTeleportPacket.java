package com.dreamsdimensions.mod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record ReadyToTeleportPacket() implements CustomPacketPayload {
    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath("dreamsdimensions", "ready_teleport");
    public static final Type<ReadyToTeleportPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, ReadyToTeleportPacket> CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {},
                    buf -> new ReadyToTeleportPacket()
            );

    @Override
    public @NotNull Type<ReadyToTeleportPacket> type() {
        return TYPE;
    }
}