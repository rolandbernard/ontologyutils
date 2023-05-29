package www.ontologyutils.apps;

import java.util.*;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.normalization.SroiqNormalization;
import www.ontologyutils.refinement.AxiomStrengthener;
import www.ontologyutils.toolbox.*;

/**
 * Take the given ontology and make it inconsistent by adding some strengthened
 * axioms.
 */
public class MakeInconsistent extends App {
    private String inputFile;
    private String outputFile = null;
    private boolean normalize = false;
    private boolean verbose = false;
    private int minIter = 0;
    private int minAfter = 0;

    @Override
    protected List<Option<?>> appOptions() {
        var options = new ArrayList<Option<?>>();
        options.addAll(super.appOptions());
        options.add(OptionType.FILE.createDefault(file -> {
            if (inputFile != null) {
                throw new IllegalArgumentException("multiple input files specified");
            }
            inputFile = file.toString();
        }, "the file containing the original ontology"));
        options.add(OptionType.FILE.create('o', "output", file -> {
            if (outputFile != null) {
                throw new IllegalArgumentException("multiple output files specified");
            }
            outputFile = file.toString();
        }, "the file to write the result to"));
        options.add(OptionType.FLAG.create('n', "normalize", b -> normalize = true,
                "normalize the ontology beforehand"));
        options.add(OptionType.FLAG.create('v', "verbose", b -> verbose = true, "print more information"));
        options.add(OptionType.UINT.create("min-iter", i -> minIter = i, "minimum number of iterations to perform"));
        options.add(OptionType.UINT.create("min-after", i -> minAfter = i, "minimum iterations after inconsistency"));
        return options;
    }

    @Override
    public void run() {
        var startTime = System.nanoTime();
        var ontology = Ontology.loadOntology(inputFile);
        System.err.println("Loaded... (" + ontology.logicalAxioms().count() + " axioms)");
        if (normalize) {
            var normalization = new SroiqNormalization();
            normalization.apply(ontology);
            System.err.println("Normalized. (" + ontology.logicalAxioms().count() + " axioms)");
        }
        if (!ontology.isConsistent()) {
            System.err.println("Ontology is already inconsistent.");
            return;
        }
        var emptyOntology = ontology.cloneOnlyStatic();
        var axiomStrengthener = new AxiomStrengthener(ontology);
        int iter = 0;
        int iterSinceInconsistency = 0;
        boolean isConsistent = ontology.isConsistent();
        while (isConsistent || iter < minIter || iterSinceInconsistency < minAfter) {
            OWLAxiom axiom = Utils.randomChoice(ontology.logicalAxioms());
            var strongerAxioms = Utils.toSet(axiomStrengthener.strongerAxioms(axiom));
            // We do not consider the axioms already in the ontology.
            strongerAxioms.removeAll(Utils.toList(ontology.axioms()));
            // We do not consider axioms that are inconsistent on their own, could be made
            // optional.
            var tooStrong = new HashSet<OWLAxiom>();
            for (var strongerAxiom : strongerAxioms) {
                emptyOntology.addAxioms(strongerAxiom);
                if (!emptyOntology.isConsistent()) {
                    tooStrong.add(strongerAxiom);
                }
                emptyOntology.removeAxioms(strongerAxiom);
            }
            strongerAxioms.removeAll(tooStrong);
            // We do not consider axioms that are tautologies, could be made optional.
            var tautologies = new HashSet<OWLAxiom>();
            for (var strongerAxiom : strongerAxioms) {
                if (emptyOntology.isEntailed(strongerAxiom)) {
                    tautologies.add(strongerAxiom);
                }
            }
            strongerAxioms.removeAll(tautologies);
            if (!strongerAxioms.isEmpty()) {
                OWLAxiom strongerAxiom = Utils.randomChoice(strongerAxioms);
                if (verbose) {
                    logMessage("adding axiom " + Utils.prettyPrintAxiom(strongerAxiom));
                }
                ontology.addAxioms(strongerAxiom);
            }
            isConsistent = ontology.isConsistent();
            iter++;
            if (!isConsistent) {
                iterSinceInconsistency++;
            } else {
                iterSinceInconsistency = 0;
            }
        }
        if (verbose) {
            System.err.println("=== BEGIN RESULT ===");
            ontology.refutableAxioms().map(OWLAxiom::toString).map(Utils::pretty)
                    .sorted().forEach(System.out::println);
            ontology.staticAxioms().map(OWLAxiom::toString).map(Utils::pretty)
                    .sorted().forEach(System.out::println);
            System.err.println("==== END RESULT ====");
        }
        if (outputFile != null) {
            ontology.saveOntology(outputFile);
        }
        emptyOntology.close();
        ontology.close();
        var endTime = System.nanoTime();
        System.err.println(
                "Done. (" + (endTime - startTime) / 1_000_000 + " ms; " + Ontology.reasonerCalls + " reasoner calls)");
    }

    /**
     * A first argument must be given, corresponding to an OWL ontology file path.
     * E.g., run with the parameter resources/catsandnumbers.owl A second argument
     * can be given, to indicate the minimal number of strengthening iterations must
     * be done. A third argument can be given, to indicate the minimal number of
     * iterations needed that must be done after reaching inconsistency.
     *
     * @param args
     *            Up to three arguments containing in order: ontology file path,
     *            minimum number of iterations, and minimum number of iterations
     *            after making the ontology inconsistent.
     */
    public static void main(String[] args) {
        (new MakeInconsistent()).launch(args);
    }
}
