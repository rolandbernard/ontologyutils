package www.ontologyutils.apps;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
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

public class AppSuperNormalize {
	private OWLOntology ontology;
	private String ontologyName;

	public AppSuperNormalize(String ontologyFilePath) {

		File ontologyFile = new File(ontologyFilePath);

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		IRI ontologyIRI = IRI.create(ontologyFile);

		try {
			ontology = manager.loadOntologyFromOntologyDocument(ontologyIRI);
			this.ontologyName = ontology.getOntologyID().getOntologyIRI().get().toString();
			System.out.println("Ontology " + ontologyName + " loaded.");
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
			System.exit(1);
		}
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

		System.out.println("\nCondor Normalized TBox");
		OWLOntology condor = null;
		// condor = Normalization.normalizeCondor(copy);
		condor = superNormalize(Normalization.normalizeCondor(copy));

		condor.tboxAxioms(Imports.EXCLUDED).forEach(ax -> System.out.println(Utils.pretty("-- " + ax.toString())));

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

	private OWLOntology runNaive() {
		FreshAtoms.resetFreshAtomsEquivalenceAxioms(); // optional; for verification purpose

		OWLOntology copy = Utils.newEmptyOntology();
		copy.addAxioms(this.ontology.axioms());

		Stream<OWLAxiom> tBoxAxioms = copy.tboxAxioms(Imports.EXCLUDED);
		tBoxAxioms.forEach((ax) -> {
			copy.remove(ax);
			copy.addAxioms(NormalizationTools.asSubClassOfAxioms(ax));
		});

		System.out.println("\nNaive Normalized TBox");
		OWLOntology naive = null;
		// naive = Normalization.normalizeNaive(copy);
		naive = superNormalize(Normalization.normalizeNaive(copy));

		naive.tboxAxioms(Imports.EXCLUDED).forEach(ax -> System.out.println(Utils.pretty("-- " + ax.toString())));

		// check every axiom of the original ontology is entailed in naive
		OWLReasoner reasoner = Utils.getHermitReasoner(naive);
		assert (this.ontology.axioms().allMatch(ax -> reasoner.isEntailed(ax)));
		// check every axiom of naive is entailed in the copy of the original ontology
		// with extended signature
		copy.addAxioms(FreshAtoms.getFreshAtomsEquivalenceAxioms());
		OWLReasoner reasonerBis = Utils.getHermitReasoner(copy);
		assert (naive.axioms().allMatch(ax -> reasonerBis.isEntailed(ax)));

		return naive;
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
		AppSuperNormalize mApp = new AppSuperNormalize(args[0]);

		System.out.println("\nOriginal TBox");
		Utils.printTBox(mApp.ontology);

		///////////////////////////////////////////////////////////////////////////////////

		System.out.println("\nNAIVE NORMALIZATION");
		OWLOntology naive = mApp.runNaive();

		System.out.println("\nTo rules");
		RuleGeneration rgn = new RuleGeneration(naive);
		rgn.getMapEntities().entrySet().stream().forEach(e -> System.out.println(rgn.entityToRule(e.getKey())));
		naive.tboxAxioms(Imports.EXCLUDED).forEach(ax -> System.out.println(rgn.normalizedSubClassAxiomToRule(ax)));

		System.out.println("\nwhere");
		rgn.getMapEntities().entrySet().stream()
				.forEach(e -> System.out.println(e.getValue() + "\t\t" + Utils.pretty(e.getKey().toString())));

		///////////////////////////////////////////////////////////////////////////////////

		System.out.println("\nCONDOR NORMALIZATION");
		OWLOntology condor = mApp.runCondor();

		System.out.println("\nTo rules");
		RuleGeneration rgc = new RuleGeneration(condor);
		rgc.getMapEntities().entrySet().stream().forEach(e -> System.out.println(rgc.entityToRule(e.getKey())));
		condor.tboxAxioms(Imports.EXCLUDED).forEach(ax -> System.out.println(rgc.normalizedSubClassAxiomToRule(ax)));

		System.out.println("\nwhere");
		rgc.getMapEntities().entrySet().stream()
				.forEach(e -> System.out.println(e.getValue() + "\t\t" + Utils.pretty(e.getKey().toString())));

		///////////////////////////////////////////////////////////////////////////////////

		System.out.println("\nFinished.");
	}
}
