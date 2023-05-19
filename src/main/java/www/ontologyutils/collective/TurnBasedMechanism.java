package www.ontologyutils.collective;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.collective.BinaryVoteFactory.BinaryVote;
import www.ontologyutils.collective.PreferenceFactory.Preference;
import www.ontologyutils.refinement.AxiomWeakener;
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
public class TurnBasedMechanism {
    private List<OWLAxiom> agenda;
    private List<Preference> preferences;
    private List<BinaryVote> approvals;
    private Ontology referenceOntology;
    private int numVoters;

    private boolean verbose = false;

    private void log(String message) {
        if (verbose) {
            System.out.print(message);
        }
    }

    /**
     * @param verbose
     *            a boolean, true for verbose logging, and false for silent
     *            execution.
     * @return the current instance.
     */
    public TurnBasedMechanism setVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    /**
     * @param agenda
     *            a list of axioms.
     * @param preferences
     *            a list of preferences over the agenda, one for every
     *            intended voter.
     * @param approvals
     *            a list of binary votes over the agenda, one for
     *            every intended voter.
     * @param referenceOntology
     *            a consistent reference ontology.
     */
    public TurnBasedMechanism(List<OWLAxiom> agenda, List<Preference> preferences, List<BinaryVote> approvals,
            Ontology referenceOntology) {
        if (preferences.stream().anyMatch(p -> !p.getAgenda().equals(agenda))) {
            throw new IllegalArgumentException("The preferences must be built from the agenda in parameter.");
        }
        if (approvals.stream().anyMatch(a -> !a.getAgenda().equals(agenda))) {
            throw new IllegalArgumentException(
                    "The binary votes in the approvals must be built from the agenda in parameter.");
        }
        if (preferences.size() != approvals.size()) {
            throw new IllegalArgumentException("There must be as many preferences as approvals.");
        }
        if (!referenceOntology.isConsistent()) {
            throw new IllegalArgumentException("The reference ontology must be consistent.");
        }
        this.referenceOntology = referenceOntology;
        for (int i = 0; i < numVoters; i++) {
            for (int rank = 1; rank < agenda.size(); rank++) {
                int approvalRank = approvals.get(i).getVote(preferences.get(i).get(rank));
                int approvalRankPlusOne = approvals.get(i).getVote(preferences.get(i).get(rank + 1));
                if (approvalRank < approvalRankPlusOne) {
                    throw new IllegalArgumentException(
                            "Approvals must be coherent with preferences. Here : in preference " + i + " axiom ranked "
                                    + (rank + 1) + " has better approval than axiom ranked " + rank);
                }
            }
        }
        this.approvals = approvals;
        this.agenda = agenda;
        this.preferences = preferences;
        this.numVoters = preferences.size();
    }

    /**
     * @param axioms
     *            a list of axioms.
     * @param pref
     *            a preference over {@code axioms}.
     * @param vote
     *            a binary vote over {@code axioms}.
     * @return The preferred axiom in {@code axioms} according to {@code pref} that
     *         is approved in {@code vote}. Sometimes it returns {@code null}.
     */
    private OWLAxiom favorite(List<OWLAxiom> axioms, Preference pref, BinaryVote vote) {
        List<OWLAxiom> approvedAxioms = new ArrayList<>();
        for (OWLAxiom a : axioms) {
            if (vote.getVote(a) == 1) {
                approvedAxioms.add(a);
            }
        }
        OWLAxiom result = null;
        if (!approvedAxioms.isEmpty()) {
            result = approvedAxioms.get(0);
        } else {
            return null;
        }
        assert (result != null);
        for (OWLAxiom a : approvedAxioms) {
            if (pref.getRank(a) < pref.getRank(result)) {
                result = a;
            }
        }
        return result;
    }

    /**
     * @param ax
     *            The axiom to test.
     * @return true if axiom {code ax} is approved in at least one of the binary
     *         votes in {@code approvals}.
     */
    private boolean hasSupport(OWLAxiom ax) {
        return approvals.stream().anyMatch(bv -> (bv.getVote(ax) == 1));
    }

    /**
     * Enum for the type of initialization.
     */
    public enum Initialization {
        /**
         * Initialize with empty ontology.
         */
        EMPTY,
        /**
         * Initialize with the reference ontology.
         */
        REFERENCE,
        /**
         * Initialize with the support axioms of the reference ontology.
         */
        REFERENCE_WITH_SUPPORT;
    }

    /**
     * @param initizalization
     *            The kind of initialization to use.
     * @return the reference ontology, the accepted subset of the reference
     *         ontology, or an empty ontology depending on {@code initialization}
     *         parameter.
     */
    private Ontology init(Initialization initialization) {
        Ontology result = null;
        switch (initialization) {
            case EMPTY:
                result = Ontology.emptyOntology();
                break;
            case REFERENCE:
                result = referenceOntology.clone();
                break;
            case REFERENCE_WITH_SUPPORT:
                result = referenceOntology.clone();
                for (OWLAxiom ax : Utils.toList(referenceOntology.axioms())) {
                    if (!hasSupport(ax)) {
                        result.removeAxioms(ax);
                    }
                }
                break;
        }
        return result;
    }

    /**
     * @param initialization
     *            the mechanism will be initialized with either
     *            <ul>
     *            <li>the reference ontology when using
     *            {@code REFERENCE},</li>
     *            <li>the accepted subset of the reference ontology when
     *            using {@code REFERENCE_WITH_SUPPORT},</li>
     *            <li>or an empty ontology when using {@code EMPTY}.</li>
     *            </ul>
     * @return the ontology resulting from the turn based mechanism.
     */
    public Ontology get(Initialization initialization) {
        Ontology result = init(initialization);
        List<OWLAxiom> currentAgenda = new ArrayList<>();
        currentAgenda.addAll(Utils.toList(agenda.stream()));
        // trim the current agenda from the axioms already in the reference ontology
        result.axioms().forEach(a -> currentAgenda.remove(a));
        // init turn
        int currentVoter = 0;
        Set<Integer> haveGivenUp = new HashSet<>();
        while (!currentAgenda.isEmpty() && haveGivenUp.size() < numVoters) {
            log("\nCurrent voter: " + (currentVoter + 1));
            // currentVoter's favorite axiom still in the current agenda
            OWLAxiom favorite = favorite(currentAgenda, preferences.get(currentVoter), approvals.get(currentVoter));
            if (favorite == null) {
                // currentVoter does not approve of any axioms remaining in currentAgenda
                haveGivenUp.add(currentVoter);
                log("\nVoter " + (currentVoter + 1) + " gives up!");
                currentVoter = (currentVoter + 1) % numVoters;
                continue;
            }
            log("\nNext accepted favorite axiom: " + favorite);
            // discard axiom favorite from the agenda
            currentAgenda.remove(favorite);
            try (var currentAxioms = result.clone()) {
                currentAxioms.addAxioms(favorite);
                while (!currentAxioms.isConsistent()) {
                    log("\n** Weakening. **");
                    // weakening of favorite axiom
                    AxiomWeakener axiomWeakener = new AxiomWeakener(referenceOntology);
                    currentAxioms.removeAxioms(favorite);
                    List<OWLAxiom> weakerAxioms = Utils.toList(axiomWeakener.weakerAxioms(favorite));
                    int randomPick = ThreadLocalRandom.current().nextInt(0, weakerAxioms.size());
                    favorite = weakerAxioms.get(randomPick);
                    currentAxioms.addAxioms(favorite);
                }
            }
            log("\nAdding axiom: " + favorite);
            result.addAxioms(favorite);
            // next turn
            currentVoter = (currentVoter + 1) % numVoters;
        }
        if (currentAgenda.isEmpty()) {
            log("\n-- End of procedure: all axioms of the agenda have been considered.\n");
        }
        if (haveGivenUp.size() >= numVoters) {
            log("\n-- End of procedure: all voters have given up.\n");
        }
        return result;
    }
}
