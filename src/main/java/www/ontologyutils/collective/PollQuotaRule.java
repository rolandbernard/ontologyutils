package www.ontologyutils.collective;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;

import www.ontologyutils.collective.BinaryVoteFactory.BinaryVote;

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
public class PollQuotaRule {
    List<OWLAxiom> agenda;
    List<Integer> quotas;
    List<BinaryVote> votes;

    public enum NamedUniformQuotaRule {
        MAJORITY, UNANIMITY;
    }

    /**
     * Creates an instance of a vote or poll, over the set of axioms in
     * {@code agenda}, with the list {@code quotas} of quotas, and {@code votes} as
     * list of votes over the agenda.
     *
     * @param agenda
     * @param quotas
     * @param votes
     */
    public PollQuotaRule(List<OWLAxiom> agenda, List<Integer> quotas, List<BinaryVote> votes) {
        if (agenda.size() != quotas.size()) {
            throw new IllegalArgumentException("Exactly one quota must be specified for every axiom in the agenda.");
        }
        if (votes.stream().anyMatch(bv -> !bv.getAgenda().equals(agenda))) {
            throw new IllegalArgumentException("The ballots must be built from the agenda in parameter.");
        }
        this.agenda = agenda;
        this.votes = votes;
        this.quotas = quotas;
    }

    /**
     * @param agenda
     * @param uniformQuotaRule
     * @param votes
     * @return The array of quotas corresponding to the uniform quota rule
     *         designated by {@code uniformQuotaRule}. Since the quota is uniform,
     *         all the quotas in the array are equal. The exact value depends on the
     *         specific rule and the number of votes. The size of the array is the
     *         same as the size of the agenda.
     */
    private static List<Integer> getNamedUniformRuleQuotas(List<OWLAxiom> agenda,
            NamedUniformQuotaRule uniformQuotaRule, List<BinaryVote> votes) {
        int numVoters = votes.size();
        int quota = 0;
        switch (uniformQuotaRule) {
            case MAJORITY:
                quota = (int) Math.ceil((numVoters + 1) / 2);
                break;
            case UNANIMITY:
                quota = numVoters;
                break;
        }
        return new ArrayList<>(Collections.nCopies(agenda.size(), quota));
    }

    /**
     * @param agenda
     * @param name
     * @param votes
     */
    public PollQuotaRule(List<OWLAxiom> agenda, NamedUniformQuotaRule name, List<BinaryVote> votes) {
        this(agenda, getNamedUniformRuleQuotas(agenda, name, votes), votes);
    }

    /**
     * Creates a uniform quota rule with the quota of every axiom in the agenda as
     * specified by the parameter {@code uniformQuota}.
     *
     * @param agenda
     * @param uniformQuota
     * @param votes
     */
    public PollQuotaRule(List<OWLAxiom> agenda, int uniformQuota, List<BinaryVote> votes) {
        this(agenda, new ArrayList<>(Collections.nCopies(agenda.size(), uniformQuota)), votes);
    }

    /**
     * @return the set of axioms that result from the poll.
     */
    public Set<OWLAxiom> result() {
        List<Integer> scores = new ArrayList<>(Collections.nCopies(agenda.size(), 0));
        for (BinaryVote v : votes) {
            for (int i = 0; i < v.getBallot().size(); i++) {
                scores.set(i, scores.get(i) + v.getBallot().get(i));
            }
        }
        Set<OWLAxiom> result = new HashSet<>();
        for (int i = 0; i < agenda.size(); i++) {
            if (scores.get(i) >= quotas.get(i)) {
                result.add(agenda.get(i));
            }
        }
        return result;
    }
}
