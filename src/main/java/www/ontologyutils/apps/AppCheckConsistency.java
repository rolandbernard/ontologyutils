package www.ontologyutils.apps;

import www.ontologyutils.toolbox.Ontology;

/**
 * Class for checking the consistency of an ontology. Checks will be performed
 * using all supported reasoners.
 */
public class AppCheckConsistency {
    private static void printResult(boolean consistent) {
        if (consistent) {
            System.out.println("-> consistent");
        } else {
            System.out.println("-> inconsistent");
        }
    }

    /**
     * @param args
     *            Must contain one argument representing the file path of an
     *            ontology.
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Ontology file name expected as parameter.");
            System.exit(1);
        }
        String ontologyFileName = args[0];
        Ontology ontology = Ontology.loadOntology(ontologyFileName);
        boolean cons = false;
        System.err.println("** Consistency check: HERMIT");
        try (var withHermit = ontology.cloneWithHermit()) {
            cons = withHermit.isConsistent();
            printResult(cons);
        } catch (Exception e) {
            System.err.println("ERROR... " + e);
        }
        System.err.println("** Consistency check: FACT");
        try (var withFact = ontology.cloneWithJFact()) {
            cons = withFact.isConsistent();
            printResult(cons);
        } catch (Exception e) {
            System.out.println("ERROR... " + e);
        }
        System.err.println("** Consistency check: OPENLLET");
        try (var withOpenllet = ontology.cloneWithOpenllet()) {
            cons = withOpenllet.isConsistent();
            printResult(cons);
        } catch (Exception e) {
            System.err.println("ERROR... " + e);
        }
        System.err.println("** Consistency check: FaCT++");
        try (var withFactPP = ontology.cloneWithFactPP()) {
            cons = withFactPP.isConsistent();
            printResult(cons);
        } catch (Exception e) {
            System.err.println("ERROR... " + e);
        }
        ontology.close();
    }
}
