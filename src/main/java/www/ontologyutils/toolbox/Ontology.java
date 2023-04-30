package www.ontologyutils.toolbox;

import java.io.File;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.profiles.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.util.OWLObjectPropertyManager;

import openllet.owlapi.OpenlletReasonerFactory;
import uk.ac.manchester.cs.jfact.JFactFactory;

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
    private static final OWLOntologyManager defaultManager = OWLManager.createOWLOntologyManager();
    private static final OWLReasonerFactory defaultFactory = OpenlletReasonerFactory.getInstance();

    private static class CachedReasoner {
        private final OWLReasonerFactory reasonerFactory;
        private final Set<Ontology> references;
        private OWLReasoner reasoner;

        /**
         * Create a new reasoner cache using the given reasoner factory.
         *
         * @param reasonerFactory
         */
        public CachedReasoner(final OWLReasonerFactory reasonerFactory) {
            this.reasonerFactory = reasonerFactory;
            this.references = new HashSet<>();
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
         *            The {@code OWLReasoner} to dispose.
         */
        public void disposeOwlReasoner(final OWLReasoner reasoner) {
            final var owlOntology = reasoner.getRootOntology();
            reasoner.dispose();
            owlOntology.getOWLOntologyManager().removeOntology(owlOntology);
        }

        /**
         * @param axioms
         * @return A new {@code OWLReasoner} created using the factory in this cache.
         */
        public OWLReasoner getOwlReasoner(final Ontology ontology) {
            try {
                final var owlOntology = defaultManager.createOntology();
                owlOntology.addAxioms(ontology.axioms());
                return reasonerFactory.createReasoner(owlOntology);
            } catch (final OWLOntologyCreationException e) {
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
            if (Thread.interrupted()) {
                throw new CanceledException();
            } else if (reasoner == null) {
                reasoner = getOwlReasoner(ontology);
            } else {
                final var owlOntology = reasoner.getRootOntology();
                if (ontology.applyChangesTo(owlOntology)) {
                    reasoner.flush();
                }
            }
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
    private Ontology(final Collection<? extends OWLAxiom> staticAxioms,
            final Collection<? extends OWLAxiom> refutableAxioms, final CachedReasoner reasonerCache) {
        this.staticAxioms = new HashSet<>(staticAxioms);
        this.refutableAxioms = new HashSet<>(refutableAxioms);
        this.refutableAxioms.removeAll(staticAxioms);
        this.reasonerCache = reasonerCache;
        this.reasonerCache.addReference(this);
    }

    public static Ontology withAxioms(final Collection<? extends OWLAxiom> staticAxioms,
            final Collection<? extends OWLAxiom> refutableAxioms,
            final OWLReasonerFactory reasonerFactory) {
        return new Ontology(staticAxioms, refutableAxioms, new CachedReasoner(reasonerFactory));
    }

    public static Ontology withAxioms(final Collection<? extends OWLAxiom> staticAxioms,
            final Collection<? extends OWLAxiom> refutableAxioms) {
        return withAxioms(staticAxioms, refutableAxioms, defaultFactory);
    }

    public static Ontology withAxioms(final Collection<? extends OWLAxiom> refutableAxioms,
            final OWLReasonerFactory reasonerFactory) {
        return withAxioms(Set.of(), refutableAxioms, reasonerFactory);
    }

    public static Ontology withAxioms(final Collection<? extends OWLAxiom> refutableAxioms) {
        return withAxioms(refutableAxioms, defaultFactory);
    }

    public static Ontology emptyOntology(final OWLReasonerFactory reasonerFactory) {
        return withAxioms(Set.of(), reasonerFactory);
    }

    public static Ontology withAxiomsFrom(final OWLOntology ontology, final OWLReasonerFactory reasonerFactory) {
        final var logicalAxioms = ontology.logicalAxioms().collect(Collectors.toSet());
        final var otherAxioms = ontology.axioms()
                .filter(axiom -> !logicalAxioms.contains(axiom))
                .collect(Collectors.toSet());
        return withAxioms(otherAxioms, logicalAxioms, reasonerFactory);
    }

    public static Ontology withAxiomsFrom(final OWLOntology ontology) {
        return withAxiomsFrom(ontology, defaultFactory);
    }

    public static Ontology emptyOntology() {
        return emptyOntology(defaultFactory);
    }

    public static Ontology loadOntology(final String filePath, final OWLReasonerFactory reasonerFactory) {
        OWLOntology ontology = null;
        try {
            final var ontologyFile = new File(filePath);
            ontology = defaultManager.loadOntologyFromOntologyDocument(ontologyFile);
            return withAxiomsFrom(ontology, reasonerFactory);
        } catch (final OWLOntologyCreationException e) {
            throw Utils.panic(e);
        } finally {
            defaultManager.removeOntology(ontology);
        }
    }

    public static Ontology loadOntology(final String filePath) {
        return loadOntology(filePath, defaultFactory);
    }

    public static Ontology loadOnlyLogicalAxioms(final String filePath) {
        final var ontology = loadOntology(filePath, defaultFactory);
        ontology.removeAxioms(ontology.nonLogicalAxioms().toList());
        return ontology;
    }

    public static Ontology loadOntologyWithOriginAnnotations(final String filePath) {
        final var ontology = loadOntology(filePath, defaultFactory);
        for (final var axiom : ontology.axioms().toList()) {
            ontology.replaceAxiom(axiom, axiom);
        }
        return ontology;
    }

    /**
     * Save the ontology to the file given by the path {@code filePath}.
     *
     * @param filePath
     */
    public void saveOntology(String filePath) {
        this.<Void>withOwlOntologyDo(ontology -> {
            final var ontologyFile = new File(filePath);
            try {
                ontology.saveOntology(new FunctionalSyntaxDocumentFormat(), IRI.create(ontologyFile));
            } catch (final OWLOntologyStorageException e) {
                Utils.panic(e);
            }
            return null;
        });
    }

    public boolean applyChangesTo(final OWLOntology ontology) {
        final var oldAxioms = ontology.axioms().collect(Collectors.toSet());
        final var newAxioms = axioms().collect(Collectors.toSet());
        if (oldAxioms.equals(newAxioms)) {
            return false;
        } else {
            ontology.addAxioms(newAxioms.stream().filter(axiom -> !oldAxioms.contains(axiom)));
            ontology.removeAxioms(oldAxioms.stream().filter(axiom -> !newAxioms.contains(axiom)));
            return true;
        }
    }

    /**
     * @return The default data factory to use for creating owl api objects.
     */
    public static OWLDataFactory getDefaultDataFactory() {
        return defaultManager.getOWLDataFactory();
    }

    public Stream<OWLAxiom> staticAxioms() {
        return staticAxioms.stream();
    }

    public Stream<OWLAxiom> staticAxioms(final AxiomType<?>... types) {
        return staticAxioms().filter(axiom -> axiom.isOfType(types));
    }

    public Stream<OWLAxiom> staticAxioms(final Collection<AxiomType<?>> types) {
        return staticAxioms().filter(axiom -> axiom.isOfType(types));
    }

    public Stream<OWLAxiom> refutableAxioms() {
        return refutableAxioms.stream();
    }

    public Stream<OWLAxiom> refutableAxioms(final AxiomType<?>... types) {
        return refutableAxioms().filter(axiom -> axiom.isOfType(types));
    }

    public Stream<OWLAxiom> refutableAxioms(final Collection<AxiomType<?>> types) {
        return refutableAxioms().filter(axiom -> axiom.isOfType(types));
    }

    public Stream<OWLAxiom> axioms() {
        return Stream.concat(staticAxioms(), refutableAxioms());
    }

    public Stream<OWLAxiom> axioms(final AxiomType<?>... types) {
        return axioms().filter(axiom -> axiom.isOfType(types));
    }

    public Stream<OWLAxiom> axioms(final Collection<AxiomType<?>> types) {
        return axioms().filter(axiom -> axiom.isOfType(types));
    }

    public Stream<OWLLogicalAxiom> logicalAxioms() {
        return axioms().filter(axiom -> axiom.isLogicalAxiom()).map(axiom -> (OWLLogicalAxiom) axiom);
    }

    public Stream<OWLAxiom> nonLogicalAxioms() {
        return axioms().filter(axiom -> !axiom.isLogicalAxiom()).map(axiom -> (OWLAxiom) axiom);
    }

    public Stream<OWLAxiom> tboxAxioms() {
        return axioms().filter(axiom -> axiom.isOfType(AxiomType.TBoxAxiomTypes));
    }

    public Stream<OWLAxiom> aboxAxioms() {
        return axioms().filter(axiom -> axiom.isOfType(AxiomType.ABoxAxiomTypes));
    }

    public Stream<OWLAxiom> rboxAxioms() {
        return axioms().filter(axiom -> axiom.isOfType(AxiomType.RBoxAxiomTypes));
    }

    public void removeAxioms(final Stream<? extends OWLAxiom> axioms) {
        axioms.forEach(axiom -> {
            staticAxioms.remove(axiom);
            refutableAxioms.remove(axiom);
        });
    }

    public void addStaticAxioms(final Stream<? extends OWLAxiom> axioms) {
        axioms.forEach(axiom -> {
            refutableAxioms.remove(axiom);
            staticAxioms.add(axiom);
        });
    }

    public void addAxioms(final Stream<? extends OWLAxiom> axioms) {
        axioms.forEach(axiom -> {
            staticAxioms.remove(axiom);
            refutableAxioms.add(axiom);
        });
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
    public static OWLAnnotationProperty getOriginAnnotationProperty() {
        return getDefaultDataFactory().getOWLAnnotationProperty("origin");
    }

    private static OWLAnnotation getNewOriginAnnotation(final OWLAxiom origin) {
        final var df = getDefaultDataFactory();
        return df.getOWLAnnotation(getOriginAnnotationProperty(), df.getOWLLiteral(origin.toString()));
    }

    /**
     * @return The annotations of {@code axiom}.
     */
    public static Stream<OWLAnnotation> axiomOriginAnnotations(final OWLAxiom axiom) {
        if (axiom.annotations(getOriginAnnotationProperty()).count() > 0) {
            return axiom.annotations(getOriginAnnotationProperty());
        } else {
            return Stream.of(getNewOriginAnnotation(axiom));
        }
    }

    /**
     * Annotate {@code axiom} with the origin annotation indicating that the origin
     * of {@code axiom} is {@code origin}.
     *
     * @param axiom
     * @param origin
     * @return A new annotated axiom equivalent to {@code axiom}.
     */
    public static OWLAxiom getOriginAnnotatedAxiom(final OWLAxiom axiom, final OWLAxiom origin) {
        if (axiom.equals(origin)) {
            return axiom;
        } else {
            return axiom.getAnnotatedAxiom(axiomOriginAnnotations(origin));
        }
    }

    public void replaceAxiom(final OWLAxiom remove, final Stream<? extends OWLAxiom> replacement) {
        removeAxioms(remove);
        addAxioms(replacement.map(a -> getOriginAnnotatedAxiom(a, remove)));
    }

    public void replaceAxiom(final OWLAxiom remove, final Collection<? extends OWLAxiom> replacement) {
        replaceAxiom(remove, replacement.stream());
    }

    public void replaceAxiom(final OWLAxiom remove, final OWLAxiom... replacement) {
        replaceAxiom(remove, Stream.of(replacement));
    }

    /**
     * @param remove
     * @return The axioms of the ontology without those in {@code remove}.
     */
    public Set<OWLAxiom> complement(final Set<OWLAxiom> remove) {
        return axioms().filter(axiom -> !remove.contains(axiom)).collect(Collectors.toSet());
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
     *            The reasoner to dispose of.
     */
    public void disposeOwlReasoner(final OWLReasoner reasoner) {
        reasonerCache.disposeOwlReasoner(reasoner);
    }

    private <T> T withReasonerDo(final Function<OWLReasoner, T> action) {
        return reasonerCache.withReasonerDo(this, action);
    }

    private <T> T withOwlOntologyDo(final Function<OWLOntology, T> action) {
        return reasonerCache.withReasonerDo(this, reasoner -> action.apply(reasoner.getRootOntology()));
    }

    public boolean isConsistent() {
        return withReasonerDo(reasoner -> reasoner.isConsistent());
    }

    public boolean isCoherent() {
        return withReasonerDo(reasoner -> reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom().isEmpty());
    }

    public boolean isEntailed(final OWLAxiom... axioms) {
        return withReasonerDo(reasoner -> reasoner.isEntailed(axioms));
    }

    public boolean isEntailed(final Stream<? extends OWLAxiom> axioms) {
        return withReasonerDo(reasoner -> reasoner.isEntailed(axioms));
    }

    public boolean isEntailed(final Ontology other) {
        return isEntailed(other.logicalAxioms());
    }

    public boolean isSatisfiable(final OWLClassExpression concepts) {
        return withReasonerDo(reasoner -> reasoner.isSatisfiable(concepts));
    }

    public List<OWLProfileReport> checkOwlProfiles() {
        return withOwlOntologyDo(ontology -> Arrays.stream(Profiles.values())
                .map(profile -> profile.checkOntology(ontology))
                .toList());
    }

    public Stream<Set<OWLAxiom>> maximalConsistentSubsets() {
        return (new MaximalConsistentSubsets(this)).stream();
    }

    public Stream<Set<OWLAxiom>> maximalConsistentSubsets(final Predicate<Ontology> isRepaired) {
        return (new MaximalConsistentSubsets(this, isRepaired)).stream();
    }

    public Stream<Set<OWLAxiom>> largestMaximalConsistentSubsets(final Predicate<Ontology> isRepaired) {
        return (new MaximalConsistentSubsets(this, isRepaired, true)).stream();
    }

    /**
     * @return A stream of all sets of axioms that when removed from the ontology
     *         yield an optimal classical repair for consistency of the ontology.
     */
    public Stream<Set<OWLAxiom>> minimalCorrectionSubsets() {
        return (new MaximalConsistentSubsets(this)).correctionStream();
    }

    public Stream<Set<OWLAxiom>> minimalCorrectionSubsets(final Predicate<Ontology> isRepaired) {
        return (new MaximalConsistentSubsets(this)).correctionStream();
    }

    public Stream<Set<OWLAxiom>> smallestMinimalCorrectionSubsets(final Predicate<Ontology> isRepaired) {
        return (new MaximalConsistentSubsets(this)).correctionStream();
    }

    /**
     * @return A single maximal consistent subset.
     */
    public Set<OWLAxiom> maximalConsistentSubset(final Predicate<Ontology> isRepaired) {
        return complement(minimalCorrectionSubset(isRepaired));
    }

    /**
     * @return A single minimal correction subset.
     */
    public Set<OWLAxiom> minimalCorrectionSubset(final Predicate<Ontology> isRepaired) {
        return MinimalSubsets.getRandomizedMinimalSubset(refutableAxioms,
                axioms -> isRepaired.test(new Ontology(staticAxioms, complement(axioms), reasonerCache)));
    }

    /**
     * @return A single set with the refutable axioms of a minimal unsatisfiable
     *         subset.
     */
    public Set<OWLAxiom> minimalUnsatisfiableSubset(final Predicate<Ontology> isRepaired) {
        return MinimalSubsets.getRandomizedMinimalSubset(refutableAxioms,
                axioms -> !isRepaired.test(new Ontology(staticAxioms, axioms, reasonerCache)));
    }

    /**
     * @return A single maximal consistent subset.
     */
    public Stream<Set<OWLAxiom>> someMaximalConsistentSubsets(final Predicate<Ontology> isRepaired) {
        return someMinimalCorrectionSubsets(isRepaired).map(this::complement);
    }

    /**
     * @return A single minimal correction subset.
     */
    public Stream<Set<OWLAxiom>> someMinimalCorrectionSubsets(final Predicate<Ontology> isRepaired) {
        return MinimalSubsets.randomizedMinimalSubsets(refutableAxioms, 8,
                axioms -> isRepaired.test(new Ontology(staticAxioms, complement(axioms), reasonerCache)));
    }

    /**
     * @return A single set with the refutable axioms of a minimal unsatisfiable
     *         subset.
     */
    public Stream<Set<OWLAxiom>> someMinimalUnsatisfiableSubsets(final Predicate<Ontology> isRepaired) {
        return MinimalSubsets.randomizedMinimalSubsets(refutableAxioms, 8,
                axioms -> !isRepaired.test(new Ontology(staticAxioms, axioms, reasonerCache)));
    }

    /**
     * @return A stream providing all subconcepts used in the ontology.
     */
    public Stream<OWLClassExpression> subConcepts() {
        return axioms().flatMap(OWLAxiom::nestedClassExpressions);
    }

    /**
     * @return A stream containing all non-simple roles.
     */
    public Stream<OWLObjectProperty> nonSimpleRoles() {
        return withOwlOntologyDo(ontology -> (new OWLObjectPropertyManager(ontology)).getNonSimpleProperties()).stream()
                .map(role -> role.getNamedProperty()).distinct();
    }

    /**
     * @return A stream containing all simple roles.
     */
    public Stream<OWLObjectProperty> simpleRoles() {
        final var nonSimple = withOwlOntologyDo(
                ontology -> (new OWLObjectPropertyManager(ontology)).getNonSimpleProperties()).stream()
                .map(role -> role.getNamedProperty()).collect(Collectors.toSet());
        return rolesInSignature().filter(role -> !nonSimple.contains(role));
    }

    /**
     * @return A stream providing all subconcepts used in the ontology's TBox.
     */
    public Stream<OWLClassExpression> subConceptsOfTbox() {
        return tboxAxioms().flatMap(OWLAxiom::nestedClassExpressions);
    }

    /**
     * @return A stream containing all entities in the signature of this ontology.
     */
    public Stream<OWLEntity> signature() {
        return axioms().flatMap(OWLAxiom::signature);
    }

    /**
     * @return A stream containing all concept names in the signature of this
     *         ontology.
     */
    public Stream<OWLClass> conceptsInSignature() {
        return axioms().flatMap(OWLAxiom::classesInSignature);
    }

    /**
     * @return A stream containing all roles in the signature of this ontology.
     */
    public Stream<OWLObjectProperty> rolesInSignature() {
        return axioms().flatMap(OWLAxiom::objectPropertiesInSignature);
    }

    /**
     * @return The stream of C1 subclass C2 axioms, C1 and C2 classes in the
     *         signature
     *         of {@code ontology}, entailed by {@code ontology}.
     */
    public Stream<OWLSubClassOfAxiom> inferredTaxonomyAxioms() {
        return withReasonerDo(reasoner -> {
            final var df = getDefaultDataFactory();
            final var ontology = reasoner.getRootOntology();
            final var isConsistent = reasoner.isConsistent();
            return ontology.classesInSignature()
                    .flatMap(left -> ontology.classesInSignature()
                            .map(right -> df.getOWLSubClassOfAxiom(left, right))
                            .filter(axiom -> !isConsistent || reasoner.isEntailed(axiom)))
                    .collect(Collectors.toSet());
        }).stream();
    }

    /**
     * Clone this ontology, but give it a cache using the hermit reasoner.
     *
     * @return The new ontology.
     */
    public Ontology cloneWithHermit() {
        final var newReasonerCache = new CachedReasoner(new ReasonerFactory());
        return new Ontology(staticAxioms, refutableAxioms, newReasonerCache);
    }

    /**
     * Clone this ontology, but give it a cache using the openllet reasoner.
     *
     * @return The new ontology.
     */
    public Ontology cloneWithOpenllet() {
        final var newReasonerCache = new CachedReasoner(OpenlletReasonerFactory.getInstance());
        return new Ontology(staticAxioms, refutableAxioms, newReasonerCache);
    }

    /**
     * Clone this ontology, but give it a cache using the jfact reasoner.
     *
     * @return The new ontology.
     */
    public Ontology cloneWithJFact() {
        final var newReasonerCache = new CachedReasoner(new JFactFactory());
        return new Ontology(staticAxioms, refutableAxioms, newReasonerCache);
    }

    /**
     * Clone this ontology, but only retain axioms in {@code axioms}.
     *
     * @param axioms
     *            The axioms that should be retained.
     * @return The new ontology.
     */
    public Ontology cloneWith(final Set<? extends OWLAxiom> axioms) {
        return new Ontology(staticAxioms.stream().filter(axiom -> axioms.contains(axiom)).toList(),
                refutableAxioms.stream().filter(axiom -> axioms.contains(axiom)).toList(), reasonerCache);
    }

    /**
     * Clone this ontology, but give it a separate reasoner.
     *
     * @return The new ontology.
     */
    public Ontology cloneWithSeparateCache() {
        return new Ontology(staticAxioms, refutableAxioms, new CachedReasoner(reasonerCache.reasonerFactory));
    }

    @Override
    public Ontology clone() {
        return new Ontology(staticAxioms, refutableAxioms, reasonerCache);
    }

    @Override
    public void close() {
        reasonerCache.removeReference(this);
    }
}
