package www.ontologyutils.collective;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.collective.PreferenceFactory.Preference;
import www.ontologyutils.toolbox.*;

/**
 * @author nico
 */
public class CollectiveReferenceOntologyTest {
    static OWLDataFactory df = Ontology.getDefaultDataFactory();
    static OWLClassExpression entity1 = (OWLClassExpression) df.getOWLEntity(EntityType.CLASS,
            IRI.create("www.first.org"));
    static OWLClassExpression entity2 = df.getOWLEntity(EntityType.CLASS, IRI.create("www.second.org"));
    static OWLClassExpression entity3 = df.getOWLEntity(EntityType.CLASS, IRI.create("www.third.org"));
    static OWLClassExpression entity4 = df.getOWLEntity(EntityType.CLASS, IRI.create("www.fourth.org"));

    static OWLIndividual indy1 = df.getOWLNamedIndividual(IRI.create("www.indy-one.org"));
    static OWLIndividual indy2 = df.getOWLNamedIndividual(IRI.create("www.indy-two.org"));

    static OWLAxiom ax1 = df.getOWLSubClassOfAxiom(entity1, entity2);
    static OWLAxiom ax2 = df.getOWLSubClassOfAxiom(entity2, entity3);
    static OWLAxiom ax3 = df.getOWLSubClassOfAxiom(entity3, entity4);
    static OWLAxiom ax4 = df.getOWLSubClassOfAxiom(entity4, entity1);
    static OWLAxiom ax5 = df.getOWLSubClassOfAxiom(df.getOWLThing(), entity1);
    static OWLAxiom ax6 = df.getOWLSubClassOfAxiom(entity4, df.getOWLNothing());
    static OWLAxiom ax7 = df.getOWLClassAssertionAxiom(entity1, indy1);
    static OWLAxiom ax8 = df.getOWLClassAssertionAxiom(entity2, indy2);
    static OWLAxiom ax9 = df.getOWLSubClassOfAxiom(entity2, df.getOWLNothing());

    static PreferenceFactory prefFactory;
    static ArrayList<OWLAxiom> agenda;

    static final int NUM_RANDOM = 15;

    public CollectiveReferenceOntologyTest() {
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
        prefFactory = new PreferenceFactory(agenda);
    }

    @Test
    public void testMaximalConsistentSets() {
        Set<OWLAxiom> agendaSet = new HashSet<>(agenda);
        Set<Set<OWLAxiom>> results = MaximalConsistentSets.maximalConsistentSubsets(agendaSet);
        assertTrue(results.stream()
                .allMatch(subset -> MaximalConsistentSets.isMaximallyConsistentSubset(subset,
                        agendaSet)));
        assertTrue(Utils.powerSet(agendaSet).allMatch(
                subset -> (!MaximalConsistentSets.isMaximallyConsistentSubset(subset, agendaSet)
                        || results.contains(subset))));
    }

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
        ArrayList<Integer> ranking1 = new ArrayList<>(Arrays.asList(2, 1, 3, 4, 5, 6, 7, 8, 9));
        Preference preference1 = prefFactory.makePreference(ranking1);
        ArrayList<Integer> ranking2 = new ArrayList<>(Arrays.asList(3, 2, 1, 4, 6, 5, 7, 8, 9));
        Preference preference2 = prefFactory.makePreference(ranking2);
        ArrayList<Integer> ranking3 = new ArrayList<>(Arrays.asList(2, 5, 1, 6, 4, 3, 7, 8, 9));
        Preference preference3 = prefFactory.makePreference(ranking3);

        CollectiveReferenceOntology collective = new CollectiveReferenceOntology(agenda,
                new ArrayList<>(Arrays.asList(preference1, preference2, preference3)));

        Ontology ontology = collective.get();
        assertTrue(ontology.isConsistent());
        assertTrue(Utils.powerSet(new HashSet<>(agenda))
                .allMatch(set -> (!collective.lexicographicallySmaller(set,
                        ontology.axioms().collect(Collectors.toSet())))
                        || !Utils.isConsistent(set)));
    }

    @Test
    public void testCollectiveGetTwo() {
        ArrayList<Integer> ranking1 = new ArrayList<>(Arrays.asList(2, 7, 3, 4, 5, 6, 1, 8, 9));
        Preference preference1 = prefFactory.makePreference(ranking1);
        ArrayList<Integer> ranking2 = new ArrayList<>(Arrays.asList(3, 2, 7, 4, 6, 5, 1, 8, 9));
        Preference preference2 = prefFactory.makePreference(ranking2);
        ArrayList<Integer> ranking3 = new ArrayList<>(Arrays.asList(2, 5, 7, 6, 4, 3, 1, 8, 9));
        Preference preference3 = prefFactory.makePreference(ranking3);

        CollectiveReferenceOntology collective = new CollectiveReferenceOntology(agenda,
                new ArrayList<>(Arrays.asList(preference1, preference2, preference3)));

        Ontology ontology = collective.get();

        assertTrue(ontology.isConsistent());

        assertTrue(Utils.powerSet(new HashSet<>(agenda))
                .allMatch(set -> (!collective.lexicographicallySmaller(set,
                        ontology.axioms().collect(Collectors.toSet())))
                        || !Utils.isConsistent(set)));
    }

    @Test
    public void testCollectiveGetThree() {
        ArrayList<Integer> ranking1 = new ArrayList<>(Arrays.asList(1, 4, 3, 7, 5, 6, 2, 8, 9));
        Preference preference1 = prefFactory.makePreference(ranking1);
        ArrayList<Integer> ranking2 = new ArrayList<>(Arrays.asList(3, 2, 7, 4, 6, 5, 1, 8, 9));
        Preference preference2 = prefFactory.makePreference(ranking2);
        ArrayList<Integer> ranking3 = new ArrayList<>(Arrays.asList(2, 5, 7, 6, 4, 3, 1, 8, 9));
        Preference preference3 = prefFactory.makePreference(ranking3);

        CollectiveReferenceOntology collective = new CollectiveReferenceOntology(agenda,
                new ArrayList<>(Arrays.asList(preference1, preference2, preference3)));

        Ontology ontology = collective.get();

        assertTrue(ontology.isConsistent());

        assertTrue(Utils.powerSet(new HashSet<>(agenda))
                .allMatch(set -> (!collective.lexicographicallySmaller(set,
                        ontology.axioms().collect(Collectors.toSet())))
                        || !Utils.isConsistent(set)));
    }

    @Test
    public void testCollectiveGetFour() {
        ArrayList<Integer> ranking1 = new ArrayList<>(Arrays.asList(6, 4, 3, 7, 5, 1, 2, 8, 9));
        Preference preference1 = prefFactory.makePreference(ranking1);
        ArrayList<Integer> ranking2 = new ArrayList<>(Arrays.asList(3, 5, 7, 4, 6, 2, 1, 8, 9));
        Preference preference2 = prefFactory.makePreference(ranking2);
        ArrayList<Integer> ranking3 = new ArrayList<>(Arrays.asList(3, 5, 7, 6, 4, 1, 2, 8, 9));
        Preference preference3 = prefFactory.makePreference(ranking3);

        CollectiveReferenceOntology collective = new CollectiveReferenceOntology(agenda,
                new ArrayList<>(Arrays.asList(preference1, preference2, preference3)));

        Ontology ontology = collective.get();

        assertTrue(ontology.isConsistent());

        assertTrue(Utils.powerSet(new HashSet<>(agenda))
                .allMatch(set -> (!collective.lexicographicallySmaller(set,
                        ontology.axioms().collect(Collectors.toSet())))
                        || !Utils.isConsistent(set)));
    }

    @Test
    public void testCollectiveGetFive() {
        ArrayList<Integer> ranking1 = new ArrayList<>(Arrays.asList(6, 4, 8, 7, 5, 9, 2, 3, 1));
        Preference preference1 = prefFactory.makePreference(ranking1);
        ArrayList<Integer> ranking2 = new ArrayList<>(Arrays.asList(9, 5, 8, 4, 6, 7, 3, 2, 1));
        Preference preference2 = prefFactory.makePreference(ranking2);
        ArrayList<Integer> ranking3 = new ArrayList<>(Arrays.asList(4, 5, 7, 6, 8, 3, 9, 1, 2));
        Preference preference3 = prefFactory.makePreference(ranking3);

        CollectiveReferenceOntology collective = new CollectiveReferenceOntology(agenda,
                new ArrayList<>(Arrays.asList(preference1, preference2, preference3)));

        Ontology ontology = collective.get();

        assertTrue(ontology.isConsistent());

        assertTrue(Utils.powerSet(new HashSet<>(agenda))
                .allMatch(set -> (!collective.lexicographicallySmaller(set,
                        ontology.axioms().collect(Collectors.toSet())))
                        || !Utils.isConsistent(set)));
    }

    @Test
    public void testCollectiveGetSix() {
        ArrayList<Integer> ranking1 = new ArrayList<>(Arrays.asList(6, 4, 8, 7, 5, 9, 2, 3, 1));
        Preference preference1 = prefFactory.makePreference(ranking1);
        ArrayList<Integer> ranking2 = new ArrayList<>(Arrays.asList(9, 5, 7, 1, 6, 8, 3, 2, 4));
        Preference preference2 = prefFactory.makePreference(ranking2);
        ArrayList<Integer> ranking3 = new ArrayList<>(Arrays.asList(4, 2, 7, 6, 8, 3, 9, 1, 5));
        Preference preference3 = prefFactory.makePreference(ranking3);

        CollectiveReferenceOntology collective = new CollectiveReferenceOntology(agenda,
                new ArrayList<>(Arrays.asList(preference1, preference2, preference3)));

        Ontology ontology = collective.get();

        assertTrue(ontology.isConsistent());

        assertTrue(Utils.powerSet(new HashSet<>(agenda))
                .allMatch(set -> (!collective.lexicographicallySmaller(set,
                        ontology.axioms().collect(Collectors.toSet())))
                        || !Utils.isConsistent(set)));
    }

    @Test
    public void testCollectiveGetRandomRankings() {
        for (int i = 0; i < NUM_RANDOM; i++) {
            Preference preference1 = prefFactory.makeRandomPreference();
            Preference preference2 = prefFactory.makeRandomPreference();
            Preference preference3 = prefFactory.makeRandomPreference();

            CollectiveReferenceOntology collective = new CollectiveReferenceOntology(agenda,
                    new ArrayList<>(Arrays.asList(preference1, preference2, preference3)));

            Ontology ontology = collective.get();

            assertTrue(ontology.isConsistent());

            assertTrue(Utils.powerSet(new HashSet<>(agenda))
                    .allMatch(set -> (!collective.lexicographicallySmaller(set,
                            ontology.axioms().collect(Collectors.toSet())))
                            || !Utils.isConsistent(set)));
        }

    }

    @Test
    public void testRafaelTest() {
        OWLAxiom ax1 = df.getOWLSubClassOfAxiom(df.getOWLThing(), entity1);
        OWLAxiom ax2 = df.getOWLSubClassOfAxiom(entity1, df.getOWLNothing());
        OWLAxiom ax3 = df.getOWLSubClassOfAxiom(entity2, entity3);
        ArrayList<OWLAxiom> rafaelAgenda = new ArrayList<OWLAxiom>();
        rafaelAgenda.add(ax1);
        rafaelAgenda.add(ax2);
        rafaelAgenda.add(ax3);
        PreferenceFactory rafaelPrefFactory = new PreferenceFactory(rafaelAgenda);

        ArrayList<Integer> ranking1 = new ArrayList<>(Arrays.asList(1, 2, 3));
        Preference preference1 = rafaelPrefFactory.makePreference(ranking1);
        ArrayList<Integer> ranking2 = new ArrayList<>(Arrays.asList(3, 1, 2));
        Preference preference2 = rafaelPrefFactory.makePreference(ranking2);

        CollectiveReferenceOntology collective = new CollectiveReferenceOntology(rafaelAgenda,
                new ArrayList<>(Arrays.asList(preference1, preference2)));

        Ontology ontology = collective.get();

        Set<OWLAxiom> expected = new HashSet<>(Arrays.asList(ax2, ax3));

        assertTrue(ontology.axioms().collect(Collectors.toSet()).equals(expected));
    }

}
