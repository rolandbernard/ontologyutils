package www.ontologyutils.normalization;

import java.util.*;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import www.ontologyutils.toolbox.*;

public class TBoxSubclassOfNormalization implements OntologyModification {
    private static class Visitor implements OWLAxiomVisitorEx<Collection<OWLSubClassOfAxiom>> {
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

        public <T> Collection<OWLSubClassOfAxiom> doDefault(final T axiom) {
            if (axiom instanceof OWLSubClassOfAxiomSetShortCut) {
                return ((OWLSubClassOfAxiomSetShortCut) axiom).asOWLSubClassOfAxioms();
            } else if (axiom instanceof OWLSubClassOfAxiomShortCut) {
                return Collections.singleton(((OWLSubClassOfAxiomShortCut) axiom).asOWLSubClassOfAxiom());
            } else {
                final OWLAxiom ax = (OWLAxiom) axiom;
                throw new IllegalArgumentException("The axiom " + ax + " of type " + ax.getAxiomType()
                        + " could not be converted into subclass axioms");
            }
        }
    }

    public Stream<OWLSubClassOfAxiom> asSubclassOfAxioms(final OWLAxiom axiom, final OWLDataFactory df) {
        return axiom.accept(new Visitor()).stream();
    }

    @Override
    public void apply(final Ontology ontology) {
        ontology.getOwlOntology().tboxAxioms(Imports.INCLUDED)
                .filter(axiom -> !axiom.isOfType(AxiomType.SUBCLASS_OF))
                .forEach(axiom -> {
                    ontology.replaceAxiom(axiom, asSubclassOfAxioms(axiom, ontology.getDataFactory()));
                });
    }
}
