package www.ontologyutils.apps;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import www.ontologyutils.normalization.NormalizationTools;
import www.ontologyutils.refinement.AxiomStrengthener;
import www.ontologyutils.toolbox.SetUtils;
import www.ontologyutils.toolbox.Utils;

public class AppMakeInconsistent {
	OWLOntology ontology;
	
	public AppMakeInconsistent(String ontologyFilePath) {
		ontology = normalize(Utils.newOntologyExcludeNonLogicalAxioms(ontologyFilePath));
	}
	
	private static OWLOntology normalize(OWLOntology ontology) {
		OWLOntology normal = Utils.newEmptyOntology();
		normal.addAxioms(ontology.axioms());
		Stream<OWLAxiom> tBoxAxioms = normal.tboxAxioms(Imports.EXCLUDED);
		tBoxAxioms.forEach((ax) -> {
			normal.remove(ax);
			normal.addAxioms(NormalizationTools.asSubClassOfAxioms(ax));
		});
		return normal;
	}
	
	public static void saveOntology(OWLOntology ontology, String origName) {
		String fileName = "resources/" 
				+ origName.replaceAll("^.*/", "").replaceAll(".owl","")
				+ "-made-inconsistent.owl";
		File file = new File(fileName);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		try {
			manager.saveOntology(ontology, ontology.getFormat(), IRI.create(file.toURI()));
			System.out.println("Inconsistent ontology saved as " + fileName);
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * @param args
	 *            A first argument must be given, corresponding to an OWL ontology file
	 *            path. E.g., run with the parameter
	 *            resources/catsandnumbers.owl
	 *            A second argument can be given, to indicate the minimal number of strengthening 
	 *            iterations must be done.
	 *            A third argument can be given, to indicate the minimal number of iterations 
	 *            needed that must be done after reaching inconsistency.
	 */
	public static void main(String[] args) {
		AppMakeInconsistent mApp = new AppMakeInconsistent(args[0]);
		System.out.println("Loaded... " + mApp.ontology);
		if (!Utils.isConsistent(mApp.ontology)) {
			System.out.println("Ontology is already inconsistent.");
			return;
		}
		int minNumIter = 0;
		try {
			minNumIter = Integer.parseInt(args[1]);
			System.out.println("Minimal number of strengthening iterations: " + minNumIter);
		} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
			System.out.println("No minimal number of strengthening iterations specified.");
		}
		int minNumIterAfterIncon = 0;
		try {
			minNumIterAfterIncon = Integer.parseInt(args[2]);
			System.out.println("Minimal number of iterations after reaching inconsistency: " + minNumIterAfterIncon);
		} catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
			System.out.println("No minimal number of iterations after reaching inconsistency specified.");
		}
		AxiomStrengthener as = new AxiomStrengthener(mApp.ontology);
		
		int iter = 0;
		int iterSinceIncon = 0;
		boolean isConsistent = Utils.isConsistent(mApp.ontology);
		while (isConsistent || iter < minNumIter || iterSinceIncon < minNumIterAfterIncon) {
			System.out.println(" ... " + mApp.ontology.axioms().count() + " axioms" + (isConsistent?"":"-> INCONSISTENT"));
			OWLAxiom ax = SetUtils.getRandom(mApp.ontology.axioms().collect(Collectors.toSet()));
			Set<OWLAxiom> stronger = null;
			if (ax.getAxiomType() == AxiomType.SUBCLASS_OF) {
				stronger = as.getStrongerSubClassAxioms((OWLSubClassOfAxiom) ax);
			} else if (ax.getAxiomType() == AxiomType.CLASS_ASSERTION) {
				stronger = as.getStrongerClassAssertionAxioms((OWLClassAssertionAxiom) ax);
			} else {
				continue;
			}
			// we do not consider the axioms already in the ontology
			stronger.removeAll(mApp.ontology.axioms().collect(Collectors.toSet()));
				
			// we do not consider axioms that are inconsistent on their own, 
			// could be made optional
			Set<OWLAxiom> tooStrong = new HashSet<>();
			for (OWLAxiom sta : stronger) { 
				OWLOntology emptyOntology = Utils.newEmptyOntology();
				emptyOntology.add(sta);
				if (!Utils.isConsistent(emptyOntology)) {
					tooStrong.add(sta);
				}
			}
			stronger.removeAll(tooStrong);
			
			// we do not consider axioms that are tautologies, 
			// could be made optional
			Set<OWLAxiom> tautologies = new HashSet<>();
			OWLReasoner emptyReasoner = Utils.getHermitReasoner(Utils.newEmptyOntology());
			for (OWLAxiom sta : stronger) { 
				if (emptyReasoner.isEntailed(sta)) {
					tautologies.add(sta);
				}
			}
			stronger.removeAll(tautologies);
			
			if (!stronger.isEmpty()) {
				OWLAxiom strongerAx = SetUtils.getRandom(stronger);
				System.out.println("strengthening: " + Utils.prettyPrintAxiom(ax));
				// alternatively, remove the old axioms...
				// System.out.println("remove: " + Utils.prettyPrintAxiom(ax));
				// mApp.ontology.remove(ax);
				System.out.println("adding: " + Utils.prettyPrintAxiom(strongerAx));
				mApp.ontology.addAxiom(strongerAx);
			}
			
			isConsistent = Utils.isConsistent(mApp.ontology);
			iter++;
			if(!isConsistent) {
				iterSinceIncon++;
			} else {
				iterSinceIncon = 0;
			}
		}
		
		System.out.println("****** Result");
		mApp.ontology.axioms().forEach(ax -> System.out.println(Utils.prettyPrintAxiom(ax)));
		
		saveOntology(mApp.ontology, args[0]);
	}
}
