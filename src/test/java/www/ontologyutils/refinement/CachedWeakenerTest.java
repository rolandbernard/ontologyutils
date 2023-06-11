
package www.ontologyutils.refinement;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.parallel.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.*;

@Execution(ExecutionMode.CONCURRENT)
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
            var cached = new AxiomWeakener(ontology);
            var uncached = new AxiomWeakener(ontology, ontology, AxiomWeakener.FLAG_UNCACHED);
            ontology.logicalAxioms().forEach(axiom -> {
                assertEquals(Utils.toSet(uncached.weakerAxioms(axiom)),
                        Utils.toSet(cached.weakerAxioms(axiom)));
            });
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "/alc/Test_hybrid.owl", "/alc/Fish.owl", "/alc/Vehicle.owl", "/shoin/pizza.owl" })
    public void cachedAndUncachedWeakeningAreEqualSlow(String resourceName) throws OWLOntologyCreationException {
        var path = SroiqAxiomWeakenerTest.class.getResource(resourceName).getFile();
        try (var ontology = Ontology.loadOntology(path)) {
            var cached = new AxiomWeakener(ontology);
            var uncached = new AxiomWeakener(ontology, ontology, AxiomWeakener.FLAG_UNCACHED);
            var axioms = Stream.concat(
                    Stream.concat(ontology.rboxAxioms(), ontology.aboxAxioms()),
                    ontology.tboxAxioms().limit(10));
            axioms.forEach(axiom -> {
                assertEquals(Utils.toSet(uncached.weakerAxioms(axiom)),
                        Utils.toSet(cached.weakerAxioms(axiom)));
            });
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
            var cached = new AxiomStrengthener(ontology);
            var uncached = new AxiomStrengthener(ontology, ontology, AxiomStrengthener.FLAG_UNCACHED);
            ontology.logicalAxioms().forEach(axiom -> {
                assertEquals(Utils.toSet(uncached.strongerAxioms(axiom)),
                        Utils.toSet(cached.strongerAxioms(axiom)));
            });
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "/alc/Test_hybrid.owl", "/shoin/pizza.owl", "/alc/Vehicle.owl", "/alc/Fish.owl", })
    public void cachedAndUncachedStrengtheningAreEqualSlow(String resourceName) throws OWLOntologyCreationException {
        var path = SroiqAxiomWeakenerTest.class.getResource(resourceName).getFile();
        try (var ontology = Ontology.loadOntology(path)) {
            var cached = new AxiomStrengthener(ontology);
            var uncached = new AxiomStrengthener(ontology, ontology, AxiomStrengthener.FLAG_UNCACHED);
            var axioms = Stream.concat(
                    Stream.concat(ontology.rboxAxioms(), ontology.aboxAxioms()),
                    ontology.tboxAxioms().limit(10));
            axioms.forEach(axiom -> {
                assertEquals(Utils.toSet(uncached.strongerAxioms(axiom)),
                        Utils.toSet(cached.strongerAxioms(axiom)));
            });
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
            var subRoles = Utils.toSet(ontology.subRoles());
            var simpleRoles = Utils.toSet(ontology.simpleRoles());
            var cached = new Covers(ontology, subConcepts, subRoles, simpleRoles, 0);
            var basicCache = new Covers(ontology, subConcepts, subRoles, simpleRoles, AxiomWeakener.FLAG_BASIC_CACHED);
            var uncached = new Covers(ontology, subConcepts, subRoles, simpleRoles, AxiomWeakener.FLAG_UNCACHED);
            ontology.subConcepts().forEach(concept -> {
                assertEquals(Utils.toSet(uncached.upCover(concept)), Utils.toSet(cached.upCover(concept)));
                assertEquals(Utils.toSet(uncached.downCover(concept)), Utils.toSet(cached.downCover(concept)));
            });
            ontology.rolesInSignature().forEach(role -> {
                for (var simple : List.of(true, false)) {
                    var up = Utils.toSet(uncached.upCover(role, simple));
                    var down = Utils.toSet(uncached.downCover(role, simple));
                    assertEquals(up, Utils.toSet(cached.upCover(role, simple)));
                    assertEquals(down, Utils.toSet(cached.downCover(role, simple)));
                    assertEquals(up, Utils.toSet(basicCache.upCover(role, simple)));
                    assertEquals(down, Utils.toSet(basicCache.downCover(role, simple)));
                }
            });
        }
    }
}
