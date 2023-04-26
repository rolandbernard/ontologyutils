package www.ontologyutils.repair;

import java.util.*;
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
        RANDOM_MCS, SOME_MCS, ONE_MCS, LARGEST_MCS, INTERSECTION_OF_MCS,
    }

    public static enum BadAxiomStrategy {
        RANDOM, NOT_IN_SOME_MCS, NOT_IN_LARGEST_MCS, IN_LEAST_MCS, IN_SOME_MUS, IN_ONE_MUS, NOT_IN_ONE_MCS
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
        this(isRepaired, RefOntologyStrategy.SOME_MCS, BadAxiomStrategy.IN_SOME_MUS);
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
     *         all {@code axioms} from being entailed by the ontology.
     */
    public static OntologyRepair forRemovingEntailments(final Collection<? extends OWLAxiom> axioms) {
        return new OntologyRepairWeakening(o -> axioms.stream().allMatch(axiom -> !o.isEntailed(axiom)));
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
    public Set<OWLAxiom> getRefAxioms(final Ontology ontology) {
        switch (refOntologySource) {
            case INTERSECTION_OF_MCS: {
                return ontology.maximalConsistentSubsets(isRepaired).reduce((a, b) -> {
                    a.removeIf(axiom -> !b.contains(axiom));
                    return a;
                }).get();
            }
            case LARGEST_MCS:
                return Utils.randomChoice(ontology.largestMaximalConsistentSubsets(isRepaired));
            case RANDOM_MCS:
                return Utils.randomChoice(ontology.maximalConsistentSubsets(isRepaired));
            case SOME_MCS:
                return Utils.randomChoice(ontology.someMaximalConsistentSubsets(isRepaired));
            case ONE_MCS:
                return ontology.maximalConsistentSubset(isRepaired);
            default:
                throw new IllegalArgumentException("Unimplemented reference ontology choice strategy.");
        }
    }

    /**
     * @param ontology
     * @return The stream of axioms between which to select the next axiom to
     *         weaken.
     */
    public Stream<OWLAxiom> findBadAxioms(final Ontology ontology) {
        switch (badAxiomSource) {
            case IN_LEAST_MCS: {
                final var occurrences = ontology.minimalCorrectionSubsets(isRepaired)
                        .flatMap(set -> set.stream())
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
            case NOT_IN_LARGEST_MCS: {
                return ontology.smallestMinimalCorrectionSubsets(isRepaired).flatMap(axioms -> axioms.stream());
            }
            case NOT_IN_SOME_MCS:
                return ontology.someMinimalCorrectionSubsets(isRepaired).flatMap(mcs -> mcs.stream());
            case IN_SOME_MUS:
                return ontology.someMinimalUnsatisfiableSubsets(isRepaired).flatMap(mus -> mus.stream());
            case IN_ONE_MUS:
                return ontology.minimalUnsatisfiableSubset(isRepaired).stream();
            case NOT_IN_ONE_MCS:
                return ontology.minimalCorrectionSubset(isRepaired).stream();
            case RANDOM:
                return ontology.refutableAxioms();
            default:
                throw new IllegalArgumentException("Unimplemented bad axiom choice strategy.");
        }
    }

    @Override
    public void repair(final Ontology ontology) {
        try (final var copy = ontology.clone()) {
            // Make all non-reparable axioms static to ensure that they are in the reference
            // ontology.
            copy.addStaticAxioms(ontology.refutableAxioms()
                    .filter(axiom -> !axiom.isOfType(getReparableAxiomTypes())));
            final var refAxioms = getRefAxioms(copy);
            try (final var refOntology = Ontology.withAxioms(refAxioms)) {
                try (final var axiomWeakener = new AxiomWeakener(refOntology, copy)) {
                    while (!isRepaired(copy)) {
                        final var badAxioms = findBadAxioms(copy);
                        final var badAxiom = Utils.randomChoice(badAxioms);
                        final var weakerAxiom = Utils.randomChoice(axiomWeakener.weakerAxioms(badAxiom));
                        ontology.replaceAxiom(badAxiom, weakerAxiom);
                        copy.replaceAxiom(badAxiom, weakerAxiom);
                    }
                }
            }
        }
    }

    @Override
    public Collection<AxiomType<?>> getReparableAxiomTypes() {
        return AxiomWeakener.SUPPORTED_AXIOM_TYPES;
    }
}
