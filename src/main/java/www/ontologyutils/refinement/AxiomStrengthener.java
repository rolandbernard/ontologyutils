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

public class AxiomStrengthener {

	private final static ArrayList<OWLAnnotation> EMPTY_ANNOTATION = new ArrayList<OWLAnnotation>();

	private OWLOntology ontology;
	private RefinementOperator genOp;
	private RefinementOperator specOp;

	/**
	 * @param ontology
	 *            a reference ontology to make inferences.
	 */
	public AxiomStrengthener(OWLOntology ontology) {
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
	 * @return all the strengthenings of axiom obtained by either specialising the
	 *         right-hand side (using the {@code GeneralisationOperator}
	 *         {@code genOp} of this {@code JavaWeakener} object) or by generalising
	 *         the left-hand side (using the {@code SpecialisationOperator}
	 *         {@code specOp} of this {@code JavaWeakener} object). Given an axiom A
	 *         -> B, the function returns the set containing all the axioms A' -> B
	 *         where A' is in {@code specOp.generalise(A)} and all the axioms A ->
	 *         B' where B' is in {@code genOp.specialise(A)}.
	 */
	public Set<OWLAxiom> getStrongerSubClassAxioms(OWLSubClassOfAxiom axiom) {
		HashSet<OWLAxiom> result = new HashSet<OWLAxiom>();

		OWLClassExpression left = axiom.getSubClass();
		OWLClassExpression right = axiom.getSuperClass();
		Set<OWLClassExpression> LeftGeneral = genOp.refine(left);
		Set<OWLClassExpression> RightSpecial = specOp.refine(right);

		for (OWLClassExpression oce : LeftGeneral) {
			OWLAxiom ax = new OWLSubClassOfAxiomImpl(oce, right, EMPTY_ANNOTATION);
			result.add(ax);
		}
		for (OWLClassExpression oce : RightSpecial) {
			OWLAxiom ax = new OWLSubClassOfAxiomImpl(left, oce, EMPTY_ANNOTATION);
			result.add(ax);
		}

		return result;
	}

	/**
	 * @param axiom
	 * @return all strengthenings of axiom obtained by specialising its class expression
	 *         (using the {@code SpecialisationOperator} {@code specOp} of this
	 *         {@code JavaWeakener} object). Given an axiom A(i), the function
	 *         returns the set containing all the axioms A'(i) where A' is in
	 *         {@code genOp.specialise(A)}.
	 */
	public Set<OWLAxiom> getStrongerClassAssertionAxioms(OWLClassAssertionAxiom axiom) {
		HashSet<OWLAxiom> result = new HashSet<OWLAxiom>();

		OWLClassExpression expression = axiom.getClassExpression();

		Set<OWLClassExpression> specialisations = specOp.refine(expression);

		for (OWLClassExpression oce : specialisations) {
			OWLAxiom ax = new OWLClassAssertionAxiomImpl(axiom.getIndividual(), oce, EMPTY_ANNOTATION);
			result.add(ax);
		}

		return result;
	}

}
