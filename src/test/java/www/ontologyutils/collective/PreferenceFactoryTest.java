package www.ontologyutils.collective;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.*;

/**
 * @author nico
 */
public class PreferenceFactoryTest {
    static OWLDataFactory df = Ontology.getDefaultDataFactory();
    static OWLClassExpression entity1 = (OWLClassExpression) df.getOWLEntity(EntityType.CLASS,
            IRI.create("www.first.org"));
    static OWLClassExpression entity2 = df.getOWLEntity(EntityType.CLASS, IRI.create("www.second.org"));
    static OWLClassExpression entity3 = df.getOWLEntity(EntityType.CLASS, IRI.create("www.third.org"));
    static OWLClassExpression entity4 = df.getOWLEntity(EntityType.CLASS, IRI.create("www.fourth.org"));

    static OWLAxiom ax1 = df.getOWLSubClassOfAxiom(entity1, entity2);
    static OWLAxiom ax2 = df.getOWLSubClassOfAxiom(entity2, entity3);
    static OWLAxiom ax3 = df.getOWLSubClassOfAxiom(entity3, entity4);
    static OWLAxiom ax4 = df.getOWLSubClassOfAxiom(entity4, entity1);

    static PreferenceFactory prefFactory;
    static PreferenceFactory.Preference preference;

    public PreferenceFactoryTest() {
        ArrayList<OWLAxiom> agenda = new ArrayList<OWLAxiom>();
        agenda.add(ax1);
        agenda.add(ax2);
        agenda.add(ax3);
        agenda.add(ax4);

        prefFactory = new PreferenceFactory(agenda);
    }

    @Test
    public void testBadRanking() {
        assertThrows(IllegalArgumentException.class, () -> {
            ArrayList<Integer> ranking = new ArrayList<>(Arrays.asList(2, 3, 1));
            preference = prefFactory.makePreference(ranking);
        });
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

    @Test
    public void testBadRankIndex() {
        assertThrows(IllegalArgumentException.class, () -> {
            ArrayList<Integer> ranking = new ArrayList<>(Arrays.asList(2, 3, 1, 4));
            preference = prefFactory.makePreference(ranking);
            // ax3 < ax1 < ax2 < ax4 (smaller is better)
            preference.get(5);
        });
    }
}
