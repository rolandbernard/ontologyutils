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
    private static final String ONTOLOGY_IRI = "http://www.semanticweb.org/ontologies/dl2017_example#";

    private final Ontology ontology;
    private final RoleCovers covers;

    public RoleCoverTest() {
        final var path = CoverTest.class.getResource("../catsandnumbers.owl").getFile();
        ontology = Ontology.loadOntology(path);
        covers = new RoleCovers(ontology);
    }

    private static Stream<Arguments> expectedUpCover() {
        final var df = Ontology.getDefaultDataFactory();
        return Stream.of(
                Arguments.of(
                        Set.of(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasQuality"),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour")),
                        df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour")),
                Arguments.of(
                        Set.of(df.getOWLObjectProperty(ONTOLOGY_IRI, "hasQuality")),
                        df.getOWLObjectProperty(ONTOLOGY_IRI, "hasQuality")),
                Arguments.of(
                        Set.of(),
                        df.getOWLTopObjectProperty()),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour").getInverseProperty(),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour")),
                        df.getOWLBottomObjectProperty()),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasQuality").getInverseProperty(),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour").getInverseProperty()),
                        df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour").getInverseProperty()),
                Arguments.of(
                        Set.of(df.getOWLObjectProperty(ONTOLOGY_IRI, "hasQuality").getInverseProperty()),
                        df.getOWLObjectProperty(ONTOLOGY_IRI, "hasQuality").getInverseProperty()),
                Arguments.of(
                        Set.of(),
                        df.getOWLTopObjectProperty().getInverseProperty()),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour").getInverseProperty(),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour")),
                        df.getOWLBottomObjectProperty().getInverseProperty()));
    }

    @ParameterizedTest
    @MethodSource("expectedUpCover")
    public void upCover(final Set<OWLObjectPropertyExpression> expected,
            final OWLObjectPropertyExpression concept) {
        assertEquals(expected, covers.upCover(concept).collect(Collectors.toSet()));
    }

    private static Stream<Arguments> expectedDownCover() {
        final var df = Ontology.getDefaultDataFactory();
        return Stream.of(
                Arguments.of(
                        Set.of(df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour")),
                        df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour")),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour"),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasQuality")),
                        df.getOWLObjectProperty(ONTOLOGY_IRI, "hasQuality")),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasQuality").getInverseProperty(),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasQuality")),
                        df.getOWLTopObjectProperty()),
                Arguments.of(
                        Set.of(),
                        df.getOWLBottomObjectProperty()),
                Arguments.of(
                        Set.of(df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour").getInverseProperty()),
                        df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour").getInverseProperty()),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasColour").getInverseProperty(),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasQuality").getInverseProperty()),
                        df.getOWLObjectProperty(ONTOLOGY_IRI, "hasQuality").getInverseProperty()),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasQuality"),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "hasQuality").getInverseProperty()),
                        df.getOWLTopObjectProperty().getInverseProperty()),
                Arguments.of(
                        Set.of(),
                        df.getOWLBottomObjectProperty().getInverseProperty()));
    }

    @ParameterizedTest
    @MethodSource("expectedDownCover")
    public void downCover(final Set<OWLObjectPropertyExpression> expected,
            final OWLObjectPropertyExpression concept) {
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
