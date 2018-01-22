package www.ontologyutils.rules;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;

import uk.ac.manchester.cs.owl.owlapi.OWLObjectAllValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectSomeValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLQuantifiedRestrictionImpl;
import www.ontologyutils.normalization.NormalForm;
import www.ontologyutils.toolbox.AnnotateOrigin;

public class RuleGeneration {

	private Map<OWLEntity, String> mapEntities;
	private Map<Collection<OWLAnnotation>, String> mapAxioms;

	public RuleGeneration(OWLOntology ontology) {
		mapEntities = mapEntitiesToNumberedLetters(ontology);
		mapAxioms = mapAxiomsToGroupNumbers(ontology);
	}

	@SuppressWarnings({ "unchecked", "unlikely-arg-type" })
	public String normalizedSubClassAxiomToRule(OWLAxiom ax) {

		if (!NormalForm.isNormalFormTBoxAxiom(ax)) {
			throw new InvalidParameterException("Axiom " + ax + " must be in normal form.");
		}

		// a "group of axioms" is identified with the annotation of the original axiom
		String axiomGroup = mapAxioms.get(AnnotateOrigin.getAxiomAnnotations(ax));

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
			OWLClassExpression filler = ((OWLQuantifiedRestrictionImpl<OWLClassExpression>) right).getFiller();
			OWLObjectPropertyExpression property = ((OWLObjectSomeValuesFromImpl) right).getProperty();
			return "a(2, " + axiomGroup + ", " + mapEntities.get(left) + ", " + mapEntities.get(property) + ", "
					+ mapEntities.get(filler) + ").";
		} else if (NormalForm.typeThreeSubClassAxiom(left, right)) {
			// isAtom(left) && isUniversalOfAtom(right)
			OWLClassExpression filler = ((OWLQuantifiedRestrictionImpl<OWLClassExpression>) right).getFiller();
			OWLObjectPropertyExpression property = ((OWLObjectAllValuesFromImpl) right).getProperty();
			return "a(3, " + axiomGroup + ", " + mapEntities.get(left) + ", " + mapEntities.get(property) + ", "
					+ mapEntities.get(filler) + ").";
		} else if (NormalForm.typeFourSubClassAxiom(left, right)) {
			// isExistentialOfAtom(left) && isAtom(right)
			OWLClassExpression filler = ((OWLQuantifiedRestrictionImpl<OWLClassExpression>) left).getFiller();
			OWLObjectPropertyExpression property = ((OWLObjectSomeValuesFromImpl) left).getProperty();
			return "a(4, " + axiomGroup + ", " + mapEntities.get(filler) + ", " + mapEntities.get(property) + ", "
					+ mapEntities.get(right) + ").";
		} else {
			throw new RuntimeException("I don't know what to do with " + ax);
		}
	}

	public String entityToRule(OWLEntity e) {
		if (e.isOWLClass()) {
			return "nc(" + mapEntities.get(e) + ").";
		} else if (e.isOWLObjectProperty()) {
			return "nr(" + mapEntities.get(e) + ").";
		}
		throw new IllegalArgumentException();
	}

	public Map<OWLEntity, String> getMapEntities() {
		return mapEntities;
	}

	public Map<Collection<OWLAnnotation>, String> getMapAxioms() {
		return mapAxioms;
	}

	private static Map<OWLEntity, String> mapEntitiesToNumberedLetters(OWLOntology ontology) {
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

	private static Map<Collection<OWLAnnotation>, String> mapAxiomsToGroupNumbers(OWLOntology ontology) {
		HashMap<Collection<OWLAnnotation>, String> map = new HashMap<Collection<OWLAnnotation>, String>();

		int numAxGr = 0;
		for (OWLAxiom ax : ontology.tboxAxioms(Imports.EXCLUDED).collect(Collectors.toSet())) {
			assert (ax.isAnnotated());
			if (!map.containsKey(AnnotateOrigin.getAxiomAnnotations(ax))) {
				map.put(AnnotateOrigin.getAxiomAnnotations(ax), "" + ++numAxGr);
			}
		}

		return map;
	}

}
