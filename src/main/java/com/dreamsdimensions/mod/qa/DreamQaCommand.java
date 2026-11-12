package com.dreamsdimensions.mod.qa;

import com.dreamsdimensions.mod.DreamsDimensions;
import java.io.IOException;
import java.nio.file.Path;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** Opt-in admin/console-only commands. No world mutation or chunk loads. */
public final class DreamQaCommand {
    private DreamQaCommand() {}

    public static void register(RegisterCommandsEvent event) {
        if (!DreamQaRecorder.global().enabled()) return;
        event.getDispatcher().register(Commands.literal("dreamqa")
                .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                .executes(ctx -> status(ctx.getSource()))
                .then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
                .then(Commands.literal("snapshot").executes(ctx -> snapshot(ctx.getSource())))
                .then(Commands.literal("reset").executes(ctx -> reset(ctx.getSource())))
                .then(Commands.literal("help").executes(ctx -> help(ctx.getSource()))));
    }

    private static int status(CommandSourceStack source) {
        DreamQaRecorder qa = DreamQaRecorder.global();
        source.sendSuccess(() -> Component.literal("DREAM_QA status=OK enabled=" + qa.enabled()
                + " run_id=" + qa.runId() + " buffered=" + qa.bufferedEvents()
                + " capacity=" + qa.capacity()), false);
        return 1;
    }

    private static int snapshot(CommandSourceStack source) {
        try {
            Path directory = Path.of(System.getProperty("dreamsdimensions.qa.reportDir", "qa-reports"));
            Path file = DreamQaRecorder.global().writeSnapshot(directory);
            // The export contains diagnostic aggregates only, never player identifiers.
            source.sendSuccess(() -> Component.literal("DREAM_QA_SNAPSHOT status=OK file="
                    + file.toAbsolutePath()), false);
            return 1;
        } catch (IOException | RuntimeException error) {
            DreamsDimensions.LOGGER.warn("DREAM_QA_SNAPSHOT status=FAIL", error);
            source.sendFailure(Component.literal("DREAM_QA_SNAPSHOT status=FAIL"));
            return 0;
        }
    }

    private static int reset(CommandSourceStack source) {
        DreamQaRecorder.global().reset();
        source.sendSuccess(() -> Component.literal("DREAM_QA_RESET status=OK"), false);
        return 1;
    }

    private static int help(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal(
                "/dreamqa status | /dreamqa snapshot | /dreamqa reset | /dreamqa help"), false);
        return 1;
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        DreamQaRecorder.global().reset();
    }
}
