package www.ontologyutils.apps;

import www.ontologyutils.normalization.TBoxSubclassOfNormalization;
import www.ontologyutils.repair.*;
import www.ontologyutils.toolbox.*;

public class AppAutomatedRepairRandomMCS {
    /**
     * One argument must be given, corresponding to an OWL ontology file path. E.g.,
     * run with the parameter
     * src/test/resources/www/ontologyutils/inconsistent-leftpolicies.owl
     *
     * @param args
     */
    public static void main(final String[] args) {
        final var ontology = Ontology.loadOntology(args[0]);
        Utils.log("Loaded...");
        final var normalization = new TBoxSubclassOfNormalization();
        final var repair = OntologyRepairRandomMcs.forConsistency();
        Utils.log("Normalizing...");
        normalization.apply(ontology);
        Utils.log("Repairing...");
        repair.apply(ontology);
        Utils.log("Repaired.");
        Utils.log("=== BEGIN RESULT ===");
        ontology.refutableAxioms().sorted().forEach(System.out::println);
        ontology.staticAxioms().sorted().forEach(System.out::println);
        assert ontology.isConsistent();
        ontology.close();
        Utils.log("==== END RESULT ====");
        Utils.log("Done.");
    }
}
