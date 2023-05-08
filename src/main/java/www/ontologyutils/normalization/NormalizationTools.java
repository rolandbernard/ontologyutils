package www.ontologyutils.normalization;

import java.security.InvalidParameterException;
import java.util.*;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.*;

/**
 * Class with utility methods for normalization.
 */
public class NormalizationTools {
    /**
     * This is a function that completes {@code asSubClassOfAxioms} that
     * already exists for {@code OWLEquivalentClassesAxiomImpl} and
     * {@code OWLDisjointClassesAxiomImpl} and for
     * {@code OWLSubClassOfAxiomShortCut} in general. It thus obviously
     * works for axiom types subclass, equivalent class, disjoint class.
     * Moreover, we extend it to axiom types: disjoint union, object
     * property range, object property domain.
     *
     * @param ax
     *            The axiom to convert.
     * @return a collection of subclass axioms that are equivalent to ax
     */
    public static Collection<OWLSubClassOfAxiom> asSubClassOfAxioms(OWLAxiom ax) throws InvalidParameterException {
        OWLDataFactory df = Ontology.getDefaultDataFactory();
        Collection<OWLSubClassOfAxiom> subClassOfAxioms = new ArrayList<OWLSubClassOfAxiom>();

        // If ax is a subclass axiom, there is nothing to do.
        if (ax.isOfType(AxiomType.SUBCLASS_OF)) {
            subClassOfAxioms.add((OWLSubClassOfAxiom) ax);
        }
        // If ax is an equivalent class axiom, we can use asOWLSubClassOfAxioms().
        else if (ax.isOfType(AxiomType.EQUIVALENT_CLASSES)) {
            subClassOfAxioms = ((OWLEquivalentClassesAxiom) ax).asOWLSubClassOfAxioms();
        }
        // If ax is a disjoint class axiom, we can use asOWLSubClassOfAxioms().
        else if (ax.isOfType(AxiomType.DISJOINT_CLASSES)) {
            subClassOfAxioms = ((OWLDisjointClassesAxiom) ax).asOWLSubClassOfAxioms();
        }
        // If ax is a disjoint union axiom, we must first transform them into one
        // disjoint class axiom
        // and one equivalent class axiom. Then, we can use asOWLSubClassOfAxioms().
        else if (ax.isOfType(AxiomType.DISJOINT_UNION)) {
            OWLDisjointClassesAxiom disjointClasses = ((OWLDisjointUnionAxiom) ax).getOWLDisjointClassesAxiom();
            OWLEquivalentClassesAxiom equivalentClasses = ((OWLDisjointUnionAxiom) ax)
                    .getOWLEquivalentClassesAxiom();
            subClassOfAxioms = disjointClasses.asOWLSubClassOfAxioms();
            subClassOfAxioms.addAll(equivalentClasses.asOWLSubClassOfAxioms());
        }
        // If ax is an object property axiom, we can use
        // property R with range C ==> subclass(top, forall R. C)
        else if (ax.isOfType(AxiomType.OBJECT_PROPERTY_RANGE)) {
            OWLObjectPropertyExpression property = ((OWLObjectPropertyRangeAxiom) ax).getProperty();
            OWLClassExpression range = ((OWLObjectPropertyRangeAxiom) ax).getRange();
            OWLSubClassOfAxiom scoa = df.getOWLSubClassOfAxiom(df.getOWLThing(),
                    df.getOWLObjectAllValuesFrom(property, range));
            subClassOfAxioms.add(scoa);
        }
        // If ax is an object domain axiom, we can use
        // property R with domain C ==> subclass(exists R. top, C)
        else if (ax.isOfType(AxiomType.OBJECT_PROPERTY_DOMAIN)) {
            OWLObjectPropertyExpression property = ((OWLObjectPropertyDomainAxiom) ax).getProperty();
            OWLClassExpression domain = ((OWLObjectPropertyDomainAxiom) ax).getDomain();
            OWLSubClassOfAxiom scoa = df
                    .getOWLSubClassOfAxiom(df.getOWLObjectSomeValuesFrom(property, df.getOWLThing()), domain);
            subClassOfAxioms.add(scoa);
        } else if (ax.isOfType(AxiomType.FUNCTIONAL_OBJECT_PROPERTY)) {
            subClassOfAxioms = Collections.singleton(((OWLFunctionalObjectPropertyAxiom) ax).asOWLSubClassOfAxiom());
        } else if (ax.isOfType(AxiomType.DATA_PROPERTY_RANGE)) {
            subClassOfAxioms = Collections.singleton(((OWLDataPropertyRangeAxiom) ax).asOWLSubClassOfAxiom());
        } else if (ax.isOfType(AxiomType.DATA_PROPERTY_DOMAIN)) {
            subClassOfAxioms = Collections.singleton(((OWLSubClassOfAxiomShortCut) ax).asOWLSubClassOfAxiom());
        } else if (ax.isOfType(AxiomType.FUNCTIONAL_DATA_PROPERTY)) {
            subClassOfAxioms = Collections.singleton(((OWLFunctionalDataPropertyAxiom) ax).asOWLSubClassOfAxiom());
        } else {
            throw new RuntimeException("The axiom " + ax + " of type " + ax.getAxiomType()
                    + " could not be converted into subclass axioms.");
        }

        // we add an annotation to each axiom referring to the original axiom in
        // parameter
        Collection<OWLSubClassOfAxiom> annotatedSubClassOfAxioms = new ArrayList<OWLSubClassOfAxiom>();
        subClassOfAxioms.forEach(
                a -> annotatedSubClassOfAxioms.add((OWLSubClassOfAxiom) Ontology.getOriginAnnotatedAxiom(a, ax)));

        return annotatedSubClassOfAxioms;
    }

    /**
     * @param ax
     *            The axiom to convert.
     * @return The collection of subClassOf axioms equivalent to {@code ax}.
     */
    @SuppressWarnings("unchecked")
    public static Collection<OWLSubClassOfAxiom> normalizeSubClassAxiom(OWLSubClassOfAxiom ax) {
        OWLDataFactory df = Ontology.getDefaultDataFactory();
        Collection<OWLSubClassOfAxiom> normalizedAxioms = new ArrayList<OWLSubClassOfAxiom>();
        LinkedList<OWLSubClassOfAxiom> axioms = new LinkedList<OWLSubClassOfAxiom>();
        axioms.add(ax);

        while (!axioms.isEmpty()) {

            OWLSubClassOfAxiom currentAxiom = axioms.remove();
            if (NormalForm.isNormalFormTBoxAxiom(currentAxiom)) {
                normalizedAxioms.add(currentAxiom);
                continue;
            }
            // if currentAxiom is not in normal form
            OWLClassExpression left = currentAxiom.getSubClass();
            OWLClassExpression right = currentAxiom.getSuperClass();

            // If left is disjunction or right is conjunction,
            // simple transformations can be done without introducing fresh variables.
            // We deal with these two cases in priority.

            // left is disjunction
            if (left.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF) {
                Set<OWLClassExpression> disjunctions = left.asDisjunctSet();
                for (OWLClassExpression d : disjunctions) {
                    OWLSubClassOfAxiom sba = df.getOWLSubClassOfAxiom(d, right,
                            Ontology.axiomOriginAnnotations(currentAxiom).toList());
                    axioms.add(sba);
                }
            }
            // right is conjunction
            else if (right.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
                Set<OWLClassExpression> conjunctions = right.asConjunctSet();
                for (OWLClassExpression c : conjunctions) {
                    OWLSubClassOfAxiom sba = df.getOWLSubClassOfAxiom(left, c,
                            Ontology.axiomOriginAnnotations(currentAxiom).toList());
                    axioms.add(sba);
                }
            }

            // If right is disjunction or left is conjunction

            // left is conjunction
            else if (left.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF) {
                if (!NormalForm.isConjunctionOfAtoms(left)) {
                    Set<OWLClassExpression> conjuncts = left.asConjunctSet();
                    Set<OWLClassExpression> newConjuncts = new HashSet<>();

                    for (OWLClassExpression conj : conjuncts) {
                        if (NormalForm.isAtom(conj)) {
                            newConjuncts.add(conj);
                        } else {
                            // creating fresh concept
                            OWLClassExpression fresh = FreshAtoms.createFreshAtomCopy(conj);
                            // it'll be a new atomic conjunct
                            newConjuncts.add(fresh);

                            // adding fresh -> conj
                            OWLSubClassOfAxiom sbaFreshOce = df.getOWLSubClassOfAxiom(fresh, conj,
                                    Ontology.axiomOriginAnnotations(currentAxiom).toList());
                            axioms.add(sbaFreshOce);
                            // adding conj -> fresh
                            OWLSubClassOfAxiom sbaOceFresh = df.getOWLSubClassOfAxiom(conj, fresh,
                                    Ontology.axiomOriginAnnotations(currentAxiom).toList());
                            axioms.add(sbaOceFresh);

                        }
                    }
                    // adding new_conjunction -> right
                    OWLSubClassOfAxiom sba = df.getOWLSubClassOfAxiom(
                            df.getOWLObjectIntersectionOf(newConjuncts.stream()), right,
                            Ontology.axiomOriginAnnotations(currentAxiom).toList());
                    axioms.add(sba);
                }

            }
            // right is disjunction
            else if (right.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF) {
                if (!NormalForm.isDisjunctionOfAtoms(right)) {
                    Set<OWLClassExpression> disjuncts = right.asDisjunctSet();
                    Set<OWLClassExpression> newDisjuncts = new HashSet<>();

                    for (OWLClassExpression disj : disjuncts) {
                        if (NormalForm.isAtom(disj)) {
                            newDisjuncts.add(disj);
                        } else {
                            // creating fresh concept
                            OWLClassExpression fresh = FreshAtoms.createFreshAtomCopy(disj);
                            // it'll be a new atomic disjunct
                            newDisjuncts.add(fresh);

                            // adding fresh -> disj
                            OWLSubClassOfAxiom sbaFreshOce = df.getOWLSubClassOfAxiom(fresh, disj,
                                    Ontology.axiomOriginAnnotations(currentAxiom).toList());
                            axioms.add(sbaFreshOce);
                            // adding disj -> fresh
                            OWLSubClassOfAxiom sbaOceFresh = df.getOWLSubClassOfAxiom(disj, fresh,
                                    Ontology.axiomOriginAnnotations(currentAxiom).toList());
                            axioms.add(sbaOceFresh);

                        }
                    }
                    // adding left -> new_disjunction
                    OWLSubClassOfAxiom sba = df.getOWLSubClassOfAxiom(left,
                            df.getOWLObjectUnionOf(newDisjuncts.stream()),
                            Ontology.axiomOriginAnnotations(currentAxiom).toList());
                    axioms.add(sba);
                }
            }

            // If left or right is negation

            // left is negation
            else if (left.getClassExpressionType() == ClassExpressionType.OBJECT_COMPLEMENT_OF) {
                // left = not filler
                OWLClassExpression filler = ((OWLObjectComplementOf) left).getOperand(); // left.getComplementNNF();

                // we add : top -> filler or right
                Set<OWLClassExpression> operands = right.asDisjunctSet();
                operands.add(filler);
                OWLObjectUnionOf newRight = df.getOWLObjectUnionOf(operands.stream());

                OWLSubClassOfAxiom sba = df.getOWLSubClassOfAxiom(df.getOWLThing(), newRight,
                        Ontology.axiomOriginAnnotations(currentAxiom).toList());
                axioms.add(sba);
            }
            // right is negation
            else if (right.getClassExpressionType() == ClassExpressionType.OBJECT_COMPLEMENT_OF) {
                // right = not filler
                OWLClassExpression filler = ((OWLObjectComplementOf) right).getOperand(); // right.getComplementNNF();

                // we add : left and filler -> bot
                Set<OWLClassExpression> operands = left.asConjunctSet();
                operands.add(filler);
                OWLObjectIntersectionOf newLeft = df.getOWLObjectIntersectionOf(operands.stream());

                OWLSubClassOfAxiom sba = df.getOWLSubClassOfAxiom(newLeft, df.getOWLNothing(),
                        Ontology.axiomOriginAnnotations(currentAxiom).toList());
                axioms.add(sba);
            }

            // If left or right is existential

            else if ((left.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM)
                    && !NormalForm.isExistentialOfAtom(left)) { // left existential atom is fine
                // left = exists property filler
                OWLClassExpression filler = ((OWLQuantifiedRestriction<OWLClassExpression>) left).getFiller();
                OWLObjectPropertyExpression property = ((OWLObjectSomeValuesFrom) left).getProperty();

                // creating fresh concept
                OWLClassExpression fresh = FreshAtoms.createFreshAtomCopy(filler);

                // we add exists property fresh -> right
                OWLObjectSomeValuesFrom evf = df.getOWLObjectSomeValuesFrom(property, fresh);
                OWLSubClassOfAxiom sba = df.getOWLSubClassOfAxiom(evf, right,
                        Ontology.axiomOriginAnnotations(currentAxiom).toList());
                axioms.add(sba);

                // we add fresh -> filler
                OWLSubClassOfAxiom sbaFreshFiller = df.getOWLSubClassOfAxiom(fresh, filler,
                        Ontology.axiomOriginAnnotations(currentAxiom).toList());
                axioms.add(sbaFreshFiller);

                // we add filler -> fresh
                OWLSubClassOfAxiom sbaFillerFresh = df.getOWLSubClassOfAxiom(filler, fresh,
                        Ontology.axiomOriginAnnotations(currentAxiom).toList());
                axioms.add(sbaFillerFresh);
            } else if ((right.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM)
                    && !NormalForm.isExistentialOfAtom(right)) { // right existential atom is fine
                // right = exists property filler
                OWLClassExpression filler = ((OWLQuantifiedRestriction<OWLClassExpression>) right).getFiller();
                OWLObjectPropertyExpression property = ((OWLObjectSomeValuesFrom) right).getProperty();

                // creating fresh concept
                OWLClassExpression fresh = FreshAtoms.createFreshAtomCopy(filler);

                // we add left -> exists property fresh
                OWLObjectSomeValuesFrom evf = df.getOWLObjectSomeValuesFrom(property, fresh);
                OWLSubClassOfAxiom sba = df.getOWLSubClassOfAxiom(left, evf,
                        Ontology.axiomOriginAnnotations(currentAxiom).toList());
                axioms.add(sba);

                // we add fresh -> filler
                OWLSubClassOfAxiom sbaFreshFiller = df.getOWLSubClassOfAxiom(fresh, filler,
                        Ontology.axiomOriginAnnotations(currentAxiom).toList());
                axioms.add(sbaFreshFiller);

                // we add filler -> fresh
                OWLSubClassOfAxiom sbaFillerFresh = df.getOWLSubClassOfAxiom(filler, fresh,
                        Ontology.axiomOriginAnnotations(currentAxiom).toList());
                axioms.add(sbaFillerFresh);
            }

            // If left or right is universal

            else if ((left.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM)) {
                // we just take the contrapositive
                OWLSubClassOfAxiom sba = df.getOWLSubClassOfAxiom(right.getComplementNNF(), left.getComplementNNF(),
                        Ontology.axiomOriginAnnotations(currentAxiom).toList());
                axioms.add(sba);
            } else if ((right.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM)
                    && !NormalForm.isUniversalOfAtom(right)) { // right universal atom is fine
                // right = forall property filler
                OWLClassExpression filler = ((OWLQuantifiedRestriction<OWLClassExpression>) right).getFiller();
                OWLObjectPropertyExpression property = ((OWLObjectSomeValuesFrom) right).getProperty();

                // creating fresh concept
                OWLClassExpression fresh = FreshAtoms.createFreshAtomCopy(filler);

                // we add left -> forall property fresh
                OWLObjectAllValuesFrom avf = df.getOWLObjectAllValuesFrom(property, fresh);
                OWLSubClassOfAxiom sba = df.getOWLSubClassOfAxiom(left, avf,
                        Ontology.axiomOriginAnnotations(currentAxiom).toList());
                axioms.add(sba);

                // we add fresh -> filler
                OWLSubClassOfAxiom sbaFreshFiller = df.getOWLSubClassOfAxiom(fresh, filler,
                        Ontology.axiomOriginAnnotations(currentAxiom).toList());
                axioms.add(sbaFreshFiller);

                // we add filler -> fresh
                OWLSubClassOfAxiom sbaFillerFresh = df.getOWLSubClassOfAxiom(filler, fresh,
                        Ontology.axiomOriginAnnotations(currentAxiom).toList());
                axioms.add(sbaFillerFresh);
            } else {
                throw new RuntimeException("I don't know what to do with " + ax);
            }

        } // end while
        return normalizedAxioms;
    }
}
