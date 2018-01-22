package www.ontologyutils.toolbox;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * TODO: useful and safe cache
 */
public class MaximalConsistentSets {

	public static final int ALL_MCSS = -1;

	// Prevent instantiation
	private MaximalConsistentSets() {
	}

	public static boolean isMaximallyConsistentSubset(Set<OWLAxiom> subset, Set<OWLAxiom> set) {
		return Utils.isConsistent(subset) && SetUtils.powerSet(set).stream()
				.allMatch(s -> (s.equals(subset) || !s.containsAll(subset) || !Utils.isConsistent(s)));
	}

	/**
	 * @param axioms
	 *            a set of axioms
	 * @return the set of maximal consistent subsets of axioms from {@code axioms}.
	 */
	public static Set<Set<OWLAxiom>> maximalConsistentSubsetsNaive(Set<OWLAxiom> axioms) {
		return maximalConsistentSubsetsNaive(axioms, ALL_MCSS);
	}

	/**
	 * @param axioms
	 *            a set of axioms
	 * @param howMany
	 *            the maximal number of maximal consistent subsets to be returned
	 * @return a set of at most {@code howMany} maximal consistent subsets of axioms
	 *         from {@code axioms}.
	 */
	public static Set<Set<OWLAxiom>> maximalConsistentSubsetsNaive(Set<OWLAxiom> axioms, int howMany) {
		return maximalConsistentSubsetsNaive(axioms, howMany, new HashSet<>());
	}

	/**
	 * @param axioms
	 *            a set of axioms
	 * @param howMany
	 *            the maximal number of maximal consistent subsets to be returned
	 * @param contained
	 *            a set of axioms that must be contained by the returned maximal
	 *            consistent sets.
	 * @return a set of at most {@code howMany} maximal consistent subsets of axioms
	 *         from {@code axioms} containing {@code contained}.
	 */
	public static Set<Set<OWLAxiom>> maximalConsistentSubsetsNaive(Set<OWLAxiom> axioms, int howMany,
			Set<OWLAxiom> contained) {
		Set<Set<OWLAxiom>> results = new HashSet<>();
		if (!axioms.containsAll(contained)) {
			return results;
		}
		for (Set<OWLAxiom> candidate : SetUtils.powerSet(axioms)) {
			if (candidate.containsAll(contained) && isMaximallyConsistentSubset(candidate, axioms)) {
				results.add(candidate);
				if (howMany != ALL_MCSS && results.size() == howMany) {
					return results;
				}
			}
		}
		return results;
	}

	/**
	 * Ad hoc data structure to be used in the function
	 * {@link maximalConsistentSubsets} implementing Robert Malouf's "Maximal
	 * Consistent Subsets", Computational Linguistics, vol 33(2), p.153-160, 2007.
	 * 
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
	 * @return the set of maximal consistent subsets of axioms from {@code axioms}.
	 */
	public static Set<Set<OWLAxiom>> maximalConsistentSubsets(Set<OWLAxiom> axioms) {
		return maximalConsistentSubsets(axioms, ALL_MCSS);
	}

	/**
	 * @param axioms
	 *            a set of axioms
	 * @param howMany
	 *            the maximal number of maximal consistent subsets to be returned
	 * @return a set of at most {@code howMany} maximal consistent subsets of axioms
	 *         from {@code axioms}.
	 */
	public static Set<Set<OWLAxiom>> maximalConsistentSubsets(Set<OWLAxiom> axioms, int howMany) {
		// This implementation follows the algorithm in Robert Malouf's "Maximal
		// Consistent Subsets", Computational Linguistics, vol 33(2), p.153-160, 2007.

		final ArrayList<OWLAxiom> orderedAxioms = new ArrayList<OWLAxiom>(axioms);
		Set<Set<OWLAxiom>> results = new HashSet<>();

		Deque<McsStruct> Q = new ArrayDeque<>();
		Q.add(new McsStruct(axioms, 0, false));

		while (!Q.isEmpty()) {
			McsStruct current = Q.poll(); // retrieve and dequeue
			if (Utils.isConsistent(current.axioms)) {
				if (results.stream().allMatch(mcs -> !mcs.containsAll(current.axioms))) {
					// current.axioms is an mcs
					results.add(current.axioms);
					if (howMany != ALL_MCSS && results.size() == howMany) {
						return results;
					}
				}
			} else {
				Set<OWLAxiom> L = current.axioms.stream().filter(ax -> orderedAxioms.indexOf(ax) + 1 <= current.k)
						.collect(Collectors.toSet());
				// L is current.axioms's deepest leaf
				if (current.leftmost || Utils.isConsistent(L)) {
					boolean leftmost = true;
					for (int i = current.k + 1; i <= orderedAxioms.size(); i++) {
						final int index = i;
						Set<OWLAxiom> newSet = current.axioms.stream()
								.filter(ax -> orderedAxioms.indexOf(ax) + 1 != index).collect(Collectors.toSet());
						Q.add(new McsStruct(newSet, i, leftmost)); // enqueue
						leftmost = false;
					}
				}
			}
		}

		return results;
	}

	/**
	 * @param axioms
	 *            a set of axioms
	 * @param howMany
	 *            the maximal number of maximal consistent subsets to be returned
	 * @param contained
	 *            a set of axioms that must be contained by the returned maximal
	 *            consistent sets.
	 * @return a set of at most {@code howMany} maximal consistent subsets of axioms
	 *         from {@code axioms} containing {@code contained}. TODO
	 */
	public static Set<Set<OWLAxiom>> maximalConsistentSubsets(Set<OWLAxiom> axioms, int howMany,
			Set<OWLAxiom> contained) {
		return null; //maximalConsistentSubsetsNaive(axioms, howMany, contained); TODO
	}

}
