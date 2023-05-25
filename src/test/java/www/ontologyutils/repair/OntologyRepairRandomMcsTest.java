package www.ontologyutils.repair;

import org.junit.jupiter.api.parallel.*;

@Execution(ExecutionMode.CONCURRENT)
public class OntologyRepairRandomMcsTest extends OntologyRepairTest {
    @Override
    protected OntologyRepair getRepairForConsistency() {
        return OntologyRepairRandomMcs.forConsistency();
    }
}
