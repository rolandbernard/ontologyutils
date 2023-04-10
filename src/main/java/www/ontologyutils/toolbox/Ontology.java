package www.ontologyutils.toolbox;

import java.io.File;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.profiles.Profiles;
import org.semanticweb.owlapi.reasoner.*;

import openllet.owlapi.OpenlletReasonerFactory;

/**
 * This class represents an ontology and is used for ontologies in this package.
 * The main utility of having this class instead of using {@code OWLOntology} is
 * the ability to reuse reasoner and owl ontology as much as possible. The class
 * also provides some utility functions for loading and saving ontologies.
 *
 * The ontology object must be closed in order to free all resources associated
 * to the {@code OWLOntology} object and the {@code OWLreasoner}.
 */
public class Ontology implements AutoCloseable {
    private static final OWLOntologyManager DEFAULT_MANAGER = OWLManager.createOWLOntologyManager();
    private static final OWLReasonerFactory DEFAULT_FACTORY = OpenlletReasonerFactory.getInstance();

    private static class CachedReasoner {
        private final OWLReasonerFactory reasonerFactory;
        private final Set<Ontology> references;
        private Set<OWLAxiom> oldAxioms;
        private OWLReasoner reasoner;

        /**
         * Create a new reasoner cache using the given reasoner factory.
         *
         * @param reasonerFactory
         */
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
                disposeOwlReasoner(reasoner);
                reasoner = null;
            }
        }

        /**
         * If an {@code OWLReasoner} was created using {@code getOwlReasoner} it must be
         * disposed again to free associated resources.
         *
         * @param reasoner
         *                 The {@code OWLReasoner} to dispose.
         */
        public void disposeOwlReasoner(final OWLReasoner reasoner) {
            final OWLOntology owlOntology = reasoner.getRootOntology();
            owlOntology.getOWLOntologyManager().removeOntology(owlOntology);
            reasoner.dispose();
        }

        /**
         * @param ontology
         * @return A new {@code OWLReasoner} created using the factory in this cache.
         */
        public OWLReasoner getOwlReasoner(final Ontology ontology) {
            try {
                final OWLOntology owlOntology = DEFAULT_MANAGER.createOntology();
                owlOntology.addAxioms(ontology.axioms());
                return reasonerFactory.createReasoner(owlOntology);
            } catch (OWLOntologyCreationException e) {
                throw Utils.panic(e);
            }
        }

        /**
         * Use the cached reasoner in this object for executing the given action.
         *
         * @param <T>
         * @param ontology
         * @param action
         * @return The value returned by {@code action}.
         */
        public <T> T withReasonerDo(final Ontology ontology, final Function<OWLReasoner, T> action) {
            final Set<OWLAxiom> newAxioms = ontology.axioms().collect(Collectors.toSet());
            if (reasoner == null) {
                reasoner = getOwlReasoner(ontology);
            } else if (!newAxioms.equals(oldAxioms)) {
                final OWLOntology owlOntology = reasoner.getRootOntology();
                owlOntology.addAxioms(newAxioms.stream().filter(axiom -> !oldAxioms.contains(axiom)));
                owlOntology.removeAxioms(oldAxioms.stream().filter(axiom -> !newAxioms.contains(axiom)));
                reasoner.flush();
            }
            oldAxioms = newAxioms;
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

    /**
     * Create a new ontology that is a copy of {@code toCopy} but using a new
     * reasoner cache.
     *
     * @param toCopy
     */
    public Ontology(final Ontology toCopy) {
        this.staticAxioms = new HashSet<>(toCopy.staticAxioms);
        this.refutableAxioms = new HashSet<>(toCopy.refutableAxioms);
        this.reasonerCache = new CachedReasoner(toCopy.reasonerCache.reasonerFactory);
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

    /**
     * @return The default data factory to use for creating owl api objects.
     */
    public static OWLDataFactory getDefaultDataFactory() {
        return DEFAULT_MANAGER.getOWLDataFactory();
    }

    public OWLDataFactory getDataFactory() {
        return getDefaultDataFactory();
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

    /**
     * @return The {@code OWLAnnotationProperty} used for the origin annotation when
     *         replacing axioms.
     */
    public OWLAnnotationProperty getOriginAnnotationProperty() {
        return getDataFactory().getOWLAnnotationProperty("origin");
    }

    private OWLAnnotation getNewOriginAnnotation(final OWLAxiom origin) {
        final OWLDataFactory df = getDataFactory();
        return df.getOWLAnnotation(getOriginAnnotationProperty(), df.getOWLLiteral(origin.toString()));
    }

    /**
     * Annotate {@code axiom} with the origin annotation indicating that the origin
     * of {@code axiom} is {@code origin}.
     *
     * @param axiom
     * @param origin
     * @return A new annotated axiom equivalent to {@code axiom}.
     */
    private OWLAxiom getAnnotatedAxiom(final OWLAxiom axiom, final OWLAxiom origin) {
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

    public void replaceAxiom(final OWLAxiom remove, final Collection<? extends OWLAxiom> replacement) {
        replaceAxiom(remove, replacement.stream());
    }

    public void replaceAxiom(final OWLAxiom remove, final OWLAxiom... replacement) {
        replaceAxiom(remove, Stream.of(replacement));
    }

    /**
     * The reasoner created by this call must be disposed of again using the
     * {@code disposeOwlReasoner} method.
     *
     * @return A new reasoner for the ontology.
     */
    public OWLReasoner getOwlReasoner() {
        return reasonerCache.getOwlReasoner(this);
    }

    /**
     * Dispose of a reasoner to release all resources associated with the
     * {@code OWLOntology} and {@code OWLReasoner}.
     *
     * @param reasoner
     *                 The reasoner to dispose of.
     */
    public void disposeOwlReasoner(final OWLReasoner reasoner) {
        reasonerCache.disposeOwlReasoner(reasoner);
    }

    private <T> T withReasonerDo(final Function<OWLReasoner, T> action) {
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

    public List<String> getOwlProfiles() {
        return withReasonerDo(reasoner -> Arrays.stream(Profiles.values())
                .map(profile -> profile.checkOntology(reasoner.getRootOntology()).toString())
                .toList());
    }

    public Stream<Set<OWLAxiom>> maximalConsistentSubsets() {
        return (new MaximalConsistentSets(this)).stream();
    }

    public Stream<Set<OWLAxiom>> maximalConsistentSubsets(final Predicate<Ontology> isRepaired) {
        return (new MaximalConsistentSets(this, isRepaired)).stream();
    }

    /**
     * @return A stream of all sets of axioms that when removed from the ontology
     *         yield an optimal classical repair for consistency of the ontology.
     */
    public Stream<Set<OWLAxiom>> optimalClassicalRepairs() {
        return (new MaximalConsistentSets(this)).repairsStream();
    }

    /**
     * @return A stream of all sets of axioms that when removed from the ontology
     *         yield an optimal classical repair for the given predicate.
     */
    public Stream<Set<OWLAxiom>> optimalClassicalRepairs(final Predicate<Ontology> isRepaired) {
        return (new MaximalConsistentSets(this)).repairsStream();
    }

    /**
     * @return A stream providing all subconcepts used in the ontology.
     */
    public Stream<OWLClassExpression> subConcepts() {
        return axioms().flatMap(axiom -> axiom.nestedClassExpressions());
    }

    @Override
    public Ontology clone() {
        return new Ontology(new HashSet<>(staticAxioms), new HashSet<>(refutableAxioms), reasonerCache);
    }

    @Override
    public void close() {
        reasonerCache.removeReference(this);
    }
}
