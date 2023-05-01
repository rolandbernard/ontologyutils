package www.ontologyutils.toolbox;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

public class MinimalSubsetsTest {
    private static final OWLDataFactory df;
    private static final List<OWLClassExpression> concepts;
    private static final List<OWLIndividual> individuals;
    private static final List<OWLAxiom> axioms;

    static {
        df = Ontology.getDefaultDataFactory();
        concepts = List.of(
                df.getOWLClass("www.first.org#", "A"),
                df.getOWLClass("www.second.org#", "A"),
                df.getOWLClass("www.third.org#", "A"),
                df.getOWLClass("www.fourth.org#", "A"),
                df.getOWLClass("www.first.org#", "A"));
        individuals = List.of(
                df.getOWLNamedIndividual("www.indy-one.org#", "A"),
                df.getOWLNamedIndividual("www.indy-two.org#", "A"));
        axioms = List.of(
                df.getOWLSubClassOfAxiom(concepts.get(0), concepts.get(1)),
                df.getOWLSubClassOfAxiom(concepts.get(1), concepts.get(2)),
                df.getOWLSubClassOfAxiom(concepts.get(2), concepts.get(3)),
                df.getOWLSubClassOfAxiom(concepts.get(3), concepts.get(4)),
                df.getOWLSubClassOfAxiom(df.getOWLThing(), concepts.get(0)),
                df.getOWLSubClassOfAxiom(concepts.get(3), df.getOWLNothing()),
                df.getOWLClassAssertionAxiom(concepts.get(0), individuals.get(0)),
                df.getOWLSubClassOfAxiom(df.getOWLThing(), df.getOWLNothing()));
    }

    @Test
    public void maximalConsistentSubset() {
        final var agenda = Set.copyOf(axioms);
        try (final var ontology = Ontology.withAxioms(agenda)) {
            final var subset = ontology.maximalConsistentSubset(o -> o.isConsistent());
            assertTrue(subset.stream().allMatch(ax -> agenda.contains(ax)));
            assertTrue(MaximalConsistentSubsets.isMaximallyConsistentSubset(subset, agenda));
        }
    }

    @Test
    public void minimalCorrectionSubset() {
        final var agenda = Set.copyOf(axioms);
        try (final var ontology = Ontology.withAxioms(agenda)) {
            final var subset = ontology.minimalCorrectionSubset(o -> o.isConsistent());
            assertTrue(subset.stream().allMatch(ax -> agenda.contains(ax)));
            assertTrue(MaximalConsistentSubsets.isMaximallyConsistentSubset(ontology.complement(subset), agenda));
        }
    }

    @Test
    public void minimalUnsatisfiableSubset() {
        final var agenda = Set.copyOf(axioms);
        try (final var ontology = Ontology.withAxioms(agenda)) {
            final var subset = ontology.minimalUnsatisfiableSubset(o -> o.isConsistent());
            ontology.removeAxioms(ontology.axioms().toList());
            ontology.addAxioms(subset);
            assertTrue(subset.stream().allMatch(ax -> agenda.contains(ax)));
            assertFalse(ontology.isConsistent());
            for (final var axiom : subset) {
                ontology.removeAxioms(axiom);
                assertTrue(ontology.isConsistent());
                ontology.addAxioms(axiom);
            }
        }
    }

    @Test
    public void someMaximalConsistentSubset() {
        final var agenda = Set.copyOf(axioms);
        try (final var ontology = Ontology.withAxioms(agenda)) {
            final var results = ontology.someMaximalConsistentSubsets(o -> o.isConsistent()).toList();
            assertFalse(results.isEmpty());
            for (final var subset : results) {
                assertTrue(subset.stream().allMatch(ax -> agenda.contains(ax)));
                assertTrue(MaximalConsistentSubsets.isMaximallyConsistentSubset(subset, agenda));
            }
        }
    }

    @Test
    public void someMinimalCorrectionSubset() {
        final var agenda = Set.copyOf(axioms);
        try (final var ontology = Ontology.withAxioms(agenda)) {
            final var results = ontology.someMinimalCorrectionSubsets(o -> o.isConsistent()).toList();
            assertFalse(results.isEmpty());
            for (final var subset : results) {
                assertTrue(subset.stream().allMatch(ax -> agenda.contains(ax)));
                assertTrue(MaximalConsistentSubsets.isMaximallyConsistentSubset(ontology.complement(subset), agenda));
            }
        }
    }

    @Test
    public void someMinimalUnsatisfiableSubset() {
        final var agenda = Set.copyOf(axioms);
        try (final var ontology = Ontology.withAxioms(agenda)) {
            final var results = ontology.someMinimalUnsatisfiableSubsets(o -> o.isConsistent()).toList();
            assertFalse(results.isEmpty());
            for (final var subset : results) {
                ontology.removeAxioms(ontology.axioms().toList());
                ontology.addAxioms(subset);
                assertTrue(subset.stream().allMatch(ax -> agenda.contains(ax)));
                assertFalse(ontology.isConsistent());
                for (final var axiom : subset) {
                    ontology.removeAxioms(axiom);
                    assertTrue(ontology.isConsistent());
                    ontology.addAxioms(axiom);
                }
            }
        }
    }

    @Test
    public void maximalConsistentSubsets() {
        final var agenda = Set.copyOf(axioms);
        try (final var ontology = Ontology.withAxioms(agenda)) {
            final var results = ontology.maximalConsistentSubsets().toList();
            for (final var subset : results) {
                assertTrue(subset.stream().allMatch(ax -> agenda.contains(ax)));
                assertTrue(MaximalConsistentSubsets.isMaximallyConsistentSubset(subset, agenda));
            }
            for (final var subset : (Iterable<Set<OWLAxiom>>) Utils.powerSet(agenda)::iterator) {
                assertTrue(
                        !MaximalConsistentSubsets.isMaximallyConsistentSubset(subset, agenda)
                                || results.contains(subset));
            }
        }
    }

    private static Stream<Arguments> axiomPowerSet() {
        return Utils.powerSet(axioms).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("axiomPowerSet")
    public void maximalConsistentSubsetsOfSubsets(final Set<OWLAxiom> agenda) {
        try (final var ontology = Ontology.withAxioms(agenda)) {
            final var subset = ontology.maximalConsistentSubset(o -> o.isConsistent());
            assertTrue(subset.stream().allMatch(ax -> agenda.contains(ax)));
            assertTrue(MaximalConsistentSubsets.isMaximallyConsistentSubset(subset, agenda));
        }
    }

    @ParameterizedTest
    @MethodSource("axiomPowerSet")
    public void someMaximalConsistentSubsetsOfSubsets(final Set<OWLAxiom> agenda) {
        try (final var ontology = Ontology.withAxioms(agenda)) {
            final var results = ontology.someMaximalConsistentSubsets(o -> o.isConsistent()).toList();
            assertFalse(results.isEmpty());
            for (final var subset : results) {
                assertTrue(subset.stream().allMatch(ax -> agenda.contains(ax)));
                assertTrue(MaximalConsistentSubsets.isMaximallyConsistentSubset(subset, agenda));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("axiomPowerSet")
    public void allMaximalConsistentSubsetsOfSubsets(final Set<OWLAxiom> agenda) {
        try (final var ontology = Ontology.withAxioms(agenda)) {
            final var results = ontology.maximalConsistentSubsets().collect(Collectors.toSet());
            final var resultsNaive = MaximalConsistentSubsets.maximalConsistentSubsetsNaive(agenda, Set.of())
                    .collect(Collectors.toSet());
            assertTrue(results.containsAll(resultsNaive));
            assertTrue(resultsNaive.containsAll(results));
        }
    }

    @ParameterizedTest
    @MethodSource("axiomPowerSet")
    public void maximalConsistentSubsetsContaining(final Set<OWLAxiom> contained) {
        final var agenda = Set.copyOf(axioms);
        try (final var ontology = Ontology.withAxioms(contained, agenda)) {
            final var results = ontology.maximalConsistentSubsets().collect(Collectors.toSet());
            final var resultsNaive = MaximalConsistentSubsets.maximalConsistentSubsetsNaive(axioms, contained)
                    .collect(Collectors.toSet());
            assertTrue(results.containsAll(resultsNaive));
            assertTrue(resultsNaive.containsAll(results));
        }
    }
}
