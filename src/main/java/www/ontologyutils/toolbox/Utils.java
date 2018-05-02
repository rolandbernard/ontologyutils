package www.ontologyutils.toolbox;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
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
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.model.parameters.OntologyCopy;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import openllet.owlapi.OpenlletReasonerFactory;
import uk.ac.manchester.cs.jfact.JFactFactory;

/**
 * @author nico
 *
 */
public class Utils {

	private static long IRI_ID = 0;
	
	// Prevent instantiation
	private Utils() {
	}

	public static void log(String tag, String message) {
		System.out.println(tag + " : " + message);
	}

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
	 *            Prints the TBox of {@code ontology} on the standard output.
	 */
	public static void printTBox(OWLOntology ontology) {
		Stream<OWLAxiom> tBoxAxioms = ontology.tboxAxioms(Imports.EXCLUDED);
		tBoxAxioms.forEach((ax) -> System.out.println(pretty(ax.toString())));
	}

	private static final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

	public static OWLOntology newEmptyOntology() {
		OWLOntology newOntology = null;
		try {
			newOntology = manager.createOntology(IRI.create("http://ontologyutils/" + (IRI_ID++)));
		} catch (OWLOntologyAlreadyExistsException e) {
			IRI_ID++;
			return newEmptyOntology();
		}
		catch (OWLOntologyCreationException e) {
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

	public static OWLOntology newOntology(Set<OWLAxiom> axioms) {
		OWLOntology ontology = newEmptyOntology();
		ontology.addAxioms(axioms);
		return ontology;
	}

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

	public static OWLOntology newOntologyExcludeNonLogicalAxioms(String ontologyFilePath) {
		OWLOntology ontology = newOntology(ontologyFilePath);
		// exclude non-logical axioms: keeping ABox, TBox, RBox axioms
		return newOntology(ontology.axioms().filter(ax -> ax.isLogicalAxiom()));
	}
	
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

	private static HashMap<Set<OWLAxiom>, Boolean> uglyAxiomSetConsistencyCache = new HashMap<>();

	public static void flushConsistencyCache() {
		uglyAxiomSetConsistencyCache = new HashMap<Set<OWLAxiom>, Boolean>();
	}

	public enum ReasonerName {
		FACT, HERMIT, OPENLLET
	}

	public static boolean isConsistent(OWLOntology ontology) {
		return isConsistent(ontology, ReasonerName.FACT);
	}

	public static boolean isConsistent(OWLOntology ontology, ReasonerName reasonerName) {
		Set<OWLAxiom> axioms = ontology.axioms().collect(Collectors.toSet());
		Boolean consistency = uglyAxiomSetConsistencyCache.get(axioms);
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
			reasoner = getOpenlletReasoner(ontology);
		}
		consistency = reasoner.isConsistent();
		reasoner.dispose();

		uglyAxiomSetConsistencyCache.put(Collections.unmodifiableSet(axioms), consistency);
		return consistency;
	}

	public static boolean isConsistent(Set<OWLAxiom> axioms) {
		Boolean consistency = uglyAxiomSetConsistencyCache.get(axioms);
		if (consistency != null) {
			return consistency;
		}
		return isConsistent(newOntology(axioms));
	}

	/**
	 * @param ontology
	 * @return the set of {@code OWLClassExpression} in the {@code ontology} TBox
	 */
	public static Set<OWLClassExpression> getSubOfTBox(OWLOntology ontology) {
		return getSubConceptsOfAxioms(ontology.tboxAxioms(Imports.EXCLUDED));
	}

	public static Set<OWLClassExpression> getSubConceptsOfAxioms(Stream<OWLAxiom> axioms) {

		Set<OWLClassExpression> subConcepts = new HashSet<>();

		axioms.forEach((ax) -> {
			ax.nestedClassExpressions().forEach((nce) -> {
				if (!subConcepts.contains(nce)) {
					subConcepts.add(nce);
				}
			});
		});

		return subConcepts;
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

}
