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

    public MaximalConsistentSets(final Ontology ontology, final Predicate<Ontology> isRepaired) throws IllegalArgumentException {
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

    public MaximalConsistentSets(final Ontology ontology) {
        this(ontology, Ontology::isConsistent);
    }

    public MaximalConsistentSets(final Set<OWLAxiom> axioms) {
        this(Ontology.withAxioms(axioms));
    }

    public MaximalConsistentSets(final Set<OWLAxiom> axioms, final Set<OWLAxiom> contained) {
        this(Ontology.withAxioms(contained, axioms));
    }

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

    public static boolean isMaximallyConsistentSubset(final Set<OWLAxiom> subset, final Set<OWLAxiom> set) {
        return set.containsAll(subset) && isMaximallyConsistentWithRespectTo(Ontology.withAxioms(subset), set);
    }

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

    private Set<OWLAxiom> complement(final Set<OWLAxiom> remove) {
        return ontology.axioms().filter(axiom -> !remove.contains(axiom)).collect(Collectors.toSet());
    }

    public Stream<Set<OWLAxiom>> stream() {
        return repairsStream().map(this::complement);
    }

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
