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
    private Ontology ontology;
    private Covers covers;
    static RefinementOperator generalization;

    public TerminationTest() {
        var path = RoleCoverTest.class.getResource("/a-and-b.owl").getFile();
        ontology = Ontology.loadOntology(path);
        covers = new Covers(ontology, Utils.toSet(ontology.simpleRoles()));
        var upCover = covers.upCover().cached();
        var downCover = covers.downCover().cached();
        generalization = new RefinementOperator(upCover, downCover);
    }

    public boolean areEquivalent(OWLClassExpression c1, OWLClassExpression c2) {
        var df = Ontology.getDefaultDataFactory();
        var lr = df.getOWLSubClassOfAxiom(c1, c2);
        var rl = df.getOWLSubClassOfAxiom(c2, c1);
        return ontology.isEntailed(lr) && ontology.isEntailed(rl);
    }

    private static Stream<Arguments> terminationRuns() {
        return IntStream.range(0, 100).mapToObj(i -> Arguments.of(i));
    }

    @ParameterizedTest
    @MethodSource("terminationRuns")
    public void testTermination(int seed) {
        Utils.randomSeed(seed);
        var df = Ontology.getDefaultDataFactory();
        OWLClassExpression current = df.getOWLNothing();
        while (!current.isOWLThing()) {
            current = Utils.randomChoice(generalization.refine(current));
        }
    }
}
