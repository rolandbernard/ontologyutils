package www.ontologyutils.apps;

import org.semanticweb.owlapi.model.OWLOntology;

import www.ontologyutils.toolbox.Utils;

public class AppShowOntology {
	
	public static void main( String[] args )
    {
		if (args.length != 1) {
			System.out.println("Ontology file name expected as parameter.");
			System.exit(1);
		}
		String ontologyFileName = args[0];
        OWLOntology ontology = Utils.newOntology(ontologyFileName);
        
        ontology.axioms().forEach(System.out::println);
    }
}
