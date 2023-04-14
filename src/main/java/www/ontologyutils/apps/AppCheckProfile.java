package www.ontologyutils.apps;

import www.ontologyutils.toolbox.*;

public class AppCheckProfile {
    /**
     * One argument must be given, corresponding to an OWL ontology file path. E.g.,
     * run with the parameter resources/inconsistent-leftpolicies-small.owl
     *
     * @param args
     */
    public static void main(final String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java " + AppCheckProfile.class.getCanonicalName() + " FILENAME");
            System.exit(1);
        }
        final var ontology = Ontology.loadOntology(args[0]);
        ontology.checkOwlProfiles().forEach(System.out::println);
        ontology.close();
    }
}
