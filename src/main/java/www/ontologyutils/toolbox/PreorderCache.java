package www.ontologyutils.toolbox;

import java.util.*;
import java.util.function.BiPredicate;

/**
 * Implements a simple cache for a preorder, i.e., a reflexive and transitive
 * relation.
 */
public class PreorderCache<T> {
    private static record Tuple<T>(T first, T second) {
    }

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
        var newPositive = new ArrayList<Tuple<T>>();
        getPredecessorMap(pred).forEach((key1, value1) -> {
            if (value1) {
                getSuccessorMap(succ).forEach((key2, value2) -> {
                    if (value2) {
                        newPositive.add(new Tuple<>(key1, key2));
                    }
                });
            }
        });
        for (var tuple : newPositive) {
            cacheImmediateObservation(tuple.first, tuple.second, true);
        }
        var newNegative = new ArrayList<Tuple<T>>();
        getSuccessorMap(pred).forEach((key1, value1) -> {
            if (!value1) {
                getSuccessorMap(succ).forEach((key2, value2) -> {
                    if (value2) {
                        newNegative.add(new Tuple<>(key2, key1));
                    }
                });
            }
        });
        for (var tuple : newNegative) {
            cacheImmediateObservation(tuple.first, tuple.second, false);
        }
    }

    private void cacheNegativeObservation(T pred, T succ) {
        cacheImmediateObservation(pred, succ, false);
        var newNegative = new ArrayList<Tuple<T>>();
        getSuccessorMap(pred).forEach((key1, value1) -> {
            if (value1) {
                getPredecessorMap(succ).forEach((key2, value2) -> {
                    if (value2) {
                        newNegative.add(new Tuple<>(key1, key2));
                    }
                });
            }
        });
        for (var tuple : newNegative) {
            cacheImmediateObservation(tuple.first, tuple.second, false);
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
