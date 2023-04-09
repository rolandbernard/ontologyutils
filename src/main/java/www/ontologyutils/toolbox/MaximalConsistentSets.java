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
     * @throws IllegalArgumentException
     *             If there is no maximal consistent subset.
     *             This is only possible if the ontology
     *             contains static axioms.
     */
    public MaximalConsistentSets(final Ontology ontology, final Predicate<Ontology> isRepaired)
            throws IllegalArgumentException {
        try (final Ontology nonRefutable = ontology.clone()) {
            nonRefutable.removeAxioms(nonRefutable.refutableAxioms().toList());
            if (!isRepaired.test(nonRefutable)) {
                throw new IllegalArgumentException("The ontology is not reparable.");
            }
        }
        this.ontology = ontology;
        this.isRepaired = isRepaired;
        axioms = ontology.refutableAxioms().toList();
        queue = new ArrayDeque<>();
        queue.add(new QueueItem(0, Set.of()));
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
     * @param axioms
     *            The axioms for which to compute the maximal consistent
     *            subsets.
     * @throws IllegalArgumentException
     *             If there is no maximal consistent subset.
     */
    public MaximalConsistentSets(final Set<OWLAxiom> axioms) throws IllegalArgumentException {
        this(Ontology.withAxioms(axioms));
    }

    /**
     * @param axioms
     *            The axioms for which to compute the maximal consistent
     *            subsets.
     * @param contained
     *            The axioms which must be contained in the subset.
     * @throws IllegalArgumentException
     *             If there is no maximal consistent subset.
     */
    public MaximalConsistentSets(final Set<OWLAxiom> axioms, final Set<OWLAxiom> contained)
            throws IllegalArgumentException {
        this(Ontology.withAxioms(contained, axioms));
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
    public static boolean isMaximallyConsistentWithRespectTo(final Ontology ontology, final Set<OWLAxiom> axioms) {
        try (final Ontology copy = ontology.clone()) {
            for (final OWLAxiom axiom : axioms) {
                copy.addAxioms(axiom);
                if (copy.isConsistent()) {
                    return false;
                }
                copy.removeAxioms(axiom);
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
    public static boolean isMaximallyConsistentSubset(final Set<OWLAxiom> subset, final Set<OWLAxiom> set) {
        return set.containsAll(subset) && isMaximallyConsistentWithRespectTo(Ontology.withAxioms(subset), set);
    }

    /**
     * Compute the next result.
     *
     * @return True if a new result was found, false otherwise.
     */
    private boolean computeNextResult() {
        while (!queue.isEmpty()) {
            final QueueItem current = queue.pop();
            if (results.containsSubset(current.removed)) {
                continue;
            } else {
                try (Ontology subset = ontology.clone()) {
                    subset.removeAxioms(current.removed);
                    if (isRepaired.test(subset)) {
                        results.add(current.removed);
                        result = current.removed;
                        return true;
                    } else {
                        subset.removeAxioms(axioms.stream().skip(current.k));
                        for (int i = current.k; i < axioms.size(); i++) {
                            final Set<OWLAxiom> removed = new HashSet<>(current.removed);
                            final OWLAxiom axiom = axioms.get(i);
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
                        final Set<OWLAxiom> next = result;
                        result = null;
                        return next;
                    }
                }, Spliterator.NONNULL), false));
    }
}
