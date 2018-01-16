package www.ontologyutils.repair;

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

public class OntologyRepairWeakening implements OntologyRepair {

	private OWLOntology originalOntology;
	private Boolean verbose;

	private void log(String s) {
		if (verbose) {
			System.out.print(s);
		}
	}

	public OntologyRepairWeakening(OWLOntology ontology, Boolean verbose) {
		this.originalOntology = ontology;
		this.verbose = verbose;
	}

	public OntologyRepairWeakening(OWLOntology ontology) {
		this(ontology, false);
	}

	@Override
	public OWLOntology repair() {
		// TODO: normalize TBox

		Set<OWLAxiom> axioms = originalOntology.axioms().collect(Collectors.toSet());
		// 1- Choosing a reference ontology as a random MCS of the original axioms
		Set<Set<OWLAxiom>> mcss = MaximalConsistentSets.maximalConsistentSubsets(axioms);
		OWLOntology referenceOntology = Utils.newOntology(SetUtils.getRandom(mcss).stream());
		// 2- AxiomWeakener
		AxiomWeakener aw = new AxiomWeakener(referenceOntology);
		// 3- Repairing
		while (!Utils.isConsistent(axioms)) {
			OWLAxiom badAxiom = findBadAxiom(axioms);
			axioms.remove(badAxiom);
			Set<OWLAxiom> weakerAxioms = null;
			if (badAxiom.isOfType(AxiomType.SUBCLASS_OF)) {
				weakerAxioms = aw.getWeakerSubClassAxioms((OWLSubClassOfAxiom) badAxiom);
			} else if (badAxiom.isOfType(AxiomType.CLASS_ASSERTION)) {
				weakerAxioms = aw.getWeakerClassAssertionAxioms((OWLClassAssertionAxiom) badAxiom);
			} else {
				throw new RuntimeException("Cannot weaken axiom that is neither a subclass nor an assertion axiom. "
						+ "Could not repair the ontology.");
			}
			// we remove the bad axiom and add one of its weakenings
			weakerAxioms.remove(badAxiom);
			OWLAxiom weakerAxiom = SetUtils.getRandom(weakerAxioms);
			axioms.add(weakerAxiom);
			// we log the operation
			log("- Weaken: \t " + badAxiom + "\n  Into:   \t " + weakerAxiom + "\n");
		}
		return Utils.newOntology(axioms.stream());
	}

	/**
	 * @param axioms
	 * @return one of the subclass or assertion axioms occurring in the least number
	 *         of maximal consistent sets of {@code axioms}.
	 */
	private OWLAxiom findBadAxiom(Set<OWLAxiom> axioms) {
		Set<Set<OWLAxiom>> mcss = MaximalConsistentSets.maximalConsistentSubsets(axioms);
		HashMap<OWLAxiom, Integer> occurences = new HashMap<>();
		for (Set<OWLAxiom> mcs : mcss) {
			mcs.stream().filter(a -> a.isOfType(AxiomType.SUBCLASS_OF) || a.isOfType(AxiomType.CLASS_ASSERTION))
					.forEach(ax -> {
						int occ = occurences.containsKey(ax) ? occurences.get(ax) : 0;
						occurences.put(ax, occ + 1);
					});
		}
		int minOcc = Integer.MAX_VALUE;
		for (OWLAxiom a : axioms) {
			if (a.isOfType(AxiomType.SUBCLASS_OF) || a.isOfType(AxiomType.CLASS_ASSERTION)) {
				minOcc = Integer.min(minOcc, occurences.get(a));
			}
		}
		Set<OWLAxiom> badAxioms = new HashSet<>();
		for (OWLAxiom a : axioms) {
			if (a.isOfType(AxiomType.SUBCLASS_OF) || a.isOfType(AxiomType.CLASS_ASSERTION)) {
				if (occurences.get(a) == minOcc) {
					badAxioms.add(a);
				}
			}
		}
		if (badAxioms.size() < 1) {
			throw new RuntimeException("Did not find a bad subclass or assertion axiom in " + axioms);
		}
		return SetUtils.getRandom(badAxioms);
	}

}
