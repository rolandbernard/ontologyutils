package www.ontologyutils.apps;

import org.semanticweb.owlapi.model.OWLAxiom;

import www.ontologyutils.normalization.SroiqNormalization;
import www.ontologyutils.repair.*;
import www.ontologyutils.toolbox.*;

public class AppAutomatedRepairWeakening {
    private static String getTimeStamp() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new java.util.Date());
    }

    /**
     * One argument must be given, corresponding to an OWL ontology file path. E.g.,
     * run with the parameter
     * src/test/resources/www/ontologyutils/inconsistent-leftpolicies.owl
     *
     * @param args
     */
    public static void main(final String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java " + AppAutomatedRepairWeakening.class.getCanonicalName() + " FILENAME");
            System.exit(1);
        }
        final var ontology = Ontology.loadOntology(args[0]);
        System.err.println("Loaded...");
        final var normalization = new SroiqNormalization();
        final var repair = OntologyRepairWeakening.forConsistency();
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
        System.err.println("Done.");
    }
}
