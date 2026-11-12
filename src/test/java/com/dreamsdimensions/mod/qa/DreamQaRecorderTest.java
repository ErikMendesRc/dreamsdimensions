package com.dreamsdimensions.mod.qa;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DreamQaRecorderTest {
    @TempDir Path temp;

    @Test void disabledRecorderDoesNotCollectOrWrite() {
        DreamQaRecorder qa = new DreamQaRecorder(false, 16, "test-disabled");
        qa.record("SPAWN", "fragmento_errante", "ENTITY_SPAWN_ATTEMPT",
                "dreamsdimensions:dreamscape", "PASS", "CANDIDATE");
        assertEquals(0, qa.bufferedEvents());
        assertTrue(qa.snapshotJson().contains("\"enabled\": false"));
        assertThrows(IllegalStateException.class, () -> qa.writeSnapshot(temp));
    }

    @Test void boundedBufferPreservesCountersAndReportsDrops() {
        DreamQaRecorder qa = new DreamQaRecorder(true, 16, "ci-seed-14011");
        for (int i = 0; i < 22; i++) qa.record("SPAWN", "vigia_adormecido",
                "ENTITY_SPAWN_REJECTED", "dreamsdimensions:dreamscape", "SKIPPED", "LOCAL_CAP");
        assertEquals(16, qa.bufferedEvents());
        String report = qa.snapshotJson();
        assertTrue(report.contains("\"dropped_events\": 6"), report);
        assertTrue(report.contains("SPAWN.vigia_adormecido.ENTITY_SPAWN_REJECTED.SKIPPED\": 22"), report);
        qa.reset();
        assertEquals(0, qa.bufferedEvents());
        assertTrue(qa.snapshotJson().contains("\"dropped_events\": 0"));
    }

    @Test void generatesSnapshotWithVersionAndBoundedIdentifiers() throws Exception {
        DreamQaRecorder qa = new DreamQaRecorder(true, 128, "ci-seed-20261108");
        qa.record("TELEPORT", "dream_entry", "DREAM_ENTRY_COMPLETED",
                "dreamsdimensions:dreamscape", "PASS", "CONFIRMED", 18L);
        qa.record("RECOVERY", "oneiric_awakener", "DREAM_RETURN_REJECTED",
                "dreamsdimensions:dreamscape", "FAIL", "INVALID_TARGET");
        Path path = qa.writeSnapshot(temp);
        assertEquals("dreamqa-ci-seed-20261108.json", path.getFileName().toString());
        String json = Files.readString(path);
        assertTrue(json.contains("\"schema_version\": \"dreamsdimensions.qa.v1\""));
        assertTrue(json.contains("\"duration_ms\": 18"));
        assertTrue(json.contains("\"evidence_type\": \"RUNTIME_OBSERVED\""));
        assertFalse(json.contains("player_uuid"));
    }

    @Test void invalidDiagnosticStateDoesNotBreakGameplay() {
        DreamQaRecorder qa = new DreamQaRecorder(true, 16, "custom");
        qa.record("SPAWN", "mob", "EVENT", "dim", "UNEXPECTED", "cause");
        qa.record("SPAWN", "mob", "EVENT", "dim", "PASS", "cause", -1L);
        assertEquals(0, qa.bufferedEvents());
    }

    @Test void reportIsThreadSafeAndBounded() throws Exception {
        DreamQaRecorder qa = new DreamQaRecorder(true, 64, "concurrency");
        Thread[] workers = new Thread[6];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Thread(() -> {
                for (int j = 0; j < 100; j++) qa.record(
                        "SPAWN", "mob", "ENTITY_SPAWN_ATTEMPT",
                        "dreamsdimensions:dreamscape", "PASS", "CANDIDATE");
            });
            workers[i].start();
        }
        for (Thread worker : workers) worker.join();
        assertEquals(64, qa.bufferedEvents());
        assertTrue(qa.snapshotJson().contains("SPAWN.mob.ENTITY_SPAWN_ATTEMPT.PASS\": 600"));
        assertTrue(qa.snapshotJson().contains("\"dropped_events\": 536"));
    }
}
