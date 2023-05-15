package www.ontologyutils.apps;

import java.util.HashSet;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.normalization.SroiqNormalization;
import www.ontologyutils.refinement.AxiomStrengthener;
import www.ontologyutils.toolbox.*;

/**
 * Take the given ontology and make it inconsistent by adding some strengthened
 * axioms.
 */
public class AppMakeInconsistent {
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
        var startTime = System.nanoTime();
        var fast = args[0].equals("fast");
        var file = fast ? args[1] : args[0];
        var ontology = Ontology.loadOntology(file);
        var normalization = new SroiqNormalization();
        System.err.println("Loaded... (" + ontology.logicalAxioms().count() + " axioms)");
        if (!fast) {
            normalization.apply(ontology);
            System.err.println("Normalized. (" + ontology.logicalAxioms().count() + " axioms)");
        }
        if (!ontology.isConsistent()) {
            System.err.println("Ontology is already inconsistent.");
            return;
        }
        int minNumIter = 0;
        try {
            minNumIter = Integer.parseInt(fast ? args[2] : args[1]);
            System.err.println("Minimal number of strengthening iterations: " + minNumIter);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            System.err.println("No minimal number of strengthening iterations specified.");
        }
        int minNumIterAfterInconsistency = 0;
        try {
            minNumIterAfterInconsistency = Integer.parseInt(fast ? args[3] : args[2]);
            System.err.println(
                    "Minimal number of iterations after reaching inconsistency: " + minNumIterAfterInconsistency);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            System.err.println("No minimal number of iterations after reaching inconsistency specified.");
        }
        var emptyOntology = ontology.cloneOnlyStatic();
        var axiomStrengthener = new AxiomStrengthener(ontology);
        int iter = 0;
        int iterSinceInconsistency = 0;
        boolean isConsistent = ontology.isConsistent();
        System.err.println(" ... " + (isConsistent ? "" : "-> INCONSISTENT"));
        while (isConsistent || iter < minNumIter || iterSinceInconsistency < minNumIterAfterInconsistency) {
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
                ontology.addAxioms(strongerAxiom);
            }
            isConsistent = ontology.isConsistent();
            iter++;
            if (!isConsistent) {
                iterSinceInconsistency++;
            } else {
                iterSinceInconsistency = 0;
            }
            System.err.println(" ... " + (isConsistent ? "" : "-> INCONSISTENT"));
        }
        System.err.println("=== BEGIN RESULT ===");
        ontology.refutableAxioms().map(OWLAxiom::toString).map(Utils::pretty)
                .sorted().forEach(System.out::println);
        ontology.staticAxioms().map(OWLAxiom::toString).map(Utils::pretty)
                .sorted().forEach(System.out::println);
        System.err.println("==== END RESULT ====");
        ontology.saveOntology(file.replaceAll(".owl$", "") + "-made-inconsistent.owl");
        axiomStrengthener.close();
        emptyOntology.close();
        ontology.close();
        var endTime = System.nanoTime();
        System.err.println(
                "Done. (" + (endTime - startTime) / 1_000_000 + " ms; " + Ontology.reasonerCalls + " reasoner calls)");
    }
}
