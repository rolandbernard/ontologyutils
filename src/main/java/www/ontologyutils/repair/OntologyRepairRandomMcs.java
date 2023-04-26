package www.ontologyutils.repair;

import java.util.Collection;
import java.util.function.Predicate;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.*;

/**
 * A simple implementation of {@code OntologyRepair}. It repairs an inconsistent
 * ontology into an ontology made of a randomly chosen maximally consistent set
 * of axioms in the input ontology.
 */
public class OntologyRepairRandomMcs extends OntologyRepair {
    public OntologyRepairRandomMcs(final Predicate<Ontology> isRepaired) {
        super(isRepaired);
    }

    /**
     * @return An instance of {@code OntologyRepairRandomMcs} that tries to make the
     *         ontology consistent.
     */
    public static OntologyRepair forConsistency() {
        return new OntologyRepairRandomMcs(Ontology::isConsistent);
    }

    /**
     * @return An instance of {@code OntologyRepairRandomMcs} that tries to remove
     *         all {@code axioms} from being entailed by the ontology.
     */
    public static OntologyRepair forRemovingEntailments(final Collection<? extends OWLAxiom> axioms) {
        return new OntologyRepairRandomMcs(o -> axioms.stream().allMatch(axiom -> !o.isEntailed(axiom)));
    }

    /**
     * @return An instance of {@code OntologyRepairRandomMcs} that tries to make
     *         {@code concept} satisfiable.
     */
    public static OntologyRepair forConceptSatisfiability(final OWLClassExpression concept) {
        return new OntologyRepairRandomMcs(o -> o.isSatisfiable(concept));
    }

    @Override
    public void repair(final Ontology ontology) {
        final var toRemove = Utils.randomChoice(ontology.minimalCorrectionSubsets(isRepaired));
        ontology.removeAxioms(toRemove);
    }

    @Override
    public Collection<AxiomType<?>> getReparableAxiomTypes() {
        return AxiomType.LOGICAL_AXIOM_TYPES;
    }
}
