package www.ontologyutils.toolbox;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLEquivalentClassesAxiomImpl;

public class FreshAtoms {

	private static Set<OWLAxiom> freshAtomsEquivalenceAxioms = new HashSet<>();

	/**
	 * @return the set of equivalence axioms resulting from the creation of fresh
	 *         atoms.
	 */
	public static Set<OWLAxiom> getFreshAtomsEquivalenceAxioms() {
		return freshAtomsEquivalenceAxioms;
	}

	/**
	 * Empties the set of equivalence axioms resulting from the creation of fresh
	 * atoms.
	 */
	public static void resetFreshAtomsEquivalenceAxioms() {
		freshAtomsEquivalenceAxioms = new HashSet<>();
	}

	/**
	 * @param e
	 * @return a fresh {@code OWLClassExpression} with name "#FRESH[string
	 *         representing {@code e}]"
	 */
	public static OWLClassExpression createFreshAtomCopy(OWLClassExpression e) {
		return createFreshAtomCopy(e, "FRESH");
	}

	/**
	 * @param e
	 * @param tag
	 * @return a fresh {@code OWLClassExpression} with name "#tag[string
	 *         representing {@code e}]"
	 */
	public static OWLClassExpression createFreshAtomCopy(OWLClassExpression e, String tag) {
		OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();
		String freshName = "#[" + e + "]";
		OWLClassExpression fresh = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create(tag + freshName));

		Collection<OWLClassExpression> equiv = new ArrayList<>();
		equiv.add(e);
		equiv.add(fresh);
		freshAtomsEquivalenceAxioms.add((new OWLEquivalentClassesAxiomImpl(equiv, new ArrayList<OWLAnnotation>())));

		return fresh;
	}
}
