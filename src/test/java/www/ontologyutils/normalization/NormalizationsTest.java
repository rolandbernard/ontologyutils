package www.ontologyutils.normalization;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.manchester.cs.owl.owlapi.OWLDisjointUnionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectAllValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectSomeValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectUnionOfImpl;
import www.ontologyutils.ontologyutils.FreshAtoms;
import www.ontologyutils.ontologyutils.Utils;

/**
 * Unit tests for Tools.
 */
public class NormalizationsTest extends TestCase {

	private static final Collection<OWLAnnotation> EMPTY_ANNOTATION = new ArrayList<OWLAnnotation>();

	OWLEntity entity1;
	OWLEntity entity2;
	OWLEntity entity3;
	OWLEntity entity4;
	OWLEntity entity1bis;
	OWLEntity role;
	OWLEntity rolebis;

	File[] FILES = { new File("./resources/catsandnumbers.owl"), new File("./resources/bodysystem.owl"),
			new File("./resources/bfo.owl"), new File("./resources/apo.owl"), new File("./resources/aeo.owl"),
			new File("./resources/duo.owl") };

	/**
	 * Create the test case
	 *
	 * @param testName
	 *            name of the test case
	 */
	public NormalizationsTest(String testName) {
		super(testName);
		init();

	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(NormalizationsTest.class);
	}

	public void init() {
		OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();
		entity1 = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create("www.first.org"));
		entity2 = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create("www.second.org"));
		entity3 = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create("www.third.org"));
		entity4 = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create("www.fourth.org"));
		entity1bis = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create("www.first.org"));
		role = dataFactory.getOWLObjectProperty(IRI.create("www.role.org"));
		rolebis = dataFactory.getOWLObjectProperty(IRI.create("www.role.org"));
	}

	/**
	 * 
	 */
	public void testConceptEquality() {
		assertTrue(entity1 == entity1bis);
		assertTrue(Utils.sameConcept((OWLClassExpression) entity1, (OWLClassExpression) entity1bis));

		Set<OWLClassExpression> operands12 = new HashSet<OWLClassExpression>();
		operands12.add((OWLClassExpression) entity1);
		operands12.add((OWLClassExpression) entity2);
		OWLObjectUnionOf union12 = new OWLObjectUnionOfImpl(operands12.stream());

		Set<OWLClassExpression> operands12bis = new HashSet<OWLClassExpression>();
		operands12bis.add((OWLClassExpression) entity1bis);
		operands12bis.add((OWLClassExpression) entity2);
		OWLObjectUnionOf union12bis = new OWLObjectUnionOfImpl(operands12bis.stream());

		assertFalse(union12 == union12bis);
		assertTrue(Utils.sameConcept(union12, union12bis));

		operands12.add((OWLClassExpression) entity3);
		operands12bis.add((OWLClassExpression) entity3);
		union12 = new OWLObjectUnionOfImpl(operands12.stream());
		union12bis = new OWLObjectUnionOfImpl(operands12bis.stream());

		assertFalse(union12 == union12bis);
		assertTrue(Utils.sameConcept(union12, union12bis));

		OWLObjectSomeValuesFromImpl exist12 = new OWLObjectSomeValuesFromImpl((OWLObjectPropertyExpression) role,
				union12);
		OWLObjectSomeValuesFromImpl exist12bis = new OWLObjectSomeValuesFromImpl((OWLObjectPropertyExpression) rolebis,
				union12bis);

		assertTrue(Utils.sameConcept(exist12, exist12bis));

		operands12.add((OWLClassExpression) exist12);
		operands12bis.add((OWLClassExpression) exist12bis);
		union12 = new OWLObjectUnionOfImpl(operands12.stream());
		union12bis = new OWLObjectUnionOfImpl(operands12bis.stream());

		assertFalse(union12 == union12bis);
		assertTrue(Utils.sameConcept(union12, union12bis));

		OWLObjectAllValuesFromImpl all12 = new OWLObjectAllValuesFromImpl((OWLObjectPropertyExpression) role,
				(OWLClassExpression) union12);
		OWLObjectAllValuesFromImpl all12bis = new OWLObjectAllValuesFromImpl((OWLObjectPropertyExpression) rolebis,
				(OWLClassExpression) union12bis);

		operands12.add((OWLClassExpression) all12);
		operands12bis.add((OWLClassExpression) all12bis);
		OWLObjectIntersectionOfImpl inter12 = new OWLObjectIntersectionOfImpl(operands12.stream());
		OWLObjectIntersectionOfImpl inter12bis = new OWLObjectIntersectionOfImpl(operands12bis.stream());

		assertFalse(inter12 == inter12bis);
		assertTrue(Utils.sameConcept(inter12, inter12bis));
	}

	/**
	 * 
	 */
	public void testFreshAtomsEquality() {
		OWLClassExpression ef1 = FreshAtoms.createFreshAtomCopy((OWLClassExpression) entity1);
		OWLClassExpression ef2 = FreshAtoms.createFreshAtomCopy((OWLClassExpression) entity2);
		OWLClassExpression ef1bis = FreshAtoms.createFreshAtomCopy((OWLClassExpression) entity1);

		assertFalse(ef1 == ef1bis);
		assertTrue(ef1.equals(ef1bis));
		assertFalse(ef1 == ef2);
		assertTrue(Utils.sameConcept(ef1, ef1bis));
	}

	/**
	 * Test whether we can normalize an ontology in such a way that all TBox axioms
	 * are replaced with subclass axioms; The new ontology entails all axioms of the
	 * original ontology, and vice versa.
	 * 
	 * @throws OWLOntologyCreationException
	 */
	public void testAsSubClass() throws OWLOntologyCreationException {
		for (File ontologyFile : FILES) {

			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(IRI.create(ontologyFile));

			System.out.println("Testing subclass normalization on " + ontology);

			Stream<OWLAxiom> tBox = ontology.tboxAxioms(Imports.EXCLUDED);
			Object[] tBoxArray = tBox.toArray();

			for (int i = 0; i < tBoxArray.length; i++) {
				OWLAxiom ax = (OWLAxiom) tBoxArray[i];
				ontology.remove(ax);
				ontology.add(NormalizationTools.asSubClassOfAxioms(ax));
			}
			// all subclass axioms
			ontology.tboxAxioms(Imports.EXCLUDED).forEach((ax) -> assertTrue(ax.isOfType(AxiomType.SUBCLASS_OF)));

			OWLOntologyManager managerOrigin = OWLManager.createOWLOntologyManager();
			OWLOntology ontologyOrigin = managerOrigin.loadOntologyFromOntologyDocument(IRI.create(ontologyFile));
			OWLReasoner originReasoner = Utils.getFactReasoner(ontologyOrigin);
			// the original ontology entails all the axioms of the new ontology.
			ontology.axioms().forEach((ax) -> assertTrue(originReasoner.isEntailed(ax)));
			OWLReasoner reasoner = Utils.getFactReasoner(ontology);
			// the new ontology entails all the axioms of the original ontology.
			ontologyOrigin.axioms().forEach((ax) -> assertTrue(reasoner.isEntailed(ax)));
		}
	}

	public void testNormalizeCondor() throws OWLOntologyCreationException {
		for (File ontologyFile : FILES) {
			FreshAtoms.resetFreshAtomsEquivalenceAxioms();
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(IRI.create(ontologyFile));

			OWLOntology copy = Utils.newEmptyOntology();
			copy.addAxioms(ontology.axioms());

			Stream<OWLAxiom> tBoxAxioms = copy.tboxAxioms(Imports.EXCLUDED);
			tBoxAxioms.forEach((ax) -> {
				copy.remove(ax);
				copy.addAxioms(NormalizationTools.asSubClassOfAxioms(ax));
			});

			// System.out.println("\nCondor Normalized TBox");
			OWLOntology condor = null;
			condor = Normalization.normalizeCondor(copy);
			System.out.println("Testing condor normalization on " + ontology);

			// condor.tboxAxioms(Imports.EXCLUDED).forEach(ax ->
			// System.out.println(Utils.pretty("-- " + ax.toString())));

			// check every axiom of the original ontology is entailed in condor
			OWLReasoner reasoner = Utils.getHermitReasoner(condor);
			assertTrue(ontology.axioms().allMatch(ax -> reasoner.isEntailed(ax)));
			// check every axiom of condor is entailed in the copy of the original ontology
			// with extended signature
			copy.addAxioms(FreshAtoms.getFreshAtomsEquivalenceAxioms());
			OWLReasoner reasonerBis = Utils.getHermitReasoner(copy);
			assertTrue(condor.axioms().allMatch(ax -> reasonerBis.isEntailed(ax)));
		}
	}

	public void testNormalizeNaive() throws OWLOntologyCreationException {
		for (File ontologyFile : FILES) {
			FreshAtoms.resetFreshAtomsEquivalenceAxioms();
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = manager.loadOntologyFromOntologyDocument(IRI.create(ontologyFile));

			OWLOntology copy = Utils.newEmptyOntology();
			copy.addAxioms(ontology.axioms());

			Stream<OWLAxiom> tBoxAxioms = copy.tboxAxioms(Imports.EXCLUDED);
			tBoxAxioms.forEach((ax) -> {
				copy.remove(ax);
				copy.addAxioms(NormalizationTools.asSubClassOfAxioms(ax));
			});

			// System.out.println("\nNaive Normalized TBox");
			OWLOntology naive = null;
			naive = Normalization.normalizeNaive(copy);
			System.out.println("Testing naive normalization on " + ontology);

			// naive.tboxAxioms(Imports.EXCLUDED).forEach(ax ->
			// System.out.println(Utils.pretty("-- " + ax.toString())));

			// check every axiom of the original ontology is entailed in naive
			OWLReasoner reasoner = Utils.getHermitReasoner(naive);
			assertTrue(ontology.axioms().allMatch(ax -> reasoner.isEntailed(ax)));
			// check every axiom of naive is entailed in the copy of the original ontology
			// with extended signature
			copy.addAxioms(FreshAtoms.getFreshAtomsEquivalenceAxioms());
			OWLReasoner reasonerBis = Utils.getHermitReasoner(copy);
			assertTrue(naive.axioms().allMatch(ax -> reasonerBis.isEntailed(ax)));
		}
	}
}
