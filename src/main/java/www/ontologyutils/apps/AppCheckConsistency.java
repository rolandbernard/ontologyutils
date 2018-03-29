package www.ontologyutils.apps;

import org.semanticweb.owlapi.model.OWLOntology;

import www.ontologyutils.toolbox.Utils;

public class AppCheckConsistency {

	private static void consincons(boolean cons) {
		if (cons) {
			System.out.println("-> consistent");
		} else {
			System.out.println("-> inconsistent");
		}
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Ontology file name expected as parameter.");
			System.exit(1);
		}
		String ontologyFileName = args[0];
		OWLOntology ontology = Utils.newOntology(ontologyFileName);

		boolean cons = false;

		System.out.println("** Consistency check: HERMIT");
		try {
			cons = Utils.isConsistent(ontology, Utils.ReasonerName.HERMIT);
			consincons(cons);
		} catch (Exception e) {
			System.out.println("ERROR... " + e);
		}
		System.out.println("** Consistency check: FACT");
		try {
			cons = Utils.isConsistent(ontology, Utils.ReasonerName.FACT);
			consincons(cons);
		} catch (Exception e) {
			System.out.println("ERROR... " + e);
		}
		System.out.println("** Consistency check: OPENLLET");
		try {
			cons = Utils.isConsistent(ontology, Utils.ReasonerName.OPENLLET);
			consincons(cons);
		} catch (Exception e) {
			System.out.println("ERROR... " + e);
		}
	}
}
