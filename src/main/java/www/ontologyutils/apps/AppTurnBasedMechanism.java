package www.ontologyutils.apps;

import java.io.*;
import java.util.*;
import java.util.stream.*;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.collective.*;
import www.ontologyutils.collective.BinaryVoteFactory.BinaryVote;
import www.ontologyutils.collective.PreferenceFactory.Preference;
import www.ontologyutils.collective.TurnBasedMechanism.Initialization;
import www.ontologyutils.normalization.NormalizationTools;
import www.ontologyutils.toolbox.Ontology;

/**
 * @author nico
 */
public class AppTurnBasedMechanism {
    private static int MAX_VOTERS = 1000;

    private Ontology ontology;

    public AppTurnBasedMechanism(String ontologyFilePath) {

        Ontology base = Ontology.loadOnlyLogicalAxioms(ontologyFilePath);

        ontology = Ontology.emptyOntology();

        ontology.addAxioms(base.aboxAxioms().collect(Collectors.toSet()));
        base.tboxAxioms().forEach(a -> ontology.addAxioms(NormalizationTools.asSubClassOfAxioms(a)));
    }

    private static int readNumber(String query, int min, int max) {
        int num;
        while (true) {
            System.out.print(query + " > ");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            try {
                num = Integer.parseInt(br.readLine());
            } catch (NumberFormatException e) {
                continue;
            } catch (IOException e) {
                continue;
            }
            if (num < min || num > max) {
                continue;
            }
            return num;
        }
    }

    /**
     * @param args
     *            One arguments must be given. It must correspond to an OWL
     *            ontology file path. E.g., run with the parameter
     *            ./resources/inconsistent-leftpolicies.owl.
     */
    public static void main(String[] args) {
        long time = System.currentTimeMillis();

        AppTurnBasedMechanism mApp = new AppTurnBasedMechanism(args[0]);

        if (mApp.ontology.isConsistent()) {
            System.out.println("\n--- The ontology is consistent.");
        } else {
            System.out.println("\n--- The ontology is not consistent.");
        }

        // Agenda
        ArrayList<OWLAxiom> agenda = new ArrayList<>(mApp.ontology.axioms().collect(Collectors.toList()));

        // Voters
        System.out.println("\n--- Voters.");
        int numVoters = readNumber("How many voters?", 1, MAX_VOTERS);

        // Preferences
        System.out.println("\n--- Preferences.");
        ArrayList<Preference> preferences = new ArrayList<>();
        System.out.println("- This is the agenda:");
        PreferenceFactory prefFactory = new PreferenceFactory(agenda);
        for (int i = 0; i < prefFactory.getAgenda().size(); i++) {
            System.out.println((i + 1) + " : " + prefFactory.getAgenda().get(i));
        }
        for (int i = 0; i < numVoters; i++) {
            System.out.println("- Preferences voter " + (i + 1));
            List<Integer> ranking = Stream.generate(String::new).limit(prefFactory.getAgenda().size()).map(s -> 0)
                    .collect(Collectors.toList());
            for (int j = 1; j <= prefFactory.getAgenda().size(); j++) {
                System.out.println("- Current ranking: " + ranking);
                int axiomIndex = readNumber("Next favorite axiom?", 1, prefFactory.getAgenda().size());
                if (ranking.get(axiomIndex - 1) != 0) {
                    j--;
                    continue;
                }
                ranking.set(axiomIndex - 1, j);
            }
            Preference preference = prefFactory.makePreference(ranking);
            System.out.println("- Preferences voter " + (i + 1) + " : " + preference);
            preferences.add(preference);
        }

        // Approvals
        System.out.println("\n--- Approvals.");
        ArrayList<BinaryVote> approvals = new ArrayList<>();
        BinaryVoteFactory bvFactory = new BinaryVoteFactory(agenda);
        System.out.println("- This is the agenda:");
        for (int i = 0; i < bvFactory.getAgenda().size(); i++) {
            System.out.println((i + 1) + " : " + bvFactory.getAgenda().get(i));
        }
        for (int i = 0; i < numVoters; i++) {
            System.out.println("- Approvals voter " + (i + 1));
            ArrayList<Integer> approval = new ArrayList<>();
            for (int j = 0; j < bvFactory.getAgenda().size(); j++) {
                int yesNo = readNumber("Approve axiom " + (j + 1) + "? (0/1)", 0, 1);
                approval.add(yesNo);
            }

            // check consistency of the set of approved axioms
            Set<OWLAxiom> selectedAxioms = new HashSet<>();
            for (int j = 0; j < bvFactory.getAgenda().size(); j++) {
                if (approval.get(j) == 1) {
                    selectedAxioms.add(bvFactory.getAgenda().get(j));
                }
            }
            try (var ontology = Ontology.withAxioms(selectedAxioms)) {
                if (!ontology.isConsistent()) {
                    System.out.println("WARNING: The set approved axioms is not consistent! You must enter "
                            + "the approvals for voter " + (i + 1) + " again.");
                    i--;
                    continue;
                }
            }

            BinaryVote bv = bvFactory.makeBinaryVote(approval);
            System.out.println("- Approvals voter " + (i + 1) + " : " + approval);

            approvals.add(bv);
        }

        System.out.println("\n--- Starting turn-based mechanism...");
        time = System.currentTimeMillis();
        TurnBasedMechanism tbm = new TurnBasedMechanism(agenda, preferences, approvals,
                new CollectiveReferenceOntology(agenda, preferences).get());
        Ontology result = tbm.setVerbose(true).get(Initialization.EMPTY);
        System.out.println(
                "\n--- Turn-based mechanism finished in " + (System.currentTimeMillis() - time) / 1000 + " seconds");

        System.out.println("\n--- RESULT ONTOLOGY\n");
        result.axioms().forEach(System.out::println);
    }

}
