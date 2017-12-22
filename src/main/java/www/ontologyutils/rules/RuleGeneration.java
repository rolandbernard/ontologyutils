package www.ontologyutils.rules;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import uk.ac.manchester.cs.owl.owlapi.OWLObjectAllValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectSomeValuesFromImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLQuantifiedRestrictionImpl;
import www.ontologyutils.normalization.NormalForm;

public class RuleGeneration {

	private Map<OWLEntity, String> map;

	public RuleGeneration(OWLOntology ontology) {
		map = mapEntitiesToNumberedLetters(ontology);
	}

	@SuppressWarnings({ "unchecked", "unlikely-arg-type" })
	public String normalizedSubClassAxiomToRule(OWLAxiom ax) {

		if (!NormalForm.isNormalFormTBoxAxiom(ax)) {
			throw new InvalidParameterException("Axiom " + ax + " must be in normal form.");
		}

		OWLClassExpression left = ((OWLSubClassOfAxiom) ax).getSubClass();
		OWLClassExpression right = ((OWLSubClassOfAxiom) ax).getSuperClass();

		if (NormalForm.typeOneSubClassAxiom(left, right)) {
			// (isAtom(left) || isConjunctionOfAtoms(left)) && (isAtom(right) ||
			// isDisjunctionOfAtoms(right))
			String res = "a(1, ";
			if (NormalForm.isAtom(left)) {
				res += map.get(left) + ", " + map.get(left);
			} else {
				boolean first = true;
				for (OWLClassExpression e : left.asConjunctSet()) {
					if (first) {
						res += map.get(e);
						first = false;
					} else {
						res += ", " + map.get(e);
					}
				}
			}
			res += ", (";
			if (NormalForm.isAtom(right)) {
				res += map.get(right) + ",";
			} else {
				boolean first = true;
				for (OWLClassExpression e : right.asDisjunctSet()) {
					if (first) {
						res += map.get(e);
						first = false;
					} else {
						res += ", " + map.get(e);
					}
				}
			}
			res += ")).";
			return res;
		} else if (NormalForm.typeTwoSubClassAxiom(left, right)) {
			// isAtom(left) && isExistentialOfAtom(right)
			OWLClassExpression filler = ((OWLQuantifiedRestrictionImpl<OWLClassExpression>) right).getFiller();
			OWLObjectPropertyExpression property = ((OWLObjectSomeValuesFromImpl) right).getProperty();
			return "a(2, " + map.get(left) + ", " + map.get(property) + ", " + map.get(filler) + ").";
		} else if (NormalForm.typeThreeSubClassAxiom(left, right)) {
			// isAtom(left) && isUniversalOfAtom(right)
			OWLClassExpression filler = ((OWLQuantifiedRestrictionImpl<OWLClassExpression>) right).getFiller();
			OWLObjectPropertyExpression property = ((OWLObjectAllValuesFromImpl) right).getProperty();
			return "a(3, " + map.get(left) + ", " + map.get(property) + ", " + map.get(filler) + ").";
		} else if (NormalForm.typeFourSubClassAxiom(left, right)) {
			// isExistentialOfAtom(left) && isAtom(right)
			OWLClassExpression filler = ((OWLQuantifiedRestrictionImpl<OWLClassExpression>) left).getFiller();
			OWLObjectPropertyExpression property = ((OWLObjectSomeValuesFromImpl) left).getProperty();
			return "a(4, " + map.get(filler) + ", " + map.get(property) + ", " + map.get(right) + ").";
		} else {
			throw new RuntimeException("I don't know what to do with " + ax);
		}
	}

	public String entityToRule(OWLEntity e) {
		if (e.isOWLClass()) {
			return "nc(" + map.get(e) + ").";
		} else if (e.isOWLObjectProperty()) {
			return "nr(" + map.get(e) + ").";
		}
		throw new IllegalArgumentException();
	}
	
	public  Map<OWLEntity, String> getMap() {
		return map;
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

}
