package com.dreamsdimensions.mod.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record MyPacket(String message) implements CustomPacketPayload {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath("dreamsdimensions", "my_packet");
    public static final Type<MyPacket> TYPE = new Type<>(ID);

    public static final StreamCodec<FriendlyByteBuf, MyPacket> CODEC = StreamCodec.of(
            (buf, packet) -> buf.writeUtf(packet.message()),
            buf -> new MyPacket(buf.readUtf())
    );

    @Override
    public @NotNull Type<MyPacket> type() {
        return TYPE;
    }
}