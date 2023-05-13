package www.ontologyutils.normalization;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.Ontology;

public class SroiqNormalizationTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "/alch/catsandnumbers.owl", "/el/bodysystem.owl", "/alc/bfo.owl", "/el/apo.owl", "/el/aeo.owl",
            "/el/duo.owl", "/el/a-and-b.owl", "/el/Empty.owl", "/alc/Alignment.owl", "/alcroiq/owl-tests.owl",
            "/alcri/sroiq-tests.owl", "/el/Disalignment.owl", "/alc/Fish.owl", "/alc/InitialOntology.owl",
            "/alc/InitialOntologyAlignment.owl", "/alc/InitialOntologyInsta.owl",
            "/alc/InitialOntologyInstantiationAlignment.owl", "/alc/Test_hybrid.owl",
            "/alc/Vehicle.owl", "/alc/C50_R10_0.001_0.001_0.001_62888.owl", "/shoin/pizza.owl"
    })
    public void normalizedOntologyIsEquivalent(String resourceName) throws OWLOntologyCreationException {
        var path = SroiqNormalizationTest.class.getResource(resourceName).getFile();
        try (var originalOntology = Ontology.loadOntology(path)) {
            try (var normalizedOntology = originalOntology.clone()) {
                var normalization = new SroiqNormalization();
                normalization.apply(normalizedOntology);
                assertTrue(originalOntology.isEntailed(normalizedOntology));
                assertTrue(normalizedOntology.isEntailed(originalOntology));
            }
        }
    }
}
