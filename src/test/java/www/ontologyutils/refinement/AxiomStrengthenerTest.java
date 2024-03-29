package www.ontologyutils.refinement;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.Ontology;
import www.ontologyutils.toolbox.Utils;

@Execution(ExecutionMode.CONCURRENT)
public class AxiomStrengthenerTest {
    private static final String ONTOLOGY_IRI = "http://www.semanticweb.org/ontologies/dl2017_example#";

    private Ontology ontology;
    private AxiomStrengthener axiomStrengthener;

    @BeforeEach
    public void setup() {
        var path = RoleCoverTest.class.getResource("/alch/catsandnumbers.owl").getFile();
        ontology = Ontology.loadOntology(path);
        axiomStrengthener = new AxiomStrengthener(ontology);
    }

    @AfterEach
    public void teardown() {
        ontology.close();
    }

    private static Stream<Arguments> expectedStrengthening() {
        var df = Ontology.getDefaultDataFactory();
        return Stream.of(
                Arguments.of(
                        Set.of(
                                df.getOWLSubClassOfAxiom(df.getOWLThing(), df.getOWLClass(ONTOLOGY_IRI, "Number")),
                                df.getOWLSubClassOfAxiom(df.getOWLThing(), df.getOWLClass(ONTOLOGY_IRI, "Integer"))),
                        df.getOWLSubClassOfAxiom(df.getOWLThing(), df.getOWLClass(ONTOLOGY_IRI, "Number"))),
                Arguments.of(
                        Set.of(
                                df.getOWLSubClassOfAxiom(df.getOWLClass(ONTOLOGY_IRI, "Number"),
                                        df.getOWLClass(ONTOLOGY_IRI, "GrayScale")),
                                df.getOWLSubClassOfAxiom(df.getOWLClass(ONTOLOGY_IRI, "Number"),
                                        df.getOWLClass(ONTOLOGY_IRI, "White")),
                                df.getOWLSubClassOfAxiom(df.getOWLClass(ONTOLOGY_IRI, "Number"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Black")),
                                df.getOWLSubClassOfAxiom(df.getOWLClass(ONTOLOGY_IRI, "AbstractObject"),
                                        df.getOWLClass(ONTOLOGY_IRI, "GrayScale"))),
                        df.getOWLSubClassOfAxiom(df.getOWLClass(ONTOLOGY_IRI, "Number"),
                                df.getOWLClass(ONTOLOGY_IRI, "GrayScale"))),
                Arguments.of(
                        Set.of(
                                df.getOWLClassAssertionAxiom(
                                        df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "Leo")),
                                df.getOWLClassAssertionAxiom(
                                        df.getOWLObjectIntersectionOf(
                                                df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                                df.getOWLObjectSomeValuesFrom(
                                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                                        df.getOWLClass(ONTOLOGY_IRI, "Black"))),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "Leo")),
                                df.getOWLClassAssertionAxiom(
                                        df.getOWLObjectIntersectionOf(
                                                df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                                df.getOWLObjectSomeValuesFrom(
                                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                                        df.getOWLClass(ONTOLOGY_IRI, "White"))),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "Leo"))),
                        df.getOWLClassAssertionAxiom(
                                df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                df.getOWLNamedIndividual(ONTOLOGY_IRI, "Leo"))));
    }

    @ParameterizedTest
    @MethodSource("expectedStrengthening")
    public void strengthenAxiom(Set<OWLAxiom> expected, OWLAxiom axiom) {
        assertEquals(expected, Utils.toSet(axiomStrengthener.strongerAxioms(axiom)));
    }

    @Test
    public void allStrongAxiomsEntailWeakerAxioms() {
        ontology.logicalAxioms().forEach(weakAxiom -> {
            try (var copy = ontology.cloneWithSeparateCache()) {
                copy.removeAxioms(weakAxiom);
                var axiomStrengthener = new AxiomStrengthener(copy);
                axiomStrengthener.strongerAxioms(weakAxiom).forEach(strongAxiom -> {
                    try (var copy2 = copy.clone()) {
                        copy2.addAxioms(strongAxiom);
                        // Some reasoners don't like entailment on inconsistent ontologies.
                        assertTrue(!copy2.isConsistent() || copy2.isEntailed(weakAxiom));
                    }
                });
            }
        });
    }
}
