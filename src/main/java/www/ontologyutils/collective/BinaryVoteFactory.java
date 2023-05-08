package www.ontologyutils.collective;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.semanticweb.owlapi.model.OWLAxiom;

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
public class BinaryVoteFactory {
    private List<OWLAxiom> agenda;

    /**
     * @param agenda
     *            The axiom agenda.
     */
    public BinaryVoteFactory(List<OWLAxiom> agenda) {
        if ((new HashSet<OWLAxiom>(agenda)).size() != agenda.size()) {
            throw new IllegalArgumentException("The agenda must not contain duplicates");
        }
        this.agenda = agenda;
    }

    /**
     * @return The agenda of the vote factory.
     */
    public List<OWLAxiom> getAgenda() {
        return agenda;
    }

    /**
     * @param ballot
     *            The ballot.
     * @return A {@code BinaryVote}
     */
    public BinaryVote makeBinaryVote(List<Integer> ballot) {
        return new BinaryVote(ballot);
    }

    private List<Integer> randomBallot(float probablePositivity) {
        if (probablePositivity < 0 || probablePositivity > 1) {
            throw new IllegalArgumentException("The value of probable positivity must be between 0 and 1");
        }

        Set<Set<OWLAxiom>> mcss = MaximalConsistentSubsets.maximalConsistentSubsets(new HashSet<>(agenda));
        int which = ThreadLocalRandom.current().nextInt(0, mcss.size());

        // the ballot will be a subset of the mcs chosen next randomly
        Set<OWLAxiom> mcs = new ArrayList<>(mcss).get(which);

        List<Integer> result = new ArrayList<>(Collections.nCopies(agenda.size(), 0));
        for (OWLAxiom ax : mcs) {
            if (ThreadLocalRandom.current().nextInt(0, Math.round(1 / probablePositivity)) == 0) {
                result.set(agenda.indexOf(ax), 1);
            }
        }
        return result;
    }

    /**
     * @param probablePositivity
     *            Probable positivity used in ballot generation.
     * @return The generated binary vote.
     */
    public BinaryVote makeRandomBinaryVote(float probablePositivity) {
        return new BinaryVote(randomBallot(probablePositivity));
    }

    /**
     * Generate with {@code probablePositivity} 0.5.
     *
     * @return The generated binary vote.
     */
    public BinaryVote makeRandomBinaryVote() {
        return new BinaryVote(randomBallot(0.5f));
    }

    /**
     * Class representing a binary vote.
     */
    public class BinaryVote {
        private List<Integer> ballot;

        // we forbid direct instantiation
        private BinaryVote() {
        }

        private BinaryVote(List<Integer> ballot) {
            if (ballot.size() != agenda.size() || ballot.stream().anyMatch(k -> k != 0 && k != 1)) {
                throw new IllegalArgumentException(
                        "A binary vote must specify a 0/1 decision for every axiom of the agenda");
            }
            // check consistency
            try (var selectedAxioms = Ontology.emptyOntology()) {
                for (int i = 0; i < agenda.size(); i++) {
                    if (ballot.get(i) == 1) {
                        selectedAxioms.addAxioms(agenda.get(i));
                    }
                }
                if (!selectedAxioms.isConsistent()) {
                    throw new IllegalArgumentException(
                            "A binary vote must specify a selection of a consistent set of axioms from the agenda.");
                }
            }
            this.ballot = ballot;
        }

        /**
         * @return The ballot of this vote.
         */
        public List<Integer> getBallot() {
            return ballot;
        }

        /**
         * @return The agenda of the vote.
         */
        public List<OWLAxiom> getAgenda() {
            return agenda;
        }

        /**
         * @return The axioms from the agenda selected by the ballot.
         */
        public Set<OWLAxiom> getBallotAxioms() {
            Set<OWLAxiom> selectedAxioms = new HashSet<>();
            for (int i = 0; i < agenda.size(); i++) {
                if (ballot.get(i) == 1) {
                    selectedAxioms.add(agenda.get(i));
                }
            }
            return selectedAxioms;
        }

        /**
         * @return The preference map.
         */
        public HashMap<OWLAxiom, Float> getBallotAxiomsPreferences() {
            HashMap<OWLAxiom, Float> selectedAxiomsPreferences = new HashMap<OWLAxiom, Float>();
            List<Float> preferences = new ArrayList<Float>();
            int selectedAxiom = 0;
            for (int i = 0; i < agenda.size(); i++) {
                if (ballot.get(i) == 1) {

                    selectedAxiom++;
                    selectedAxiomsPreferences.put(agenda.get(i), (float) (1) / (float) selectedAxiom);
                    preferences.add((float) (1) / (float) selectedAxiom);
                }
            }
            System.out.println("///getBallotAxiomsPreferences");
            ballot.stream().forEach(System.out::println);
            preferences.stream().forEach(System.out::println);
            System.out.println("///getBallotAxiomsPreferences");
            return selectedAxiomsPreferences;
        }

        /**
         * @return The ontology with the ballot axioms of this vote.
         */
        public Ontology getOnto() {
            Set<OWLAxiom> axioms = this.getBallotAxioms();
            Ontology onto = Ontology.withAxioms(axioms);
            if (!onto.isConsistent()) {
                System.out.println("getOnto returns inconsistent ontology!");
            }
            return onto;
        }

        /**
         * @param ax
         *            The axiom for which to get the vote value.
         * @return The vote value on the ballot for this axiom.
         */
        public int getVote(OWLAxiom ax) {
            if (!agenda.contains(ax)) {
                throw new IllegalArgumentException("Argument must be an axiom from the agenda");
            }
            return ballot.get(agenda.indexOf(ax));
        }

        @Override
        public String toString() {
            return ballot.toString();
        }
    } // End BinaryVote
}
