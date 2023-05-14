package www.ontologyutils.repair;

public class OntologyRepairBestMcsTest extends OntologyRepairTest {
    @Override
    protected OntologyRepair getRepairForConsistency() {
        return OntologyRepairBestMcs.forConsistency();
    }
}
