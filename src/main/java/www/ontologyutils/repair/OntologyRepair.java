package www.ontologyutils.repair;

import java.util.function.Predicate;

import www.ontologyutils.toolbox.*;

public abstract class OntologyRepair implements OntologyModification {
    protected final Predicate<Ontology> isRepaired;

    protected OntologyRepair(final Predicate<Ontology> isRepaired) {
        this.isRepaired = isRepaired;
    }

    public abstract void repair(Ontology ontology);

    public boolean isRepaired(final Ontology ontology) {
        return isRepaired.test(ontology);
    }

    @Override
    public void apply(final Ontology ontology) throws IllegalArgumentException {
        try (final Ontology nonRefutable = ontology.clone()) {
            nonRefutable.removeAxioms(nonRefutable.refutableAxioms().toList());
            if (!isRepaired(nonRefutable)) {
                throw new IllegalArgumentException("The ontology is not reparable.");
            }
        }
        repair(ontology);
    }
}
