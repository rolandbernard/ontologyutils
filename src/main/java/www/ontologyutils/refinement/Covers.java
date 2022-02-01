package www.ontologyutils.refinement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;
import www.ontologyutils.toolbox.Utils;

public class Covers {

	private static Boolean CACHE = true;

	private static int UPCOVER_CACHE_SIZE = 1024;
	private static int DOWNCOVER_CACHE_SIZE = 1024;

	/**
	 * A naive cache for upcovers and downcovers of class expressions.
	 *
	 */
	private class Cache {

		private HashMap<OWLClassExpression, Set<OWLClassExpression>> upCoverCache = new HashMap<>();
		private HashMap<OWLClassExpression, Set<OWLClassExpression>> downCoverCache = new HashMap<>();

		Set<OWLClassExpression> upCoverGet(OWLClassExpression e) {
			return upCoverCache.get(e);
		}

		Set<OWLClassExpression> downCoverGet(OWLClassExpression e) {
			return downCoverCache.get(e);
		}

		void upCoverAdd(OWLClassExpression e, Set<OWLClassExpression> upCover) {
			if (upCoverCache.size() >= UPCOVER_CACHE_SIZE) {
				upCoverCache.clear();
			}
			upCoverCache.put(e, upCover);
		}

		void basicDownCoverAdd(OWLClassExpression e, Set<OWLClassExpression> downCover) {
			if (downCoverCache.size() >= DOWNCOVER_CACHE_SIZE) {
				downCoverCache.clear();
			}
			downCoverCache.put(e, downCover);
		}
	}

	Cache cache;

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
		cache = new Cache();
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
		this(Utils.getReasoner(Utils.copyOntology(ontology)));
	}

	/**
	 * @param concept
	 */
	protected Set<OWLClassExpression> getUpCover(OWLClassExpression concept) {
		if (!CACHE) {
			return subConcepts.stream().parallel().filter(c -> inUpCover(concept, c)).collect(Collectors.toSet());
		}
		Set<OWLClassExpression> upCover = cache.upCoverGet(concept);
		if (upCover != null) {
			return upCover;
		}
		upCover = subConcepts.stream().parallel().filter(c -> inUpCover(concept, c)).collect(Collectors.toSet());
		cache.upCoverAdd(concept, upCover);
		return upCover;
	}

	/**
	 * @param concept
	 */
	protected Set<OWLClassExpression> getDownCover(OWLClassExpression concept) {
		if (!CACHE) {
			return subConcepts.stream().parallel().filter(c -> inDownCover(concept, c)).collect(Collectors.toSet());
		}
		Set<OWLClassExpression> downCover = cache.downCoverGet(concept);
		if (downCover != null) {
			return downCover;
		}
		downCover = subConcepts.stream().parallel().filter(c -> inDownCover(concept, c)).collect(Collectors.toSet());
		cache.basicDownCoverAdd(concept, downCover);
		return downCover;
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
		UP, DOWN
	}

	class Cover {
		Direction dir;

		Cover(Direction dir) {
			this.dir = dir;
		}

		Set<OWLClassExpression> getCover(OWLClassExpression concept) {
			switch (dir) {
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
