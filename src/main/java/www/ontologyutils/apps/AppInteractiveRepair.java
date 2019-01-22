package www.ontologyutils.apps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
import www.ontologyutils.toolbox.MaximalConsistentSets;
import www.ontologyutils.toolbox.SetUtils;
import www.ontologyutils.toolbox.Utils;

public class AppInteractiveRepair {

	OWLOntology ontology;

	public AppInteractiveRepair(String ontologyFilePath) {

		ontology = Utils.newOntology(ontologyFilePath);

	}

	/**
	 * @param args
	 *            One argument must be given, corresponding to an OWL ontology file
	 *            path. E.g., run with the parameter
	 *            resources/inconsistent-leftpolicies.owl
	 */
	public static void main(String[] args) {
		final int MCS_SAMPLE_SIZE = 1;
		AppInteractiveRepair mApp = new AppInteractiveRepair(args[0]);
		System.out.println("Loaded... " + mApp.ontology);
		Set<OWLAxiom> axioms = mApp.ontology.axioms().collect(Collectors.toSet());
		Set<OWLAxiom> nonLogicalAxioms = axioms.stream().filter(ax -> !ax.isLogicalAxiom()).collect(Collectors.toSet());
		// 0- We isolate the logical axioms, and make sure the TBox axioms are all
		// subclass axioms, converting them when necessary.
		Set<OWLAxiom> logicalAxioms = new HashSet<>();// axioms.stream().filter(ax ->
														// ax.isLogicalAxiom()).collect(Collectors.toSet());
		logicalAxioms.addAll(mApp.ontology.aboxAxioms(Imports.EXCLUDED).collect(Collectors.toSet()));
		logicalAxioms.addAll(mApp.ontology.rboxAxioms(Imports.EXCLUDED).collect(Collectors.toSet()));
		mApp.ontology.tboxAxioms(Imports.EXCLUDED).forEach(ax -> {
			logicalAxioms.addAll(NormalizationTools.asSubClassOfAxioms(ax));
		});
		System.out.println("Converted ontology: " + logicalAxioms.size() + " logical axioms:");
		logicalAxioms.forEach(System.out::println);

		Set<OWLAxiom> axiomsToKeep = new HashSet<>();

		// 1- Choosing a reference ontology (randomly)
		System.out.println("Searching some MCSs and electing one as reference ontology...");
		Set<Set<OWLAxiom>> mcss = MaximalConsistentSets.maximalConsistentSubsets(logicalAxioms, MCS_SAMPLE_SIZE);
		OWLOntology referenceOntology = Utils.newOntology(SetUtils.getRandom(mcss));

		// 2- AxiomWeakener
		AxiomWeakener aw = new AxiomWeakener(referenceOntology);

		// 3- Repairing interactively
		while (!Utils.isConsistent(logicalAxioms)) {
			System.out.println("Looking for a bad axiom...");
			ArrayList<OWLAxiom> badAxioms = new ArrayList<OWLAxiom>(findSomehowBadAxioms(logicalAxioms, axiomsToKeep));

			// SELECT BAD AXIOM
			System.out.println("Select an axiom to weaken.");
			for (int i = 0; i < badAxioms.size(); i++) {
				System.out.println((i + 1) + "\t" + Utils.prettyPrintAxiom(badAxioms.get(i)));
			}
			System.out.print("Enter axiom number > ");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			int axNum = -777;
			try {
				axNum = Integer.parseInt(br.readLine()) - 1;
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			OWLAxiom badAxiom = badAxioms.get(axNum);

			// SELECT WEAKENING
			ArrayList<OWLAxiom> weakerAxiomsAux = null;
			if (badAxiom.isOfType(AxiomType.SUBCLASS_OF)) {
				weakerAxiomsAux = new ArrayList<OWLAxiom>(aw.getWeakerSubClassAxioms((OWLSubClassOfAxiom) badAxiom));
			} else if (badAxiom.isOfType(AxiomType.CLASS_ASSERTION)) {
				weakerAxiomsAux = new ArrayList<OWLAxiom>(
						aw.getWeakerClassAssertionAxioms((OWLClassAssertionAxiom) badAxiom));
			} else {
				throw new RuntimeException("Cannot weaken axiom that is neither a subclass nor an assertion axiom. "
						+ "Could not repair the ontology.");
			}
			ArrayList<OWLAxiom> weakerAxioms = new ArrayList<OWLAxiom>();
			for (OWLAxiom ax : weakerAxiomsAux) {
				weakerAxioms.add(ax.getAxiomWithoutAnnotations());
			}
			weakerAxioms.remove(badAxiom.getAxiomWithoutAnnotations());
			weakerAxioms.add(0, badAxiom.getAxiomWithoutAnnotations());

			System.out.println("Select a weakening.");
			System.out.println("0 \t[KEEP AND DO NOT ASK AGAIN]");
			for (int i = 0; i < weakerAxioms.size(); i++) {
				System.out.println((i + 1) + "\t"
						+ ((i == 0) ? "[KEEP FOR NOW]" : "" + Utils.prettyPrintAxiom(weakerAxioms.get(i))));
			}
			System.out.print("Enter axiom number > ");
			BufferedReader brW = new BufferedReader(new InputStreamReader(System.in));
			int axNumW = -777;
			try {
				axNumW = Integer.parseInt(brW.readLine()) - 1;
			} catch (NumberFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			OWLAxiom weakerAxiom = null;
			if (axNumW == -1) {
				axiomsToKeep.add(badAxiom);
				if (!Utils.isConsistent(axiomsToKeep)) {
					System.out.println("The set of axioms to keep is inconsistent; we empty it.");
					axiomsToKeep = new HashSet<>();
				}
				System.out.println("Keeping " + axiomsToKeep.size() + " axiom" + (axiomsToKeep.size() >= 2?"s.":"."));
				continue;
			} else {
				weakerAxiom = weakerAxioms.get(axNumW);
			}

			// we remove the bad axiom and add its weakenings
			logicalAxioms.remove(badAxiom);
			logicalAxioms.add(AnnotateOrigin.getAnnotatedAxiom(weakerAxiom, badAxiom));
			// we log the operation
			System.out.println("- Weaken: \t " + badAxiom + "\n  Into:   \t "
					+ AnnotateOrigin.getAnnotatedAxiom(weakerAxiom, badAxiom) + "\n");
		}

		System.out.println("Repaired ontology.");
		logicalAxioms.forEach(System.out::println);
		nonLogicalAxioms.forEach(System.out::println);
		System.out.println("We specifically tried to keep the following axioms:");
		axiomsToKeep.forEach(System.out::println);
		System.out.println("Done.");
	}

	// TODO remove code duplication with {@code OntologyRepairWeakening}
	private static Set<OWLAxiom> findSomehowBadAxioms(Set<OWLAxiom> axioms, Set<OWLAxiom> axiomsToKeep) {
		Set<Set<OWLAxiom>> mcss = MaximalConsistentSets.maximalConsistentSubsets(axioms,
				(int) ((axioms.size() - axiomsToKeep.size()) / 4) + 1, axiomsToKeep);
		HashMap<OWLAxiom, Integer> occurences = new HashMap<>();
		for (OWLAxiom ax : axioms) {
			occurences.put(ax, 0);
		}
		for (Set<OWLAxiom> mcs : mcss) {
			mcs.stream().forEach(ax -> {
				if (!occurences.containsKey(ax)) {
					throw new RuntimeException("Did not expect " + ax);
				}
				occurences.put(ax, occurences.get(ax) + 1);
			});
		}
		int minOcc = Integer.MAX_VALUE;
		for (OWLAxiom ax : axioms) {
			if (ax.isOfType(AxiomType.SUBCLASS_OF) || ax.isOfType(AxiomType.CLASS_ASSERTION)) {
				if (!occurences.containsKey(ax)) {
					throw new RuntimeException("Did not expect " + ax);
				}
				minOcc = Integer.min(minOcc, occurences.get(ax));
			}
		}
		Set<OWLAxiom> badAxioms = new HashSet<>();
		for (OWLAxiom ax : axioms) {
			if (ax.isOfType(AxiomType.SUBCLASS_OF) || ax.isOfType(AxiomType.CLASS_ASSERTION)) {
				if (occurences.get(ax) == minOcc) {
					badAxioms.add(ax);
				}
			}
		}
		if (badAxioms.size() < 1) {
			throw new RuntimeException("Did not find a bad subclass or assertion axiom in " + axioms);
		}
		return badAxioms;
	}
}
