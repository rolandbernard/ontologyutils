package www.ontologyutils.refinement;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.*;

@Execution(ExecutionMode.CONCURRENT)
public class RefinementOperatorTest {
    private static final String ONTOLOGY_IRI = "http://www.semanticweb.org/ontologies/dl2017_example#";

    private Ontology ontology;
    private Covers covers;
    private RefinementOperator generalization;
    private RefinementOperator specialization;

    @BeforeEach
    public void setup() {
        var path = RoleCoverTest.class.getResource("/alch/catsandnumbers.owl").getFile();
        ontology = Ontology.loadOntology(path);
        var subConcepts = Utils.toSet(ontology.subConcepts());
        var simpleRoles = Utils.toSet(ontology.simpleRoles());
        covers = new Covers(ontology, subConcepts, simpleRoles, simpleRoles, 0);
        var upCover = covers.upCover().cached();
        var downCover = covers.downCover().cached();
        generalization = new RefinementOperator(upCover, downCover);
        specialization = new RefinementOperator(downCover, upCover);
    }

    @AfterEach
    public void teardown() {
        ontology.close();
    }

    private static Stream<Arguments> expectedGeneralization() {
        var df = Ontology.getDefaultDataFactory();
        return Stream.of(
                Arguments.of(
                        Set.of(df.getOWLThing()),
                        df.getOWLThing()),
                Arguments.of(
                        Set.of(
                                df.getOWLClass(ONTOLOGY_IRI, "AbstractObject"),
                                df.getOWLClass(ONTOLOGY_IRI, "Number")),
                        df.getOWLClass(ONTOLOGY_IRI, "Number")),
                Arguments.of(
                        Set.of(
                                df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                df.getOWLClass(ONTOLOGY_IRI, "Pet")),
                        df.getOWLClass(ONTOLOGY_IRI, "Cat")),
                Arguments.of(
                        Set.of(
                                df.getOWLClass(ONTOLOGY_IRI, "Pet"),
                                df.getOWLClass(ONTOLOGY_IRI, "Animal")),
                        df.getOWLClass(ONTOLOGY_IRI, "Pet")),
                Arguments.of(
                        Set.of(
                                df.getOWLClass(ONTOLOGY_IRI, "Integer"),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Integer"),
                                        df.getOWLObjectAllValuesFrom(
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasQuality"),
                                                df.getOWLClass(ONTOLOGY_IRI, "Quality"))),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Number"),
                                        df.getOWLObjectAllValuesFrom(
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasQuality"),
                                                df.getOWLClass(ONTOLOGY_IRI, "Primeness"))),
                                df.getOWLClass(ONTOLOGY_IRI, "PrimeNumber"),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Integer"),
                                        df.getOWLObjectAllValuesFrom(
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasQuality"),
                                                df.getOWLClass(ONTOLOGY_IRI, "Primeness"))),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Integer"),
                                        df.getOWLThing()),
                                df.getOWLObjectAllValuesFrom(df.getOWLObjectProperty(ONTOLOGY_IRI, "hasQuality"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Primeness"))),
                        df.getOWLObjectIntersectionOf(
                                df.getOWLClass(ONTOLOGY_IRI, "Integer"),
                                df.getOWLObjectAllValuesFrom(df.getOWLObjectProperty(ONTOLOGY_IRI, "hasQuality"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Primeness")))),
                Arguments.of(
                        Set.of(
                                df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                df.getOWLObjectSomeValuesFrom(df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Black")),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                        df.getOWLClass(ONTOLOGY_IRI, "PhysicalObject")),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                        df.getOWLObjectSomeValuesFrom(
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                                df.getOWLClass(ONTOLOGY_IRI, "GrayScale"))),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Pet"),
                                        df.getOWLObjectSomeValuesFrom(
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                                df.getOWLClass(ONTOLOGY_IRI, "Black"))),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                        df.getOWLObjectSomeValuesFrom(
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                                df.getOWLClass(ONTOLOGY_IRI, "Black")))),
                        df.getOWLObjectIntersectionOf(
                                df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                df.getOWLObjectSomeValuesFrom(df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Black")))));
    }

    @ParameterizedTest
    @MethodSource("expectedGeneralization")
    public void generalize(Set<OWLClassExpression> expected, OWLClassExpression concept) {
        assertEquals(expected, Utils.toSet(generalization.refine(concept)));
    }

    private static Stream<Arguments> expectedSpecialization() {
        var df = Ontology.getDefaultDataFactory();
        return Stream.of(
                Arguments.of(
                        Set.of(
                                df.getOWLClass(ONTOLOGY_IRI, "Colour"),
                                df.getOWLClass(ONTOLOGY_IRI, "GrayScale")),
                        df.getOWLClass(ONTOLOGY_IRI, "Colour")),
                Arguments.of(
                        Set.of(
                                df.getOWLClass(ONTOLOGY_IRI, "GrayScale"),
                                df.getOWLClass(ONTOLOGY_IRI, "White"),
                                df.getOWLClass(ONTOLOGY_IRI, "Black")),
                        df.getOWLClass(ONTOLOGY_IRI, "GrayScale")),
                Arguments.of(
                        Set.of(
                                df.getOWLClass(ONTOLOGY_IRI, "Primeness"),
                                df.getOWLNothing()),
                        df.getOWLClass(ONTOLOGY_IRI, "Primeness")),
                Arguments.of(
                        Set.of(
                                df.getOWLClass(ONTOLOGY_IRI, "Number"),
                                df.getOWLClass(ONTOLOGY_IRI, "Integer")),
                        df.getOWLClass(ONTOLOGY_IRI, "Number")),
                Arguments.of(
                        Set.of(
                                df.getOWLThing(),
                                df.getOWLClass(ONTOLOGY_IRI, "PhysicalObject"),
                                df.getOWLObjectAllValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "hasQuality"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Primeness")),
                                df.getOWLObjectAllValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                        df.getOWLClass(ONTOLOGY_IRI, "White")),
                                df.getOWLObjectAllValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Black"))),
                        df.getOWLThing()),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLObjectIntersectionOf(
                                                df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                                df.getOWLObjectSomeValuesFrom(
                                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                                        df.getOWLClass(ONTOLOGY_IRI, "White"))),
                                        df.getOWLObjectSomeValuesFrom(
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                                df.getOWLClass(ONTOLOGY_IRI, "Black"))),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                        df.getOWLObjectIntersectionOf(
                                                df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                                df.getOWLObjectSomeValuesFrom(
                                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                                        df.getOWLClass(ONTOLOGY_IRI, "Black")))),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                        df.getOWLObjectSomeValuesFrom(
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                                df.getOWLNothing())),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                        df.getOWLObjectSomeValuesFrom(
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                                df.getOWLClass(ONTOLOGY_IRI, "Black"))),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLObjectIntersectionOf(
                                                df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                                df.getOWLObjectSomeValuesFrom(
                                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                                        df.getOWLClass(ONTOLOGY_IRI, "Black"))),
                                        df.getOWLObjectSomeValuesFrom(
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                                df.getOWLClass(ONTOLOGY_IRI, "Black"))),
                                df.getOWLClass(ONTOLOGY_IRI, "BlackCat"),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLObjectIntersectionOf(
                                                df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                                df.getOWLObjectSomeValuesFrom(
                                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                                        df.getOWLClass(ONTOLOGY_IRI, "Black"))),
                                        df.getOWLObjectAllValuesFrom(
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                                df.getOWLClass(ONTOLOGY_IRI, "Black")))),
                        df.getOWLObjectIntersectionOf(
                                df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                df.getOWLObjectSomeValuesFrom(df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Black")))));
    }

    @ParameterizedTest
    @MethodSource("expectedSpecialization")
    public void specialize(Set<OWLClassExpression> expected, OWLClassExpression concept) {
        assertEquals(expected, Utils.toSet(specialization.refine(concept)));
    }

    @Test
    public void allGeneralize() {
        ontology.subConcepts().map(generalization::refine).forEach(Stream::count);
    }

    @Test
    public void allSpecialize() {
        ontology.subConcepts().map(specialization::refine).forEach(Stream::count);
    }
}
