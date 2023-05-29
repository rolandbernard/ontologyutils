package www.ontologyutils.apps;

import www.ontologyutils.toolbox.*;

/**
 * Check the different OWL2 profiles against the given ontology. Outputting the
 * report results.
 */
public class ClassifyOntology {
    /**
     * One argument must be given, corresponding to an OWL ontology file path. E.g.,
     * run with the parameter resources/inconsistent-leftpolicies-small.owl
     *
     * @param args
     *            Must contain one argument representing the file path of an
     *            ontology.
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java " + ClassifyOntology.class.getCanonicalName() + " FILENAME");
            System.exit(1);
        }
        var ontology = Ontology.loadOntology(args[0]).withHermit();
        System.out.print("OWL 2 profiles: ");
        ontology.checkOwlProfiles().forEach(profile -> {
            if (profile.isInProfile()) {
                System.out.print(profile.getProfile().getName() + "; ");
            }
        });
        System.out.println();
        System.out.print("DL languages: ");
        ontology.checkDlExpressivity().forEach(language -> {
            System.out.print(language.name() + "; ");
        });
        System.out.println();
        ontology.close();
    }
}
