package www.ontologyutils.refinement;

import java.util.List;
import java.util.stream.*;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.refinement.Covers.Cover;
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

    private static class Visitor implements OWLClassExpressionVisitorEx<Stream<OWLClassExpression>> {
        private final Cover way;
        private final Cover back;
        private final int flags;
        private Visitor reverse;

        public Visitor(final Cover way, final Cover back, final int flags) {
            this.way = way;
            this.back = back;
            this.flags = flags;
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
            return reverse.refine(operand)
                    .map(c -> (flags & FLAG_NNF_STRICT) != 0
                            ? c.getComplementNNF()
                            : c.getObjectComplementOf());
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
            return Stream.concat(
                    refine(filler).map(c -> df.getOWLObjectAllValuesFrom(property, c)),
                    reverse.refine(property).map(r -> df.getOWLObjectAllValuesFrom(r, filler)));
        }

        @Override
        public Stream<OWLClassExpression> visit(final OWLObjectSomeValuesFrom concept) {
            final var df = Ontology.getDefaultDataFactory();
            final var filler = concept.getFiller();
            final var property = concept.getProperty();
            return Stream.concat(
                    refine(filler).map(c -> df.getOWLObjectSomeValuesFrom(property, c)),
                    refine(property).map(r -> df.getOWLObjectSomeValuesFrom(r, filler)));
        }

        @Override
        public Stream<OWLClassExpression> visit(final OWLObjectHasSelf concept) {
            if ((flags & FLAG_ALC_STRICT) != 0) {
                throw new IllegalArgumentException("The concept " + concept + " is not an ALC concept.");
            }
            final var df = Ontology.getDefaultDataFactory();
            final var property = concept.getProperty();
            return refine(property).map(r -> df.getOWLObjectHasSelf(r));
        }

        @Override
        public Stream<OWLClassExpression> visit(final OWLObjectMaxCardinality concept) {
            if ((flags & FLAG_ALC_STRICT) != 0) {
                throw new IllegalArgumentException("The concept " + concept + " is not an ALC concept.");
            }
            final var df = Ontology.getDefaultDataFactory();
            final var number = concept.getCardinality();
            final var filler = concept.getFiller();
            final var property = concept.getProperty();
            return Stream.concat(
                    reverse.refine(filler).map(c -> df.getOWLObjectMaxCardinality(number, property, c)),
                    Stream.concat(
                            reverse.refine(property).map(r -> df.getOWLObjectMaxCardinality(number, r, filler)),
                            way.apply(number).map(n -> df.getOWLObjectMaxCardinality(n, property, filler))));
        }

        @Override
        public Stream<OWLClassExpression> visit(final OWLObjectMinCardinality concept) {
            if ((flags & FLAG_ALC_STRICT) != 0) {
                throw new IllegalArgumentException("The concept " + concept + " is not an ALC concept.");
            }
            final var df = Ontology.getDefaultDataFactory();
            final var number = concept.getCardinality();
            final var filler = concept.getFiller();
            final var property = concept.getProperty();
            return Stream.concat(
                    refine(filler).map(c -> df.getOWLObjectMinCardinality(number, property, c)),
                    Stream.concat(
                            refine(property).map(r -> df.getOWLObjectMinCardinality(number, r, filler)),
                            back.apply(number).map(n -> df.getOWLObjectMinCardinality(n, property, filler))));
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

        public Stream<OWLClassExpression> refine(final OWLClassExpression concept) throws IllegalArgumentException {
            // Since all rules include {@code way.apply(concept)} we perform this operation
            // here.
            return Stream.concat(way.apply(concept), concept.accept(this)).distinct();
        }

        public Stream<OWLObjectPropertyExpression> refine(final OWLObjectPropertyExpression role) {
            if ((flags & FLAG_ALC_STRICT) != 0) {
                return Stream.of(role);
            } else {
                return way.apply(role);
            }
        }
    }

    private final Visitor visitor;

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
    public RefinementOperator(final Cover way, final Cover back, final int flags) {
        visitor = new Visitor(way, back, flags);
        visitor.reverse = new Visitor(back, way, flags);
        visitor.reverse.reverse = visitor;
    }

    public RefinementOperator(final Cover way, final Cover back) {
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
        return visitor.refine(concept);
    }

    /**
     * Apply refinement to a role. This is equivalent to simply applying the way
     * cover.
     *
     * @param role
     *            The role that should be refined.
     * @return A stream of all refinements of {@code role} using the covers.
     */
    public Stream<OWLObjectPropertyExpression> refine(final OWLObjectPropertyExpression role) {
        return visitor.refine(role);
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
        return visitor.reverse.refine(concept);
    }

    /**
     * Apply reverse refinement to a role. This is equivalent to simply applying the
     * way cover.
     *
     * @param role
     *            The role that should be refined.
     * @return A stream of all refinements of {@code role} using the covers.
     */
    public Stream<OWLObjectPropertyExpression> refineReverse(final OWLObjectPropertyExpression role) {
        return visitor.reverse.refine(role);
    }
}
