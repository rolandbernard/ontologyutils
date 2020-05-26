package www.ontologyutils.refinement;

import java.util.IntSummaryStatistics;
import java.util.stream.IntStream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import www.ontologyutils.toolbox.SetUtils;
import www.ontologyutils.toolbox.Utils;

/**
 * @author nico
 * 
 *         Testing almost-sure termination for the problem of reaching TOP by
 *         iterated generalization from an arbitrary concept wrt a reference
 *         ontology.
 */
public class TesterTermination extends TestCase {

	private static final String OWL_FILE_PATH = "resources/a-and-b.owl";

	private final static OWLClassExpression TOP = OWLManager.getOWLDataFactory().getOWLThing();
	private final int NUMBER_TESTS = 100;
	static OWLOntology ontology;
	static Covers covers;
	static RefinementOperator generalisation;

	public TesterTermination(String testName) {
		super(testName);

		ontology = Utils.newOntology(OWL_FILE_PATH);
		covers = new Covers(ontology);
		generalisation = new RefinementOperator(covers.getUpCoverOperator(), covers.getDownCoverOperator());
	}

	public static Test suite() {
		return new TestSuite(TesterTermination.class);
	}

	public void testApp() {

		System.out.println("**** Ontology " + OWL_FILE_PATH);
		ontology.axioms().forEach(ax -> System.out.println(Utils.prettyPrintAxiom(ax)));
		System.out.println("**** Sub of ontology " + OWL_FILE_PATH);
		Utils.getSubOfTBox(ontology).forEach(e -> System.out.println(Utils.pretty(e.toString())));

		IntSummaryStatistics summary = new IntSummaryStatistics();
		IntStream.range(1, NUMBER_TESTS + 1).forEach(i -> {
			int steps = 0;
			OWLClassExpression e = SetUtils.getRandom(Utils.getSubOfTBox(ontology));
			System.out.format("**** Test %d. Trying to reach TOP from %s.%n", i, Utils.pretty(e.toString()));
			while (!e.equals(TOP)) {
				steps++;
				e = SetUtils.getRandom(generalisation.refine(e));
				System.out.println(Utils.pretty(e.toString()));
			}
			System.out.format("Required %d steps.%n", steps);
			summary.accept(steps);
		});
		System.out.format("Average %f. Min %d. Max %d.", summary.getAverage(), summary.getMin(), summary.getMax());

		assertTrue(true);
	}

}
