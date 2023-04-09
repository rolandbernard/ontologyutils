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
    public OntologyRepairWeakening(final Predicate<Ontology> isRepaired) {
        super(isRepaired);
    }

    public static OntologyRepairRandomMcs forConsistency() {
        return new OntologyRepairRandomMcs(Ontology::isConsistent);
    }

    public static OntologyRepairRandomMcs forRemovingConsequence(final OWLAxiom axiom) {
        return new OntologyRepairRandomMcs(o -> o.isEntailed(axiom));
    }

    public static OntologyRepairRandomMcs forConceptSatisfiability(final OWLClassExpression concept) {
        return new OntologyRepairRandomMcs(o -> o.isSatisfiable(concept));
    }

    @Override
    public void repair(final Ontology ontology) {
        final Set<OWLAxiom> randomMcss = Utils.randomChoice(ontology.maximalConsistentSubsets(isRepaired));
        try (final Ontology refOntology = Ontology.withAxioms(randomMcss)) {
            try (final AxiomWeakener axiomWeakener = new AxiomWeakener(refOntology)) {
                while (!isRepaired(ontology)) {
                    final OWLAxiom badAxiom = Utils.randomChoice(findBadAxioms(ontology));
                    final OWLAxiom weakerAxiom = Utils.randomChoice(axiomWeakener.weakerAxioms(badAxiom));
                    ontology.replaceAxiom(badAxiom, weakerAxiom);
                }
            }
        }
    }

    /**
     * @param axioms
     * @return the set of axioms occurring in the least number of maximal consistent
     *         sets of {@code axioms}.
     */
    private Stream<OWLAxiom> findBadAxioms(final Ontology ontology) {
        final Map<OWLAxiom, Long> occurrences = ontology.optimalClassicalRepairs(isRepaired)
                .flatMap(set -> set.stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        final Optional<Long> max = occurrences.values().stream().max(Long::compareTo);
        if (max.isEmpty()) {
            throw new RuntimeException(
                    "Did not find a bad subclass or assertion axiom in " + ontology.axioms().toList());
        }
        return occurrences.entrySet().stream()
                .filter(entry -> entry.getValue() == max.get())
                .map(entry -> entry.getKey());
    }
}
