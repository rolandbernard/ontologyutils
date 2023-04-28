package www.ontologyutils.refinement;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.Ontology;

public class AxiomWeakenerTest {
    private static final String ONTOLOGY_IRI = "http://www.semanticweb.org/ontologies/dl2017_example#";

    private final Ontology ontology;
    private final AxiomWeakener axiomWeakener;

    public AxiomWeakenerTest() {
        final var path = RoleCoverTest.class.getResource("../catsandnumbers.owl").getFile();
        ontology = Ontology.loadOntology(path);
        axiomWeakener = new AxiomWeakener(ontology, ontology);
    }

    private static Stream<Arguments> expectedWeakening() {
        final var df = Ontology.getDefaultDataFactory();
        return Stream.of(
                Arguments.of(
                        Set.of(
                                df.getOWLSubClassOfAxiom(df.getOWLThing(), df.getOWLClass(ONTOLOGY_IRI, "Number")),
                                df.getOWLSubClassOfAxiom(df.getOWLThing(),
                                        df.getOWLClass(ONTOLOGY_IRI, "AbstractObject")),
                                df.getOWLSubClassOfAxiom(df.getOWLClass(ONTOLOGY_IRI, "PhysicalObject"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Number")),
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLObjectAllValuesFrom(
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasQuality"),
                                                df.getOWLClass(ONTOLOGY_IRI, "Primeness")),
                                        df.getOWLClass(ONTOLOGY_IRI, "Number")),
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLObjectAllValuesFrom(
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                                df.getOWLClass(ONTOLOGY_IRI, "White")),
                                        df.getOWLClass(ONTOLOGY_IRI, "Number")),
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLObjectAllValuesFrom(
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                                df.getOWLClass(ONTOLOGY_IRI, "Black")),
                                        df.getOWLClass(ONTOLOGY_IRI, "Number"))),
                        df.getOWLSubClassOfAxiom(df.getOWLThing(), df.getOWLClass(ONTOLOGY_IRI, "Number"))),
                Arguments.of(
                        Set.of(
                                df.getOWLSubClassOfAxiom(df.getOWLClass(ONTOLOGY_IRI, "Colour"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Cat")),
                                df.getOWLSubClassOfAxiom(df.getOWLClass(ONTOLOGY_IRI, "GrayScale"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Cat")),
                                df.getOWLSubClassOfAxiom(df.getOWLClass(ONTOLOGY_IRI, "Colour"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Pet"))),
                        df.getOWLSubClassOfAxiom(df.getOWLClass(ONTOLOGY_IRI, "Colour"),
                                df.getOWLClass(ONTOLOGY_IRI, "Cat"))),
                Arguments.of(
                        Set.of(
                                df.getOWLClassAssertionAxiom(df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "Leo")),
                                df.getOWLClassAssertionAxiom(df.getOWLClass(ONTOLOGY_IRI, "Pet"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "Leo"))),
                        df.getOWLClassAssertionAxiom(df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                df.getOWLNamedIndividual(ONTOLOGY_IRI, "Leo"))),
                Arguments.of(
                        Set.of(
                                df.getOWLClassAssertionAxiom(
                                        df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "Leo")),
                                df.getOWLClassAssertionAxiom(
                                        df.getOWLObjectSomeValuesFrom(
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                                df.getOWLClass(ONTOLOGY_IRI, "Black")),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "Leo")),
                                df.getOWLClassAssertionAxiom(
                                        df.getOWLObjectIntersectionOf(
                                                df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                                df.getOWLClass(ONTOLOGY_IRI, "PhysicalObject")),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "Leo")),
                                df.getOWLClassAssertionAxiom(
                                        df.getOWLObjectIntersectionOf(
                                                df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                                df.getOWLObjectSomeValuesFrom(
                                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                                        df.getOWLClass(ONTOLOGY_IRI, "GrayScale"))),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "Leo")),
                                df.getOWLClassAssertionAxiom(
                                        df.getOWLObjectIntersectionOf(
                                                df.getOWLClass(ONTOLOGY_IRI, "Pet"),
                                                df.getOWLObjectSomeValuesFrom(
                                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                                        df.getOWLClass(ONTOLOGY_IRI, "Black"))),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "Leo")),
                                df.getOWLClassAssertionAxiom(df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                        df.getOWLObjectSomeValuesFrom(
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                                df.getOWLClass(ONTOLOGY_IRI, "Black"))),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "Leo"))),
                        df.getOWLClassAssertionAxiom(
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Cat"),
                                        df.getOWLObjectSomeValuesFrom(
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                                df.getOWLClass(ONTOLOGY_IRI, "Black"))),
                                df.getOWLNamedIndividual(ONTOLOGY_IRI, "Leo"))));
    }

    @ParameterizedTest
    @MethodSource("expectedWeakening")
    public void weakenAxiom(final Set<OWLAxiom> expected, final OWLAxiom axiom) {
        assertEquals(expected, axiomWeakener.weakerAxioms(axiom).collect(Collectors.toSet()));
    }

    @Test
    public void allWeakerAxiomsAreEntailed() {
        ontology.logicalAxioms().forEach(strongAxiom -> {
            axiomWeakener.weakerAxioms(strongAxiom).forEach(weakAxiom -> {
                assertTrue(ontology.isEntailed(weakAxiom));
            });
        });
    }
}
