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
 * 
 * Guendalina Righetti, Daniele Porello, Nicolas Troquard, Oliver Kutz, Maria M.
 * Hedblom, Pietro Galliani. Asymmetric Hybrids: Dialogues for Computational
 * Concept Combination. 12th International Conference on Formal Ontology in
 * Information Systems (FOIS 2021). IOS Press.
 *
 */
public class BlendingDialogue {

	public static int NO_TURN_LIMIT = -1;

	private List<OWLAxiom> aone;
	private List<OWLAxiom> atwo;
	private Preference pone;
	private Preference ptwo;
	private OWLOntology initialOntology;
	private int numWeakeningOne;
	private int numWeakeningTwo;

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
		this.numWeakeningOne = 0;
		this.numWeakeningTwo = 0;
	}

	/**
	 * @return the number of axiom weakening action performed by ontology one since
	 *         the last call to {@code get}.
	 * @see get()
	 */
	public int getNumWeakeningOne() {
		return numWeakeningOne;
	}

	/**
	 * @return the number of axiom weakening action performed by ontology two since
	 *         the last call to {@code get}.
	 * @see get()
	 */
	public int getNumWeakeningTwo() {
		return numWeakeningTwo;
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
		for (OWLAxiom a : axioms.stream().filter(b -> !Utils.isEntailed(context, b)).collect(Collectors.toList())) {
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
	/**
	 * @param probabilityTurnOne the probability of the first ontology to take turn
	 *                           in the blending dialogue.
	 * @param maxTurns           number of maximum turns of dialogue. Use
	 *                           {@link NO_TURN_LIMIT} for no limits.
	 * @return a consistent ontology resulting from the dialogue.
	 */
	public OWLOntology get(double probabilityTurnOne, int maxTurns) {

		this.numWeakeningOne = 0;
		this.numWeakeningTwo = 0;

		OWLOntology result = Utils.newOntology(this.initialOntology.axioms());

		List<OWLAxiom> remainone = new ArrayList<>(this.aone);
		List<OWLAxiom> remaintwo = new ArrayList<>(this.atwo);

		boolean hasFinishedOne = false;
		boolean hasFinishedTwo = false;

		int numTurns = 0; // turns so far
		int turnAgent; // will contain 1 or 2, referring to the agent whose turn it is

		while (!hasFinishedOne || !hasFinishedTwo) {
			if (maxTurns != NO_TURN_LIMIT && ++numTurns > maxTurns)
				break;

			if (hasFinishedOne) {
				turnAgent = 2;
			} else if (hasFinishedTwo) {
				turnAgent = 1;
			} else {
				turnAgent = (ThreadLocalRandom.current().nextDouble() <= probabilityTurnOne) ? 1 : 2;
			}
			log("\nTurn: " + turnAgent);

			OWLAxiom consideredAxiom = null;
			if (turnAgent == 1) {
				consideredAxiom = favorite(remainone, this.pone, result);
				if (consideredAxiom == null) {
					log("\nAll axioms considered or already entailed.");
					hasFinishedOne = true;
					continue;
				}
				remainone.remove(consideredAxiom);
			} else {
				assert (turnAgent == 2);
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
				if (turnAgent == 1) {
					numWeakeningOne++;
				} else {
					numWeakeningTwo++;
				}
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

	/**
	 * @param probabilityTurnOne the probability of the first ontology to take turn
	 *                           in the blending dialogue.
	 * @return a consistent ontology resulting from the dialogue.
	 */
	public OWLOntology get(double probabilityTurnOne) {
		return this.get(probabilityTurnOne, NO_TURN_LIMIT);
	}

}
