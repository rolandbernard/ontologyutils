package www.ontologyutils.apps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;

import www.ontologyutils.normalization.NormalizationTools;
import www.ontologyutils.refinement.AxiomWeakener;
import www.ontologyutils.toolbox.AnnotateOrigin;
import www.ontologyutils.toolbox.Utils;

public class AppInteractiveReferenceOntologyAndRepair {

	OWLOntology ontology;

	public AppInteractiveReferenceOntologyAndRepair(String ontologyFilePath) {

		ontology = Utils.newOntology(ontologyFilePath);

	}

	private static int readNumber(String query, int min, int max) {
		int num;
		while (true) {
			System.out.print(query + " > ");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			try {
				num = Integer.parseInt(br.readLine());
			} catch (NumberFormatException e) {
				continue;
			} catch (IOException e) {
				continue;
			}
			if (num < min || num > max) {
				continue;
			}
			return num;
		}
	}

	private static Set<OWLAxiom> findMIS(Set<OWLAxiom> axiomSet, Set<OWLAxiom> axiomsToKeep) {
		HashSet<OWLAxiom> copyAxiomSet = new HashSet<OWLAxiom>(axiomSet);
		if (copyAxiomSet.size() == 1) {
			return copyAxiomSet;
		}
		copyAxiomSet.removeAll(axiomsToKeep);

		OWLAxiom axiom = null;		
		HashSet<OWLAxiom> smallerAxiomSet = null;
		boolean foundSmaller = true;
		while(foundSmaller){
			foundSmaller = false;
			
			// Every time, consider the axioms in a different order
			ArrayList<Integer> axiomsNum = new ArrayList<Integer>();
			for (int i = 0; i < copyAxiomSet.size(); i++) {
				axiomsNum.add(i);
			}
			Collections.shuffle(axiomsNum);
			
			for (int i = 0; i < copyAxiomSet.size(); i++){
				smallerAxiomSet = new HashSet<OWLAxiom>(copyAxiomSet);
				axiom = (OWLAxiom) (copyAxiomSet.toArray())[axiomsNum.get(i)];
				smallerAxiomSet.remove(axiom);
				HashSet<OWLAxiom> smaller = new HashSet<OWLAxiom>(smallerAxiomSet);
				smaller.addAll(axiomsToKeep);
				if (!Utils.isConsistent(smaller)) {
					foundSmaller = true;
					copyAxiomSet.clear();
					copyAxiomSet = smallerAxiomSet;
					break;
				}
			}
		}

		return copyAxiomSet;
	}

	private static Set<OWLAxiom> findSomehowBadAxioms(Set<OWLAxiom> axiomSet, Set<OWLAxiom> axiomsToKeep) {
		
		int precision = (axiomSet.size() - axiomsToKeep.size()) / 2 + 1;
		
		HashMap<OWLAxiom, Integer> hm = new HashMap<OWLAxiom, Integer>();
		ArrayList<OWLAxiom> worstAxioms = new ArrayList<OWLAxiom>();
		
		for(OWLAxiom ax : axiomSet) {
			hm.put(ax, 0);
		}

		int max = 0;
		for(int i = 0 ; i < precision ; i++) {
			ArrayList<OWLAxiom> MIS = new ArrayList<OWLAxiom>(findMIS(axiomSet, axiomsToKeep));
			Collections.shuffle(MIS);
			for (OWLAxiom ax : MIS) {
				int axOccurences = hm.get(ax);
				
				if (axOccurences == max) {
					worstAxioms = new ArrayList<OWLAxiom>();
					worstAxioms.add(ax);
					max = hm.get(ax) + 1;
				} 
				else if (axOccurences == max-1) {
					worstAxioms.add(ax);
				}
				
				hm.put(ax, hm.get(ax) + 1);
			}
		}
		//hm.entrySet().stream().forEach(e -> System.out.println(e.getValue() + "\t" + e.getKey()));;

		return new HashSet<>(worstAxioms);
	}

	private static OWLAxiom selectBadAxiom(Set<OWLAxiom> allAxioms, Set<OWLAxiom> axiomsToKeep) {
		System.out.println("Looking for a bad axiom...");
		ArrayList<OWLAxiom> badAxioms = new ArrayList<OWLAxiom>(findSomehowBadAxioms(allAxioms, axiomsToKeep));
		System.out.println("Select an axiom to weaken.");
		for (int i = 0; i < badAxioms.size(); i++) {
			System.out.println((i + 1) + "\t" + Utils.prettyPrintAxiom(badAxioms.get(i)));
		}
		int axNum = readNumber("Enter axiom number", 1, badAxioms.size());
		OWLAxiom badAxiom = badAxioms.get(axNum - 1);
		return badAxiom;
	}

	private static OWLAxiom selectAxiomManual(Set<OWLAxiom> allAxioms, Set<OWLAxiom> axiomsToKeep) {
		ArrayList<OWLAxiom> axioms = new ArrayList<OWLAxiom>(
				allAxioms.stream().filter(ax -> !axiomsToKeep.contains(ax)).collect(Collectors.toSet()));
		System.out.println("Select an axiom to act upon.");
		for (int i = 0; i < axioms.size(); i++) {
			System.out.println((i + 1) + "\t" + Utils.prettyPrintAxiom(axioms.get(i)));
		}
		int axNum = readNumber("Enter axiom number", 1, axioms.size());
		OWLAxiom axiom = axioms.get(axNum - 1);
		return axiom;
	}

	static class InconsistentSetException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public InconsistentSetException(String message) {
			super(message);
		}
	}

	private static OWLOntology buildReferenceOntology(Set<OWLAxiom> axioms) throws InconsistentSetException {
		final int STOP = 0;
		Set<OWLAxiom> axiomsToKeep = new HashSet<>();
		ArrayList<OWLAxiom> orderedAxioms = new ArrayList<OWLAxiom>(axioms);
		while (Utils.isConsistent(axiomsToKeep)) {
			System.out.println("Select an axiom you want to keep.");
			System.out.println(STOP + "\t[START REPAIR]");
			for (int i = 0; i < orderedAxioms.size(); i++) {
				System.out.println((i + 1) + "\t" + Utils.prettyPrintAxiom(orderedAxioms.get(i)));
			}
			int choice = readNumber("Enter axiom number", STOP, orderedAxioms.size());
			if (choice == STOP) {
				return Utils.newOntology(axiomsToKeep);
			}
			OWLAxiom axiomToKeep = orderedAxioms.get(choice - 1);
			axiomsToKeep.add(axiomToKeep);
			orderedAxioms.remove(axiomToKeep);
		}
		throw new InconsistentSetException("Inconsistent set of axioms.");
	}

	private static Set<OWLAxiom> keepAxiom(Set<OWLAxiom> axiomsToKeep, OWLAxiom newAxiom)
			throws InconsistentSetException {
		axiomsToKeep.add(newAxiom);
		if (!Utils.isConsistent(axiomsToKeep)) {
			throw new InconsistentSetException("Inconsistent set of axioms.");
		}
		System.out.println("Keeping " + axiomsToKeep.size() + " axiom" + (axiomsToKeep.size() >= 2 ? "s." : "."));
		return axiomsToKeep;
	}

	private static ArrayList<OWLAxiom> getWeakerAxioms(OWLAxiom axiom, AxiomWeakener aw) {
		ArrayList<OWLAxiom> weakerAxiomsAux = null;
		if (axiom.isOfType(AxiomType.SUBCLASS_OF)) {
			weakerAxiomsAux = new ArrayList<OWLAxiom>(aw.getWeakerSubClassAxioms((OWLSubClassOfAxiom) axiom));
		} else if (axiom.isOfType(AxiomType.CLASS_ASSERTION)) {
			weakerAxiomsAux = new ArrayList<OWLAxiom>(aw.getWeakerClassAssertionAxioms((OWLClassAssertionAxiom) axiom));
		} else {
			throw new RuntimeException("Cannot weaken axiom that is neither a subclass nor an assertion axiom. "
					+ "Could not repair the ontology.");
		}
		ArrayList<OWLAxiom> weakerAxioms = new ArrayList<OWLAxiom>();
		for (OWLAxiom ax : weakerAxiomsAux) {
			weakerAxioms.add(ax.getAxiomWithoutAnnotations());
		}
		weakerAxioms.remove(axiom.getAxiomWithoutAnnotations());
		weakerAxioms.add(0, axiom.getAxiomWithoutAnnotations());

		return weakerAxioms;
	}

	/**
	 * @param args
	 *            One argument must be given, corresponding to an OWL ontology file
	 *            path. E.g., run with the parameter
	 *            resources/inconsistent-leftpolicies.owl
	 */
	public static void main(String[] args) { // TODO improve user interaction and code
		AppInteractiveReferenceOntologyAndRepair mApp = new AppInteractiveReferenceOntologyAndRepair(args[0]);
		System.out.println("Loaded " + mApp.ontology);
		Set<OWLAxiom> axioms = mApp.ontology.axioms().collect(Collectors.toSet());
		Set<OWLAxiom> nonLogicalAxioms = axioms.stream().filter(ax -> !ax.isLogicalAxiom()).collect(Collectors.toSet());
		// 0- We isolate the logical axioms, and make sure the TBox axioms are all
		// subclass axioms, converting them when necessary.
		Set<OWLAxiom> logicalAxioms = new HashSet<>();
		logicalAxioms.addAll(mApp.ontology.aboxAxioms(Imports.EXCLUDED).collect(Collectors.toSet()));
		logicalAxioms.addAll(mApp.ontology.rboxAxioms(Imports.EXCLUDED).collect(Collectors.toSet()));
		mApp.ontology.tboxAxioms(Imports.EXCLUDED).forEach(ax -> {
			logicalAxioms.addAll(NormalizationTools.asSubClassOfAxioms(ax));
		});
		System.out.println("Converted ontology has " + logicalAxioms.size() + " logical axioms.");

		// 1- Choosing a reference ontology
		OWLOntology referenceOntology = null;
		try {
			referenceOntology = buildReferenceOntology(logicalAxioms);
		} catch (InconsistentSetException e) {
			System.out.println("You selected an inconsistent set of axioms.");
			return;
		}
		// We keep the axioms of the reference ontology, that is, we make sure to have
		// them untouched in the repaired ontology
		Set<OWLAxiom> axiomsToKeep = new HashSet<>(referenceOntology.axioms().collect(Collectors.toSet()));

		// 2- AxiomWeakener
		AxiomWeakener aw = new AxiomWeakener(referenceOntology);

		// 3- Repairing interactively
		while (!Utils.isConsistent(logicalAxioms)) {
			
			// AXIOM SELECTION MODE
			System.out.println("Select an axiom selection mode:");
			System.out.println("1\tManual");
			System.out.println("2\tSuggest axioms to me");
			int axiomSelectionMode = readNumber("Enter the mode number", 1, 2);

			// SELECT AN AXIOM OF INTEREST
			OWLAxiom axiomOfInterest;
			switch (axiomSelectionMode) {
			case 1: {
				axiomOfInterest = selectAxiomManual(logicalAxioms, axiomsToKeep); // All axioms
				break;
			}
			case 2: {
				axiomOfInterest = selectBadAxiom(logicalAxioms, axiomsToKeep); // Bad axioms
				break;
			}
			default:
				axiomOfInterest = selectAxiomManual(logicalAxioms, axiomsToKeep); // All axioms
			}

			// SELECT WEAKENING OF IT (OR KEEP IT)
			ArrayList<OWLAxiom> weakerAxioms = getWeakerAxioms(axiomOfInterest, aw);

			System.out.println("Select an action or a weakening.");
			System.out.println("0 \t[KEEP]");
			for (int i = 0; i < weakerAxioms.size(); i++) {
				System.out.println((i + 1) + "\t"
						+ ((i == 0) ? "[KEEP FOR NOW]" : "" + Utils.prettyPrintAxiom(weakerAxioms.get(i))));
			}
			int axNumW = readNumber("Enter axiom number", 0, weakerAxioms.size());

			if (axNumW == 0) {
				// we try to keep the axiom
				try {
					keepAxiom(axiomsToKeep, axiomOfInterest);
				} catch (InconsistentSetException e) {
					System.out.println("The set of axioms to keep is inconsistent; we reset it.");
					axiomsToKeep = new HashSet<>(referenceOntology.axioms().collect(Collectors.toSet()));
				}
				continue;
			} else {
				// we weaken the axiom
				OWLAxiom weakerAxiom = weakerAxioms.get(axNumW - 1);
				// we remove the axiom of interest and add its weakenings
				logicalAxioms.remove(axiomOfInterest);
				logicalAxioms.add(AnnotateOrigin.getAnnotatedAxiom(weakerAxiom, axiomOfInterest));
				// we log the operation
				System.out.println("- Weaken: \t " + axiomOfInterest + "\n  Into:   \t "
						+ AnnotateOrigin.getAnnotatedAxiom(weakerAxiom, axiomOfInterest) + "\n");
			}
		}

		System.out.println("Repaired ontology.");
		logicalAxioms.forEach(System.out::println);
		nonLogicalAxioms.forEach(System.out::println);
		System.out.println("We specifically tried to keep the following axioms:");
		axiomsToKeep.forEach(System.out::println);
		System.out.println("Done.");
	}

}
