package www.ontologyutils.repair;

import org.junit.jupiter.api.parallel.*;

import www.ontologyutils.repair.OntologyRepairWeakening.*;
import www.ontologyutils.toolbox.Ontology;

@Execution(ExecutionMode.CONCURRENT)
public class OntologyRepairWeakeningSlowTest extends OntologyRepairTest {
    @Override
    protected OntologyRepair getRepairForConsistency() {
        return new OntologyRepairWeakening(
            Ontology::isConsistent, RefOntologyStrategy.RANDOM_MCS, BadAxiomStrategy.IN_LEAST_MCS);
    }

    @Override
    protected OntologyRepair getRepairForCoherence() {
        return new OntologyRepairWeakening(
            Ontology::isCoherent, RefOntologyStrategy.RANDOM_MCS, BadAxiomStrategy.IN_LEAST_MCS);
    }
}
