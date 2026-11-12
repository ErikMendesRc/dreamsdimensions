package com.dreamsdimensions.mod.anomaly;

import static com.dreamsdimensions.mod.anomaly.OneiricAnomalyContract.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class OneiricAnomalyContractTest {
    private static final Pos ANCHOR = new Pos(0, 64, 0);
    private static final Pos GEOLOGY = new Pos(24, 64, 0);
    private static final Pos CORROBORATION = new Pos(0, 64, 32);
    private static final Pos INTERPRETATION = new Pos(8, 64, 8);
    private static final Pos PAYOFF = new Pos(80, 64, 48);

    private Site site() {
        return new Site("dreamsdimensions:blue_field/cell_0_0", 14011L, OVERWORLD,
                Lineage.BLUE_FIELD, Origin.NATURAL_WORLDGEN,
                ANCHOR, GEOLOGY, CORROBORATION, INTERPRETATION, PAYOFF, true);
    }

    private Evidence proof(Signal signal, Pos at) {
        return new Evidence(site().stableId(), OVERWORLD, signal, at,
                Origin.NATURAL_WORLDGEN, true, true);
    }

    private Progress start() {
        return Progress.empty();
    }

    private Progress noticed() {
        return apply(site(), start(), proof(Signal.NATURAL_MARK, ANCHOR)).progress();
    }

    private Progress corroborated() {
        Progress first = noticed();
        first = apply(site(), first, proof(Signal.GEOLOGIC_DISCONTINUITY, GEOLOGY)).progress();
        return apply(site(), first, proof(Signal.AMBIENT_INVERSION, CORROBORATION)).progress();
    }

    private Progress interpreted() {
        return apply(site(), corroborated(), proof(Signal.SLEEP_RECALL, INTERPRETATION)).progress();
    }

    @Test void fourNarrativeStagesNeedActualProgress() {
        assertEquals(Stage.HIDDEN, start().stage());
        assertEquals(Stage.NOTICED, noticed().stage());
        assertEquals(Stage.CORRELATED, corroborated().stage());
        assertEquals(Stage.INTERPRETED, interpreted().stage());
        Transition finalStep = apply(site(), interpreted(), proof(Signal.PHYSICAL_PAYOFF, PAYOFF));
        assertEquals(Stage.RESOLVED, finalStep.progress().stage());
        assertTrue(finalStep.discoveryUnlocked());
        assertEquals(ResultType.ADVANCED, finalStep.result());
    }

    @Test void twoIndependentCluesMandatory() {
        Progress p = noticed();
        p = apply(site(), p, proof(Signal.GEOLOGIC_DISCONTINUITY, GEOLOGY)).progress();
        assertEquals(Stage.NOTICED, p.stage());
        Transition shortcut = apply(site(), p, proof(Signal.WAKING_RECONCILIATION, INTERPRETATION));
        assertEquals(Reason.PREMATURE, shortcut.reason());
        assertEquals(p, shortcut.progress());
        assertFalse(shortcut.discoveryUnlocked());
    }

    @Test void wakingInterpretationCanReplaceSleepSafely() {
        Progress p = corroborated();
        Transition awake = apply(site(), p,
                proof(Signal.WAKING_RECONCILIATION, INTERPRETATION));
        assertEquals(Stage.INTERPRETED, awake.progress().stage());
        assertEquals(ResultType.ADVANCED, awake.result());
        // Task 7.6 never tries to enter a dream or override existing bed entry.
        assertEquals(Stage.INTERPRETED, awake.progress().stage());
    }

    @Test void visualFallbackMakesAudioOptional() {
        Progress p = noticed();
        p = apply(site(), p, proof(Signal.GEOLOGIC_DISCONTINUITY, GEOLOGY)).progress();
        Transition visual = apply(site(), p, proof(Signal.VISUAL_FALLBACK, CORROBORATION));
        assertEquals(Stage.CORRELATED, visual.progress().stage());
        Transition audio = apply(site(), visual.progress(),
                proof(Signal.AMBIENT_INVERSION, CORROBORATION));
        assertEquals(ResultType.NO_CHANGE, audio.result());
    }

    @Test void duplicateEvidenceIsIdempotent() {
        Progress p = noticed();
        Transition twice = apply(site(), p, proof(Signal.NATURAL_MARK, ANCHOR));
        assertEquals(ResultType.NO_CHANGE, twice.result());
        assertEquals(p, twice.progress());
        assertFalse(twice.discoveryUnlocked());
    }

    @Test void cannotResolveWithoutInterpretation() {
        Transition denied = apply(site(), corroborated(),
                proof(Signal.PHYSICAL_PAYOFF, PAYOFF));
        assertEquals(Reason.PREMATURE, denied.reason());
        assertFalse(denied.discoveryUnlocked());
    }

    @Test void cannotResolveWhenPhysicalTargetMissing() {
        Progress p = interpreted();
        Evidence noTarget = new Evidence(site().stableId(), OVERWORLD,
                Signal.PHYSICAL_PAYOFF, PAYOFF, Origin.NATURAL_WORLDGEN,
                true, false);
        Transition missing = apply(site(), p, noTarget);
        assertEquals(Reason.TARGET_UNAVAILABLE, missing.reason());
        assertEquals(p, missing.progress());
        assertFalse(missing.discoveryUnlocked());
    }

    @Test void cannotFarmFinalRewardAfterResolution() {
        Progress p = apply(site(), interpreted(), proof(Signal.PHYSICAL_PAYOFF, PAYOFF)).progress();
        Transition duplicate = apply(site(), p, proof(Signal.PHYSICAL_PAYOFF, PAYOFF));
        assertEquals(ResultType.NO_CHANGE, duplicate.result());
        assertEquals(Reason.ALREADY_RESOLVED, duplicate.reason());
        assertFalse(duplicate.discoveryUnlocked());
    }

    @Test void playerPlacedStoneNeverCreatesRealAnomaly() {
        Site mock = new Site(site().stableId(), site().worldSeed(), OVERWORLD,
                site().lineage(), Origin.PLAYER_PLACED,
                ANCHOR, GEOLOGY, CORROBORATION, INTERPRETATION, PAYOFF, true);
        Transition denied = apply(mock, start(), proof(Signal.NATURAL_MARK, ANCHOR));
        assertEquals(Reason.INVALID_PROVENANCE, denied.reason());
        assertEquals(Stage.HIDDEN, denied.progress().stage());
        Evidence placed = new Evidence(site().stableId(), OVERWORLD,
                Signal.NATURAL_MARK, ANCHOR, Origin.PLAYER_PLACED, true, false);
        assertEquals(Reason.INVALID_PROVENANCE,
                apply(site(), start(), placed).reason());
    }

    @Test void clientMessageDoesNotCountAsTrustedProof() {
        Evidence spoof = new Evidence(site().stableId(), OVERWORLD,
                Signal.NATURAL_MARK, ANCHOR, Origin.NATURAL_WORLDGEN, false, false);
        assertEquals(Reason.UNTRUSTED_EVENT,
                apply(site(), start(), spoof).reason());
    }

    @Test void requiresExactWorldSiteAndProximity() {
        Evidence wrongWorld = new Evidence(site().stableId(),
                Lineage.BLUE_FIELD.motifDimension(), Signal.NATURAL_MARK,
                ANCHOR, Origin.NATURAL_WORLDGEN, true, true);
        assertEquals(Reason.WRONG_DIMENSION,
                apply(site(), start(), wrongWorld).reason());
        Evidence wrongSite = new Evidence("dreamsdimensions:other/cell", OVERWORLD,
                Signal.NATURAL_MARK, ANCHOR, Origin.NATURAL_WORLDGEN, true, true);
        assertEquals(Reason.WRONG_SITE, apply(site(), start(), wrongSite).reason());
        assertEquals(Reason.OUT_OF_RANGE,
                apply(site(), start(), proof(Signal.NATURAL_MARK, new Pos(9, 64, 0))).reason());
        assertEquals(ResultType.ADVANCED,
                apply(site(), start(), proof(Signal.NATURAL_MARK, new Pos(8, 64, 0))).result());
    }

    @Test void rejectsFakeTrivialDistantOrOverlappingSites() {
        assertThrows(IllegalArgumentException.class, () -> new Site(site().stableId(),
                14011L, OVERWORLD, Lineage.BLUE_FIELD, Origin.NATURAL_WORLDGEN,
                ANCHOR, new Pos(2, 64, 0), CORROBORATION, INTERPRETATION, PAYOFF, true));
        assertThrows(IllegalArgumentException.class, () -> new Site(site().stableId(),
                14011L, OVERWORLD, Lineage.BLUE_FIELD, Origin.NATURAL_WORLDGEN,
                ANCHOR, GEOLOGY, GEOLOGY, INTERPRETATION, PAYOFF, true));
        assertThrows(IllegalArgumentException.class, () -> new Site(site().stableId(),
                14011L, OVERWORLD, Lineage.BLUE_FIELD, Origin.NATURAL_WORLDGEN,
                ANCHOR, GEOLOGY, CORROBORATION, INTERPRETATION, new Pos(2, 64, 0), true));
        assertThrows(IllegalArgumentException.class, () -> new Site(site().stableId(),
                14011L, OVERWORLD, Lineage.BLUE_FIELD, Origin.NATURAL_WORLDGEN,
                ANCHOR, GEOLOGY, CORROBORATION, INTERPRETATION, new Pos(400, 64, 0), true));
    }

    @Test void corruptRestoredStateCannotSkipEvidence() {
        Progress fabricated = new Progress(false, false, false, true, true);
        assertFalse(fabricated.valid());
        assertThrows(IllegalArgumentException.class, () -> apply(site(), fabricated,
                proof(Signal.PHYSICAL_PAYOFF, PAYOFF)));
    }

    @Test void noCommittedPayoffMeansNoFakePayoff() {
        Site incomplete = new Site(site().stableId(), site().worldSeed(),
                OVERWORLD, site().lineage(), Origin.NATURAL_WORLDGEN,
                ANCHOR, GEOLOGY, CORROBORATION, INTERPRETATION, PAYOFF, false);
        assertEquals(Reason.TARGET_UNAVAILABLE,
                apply(incomplete, interpreted(), proof(Signal.PHYSICAL_PAYOFF, PAYOFF)).reason());
    }

    @Test void playerProgressIsExplicitlyIndependent() {
        Progress playerA = interpreted();
        Progress playerB = noticed();
        Progress playerAResolved = apply(site(), playerA,
                proof(Signal.PHYSICAL_PAYOFF, PAYOFF)).progress();
        assertEquals(Stage.RESOLVED, playerAResolved.stage());
        assertEquals(Stage.NOTICED, playerB.stage());
    }

    @Test void cellMathIsStableAndNegativeCoordinatesBehave() {
        assertEquals(new AnomalyScenePlanner.Cell(-1, -1),
                AnomalyScenePlanner.containingChunk(-1, -1));
        assertEquals(new AnomalyScenePlanner.Cell(-2, 1),
                AnomalyScenePlanner.containingChunk(-13, 12));
        assertEquals(new AnomalyScenePlanner.Cell(0, 0),
                AnomalyScenePlanner.containingChunk(11, 11));
    }

    @Test void proposalStableAcrossLoadOrderAndNotAClaimOfWorldgen() {
        AnomalyScenePlanner.Candidate candidate = null;
        for (int x = -80; x <= 80 && candidate == null; x++) {
            candidate = AnomalyScenePlanner.propose(14011L, x * 12, -4);
        }
        assertNotNull(candidate, "At least one candidate should exist in a large deterministic range");
        int sameCellX = candidate.cell().x() * 12 + 7;
        int sameCellZ = candidate.cell().z() * 12 + 5;
        AnomalyScenePlanner.Candidate repeat =
                AnomalyScenePlanner.propose(14011L, sameCellX, sameCellZ);
        assertEquals(candidate, repeat);
        assertTrue(candidate.candidateOnly());
        assertTrue(candidate.stableId().startsWith("dreamsdimensions:"));
        var proposal = AnomalyScenePlanner.geometry(candidate, new Pos(-64, 62, -64));
        assertFalse(proposal.terrainVerified());
        assertNotEquals(proposal.anchor(), proposal.physicalPayoff());
        assertTrue(proposal.anchor().distanceSquared(proposal.physicalPayoff())
                >= (long) PAYOFF_MIN_DISTANCE * PAYOFF_MIN_DISTANCE);
    }

    @Test void candidateIsSparseButNeverDependsOnPlayerTick() {
        int candidateCells = 0;
        Set<String> keys = new HashSet<>();
        for (int x = -20; x <= 20; x++) {
            for (int z = -20; z <= 20; z++) {
                int cx = x * 12;
                int cz = z * 12;
                var candidate = AnomalyScenePlanner.propose(20261108L, cx, cz);
                if (candidate == null) continue;
                candidateCells++;
                assertTrue(keys.add(candidate.stableId()));
            }
        }
        assertTrue(candidateCells > 250 && candidateCells < 850,
                "Selector proposal must remain neither ubiquitous nor effectively absent");
    }
}
