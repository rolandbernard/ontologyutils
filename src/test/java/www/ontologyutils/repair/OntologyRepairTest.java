package www.ontologyutils.repair;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public abstract class OntologyRepairTest {
    private final OWLDataFactory df;
    private final List<OWLClassExpression> concepts;
    private final List<OWLIndividual> individuals;
    private final List<OWLAxiom> axioms;

    public OntologyRepairTest() {
        df = Ontology.getDefaultDataFactory();
        concepts = List.of(
                df.getOWLClass("www.first.org#", "A"),
                df.getOWLClass("www.second.org#", "A"),
                df.getOWLClass("www.third.org#", "A"),
                df.getOWLClass("www.fourth.org#", "A"));
        individuals = List.of(
                df.getOWLNamedIndividual("www.indy-one.org#", "A"),
                df.getOWLNamedIndividual("www.indy-two.org#", "A"));
        axioms = List.of(
                df.getOWLSubClassOfAxiom(concepts.get(0), concepts.get(1)),
                df.getOWLSubClassOfAxiom(concepts.get(1), concepts.get(2)),
                df.getOWLSubClassOfAxiom(concepts.get(2), concepts.get(3)),
                df.getOWLSubClassOfAxiom(concepts.get(3), concepts.get(0)),
                df.getOWLSubClassOfAxiom(df.getOWLThing(), concepts.get(0)),
                df.getOWLSubClassOfAxiom(concepts.get(3), df.getOWLNothing()),
                df.getOWLClassAssertionAxiom(concepts.get(0), individuals.get(0)));
    }

    protected abstract OntologyRepair getRepairForConsistency();

    @Test
    public void completeOntologyIsInconsistent() {
        try (final var ontology = Ontology.withAxioms(axioms)) {
            assertFalse(ontology.isConsistent());
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 })
    public void repairInconsistentOntology(final int seed) {
        Utils.randomSeed(seed);
        try (final var ontology = Ontology.withAxioms(axioms)) {
            final var repair = getRepairForConsistency();
            assertFalse(ontology.isConsistent());
            repair.apply(ontology);
            assertTrue(ontology.isConsistent());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "../inconsistent-leftpolicies-small.owl", "../inconsistent-leftpolicies.owl" })
    public void repairInconsistentOntologyFromFile(final String resourceName) {
        final var path = OntologyRepairTest.class.getResource(resourceName).getFile();
        try (final var ontology = Ontology.loadOntology(path)) {
            Utils.randomSeed(0);
            final var repair = getRepairForConsistency();
            assertFalse(ontology.isConsistent());
            repair.apply(ontology);
            assertTrue(ontology.isConsistent());
        }
    }
}
