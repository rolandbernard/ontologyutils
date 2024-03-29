package www.ontologyutils.toolbox;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

@Execution(ExecutionMode.CONCURRENT)
public class MaximalConsistentSetsTest {
    private OWLDataFactory df;
    private List<OWLClassExpression> concepts;
    private List<OWLIndividual> individuals;
    private List<OWLAxiom> axioms;

    @BeforeEach
    public void setup() {
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
        var agenda = Set.copyOf(axioms);
        var results = Utils.toList(MaximalConsistentSubsets.maximalConsistentSubsetsNaive(agenda, Set.of()));
        for (var subset : results) {
            assertTrue(subset.stream().allMatch(ax -> agenda.contains(ax)));
            assertTrue(MaximalConsistentSubsets.isMaximallyConsistentSubset(subset, agenda));
        }
        for (var subset : (Iterable<Set<OWLAxiom>>) Utils.powerSet(agenda)::iterator) {
            assertTrue(
                    !MaximalConsistentSubsets.isMaximallyConsistentSubset(subset, agenda) || results.contains(subset));
        }
    }

    @Test
    public void maximalConsistentSubsets() {
        var agenda = Set.copyOf(axioms);
        var results = MaximalConsistentSubsets.maximalConsistentSubsets(agenda, Set.of());
        for (var subset : results) {
            assertTrue(subset.stream().allMatch(ax -> agenda.contains(ax)));
            assertTrue(MaximalConsistentSubsets.isMaximallyConsistentSubset(subset, agenda));
        }
        for (var subset : (Iterable<Set<OWLAxiom>>) Utils.powerSet(agenda)::iterator) {
            assertTrue(
                    !MaximalConsistentSubsets.isMaximallyConsistentSubset(subset, agenda) || results.contains(subset));
        }
    }

    private static Stream<Arguments> axiomPowerSet() {
        var temp = new MaximalConsistentSetsTest();
        temp.setup();
        return Utils.powerSet(temp.axioms).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("axiomPowerSet")
    public void maximalConsistentSubsetsContaining(Set<OWLAxiom> contained) {
        var results = MaximalConsistentSubsets.maximalConsistentSubsets(axioms, contained);
        var resultsNaive = Utils.toSet(MaximalConsistentSubsets.maximalConsistentSubsetsNaive(axioms, contained));
        assertTrue(results.containsAll(resultsNaive));
        assertTrue(resultsNaive.containsAll(results));
    }
}
