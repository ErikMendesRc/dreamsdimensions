package com.dreamsdimensions.mod.registry;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.attachment.DreamReturnData;
import com.dreamsdimensions.mod.attachment.DreamHangoverData;
import com.dreamsdimensions.mod.attachment.DreamExpeditionAnchorData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Registro de Data Attachments do mod.
 */
public final class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, DreamsDimensions.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<DreamReturnData>> DREAM_RETURN = ATTACHMENTS.register(
            "dream_return",
            () -> AttachmentType.builder(DreamReturnData::new)
                    .serialize(DreamReturnData.CODEC)
                    .copyOnDeath()
                    .build()
    );

    /**
     * Persistent local dream respawn point. This deliberately does NOT replace
     * or modify the Overworld-return attachment.
     */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<DreamExpeditionAnchorData>>
            DREAM_EXPEDITION_ANCHOR = ATTACHMENTS.register(
                    "dream_expedition_anchor",
                    () -> AttachmentType.builder(DreamExpeditionAnchorData::new)
                            .serialize(DreamExpeditionAnchorData.CODEC)
                            .copyOnDeath()
                            .build()
            );

    /** Three-night penalty survives logout and death; never overwrites dream_return. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<DreamHangoverData>>
            DREAM_HANGOVER = ATTACHMENTS.register(
                    "dream_hangover",
                    () -> AttachmentType.builder(() -> new DreamHangoverData(0, Long.MIN_VALUE))
                            .serialize(DreamHangoverData.CODEC)
                            .copyOnDeath()
                            .build()
            );

    private ModAttachments() {
    }

    public static void register(IEventBus eventBus) {
        ATTACHMENTS.register(eventBus);
    }
}
