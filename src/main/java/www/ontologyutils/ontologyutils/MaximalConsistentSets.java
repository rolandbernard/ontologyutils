package www.ontologyutils.ontologyutils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * TODO: useful and safe cache
 */
public class MaximalConsistentSets {

	// Prevent instantiation
	private MaximalConsistentSets() {
	}
	
	public static boolean isMaximallyConsistentSubset(Set<OWLAxiom> subset, Set<OWLAxiom> set) {
		return Utils.isConsistent(subset) && SetUtils.powerSet(set).stream().parallel()
				.allMatch(s -> (s.equals(subset) || !s.containsAll(subset) || !Utils.isConsistent(s)));
	}

	public static Set<Set<OWLAxiom>> maximalConsistentSubsetsNaive(Set<OWLAxiom> axioms) {
		Set<Set<OWLAxiom>> results = new HashSet<>();

		for (Set<OWLAxiom> candidate : SetUtils.powerSet(axioms)) {
			if (isMaximallyConsistentSubset(candidate, axioms)) {
				results.add(candidate);
			}
		}

		return results;
	}

	/**
	 * @author nico
	 *
	 *         Ad hoc data structure to be used in the function
	 *         {@link maximalConsistentSubsets} implementing Robert Malouf's
	 *         "Maximal Consistent Subsets", Computational Linguistics, vol 33(2),
	 *         p.153-160, 2007.
	 * @see maximalConsistentSubsets(Set<OWLAxiom> axioms)
	 */
	private static class McsStruct {
		Set<OWLAxiom> axioms;
		int k;
		boolean leftmost;

		McsStruct(Set<OWLAxiom> axioms, int k, boolean leftmost) {
			this.axioms = axioms;
			this.k = k;
			this.leftmost = leftmost;
		}
	}

	/**
	 * @param axioms
	 *            a set of axioms
	 * @return the set of maximal consistent subsets of axioms.
	 */
	public static Set<Set<OWLAxiom>> maximalConsistentSubsets(Set<OWLAxiom> axioms) {
		// This implementation follows the algorithm in Robert Malouf's "Maximal
		// Consistent Subsets", Computational Linguistics, vol 33(2), p.153-160, 2007.

		ArrayList<OWLAxiom> orderedAxioms = new ArrayList<OWLAxiom>(axioms);
		Set<Set<OWLAxiom>> results = new HashSet<>();

		LinkedList<McsStruct> Q = new LinkedList<>();
		Q.add(new McsStruct(axioms, 0, false));

		while (!Q.isEmpty()) {
			McsStruct current = Q.poll(); // retrieve and dequeue
			if (Utils.isConsistent(current.axioms)) {
				if (results.stream().parallel().allMatch(mcs -> !mcs.containsAll(current.axioms))) {
					// current.axioms is an mcs
					results.add(current.axioms);
				}
			} else {
				Set<OWLAxiom> L = new HashSet<>();
				for (OWLAxiom ax : current.axioms) {
					if (orderedAxioms.indexOf(ax) + 1 <= current.k) {
						L.add(ax);
					}
				} // L is current.axioms's deepest leaf
				if (current.leftmost || Utils.isConsistent(L)) {
					boolean leftmost = true;
					for (int i = current.k + 1; i <= orderedAxioms.size(); i++) {
						Set<OWLAxiom> newSet = new HashSet<>();
						for (OWLAxiom ax : current.axioms) {
							if (orderedAxioms.indexOf(ax) + 1 != i) {
								newSet.add(ax);
							}
						}
						Q.add(new McsStruct(newSet, i, leftmost)); // enqueue
						leftmost = false;
					}
				}
			}
		}

		return results;
	}
	
}
