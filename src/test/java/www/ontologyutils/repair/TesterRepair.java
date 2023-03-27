package www.ontologyutils.repair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

import junit.framework.TestCase;
import uk.ac.manchester.cs.owl.owlapi.OWLClassAssertionAxiomImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl;
import www.ontologyutils.toolbox.Utils;

public class TesterRepair extends TestCase {
    private static final Collection<OWLAnnotation> EMPTY_ANNOTATION = new ArrayList<OWLAnnotation>();
    private static final OWLClassExpression TOP = new OWLDataFactoryImpl().getOWLThing();
    private static final OWLClassExpression BOT = new OWLDataFactoryImpl().getOWLNothing();

    static OWLDataFactory dataFactory = OWLManager.getOWLDataFactory();
    static OWLClassExpression entity1 = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create("www.first.org"));
    static OWLClassExpression entity2 = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create("www.second.org"));
    static OWLClassExpression entity3 = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create("www.third.org"));
    static OWLClassExpression entity4 = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create("www.fourth.org"));

    static OWLIndividual indy1 = dataFactory.getOWLNamedIndividual(IRI.create("www.indy-one.org"));
    static OWLIndividual indy2 = dataFactory.getOWLNamedIndividual(IRI.create("www.indy-two.org"));

    static OWLAxiom ax1 = new OWLSubClassOfAxiomImpl(entity1, entity2, EMPTY_ANNOTATION);
    static OWLAxiom ax2 = new OWLSubClassOfAxiomImpl(entity2, entity3, EMPTY_ANNOTATION);
    static OWLAxiom ax3 = new OWLSubClassOfAxiomImpl(entity3, entity4, EMPTY_ANNOTATION);
    static OWLAxiom ax4 = new OWLSubClassOfAxiomImpl(entity4, entity1, EMPTY_ANNOTATION);
    static OWLAxiom ax5 = new OWLSubClassOfAxiomImpl(TOP, entity1, EMPTY_ANNOTATION);
    static OWLAxiom ax6 = new OWLSubClassOfAxiomImpl(entity4, BOT, EMPTY_ANNOTATION);
    static OWLAxiom ax7 = new OWLClassAssertionAxiomImpl(indy1, entity1, EMPTY_ANNOTATION);

    static Set<OWLAxiom> agenda;

    public TesterRepair(String testName) {
        super(testName);

        agenda = new HashSet<OWLAxiom>();
        agenda.add(ax1);
        agenda.add(ax2);
        agenda.add(ax3);
        agenda.add(ax4);
        agenda.add(ax5);
        agenda.add(ax6);
        agenda.add(ax7);

        System.out.println("AGENDA");
        agenda.stream().forEach(System.out::println);
    }

    public void testIsInconsistent() {
        System.out.println("%%% TEST (IN)CONSISTENCY OF AGENDA");

        assertFalse(Utils.isConsistent(agenda));
    }

    public void testRepairRandomMCS() {
        System.out.println("%%% TEST REPAIR RANDOM MCSs");

        OntologyRepair orrmcs = new OntologyRepairRandomMCS(Utils.newOntology(agenda));
        OWLOntology repair;

        for (int i = 1; i <= 3; i++) {
            repair = orrmcs.repair();

            System.out.println("REPAIRED AGENDA " + i);
            repair.axioms().forEach(System.out::println);
            assertTrue(Utils.isConsistent(repair));
        }
    }

    public void testRepairWeaken() {
        System.out.println("%%% TEST REPAIR WEAKEN");

        Boolean verbose = true;
        OntologyRepair orrmcs = new OntologyRepairWeakening(Utils.newOntology(agenda), verbose);
        OWLOntology repair;

        for (int i = 1; i <= 3; i++) {
            if (verbose) {
                System.out.println("--- Repair " + i);
            }
            repair = orrmcs.repair();

            System.out.println("REPAIRED AGENDA " + i);
            repair.axioms().forEach(System.out::println);
            assertTrue(Utils.isConsistent(repair));
        }
    }
}
