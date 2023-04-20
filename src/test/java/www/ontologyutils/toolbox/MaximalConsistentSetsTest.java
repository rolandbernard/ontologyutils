package www.ontologyutils.toolbox;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

public class MaximalConsistentSetsTest {
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
    public void maximalConsistentSubsetsNaive() {
        final var agenda = Set.copyOf(axioms);
        final var results = MaximalConsistentSubsets.maximalConsistentSubsetsNaive(agenda, Set.of()).toList();
        for (final var subset : results) {
            assertTrue(subset.stream().allMatch(ax -> agenda.contains(ax)));
            assertTrue(MaximalConsistentSubsets.isMaximallyConsistentSubset(subset, agenda));
        }
        for (final var subset : (Iterable<Set<OWLAxiom>>) Utils.powerSet(agenda)::iterator) {
            assertTrue(
                    !MaximalConsistentSubsets.isMaximallyConsistentSubset(subset, agenda) || results.contains(subset));
        }
    }

    @Test
    public void maximalConsistentSubsets() {
        final var agenda = Set.copyOf(axioms);
        final var results = MaximalConsistentSubsets.maximalConsistentSubsets(agenda, Set.of());
        for (final var subset : results) {
            assertTrue(subset.stream().allMatch(ax -> agenda.contains(ax)));
            assertTrue(MaximalConsistentSubsets.isMaximallyConsistentSubset(subset, agenda));
        }
        for (final var subset : (Iterable<Set<OWLAxiom>>) Utils.powerSet(agenda)::iterator) {
            assertTrue(
                    !MaximalConsistentSubsets.isMaximallyConsistentSubset(subset, agenda) || results.contains(subset));
        }
    }

    private static Stream<Arguments> axiomPowerSet() {
        return Utils.powerSet(axioms).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("axiomPowerSet")
    public void maximalConsistentSubsetsContaining(final Set<OWLAxiom> contained) {
        final var results = MaximalConsistentSubsets.maximalConsistentSubsets(axioms, contained);
        final var resultsNaive = MaximalConsistentSubsets.maximalConsistentSubsetsNaive(axioms, contained)
                .collect(Collectors.toSet());
        assertTrue(results.containsAll(resultsNaive));
        assertTrue(resultsNaive.containsAll(results));
    }
}
