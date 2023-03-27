package www.ontologyutils.collective;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import www.ontologyutils.toolbox.MaximalConsistentSets;
import www.ontologyutils.toolbox.Utils;

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

    public BinaryVoteFactory(List<OWLAxiom> agenda) {
        if ((new HashSet<OWLAxiom>(agenda)).size() != agenda.size()) {
            throw new IllegalArgumentException("The agenda must not contain duplicates");
        }
        this.agenda = agenda;
    }

    public List<OWLAxiom> getAgenda() {
        return agenda;
    }

    public BinaryVote makeBinaryVote(List<Integer> ballot) {
        return new BinaryVote(ballot);
    }

    private List<Integer> randomBallot(float probablePositivity) {
        if (probablePositivity < 0 || probablePositivity > 1) {
            throw new IllegalArgumentException("The value of probable positivity must be between 0 and 1");
        }

        Set<Set<OWLAxiom>> mcss = MaximalConsistentSets.maximalConsistentSubsets(new HashSet<>(agenda));
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

    public BinaryVote makeRandomBinaryVote(float probablePositivity) {
        return new BinaryVote(randomBallot(probablePositivity));
    }

    public BinaryVote makeRandomBinaryVote() {
        return new BinaryVote(randomBallot(0.5f));
    }

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
            Set<OWLAxiom> selectedAxioms = new HashSet<>();

            for (int i = 0; i < agenda.size(); i++) {
                if (ballot.get(i) == 1) {
                    selectedAxioms.add(agenda.get(i));
                }
            }
            if (!Utils.isConsistent(selectedAxioms)) {
                throw new IllegalArgumentException(
                        "A binary vote must specify a selection of a consistent set of axioms from the agenda.");
            }

            this.ballot = ballot;
        }

        public List<Integer> getBallot() {
            return ballot;
        }

        public List<OWLAxiom> getAgenda() {
            return agenda;
        }

        public Set<OWLAxiom> getBallotAxioms() {
            Set<OWLAxiom> selectedAxioms = new HashSet<>();
            for (int i = 0; i < agenda.size(); i++) {
                if (ballot.get(i) == 1) {
                    selectedAxioms.add(agenda.get(i));
                }
            }
            return selectedAxioms;
        }

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

        public OWLOntology getOnto() {
            Set<OWLAxiom> axioms = this.getBallotAxioms();
            OWLOntology onto = Utils.newOntology(axioms.stream());
            if (!Utils.isConsistent(onto)) {
                System.out.println("getOnto returns inconsistent ontology!");
            }
            return onto;
        }

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
