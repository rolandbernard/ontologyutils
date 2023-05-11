
package www.ontologyutils.refinement;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.*;

public class CachedWeakenerTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "/catsandnumbers.owl", "/bodysystem.owl", "/bfo.owl",
            "/a-and-b.owl", "/Empty.owl", "/FishVehicle/Alignment.owl", "/owl-tests.owl",
            "/FishVehicle/Disalignment.owl", "/FishVehicle/Fish.owl", "/FishVehicle/InitialOntology.owl",
            "/FishVehicle/InitialOntologyAlignment.owl", "/FishVehicle/InitialOntologyInsta.owl",
            "/FishVehicle/InitialOntologyInstantiationAlignment.owl", "/FishVehicle/Test_hybrid.owl",
            "/FishVehicle/Vehicle.owl", "/pizza.owl"
    })
    public void cachedAndUncachedWeakeningAreEqual(String resourceName) throws OWLOntologyCreationException {
        var path = SroiqAxiomWeakenerTest.class.getResource(resourceName).getFile();
        try (var ontology = Ontology.loadOntology(path)) {
            try (var cached = new AxiomWeakener(ontology)) {
                try (var uncached = new AxiomWeakener(ontology, ontology, true)) {
                    ontology.logicalAxioms().forEach(axiom -> {
                        assertEquals(Utils.toSet(uncached.weakerAxioms(axiom)),
                                Utils.toSet(cached.weakerAxioms(axiom)));
                    });
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/catsandnumbers.owl", "/bodysystem.owl", "/bfo.owl",
            "/a-and-b.owl", "/Empty.owl", "/FishVehicle/Alignment.owl", "/owl-tests.owl",
            "/FishVehicle/Disalignment.owl", "/FishVehicle/Fish.owl", "/FishVehicle/InitialOntology.owl",
            "/FishVehicle/InitialOntologyAlignment.owl", "/FishVehicle/InitialOntologyInsta.owl",
            "/FishVehicle/InitialOntologyInstantiationAlignment.owl", "/FishVehicle/Test_hybrid.owl",
            "/FishVehicle/Vehicle.owl", "/pizza.owl"
    })
    public void cachedAndUncachedStrengtheningAreEqual(String resourceName) throws OWLOntologyCreationException {
        var path = SroiqAxiomWeakenerTest.class.getResource(resourceName).getFile();
        try (var ontology = Ontology.loadOntology(path)) {
            try (var cached = new AxiomStrengthener(ontology)) {
                try (var uncached = new AxiomStrengthener(ontology, ontology, true)) {
                    ontology.logicalAxioms().forEach(axiom -> {
                        assertEquals(Utils.toSet(uncached.strongerAxioms(axiom)),
                                Utils.toSet(cached.strongerAxioms(axiom)));
                    });
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/catsandnumbers.owl", "/bodysystem.owl", "/bfo.owl", "/a-and-b.owl", "/Empty.owl",
            "/FishVehicle/Alignment.owl", "/owl-tests.owl", "/FishVehicle/Disalignment.owl",
            "/FishVehicle/Fish.owl", "/FishVehicle/InitialOntology.owl", "/FishVehicle/Vehicle.owl",
            "/FishVehicle/InitialOntologyAlignment.owl", "/FishVehicle/InitialOntologyInsta.owl",
            "/FishVehicle/InitialOntologyInstantiationAlignment.owl", "/FishVehicle/Test_hybrid.owl",
    })
    public void cachedAndUncachedCoversAreEqual(String resourceName) throws OWLOntologyCreationException {
        var path = SroiqAxiomWeakenerTest.class.getResource(resourceName).getFile();
        try (var ontology = Ontology.loadOntology(path)) {
            var simpleRoles = Utils.toSet(ontology.simpleRoles());
            try (var cached = new Covers(ontology, simpleRoles)) {
                try (var uncached = new Covers(ontology, simpleRoles, true)) {
                    ontology.subConcepts().forEach(concept -> {
                        assertEquals(Utils.toSet(uncached.upCover(concept)), Utils.toSet(cached.upCover(concept)));
                        assertEquals(Utils.toSet(uncached.downCover(concept)), Utils.toSet(cached.downCover(concept)));
                    });
                    ontology.rolesInSignature().forEach(role -> {
                        assertEquals(Utils.toSet(uncached.upCover(role)), Utils.toSet(cached.upCover(role)));
                        assertEquals(Utils.toSet(uncached.downCover(role)), Utils.toSet(cached.downCover(role)));
                    });
                }
            }
        }
    }
}
