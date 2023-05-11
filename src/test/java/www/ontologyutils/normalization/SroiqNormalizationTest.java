package www.ontologyutils.normalization;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.Ontology;

public class SroiqNormalizationTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "/catsandnumbers.owl", "/bodysystem.owl", "/bfo.owl", "/apo.owl", "/aeo.owl", "/duo.owl",
            "/a-and-b.owl", "/Empty.owl", "/FishVehicle/Alignment.owl", "/owl-tests.owl", "/sroiq-tests.owl",
            "/FishVehicle/Disalignment.owl", "/FishVehicle/Fish.owl", "/FishVehicle/InitialOntology.owl",
            "/FishVehicle/InitialOntologyAlignment.owl", "/FishVehicle/InitialOntologyInsta.owl",
            "/FishVehicle/InitialOntologyInstantiationAlignment.owl", "/FishVehicle/Test_hybrid.owl",
            "/FishVehicle/Vehicle.owl", "/Random/C50_R10_0.001_0.001_0.001_62888.owl", "/pizza.owl"
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
