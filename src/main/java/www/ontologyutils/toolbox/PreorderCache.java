package www.ontologyutils.toolbox;

import java.util.*;
import java.util.function.BiPredicate;

/**
 * Implements a simple cache for a preorder, i.e., a reflexive and transitive
 * relation.
 *
 * Loosely inspired by the approach presented in Shearer, R., & Horrocks, I.
 * (2009). Exploiting partial information in taxonomy construction. In The
 * Semantic Web-ISWC 2009: 8th International Semantic Web Conference, ISWC 2009,
 * Chantilly, VA, USA, October 25-29, 2009. Proceedings 8 (pp. 569-584).
 * Springer Berlin Heidelberg.
 */
public class PreorderCache<T> {
    private Map<T, Set<T>> knownSuccessors;
    private Map<T, Set<T>> knownPredecessors;
    private Map<T, Set<T>> possibleSuccessors;
    private Map<T, Set<T>> possiblePredecessors;

    /**
     * Create a new empty cache.
     */
    public PreorderCache() {
        knownSuccessors = new HashMap<>();
        knownPredecessors = new HashMap<>();
        possibleSuccessors = new HashMap<>();
        possiblePredecessors = new HashMap<>();
    }

    private void assureExistence(T elem) {
        if (!knownSuccessors.containsKey(elem)) {
            var existing = new HashSet<>(knownSuccessors.keySet());
            knownSuccessors.put(elem, new HashSet<>(Set.of(elem)));
            knownPredecessors.put(elem, new HashSet<>(Set.of(elem)));
            for (var other : existing) {
                possiblePredecessors.get(other).add(elem);
                possibleSuccessors.get(other).add(elem);
            }
            possibleSuccessors.put(elem, new HashSet<>(existing));
            possiblePredecessors.put(elem, existing);
        }
    }

    private void removePossibleSuccessors(T pred, T succ) {
        if (possibleSuccessors.get(pred).remove(succ)) {
            possiblePredecessors.get(succ).remove(pred);
            for (var pred2 : Utils.toArray(knownSuccessors.get(pred))) {
                for (var succ2 : Utils.toArray(knownPredecessors.get(succ))) {
                    removePossibleSuccessors(pred2, succ2);
                }
            }
        }
    }

    private void addKnownSuccessors(T pred, T succ) {
        if (knownSuccessors.get(pred).add(succ)) {
            knownPredecessors.get(succ).add(pred);
            possibleSuccessors.get(pred).remove(succ);
            possiblePredecessors.get(succ).remove(pred);
            for (var pred2 : Utils.toArray(knownPredecessors.get(pred))) {
                for (var succ2 : Utils.toArray(knownSuccessors.get(succ))) {
                    addKnownSuccessors(pred2, succ2);
                }
            }
            for (var succ2 : Utils.toArray(possibleSuccessors.get(succ))) {
                for (var succ3 : Utils.toArray(knownSuccessors.get(succ2))) {
                    if (!possibleSuccessors.get(pred).contains(succ3) && !knownSuccessors.get(pred).contains(succ3)) {
                        removePossibleSuccessors(succ, succ2);
                        break;
                    }
                }
            }
            for (var pred2 : Utils.toArray(possiblePredecessors.get(pred))) {
                for (var pred3 : Utils.toArray(knownPredecessors.get(pred2))) {
                    if (!possibleSuccessors.get(pred3).contains(succ) && !knownSuccessors.get(pred3).contains(succ)) {
                        removePossibleSuccessors(pred2, pred);
                        break;
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
        assureExistence(pred);
        assureExistence(succ);
        if (knownSuccessors.get(pred).contains(succ)) {
            return true;
        } else if (!possibleSuccessors.get(pred).contains(succ)) {
            return false;
        } else if (order.test(pred, succ)) {
            addKnownSuccessors(pred, succ);
            return true;
        } else {
            removePossibleSuccessors(pred, succ);
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
