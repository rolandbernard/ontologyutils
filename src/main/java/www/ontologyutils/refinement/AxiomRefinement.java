package www.ontologyutils.refinement;

import java.util.stream.Stream;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.*;

/**
 * Abstract base class for axiom weakener and axiom strengthener. Most of the
 * functionality is here, only some axiom types require more careful
 * considerations.
 *
 * The implementation is based on the approach presented in Troquard, Nicolas,
 * et al. "Repairing ontologies via axiom weakening." Proceedings of the AAAI
 * Conference on Artificial Intelligence. Vol. 32. No. 1. 2018. Definition 3.
 *
 * The implementation for SROIQ axioms is based on the approach presented in
 * Confalonieri, R., Galliani, P., Kutz, O., Porello, D., Righetti, G., &
 * Toquard, N. (2020). Towards even more irresistible axiom weakening.
 */
public abstract class AxiomRefinement implements AutoCloseable {
    public static final AxiomType<?>[] SUPPORTED_AXIOM_TYPES = new AxiomType<?>[] {
            AxiomType.SUBCLASS_OF, AxiomType.CLASS_ASSERTION, AxiomType.OBJECT_PROPERTY_ASSERTION,
            AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION, AxiomType.SAME_INDIVIDUAL, AxiomType.DIFFERENT_INDIVIDUALS
    };

    protected static abstract class Visitor implements OWLAxiomVisitorEx<Stream<OWLAxiom>> {
        protected final OWLDataFactory df;
        protected final RefinementOperator up;
        protected final RefinementOperator down;

        public Visitor(final RefinementOperator up, final RefinementOperator down) {
            df = Ontology.getDefaultDataFactory();
            this.up = up;
            this.down = down;
        }

        protected abstract OWLAxiom noopAxiom();

        @Override
        public Stream<OWLAxiom> visit(final OWLSubClassOfAxiom axiom) {
            final var subclass = axiom.getSubClass();
            final var superclass = axiom.getSuperClass();
            return Stream.concat(
                    down.refine(subclass)
                            .map(newSubclass -> df.getOWLSubClassOfAxiom(newSubclass, superclass)),
                    up.refine(superclass)
                            .map(newSuperclass -> df.getOWLSubClassOfAxiom(subclass, newSuperclass)));
        }

        @Override
        public Stream<OWLAxiom> visit(final OWLClassAssertionAxiom axiom) {
            final var concept = axiom.getClassExpression();
            final var individual = axiom.getIndividual();
            return up.refine(concept)
                    .map(newConcept -> df.getOWLClassAssertionAxiom(newConcept, individual));
        }

        @Override
        public Stream<OWLAxiom> visit(final OWLObjectPropertyAssertionAxiom axiom) {
            final var subject = axiom.getSubject();
            final var role = axiom.getProperty();
            final var object = axiom.getObject();
            return Stream.concat(
                    up.refine(role)
                            .map(newRole -> df.getOWLObjectPropertyAssertionAxiom(newRole, subject, object)),
                    Stream.of(noopAxiom()));
        }

        @Override
        public Stream<OWLAxiom> visit(final OWLNegativeObjectPropertyAssertionAxiom axiom) {
            final var subject = axiom.getSubject();
            final var role = axiom.getProperty();
            final var object = axiom.getObject();
            return Stream.concat(
                    down.refine(role)
                            .map(newRole -> df.getOWLNegativeObjectPropertyAssertionAxiom(newRole, subject, object)),
                    Stream.of(noopAxiom()));
        }

        @Override
        public Stream<OWLAxiom> visit(final OWLSameIndividualAxiom axiom) {
            return Stream.of(axiom, noopAxiom());
        }

        @Override
        public Stream<OWLAxiom> visit(final OWLDifferentIndividualsAxiom axiom) {
            return Stream.of(axiom, noopAxiom());
        }

        @Override
        public <T> Stream<OWLAxiom> doDefault(final T axiom) throws IllegalArgumentException {
            final var ax = (OWLAxiom) axiom;
            throw new IllegalArgumentException(
                    "The axiom " + ax + " of type " + ax.getAxiomType()
                            + " is not supported for axiom weakening.");
        }
    }

    private final Visitor visitor;
    private final Covers covers;

    protected AxiomRefinement(final Visitor visitor, final Covers covers) {
        this.visitor = visitor;
        this.covers = covers;
    }

    /**
     * Computes all axioms derived by from {@code axiom} using the refinement
     * operators.
     *
     * @param axiom
     *            The axiom for which we want to find weaker/stronger axioms.
     * @return A stream of axioms that are all weaker/stronger than {@code axiom}.
     */
    public Stream<OWLAxiom> refineAxioms(final OWLAxiom axiom) {
        return axiom.accept(visitor).distinct();
    }

    @Override
    public void close() {
        covers.close();
    }
}
