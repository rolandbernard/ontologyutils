package www.ontologyutils.apps;

import www.ontologyutils.normalization.OntologyNormalization;
import www.ontologyutils.repair.OntologyRepairRandomMcs;
import www.ontologyutils.toolbox.*;

public class AppAutomatedRepairRandomMCS {
    /**
     * One argument must be given, corresponding to an OWL ontology file path. E.g.,
     * run with the parameter resources/inconsistent-leftpolicies-small.owl
     *
     * @param args
     */
    public static void main(String[] args) {
        Ontology ontology = Ontology.loadOntology(args[0]);
        Utils.log("Loaded...");
        OntologyNormalization normalization = new OntologyNormalization();
        OntologyRepairRandomMcs repair = new OntologyRepairRandomMcs();
        Utils.log("Normalizing...");
        normalization.apply(ontology);
        Utils.log("Repairing...");
        repair.apply(ontology);
        Utils.log("Repaired");
        Utils.log("=== RESULT ===");
        System.out.println(ontology.getOwlOntology());
    }
}
