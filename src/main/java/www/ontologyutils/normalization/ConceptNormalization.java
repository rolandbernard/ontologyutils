package www.ontologyutils.normalization;

import java.util.List;
import java.util.function.*;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.*;

/**
 * Normalization that converts every concept in the ontology to ones that are
 * part of SROIQ. If {@code binaryOperators} is true, all Nary operators are
 * converted to binary ones.
 */
public class ConceptNormalization implements OntologyModification {
    /**
     * Visitor class used for converting the axioms.
     */
    private static class AxiomVisitor implements OWLAxiomVisitorEx<OWLAxiom> {
        protected final OWLDataFactory df;
        public final ConceptVisitor visitor;

        public AxiomVisitor(final ConceptVisitor visitor) {
            df = Ontology.getDefaultDataFactory();
            this.visitor = visitor;
        }

        @Override
        public OWLAxiom visit(OWLSubClassOfAxiom axiom) {
            return df.getOWLSubClassOfAxiom(
                    axiom.getSubClass().accept(visitor),
                    axiom.getSuperClass().accept(visitor));
        }

        @Override
        public OWLAxiom visit(OWLDisjointClassesAxiom axiom) {
            return df.getOWLDisjointClassesAxiom(
                    axiom.classExpressions().map(concept -> concept.accept(visitor)));
        }

        @Override
        public OWLAxiom visit(OWLObjectPropertyDomainAxiom axiom) {
            return df.getOWLObjectPropertyDomainAxiom(
                    axiom.getProperty(),
                    axiom.getDomain().accept(visitor));
        }

        @Override
        public OWLAxiom visit(OWLObjectPropertyRangeAxiom axiom) {
            return df.getOWLObjectPropertyRangeAxiom(
                    axiom.getProperty(),
                    axiom.getRange().accept(visitor));
        }

        @Override
        public OWLAxiom visit(OWLDisjointUnionAxiom axiom) {
            return df.getOWLDisjointUnionAxiom(
                    axiom.getOWLClass(),
                    axiom.classExpressions().map(concept -> concept.accept(visitor)));
        }

        @Override
        public OWLAxiom visit(OWLClassAssertionAxiom axiom) {
            return df.getOWLClassAssertionAxiom(
                    axiom.getClassExpression().accept(visitor),
                    axiom.getIndividual());
        }

        @Override
        public OWLAxiom visit(OWLEquivalentClassesAxiom axiom) {
            return df.getOWLDisjointClassesAxiom(
                    axiom.classExpressions().map(concept -> concept.accept(visitor)));
        }

        @Override
        public <T> OWLAxiom doDefault(final T axiom) {
            return (OWLAxiom) axiom;
        }
    }

    /**
     * Visitor class used for converting the concepts.
     */
    private static class ConceptVisitor implements OWLClassExpressionVisitorEx<OWLClassExpression> {
        protected final OWLDataFactory df;
        private final boolean binaryOperators;

        private ConceptVisitor(final boolean binaryOperators) {
            df = Ontology.getDefaultDataFactory();
            this.binaryOperators = binaryOperators;
        }

        @Override
        public OWLClassExpression visit(OWLClass ce) {
            return ce;
        }

        private OWLClassExpression binaryOperator(final List<OWLClassExpression> operands,
                final Function<Stream<OWLClassExpression>, OWLClassExpression> constructor,
                final Supplier<OWLClassExpression> empty) {
            if (binaryOperators && operands.size() != 2) {
                if (operands.size() == 0) {
                    return empty.get();
                } else if (operands.size() == 1) {
                    return operands.get(0).accept(this);
                } else {
                    var result = constructor.apply(operands.stream().limit(2).map(c -> c.accept(this)));
                    for (int i = 2; i < operands.size(); i++) {
                        result = constructor.apply(Stream.of(result, operands.get(i).accept(this)));
                    }
                    return result;
                }
            } else {
                return constructor.apply(operands.stream().map(c -> c.accept(this)));
            }
        }

        @Override
        public OWLClassExpression visit(OWLObjectIntersectionOf ce) {
            final var operands = ce.getOperandsAsList();
            return binaryOperator(operands, ces -> df.getOWLObjectIntersectionOf(ces), () -> df.getOWLThing());
        }

        @Override
        public OWLClassExpression visit(OWLObjectUnionOf ce) {
            final var operands = ce.getOperandsAsList();
            return binaryOperator(operands, ces -> df.getOWLObjectUnionOf(ces), () -> df.getOWLNothing());
        }

        @Override
        public OWLClassExpression visit(OWLObjectComplementOf ce) {
            return df.getOWLObjectComplementOf(ce.getOperand().accept(this));
        }

        @Override
        public OWLClassExpression visit(OWLObjectSomeValuesFrom ce) {
            return df.getOWLObjectSomeValuesFrom(ce.getProperty(), ce.getFiller().accept(this));
        }

        @Override
        public OWLClassExpression visit(OWLObjectAllValuesFrom ce) {
            return df.getOWLObjectAllValuesFrom(ce.getProperty(), ce.getFiller().accept(this));
        }

        @Override
        public OWLClassExpression visit(OWLObjectHasValue ce) {
            return df.getOWLObjectSomeValuesFrom(ce.getProperty(), df.getOWLObjectOneOf(ce.getFiller()));
        }

        @Override
        public OWLClassExpression visit(OWLObjectMinCardinality ce) {
            return df.getOWLObjectMinCardinality(ce.getCardinality(), ce.getProperty(), ce.getFiller().accept(this));
        }

        @Override
        public OWLClassExpression visit(OWLObjectExactCardinality ce) {
            final var filler = ce.getFiller().accept(this);
            return df.getOWLObjectIntersectionOf(
                    df.getOWLObjectMinCardinality(ce.getCardinality(), ce.getProperty(), filler),
                    df.getOWLObjectMaxCardinality(ce.getCardinality(), ce.getProperty(), filler));
        }

        @Override
        public OWLClassExpression visit(OWLObjectMaxCardinality ce) {
            return df.getOWLObjectMaxCardinality(ce.getCardinality(), ce.getProperty(), ce.getFiller().accept(this));
        }

        @Override
        public OWLClassExpression visit(OWLObjectHasSelf ce) {
            return ce;
        }

        @Override
        public OWLClassExpression visit(OWLObjectOneOf ce) {
            return ce;
        }

        @Override
        public <T> OWLClassExpression doDefault(final T axiom) {
            throw new IllegalArgumentException("Unsupported concept type for normalization.");
        }
    }

    private final AxiomVisitor visitor;

    public ConceptNormalization(final boolean binaryOperators) {
        visitor = new AxiomVisitor(new ConceptVisitor(binaryOperators));
    }

    public ConceptNormalization() {
        this(false);
    }

    /**
     * @param axiom
     *            The axiom that should be converted.
     * @return A axiom that contains only SROIQ concepts.
     */
    public OWLAxiom asSroiqAxiom(final OWLAxiom axiom) {
        return axiom.accept(visitor);
    }

    /**
     * @param concept
     *            The concept that should be converted.
     * @return A SROIQ concepts equivalent to {@code concept}.
     */
    public OWLClassExpression asSroiqConcept(final OWLClassExpression concept) {
        return concept.accept(visitor.visitor);
    }

    @Override
    public void apply(final Ontology ontology) throws IllegalArgumentException {
        final var axioms = ontology.axioms().toList();
        for (final var axiom : axioms) {
            ontology.replaceAxiom(axiom, asSroiqAxiom(axiom));
        }
    }
}
