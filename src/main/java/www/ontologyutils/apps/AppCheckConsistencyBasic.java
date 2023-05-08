package www.ontologyutils.apps;

import www.ontologyutils.toolbox.Ontology;

/**
 * Takes an owl file in parameter. Prints 1 on the standard output if it
 * is consistent, and prints 0 otherwise.
 *
 * @author nico
 */
public class AppCheckConsistencyBasic {
    private static void printOutput(boolean cons) {
        if (cons) {
            System.out.print("1");
        } else {
            System.out.print("0");
        }
    }

    /**
     * @param args
     *            A single argument with the file path to the ontology.
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Ontology file name expected as parameter.");
            System.err
                    .println("The app prints 1 on the standard output if the ontology is consistent, and 0 otherwise.");
            System.exit(1);
        }
        String ontologyFileName = args[0];
        Ontology ontology = Ontology.loadOntology(ontologyFileName);
        boolean cons = false;
        try {
            cons = ontology.isConsistent();
            printOutput(cons);
        } catch (Exception e) {
            System.err.println("ERROR... " + e);
            System.exit(1);
        }
        System.exit(0);
        ontology.close();
    }
}
