package www.ontologyutils.refinement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import uk.ac.manchester.cs.owl.owlapi.OWLClassAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;

public class AxiomWeakener {

	private final static ArrayList<OWLAnnotation> EMPTY_ANNOTATION = new ArrayList<OWLAnnotation>();

	private OWLOntology ontology;
	private RefinementOperator genOp;
	private RefinementOperator specOp;

	/**
	 * @param ontology
	 *            a reference ontology to make inferences.
	 */
	public AxiomWeakener(OWLOntology ontology) {
		this.ontology = ontology;
		Covers covers = new Covers(ontology);
		this.genOp = new RefinementOperator( 
				covers.getUpCoverOperator(), 
				covers.getDownCoverOperator());
		this.specOp = new RefinementOperator(
				covers.getDownCoverOperator(),
				covers.getUpCoverOperator());
	}

	/**
	 * @param axiom
	 * @return all the weakenings of axiom obtained by either specialising the
	 *         left-hand side (using the {@code GeneralisationOperator}
	 *         {@code genOp} of this {@code JavaWeakener} object) or by generalising
	 *         the right-hand side (using the {@code SpecialisationOperator}
	 *         {@code specOp} of this {@code JavaWeakener} object). Given an axiom A
	 *         -> B, the function returns the set containing all the axioms A' -> B
	 *         where A' is in {@code specOp.specialise(A)} and all the axioms A ->
	 *         B' where B' is in {@code genOp.generalise(A)}.
	 */
	public Set<OWLAxiom> getWeakerSubClassAxioms(OWLSubClassOfAxiom axiom) {
		HashSet<OWLAxiom> result = new HashSet<OWLAxiom>();

		OWLClassExpression left = axiom.getSubClass();
		OWLClassExpression right = axiom.getSuperClass();
		Set<OWLClassExpression> LeftSpecial = specOp.refine(left);
		Set<OWLClassExpression> RightGeneral = genOp.refine(right);

		for (OWLClassExpression oce : LeftSpecial) {
			OWLAxiom ax = new OWLSubClassOfAxiomImpl(oce, right, EMPTY_ANNOTATION);
			result.add(ax);
		}
		for (OWLClassExpression oce : RightGeneral) {
			OWLAxiom ax = new OWLSubClassOfAxiomImpl(left, oce, EMPTY_ANNOTATION);
			result.add(ax);
		}

		return result;
	}

	/**
	 * @param axiom
	 * @return all weakenings of axiom obtained by generalising its class expression
	 *         (using the {@code SpecialisationOperator} {@code specOp} of this
	 *         {@code JavaWeakener} object). Given an axiom A(i), the function
	 *         returns the set containing all the axioms A'(i) where A' is in
	 *         {@code genOp.generalise(A)}.
	 */
	public Set<OWLAxiom> getWeakerClassAssertionAxioms(OWLClassAssertionAxiom axiom) {
		HashSet<OWLAxiom> result = new HashSet<OWLAxiom>();

		OWLClassExpression expression = axiom.getClassExpression();

		Set<OWLClassExpression> generalisations = genOp.refine(expression);

		for (OWLClassExpression oce : generalisations) {
			OWLAxiom ax = new OWLClassAssertionAxiomImpl(axiom.getIndividual(), oce, EMPTY_ANNOTATION);
			result.add(ax);
		}

		return result;
	}

}
