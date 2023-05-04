package www.ontologyutils.toolbox;

import java.util.*;
import java.util.function.BiPredicate;

/**
 * Implements a simple cache for a preorder, i.e., a reflexive and transitive
 * relation.
 */
public class PreorderCache<T> {
    private Map<T, Map<T, Boolean>> successors;
    private Map<T, Map<T, Boolean>> predecessors;

    /**
     * Create a new empty cache.
     */
    public PreorderCache() {
        successors = new HashMap<>();
        predecessors = new HashMap<>();
    }

    private Map<T, Boolean> getSuccessorMap(T pred) {
        return successors.computeIfAbsent(pred, arg -> new HashMap<>(Map.of(arg, true)));
    }

    private Map<T, Boolean> getPredecessorMap(T succ) {
        return predecessors.computeIfAbsent(succ, arg -> new HashMap<>(Map.of(arg, true)));
    }

    private void cacheImmediateObservation(T pred, T succ, boolean connected) {
        getSuccessorMap(pred).put(succ, connected);
        getPredecessorMap(succ).put(pred, connected);
    }

    private void cachePositiveObservation(T pred, T succ) {
        cacheImmediateObservation(pred, succ, true);
        for (var entry1 : Set.copyOf(getPredecessorMap(pred).entrySet())) {
            if (entry1.getValue()) {
                for (var entry2 : Set.copyOf(getSuccessorMap(succ).entrySet())) {
                    if (entry2.getValue()) {
                        cacheImmediateObservation(entry1.getKey(), entry2.getKey(), true);
                    }
                }
            }
        }
        for (var entry : Set.copyOf(getSuccessorMap(pred).entrySet())) {
            if (!entry.getValue()) {
                cacheImmediateObservation(succ, entry.getKey(), false);
            }
        }
    }

    private void cacheNegativeObservation(T pred, T succ) {
        cacheImmediateObservation(pred, succ, false);
        for (var entry1 : Set.copyOf(getSuccessorMap(pred).entrySet())) {
            if (entry1.getValue()) {
                for (var entry2 : Set.copyOf(getPredecessorMap(succ).entrySet())) {
                    if (entry2.getValue()) {
                        cacheImmediateObservation(entry1.getKey(), entry2.getKey(), false);
                    }
                }
            }
        }
    }

    /**
     * Get whether the relation contains the pair ({@code pred}, {@code succ}). If
     * the result is already known form the cached values it is returned
     * immediately, other wise {@code order} is called to find the result.
     *
     * @param pred
     * @param succ
     * @param order
     *            A function defining the relation to cache.
     * @return True iff the relation contains a connection from {@code pred} to
     *         {@code succ}.
     */
    public boolean computeIfAbsent(T pred, T succ, BiPredicate<T, T> order) {
        var predSucc = getSuccessorMap(pred);
        if (predSucc.containsKey(succ)) {
            return predSucc.get(succ);
        } else if (order.test(pred, succ)) {
            cachePositiveObservation(pred, succ);
            return true;
        } else {
            cacheNegativeObservation(pred, succ);
            return false;
        }
    }

    /**
     * Wrap the given preorder {@code preorder} using a {@code PreorderCache}.
     *
     * @param <K>
     * @param <V>
     * @param preorder
     * @return The wrapped preorder.
     */
    public static <T> BiPredicate<T, T> wrapPreorder(BiPredicate<T, T> preorder) {
        var cache = new PreorderCache<T>();
        return (pred, succ) -> {
            return cache.computeIfAbsent(pred, succ, preorder);
        };
    }
}
