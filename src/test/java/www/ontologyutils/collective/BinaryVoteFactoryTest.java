package www.ontologyutils.collective;

import java.util.*;

import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.model.*;

import www.ontologyutils.toolbox.Ontology;

public class BinaryVoteFactoryTest {
    static OWLDataFactory df = Ontology.getDefaultDataFactory();
    static OWLClassExpression entity1 = (OWLClassExpression) df.getOWLEntity(EntityType.CLASS,
            IRI.create("www.first.org"));
    static OWLClassExpression entity2 = df.getOWLEntity(EntityType.CLASS, IRI.create("www.second.org"));
    static OWLClassExpression entity3 = df.getOWLEntity(EntityType.CLASS, IRI.create("www.third.org"));
    static OWLClassExpression entity4 = df.getOWLEntity(EntityType.CLASS, IRI.create("www.fourth.org"));

    static OWLIndividual indy1 = df.getOWLNamedIndividual(IRI.create("www.indy-one.org"));
    static OWLIndividual indy2 = df.getOWLNamedIndividual(IRI.create("www.indy-two.org"));

    static OWLAxiom ax1 = df.getOWLSubClassOfAxiom(entity1, entity2);
    static OWLAxiom ax2 = df.getOWLSubClassOfAxiom(entity2, entity3);
    static OWLAxiom ax3 = df.getOWLSubClassOfAxiom(entity3, entity4);
    static OWLAxiom ax4 = df.getOWLSubClassOfAxiom(entity4, entity1);
    static OWLAxiom ax5 = df.getOWLSubClassOfAxiom(df.getOWLThing(), entity1);
    static OWLAxiom ax6 = df.getOWLSubClassOfAxiom(entity4, df.getOWLNothing());
    static OWLAxiom ax7 = df.getOWLClassAssertionAxiom(entity1, indy1);
    static OWLAxiom ax8 = df.getOWLClassAssertionAxiom(entity2, indy2);
    static OWLAxiom ax9 = df.getOWLSubClassOfAxiom(entity2, df.getOWLNothing());

    static BinaryVoteFactory bvFactory;
    static ArrayList<OWLAxiom> agenda;

    public BinaryVoteFactoryTest() {
        agenda = new ArrayList<OWLAxiom>();
        agenda.add(ax1);
        agenda.add(ax2);
        agenda.add(ax3);
        agenda.add(ax4);
        agenda.add(ax5);
        agenda.add(ax6);
        agenda.add(ax7);
        agenda.add(ax8);
        agenda.add(ax9);
        bvFactory = new BinaryVoteFactory(agenda);
    }

    @Test
    public void testRandomVote() {
        int NUM_ITERATIONS = 10;
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            bvFactory.makeRandomBinaryVote();
        }
    }

    @Test
    public void testRandomVoteSmallPositivity() {
        int NUM_ITERATIONS = 10;
        float positivity = 0.1f;
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            bvFactory.makeRandomBinaryVote(positivity);
        }
    }

    @Test
    public void testRandomVoteLargePositivity() {
        int NUM_ITERATIONS = 10;
        float positivity = 0.9f;
        for (int i = 0; i < NUM_ITERATIONS; i++) {
            bvFactory.makeRandomBinaryVote(positivity);
        }
    }
}
