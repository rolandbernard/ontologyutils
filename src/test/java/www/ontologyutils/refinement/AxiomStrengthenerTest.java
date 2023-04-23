package www.ontologyutils.refinement;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.Ontology;

public class AxiomStrengthenerTest {
    private static final String ONTOLOGY_IRI = "http://www.semanticweb.org/ontologies/dl2017_example#";

    private final Ontology ontology;
    private final AxiomStrengthener axiomStrengthener;

    public AxiomStrengthenerTest() {
        final var path = RoleCoverTest.class.getResource("../catsandnumbers.owl").getFile();
        ontology = Ontology.loadOntology(path);
        axiomStrengthener = new AxiomStrengthener(ontology);
    }

    private static Stream<Arguments> expectedStrengthening() {
        final var df = Ontology.getDefaultDataFactory();
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
    public void strengthenAxiom(final Set<OWLAxiom> expected, final OWLAxiom axiom) {
        assertEquals(expected, axiomStrengthener.strongerAxioms(axiom).collect(Collectors.toSet()));
    }

    @Test
    public void allStrongAxiomsEntailWeakerAxioms() {
        ontology.axioms(AxiomStrengthener.SUPPORTED_AXIOM_TYPES).forEach(weakAxiom -> {
            try (final var copy = ontology.clone()) {
                copy.removeAxioms(weakAxiom);
                try (final var axiomStrengthener = new AxiomStrengthener(copy)) {
                    axiomStrengthener.strongerAxioms(weakAxiom).forEach(strongAxiom -> {
                        try (final var copy2 = copy.clone()) {
                            copy2.addAxioms(strongAxiom);
                            assertTrue(!copy2.isConsistent() || copy2.isEntailed(weakAxiom));
                        }
                    });
                }
            }
        });
    }
}
