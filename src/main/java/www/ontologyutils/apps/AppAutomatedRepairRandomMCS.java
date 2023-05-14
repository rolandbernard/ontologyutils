package www.ontologyutils.apps;

import org.semanticweb.owlapi.model.OWLAxiom;

import www.ontologyutils.normalization.SroiqNormalization;
import www.ontologyutils.repair.*;
import www.ontologyutils.toolbox.*;

/**
 * Repair the given ontology using a random maximal consistent subset.
 */
public class AppAutomatedRepairRandomMCS {
    private static String getTimeStamp() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new java.util.Date());
    }

    /**
     * One argument must be given, corresponding to an OWL ontology file path. E.g.,
     * run with the parameter
     * src/test/resources/www/ontologyutils/inconsistent-leftpolicies.owl
     *
     * @param args
     *            Must contain one argument representing the file path of an
     *            ontology.
     */
    public static void main(String[] args) {
        var startTime = System.nanoTime();
        if (args.length != 1) {
            System.err.println("Usage: java " + AppAutomatedRepairRandomMCS.class.getCanonicalName() + " FILENAME");
            System.exit(1);
        }
        var file = args[0];
        var ontology = Ontology.loadOntology(file);
        System.err.println("Loaded...");
        var normalization = new SroiqNormalization();
        var repair = OntologyRepairRandomMcs.forConsistency();
        repair.setInfoCallback(msg -> System.out.println("[" + getTimeStamp() + "] " + msg));
        System.err.println("Normalizing...");
        normalization.apply(ontology);
        System.err.println("Repairing...");
        repair.apply(ontology);
        System.err.println("Repaired.");
        System.err.println("=== BEGIN RESULT ===");
        ontology.refutableAxioms().map(OWLAxiom::toString).map(Utils::pretty)
                .sorted().forEach(System.out::println);
        ontology.staticAxioms().map(OWLAxiom::toString).map(Utils::pretty)
                .sorted().forEach(System.out::println);
        assert ontology.isConsistent();
        ontology.close();
        System.err.println("==== END RESULT ====");
        ontology.saveOntology(file.replaceAll(".owl$", "") + "-made-consistent.owl");
        var endTime = System.nanoTime();
        System.err.println(
                "Done. (" + (endTime - startTime) / 1_000_000 + " ms; " + Ontology.reasonerCalls + " reasoner calls)");
    }
}
