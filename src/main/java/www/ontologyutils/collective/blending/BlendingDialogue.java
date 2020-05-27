package www.ontologyutils.collective.blending;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import www.ontologyutils.collective.PreferenceFactory.Preference;
import www.ontologyutils.refinement.AxiomWeakener;
import www.ontologyutils.toolbox.Utils;

/**
 * @author nico
 *
 */
public class BlendingDialogue {

	private List<OWLAxiom> aone;
	private List<OWLAxiom> atwo;
	private Preference pone;
	private Preference ptwo;
	private OWLOntology initialOntology;

	private boolean verbose = false;

	private void log(String message) {
		if (verbose) {
			System.out.print(message);
		}
	}

	public BlendingDialogue setVerbose(boolean verbose) {
		this.verbose = verbose;
		return this;
	}

	/**
	 * @param aone            a list of axioms
	 * @param pone            a preference over {@code aone} axioms
	 * @param otwo            a list of axioms
	 * @param ptwo            a preference over {@code atwo} axioms
	 * @param initialOntology a consistent ontology
	 */
	public BlendingDialogue(List<OWLAxiom> aone, Preference pone, List<OWLAxiom> atwo, Preference ptwo,
			OWLOntology initialOntology) {
		if (!Utils.isConsistent(initialOntology)) {
			throw new IllegalArgumentException("The initial ontology must be consistent.");
		}
		if (!aone.equals(pone.getAgenda()) || !atwo.equals(ptwo.getAgenda())) {
			throw new IllegalArgumentException("The preferences must be over their respective list of axioms.");
		}
		this.initialOntology = initialOntology;
		this.aone = aone;
		this.atwo = atwo;
		this.pone = pone;
		this.ptwo = ptwo;
	}

	/**
	 * @param axioms  a list of axioms
	 * @param pref    a preference, over at least the axioms in {@ode axioms}, and
	 *                possibly more.
	 * @param context the ontology in which of collectively accepted axioms.
	 * @return the favorite axiom in {@code axioms} that is not yet entailed by
	 *         {@code context}, if any. Otherwise, returns null.
	 */
	private OWLAxiom favorite(List<OWLAxiom> axioms, Preference pref, OWLOntology context) {
		assert (axioms.stream().allMatch(a -> pref.getAgenda().contains(a)));

		OWLAxiom result = null;
		for (OWLAxiom a : axioms.stream().filter(b -> !Utils.isEntailed(context, b, Utils.ReasonerName.HERMIT))
				.collect(Collectors.toList())) {
			if (result == null || pref.prefers(a, result)) {
				result = a;
			}
		}
		return result;
	}

	/**
	 * @param probabilityTurnOne the probability of the first ontology to take turn
	 *                           in the blending dialogue.
	 * @return a consistent ontology resulting from the dialogue.
	 */
	public OWLOntology get(double probabilityTurnOne) {

		OWLOntology result = Utils.newOntology(this.initialOntology.axioms());

		List<OWLAxiom> remainone = new ArrayList<>(this.aone);
		List<OWLAxiom> remaintwo = new ArrayList<>(this.atwo);

		boolean hasFinishedOne = false;
		boolean hasFinishedTwo = false;

		int turn;

		while (!hasFinishedOne || !hasFinishedTwo) {

			if (hasFinishedOne) {
				turn = 2;
			} else if (hasFinishedTwo) {
				turn = 1;
			} else {
				turn = (ThreadLocalRandom.current().nextDouble() <= probabilityTurnOne) ? 1 : 2;
			}
			log("\nTurn: " + turn);

			OWLAxiom consideredAxiom = null;
			if (turn == 1) {
				consideredAxiom = favorite(remainone, this.pone, result);
				if (consideredAxiom == null) {
					log("\nAll axioms considered or already entailed.");
					hasFinishedOne = true;
					continue;
				}
				remainone.remove(consideredAxiom);
			} else {
				assert (turn == 2);
				consideredAxiom = favorite(remaintwo, this.ptwo, result);
				if (consideredAxiom == null) {
					log("\nAll axioms considered or already entailed.");
					hasFinishedTwo = true;
					continue;
				}
				remaintwo.remove(consideredAxiom);
			}
			log("\nConsidering axiom " + Utils.prettyPrintAxiom(consideredAxiom));
			result.add(consideredAxiom);
			while (!Utils.isConsistent(result)) {
				log("\n** Weakening. **");
				result.remove(consideredAxiom);
				AxiomWeakener axiomWeakener = new AxiomWeakener(result);

				Set<OWLAxiom> weakerAxioms = axiomWeakener.getWeakerAxioms(consideredAxiom);

				int randomPick = ThreadLocalRandom.current().nextInt(0, weakerAxioms.size());
				consideredAxiom = (OWLAxiom) (weakerAxioms.toArray())[randomPick];
				result.add(consideredAxiom);
			}
			log("\nAdding axiom: " + Utils.prettyPrintAxiom(consideredAxiom));
		}

		return result;

	}

}
