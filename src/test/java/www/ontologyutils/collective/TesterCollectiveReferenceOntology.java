package www.ontologyutils.collective;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.manchester.cs.owl.owlapi.OWLClassAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;
import www.ontologyutils.collective.PreferenceFactory.Preference;
import www.ontologyutils.toolbox.MaximalConsistentSets;
import www.ontologyutils.toolbox.SetUtils;
import www.ontologyutils.toolbox.Utils;

/**
 * @author nico
 *
 */
public class TesterCollectiveReferenceOntology {

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

	static PreferenceFactory prefFactory;
	static ArrayList<OWLAxiom> agenda;

	static final int NUM_RANDOM = 15;

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

		prefFactory = new PreferenceFactory(agenda);
	}

	@AfterClass
	public static void testCleanup() {
		// clean stuff
	}

	@Test
	public void testMaximalConsistentSets() {
		System.out.println("\n%%% TEST MCSs");

		Set<OWLAxiom> agendaSet = new HashSet<>(agenda);

		Set<Set<OWLAxiom>> results = MaximalConsistentSets.maximalConsistentSubsets(agendaSet);

		System.out.println("Found " + results.size() + " MCSs in " + agendaSet);
		results.stream().forEach(System.out::println);

		assertTrue(results.stream().allMatch(subset -> MaximalConsistentSets.isMaximallyConsistentSubset(subset, agendaSet)));
		assertTrue(SetUtils.powerSet(agendaSet).stream().allMatch(
				subset -> (!MaximalConsistentSets.isMaximallyConsistentSubset(subset, agendaSet) || results.contains(subset))));
	}

	@Test
	public void testCollectivelyPrefersSets() {

		ArrayList<Integer> ranking1 = new ArrayList<>(Arrays.asList(2, 1, 3, 4, 5, 6, 7, 8, 9));
		Preference preference1 = prefFactory.makePreference(ranking1);
		ArrayList<Integer> ranking2 = new ArrayList<>(Arrays.asList(3, 2, 1, 4, 6, 5, 7, 8, 9));
		Preference preference2 = prefFactory.makePreference(ranking2);
		ArrayList<Integer> ranking3 = new ArrayList<>(Arrays.asList(2, 5, 1, 6, 4, 3, 7, 8, 9));
		Preference preference3 = prefFactory.makePreference(ranking3);

		CollectiveReferenceOntology collective = new CollectiveReferenceOntology(agenda,
				new ArrayList<>(Arrays.asList(preference1, preference2, preference3)));

		Set<OWLAxiom> set1 = new HashSet<OWLAxiom>();
		Set<OWLAxiom> set2 = new HashSet<OWLAxiom>();

		set1.add(ax2); // set1 = {ax2}
		set2.add(ax2); // set2 = {ax2}
		assertFalse(collective.lexicographicallySmaller(set1, set2));
		set1.add(ax4); // set1 = {ax2, ax4}
		assertTrue(collective.lexicographicallySmaller(set1, set2));
		set2.add(ax1); // set2 = {ax2, ax1}
		assertTrue(collective.lexicographicallySmaller(set2, set1));
	}

	@Test
	public void testCollectiveGetOne() {

		System.out.println("\n%%% ONE");

		ArrayList<Integer> ranking1 = new ArrayList<>(Arrays.asList(2, 1, 3, 4, 5, 6, 7, 8, 9));
		Preference preference1 = prefFactory.makePreference(ranking1);
		ArrayList<Integer> ranking2 = new ArrayList<>(Arrays.asList(3, 2, 1, 4, 6, 5, 7, 8, 9));
		Preference preference2 = prefFactory.makePreference(ranking2);
		ArrayList<Integer> ranking3 = new ArrayList<>(Arrays.asList(2, 5, 1, 6, 4, 3, 7, 8, 9));
		Preference preference3 = prefFactory.makePreference(ranking3);

		System.out.println("Pref1 : " + preference1);
		System.out.println("Pref2 : " + preference2);
		System.out.println("Pref3 : " + preference3);

		CollectiveReferenceOntology collective = new CollectiveReferenceOntology(agenda,
				new ArrayList<>(Arrays.asList(preference1, preference2, preference3)));

		OWLOntology ontology = collective.get();
		System.out.println("Collective ontology : " + ontology);

		assertTrue(Utils.isConsistent(ontology));

		assertTrue(SetUtils.powerSet(new HashSet<>(agenda)).stream()
				.allMatch(set -> (!collective.lexicographicallySmaller(set, ontology.axioms().collect(Collectors.toSet())))
						|| !Utils.isConsistent(set)));
	}

	@Test
	public void testCollectiveGetTwo() {

		System.out.println("\n%%% TWO");

		ArrayList<Integer> ranking1 = new ArrayList<>(Arrays.asList(2, 7, 3, 4, 5, 6, 1, 8, 9));
		Preference preference1 = prefFactory.makePreference(ranking1);
		ArrayList<Integer> ranking2 = new ArrayList<>(Arrays.asList(3, 2, 7, 4, 6, 5, 1, 8, 9));
		Preference preference2 = prefFactory.makePreference(ranking2);
		ArrayList<Integer> ranking3 = new ArrayList<>(Arrays.asList(2, 5, 7, 6, 4, 3, 1, 8, 9));
		Preference preference3 = prefFactory.makePreference(ranking3);

		System.out.println("Pref1 : " + preference1);
		System.out.println("Pref2 : " + preference2);
		System.out.println("Pref3 : " + preference3);

		CollectiveReferenceOntology collective = new CollectiveReferenceOntology(agenda,
				new ArrayList<>(Arrays.asList(preference1, preference2, preference3)));

		OWLOntology ontology = collective.get();
		System.out.println("Collective ontology : " + ontology);

		assertTrue(Utils.isConsistent(ontology));

		assertTrue(SetUtils.powerSet(new HashSet<>(agenda)).stream()
				.allMatch(set -> (!collective.lexicographicallySmaller(set, ontology.axioms().collect(Collectors.toSet())))
						|| !Utils.isConsistent(set)));
	}

	@Test
	public void testCollectiveGetThree() {

		System.out.println("\n%%% THREE");

		ArrayList<Integer> ranking1 = new ArrayList<>(Arrays.asList(1, 4, 3, 7, 5, 6, 2, 8, 9));
		Preference preference1 = prefFactory.makePreference(ranking1);
		ArrayList<Integer> ranking2 = new ArrayList<>(Arrays.asList(3, 2, 7, 4, 6, 5, 1, 8, 9));
		Preference preference2 = prefFactory.makePreference(ranking2);
		ArrayList<Integer> ranking3 = new ArrayList<>(Arrays.asList(2, 5, 7, 6, 4, 3, 1, 8, 9));
		Preference preference3 = prefFactory.makePreference(ranking3);

		System.out.println("Pref1 : " + preference1);
		System.out.println("Pref2 : " + preference2);
		System.out.println("Pref3 : " + preference3);

		CollectiveReferenceOntology collective = new CollectiveReferenceOntology(agenda,
				new ArrayList<>(Arrays.asList(preference1, preference2, preference3)));

		OWLOntology ontology = collective.get();
		System.out.println("Collective ontology : " + ontology);

		assertTrue(Utils.isConsistent(ontology));

		assertTrue(SetUtils.powerSet(new HashSet<>(agenda)).stream()
				.allMatch(set -> (!collective.lexicographicallySmaller(set, ontology.axioms().collect(Collectors.toSet())))
						|| !Utils.isConsistent(set)));
	}

	@Test
	public void testCollectiveGetFour() {

		System.out.println("\n%%% FOUR");

		ArrayList<Integer> ranking1 = new ArrayList<>(Arrays.asList(6, 4, 3, 7, 5, 1, 2, 8, 9));
		Preference preference1 = prefFactory.makePreference(ranking1);
		ArrayList<Integer> ranking2 = new ArrayList<>(Arrays.asList(3, 5, 7, 4, 6, 2, 1, 8, 9));
		Preference preference2 = prefFactory.makePreference(ranking2);
		ArrayList<Integer> ranking3 = new ArrayList<>(Arrays.asList(3, 5, 7, 6, 4, 1, 2, 8, 9));
		Preference preference3 = prefFactory.makePreference(ranking3);

		System.out.println("Pref1 : " + preference1);
		System.out.println("Pref2 : " + preference2);
		System.out.println("Pref3 : " + preference3);

		CollectiveReferenceOntology collective = new CollectiveReferenceOntology(agenda,
				new ArrayList<>(Arrays.asList(preference1, preference2, preference3)));

		OWLOntology ontology = collective.get();
		System.out.println("Collective ontology : " + ontology);

		assertTrue(Utils.isConsistent(ontology));

		assertTrue(SetUtils.powerSet(new HashSet<>(agenda)).stream()
				.allMatch(set -> (!collective.lexicographicallySmaller(set, ontology.axioms().collect(Collectors.toSet())))
						|| !Utils.isConsistent(set)));
	}

	@Test
	public void testCollectiveGetFive() {

		System.out.println("\n%%% FIVE");

		ArrayList<Integer> ranking1 = new ArrayList<>(Arrays.asList(6, 4, 8, 7, 5, 9, 2, 3, 1));
		Preference preference1 = prefFactory.makePreference(ranking1);
		ArrayList<Integer> ranking2 = new ArrayList<>(Arrays.asList(9, 5, 8, 4, 6, 7, 3, 2, 1));
		Preference preference2 = prefFactory.makePreference(ranking2);
		ArrayList<Integer> ranking3 = new ArrayList<>(Arrays.asList(4, 5, 7, 6, 8, 3, 9, 1, 2));
		Preference preference3 = prefFactory.makePreference(ranking3);

		System.out.println("Pref1 : " + preference1);
		System.out.println("Pref2 : " + preference2);
		System.out.println("Pref3 : " + preference3);

		CollectiveReferenceOntology collective = new CollectiveReferenceOntology(agenda,
				new ArrayList<>(Arrays.asList(preference1, preference2, preference3)));

		OWLOntology ontology = collective.get();
		System.out.println("Collective ontology : " + ontology);

		assertTrue(Utils.isConsistent(ontology));

		assertTrue(SetUtils.powerSet(new HashSet<>(agenda)).stream()
				.allMatch(set -> (!collective.lexicographicallySmaller(set, ontology.axioms().collect(Collectors.toSet())))
						|| !Utils.isConsistent(set)));
	}

	@Test
	public void testCollectiveGetSix() {

		System.out.println("\n%%% SIX");

		ArrayList<Integer> ranking1 = new ArrayList<>(Arrays.asList(6, 4, 8, 7, 5, 9, 2, 3, 1));
		Preference preference1 = prefFactory.makePreference(ranking1);
		ArrayList<Integer> ranking2 = new ArrayList<>(Arrays.asList(9, 5, 7, 1, 6, 8, 3, 2, 4));
		Preference preference2 = prefFactory.makePreference(ranking2);
		ArrayList<Integer> ranking3 = new ArrayList<>(Arrays.asList(4, 2, 7, 6, 8, 3, 9, 1, 5));
		Preference preference3 = prefFactory.makePreference(ranking3);

		System.out.println("Pref1 : " + preference1);
		System.out.println("Pref2 : " + preference2);
		System.out.println("Pref3 : " + preference3);

		CollectiveReferenceOntology collective = new CollectiveReferenceOntology(agenda,
				new ArrayList<>(Arrays.asList(preference1, preference2, preference3)));

		OWLOntology ontology = collective.get();
		System.out.println("Collective ontology : " + ontology);

		assertTrue(Utils.isConsistent(ontology));

		assertTrue(SetUtils.powerSet(new HashSet<>(agenda)).stream()
				.allMatch(set -> (!collective.lexicographicallySmaller(set, ontology.axioms().collect(Collectors.toSet())))
						|| !Utils.isConsistent(set)));
	}

	@Test
	public void testCollectiveGetRandomRankings() {

		for (int i = 0; i < NUM_RANDOM; i++) {

			System.out.println("\n%%% RANDOM " + i);

			Preference preference1 = prefFactory.makeRandomPreference();
			Preference preference2 = prefFactory.makeRandomPreference();
			Preference preference3 = prefFactory.makeRandomPreference();

			System.out.println("Pref1 : " + preference1);
			System.out.println("Pref2 : " + preference2);
			System.out.println("Pref3 : " + preference3);

			CollectiveReferenceOntology collective = new CollectiveReferenceOntology(agenda,
					new ArrayList<>(Arrays.asList(preference1, preference2, preference3)));

			OWLOntology ontology = collective.get();
			System.out.println("Collective ontology : " + ontology);

			assertTrue(Utils.isConsistent(ontology));

			assertTrue(SetUtils.powerSet(new HashSet<>(agenda)).stream()
					.allMatch(set -> (!collective.lexicographicallySmaller(set, ontology.axioms().collect(Collectors.toSet())))
							|| !Utils.isConsistent(set)));
		}

	}
	
	@Test
	public void testRafaelTest() {
		OWLAxiom ax1 = new OWLSubClassOfAxiomImpl(TOP, entity1, EMPTY_ANNOTATION);
		OWLAxiom ax2 = new OWLSubClassOfAxiomImpl(entity1, BOT, EMPTY_ANNOTATION);
		OWLAxiom ax3 = new OWLSubClassOfAxiomImpl(entity2, entity3, EMPTY_ANNOTATION);
		ArrayList<OWLAxiom> rafaelAgenda = new ArrayList<OWLAxiom>();
		rafaelAgenda.add(ax1);
		rafaelAgenda.add(ax2);
		rafaelAgenda.add(ax3);
		PreferenceFactory rafaelPrefFactory = new PreferenceFactory(rafaelAgenda);

		
		ArrayList<Integer> ranking1 = new ArrayList<>(Arrays.asList(1,2,3));
		Preference preference1 = rafaelPrefFactory.makePreference(ranking1);
		ArrayList<Integer> ranking2 = new ArrayList<>(Arrays.asList(3,1,2));
		Preference preference2 = rafaelPrefFactory.makePreference(ranking2);
		
		
		CollectiveReferenceOntology collective = new CollectiveReferenceOntology(rafaelAgenda,
				new ArrayList<>(Arrays.asList(preference1, preference2)));

		OWLOntology ontology = collective.get();
		System.out.println("Collective ontology : " + ontology);
		
		Set<OWLAxiom> expected = new HashSet<>(Arrays.asList(ax2,ax3));
		
		assertTrue(ontology.axioms().collect(Collectors.toSet()).equals(expected));
	}

}
