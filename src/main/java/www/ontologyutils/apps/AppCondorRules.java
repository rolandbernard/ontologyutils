package www.ontologyutils.apps;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import uk.ac.manchester.cs.owl.owlapi.OWLObjectIntersectionOfImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;
import www.ontologyutils.normalization.NormalForm;
import www.ontologyutils.normalization.Normalization;
import www.ontologyutils.normalization.NormalizationTools;
import www.ontologyutils.rules.RuleGeneration;
import www.ontologyutils.toolbox.AnnotateOrigin;
import www.ontologyutils.toolbox.FreshAtoms;
import www.ontologyutils.toolbox.Utils;

public class AppCondorRules {
	private OWLOntology ontology;

	public AppCondorRules(String ontologyFilePath) {

		ontology = AnnotateOrigin.newOntology(ontologyFilePath);

	}

	private OWLOntology runCondor() {
		FreshAtoms.resetFreshAtomsEquivalenceAxioms(); // optional; for verification purpose

		OWLOntology copy = Utils.newEmptyOntology();
		copy.addAxioms(this.ontology.axioms());

		Stream<OWLAxiom> tBoxAxioms = copy.tboxAxioms(Imports.EXCLUDED);
		tBoxAxioms.forEach((ax) -> {
			copy.remove(ax);
			copy.addAxioms(NormalizationTools.asSubClassOfAxioms(ax));
		});

		OWLOntology condor = null;
		// condor = Normalization.normalizeCondor(copy);
		condor = superNormalize(Normalization.normalizeCondor(copy));

		// check every axiom of the original ontology is entailed in condor
		OWLReasoner reasoner = Utils.getHermitReasoner(condor);
		assert (this.ontology.axioms().allMatch(ax -> reasoner.isEntailed(ax)));
		// check every axiom of condor is entailed in the copy of the original ontology
		// with extended signature
		copy.addAxioms(FreshAtoms.getFreshAtomsEquivalenceAxioms());
		OWLReasoner reasonerBis = Utils.getHermitReasoner(copy);
		assert (condor.axioms().allMatch(ax -> reasonerBis.isEntailed(ax)));

		return condor;
	}

	/**
	 * @param on
	 *            an ontology in normal form
	 * @return an equivalent ontology where type-1 rules have at most 2 conjuncts on
	 *         the left.
	 */

	private static OWLOntology superNormalize(OWLOntology on) {
		OWLOntology res = Utils.newEmptyOntology();
		on.tboxAxioms(Imports.EXCLUDED).forEach(a -> {
			res.addAxioms(superNormalize(a));
		});
		res.addAxioms(on.rboxAxioms(Imports.EXCLUDED));
		res.addAxioms(on.aboxAxioms(Imports.EXCLUDED));

		return res;
	}

	private static Set<OWLAxiom> superNormalize(OWLAxiom a) {
		Set<OWLAxiom> res = new HashSet<>();
		OWLClassExpression left = ((OWLSubClassOfAxiom) a).getSubClass();
		OWLClassExpression right = ((OWLSubClassOfAxiom) a).getSuperClass();
		Set<OWLClassExpression> leftConj = left.asConjunctSet();
		if (!NormalForm.typeOneSubClassAxiom(left, right) || leftConj.size() <= 2) {
			// nothing to do
			res.add(a);
			return res;
		}
		while (true) {
			Iterator<OWLClassExpression> iter = leftConj.iterator();
			OWLClassExpression one = iter.next();
			OWLClassExpression two = iter.next();

			OWLClassExpression newConj = new OWLObjectIntersectionOfImpl(one, two);
			assert (newConj.asConjunctSet().size() == 2);
			if (leftConj.size() == 2) {
				assert (!iter.hasNext());
				OWLAxiom axiom = new OWLSubClassOfAxiomImpl(newConj, right, AnnotateOrigin.getAxiomAnnotations(a));
				res.add(axiom);
				return res;
			}

			OWLClassExpression newAtom = FreshAtoms.createFreshAtomCopy(newConj);
			leftConj.remove(one);
			leftConj.remove(two);
			leftConj.add(newAtom);

			OWLAxiom axiom = new OWLSubClassOfAxiomImpl(newConj, newAtom, AnnotateOrigin.getAxiomAnnotations(a));
			res.add(axiom);
		}
	}

	/**
	 * @param args
	 *            One argument must be given, corresponding to an OWL ontology file
	 *            path. E.g., run with the parameter resources/bodysystem.owl
	 */
	public static void main(String[] args) {
		AppCondorRules mApp = new AppCondorRules(args[0]);

		OWLOntology condor = mApp.runCondor();

		RuleGeneration rgc = new RuleGeneration(condor);

		condor.tboxAxioms(Imports.EXCLUDED).forEach(ax -> {
			System.out.println(rgc.normalizedSubClassAxiomToRule(ax));
		});

		// we write the mappings in to a file
		try {
			FileWriter write = new FileWriter("rules-mappings.txt", false);
			write.append("ENTITIES\n");
			rgc.getMapEntities().entrySet().stream().forEach(e -> {
				try {
					write.append(e.getValue() + "\t\t" + Utils.pretty(e.getKey().toString()) + "\n");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			});
			write.append("AXIOMS GROUPS\n");
			rgc.getMapAxioms().entrySet().stream().forEach(e -> {
				try {
					write.append(e.getValue() + "\t\t");
					e.getKey().forEach(ann -> {
						try {
							write.append(ann.getValue().toString());
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					});
					write.append("\n");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			});
			write.flush();
			write.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
