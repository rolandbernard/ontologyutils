package www.ontologyutils.refinement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLNaryBooleanClassExpressionImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectAllValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectComplementOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectSomeValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectUnionOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLQuantifiedRestrictionImpl;
import www.ontologyutils.refinement.Covers.Cover;

public class RefinementOperator {

	private int flags_strict;
	private Cover way;
	private Cover back;

	private static final OWLClassExpression TOP = new OWLDataFactoryImpl().getOWLThing();
	private static final OWLClassExpression BOTTOM = new OWLDataFactoryImpl().getOWLNothing();

	public static final int FLAG_NON_STRICT = 0; // bit flag 00
	public static final int FLAG_ALC_STRICT = 1; // bit flag 01
	public static final int FLAG_NNF_STRICT = 2; // bit flag 10

	/**
	 * @param way
	 * @param back
	 * @param flags Use flag {@code FLAG_NNF_STRICT} to enforce inputs as NNF
	 *              formulas. Use {@code FLAG_ALC_STRICT} to enforce inputs as ALC
	 *              formulas (with strict binary conjunctions and disjunctions).
	 *              Combine them with {@code FLAG_NNF_STRICT | FLAG_ALC_STRICT}. Use
	 *              {@code FLAG_NON_STRICT} otherwise.
	 */
	public RefinementOperator(Cover way, Cover back, int flags) {
		this.way = way;
		this.back = back;
		this.flags_strict = flags;
	}

	public RefinementOperator(Cover way, Cover back) {
		this(way, back, FLAG_NON_STRICT);
	}

	/**
	 * @param concept
	 * @return
	 * @throws IllegalArgumentException if {@code concept} is not a well-formed
	 *                                  formula respecting the flags passed to the
	 *                                  constructor.
	 */
	public Set<OWLClassExpression> refine(OWLClassExpression concept) throws IllegalArgumentException {

		if (concept.isTopEntity()) {
			return refineTop();
		}
		if (concept.isBottomEntity()) {
			return refineBottom();
		}

		switch (concept.getClassExpressionType()) {
		case OWL_CLASS:
			return refineAtom(concept);
		case OBJECT_COMPLEMENT_OF:
			return refineNegation(concept);
		case OBJECT_INTERSECTION_OF:
			return refineConjunction(concept);
		case OBJECT_UNION_OF:
			return refineDisjunction(concept);
		case OBJECT_SOME_VALUES_FROM:
			return refineExistential(concept);
		case OBJECT_ALL_VALUES_FROM:
			return refineUniversal(concept);
		default:
			if ((this.flags_strict & FLAG_ALC_STRICT) == FLAG_ALC_STRICT) {
				throw (new IllegalArgumentException(
						"OWLClassExpression " + concept + " must represent an ALC concept."));
			} else {
				return way.getCover(concept);
			}
		}
	}

	/**
	 * @return
	 */
	private Set<OWLClassExpression> refineBottom() {
		HashSet<OWLClassExpression> result = new HashSet<OWLClassExpression>();
		result.addAll(way.getCover(BOTTOM));
		return result;
	}

	/**
	 * @return
	 */
	private Set<OWLClassExpression> refineTop() {
		HashSet<OWLClassExpression> result = new HashSet<OWLClassExpression>();
		result.addAll(way.getCover(TOP));
		return result;
	}

	/**
	 * @param concept
	 * @return
	 * @throws IllegalArgumentException
	 */
	private Set<OWLClassExpression> refineAtom(OWLClassExpression concept) throws IllegalArgumentException {
		if (!(concept.getClassExpressionType() == ClassExpressionType.OWL_CLASS)) {
			throw (new IllegalArgumentException("OWLClassExpression " + concept + "  must represent an OWL class."));
		}

		HashSet<OWLClassExpression> result = new HashSet<OWLClassExpression>();
		result.addAll(way.getCover(concept));
		return result;
	}

	/**
	 * @param concept
	 * @return
	 * @throws IllegalArgumentException
	 */
	private Set<OWLClassExpression> refineNegation(OWLClassExpression concept) throws IllegalArgumentException {
		if (!(concept.getClassExpressionType() == ClassExpressionType.OBJECT_COMPLEMENT_OF)) {
			throw (new IllegalArgumentException(
					"OWLClassExpression " + concept + "  must represent a negation (of an OWL class)."));
		}
		HashSet<OWLClassExpression> result = new HashSet<OWLClassExpression>();

		OWLObjectComplementOf complement = (OWLObjectComplementOf) concept;
		OWLClassExpression filler = complement.getOperand();

		if (((this.flags_strict & FLAG_NNF_STRICT) == FLAG_NNF_STRICT)
				&& !(filler.getClassExpressionType() == ClassExpressionType.OWL_CLASS)) {
			throw (new IllegalArgumentException("OWLClassExpression  " + filler + " must represent an OWL class."));
		}

		for (OWLClassExpression oce : back.getCover(filler)) {
			OWLObjectComplementOfImpl compl = new OWLObjectComplementOfImpl(oce);
			if ((this.flags_strict & FLAG_NNF_STRICT) == FLAG_NNF_STRICT) {
				result.add(compl.getNNF());
			} else {
				result.add(compl);
			}
		}

		// finally we add the "way" cover of concept
		result.addAll(way.getCover(concept));
		return result;
	}

	/**
	 * @param concept
	 * @return
	 * @throws IllegalArgumentException
	 */
	private Set<OWLClassExpression> refineConjunction(OWLClassExpression concept) throws IllegalArgumentException {

		List<OWLClassExpression> conjuncts = ((OWLNaryBooleanClassExpressionImpl) concept).getOperandsAsList();

		if (!(concept.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF)) {
			throw (new IllegalArgumentException("OWLClassExpression " + concept + "  must represent a conjunction."));
		}
		if (((this.flags_strict & FLAG_ALC_STRICT) == FLAG_ALC_STRICT) && (conjuncts.size() != 2)) {
			throw (new IllegalArgumentException(
					"OWLClassExpression " + concept + "  must represent a conjunction with two operands."));
		}

		HashSet<OWLClassExpression> result = new HashSet<OWLClassExpression>();

		for (int i = 0; i < conjuncts.size(); i++) {
			OWLClassExpression c = conjuncts.get(i);

			for (OWLClassExpression oce : refine(c)) {
				MyMultiSet<OWLClassExpression> newConj = new MyMultiSet<OWLClassExpression>();

				for (int j = 0; j < conjuncts.size(); j++) {
					if (j == i) {
						newConj.add(oce);
					} else {
						newConj.add(conjuncts.get(j));
					}
				}
				result.add(new OWLObjectIntersectionOfImpl(newConj.stream()));
			}
		}

		// finally we add the "way" cover of concept
		result.addAll(way.getCover(concept));

		return result;
	}

	/**
	 * @param concept
	 * @return
	 * @throws IllegalArgumentException
	 */
	private Set<OWLClassExpression> refineDisjunction(OWLClassExpression concept) throws IllegalArgumentException {

		List<OWLClassExpression> disjuncts = ((OWLNaryBooleanClassExpressionImpl) concept).getOperandsAsList();

		if (!(concept.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF)) {
			throw (new IllegalArgumentException("OWLClassExpression " + concept + "  must represent a disjunction."));
		}
		if (((this.flags_strict & FLAG_ALC_STRICT) == FLAG_ALC_STRICT) && (disjuncts.size() != 2)) {
			throw (new IllegalArgumentException(
					"OWLClassExpression " + concept + "  must represent a disjunction with two operands."));
		}

		HashSet<OWLClassExpression> result = new HashSet<OWLClassExpression>();

		for (int i = 0; i < disjuncts.size(); i++) {
			OWLClassExpression c = disjuncts.get(i);
			for (OWLClassExpression oce : refine(c)) {
				MyMultiSet<OWLClassExpression> newDisj = new MyMultiSet<OWLClassExpression>();

				for (int j = 0; j < disjuncts.size(); j++) {
					if (j == i) {
						newDisj.add(oce);
					} else {
						newDisj.add(disjuncts.get(j));
					}
				}
				result.add(new OWLObjectUnionOfImpl(newDisj.stream()));
			}
		}

		// finally we add the "way" cover of concept
		result.addAll(way.getCover(concept));

		return result;
	}

	/**
	 * @param concept
	 * @return
	 * @throws IllegalArgumentException
	 */
	private Set<OWLClassExpression> refineUniversal(OWLClassExpression concept) throws IllegalArgumentException {
		if (!(concept.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM)) {
			throw (new IllegalArgumentException(
					"OWLClassExpression  " + concept + " must represent a universal quantification."));
		}

		HashSet<OWLClassExpression> result = new HashSet<OWLClassExpression>();

		OWLClassExpression filler = ((OWLQuantifiedRestrictionImpl<OWLClassExpression>) concept).getFiller();
		OWLObjectPropertyExpression property = ((OWLObjectAllValuesFromImpl) concept).getProperty();

		for (OWLClassExpression oce : refine(filler)) {
			result.add(new OWLObjectAllValuesFromImpl(property, oce));
		}

		// finally we add the "way" cover of concept
		result.addAll(way.getCover(concept));
		return result;
	}

	/**
	 * @param concept
	 * @return
	 * @throws IllegalArgumentException
	 */
	private Set<OWLClassExpression> refineExistential(OWLClassExpression concept) throws IllegalArgumentException {
		if (!(concept.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM)) {
			throw (new IllegalArgumentException(
					"OWLClassExpression " + concept + "  must represent an existential quantification."));
		}

		HashSet<OWLClassExpression> result = new HashSet<OWLClassExpression>();

		OWLClassExpression filler = ((OWLQuantifiedRestrictionImpl<OWLClassExpression>) concept).getFiller();
		OWLObjectPropertyExpression property = ((OWLObjectSomeValuesFromImpl) concept).getProperty();

		for (OWLClassExpression oce : refine(filler)) {
			result.add(new OWLObjectSomeValuesFromImpl(property, oce));
		}

		// finally we add the "way" cover of concept
		result.addAll(way.getCover(concept));
		return result;
	}

	/**
	 * This class implements the Set Java interface as a MultiSet.
	 * 
	 * This allows us create formulas like OWLObjectIntersectionOfImp({owl:Thing,
	 * owl:Thing}) or OWLObjectUnionOfImpl({:A,:B,:A}) with the OWL API
	 * uk.ac.manchester.cs.owl.owlapi.
	 *
	 * @param <E>
	 */
	class MyMultiSet<E> implements Set<E> {

		ArrayList<E> elements = new ArrayList<E>();

		@Override
		public int size() {
			return elements.size();
		}

		@Override
		public boolean isEmpty() {
			return elements.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return elements.contains(o);
		}

		@Override
		public Iterator<E> iterator() {
			return elements.iterator();
		}

		@Override
		public Object[] toArray() {
			return elements.toArray();
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return (T[]) elements.toArray();
		}

		@Override
		public boolean add(E e) {
			return elements.add(e);
		}

		@Override
		public boolean remove(Object o) {
			return elements.remove(o);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return elements.containsAll(c);
		}

		@Override
		public boolean addAll(Collection<? extends E> c) {
			return elements.addAll(c);
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			return elements.retainAll(c);
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			return elements.removeAll(c);
		}

		@Override
		public void clear() {
			elements.clear();
		}

	}

}
