package www.ontologyutils.normalization;

import java.util.*;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.*;

/**
 * Normalization that converts every axiom in the ABox of the ontology to
 * class assertions, (negative) role assertions, or binary (in)equality axioms.
 *
 * This normalization is not strictly necessary, but since the axiom weakener
 * will only remove complete DifferentIndividuals and SameIndividuals axioms,
 * splitting them will make the repair more gentle.
 */
public class ABoxNormalization implements OntologyModification {
    /**
     * SameIndividual axioms can be normalized in different ways.
     * FULL will create all n*(n-1) binary equality axioms between two distinct
     * individual names.
     * STAR chooses one arbitrary individual name as the center and connects all
     * others with (n - 1) binary equality axioms.
     */
    public static enum EqualityStrategy {
        FULL, STAR,
    }

    /**
     * Visitor class used for converting the axioms.
     */
    private static class Visitor implements OWLAxiomVisitorEx<Collection<OWLAxiom>> {
        private final EqualityStrategy equalityStrategy;

        private Visitor(final EqualityStrategy equalityStrategy) {
            this.equalityStrategy = equalityStrategy;
        }

        @Override
        public Collection<OWLAxiom> visit(final OWLDifferentIndividualsAxiom axiom) {
            final var df = Ontology.getDefaultDataFactory();
            final var individuals = axiom.getIndividualsAsList();
            return individuals.stream()
                    .flatMap(first -> individuals.stream()
                            .filter(second -> !first.equals(second))
                            .map(second -> (OWLAxiom) df.getOWLDifferentIndividualsAxiom(first, second)))
                    .toList();
        }

        @Override
        public Collection<OWLAxiom> visit(final OWLSameIndividualAxiom axiom) {
            final var df = Ontology.getDefaultDataFactory();
            final var individuals = axiom.getIndividualsAsList();
            switch (equalityStrategy) {
                case FULL: {
                    return individuals.stream()
                            .flatMap(first -> individuals.stream()
                                    .filter(second -> !first.equals(second))
                                    .map(second -> (OWLAxiom) df.getOWLSameIndividualAxiom(first, second)))
                            .toList();
                }
                case STAR: {
                    final var first = individuals.get(0);
                    return individuals.stream()
                            .filter(second -> !first.equals(second))
                            .map(second -> (OWLAxiom) df.getOWLSameIndividualAxiom(first, second))
                            .toList();
                }
                default:
                    throw new IllegalArgumentException("Unimplemented equality normalization strategy.");
            }
        }

        @Override
        public <T> Collection<OWLAxiom> doDefault(final T axiom) {
            return Set.of((OWLAxiom) axiom);
        }
    }

    private final Visitor visitor;

    public ABoxNormalization(final EqualityStrategy equalityStrategy) {
        visitor = new Visitor(equalityStrategy);
    }

    public ABoxNormalization() {
        this(EqualityStrategy.STAR);
    }

    /**
     * @param axiom
     *            The axiom that should be converted.
     * @return A number of sroiq axioms that together are equivalent to
     *         {@code axiom} in every ontology.
     */
    public Stream<OWLAxiom> asSroiqAxioms(final OWLAxiom axiom) {
        return axiom.accept(visitor).stream();
    }

    @Override
    public void apply(final Ontology ontology) throws IllegalArgumentException {
        final var tBox = ontology.aboxAxioms().toList();
        for (final var axiom : tBox) {
            ontology.replaceAxiom(axiom, asSroiqAxioms(axiom));
        }
    }
}
