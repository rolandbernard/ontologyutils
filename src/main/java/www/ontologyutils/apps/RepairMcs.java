package www.ontologyutils.apps;

import java.util.*;

import www.ontologyutils.repair.*;
import www.ontologyutils.repair.OntologyRepairRandomMcs.McsComputationStrategy;
import www.ontologyutils.toolbox.Ontology;

/**
 * Repair the given ontology using a random maximal consistent subset.
 */
public class RepairMcs extends RepairApp {
    private boolean coherence = false;
    private McsComputationStrategy mcsComputation = McsComputationStrategy.SOME_MCS;

    @Override
    protected List<Option<?>> appOptions() {
        var options = new ArrayList<Option<?>>();
        options.addAll(super.appOptions());
        options.add(OptionType.FLAG.create("coherence", b -> coherence = true, "make the ontology coherent"));
        options.add(OptionType.options(
                Map.of("one", McsComputationStrategy.ONE_MCS,
                        "some", McsComputationStrategy.SOME_MCS,
                        "all", McsComputationStrategy.ALL_MCS,
                        "sample", McsComputationStrategy.SAMPLE_MCS))
                .create("compute", method -> mcsComputation = method,
                        "how many maximal consistent subsets to compute"));
        return options;
    }

    @Override
    protected OntologyRepair getRepair() {
        if (coherence) {
            return new OntologyRepairRandomMcs(Ontology::isCoherent, mcsComputation);
        } else {
            return new OntologyRepairRandomMcs(Ontology::isConsistent, mcsComputation);
        }
    }

    /**
     * One argument must be given, corresponding to an OWL ontology file path. E.g.,
     * run with the parameter
     * src/test/resources/inconsistent/leftpolicies.owl
     *
     * @param args
     *            Must contain one argument representing the file path of an
     *            ontology.
     */
    public static void main(String[] args) {
        (new RepairMcs()).launch(args);
    }
}
