package www.ontologyutils.refinement;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.refinement.Covers.Cover;
import www.ontologyutils.toolbox.*;

/**
 * Implementation that can be used for weakening an axiom. Must be closed
 * after usage to free up resources used by the inner {@code Covers} object.
 *
 * The implementation is based on the approach presented in Troquard, Nicolas,
 * et al. "Repairing ontologies via axiom weakening." Proceedings of the AAAI
 * Conference on Artificial Intelligence. Vol. 32. No. 1. 2018. Definition 19.
 */
public class AxiomWeakener extends AxiomRefinement {
    private static class Visitor extends AxiomRefinement.Visitor {
        public Visitor(final RefinementOperator up, final RefinementOperator down,
                final Set<OWLObjectProperty> simpleRoles) {
            super(up, down, simpleRoles);
        }

        @Override
        protected OWLAxiom noopAxiom() {
            return df.getOWLSubClassOfAxiom(df.getOWLNothing(), df.getOWLThing());
        }
    }

    private AxiomWeakener(final Covers covers, final Cover upCover, final Cover downCover,
            final Set<OWLObjectProperty> simpleRoles) {
        super(new Visitor(new RefinementOperator(upCover, downCover), new RefinementOperator(downCover, upCover),
                simpleRoles), covers);
    }

    private AxiomWeakener(final Covers covers, final Set<OWLObjectProperty> simpleRoles) {
        this(covers, covers.upCover().cached(), covers.downCover().cached(), simpleRoles);
    }

    /**
     * Create a new axiom weakener with the given reference ontology. To maintain
     * global restrictions on roles, all roles in {@code simpleRoles} must be simple
     * in all ontologies the weakened axioms are used in.
     *
     * @param refOntology
     *            The reference ontology to use for the up and down covers.
     * @param simpleRoles
     *            The roles that are guaranteed to be simple.
     */
    public AxiomWeakener(final Ontology refOntology, final Set<OWLObjectProperty> simpleRoles) {
        this(new Covers(refOntology, simpleRoles), simpleRoles);
    }

    /**
     * Create a new axiom weakener with the given reference ontology.
     *
     * @param refOntology
     *            The reference ontology to use for the up and down covers.
     * @param fullOntology
     *            The maximal ontology in which the weaker axioms will be
     *            used in.
     */
    public AxiomWeakener(final Ontology refOntology, final Ontology fullOntology) {
        this(refOntology, fullOntology.simpleRoles().collect(Collectors.toSet()));
    }

    /**
     * Create a new axiom weakener with the given reference ontology.The reference
     * ontology must contain all RBox axioms of all ontologies the weaker axioms
     * are used in, otherwise the resulting axiom is not guaranteed to satisfy
     * global restrictions on roles.
     *
     * @param refOntology
     *            The reference ontology to use for the up and down covers.
     */
    public AxiomWeakener(final Ontology refOntology) {
        this(refOntology, refOntology);
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
        return refineAxioms(axiom);
    }
}
