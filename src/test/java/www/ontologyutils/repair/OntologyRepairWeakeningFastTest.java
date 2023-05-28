package www.ontologyutils.repair;

import org.junit.jupiter.api.parallel.*;

import www.ontologyutils.repair.OntologyRepairWeakening.*;
import www.ontologyutils.toolbox.Ontology;

@Execution(ExecutionMode.CONCURRENT)
public class OntologyRepairWeakeningFastTest extends OntologyRepairTest {
    @Override
    protected OntologyRepair getRepairForConsistency() {
        return new OntologyRepairWeakening(
            Ontology::isConsistent, RefOntologyStrategy.ONE_MCS, BadAxiomStrategy.IN_ONE_MUS);
    }

    @Override
    protected OntologyRepair getRepairForCoherence() {
        return new OntologyRepairWeakening(
            Ontology::isCoherent, RefOntologyStrategy.ONE_MCS, BadAxiomStrategy.IN_ONE_MUS);
    }
}
