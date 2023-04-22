package www.ontologyutils.normalization;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.Ontology;

public class ConceptNormalizationTest {
    private static Stream<Arguments> testAxioms() {
        final var df = Ontology.getDefaultDataFactory();
        return Stream.of(
                Arguments.of(
                        Set.of(
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLClass("A"),
                                        df.getOWLObjectIntersectionOf(
                                                df.getOWLClass("B"),
                                                df.getOWLClass("C"),
                                                df.getOWLClass("D"),
                                                df.getOWLClass("E"))),
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLObjectExactCardinality(15, df.getOWLObjectProperty("aaa"),
                                                df.getOWLClass("A")),
                                        df.getOWLObjectIntersectionOf(
                                                df.getOWLClass("B"),
                                                df.getOWLClass("C").getObjectComplementOf(),
                                                df.getOWLClass("D"),
                                                df.getOWLObjectUnionOf(
                                                        df.getOWLClass("E"),
                                                        df.getOWLClass("F"),
                                                        df.getOWLClass("G"))))

                        )),
                Arguments.of(
                        Set.of(
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLClass("A"),
                                        df.getOWLObjectUnionOf(
                                                df.getOWLClass("B"),
                                                df.getOWLClass("C"),
                                                df.getOWLClass("D"),
                                                df.getOWLObjectHasValue(df.getOWLObjectProperty("aaa"),
                                                        df.getOWLNamedIndividual("iii")))))),
                Arguments.of(
                        Set.of(
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLClass("A"),
                                        df.getOWLObjectUnionOf(
                                                df.getOWLClass("B"),
                                                df.getOWLClass("C"),
                                                df.getOWLClass("D"),
                                                df.getOWLObjectHasValue(df.getOWLObjectProperty("aaa"),
                                                        df.getOWLNamedIndividual("iii")))),
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLObjectIntersectionOf(
                                                df.getOWLClass("B"),
                                                df.getOWLClass("C"),
                                                df.getOWLClass("D"),
                                                df.getOWLClass("E")),
                                        df.getOWLClass("A")),
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLObjectExactCardinality(15, df.getOWLObjectProperty("aaa"),
                                                df.getOWLClass("A")),
                                        df.getOWLObjectIntersectionOf(
                                                df.getOWLClass("B"),
                                                df.getOWLObjectUnionOf(
                                                        df.getOWLClass("F"),
                                                        df.getOWLClass("E"),
                                                        df.getOWLClass("C"),
                                                        df.getOWLClass("D"),
                                                        df.getOWLClass("G")))))));
    }

    @ParameterizedTest
    @MethodSource("testAxioms")
    public void allConceptsAxiomsAreInSroiq(final Set<OWLAxiom> axioms) throws OWLOntologyCreationException {
        try (final var ontology = Ontology.withAxioms(axioms)) {
            final var normalization = new ConceptNormalization();
            normalization.apply(ontology);
            ontology.subConcepts().forEach(ce -> {
                final var type = ce.getClassExpressionType();
                assertTrue(
                        type == ClassExpressionType.OWL_CLASS
                                || type == ClassExpressionType.OBJECT_COMPLEMENT_OF
                                || type == ClassExpressionType.OBJECT_INTERSECTION_OF
                                || type == ClassExpressionType.OBJECT_UNION_OF
                                || type == ClassExpressionType.OBJECT_ALL_VALUES_FROM
                                || type == ClassExpressionType.OBJECT_SOME_VALUES_FROM
                                || type == ClassExpressionType.OBJECT_HAS_SELF
                                || type == ClassExpressionType.OBJECT_ONE_OF
                                || type == ClassExpressionType.OBJECT_MIN_CARDINALITY
                                || type == ClassExpressionType.OBJECT_MAX_CARDINALITY);
            });
        }
    }

    @ParameterizedTest
    @MethodSource("testAxioms")
    public void unionAndIntersectionAreInBinaryOperations(final Set<OWLAxiom> axioms)
            throws OWLOntologyCreationException {
        try (final var ontology = Ontology.withAxioms(axioms)) {
            final var normalization = new ConceptNormalization(true);
            normalization.apply(ontology);
            ontology.subConcepts()
                    .filter(ce -> ce.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF
                            || ce.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF)
                    .forEach(ce -> assertEquals(2, ((OWLNaryBooleanClassExpression) ce).getOperandsAsList().size()));
        }
    }

    @ParameterizedTest
    @MethodSource("testAxioms")
    public void normalizedOntologyIsEquivalent(final Set<OWLAxiom> axioms) throws OWLOntologyCreationException {
        try (final var originalOntology = Ontology.withAxioms(axioms)) {
            try (final var normalizedOntology = Ontology.withAxioms(axioms)) {
                final var normalization = new ConceptNormalization();
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

    @ParameterizedTest
    @MethodSource("testAxioms")
    public void normalizedOntologyIsEquivalentBinary(final Set<OWLAxiom> axioms) throws OWLOntologyCreationException {
        try (final var originalOntology = Ontology.withAxioms(axioms)) {
            try (final var normalizedOntology = Ontology.withAxioms(axioms)) {
                final var normalization = new ConceptNormalization(true);
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
