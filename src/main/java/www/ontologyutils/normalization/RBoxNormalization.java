package www.ontologyutils.normalization;

import java.util.*;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.*;

/**
 * Normalization that converts every axiom in the RBox of the ontology to
 * role inclusion axioms, and disjoint role assertions.
 * Some axioms are converted to TBox axioms.
 */
public class RBoxNormalization implements OntologyModification {
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
        public Collection<OWLAxiom> visit(final OWLSubObjectPropertyOfAxiom axiom) {
            return List.of(axiom);
        }

        @Override
        public Collection<OWLAxiom> visit(final OWLSubPropertyChainOfAxiom axiom) {
            return List.of(axiom);
        }

        @Override
        public Collection<OWLAxiom> visit(final OWLTransitiveObjectPropertyAxiom axiom) {
            final var property = axiom.getProperty();
            return List.of(df.getOWLSubPropertyChainOfAxiom(List.of(property, property), property));
        }

        @Override
        public Collection<OWLAxiom> visit(final OWLInverseObjectPropertiesAxiom axiom) {
            final var first = axiom.getFirstProperty();
            final var second = axiom.getSecondProperty();
            return List.of(
                    df.getOWLSubObjectPropertyOfAxiom(first.getInverseProperty(), second),
                    df.getOWLSubObjectPropertyOfAxiom(second, first.getInverseProperty()));
        }

        @Override
        public Collection<OWLAxiom> visit(final OWLSymmetricObjectPropertyAxiom axiom) {
            final var property = axiom.getProperty();
            return List.of(df.getOWLSubObjectPropertyOfAxiom(property.getInverseProperty(), property));
        }

        @Override
        public Collection<OWLAxiom> visit(final OWLAsymmetricObjectPropertyAxiom axiom) {
            final var property = axiom.getProperty();
            return List.of(df.getOWLDisjointObjectPropertiesAxiom(property, property.getInverseProperty()));
        }

        @Override
        public Collection<OWLAxiom> visit(final OWLReflexiveObjectPropertyAxiom axiom) {
            final var property = axiom.getProperty();
            final var freshProperty = df
                    .getOWLObjectProperty(property.getNamedProperty().getIRI().toString() + "[reflexive-subrole]");
            return List.of(
                    df.getOWLSubObjectPropertyOfAxiom(freshProperty, property),
                    df.getOWLSubClassOfAxiom(df.getOWLThing(), df.getOWLObjectHasSelf(freshProperty)));
        }

        @Override
        public Collection<OWLAxiom> visit(final OWLIrreflexiveObjectPropertyAxiom axiom) {
            final var property = axiom.getProperty();
            return List.of(df.getOWLSubClassOfAxiom(df.getOWLThing(),
                    df.getOWLObjectHasSelf(property).getObjectComplementOf()));
        }

        @Override
        public Collection<OWLAxiom> visit(final OWLDisjointObjectPropertiesAxiom axiom) {
            final var properties = axiom.properties().toList();
            return properties.stream()
                    .flatMap(first -> properties.stream()
                            .filter(second -> !first.equals(second))
                            .map(second -> (OWLAxiom) df.getOWLDisjointObjectPropertiesAxiom(first, second)))
                    .toList();
        }

        @Override
        public Collection<OWLAxiom> visit(final OWLEquivalentObjectPropertiesAxiom axiom) {
            final var properties = axiom.properties().toList();
            if (fullEquality) {
                return properties.stream()
                        .flatMap(first -> properties.stream()
                                .filter(second -> !first.equals(second))
                                .flatMap(second -> Stream.of(
                                        (OWLAxiom) df.getOWLSubObjectPropertyOfAxiom(second, first),
                                        (OWLAxiom) df.getOWLSubObjectPropertyOfAxiom(first, second))))
                        .toList();
            } else {
                final var first = properties.get(0);
                return properties.stream()
                        .filter(second -> !first.equals(second))
                        .flatMap(second -> Stream.of(
                                (OWLAxiom) df.getOWLSubObjectPropertyOfAxiom(second, first),
                                (OWLAxiom) df.getOWLSubObjectPropertyOfAxiom(first, second)))
                        .toList();
            }
        }

        @Override
        public <T> Collection<OWLAxiom> doDefault(final T axiom) {
            throw new IllegalArgumentException("RBox normalization does not support axiom " + axiom);
        }
    }

    private final Visitor visitor;

    /**
     * @param fullEquality
     *            Set to true if you want equality asserted between all
     *            pairs of
     *            individuals.
     */
    public RBoxNormalization(final boolean fullEquality) {
        visitor = new Visitor(fullEquality);
    }

    public RBoxNormalization() {
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
        final var tBox = ontology.rboxAxioms().toList();
        for (final var axiom : tBox) {
            ontology.replaceAxiom(axiom, asSroiqAxioms(axiom));
        }
    }
}
