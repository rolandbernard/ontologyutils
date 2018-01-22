package www.ontologyutils.repair;

import java.util.Set;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import www.ontologyutils.toolbox.MaximalConsistentSets;
import www.ontologyutils.toolbox.SetUtils;
import www.ontologyutils.toolbox.Utils;

/**
 * A simple implementation of {@code OntologyRepair}. It repairs an inconsistent
 * ontology into an ontology made of a randomly chosen maximally consistent set
 * of axioms in the input ontology.
 *
 */
public class OntologyRepairRandomMCS implements OntologyRepair {

	private Set<Set<OWLAxiom>> mcss;

	public OntologyRepairRandomMCS(OWLOntology ontology) {
		Set<OWLAxiom> originalAxioms = ontology.axioms().collect(Collectors.toSet());
		this.mcss = MaximalConsistentSets.maximalConsistentSubsets(originalAxioms);
	}

	@Override
	public OWLOntology repair() {
		return Utils.newOntology(SetUtils.getRandom(mcss));
	}

}
