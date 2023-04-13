package www.ontologyutils.normalization;

import www.ontologyutils.toolbox.*;

/**
 * Normalization that converts all concepts to negation normal form.
 */
public class NnfNormalization implements OntologyModification {
    @Override
    public void apply(final Ontology ontology) {
        final var axioms = ontology.axioms().toList();
        for (final var axiom : axioms) {
            ontology.replaceAxiom(axiom, axiom.getNNF());
        }
    }
}
