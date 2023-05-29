package www.ontologyutils.apps;

import www.ontologyutils.toolbox.Ontology;

/**
 * Show the ontology.
 */
public class ShowOntology {
    /**
     * @param args
     *            Must contain one argument representing the file path of an
     *            ontology.
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Ontology file name expected as parameter.");
            System.exit(1);
        }
        String ontologyFileName = args[0];
        Ontology ontology = Ontology.loadOntology(ontologyFileName);
        ontology.axioms().forEach(System.out::println);
    }
}
