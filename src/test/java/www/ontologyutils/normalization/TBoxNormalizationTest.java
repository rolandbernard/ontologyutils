package www.ontologyutils.normalization;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.Ontology;

public class TBoxNormalizationTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "../catsandnumbers.owl", "../bodysystem.owl", "../bfo.owl", "../apo.owl", "../aeo.owl", "../duo.owl",
    })
    public void allTBoxAxiomsAreSubclassOf(final String resourceName) throws OWLOntologyCreationException {
        final var path = TBoxNormalizationTest.class.getResource(resourceName).getFile();
        try (final var ontology = Ontology.loadOntology(path)) {
            final var normalization = new TBoxNormalization();
            normalization.apply(ontology);
            ontology.tboxAxioms()
                    .forEach(axiom -> assertEquals(AxiomType.SUBCLASS_OF, axiom.getAxiomType()));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "../catsandnumbers.owl", "../bodysystem.owl", "../bfo.owl", "../apo.owl", "../aeo.owl", "../duo.owl",
    })
    public void normalizedOntologyIsEquivalent(final String resourceName) throws OWLOntologyCreationException {
        final var path = TBoxNormalizationTest.class.getResource(resourceName).getFile();
        try (final var originalOntology = Ontology.loadOntology(path)) {
            try (final var normalizedOntology = Ontology.loadOntology(path)) {
                final var normalization = new TBoxNormalization();
                normalization.apply(normalizedOntology);
                normalizedOntology.axioms()
                        .forEach(normalizedAxiom -> assertTrue(originalOntology.isEntailed(normalizedAxiom)));
                originalOntology.axioms()
                        .forEach(originalAxiom -> assertTrue(normalizedOntology.isEntailed(originalAxiom)));
                normalizedOntology.close();
                originalOntology.close();
            }
        }
    }
}
