package www.ontologyutils.refinement;

import java.util.stream.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.Ontology;
import www.ontologyutils.toolbox.Utils;

/**
 * Testing almost-sure termination for the problem of reaching TOP by
 * iterated generalization from BOT.
 */
public class TerminationTest {
    private final Ontology ontology;
    private final Covers covers;
    static RefinementOperator generalization;

    public TerminationTest() {
        final var path = RoleCoverTest.class.getResource("../a-and-b.owl").getFile();
        ontology = Ontology.loadOntology(path);
        covers = new Covers(ontology);
        generalization = new RefinementOperator(covers::upCover, covers::downCover);
    }

    public boolean areEquivalent(final OWLClassExpression c1, final OWLClassExpression c2) {
        final var df = Ontology.getDefaultDataFactory();
        final var lr = df.getOWLSubClassOfAxiom(c1, c2);
        final var rl = df.getOWLSubClassOfAxiom(c2, c1);
        return ontology.isEntailed(lr) && ontology.isEntailed(rl);
    }

    private static Stream<Arguments> terminationRuns() {
        return IntStream.range(0, 100).mapToObj(i -> Arguments.of(i));
    }

    @ParameterizedTest
    @MethodSource("terminationRuns")
    public void testTermination(final int seed) {
        Utils.randomSeed(seed);
        final var df = Ontology.getDefaultDataFactory();
        OWLClassExpression current = df.getOWLNothing();
        while (!current.isOWLThing()) {
            current = Utils.randomChoice(generalization.refine(current));
        }
    }
}
