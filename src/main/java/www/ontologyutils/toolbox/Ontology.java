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
 * This class represents an ontology and is used for ontologies in this package.
 * The main utility of having this class instead of using {@code OWLOntology} is
 * the ability to reuse reasoner and owl ontology as much as possible. The class
 * also provides some utility functions for loading and saving ontologies.
 */
public class Ontology implements AutoCloseable {
    private static final OWLOntologyManager DEFAULT_MANAGER = OWLManager.createOWLOntologyManager();
    private static final OWLReasonerFactory DEFAULT_FACTORY = OpenlletReasonerFactory.getInstance();

    private static class CachedReasoner {
        private final OWLReasonerFactory reasonerFactory;
        private final Set<Ontology> references;
        private Set<OWLAxiom> oldAxioms;
        private OWLReasoner reasoner;

        public CachedReasoner(final OWLReasonerFactory reasonerFactory) {
            this.reasonerFactory = reasonerFactory;
            this.references = new HashSet<>();
            this.oldAxioms = new HashSet<>();
        }

        public void addReference(final Ontology ontology) {
            references.add(ontology);
        }

        public void removeReference(final Ontology ontology) {
            references.remove(ontology);
            if (references.isEmpty() && reasoner != null) {
                final OWLOntology owlOntology = reasoner.getRootOntology();
                owlOntology.getOWLOntologyManager().removeOntology(owlOntology);
                reasoner.dispose();
                reasoner = null;
            }
        }

        public synchronized <T> T withReasonerDo(final Ontology ontology, final Function<OWLReasoner, T> action) {
            if (reasoner == null) {
                try {
                    reasoner = reasonerFactory.createReasoner(DEFAULT_MANAGER.createOntology());
                } catch (OWLOntologyCreationException e) {
                    throw Utils.panic(e);
                }
            }
            final OWLOntology owlOntology = reasoner.getRootOntology();
            final Set<OWLAxiom> newAxioms = ontology.axioms().collect(Collectors.toSet());
            owlOntology.addAxioms(newAxioms.stream().filter(axiom -> !oldAxioms.contains(axiom)));
            owlOntology.removeAxioms(oldAxioms.stream().filter(axiom -> !newAxioms.contains(axiom)));
            oldAxioms = newAxioms;
            reasoner.flush();
            return action.apply(reasoner);
        }
    }

    private final Set<OWLAxiom> staticAxioms;
    private final Set<OWLAxiom> refutableAxioms;
    private final CachedReasoner reasonerCache;

    /**
     * Create a new ontology around the given static and refutable axioms. Should
     * the need arise to create a reasoner, use {@code reasonerFactory} to create
     * it.
     *
     * @param staticAxioms
     * @param refutableAxioms
     * @param reasonerFactory
     */
    private Ontology(final Set<OWLAxiom> staticAxioms, final Set<OWLAxiom> refutableAxioms,
            final CachedReasoner reasonerCache) {
        this.staticAxioms = staticAxioms;
        this.refutableAxioms = refutableAxioms;
        this.reasonerCache = reasonerCache;
        this.reasonerCache.addReference(this);
    }

    public static Ontology withAxioms(final Set<OWLAxiom> staticAxioms, final Set<OWLAxiom> refutableAxioms,
            final OWLReasonerFactory reasonerFactory) {
        return new Ontology(staticAxioms, refutableAxioms, new CachedReasoner(reasonerFactory));
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
        return withAxioms(Set.of(), reasonerFactory);
    }

    public static Ontology emptyOntology() {
        return emptyOntology(DEFAULT_FACTORY);
    }

    public static Ontology loadOntology(final String filePath, final OWLReasonerFactory reasonerFactory) {
        final OWLOntologyManager manager = DEFAULT_MANAGER;
        final File ontologyFile = new File(filePath);
        OWLOntology ontology = null;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(ontologyFile);
        } catch (OWLOntologyCreationException e) {
            throw Utils.panic(e);
        }
        final Set<OWLAxiom> logicalAxioms = ontology.logicalAxioms().collect(Collectors.toSet());
        final Set<OWLAxiom> otherAxioms = ontology.axioms()
                .filter(axiom -> !logicalAxioms.contains(axiom))
                .collect(Collectors.toSet());
        manager.removeOntology(ontology);
        return withAxioms(otherAxioms, logicalAxioms, reasonerFactory);
    }

    public static Ontology loadOntology(final String filePath) {
        return loadOntology(filePath, DEFAULT_FACTORY);
    }

    public Stream<OWLAxiom> staticAxioms() {
        return staticAxioms.stream();
    }

    public Stream<OWLAxiom> refutableAxioms() {
        return refutableAxioms.stream();
    }

    public Stream<OWLAxiom> axioms() {
        return Stream.concat(staticAxioms(), refutableAxioms());
    }

    public Stream<OWLLogicalAxiom> logicalAxioms() {
        return axioms().filter(axiom -> axiom.isLogicalAxiom()).map(axiom -> (OWLLogicalAxiom) axiom);
    }

    public void removeAxioms(final Stream<? extends OWLAxiom> axioms) {
        axioms.forEach(axiom -> {
            staticAxioms.remove(axiom);
            refutableAxioms.remove(axiom);
        });
    }

    public void addStaticAxioms(final Stream<? extends OWLAxiom> axioms) {
        axioms.forEach(axiom -> staticAxioms.add(axiom));
    }

    public void addAxioms(final Stream<? extends OWLAxiom> axioms) {
        axioms.forEach(axiom -> refutableAxioms.add(axiom));
    }

    public void removeAxioms(final Collection<? extends OWLAxiom> axioms) {
        removeAxioms(axioms.stream());
    }

    public void addStaticAxioms(final Collection<? extends OWLAxiom> axioms) {
        addStaticAxioms(axioms.stream());
    }

    public void addAxioms(final Collection<? extends OWLAxiom> axioms) {
        addAxioms(axioms.stream());
    }

    public void removeAxioms(final OWLAxiom... axioms) {
        removeAxioms(Stream.of(axioms));
    }

    public void addStaticAxioms(final OWLAxiom... axioms) {
        addStaticAxioms(Stream.of(axioms));
    }

    public void addAxioms(final OWLAxiom... axioms) {
        addAxioms(Stream.of(axioms));
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

    public OWLDataFactory getDataFactory() {
        return DEFAULT_MANAGER.getOWLDataFactory();
    }

    public <T> T withReasonerDo(final Function<OWLReasoner, T> action) {
        return reasonerCache.withReasonerDo(this, action);
    }

    public boolean isConsistent() {
        return withReasonerDo(reasoner -> reasoner.isConsistent());
    }

    public boolean isEntailed(final OWLAxiom... axioms) {
        return withReasonerDo(reasoner -> reasoner.isEntailed(axioms));
    }

    public boolean isSatisfiable(final OWLClassExpression concepts) {
        return withReasonerDo(reasoner -> reasoner.isSatisfiable(concepts));
    }

    public Stream<Set<OWLAxiom>> maximalConsistentSubsets() {
        return (new MaximalConsistentSets(this)).stream();
    }

    public Stream<Set<OWLAxiom>> optimalClassicalRepairs(final Predicate<Ontology> isRepaired) {
        return (new MaximalConsistentSets(this)).repairsStream();
    }

    @Override
    public Ontology clone() {
        return new Ontology(new HashSet<>(staticAxioms), new HashSet<>(refutableAxioms), reasonerCache);
    }

    public void dispose() {
        reasonerCache.removeReference(this);
    }

    @Override
    public void close() {
        dispose();
    }
}
