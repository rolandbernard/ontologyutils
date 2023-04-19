package www.ontologyutils.normalization;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.Ontology;

public class ABoxNormalizationTest {
    private static Stream<Arguments> testAxioms() {
        final var df = Ontology.getDefaultDataFactory();
        return Stream.of(
                Arguments.of(
                        Set.of(
                                df.getOWLDifferentIndividualsAxiom(df.getOWLNamedIndividual("A"),
                                        df.getOWLNamedIndividual("B"), df.getOWLNamedIndividual("C"),
                                        df.getOWLNamedIndividual("D"), df.getOWLNamedIndividual("E"),
                                        df.getOWLNamedIndividual("F")))),
                Arguments.of(
                        Set.of(
                                df.getOWLDifferentIndividualsAxiom(df.getOWLNamedIndividual("A"),
                                        df.getOWLNamedIndividual("B"), df.getOWLNamedIndividual("C")),
                                df.getOWLDifferentIndividualsAxiom(df.getOWLNamedIndividual("D"),
                                        df.getOWLNamedIndividual("E"), df.getOWLNamedIndividual("F")))),
                Arguments.of(
                        Set.of(
                                df.getOWLDifferentIndividualsAxiom(df.getOWLNamedIndividual("A"),
                                        df.getOWLNamedIndividual("B")),
                                df.getOWLDifferentIndividualsAxiom(df.getOWLNamedIndividual("C"),
                                        df.getOWLNamedIndividual("D")),
                                df.getOWLDifferentIndividualsAxiom(df.getOWLNamedIndividual("E"),
                                        df.getOWLNamedIndividual("F")))),
                Arguments.of(
                        Set.of(
                                df.getOWLSameIndividualAxiom(df.getOWLNamedIndividual("A"),
                                        df.getOWLNamedIndividual("B")),
                                df.getOWLSameIndividualAxiom(df.getOWLNamedIndividual("C"),
                                        df.getOWLNamedIndividual("D")),
                                df.getOWLDifferentIndividualsAxiom(df.getOWLNamedIndividual("E"),
                                        df.getOWLNamedIndividual("F")))),
                Arguments.of(
                        Set.of(
                                df.getOWLDifferentIndividualsAxiom(df.getOWLNamedIndividual("A"),
                                        df.getOWLNamedIndividual("B"), df.getOWLNamedIndividual("C")),
                                df.getOWLSameIndividualAxiom(df.getOWLNamedIndividual("D"),
                                        df.getOWLNamedIndividual("E"), df.getOWLNamedIndividual("F")))),
                Arguments.of(
                        Set.of(
                                df.getOWLSameIndividualAxiom(df.getOWLNamedIndividual("A"),
                                        df.getOWLNamedIndividual("B"), df.getOWLNamedIndividual("C"),
                                        df.getOWLNamedIndividual("D"), df.getOWLNamedIndividual("E"),
                                        df.getOWLNamedIndividual("F")))));
    }

    @ParameterizedTest
    @MethodSource("testAxioms")
    public void allTBoxAxiomsAreSubclassOf(final Set<OWLAxiom> axioms) throws OWLOntologyCreationException {
        try (final var ontology = Ontology.withAxioms(axioms)) {
            final var normalization = new ABoxNormalization();
            normalization.apply(ontology);
            ontology.axioms(AxiomType.SAME_INDIVIDUAL)
                    .forEach(axiom -> assertEquals(2, ((OWLSameIndividualAxiom) axiom).getIndividuals().size()));
            ontology.axioms(AxiomType.DIFFERENT_INDIVIDUALS)
                    .forEach(axiom -> assertEquals(2, ((OWLDifferentIndividualsAxiom) axiom).getIndividuals().size()));
        }
    }

    @ParameterizedTest
    @MethodSource("testAxioms")
    public void normalizedOntologyIsEquivalent(final Set<OWLAxiom> axioms) throws OWLOntologyCreationException {
        try (final var originalOntology = Ontology.withAxioms(axioms)) {
            try (final var normalizedOntology = Ontology.withAxioms(axioms)) {
                final var normalization = new ABoxNormalization();
                normalization.apply(normalizedOntology);
                normalizedOntology.axioms()
                        .forEach(normalizedAxiom -> assertTrue(originalOntology.isEntailed(normalizedAxiom)));
                originalOntology.axioms()
                        .forEach(originalAxiom -> assertTrue(normalizedOntology.isEntailed(originalAxiom)));
                normalizedOntology.close();
                originalOntology.close();
            }
        }
    }
}
