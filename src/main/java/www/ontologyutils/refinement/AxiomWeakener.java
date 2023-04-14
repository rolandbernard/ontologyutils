package www.ontologyutils.refinement;

import java.util.stream.Stream;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.*;

/**
 * Implementation that can be used for weakening an axiom. Must be closed
 * after usage to free up resources used by the inner {@code Covers} object.
 *
 * The implementation is based on the approach presented in Troquard, Nicolas,
 * et al. "Repairing ontologies via axiom weakening." Proceedings of the AAAI
 * Conference on Artificial Intelligence. Vol. 32. No. 1. 2018. Definition 19.
 */
public class AxiomWeakener implements AutoCloseable {
    private class Visitor implements OWLAxiomVisitorEx<Stream<OWLAxiom>> {
        @Override
        public Stream<OWLAxiom> visit(final OWLSubClassOfAxiom axiom) {
            final var df = Ontology.getDefaultDataFactory();
            final var subclass = axiom.getSubClass();
            final var superclass = axiom.getSuperClass();
            return Stream.concat(
                    specialization.refine(subclass)
                            .map(newSubclass -> df.getOWLSubClassOfAxiom(newSubclass, superclass)),
                    generalization.refine(superclass)
                            .map(newSuperclass -> df.getOWLSubClassOfAxiom(subclass, newSuperclass)));
        }

        @Override
        public Stream<OWLAxiom> visit(final OWLClassAssertionAxiom axiom) {
            final var df = Ontology.getDefaultDataFactory();
            final var concept = axiom.getClassExpression();
            final var individual = axiom.getIndividual();
            return generalization.refine(concept)
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
    private final RefinementOperator generalization;
    private final RefinementOperator specialization;

    /**
     * Create a new axiom weakener with the given reference ontology.
     *
     * @param refOntology
     *            The reference ontology to use for the up and down covers.
     */
    public AxiomWeakener(final Ontology refOntology) {
        visitor = new Visitor();
        covers = new Covers(refOntology);
        final var upCover = LruCache.wrapStreamFunction(covers::upCover);
        final var downCover = LruCache.wrapStreamFunction(covers::downCover);
        generalization = new RefinementOperator(upCover, downCover);
        specialization = new RefinementOperator(downCover, upCover);
    }

    /**
     * Computes all axioms derived by:
     * - for subclass axioms: either specializing the left hand side or generalizing
     * the right hand side.
     * - for assertion axioms: generalizing the concept.
     *
     * @param axiom
     *            The axiom for which we want to find weaker axioms.
     * @return A stream of axioms that are all weaker than {@code axiom}.
     */
    public Stream<OWLAxiom> weakerAxioms(final OWLAxiom axiom) {
        return axiom.accept(visitor).distinct();
    }

    @Override
    public void close() {
        covers.close();
    }
}
