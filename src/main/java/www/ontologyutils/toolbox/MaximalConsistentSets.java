package www.ontologyutils.toolbox;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.*;

import org.semanticweb.owlapi.model.*;

/**
 * This an adaptation of the algorithm in Robert Malouf's "Maximal Consistent
 * Subsets", Computational Linguistics, vol 33(2), p.153-160, 2007.
 */
public class MaximalConsistentSets {
    private static record QueueItem(int k, Set<OWLAxiom> removed) {
    }

    private final Predicate<Ontology> isRepaired;
    private final Ontology ontology;
    private final List<OWLAxiom> axioms;
    private final Deque<QueueItem> queue;
    private final SetOfSets<OWLAxiom> results;
    private Set<OWLAxiom> result;

    /**
     * @param ontology
     *            The ontology for which to compute the maximal consistent
     *            subsets.
     * @param isRepaired
     *            The predicate with which to measure "consistency".
     */
    public MaximalConsistentSets(final Ontology ontology, final Predicate<Ontology> isRepaired) {
        this.ontology = ontology;
        this.isRepaired = isRepaired;
        axioms = ontology.refutableAxioms().toList();
        queue = new ArrayDeque<>();
        queue.add(new QueueItem(0, new HashSet<>()));
        results = new SetOfSets<>();
    }

    /**
     * @param ontology
     *            The ontology for which to compute the maximal consistent
     *            subsets.
     * @throws IllegalArgumentException
     *             If there is no maximal consistent subset.
     */
    public MaximalConsistentSets(final Ontology ontology) throws IllegalArgumentException {
        this(ontology, Ontology::isConsistent);
    }

    /**
     * Tests whether adding any of the axioms in {@code axioms} to {@code ontology}
     * make the ontology inconsistent.
     *
     * @param ontology
     * @param axioms
     * @return True iff {@code ontology} is maximally consistent with respect to
     *         {@code axioms}.
     */
    public static boolean isMaximallyConsistentWithRespectTo(final Ontology ontology,
            final Collection<? extends OWLAxiom> axioms) {
        if (!ontology.isConsistent()) {
            return false;
        }
        final var contained = ontology.axioms().collect(Collectors.toSet());
        try (final var copy = ontology.clone()) {
            for (final var axiom : axioms) {
                if (!contained.contains(axiom)) {
                    copy.addAxioms(axiom);
                    if (copy.isConsistent()) {
                        return false;
                    }
                    copy.removeAxioms(axiom);
                }
            }
            return true;
        }
    }

    /**
     * @param subset
     * @param set
     * @return True iff {@code subset} is a maximal consistent subset of
     *         {@code set}.
     */
    public static boolean isMaximallyConsistentSubset(final Collection<? extends OWLAxiom> subset,
            final Collection<? extends OWLAxiom> set) {
        try (final var ontology = Ontology.withAxioms(subset)) {
            return set.containsAll(subset) && isMaximallyConsistentWithRespectTo(ontology, set);
        }
    }

    /**
     * @param axioms
     *            A set of axioms
     * @param contained
     *            A set of axioms that must be contained by the returned
     *            maximal consistent sets.
     * @return A stream of maximal consistent subsets of axioms from {@code axioms}
     *         containing {@code contained}.
     */
    public static Stream<Set<OWLAxiom>> maximalConsistentSubsetsNaive(final Collection<? extends OWLAxiom> axioms,
            final Collection<? extends OWLAxiom> contained) {
        return Utils.<OWLAxiom>powerSet(axioms).filter(
                candidate -> candidate.containsAll(contained) && isMaximallyConsistentSubset(candidate, axioms));
    }

    /**
     * @param axioms
     *            A set of axioms
     * @param howMany
     *            the maximal number of maximal consistent subsets to be
     *            returned
     * @param contained
     *            A set of axioms that must be contained by the returned
     *            maximal consistent sets.
     * @return A set of at most {@code howMany} maximal consistent subsets of axioms
     *         from {@code axioms} containing {@code contained}.
     */
    public static Set<Set<OWLAxiom>> maximalConsistentSubsets(final Collection<? extends OWLAxiom> axioms,
            final int howMany, final Collection<? extends OWLAxiom> contained) {
        try (final var ontology = Ontology.withAxioms(contained, axioms)) {
            return ontology.maximalConsistentSubsets().limit(howMany).collect(Collectors.toSet());
        }
    }

    /**
     * @param axioms
     *            A set of axioms
     * @param contained
     *            A set of axioms that must be contained by the returned
     *            maximal consistent sets.
     * @return The set of all maximal consistent subsets of axioms from
     *         {@code axioms} containing {@code contained}.
     */
    public static Set<Set<OWLAxiom>> maximalConsistentSubsets(final Collection<? extends OWLAxiom> axioms,
            final Collection<? extends OWLAxiom> contained) {
        return maximalConsistentSubsets(axioms, Integer.MAX_VALUE, contained);
    }

    /**
     * Compute the next result.
     *
     * @return True if a new result was found, false otherwise.
     */
    private boolean computeNextResult() {
        while (!queue.isEmpty()) {
            final var current = queue.pop();
            if (results.containsSubset(current.removed)) {
                continue;
            } else {
                try (final var subset = ontology.clone()) {
                    subset.removeAxioms(current.removed);
                    if (isRepaired.test(subset)) {
                        results.add(current.removed);
                        result = current.removed;
                        return true;
                    } else {
                        subset.removeAxioms(axioms.stream().skip(current.k));
                        for (int i = current.k; i < axioms.size(); i++) {
                            final var removed = new HashSet<>(current.removed);
                            final var axiom = axioms.get(i);
                            removed.add(axiom);
                            queue.add(new QueueItem(i + 1, removed));
                            subset.addAxioms(axiom);
                            if (i < axioms.size() - 1 && !isRepaired.test(subset)) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * @param remove
     * @return The axioms of the ontology without those in {@code remove}.
     */
    private Set<OWLAxiom> complement(final Set<OWLAxiom> remove) {
        return ontology.axioms().filter(axiom -> !remove.contains(axiom)).collect(Collectors.toSet());
    }

    /**
     * @return A stream producing all maximal consistent sets.
     */
    public Stream<Set<OWLAxiom>> stream() {
        return repairsStream().map(this::complement);
    }

    /**
     * @return A stream producing all complements of maximal consistent sets.
     */
    public Stream<Set<OWLAxiom>> repairsStream() {
        return Stream.concat(results.stream(),
                StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<Set<OWLAxiom>>() {
                    public boolean hasNext() {
                        if (result == null) {
                            computeNextResult();
                        }
                        return result != null;
                    }

                    @Override
                    public Set<OWLAxiom> next() {
                        if (result == null) {
                            computeNextResult();
                        }
                        final var next = result;
                        result = null;
                        return next;
                    }
                }, Spliterator.NONNULL), false));
    }
}
