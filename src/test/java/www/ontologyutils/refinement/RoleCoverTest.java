package www.ontologyutils.refinement;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.*;

public class RoleCoverTest {
    private static final String ONTOLOGY_IRI = "http://www.semanticweb.org/roland/ontologies/2023/3/untitled/";

    private Ontology ontology;
    private Covers covers;

    @BeforeEach
    public void setup() {
        var path = CoverTest.class.getResource("/alcri/sroiq-tests.owl").getFile();
        ontology = Ontology.loadOntology(path);
        var subConcepts = Utils.toSet(ontology.subConcepts());
        var simpleRoles = Utils.toSet(ontology.simpleRoles());
        covers = new Covers(ontology, subConcepts, simpleRoles);
    }

    @AfterEach
    public void teardown() {
        ontology.close();
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
        assertEquals(expected, Utils.toSet(covers.upCover(concept)));
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
        assertEquals(expected, Utils.toSet(covers.downCover(concept)));
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
