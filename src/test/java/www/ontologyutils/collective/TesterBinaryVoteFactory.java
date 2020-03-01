package www.ontologyutils.collective;

import java.util.ArrayList;
import java.util.Collection;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;

import www.ontologyutils.collective.BinaryVoteFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLClassAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;

public class TesterBinaryVoteFactory {

	private static final Collection<OWLAnnotation> EMPTY_ANNOTATION = new ArrayList<OWLAnnotation>();
	private static final OWLClassExpression TOP = new OWLDataFactoryImpl().getOWLThing();
	private static final OWLClassExpression BOT = new OWLDataFactoryImpl().getOWLNothing();
	
	static OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();
	static OWLClassExpression entity1 = (OWLClassExpression) dataFactory.getOWLEntity(EntityType.CLASS,
			IRI.create("www.first.org"));
	static OWLClassExpression entity2 = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create("www.second.org"));
	static OWLClassExpression entity3 = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create("www.third.org"));
	static OWLClassExpression entity4 = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create("www.fourth.org"));

	static OWLIndividual indy1 = dataFactory.getOWLNamedIndividual(IRI.create("www.indy-one.org"));
	static OWLIndividual indy2 = dataFactory.getOWLNamedIndividual(IRI.create("www.indy-two.org"));

	static OWLAxiom ax1 = new OWLSubClassOfAxiomImpl(entity1, entity2, EMPTY_ANNOTATION);
	static OWLAxiom ax2 = new OWLSubClassOfAxiomImpl(entity2, entity3, EMPTY_ANNOTATION);
	static OWLAxiom ax3 = new OWLSubClassOfAxiomImpl(entity3, entity4, EMPTY_ANNOTATION);
	static OWLAxiom ax4 = new OWLSubClassOfAxiomImpl(entity4, entity1, EMPTY_ANNOTATION);
	static OWLAxiom ax5 = new OWLSubClassOfAxiomImpl(TOP, entity1, EMPTY_ANNOTATION);
	static OWLAxiom ax6 = new OWLSubClassOfAxiomImpl(entity4, BOT, EMPTY_ANNOTATION);
	static OWLAxiom ax7 = new OWLClassAssertionAxiomImpl(indy1, entity1, EMPTY_ANNOTATION);
	static OWLAxiom ax8 = new OWLClassAssertionAxiomImpl(indy2, entity2, EMPTY_ANNOTATION);
	static OWLAxiom ax9 = new OWLSubClassOfAxiomImpl(entity2, BOT, EMPTY_ANNOTATION);

	static BinaryVoteFactory bvFactory;
	static ArrayList<OWLAxiom> agenda;
	
	@BeforeClass
	public static void testSetup() {

		agenda = new ArrayList<OWLAxiom>();
		agenda.add(ax1);
		agenda.add(ax2);
		agenda.add(ax3);
		agenda.add(ax4);
		agenda.add(ax5);
		agenda.add(ax6);
		agenda.add(ax7);
		agenda.add(ax8);
		agenda.add(ax9);

		System.out.println("AGENDA " + agenda);

		bvFactory = new BinaryVoteFactory(agenda);
	}

	@AfterClass
	public static void testCleanup() {
		// clean stuff
	}
	
	@Test
	public void testRandomVote() {
		System.out.println("RANDOM - positivity 0.5f");
		int NUM_ITERATIONS = 10;
		for (int i = 0 ; i < NUM_ITERATIONS ; i++) {
			System.out.println(bvFactory.makeRandomBinaryVote());
		}
	}
	
	@Test
	public void testRandomVoteSmallPositivity() {
		System.out.println("RANDOM - positivity 0.1f");
		int NUM_ITERATIONS = 10;
		float positivity = 0.1f;
		for (int i = 0 ; i < NUM_ITERATIONS ; i++) {
			System.out.println(bvFactory.makeRandomBinaryVote(positivity));
		}
	}
	
	@Test
	public void testRandomVoteLargePositivity() {
		System.out.println("RANDOM - positivity 0.9f");
		int NUM_ITERATIONS = 10;
		float positivity = 0.9f;
		for (int i = 0 ; i < NUM_ITERATIONS ; i++) {
			System.out.println(bvFactory.makeRandomBinaryVote(positivity));
		}
	}
}
