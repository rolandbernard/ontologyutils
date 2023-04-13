package www.ontologyutils.refinement;

import java.util.List;
import java.util.function.Function;
import java.util.stream.*;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.Ontology;

/**
 * Implements a abstract refinement operator that given the upward and downward
 * cover can be used for generalization and specialization operators. Flags are
 * provided for conforming closer to the definitions presented in the paper.
 *
 * The implementation is based on the approach presented in Troquard, Nicolas,
 * et al. "Repairing ontologies via axiom weakening." Proceedings of the AAAI
 * Conference on Artificial Intelligence. Vol. 32. No. 1. 2018. Table 1.
 */
public class RefinementOperator {
    public static final int FLAG_NON_STRICT = 0;
    public static final int FLAG_ALC_STRICT = 1 << 0;
    public static final int FLAG_NNF_STRICT = 1 << 1;

    private class Visitor implements OWLClassExpressionVisitorEx<Stream<OWLClassExpression>> {
        private final Function<OWLClassExpression, Stream<OWLClassExpression>> way;

        public Visitor(final Function<OWLClassExpression, Stream<OWLClassExpression>> way) {
            this.way = way;
        }

        @Override
        public Stream<OWLClassExpression> visit(final OWLClass concept) {
            return Stream.of();
        }

        @Override
        public Stream<OWLClassExpression> visit(final OWLObjectComplementOf concept) {
            final var operand = concept.getOperand();
            if ((flags & FLAG_NNF_STRICT) != 0 && operand.getClassExpressionType() != ClassExpressionType.OWL_CLASS) {
                throw new IllegalArgumentException("The concept " + concept + " is not in NNF.");
            }
            return refineReverse(operand)
                    .map(c -> (flags & FLAG_NNF_STRICT) != 0
                            ? concept.getComplementNNF()
                            : concept.getObjectComplementOf());
        }

        /**
         * @param <T>
         * @param list
         * @param idx
         * @param value
         * @return A stream that contains all elements in {@code list} but the one at
         *         {@code idx} which is replace by {@code value}.
         */
        private <T> Stream<T> replaceInList(final List<T> list, final int idx, final T value) {
            return IntStream.range(0, list.size()).mapToObj(j -> j)
                    .map(j -> idx == j ? value : list.get(j));
        }

        @Override
        public Stream<OWLClassExpression> visit(final OWLObjectIntersectionOf concept) {
            final var df = Ontology.getDefaultDataFactory();
            final var conjuncts = concept.getOperandsAsList();
            if ((flags & FLAG_ALC_STRICT) != 0 && conjuncts.size() != 2) {
                throw new IllegalArgumentException("The concept " + concept + " is not an ALC concept.");
            }
            return IntStream.range(0, conjuncts.size()).mapToObj(i -> i)
                    .flatMap(i -> refine(conjuncts.get(i))
                            .map(refined -> df.getOWLObjectIntersectionOf(replaceInList(conjuncts, i, refined))));
        }

        @Override
        public Stream<OWLClassExpression> visit(final OWLObjectUnionOf concept) {
            final var df = Ontology.getDefaultDataFactory();
            final var disjuncts = concept.getOperandsAsList();
            if ((flags & FLAG_ALC_STRICT) != 0 && disjuncts.size() != 2) {
                throw new IllegalArgumentException("The concept " + concept + " is not an ALC concept.");
            }
            return IntStream.range(0, disjuncts.size()).mapToObj(i -> i)
                    .flatMap(i -> refine(disjuncts.get(i))
                            .map(refined -> df.getOWLObjectUnionOf(replaceInList(disjuncts, i, refined))));
        }

        @Override
        public Stream<OWLClassExpression> visit(final OWLObjectAllValuesFrom concept) {
            final var df = Ontology.getDefaultDataFactory();
            final var filler = concept.getFiller();
            final var property = concept.getProperty();
            return refine(filler)
                    .map(c -> df.getOWLObjectAllValuesFrom(property, c));
        }

        @Override
        public Stream<OWLClassExpression> visit(final OWLObjectSomeValuesFrom concept) {
            final var df = Ontology.getDefaultDataFactory();
            final var filler = concept.getFiller();
            final var property = concept.getProperty();
            return refine(filler)
                    .map(c -> df.getOWLObjectSomeValuesFrom(property, c));
        }

        @Override
        public <T> Stream<OWLClassExpression> doDefault(final T obj) throws IllegalArgumentException {
            final var concept = (OWLClassExpression) obj;
            if ((flags & FLAG_ALC_STRICT) != 0) {
                throw new IllegalArgumentException("The concept " + concept + " is not an ALC concept.");
            } else {
                return Stream.of();
            }
        }

        /**
         * @param concept
         *            The concept for which to compute all refinements.
         * @return All refinements produced for {@code concept}.
         */
        public Stream<OWLClassExpression> refineVisit(final OWLClassExpression concept) {
            // Since all rules include {@code way.apply(concept)} we perform this operation
            // here.
            return Stream.concat(way.apply(concept), concept.accept(this)).distinct();
        }
    }

    private final int flags;
    private final Visitor visitor;
    private final Visitor visitorReverse;

    /**
     * Create a new refinement operator.
     *
     * @param way
     * @param back
     * @param flags
     *            Bitset containing flags for restricting the implementation. If
     *            FLAG_ALC_STRICT is set, an exception will be raised if a concept
     *            is not valid in ALC. If FLAG_NNF_STRICT is set, the input must
     *            be in NNF and the output will also be in NNF.
     */
    public RefinementOperator(final Function<OWLClassExpression, Stream<OWLClassExpression>> way,
            final Function<OWLClassExpression, Stream<OWLClassExpression>> back, final int flags) {
        this.flags = flags;
        visitor = new Visitor(way);
        visitorReverse = new Visitor(back);
    }

    public RefinementOperator(final Function<OWLClassExpression, Stream<OWLClassExpression>> way,
            final Function<OWLClassExpression, Stream<OWLClassExpression>> back) {
        this(way, back, FLAG_NON_STRICT);
    }

    /**
     * If this is the generalization operator, then this will return all
     * generalization of {@code concept}.
     * If this is the specialization operator, then this will return all
     * specialization of {@code concept}.
     *
     * @param concept
     *            The concept to which the refinement operator should be
     *            applied.
     * @return A stream with all refinements of {@code concept}.
     * @throws IllegalArgumentException
     *             If the axioms in this ontology are not
     *             supported by the current flags.
     */
    public Stream<OWLClassExpression> refine(final OWLClassExpression concept) throws IllegalArgumentException {
        return visitor.refineVisit(concept);
    }

    /**
     * This applies the refinement operator with swapped way and back functions.
     * If this is the generalization operator, then this will return all
     * specializations of {@code concept}.
     * If this is the specialization operator, then this will return all
     * generalization of {@code concept}.
     *
     * @param concept
     *            The concept to which the refinement operator should be
     *            applied.
     * @return A stream with all refinements of {@code concept}.
     * @throws IllegalArgumentException
     *             If the axioms in this ontology are not
     *             supported by the current flags.
     */
    public Stream<OWLClassExpression> refineReverse(final OWLClassExpression concept) throws IllegalArgumentException {
        return visitorReverse.refineVisit(concept);
    }
}
