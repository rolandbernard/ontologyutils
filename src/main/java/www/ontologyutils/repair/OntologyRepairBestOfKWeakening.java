package www.ontologyutils.repair;

import java.util.*;
import java.util.function.*;
import java.util.stream.IntStream;

import org.semanticweb.owlapi.model.OWLAxiom;

import www.ontologyutils.refinement.AxiomWeakener;
import www.ontologyutils.toolbox.*;

/**
 * An implementation of {@code OntologyRepair}. It repairs an inconsistent
 * ontology by choosing the best repair found withing k weakening repairs.
 * For efficiency, all repairs are performed using the same reference ontology.
 */
public class OntologyRepairBestOfKWeakening extends OntologyRepairWeakening {
    /**
     * The function to use as a quality measure.
     */
    protected Function<Ontology, Double> quality;
    /**
     * The number of rounds of repairs that should be performed.
     */
    protected int numberOfRounds;

    /**
     * @param isRepaired
     *            The predicate testing whether an ontology is repaired.
     * @param refOntologySource
     *            The strategy for computing the reference ontology.
     * @param badAxiomSource
     *            The strategy for computing bad axioms.
     * @param quality
     *            Function for evaluating the quality of a repair.
     * @param numberOfRounds
     *            The number of repairs to perform.
     */
    public OntologyRepairBestOfKWeakening(Predicate<Ontology> isRepaired, RefOntologyStrategy refOntologySource,
            BadAxiomStrategy badAxiomSource, Function<Ontology, Double> quality, int numberOfRounds) {
        super(isRepaired, refOntologySource, badAxiomSource);
        this.quality = quality;
        this.numberOfRounds = numberOfRounds;
    }

    /**
     * @param isRepaired
     *            The predicate testing whether an ontology is repaired.
     * @param numberOfRounds
     *            The number of repairs to perform.
     */
    public OntologyRepairBestOfKWeakening(Predicate<Ontology> isRepaired, int numberOfRounds) {
        this(isRepaired, RefOntologyStrategy.INTERSECTION_OF_SOME_MCS, BadAxiomStrategy.IN_ONE_MUS,
                o -> (double) o.inferredTaxonomyAxioms().count(), numberOfRounds);
    }

    /**
     * @param numberOfRounds
     *            The number of repairs to perform.
     * @return An instance of {@code OntologyRepairBestOfKWeakening} that tries to
     *         make the ontology consistent.
     */
    public static OntologyRepair forConsistency(int numberOfRounds) {
        return new OntologyRepairBestOfKWeakening(Ontology::isConsistent, numberOfRounds);
    }

    /**
     * @param numberOfRounds
     *            The number of repairs to perform.
     * @return An instance of {@code OntologyRepairBestOfKWeakening} that tries to
     *         make the ontology coherent.
     */
    public static OntologyRepair forCoherence(int numberOfRounds) {
        return new OntologyRepairBestOfKWeakening(isCoherent(), numberOfRounds);
    }

    @Override
    public void repair(Ontology ontology) {
        var refAxioms = Utils.randomChoice(getRefAxioms(ontology));
        infoMessage("Selected a reference ontology with " + refAxioms.size() + " axioms.");
        var bestAxioms = Set.<OWLAxiom>of();
        checkpoint(ontology);
        try (var refOntology = ontology.cloneWithRefutable(refAxioms)) {
            var axiomWeakener = new AxiomWeakener(refOntology, ontology);
            bestAxioms = IntStream.range(0, numberOfRounds)
                    .mapToObj(k -> {
                        try (var copy = ontology.clone()) {
                            while (!isRepaired(copy)) {
                                var badAxioms = Utils.toList(findBadAxioms(copy));
                                var badAxiom = Utils.randomChoice(badAxioms);
                                var weakerAxioms = Utils.toList(axiomWeakener.weakerAxioms(badAxiom));
                                var weakerAxiom = Utils.randomChoice(weakerAxioms);
                                copy.replaceAxiom(badAxiom, weakerAxiom);
                            }
                            infoMessage("Found a repair.");
                            return new AbstractMap.SimpleEntry<>(Utils.toSet(copy.refutableAxioms()), quality.apply(copy));
                        }
                    })
                    .max(Comparator.comparingDouble(e -> e.getValue()))
                    .get().getKey();
        }
        ontology.setRefutableAxioms(bestAxioms);
    }
}
