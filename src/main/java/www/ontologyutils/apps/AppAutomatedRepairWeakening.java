package www.ontologyutils.apps;

import www.ontologyutils.normalization.TBoxSubclassOfNormalization;
import www.ontologyutils.repair.*;
import www.ontologyutils.toolbox.*;

public class AppAutomatedRepairWeakening {
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
        OntologyRepair repair = OntologyRepairWeakening.forConsistency();
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
