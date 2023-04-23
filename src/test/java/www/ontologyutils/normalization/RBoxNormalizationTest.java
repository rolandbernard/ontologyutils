package www.ontologyutils.normalization;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.Ontology;

public class RBoxNormalizationTest {
    private static Stream<Arguments> testAxioms() {
        final var df = Ontology.getDefaultDataFactory();
        return Stream.of(
                Arguments.of(
                        Set.of(
                                df.getOWLEquivalentObjectPropertiesAxiom(df.getOWLObjectProperty("A"),
                                        df.getOWLObjectProperty("B"), df.getOWLObjectProperty("C"),
                                        df.getOWLObjectProperty("D"), df.getOWLObjectProperty("E"),
                                        df.getOWLObjectProperty("F")))),
                Arguments.of(
                        Set.of(
                                df.getOWLDisjointObjectPropertiesAxiom(df.getOWLObjectProperty("A"),
                                        df.getOWLObjectProperty("B"), df.getOWLObjectProperty("C")),
                                df.getOWLInverseObjectPropertiesAxiom(df.getOWLObjectProperty("D"),
                                        df.getOWLObjectProperty("E")))),
                Arguments.of(
                        Set.of(
                                df.getOWLSymmetricObjectPropertyAxiom(df.getOWLObjectProperty("A")),
                                df.getOWLAsymmetricObjectPropertyAxiom(df.getOWLObjectProperty("B")),
                                df.getOWLTransitiveObjectPropertyAxiom(df.getOWLObjectProperty("C")),
                                df.getOWLIrreflexiveObjectPropertyAxiom(df.getOWLObjectProperty("D")),
                                df.getOWLReflexiveObjectPropertyAxiom(df.getOWLObjectProperty("E")))),
                Arguments.of(
                        Set.of(
                                df.getOWLSubObjectPropertyOfAxiom(df.getOWLObjectProperty("A"),
                                        df.getOWLObjectProperty("B")),
                                df.getOWLSubPropertyChainOfAxiom(
                                        List.of(df.getOWLObjectProperty("A"), df.getOWLObjectProperty("E")),
                                        df.getOWLObjectProperty("B")),
                                df.getOWLSubObjectPropertyOfAxiom(df.getOWLObjectProperty("B"),
                                        df.getOWLObjectProperty("C")),
                                df.getOWLSubObjectPropertyOfAxiom(df.getOWLObjectProperty("A"),
                                        df.getOWLObjectProperty("C")),
                                df.getOWLSubObjectPropertyOfAxiom(df.getOWLObjectProperty("C"),
                                        df.getOWLObjectProperty("D")))));
    }

    @ParameterizedTest
    @MethodSource("testAxioms")
    public void allRBoxAxiomsAreInSroiq(final Set<OWLAxiom> axioms) throws OWLOntologyCreationException {
        try (final var ontology = Ontology.withAxioms(axioms)) {
            final var normalization = new RBoxNormalization();
            normalization.apply(ontology);
            ontology.rboxAxioms().forEach(axiom -> {
                assertTrue(axiom.isOfType(AxiomType.SUB_OBJECT_PROPERTY, AxiomType.SUB_PROPERTY_CHAIN_OF,
                        AxiomType.DISJOINT_OBJECT_PROPERTIES));
            });
            ontology.axioms(AxiomType.DISJOINT_OBJECT_PROPERTIES)
                    .forEach(axiom -> assertEquals(2,
                            ((OWLDisjointObjectPropertiesAxiom) axiom).getProperties().size()));
        }
    }

    @ParameterizedTest
    @MethodSource("testAxioms")
    public void allRBoxAxiomsAreInSroiqFull(final Set<OWLAxiom> axioms) throws OWLOntologyCreationException {
        try (final var ontology = Ontology.withAxioms(axioms)) {
            final var normalization = new RBoxNormalization(true);
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
            try (final var normalizedOntology = originalOntology.cloneWithJFact()) {
                final var normalization = new RBoxNormalization();
                normalization.apply(normalizedOntology);
                originalOntology.axioms()
                        .forEach(originalAxiom -> assertTrue(normalizedOntology.isEntailed(originalAxiom)));
                normalizedOntology.close();
                originalOntology.close();
            }
        }
    }

    @ParameterizedTest
    @MethodSource("testAxioms")
    public void normalizedOntologyIsEquivalentFull(final Set<OWLAxiom> axioms) throws OWLOntologyCreationException {
        try (final var originalOntology = Ontology.withAxioms(axioms)) {
            try (final var normalizedOntology = originalOntology.cloneWithJFact()) {
                final var normalization = new RBoxNormalization(true);
                normalization.apply(normalizedOntology);
                originalOntology.axioms()
                        .forEach(originalAxiom -> assertTrue(normalizedOntology.isEntailed(originalAxiom)));
                normalizedOntology.close();
                originalOntology.close();
            }
        }
    }
}
