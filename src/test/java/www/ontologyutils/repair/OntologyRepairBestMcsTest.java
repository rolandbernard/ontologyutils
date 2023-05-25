package www.ontologyutils.repair;

import org.junit.jupiter.api.parallel.*;

@Execution(ExecutionMode.CONCURRENT)
public class OntologyRepairBestMcsTest extends OntologyRepairTest {
    @Override
    protected OntologyRepair getRepairForConsistency() {
        return OntologyRepairBestMcs.forConsistency();
    }
}
