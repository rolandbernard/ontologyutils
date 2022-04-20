package www.ontologyutils.refinement;

import java.util.Arrays;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;
import www.ontologyutils.toolbox.SetUtils;
import www.ontologyutils.toolbox.Utils;
import www.ontologyutils.toolbox.Utils.ReasonerName;

/**
 * @author nico
 * 
 *         Testing almost-sure termination for the problem of reaching TOP by
 *         iterated generalization from BOT.
 */
public class TesterTermination extends TestCase {

	private static final String OWL_FILE_PATH = "resources/a-and-b.owl";

	private final static OWLClassExpression BOT = OWLManager.getOWLDataFactory().getOWLNothing();
	private final int NUMBER_TESTS = 100;
	private final boolean PROPER = true; // true to force each generalization step to find non-equivalent
											// generalization
											// concept (unless it is TOP).
	static OWLOntology ontology;
	static Covers covers;
	static RefinementOperator generalisation;
	static OWLReasoner equivReasoner;

	public TesterTermination(String testName) {
		super(testName);
		Utils.DEFAULT_REASONER = ReasonerName.OPENLLET;

		ontology = Utils.newOntology(OWL_FILE_PATH);
		covers = new Covers(ontology);
		generalisation = new RefinementOperator(covers.getUpCoverOperator(), covers.getDownCoverOperator());

		equivReasoner = Utils.getReasoner(ontology);
	}

	public static Test suite() {
		return new TestSuite(TesterTermination.class);
	}

	public static boolean areEquivalent(OWLClassExpression c1, OWLClassExpression c2) {
		OWLSubClassOfAxiomImpl lr = new OWLSubClassOfAxiomImpl(c1, c2, new HashSet<>());
		OWLSubClassOfAxiomImpl rl = new OWLSubClassOfAxiomImpl(c2, c1, new HashSet<>());
		return equivReasoner.isEntailed(lr) && equivReasoner.isEntailed(rl);
	}

	public void testApp() {

		IntSummaryStatistics summary = new IntSummaryStatistics();

		int[] stepsArray = new int[NUMBER_TESTS];

		IntStream.range(1, NUMBER_TESTS + 1).forEach(i -> {
			int steps = 0;
			OWLClassExpression e = BOT;
			System.out.format("**** Test %d. Trying to reach TOP from %s.%n", i, Utils.pretty(e.toString()));
			while (!e.isOWLThing()) {
				System.out.print(".");
				steps++;
				Set<OWLClassExpression> gen;
				if (PROPER) {
					final OWLClassExpression ee = e;
					gen = generalisation.refine(e).stream().filter(c -> !areEquivalent(ee, c) || c.isOWLThing())
							.collect(Collectors.toSet());
				} else {
					gen = generalisation.refine(e);
				}
				e = SetUtils.getRandom(gen);
				// System.out.println(Utils.pretty(e.toString()));
			}
			System.out.format("Required %d steps.%n", steps);
			summary.accept(steps);
			stepsArray[i - 1] = steps;
		});
		Arrays.sort(stepsArray);
		double median;
		if (stepsArray.length % 2 == 0)
			median = ((double) stepsArray[stepsArray.length / 2] + (double) stepsArray[stepsArray.length / 2 - 1]) / 2;
		else
			median = (double) stepsArray[stepsArray.length / 2];
		System.out.format("Average %f. Median %f. Min %d. Max %d.", summary.getAverage(), median, summary.getMin(),
				summary.getMax());

		assertTrue(true);
	}

}
