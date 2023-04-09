package www.ontologyutils.refinement;

import java.util.Set;
import java.util.stream.*;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import www.ontologyutils.toolbox.Ontology;

public class Covers implements AutoCloseable {
    public final Ontology refOntology;
    public final Set<OWLClassExpression> subConcepts;
    public OWLReasoner reasoner;

    public Covers(final Ontology refOntology) {
        this.refOntology = refOntology;
        this.reasoner = refOntology.getOwlReasoner();
        this.subConcepts = refOntology.subConcepts().collect(Collectors.toSet());
        final OWLDataFactory df = refOntology.getDataFactory();
        this.subConcepts.add(df.getOWLThing());
        this.subConcepts.add(df.getOWLNothing());
    }

    private boolean isSubclass(final OWLClassExpression subclass, final OWLClassExpression superclass) {
        final OWLDataFactory df = refOntology.getDataFactory();
        final OWLSubClassOfAxiom testAxiom = df.getOWLSubClassOfAxiom(subclass, superclass);
        return reasoner.isEntailed(testAxiom);
    }

    private boolean isStrictSubclass(final OWLClassExpression subclass, final OWLClassExpression superclass) {
        return isSubclass(subclass, superclass) && !isSubclass(superclass, subclass);
    }

    private boolean isInUpCover(final OWLClassExpression concept, final OWLClassExpression candidate) {
        if (!subConcepts.contains(candidate) || !isSubclass(concept, candidate)) {
            return false;
        } else {
            return !subConcepts.stream().parallel()
                    .anyMatch(other -> isStrictSubclass(concept, other) && isStrictSubclass(other, candidate));
        }
    }

    public Stream<OWLClassExpression> upCover(final OWLClassExpression concept) {
        return subConcepts.stream().parallel()
                .filter(candidate -> isInUpCover(concept, candidate));
    }

    private boolean isInDownCover(final OWLClassExpression concept, final OWLClassExpression candidate) {
        if (!subConcepts.contains(candidate) || !isSubclass(candidate, concept)) {
            return false;
        } else {
            return !subConcepts.stream().parallel()
                    .anyMatch(other -> isStrictSubclass(candidate, other) && isStrictSubclass(other, concept));
        }
    }

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
