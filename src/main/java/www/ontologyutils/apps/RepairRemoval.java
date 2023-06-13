package www.ontologyutils.apps;

import java.util.*;

import www.ontologyutils.repair.*;
import www.ontologyutils.repair.OntologyRepairRemoval.BadAxiomStrategy;
import www.ontologyutils.toolbox.Ontology;

/**
 * Repair the given ontology using the axiom removal repair algorithm.
 */
public class RepairRemoval extends RepairApp {
    private boolean coherence = false;
    private BadAxiomStrategy badAxiomStrategy = BadAxiomStrategy.IN_SOME_MUS;

    @Override
    protected List<Option<?>> appOptions() {
        var options = new ArrayList<Option<?>>();
        options.addAll(super.appOptions());
        options.add(
                OptionType.FLAG.create("coherence", b -> coherence = true, "make the ontology coherent"));
        options.add(OptionType.FLAG.create("fast", b -> {
            badAxiomStrategy = BadAxiomStrategy.IN_ONE_MUS;
        }, "use fast methods for selection"));
        options.add(OptionType.options(
                Map.of("one-mus", BadAxiomStrategy.IN_ONE_MUS,
                        "some-mus", BadAxiomStrategy.IN_SOME_MUS,
                        "most-mus", BadAxiomStrategy.IN_MOST_MUS,
                        "least-mcs", BadAxiomStrategy.IN_LEAST_MCS,
                        "largest-mcs", BadAxiomStrategy.NOT_IN_LARGEST_MCS,
                        "one-mcs", BadAxiomStrategy.NOT_IN_ONE_MCS,
                        "some-mcs", BadAxiomStrategy.NOT_IN_SOME_MCS,
                        "random", BadAxiomStrategy.RANDOM))
                .create("bad-axiom", method -> badAxiomStrategy = method,
                        "method for bad axiom selection"));
        return options;
    }

    @Override
    protected OntologyRepair getRepair() {
        return new OntologyRepairRemoval(coherence ? Ontology::isCoherent : Ontology::isConsistent, badAxiomStrategy);
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
        (new RepairRemoval()).launch(args);
    }
}
