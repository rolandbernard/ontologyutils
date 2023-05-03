package www.ontologyutils.toolbox;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.semanticweb.owlapi.model.*;

import org.junit.jupiter.api.Test;

public class OntologyTest {
    private OWLDataFactory df;
    private List<OWLClassExpression> concepts;

    public OntologyTest() {
        df = Ontology.getDefaultDataFactory();
        concepts = List.of(
                df.getOWLClass("www.first.org#", "A"),
                df.getOWLClass("www.second.org#", "A"),
                df.getOWLClass("www.third.org#", "A"),
                df.getOWLClass("www.fourth.org#", "A"),
                df.getOWLClass("www.first.org#", "A"));
    }

    @Test
    public void inferredTaxonomyAxioms() {
        var df = Ontology.getDefaultDataFactory();
        var ax1 = df.getOWLSubClassOfAxiom(concepts.get(0), concepts.get(1));
        var ax2 = df.getOWLSubClassOfAxiom(concepts.get(1), concepts.get(2));
        var ax3 = df.getOWLSubClassOfAxiom(concepts.get(1), concepts.get(0));
        var axioms = new HashSet<OWLAxiom>();
        axioms.add(ax1);
        axioms.add(ax2);
        try (var ontology = Ontology.withAxioms(axioms)) {
            long infSize = ontology.inferredTaxonomyAxioms().count();
            assertEquals(6, infSize);
        }
        axioms.add(ax3);
        try (var ontology = Ontology.withAxioms(axioms)) {
            long infSize = ontology.inferredTaxonomyAxioms().count();
            assertEquals(7, infSize);
        }
    }
}
