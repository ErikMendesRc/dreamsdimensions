package com.dreamsdimensions.mod.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import java.util.Optional;

/**
 * One persistent, server-owned dream expedition spawn anchor per player.
 * Unlike dream_return, this is NEVER an Overworld return address.
 * Null/absent fields allow saves predating Task 5.5 to load normally.
 */
public record DreamExpeditionAnchorData(ResourceKey<Level> dimension, BlockPos pos, Float yaw) {
    public static final MapCodec<DreamExpeditionAnchorData> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResourceKey.codec(Registries.DIMENSION).optionalFieldOf("dimension")
                            .forGetter(data -> Optional.ofNullable(data.dimension)),
                    BlockPos.CODEC.optionalFieldOf("pos")
                            .forGetter(data -> Optional.ofNullable(data.pos)),
                    Codec.FLOAT.optionalFieldOf("yaw")
                            .forGetter(data -> Optional.ofNullable(data.yaw))
            ).apply(instance, (dimension, pos, yaw) ->
                    new DreamExpeditionAnchorData(
                            dimension.orElse(null), pos.orElse(null), yaw.orElse(null))));

    public DreamExpeditionAnchorData() {
        this(null, null, null);
    }

    public boolean isActiveFor(ResourceKey<Level> level) {
        return pos != null && dimension != null && dimension.equals(level);
    }

    public float yawOr(float fallback) {
        return yaw == null ? fallback : yaw;
    }
}
