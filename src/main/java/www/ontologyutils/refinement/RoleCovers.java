package www.ontologyutils.refinement;

import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import www.ontologyutils.toolbox.Ontology;

/**
 * Implements the upward and downward cover operations for roles. This object
 * must be closed after use to free up all resources associated with the
 * internal {@code OWLReasoner} and {@code OWLOntology}.
 *
 * The implementation is based on the approach presented in Confalonieri, R.,
 * Galliani, P., Kutz, O., Porello, D., Righetti, G., & Toquard, N. (2020).
 * Towards even more irresistible axiom weakening.
 */
public class RoleCovers implements AutoCloseable {
    public final Ontology refOntology;
    public final Set<OWLObjectProperty> simpleRoles;
    public final Set<OWLObjectProperty> nonSimpleRoles;
    public OWLReasoner reasoner;

    /**
     * Creates a new {@code RoleCover} object for the given reference object.
     *
     * @param refOntology
     *                    The ontology used for entailment check.
     */
    public RoleCovers(final Ontology refOntology) {
        this.refOntology = refOntology;
        this.reasoner = refOntology.getOwlReasoner();
        this.simpleRoles = refOntology.rolesInSignature().collect(Collectors.toSet());
        this.nonSimpleRoles = new HashSet<>();
        computeNonSimpleRoles();
    }

    /**
     * Move all roles from {@code simpleRoles} to {@code nonSimpleRoles} if they are
     * determined to me non-simple.
     */
    private void computeNonSimpleRoles() {
        // TODO: move this into seperate class with visitor and make it accesible via a method on Ontology?
        final OWLDataFactory df = Ontology.getDefaultDataFactory();
        simpleRoles.remove(df.getOWLTopObjectProperty());
        nonSimpleRoles.add(df.getOWLTopObjectProperty());
        simpleRoles.remove(df.getOWLBottomObjectProperty());
        nonSimpleRoles.add(df.getOWLBottomObjectProperty());
        final var references = simpleRoles.stream()
                .collect(Collectors.toMap(Function.identity(),
                        role -> refOntology.rboxAxioms().filter(axiom -> axiom.containsEntityInSignature(role))
                                .toList()));
        final var toCheck = new HashSet<>(refOntology.tboxAxioms().toList());
        while (!toCheck.isEmpty()) {
            final var newNonSimple = new HashSet<OWLObjectProperty>();
            for (final var axiom : toCheck) {
                if (axiom.getAxiomType() == AxiomType.SUB_OBJECT_PROPERTY) {
                    final var ax = (OWLSubObjectPropertyOfAxiom) axiom;
                    if (nonSimpleRoles.contains(ax.getSubProperty().getNamedProperty())) {
                        newNonSimple.add(ax.getSuperProperty().getNamedProperty());
                    }
                } else if (axiom.getAxiomType() == AxiomType.SUB_PROPERTY_CHAIN_OF) {
                    final var ax = (OWLSubPropertyChainOfAxiom) axiom;
                    if (ax.getPropertyChain().size() > 1
                            || nonSimpleRoles.contains(ax.getPropertyChain().get(0).getNamedProperty())) {
                        newNonSimple.add(ax.getSuperProperty().getNamedProperty());
                    }
                } else if (axiom.getAxiomType() == AxiomType.TRANSITIVE_OBJECT_PROPERTY) {
                    final var ax = (OWLTransitiveObjectPropertyAxiom) axiom;
                    newNonSimple.add(ax.getProperty().getNamedProperty());
                } else if (axiom.getAxiomType() == AxiomType.EQUIVALENT_OBJECT_PROPERTIES) {
                    final var ax = (OWLEquivalentObjectPropertiesAxiom) axiom;
                    if (ax.properties().anyMatch(role -> nonSimpleRoles.contains(role.getNamedProperty()))) {
                        ax.properties().map(OWLObjectPropertyExpression::getNamedProperty).forEach(newNonSimple::add);
                    }
                } else if (axiom.getAxiomType() == AxiomType.INVERSE_OBJECT_PROPERTIES) {
                    final var ax = (OWLInverseObjectPropertiesAxiom) axiom;
                    if (ax.properties().anyMatch(role -> nonSimpleRoles.contains(role.getNamedProperty()))) {
                        ax.properties().map(OWLObjectPropertyExpression::getNamedProperty).forEach(newNonSimple::add);
                    }
                }
            }
            toCheck.clear();
            for (final var role : newNonSimple) {
                if (simpleRoles.contains(role)) {
                    simpleRoles.remove(role);
                    nonSimpleRoles.add(role);
                    toCheck.addAll(references.get(role));
                }
            }
        }
    }

    /**
     * @param subclass
     * @param superclass
     * @return True iff the reference ontology of this cover entails that
     *         {@code subclass} is subsumed by {@code superclass}.
     */
    private boolean isSubclass(final OWLObjectPropertyExpression subclass,
            final OWLObjectPropertyExpression superclass) {
        final var df = Ontology.getDefaultDataFactory();
        final var testAxiom = df.getOWLSubObjectPropertyOfAxiom(subclass, superclass);
        return reasoner.isEntailed(testAxiom);
    }

    /**
     * For this function, a class A is a strict subclass of B iff A
     * isSubObjectPropertyOf B is entailed but B isSubObjectPropertyOf A is not.
     *
     * @param subclass
     * @param superclass
     * @return True iff the reference ontology of this cover entails that
     *         {@code subclass} is strictly subsumed by {@code superclass}.
     */
    private boolean isStrictSubclass(final OWLObjectPropertyExpression subclass,
            final OWLObjectPropertyExpression superclass) {
        return isSubclass(subclass, superclass) && !isSubclass(superclass, subclass);
    }

    /**
     * @param concept
     * @param candidate
     * @return True iff {@code candidate} is in the upward cover of {@code concept}.
     */
    private boolean isInUpCover(final OWLObjectPropertyExpression concept,
            final OWLObjectPropertyExpression candidate) {
        if (!simpleRoles.contains(candidate) || !isSubclass(concept, candidate)) {
            return false;
        } else {
            return !simpleRoles.stream().parallel()
                    .anyMatch(other -> isStrictSubclass(concept, other) && isStrictSubclass(other, candidate));
        }
    }

    /**
     * @param concept
     * @return All concepts that are in the upward cover of {@code concept}.
     */
    public Stream<OWLObjectPropertyExpression> upCover(final OWLObjectPropertyExpression concept) {
        return simpleRoles.stream().flatMap(role -> Stream.of(role, role.getInverseProperty())).parallel()
                .filter(candidate -> isInUpCover(concept, candidate));
    }

    /**
     * @param concept
     * @param candidate
     * @return True iff {@code candidate} is in the downward cover of
     *         {@code concept}.
     */
    private boolean isInDownCover(final OWLObjectPropertyExpression concept,
            final OWLObjectPropertyExpression candidate) {
        if (!simpleRoles.contains(candidate) || !isSubclass(candidate, concept)) {
            return false;
        } else {
            return !simpleRoles.stream().parallel()
                    .anyMatch(other -> isStrictSubclass(candidate, other) && isStrictSubclass(other, concept));
        }
    }

    /**
     * @param concept
     * @return All concepts that are in the downward cover of {@code concept}.
     */
    public Stream<OWLObjectPropertyExpression> downCover(final OWLObjectPropertyExpression concept) {
        return simpleRoles.stream().flatMap(role -> Stream.of(role, role.getInverseProperty())).parallel()
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
