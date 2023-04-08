package www.ontologyutils.apps;

import www.ontologyutils.normalization.TBoxSubclassOfNormalization;
import www.ontologyutils.repair.*;
import www.ontologyutils.toolbox.*;

public class AppAutomatedRepairRandomMCS {
    /**
     * One argument must be given, corresponding to an OWL ontology file path. E.g.,
     * run with the parameter resources/inconsistent-leftpolicies-small.owl
     *
     * @param args
     */
    public static void main(final String[] args) {
        Ontology ontology = Ontology.loadOntology(args[0]);
        Utils.log("Loaded...");
        TBoxSubclassOfNormalization normalization = new TBoxSubclassOfNormalization();
        OntologyRepair repair = OntologyRepairRandomMcs.forConsistency();
        Utils.log("Normalizing...");
        normalization.apply(ontology);
        Utils.log("Repairing...");
        repair.apply(ontology);
        Utils.log("Repaired.");
        Utils.log("=== BEGIN RESULT ===");
        ontology.refutableAxioms().forEach(System.out::println);
        ontology.staticAxioms().forEach(System.out::println);
        assert ontology.isConsistent();
        ontology.dispose();
        Utils.log("==== END RESULT ====");
        Utils.log("Done.");
    }
}
