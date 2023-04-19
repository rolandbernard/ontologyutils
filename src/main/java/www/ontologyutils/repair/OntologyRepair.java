package www.ontologyutils.repair;

import java.util.Collection;
import java.util.function.Predicate;

import org.semanticweb.owlapi.model.AxiomType;

import www.ontologyutils.toolbox.*;

/**
 * An ontology is repaired if {@code isRepaired} returns true. The predicate
 * must be such that if an ontology satisfies the predicate, every ontology with
 * fewer consequences will also satisfy the predicate.
 */
public abstract class OntologyRepair implements OntologyModification {
    protected final Predicate<Ontology> isRepaired;

    /**
     * @param isRepaired
     *            The condition by which the repair is evaluated.
     */
    protected OntologyRepair(final Predicate<Ontology> isRepaired) {
        this.isRepaired = isRepaired;
    }

    public abstract void repair(Ontology ontology);

    public abstract Collection<AxiomType<?>> getReparableAxiomTypes();

    /**
     * Simple utility function applying the internal predicate.
     *
     * @param ontology
     *            The ontology to check.
     * @return True iff the ontology is repaired.
     */
    public boolean isRepaired(final Ontology ontology) {
        return isRepaired.test(ontology);
    }

    @Override
    public void apply(final Ontology ontology) throws IllegalArgumentException {
        try (final var nonRefutable = ontology.clone()) {
            nonRefutable.removeAxioms(ontology.refutableAxioms(getReparableAxiomTypes()));
            if (!isRepaired(nonRefutable)) {
                throw new IllegalArgumentException("The ontology is not reparable.");
            }
        }
        repair(ontology);
    }
}
