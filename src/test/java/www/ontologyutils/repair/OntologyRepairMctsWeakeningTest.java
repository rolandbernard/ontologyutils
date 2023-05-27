package www.ontologyutils.repair;

import org.junit.jupiter.api.parallel.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Execution(ExecutionMode.CONCURRENT)
public class OntologyRepairMctsWeakeningTest extends OntologyRepairTest {
    @Override
    protected OntologyRepair getRepairForConsistency() {
        return OntologyRepairMctsWeakening.forConsistency(100);
    }

    @Override
    protected OntologyRepair getRepairForCoherence() {
        return OntologyRepairMctsWeakening.forCoherence(100);
    }

    @Override
    @ParameterizedTest
    @ValueSource(strings = { "/inconsistent/leftpolicies-small.owl", "/inconsistent/leftpolicies.owl" })
    public void repairInconsistentOntologyFromFile(String resourceName) {
        super.repairInconsistentOntologyFromFile(resourceName);
    }

    @ParameterizedTest
    @ValueSource(strings = { "/inconsistent/leftpolicies-small.owl", "/inconsistent/leftpolicies.owl" })
    public void repairWithNormalizationInconsistentOntologyFromFile(String resourceName) {
        super.repairWithNormalizationInconsistentOntologyFromFile(resourceName);
    }
}
