package www.ontologyutils.toolbox;

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
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import uk.ac.manchester.cs.owl.owlapi.OWLClassAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectAllValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectSomeValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectUnionOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;
import www.ontologyutils.normalization.NormalizationTools;

public class TesterUtils extends TestCase {

	private static final Collection<OWLAnnotation> EMPTY_ANNOTATION = new ArrayList<OWLAnnotation>();
	private static final OWLClassExpression TOP = new OWLDataFactoryImpl().getOWLThing();
	private static final OWLClassExpression BOT = new OWLDataFactoryImpl().getOWLNothing();

	static OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();
	static OWLClassExpression entity1 = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create("www.first.org"));
	static OWLClassExpression entity2 = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create("www.second.org"));
	static OWLClassExpression entity3 = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create("www.third.org"));
	static OWLClassExpression entity4 = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create("www.fourth.org"));
	static OWLClassExpression entity1bis = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create("www.first.org"));
	static OWLEntity role = dataFactory.getOWLObjectProperty(IRI.create("www.role.org"));
	static OWLEntity rolebis = dataFactory.getOWLObjectProperty(IRI.create("www.role.org"));
	
	static OWLIndividual indy1 = dataFactory.getOWLNamedIndividual(IRI.create("www.indy-one.org"));
	static OWLIndividual indy2 = dataFactory.getOWLNamedIndividual(IRI.create("www.indy-two.org"));

	static OWLAxiom ax1 = new OWLSubClassOfAxiomImpl(entity1, entity2, EMPTY_ANNOTATION);
	static OWLAxiom ax2 = new OWLSubClassOfAxiomImpl(entity2, entity3, EMPTY_ANNOTATION);
	static OWLAxiom ax3 = new OWLSubClassOfAxiomImpl(entity3, entity4, EMPTY_ANNOTATION);
	static OWLAxiom ax4 = new OWLSubClassOfAxiomImpl(entity4, entity1, EMPTY_ANNOTATION);
	static OWLAxiom ax5 = new OWLSubClassOfAxiomImpl(TOP, entity1, EMPTY_ANNOTATION);
	static OWLAxiom ax6 = new OWLSubClassOfAxiomImpl(entity4, BOT, EMPTY_ANNOTATION);
	static OWLAxiom ax7 = new OWLClassAssertionAxiomImpl(indy1, entity1, EMPTY_ANNOTATION);

	static Set<OWLAxiom> agenda;

	File[] FILES = { new File("./resources/catsandnumbers.owl"), new File("./resources/bodysystem.owl"),
			new File("./resources/bfo.owl"), new File("./resources/apo.owl"), new File("./resources/aeo.owl"),
			new File("./resources/duo.owl") };
	
	public TesterUtils(String testName) {
		super(testName);

		agenda = new HashSet<OWLAxiom>();
		agenda.add(ax1);
		agenda.add(ax2);
		agenda.add(ax3);
		agenda.add(ax4);
		agenda.add(ax5);
		agenda.add(ax6);
		agenda.add(ax7);

		System.out.println("AGENDA " + agenda);
	}

	public static Test suite() {
		return new TestSuite(TesterUtils.class);
	}

	public void testMaximalConsistentSetsNaive() {
		System.out.println("%%% TEST MCSs");

		Set<Set<OWLAxiom>> results = MaximalConsistentSets.maximalConsistentSubsetsNaive(agenda);

		System.out.println("Found " + results.size() + " MCSs in " + agenda);
		results.stream().forEach(System.out::println);

		assertTrue(
				results.stream().allMatch(subset -> MaximalConsistentSets.isMaximallyConsistentSubset(subset, agenda)));
		assertTrue(SetUtils.powerSet(agenda).stream()
				.allMatch(subset -> (!MaximalConsistentSets.isMaximallyConsistentSubset(subset, agenda)
						|| results.contains(subset))));
		assertTrue(results.stream().allMatch(mcs -> mcs.stream().allMatch(ax -> agenda.contains(ax))));
	}

	public void testMaximalConsistentSets() {
		System.out.println("%%% TEST MCSs");

		Set<Set<OWLAxiom>> results = MaximalConsistentSets.maximalConsistentSubsets(agenda);

		System.out.println("Found " + results.size() + " MCSs in " + agenda);
		results.stream().forEach(System.out::println);

		assertTrue(
				results.stream().allMatch(subset -> MaximalConsistentSets.isMaximallyConsistentSubset(subset, agenda)));
		assertTrue(SetUtils.powerSet(agenda).stream()
				.allMatch(subset -> (!MaximalConsistentSets.isMaximallyConsistentSubset(subset, agenda)
						|| results.contains(subset))));
		assertTrue(results.stream().allMatch(mcs -> mcs.stream().allMatch(ax -> agenda.contains(ax))));
	}
	
	/**
	 * 
	 */
	public void testConceptEquality() {
		System.out.println("%%% TEST Concept Equality");
		
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
		System.out.println("%%% TEST Fresh Atoms Equality");
		
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
		System.out.println("%%% TEST AsSubClass");
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
}
