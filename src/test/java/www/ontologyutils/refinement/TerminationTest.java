package www.ontologyutils.refinement;

import java.util.stream.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.Ontology;
import www.ontologyutils.toolbox.Utils;

/**
 * Testing almost-sure termination for the problem of reaching TOP by
 * iterated generalization from BOT.
 */
@Execution(ExecutionMode.CONCURRENT)
public class TerminationTest {
    private Ontology ontology;
    private Covers covers;
    private RefinementOperator generalization;

    @BeforeEach
    public void setup() {
        var path = RoleCoverTest.class.getResource("/el/a-and-b.owl").getFile();
        ontology = Ontology.loadOntology(path);
        var subConcepts = Utils.toSet(ontology.subConcepts());
        var subRoles = Utils.toSet(ontology.subRoles());
        var simpleRoles = Utils.toSet(ontology.simpleRoles());
        covers = new Covers(ontology, subConcepts, subRoles, simpleRoles, false);
        var upCover = covers.upCover().cached();
        var downCover = covers.downCover().cached();
        generalization = new RefinementOperator(upCover, downCover);
    }

    @AfterEach
    public void teardown() {
        ontology.close();
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
