package www.ontologyutils.apps;

import java.util.HashSet;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.normalization.TBoxSubclassOfNormalization;
import www.ontologyutils.refinement.AxiomStrengthener;
import www.ontologyutils.toolbox.*;

public class AppMakeInconsistent {
    /**
     * A first argument must be given, corresponding to an OWL ontology file path.
     * E.g., run with the parameter resources/catsandnumbers.owl A second argument
     * can be given, to indicate the minimal number of strengthening iterations must
     * be done. A third argument can be given, to indicate the minimal number of
     * iterations needed that must be done after reaching inconsistency.
     *
     * @param args
     */
    public static void main(String[] args) {
        final var ontology = Ontology.loadOntology(args[0]);
        final var normalization = new TBoxSubclassOfNormalization();
        normalization.apply(ontology);
        System.err.println("Loaded...");
        if (!ontology.isConsistent()) {
            System.err.println("Ontology is already inconsistent.");
            return;
        }
        int minNumIter = 0;
        try {
            minNumIter = Integer.parseInt(args[1]);
            System.err.println("Minimal number of strengthening iterations: " + minNumIter);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            System.err.println("No minimal number of strengthening iterations specified.");
        }
        int minNumIterAfterInconsistency = 0;
        try {
            minNumIterAfterInconsistency = Integer.parseInt(args[2]);
            System.err.println(
                    "Minimal number of iterations after reaching inconsistency: " + minNumIterAfterInconsistency);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            System.err.println("No minimal number of iterations after reaching inconsistency specified.");
        }
        final var emptyOntology = Ontology.emptyOntology();
        final var axiomStrengthener = new AxiomStrengthener(ontology);
        int iter = 0;
        int iterSinceInconsistency = 0;
        boolean isConsistent = ontology.isConsistent();
        System.err.println(" ... " + (isConsistent ? "" : "-> INCONSISTENT"));
        while (isConsistent || iter < minNumIter || iterSinceInconsistency < minNumIterAfterInconsistency) {
            final OWLAxiom axiom = Utils.randomChoice(ontology.tboxAxioms());
            final var strongerAxioms = axiomStrengthener.strongerAxioms(axiom).collect(Collectors.toSet());
            // We do not consider the axioms already in the ontology.
            strongerAxioms.removeAll(ontology.axioms().toList());
            // We do not consider axioms that are inconsistent on their own, could be made
            // optional.
            final var tooStrong = new HashSet<OWLAxiom>();
            for (final var strongerAxiom : strongerAxioms) {
                emptyOntology.addAxioms(strongerAxiom);
                if (!ontology.isConsistent()) {
                    tooStrong.add(strongerAxiom);
                }
                emptyOntology.removeAxioms(strongerAxiom);
            }
            strongerAxioms.removeAll(tooStrong);
            // We do not consider axioms that are tautologies, could be made optional.
            final var tautologies = new HashSet<OWLAxiom>();
            for (final var strongerAxiom : strongerAxioms) {
                if (emptyOntology.isEntailed(strongerAxiom)) {
                    tautologies.add(strongerAxiom);
                }
            }
            strongerAxioms.removeAll(tautologies);
            if (!strongerAxioms.isEmpty()) {
                OWLAxiom strongerAxiom = Utils.randomChoice(strongerAxioms);
                ontology.replaceAxiom(axiom, strongerAxiom);
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
        ontology.refutableAxioms().map(Utils::prettyPrintAxiom)
                .sorted().forEach(System.out::println);
        ontology.staticAxioms().map(Utils::prettyPrintAxiom)
                .sorted().forEach(System.out::println);
        System.err.println("==== END RESULT ====");
        ontology.saveOntology(args[0].replaceAll(".owl$", "") + "-made-inconsistent.owl");
        axiomStrengthener.close();
        emptyOntology.close();
        ontology.close();
        System.err.println("Done.");
    }
}
