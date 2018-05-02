package www.ontologyutils.refinement;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;
import www.ontologyutils.toolbox.Utils;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

public class Covers {
	
	private OWLReasoner reasoner;
	private OWLOntology ontology;
	private ArrayList<OWLClassExpression> subConcepts;

	private final static OWLClassExpression TOP = OWLManager.getOWLDataFactory().getOWLThing();
	private final static OWLClassExpression BOTTOM = OWLManager.getOWLDataFactory().getOWLNothing();;
	private final static ArrayList<OWLAnnotation> EMPTY_ANNOTATION = new ArrayList<OWLAnnotation>();

	/**
	 * @param reasoner
	 */
	private Covers(OWLReasoner reasoner) {
		this.ontology = reasoner.getRootOntology();
		this.reasoner = reasoner;

		// get all subConcepts in the TBox
		subConcepts = new ArrayList<OWLClassExpression>();
		subConcepts.addAll(Utils.getSubOfTBox(ontology));
		if (!subConcepts.contains(TOP)) {
			subConcepts.add(TOP);
		}
		if (!subConcepts.contains(BOTTOM)) {
			subConcepts.add(BOTTOM);
		}
	}

	/**
	 * @param ontology
	 */
	public Covers(OWLOntology ontology) {
		this(Utils.getFactReasoner(Utils.copyOntology(ontology)));
		//this(Utils.getHermitReasoner(Utils.copyOntology(ontology)));
		//this(Utils.getOpenlletReasoner(Utils.copyOntology(ontology)));
	}
	
	/**
	 * @param concept
	 */
	protected Set<OWLClassExpression> getUpCover(OWLClassExpression concept) {
		return subConcepts.stream().parallel().filter(c -> inUpCover(concept, c)).collect(Collectors.toSet());
	}

	/**
	 * @param concept
	 */
	protected Set<OWLClassExpression> getDownCover(OWLClassExpression concept) {
		return subConcepts.stream().parallel().filter(c -> inDownCover(concept, c)).collect(Collectors.toSet());
	}

	/**
	 * @param concept
	 * @param testConcept
	 * @return
	 */
	private boolean inUpCover(OWLClassExpression concept, OWLClassExpression testConcept) {
		// UpCover only contains elements of subConcepts (TBox subconcepts)
		if (!subConcepts.contains(testConcept)) {
			return false;
		}
		// UpCover of concept can only contain super classes of concept
		if (!superclass(testConcept, concept)) {
			return false;
		}

		return !subConcepts.stream().parallel().anyMatch(other -> (subclass(concept, other)
				&& !superclass(concept, other) && subclass(other, testConcept) && !superclass(other, testConcept)));
	}

	/**
	 * @param concept
	 * @param testConcept
	 * @return
	 */
	private boolean inDownCover(OWLClassExpression concept, OWLClassExpression testConcept) {
		// DownCover only contains elements of subConcepts (TBox subconcepts)
		if (!subConcepts.contains(testConcept)) {
			return false;
		}
		// DownCover of concept can only contain subclasses of concept
		if (!subclass(testConcept, concept)) {
			return false;
		}

		return !subConcepts.stream().parallel().anyMatch(other -> (superclass(concept, other)
				&& !subclass(concept, other) && superclass(other, testConcept) && !subclass(other, testConcept)));
	}

	/**
	 * @param concept1
	 * @param concept2
	 * @return true when concept1 is a subclass of concept2
	 */
	private boolean subclass(OWLClassExpression concept1, OWLClassExpression concept2) {
		OWLAxiom ax = new OWLSubClassOfAxiomImpl(concept1, concept2, EMPTY_ANNOTATION);
		return reasoner.isEntailed(ax);
	}

	/**
	 * @param concept1
	 * @param concept2
	 * @return true when concept1 is a superclass of concept2
	 */
	private boolean superclass(OWLClassExpression concept1, OWLClassExpression concept2) {
		OWLAxiom ax = new OWLSubClassOfAxiomImpl(concept2, concept1, EMPTY_ANNOTATION);
		return reasoner.isEntailed(ax);
	}
	
	
	
	enum Direction {
		UP,
		DOWN
	}
	
	class Cover {
		Direction dir;
		Cover(Direction dir) {
			this.dir = dir;
		}
		Set<OWLClassExpression> getCover(OWLClassExpression concept) {
			switch(dir) {
			case UP: 
				return getUpCover(concept);
			case DOWN:
				return getDownCover(concept);
			default:
				throw new RuntimeException();
			}
		}
	}
	
	public Cover getUpCoverOperator() {
		return new Cover(Direction.UP);
	}
	
	public Cover getDownCoverOperator() {
		return new Cover(Direction.DOWN);
	}

}
