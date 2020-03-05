package www.ontologyutils.collective;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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

import www.ontologyutils.collective.PreferenceFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;

/**
 * @author nico
 *
 */
public class TesterPreferenceFactory {

	private static final Collection<OWLAnnotation> EMPTY_ANNOTATION = new ArrayList<OWLAnnotation>();

	static OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();
	static OWLClassExpression entity1 = (OWLClassExpression) dataFactory.getOWLEntity(EntityType.CLASS,
			IRI.create("www.first.org"));
	static OWLClassExpression entity2 = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create("www.second.org"));
	static OWLClassExpression entity3 = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create("www.third.org"));
	static OWLClassExpression entity4 = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create("www.fourth.org"));

	static OWLAxiom ax1 = new OWLSubClassOfAxiomImpl(entity1, entity2, EMPTY_ANNOTATION);
	static OWLAxiom ax2 = new OWLSubClassOfAxiomImpl(entity2, entity3, EMPTY_ANNOTATION);
	static OWLAxiom ax3 = new OWLSubClassOfAxiomImpl(entity3, entity4, EMPTY_ANNOTATION);
	static OWLAxiom ax4 = new OWLSubClassOfAxiomImpl(entity4, entity1, EMPTY_ANNOTATION);

	static PreferenceFactory prefFactory;
	static PreferenceFactory.Preference preference;

	@BeforeClass
	public static void testSetup() {

		ArrayList<OWLAxiom> agenda = new ArrayList<OWLAxiom>();
		agenda.add(ax1);
		agenda.add(ax2);
		agenda.add(ax3);
		agenda.add(ax4);

		prefFactory = new PreferenceFactory(agenda);
	}

	@AfterClass
	public static void testCleanup() {
		// clean stuff
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadRanking() {

		ArrayList<Integer> ranking = new ArrayList<>(Arrays.asList(2, 3, 1));

		preference = prefFactory.makePreference(ranking);
	}

	@Test
	public void testPrefersElements() {

		ArrayList<Integer> ranking = new ArrayList<>(Arrays.asList(2, 3, 1, 4));

		preference = prefFactory.makePreference(ranking);

		assertTrue(preference.prefers(ax1, ax2));
		assertTrue(preference.prefers(ax3, ax2));
		assertFalse(preference.prefers(ax4, ax2));
	}

	@Test
	public void testPrefersSets() {

		ArrayList<Integer> ranking = new ArrayList<>(Arrays.asList(2, 3, 1, 4));
		preference = prefFactory.makePreference(ranking);
		// ax3 < ax1 < ax2 < ax4 (smaller is better)

		Set<OWLAxiom> set1 = new HashSet<OWLAxiom>();
		Set<OWLAxiom> set2 = new HashSet<OWLAxiom>();

		set1.add(ax2); // set1 = {ax2}
		set1.add(ax4); // set1 = {ax2, ax4}
		set2.add(ax1); // set2 = {ax1}
		assertFalse(preference.prefers(set1, set2));
		set1.add(ax1); // set1 = {ax2,ax4,ax1}
		assertTrue(preference.prefers(set1, set2));
		set1.remove(ax4); // set1 = {ax2,ax1}
		assertTrue(preference.prefers(set1, set2));
		assertFalse(preference.prefers(set2, set1));
		set2.add(ax4); // set2 = {ax1, ax4}
		assertFalse(preference.prefers(set2, set1));
		set1.remove(ax1); // set1 = {ax2}
		assertTrue(preference.prefers(set2, set1));
		set2.remove(ax1); // set2 = {ax4}
		assertTrue(preference.prefers(set1, set2));
		set2.remove(ax4); // set2 = {}
		set2.add(ax2); // set2 = {ax2}
		assertTrue(set1.equals(set2));
		assertFalse(preference.prefers(set1, set2));
		assertFalse(preference.prefers(set2, set1));
		set2.remove(ax2); // set2 = {}
		assertTrue(preference.prefers(set1, set2));
		set2.add(ax3); // set2 = {ax3}
		assertTrue(preference.prefers(set2, set1));
		set1.add(ax1); // set1 = {ax2,ax1}
		set1.add(ax4); // set1 = {ax2, ax1, ax4}
		assertTrue(preference.prefers(set2, set1));
		set2.remove(ax3); // set2 = {}
		assertFalse(preference.prefers(set2, set1));

	}

	
	@Test
	public void testGetRank() {

		ArrayList<Integer> ranking = new ArrayList<>(Arrays.asList(2, 3, 1, 4));

		preference = prefFactory.makePreference(ranking);

		assertTrue(preference.getRank(ax1) == 2);
		assertTrue(preference.getRank(ax2) == 3);
		assertTrue(preference.getRank(ax3) == 1);
		assertTrue(preference.getRank(ax4) == 4);
		assertTrue(preference.getRank(prefFactory.getAgenda().get(0)) == 2);
		assertTrue(preference.getRank(prefFactory.getAgenda().get(1)) == 3);
		assertTrue(preference.getRank(prefFactory.getAgenda().get(2)) == 1);
		assertTrue(preference.getRank(prefFactory.getAgenda().get(3)) == 4);
	}
	
	@Test
	public void testGet1() {

		ArrayList<Integer> ranking = new ArrayList<>(Arrays.asList(3, 2, 4, 1));

		preference = prefFactory.makePreference(ranking);

		assertTrue(preference.get(1).equals(prefFactory.getAgenda().get(3)));
		assertTrue(preference.get(2).equals(prefFactory.getAgenda().get(1)));
		assertTrue(preference.get(3).equals(prefFactory.getAgenda().get(0)));
		assertTrue(preference.get(4).equals(prefFactory.getAgenda().get(2)));
	}
	
	@Test
	public void testGet2() {

		ArrayList<Integer> ranking = new ArrayList<>(Arrays.asList(2, 3, 1, 4));
		preference = prefFactory.makePreference(ranking);
		// ax3 < ax1 < ax2 < ax4 (smaller is better)

		assertTrue(preference.get(1).equals(ax3));
		assertTrue(preference.get(3).equals(ax2));
		assertFalse(preference.get(4).equals(ax1));
		assertTrue(preference.get(4).equals(ax4));

	}

	@Test(expected = IllegalArgumentException.class)
	public void testBadRankIndex() {

		ArrayList<Integer> ranking = new ArrayList<>(Arrays.asList(2, 3, 1, 4));
		preference = prefFactory.makePreference(ranking);
		// ax3 < ax1 < ax2 < ax4 (smaller is better)

		preference.get(5);
	}

}
