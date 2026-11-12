package com.dreamsdimensions.mod.qa;

import com.dreamsdimensions.mod.DreamsDimensions;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Path;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

/** Always discoverable; event collection is opt-in and disabled by default. */
public final class DreamQaCommand {
    private DreamQaCommand() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("dreamqa")
                // Only status/help are public. Mutations and exports require operator permission.
                .executes(ctx -> status(ctx.getSource()))
                .then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
                .then(Commands.literal("help").executes(ctx -> help(ctx.getSource())))
                .then(Commands.literal("enable")
                        .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                        .executes(ctx -> setEnabled(ctx.getSource(), true)))
                .then(Commands.literal("disable")
                        .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                        .executes(ctx -> setEnabled(ctx.getSource(), false)))
                .then(Commands.literal("snapshot")
                        .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                        .executes(ctx -> snapshot(ctx.getSource())))
                .then(Commands.literal("reset")
                        .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                        .executes(ctx -> reset(ctx.getSource()))));
    }

    private static int setEnabled(CommandSourceStack source, boolean active) {
        DreamQaRecorder.global().setEnabled(active);
        source.sendSuccess(() -> Component.literal("DREAM_QA status=OK enabled=" + active), false);
        return 1;
    }

    private static int status(CommandSourceStack source) {
        DreamQaRecorder qa = DreamQaRecorder.global();
        source.sendSuccess(() -> Component.literal("DREAM_QA status=OK enabled=" + qa.enabled()
                + " run_id=" + qa.runId() + " buffered=" + qa.bufferedEvents()
                + " capacity=" + qa.capacity()
                + (qa.enabled() ? "" : " hint=operator_can_run_dreamqa_enable")), false);
        return 1;
    }

    private static int snapshot(CommandSourceStack source) {
        // A disabled collector has no evidence to export. This is a normal
        // user-facing state, not a filesystem exception or a gameplay failure.
        if (!DreamQaRecorder.global().enabled()) {
            source.sendFailure(Component.literal(
                    "DREAM_QA_SNAPSHOT status=FAIL reason=telemetry_disabled "
                    + "action=/dreamqa enable"));
            return 0;
        }
        try {
            Path directory = Path.of(System.getProperty("dreamsdimensions.qa.reportDir", "qa-reports"));
            Path file = DreamQaRecorder.global().writeSnapshot(directory);
            source.sendSuccess(() -> Component.literal("DREAM_QA_SNAPSHOT status=OK file="
                    + file.toAbsolutePath()), false);
            return 1;
        } catch (AccessDeniedException error) {
            return snapshotFailed(source, "access_denied", error);
        } catch (IOException error) {
            return snapshotFailed(source, "io_error", error);
        } catch (IllegalStateException error) {
            // Covers a concurrent operator disable during snapshot.
            return snapshotFailed(source, "telemetry_disabled", error);
        } catch (RuntimeException error) {
            return snapshotFailed(source, "invalid_path_or_runtime_error", error);
        }
    }

    private static int snapshotFailed(CommandSourceStack source, String reason, Exception error) {
        // Preserve stack trace in server log, but never echo local paths or
        // arbitrary exception text into chat. Reason is a stable diagnostic ID.
        DreamsDimensions.LOGGER.warn("DREAM_QA_SNAPSHOT status=FAIL reason={}", reason, error);
        source.sendFailure(Component.literal("DREAM_QA_SNAPSHOT status=FAIL reason=" + reason
                + " action=check_latest.log"));
        return 0;
    }

    private static int reset(CommandSourceStack source) {
        DreamQaRecorder.global().reset();
        source.sendSuccess(() -> Component.literal("DREAM_QA_RESET status=OK"), false);
        return 1;
    }

    private static int help(CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal(
                "/dreamqa status | /dreamqa help | /dreamqa enable | /dreamqa disable"
                + " | /dreamqa snapshot | /dreamqa reset (admin: enable/disable/snapshot/reset)"), false);
        return 1;
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        DreamQaRecorder.global().reset();
        // Integrated server may restart without restarting Minecraft's JVM.
        DreamQaRecorder.global().setEnabled(Boolean.getBoolean("dreamsdimensions.qa.enabled"));
    }
}
