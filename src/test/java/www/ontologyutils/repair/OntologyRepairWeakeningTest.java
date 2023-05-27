package www.ontologyutils.repair;

import org.junit.jupiter.api.parallel.*;

@Execution(ExecutionMode.CONCURRENT)
public class OntologyRepairWeakeningTest extends OntologyRepairTest {
    @Override
    protected OntologyRepair getRepairForConsistency() {
        return OntologyRepairWeakening.forConsistency();
    }

    @Override
    protected OntologyRepair getRepairForCoherence() {
        return OntologyRepairWeakening.forCoherence();
    }
}
