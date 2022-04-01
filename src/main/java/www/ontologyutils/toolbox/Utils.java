package www.ontologyutils.toolbox;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyAlreadyExistsException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import openllet.owlapi.OpenlletReasonerFactory;
import uk.ac.manchester.cs.jfact.JFactFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLEquivalentClassesAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;

/**
 * @author nico
 *
 */
public class Utils {

	private static Boolean CACHE = true;

	private static int AXIOM_SET_CONSISTENCY_CACHE_SIZE = 1024;

	private static long IRI_ID = 0;

	/**
	 * A naive FIFO cache for consistency checks of sets of axioms.
	 *
	 */
	private static class Cache {

		private static HashMap<Set<OWLAxiom>, Boolean> axiomSetConsistencyCache = new HashMap<>();

		private static LinkedList<Set<OWLAxiom>> contentAxiomSetConsistencyCache = new LinkedList<>();

		private static Boolean axiomSetConsistencyCheck(Set<OWLAxiom> axioms) {
			if (CACHE) {
				return axiomSetConsistencyCache.get(axioms);
			} else {
				return null;
			}
		}

		private static void axiomsSetConsistencyAdd(Set<OWLAxiom> axioms, Boolean consistency) {
			if (CACHE) {
				if (axiomSetConsistencyCache.size() >= AXIOM_SET_CONSISTENCY_CACHE_SIZE) {
					Set<OWLAxiom> s = contentAxiomSetConsistencyCache.removeFirst();
					contentAxiomSetConsistencyCache.remove(s);
					axiomSetConsistencyCache.clear();
				}
				Set<OWLAxiom> s = Collections.unmodifiableSet(axioms);
				axiomSetConsistencyCache.put(s, consistency);
				contentAxiomSetConsistencyCache.addLast(s);
			}
		}
	}

	public static void log(String tag, String message) {
		System.out.println(tag + " : " + message);
	}

	// Prevent instantiation
	private Utils() {
	}

	public enum ReasonerName {
		FACT, HERMIT, OPENLLET
	}

	public static ReasonerName DEFAULT_REASONER = ReasonerName.OPENLLET;

	public static final Collection<OWLAnnotation> EMPTY_ANNOTATION = new ArrayList<OWLAnnotation>();

	/**
	 * @param owlString
	 * @return
	 */
	public static String pretty(String owlString) {
		return owlString.replaceAll("<http.*?#", "").replaceAll(">", "").replaceAll("<", "");
	}

	/**
	 * @param ax
	 * @return a pretty string representing {@code ax}, without its annotations and
	 *         without namespaces.
	 */
	public static String prettyPrintAxiom(OWLAxiom ax) {
		return ax.getAxiomWithoutAnnotations().toString().replaceAll("<http.*?#", "").replaceAll(">", "")
				.replaceAll("<", "").replaceFirst("Annotation(.*?) ", "");
	}

	/**
	 * @param ontology
	 * 
	 *                 Prints the TBox of {@code ontology} on the standard output.
	 */
	public static void printTBox(OWLOntology ontology) {
		Stream<OWLAxiom> tBoxAxioms = ontology.tboxAxioms(Imports.EXCLUDED);
		tBoxAxioms.forEach((ax) -> System.out.println(pretty(ax.toString())));
	}

	private static final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

	/**
	 * @return
	 */
	public static OWLOntology newEmptyOntology() {
		OWLOntology newOntology = null;
		try {
			newOntology = manager.createOntology(IRI.create("http://ontologyutils/" + (IRI_ID++)));
		} catch (OWLOntologyAlreadyExistsException e) {
			IRI_ID++;
			return newEmptyOntology();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return newOntology;
	}

	/**
	 * @param axioms
	 * @return
	 */
	public static OWLOntology newOntology(Stream<OWLAxiom> axioms) {
		OWLOntology ontology = newEmptyOntology();
		ontology.addAxioms(axioms);
		return ontology;
	}

	/**
	 * @param axioms
	 * @return
	 */
	public static OWLOntology newOntology(Set<OWLAxiom> axioms) {
		OWLOntology ontology = newEmptyOntology();
		ontology.addAxioms(axioms);
		return ontology;
	}

	/**
	 * @param ontologyFilePath
	 * @return
	 */
	public static OWLOntology newOntology(String ontologyFilePath) {
		File ontologyFile = new File(ontologyFilePath);
		IRI ontologyIRI = IRI.create(ontologyFile);
		OWLOntology ontology = null;
		try {
			ontology = manager.loadOntologyFromOntologyDocument(ontologyIRI);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			System.exit(1);
		}

		return ontology;
	}

	/**
	 * @param ontologyFilePath
	 * @return
	 */
	public static OWLOntology newOntologyExcludeNonLogicalAxioms(String ontologyFilePath) {
		OWLOntology ontology = newOntology(ontologyFilePath);
		// exclude non-logical axioms: keeping ABox, TBox, RBox axioms
		return newOntology(ontology.axioms().filter(ax -> ax.isLogicalAxiom()));
	}

	/**
	 * @param ontology
	 * @return
	 */
	public static OWLOntology copyOntology(OWLOntology ontology) {
		try {
			return OWLManager.createOWLOntologyManager().copyOntology(ontology, OntologyCopy.SHALLOW);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			System.exit(1);
		}
		return null;
	}

	private static final OWLReasonerFactory reasonerFactoryFact = new JFactFactory();

	/**
	 * @param ontology
	 * @return a JFact reasoner for {@code ontology}
	 */
	public static OWLReasoner getFactReasoner(OWLOntology ontology) {
		return reasonerFactoryFact.createNonBufferingReasoner(ontology);
	}

	@SuppressWarnings("deprecation")
	private static final OWLReasonerFactory reasonerFactoryHermit = new Reasoner.ReasonerFactory();

	/**
	 * @param ontology
	 * @return a Hermit reasoner for {@code ontology}
	 */
	public static OWLReasoner getHermitReasoner(OWLOntology ontology) {
		return reasonerFactoryHermit.createNonBufferingReasoner(ontology);
	}

	private static final OWLReasonerFactory reasonerFactoryOpenllet = new OpenlletReasonerFactory();

	/**
	 * @param ontology
	 * @return a Openllet reasoner for {@code ontology}
	 */
	public static OWLReasoner getOpenlletReasoner(OWLOntology ontology) {
		return reasonerFactoryOpenllet.createNonBufferingReasoner(ontology);
	}

	/**
	 * @param ontology
	 * @return A default reasoner for {@code ontology}. Returns null if the default
	 *         reasoner is not recognised.Ò
	 */
	public static OWLReasoner getReasoner(OWLOntology ontology) {
		switch (DEFAULT_REASONER) {
		case HERMIT:
			return getHermitReasoner(ontology);
		case FACT:
			return getFactReasoner(ontology);
		case OPENLLET:
			return getOpenlletReasoner(ontology);
		default:
			return null;
		}
	}

	/**
	 * @param ontology
	 * @return
	 */
	public static boolean isConsistent(OWLOntology ontology) {
		return isConsistent(ontology, DEFAULT_REASONER);
	}

	/**
	 * @param ontology
	 * @param reasonerName
	 * @return
	 */
	public static boolean isConsistent(OWLOntology ontology, ReasonerName reasonerName) {
		Set<OWLAxiom> axioms = ontology.axioms().collect(Collectors.toSet());
		Boolean consistency = Cache.axiomSetConsistencyCheck(axioms);
		if (consistency != null) {
			return consistency;
		}
		OWLReasoner reasoner;
		switch (reasonerName) {
		case HERMIT:
			reasoner = getHermitReasoner(ontology);
			break;
		case FACT:
			reasoner = getFactReasoner(ontology);
			break;
		case OPENLLET:
			reasoner = getOpenlletReasoner(ontology);
			break;
		default:
			reasoner = getReasoner(ontology);
		}
		consistency = reasoner.isConsistent();
		reasoner.dispose();

		Cache.axiomsSetConsistencyAdd(Collections.unmodifiableSet(axioms), consistency);
		return consistency;
	}

	/**
	 * @param axioms
	 * @return
	 */
	public static boolean isConsistent(Set<OWLAxiom> axioms) {
		Boolean consistency = Cache.axiomSetConsistencyCache.get(axioms);
		if (consistency != null) {
			return consistency;
		}
		return isConsistent(newOntology(axioms));
	}

	/**
	 * @param ontology
	 * @param axiom
	 * @param reasonerName
	 * @return
	 */
	public static boolean isEntailed(OWLOntology ontology, OWLAxiom axiom, ReasonerName reasonerName) {
		OWLReasoner reasoner;
		switch (reasonerName) {
		case HERMIT:
			reasoner = getHermitReasoner(ontology);
			break;
		case FACT:
			reasoner = getFactReasoner(ontology);
			break;
		case OPENLLET:
			reasoner = getOpenlletReasoner(ontology);
			break;
		default:
			reasoner = getReasoner(ontology);
		}
		boolean entailment = reasoner.isEntailed(axiom);
		reasoner.dispose();

		return entailment;
	}

	/**
	 * @param ontology
	 * @param axiom
	 * @return
	 */
	public static boolean isEntailed(OWLOntology ontology, OWLAxiom axiom) {
		return isEntailed(ontology, axiom, DEFAULT_REASONER);
	}

	/**
	 * @param ontology
	 * @return the set of {@code OWLClassExpression} in the {@code ontology} TBox
	 */
	public static Set<OWLClassExpression> getSubOfTBox(OWLOntology ontology) {
		Set<OWLClassExpression> subConcepts = new HashSet<>();
		ontology.tboxAxioms(Imports.EXCLUDED)
				.forEach((ax) -> subConcepts.addAll(ax.nestedClassExpressions().collect(Collectors.toSet())));

		return subConcepts;
	}

	/**
	 * @param ontology
	 * @return the set of {@code OWLClassExpression} in {@code ontology}
	 */
	public static Set<OWLClassExpression> getSubClasses(OWLOntology ontology) {
		Set<OWLClassExpression> subConcepts = new HashSet<>();
		ontology.axioms(Imports.EXCLUDED)
				.forEach((ax) -> subConcepts.addAll(ax.nestedClassExpressions().collect(Collectors.toSet())));

		return subConcepts;
	}

	/**
	 * @param c1
	 * @param c2
	 * @param ontology
	 * @return true exactly when {@code c1} and {@code c2} are provably equivalent
	 *         in {@code ontology}.
	 */
	public static boolean areEquivalent(OWLClassExpression c1, OWLClassExpression c2, OWLOntology ontology) {
		OWLAxiom equiv = new OWLEquivalentClassesAxiomImpl(Arrays.asList(c1, c2), new HashSet<>());
		return isEntailed(ontology, equiv);
	}

	/**
	 * @param ontology
	 * @return the set of C1 subclass C2 axioms, C1 and C2 classes in the signature
	 *         of {@code ontology}, entailed by {@code ontology}.
	 */
	public static Set<OWLAxiom> inferredTaxonomyAxioms(OWLOntology ontology) {
		final Collection<OWLAnnotation> EMPTY_ANNOTATION = new ArrayList<OWLAnnotation>();
		OWLReasoner reasoner = getReasoner(ontology);
		boolean isConsistent = reasoner.isConsistent();
		Set<OWLAxiom> result = new HashSet<>();
		ontology.classesInSignature(Imports.EXCLUDED).forEach((left) -> {
			ontology.classesInSignature(Imports.EXCLUDED).forEach((right) -> {
				OWLSubClassOfAxiom scoa = new OWLSubClassOfAxiomImpl(left, right, EMPTY_ANNOTATION);
				if (!isConsistent) {
					result.add(scoa);
				} else if (reasoner.isEntailed(scoa)) {
					result.add(scoa);
				}
			});
		});

		reasoner.dispose();
		return result;
	}

	/**
	 * @param c1
	 * @param c2
	 * @return true when {@code c1} and {@code c2} are the same concept at the
	 *         syntactic level. E.g., C1 = exists p. (A or B) is the same as C2 =
	 *         exists p. (A or B), even if the representing objects are different,
	 *         that is they are the same even if C1 != C2 or !C1.equals(C2). On the
	 *         other hand, we say that A and B is the same as B and A.
	 */
	public static boolean sameConcept(OWLClassExpression c1, OWLClassExpression c2) {
		if (c1 == c2) {
			return true;
		}
		if (c1.getClassExpressionType() != c2.getClassExpressionType()) {
			return false;
		}
		switch (c1.getClassExpressionType()) {
		case OWL_CLASS: {
			return c1.equals(c2);
		}
		case OBJECT_COMPLEMENT_OF: {
			OWLClassExpression op1 = ((OWLObjectComplementOf) c1).getOperand();
			OWLClassExpression op2 = ((OWLObjectComplementOf) c2).getOperand();
			return sameConcept(op1, op2);
		}
		case OBJECT_UNION_OF: {
			Set<OWLClassExpression> disjuncts1 = c1.asDisjunctSet();
			Set<OWLClassExpression> disjuncts2 = c2.asDisjunctSet();
			if (disjuncts1.size() != disjuncts2.size()) {
				return false;
			}
			for (OWLClassExpression e : disjuncts1) {
				if (disjuncts2.stream().allMatch(c -> !sameConcept(c, e))) {
					return false;
				}
			}
			return true;
		}
		case OBJECT_INTERSECTION_OF: {
			Set<OWLClassExpression> conjuncts1 = c1.asConjunctSet();
			Set<OWLClassExpression> conjuncts2 = c2.asConjunctSet();
			if (conjuncts1.size() != conjuncts2.size()) {
				return false;
			}
			for (OWLClassExpression e : conjuncts1) {
				if (conjuncts2.stream().allMatch(c -> !sameConcept(c, e))) {
					return false;
				}
			}
			return true;
		}
		case OBJECT_SOME_VALUES_FROM: {
			OWLClassExpression op1 = ((OWLObjectSomeValuesFrom) c1).getFiller();
			OWLObjectPropertyExpression prop1 = ((OWLObjectSomeValuesFrom) c1).getProperty();
			OWLClassExpression op2 = ((OWLObjectSomeValuesFrom) c2).getFiller();
			OWLObjectPropertyExpression prop2 = ((OWLObjectSomeValuesFrom) c2).getProperty();
			return prop1.equals(prop2) && sameConcept(op1, op2);
		}
		case OBJECT_ALL_VALUES_FROM: {
			OWLClassExpression op1 = ((OWLObjectAllValuesFrom) c1).getFiller();
			OWLObjectPropertyExpression prop1 = ((OWLObjectAllValuesFrom) c1).getProperty();
			OWLClassExpression op2 = ((OWLObjectAllValuesFrom) c2).getFiller();
			OWLObjectPropertyExpression prop2 = ((OWLObjectAllValuesFrom) c2).getProperty();
			return prop1.equals(prop2) && sameConcept(op1, op2);
		}
		default:
			throw new RuntimeException();
		}

	}

	/**
	 * @param ontology
	 * @param fileName
	 */
	public static void saveOntology(OWLOntology ontology, String fileName) {
		File file = new File(fileName);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		try {
			manager.saveOntology(ontology, ontology.getFormat(), IRI.create(file.toURI()));
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		}

	}

}
