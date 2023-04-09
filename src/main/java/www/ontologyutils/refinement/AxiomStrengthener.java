package www.ontologyutils.refinement;

import java.util.stream.Stream;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.Ontology;

/**
 * Implementation that can be used for strengthening an axiom. Must be closed
 * after usage to free up resources used by the inner {@code Covers} object.
 */
public class AxiomStrengthener implements AutoCloseable {
    private class Visitor implements OWLAxiomVisitorEx<Stream<OWLAxiom>> {
        @Override
        public Stream<OWLAxiom> visit(final OWLSubClassOfAxiom axiom) {
            final OWLDataFactory df = Ontology.getDefaultDataFactory();
            final OWLClassExpression subclass = axiom.getSubClass();
            final OWLClassExpression superclass = axiom.getSuperClass();
            return Stream.concat(
                    generalization.refine(subclass)
                            .map(newSubclass -> df.getOWLSubClassOfAxiom(newSubclass, superclass)),
                    specialization.refine(superclass)
                            .map(newSuperclass -> df.getOWLSubClassOfAxiom(subclass, newSuperclass)));
        }

        @Override
        public Stream<OWLAxiom> visit(final OWLClassAssertionAxiom axiom) {
            final OWLDataFactory df = Ontology.getDefaultDataFactory();
            final OWLClassExpression concept = axiom.getClassExpression();
            final OWLIndividual individual = axiom.getIndividual();
            return specialization.refine(concept)
                    .map(newConcept -> df.getOWLClassAssertionAxiom(newConcept, individual));
        }

        @Override
        public <T> Stream<OWLAxiom> doDefault(final T axiom) throws IllegalArgumentException {
            final OWLAxiom ax = (OWLAxiom) axiom;
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
     * Create a new axiom strengthener with the given reference ontology.
     *
     * @param refOntology
     *            The reference ontology to use for the up and down covers.
     */
    public AxiomStrengthener(final Ontology refOntology) {
        visitor = new Visitor();
        covers = new Covers(refOntology);
        generalization = new RefinementOperator(covers::upCover, covers::downCover);
        specialization = new RefinementOperator(covers::downCover, covers::upCover);
    }

    /**
     * Computes all axioms derived by:
     * - for subclass axioms: either generalizing the left hand side or specializing
     * the right hand side.
     * - for assertion axioms: specializing the concept.
     *
     * @param axiom
     *            The axiom for which we want to find stronger axioms.
     * @return A stream of axioms that are all stronger than {@code axiom}.
     */
    public Stream<OWLAxiom> strongerAxioms(final OWLAxiom axiom) {
        return axiom.accept(visitor).distinct();
    }

    @Override
    public void close() {
        covers.close();
    }
}
