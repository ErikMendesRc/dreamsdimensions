package com.dreamsdimensions.mod.registry;

import com.dreamsdimensions.mod.DreamsDimensions;
import com.dreamsdimensions.mod.attachment.DreamReturnData;
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

    private ModAttachments() {
    }

    public static void register(IEventBus eventBus) {
        ATTACHMENTS.register(eventBus);
    }
}
