package www.ontologyutils.apps;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.*;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.collective.PreferenceFactory;
import www.ontologyutils.collective.PreferenceFactory.Preference;
import www.ontologyutils.collective.blending.BlendingDialogue;
import www.ontologyutils.normalization.NormalizationTools;
import www.ontologyutils.toolbox.*;

/**
 * <p>
 * App to experiment with www.ontologyutils.collective.blending.BlendingDialogue
 * to make asymmetric hybrids dialogues. See: <br>
 * Guendalina Righetti, Daniele Porello, Nicolas Troquard, Oliver Kutz, Maria M.
 * Hedblom, Pietro Galliani. Asymmetric Hybrids: Dialogues for Computational
 * Concept Combination. 12th International Conference on Formal Ontology in
 * Information Systems (FOIS 2021). IOS Press.
 * </p>
 * <p>
 * One must specify two ontologies for both agents in the dialogue, an initial
 * ontology, an alignment ontology, and an ontology containing axioms whose
 * entailment will be checked after a run. Each one can be an empty ontology.
 * <br>
 * The app allows the user to specify preferences even partially (specify the
 * highest-ranked axioms and let the computer choose the others at random at
 * each run), and to run a specified number of experiments with the same
 * settings. <br>
 * Optionally, -o &lt;outputsBaseFileName&gt; can be used at the end of the
 * command to
 * save statistics and the result of each run.
 * </p>
 *
 * <p>
 * E.g., run with parameters ./resources/FishVehicle/Vehicle.owl
 * ./resources/FishVehicle/Fish.owl ./resources/FishVehicle/InitialOntology.owl
 * ./resources/FishVehicle/Disalignment.owl
 * ./resources/FishVehicle/Test_hybrid.owl
 * http://www.semanticweb.org/anonym/ontologies/2020/2/fv#Vehicle
 * http://www.semanticweb.org/anonym/ontologies/2020/2/fv#Fish
 * http://www.semanticweb.org/anonym/ontologies/2020/2/fv#FishVehicle 15 -o
 * fv-outputs
 * </p>
 */
public class AppBlendingDialogue {
    Ontology ontologyOne;
    Ontology ontologyTwo;
    Ontology initialOntology;
    Ontology alignmentOntology;
    Ontology testOntology;

    OWLClassExpression conceptOne;
    OWLClassExpression conceptTwo;
    OWLClassExpression conceptTarget;

    long whenLaunched;

    Set<OWLClassExpression> ascendantsDescendantsOne; // ascendants and descendants of conceptOne in ontologyOne
    Set<OWLClassExpression> ascendantsDescendantsTwo; // ascendants and descendants of conceptTwo in ontologyTwo

    private static final int STOP = -1;
    private static final int INIT_NUM = 0;

    private static final String STATS_HEADER = "run,hT1  ,hT2  ,hS1  ,hS2  ,hTN1 ,hTN2 ,aT   ,aS   ,aTN  ,hyb  ,numW1,numW2,imp1,imp2,maxT";

    private static final String STATS_HEADER_COMPACT = "ontoName,run,hT1,hT2,hS1,hS2,hTN1,hTN2,aT,aS,aTN,hyb,numW1,numW2,imp1,imp2,maxT";

    private static final String MSG_USAGE = "Usage: the program expects five (paths to) ontologies in parameter, "
            + "the IRI of the first source concept, the IRI of the second source concept, and the IRI of the target concept, "
            + "a number of desired runs. Optionally, a base filename to save the results of the blending dialogue, preceded by the flag -o "
            + "(stats will be saved in an csv file containing the columns " + "[ontoFile     ," + STATS_HEADER
            + "] , and ontologies in owl files, labeled with the time when the app was launched in millis - https://currentmillis.com/): "
            + "\n<ontologyFilePath1> <ontologyFilePath2> <initialOntologyFilePath> <alignmentsOntologyFilePath> <testOntologyFilePath> "
            + "<IRI1> <IRI2> <IRITarget> <numberOfRuns> -o <outputsBaseFileName>";

    private static void usage(String[] args) {
        if ((args.length != 9 && args.length != 11) || (args.length == 11 && !args[9].equals("-o"))) {
            System.out.println(MSG_USAGE);
            System.exit(1);
        }
        if (args.length == 11 && args[9].equals("-o") && args[10].contains("/")) {
            System.out.println("Sorry, saving the results in another directory is not supported. Please change "
                    + args[10] + " .");
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
        Set<A> s = new HashSet<A>(list);
        list.clear();
        list.addAll(s);
    }

    private static void addRecord(String outputsBaseFileName, String statistics, String ontologyName) {
        Path path = Paths.get(outputsBaseFileName + ".stats.csv");
        if (!Files.exists(path)) {
            List<String> data = Arrays.asList(STATS_HEADER_COMPACT, ontologyName + "," + statistics);
            try {
                Files.write(path, data, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                List<String> data = Arrays.asList(ontologyName + "," + statistics);
                Files.write(path, data, StandardCharsets.UTF_8, StandardOpenOption.APPEND);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @param ontology
     * @return an estimation of the "amount of information" in {@code ontology}. We
     *         define it as the number of axiom plus the number of sub-class axioms
     *         in the inferred class taxonomy.
     *
     */
    private static int quantifyInformation(Ontology ontology) {
        return (int) (ontology.inferredTaxonomyAxioms().count() + ontology.axioms().count());
    }

    /**
     * @param ontology
     *            The ontology for which to compute the axioms.
     * @return the set of C1 subclass C2 axioms, C1 and C2 classes in the signature
     *         of {@code ontology}, that are not entailed by {@code ontology}.
     */
    public static Set<OWLAxiom> notInferredTaxonomyAxioms(Ontology ontology) {
        Set<OWLAxiom> result = new HashSet<>();
        if (!ontology.isConsistent()) {
            return result;
        }
        OWLDataFactory df = Ontology.getDefaultDataFactory();
        ontology.conceptsInSignature().forEach((left) -> {
            ontology.conceptsInSignature().forEach((right) -> {
                OWLSubClassOfAxiom scoa = df.getOWLSubClassOfAxiom(left, right);
                if (!ontology.isEntailed(scoa)) {
                    result.add(scoa);
                }
            });
        });
        return result;
    }

    /**
     * @param agent
     * @param ontology
     * @return an estimation of the "happiness" of ontology {@code agent} with
     *         ontology {@code two}. The ratio of the number of axioms and inferred
     *         taxonomy axioms in {@code agent} that are inferred by {code
     *         ontology}.
     *
     *         See Daniele Porello, Nicolas Troquard, Rafael Peñaloza, Roberto
     *         Confalonieri, Pietro Galliani, and Oliver Kutz. Two Approaches to
     *         Ontology Aggregation Based on Axiom Weakening. In 27th International
     *         Joint Conference on Artificial Intelligence (IJCAI-ECAI 2018), 2018,
     *         pages 1942-1948.
     */
    private static double happinessTolerant(Ontology agent, Ontology ontology) {
        Set<OWLAxiom> axioms = agent.inferredTaxonomyAxioms().collect(Collectors.toSet());
        axioms.addAll(agent.axioms().collect(Collectors.toSet()));

        long countSatisfiedAxioms = axioms.stream().filter(a -> ontology.isEntailed(a)).count();

        return (double) countSatisfiedAxioms / axioms.size();
    }

    /**
     * @param agent
     * @param ontology
     * @return an estimation of the "happiness" of ontology {@code agent} with
     *         ontology {@code two}. The ratio of the number non inferred taxonomy
     *         axioms in {@code agent} that are not inferred by {code ontology}.
     */
    private static double happinessTolerantNeg(Ontology agent, Ontology ontology) {
        Set<OWLAxiom> nonAxioms = notInferredTaxonomyAxioms(agent);

        long countNotSatisfiedAxioms = nonAxioms.stream().filter(a -> !ontology.isEntailed(a)).count();

        return (double) countNotSatisfiedAxioms / nonAxioms.size();
    }

    /**
     * @param agent
     * @param ontology
     * @return an estimation of the "happiness" of ontology {@code agent} with
     *         ontology {@code two}. The ratio of the number of axioms and inferred
     *         taxonomy axioms in {@code agent} and {@code ontology} that are
     *         inferred by {code ontology} and {@code agent}.
     *
     *         See Daniele Porello, Nicolas Troquard, Rafael Peñaloza, Roberto
     *         Confalonieri, Pietro Galliani, and Oliver Kutz. Two Approaches to
     *         Ontology Aggregation Based on Axiom Weakening. In 27th International
     *         Joint Conference on Artificial Intelligence (IJCAI-ECAI 2018), 2018,
     *         pages 1942-1948.
     */
    private static double happinessStrict(Ontology agent, Ontology ontology) {
        Set<OWLAxiom> axioms = agent.inferredTaxonomyAxioms().collect(Collectors.toSet());
        axioms.addAll(agent.axioms().collect(Collectors.toSet()));
        axioms.addAll(ontology.inferredTaxonomyAxioms().collect(Collectors.toSet()));
        axioms.addAll(ontology.axioms().collect(Collectors.toSet()));

        long countSatisfiedAxioms = axioms.stream().filter(a -> agent.isEntailed(a) && ontology.isEntailed(a))
                .count();

        return (double) countSatisfiedAxioms / axioms.size();
    }

    /**
     * @param ontology
     *            an ontology, typically the result of hybridization of
     *            agent 1 and agent 2
     * @param conceptTarget
     *            the target hybrid concept
     * @param ad1
     *            the set of ascendants and descendants of agent 1
     * @param ad2
     *            the set of ascendants and descendants of agent 2
     * @return an estimation of the hybridization of ontology {@code ontology}.
     */
    private static double hybridity(Ontology ontology, OWLClassExpression conceptTarget, Set<OWLClassExpression> ad1,
            Set<OWLClassExpression> ad2) {
        int countPositiveChecks = 0;

        OWLDataFactory df = Ontology.getDefaultDataFactory();
        for (OWLClassExpression ce1 : ad1) {
            for (OWLClassExpression ce2 : ad2) {
                OWLClassExpression conj = df.getOWLObjectIntersectionOf(ce1, ce2);
                OWLSubClassOfAxiom scoac = df.getOWLSubClassOfAxiom(conceptTarget, conj);

                System.out.print(Utils.prettyPrintAxiom(scoac) + " ?\t");
                if (ontology.isEntailed(scoac)) {
                    countPositiveChecks++;
                    System.out.println("yes");
                } else {
                    System.out.println("no");
                }
            }
        }

        System.out.println("Hybridity ratio : " + countPositiveChecks + " / " + (ad1.size() * ad2.size()));
        return (double) countPositiveChecks / (ad1.size() * ad2.size());
    }

    /**
     * @param ontology
     * @param concept
     * @param notHere
     * @return the set of subconcepts in ontology {@code ontology}, not in
     *         {@code notHere}, that are included in or include the concept
     *         {@code concept}.
     */
    private static Set<OWLClassExpression> computeSpecificAscendanstDescendants(Ontology ontology,
            OWLClassExpression concept, Ontology notHere) {
        OWLSubClassOfAxiom scoa;

        Set<OWLClassExpression> oces = new HashSet<OWLClassExpression>();

        Set<OWLClassExpression> notThose = notHere.subConcepts().collect(Collectors.toSet());
        OWLDataFactory df = Ontology.getDefaultDataFactory();
        for (OWLClassExpression ce : ontology.subConcepts().toList()) {
            if (notThose.contains(ce)) {
                continue;
            }
            scoa = df.getOWLSubClassOfAxiom(concept, ce);
            if (ontology.isEntailed(scoa)) {
                oces.add(ce);
            }
            scoa = df.getOWLSubClassOfAxiom(ce, concept);
            if (ontology.isEntailed(scoa)) {
                oces.add(ce);
            }
        }
        return oces;
    }

    /**
     * @param ontologyFilePath1
     *            the path to a first ontology to be blended
     *            through a blending dialogue. The moves of
     *            agent one will contain the (normalized)
     *            axioms of this ontology.
     * @param ontologyFilePath2
     *            the path to a second ontology to be blended
     *            through a blending dialogue. The moves of
     *            agent two will contain the (normalized)
     *            axioms of this ontology.
     * @param initialOntologyFilePath
     *            the path to a consistent ontology to serve
     *            as initial ontology. The axioms of this
     *            ontology will be in the blend.
     * @param alignmentsOntologyFilePath
     *            the path to an ontology intended to contain
     *            alignments between the entities occurring
     *            in the first and second ontologies. The
     *            (normalized) axioms of this ontology will
     *            be part of the moves of both agent one and
     *            agent two.
     * @param testOntologyFilePath
     *            the path to an ontology.
     * @param IRI1
     *            IRI of the first concept.
     * @param IRI2
     *            IRI of the second concept.
     * @param IRITarget
     *            IRI of the target concept.
     */
    public AppBlendingDialogue(String ontologyFilePath1, String ontologyFilePath2, String initialOntologyFilePath,
            String alignmentsOntologyFilePath, String testOntologyFilePath, String IRI1, String IRI2,
            String IRITarget) {
        whenLaunched = System.currentTimeMillis();

        Ontology base1 = Ontology.loadOnlyLogicalAxioms(ontologyFilePath1);
        Ontology base2 = Ontology.loadOnlyLogicalAxioms(ontologyFilePath2);
        Ontology baseAlignments = Ontology.loadOnlyLogicalAxioms(alignmentsOntologyFilePath);

        this.initialOntology = Ontology.loadOnlyLogicalAxioms(initialOntologyFilePath);

        if (!base1.isConsistent() || !base2.isConsistent() || !initialOntology.isConsistent()) {
            System.out.println("The ontologies must be consistent.");
            System.exit(1);
        }

        this.ontologyOne = Ontology.emptyOntology();
        this.ontologyOne.addAxioms(base1.aboxAxioms().collect(Collectors.toSet()));
        base1.tboxAxioms().forEach(a -> this.ontologyOne.addAxioms(NormalizationTools.asSubClassOfAxioms(a)));
        this.ontologyTwo = Ontology.emptyOntology();
        this.ontologyTwo.addAxioms(base2.aboxAxioms().collect(Collectors.toSet()));
        base2.tboxAxioms().forEach(a -> this.ontologyTwo.addAxioms(NormalizationTools.asSubClassOfAxioms(a)));
        this.alignmentOntology = Ontology.emptyOntology();
        this.alignmentOntology.addAxioms(baseAlignments.aboxAxioms().collect(Collectors.toSet()));
        baseAlignments.tboxAxioms()
                .forEach(a -> this.alignmentOntology.addAxioms(NormalizationTools.asSubClassOfAxioms(a)));

        this.testOntology = Ontology.loadOnlyLogicalAxioms(testOntologyFilePath);

        OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();
        this.conceptOne = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create(IRI1));
        this.conceptTwo = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create(IRI2));
        this.conceptTarget = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create(IRITarget));

        this.ascendantsDescendantsOne = computeSpecificAscendanstDescendants(this.ontologyOne, this.conceptOne,
                this.ontologyTwo);
        this.ascendantsDescendantsTwo = computeSpecificAscendanstDescendants(this.ontologyTwo, this.conceptTwo,
                this.ontologyOne);
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

    /**
     * @param args
     *            Array of at least eight arguments containing ontology file paths
     *            and concept names.
     */
    public static void main(String[] args) {
        usage(args);

        AppBlendingDialogue mApp = new AppBlendingDialogue(args[0], args[1], args[2], args[3], args[4], args[5],
                args[6], args[7]);
        int numberOfTestRuns = Integer.parseInt(args[8]);
        ArrayList<String> metricsResults = new ArrayList<String>();
        metricsResults.add(STATS_HEADER);
        // Preferences
        List<OWLAxiom> listAxiomsOne = mApp.ontologyOne.axioms().collect(Collectors.toList());
        List<OWLAxiom> listAxiomsTwo = mApp.ontologyTwo.axioms().collect(Collectors.toList());
        List<OWLAxiom> listAxiomsAlignments = mApp.alignmentOntology.axioms().collect(Collectors.toList());
        listAxiomsOne.addAll(listAxiomsAlignments);
        listAxiomsTwo.addAll(listAxiomsAlignments);
        removeDuplicates(listAxiomsOne);
        removeDuplicates(listAxiomsTwo);
        Collections.sort(listAxiomsOne);
        Collections.sort(listAxiomsTwo);

        PreferenceFactory prefFactoryOne = new PreferenceFactory(listAxiomsOne);
        PreferenceFactory prefFactoryTwo = new PreferenceFactory(listAxiomsTwo);

        System.out.println("************************\nRunning app with arguments:");
        for (String s : args) {
            System.out.println(s);
        }
        System.out.println("************************");
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

        int importanceOne = readNumber("Importance of agent one?", 1, 101);
        int importanceTwo = readNumber("Importance of agent two?", 1, 101);
        int maxTurns = readNumber("Maximum number of turns?", 1, 10000000);

        // deciding the probability of each agent to take turn in the dialogue
        int infoInOne, infoInTwo;
        try (var ontology = Ontology.withAxioms(prefFactoryOne.getAgenda())) {
            infoInOne = quantifyInformation(ontology);
        }
        try (var ontology = Ontology.withAxioms(prefFactoryTwo.getAgenda())) {
            infoInTwo = quantifyInformation(ontology);
        }
        double probabilityTurnOne = (double) importanceOne * infoInOne
                / (importanceOne * infoInOne + importanceTwo * infoInTwo);
        System.out.println("\n--- At each turn, probability for agent one to act: " + probabilityTurnOne);

        // happiness and testing
        double sumHappinessTolerantOne = 0.0;
        double sumHappinessTolerantTwo = 0.0;
        double sumHappinessStrictOne = 0.0;
        double sumHappinessStrictTwo = 0.0;
        double sumHappinessTolerantNegOne = 0.0;
        double sumHappinessTolerantNegTwo = 0.0;
        double sumAsymmetryTolerant = 0.0;
        double sumAsymmetryStrict = 0.0;
        double sumAsymmetryTolerantNeg = 0.0;
        double sumHybridity = 0.0;
        int sumNumWeakeningsOne = 0;
        int sumNumWeakeningsTwo = 0;

        List<OWLAxiom> listAxiomsTest = mApp.testOntology.axioms().collect(Collectors.toList());
        Collections.sort(listAxiomsTest);
        Map<OWLAxiom, Integer> counts = new HashMap<OWLAxiom, Integer>();

        for (int i = 0; i < numberOfTestRuns; i++) {
            System.out.println("\n\n*********************** RUN " + (i + 1));

            System.out.println("\n--- Preferences.");
            Preference preferenceOne = prefFactoryOne.makePreference(randomRanking(prefFactoryOne, initRankingOne));
            System.out.println("\n- Preferences agent one: ");
            IntStream.range(0, preferenceOne.getRanking().size()).forEach(j -> {
                System.out.print("Ax " + String.format("%3d", (j + 1)) + " has rank "
                        + String.format("%3d", preferenceOne.getRanking().get(j)) + ", ");
                System.out.print(((j + 1) % 5 == 0) ? "\n" : "");
            });
            Preference preferenceTwo = prefFactoryTwo.makePreference(randomRanking(prefFactoryTwo, initRankingTwo));
            System.out.println("\n- Preferences agent two: ");
            IntStream.range(0, preferenceTwo.getRanking().size()).forEach(j -> {
                System.out.print("Ax " + String.format("%3d", (j + 1)) + " has rank "
                        + String.format("%3d", preferenceTwo.getRanking().get(j)) + ", ");
                System.out.print(((j + 1) % 5 == 0) ? "\n" : "");
            });

            BlendingDialogue bdg = new BlendingDialogue(prefFactoryOne.getAgenda(), preferenceOne,
                    prefFactoryTwo.getAgenda(), preferenceTwo, mApp.initialOntology);

            Ontology result = bdg.setVerbose(true).get(probabilityTurnOne, maxTurns);

            System.out.println("\n--- RESULT ONTOLOGY\n");
            result.axioms().forEach(a -> System.out.println("- " + Utils.prettyPrintAxiom(a)));

            if (!result.isConsistent()) {
                System.out.println("THERE WAS A PROBLEM. THE RESULT ONTOLOGY IS NOT CONSISTENT.");
                System.exit(1);
            }

            System.out.println("\n-- EVALUATION RUN " + (i + 1) + "\n");
            double happinessTolerantOne = happinessTolerant(mApp.ontologyOne, result);
            double happinessTolerantTwo = happinessTolerant(mApp.ontologyTwo, result);
            double happinessStrictOne = happinessStrict(mApp.ontologyOne, result);
            double happinessStrictTwo = happinessStrict(mApp.ontologyTwo, result);
            double happinessTolerantNegOne = happinessTolerantNeg(mApp.ontologyOne, result);
            double happinessTolerantNegTwo = happinessTolerantNeg(mApp.ontologyTwo, result);
            double asymmetryTolerant = happinessTolerantOne - happinessTolerantTwo;
            double asymmetryStrict = happinessStrictOne - happinessStrictTwo;
            double asymmetryTolerantNeg = happinessTolerantNegOne - happinessTolerantNegTwo;
            double hybridity = hybridity(result, mApp.conceptTarget, mApp.ascendantsDescendantsOne,
                    mApp.ascendantsDescendantsTwo);
            int numWeakeningsOne = bdg.getNumWeakeningOne();
            int numWeakeningsTwo = bdg.getNumWeakeningTwo();
            String statistics = String.format("%03d", (i + 1)) + ","
                    + String.format(Locale.US, "%.3f", happinessTolerantOne) + ","
                    + String.format(Locale.US, "%.3f", happinessTolerantTwo) + ","
                    + String.format(Locale.US, "%.3f", happinessStrictOne) + ","
                    + String.format(Locale.US, "%.3f", happinessStrictTwo) + ","
                    + String.format(Locale.US, "%.3f", happinessTolerantNegOne) + ","
                    + String.format(Locale.US, "%.3f", happinessTolerantNegTwo) + ","
                    + String.format(Locale.US, "%.3f", asymmetryTolerant) + ","
                    + String.format(Locale.US, "%.3f", asymmetryStrict) + ","
                    + String.format(Locale.US, "%.3f", asymmetryTolerantNeg) + ","
                    + String.format(Locale.US, "%.3f", hybridity) + "," + numWeakeningsOne + "," + numWeakeningsTwo
                    + "," + importanceOne + "," + importanceTwo + "," + maxTurns;
            metricsResults.add(statistics);
            sumHappinessTolerantOne += happinessTolerantOne;
            sumHappinessTolerantTwo += happinessTolerantTwo;
            sumHappinessStrictOne += happinessStrictOne;
            sumHappinessStrictTwo += happinessStrictTwo;
            sumHappinessTolerantNegOne += happinessTolerantNegOne;
            sumHappinessTolerantNegTwo += happinessTolerantNegTwo;
            sumAsymmetryTolerant += asymmetryTolerant;
            sumAsymmetryStrict += asymmetryStrict;
            sumAsymmetryTolerantNeg += asymmetryTolerantNeg;
            sumHybridity += hybridity;
            sumNumWeakeningsOne += numWeakeningsOne;
            sumNumWeakeningsTwo += numWeakeningsTwo;
            System.out.println("Happiness Tolerant of one: " + happinessTolerantOne);
            System.out.println("Happiness Tolerant of two: " + happinessTolerantTwo);
            System.out.println("Happiness Strict of one: " + happinessStrictOne);
            System.out.println("Happiness Strict of two: " + happinessStrictTwo);
            System.out.println("Happiness TolerantNeg of one: " + happinessTolerantNegOne);
            System.out.println("Happiness TolerantNeg of two: " + happinessTolerantNegTwo);
            System.out.println("Asymmetry Tolerant: " + asymmetryTolerant);
            System.out.println("Asymmetry Strict: " + asymmetryStrict);
            System.out.println("Asymmetry TolerantNeg: " + asymmetryTolerantNeg);
            System.out.println("Hybridity: " + hybridity);
            System.out.println("Number of weakenings one: " + numWeakeningsOne);
            System.out.println("Number of weakenings two: " + numWeakeningsTwo);

            System.out.println("\n-- TESTS RUN " + (i + 1) + "\n");
            for (OWLAxiom a : listAxiomsTest) {
                System.out.print(Utils.prettyPrintAxiom(a) + " ?\t");
                if (result.isEntailed(a)) {
                    System.out.println("yes");
                    Integer num = counts.get(a);
                    counts.put(a, (num == null ? 1 : num + 1));
                } else {
                    System.out.println("no");
                }
            }

            if (args.length == 11 && args[9].equals("-o")) {
                System.out.println("\n--- Saving result ontology and recording stats.");
                String leadingZeros = "%0" + (int) (Math.floor(Math.log10(numberOfTestRuns)) + 1) + "d";
                String ontologyName = mApp.whenLaunched + "_" + String.format(leadingZeros, (i + 1)) + "_" + args[10]
                        + ".owl";
                result.saveOntology(ontologyName);
                addRecord(args[10], statistics, ontologyName);
            }
        }

        // SUMMARY
        System.out.println("\n-- SUMMARY");

        for (String res : metricsResults) {
            System.out.println(res);
        }

        System.out.println("Average Happiness Tolerant of one: " + sumHappinessTolerantOne / numberOfTestRuns);
        System.out.println("Average Happiness Tolerant of two: " + sumHappinessTolerantTwo / numberOfTestRuns);
        System.out.println("Average Happiness Strict of one: " + sumHappinessStrictOne / numberOfTestRuns);
        System.out.println("Average Happiness Strict of two: " + sumHappinessStrictTwo / numberOfTestRuns);
        System.out.println("Average Happiness TolerantNeg of one: " + sumHappinessTolerantNegOne / numberOfTestRuns);
        System.out.println("Average Happiness TolerantNeg of two: " + sumHappinessTolerantNegTwo / numberOfTestRuns);
        System.out.println("Average Asymmetry Tolerant: " + sumAsymmetryTolerant / numberOfTestRuns);
        System.out.println("Average Asymmetry Strict: " + sumAsymmetryStrict / numberOfTestRuns);
        System.out.println("Average Asymmetry TolerantNeg: " + sumAsymmetryTolerantNeg / numberOfTestRuns);
        System.out.println("Average Hybridity: " + sumHybridity / numberOfTestRuns);
        System.out.println("Average number of weakenings of one: " + sumNumWeakeningsOne / numberOfTestRuns);
        System.out.println("Average number of weakenings of two: " + sumNumWeakeningsTwo / numberOfTestRuns);

        System.out.println("(\"Happiness Tolerant\" of an agent with the result is estimated as "
                + "the ratio of the number of axioms and inferred taxonomy axioms "
                + "in the ontology of the agent that are inferred by the result ontology.)");
        System.out.println("(\"Happiness Strict\" of an agent with the result is estimated as "
                + "the ratio of the number of axioms and inferred taxonomy axioms "
                + "in the ontology of the agent and of the result ontology that are inferred "
                + "by both the agent and the result ontology.)");
        System.out.println("(\"Happiness TolerantNeg\" of an agent with the result is estimated "
                + "as the ratio of non-inferred taxonomy axioms in the ontology of the agent that "
                + "are not inferred by the result ontology.)");
        System.out.println("(\"Asymmetry\" of the result ontology is the difference between agent one's "
                + "happiness and agent two's happiness.)");
        System.out.println(
                "(\"Hybrididty\" of the result ontology is the ratio of \"hybrid\" axioms that " + "are inferred.)");

        for (OWLAxiom a : listAxiomsTest) {
            Integer num = counts.get(a);
            System.out.println(
                    Utils.prettyPrintAxiom(a) + " : " + (num == null ? 0 : num) + " out of " + numberOfTestRuns);
        }
    }
}
