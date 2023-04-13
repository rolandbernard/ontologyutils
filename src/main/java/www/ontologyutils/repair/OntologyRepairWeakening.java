package www.ontologyutils.repair;

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
    public OntologyRepairWeakening(final Predicate<Ontology> isRepaired) {
        super(isRepaired);
    }

    /**
     * @return An instance of {@code OntologyRepairRandomMcs} that tries to make the
     *         ontology consistent.
     */
    public static OntologyRepairRandomMcs forConsistency() {
        return new OntologyRepairRandomMcs(Ontology::isConsistent);
    }

    /**
     * @return An instance of {@code OntologyRepairRandomMcs} that tries to remove
     *         {@code axiom} from the set of consequences of the ontology.
     */
    public static OntologyRepairRandomMcs forRemovingConsequence(final OWLAxiom axiom) {
        return new OntologyRepairRandomMcs(o -> o.isEntailed(axiom));
    }

    /**
     * @return An instance of {@code OntologyRepairRandomMcs} that tries to make
     *         {@code concept} satisfiable.
     */
    public static OntologyRepairRandomMcs forConceptSatisfiability(final OWLClassExpression concept) {
        return new OntologyRepairRandomMcs(o -> o.isSatisfiable(concept));
    }

    /**
     * @param axioms
     * @return the set of axioms occurring in the least number of maximal consistent
     *         sets of {@code axioms}.
     */
    private Stream<OWLAxiom> findBadAxioms(final Ontology ontology) {
        final var occurrences = ontology.optimalClassicalRepairs(isRepaired)
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

    @Override
    public void repair(final Ontology ontology) {
        final var randomMcss = Utils.randomChoice(ontology.maximalConsistentSubsets(isRepaired));
        try (final var refOntology = Ontology.withAxioms(randomMcss)) {
            try (final var axiomWeakener = new AxiomWeakener(refOntology)) {
                while (!isRepaired(ontology)) {
                    final var badAxiom = Utils.randomChoice(findBadAxioms(ontology));
                    final var weakerAxiom = Utils.randomChoice(axiomWeakener.weakerAxioms(badAxiom));
                    ontology.replaceAxiom(badAxiom, weakerAxiom);
                }
            }
        }
    }
}
