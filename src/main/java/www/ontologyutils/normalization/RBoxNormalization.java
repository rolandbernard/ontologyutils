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
    private static final String REFLEXIVE_SUBROLE_NAME = "http://www.ontologyutils.rbox-normalization#[reflexive-subrole]";

    /**
     * Visitor class used for converting the axioms.
     */
    private static class Visitor implements OWLAxiomVisitorEx<Collection<OWLAxiom>> {
        protected OWLDataFactory df;
        private boolean fullEquality;

        private Visitor(boolean fullEquality) {
            df = Ontology.getDefaultDataFactory();
            this.fullEquality = fullEquality;
        }

        @Override
        public Collection<OWLAxiom> visit(OWLSubObjectPropertyOfAxiom axiom) {
            return List.of(axiom);
        }

        @Override
        public Collection<OWLAxiom> visit(OWLSubPropertyChainOfAxiom axiom) {
            return List.of(axiom);
        }

        @Override
        public Collection<OWLAxiom> visit(OWLTransitiveObjectPropertyAxiom axiom) {
            var property = axiom.getProperty();
            return List.of(df.getOWLSubPropertyChainOfAxiom(List.of(property, property), property));
        }

        @Override
        public Collection<OWLAxiom> visit(OWLInverseObjectPropertiesAxiom axiom) {
            var first = axiom.getFirstProperty();
            var second = axiom.getSecondProperty();
            return List.of(
                    df.getOWLSubObjectPropertyOfAxiom(first.getInverseProperty(), second),
                    df.getOWLSubObjectPropertyOfAxiom(second, first.getInverseProperty()));
        }

        @Override
        public Collection<OWLAxiom> visit(OWLSymmetricObjectPropertyAxiom axiom) {
            var property = axiom.getProperty();
            return List.of(df.getOWLSubObjectPropertyOfAxiom(property.getInverseProperty(), property));
        }

        @Override
        public Collection<OWLAxiom> visit(OWLAsymmetricObjectPropertyAxiom axiom) {
            var property = axiom.getProperty();
            return List.of(df.getOWLDisjointObjectPropertiesAxiom(property, property.getInverseProperty()));
        }

        @Override
        public Collection<OWLAxiom> visit(OWLReflexiveObjectPropertyAxiom axiom) {
            var property = axiom.getProperty();
            var reflexiveProperty = df.getOWLObjectProperty(REFLEXIVE_SUBROLE_NAME);
            return List.of(
                    df.getOWLSubObjectPropertyOfAxiom(reflexiveProperty, property),
                    df.getOWLSubClassOfAxiom(df.getOWLThing(), df.getOWLObjectHasSelf(reflexiveProperty)));
        }

        @Override
        public Collection<OWLAxiom> visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
            var property = axiom.getProperty();
            return List.of(df.getOWLSubClassOfAxiom(df.getOWLThing(),
                    df.getOWLObjectHasSelf(property).getObjectComplementOf()));
        }

        @Override
        public Collection<OWLAxiom> visit(OWLDisjointObjectPropertiesAxiom axiom) {
            var properties = Utils.toList(axiom.properties());
            return Utils.toList(properties.stream()
                    .flatMap(first -> properties.stream()
                            .filter(second -> !first.equals(second))
                            .map(second -> (OWLAxiom) df.getOWLDisjointObjectPropertiesAxiom(first, second))));
        }

        @Override
        public Collection<OWLAxiom> visit(OWLEquivalentObjectPropertiesAxiom axiom) {
            var properties = Utils.toList(axiom.properties());
            if (fullEquality) {
                return Utils.toList(properties.stream()
                        .flatMap(first -> properties.stream()
                                .filter(second -> !first.equals(second))
                                .flatMap(second -> Stream.of(
                                        (OWLAxiom) df.getOWLSubObjectPropertyOfAxiom(second, first),
                                        (OWLAxiom) df.getOWLSubObjectPropertyOfAxiom(first, second)))));
            } else {
                var first = properties.get(0);
                return Utils.toList(properties.stream()
                        .filter(second -> !first.equals(second))
                        .flatMap(second -> Stream.of(
                                (OWLAxiom) df.getOWLSubObjectPropertyOfAxiom(second, first),
                                (OWLAxiom) df.getOWLSubObjectPropertyOfAxiom(first, second))));
            }
        }

        @Override
        public <T> Collection<OWLAxiom> doDefault(T axiom) {
            throw new IllegalArgumentException("RBox normalization does not support axiom " + axiom);
        }
    }

    private Visitor visitor;

    /**
     * @param fullEquality
     *            Set to true if you want equality asserted between all
     *            pairs of individuals.
     */
    public RBoxNormalization(boolean fullEquality) {
        visitor = new Visitor(fullEquality);
    }

    /**
     * Create a new RBox normalization object.
     */
    public RBoxNormalization() {
        this(false);
    }

    /**
     * Add an axiom defining the reflexive subrole that is used during
     * normalization.
     * Note that this method is intended only for testing, to ensure the original
     * and normalized ontologies are equivalent it will assert that the returned
     * object property contains only reflexive connections.
     *
     * @param ontology
     *            The ontology to which the axioms should be added.
     * @return The reflexive object property.
     */
    public static OWLObjectProperty addSimpleReflexiveRole(Ontology ontology) {
        var df = Ontology.getDefaultDataFactory();
        var reflexiveProperty = df.getOWLObjectProperty(REFLEXIVE_SUBROLE_NAME);
        ontology.addAxioms(
                df.getOWLSubClassOfAxiom(df.getOWLThing(), df.getOWLObjectHasSelf(reflexiveProperty)),
                df.getOWLSubClassOfAxiom(df.getOWLThing(), df.getOWLObjectMaxCardinality(1, reflexiveProperty)));
        return reflexiveProperty;
    }

    /**
     * @param axiom
     *            The axiom that should be converted.
     * @return A number of sroiq axioms that together are equivalent to
     *         {@code axiom} in every ontology.
     */
    public Stream<OWLAxiom> asSroiqAxioms(OWLAxiom axiom) {
        return axiom.accept(visitor).stream();
    }

    @Override
    public void apply(Ontology ontology) throws IllegalArgumentException {
        var rBox = Utils.toList(ontology.rboxAxioms());
        for (var axiom : rBox) {
            ontology.replaceAxiom(axiom, asSroiqAxioms(axiom));
        }
    }
}
