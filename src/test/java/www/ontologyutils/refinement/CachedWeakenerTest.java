
package www.ontologyutils.refinement;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.*;

public class CachedWeakenerTest {
    @ParameterizedTest
    @ValueSource(strings = {
            "/alch/catsandnumbers.owl", "/el/bodysystem.owl", "/alc/bfo.owl",
            "/el/a-and-b.owl", "/el/Empty.owl", "/alc/Alignment.owl", "/alcroiq/owl-tests.owl",
            "/alcri/sroiq-tests.owl", "/el/Disalignment.owl", "/alc/InitialOntology.owl",
            "/alc/InitialOntologyAlignment.owl", "/alc/InitialOntologyInsta.owl",
            "/alc/InitialOntologyInstantiationAlignment.owl",
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
    @ValueSource(strings = { "/alc/Test_hybrid.owl", "/alc/Fish.owl", "/alc/Vehicle.owl", "/shoin/pizza.owl" })
    public void cachedAndUncachedWeakeningAreEqualSlow(String resourceName) throws OWLOntologyCreationException {
        var path = SroiqAxiomWeakenerTest.class.getResource(resourceName).getFile();
        try (var ontology = Ontology.loadOntology(path)) {
            try (var cached = new AxiomWeakener(ontology)) {
                try (var uncached = new AxiomWeakener(ontology, ontology, true)) {
                    Stream.concat(
                            Stream.concat(ontology.rboxAxioms(), ontology.aboxAxioms()),
                            ontology.tboxAxioms().limit(10)).forEach(axiom -> {
                                assertEquals(Utils.toSet(uncached.weakerAxioms(axiom)),
                                        Utils.toSet(cached.weakerAxioms(axiom)));
                            });
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/alch/catsandnumbers.owl", "/el/bodysystem.owl", "/alc/bfo.owl",
            "/el/a-and-b.owl", "/el/Empty.owl", "/alc/Alignment.owl", "/alcroiq/owl-tests.owl",
            "/alcri/sroiq-tests.owl", "/el/Disalignment.owl", "/alc/InitialOntology.owl",
            "/alc/InitialOntologyAlignment.owl", "/alc/InitialOntologyInsta.owl",
            "/alc/InitialOntologyInstantiationAlignment.owl",
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
    @ValueSource(strings = { "/alc/Test_hybrid.owl", "/shoin/pizza.owl", "/alc/Vehicle.owl", "/alc/Fish.owl", })
    public void cachedAndUncachedStrengtheningAreEqualSlow(String resourceName) throws OWLOntologyCreationException {
        var path = SroiqAxiomWeakenerTest.class.getResource(resourceName).getFile();
        try (var ontology = Ontology.loadOntology(path)) {
            try (var cached = new AxiomStrengthener(ontology)) {
                try (var uncached = new AxiomStrengthener(ontology, ontology, true)) {
                    Stream.concat(
                            Stream.concat(ontology.rboxAxioms(), ontology.aboxAxioms()),
                            ontology.tboxAxioms().limit(10)).forEach(axiom -> {
                                assertEquals(Utils.toSet(uncached.strongerAxioms(axiom)),
                                        Utils.toSet(cached.strongerAxioms(axiom)));
                            });
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/alch/catsandnumbers.owl", "/el/bodysystem.owl", "/alc/bfo.owl",
            "/el/a-and-b.owl", "/el/Empty.owl", "/alc/Alignment.owl", "/alcroiq/owl-tests.owl",
            "/alcri/sroiq-tests.owl", "/el/Disalignment.owl", "/alc/Fish.owl", "/alc/InitialOntology.owl",
            "/alc/InitialOntologyAlignment.owl", "/alc/InitialOntologyInsta.owl",
            "/alc/InitialOntologyInstantiationAlignment.owl", "/alc/Test_hybrid.owl",
            "/alc/Vehicle.owl",
    })
    public void cachedAndUncachedCoversAreEqual(String resourceName) throws OWLOntologyCreationException {
        var path = SroiqAxiomWeakenerTest.class.getResource(resourceName).getFile();
        try (var ontology = Ontology.loadOntology(path)) {
            var subConcepts = Utils.toSet(ontology.subConcepts());
            var simpleRoles = Utils.toSet(ontology.simpleRoles());
            try (var cached = new Covers(ontology, subConcepts, simpleRoles)) {
                try (var uncached = new Covers(ontology, subConcepts, simpleRoles, true)) {
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
