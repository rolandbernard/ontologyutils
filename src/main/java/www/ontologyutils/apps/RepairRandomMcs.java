package www.ontologyutils.apps;

import java.util.*;

import www.ontologyutils.repair.*;

/**
 * Repair the given ontology using a random maximal consistent subset.
 */
public class RepairRandomMcs extends RepairApp {
    private boolean coherence = false;

    @Override
    protected List<Option<?>> appOptions() {
        var options = new ArrayList<Option<?>>();
        options.addAll(super.appOptions());
        options.add(
                OptionType.FLAG.create(null, "coherence", b -> coherence = true, "make the ontology coherent", null));
        return options;
    }

    @Override
    protected OntologyRepair getRepair() {
        if (coherence) {
            return OntologyRepairRandomMcs.forCoherence();
        } else {
            return OntologyRepairRandomMcs.forConsistency();
        }
    }

    @Override
    protected String appName() {
        return "RepairRandomMcs";
    }

    /**
     * One argument must be given, corresponding to an OWL ontology file path. E.g.,
     * run with the parameter
     * src/test/resources/www/ontologyutils/inconsistent-leftpolicies.owl
     *
     * @param args
     *            Must contain one argument representing the file path of an
     *            ontology.
     */
    public static void main(String[] args) {
        (new RepairRandomMcs()).launch(args);
    }
}
