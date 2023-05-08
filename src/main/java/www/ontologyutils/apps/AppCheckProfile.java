package www.ontologyutils.apps;

import www.ontologyutils.toolbox.*;

/**
 * Check the different OWL2 profiles against the given ontology. Outputting the
 * report results.
 */
public class AppCheckProfile {
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
            System.err.println("Usage: java " + AppCheckProfile.class.getCanonicalName() + " FILENAME");
            System.exit(1);
        }
        var ontology = Ontology.loadOntology(args[0]);
        ontology.checkOwlProfiles().forEach(System.out::println);
        ontology.close();
    }
}
