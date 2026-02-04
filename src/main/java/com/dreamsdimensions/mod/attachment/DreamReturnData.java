package com.dreamsdimensions.mod.attachment;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class DreamReturnData {

    public static final MapCodec<DreamReturnData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(Registries.DIMENSION)
                    .optionalFieldOf("dimension")
                    .forGetter(data -> Optional.ofNullable(data.dimension)),
            BlockPos.CODEC
                    .optionalFieldOf("pos")
                    .forGetter(data -> Optional.ofNullable(data.pos)),
            Codec.FLOAT
                    .optionalFieldOf("yaw")
                    .forGetter(data -> Optional.ofNullable(data.yaw))
    ).apply(instance, (dimension, pos, yaw) -> new DreamReturnData(
            dimension.orElse(null),
            pos.orElse(null),
            yaw.orElse(null)
    )));

    @Nullable private ResourceKey<Level> dimension;
    @Nullable private BlockPos pos;
    @Nullable private Float yaw;

    public DreamReturnData() {}

    private DreamReturnData(@Nullable ResourceKey<Level> dimension, @Nullable BlockPos pos, @Nullable Float yaw) {
        this.dimension = dimension;
        this.pos = pos;
        this.yaw = yaw;
    }

    public void setOverworldBed(BlockPos pos, float yaw) {
        this.dimension = Level.OVERWORLD;
        this.pos = pos;
        this.yaw = yaw;
    }

    public boolean hasBedData() {
        return dimension != null && pos != null;
    }

    @Nullable
    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    @Nullable
    public BlockPos getPos() {
        return pos;
    }

    public float getYawOr(float fallback) {
        return yaw != null ? yaw : fallback;
    }
}