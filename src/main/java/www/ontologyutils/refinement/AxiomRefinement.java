package www.ontologyutils.refinement;

import java.util.stream.Stream;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.*;

/**
 * Abstract base class for axiom weakener and axiom strengthener. Most of the
 * functionality is here, only some axiom types require more careful
 * considerations.
 */
public abstract class AxiomRefinement implements AutoCloseable {
    public static final AxiomType<?>[] SUPPORTED_AXIOM_TYPES = new AxiomType<?>[] {
            AxiomType.SUBCLASS_OF, AxiomType.CLASS_ASSERTION
    };

    protected static class Visitor implements OWLAxiomVisitorEx<Stream<OWLAxiom>> {
        private final RefinementOperator up;
        private final RefinementOperator down;

        public Visitor(final RefinementOperator up, final RefinementOperator down) {
            this.up = up;
            this.down = down;
        }

        @Override
        public Stream<OWLAxiom> visit(final OWLSubClassOfAxiom axiom) {
            final var df = Ontology.getDefaultDataFactory();
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
            final var df = Ontology.getDefaultDataFactory();
            final var concept = axiom.getClassExpression();
            final var individual = axiom.getIndividual();
            return up.refine(concept)
                    .map(newConcept -> df.getOWLClassAssertionAxiom(newConcept, individual));
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
