package www.ontologyutils.normalization;

import java.io.File;
import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import www.ontologyutils.toolbox.FreshAtoms;
import www.ontologyutils.toolbox.Utils;

/**
 * Unit tests for Tools.
 */
public class NormalizationsTest extends TestCase {
    File[] FILES = { new File("./resources/catsandnumbers.owl"), new File("./resources/bodysystem.owl"),
            new File("./resources/bfo.owl"), new File("./resources/apo.owl"), new File("./resources/aeo.owl"),
            new File("./resources/duo.owl") };

    /**
     * Create the test case
     *
     * @param testName
     *            name of the test case
     */
    public NormalizationsTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(NormalizationsTest.class);
    }

    public void testNormalizeCondor() throws OWLOntologyCreationException {
        for (File ontologyFile : FILES) {
            FreshAtoms.resetFreshAtomsEquivalenceAxioms();
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(IRI.create(ontologyFile));

            OWLOntology copy = Utils.newEmptyOntology();
            copy.addAxioms(ontology.axioms());

            Stream<OWLAxiom> tBoxAxioms = copy.tboxAxioms(Imports.EXCLUDED);
            tBoxAxioms.forEach((ax) -> {
                copy.remove(ax);
                copy.addAxioms(NormalizationTools.asSubClassOfAxioms(ax));
            });

            // System.out.println("\nCondor Normalized TBox");
            OWLOntology condor = null;
            condor = Normalization.normalizeCondor(copy);
            System.out.println("Testing condor normalization on " + ontology);

            // condor.tboxAxioms(Imports.EXCLUDED).forEach(ax ->
            // System.out.println(Utils.pretty("-- " + ax.toString())));

            // check every axiom of the original ontology is entailed in condor
            OWLReasoner reasoner = Utils.getHermitReasoner(condor);
            assertTrue(ontology.axioms().allMatch(ax -> reasoner.isEntailed(ax)));
            // check every axiom of condor is entailed in the copy of the original ontology
            // with extended signature
            copy.addAxioms(FreshAtoms.getFreshAtomsEquivalenceAxioms());
            OWLReasoner reasonerBis = Utils.getHermitReasoner(copy);
            assertTrue(condor.axioms().allMatch(ax -> reasonerBis.isEntailed(ax)));
        }
    }

    public void testNormalizeNaive() throws OWLOntologyCreationException {
        for (File ontologyFile : FILES) {
            FreshAtoms.resetFreshAtomsEquivalenceAxioms();
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(IRI.create(ontologyFile));

            OWLOntology copy = Utils.newEmptyOntology();
            copy.addAxioms(ontology.axioms());

            Stream<OWLAxiom> tBoxAxioms = copy.tboxAxioms(Imports.EXCLUDED);
            tBoxAxioms.forEach((ax) -> {
                copy.remove(ax);
                copy.addAxioms(NormalizationTools.asSubClassOfAxioms(ax));
            });

            // System.out.println("\nNaive Normalized TBox");
            OWLOntology naive = null;
            naive = Normalization.normalizeNaive(copy);
            System.out.println("Testing naive normalization on " + ontology);

            // naive.tboxAxioms(Imports.EXCLUDED).forEach(ax ->
            // System.out.println(Utils.pretty("-- " + ax.toString())));

            // check every axiom of the original ontology is entailed in naive
            OWLReasoner reasoner = Utils.getHermitReasoner(naive);
            assertTrue(ontology.axioms().allMatch(ax -> reasoner.isEntailed(ax)));
            // check every axiom of naive is entailed in the copy of the original ontology
            // with extended signature
            copy.addAxioms(FreshAtoms.getFreshAtomsEquivalenceAxioms());
            OWLReasoner reasonerBis = Utils.getHermitReasoner(copy);
            assertTrue(naive.axioms().allMatch(ax -> reasonerBis.isEntailed(ax)));
        }
    }
}
