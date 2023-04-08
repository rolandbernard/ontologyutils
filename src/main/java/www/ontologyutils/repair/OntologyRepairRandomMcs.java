package www.ontologyutils.repair;

import java.util.Set;
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
        final Set<OWLAxiom> toRemove = Utils.randomChoice(ontology.optimalClassicalRepairs(isRepaired));
        ontology.removeAxioms(toRemove.stream());
    }
}
