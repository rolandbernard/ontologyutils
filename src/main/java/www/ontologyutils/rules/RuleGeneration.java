package www.ontologyutils.rules;

import java.util.*;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.*;

import www.ontologyutils.normalization.NormalForm;
import www.ontologyutils.toolbox.Ontology;

/**
 * Class that implements role generation.
 */
public class RuleGeneration {
    private Map<OWLEntity, String> mapEntities;
    private Map<Collection<OWLAnnotation>, String> mapAxioms;

    /**
     * @param ontology
     *            The ontology to get entities and axioms from.
     */
    public RuleGeneration(Ontology ontology) {
        mapEntities = mapEntitiesToNumberedLetters(ontology);
        mapAxioms = mapAxiomsToGroupNumbers(ontology);
    }

    /**
     * @param ax
     *            The axiom to convert.
     * @return A rule string for the axiom.
     */
    @SuppressWarnings({ "unchecked", "unlikely-arg-type" })
    public String normalizedSubClassAxiomToRule(OWLAxiom ax) {
        if (!NormalForm.isNormalFormTBoxAxiom(ax)) {
            throw new IllegalArgumentException("Axiom " + ax + " must be in normal form.");
        }
        // a "group of axioms" is identified with the annotation of the original axiom
        String axiomGroup = mapAxioms.get(Ontology.axiomOriginAnnotations(ax).collect(Collectors.toSet()));
        OWLClassExpression left = ((OWLSubClassOfAxiom) ax).getSubClass();
        OWLClassExpression right = ((OWLSubClassOfAxiom) ax).getSuperClass();
        if (NormalForm.typeOneSubClassAxiom(left, right)) {
            // (isAtom(left) || isConjunctionOfAtoms(left)) && (isAtom(right) ||
            // isDisjunctionOfAtoms(right))
            String res = "a(1, " + axiomGroup + ", ";
            if (NormalForm.isAtom(left)) {
                res += mapEntities.get(left) + ", " + mapEntities.get(left);
            } else {
                boolean first = true;
                for (OWLClassExpression e : left.asConjunctSet()) {
                    if (first) {
                        res += mapEntities.get(e);
                        first = false;
                    } else {
                        res += ", " + mapEntities.get(e);
                    }
                }
            }
            res += ", (";
            if (NormalForm.isAtom(right)) {
                res += mapEntities.get(right) + ",";
            } else {
                boolean first = true;
                for (OWLClassExpression e : right.asDisjunctSet()) {
                    if (first) {
                        res += mapEntities.get(e);
                        first = false;
                    } else {
                        res += ", " + mapEntities.get(e);
                    }
                }
            }
            res += ")).";
            return res;
        } else if (NormalForm.typeTwoSubClassAxiom(left, right)) {
            // isAtom(left) && isExistentialOfAtom(right)
            OWLClassExpression filler = ((OWLQuantifiedRestriction<OWLClassExpression>) right).getFiller();
            OWLObjectPropertyExpression property = ((OWLObjectSomeValuesFrom) right).getProperty();
            return "a(2, " + axiomGroup + ", " + mapEntities.get(left) + ", " + mapEntities.get(property) + ", "
                    + mapEntities.get(filler) + ").";
        } else if (NormalForm.typeThreeSubClassAxiom(left, right)) {
            // isAtom(left) && isUniversalOfAtom(right)
            OWLClassExpression filler = ((OWLQuantifiedRestriction<OWLClassExpression>) right).getFiller();
            OWLObjectPropertyExpression property = ((OWLObjectAllValuesFrom) right).getProperty();
            return "a(3, " + axiomGroup + ", " + mapEntities.get(left) + ", " + mapEntities.get(property) + ", "
                    + mapEntities.get(filler) + ").";
        } else if (NormalForm.typeFourSubClassAxiom(left, right)) {
            // isExistentialOfAtom(left) && isAtom(right)
            OWLClassExpression filler = ((OWLQuantifiedRestriction<OWLClassExpression>) left).getFiller();
            OWLObjectPropertyExpression property = ((OWLObjectSomeValuesFrom) left).getProperty();
            return "a(4, " + axiomGroup + ", " + mapEntities.get(filler) + ", " + mapEntities.get(property) + ", "
                    + mapEntities.get(right) + ").";
        } else {
            throw new RuntimeException("I don't know what to do with " + ax);
        }
    }

    /**
     * @param e
     *            The entity.
     * @return The role for the entity.
     */
    public String entityToRule(OWLEntity e) {
        if (e.isOWLClass()) {
            return "nc(" + mapEntities.get(e) + ").";
        } else if (e.isOWLObjectProperty()) {
            return "nr(" + mapEntities.get(e) + ").";
        }
        throw new IllegalArgumentException();
    }

    /**
     * @return The map of entities to rules.
     */
    public Map<OWLEntity, String> getMapEntities() {
        return mapEntities;
    }

    /**
     * @return The map of axioms to rules.
     */
    public Map<Collection<OWLAnnotation>, String> getMapAxioms() {
        return mapAxioms;
    }

    private static Map<OWLEntity, String> mapEntitiesToNumberedLetters(Ontology ontology) {
        HashMap<OWLEntity, String> map = new HashMap<OWLEntity, String>();
        Object[] entities = ontology.signature().toArray();
        int numClass = 0;
        int numProp = 0;
        for (int i = 0; i < entities.length; i++) {
            if (!map.containsKey(entities[i])) {
                if (((OWLEntity) entities[i]).isOWLClass()) {
                    map.put((OWLEntity) entities[i], "a" + ++numClass);
                } else if (((OWLEntity) entities[i]).isOWLObjectProperty()) {
                    map.put((OWLEntity) entities[i], "r" + ++numProp);
                }
            }
        }
        return map;
    }

    private static Map<Collection<OWLAnnotation>, String> mapAxiomsToGroupNumbers(Ontology ontology) {
        HashMap<Collection<OWLAnnotation>, String> map = new HashMap<Collection<OWLAnnotation>, String>();
        int numAxGr = 0;
        for (OWLAxiom ax : ontology.tboxAxioms().collect(Collectors.toSet())) {
            assert (ax.isAnnotated());
            var annotations = Ontology.axiomOriginAnnotations(ax).collect(Collectors.toSet());
            if (!map.containsKey(annotations)) {
                map.put(annotations, "" + ++numAxGr);
            }
        }
        return map;
    }
}
