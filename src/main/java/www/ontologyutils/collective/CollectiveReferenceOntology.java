package www.ontologyutils.collective;

import java.util.*;
import java.util.stream.IntStream;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.collective.PreferenceFactory.Preference;
import www.ontologyutils.toolbox.*;

/**
 * Daniele Porello, Nicolas Troquard, Rafael Peñaloza, Roberto
 * Confalonieri, Pietro Galliani, and Oliver Kutz. Two Approaches to
 * Ontology Aggregation Based on Axiom Weakening. In 27th International
 * Joint Conference on Artificial Intelligence and 23rd European
 * Conference on Artificial Intelligence (IJCAI-ECAI 2018).
 * International Joint Conferences on Artificial Intelligence
 * Organization, 2018, pages 1942-1948.
 *
 * @author nico
 */
public class CollectiveReferenceOntology {
    private List<OWLAxiom> agenda;
    private ArrayList<Preference> preferences;

    /**
     * @param agenda
     *            a list of axioms.
     * @param preferences
     *            a list of preferences over the agenda, one for every
     *            intended voter.
     */
    public CollectiveReferenceOntology(List<OWLAxiom> agenda, ArrayList<Preference> preferences) {
        for (PreferenceFactory.Preference pref : preferences) {
            if (!agenda.equals(pref.getAgenda())) {
                throw new IllegalStateException(
                        "The agenda of all preferences must coincide with the agenda in parameter");
            }
        }
        this.agenda = agenda;
        this.preferences = preferences;
    }

    /**
     * @return a reference ontology that is a lexicographically minimal consistent
     *         subontology of the agenda
     */
    public Ontology get() {
        Set<Set<OWLAxiom>> mcss = MaximalConsistentSubsets.maximalConsistentSubsets(agenda);
        for (Set<OWLAxiom> set1 : mcss) {
            if (mcss.stream().allMatch(set2 -> !lexicographicallySmaller(set2, set1))) {
                return Ontology.withAxioms(set1);
            }
        }
        return null;
    }

    /**
     * @param set
     * @param n
     * @return the number of preferences in this.preferences whose n-th element
     *         (axiom at rank n) appears in {@code set}
     * @see PreferenceFactory.Preference#get(int rank)
     */
    private int numPrefsWithN(Set<OWLAxiom> set, int n) {
        int result = 0;
        for (Preference pref : preferences) {
            if (set.contains(pref.get(n))) {
                result++;
            }
        }
        return result;
    }

    /**
     * @param set1
     * @param set2
     * @return true if set1 is lexicographically smaller than set2 in this.agenda
     *         with respect to the preference profile this.preferences
     */
    public boolean lexicographicallySmaller(Set<OWLAxiom> set1, Set<OWLAxiom> set2) {
        for (OWLAxiom ax : set1) {
            if (!agenda.contains(ax)) {
                throw new IllegalStateException("The axioms in the first set must all be in the agenda");
            }
        }
        for (OWLAxiom ax : set2) {
            if (!agenda.contains(ax)) {
                throw new IllegalStateException("The axioms in the second set must all be in the agenda");
            }
        }
        for (int i = 1; i <= agenda.size(); i++) {
            if (numPrefsWithN(set1, i) > numPrefsWithN(set2, i)) {
                if (IntStream.range(1, i).allMatch(e -> numPrefsWithN(set1, e) == numPrefsWithN(set2, e))) {
                    return true;
                }
            }
        }
        return false;
    }
}
