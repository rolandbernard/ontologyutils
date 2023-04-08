package www.ontologyutils.toolbox;

import java.io.File;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;

import openllet.owlapi.OpenlletReasonerFactory;

/**
 * This class represents a wrapper used for ontologies in this package. The main
 * utility of having this wrapper is the ability to reuse reasoner and ontology
 * as much as possible. The class also provides some utility functions for
 * creating copies without certain axioms or empty ontologies.
 */
public class Ontology implements AutoCloseable {
    private static final OWLReasonerFactory DEFAULT_FACTORY = OpenlletReasonerFactory.getInstance();

    private final OWLOntology ontology;
    private final OWLReasonerFactory reasonerFactory;
    private final Set<OWLAxiom> refutableAxioms;
    protected OWLReasoner reasoner;

    /**
     * Create a new ontology wrapper around the given {@code OWLOntology} and
     * refutable axioms. Should the need arise to create a reasoner, use
     * {@code reasonerFactory} to create it.
     *
     * @param ontology
     * @param refutableAxioms
     * @param reasonerFactory
     */
    private Ontology(final OWLOntology ontology, final Set<OWLAxiom> refutableAxioms,
            final OWLReasonerFactory reasonerFactory) {
        this.ontology = ontology;
        this.refutableAxioms = refutableAxioms;
        this.reasonerFactory = reasonerFactory;
    }

    public static Ontology wrapOntology(final OWLOntology ontology, final Set<OWLAxiom> refutableAxioms,
            final OWLReasonerFactory reasonerFactory) {
        return new Ontology(ontology, refutableAxioms, reasonerFactory);
    }

    public static Ontology wrapOntology(final OWLOntology ontology, final Set<OWLAxiom> refutableAxioms) {
        return wrapOntology(ontology, refutableAxioms, DEFAULT_FACTORY);
    }

    public static Ontology wrapOntology(final OWLOntology ontology, final OWLReasonerFactory reasonerFactory) {
        return wrapOntology(ontology, ontology.logicalAxioms().collect(Collectors.toCollection(HashSet::new)),
                reasonerFactory);
    }

    public static Ontology wrapOntology(final OWLOntology ontology) {
        return wrapOntology(ontology, DEFAULT_FACTORY);
    }

    public static Ontology withAxioms(final Set<OWLAxiom> staticAxioms, final Set<OWLAxiom> refutableAxioms,
            final OWLReasonerFactory reasonerFactory) {
        refutableAxioms.removeAll(staticAxioms);
        final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = null;
        try {
            ontology = manager.createOntology();
            ontology.addAxioms(staticAxioms);
            ontology.addAxioms(refutableAxioms);
        } catch (OWLOntologyCreationException e) {
            Utils.panic(e);
        }
        return wrapOntology(ontology, refutableAxioms, reasonerFactory);
    }

    public static Ontology withAxioms(final Set<OWLAxiom> staticAxioms, final Set<OWLAxiom> refutableAxioms) {
        return withAxioms(staticAxioms, refutableAxioms, DEFAULT_FACTORY);
    }

    public static Ontology withAxioms(final Set<OWLAxiom> refutableAxioms,
            final OWLReasonerFactory reasonerFactory) {
        return withAxioms(Set.of(), refutableAxioms, reasonerFactory);
    }

    public static Ontology withAxioms(final Set<OWLAxiom> refutableAxioms) {
        return withAxioms(refutableAxioms, DEFAULT_FACTORY);
    }

    public static Ontology emptyOntology(final OWLReasonerFactory reasonerFactory) {
        final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = null;
        try {
            ontology = manager.createOntology();
        } catch (OWLOntologyCreationException e) {
            Utils.panic(e);
        }
        return wrapOntology(ontology, reasonerFactory);
    }

    public static Ontology emptyOntology() {
        return emptyOntology(DEFAULT_FACTORY);
    }

    public static Ontology loadOntology(final String filePath, final OWLReasonerFactory reasonerFactory) {
        final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        final File ontologyFile = new File(filePath);
        OWLOntology ontology = null;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(ontologyFile);
        } catch (OWLOntologyCreationException e) {
            Utils.panic(e);
        }
        return wrapOntology(ontology, reasonerFactory);
    }

    public static Ontology loadOntology(final String filePath) {
        return loadOntology(filePath, DEFAULT_FACTORY);
    }

    public Stream<OWLAxiom> axioms() {
        return ontology.axioms();
    }

    public Stream<OWLLogicalAxiom> logicalAxioms() {
        return ontology.logicalAxioms().map(a -> a);
    }

    public Stream<OWLAxiom> refutableAxioms() {
        return refutableAxioms.stream();
    }

    public Stream<OWLAxiom> staticAxioms() {
        return ontology.axioms().filter(Predicate.not(refutableAxioms::contains));
    }

    public void removeAxioms(final OWLAxiom... axioms) {
        refutableAxioms.removeAll(List.of(axioms));
        ontology.removeAxioms(axioms);
    }

    public void addStaticAxioms(final OWLAxiom... axioms) {
        ontology.addAxioms(axioms);
    }

    public void addAxioms(final OWLAxiom... axioms) {
        refutableAxioms.addAll(List.of(axioms));
        ontology.addAxioms(axioms);
    }

    public void removeAxioms(final Stream<? extends OWLAxiom> axioms) {
        removeAxioms(axioms.toArray(n -> new OWLAxiom[n]));
    }

    public void addStaticAxioms(final Stream<? extends OWLAxiom> axioms) {
        addStaticAxioms(axioms.toArray(n -> new OWLAxiom[n]));
    }

    public void addAxioms(final Stream<? extends OWLAxiom> axioms) {
        addAxioms(axioms.toArray(n -> new OWLAxiom[n]));
    }

    public OWLAnnotationProperty getOriginAnnotationProperty() {
        return getDataFactory().getOWLAnnotationProperty("origin");
    }

    public OWLAnnotation getNewOriginAnnotation(final OWLAxiom origin) {
        final OWLDataFactory df = getDataFactory();
        return df.getOWLAnnotation(getOriginAnnotationProperty(), df.getOWLLiteral(origin.toString()));
    }

    public OWLAxiom getAnnotatedAxiom(final OWLAxiom axiom, final OWLAxiom origin) {
        if (origin.annotations(getOriginAnnotationProperty()).count() > 0) {
            return axiom.getAnnotatedAxiom(origin.annotations(getOriginAnnotationProperty()));
        } else {
            return axiom.getAnnotatedAxiom(
                    Stream.concat(axiom.annotations(), Stream.of(getNewOriginAnnotation(origin))));
        }
    }

    public void replaceAxiom(final OWLAxiom remove, final Stream<? extends OWLAxiom> replacement) {
        removeAxioms(remove);
        addAxioms(replacement.map(a -> getAnnotatedAxiom(a, remove)));
    }

    public void replaceAxiom(final OWLAxiom remove, final OWLAxiom... replacement) {
        replaceAxiom(remove, Stream.of(replacement));
    }

    public Ontology withoutAxioms(final OWLAxiom... axioms) {
        final Ontology copy = clone();
        copy.removeAxioms(axioms);
        return copy;
    }

    public Ontology withStaticAxioms(final OWLAxiom... axioms) {
        final Ontology copy = clone();
        copy.addStaticAxioms(axioms);
        return copy;
    }

    public Ontology withAxioms(final OWLAxiom... axioms) {
        final Ontology copy = clone();
        copy.addAxioms(axioms);
        return copy;
    }

    public Ontology withoutAxioms(final Stream<? extends OWLAxiom> axioms) {
        final Ontology copy = clone();
        copy.removeAxioms(axioms);
        return copy;
    }

    public Ontology withStaticAxioms(final Stream<? extends OWLAxiom> axioms) {
        final Ontology copy = clone();
        copy.addStaticAxioms(axioms);
        return copy;
    }

    public Ontology withAxioms(final Stream<? extends OWLAxiom> axioms) {
        final Ontology copy = clone();
        copy.addAxioms(axioms);
        return copy;
    }

    public Ontology withoutRefutableAxioms() {
        return withoutAxioms(refutableAxioms());
    }

    public OWLOntology getOwlOntology() {
        return ontology;
    }

    public OWLDataFactory getDataFactory() {
        return ontology.getOWLOntologyManager().getOWLDataFactory();
    }

    public OWLReasoner getReasoner() {
        if (reasoner == null) {
            reasoner = reasonerFactory.createReasoner(ontology);
        }
        reasoner.flush();
        return reasoner;
    }

    public boolean isConsistent() {
        return getReasoner().isConsistent();
    }

    public boolean isEntailed(final OWLAxiom... axioms) {
        return getReasoner().isEntailed(axioms);
    }

    public boolean isSatisfiable(final OWLClassExpression concepts) {
        return getReasoner().isSatisfiable(concepts);
    }

    public Stream<Set<OWLAxiom>> maximalConsistentSubsets() {
        return (new MaximalConsistentSets(this)).stream();
    }

    public Stream<Set<OWLAxiom>> optimalClassicalRepairs(final Predicate<Ontology> isRepaired) {
        return (new MaximalConsistentSets(this)).repairsStream();
    }

    @Override
    public Ontology clone() {
        final OWLOntologyManager manager = ontology.getOWLOntologyManager();
        OWLOntology newOntology = null;
        try {
            newOntology = manager.createOntology();
            newOntology.addAxioms(axioms());
        } catch (final OWLOntologyCreationException e) {
            Utils.panic(e);
        }
        return new Ontology(newOntology, new HashSet<>(refutableAxioms), reasonerFactory);
    }

    public void dispose() {
        ontology.getOWLOntologyManager().removeOntology(ontology);
        if (reasoner != null) {
            reasoner.dispose();
            reasoner = null;
        }
    }

    @Override
    public void close() {
        dispose();
    }
}
