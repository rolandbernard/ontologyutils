package www.ontologyutils.normalization;

import java.util.*;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.*;

/**
 * Normalization that converts every axiom in the TBox of the ontology to
 * subclass axioms.
 */
public class TBoxSubclassOfNormalization implements OntologyModification {
    /**
     * Visitor class used for converting the axioms. Only the
     * {@code OWLDisjointUnionAxiom} axioms must be handles specially, other axioms
     * implement either {@code OWLSubClassOfAxiomSetShortCut} or
     * {@code OWLSubClassOfAxiomShortCut}.
     */
    private static class Visitor implements OWLAxiomVisitorEx<Collection<OWLSubClassOfAxiom>> {
        @Override
        public Collection<OWLSubClassOfAxiom> visit(final OWLDisjointUnionAxiom axiom) {
            // Since OWLDisjointUnionAxiom does not implement OWLSubClassOfAxiomSetShortCut
            // directly, we must first split the axiom into a disjoint class and equivalent
            // class axioms. Then we split the result axioms into subclass axioms.
            final OWLDisjointClassesAxiom disjointClasses = axiom.getOWLDisjointClassesAxiom();
            final OWLEquivalentClassesAxiom equivalentClasses = axiom.getOWLEquivalentClassesAxiom();
            final Collection<OWLSubClassOfAxiom> axioms = disjointClasses.asOWLSubClassOfAxioms();
            axioms.addAll(equivalentClasses.asOWLSubClassOfAxioms());
            return axioms;
        }

        @Override
        public <T> Collection<OWLSubClassOfAxiom> doDefault(final T axiom) throws IllegalArgumentException {
            if (axiom instanceof OWLSubClassOfAxiomSetShortCut) {
                return ((OWLSubClassOfAxiomSetShortCut) axiom).asOWLSubClassOfAxioms();
            } else if (axiom instanceof OWLSubClassOfAxiomShortCut) {
                return Collections.singleton(((OWLSubClassOfAxiomShortCut) axiom).asOWLSubClassOfAxiom());
            } else {
                final OWLAxiom ax = (OWLAxiom) axiom;
                throw new IllegalArgumentException("The axiom " + ax + " of type " + ax.getAxiomType()
                        + " could not be converted into subclass axioms.");
            }
        }
    }

    private final Visitor visitor;

    public TBoxSubclassOfNormalization() {
        visitor = new Visitor();
    }

    /**
     * @param axiom
     *            The axiom that should be split into subclasses.
     * @return A number of subclass axioms that together are equivalent to
     *         {@code axiom} in every ontology.
     */
    public Stream<OWLSubClassOfAxiom> asSubclassOfAxioms(final OWLAxiom axiom) {
        return axiom.accept(visitor).stream();
    }

    @Override
    public void apply(final Ontology ontology) throws IllegalArgumentException {
        final List<OWLAxiom> tBox = ontology.axioms()
                .filter(axiom -> !axiom.isOfType(AxiomType.SUBCLASS_OF))
                .filter(axiom -> axiom.isOfType(AxiomType.TBoxAxiomTypes)).toList();
        for (final OWLAxiom axiom : tBox) {
            ontology.replaceAxiom(axiom, asSubclassOfAxioms(axiom));
        }
    }
}
