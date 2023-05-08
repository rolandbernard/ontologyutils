package www.ontologyutils.normalization;

import java.util.Set;

import org.semanticweb.owlapi.model.*;

/**
 * A TBox axiom in normal form can be of one of four types:
 * <ul>
 * <li>Type 1: Subclass(atom or conjunction of atoms, atom or
 * disjunction of atoms)
 * <li>Type 2: Subclass(atom, exists property atom)
 * <li>Type 3: Subclass(atom, forall property atom)
 * <li>Type 4: Subclass(exists property atom, atom)
 * </ul>
 *
 * @author nico
 */
public class NormalForm {
    /**
     * @param ax
     *            The axiom to test.
     * @return true if the axiom is in normal form.
     */
    public static boolean isNormalFormTBoxAxiom(OWLAxiom ax) {
        if (!ax.isOfType(AxiomType.SUBCLASS_OF)) {
            return false;
        }
        OWLClassExpression left = ((OWLSubClassOfAxiom) ax).getSubClass();
        OWLClassExpression right = ((OWLSubClassOfAxiom) ax).getSuperClass();
        if (typeOneSubClassAxiom(left, right) || typeTwoSubClassAxiom(left, right)
                || typeThreeSubClassAxiom(left, right) || typeFourSubClassAxiom(left, right)) {
            return true;
        }
        return false;
    }

    /**
     * @param left
     *            The left concept.
     * @param right
     *            The right concept.
     * @return true if the axiom is a type one subclass axiom.
     */
    public static boolean typeOneSubClassAxiom(OWLClassExpression left, OWLClassExpression right) {
        return ((isAtom(left) || isConjunctionOfAtoms(left)) && (isAtom(right) || isDisjunctionOfAtoms(right)));
    }

    /**
     * @param left
     *            The left concept.
     * @param right
     *            The right concept.
     * @return true if the axiom is a type two subclass axiom.
     */
    public static boolean typeTwoSubClassAxiom(OWLClassExpression left, OWLClassExpression right) {
        return (isAtom(left) && isExistentialOfAtom(right));
    }

    /**
     * @param left
     *            The left concept.
     * @param right
     *            The right concept.
     * @return true if the axiom is a type three subclass axiom.
     */
    public static boolean typeThreeSubClassAxiom(OWLClassExpression left, OWLClassExpression right) {
        return (isAtom(left) && isUniversalOfAtom(right));
    }

    /**
     * @param left
     *            The left concept.
     * @param right
     *            The right concept.
     * @return true if the axiom is a type four subclass axiom.
     */
    public static boolean typeFourSubClassAxiom(OWLClassExpression left, OWLClassExpression right) {
        return (isExistentialOfAtom(left) && isAtom(right));
    }

    /**
     * @param e
     *            The concept.
     * @return true if the concept is a atom.
     */
    public static boolean isAtom(OWLClassExpression e) {
        return e.isOWLClass() || e.isTopEntity() || e.isBottomEntity();
    }

    /**
     * @param e
     *            The concept.
     * @return true if the concept is a type A atom.
     */
    static boolean isTypeAAtom(OWLClassExpression e) {
        return e.isOWLClass() || e.isTopEntity();
    }

    /**
     * @param e
     *            The concept.
     * @return true if the concept is a type V atom.
     */
    static boolean isTypeBAtom(OWLClassExpression e) {
        return e.isOWLClass() || e.isBottomEntity();
    }

    /**
     * @param e
     *            The concept.
     * @return true if the concept is a conjunction of atom.
     */
    static boolean isConjunctionOfAtoms(OWLClassExpression e) {
        if (!(e.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF)) {
            return false;
        }
        Set<OWLClassExpression> conjunctions = e.asConjunctSet();
        for (OWLClassExpression c : conjunctions) {
            if (!isAtom(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param e
     *            The concept.
     * @return true if the concept is a disjunction of atom.
     */
    static boolean isDisjunctionOfAtoms(OWLClassExpression e) {
        if (!(e.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF)) {
            return false;
        }
        Set<OWLClassExpression> disjunctions = e.asDisjunctSet();
        for (OWLClassExpression d : disjunctions) {
            if (!isAtom(d)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param e
     *            The concept.
     * @return true if the concept is an existential of an atom.
     */
    @SuppressWarnings("unchecked")
    static boolean isExistentialOfAtom(OWLClassExpression e) {
        if (!(e.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM)) {
            return false;
        }
        OWLClassExpression filler = ((OWLQuantifiedRestriction<OWLClassExpression>) e).getFiller();
        if (!isAtom(filler)) {
            return false;
        }
        return true;
    }

    /**
     * @param e
     *            The concept.
     * @return true if the concept is a universal of an atom.
     */
    @SuppressWarnings("unchecked")
    static boolean isUniversalOfAtom(OWLClassExpression e) {
        if (!(e.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM)) {
            return false;
        }
        OWLClassExpression filler = ((OWLQuantifiedRestriction<OWLClassExpression>) e).getFiller();
        if (!isAtom(filler)) {
            return false;
        }
        return true;
    }
}
