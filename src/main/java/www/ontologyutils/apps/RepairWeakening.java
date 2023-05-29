package www.ontologyutils.apps;

import java.util.*;

import www.ontologyutils.repair.*;
import www.ontologyutils.repair.OntologyRepairWeakening.*;
import www.ontologyutils.toolbox.Ontology;

/**
 * Repair the given ontology using the axiom weakening repair algorithm.
 */
public class RepairWeakening extends RepairApp {
    private boolean coherence = false;
    RefOntologyStrategy refOntologyStrategy = RefOntologyStrategy.SOME_MCS;
    BadAxiomStrategy badAxiomStrategy = BadAxiomStrategy.IN_SOME_MUS;

    @Override
    protected List<Option<?>> appOptions() {
        var options = new ArrayList<Option<?>>();
        options.addAll(super.appOptions());
        options.add(
                OptionType.FLAG.create(null, "coherence", b -> coherence = true, "make the ontology coherent", null));
        options.add(OptionType.FLAG.create(null, "fast", b -> {
            refOntologyStrategy = RefOntologyStrategy.ONE_MCS;
            badAxiomStrategy = BadAxiomStrategy.IN_ONE_MUS;
        }, "use fast methods for selection", null));
        options.add(OptionType.options(
                Map.of("intersect", RefOntologyStrategy.INTERSECTION_OF_MCS,
                        "intersect-of-some", RefOntologyStrategy.INTERSECTION_OF_SOME_MCS,
                        "largest", RefOntologyStrategy.LARGEST_MCS,
                        "any", RefOntologyStrategy.ONE_MCS,
                        "random", RefOntologyStrategy.RANDOM_MCS,
                        "random-of-some", RefOntologyStrategy.SOME_MCS))
                .create(null, "ref-ontology", method -> {
                    refOntologyStrategy = method;
                }, "method for reference ontology selection", null));
        options.add(OptionType.options(
                Map.of("least-mcs", BadAxiomStrategy.IN_LEAST_MCS,
                        "one-mus", BadAxiomStrategy.IN_ONE_MUS,
                        "some-mus", BadAxiomStrategy.IN_SOME_MUS,
                        "largest-mcs", BadAxiomStrategy.NOT_IN_LARGEST_MCS,
                        "one-mcs", BadAxiomStrategy.NOT_IN_ONE_MCS,
                        "some-mcs", BadAxiomStrategy.NOT_IN_SOME_MCS,
                        "random", BadAxiomStrategy.RANDOM))
                .create(null, "bad-axiom", method -> {
                    badAxiomStrategy = method;
                }, "method for bad axiom selection", null));
        return options;
    }

    @Override
    protected OntologyRepair getRepair() {
        return new OntologyRepairWeakening(coherence ? Ontology::isCoherent : Ontology::isConsistent,
                refOntologyStrategy, badAxiomStrategy);
    }

    @Override
    protected String appName() {
        return "RepairWeakening";
    }

    /**
     * One argument must be given, corresponding to an OWL ontology file path. E.g.,
     * run with the parameter
     * src/test/resources/www/ontologyutils/inconsistent-leftpolicies.owl
     *
     * @param args
     *            Must contain one or two argument representing the keyword "fast"
     *            or file path of an ontology.
     */
    public static void main(String[] args) {
        (new RepairWeakening()).launch(args);
    }
}
