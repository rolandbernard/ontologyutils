package www.ontologyutils.apps;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import www.ontologyutils.collective.PreferenceFactory;
import www.ontologyutils.collective.PreferenceFactory.Preference;
import www.ontologyutils.collective.blending.BlendingDialogue;
import www.ontologyutils.normalization.NormalizationTools;
import www.ontologyutils.toolbox.Utils;

/**
 * @author nico
 *
 *         App to experiment with asymmetric hybrids dialogues.
 *
 *         One must specify two ontologies for both agents in the dialogue, an
 *         initial ontology, an alignment ontology, and an ontology containing
 *         axioms whose entailment will be checked after a run. Each one can be
 *         an empty ontology.
 *
 *         The app allows the user to specify preferences even partially
 *         (specify the highest-ranked axioms and let the computer choose the
 *         others at random at each run), and to run a specified number of
 *         experiments with the same settings.
 * 
 */
public class AppBlendingDialogue {

	OWLOntology ontologyOne;
	OWLOntology ontologyTwo;
	OWLOntology initialOntology;
	OWLOntology alignmentOntology;
	OWLOntology testOntology;

	private static final int STOP = -1;
	private static final int INIT_NUM = 0;

	private static final String MSG_USAGE = "Usage: the program expects five (paths to) ontologies in parameter, "
			+ "a number of desired runs, and optionally, "
			+ "a file pathname to save the result of the blending dialog, preceded by the flag -o: "
			+ "<ontologyFilePath1> <ontologyFilePath2> <initialOntologyFilePath> <alignmentsOntologyFilePath> <testOntologyFilePath> "
			+ "<numberOfRuns> -o <outputOntologyFilePath>";

	private static void usage(String[] args) {
		if ((args.length != 6 && args.length != 8) || (args.length == 8 && !args[6].equals("-o"))) {
			System.out.println(MSG_USAGE);
			System.exit(1);
		}
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
			if (num == STOP) {
				return STOP;
			}
			if (num < min || num > max) {
				continue;
			}
			return num;
		}
	}

	private static <A> void removeDuplicates(List<A> list) {
		Set<A> s = new LinkedHashSet<A>(list);
		list.clear();
		list.addAll(s);
	}

	public static void saveOntology(OWLOntology ontology, String fileName) {
		File file = new File(fileName);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		try {
			manager.saveOntology(ontology, ontology.getFormat(), IRI.create(file.toURI()));
			System.out.println("Ontology saved as " + fileName);
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param ontology
	 * @return an estimation of the "amount of information" in {@code ontology}. We
	 *         define it as the number of axiom plus the number of sub-class axioms
	 *         in the inferred class taxonomy.
	 * 
	 */
	private static int quantifyInformation(OWLOntology ontology) {
		return Utils.inferredClassSubClassClassAxioms(ontology).size() + ontology.getAxiomCount();
	}

	/**
	 * @param agent
	 * @param ontology
	 * @return an estimation of the "happiness" of ontology {@code agent} with
	 *         ontology {@code two}. The ratio of the number of axioms and inferred
	 *         taxonomy axioms in {@code agent} that are inferred by {code
	 *         ontology}.
	 */
	private static double happiness(OWLOntology agent, OWLOntology ontology) {
		Set<OWLAxiom> axioms = Utils.inferredClassSubClassClassAxioms(agent);
		axioms.addAll(agent.axioms().collect(Collectors.toSet()));

		OWLReasoner reasoner = Utils.getReasoner(ontology);
		long countSatisfiedAxioms = axioms.stream().filter(a -> reasoner.isEntailed(a)).count();

		reasoner.dispose();
		return (double) countSatisfiedAxioms / axioms.size();
	}

	/**
	 * @param ontologyFilePath1          the path to a first ontology to be blended
	 *                                   through a blending dialogue. The moves of
	 *                                   agent one will contain the (normalized)
	 *                                   axioms of this ontology.
	 * @param ontologyFilePath2          the path to a second ontology to be blended
	 *                                   through a blending dialogue. The moves of
	 *                                   agent two will contain the (normalized)
	 *                                   axioms of this ontology.
	 * @param initialOntologyFilePath    the path to a consistent ontology to serve
	 *                                   as initial ontology. The axioms of this
	 *                                   ontology will be in the blend.
	 * @param alignmentsOntologyFilePath the path to an ontology intended to contain
	 *                                   alignments between the entities occurring
	 *                                   in the first and second ontologies. The
	 *                                   (normalized) axioms of this ontology will
	 *                                   be part of the moves of both agent one and
	 *                                   agent two.
	 */
	public AppBlendingDialogue(String ontologyFilePath1, String ontologyFilePath2,
			String initialOntologyFilePath, String alignmentsOntologyFilePath, String testOntologyFilePath) {

		OWLOntology base1 = ontologyOne = Utils.newOntologyExcludeNonLogicalAxioms(ontologyFilePath1);
		OWLOntology base2 = ontologyTwo = Utils.newOntologyExcludeNonLogicalAxioms(ontologyFilePath2);
		OWLOntology baseAlignments = ontologyTwo = Utils.newOntologyExcludeNonLogicalAxioms(alignmentsOntologyFilePath);

		this.initialOntology = Utils.newOntologyExcludeNonLogicalAxioms(initialOntologyFilePath);

		if (!Utils.isConsistent(base1) || !Utils.isConsistent(base2) || !Utils.isConsistent(initialOntology)) {
			System.out.println("The ontologies must be consistent.");
			System.exit(1);
		}

		this.ontologyOne = Utils.newEmptyOntology();
		this.ontologyOne.add(base1.aboxAxioms(Imports.EXCLUDED).collect(Collectors.toSet()));
		base1.tboxAxioms(Imports.EXCLUDED).forEach(a -> this.ontologyOne.add(NormalizationTools.asSubClassOfAxioms(a)));
		this.ontologyTwo = Utils.newEmptyOntology();
		this.ontologyTwo.add(base2.aboxAxioms(Imports.EXCLUDED).collect(Collectors.toSet()));
		base2.tboxAxioms(Imports.EXCLUDED).forEach(a -> this.ontologyTwo.add(NormalizationTools.asSubClassOfAxioms(a)));
		this.alignmentOntology = Utils.newEmptyOntology();
		this.alignmentOntology.add(base1.aboxAxioms(Imports.EXCLUDED).collect(Collectors.toSet()));
		baseAlignments.tboxAxioms(Imports.EXCLUDED)
				.forEach(a -> this.alignmentOntology.add(NormalizationTools.asSubClassOfAxioms(a)));

		this.testOntology = Utils.newOntologyExcludeNonLogicalAxioms(testOntologyFilePath);
	}

	/**
	 * @param pf
	 * @param init
	 * @return a ranking of axioms in pf.getAgenda() respecting {@param init} for
	 *         highest-ranked axioms, while the other axioms appear at random.
	 */
	private static List<Integer> randomRanking(PreferenceFactory pf, List<Integer> init) {

		List<Integer> ranking = new ArrayList<>();
		int maxSpecSoFar = 0;
		for (int i = 0; i < pf.getAgenda().size(); i++) {
			int inInit = init.get(i);
			ranking.add(inInit);
			if (inInit != INIT_NUM) {
				maxSpecSoFar++;
			}
		}
		for (int k = maxSpecSoFar + 1; k <= pf.getAgenda().size(); k++) {
			int where = ThreadLocalRandom.current().nextInt(0, pf.getAgenda().size());
			while (ranking.get(where) != INIT_NUM) {
				where = ThreadLocalRandom.current().nextInt(0, pf.getAgenda().size());
			}
			ranking.set(where, k);
		}
		return ranking;
	}

	public static void main(String[] args) {

		usage(args);

		AppBlendingDialogue mApp = new AppBlendingDialogue(
				args[0], args[1], args[2], args[3], args[4]);
		int numberOfTestRuns = Integer.parseInt(args[5]);

		// Preferences
		List<OWLAxiom> listAxiomsOne = mApp.ontologyOne.axioms().collect(Collectors.toList());
		List<OWLAxiom> listAxiomsTwo = mApp.ontologyTwo.axioms().collect(Collectors.toList());
		List<OWLAxiom> listAxiomsAlignments = mApp.alignmentOntology.axioms().collect(Collectors.toList());
		Collections.sort(listAxiomsOne);
		Collections.sort(listAxiomsTwo);
		Collections.sort(listAxiomsAlignments);
		listAxiomsOne.addAll(listAxiomsAlignments);
		listAxiomsTwo.addAll(listAxiomsAlignments);
		removeDuplicates(listAxiomsOne);
		removeDuplicates(listAxiomsTwo);

		PreferenceFactory prefFactoryOne = new PreferenceFactory(listAxiomsOne);
		PreferenceFactory prefFactoryTwo = new PreferenceFactory(listAxiomsTwo);

		System.out.println("\n--- Preferences will be generated at random at each run.");

		System.out.println("\n--- You can specify the top preferences of agent one.");

		System.out.println("- These are the base moves of agent one:");
		for (int i = 0; i < prefFactoryOne.getAgenda().size(); i++) {
			System.out.println((i + 1) + " : " + Utils.prettyPrintAxiom(prefFactoryOne.getAgenda().get(i)));
		}
		List<Integer> initRankingOne = Stream.generate(String::new).limit(prefFactoryOne.getAgenda().size())
				.map(s -> INIT_NUM).collect(Collectors.toList());
		for (int j = 1; j <= prefFactoryOne.getAgenda().size(); j++) {
			System.out.println("- Current ranking: " + initRankingOne);
			int axiomIndex = readNumber("Next favorite axiom of agent one (enter " + STOP + " to stop)?", 1,
					prefFactoryOne.getAgenda().size());
			if (axiomIndex == STOP) {
				break;
			}
			if (initRankingOne.get(axiomIndex - 1) != 0) {
				j--;
				continue;
			}
			initRankingOne.set(axiomIndex - 1, j);
		}

		System.out.println("\n--- You can specify the top preferences of agent two.");

		System.out.println("- These are the base moves of agent two:");
		for (int i = 0; i < prefFactoryTwo.getAgenda().size(); i++) {
			System.out.println((i + 1) + " : " + Utils.prettyPrintAxiom(prefFactoryTwo.getAgenda().get(i)));
		}
		List<Integer> initRankingTwo = Stream.generate(String::new).limit(prefFactoryTwo.getAgenda().size())
				.map(s -> INIT_NUM).collect(Collectors.toList());
		for (int j = 1; j <= prefFactoryTwo.getAgenda().size(); j++) {
			System.out.println("- Current ranking: " + initRankingTwo);
			int axiomIndex = readNumber("Next favorite axiom of agent two (enter " + STOP + " to stop)?", 1,
					prefFactoryTwo.getAgenda().size());
			if (axiomIndex == STOP) {
				break;
			}
			if (initRankingTwo.get(axiomIndex - 1) != 0) {
				j--;
				continue;
			}
			initRankingTwo.set(axiomIndex - 1, j);
		}

		int importanceOne = readNumber("Importance of agent one?", 1, 100);
		int importanceTwo = readNumber("Importance of agent two?", 1, 100);

		// deciding the probability of each agent to take turn in the dialogue
		int infoInOne = quantifyInformation(Utils.newOntology(prefFactoryOne.getAgenda().stream()));
		int infoInTwo = quantifyInformation(Utils.newOntology(prefFactoryTwo.getAgenda().stream()));
		double probabilityTurnOne = (double) importanceOne * infoInOne
				/ (importanceOne * infoInOne + importanceTwo * infoInTwo);
		System.out.println("\n--- At each turn, probability for agent one to act: " + probabilityTurnOne);

		// happiness and testing
		double sumHappinessOne = 0.0;
		double sumHappinessTwo = 0.0;
		List<OWLAxiom> listAxiomsTest = mApp.testOntology.axioms().collect(Collectors.toList());
		Collections.sort(listAxiomsTest);
		Map<OWLAxiom, Integer> counts = new HashMap<OWLAxiom, Integer>();

		for (int i = 0; i < numberOfTestRuns; i++) {
			System.out.println("\n\n*********************** RUN " + (i + 1));

			System.out.println("\n--- Preferences.");
			System.out.println("(Reminder: How to read them: [rank axiom 1, rank axiom 2, ..., rank axiom last])");
			Preference preferenceOne = prefFactoryOne.makePreference(randomRanking(prefFactoryOne, initRankingOne));
			System.out.println("- Preferences agent one: " + preferenceOne);
			Preference preferenceTwo = prefFactoryTwo.makePreference(randomRanking(prefFactoryTwo, initRankingTwo));
			System.out.println("- Preferences agent two: " + preferenceTwo);

			BlendingDialogue bdg = new BlendingDialogue(prefFactoryOne.getAgenda(), preferenceOne,
					prefFactoryTwo.getAgenda(), preferenceTwo, mApp.initialOntology);

			OWLOntology result = bdg.setVerbose(true).get(probabilityTurnOne);

			System.out.println("\n--- RESULT ONTOLOGY\n");
			result.axioms().forEach(a -> System.out.println("- " + Utils.prettyPrintAxiom(a)));

			OWLReasoner reasoner = Utils.getReasoner(result);
			if (!reasoner.isConsistent()) {
				System.out.println("THERE WAS A PROBLEM. THE RESULT ONTOLOGY IS NOT CONSISTENT.");
				System.exit(1);
			}

			System.out.println("\n-- EVALUATION RUN " + (i + 1) + "\n");
			double happinessOne = happiness(mApp.ontologyOne, result);
			double happinessTwo = happiness(mApp.ontologyTwo, result);
			sumHappinessOne += happinessOne;
			sumHappinessTwo += happinessTwo;
			System.out.println("Happiness of one: " + happinessOne);
			System.out.println("Happiness of two: " + happinessTwo);

			System.out.println("\n-- TESTS RUN " + (i + 1) + "\n");
			for (OWLAxiom a : listAxiomsTest) {
				System.out.print(Utils.prettyPrintAxiom(a) + " ?\t");
				if (reasoner.isEntailed(a)) {
					System.out.println("yes");
					Integer num = counts.get(a);
					counts.put(a, (num == null ? 1 : num + 1));
				} else {
					System.out.println("no");
				}
			}
			reasoner.dispose();

			if (args.length == 8 && args[6].equals("-o")) {
				System.out.println("\n--- Saving result ontology.");
				saveOntology(result, i + " " + args[7]);
			}
		}

		// SUMMARY
		System.out.println("\n-- SUMMARY");

		System.out.println("Average Happiness of one: " + sumHappinessOne / numberOfTestRuns);
		System.out.println("Average Happiness of two: " + sumHappinessTwo / numberOfTestRuns);
		System.out.println("(\"Happiness\" of an agent with the result is estimated as "
				+ "the ratio of the number of axioms and inferred taxonomy axioms "
				+ "in the ontology of the agent that are inferred by the result ontology.)");

		for (OWLAxiom a : listAxiomsTest) {
			Integer num = counts.get(a);
			System.out.println(
					Utils.prettyPrintAxiom(a) + " : " + (num == null ? 0 : num) + " out of " + numberOfTestRuns);
		}

	}

}
