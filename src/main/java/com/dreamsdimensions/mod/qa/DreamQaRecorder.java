package com.dreamsdimensions.mod.qa;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Task 7.1: bounded, opt-in, server-side diagnostic recorder.
 * Pure Java: safe for JUnit without a Minecraft runtime.
 * No player identifiers, no network requests and no per-tick disk writes.
 */
public final class DreamQaRecorder {
    public static final String SCHEMA = "dreamsdimensions.qa.v1";
    private static final DreamQaRecorder GLOBAL = new DreamQaRecorder(
            Boolean.getBoolean("dreamsdimensions.qa.enabled"),
            Integer.getInteger("dreamsdimensions.qa.maxBuffer", 512),
            System.getProperty("dreamsdimensions.qa.runId", "runtime"));

    public record Event(String timestampUtc, String category, String subsystem,
            String name, String dimension, String status, String reason, Long durationMs) {}

    // JVM opt-in remains the default, but operators can toggle collection in-game.
    private volatile boolean enabled;
    private final int capacity;
    private final String runId;
    private final ArrayDeque<Event> events = new ArrayDeque<>();
    private final ConcurrentHashMap<String, LongAdder> counters = new ConcurrentHashMap<>();
    private long dropped;

    public DreamQaRecorder(boolean enabled, int capacity, String runId) {
        this.enabled = enabled;
        this.capacity = Math.max(16, Math.min(4096, capacity));
        this.runId = safeToken(runId);
    }

    public static DreamQaRecorder global() {
        return GLOBAL;
    }

    public boolean enabled() {
        return enabled;
    }

    /** Session-only operator toggle. Disabling clears buffered events and counters. */
    public void setEnabled(boolean state) {
        synchronized (events) {
            enabled = state;
            if (!state) {
                events.clear();
                counters.clear();
                dropped = 0;
            }
        }
    }

    public String runId() {
        return runId;
    }

    public int capacity() {
        return capacity;
    }

    public void record(String category, String subsystem, String name,
            String dimension, String status, String reason) {
        record(category, subsystem, name, dimension, status, reason, null);
    }

    public void record(String category, String subsystem, String name,
            String dimension, String status, String reason, Long durationMs) {
        if (!enabled) return;
        // Only allow bounded identifiers. Never serialize arbitrary player/user data.
        String cat = safeToken(category);
        String sys = safeToken(subsystem);
        String event = safeToken(name);
        String dim = safeToken(dimension);
        String state = safeToken(status);
        String code = safeToken(reason);
        if (!List.of("PASS", "FAIL", "WARN", "SKIPPED", "NOT_RUN").contains(state)) {
            return; // A bad diagnostic must never interrupt gameplay.
        }
        if (durationMs != null && durationMs < 0) return;
        String key = cat + "." + sys + "." + event + "." + state;
        Event row = new Event(Instant.now().toString(), cat, sys, event, dim, state, code, durationMs);
        synchronized (events) {
            if (!enabled) return; // No event may survive an operator disable.
            counters.computeIfAbsent(key, ignored -> new LongAdder()).increment();
            if (events.size() == capacity) {
                events.removeFirst();
                dropped++;
            }
            events.addLast(row);
        }
    }

    public void reset() {
        synchronized (events) {
            events.clear();
            dropped = 0L;
            counters.clear();
        }
    }

    public int bufferedEvents() {
        synchronized (events) {
            return events.size();
        }
    }

    public String snapshotJson() {
        List<Event> copy;
        long droppedCopy;
        Map<String, Long> counts = new HashMap<>();
        synchronized (events) {
            copy = new ArrayList<>(events);
            droppedCopy = dropped;
            counters.forEach((key, value) -> counts.put(key, value.sum()));
        }
        StringBuilder out = new StringBuilder(4096);
        out.append("{\n  \"schema_version\": ").append(quote(SCHEMA))
                .append(",\n  \"run_id\": ").append(quote(runId))
                .append(",\n  \"generated_at_utc\": ").append(quote(Instant.now().toString()))
                .append(",\n  \"enabled\": ").append(enabled)
                .append(",\n  \"evidence_type\": \"RUNTIME_OBSERVED\"")
                .append(",\n  \"buffer_capacity\": ").append(capacity)
                .append(",\n  \"buffered_events\": ").append(copy.size())
                .append(",\n  \"dropped_events\": ").append(droppedCopy)
                .append(",\n  \"counters\": {");
        boolean first = true;
        for (String key : counts.keySet().stream().sorted().toList()) {
            if (!first) out.append(',');
            first = false;
            out.append("\n    ").append(quote(key)).append(": ").append(counts.get(key));
        }
        if (!first) out.append('\n');
        out.append("  },\n  \"events\": [");
        for (int i = 0; i < copy.size(); i++) {
            Event e = copy.get(i);
            if (i != 0) out.append(',');
            out.append("\n    {\"schema_version\": ").append(quote(SCHEMA))
                    .append(", \"timestamp_utc\": ").append(quote(e.timestampUtc()))
                    .append(", \"run_id\": ").append(quote(runId))
                    .append(", \"event_type\": \"DIAGNOSTIC\"")
                    .append(", \"category\": ").append(quote(e.category()))
                    .append(", \"subsystem\": ").append(quote(e.subsystem()))
                    .append(", \"event_name\": ").append(quote(e.name()))
                    .append(", \"dimension\": ").append(quote(e.dimension()))
                    .append(", \"status\": ").append(quote(e.status()))
                    .append(", \"evidence_type\": \"RUNTIME_OBSERVED\"")
                    .append(", \"metadata\": {\"reason\": ").append(quote(e.reason())).append('}');
            if (e.durationMs() != null) out.append(", \"duration_ms\": ").append(e.durationMs());
            out.append('}');
        }
        if (!copy.isEmpty()) out.append('\n');
        return out.append("  ]\n}\n").toString();
    }

    /** Admin-only, bounded export. Path is chosen by code/config, not by command input. */
    public Path writeSnapshot(Path directory) throws IOException {
        if (!enabled) throw new IllegalStateException("QA is disabled");
        Files.createDirectories(directory);
        Path destination = directory.resolve("dreamqa-" + runId + ".json");
        Path temporary = Files.createTempFile(directory, "dreamqa-", ".tmp");
        try {
            Files.writeString(temporary, snapshotJson(), StandardCharsets.UTF_8);
            Files.move(temporary, destination, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
        return destination;
    }

    private static String safeToken(String raw) {
        if (raw == null || raw.isBlank()) return "unspecified";
        String safe = raw.replaceAll("[^A-Za-z0-9_.:-]", "_");
        return safe.length() <= 96 ? safe : safe.substring(0, 96);
    }

    private static String quote(String value) {
        StringBuilder out = new StringBuilder(value.length() + 2).append('"');
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 32) {
                        String hex = Integer.toHexString(c);
                        out.append("\\u");
                        out.append("0".repeat(4 - hex.length()));
                        out.append(hex);
                    } else out.append(c);
                }
            }
        }
        return out.append('"').toString();
    }
}
