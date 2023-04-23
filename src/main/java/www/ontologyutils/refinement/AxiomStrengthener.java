package www.ontologyutils.refinement;

import java.util.stream.*;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.refinement.Covers.Cover;
import www.ontologyutils.toolbox.Ontology;

/**
 * Implementation that can be used for strengthening an axiom. Must be closed
 * after usage to free up resources used by the inner {@code Covers} object.
 */
public class AxiomStrengthener extends AxiomRefinement {
    private static class Visitor extends AxiomRefinement.Visitor {
        public Visitor(final RefinementOperator up, final RefinementOperator down) {
            super(up, down);
        }

        @Override
        protected OWLAxiom noopAxiom() {
            return df.getOWLSubClassOfAxiom(df.getOWLThing(), df.getOWLNothing());
        }
    }

    private AxiomStrengthener(final Covers covers, final Cover upCover, final Cover downCover) {
        super(new Visitor(new RefinementOperator(downCover, upCover), new RefinementOperator(upCover, downCover)),
                covers);
    }

    private AxiomStrengthener(final Covers covers) {
        this(covers, covers.upCover().cached(), covers.downCover().cached());
    }

    /**
     * Create a new axiom strengthener with the given reference ontology. The
     * reference ontology must contain all RBox axioms of all ontologies the
     * stronger axioms are used in, otherwise the resulting axiom is not guaranteed
     * to satisfy global restrictions on roles.
     *
     * @param refOntology
     *            The reference ontology to use for the up and down covers.
     */
    public AxiomStrengthener(final Ontology refOntology) {
        this(new Covers(refOntology, refOntology.simpleRoles().collect(Collectors.toSet())));
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
        return refineAxioms(axiom);
    }
}
