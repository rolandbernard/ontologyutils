package www.ontologyutils.refinement;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.Ontology;

public class SroiqRefinementOperatorTest {
    private static final String ONTOLOGY_IRI = "http://www.semanticweb.org/roland/ontologies/2023/3/untitled/";

    private final Ontology ontology;
    private final Covers covers;
    static RefinementOperator generalization;
    static RefinementOperator specialization;

    public SroiqRefinementOperatorTest() {
        final var path = RoleCoverTest.class.getResource("../sroiq-tests.owl").getFile();
        ontology = Ontology.loadOntology(path);
        covers = new Covers(ontology, ontology.simpleRoles().collect(Collectors.toSet()));
        final var upCover = covers.upCover().cached();
        final var downCover = covers.downCover().cached();
        generalization = new RefinementOperator(upCover, downCover);
        specialization = new RefinementOperator(downCover, upCover);
    }

    private static Stream<Arguments> expectedGeneralization() {
        final var df = Ontology.getDefaultDataFactory();
        return Stream.of(
                Arguments.of(
                        Set.of(
                                df.getOWLClass(ONTOLOGY_IRI, "Parent"),
                                df.getOWLClass(ONTOLOGY_IRI, "Person")),
                        df.getOWLClass(ONTOLOGY_IRI, "Parent")),
                Arguments.of(
                        Set.of(
                                df.getOWLClass(ONTOLOGY_IRI, "Mother").getObjectComplementOf(),
                                df.getOWLClass(ONTOLOGY_IRI, "Father").getObjectComplementOf(),
                                df.getOWLClass(ONTOLOGY_IRI, "Parent").getObjectComplementOf(),
                                df.getOWLObjectSomeValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Person")).getObjectComplementOf(),
                                df.getOWLObjectHasSelf(df.getOWLObjectProperty(ONTOLOGY_IRI, "knows")),
                                df.getOWLThing()),
                        df.getOWLClass(ONTOLOGY_IRI, "Parent").getObjectComplementOf()),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectUnionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Person"), df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectUnionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent"), df.getOWLThing()),
                                df.getOWLObjectUnionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent"),
                                        df.getOWLObjectHasSelf(df.getOWLObjectProperty(ONTOLOGY_IRI, "knows"))),
                                df.getOWLClass(ONTOLOGY_IRI, "Person"),
                                df.getOWLObjectUnionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent"), df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectHasSelf(df.getOWLObjectProperty(ONTOLOGY_IRI, "knows")),
                                df.getOWLThing()),
                        df.getOWLObjectUnionOf(
                                df.getOWLClass(ONTOLOGY_IRI, "Parent"), df.getOWLClass(ONTOLOGY_IRI, "Person"))),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent"), df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Person"), df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent"), df.getOWLThing()),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent"),
                                        df.getOWLObjectHasSelf(df.getOWLObjectProperty(ONTOLOGY_IRI, "knows"))),
                                df.getOWLClass(ONTOLOGY_IRI, "Person"),
                                df.getOWLClass(ONTOLOGY_IRI, "Parent")),
                        df.getOWLObjectIntersectionOf(
                                df.getOWLClass(ONTOLOGY_IRI, "Parent"), df.getOWLClass(ONTOLOGY_IRI, "Person"))),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectAllValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent")),
                                df.getOWLObjectAllValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectAllValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "motherOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent")),
                                df.getOWLObjectAllValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "fatherOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent")),
                                df.getOWLObjectAllValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf").getInverseProperty(),
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent")),
                                df.getOWLObjectHasSelf(df.getOWLObjectProperty(ONTOLOGY_IRI, "knows")),
                                df.getOWLThing()),
                        df.getOWLObjectAllValuesFrom(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                df.getOWLClass(ONTOLOGY_IRI, "Parent"))),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectSomeValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent")),
                                df.getOWLObjectSomeValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectSomeValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "ancestorOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent")),
                                df.getOWLObjectSomeValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf").getInverseProperty(),
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent"))),
                        df.getOWLObjectSomeValuesFrom(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                df.getOWLClass(ONTOLOGY_IRI, "Parent"))),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectMinCardinality(4,
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectMinCardinality(3,
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectMinCardinality(4,
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf").getInverseProperty(),
                                        df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectMinCardinality(4,
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf"),
                                        df.getOWLThing()),
                                df.getOWLObjectMinCardinality(4,
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf"),
                                        df.getOWLObjectHasSelf(df.getOWLObjectProperty(ONTOLOGY_IRI, "knows"))),
                                df.getOWLObjectMinCardinality(4,
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "ancestorOf").getInverseProperty(),
                                        df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectHasSelf(df.getOWLObjectProperty(ONTOLOGY_IRI, "knows")),
                                df.getOWLThing()),
                        df.getOWLObjectMinCardinality(4,
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf"),
                                df.getOWLClass(ONTOLOGY_IRI, "Person"))),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectMaxCardinality(4,
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectMaxCardinality(5,
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectMaxCardinality(4,
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent")),
                                df.getOWLObjectMaxCardinality(4,
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf").getInverseProperty(),
                                        df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectMaxCardinality(4,
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "fatherOf").getInverseProperty(),
                                        df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectMaxCardinality(4,
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "motherOf").getInverseProperty(),
                                        df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectHasSelf(df.getOWLObjectProperty(ONTOLOGY_IRI, "knows")),
                                df.getOWLThing()),
                        df.getOWLObjectMaxCardinality(4,
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf"),
                                df.getOWLClass(ONTOLOGY_IRI, "Person"))),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectHasSelf(df.getOWLObjectProperty(ONTOLOGY_IRI, "knows")),
                                df.getOWLThing()),
                        df.getOWLObjectHasSelf(df.getOWLObjectProperty(ONTOLOGY_IRI, "knows"))));
    }

    @ParameterizedTest
    @MethodSource("expectedGeneralization")
    public void generalize(final Set<OWLClassExpression> expected, final OWLClassExpression concept) {
        assertEquals(expected, generalization.refine(concept).collect(Collectors.toSet()));
    }

    private static Stream<Arguments> expectedSpecialization() {
        final var df = Ontology.getDefaultDataFactory();
        return Stream.of(
                Arguments.of(
                        Set.of(
                                df.getOWLClass(ONTOLOGY_IRI, "Mother"),
                                df.getOWLClass(ONTOLOGY_IRI, "Father"),
                                df.getOWLClass(ONTOLOGY_IRI, "Parent"),
                                df.getOWLObjectSomeValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Person"))),
                        df.getOWLClass(ONTOLOGY_IRI, "Parent")),
                Arguments.of(
                        Set.of(
                                df.getOWLClass(ONTOLOGY_IRI, "Parent").getObjectComplementOf(),
                                df.getOWLClass(ONTOLOGY_IRI, "Person").getObjectComplementOf(),
                                df.getOWLNothing()),
                        df.getOWLClass(ONTOLOGY_IRI, "Parent").getObjectComplementOf()),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectUnionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent"), df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectUnionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Mother"), df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectUnionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Father"), df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectUnionOf(
                                        df.getOWLObjectSomeValuesFrom(
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                                df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                        df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectUnionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent"), df.getOWLClass(ONTOLOGY_IRI, "Parent")),
                                df.getOWLClass(ONTOLOGY_IRI, "Parent"),
                                df.getOWLClass(ONTOLOGY_IRI, "Person")),
                        df.getOWLObjectUnionOf(
                                df.getOWLClass(ONTOLOGY_IRI, "Parent"), df.getOWLClass(ONTOLOGY_IRI, "Person"))),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent"), df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Mother"), df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Father"), df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLObjectSomeValuesFrom(
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                                df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                        df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent"), df.getOWLClass(ONTOLOGY_IRI, "Parent")),
                                df.getOWLClass(ONTOLOGY_IRI, "Mother"),
                                df.getOWLClass(ONTOLOGY_IRI, "Father"),
                                df.getOWLClass(ONTOLOGY_IRI, "Parent"),
                                df.getOWLObjectSomeValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Person"))),
                        df.getOWLObjectIntersectionOf(
                                df.getOWLClass(ONTOLOGY_IRI, "Parent"), df.getOWLClass(ONTOLOGY_IRI, "Person"))),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectAllValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent")),
                                df.getOWLObjectAllValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Mother")),
                                df.getOWLObjectAllValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Father")),
                                df.getOWLObjectAllValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "ancestorOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent")),
                                df.getOWLObjectAllValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf").getInverseProperty(),
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent")),
                                df.getOWLObjectAllValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                        df.getOWLObjectSomeValuesFrom(
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                                df.getOWLClass(ONTOLOGY_IRI, "Person"))),
                                df.getOWLNothing()),
                        df.getOWLObjectAllValuesFrom(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                df.getOWLClass(ONTOLOGY_IRI, "Parent"))),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectSomeValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent")),
                                df.getOWLObjectSomeValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Mother")),
                                df.getOWLObjectSomeValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Father")),
                                df.getOWLObjectSomeValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "motherOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent")),
                                df.getOWLObjectSomeValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "fatherOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent")),
                                df.getOWLObjectSomeValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf").getInverseProperty(),
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent")),
                                df.getOWLObjectSomeValuesFrom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                        df.getOWLObjectSomeValuesFrom(
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                                df.getOWLClass(ONTOLOGY_IRI, "Person"))),
                                df.getOWLNothing()),
                        df.getOWLObjectSomeValuesFrom(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                df.getOWLClass(ONTOLOGY_IRI, "Parent"))),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectMinCardinality(4,
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectMinCardinality(5,
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectMinCardinality(4,
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf").getInverseProperty(),
                                        df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectMinCardinality(4,
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "fatherOf").getInverseProperty(),
                                        df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectMinCardinality(4,
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "motherOf").getInverseProperty(),
                                        df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectMinCardinality(4,
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent")),
                                df.getOWLNothing()),
                        df.getOWLObjectMinCardinality(4,
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf"),
                                df.getOWLClass(ONTOLOGY_IRI, "Person"))),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectMaxCardinality(4,
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectMaxCardinality(3,
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectMaxCardinality(4,
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf").getInverseProperty(),
                                        df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectMaxCardinality(4,
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "ancestorOf").getInverseProperty(),
                                        df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLObjectMaxCardinality(4,
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf"),
                                        df.getOWLObjectHasSelf(df.getOWLObjectProperty(ONTOLOGY_IRI, "knows"))),
                                df.getOWLObjectMaxCardinality(4,
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf"),
                                        df.getOWLThing()),
                                df.getOWLNothing()),
                        df.getOWLObjectMaxCardinality(4,
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf"),
                                df.getOWLClass(ONTOLOGY_IRI, "Person"))),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectHasSelf(df.getOWLObjectProperty(ONTOLOGY_IRI, "knows")),
                                df.getOWLObjectHasSelf(df.getOWLObjectProperty(ONTOLOGY_IRI, "ancestorOf")),
                                df.getOWLClass(ONTOLOGY_IRI, "Person"),
                                df.getOWLClass(ONTOLOGY_IRI, "Grandparent"),
                                df.getOWLThing()),
                        df.getOWLObjectHasSelf(df.getOWLObjectProperty(ONTOLOGY_IRI, "knows"))));
    }

    @ParameterizedTest
    @MethodSource("expectedSpecialization")
    public void specialize(final Set<OWLClassExpression> expected, final OWLClassExpression concept) {
        assertEquals(expected, specialization.refine(concept).collect(Collectors.toSet()));
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
