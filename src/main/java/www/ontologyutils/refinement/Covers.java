package www.ontologyutils.refinement;

import java.util.Set;
import java.util.stream.*;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import www.ontologyutils.toolbox.Ontology;

/**
 * Implements the upward and downward cover operations. This object must be
 * closed after use to free up all resources associated with the internal
 * {@code OWLReasoner} and {@code OWLOntology}.
 *
 * The implementation is based on the approach presented in Troquard, Nicolas,
 * et al. "Repairing ontologies via axiom weakening." Proceedings of the AAAI
 * Conference on Artificial Intelligence. Vol. 32. No. 1. 2018. Definition 3.
 */
public class Covers implements AutoCloseable {
    public final Ontology refOntology;
    public final Set<OWLClassExpression> subConcepts;
    public OWLReasoner reasoner;

    /**
     * Creates a new {@code Cover} object for the given reference object.
     *
     * @param refOntology
     *            The ontology used for entailment check.
     */
    public Covers(final Ontology refOntology) {
        this.refOntology = refOntology;
        this.reasoner = refOntology.getOwlReasoner();
        this.subConcepts = refOntology.subConcepts().collect(Collectors.toSet());
        final var df = Ontology.getDefaultDataFactory();
        this.subConcepts.add(df.getOWLThing());
        this.subConcepts.add(df.getOWLNothing());
    }

    /**
     * @param subclass
     * @param superclass
     * @return True iff the reference ontology of this cover entails that
     *         {@code subclass} is a subclass of {@code superclass}.
     */
    private boolean isSubclass(final OWLClassExpression subclass, final OWLClassExpression superclass) {
        final var df = Ontology.getDefaultDataFactory();
        final var testAxiom = df.getOWLSubClassOfAxiom(subclass, superclass);
        return reasoner.isEntailed(testAxiom);
    }

    /**
     * For this function, a class A is a string subclass of B iff A isSubclassOf B
     * and not B isSubclassOf A.
     *
     * @param subclass
     * @param superclass
     * @return True iff the reference ontology of this cover entails that
     *         {@code subclass} is a strict subclass of {@code superclass}.
     */
    private boolean isStrictSubclass(final OWLClassExpression subclass, final OWLClassExpression superclass) {
        return isSubclass(subclass, superclass) && !isSubclass(superclass, subclass);
    }

    /**
     * @param concept
     * @param candidate
     * @return True iff {@code candidate} is in the upward cover of {@code concept}.
     */
    private boolean isInUpCover(final OWLClassExpression concept, final OWLClassExpression candidate) {
        if (!subConcepts.contains(candidate) || !isSubclass(concept, candidate)) {
            return false;
        } else {
            return !subConcepts.stream().parallel()
                    .anyMatch(other -> isStrictSubclass(concept, other) && isStrictSubclass(other, candidate));
        }
    }

    /**
     * @param concept
     * @return All concepts that are in the upward cover of {@code concept}.
     */
    public Stream<OWLClassExpression> upCover(final OWLClassExpression concept) {
        return subConcepts.stream().parallel()
                .filter(candidate -> isInUpCover(concept, candidate));
    }

    /**
     * @param concept
     * @param candidate
     * @return True iff {@code candidate} is in the downward cover of
     *         {@code concept}.
     */
    private boolean isInDownCover(final OWLClassExpression concept, final OWLClassExpression candidate) {
        if (!subConcepts.contains(candidate) || !isSubclass(candidate, concept)) {
            return false;
        } else {
            return !subConcepts.stream().parallel()
                    .anyMatch(other -> isStrictSubclass(candidate, other) && isStrictSubclass(other, concept));
        }
    }

    /**
     * @param concept
     * @return All concepts that are in the downward cover of {@code concept}.
     */
    public Stream<OWLClassExpression> downCover(final OWLClassExpression concept) {
        return subConcepts.stream().parallel()
                .filter(candidate -> isInDownCover(concept, candidate));
    }

    @Override
    public void close() {
        if (reasoner != null) {
            refOntology.disposeOwlReasoner(reasoner);
            reasoner = null;
        }
    }
}
