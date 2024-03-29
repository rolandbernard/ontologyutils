package www.ontologyutils.normalization;

import java.util.*;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.*;

/**
 * Normalization that converts every axiom in the TBox of the ontology to
 * subclass axioms.
 */
public class TBoxNormalization implements OntologyModification {
    /**
     * Visitor class used for converting the axioms. Only the
     * {@code OWLDisjointUnionAxiom} axioms must be handles specially, other axioms
     * implement either {@code OWLSubClassOfAxiomSetShortCut} or
     * {@code OWLSubClassOfAxiomShortCut}.
     */
    private static class Visitor implements OWLAxiomVisitorEx<Collection<OWLSubClassOfAxiom>> {
        @Override
        public Collection<OWLSubClassOfAxiom> visit(OWLDisjointUnionAxiom axiom) {
            // Since OWLDisjointUnionAxiom does not implement OWLSubClassOfAxiomSetShortCut
            // directly, we must first split the axiom into a disjoint class and equivalent
            // class axioms. Then we split the result axioms into subclass axioms.
            var disjointClasses = axiom.getOWLDisjointClassesAxiom();
            var equivalentClasses = axiom.getOWLEquivalentClassesAxiom();
            var axioms = disjointClasses.asOWLSubClassOfAxioms();
            axioms.addAll(equivalentClasses.asOWLSubClassOfAxioms());
            return axioms;
        }

        @Override
        public <T> Collection<OWLSubClassOfAxiom> doDefault(T axiom) throws IllegalArgumentException {
            if (axiom instanceof OWLSubClassOfAxiomSetShortCut ax) {
                return ax.asOWLSubClassOfAxioms();
            } else if (axiom instanceof OWLSubClassOfAxiomShortCut ax) {
                return Collections.singleton(ax.asOWLSubClassOfAxiom());
            } else {
                throw new IllegalArgumentException("TBox normalization does not support axiom " + axiom);
            }
        }
    }

    private Visitor visitor;

    /**
     * Create a new TBox normalization object.
     */
    public TBoxNormalization() {
        visitor = new Visitor();
    }

    /**
     * @param axiom
     *            The axiom that should be split into subclasses.
     * @return A number of subclass axioms that together are equivalent to
     *         {@code axiom} in every ontology.
     */
    public Stream<OWLSubClassOfAxiom> asSubclassOfAxioms(OWLAxiom axiom) {
        return axiom.accept(visitor).stream();
    }

    @Override
    public void apply(Ontology ontology) throws IllegalArgumentException {
        var tBox = Utils.toList(ontology.tboxAxioms().filter(axiom -> !axiom.isOfType(AxiomType.SUBCLASS_OF)));
        for (var axiom : tBox) {
            ontology.replaceAxiom(axiom, asSubclassOfAxioms(axiom));
        }
    }
}
