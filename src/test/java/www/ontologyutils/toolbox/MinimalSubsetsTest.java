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
public class MinimalSubsetsTest {
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
    public void maximalConsistentSubset() {
        var agenda = Set.copyOf(axioms);
        try (var ontology = Ontology.withAxioms(agenda)) {
            var subset = ontology.maximalConsistentSubset(o -> o.isConsistent());
            assertTrue(subset.stream().allMatch(ax -> agenda.contains(ax)));
            assertTrue(MaximalConsistentSubsets.isMaximallyConsistentSubset(subset, agenda));
        }
    }

    @Test
    public void minimalCorrectionSubset() {
        var agenda = Set.copyOf(axioms);
        try (var ontology = Ontology.withAxioms(agenda)) {
            var subset = ontology.minimalCorrectionSubset(o -> o.isConsistent());
            assertTrue(subset.stream().allMatch(ax -> agenda.contains(ax)));
            assertTrue(MaximalConsistentSubsets.isMaximallyConsistentSubset(ontology.complement(subset), agenda));
        }
    }

    @Test
    public void minimalUnsatisfiableSubset() {
        var agenda = Set.copyOf(axioms);
        try (var ontology = Ontology.withAxioms(agenda)) {
            var subset = ontology.minimalUnsatisfiableSubset(o -> o.isConsistent());
            ontology.removeAxioms(Utils.toList(ontology.axioms()));
            ontology.addAxioms(subset);
            assertTrue(subset.stream().allMatch(ax -> agenda.contains(ax)));
            assertFalse(ontology.isConsistent());
            for (var axiom : subset) {
                ontology.removeAxioms(axiom);
                assertTrue(ontology.isConsistent());
                ontology.addAxioms(axiom);
            }
        }
    }

    @Test
    public void someMaximalConsistentSubset() {
        var agenda = Set.copyOf(axioms);
        try (var ontology = Ontology.withAxioms(agenda)) {
            var results = Utils.toList(ontology.someMaximalConsistentSubsets(o -> o.isConsistent()));
            assertFalse(results.isEmpty());
            for (var subset : results) {
                assertTrue(subset.stream().allMatch(ax -> agenda.contains(ax)));
                assertTrue(MaximalConsistentSubsets.isMaximallyConsistentSubset(subset, agenda));
            }
        }
    }

    @Test
    public void someMinimalCorrectionSubset() {
        var agenda = Set.copyOf(axioms);
        try (var ontology = Ontology.withAxioms(agenda)) {
            var results = Utils.toList(ontology.someMinimalCorrectionSubsets(o -> o.isConsistent()));
            assertFalse(results.isEmpty());
            for (var subset : results) {
                assertTrue(subset.stream().allMatch(ax -> agenda.contains(ax)));
                assertTrue(MaximalConsistentSubsets.isMaximallyConsistentSubset(ontology.complement(subset), agenda));
            }
        }
    }

    @Test
    public void someMinimalUnsatisfiableSubset() {
        var agenda = Set.copyOf(axioms);
        try (var ontology = Ontology.withAxioms(agenda)) {
            var results = Utils.toList(ontology.someMinimalUnsatisfiableSubsets(o -> o.isConsistent()));
            assertFalse(results.isEmpty());
            for (var subset : results) {
                ontology.removeAxioms(Utils.toList(ontology.axioms()));
                ontology.addAxioms(subset);
                assertTrue(subset.stream().allMatch(ax -> agenda.contains(ax)));
                assertFalse(ontology.isConsistent());
                for (var axiom : subset) {
                    ontology.removeAxioms(axiom);
                    assertTrue(ontology.isConsistent());
                    ontology.addAxioms(axiom);
                }
            }
        }
    }

    @Test
    public void maximalConsistentSubsets() {
        var agenda = Set.copyOf(axioms);
        try (var ontology = Ontology.withAxioms(agenda)) {
            var results = Utils.toList(ontology.maximalConsistentSubsets());
            for (var subset : results) {
                assertTrue(subset.stream().allMatch(ax -> agenda.contains(ax)));
                assertTrue(MaximalConsistentSubsets.isMaximallyConsistentSubset(subset, agenda));
            }
            for (var subset : (Iterable<Set<OWLAxiom>>) Utils.powerSet(agenda)::iterator) {
                assertTrue(
                        !MaximalConsistentSubsets.isMaximallyConsistentSubset(subset, agenda)
                                || results.contains(subset));
            }
        }
    }

    private static Stream<Arguments> axiomPowerSet() {
        var temp = new MinimalSubsetsTest();
        temp.setup();
        return Utils.powerSet(temp.axioms).map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("axiomPowerSet")
    public void maximalConsistentSubsetsOfSubsets(Set<OWLAxiom> agenda) {
        try (var ontology = Ontology.withAxioms(agenda)) {
            var subset = ontology.maximalConsistentSubset(o -> o.isConsistent());
            assertTrue(subset.stream().allMatch(ax -> agenda.contains(ax)));
            assertTrue(MaximalConsistentSubsets.isMaximallyConsistentSubset(subset, agenda));
        }
    }

    @ParameterizedTest
    @MethodSource("axiomPowerSet")
    public void someMaximalConsistentSubsetsOfSubsets(Set<OWLAxiom> agenda) {
        try (var ontology = Ontology.withAxioms(agenda)) {
            var results = Utils.toList(ontology.someMaximalConsistentSubsets(o -> o.isConsistent()));
            assertFalse(results.isEmpty());
            for (var subset : results) {
                assertTrue(subset.stream().allMatch(ax -> agenda.contains(ax)));
                assertTrue(MaximalConsistentSubsets.isMaximallyConsistentSubset(subset, agenda));
            }
        }
    }

    @ParameterizedTest
    @MethodSource("axiomPowerSet")
    public void allMaximalConsistentSubsetsOfSubsets(Set<OWLAxiom> agenda) {
        try (var ontology = Ontology.withAxioms(agenda)) {
            var results = Utils.toSet(ontology.maximalConsistentSubsets());
            var resultsNaive = Utils.toSet(MaximalConsistentSubsets.maximalConsistentSubsetsNaive(agenda, Set.of()));
            assertTrue(results.containsAll(resultsNaive));
            assertTrue(resultsNaive.containsAll(results));
        }
    }

    @ParameterizedTest
    @MethodSource("axiomPowerSet")
    public void maximalConsistentSubsetsContaining(Set<OWLAxiom> contained) {
        var agenda = Set.copyOf(axioms);
        try (var ontology = Ontology.withAxioms(contained, agenda)) {
            var results = Utils.toSet(ontology.maximalConsistentSubsets());
            var resultsNaive = Utils.toSet(MaximalConsistentSubsets.maximalConsistentSubsetsNaive(axioms, contained));
            assertTrue(results.containsAll(resultsNaive));
            assertTrue(resultsNaive.containsAll(results));
        }
    }
}
