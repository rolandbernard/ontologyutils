package www.ontologyutils.refinement;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.Ontology;

public class RoleCoverTest {
    private static final String ONTOLOGY_IRI = "http://www.semanticweb.org/roland/ontologies/2023/3/untitled/";

    private Ontology ontology;
    private Covers covers;

    public RoleCoverTest() {
        var path = CoverTest.class.getResource("/sroiq-tests.owl").getFile();
        ontology = Ontology.loadOntology(path);
        covers = new Covers(ontology, ontology.simpleRoles().collect(Collectors.toSet()));
    }

    private static Stream<Arguments> expectedUpCover() {
        var df = Ontology.getDefaultDataFactory();
        return Stream.of(
                Arguments.of(
                        Set.of(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "fatherOf"),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf").getInverseProperty()),
                        df.getOWLObjectProperty(ONTOLOGY_IRI, "fatherOf")),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf"),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf").getInverseProperty(),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "ancestorOf").getInverseProperty()),
                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf")),
                Arguments.of(
                        Set.of(),
                        df.getOWLObjectProperty(ONTOLOGY_IRI, "grandparentOf")),
                Arguments.of(
                        Set.of(),
                        df.getOWLTopObjectProperty()),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "fatherOf")
                                        .getInverseProperty(),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "fatherOf"),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "motherOf")
                                        .getInverseProperty(),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "motherOf")),
                        df.getOWLBottomObjectProperty()),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "fatherOf").getInverseProperty(),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf").getInverseProperty(),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf")),
                        df.getOWLObjectProperty(ONTOLOGY_IRI, "fatherOf").getInverseProperty()),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf").getInverseProperty(),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "ancestorOf")),
                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf").getInverseProperty()),
                Arguments.of(
                        Set.of(),
                        df.getOWLObjectProperty(ONTOLOGY_IRI, "grandparentOf").getInverseProperty()),
                Arguments.of(
                        Set.of(),
                        df.getOWLTopObjectProperty().getInverseProperty()),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "fatherOf")
                                        .getInverseProperty(),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "fatherOf"),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "motherOf")
                                        .getInverseProperty(),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "motherOf")),
                        df.getOWLBottomObjectProperty().getInverseProperty()));
    }

    @ParameterizedTest
    @MethodSource("expectedUpCover")
    public void upCover(Set<OWLObjectPropertyExpression> expected,
            OWLObjectPropertyExpression concept) {
        assertEquals(expected, covers.upCover(concept).collect(Collectors.toSet()));
    }

    private static Stream<Arguments> expectedDownCover() {
        var df = Ontology.getDefaultDataFactory();
        return Stream.of(
                Arguments.of(
                        Set.of(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "fatherOf")),
                        df.getOWLObjectProperty(ONTOLOGY_IRI, "fatherOf")),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf"),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf").getInverseProperty(),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "fatherOf").getInverseProperty(),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "motherOf").getInverseProperty()),
                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf")),
                Arguments.of(
                        Set.of(),
                        df.getOWLObjectProperty(ONTOLOGY_IRI, "grandparentOf")),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "knows").getInverseProperty(),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "knows")),
                        df.getOWLTopObjectProperty()),
                Arguments.of(
                        Set.of(),
                        df.getOWLBottomObjectProperty()),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "fatherOf").getInverseProperty()),
                        df.getOWLObjectProperty(ONTOLOGY_IRI, "fatherOf").getInverseProperty()),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf").getInverseProperty(),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "fatherOf"),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "motherOf")),
                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf").getInverseProperty()),
                Arguments.of(
                        Set.of(),
                        df.getOWLObjectProperty(ONTOLOGY_IRI, "grandparentOf").getInverseProperty()),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "knows").getInverseProperty(),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "knows")),
                        df.getOWLTopObjectProperty()),
                Arguments.of(
                        Set.of(),
                        df.getOWLBottomObjectProperty()));
    }

    @ParameterizedTest
    @MethodSource("expectedDownCover")
    public void downCover(Set<OWLObjectPropertyExpression> expected,
            OWLObjectPropertyExpression concept) {
        assertEquals(expected, covers.downCover(concept).collect(Collectors.toSet()));
    }

    @Test
    public void allUpCover() {
        ontology.rolesInSignature()
                .flatMap(role -> Stream.of(role, role.getInverseProperty()))
                .map(covers::upCover).forEach(Stream::count);
    }

    @Test
    public void allDownCover() {
        ontology.rolesInSignature()
                .flatMap(role -> Stream.of(role, role.getInverseProperty()))
                .map(covers::downCover).forEach(Stream::count);
    }
}
