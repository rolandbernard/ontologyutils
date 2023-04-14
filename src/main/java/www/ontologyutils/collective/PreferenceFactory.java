package www.ontologyutils.collective;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * An agenda is a list of OWLAxioms, e.g., [ax1,ax2,ax3]. A preference
 * over an agenda is a list of Integers, e.g., [2,3,1]. [2,3,1] says
 * that ax3 (rank 1) is preferred to ax1 (rank 2), which is preferred to
 * ax2 (rank 3). It must not contain duplicates.
 *
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
public class PreferenceFactory {

    private List<OWLAxiom> agenda;

    public PreferenceFactory(List<OWLAxiom> agenda) {
        if ((new HashSet<OWLAxiom>(agenda)).size() != agenda.size()) {
            throw new IllegalArgumentException("The agenda must not contain duplicates");
        }
        this.agenda = agenda;
    }

    public List<OWLAxiom> getAgenda() {
        return agenda;
    }

    public Preference makePreference(List<Integer> ranking) {
        return new Preference(ranking);
    }

    private List<Integer> randomRanking() {

        List<Integer> ranking = new ArrayList<>();
        for (int i = 0; i < agenda.size(); i++) {
            ranking.add(-1);
        }
        for (int k = 1; k <= agenda.size(); k++) {
            int where = ThreadLocalRandom.current().nextInt(0, agenda.size());
            while (ranking.get(where) != -1) {
                where = ThreadLocalRandom.current().nextInt(0, agenda.size());
            }
            ranking.set(where, k);
        }
        return ranking;
    }

    /**
     * @return a random Preference over the agenda
     */
    public Preference makeRandomPreference() {
        return new Preference(randomRanking());
    }

    /**
     * @author nico
     *
     *         An agenda is a list of OWLAxioms, e.g., [ax1,ax2,ax3]. A preference
     *         over an agenda is a list of Integers, e.g., [2,3,1]. [2,3,1] says
     *         that ax3 (rank 1) is preferred to ax1 (rank 2), which is preferred to
     *         ax2 (rank 3). It must not contain duplicates.
     */
    public class Preference {

        private List<Integer> ranking;

        // we forbid direct instantiation
        private Preference() {
        }

        private Preference(List<Integer> ranking) {
            if (ranking.size() != agenda.size()) {
                throw new IllegalArgumentException("A ranking must specify a rank for every axiom of the agenda.");
            }
            for (int i = 1; i <= ranking.size(); i++) {
                if (!ranking.contains(i)) {
                    throw new IllegalArgumentException("A ranking must specificy a rank unique "
                            + "from 1 to agenda.size() to every axiom of the agenda.");
                }
            }
            this.ranking = ranking;
        }

        public int size() {
            return ranking.size();
        }

        public List<Integer> getRanking() {
            return ranking;
        }

        public List<OWLAxiom> getAgenda() {
            return agenda;
        }

        public int getRank(OWLAxiom ax) {
            if (!agenda.contains(ax)) {
                throw new IllegalArgumentException("Argument must be an axiom from the agenda.");
            }
            return ranking.get(agenda.indexOf(ax));
        }

        /**
         * @param rank
         * @return OWLAxiom with rank {@code rank} in this.ranking (the rank-th element
         *         according to the preference)
         */
        public OWLAxiom get(int rank) {
            if (rank < 1 || rank > agenda.size()) {
                throw new IllegalArgumentException("Rank out of bounds.");
            }
            return agenda.get(ranking.indexOf(rank));
        }

        /**
         * @param a1
         * @param a2
         * @return true if a1 is preferred to a2 (a1 is smaller than a2)
         */
        public boolean prefers(OWLAxiom a1, OWLAxiom a2) {
            if (!(agenda.contains(a1) && agenda.contains(a2))) {
                throw new IllegalStateException("The axioms in parameter must be in the agenda.");
            }
            return ranking.get(agenda.indexOf(a1)) < ranking.get(agenda.indexOf(a2));
        }

        /**
         * @param set1
         * @param set2
         * @return true if set1 is lexicographically smaller than set2
         */
        public boolean prefers(Set<OWLAxiom> set1, Set<OWLAxiom> set2) {
            for (OWLAxiom ax : set1) {
                if (!agenda.contains(ax)) {
                    throw new IllegalStateException("The axioms in the first set must all be in the agenda.");
                }
            }
            for (OWLAxiom ax : set2) {
                if (!agenda.contains(ax)) {
                    throw new IllegalStateException("The axioms in the second set must all be in the agenda.");
                }
            }

            // set1 is lexicographically smaller than set2 if
            // there is an axiom axe in set1...
            return set1.stream().anyMatch(axe ->
            // ... that is not in set2...
            (!set2.contains(axe) &&
            // ... and every axiom ax...
                    agenda.stream().allMatch(ax ->
                    // ... is not smaller than axe
                    (!prefers(ax, axe)
                            // ... or is in set1 inter set2...
                            || set1.contains(ax) && set2.contains(ax)
                            // ... or is not in set1 union set2
                            || (!set1.contains(ax) && !set2.contains(ax))))));

        }

        @Override
        public String toString() {
            return ranking.toString();
        }

    } // End of Preference

}
