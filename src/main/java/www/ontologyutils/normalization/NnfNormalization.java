package www.ontologyutils.normalization;

import www.ontologyutils.toolbox.*;

public class NnfNormalization implements OntologyModification {
    @Override
    public void apply(final Ontology ontology) {
        ontology.axioms().forEach(axiom -> {
            ontology.replaceAxiom(axiom, axiom.getNNF());
        });
    }
}
