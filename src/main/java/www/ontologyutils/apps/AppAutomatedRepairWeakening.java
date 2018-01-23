package www.ontologyutils.apps;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;

import www.ontologyutils.normalization.NormalizationTools;
import www.ontologyutils.repair.OntologyRepairWeakening;
import www.ontologyutils.toolbox.Utils;

public class AppAutomatedRepairWeakening {

	OWLOntology ontology;

	public AppAutomatedRepairWeakening(String ontologyFilePath) {

		ontology = Utils.newOntology(ontologyFilePath);

	}

	/**
	 * @param args
	 *            One argument must be given, corresponding to an OWL ontology file
	 *            path. E.g., run with the parameter
	 *            resources/inconsistent-leftpolicies-small.owl
	 */
	public static void main(String[] args) {
		AppAutomatedRepairWeakening mApp = new AppAutomatedRepairWeakening(args[0]);
		System.out.println("Loaded... " + mApp.ontology);
		Set<OWLAxiom> axioms = mApp.ontology.axioms().collect(Collectors.toSet());
		Set<OWLAxiom> nonLogicalAxioms = axioms.stream().filter(ax -> !ax.isLogicalAxiom()).collect(Collectors.toSet());
		// We isolate the logical axioms, and make sure the TBox axioms are all
		// subclass axioms, converting them when necessary.
		Set<OWLAxiom> logicalAxioms = new HashSet<>();
		logicalAxioms.addAll(mApp.ontology.aboxAxioms(Imports.EXCLUDED).collect(Collectors.toSet()));
		logicalAxioms.addAll(mApp.ontology.rboxAxioms(Imports.EXCLUDED).collect(Collectors.toSet()));
		mApp.ontology.tboxAxioms(Imports.EXCLUDED).forEach(ax -> {
			logicalAxioms.addAll(NormalizationTools.asSubClassOfAxioms(ax));
		});
		System.out.println("Converted ontology: " + logicalAxioms.size() + " logical axioms:");
		logicalAxioms.forEach(System.out::println);
		
		OntologyRepairWeakening orw = new OntologyRepairWeakening(Utils.newOntology(logicalAxioms));
		System.out.println("Repairing... ");
		OWLOntology repaired = orw.repair();
		
		System.out.println("Repaired ontology.");
		repaired.axioms().forEach(System.out::println);
		nonLogicalAxioms.forEach(System.out::println);
		
		System.out.println("Done.");
	}
}
