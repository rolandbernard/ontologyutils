package www.ontologyutils.toolbox;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.semanticweb.owlapi.model.*;

import org.junit.jupiter.api.Test;

public class UtilsTest {
    private final OWLDataFactory df;
    private final List<OWLClassExpression> concepts;
    private final List<OWLObjectProperty> roles;
    private final List<OWLIndividual> individuals;
    private final List<OWLAxiom> axioms;

    public UtilsTest() {
        df = Ontology.getDefaultDataFactory();
        concepts = List.of(
                df.getOWLClass("www.first.org#", "A"),
                df.getOWLClass("www.second.org#", "A"),
                df.getOWLClass("www.third.org#", "A"),
                df.getOWLClass("www.fourth.org#", "A"),
                df.getOWLClass("www.first.org#", "A"));
        roles = List.of(
                df.getOWLObjectProperty("www.role.org#", "A"),
                df.getOWLObjectProperty("www.role.org#", "A"));
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
                df.getOWLClassAssertionAxiom(concepts.get(0), individuals.get(0)),
                df.getOWLSubClassOfAxiom(df.getOWLThing(), df.getOWLNothing()));
    }

    @Test
    public void powerSet() {
        final var powerSet = Utils.powerSet(axioms).toList();
        assertEquals((int) Math.pow(2, axioms.size()), powerSet.size());
        assertTrue(powerSet.stream().allMatch(subset -> axioms.containsAll(subset)));
    }

    @Test
    public void sameConcept() {
        // Note: this test is fragile. It could be failing simply by a change in the owl
        // api implementation.
        final var df = Ontology.getDefaultDataFactory();

        assertNotSame(concepts.get(0), concepts.get(4));
        assertTrue(Utils.sameConcept(concepts.get(0), concepts.get(4)));

        final var union1A = df.getOWLObjectUnionOf(concepts.get(0), concepts.get(1));
        final var union1B = df.getOWLObjectUnionOf(concepts.get(4), concepts.get(1));
        assertNotSame(union1A, union1B);
        assertTrue(Utils.sameConcept(union1A, union1B));

        final var union2A = df.getOWLObjectUnionOf(concepts.get(0), concepts.get(1), concepts.get(2));
        final var union2B = df.getOWLObjectUnionOf(concepts.get(4), concepts.get(1), concepts.get(2));
        assertNotSame(union2A, union2B);
        assertTrue(Utils.sameConcept(union2A, union2B));

        final var existsA = df.getOWLObjectSomeValuesFrom(roles.get(0), union2A);
        final var existsB = df.getOWLObjectSomeValuesFrom(roles.get(1), union2B);
        assertNotSame(existsA, existsB);
        assertTrue(Utils.sameConcept(existsA, existsB));

        final var union3A = df.getOWLObjectUnionOf(concepts.get(0), concepts.get(1), concepts.get(2), existsA);
        final var union3B = df.getOWLObjectUnionOf(concepts.get(4), concepts.get(1), concepts.get(2), existsB);
        assertNotSame(union3A, union3B);
        assertTrue(Utils.sameConcept(union3A, union3B));

        final var allA = df.getOWLObjectAllValuesFrom(roles.get(0), union3A);
        final var allB = df.getOWLObjectAllValuesFrom(roles.get(1), union3B);
        assertNotSame(allA, allB);
        assertTrue(Utils.sameConcept(allA, allB));

        final var interA = df.getOWLObjectIntersectionOf(concepts.get(0), concepts.get(1), concepts.get(2), existsA,
                allA);
        final var interB = df.getOWLObjectIntersectionOf(concepts.get(4), concepts.get(1), concepts.get(2), existsB,
                allB);
        assertNotSame(interA, interB);
        assertTrue(Utils.sameConcept(interA, interB));
    }
}
