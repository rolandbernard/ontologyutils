package www.ontologyutils.repair;

public class OntologyRepairRandomMcsTest extends OntologyRepairTest {
    @Override
    protected OntologyRepair getRepairForConsistency() {
        return OntologyRepairRandomMcs.forConsistency();
    }
}
