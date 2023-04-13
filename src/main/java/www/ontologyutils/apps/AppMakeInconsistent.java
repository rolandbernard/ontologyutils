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
        Utils.log("Loaded...");
        if (!ontology.isConsistent()) {
            Utils.log("Ontology is already inconsistent.");
            return;
        }
        int minNumIter = 0;
        try {
            minNumIter = Integer.parseInt(args[1]);
            Utils.log("Minimal number of strengthening iterations: " + minNumIter);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            Utils.log("No minimal number of strengthening iterations specified.");
        }
        int minNumIterAfterInconsistency = 0;
        try {
            minNumIterAfterInconsistency = Integer.parseInt(args[2]);
            Utils.log("Minimal number of iterations after reaching inconsistency: " + minNumIterAfterInconsistency);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            Utils.log("No minimal number of iterations after reaching inconsistency specified.");
        }
        final var emptyOntology = Ontology.emptyOntology();
        final var axiomStrengthener = new AxiomStrengthener(ontology);
        int iter = 0;
        int iterSinceInconsistency = 0;
        boolean isConsistent = ontology.isConsistent();
        Utils.log(" ... " + (isConsistent ? "" : "-> INCONSISTENT"));
        while (isConsistent || iter < minNumIter || iterSinceInconsistency < minNumIterAfterInconsistency) {
            final OWLAxiom axiom = Utils.randomChoice(ontology.tBoxAxioms());
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
            Utils.log(" ... " + (isConsistent ? "" : "-> INCONSISTENT"));
        }
        Utils.log("=== BEGIN RESULT ===");
        ontology.axioms().forEach(System.out::println);
        Utils.log("==== END RESULT ====");
        ontology.saveOntology(args[0].replaceAll(".owl$", "") + "-made-inconsistent.owl");
        axiomStrengthener.close();
        emptyOntology.close();
        ontology.close();
        Utils.log("Done.");
    }
}
