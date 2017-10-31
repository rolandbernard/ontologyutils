package www.ontologyutils.ontologyutils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;

import uk.ac.manchester.cs.jfact.JFactFactory;

/**
 * @author nico
 *
 */
public class Utils {

	// Prevent instantiation
	private Utils() {
	}

	public static void log(String tag, String message) {
		System.out.println(tag + " : " + message);
	}

	private static void log(String message) {
		log("Utils", message);
	}
	
	public static OWLOntology newEmptyOntology() {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology newOntology = null;
		try {
			newOntology = manager.createOntology();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return newOntology;
	}

	public static OWLOntology newOntology(Stream<OWLAxiom> axioms) {
		OWLOntology ontology = newEmptyOntology();
		ontology.addAxioms(axioms);
		return ontology;
	}
	
	public static OWLOntology newOntology(String ontologyFilePath) {

		File ontologyFile = new File(ontologyFilePath);

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		IRI ontologyIRI = IRI.create(ontologyFile);

		OWLOntology ontology = null;

		try {
			ontology = manager.loadOntologyFromOntologyDocument(ontologyIRI);
			System.out.println("Ontology loaded.");
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			System.exit(1);
		}

		return ontology;
	}

	public static boolean isConsistent(OWLOntology ontology) {
		OWLReasonerFactory reasonerFactory = new JFactFactory();
		OWLReasonerConfiguration config = new SimpleConfiguration(5000);
		OWLReasoner reasoner = reasonerFactory.createReasoner(ontology, config);

		boolean result = reasoner.isConsistent();
		reasoner.dispose();

		return result;
	}

	private static HashMap<Set<OWLAxiom>, Boolean> uglyAxiomSetConsistencyCache = new HashMap<>();

	public static boolean isConsistent(Set<OWLAxiom> axioms) {
		Boolean consistency = uglyAxiomSetConsistencyCache.get(axioms);
		if (consistency != null) {
			return consistency;
		}
		consistency = isConsistent(newOntology(axioms.stream()));
		uglyAxiomSetConsistencyCache.put(axioms, consistency);
		return consistency;
	}

	public static <T> Set<Set<T>> powerSet(Set<T> set) {
		T[] element = (T[]) set.toArray();
		final int SET_LENGTH = 1 << element.length;
		Set<Set<T>> powerSet = new HashSet<>();
		for (int binarySet = 0; binarySet < SET_LENGTH; binarySet++) {
			Set<T> subset = new HashSet<>();
			for (int bit = 0; bit < element.length; bit++) {
				int mask = 1 << bit;
				if ((binarySet & mask) != 0) {
					subset.add(element[bit]);
				}
			}
			powerSet.add(subset);
		}
		return powerSet;
	}

	public static <T> Set<T> streamToSet(Stream<T> stream) {
		return stream.collect(Collectors.toSet());
	}

	public static boolean isMaximallyConsistentSubset(Set<OWLAxiom> subset, Set<OWLAxiom> set) {
		return isConsistent(subset) && powerSet(set).stream()
				.allMatch(s -> (s.equals(subset) || !s.containsAll(subset) || !isConsistent(s)));
	}

	private static HashMap<Set<OWLAxiom>, Set<Set<OWLAxiom>>> uglyMaximalConsistentSubsetsCache = new HashMap<>();

	public static Set<Set<OWLAxiom>> maximalConsistentSubsetsNaive(Set<OWLAxiom> axioms) {

		Set<Set<OWLAxiom>> results = uglyMaximalConsistentSubsetsCache.get(axioms);
		if (results != null) {
			return results;
		}
		results = new HashSet<>();

		for (Set<OWLAxiom> candidate : powerSet(axioms)) {
			if (isMaximallyConsistentSubset(candidate, axioms)) {
				results.add(candidate);
			}
		}
		uglyMaximalConsistentSubsetsCache.put(axioms, results);
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

		Set<Set<OWLAxiom>> results = uglyMaximalConsistentSubsetsCache.get(axioms);
		if (results != null) {
			return results;
		}
		results = new HashSet<>();

		LinkedList<McsStruct> Q = new LinkedList<>();
		Q.add(new McsStruct(axioms, 0, false));

		while (!Q.isEmpty()) {
			McsStruct current = Q.poll(); // retrieve and dequeue
			if (isConsistent(current.axioms)) {
				if (results.stream().allMatch(mcs -> !mcs.containsAll(current.axioms))) {
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
				if (current.leftmost || isConsistent(L)) {
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

		uglyMaximalConsistentSubsetsCache.put(axioms, results);
		return results;
	}

}
