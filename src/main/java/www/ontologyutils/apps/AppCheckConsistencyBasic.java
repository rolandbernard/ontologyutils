package www.ontologyutils.apps;

import org.semanticweb.owlapi.model.OWLOntology;

import www.ontologyutils.toolbox.Utils;


/**
 * @author nico
 *
 * Takes an owl file in parameter. Prints 1 on the standard output if it is consistent, and prints 0 otherwise.
 *
 */
public class AppCheckConsistencyBasic {

	private static void consincons(boolean cons) {
		if (cons) {
			System.out.print("1");
		} else {
			System.out.print("0");
		}
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Ontology file name expected as parameter.");
			System.out.println("The app prints 1 on the standard output if the ontology is consistent, and 0 otherwise.");
			System.exit(1);
		}
		String ontologyFileName = args[0];
		OWLOntology ontology = Utils.newOntology(ontologyFileName);

		boolean cons = false;

		try {
			cons = Utils.isConsistent(ontology, Utils.ReasonerName.HERMIT);
			consincons(cons);
		} catch (Exception e) {
			System.err.println("ERROR... " + e);
			System.exit(1);
		}
		System.exit(0);
	}
}
