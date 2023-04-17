package www.ontologyutils.repair;

import java.util.Set;
import java.util.function.*;
import java.util.stream.*;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.refinement.AxiomWeakener;
import www.ontologyutils.toolbox.*;

/**
 * An implementation of {@code OntologyRepair} following closely (but not
 * strictly) the axiom weakening approach described in Nicolas Troquard, Roberto
 * Confalonieri, Pietro Galliani, Rafael Peñaloza, Daniele Porello, Oliver Kutz:
 * "Repairing Ontologies via Axiom Weakening", AAAI 2018.
 *
 * The ontology passed in parameter of {@code repair} should only contain
 * assertion or subclass axioms.
 */
public class OntologyRepairWeakening extends OntologyRepair {
    public static enum RefOntologyStrategy {
        RANDOM_MCS, SOME_MCS, LARGEST_MCS, INTERSECTION_OF_MCS,
    }

    public static enum BadAxiomStrategy {
        RANDOM, NOT_IN_SOME_MCS, NOT_IN_LARGEST_MCS, IN_LEAST_MCS,
    }

    private final RefOntologyStrategy refOntologySource;
    private final BadAxiomStrategy badAxiomSource;

    public OntologyRepairWeakening(final Predicate<Ontology> isRepaired, final RefOntologyStrategy refOntologySource,
            final BadAxiomStrategy badAxiomSource) {
        super(isRepaired);
        this.refOntologySource = refOntologySource;
        this.badAxiomSource = badAxiomSource;
    }

    public OntologyRepairWeakening(final Predicate<Ontology> isRepaired) {
        this(isRepaired, RefOntologyStrategy.RANDOM_MCS, BadAxiomStrategy.IN_LEAST_MCS);
    }

    /**
     * @return An instance of {@code OntologyRepairRandomMcs} that tries to make the
     *         ontology consistent.
     */
    public static OntologyRepair forConsistency() {
        return new OntologyRepairWeakening(Ontology::isConsistent);
    }

    /**
     * @return An instance of {@code OntologyRepairRandomMcs} that tries to remove
     *         {@code axiom} from the set of consequences of the ontology.
     */
    public static OntologyRepair forRemovingConsequence(final OWLAxiom axiom) {
        return new OntologyRepairWeakening(o -> o.isEntailed(axiom));
    }

    /**
     * @return An instance of {@code OntologyRepairRandomMcs} that tries to make
     *         {@code concept} satisfiable.
     */
    public static OntologyRepair forConceptSatisfiability(final OWLClassExpression concept) {
        return new OntologyRepairWeakening(o -> o.isSatisfiable(concept));
    }

    /**
     * @param ontology
     * @return The set of axioms to include in the reference ontology to use for
     *         repairs.
     */
    private Set<OWLAxiom> getRefAxioms(final Ontology ontology) {
        final var mcss = ontology.maximalConsistentSubsets(isRepaired);
        switch (refOntologySource) {
            case INTERSECTION_OF_MCS:
                return mcss.reduce((a, b) -> {
                    a.removeIf(axiom -> !b.contains(axiom));
                    return a;
                }).get();
            case LARGEST_MCS:
                return mcss.findFirst().get();
            case RANDOM_MCS:
                return Utils.randomChoice(mcss);
            case SOME_MCS:
                return mcss.findAny().get();
            default:
                throw new IllegalArgumentException("Unimplemented reference ontology choice algorithm.");
        }
    }

    /**
     * @param axioms
     * @return The stream of axioms between which to select the next axiom to
     *         weaken.
     */
    private Stream<OWLAxiom> findBadAxioms(final Ontology ontology) {
        switch (badAxiomSource) {
            case IN_LEAST_MCS: {
                final var occurrences = ontology.optimalClassicalRepairs(isRepaired)
                        .flatMap(set -> set.stream()
                                .filter(axiom -> axiom.isOfType(AxiomType.SUBCLASS_OF, AxiomType.CLASS_ASSERTION)))
                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
                final var max = occurrences.values().stream().max(Long::compareTo);
                if (max.isEmpty()) {
                    throw new RuntimeException(
                            "Did not find a bad subclass or assertion axiom in " + ontology.axioms().toList());
                }
                return occurrences.entrySet().stream()
                        .filter(entry -> entry.getValue() == max.get())
                        .map(entry -> entry.getKey());
            }
            case NOT_IN_LARGEST_MCS:
                return ontology.optimalClassicalRepairs(isRepaired).findFirst().get().stream();
            case NOT_IN_SOME_MCS:
                return ontology.optimalClassicalRepairs(isRepaired).findAny().get().stream();
            case RANDOM:
                return ontology.axioms();
            default:
                throw new IllegalArgumentException("Unimplemented bad axiom choice algorithm.");
        }
    }

    @Override
    public void repair(final Ontology ontology) {
        final var refAxioms = getRefAxioms(ontology);
        try (final var refOntology = Ontology.withAxioms(refAxioms)) {
            try (final var axiomWeakener = new AxiomWeakener(refOntology)) {
                while (!isRepaired(ontology)) {
                    final var badAxioms = findBadAxioms(ontology)
                            .filter(axiom -> axiom.isOfType(AxiomType.SUBCLASS_OF, AxiomType.CLASS_ASSERTION));
                    final var badAxiom = Utils.randomChoice(badAxioms);
                    final var weakerAxiom = Utils.randomChoice(axiomWeakener.weakerAxioms(badAxiom));
                    ontology.replaceAxiom(badAxiom, weakerAxiom);
                }
            }
        }
    }
}
