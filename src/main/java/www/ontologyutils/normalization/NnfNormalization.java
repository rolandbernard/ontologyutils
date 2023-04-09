package www.ontologyutils.normalization;

import java.util.List;

import org.semanticweb.owlapi.model.OWLAxiom;

import www.ontologyutils.toolbox.*;

public class NnfNormalization implements OntologyModification {
    @Override
    public void apply(final Ontology ontology) {
        final List<OWLAxiom> axioms = ontology.axioms().toList();
        for (final OWLAxiom axiom : axioms) {
            ontology.replaceAxiom(axiom, axiom.getNNF());
        }
    }
}
