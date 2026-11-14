package com.dreamsdimensions.mod.dimension;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

/** Small, pure weighted draw without replacement; preserves all fallback candidates. */
public final class WeightedDimensionPicker {
    private WeightedDimensionPicker() {}

    public record Candidate<T>(T value, int weight) {
        public Candidate {
            Objects.requireNonNull(value, "value");
            if (weight <= 0) throw new IllegalArgumentException("weight must be positive");
        }
    }

    public static <T> List<T> order(List<Candidate<T>> candidates, RandomGenerator random) {
        Objects.requireNonNull(candidates, "candidates");
        Objects.requireNonNull(random, "random");
        List<Candidate<T>> remaining = new ArrayList<>(candidates);
        List<T> ordered = new ArrayList<>(remaining.size());
        while (!remaining.isEmpty()) {
            int total = 0;
            for (Candidate<T> candidate : remaining) {
                total = Math.addExact(total, candidate.weight());
            }
            int draw = random.nextInt(total);
            for (int i = 0; i < remaining.size(); i++) {
                Candidate<T> candidate = remaining.get(i);
                draw -= candidate.weight();
                if (draw < 0) {
                    ordered.add(candidate.value());
                    remaining.remove(i);
                    break;
                }
            }
        }
        return List.copyOf(ordered);
    }
}
