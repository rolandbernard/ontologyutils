package www.ontologyutils.refinement;

import java.util.stream.Stream;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.Ontology;

public class AxiomWeakener implements AutoCloseable {
    private class Visitor implements OWLAxiomVisitorEx<Stream<OWLAxiom>> {
        public Stream<OWLAxiom> visit(final OWLSubClassOfAxiom axiom) {
            final OWLDataFactory df = Ontology.getDefaultDataFactory();
            final OWLClassExpression subclass = axiom.getSubClass();
            final OWLClassExpression superclass = axiom.getSuperClass();
            return Stream.concat(
                    specialization.refine(subclass)
                            .map(newSubclass -> df.getOWLSubClassOfAxiom(newSubclass, superclass)),
                    generalization.refine(superclass)
                            .map(newSuperclass -> df.getOWLSubClassOfAxiom(subclass, newSuperclass)));
        }

        public Stream<OWLAxiom> visit(final OWLClassAssertionAxiom axiom) {
            final OWLDataFactory df = Ontology.getDefaultDataFactory();
            final OWLClassExpression concept = axiom.getClassExpression();
            final OWLIndividual individual = axiom.getIndividual();
            return generalization.refine(concept)
                    .map(newConcept -> df.getOWLClassAssertionAxiom(newConcept, individual));
        }

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

    public AxiomWeakener(final Ontology refOntology) {
        visitor = new Visitor();
        covers = new Covers(refOntology);
        generalization = new RefinementOperator(covers::upCover, covers::downCover);
        specialization = new RefinementOperator(covers::downCover, covers::upCover);
    }

    public Stream<OWLAxiom> weakerAxioms(final OWLAxiom axiom) {
        return axiom.accept(visitor).distinct();
    }

    @Override
    public void close() throws Exception {
        covers.close();
    }
}
