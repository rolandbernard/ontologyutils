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
 *
 * SameIndividual axioms can be normalized in different ways. If
 * {@code fullEquality} is:
 * true, it will create all n*(n-1) binary equality axioms between two distinct
 * individual names.
 * false, it will choose one arbitrary individual name as the center and
 * connects all others with (n - 1) binary equality axioms.
 */
public class ABoxNormalization implements OntologyModification {
    /**
     * Visitor class used for converting the axioms.
     */
    private static class Visitor implements OWLAxiomVisitorEx<Collection<OWLAxiom>> {
        protected final OWLDataFactory df;
        private final boolean fullEquality;

        private Visitor(final boolean fullEquality) {
            df = Ontology.getDefaultDataFactory();
            this.fullEquality = fullEquality;
        }

        @Override
        public Collection<OWLAxiom> visit(final OWLClassAssertionAxiom axiom) {
            return List.of(axiom);
        }

        @Override
        public Collection<OWLAxiom> visit(final OWLObjectPropertyAssertionAxiom axiom) {
            return List.of(axiom);
        }

        @Override
        public Collection<OWLAxiom> visit(final OWLNegativeObjectPropertyAssertionAxiom axiom) {
            return List.of(axiom);
        }

        @Override
        public Collection<OWLAxiom> visit(final OWLDifferentIndividualsAxiom axiom) {
            final var individuals = axiom.getIndividualsAsList();
            return individuals.stream()
                    .flatMap(first -> individuals.stream()
                            .filter(second -> !first.equals(second))
                            .map(second -> (OWLAxiom) df.getOWLDifferentIndividualsAxiom(first, second)))
                    .toList();
        }

        @Override
        public Collection<OWLAxiom> visit(final OWLSameIndividualAxiom axiom) {
            final var individuals = axiom.getIndividualsAsList();
            if (fullEquality) {
                return individuals.stream()
                        .flatMap(first -> individuals.stream()
                                .filter(second -> !first.equals(second))
                                .map(second -> (OWLAxiom) df.getOWLSameIndividualAxiom(first, second)))
                        .toList();
            } else {
                final var first = individuals.get(0);
                return individuals.stream()
                        .filter(second -> !first.equals(second))
                        .map(second -> (OWLAxiom) df.getOWLSameIndividualAxiom(first, second))
                        .toList();
            }
        }

        @Override
        public <T> Collection<OWLAxiom> doDefault(final T axiom) {
            throw new IllegalArgumentException("ABox normalization does not support axiom " + axiom);
        }
    }

    private final Visitor visitor;

    /**
     * @param fullEquality
     *            Set to true if you want equality asserted between all
     *            pairs of
     *            individuals.
     */
    public ABoxNormalization(final boolean fullEquality) {
        visitor = new Visitor(fullEquality);
    }

    public ABoxNormalization() {
        this(false);
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
        final var aBox = ontology.aboxAxioms().toList();
        for (final var axiom : aBox) {
            ontology.replaceAxiom(axiom, asSroiqAxioms(axiom));
        }
    }
}
