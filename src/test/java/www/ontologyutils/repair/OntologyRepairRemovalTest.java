package www.ontologyutils.repair;

import org.junit.jupiter.api.parallel.*;

@Execution(ExecutionMode.CONCURRENT)
public class OntologyRepairRemovalTest extends OntologyRepairTest {
    @Override
    protected OntologyRepair getRepairForConsistency() {
        return OntologyRepairRemoval.forConsistency();
    }

    @Override
    protected OntologyRepair getRepairForCoherence() {
        return OntologyRepairRemoval.forCoherence();
    }
}
