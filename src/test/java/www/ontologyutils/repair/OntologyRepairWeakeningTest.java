package www.ontologyutils.repair;

public class OntologyRepairWeakeningTest extends OntologyRepairTest {
    @Override
    protected OntologyRepair getRepairForConsistency() {
        return OntologyRepairWeakening.forConsistency();
    }
}
