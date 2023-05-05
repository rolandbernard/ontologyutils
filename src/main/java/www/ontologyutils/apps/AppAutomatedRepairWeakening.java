package www.ontologyutils.apps;

import org.semanticweb.owlapi.model.OWLAxiom;

import www.ontologyutils.normalization.SroiqNormalization;
import www.ontologyutils.repair.*;
import www.ontologyutils.repair.OntologyRepairWeakening.BadAxiomStrategy;
import www.ontologyutils.repair.OntologyRepairWeakening.RefOntologyStrategy;
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
    public static void main(String[] args) {
        if (args.length < 1 || args.length > 2 || args.length == 2 && !args[0].equals("fast")) {
            System.err
                    .println("Usage: java " + AppAutomatedRepairWeakening.class.getCanonicalName() + "[fast] FILENAME");
            System.exit(1);
        }
        boolean fast = args[0].equals("fast");
        var ontology = Ontology.loadOntology(fast ? args[1] : args[0]);
        System.err.println("Loaded...");
        var normalization = new SroiqNormalization();
        var repair = fast
                ? new OntologyRepairWeakening(Ontology::isConsistent, RefOntologyStrategy.ONE_MCS,
                        BadAxiomStrategy.IN_ONE_MUS)
                : OntologyRepairWeakening.forConsistency();
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
