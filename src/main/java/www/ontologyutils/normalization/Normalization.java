package www.ontologyutils.normalization;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.*;

public class Normalization {
    /**
     * @param ontology
     *            with TBox axioms all in subclass form.
     * @see NormalizationTools#asSubClassOfAxioms(OWLAxiom)
     * @return A normalized version of {@code ontology}.
     */
    public static Ontology normalizeNaive(Ontology ontology) {
        // we make a copy of the ontology without the TBox
        Ontology newOntology = Ontology.emptyOntology();
        newOntology.addAxioms(ontology.rboxAxioms());
        newOntology.addAxioms(ontology.aboxAxioms());

        Set<OWLAxiom> tBoxAxioms = ontology.tboxAxioms().collect(Collectors.toSet());

        tBoxAxioms.forEach(
                ax -> newOntology.addAxioms(NormalizationTools.normalizeSubClassAxiom((OWLSubClassOfAxiom) ax)));

        return newOntology;
    }

    /**
     * @param ontology
     *            with TBox axioms all in subclass form.
     * @see NormalizationTools#asSubClassOfAxioms(OWLAxiom)
     * @return A normalized version of {@code ontology}, following the procedure of
     *         Simancik et al. "Consequence-Based Reasoning beyond Horn Ontologies"
     *         (IJCAI 2011).
     */
    public static Ontology normalizeCondor(Ontology ontology) {
        // we make a copy of the ontology without the TBox
        Ontology newOntology = Ontology.emptyOntology();
        newOntology.addAxioms(ontology.rboxAxioms());
        newOntology.addAxioms(ontology.aboxAxioms());

        Set<OWLAxiom> tBoxAxioms = ontology.tboxAxioms().collect(Collectors.toSet());

        // we replace negative occurrences of forall p C with not exists p not C
        List<OWLClassExpression> subConcepts = ontology.subConceptsOfTbox().toList();
        for (OWLClassExpression e : subConcepts) {
            Set<OWLAxiom> newtBoxAxioms = new HashSet<OWLAxiom>(tBoxAxioms);
            if (e.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM) {
                newtBoxAxioms.stream().forEach(a -> {
                    tBoxAxioms.remove(a);
                    tBoxAxioms.add(substituteNegativeForAll((OWLObjectAllValuesFrom) e, (OWLSubClassOfAxiom) a));
                });
            }
        }
        newOntology.addAxioms(tBoxAxioms);

        // we get the structural transformation of newOntology
        Ontology transformed = structuralTransformation(newOntology);

        // replace X -> Y and Z with two axioms X -> Y and X -> Z
        // replace X or Y -> Z with two axioms X -> Z and Y -> Z
        // replace X -> not Y with X and Y -> bot
        // replace not X -> Y with top -> X or Y
        transformed.tboxAxioms().toList().forEach(axiom -> {
            transformed.removeAxioms(axiom);
            transformed.addAxioms(NormalizationTools.normalizeSubClassAxiom((OWLSubClassOfAxiom) axiom));
        });
        return transformed;
    }

    private static OWLAxiom substituteNegativeForAll(OWLObjectAllValuesFrom e, OWLSubClassOfAxiom a) {
        OWLDataFactory df = Ontology.getDefaultDataFactory();
        OWLObjectPropertyExpression property = e.getProperty();
        OWLClassExpression filler = e.getFiller();

        // new concept: not exists property not filler
        OWLObjectComplementOf substitute = df.getOWLObjectComplementOf(
                df.getOWLObjectSomeValuesFrom(property, df.getOWLObjectComplementOf(filler)));

        OWLClassExpression newLeft = replaceIfPolarity(e, substitute, a.getSubClass(), true);
        OWLClassExpression newRight = replaceIfPolarity(e, substitute, a.getSuperClass(), false);

        return df.getOWLSubClassOfAxiom(newLeft, newRight, Ontology.axiomOriginAnnotations(a).toList());
    }

    /**
     * @param e
     *            a forall expression
     * @param substitute
     *            a substitute not exists no expression for {@code e}
     * @param in
     *            the expression in which to substitute {@code e} with
     *            {@code substitute}
     * @param polarity
     *            substitute {@code e} with {@code substitute} when {@code e}
     *            appears positively ({@code polarity} is true) or negatively
     *            ({@code polarity} is false) in {@code in}
     * @return the resulting expression
     */
    private static OWLClassExpression replaceIfPolarity(OWLObjectAllValuesFrom e, OWLObjectComplementOf substitute,
            OWLClassExpression in, Boolean polarity) {
        /*
         * if (polarity ? !isPositiveIn(e, in) : !isNegativeIn(e, in)) { return in; }
         */
        OWLDataFactory df = Ontology.getDefaultDataFactory();
        switch (in.getClassExpressionType()) {
            case OWL_CLASS: {
                return in;
            }
            case OBJECT_COMPLEMENT_OF: {
                OWLClassExpression op = ((OWLObjectComplementOf) in).getOperand();
                return df.getOWLObjectComplementOf(replaceIfPolarity(e, substitute, op, !polarity));
            }
            case OBJECT_UNION_OF: {
                Set<OWLClassExpression> disjuncts = in.asDisjunctSet();
                return df.getOWLObjectUnionOf(
                        disjuncts.stream().map(d -> replaceIfPolarity(e, substitute, d, polarity)));
            }
            case OBJECT_INTERSECTION_OF: {
                Set<OWLClassExpression> conjuncts = in.asConjunctSet();
                return df.getOWLObjectIntersectionOf(
                        conjuncts.stream().map(c -> replaceIfPolarity(e, substitute, c, polarity)));
            }
            case OBJECT_SOME_VALUES_FROM: {
                OWLClassExpression filler = ((OWLObjectSomeValuesFrom) in).getFiller();
                OWLObjectPropertyExpression property = ((OWLObjectSomeValuesFrom) in).getProperty();
                return df.getOWLObjectSomeValuesFrom(property, replaceIfPolarity(e, substitute, filler, polarity));
            }
            case OBJECT_ALL_VALUES_FROM: {
                if (Utils.sameConcept(e, in)) {
                    return polarity ? substitute : e;
                }
                OWLClassExpression filler = ((OWLObjectAllValuesFrom) in).getFiller();
                OWLObjectPropertyExpression property = ((OWLObjectAllValuesFrom) in).getProperty();
                return df.getOWLObjectAllValuesFrom(property, replaceIfPolarity(e, substitute, filler, polarity));

            }
            default:
                throw new RuntimeException();
        }
    }

    /**
     * @param ontology
     *            with TBox axioms all in subclass form.
     * @see NormalizationTools#asSubClassOfAxioms()
     * @return A new ontology with the TBox axioms structurally transformed,
     *         following the procedure of Simancik et al. "Consequence-Based
     *         Reasoning beyond Horn Ontologies" (IJCAI 2011).
     */
    private static Ontology structuralTransformation(Ontology ontology) {
        OWLDataFactory df = Ontology.getDefaultDataFactory();
        Ontology newOntology = Ontology.emptyOntology();
        newOntology.addAxioms(ontology.rboxAxioms());
        newOntology.addAxioms(ontology.aboxAxioms());
        Collection<OWLSubClassOfAxiom> transformed = new ArrayList<>();
        List<OWLClassExpression> subConcepts = ontology.subConceptsOfTbox().toList();
        Map<OWLClassExpression, OWLClassExpression> map = new HashMap<>();
        for (OWLClassExpression e : subConcepts) {
            if (e.isOWLClass()) {
                map.put(e, e);
            } else { // as per the article it should be like that always
                OWLClassExpression fresh = FreshAtoms.createFreshAtomCopy(e);
                map.put(e, fresh);
            }
        }
        for (OWLClassExpression e : subConcepts) {
            if (e.isOWLClass()) { // as per the article it shouldn't be skipped
                continue;
            }
            if (ontology.tboxAxioms().anyMatch(ax -> isNegativeIn(e, ax))) {
                ontology.tboxAxioms().forEach(ax -> {
                    if (isNegativeIn(e, ax)) {
                        OWLSubClassOfAxiom sba = df.getOWLSubClassOfAxiom(structuralTransformation(e, map),
                                map.get(e), Ontology.axiomOriginAnnotations(ax).toList());
                        transformed.add(sba);
                    }
                });
            }
            if (ontology.tboxAxioms().anyMatch(ax -> isPositiveIn(e, ax))) {
                ontology.tboxAxioms().forEach(ax -> {
                    if (isPositiveIn(e, ax)) {
                        OWLSubClassOfAxiom sba = df.getOWLSubClassOfAxiom(map.get(e),
                                structuralTransformation(e, map), Ontology.axiomOriginAnnotations(ax).toList());
                        transformed.add(sba);
                    }
                });
            }
        }
        ontology.tboxAxioms().forEach((a) -> {
            if (!(a instanceof OWLSubClassOfAxiom)) {
                throw new RuntimeException("Axiom " + a + " should be a subclass axiom.");
            } else {
                OWLClassExpression left = ((OWLSubClassOfAxiom) a).getSubClass();
                OWLClassExpression right = ((OWLSubClassOfAxiom) a).getSuperClass();

                OWLSubClassOfAxiom sba = df.getOWLSubClassOfAxiom(map.get(left), map.get(right),
                        Ontology.axiomOriginAnnotations(a).toList());
                transformed.add(sba);
            }
        });
        newOntology.addAxioms(transformed);
        return newOntology;
    }

    @SuppressWarnings("unchecked")
    private static boolean isPositiveIn(OWLClassExpression e, OWLClassExpression in) {
        if (e.equals(in)) {
            return true;
        } else if ((in.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF)) {
            return in.asDisjunctSet().stream().anyMatch(disj -> isPositiveIn(e, (OWLClassExpression) disj));
        } else if ((in.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF)) {
            return in.asConjunctSet().stream().anyMatch(conj -> isPositiveIn(e, (OWLClassExpression) conj));
        } else if ((in.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM)) {
            return isPositiveIn(e, ((OWLQuantifiedRestriction<OWLClassExpression>) in).getFiller());
        } else if ((in.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM)) {
            return isPositiveIn(e, ((OWLQuantifiedRestriction<OWLClassExpression>) in).getFiller());
        } else if ((in.getClassExpressionType() == ClassExpressionType.OBJECT_COMPLEMENT_OF)) {
            return isNegativeIn(e, ((OWLObjectComplementOf) in).getOperand());
        }
        return false;
    }

    private static boolean isPositiveIn(OWLClassExpression e, OWLAxiom ax) {
        OWLClassExpression left = ((OWLSubClassOfAxiom) ax).getSubClass();
        OWLClassExpression right = ((OWLSubClassOfAxiom) ax).getSuperClass();
        return isPositiveIn(e, right) || isNegativeIn(e, left);
    }

    @SuppressWarnings("unchecked")
    private static boolean isNegativeIn(OWLClassExpression e, OWLClassExpression in) {
        if (e.equals(in)) {
            return false;
        } else if ((in.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF)) {
            return in.asDisjunctSet().stream().anyMatch(disj -> isNegativeIn(e, (OWLClassExpression) disj));
        } else if ((in.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF)) {
            return in.asConjunctSet().stream().anyMatch(conj -> isNegativeIn(e, (OWLClassExpression) conj));
        } else if ((in.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM)) {
            return isNegativeIn(e, ((OWLQuantifiedRestriction<OWLClassExpression>) in).getFiller());
        } else if ((in.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM)) {
            return isNegativeIn(e, ((OWLQuantifiedRestriction<OWLClassExpression>) in).getFiller());
        } else if ((in.getClassExpressionType() == ClassExpressionType.OBJECT_COMPLEMENT_OF)) {
            return isPositiveIn(e, ((OWLObjectComplementOf) in).getOperand());
        }
        return false;
    }

    private static boolean isNegativeIn(OWLClassExpression e, OWLAxiom ax) {
        OWLClassExpression left = ((OWLSubClassOfAxiom) ax).getSubClass();
        OWLClassExpression right = ((OWLSubClassOfAxiom) ax).getSuperClass();
        return isPositiveIn(e, left) || isNegativeIn(e, right);
    }

    @SuppressWarnings("unchecked")
    private static OWLClassExpression structuralTransformation(OWLClassExpression e,
            Map<OWLClassExpression, OWLClassExpression> map) {
        OWLDataFactory df = Ontology.getDefaultDataFactory();
        if (e.isTopEntity()) {
            return e;
        } else if (e.isBottomEntity()) {
            return e;
        } else if (e.isOWLClass()) {
            return e;
        } else if (e instanceof OWLObjectComplementOf) {
            return df.getOWLObjectComplementOf(map.get(((OWLObjectComplementOf) e).getOperand()));
        } else if ((e.getClassExpressionType() == ClassExpressionType.OBJECT_INTERSECTION_OF)) {
            Set<OWLClassExpression> conjunctions = e.asConjunctSet();
            return df.getOWLObjectIntersectionOf(conjunctions.stream().map((c) -> map.get(c)));
        } else if ((e.getClassExpressionType() == ClassExpressionType.OBJECT_UNION_OF)) {
            Set<OWLClassExpression> disjunctions = e.asDisjunctSet();
            return df.getOWLObjectUnionOf(disjunctions.stream().map((c) -> map.get(c)));
        } else if ((e.getClassExpressionType() == ClassExpressionType.OBJECT_ALL_VALUES_FROM)) {
            OWLClassExpression filler = ((OWLQuantifiedRestriction<OWLClassExpression>) e).getFiller();
            OWLObjectPropertyExpression property = ((OWLObjectAllValuesFrom) e).getProperty();
            return df.getOWLObjectAllValuesFrom(property, map.get(filler));
        } else if ((e.getClassExpressionType() == ClassExpressionType.OBJECT_SOME_VALUES_FROM)) {
            OWLClassExpression filler = ((OWLQuantifiedRestriction<OWLClassExpression>) e).getFiller();
            OWLObjectPropertyExpression property = ((OWLObjectSomeValuesFrom) e).getProperty();
            return df.getOWLObjectSomeValuesFrom(property, map.get(filler));
        } else {
            throw new InvalidParameterException("The expression " + e + " can not be transformed.");
        }
    }
}
