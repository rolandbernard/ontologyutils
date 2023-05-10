package www.ontologyutils.normalization;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.Ontology;

public class TBoxNormalizationTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "/catsandnumbers.owl", "/bodysystem.owl", "/bfo.owl", "/apo.owl", "/aeo.owl", "/duo.owl",
    })
    public void allTBoxAxiomsAreSubclassOf(String resourceName) throws OWLOntologyCreationException {
        var path = TBoxNormalizationTest.class.getResource(resourceName).getFile();
        try (var ontology = Ontology.loadOntology(path)) {
            var normalization = new TBoxNormalization();
            normalization.apply(ontology);
            ontology.tboxAxioms()
                    .forEach(axiom -> assertEquals(AxiomType.SUBCLASS_OF, axiom.getAxiomType()));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/catsandnumbers.owl", "/bodysystem.owl", "/bfo.owl", "/apo.owl", "/aeo.owl", "/duo.owl",
    })
    public void normalizedOntologyIsEquivalent(String resourceName) throws OWLOntologyCreationException {
        var path = TBoxNormalizationTest.class.getResource(resourceName).getFile();
        try (var originalOntology = Ontology.loadOntology(path)) {
            try (var normalizedOntology = Ontology.loadOntology(path)) {
                var normalization = new TBoxNormalization();
                normalization.apply(normalizedOntology);
                assertTrue(originalOntology.isEntailed(normalizedOntology));
                assertTrue(normalizedOntology.isEntailed(originalOntology));
            }
        }
    }
}
