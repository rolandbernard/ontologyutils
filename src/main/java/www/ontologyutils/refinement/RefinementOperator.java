package www.ontologyutils.refinement;

import java.util.List;
import java.util.function.Function;
import java.util.stream.*;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.Ontology;

public class RefinementOperator {
    public static final int FLAG_NON_STRICT = 0;
    public static final int FLAG_ALC_STRICT = 1 << 0;
    public static final int FLAG_NNF_STRICT = 1 << 1;

    private class Visitor implements OWLClassExpressionVisitorEx<Stream<OWLClassExpression>> {
        private final Function<OWLClassExpression, Stream<OWLClassExpression>> way;

        public Visitor(final Function<OWLClassExpression, Stream<OWLClassExpression>> way) {
            this.way = way;
        }

        public Stream<OWLClassExpression> visit(final OWLClass concept) {
            return Stream.of();
        }

        public Stream<OWLClassExpression> visit(final OWLObjectComplementOf concept) {
            final OWLClassExpression operand = concept.getOperand();
            if ((flags & FLAG_NNF_STRICT) != 0 && operand.getClassExpressionType() != ClassExpressionType.OWL_CLASS) {
                throw new IllegalArgumentException("The concept " + concept + " is not in NNF.");
            }
            return refineReverse(operand)
                    .map(c -> (flags & FLAG_NNF_STRICT) != 0
                            ? concept.getComplementNNF()
                            : concept.getObjectComplementOf());
        }

        private <T> Stream<T> replaceInList(final List<T> list, final int idx, final T value) {
            return IntStream.range(0, list.size()).mapToObj(j -> j)
                    .map(j -> idx == j ? value : list.get(j));
        }

        public Stream<OWLClassExpression> visit(final OWLObjectIntersectionOf concept) {
            final OWLDataFactory df = Ontology.getDefaultDataFactory();
            final List<OWLClassExpression> conjuncts = concept.getOperandsAsList();
            if ((flags & FLAG_ALC_STRICT) != 0 && conjuncts.size() != 2) {
                throw new IllegalArgumentException("The concept " + concept + " is not an ALC concept.");
            }
            return IntStream.range(0, conjuncts.size()).mapToObj(i -> i)
                    .flatMap(i -> refine(conjuncts.get(i))
                            .map(refined -> df.getOWLObjectIntersectionOf(replaceInList(conjuncts, i, refined))));
        }

        public Stream<OWLClassExpression> visit(final OWLObjectUnionOf concept) {
            final OWLDataFactory df = Ontology.getDefaultDataFactory();
            final List<OWLClassExpression> disjuncts = concept.getOperandsAsList();
            if ((flags & FLAG_ALC_STRICT) != 0 && disjuncts.size() != 2) {
                throw new IllegalArgumentException("The concept " + concept + " is not an ALC concept.");
            }
            return IntStream.range(0, disjuncts.size()).mapToObj(i -> i)
                    .flatMap(i -> refine(disjuncts.get(i))
                            .map(refined -> df.getOWLObjectUnionOf(replaceInList(disjuncts, i, refined))));
        }

        public Stream<OWLClassExpression> visit(final OWLObjectAllValuesFrom concept) {
            final OWLDataFactory df = Ontology.getDefaultDataFactory();
            final OWLClassExpression filler = concept.getFiller();
            final OWLObjectPropertyExpression property = concept.getProperty();
            return refine(filler)
                    .map(c -> df.getOWLObjectAllValuesFrom(property, c));
        }

        public Stream<OWLClassExpression> visit(final OWLObjectSomeValuesFrom concept) {
            final OWLDataFactory df = Ontology.getDefaultDataFactory();
            final OWLClassExpression filler = concept.getFiller();
            final OWLObjectPropertyExpression property = concept.getProperty();
            return refine(filler)
                    .map(c -> df.getOWLObjectSomeValuesFrom(property, c));
        }

        public <T> Stream<OWLClassExpression> doDefault(final T obj) throws IllegalArgumentException {
            final OWLClassExpression concept = (OWLClassExpression) obj;
            if ((flags & FLAG_ALC_STRICT) != 0) {
                throw new IllegalArgumentException("The concept " + concept + " is not an ALC concept.");
            } else {
                return Stream.of();
            }
        }

        public Stream<OWLClassExpression> visit(final OWLClassExpression concept) {
            return Stream.concat(way.apply(concept), concept.accept(this)).distinct();
        }
    }

    private final int flags;
    private final Visitor visitor;
    private final Visitor visitorReverse;

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

    public Stream<OWLClassExpression> refine(final OWLClassExpression concept) throws IllegalArgumentException {
        return visitor.visit(concept);
    }

    public Stream<OWLClassExpression> refineReverse(final OWLClassExpression concept) throws IllegalArgumentException {
        return visitorReverse.visit(concept);
    }
}
