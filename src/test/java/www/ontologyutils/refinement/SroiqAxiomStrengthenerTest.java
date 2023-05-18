package www.ontologyutils.refinement;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.*;

public class SroiqAxiomStrengthenerTest {
    private static final String ONTOLOGY_IRI = "http://www.semanticweb.org/roland/ontologies/2023/3/untitled/";

    private Ontology ontology;
    private AxiomStrengthener axiomStrengthener;

    @BeforeEach
    public void setup() {
        var path = RoleCoverTest.class.getResource("/alcri/sroiq-tests.owl").getFile();
        ontology = Ontology.loadOntology(path);
        axiomStrengthener = new AxiomStrengthener(ontology);
    }

    @AfterEach
    public void teardown() {
        ontology.close();
        axiomStrengthener.close();
    }

    private static Stream<Arguments> expectedStrengthening() {
        var df = Ontology.getDefaultDataFactory();
        return Stream.of(
                Arguments.of(
                        Set.of(
                                df.getOWLSubClassOfAxiom(df.getOWLClass(ONTOLOGY_IRI, "Parent"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent")),
                                df.getOWLSubClassOfAxiom(df.getOWLClass(ONTOLOGY_IRI, "Person"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent")),
                                df.getOWLSubClassOfAxiom(df.getOWLClass(ONTOLOGY_IRI, "Parent"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Mother")),
                                df.getOWLSubClassOfAxiom(df.getOWLClass(ONTOLOGY_IRI, "Parent"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Father")),
                                df.getOWLSubClassOfAxiom(df.getOWLClass(ONTOLOGY_IRI, "Parent"),
                                        df.getOWLObjectSomeValuesFrom(
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                                df.getOWLClass(ONTOLOGY_IRI, "Person")))),
                        df.getOWLSubClassOfAxiom(df.getOWLClass(ONTOLOGY_IRI, "Parent"),
                                df.getOWLClass(ONTOLOGY_IRI, "Parent"))),
                Arguments.of(
                        Set.of(
                                df.getOWLClassAssertionAxiom(
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent").getObjectComplementOf(),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "Test")),
                                df.getOWLClassAssertionAxiom(
                                        df.getOWLClass(ONTOLOGY_IRI, "Person").getObjectComplementOf(),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "Test")),
                                df.getOWLClassAssertionAxiom(
                                        df.getOWLNothing(),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "Test"))),
                        df.getOWLClassAssertionAxiom(
                                df.getOWLClass(ONTOLOGY_IRI, "Parent").getObjectComplementOf(),
                                df.getOWLNamedIndividual(ONTOLOGY_IRI, "Test"))),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectPropertyAssertionAxiom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "fatherOf"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "B")),
                                df.getOWLObjectPropertyAssertionAxiom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "motherOf"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "B")),
                                df.getOWLObjectPropertyAssertionAxiom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "B")),
                                df.getOWLObjectPropertyAssertionAxiom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf").getInverseProperty(),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "B")),
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLThing(), df.getOWLNothing())),
                        df.getOWLObjectPropertyAssertionAxiom(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                df.getOWLNamedIndividual(ONTOLOGY_IRI, "B"))),
                Arguments.of(
                        Set.of(
                                df.getOWLNegativeObjectPropertyAssertionAxiom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "ancestorOf"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "B")),
                                df.getOWLNegativeObjectPropertyAssertionAxiom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf").getInverseProperty(),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "B")),
                                df.getOWLNegativeObjectPropertyAssertionAxiom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "B")),
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLThing(), df.getOWLNothing())),
                        df.getOWLNegativeObjectPropertyAssertionAxiom(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                df.getOWLNamedIndividual(ONTOLOGY_IRI, "B"))),
                Arguments.of(
                        Set.of(
                                df.getOWLSameIndividualAxiom(
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "B")),
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLThing(), df.getOWLNothing())),
                        df.getOWLSameIndividualAxiom(
                                df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                df.getOWLNamedIndividual(ONTOLOGY_IRI, "B"))),
                Arguments.of(
                        Set.of(
                                df.getOWLDifferentIndividualsAxiom(
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "B")),
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLThing(), df.getOWLNothing())),
                        df.getOWLDifferentIndividualsAxiom(
                                df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                df.getOWLNamedIndividual(ONTOLOGY_IRI, "B"))));
    }

    @ParameterizedTest
    @MethodSource("expectedStrengthening")
    public void strengthenAxiom(Set<OWLAxiom> expected, OWLAxiom axiom) {
        assertEquals(expected, Utils.toSet(axiomStrengthener.strongerAxioms(axiom)));
    }

    @Test
    public void allStrongAxiomsEntailWeakerAxioms() {
        ontology.logicalAxioms().forEach(weakAxiom -> {
            try (var copy = ontology.clone()) {
                copy.removeAxioms(weakAxiom);
                try (var axiomStrengthener = new AxiomStrengthener(copy)) {
                    axiomStrengthener.strongerAxioms(weakAxiom).forEach(strongAxiom -> {
                        try (var copy2 = copy.clone()) {
                            copy2.addAxioms(strongAxiom);
                            // Some reasoners don't like entailment on inconsistent ontologies.
                            assertTrue(!copy2.isConsistent() || copy2.isEntailed(weakAxiom));
                        }
                    });
                }
            }
        });
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "/alch/catsandnumbers.owl", "/el/bodysystem.owl", "/alc/bfo.owl",
            "/el/a-and-b.owl", "/el/Empty.owl", "/alc/Alignment.owl", "/alcroiq/owl-tests.owl",
            "/alcri/sroiq-tests.owl", "/el/Disalignment.owl", "/alc/Fish.owl", "/alc/InitialOntology.owl",
            "/alc/InitialOntologyAlignment.owl", "/alc/InitialOntologyInsta.owl",
            "/alc/InitialOntologyInstantiationAlignment.owl", "/alc/Vehicle.owl",
    })
    public void allStrongAxiomsEntailWeakerAxiomsFromFile(String resourceName)
            throws OWLOntologyCreationException {
        var path = SroiqAxiomWeakenerTest.class.getResource(resourceName).getFile();
        try (var ontology = Ontology.loadOntology(path)) {
            ontology.logicalAxioms().forEach(weakAxiom -> {
                try (var copy = ontology.clone()) {
                    copy.removeAxioms(weakAxiom);
                    try (var axiomStrengthener = new AxiomStrengthener(copy)) {
                        axiomStrengthener.strongerAxioms(weakAxiom).forEach(strongAxiom -> {
                            try (var copy2 = copy.clone()) {
                                copy2.addAxioms(strongAxiom);
                                // Some reasoners don't like entailment on inconsistent ontologies.
                                assertTrue(!copy2.isConsistent() || copy2.isEntailed(weakAxiom));
                            }
                        });
                    }
                }
            });
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "/shoin/pizza.owl", "/alc/Test_hybrid.owl", })
    public void allStrongAxiomsEntailWeakerAxiomsFromFileSlow(String resourceName)
            throws OWLOntologyCreationException {
        var path = SroiqAxiomWeakenerTest.class.getResource(resourceName).getFile();
        try (var ontology = Ontology.loadOntology(path)) {
            Stream.concat(
                    Stream.concat(ontology.rboxAxioms(), ontology.aboxAxioms()),
                    ontology.tboxAxioms().limit(10)).forEach(weakAxiom -> {
                        try (var copy = ontology.clone()) {
                            copy.removeAxioms(weakAxiom);
                            try (var axiomStrengthener = new AxiomStrengthener(copy)) {
                                axiomStrengthener.strongerAxioms(weakAxiom)
                                        .forEach(strongAxiom -> {
                                            try (var copy2 = copy.clone()) {
                                                copy2.addAxioms(strongAxiom);
                                                // Some reasoners don't like
                                                // entailment on inconsistent
                                                // ontologies.
                                                assertTrue(!copy2.isConsistent()
                                                        || copy2.isEntailed(
                                                                weakAxiom));
                                            }
                                        });
                            }
                        }
                    });
        }
    }
}
