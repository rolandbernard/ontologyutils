package www.ontologyutils.refinement;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.Ontology;

public class CoverTest {
    private static final String ONTOLOGY_IRI = "http://www.semanticweb.org/ontologies/dl2017_example#";

    private final Ontology ontology;
    private final Covers covers;

    public CoverTest() {
        final var path = RoleCoverTest.class.getResource("../catsandnumbers.owl").getFile();
        ontology = Ontology.loadOntology(path);
        covers = new Covers(ontology, ontology.simpleRoles().collect(Collectors.toSet()));
    }

    private static Stream<Arguments> expectedUpCover() {
        final var df = Ontology.getDefaultDataFactory();
        return Stream.of(
                Arguments.of(
                        Set.of(
                                df.getOWLClass(ONTOLOGY_IRI, "Integer"),
                                df.getOWLClass(ONTOLOGY_IRI, "Number")),
                        df.getOWLClass(ONTOLOGY_IRI, "Integer")),
                Arguments.of(
                        Set.of(
                                df.getOWLClass(ONTOLOGY_IRI, "Animal"),
                                df.getOWLClass(ONTOLOGY_IRI, "PhysicalObject")),
                        df.getOWLClass(ONTOLOGY_IRI, "Animal")),
                Arguments.of(
                        Set.of(
                                df.getOWLThing()),
                        df.getOWLThing()),
                Arguments.of(
                        Set.of(
                                df.getOWLClass(ONTOLOGY_IRI, "Number"),
                                df.getOWLClass(ONTOLOGY_IRI, "AbstractObject")),
                        df.getOWLClass(ONTOLOGY_IRI, "Number")),
                Arguments.of(
                        Set.of(
                                df.getOWLClass(ONTOLOGY_IRI, "Colour"),
                                df.getOWLClass(ONTOLOGY_IRI, "Quality")),
                        df.getOWLClass(ONTOLOGY_IRI, "Colour")),
                Arguments.of(
                        Set.of(
                                df.getOWLClass(ONTOLOGY_IRI, "Integer"),
                                df.getOWLClass(ONTOLOGY_IRI, "PrimeNumber"),
                                df.getOWLObjectAllValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI,
                                                "hasQuality"),
                                        df.getOWLClass(ONTOLOGY_IRI,
                                                "Primeness")),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Integer"),
                                        df.getOWLObjectAllValuesFrom(
                                                df.getOWLObjectProperty(
                                                        ONTOLOGY_IRI,
                                                        "hasQuality"),
                                                df.getOWLClass(ONTOLOGY_IRI,
                                                        "Primeness")))),
                        df.getOWLObjectIntersectionOf(
                                df.getOWLClass(ONTOLOGY_IRI, "Integer"),
                                df.getOWLObjectAllValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI,
                                                "hasQuality"),
                                        df.getOWLClass(ONTOLOGY_IRI,
                                                "Primeness")))),
                Arguments.of(
                        Set.of(
                                df.getOWLClass(ONTOLOGY_IRI, "BlackCat"),
                                df.getOWLObjectAllValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI,
                                                "hasColour"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Black")),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                        df.getOWLObjectSomeValuesFrom(
                                                df.getOWLObjectProperty(
                                                        ONTOLOGY_IRI,
                                                        "hasColour"),
                                                df.getOWLClass(ONTOLOGY_IRI,
                                                        "Black"))),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLObjectIntersectionOf(
                                                df.getOWLClass(ONTOLOGY_IRI,
                                                        "Cat"),
                                                df.getOWLObjectSomeValuesFrom(
                                                        df.getOWLObjectProperty(
                                                                ONTOLOGY_IRI,
                                                                "hasColour"),
                                                        df.getOWLClass(ONTOLOGY_IRI,
                                                                "Black"))),
                                        df.getOWLObjectAllValuesFrom(
                                                df.getOWLObjectProperty(
                                                        ONTOLOGY_IRI,
                                                        "hasColour"),
                                                df.getOWLClass(ONTOLOGY_IRI,
                                                        "Black")))),
                        df.getOWLClass(ONTOLOGY_IRI, "BlackCat")));
    }

    @ParameterizedTest
    @MethodSource("expectedUpCover")
    public void upCover(final Set<OWLClassExpression> expected, final OWLClassExpression concept) {
        assertEquals(expected, covers.upCover(concept).collect(Collectors.toSet()));
    }

    private static Stream<Arguments> expectedDownCover() {
        final var df = Ontology.getDefaultDataFactory();
        return Stream.of(
                Arguments.of(
                        Set.of(
                                df.getOWLClass(ONTOLOGY_IRI, "Integer"),
                                df.getOWLClass(ONTOLOGY_IRI, "PrimeNumber"),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Integer"),
                                        df.getOWLObjectAllValuesFrom(
                                                df.getOWLObjectProperty(
                                                        ONTOLOGY_IRI,
                                                        "hasQuality"),
                                                df.getOWLClass(ONTOLOGY_IRI,
                                                        "Primeness")))),
                        df.getOWLClass(ONTOLOGY_IRI, "Integer")),
                Arguments.of(
                        Set.of(
                                df.getOWLClass(ONTOLOGY_IRI, "Animal"),
                                df.getOWLClass(ONTOLOGY_IRI, "Pet")),
                        df.getOWLClass(ONTOLOGY_IRI, "Animal")),
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
                                        df.getOWLObjectProperty(ONTOLOGY_IRI,
                                                "hasColour"),
                                        df.getOWLClass(ONTOLOGY_IRI, "White")),
                                df.getOWLObjectAllValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI,
                                                "hasColour"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Black")),
                                df.getOWLObjectAllValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI,
                                                "hasQuality"),
                                        df.getOWLClass(ONTOLOGY_IRI,
                                                "Primeness"))),
                        df.getOWLThing()),
                Arguments.of(
                        Set.of(
                                df.getOWLNothing(),
                                df.getOWLClass(ONTOLOGY_IRI, "PrimeNumber"),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Integer"),
                                        df.getOWLObjectAllValuesFrom(
                                                df.getOWLObjectProperty(
                                                        ONTOLOGY_IRI,
                                                        "hasQuality"),
                                                df.getOWLClass(ONTOLOGY_IRI,
                                                        "Primeness")))),
                        df.getOWLObjectIntersectionOf(
                                df.getOWLClass(ONTOLOGY_IRI, "Integer"),
                                df.getOWLObjectAllValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI,
                                                "hasQuality"),
                                        df.getOWLClass(ONTOLOGY_IRI,
                                                "Primeness")))),
                Arguments.of(
                        Set.of(
                                df.getOWLClass(ONTOLOGY_IRI, "BlackCat"),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLObjectIntersectionOf(
                                                df.getOWLClass(ONTOLOGY_IRI,
                                                        "Cat"),
                                                df.getOWLObjectSomeValuesFrom(
                                                        df.getOWLObjectProperty(
                                                                ONTOLOGY_IRI,
                                                                "hasColour"),
                                                        df.getOWLClass(ONTOLOGY_IRI,
                                                                "Black"))),
                                        df.getOWLObjectAllValuesFrom(
                                                df.getOWLObjectProperty(
                                                        ONTOLOGY_IRI,
                                                        "hasColour"),
                                                df.getOWLClass(ONTOLOGY_IRI,
                                                        "Black"))),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                        df.getOWLObjectSomeValuesFrom(
                                                df.getOWLObjectProperty(
                                                        ONTOLOGY_IRI,
                                                        "hasColour"),
                                                df.getOWLClass(ONTOLOGY_IRI,
                                                        "Black")))),
                        df.getOWLObjectIntersectionOf(
                                df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                df.getOWLObjectSomeValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI,
                                                "hasColour"),
                                        df.getOWLClass(ONTOLOGY_IRI,
                                                "Black")))),
                Arguments.of(
                        Set.of(
                                df.getOWLClass(ONTOLOGY_IRI, "Colour"),
                                df.getOWLClass(ONTOLOGY_IRI, "GrayScale")),
                        df.getOWLClass(ONTOLOGY_IRI, "Colour")));
    }

    @ParameterizedTest
    @MethodSource("expectedDownCover")
    public void downCover(final Set<OWLClassExpression> expected, final OWLClassExpression concept) {
        assertEquals(expected, covers.downCover(concept).collect(Collectors.toSet()));
    }

    @Test
    public void allUpCover() {
        ontology.subConcepts().map(covers::upCover).forEach(Stream::count);
    }

    @Test
    public void allDownCover() {
        ontology.subConcepts().map(covers::downCover).forEach(Stream::count);
    }
}
