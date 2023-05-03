package www.ontologyutils.refinement;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.Ontology;

public class SroiqAxiomWeakenerTest {
    private static final String ONTOLOGY_IRI = "http://www.semanticweb.org/roland/ontologies/2023/3/untitled/";

    private Ontology ontology;
    private AxiomWeakener axiomWeakener;

    public SroiqAxiomWeakenerTest() {
        var path = RoleCoverTest.class.getResource("../sroiq-tests.owl").getFile();
        ontology = Ontology.loadOntology(path);
        axiomWeakener = new AxiomWeakener(ontology, ontology);
    }

    private static Stream<Arguments> expectedWeakening() {
        var df = Ontology.getDefaultDataFactory();
        return Stream.of(
                Arguments.of(
                        Set.of(
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLObjectHasSelf(df.getOWLObjectProperty(ONTOLOGY_IRI, "knows")),
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent").getObjectComplementOf()),
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLObjectHasSelf(df.getOWLObjectProperty(ONTOLOGY_IRI, "knows")),
                                        df.getOWLClass(ONTOLOGY_IRI, "Mother").getObjectComplementOf()),
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLObjectHasSelf(df.getOWLObjectProperty(ONTOLOGY_IRI, "knows")),
                                        df.getOWLClass(ONTOLOGY_IRI, "Father").getObjectComplementOf()),
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLObjectHasSelf(df.getOWLObjectProperty(ONTOLOGY_IRI, "knows")),
                                        df.getOWLObjectSomeValuesFrom(df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                                df.getOWLClass(ONTOLOGY_IRI, "Person")).getObjectComplementOf()),
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLObjectHasSelf(df.getOWLObjectProperty(ONTOLOGY_IRI, "knows")),
                                        df.getOWLThing()),
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLObjectHasSelf(df.getOWLObjectProperty(ONTOLOGY_IRI, "knows")),
                                        df.getOWLObjectHasSelf(df.getOWLObjectProperty(ONTOLOGY_IRI, "knows"))),
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLObjectHasSelf(df.getOWLObjectProperty(ONTOLOGY_IRI, "ancestorOf")),
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent").getObjectComplementOf()),
                                df.getOWLSubClassOfAxiom(df.getOWLClass(ONTOLOGY_IRI, "Person"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent").getObjectComplementOf()),
                                df.getOWLSubClassOfAxiom(df.getOWLClass(ONTOLOGY_IRI, "Grandparent"),
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent").getObjectComplementOf()),
                                df.getOWLSubClassOfAxiom(df.getOWLThing(),
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent").getObjectComplementOf())),
                        df.getOWLSubClassOfAxiom(df.getOWLObjectHasSelf(df.getOWLObjectProperty(ONTOLOGY_IRI, "knows")),
                                df.getOWLClass(ONTOLOGY_IRI, "Parent").getObjectComplementOf())),
                Arguments.of(
                        Set.of(
                                df.getOWLClassAssertionAxiom(
                                        df.getOWLObjectIntersectionOf(
                                                df.getOWLClass(ONTOLOGY_IRI, "Parent"),
                                                df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "Test")),
                                df.getOWLClassAssertionAxiom(
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "Test")),
                                df.getOWLClassAssertionAxiom(
                                        df.getOWLClass(ONTOLOGY_IRI, "Person"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "Test")),
                                df.getOWLClassAssertionAxiom(
                                        df.getOWLObjectIntersectionOf(
                                                df.getOWLClass(ONTOLOGY_IRI, "Person"),
                                                df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "Test")),
                                df.getOWLClassAssertionAxiom(
                                        df.getOWLObjectIntersectionOf(
                                                df.getOWLClass(ONTOLOGY_IRI, "Parent"), df.getOWLThing()),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "Test")),
                                df.getOWLClassAssertionAxiom(
                                        df.getOWLObjectIntersectionOf(
                                                df.getOWLClass(ONTOLOGY_IRI, "Parent"),
                                                df.getOWLObjectHasSelf(df.getOWLObjectProperty(ONTOLOGY_IRI, "knows"))),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "Test"))),
                        df.getOWLClassAssertionAxiom(
                                df.getOWLObjectIntersectionOf(
                                        df.getOWLClass(ONTOLOGY_IRI, "Parent"), df.getOWLClass(ONTOLOGY_IRI, "Person")),
                                df.getOWLNamedIndividual(ONTOLOGY_IRI, "Test"))),
                Arguments.of(
                        Set.of(
                                df.getOWLObjectPropertyAssertionAxiom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "fatherOf"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "B")),
                                df.getOWLObjectPropertyAssertionAxiom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "B")),
                                df.getOWLObjectPropertyAssertionAxiom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf").getInverseProperty(),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "B")),
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLNothing(), df.getOWLThing())),
                        df.getOWLObjectPropertyAssertionAxiom(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "fatherOf"),
                                df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                df.getOWLNamedIndividual(ONTOLOGY_IRI, "B"))),
                Arguments.of(
                        Set.of(
                                df.getOWLNegativeObjectPropertyAssertionAxiom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "B")),
                                df.getOWLNegativeObjectPropertyAssertionAxiom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf").getInverseProperty(),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "B")),
                                df.getOWLNegativeObjectPropertyAssertionAxiom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "motherOf"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "B")),
                                df.getOWLNegativeObjectPropertyAssertionAxiom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "fatherOf"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "B")),
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLNothing(), df.getOWLThing())),
                        df.getOWLNegativeObjectPropertyAssertionAxiom(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                df.getOWLNamedIndividual(ONTOLOGY_IRI, "B"))),
                Arguments.of(
                        Set.of(
                                df.getOWLSameIndividualAxiom(
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "B")),
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLNothing(), df.getOWLThing())),
                        df.getOWLSameIndividualAxiom(
                                df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                df.getOWLNamedIndividual(ONTOLOGY_IRI, "B"))),
                Arguments.of(
                        Set.of(
                                df.getOWLDifferentIndividualsAxiom(
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                        df.getOWLNamedIndividual(ONTOLOGY_IRI, "B")),
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLNothing(), df.getOWLThing())),
                        df.getOWLDifferentIndividualsAxiom(
                                df.getOWLNamedIndividual(ONTOLOGY_IRI, "A"),
                                df.getOWLNamedIndividual(ONTOLOGY_IRI, "B"))),
                Arguments.of(
                        Set.of(
                                df.getOWLSubObjectPropertyOfAxiom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "grandparentOf")),
                                df.getOWLSubObjectPropertyOfAxiom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf").getInverseProperty(),
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "grandparentOf")),
                                df.getOWLSubObjectPropertyOfAxiom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "fatherOf"),
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "grandparentOf")),
                                df.getOWLSubObjectPropertyOfAxiom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "motherOf"),
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "grandparentOf")),
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLNothing(), df.getOWLThing())),
                        df.getOWLSubObjectPropertyOfAxiom(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "grandparentOf"))),
                Arguments.of(
                        Set.of(
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLNothing(), df.getOWLThing())),
                        df.getOWLSubObjectPropertyOfAxiom(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "grandparentOf"),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"))),
                Arguments.of(
                        Set.of(
                                df.getOWLSubObjectPropertyOfAxiom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf")),
                                df.getOWLSubObjectPropertyOfAxiom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf").getInverseProperty(),
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf")),
                                df.getOWLSubObjectPropertyOfAxiom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "fatherOf"),
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf")),
                                df.getOWLSubObjectPropertyOfAxiom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "motherOf"),
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf")),
                                df.getOWLSubObjectPropertyOfAxiom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf").getInverseProperty()),
                                df.getOWLSubObjectPropertyOfAxiom(
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "ancestorOf")),
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLNothing(), df.getOWLThing())),
                        df.getOWLSubObjectPropertyOfAxiom(
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"))),
                Arguments.of(
                        Set.of(
                                df.getOWLSubPropertyChainOfAxiom(
                                        List.of(df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "ancestorOf")),
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf")),
                                df.getOWLSubPropertyChainOfAxiom(
                                        List.of(df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf").getInverseProperty(),
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "ancestorOf")),
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf")),
                                df.getOWLSubPropertyChainOfAxiom(
                                        List.of(df.getOWLObjectProperty(ONTOLOGY_IRI, "motherOf"),
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "ancestorOf")),
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf")),
                                df.getOWLSubPropertyChainOfAxiom(
                                        List.of(df.getOWLObjectProperty(ONTOLOGY_IRI, "fatherOf"),
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "ancestorOf")),
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf")),
                                df.getOWLSubPropertyChainOfAxiom(
                                        List.of(df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf")),
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf")),
                                df.getOWLSubPropertyChainOfAxiom(
                                        List.of(df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                                df.getOWLObjectProperty(ONTOLOGY_IRI, "childOf").getInverseProperty()),
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf")),
                                df.getOWLSubClassOfAxiom(
                                        df.getOWLNothing(), df.getOWLThing())),
                        df.getOWLSubPropertyChainOfAxiom(
                                List.of(df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"),
                                        df.getOWLObjectProperty(ONTOLOGY_IRI, "ancestorOf")),
                                df.getOWLObjectProperty(ONTOLOGY_IRI, "parentOf"))));
    }

    @ParameterizedTest
    @MethodSource("expectedWeakening")
    public void weakenAxiom(Set<OWLAxiom> expected, OWLAxiom axiom) {
        assertEquals(expected, axiomWeakener.weakerAxioms(axiom).collect(Collectors.toSet()));
    }

    @Test
    public void allWeakerAxiomsAreEntailed() {
        try (var copy = ontology.cloneWithJFact()) {
            ontology.logicalAxioms().forEach(strongAxiom -> {
                axiomWeakener.weakerAxioms(strongAxiom).forEach(weakAxiom -> {
                    assertTrue(copy.isEntailed(weakAxiom));
                });
            });
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "../catsandnumbers.owl", "../bodysystem.owl", "../bfo.owl", "../apo.owl", "../aeo.owl", "../duo.owl",
            "../a-and-b.owl", "../Empty.owl", "../FishVehicle/Alignment.owl", "../owl-tests.owl",
            "../FishVehicle/Disalignment.owl", "../FishVehicle/Fish.owl", "../FishVehicle/InitialOntology.owl",
            "../FishVehicle/InitialOntologyAlignment.owl", "../FishVehicle/InitialOntologyInsta.owl",
            "../FishVehicle/InitialOntologyInstantiationAlignment.owl", "../FishVehicle/Test_hybrid.owl",
            "../FishVehicle/Vehicle.owl", "../Random/C50_R10_0.001_0.001_0.001_62888.owl",
    })
    public void allWeakerAxiomsAreEntailedFromFile(String resourceName) throws OWLOntologyCreationException {
        var path = SroiqAxiomWeakenerTest.class.getResource(resourceName).getFile();
        try (var ontology = Ontology.loadOntology(path)) {
            try (var jfact = ontology.cloneWithJFact()) {
                try (var axiomWeakener = new AxiomWeakener(ontology)) {
                    ontology.logicalAxioms().forEach(strongAxiom -> {
                        axiomWeakener.weakerAxioms(strongAxiom).forEach(weakAxiom -> {
                            if (weakAxiom.isOfType(AxiomType.SUB_PROPERTY_CHAIN_OF, AxiomType.DISJOINT_UNION)) {
                                // Using JFact, because Openllet does not support some axioms in entailments.
                                assertTrue(jfact.isEntailed(weakAxiom));
                            } else {
                                assertTrue(ontology.isEntailed(weakAxiom));
                            }
                        });
                    });
                }
            }
        }
    }
}
