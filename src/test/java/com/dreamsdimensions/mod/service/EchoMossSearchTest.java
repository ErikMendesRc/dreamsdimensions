package com.dreamsdimensions.mod.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class EchoMossSearchTest {
    private static final EchoMossSearch.Cell ORIGIN = new EchoMossSearch.Cell(0, 100, 0);

    private static EchoMossSearch.Result find(Set<EchoMossSearch.Cell> targets) {
        return EchoMossSearch.findNearest(ORIGIN, 0, 256,
                (x, y, z) -> targets.contains(new EchoMossSearch.Cell(x, y, z))
                        ? EchoMossSearch.Sample.TARGET : EchoMossSearch.Sample.OTHER);
    }

    @Test
    void radiusIncludesTwelveExcludesThirteen() {
        assertEquals(new EchoMossSearch.Cell(12, 100, 0),
                find(Set.of(new EchoMossSearch.Cell(12, 100, 0),
                        new EchoMossSearch.Cell(13, 100, 0))).target());
        assertFalse(find(Set.of(new EchoMossSearch.Cell(13, 100, 0))).found());
        assertFalse(find(Set.of(new EchoMossSearch.Cell(9, 100, 9))).found());
    }

    @Test
    void verticalRangeIncludesSixExcludesSeven() {
        assertEquals(new EchoMossSearch.Cell(0, 106, 0),
                find(Set.of(new EchoMossSearch.Cell(0, 106, 0),
                        new EchoMossSearch.Cell(0, 107, 0))).target());
        assertFalse(find(Set.of(new EchoMossSearch.Cell(0, 107, 0))).found());
    }

    @Test
    void alwaysChoosesNearestInThreeDimensions() {
        assertEquals(new EchoMossSearch.Cell(2, 100, 0),
                find(Set.of(new EchoMossSearch.Cell(6, 100, 0),
                        new EchoMossSearch.Cell(2, 100, 0),
                        new EchoMossSearch.Cell(1, 103, 0))).target());
    }

    @Test
    void tiesBreakByXThenYThenZ() {
        assertEquals(new EchoMossSearch.Cell(-1, 100, 0),
                find(Set.of(new EchoMossSearch.Cell(1, 100, 0),
                        new EchoMossSearch.Cell(-1, 100, 0))).target());
        assertEquals(new EchoMossSearch.Cell(0, 99, 0),
                find(Set.of(new EchoMossSearch.Cell(0, 101, 0),
                        new EchoMossSearch.Cell(0, 99, 0))).target());
        assertEquals(new EchoMossSearch.Cell(0, 100, -1),
                find(Set.of(new EchoMossSearch.Cell(0, 100, 1),
                        new EchoMossSearch.Cell(0, 100, -1))).target());
    }

    @Test
    void absenceAndUnavailableAreDifferentTypedOutcomes() {
        EchoMossSearch.Result absent = find(Set.of());
        assertEquals(EchoMossSearch.Status.ABSENT, absent.status());
        assertNull(absent.target());
        EchoMossSearch.Result unloaded = EchoMossSearch.findNearest(ORIGIN, 0, 256,
                (x, y, z) -> x > 0 ? EchoMossSearch.Sample.UNAVAILABLE : EchoMossSearch.Sample.OTHER);
        assertEquals(EchoMossSearch.Status.UNAVAILABLE, unloaded.status());
        assertTrue(unloaded.unavailable() > 0);
        assertNull(unloaded.target());
        assertEquals(EchoMossSearch.Status.FOUND, EchoMossSearch.findNearest(ORIGIN, 0, 256,
                (x, y, z) -> x == 1 && y == 100 && z == 0
                        ? EchoMossSearch.Sample.TARGET : EchoMossSearch.Sample.UNAVAILABLE).status());
    }

    @Test
    void worldBoundsAndBorderAreSkippedWithoutReadingThem() {
        EchoMossSearch.Result result = EchoMossSearch.findNearest(
                new EchoMossSearch.Cell(0, 0, 0), 0, 1,
                (x, y, z) -> Math.abs(x) > 4
                        ? EchoMossSearch.Sample.OUTSIDE : EchoMossSearch.Sample.OTHER);
        assertEquals(EchoMossSearch.Status.ABSENT, result.status());
        assertEquals(0, result.unavailable());
        assertTrue(result.inspected() <= EchoMossSearch.MAX_BOX_POSITIONS);
        assertTrue(result.inspected() > 0);
    }

    @Test
    void searchRemainsStrictlyBounded() {
        int[] visits = {0};
        EchoMossSearch.Result result = EchoMossSearch.findNearest(ORIGIN, 0, 256,
                (x, y, z) -> {
                    visits[0]++;
                    return EchoMossSearch.Sample.OTHER;
                });
        assertEquals(visits[0], result.inspected());
        assertTrue(visits[0] < EchoMossSearch.MAX_BOX_POSITIONS);
        assertEquals(EchoMossSearch.Status.ABSENT, result.status());
    }
}
