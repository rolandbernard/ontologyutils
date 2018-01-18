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

import www.ontologyutils.ontologyutils.MaximalConsistentSets;
import www.ontologyutils.ontologyutils.SetUtils;
import www.ontologyutils.ontologyutils.Utils;
import www.ontologyutils.refinement.AxiomWeakener;

public class AppRepair {

	OWLOntology ontology;
	
	public AppRepair(String ontologyFilePath) {

		ontology = Utils.newOntology(ontologyFilePath);

	}

	/**
	 * @param args
	 *            One argument must be given, corresponding to an OWL ontology file
	 *            path. E.g., run with the parameter resources/inconsistent-leftpolicies.owl
	 */
	public static void main(String[] args) {		
		AppRepair mApp = new AppRepair(args[0]);
		System.out.println("Loaded... " + mApp.ontology);
		Set<OWLAxiom> axioms = mApp.ontology.axioms().collect(Collectors.toSet());
		Set<OWLAxiom> logicalAxioms = axioms.stream().filter(ax -> ax.isLogicalAxiom()).collect(Collectors.toSet());
		Set<OWLAxiom> nonLogicalAxioms = axioms.stream().filter(ax -> !ax.isLogicalAxiom()).collect(Collectors.toSet());
		
		// 1- Choosing a reference ontology (randomly)
		System.out.println("Searching all MCSs and electing one as reference ontology...");
		Set<Set<OWLAxiom>> mcss = MaximalConsistentSets.maximalConsistentSubsets(logicalAxioms);
		OWLOntology referenceOntology = Utils.newOntology(SetUtils.getRandom(mcss).stream());
				
		// 2- AxiomWeakener
		AxiomWeakener aw = new AxiomWeakener(referenceOntology);
		
		// 3- Repairing
		while (!Utils.isConsistent(logicalAxioms)) {
			ArrayList<OWLAxiom> badAxioms = new ArrayList<OWLAxiom>(findBadAxioms(logicalAxioms));
			
			// SELECT BAD AXIOM
			System.out.println("Select an axiom to weaken.");
			for (int i = 0 ; i < badAxioms.size() ; i++) {
				System.out.println((i + 1) + "\t" + badAxioms.get(i));
			}
			System.out.println("Enter axiom number >");
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
			int axNum = -777;
			try{
	            axNum = Integer.parseInt(br.readLine()) - 1;
	        } catch (NumberFormatException e) {
	        		e.printStackTrace();
	        } catch (IOException e) {
				e.printStackTrace();
			}
			OWLAxiom badAxiom = badAxioms.get(axNum);
			
			// SELECT WEAKENING
			ArrayList<OWLAxiom> weakerAxioms = null;
			if (badAxiom.isOfType(AxiomType.SUBCLASS_OF)) {
				weakerAxioms = new ArrayList<OWLAxiom>(aw.getWeakerSubClassAxioms((OWLSubClassOfAxiom) badAxiom));
			} else if (badAxiom.isOfType(AxiomType.CLASS_ASSERTION)) {
				weakerAxioms = new ArrayList<OWLAxiom>(aw.getWeakerClassAssertionAxioms((OWLClassAssertionAxiom) badAxiom));
			} else {
				throw new RuntimeException("Cannot weaken axiom that is neither a subclass nor an assertion axiom. "
						+ "Could not repair the ontology.");
			}
			weakerAxioms.remove(badAxiom);
			weakerAxioms.add(0, badAxiom);

			System.out.println("Select a weakening.");
			for (int i = 0 ; i < weakerAxioms.size() ; i++) {
				System.out.println((i + 1) + "\t" + weakerAxioms.get(i));
			}
			System.out.println("Enter axiom number > ");
			BufferedReader brW = new BufferedReader(new InputStreamReader(System.in));
			int axNumW = -777;
			try{
	            axNumW = Integer.parseInt(brW.readLine()) - 1;
	        } catch (NumberFormatException e) {
	        		e.printStackTrace();
	        } catch (IOException e) {
				e.printStackTrace();
			}
			OWLAxiom weakerAxiom = weakerAxioms.get(axNumW);
			
			// we remove the bad axiom and add its weakenings
			logicalAxioms.remove(badAxiom);
			logicalAxioms.add(weakerAxiom);
			// we log the operation
			System.out.println("- Weaken: \t " + badAxiom + "\n  Into:   \t " + weakerAxiom + "\n");
		}
		
		System.out.println("Repaired ontology.");
		logicalAxioms.forEach(System.out::println);
		nonLogicalAxioms.forEach(System.out::println);
	}
	
	// TODO remove code duplication with {@code OntologyRepairWeakening}
	private static Set<OWLAxiom> findBadAxioms(Set<OWLAxiom> axioms) {
		Set<Set<OWLAxiom>> mcss = MaximalConsistentSets.maximalConsistentSubsets(axioms);
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
